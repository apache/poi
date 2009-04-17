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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.record.chart.*;
import org.apache.poi.hssf.record.pivottable.*;
import org.apache.poi.util.HexDump;

/**
 * Title:  Record Factory<P>
 * Description:  Takes a stream and outputs an array of Record objects.<P>
 *
 * @see org.apache.poi.hssf.eventmodel.EventRecordFactory
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
public final class RecordFactory {
	private static final int NUM_RECORDS = 512;
	
	private interface I_RecordCreator {
		Record create(RecordInputStream in);

		String getRecordClassName();
	}
	private static final class ReflectionRecordCreator implements I_RecordCreator {

		private final Constructor<? extends Record> _c;
		public ReflectionRecordCreator(Constructor<? extends Record> c) {
			_c = c;
		}
		public Record create(RecordInputStream in) {
			Object[] args = { in, };
			try {
				return _c.newInstance(args);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RecordFormatException("Unable to construct record instance" , e.getTargetException());
			}
		}
		public String getRecordClassName() {
			return _c.getDeclaringClass().getName();
		}
	}


	private static final Class<?>[] CONSTRUCTOR_ARGS = { RecordInputStream.class, };

	/**
	 * contains the classes for all the records we want to parse.<br/>
	 * Note - this most but not *every* subclass of Record.
	 */
	@SuppressWarnings("unchecked")
	private static final Class<? extends Record>[] recordClasses = new Class[] {
		ArrayRecord.class,
		BackupRecord.class,
		BlankRecord.class,
		BOFRecord.class,
		BookBoolRecord.class,
		BoolErrRecord.class,
		BottomMarginRecord.class,
		BoundSheetRecord.class,
		CalcCountRecord.class,
		CalcModeRecord.class,
		CFHeaderRecord.class,
		CFRuleRecord.class,
		ChartRecord.class,
		ChartTitleFormatRecord.class,
		CodepageRecord.class,
		ColumnInfoRecord.class,
		ContinueRecord.class,
		CountryRecord.class,
		CRNCountRecord.class,
		CRNRecord.class,
		DateWindow1904Record.class,
		DBCellRecord.class,
		DefaultColWidthRecord.class,
		DefaultRowHeightRecord.class,
		DeltaRecord.class,
		DimensionsRecord.class,
		DrawingGroupRecord.class,
		DrawingRecord.class,
		DrawingSelectionRecord.class,
		DSFRecord.class,
		DVALRecord.class,
		DVRecord.class,
		EOFRecord.class,
		ExtendedFormatRecord.class,
		ExternalNameRecord.class,
		ExternSheetRecord.class,
		ExtSSTRecord.class,
		FilePassRecord.class,
		FileSharingRecord.class,
		FnGroupCountRecord.class,
		FontRecord.class,
		FooterRecord.class,
		FormatRecord.class,
		FormulaRecord.class,
		GridsetRecord.class,
		GutsRecord.class,
		HCenterRecord.class,
		HeaderRecord.class,
		HideObjRecord.class,
		HorizontalPageBreakRecord.class,
		HyperlinkRecord.class,
		IndexRecord.class,
		InterfaceEndRecord.class,
		InterfaceHdrRecord.class,
		IterationRecord.class,
		LabelRecord.class,
		LabelSSTRecord.class,
		LeftMarginRecord.class,
		LegendRecord.class,
		MergeCellsRecord.class,
		MMSRecord.class,
		MulBlankRecord.class,
		MulRKRecord.class,
		NameRecord.class,
		NoteRecord.class,
		NumberRecord.class,
		ObjectProtectRecord.class,
		ObjRecord.class,
		PaletteRecord.class,
		PaneRecord.class,
		PasswordRecord.class,
		PasswordRev4Record.class,
		PrecisionRecord.class,
		PrintGridlinesRecord.class,
		PrintHeadersRecord.class,
		PrintSetupRecord.class,
		ProtectionRev4Record.class,
		ProtectRecord.class,
		RecalcIdRecord.class,
		RefModeRecord.class,
		RefreshAllRecord.class,
		RightMarginRecord.class,
		RKRecord.class,
		RowRecord.class,
		SaveRecalcRecord.class,
		ScenarioProtectRecord.class,
		SelectionRecord.class,
		SeriesRecord.class,
		SeriesTextRecord.class,
		SharedFormulaRecord.class,
		SSTRecord.class,
		StringRecord.class,
		StyleRecord.class,
		SupBookRecord.class,
		TabIdRecord.class,
		TableRecord.class,
		TableStylesRecord.class,
		TextObjectRecord.class,
		TopMarginRecord.class,
		UncalcedRecord.class,
		UseSelFSRecord.class,
		VCenterRecord.class,
		VerticalPageBreakRecord.class,
		WindowOneRecord.class,
		WindowProtectRecord.class,
		WindowTwoRecord.class,
		WriteAccessRecord.class,
		WriteProtectRecord.class,
		WSBoolRecord.class,
		
		// chart records
		BeginRecord.class,
		ChartFRTInfoRecord.class,
		ChartStartBlockRecord.class,
		ChartEndBlockRecord.class,
		ChartStartObjectRecord.class,
		ChartEndObjectRecord.class,
		CatLabRecord.class,
		EndRecord.class,
		LinkedDataRecord.class,
		SeriesToChartGroupRecord.class,
		
		// pivot table records
		DataItemRecord.class,
		ExtendedPivotTableViewFieldsRecord.class,
		PageItemRecord.class,
		StreamIDRecord.class,
		ViewDefinitionRecord.class, 
		ViewFieldsRecord.class,
		ViewSourceRecord.class,
	};
	
	/**
	 * cache of the recordsToMap();
	 */
	private static Map<Short, I_RecordCreator> recordsMap  = recordsToMap(recordClasses);

	private static short[] _allKnownRecordSIDs;
	
	/**
	 * create a record, if there are MUL records than multiple records
	 * are returned digested into the non-mul form.
	 */
	public static Record [] createRecord(RecordInputStream in) {
		
		Record record = createSingleRecord(in);
		if (record instanceof DBCellRecord) {
			// Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
			return new Record[] { null, };
		}
		if (record instanceof RKRecord) {
			return new Record[] { convertToNumberRecord((RKRecord) record), };
		}
		if (record instanceof MulRKRecord) {
			return convertRKRecords((MulRKRecord)record);
		}
		return new Record[] { record, };
	}
	
	static Record createSingleRecord(RecordInputStream in) {
		I_RecordCreator constructor = recordsMap.get(new Short(in.getSid()));

		if (constructor == null) {
			return new UnknownRecord(in);
		}
		
		return constructor.create(in);
	}

	/**
	 * RK record is a slightly smaller alternative to NumberRecord
	 * POI likes NumberRecord better
	 */
	private static NumberRecord convertToNumberRecord(RKRecord rk) {
		NumberRecord num = new NumberRecord();

		num.setColumn(rk.getColumn());
		num.setRow(rk.getRow());
		num.setXFIndex(rk.getXFIndex());
		num.setValue(rk.getRKNumber());
		return num;
	}

	/**
	 * Converts a {@link MulRKRecord} into an equivalent array of {@link NumberRecord}s
	 */
	private static NumberRecord[] convertRKRecords(MulRKRecord mrk) {

		NumberRecord[] mulRecs = new NumberRecord[mrk.getNumColumns()];
		for (int k = 0; k < mrk.getNumColumns(); k++) {
			NumberRecord nr = new NumberRecord();

			nr.setColumn((short) (k + mrk.getFirstColumn()));
			nr.setRow(mrk.getRow());
			nr.setXFIndex(mrk.getXFAt(k));
			nr.setValue(mrk.getRKNumberAt(k));
			mulRecs[k] = nr;
		}
		return mulRecs;
	}

	/**
	 * @return an array of all the SIDS for all known records
	 */
	public static short[] getAllKnownRecordSIDs() {
		if (_allKnownRecordSIDs == null) {
			short[] results = new short[ recordsMap.size() ];
			int i = 0;

			for (Iterator<Short> iterator = recordsMap.keySet().iterator(); iterator.hasNext(); ) {
				Short sid = iterator.next();

				results[i++] = sid.shortValue();
			}
			Arrays.sort(results);
 			_allKnownRecordSIDs = results;
		}

		return _allKnownRecordSIDs.clone();
	}

	/**
	 * gets the record constructors and sticks them in the map by SID
	 * @return map of SIDs to short,short,byte[] constructors for Record classes
	 * most of org.apache.poi.hssf.record.*
	 */
	private static Map<Short, I_RecordCreator> recordsToMap(Class<? extends Record> [] records) {
		Map<Short, I_RecordCreator> result = new HashMap<Short, I_RecordCreator>();
		Set<Class<?>> uniqueRecClasses = new HashSet<Class<?>>(records.length * 3 / 2);

		for (int i = 0; i < records.length; i++) {

			Class<? extends Record> recClass = records[ i ];
			if(!Record.class.isAssignableFrom(recClass)) {
				throw new RuntimeException("Invalid record sub-class (" + recClass.getName() + ")");
			}
			if(Modifier.isAbstract(recClass.getModifiers())) {
				throw new RuntimeException("Invalid record class (" + recClass.getName() + ") - must not be abstract");
			}
			if(!uniqueRecClasses.add(recClass)) {
				throw new RuntimeException("duplicate record class (" + recClass.getName() + ")");
			}
			
			short sid;
			Constructor<? extends Record> constructor;
			try {
				sid = recClass.getField("sid").getShort(null);
				constructor = recClass.getConstructor(CONSTRUCTOR_ARGS);
			} catch (Exception illegalArgumentException) {
				throw new RecordFormatException(
					"Unable to determine record types");
			}
			Short key = new Short(sid);
			if (result.containsKey(key)) {
				String prevClassName = result.get(key).getRecordClassName();
				throw new RuntimeException("duplicate record sid 0x" + Integer.toHexString(sid).toUpperCase()
						+ " for classes (" + recClass.getName() + ") and (" + prevClassName + ")");
			}
			result.put(key, new ReflectionRecordCreator(constructor));
		}
		return result;
	}

	private static void checkZeros(InputStream in, int avail) throws IOException {
		int count=0;
		while(true) {
			int b = in.read();
			if (b < 0) {
				break;
			}
			if (b!=0) {
				System.err.print(HexDump.byteToHex(b));
			}
			count++;
		}
		if (avail != count) {
			System.err.println("avail!=count (" + avail + "!=" + count + ").");
		}
	}
	/**
	 * Create an array of records from an input stream
	 *
	 * @param in the InputStream from which the records will be obtained
	 *
	 * @return an array of Records created from the InputStream
	 *
	 * @exception RecordFormatException on error processing the InputStream
	 */
	public static List<Record> createRecords(InputStream in) throws RecordFormatException {

		List<Record> records = new ArrayList<Record>(NUM_RECORDS);

		RecordInputStream recStream = new RecordInputStream(in);
		DrawingRecord lastDrawingRecord = new DrawingRecord( );
		Record lastRecord = null;
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
		 
		int bofDepth=0;
		boolean lastRecordWasEOFLevelZero = false;
		while (recStream.hasNextRecord()) {
			recStream.nextRecord();
			if (lastRecordWasEOFLevelZero && recStream.getSid() != BOFRecord.sid) {
				// Normally InputStream (in) contains only zero padding after this point
				break;
			}
			Record record = createSingleRecord(recStream);
			lastRecordWasEOFLevelZero = false;
			if (record instanceof BOFRecord) {
				bofDepth++;
				records.add(record);
				continue;
			}
			if (record instanceof EOFRecord) {
				bofDepth--;
				records.add(record);
				if (bofDepth<1) {
					lastRecordWasEOFLevelZero = true;
				}
				continue;
			}
			
			if (record instanceof DBCellRecord) {
				// Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
				continue;
			}

			if (record instanceof RKRecord) {
				records.add(convertToNumberRecord((RKRecord) record));
				continue;
			}
			if (record instanceof MulRKRecord) {
				addAll(records, convertRKRecords((MulRKRecord)record));
				continue;
			}

			if (record.getSid() == DrawingGroupRecord.sid
				   && lastRecord instanceof DrawingGroupRecord) {
				DrawingGroupRecord lastDGRecord = (DrawingGroupRecord) lastRecord;
				lastDGRecord.join((AbstractEscherHolderRecord) record);
			} else if (record.getSid() == ContinueRecord.sid) {
				ContinueRecord contRec = (ContinueRecord)record;
				
				if (lastRecord instanceof ObjRecord || lastRecord instanceof TextObjectRecord) {
					// Drawing records have a very strange continue behaviour.
					//There can actually be OBJ records mixed between the continues.
					lastDrawingRecord.processContinueRecord(contRec.getData() );
					//we must remember the position of the continue record.
					//in the serialization procedure the original structure of records must be preserved
					records.add(record);
				} else if (lastRecord instanceof DrawingGroupRecord) {
					((DrawingGroupRecord)lastRecord).processContinueRecord(contRec.getData());
				} else if (lastRecord instanceof UnknownRecord) {
					//Gracefully handle records that we don't know about,
					//that happen to be continued
					records.add(record);
				} else if (lastRecord instanceof EOFRecord) {
					// This is really odd, but excel still sometimes
					//  outputs a file like this all the same
					records.add(record);
				} else {
					throw new RecordFormatException("Unhandled Continue Record");
				}
			} else {
				lastRecord = record;
				if (record instanceof DrawingRecord) {
					lastDrawingRecord = (DrawingRecord) record;
				}
				records.add(record);
			}
		}
		return records;
	}

	private static void addAll(List<Record> destList, Record[] srcRecs) {
		for (int i = 0; i < srcRecs.length; i++) {
			destList.add(srcRecs[i]);
		}
	}
}
