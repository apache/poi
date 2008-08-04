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

	private static final Class[] CONSTRUCTOR_ARGS = { RecordInputStream.class, };

	/**
	 * contains the classes for all the records we want to parse.<br/>
	 * Note - this most but not *every* subclass of Record.
	 */
	private static final Class[] records = {
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
	};
	
	/**
	 * cache of the recordsToMap();
	 */
	private static Map recordsMap  = recordsToMap(records);

	private static short[] _allKnownRecordSIDs;
	
	/**
	 * create a record, if there are MUL records than multiple records
	 * are returned digested into the non-mul form.
	 */
	public static Record [] createRecord(RecordInputStream in) {
		Constructor constructor = (Constructor) recordsMap.get(new Short(in.getSid()));

		if (constructor == null) {
			return new Record[] { new UnknownRecord(in), };
		}
		
		Record retval;

		try {
			retval = ( Record ) constructor.newInstance(new Object[] { in });
		} catch (InvocationTargetException e) {
			throw new RecordFormatException("Unable to construct record instance" , e.getTargetException());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		if (retval instanceof RKRecord) {
			// RK record is a slightly smaller alternative to NumberRecord
			// POI likes NumberRecord better
			RKRecord	 rk  = ( RKRecord ) retval;
			NumberRecord num = new NumberRecord();

			num.setColumn(rk.getColumn());
			num.setRow(rk.getRow());
			num.setXFIndex(rk.getXFIndex());
			num.setValue(rk.getRKNumber());
			return new Record[] { num, };
		}
		if (retval instanceof DBCellRecord) {
			// Not needed by POI.  Regenerated from scratch by POI when spreadsheet is written
			return new Record[] { null, };
		}
		// expand multiple records where necessary
		if (retval instanceof MulRKRecord) {
			MulRKRecord mrk = ( MulRKRecord ) retval;

			Record[] mulRecs = new Record[ mrk.getNumColumns() ];
			for (int k = 0; k < mrk.getNumColumns(); k++) {
				NumberRecord nr = new NumberRecord();

				nr.setColumn(( short ) (k + mrk.getFirstColumn()));
				nr.setRow(mrk.getRow());
				nr.setXFIndex(mrk.getXFAt(k));
				nr.setValue(mrk.getRKNumberAt(k));
				mulRecs[ k ] = nr;
			}
			return mulRecs;
		}
		if (retval instanceof MulBlankRecord) {
			MulBlankRecord mb = ( MulBlankRecord ) retval;

			Record[] mulRecs = new Record[ mb.getNumColumns() ];
			for (int k = 0; k < mb.getNumColumns(); k++) {
				BlankRecord br = new BlankRecord();

				br.setColumn(( short ) (k + mb.getFirstColumn()));
				br.setRow(mb.getRow());
				br.setXFIndex(mb.getXFAt(k));
				mulRecs[ k ] = br;
			}
			return mulRecs;
		}
		return new Record[] { retval, };
	}

	/**
	 * @return an array of all the SIDS for all known records
	 */
	public static short[] getAllKnownRecordSIDs() {
		if (_allKnownRecordSIDs == null) {
			short[] results = new short[ recordsMap.size() ];
			int i = 0;

			for (Iterator iterator = recordsMap.keySet().iterator(); iterator.hasNext(); ) {
				Short sid = (Short) iterator.next();

				results[i++] = sid.shortValue();
			}
			Arrays.sort(results);
 			_allKnownRecordSIDs = results;
		}

		return (short[]) _allKnownRecordSIDs.clone();
	}

	/**
	 * gets the record constructors and sticks them in the map by SID
	 * @return map of SIDs to short,short,byte[] constructors for Record classes
	 * most of org.apache.poi.hssf.record.*
	 */
	private static Map recordsToMap(Class [] records) {
		Map result = new HashMap();
		Set uniqueRecClasses = new HashSet(records.length * 3 / 2);

		for (int i = 0; i < records.length; i++) {

			Class recClass = records[ i ];
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
			Constructor constructor;
			try {
				sid		 = recClass.getField("sid").getShort(null);
				constructor = recClass.getConstructor(CONSTRUCTOR_ARGS);
			} catch (Exception illegalArgumentException) {
				throw new RecordFormatException(
					"Unable to determine record types");
			}
			Short key = new Short(sid);
			if (result.containsKey(key)) {
				Class prev = (Class)result.get(key);
				throw new RuntimeException("duplicate record sid 0x" + Integer.toHexString(sid).toUpperCase()
						+ " for classes (" + recClass.getName() + ") and (" + prev.getName() + ")");
			}
			result.put(key, constructor);
		}
		return result;
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
	public static List createRecords(InputStream in) throws RecordFormatException {

		List records = new ArrayList(NUM_RECORDS);

		RecordInputStream recStream = new RecordInputStream(in);
		DrawingRecord lastDrawingRecord = new DrawingRecord( );
		Record lastRecord = null;
		while (recStream.hasNextRecord()) {
			recStream.nextRecord();
			if (recStream.getSid() != 0) {
				Record[] recs = createRecord(recStream);   // handle MulRK records

				if (recs.length > 1) {
					for (int k = 0; k < recs.length; k++) {
						records.add(recs[ k ]);			   // these will be number records
					}
				} else {
					Record record = recs[ 0 ];

					if (record != null) {
						if (record.getSid() == DrawingGroupRecord.sid
							   && lastRecord instanceof DrawingGroupRecord) {
							DrawingGroupRecord lastDGRecord = (DrawingGroupRecord) lastRecord;
							lastDGRecord.join((AbstractEscherHolderRecord) record);
						} else if (record.getSid() == ContinueRecord.sid &&
								 ((lastRecord instanceof ObjRecord) || (lastRecord instanceof TextObjectRecord))) {
							// Drawing records have a very strange continue behaviour.
							//There can actually be OBJ records mixed between the continues.
							lastDrawingRecord.processContinueRecord( ((ContinueRecord)record).getData() );
							//we must remember the position of the continue record.
							//in the serialization procedure the original structure of records must be preserved
							records.add(record);
						} else if (record.getSid() == ContinueRecord.sid &&
								(lastRecord instanceof DrawingGroupRecord)) {
							((DrawingGroupRecord)lastRecord).processContinueRecord(((ContinueRecord)record).getData());
						} else if (record.getSid() == ContinueRecord.sid &&
								(lastRecord instanceof StringRecord)) {
							((StringRecord)lastRecord).processContinueRecord(((ContinueRecord)record).getData());
						} else if (record.getSid() == ContinueRecord.sid) {
							if (lastRecord instanceof UnknownRecord) {
								//Gracefully handle records that we dont know about,
								//that happen to be continued
								records.add(record);
							} else 
								throw new RecordFormatException("Unhandled Continue Record");
						} else {
							lastRecord = record;
							if (record instanceof DrawingRecord) {
								lastDrawingRecord = (DrawingRecord) record;
							}
							records.add(record);
						}
					}
				}
			}
		}
		return records;
	}
}
