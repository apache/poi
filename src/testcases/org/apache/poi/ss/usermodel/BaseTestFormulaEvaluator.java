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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Locale;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.junit.jupiter.api.Test;

/**
 * Common superclass for testing implementation of {@link FormulaEvaluator}
 */
public abstract class BaseTestFormulaEvaluator {

	protected final ITestDataProvider _testDataProvider;

	/**
	 * @param testDataProvider an object that provides test data in  /  specific way
	 */
	protected BaseTestFormulaEvaluator(ITestDataProvider testDataProvider) {
		_testDataProvider = testDataProvider;
	}

	@Test
    void testSimpleArithmetic() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);

        Cell c1 = r.createCell(0);
        c1.setCellFormula("1+5");
        assertEquals(0.0, c1.getNumericCellValue(), 0.0);

        Cell c2 = r.createCell(1);
        c2.setCellFormula("10/2");
        assertEquals(0.0, c2.getNumericCellValue(), 0.0);

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        fe.evaluateFormulaCell(c1);
        fe.evaluateFormulaCell(c2);

        assertEquals(6.0, c1.getNumericCellValue(), 0.0001);
        assertEquals(5.0, c2.getNumericCellValue(), 0.0001);

        wb.close();
    }

	@Test
	void testSumCount() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        r.createCell(0).setCellValue(2.5);
        r.createCell(1).setCellValue(1.1);
        r.createCell(2).setCellValue(3.2);
        r.createCell(4).setCellValue(10.7);

        r = s.createRow(1);

        Cell c1 = r.createCell(0);
        c1.setCellFormula("SUM(A1:B1)");
        assertEquals(0.0, c1.getNumericCellValue(), 0.0);

        Cell c2 = r.createCell(1);
        c2.setCellFormula("SUM(A1:E1)");
        assertEquals(0.0, c2.getNumericCellValue(), 0.0);

        Cell c3 = r.createCell(2);
        c3.setCellFormula("COUNT(A1:A1)");
        assertEquals(0.0, c3.getNumericCellValue(), 0.0);

        Cell c4 = r.createCell(3);
        c4.setCellFormula("COUNTA(A1:E1)");
        assertEquals(0.0, c4.getNumericCellValue(), 0.0);


        // Evaluate and test
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        fe.evaluateFormulaCell(c1);
        fe.evaluateFormulaCell(c2);
        fe.evaluateFormulaCell(c3);
        fe.evaluateFormulaCell(c4);

        assertEquals(3.6, c1.getNumericCellValue(), 0.0001);
        assertEquals(17.5, c2.getNumericCellValue(), 0.0001);
        assertEquals(1, c3.getNumericCellValue(), 0.0001);
        assertEquals(4, c4.getNumericCellValue(), 0.0001);

        wb.close();
    }

    @Test
	void testSharedFormulas() throws IOException {
        String fileName = "shared_formulas.xls" + (getClass().getName().contains("xssf") ? "x" : "");
        try (Workbook wb = _testDataProvider.openSampleWorkbook(fileName)) {

            Sheet sheet = wb.getSheetAt(0);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Cell cell;

            cell = sheet.getRow(1).getCell(0);
            assertEquals("B2", cell.getCellFormula());
            assertEquals("ProductionOrderConfirmation", evaluator.evaluate(cell).getStringValue());

            cell = sheet.getRow(2).getCell(0);
            assertEquals("B3", cell.getCellFormula());
            assertEquals("RequiredAcceptanceDate", evaluator.evaluate(cell).getStringValue());

            cell = sheet.getRow(3).getCell(0);
            assertEquals("B4", cell.getCellFormula());
            assertEquals("Header", evaluator.evaluate(cell).getStringValue());

            cell = sheet.getRow(4).getCell(0);
            assertEquals("B5", cell.getCellFormula());
            assertEquals("UniqueDocumentNumberID", evaluator.evaluate(cell).getStringValue());
        }
    }

    /**
     * Test creation / evaluation of formulas with sheet-level names
     */
	@Test
    void testSheetLevelFormulas() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Row row;
        Sheet sh1 = wb.createSheet("Sheet1");
        Name nm1 = wb.createName();
        nm1.setNameName("sales_1");
        nm1.setSheetIndex(0);
        nm1.setRefersToFormula("Sheet1!$A$1");
        row = sh1.createRow(0);
        row.createCell(0).setCellValue(3);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*2");

        Sheet sh2 = wb.createSheet("Sheet2");
        Name nm2 = wb.createName();
        nm2.setNameName("sales_1");
        nm2.setSheetIndex(1);
        nm2.setRefersToFormula("Sheet2!$A$1");

        row = sh2.createRow(0);
        row.createCell(0).setCellValue(5);
        row.createCell(1).setCellFormula("sales_1");
        row.createCell(2).setCellFormula("sales_1*3");

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(3.0, evaluator.evaluate(sh1.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(6.0, evaluator.evaluate(sh1.getRow(0).getCell(2)).getNumberValue(), 0.0);

        assertEquals(5.0, evaluator.evaluate(sh2.getRow(0).getCell(1)).getNumberValue(), 0.0);
        assertEquals(15.0, evaluator.evaluate(sh2.getRow(0).getCell(2)).getNumberValue(), 0.0);

        wb.close();
    }

	@Test
    void testFullColumnRefs() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        cell0.setCellFormula("sum(D:D)");
        Cell cell1 = row.createCell(1);
        cell1.setCellFormula("sum(D:E)");

        // some values in column D
        setValue(sheet, 1, 3, 5.0);
        setValue(sheet, 2, 3, 6.0);
        setValue(sheet, 5, 3, 7.0);
        setValue(sheet, 50, 3, 8.0);

        // some values in column E
        setValue(sheet, 1, 4, 9.0);
        setValue(sheet, 2, 4, 10.0);
        setValue(sheet, 30000, 4, 11.0);

        // some other values
        setValue(sheet, 1, 2, 100.0);
        setValue(sheet, 2, 5, 100.0);
        setValue(sheet, 3, 6, 100.0);


        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(26.0, fe.evaluate(cell0).getNumberValue(), 0.0);
        assertEquals(56.0, fe.evaluate(cell1).getNumberValue(), 0.0);

        wb.close();
    }

	@Test
    void testRepeatedEvaluation() throws IOException {
       Workbook wb = _testDataProvider.createWorkbook();
       FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
       Sheet sheet = wb.createSheet("Sheet1");
       Row r = sheet.createRow(0);
       Cell c = r.createCell(0, CellType.FORMULA);

       // Create a value and check it
       c.setCellFormula("Date(2011,10,6)");
       CellValue cellValue = fe.evaluate(c);
       assertEquals(40822.0, cellValue.getNumberValue(), 0.0);
       cellValue = fe.evaluate(c);
       assertEquals(40822.0, cellValue.getNumberValue(), 0.0);

       // Change it
       c.setCellFormula("Date(2011,10,4)");

       // Evaluate it, no change as the formula evaluator
       //  won't know to clear the cache
       cellValue = fe.evaluate(c);
       assertEquals(40822.0, cellValue.getNumberValue(), 0.0);

       // Manually flush for this cell, and check
       fe.notifySetFormula(c);
       cellValue = fe.evaluate(c);
       assertEquals(40820.0, cellValue.getNumberValue(), 0.0);

       // Change again, without notifying
       c.setCellFormula("Date(2010,10,4)");
       cellValue = fe.evaluate(c);
       assertEquals(40820.0, cellValue.getNumberValue(), 0.0);

       // Now manually clear all, will see the new value
       fe.clearAllCachedResultValues();
       cellValue = fe.evaluate(c);
       assertEquals(40455.0, cellValue.getNumberValue(), 0.0);

       wb.close();
    }

    private static void setValue(Sheet sheet, int rowIndex, int colIndex, double value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        row.createCell(colIndex).setCellValue(value);
    }

    /**
     * {@link FormulaEvaluator#evaluate(org.apache.poi.ss.usermodel.Cell)} should behave the same whether the cell
     * is <code>null</code> or blank.
     */
    @Test
    void testEvaluateBlank() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        assertNull(fe.evaluate(null));
        Sheet sheet = wb.createSheet("Sheet1");
        Cell cell = sheet.createRow(0).createCell(0);
        assertNull(fe.evaluate(cell));
        wb.close();
    }

    /**
     * Test for bug due to attempt to convert a cached formula error result to a boolean
     */
    @Test
    void testUpdateCachedFormulaResultFromErrorToNumber_bug46479() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row row = sheet.createRow(0);
            Cell cellA1 = row.createCell(0);
            Cell cellB1 = row.createCell(1);
            cellB1.setCellFormula("A1+1");
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

            cellA1.setCellErrorValue(FormulaError.NAME.getCode());
            assertEquals(CellType.ERROR, fe.evaluateFormulaCell(cellB1));
            assertEquals(CellType.FORMULA, cellB1.getCellType());

            cellA1.setCellValue(2.5);
            fe.notifyUpdateCell(cellA1);
            try {
                fe.evaluateInCell(cellB1);
            } catch (IllegalStateException e) {
                if (e.getMessage().equalsIgnoreCase("Cannot get a numeric value from a error formula cell")) {
                    fail("Identified bug 46479a");
                }
            }
            assertEquals(3.5, cellB1.getNumericCellValue(), 0.0);
        }
    }

    @Test
    void testRounding_bug51339() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cellA1 = row.createCell(0);
        cellA1.setCellValue(2162.615d);
        Cell cellB1 = row.createCell(1);
        cellB1.setCellFormula("round(a1,2)");
        Cell cellC1 = row.createCell(2);
        cellC1.setCellFormula("roundup(a1,2)");
        Cell cellD1 = row.createCell(3);
        cellD1.setCellFormula("rounddown(a1,2)");
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        assertEquals(2162.62, fe.evaluateInCell(cellB1).getNumericCellValue(), 0.0);
        assertEquals(2162.62, fe.evaluateInCell(cellC1).getNumericCellValue(), 0.0);
        assertEquals(2162.61, fe.evaluateInCell(cellD1).getNumericCellValue(), 0.0);

        wb.close();
    }

    @Test
    void evaluateInCellReturnsSameCell() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            wb.createSheet().createRow(0).createCell(0);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Cell cell = wb.getSheetAt(0).getRow(0).getCell(0);
            Cell same = evaluator.evaluateInCell(cell);
            assertSame(cell, same);
        }
    }

    @Test
    void testBug61148() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            final Cell cell = wb.createSheet().createRow(0).createCell(0);
            cell.setCellFormula("1+2");

            assertEquals(0, (int)cell.getNumericCellValue());
            assertEquals("1+2", cell.toString());

            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

            eval.evaluateInCell(cell);

            assertEquals("3.0", cell.toString());
        }
    }

    @Test
    void testMultisheetFormulaEval() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet1 = wb.createSheet("Sheet1");
            Sheet sheet2 = wb.createSheet("Sheet2");
            Sheet sheet3 = wb.createSheet("Sheet3");

            // sheet1 A1
            Cell cell = sheet1.createRow(0).createCell(0);
            cell.setCellValue(1.0);

            // sheet2 A1
            cell = sheet2.createRow(0).createCell(0);
            cell.setCellValue(1.0);

            // sheet2 B1
            cell = sheet2.getRow(0).createCell(1);
            cell.setCellValue(1.0);

            // sheet3 A1
            cell = sheet3.createRow(0).createCell(0);
            cell.setCellValue(1.0);

            // sheet1 A2 formulae
            cell = sheet1.createRow(1).createCell(0);
            cell.setCellFormula("SUM(Sheet1:Sheet3!A1)");

            // sheet1 A3 formulae
            cell = sheet1.createRow(2).createCell(0);
            cell.setCellFormula("SUM(Sheet1:Sheet3!A1:B1)");

            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            cell = sheet1.getRow(1).getCell(0);
            assertEquals(3.0, cell.getNumericCellValue(), 0);

            cell = sheet1.getRow(2).getCell(0);
            assertEquals(4.0, cell.getNumericCellValue(), 0);
        }
    }

    @Test
    void testBug55843() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            cellA2.setCellFormula("IF(B1=0,\"\",((ROW()-ROW(A$1))*12))");
            CellValue evaluate = formulaEvaluator.evaluate(cellA2);
            assertEquals("12.0", evaluate.formatAsString());

            cellA2.setCellFormula("IF(NOT(B1=0),((ROW()-ROW(A$1))*12),\"\")");
            CellValue evaluateN = formulaEvaluator.evaluate(cellA2);

            assertEquals(evaluate.toString(), evaluateN.toString());
            assertEquals("12.0", evaluateN.formatAsString());
        }
    }

    @Test
    void testBug55843a() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            cellA2.setCellFormula("IF(B1=0,\"\",((ROW(A$1))))");
            CellValue evaluate = formulaEvaluator.evaluate(cellA2);
            assertEquals("1.0", evaluate.formatAsString());

            cellA2.setCellFormula("IF(NOT(B1=0),((ROW(A$1))),\"\")");
            CellValue evaluateN = formulaEvaluator.evaluate(cellA2);

            assertEquals(evaluate.toString(), evaluateN.toString());
            assertEquals("1.0", evaluateN.formatAsString());
        }
    }

    @Test
    void testBug55843b() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            cellA2.setCellFormula("IF(B1=0,\"\",((ROW())))");
            CellValue evaluate = formulaEvaluator.evaluate(cellA2);
            assertEquals("2.0", evaluate.formatAsString());

            cellA2.setCellFormula("IF(NOT(B1=0),((ROW())),\"\")");
            CellValue evaluateN = formulaEvaluator.evaluate(cellA2);

            assertEquals(evaluate.toString(), evaluateN.toString());
            assertEquals("2.0", evaluateN.formatAsString());
        }
    }

    @Test
    void testBug55843c() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            cellA2.setCellFormula("IF(NOT(B1=0),((ROW())))");
            CellValue evaluateN = formulaEvaluator.evaluate(cellA2);
            assertEquals("2.0", evaluateN.formatAsString());
        }
    }

    @Test
    void testBug55843d() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            cellA2.setCellFormula("IF(NOT(B1=0),((ROW())),\"\")");
            CellValue evaluateN = formulaEvaluator.evaluate(cellA2);
            assertEquals("2.0", evaluateN.formatAsString());
        }
    }

    @Test
    void testBug55843e() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            cellA2.setCellFormula("IF(B1=0,\"\",((ROW())))");
            CellValue evaluate = formulaEvaluator.evaluate(cellA2);
            assertEquals("2.0", evaluate.formatAsString());
        }
    }

    @Test
    void testBug55843f() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet("test");
            Row row = sheet.createRow(0);
            Row row2 = sheet.createRow(1);
            Cell cellA2 = row2.createCell(0, CellType.FORMULA);
            Cell cellB1 = row.createCell(1, CellType.NUMERIC);
            cellB1.setCellValue(10);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

            cellA2.setCellFormula("IF(B1=0,\"\",IF(B1=10,3,4))");
            CellValue evaluate = formulaEvaluator.evaluate(cellA2);
            assertEquals("3.0", evaluate.formatAsString());
        }
    }

    @Test
    void testBug56655() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            setCellFormula(sheet, 0, 0, "#VALUE!");
            setCellFormula(sheet, 0, 1, "SUMIFS(A:A,A:A,#VALUE!)");

            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            assertEquals(CellType.ERROR, getCell(sheet, 0, 0).getCachedFormulaResultType());
            assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 0).getErrorCellValue());
            assertEquals(CellType.ERROR, getCell(sheet, 0, 1).getCachedFormulaResultType());
            assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 1).getErrorCellValue());
        }
    }

    @Test
    void testBug56655a() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();

            setCellFormula(sheet, 0, 0, "B1*C1");
            sheet.getRow(0).createCell(1).setCellValue("A");
            setCellFormula(sheet, 1, 0, "B1*C1");
            sheet.getRow(1).createCell(1).setCellValue("A");
            setCellFormula(sheet, 0, 3, "SUMIFS(A:A,A:A,A2)");

            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

            assertEquals(CellType.ERROR, getCell(sheet, 0, 0).getCachedFormulaResultType());
            assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 0).getErrorCellValue());
            assertEquals(CellType.ERROR, getCell(sheet, 1, 0).getCachedFormulaResultType());
            assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 1, 0).getErrorCellValue());
            assertEquals(CellType.ERROR, getCell(sheet, 0, 3).getCachedFormulaResultType());
            assertEquals(FormulaError.VALUE.getCode(), getCell(sheet, 0, 3).getErrorCellValue());
        }
    }

    /**
     * @param row 0-based
     * @param column 0-based
     */
    private void setCellFormula(Sheet sheet, int row, int column, String formula) {
        Row r = sheet.getRow(row);
        if (r == null) {
            r = sheet.createRow(row);
        }
        Cell cell = r.getCell(column);
        if (cell == null) {
            cell = r.createCell(column);
        }
        cell.setCellFormula(formula);
    }

    /**
     * @param rowNo 0-based
     * @param column 0-based
     */
    private Cell getCell(Sheet sheet, int rowNo, int column) {
        return sheet.getRow(rowNo).getCell(column);
    }

    @Test
    void testBug61532() throws IOException {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            final Cell cell = wb.createSheet().createRow(0).createCell(0);
            cell.setCellFormula("1+2");

            assertEquals(0, (int)cell.getNumericCellValue());
            assertEquals("1+2", cell.toString());

            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

            CellValue value = eval.evaluate(cell);

            assertEquals(CellType.NUMERIC, value.getCellType());
            assertEquals(3.0, value.getNumberValue(), 0.01);
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("1+2", cell.getCellFormula());
            assertEquals("1+2", cell.toString());

            assertNotNull(eval.evaluateInCell(cell));

            assertEquals("3.0", cell.toString());
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(3.0, cell.getNumericCellValue(), 0.01);
        }
    }

    @Test
    void testFormulaEvaluatorEvaluateSimpleFormulaCell() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            final Row row = wb.createSheet().createRow(0);
            final Cell a1 = row.createCell(0, CellType.NUMERIC);
            a1.setCellValue(1.0);
            final Cell a2 = row.createCell(1, CellType.NUMERIC);
            a2.setCellValue(2.0);
            final Cell a3 = row.createCell(2, CellType.FORMULA);
            final String formula = "SUM(A1:B1)";
            a3.setCellFormula(formula);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            CellType resultType = evaluator.evaluateFormulaCell(a3);
            assertEquals(CellType.NUMERIC, resultType);

            double result = a3.getNumericCellValue();
            // result is correct
            assertTrue(result > 2.0, String.format(Locale.ROOT, "Expected %f to be greater than %f", result, 2.0));
            assertTrue(result < 4.0, String.format(Locale.ROOT, "Expected %f to be less than %f", result, 4.0));

            // ensure that this works for SUM
            assertEquals(CellType.FORMULA, a3.getCellType());
            assertEquals(formula, a3.getCellFormula());
        }
    }

    @Test
    void testFormulaEvaluatorEvaluateVlookupFormulaCell() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            final Sheet mainSheet = wb.createSheet("main");
            final Sheet otherSheet = wb.createSheet("other");
            final Row otherRow1 = otherSheet.createRow(0);
            final Cell label1 = otherRow1.createCell(0, CellType.STRING);
            label1.setCellValue("Thing One");
            final Cell id1 = otherRow1.createCell(1, CellType.STRING);
            id1.setCellValue("1");
            final Row otherRow2 = otherSheet.createRow(1);
            final Cell label2 = otherRow2.createCell(0, CellType.STRING);
            label2.setCellValue("Thing Two");
            final Cell id2 = otherRow2.createCell(1, CellType.STRING);
            id2.setCellValue("2");
            final DataValidationHelper dvHelper = mainSheet.getDataValidationHelper();
            final int maxRows = SpreadsheetVersion.EXCEL2007.getMaxRows() - 1;
            final CellRangeAddressList addressList = new CellRangeAddressList(0, maxRows, 0, 0);
            final String constraint = "'other'!$A:$A";
            final DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint(constraint);
            final DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList);
            dataValidation.setShowErrorBox(true);
            mainSheet.addValidationData(dataValidation);
            wb.setSheetHidden(wb.getSheetIndex(otherSheet), true);
            final Row row = mainSheet.createRow(0);
            final Cell a1 = row.createCell(0, CellType.STRING);
            a1.setCellValue("Thing Two");
            final Cell a2 = row.createCell(1, CellType.FORMULA);
            final String formula = "VLOOKUP(A1,other!A:B,2,FALSE)";
            a2.setCellFormula(formula);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            CellType resultType = evaluator.evaluateFormulaCell(a2);
            assertEquals(CellType.STRING, resultType);

            // result is correct
            String result = a2.getStringCellValue();
            assertEquals("2", result);

            // ensure that this works for vlookup as well
            assertEquals(CellType.FORMULA, a2.getCellType());
            assertEquals(formula, a2.getCellFormula());
        }
    }

    // setting an evaluation of function arguments with the intersect operator (space)
    // see https://bz.apache.org/bugzilla/show_bug.cgi?id=60980
    @Test
    void testIntersectionInFunctionArgs_60980() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();


        Name n1 = wb.createName();
        n1.setNameName("foo");
        n1.setRefersToFormula("A4:B5");
        Name n2 = wb.createName();
        n2.setNameName("bar");
        n2.setRefersToFormula("B4:C5");

        Sheet sheet = wb.createSheet();
        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue(1);
        row3.createCell(1).setCellValue(2);
        row3.createCell(2).setCellValue(3);
        Row row4 = sheet.createRow(4);
        row4.createCell(0).setCellValue(4);
        row4.createCell(1).setCellValue(5);
        row4.createCell(2).setCellValue(6);

        Cell fmla1 = row3.createCell(4);
        fmla1.setCellFormula("SUM(A4:B5 B4:C5)");
        fe.evaluateFormulaCell(fmla1);
        assertEquals(7, fmla1.getNumericCellValue(), 0.000);

        Cell fmla2 = row3.createCell(5);
        fmla2.setCellFormula("SUM(foo bar)");
        fe.evaluateFormulaCell(fmla2);
        assertEquals(7, fmla2.getNumericCellValue(), 0.000);

        wb.close();
    }
}
