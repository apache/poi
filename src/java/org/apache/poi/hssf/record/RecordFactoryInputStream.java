/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hssf.record;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;

/**
 * A stream based way to get at complete records, with
 * as low a memory footprint as possible.
 * This handles reading from a RecordInputStream, turning
 * the data into full records, processing continue records
 * etc.
 * Most users should use {@link HSSFEventFactory} /
 * {@link HSSFListener} and have new records pushed to
 * them, but this does allow for a "pull" style of coding.
 */
public class RecordFactoryInputStream {

	private final RecordInputStream _recStream;
	private final boolean _shouldIncludeContinueRecords;

	/**
	 * Temporarily stores a group of {@link NumberRecord}s.  This is uses when the most
	 * recently read underlying record is a {@link MulRKRecord}
	 */
	private NumberRecord[] _multipleNumberRecords;

	/**
	 * used to help iterating over multiple number records
	 */
	private int _multipleNumberRecordIndex = -1;

	/**
	 * The most recent record that we gave to the user
	 */
	private Record _lastRecord = null;
	/**
	 * The most recent DrawingRecord seen
	 */
	private DrawingRecord _lastDrawingRecord = new DrawingRecord();

	private int _bofDepth;

	private boolean _lastRecordWasEOFLevelZero;


	/**
	 * @param shouldIncludeContinueRecords caller can pass <code>false</code> if loose
	 * {@link ContinueRecord}s should be skipped (this is sometimes useful in event based
	 * processing).
	 */
	public RecordFactoryInputStream(RecordInputStream inp, boolean shouldIncludeContinueRecords) {
		_recStream = inp;
		_shouldIncludeContinueRecords = shouldIncludeContinueRecords;

		/*
		* How to recognise end of stream?
		* In the best case, the underlying input stream (in) ends just after the last EOF record
		* Usually however, the stream is padded with an arbitrary byte count.  Excel and most apps
		* reliably use zeros for padding and if this were always the case, this code could just
		* skip all the (zero sized) records with sid==0.  However, bug 46987 shows a file with
		* non-zero padding that is read OK by Excel (Excel also fixes the padding).
		*
		* So to properly detect the workbook end of stream, this code has to identify the last
		* EOF record.  This is not so easy because the worbook bof+eof pair do not bracket the
		* whole stream.  The worksheets follow the workbook, but it is not easy to tell how many
		* sheet sub-streams should be present.  Hence we are looking for an EOF record that is not
		* immediately followed by a BOF record.  One extra complication is that bof+eof sub-
		* streams can be nested within worksheet streams and it's not clear in these cases what
		* record might follow any EOF record.  So we also need to keep track of the bof/eof
		* nesting level.
		*/
		_bofDepth=0;
		_lastRecordWasEOFLevelZero = false;
	}

	/**
	 * Returns the next (complete) record from the
	 * stream, or null if there are no more.
	 */
	public Record nextRecord() {
		Record r;
		r = getNextMultipleNumberRecord();
		if (r != null) {
			// found a NumberRecord (expanded from a recent MULRK record)
			return r;
		}
		while (true) {
			if (!_recStream.hasNextRecord()) {
				// recStream is exhausted;
	    		return null;
			}

			// step underlying RecordInputStream to the next record
			_recStream.nextRecord();

			if (_lastRecordWasEOFLevelZero) {
				// Potential place for ending the workbook stream
				// Check that the next record is not BOFRecord(0x0809)
				// Normally the input stream contains only zero padding after the last EOFRecord,
				// but bug 46987 suggests that the padding may be garbage.
				// This code relies on the padding bytes not starting with BOFRecord.sid
				if (_recStream.getSid() != BOFRecord.sid) {
					return null;
				}
				// else - another sheet substream starting here
			}

			r = readNextRecord();
			if (r == null) {
				// some record types may get skipped (e.g. DBCellRecord and ContinueRecord)
				continue;
			}
			return r;
		}
	}

	/**
	 * @return the next {@link NumberRecord} from the multiple record group as expanded from
	 * a recently read {@link MulRKRecord}. <code>null</code> if not present.
	 */
	private NumberRecord getNextMultipleNumberRecord() {
		if (_multipleNumberRecords != null) {
			int ix = _multipleNumberRecordIndex;
			if (ix < _multipleNumberRecords.length) {
				NumberRecord result = _multipleNumberRecords[ix];
				_multipleNumberRecordIndex = ix + 1;
				return result;
			}
			_multipleNumberRecordIndex = -1;
			_multipleNumberRecords = null;
		}
		return null;
	}

	/**
	 * @return the next available record, or <code>null</code> if
	 * this pass didn't return a record that's
	 * suitable for returning (eg was a continue record).
	 */
	private Record readNextRecord() {

		Record record = RecordFactory.createSingleRecord(_recStream);
		_lastRecordWasEOFLevelZero = false;

		if (record instanceof BOFRecord) {
			_bofDepth++;
			return record;
		}

		if (record instanceof EOFRecord) {
			_bofDepth--;
			if (_bofDepth < 1) {
				_lastRecordWasEOFLevelZero = true;
			}

			return record;
		}

		if (record instanceof DBCellRecord) {
			// Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
			return null;
		}

		if (record instanceof RKRecord) {
			return RecordFactory.convertToNumberRecord((RKRecord) record);
		}

		if (record instanceof MulRKRecord) {
			NumberRecord[] records = RecordFactory.convertRKRecords((MulRKRecord) record);

			_multipleNumberRecords = records;
			_multipleNumberRecordIndex = 1;
			return records[0];
		}

		if (record.getSid() == DrawingGroupRecord.sid
				&& _lastRecord instanceof DrawingGroupRecord) {
			DrawingGroupRecord lastDGRecord = (DrawingGroupRecord) _lastRecord;
			lastDGRecord.join((AbstractEscherHolderRecord) record);
			return null;
		}
		if (record.getSid() == ContinueRecord.sid) {
			ContinueRecord contRec = (ContinueRecord) record;

			if (_lastRecord instanceof ObjRecord || _lastRecord instanceof TextObjectRecord) {
				// Drawing records have a very strange continue behaviour.
				//There can actually be OBJ records mixed between the continues.
				_lastDrawingRecord.processContinueRecord(contRec.getData());
				//we must remember the position of the continue record.
				//in the serialization procedure the original structure of records must be preserved
				if (_shouldIncludeContinueRecords) {
					return record;
				}
				return null;
			}
			if (_lastRecord instanceof DrawingGroupRecord) {
				((DrawingGroupRecord) _lastRecord).processContinueRecord(contRec.getData());
				return null;
			}
			if (_lastRecord instanceof DrawingRecord) {
				((DrawingRecord) _lastRecord).processContinueRecord(contRec.getData());
				return null;
			}
			if (_lastRecord instanceof UnknownRecord) {
				//Gracefully handle records that we don't know about,
				//that happen to be continued
				return record;
			}
			if (_lastRecord instanceof EOFRecord) {
				// This is really odd, but excel still sometimes
				//  outputs a file like this all the same
				return record;
			}
			throw new RecordFormatException("Unhandled Continue Record");
		}
		_lastRecord = record;
		if (record instanceof DrawingRecord) {
			_lastDrawingRecord = (DrawingRecord) record;
		}
		return record;
	}
}
