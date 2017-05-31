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

package org.apache.poi.ss.formula.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Tests for {@link Subtotal}
 */
public final class TestSubtotal {
	private static final int FUNCTION_AVERAGE = 1;
	private static final int FUNCTION_COUNT = 2;
	private static final int FUNCTION_MAX = 4;
	private static final int FUNCTION_MIN = 5;
	private static final int FUNCTION_PRODUCT = 6;
	private static final int FUNCTION_STDEV = 7;
	private static final int FUNCTION_SUM = 9;

	private static final double[] TEST_VALUES0 = {
		1, 2,
		3, 4,
		5, 6,
		7, 8,
		9, 10
	};

	private static void confirmSubtotal(int function, double expected) {
		ValueEval[] values = new ValueEval[TEST_VALUES0.length];
		for (int i = 0; i < TEST_VALUES0.length; i++) {
			values[i] = new NumberEval(TEST_VALUES0[i]);
		}

		AreaEval arg1 = EvalFactory.createAreaEval("C1:D5", values);
		ValueEval args[] = { new NumberEval(function), arg1 };

		ValueEval result = new Subtotal().evaluate(args, 0, 0);

		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
	}

	@Test
	public void testBasics() {
		confirmSubtotal(FUNCTION_SUM, 55.0);
		confirmSubtotal(FUNCTION_AVERAGE, 5.5);
		confirmSubtotal(FUNCTION_COUNT, 10.0);
		confirmSubtotal(FUNCTION_MAX, 10.0);
		confirmSubtotal(FUNCTION_MIN, 1.0);
		confirmSubtotal(FUNCTION_PRODUCT, 3628800.0);
		confirmSubtotal(FUNCTION_STDEV, 3.0276503540974917);
	}

    @Test
     public void testAvg() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(1,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue(1);
        Cell a5 = sh.createRow(5).createCell(1);
        a5.setCellValue(7);
        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(1,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(1,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(1,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, a3.getNumericCellValue(), 0);
        assertEquals(8.0, a6.getNumericCellValue(), 0);
        assertEquals(3.0, a7.getNumericCellValue(), 0);
        assertEquals(3.0, a8.getNumericCellValue(), 0);
        wb.close();

    }

    @Test
    public void testSum() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(9,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue(1);
        Cell a5 = sh.createRow(5).createCell(1);
        a5.setCellValue(7);
        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(9,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(9,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(9,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(4.0, a3.getNumericCellValue(), 0);
        assertEquals(26.0, a6.getNumericCellValue(), 0);
        assertEquals(12.0, a7.getNumericCellValue(), 0);
        assertEquals(12.0, a8.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    public void testCount() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(2,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue("POI");                  // A4 is string and not counted
        /*Cell a5 =*/ sh.createRow(5).createCell(1); // A5 is blank and not counted

        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(2,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(2,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(2,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, a3.getNumericCellValue(), 0);
        assertEquals(6.0, a6.getNumericCellValue(), 0);
        assertEquals(2.0, a7.getNumericCellValue(), 0);
        assertEquals(2.0, a8.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    public void testCounta() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(3,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue("POI");                  // A4 is string and not counted
        /*Cell a5 =*/ sh.createRow(5).createCell(1); // A5 is blank and not counted

        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(3,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(3,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(3,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, a3.getNumericCellValue(), 0);
        assertEquals(8.0, a6.getNumericCellValue(), 0);
        assertEquals(3.0, a7.getNumericCellValue(), 0);
        assertEquals(3.0, a8.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    public void testMax() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(4,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue(1);
        Cell a5 = sh.createRow(5).createCell(1);
        a5.setCellValue(7);
        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(4,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(4,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(4,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(3.0, a3.getNumericCellValue(), 0);
        assertEquals(16.0, a6.getNumericCellValue(), 0);
        assertEquals(7.0, a7.getNumericCellValue(), 0);
        assertEquals(7.0, a8.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    public void testMin() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(5,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue(1);
        Cell a5 = sh.createRow(5).createCell(1);
        a5.setCellValue(7);
        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(5,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(5,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(5,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(1.0, a3.getNumericCellValue(), 0);
        assertEquals(4.0, a6.getNumericCellValue(), 0);
        assertEquals(1.0, a7.getNumericCellValue(), 0);
        assertEquals(1.0, a8.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    public void testStdev() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellValue(3);
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(7,B2:B3)");
        Cell a4 = sh.createRow(4).createCell(1);
        a4.setCellValue(1);
        Cell a5 = sh.createRow(5).createCell(1);
        a5.setCellValue(7);
        Cell a6 = sh.createRow(6).createCell(1);
        a6.setCellFormula("SUBTOTAL(7,B2:B6)*2 + 2");
        Cell a7 = sh.createRow(7).createCell(1);
        a7.setCellFormula("SUBTOTAL(7,B2:B7)");
        Cell a8 = sh.createRow(8).createCell(1);
        a8.setCellFormula("SUBTOTAL(7,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(1.41421, a3.getNumericCellValue(), 0.0001);
        assertEquals(7.65685, a6.getNumericCellValue(), 0.0001);
        assertEquals(2.82842, a7.getNumericCellValue(), 0.0001);
        assertEquals(2.82842, a8.getNumericCellValue(), 0.0001);
        wb.close();
    }

    @Test
    public void test50209() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Cell a1 = sh.createRow(1).createCell(1);
        a1.setCellValue(1);
        Cell a2 = sh.createRow(2).createCell(1);
        a2.setCellFormula("SUBTOTAL(9,B2)");
        Cell a3 = sh.createRow(3).createCell(1);
        a3.setCellFormula("SUBTOTAL(9,B2:B3)");

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        fe.evaluateAll();
        assertEquals(1.0, a2.getNumericCellValue(), 0);
        assertEquals(1.0, a3.getNumericCellValue(), 0);
        wb.close();
    }

    private static void confirmExpectedResult(FormulaEvaluator evaluator, String msg, Cell cell, double expected) {

        CellValue value = evaluator.evaluate(cell);
        if (value.getErrorValue() != 0)
            throw new RuntimeException(msg + ": " + value.formatAsString());
        assertEquals(msg, expected, value.getNumberValue(), 0);
    }

    @Test
    public void testFunctionsFromTestSpreadsheet() throws IOException {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("SubtotalsNested.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        assertEquals("B2", 10.0, sheet.getRow(1).getCell(1).getNumericCellValue(), 0);
        assertEquals("B3", 20.0, sheet.getRow(2).getCell(1).getNumericCellValue(), 0);

        //Test simple subtotal over one area
        Cell cellA3 = sheet.getRow(3).getCell(1);
        confirmExpectedResult(evaluator, "B4", cellA3, 30.0);

        //Test existence of the second area
        assertNotNull("C2 must not be null", sheet.getRow(1).getCell(2));
        assertEquals("C2", 7.0, sheet.getRow(1).getCell(2).getNumericCellValue(), 0);

        Cell cellC1 = sheet.getRow(1).getCell(3);
        Cell cellC2 = sheet.getRow(2).getCell(3);
        Cell cellC3 = sheet.getRow(3).getCell(3);

        //Test Functions SUM, COUNT and COUNTA calculation of SUBTOTAL
        //a) areas A and B are used
        //b) first 2 subtotals don't consider the value of nested subtotal in A3
        confirmExpectedResult(evaluator, "SUBTOTAL(SUM;B2:B8;C2:C8)", cellC1, 37.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNT;B2:B8,C2:C8)", cellC2, 3.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNTA;B2:B8,C2:C8)", cellC3, 5.0);
    
        workbook.close();
    }

    @Test
    public void testUnimplemented() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell a3 = sh.createRow(3).createCell(1);
        
        // formula, throws NotImplemnted?
        String[][] formulas = {
            { "SUBTOTAL(8,B2:B3)", NotImplementedException.class.getName() },
            { "SUBTOTAL(10,B2:B3)", NotImplementedException.class.getName() },
            { "SUBTOTAL(11,B2:B3)", NotImplementedException.class.getName() },
            { "SUBTOTAL(107,B2:B3)", NotImplementedException.class.getName() },
            { "SUBTOTAL(0,B2:B3)", null },
            { "SUBTOTAL(9)", FormulaParseException.class.getName() },
            { "SUBTOTAL()", FormulaParseException.class.getName() },
        };
        
        for (String[] f : formulas) {
            Exception actualEx = null;
            try {
                a3.setCellFormula(f[0]);
                fe.evaluateAll();
                assertEquals(FormulaError.VALUE.getCode(), a3.getErrorCellValue());
            } catch (Exception e) {
                actualEx = e;
            }
            String msg =
                "Check "+(f[1] == null ? "unexpected exception" : f[1])+" here, "+
                "adjust these tests if it was actually implemented - "+f[0];
            assertEquals(msg, f[1], (actualEx == null ? null : actualEx.getClass().getName()));
        }

        Subtotal subtotal = new Subtotal();
        assertEquals(ErrorEval.VALUE_INVALID, subtotal.evaluate(new ValueEval[] {}, 0, 0));
        
        wb.close();
    }
}
