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

package org.apache.poi.hssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationListener;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorTestHelper;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.BaseTestFormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

final class TestHSSFFormulaEvaluator extends BaseTestFormulaEvaluator {

    public TestHSSFFormulaEvaluator() {
        super(HSSFITestDataProvider.instance);
    }

    /**
     * Test that the HSSFFormulaEvaluator can evaluate simple named ranges
     * (single cells and rectangular areas)
     */
    @Test
    void testEvaluateSimple() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testNames.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFCell cell = sheet.getRow(8).getCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        CellValue cv = fe.evaluate(cell);
        assertEquals(CellType.NUMERIC, cv.getCellType());
        assertEquals(3.72, cv.getNumberValue(), 0.0);
        wb.close();
    }

    /**
     * When evaluating defined names, POI has to decide whether it is capable.  Currently
     * (May2009) POI only supports simple cell and area refs.<br>
     * The sample spreadsheet (bugzilla attachment 23508) had a name flagged as 'complex'
     * which contained a simple area ref.  It is not clear what the 'complex' flag is used
     * for but POI should look elsewhere to decide whether it can evaluate the name.
     */
    @Test
    void testDefinedNameWithComplexFlag_bug47048() throws IOException {
        // Mock up a spreadsheet to match the critical details of the sample
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Input");
        HSSFName definedName = wb.createName();
        definedName.setNameName("Is_Multicar_Vehicle");
        definedName.setRefersToFormula("Input!$B$17:$G$17");

        // Set up some data and the formula
        HSSFRow row17 = sheet.createRow(16);
        row17.createCell(0).setCellValue(25.0);
        row17.createCell(1).setCellValue(1.33);
        row17.createCell(2).setCellValue(4.0);

        HSSFRow row = sheet.createRow(0);
        HSSFCell cellA1 = row.createCell(0);
        cellA1.setCellFormula("SUM(Is_Multicar_Vehicle)");

        // Set the complex flag - POI doesn't usually manipulate this flag
        NameRecord nameRec = TestHSSFName.getNameRecord(definedName);
        nameRec.setOptionFlag((short) 0x10); // 0x10 -> complex

        HSSFFormulaEvaluator hsf = new HSSFFormulaEvaluator(wb);
        CellValue value;
        try {
            value = hsf.evaluate(cellA1);

            assertEquals(CellType.NUMERIC, value.getCellType());
            assertEquals(5.33, value.getNumberValue(), 0.0);

        } catch (RuntimeException e) {
            if (e.getMessage().equals("Don't know how to evaluate name 'Is_Multicar_Vehicle'")) {
                fail("Identified bug 47048a");
            }
            throw e;
        } finally {
            wb.close();
        }
    }

    private static final class EvalCountListener extends EvaluationListener {
        private int _evalCount;

        public EvalCountListener() {
            _evalCount = 0;
        }

        @Override
        public void onStartEvaluate(EvaluationCell cell, ICacheEntry entry) {
            _evalCount++;
        }

        public int getEvalCount() {
            return _evalCount;
        }
    }

    /**
     * The HSSFFormula evaluator performance benefits greatly from caching of intermediate cell values
     */
    @Test
    void testShortCircuitIfEvaluation() throws IOException {
        // Set up a simple IF() formula that has measurable evaluation cost for its operands.
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Sheet1");
            HSSFRow row = sheet.createRow(0);
            HSSFCell cellA1 = row.createCell(0);
            cellA1.setCellFormula("if(B1,C1,D1+E1+F1)");
            // populate cells B1..F1 with simple formulas instead of plain values so we can use
            // EvaluationListener to check which parts of the first formula get evaluated
            for (int i = 1; i < 6; i++) {
                // formulas are just literal constants "1".."5"
                row.createCell(i).setCellFormula(String.valueOf(i));
            }

            EvalCountListener evalListener = new EvalCountListener();
            WorkbookEvaluator evaluator = WorkbookEvaluatorTestHelper.createEvaluator(wb, evalListener);
            ValueEval ve = evaluator.evaluate(HSSFEvaluationTestHelper.wrapCell(cellA1));
            int evalCount = evalListener.getEvalCount();
            // Without short-circuit-if evaluation, evaluating cell 'A1' takes 3 extra evaluations (for D1,E1,F1)
            assertNotEquals(6, evalCount, "Identifed bug 48195 - Formula evaluator should short-circuit IF() calculations.");
            assertEquals(3, evalCount);
            assertEquals(2.0, ((NumberEval) ve).getNumberValue(), 0D);
        }
    }

    /**
     * Ensures that we can handle NameXPtgs in the formulas
     * we parse.
     */
    @Test
    void testXRefs() throws IOException {
        try (HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("XRefCalc.xls");
        HSSFWorkbook wb2 = HSSFTestDataSamples.openSampleWorkbook("XRefCalcData.xls")) {
			Cell cell;

			// VLookup on a name in another file
			cell = wb1.getSheetAt(0).getRow(1).getCell(2);
			assertEquals(CellType.FORMULA, cell.getCellType());
			assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
			assertEquals(12.30, cell.getNumericCellValue(), 0.0001);
			// WARNING - this is wrong!
			// The file name should be showing, but bug #45970 is fixed
			//  we seem to loose it
			assertEquals("VLOOKUP(PART,COSTS,2,FALSE)", cell.getCellFormula());


			// Simple reference to a name in another file
			cell = wb1.getSheetAt(0).getRow(1).getCell(4);
			assertEquals(CellType.FORMULA, cell.getCellType());
			assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
			assertEquals(36.90, cell.getNumericCellValue(), 0.0001);
			// TODO Correct this!
			// The file name should be shown too, see bug #56742
			assertEquals("Cost*Markup_Cost", cell.getCellFormula());


			// Evaluate the cells
			HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb1);
			HSSFFormulaEvaluator.setupEnvironment(
				new String[]{"XRefCalc.xls", "XRefCalcData.xls"},
				new HSSFFormulaEvaluator[]{
					eval,
					new HSSFFormulaEvaluator(wb2)
				}
			);
			eval.evaluateFormulaCell(
				wb1.getSheetAt(0).getRow(1).getCell(2)
			);
			eval.evaluateFormulaCell(
				wb1.getSheetAt(0).getRow(1).getCell(4)
			);


			// Re-check VLOOKUP one
			cell = wb1.getSheetAt(0).getRow(1).getCell(2);
			assertEquals(CellType.FORMULA, cell.getCellType());
			assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
			assertEquals(12.30, cell.getNumericCellValue(), 0.0001);

			// Re-check ref one
			cell = wb1.getSheetAt(0).getRow(1).getCell(4);
			assertEquals(CellType.FORMULA, cell.getCellType());
			assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
			assertEquals(36.90, cell.getNumericCellValue(), 0.0001);


			// Add a formula that refers to one of the existing external workbooks
			cell = wb1.getSheetAt(0).getRow(1).createCell(40);
			cell.setCellFormula("Cost*[XRefCalcData.xls]MarkupSheet!$B$1");

			// Check is was stored correctly
			assertEquals("Cost*[XRefCalcData.xls]MarkupSheet!$B$1", cell.getCellFormula());

			// Check it evaluates correctly
			eval.evaluateFormulaCell(cell);
			assertEquals(24.60 * 1.8, cell.getNumericCellValue(), 0);

			// Try to add a formula for a new external workbook, won't be allowed to start
            cell = wb1.getSheetAt(0).getRow(1).createCell(42);
            final Cell cell2 = cell;
            assertThrows(Exception.class, () -> cell2.setCellFormula("[alt.xls]Sheet0!$A$1"),
                "New workbook not linked, shouldn't be able to add");

			// Link our new workbook
			try (HSSFWorkbook wb3 = new HSSFWorkbook()) {
				wb3.createSheet().createRow(0).createCell(0).setCellValue("In another workbook");
				assertEquals(2, wb1.linkExternalWorkbook("alt.xls", wb3));

				// Now add a formula that refers to our new workbook
				cell.setCellFormula("[alt.xls]Sheet0!$A$1");
				assertEquals("[alt.xls]Sheet0!$A$1", cell.getCellFormula());

				HSSFFormulaEvaluator eval2 = eval;
				assertThrows(Exception.class, () -> eval2.evaluate(cell2),
					"No cached value and no link to workbook, shouldn't evaluate");

				// Add a link, check it does
				HSSFFormulaEvaluator.setupEnvironment(
					new String[]{"XRefCalc.xls", "XRefCalcData.xls", "alt.xls"},
					new HSSFFormulaEvaluator[]{
						eval,
						new HSSFFormulaEvaluator(wb2),
						new HSSFFormulaEvaluator(wb3)
					}
				);
				eval.evaluateFormulaCell(cell);
				assertEquals("In another workbook", cell.getStringCellValue());


				// Save and re-load
				try (HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
					eval = new HSSFFormulaEvaluator(wb4);
					HSSFFormulaEvaluator.setupEnvironment(
						new String[]{"XRefCalc.xls", "XRefCalcData.xls", "alt.xls"},
						new HSSFFormulaEvaluator[]{
							eval,
							new HSSFFormulaEvaluator(wb2),
							new HSSFFormulaEvaluator(wb3)
						}
					);

					// Check the one referring to the previously existing workbook behaves
					cell = wb4.getSheetAt(0).getRow(1).getCell(40);
					assertEquals("Cost*[XRefCalcData.xls]MarkupSheet!$B$1", cell.getCellFormula());
					eval.evaluateFormulaCell(cell);
					assertEquals(24.60 * 1.8, cell.getNumericCellValue(), 0);

					// Now check the newly added reference
					cell = wb4.getSheetAt(0).getRow(1).getCell(42);
					assertEquals("[alt.xls]Sheet0!$A$1", cell.getCellFormula());
					eval.evaluateFormulaCell(cell);
					assertEquals("In another workbook", cell.getStringCellValue());
				}
			}
		}
    }
}
