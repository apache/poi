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

package org.apache.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.hssf.record.PaneRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SelectionRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;

/**
 * Manages the DVALRecord and DVRecords for a single sheet<br/>
 * See OOO excelfileformat.pdf section 4.14
 * @author Josh Micich
 */
public final class DataValidityTable extends RecordAggregate {

	private static final short sid = -0x01B2; // not a real record
	private final DVALRecord _headerRec;
	/**
	 * The list of data validations for the current sheet.
	 * Note - this may be empty (contrary to OOO documentation)
	 */
	private final List _validationList;

	public DataValidityTable(RecordStream rs) {
		_headerRec = (DVALRecord) rs.getNext();
		List temp = new ArrayList();
		while (rs.peekNextClass() == DVRecord.class) {
			temp.add(rs.getNext());
		}
		_validationList = temp;
	}

	private DataValidityTable() {
		_headerRec = new DVALRecord();
		_validationList = new ArrayList();
	}

	public short getSid() {
		return sid;
	}

	public int serialize(int offset, byte[] data) {
		int result = _headerRec.serialize(offset, data);
		for (int i = 0; i < _validationList.size(); i++) {
			result += ((Record) _validationList.get(i)).serialize(offset + result, data);
		}
		return result;
	}

	public int getRecordSize() {
		int result = _headerRec.getRecordSize();
		for (int i = _validationList.size() - 1; i >= 0; i--) {
			result += ((Record) _validationList.get(i)).getRecordSize();
		}
		return result;
	}

	/**
	 * Creates a new <tt>DataValidityTable</tt> and inserts it in the right
	 * place in the sheetRecords list.
	 */
	public static DataValidityTable createForSheet(List sheetRecords) {
		int index = findDVTableInsertPos(sheetRecords);

		DataValidityTable result = new DataValidityTable();
		sheetRecords.add(index, result);
		return result;
	}
	
    /**
     * Finds the index where the sheet validations header record should be inserted
     * @param records the records for this sheet
     * 
     * + WINDOW2
     * o SCL
     * o PANE
     * oo SELECTION
     * o STANDARDWIDTH
     * oo MERGEDCELLS
     * o LABELRANGES
     * o PHONETICPR
     * o Conditional Formatting Table
     * o Hyperlink Table
     * o Data Validity Table
     * o SHEETLAYOUT
     * o SHEETPROTECTION
     * o RANGEPROTECTION
     * + EOF
     */
    private static int findDVTableInsertPos(List records) {
		int i = records.size() - 1;
		if (!(records.get(i) instanceof EOFRecord)) {
			throw new IllegalStateException("Last sheet record should be EOFRecord");
		}
		while (i > 0) {
			i--;
			Record rec = (Record) records.get(i);
			if (isPriorRecord(rec.getSid())) {
				Record nextRec = (Record) records.get(i + 1);
				if (!isSubsequentRecord(nextRec.getSid())) {
					throw new IllegalStateException("Unexpected (" + nextRec.getClass().getName()
							+ ") found after (" + rec.getClass().getName() + ")");
				}
				return i;
			}
			if (!isSubsequentRecord(rec.getSid())) {
				throw new IllegalStateException("Unexpected (" + rec.getClass().getName()
						+ ") while looking for DV Table insert pos");
			}
		}
		return 0;
	}

	// TODO - add UninterpretedRecord as base class for many of these
	// unimplemented sids

	private static boolean isPriorRecord(short sid) {
		switch(sid) {
			case WindowTwoRecord.sid:
			case 0x00A0: // SCL
			case PaneRecord.sid:
			case SelectionRecord.sid:
			case 0x0099: // STANDARDWIDTH
			case MergeCellsRecord.sid:
			case 0x015F: // LABELRANGES
			case 0x00EF: // PHONETICPR
			case CFHeaderRecord.sid:
			case CFRuleRecord.sid:
			case HyperlinkRecord.sid:
			case 0x0800: // QUICKTIP
				return true;
		}
		return false;
	}

	private static boolean isSubsequentRecord(short sid) {
		switch(sid) {
			case 0x0862: // SHEETLAYOUT
			case 0x0867: // SHEETPROTECTION
			case 0x0868: // RANGEPROTECTION
			case EOFRecord.sid:
				return true;
		}
		return false;
	}

	public void addDataValidation(DVRecord dvRecord) {
		_validationList.add(dvRecord);
		_headerRec.setDVRecNo(_validationList.size());
	}
}
