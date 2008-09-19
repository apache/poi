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
package org.apache.poi.hssf.eventusermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * When working with the EventUserModel, if you want to 
 *  process formulas, you need an instance of
 *  {@link Workbook} to pass to a {@link HSSFWorkbook},
 *  to finally give to {@link HSSFFormulaParser}, 
 *  and this will build you stub ones.
 * Since you're working with the EventUserModel, you
 *  wouldn't want to get a full {@link Workbook} and
 *  {@link HSSFWorkbook}, as they would eat too much memory.
 *  Instead, you should collect a few key records as they
 *  go past, then call this once you have them to build a
 *  stub {@link Workbook}, and from that a stub
 *  {@link HSSFWorkbook}, to use with the {@link HSSFFormulaParser}.
 * 
 * The records you should collect are:
 *  * {@link ExternSheetRecord}
 *  * {@link BoundSheetRecord}
 * You should probably also collect {@link SSTRecord}, 
 *  but it's not required to pass this in.
 *  
 * To help, this class includes a HSSFListener wrapper
 *  that will do the collecting for you.
 */
public class EventWorkbookBuilder {
	/**
	 * Wraps up your stub {@link Workbook} as a stub
	 *  {@link HSSFWorkbook}, ready for passing to
	 *  {@link HSSFFormulaParser}
	 * @param workbook A stub {@link Workbook}
	 */
	public static HSSFWorkbook createStubHSSFWorkbook(Workbook workbook) {
		return new StubHSSFWorkbook(workbook);
	}
	
	/**
	 * Creates a stub Workbook from the supplied records,
	 *  suitable for use with the {@link HSSFFormulaParser}
	 * @param externs The ExternSheetRecords in your file
	 * @param bounds The BoundSheetRecords in your file
	 * @param sst The SSTRecord in your file.
	 * @return A stub Workbook suitable for use with {@link HSSFFormulaParser}
	 */
	public static Workbook createStubWorkbook(ExternSheetRecord[] externs,
			BoundSheetRecord[] bounds, SSTRecord sst) {
		List wbRecords = new ArrayList();
		
		// Core Workbook records go first
		if(bounds != null) {
			for(int i=0; i<bounds.length; i++) {
				wbRecords.add(bounds[i]);
			}
		}
		if(sst != null) {
			wbRecords.add(sst);
		}
		
		// Now we can have the ExternSheetRecords,
		//  preceded by a SupBookRecord
		if(externs != null) {
			wbRecords.add(SupBookRecord.createInternalReferences(
					(short)externs.length));
			for(int i=0; i<externs.length; i++) {
				wbRecords.add(externs[i]);
			}
		}
		
		// Finally we need an EoF record
		wbRecords.add(EOFRecord.instance);
		
		return Workbook.createWorkbook(wbRecords);
	}
	
	/**
	 * Creates a stub workbook from the supplied records,
	 *  suitable for use with the {@link HSSFFormulaParser}
	 * @param externs The ExternSheetRecords in your file
	 * @param bounds The BoundSheetRecords in your file
	 * @return A stub Workbook suitable for use with {@link HSSFFormulaParser}
	 */
	public static Workbook createStubWorkbook(ExternSheetRecord[] externs,
			BoundSheetRecord[] bounds) {
		return createStubWorkbook(externs, bounds, null);
	}
	
	
	/**
	 * A wrapping HSSFListener which will collect 
	 *  {@link BoundSheetRecord}s and {@link ExternSheetRecord}s as
	 *  they go past, so you can create a Stub {@link Workbook} from
	 *  them once required.
	 */
	public static class SheetRecordCollectingListener implements HSSFListener {
		private HSSFListener childListener;
		private List boundSheetRecords = new ArrayList();
		private List externSheetRecords = new ArrayList();
		private SSTRecord sstRecord = null;
		
		public SheetRecordCollectingListener(HSSFListener childListener) {
			this.childListener = childListener;
		}
		
		
		public BoundSheetRecord[] getBoundSheetRecords() {
			return (BoundSheetRecord[])boundSheetRecords.toArray(
					new BoundSheetRecord[boundSheetRecords.size()]
			);
		}
		public ExternSheetRecord[] getExternSheetRecords() {
			return (ExternSheetRecord[])externSheetRecords.toArray(
					new ExternSheetRecord[externSheetRecords.size()]
			);
		}
		public SSTRecord getSSTRecord() {
			return sstRecord;
		}
		
		public HSSFWorkbook getStubHSSFWorkbook() {
			return createStubHSSFWorkbook(
					getStubWorkbook()
			);
		}
		public Workbook getStubWorkbook() {
			return createStubWorkbook(
					getExternSheetRecords(), getBoundSheetRecords(), 
					getSSTRecord()
			);
		}
		
		
		/**
		 * Process this record ourselves, and then
		 *  pass it on to our child listener
		 */
		public void processRecord(Record record) {
			// Handle it ourselves
			processRecordInternally(record);
			
			// Now pass on to our child
			childListener.processRecord(record);
		}
		
		/**
		 * Process the record ourselves, but do not
		 *  pass it on to the child Listener.
		 */
		public void processRecordInternally(Record record) {
			if(record instanceof BoundSheetRecord) {
				boundSheetRecords.add(record);
			}
			else if(record instanceof ExternSheetRecord) {
				externSheetRecords.add(record);
			}
			else if(record instanceof SSTRecord) {
				sstRecord = (SSTRecord)record;
			}
		}
	}
	
	/**
	 * Let us at the {@link Workbook} constructor on
	 *  {@link HSSFWorkbook}
	 */
	private static class StubHSSFWorkbook extends HSSFWorkbook {
		private StubHSSFWorkbook(Workbook wb) {
			super(wb);
		}
	}
}