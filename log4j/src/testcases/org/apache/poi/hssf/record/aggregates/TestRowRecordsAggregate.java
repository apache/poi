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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.ArrayRecord;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.SharedValueRecordBase;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RowRecordsAggregate}
 */
final class TestRowRecordsAggregate {

    @Test
	void testRowGet() {
		RowRecordsAggregate rra = new RowRecordsAggregate();
		RowRecord rr = new RowRecord(4);
		rra.insertRow(rr);
		rra.insertRow(new RowRecord(1));

		RowRecord rr1 = rra.getRow(4);

		assertNotNull(rr1);
		assertEquals(4, rr1.getRowNumber(), "Row number is 1");
        assertSame(rr1, rr, "Row record retrieved is identical ");
	}

	/**
	 * Prior to Aug 2008, POI would re-serialize spreadsheets with {@link ArrayRecord}s or
	 * {@link TableRecord}s with those records out of order.  Similar to
	 * {@link SharedFormulaRecord}s, these records should appear immediately after the first
	 * {@link FormulaRecord}s that they apply to (and only once).<br>
	 */
    @Test
	void testArraysAndTables() throws Exception {
		try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testArraysAndTables.xls")) {
			final List<org.apache.poi.hssf.record.Record> sheetRecs = new ArrayList<>();
			wb.getSheetAt(0).getSheet().visitContainedRecords(sheetRecs::add, 0);

			int countArrayFormulas = verifySharedValues(sheetRecs, ArrayRecord.class);
			assertEquals(5, countArrayFormulas);
			int countTableFormulas = verifySharedValues(sheetRecs, TableRecord.class);
			assertEquals(3, countTableFormulas);

			// Note - SharedFormulaRecords are currently not re-serialized by POI (each is extracted
			// into many non-shared formulas), but if they ever were, the same rules would apply.
			int countSharedFormulas = verifySharedValues(sheetRecs, SharedFormulaRecord.class);
			assertEquals(0, countSharedFormulas);


//		if (false) { // set true to observe re-serialized file
//			File f = new File(System.getProperty("java.io.tmpdir") + "/testArraysAndTables-out.xls");
//			try {
//				OutputStream os = new FileOutputStream(f);
//				wb.write(os);
//				os.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			System.out.println("Output file to " + f.getAbsolutePath());
//		}

		}
	}

	private static int verifySharedValues(List<org.apache.poi.hssf.record.Record> recs, Class<? extends SharedValueRecordBase> shfClass) {

		int result =0;
		for(int i=0; i<recs.size(); i++) {
			Record rec = recs.get(i);
			if (rec.getClass() == shfClass) {
				result++;
				Record prevRec = recs.get(i-1);
				assertTrue(prevRec instanceof FormulaRecord,
					"Bad record order at index " + i + ": Formula record expected but got (" + prevRec.getClass().getName() + ")");
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

	/**
	 * This problem was noted as the overt symptom of bug 46280.  The logic for skipping {@link
	 * UnknownRecord}s in the constructor {@link RowRecordsAggregate} did not allow for the
	 * possibility of tailing {@link ContinueRecord}s.<br>
	 * The functionality change being tested here is actually not critical to the overall fix
	 * for bug 46280, since the fix involved making sure the that offending <i>PivotTable</i>
	 * records do not get into {@link RowRecordsAggregate}.<br>
	 * This fix in {@link RowRecordsAggregate} was implemented anyway since any {@link
	 * UnknownRecord} has the potential of being 'continued'.
	 */
	@Test
	void testUnknownContinue_bug46280() {
		org.apache.poi.hssf.record.Record[] inRecs = {
			new RowRecord(0),
			new NumberRecord(),
			new UnknownRecord(0x5555, "dummydata".getBytes(LocaleUtil.CHARSET_1252)),
			new ContinueRecord("moredummydata".getBytes(LocaleUtil.CHARSET_1252)),
		};
		RecordStream rs = new RecordStream(Arrays.asList(inRecs), 0);
		RowRecordsAggregate rra;
		try {
			rra = new RowRecordsAggregate(rs, SharedValueManager.createEmpty());
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith("Unexpected record type")) {
				fail("Identified bug 46280a");
			}
			throw e;
		}

		List<org.apache.poi.hssf.record.Record> outRecs = new ArrayList<>();
		rra.visitContainedRecords(outRecs::add);
		assertEquals(5, outRecs.size());
	}
}
