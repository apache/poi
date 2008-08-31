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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.SharedValueRecordBase;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.RecordInspector;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;

/**
 * 
 */
public final class TestRowRecordsAggregate extends TestCase {

	public void testRowGet() {
		RowRecordsAggregate rra = new RowRecordsAggregate();
		RowRecord rr = new RowRecord(4);
		rra.insertRow(rr);
		rra.insertRow(new RowRecord(1));

		RowRecord rr1 = rra.getRow(4);

		assertNotNull(rr1);
		assertEquals("Row number is 1", 4, rr1.getRowNumber());
		assertTrue("Row record retrieved is identical ", rr1 == rr);
	}

	/**
	 * Prior to Aug 2008, POI would re-serialize spreadsheets with {@link ArrayRecord}s or
	 * {@link TableRecord}s with those records out of order.  Similar to 
	 * {@link SharedFormulaRecord}s, these records should appear immediately after the first
	 * {@link FormulaRecord}s that they apply to (and only once).<br/>
	 */
	public void testArraysAndTables() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testArraysAndTables.xls");
		Record[] sheetRecs = RecordInspector.getRecords(wb.getSheetAt(0), 0);
		
		int countArrayFormulas = verifySharedValues(sheetRecs, ArrayRecord.class);
		assertEquals(5, countArrayFormulas);
		int countTableFormulas = verifySharedValues(sheetRecs, TableRecord.class);
		assertEquals(3, countTableFormulas);

		// Note - SharedFormulaRecords are currently not re-serialized by POI (each is extracted
		// into many non-shared formulas), but if they ever were, the same rules would apply. 
		int countSharedFormulas = verifySharedValues(sheetRecs, SharedFormulaRecord.class);
		assertEquals(0, countSharedFormulas);
		

		if (false) { // set true to observe re-serialized file
			File f = new File(System.getProperty("java.io.tmpdir") + "/testArraysAndTables-out.xls");
			try {
				OutputStream os = new FileOutputStream(f);
				wb.write(os);
				os.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Output file to " + f.getAbsolutePath());
		}
	}

	private static int verifySharedValues(Record[] recs, Class shfClass) {
		
		int result =0;
		for(int i=0; i<recs.length; i++) {
			Record rec = recs[i];
			if (rec.getClass() == shfClass) {
				result++;
				Record prevRec = recs[i-1];
				if (!(prevRec instanceof FormulaRecord)) {
					throw new AssertionFailedError("Bad record order at index "
							+ i + ": Formula record expected but got (" 
							+ prevRec.getClass().getName() + ")");
				}
				verifySharedFormula((FormulaRecord) prevRec, rec);
			}
		}
		return result;
	}

	private static void verifySharedFormula(FormulaRecord firstFormula, Record rec) {
		CellRangeAddress8Bit range = ((SharedValueRecordBase)rec).getRange();
		assertEquals(range.getFirstRow(), firstFormula.getRow());
		assertEquals(range.getFirstColumn(), firstFormula.getColumn());
	}
}
