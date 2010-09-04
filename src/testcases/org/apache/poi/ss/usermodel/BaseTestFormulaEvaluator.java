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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * Common superclass for testing implementatiosn of{@link FormulaEvaluator}
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestFormulaEvaluator extends TestCase {

	protected final ITestDataProvider _testDataProvider;

	/**
	 * @param testDataProvider an object that provides test data in  /  specific way
	 */
	protected BaseTestFormulaEvaluator(ITestDataProvider testDataProvider) {
		_testDataProvider = testDataProvider;
	}

    public void testSimpleArithmetic() {
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
    }

    public void testSumCount() {
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
    }

    public void baseTestSharedFormulas(String sampleFile){
        Workbook wb = _testDataProvider.openSampleWorkbook(sampleFile);

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

    /**
     * Test creation / evaluation of formulas with sheet-level names
     */
    public void testSheetLevelFormulas(){
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
    }

    public void testFullColumnRefs() {
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
    public void testEvaluateBlank() {
        Workbook wb = _testDataProvider.createWorkbook();
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        assertNull(fe.evaluate(null));
        Sheet sheet = wb.createSheet("Sheet1");
        Cell cell = sheet.createRow(0).createCell(0);
        assertNull(fe.evaluate(cell));
    }

    /**
     * Test for bug due to attempt to convert a cached formula error result to a boolean
     */
    public void testUpdateCachedFormulaResultFromErrorToNumber_bug46479() {

        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cellA1 = row.createCell(0);
        Cell cellB1 = row.createCell(1);
        cellB1.setCellFormula("A1+1");
        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        cellA1.setCellErrorValue((byte)ErrorConstants.ERROR_NAME);
        fe.evaluateFormulaCell(cellB1);

        cellA1.setCellValue(2.5);
        fe.notifyUpdateCell(cellA1);
        try {
            fe.evaluateInCell(cellB1);
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("Cannot get a numeric value from a error formula cell")) {
                throw new AssertionFailedError("Identified bug 46479a");
            }
        }
        assertEquals(3.5, cellB1.getNumericCellValue(), 0.0);
    }

}