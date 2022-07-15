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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Subtotal}
 */
final class TestSubtotal {
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
        ValueEval[] args = {new NumberEval(function), arg1};

        ValueEval result = new Subtotal().evaluate(args, 0, 0);

        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
    }

    @Test
    void testBasics() {
        confirmSubtotal(FUNCTION_SUM, 55.0);
        confirmSubtotal(FUNCTION_AVERAGE, 5.5);
        confirmSubtotal(FUNCTION_COUNT, 10.0);
        confirmSubtotal(FUNCTION_MAX, 10.0);
        confirmSubtotal(FUNCTION_MIN, 1.0);
        confirmSubtotal(FUNCTION_PRODUCT, 3628800.0);
        confirmSubtotal(FUNCTION_STDEV, 3.0276503540974917);
    }

    @Test
     void testAvg() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(1,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue(1);
        Cell b6 = sh.createRow(5).createCell(1);
        b6.setCellValue(7);
        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(1,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(1,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(1,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, b4.getNumericCellValue(), 0);
        assertEquals(8.0, b7.getNumericCellValue(), 0);
        assertEquals(3.0, b8.getNumericCellValue(), 0);
        assertEquals(3.0, b9.getNumericCellValue(), 0);
        wb.close();

    }

    @Test
    void testSum() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(9,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue(1);
        Cell b6 = sh.createRow(5).createCell(1);
        b6.setCellValue(7);
        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(9,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(9,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(9,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(4.0, b4.getNumericCellValue(), 0);
        assertEquals(26.0, b7.getNumericCellValue(), 0);
        assertEquals(12.0, b8.getNumericCellValue(), 0);
        assertEquals(12.0, b9.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    void testCount() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(2,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue("POI");                  // A4 is string and not counted
        /*Cell b6 =*/ sh.createRow(5).createCell(1); // A5 is blank and not counted

        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(2,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(2,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(2,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, b4.getNumericCellValue(), 0);
        assertEquals(6.0, b7.getNumericCellValue(), 0);
        assertEquals(2.0, b8.getNumericCellValue(), 0);
        assertEquals(2.0, b9.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    void testCounta() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(3,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue("POI");                  // A4 is string and not counted
        /*Cell b6 =*/ sh.createRow(5).createCell(1); // A5 is blank and not counted

        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(3,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(3,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(3,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(2.0, b4.getNumericCellValue(), 0);
        assertEquals(8.0, b7.getNumericCellValue(), 0);
        assertEquals(3.0, b8.getNumericCellValue(), 0);
        assertEquals(3.0, b9.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    void testMax() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(4,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue(1);
        Cell b6 = sh.createRow(5).createCell(1);
        b6.setCellValue(7);
        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(4,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(4,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(4,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(3.0, b4.getNumericCellValue(), 0);
        assertEquals(16.0, b7.getNumericCellValue(), 0);
        assertEquals(7.0, b8.getNumericCellValue(), 0);
        assertEquals(7.0, b9.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    void testMin() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(5,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue(1);
        Cell b6 = sh.createRow(5).createCell(1);
        b6.setCellValue(7);
        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(5,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(5,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(5,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(1.0, b4.getNumericCellValue(), 0);
        assertEquals(4.0, b7.getNumericCellValue(), 0);
        assertEquals(1.0, b8.getNumericCellValue(), 0);
        assertEquals(1.0, b9.getNumericCellValue(), 0);
        wb.close();
    }

    @Test
    void testStdev() throws IOException {

        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellValue(3);
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(7,B2:B3)");
        Cell b5 = sh.createRow(4).createCell(1);
        b5.setCellValue(1);
        Cell b6 = sh.createRow(5).createCell(1);
        b6.setCellValue(7);
        Cell b7 = sh.createRow(6).createCell(1);
        b7.setCellFormula("SUBTOTAL(7,B2:B6)*2 + 2");
        Cell b8 = sh.createRow(7).createCell(1);
        b8.setCellFormula("SUBTOTAL(7,B2:B7)");
        Cell b9 = sh.createRow(8).createCell(1);
        b9.setCellFormula("SUBTOTAL(7,B2,B3,B4,B5,B6,B7,B8)");

        fe.evaluateAll();

        assertEquals(1.41421, b4.getNumericCellValue(), 0.00001);
        assertEquals(7.65685, b7.getNumericCellValue(), 0.00001);
        assertEquals(2.82842, b8.getNumericCellValue(), 0.00001);
        assertEquals(2.82842, b9.getNumericCellValue(), 0.00001);
        wb.close();
    }

    @Test
    void testStdevp() throws IOException {

        try (Workbook wb = new HSSFWorkbook()) {
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sh = wb.createSheet();
            Cell b2 = sh.createRow(1).createCell(1);
            b2.setCellValue(1);
            Cell b3 = sh.createRow(2).createCell(1);
            b3.setCellValue(3);
            Cell b4 = sh.createRow(3).createCell(1);
            b4.setCellFormula("SUBTOTAL(8,B2:B3)");
            Cell b5 = sh.createRow(4).createCell(1);
            b5.setCellValue(1);
            Cell b6 = sh.createRow(5).createCell(1);
            b6.setCellValue(7);
            Cell b7 = sh.createRow(6).createCell(1);
            b7.setCellFormula("SUBTOTAL(8,B2:B6)*2 + 2");
            Cell b8 = sh.createRow(7).createCell(1);
            b8.setCellFormula("SUBTOTAL(8,B2:B7)");
            Cell b9 = sh.createRow(8).createCell(1);
            b9.setCellFormula("SUBTOTAL(8,B2,B3,B4,B5,B6,B7,B8)");

            fe.evaluateAll();

            assertEquals(1.0, b4.getNumericCellValue(), 0.00001);
            assertEquals(6.898979, b7.getNumericCellValue(), 0.00001);
            assertEquals(2.44949, b8.getNumericCellValue(), 0.00001);
            assertEquals(2.44949, b9.getNumericCellValue(), 0.00001);
        }
    }

    @Test
    void testVar() throws IOException {

        try (Workbook wb = new HSSFWorkbook()) {
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sh = wb.createSheet();
            Cell b2 = sh.createRow(1).createCell(1);
            b2.setCellValue(1);
            Cell b3 = sh.createRow(2).createCell(1);
            b3.setCellValue(3);
            Cell b4 = sh.createRow(3).createCell(1);
            b4.setCellFormula("SUBTOTAL(10,B2:B3)");
            Cell b5 = sh.createRow(4).createCell(1);
            b5.setCellValue(1);
            Cell b6 = sh.createRow(5).createCell(1);
            b6.setCellValue(7);
            Cell b7 = sh.createRow(6).createCell(1);
            b7.setCellFormula("SUBTOTAL(10,B2:B6)*2 + 2");
            Cell b8 = sh.createRow(7).createCell(1);
            b8.setCellFormula("SUBTOTAL(10,B2:B7)");
            Cell b9 = sh.createRow(8).createCell(1);
            b9.setCellFormula("SUBTOTAL(10,B2,B3,B4,B5,B6,B7,B8)");

            fe.evaluateAll();

            assertEquals(2.0, b4.getNumericCellValue());
            assertEquals(18.0, b7.getNumericCellValue());
            assertEquals(8.0, b8.getNumericCellValue());
            assertEquals(8.0, b9.getNumericCellValue());
        }
    }

    @Test
    void testVarp() throws IOException {

        try (Workbook wb = new HSSFWorkbook()) {
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sh = wb.createSheet();
            Cell b2 = sh.createRow(1).createCell(1);
            b2.setCellValue(1);
            Cell b3 = sh.createRow(2).createCell(1);
            b3.setCellValue(3);
            Cell b4 = sh.createRow(3).createCell(1);
            b4.setCellFormula("SUBTOTAL(11,B2:B3)");
            Cell b5 = sh.createRow(4).createCell(1);
            b5.setCellValue(1);
            Cell b6 = sh.createRow(5).createCell(1);
            b6.setCellValue(7);
            Cell b7 = sh.createRow(6).createCell(1);
            b7.setCellFormula("SUBTOTAL(11,B2:B6)*2 + 2");
            Cell b8 = sh.createRow(7).createCell(1);
            b8.setCellFormula("SUBTOTAL(11,B2:B7)");
            Cell b9 = sh.createRow(8).createCell(1);
            b9.setCellFormula("SUBTOTAL(11,B2,B3,B4,B5,B6,B7,B8)");

            fe.evaluateAll();

            assertEquals(1.0, b4.getNumericCellValue());
            assertEquals(14.0, b7.getNumericCellValue());
            assertEquals(6.0, b8.getNumericCellValue());
            assertEquals(6.0, b9.getNumericCellValue());
        }
    }

    @Test
    void test50209() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Cell b2 = sh.createRow(1).createCell(1);
        b2.setCellValue(1);
        Cell b3 = sh.createRow(2).createCell(1);
        b3.setCellFormula("SUBTOTAL(9,B2)");
        Cell b4 = sh.createRow(3).createCell(1);
        b4.setCellFormula("SUBTOTAL(9,B2:B3)");

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        fe.evaluateAll();
        assertEquals(1.0, b3.getNumericCellValue(), 0);
        assertEquals(1.0, b4.getNumericCellValue(), 0);
        wb.close();
    }

    private static void confirmExpectedResult(FormulaEvaluator evaluator, String msg, Cell cell, double expected) {

        CellValue value = evaluator.evaluate(cell);
        if (value.getErrorValue() != 0)
            throw new RuntimeException(msg + ": " + value.formatAsString());
        assertEquals(expected, value.getNumberValue(), 0, msg);
    }

    @Test
    void testFunctionsFromTestSpreadsheet() throws IOException {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("SubtotalsNested.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        assertEquals(10.0, sheet.getRow(1).getCell(1).getNumericCellValue(), 0, "B2");
        assertEquals(20.0, sheet.getRow(2).getCell(1).getNumericCellValue(), 0, "B3");

        //Test simple subtotal over one area
        Cell cellA3 = sheet.getRow(3).getCell(1);
        confirmExpectedResult(evaluator, "B4", cellA3, 30.0);

        //Test existence of the second area
        assertNotNull(sheet.getRow(1).getCell(2), "C2 must not be null");
        assertEquals(7.0, sheet.getRow(1).getCell(2).getNumericCellValue(), 0, "C2");

        Cell cellC1 = sheet.getRow(1).getCell(3);
        Cell cellC2 = sheet.getRow(2).getCell(3);
        Cell cellC3 = sheet.getRow(3).getCell(3);

        //Test Functions SUM, COUNT and COUNTA calculation of SUBTOTAL
        //a) areas A and B are used
        //b) first 2 subtotals don't consider the value of nested subtotal in A3
        confirmExpectedResult(evaluator, "SUBTOTAL(SUM;B2:B8;C2:C8)", cellC1, 37.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNT;B2:B8,C2:C8)", cellC2, 3.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNTA;B2:B8,C2:C8)", cellC3, 5.0);

        // test same functions ignoring hidden rows over a copy of the same data
        cellC1 = sheet.getRow(11).getCell(3);
        cellC2 = sheet.getRow(12).getCell(3);
        cellC3 = sheet.getRow(13).getCell(3);
        confirmExpectedResult(evaluator, "SUBTOTAL(SUM NO HIDDEN;B22:B28;C22:C28)", cellC1, 17.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNT NO HIDDEN;B22:B28,C22:C28)", cellC2, 2.0);
        confirmExpectedResult(evaluator, "SUBTOTAL(COUNTA NO HIDDEN;B22:B28,C22:C28)", cellC3, 4.0);


        workbook.close();
    }

    @Test
    void testUnimplemented() throws IOException {
        Workbook wb = new HSSFWorkbook();

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sh = wb.createSheet();
        Cell b4 = sh.createRow(3).createCell(1);

        // formula, throws NotImplemnted?
        String[][] formulas = {
            { "SUBTOTAL(0,B2:B3)", null },
            { "SUBTOTAL(9)", FormulaParseException.class.getName() },
            { "SUBTOTAL()", FormulaParseException.class.getName() },
        };

        for (String[] f : formulas) {
            Exception actualEx = null;
            try {
                b4.setCellFormula(f[0]);
                fe.evaluateAll();
                assertEquals(FormulaError.VALUE.getCode(), b4.getErrorCellValue(), f[0]);
            } catch (Exception e) {
                actualEx = e;
            }
            String msg =
                "Check "+(f[1] == null ? "unexpected exception" : f[1])+" here, "+
                "adjust these tests if it was actually implemented - "+f[0];
            assertEquals(f[1], (actualEx == null ? null : actualEx.getClass().getName()), msg);
        }

        Subtotal subtotal = new Subtotal();
        assertEquals(ErrorEval.VALUE_INVALID, subtotal.evaluate(new ValueEval[] {}, 0, 0));

        wb.close();
    }
}
