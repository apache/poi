/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Test cases for SUMIFS()
 */
final class TestSumifs {

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 0, 1, 0, null);

	private static ValueEval invokeSumifs(ValueEval[] args) {
		return new Sumifs().evaluate(args, EC);
	}

	private static void confirmDouble(double expected, ValueEval actualEval) {
	    assertTrue(actualEval instanceof NumericValueEval, "Expected numeric result");
		NumericValueEval nve = (NumericValueEval)actualEval;
		assertEquals(expected, nve.getNumberValue(), 0);
	}

    private static void confirm(double expectedResult, ValueEval[] args) {
        confirmDouble(expectedResult, invokeSumifs(args));
    }

    /**
     *  Example 1 from
     *  http://office.microsoft.com/en-us/excel-help/sumifs-function-HA010047504.aspx
     */
    @Test
	void testExample1() {
        // mimic test sample from http://office.microsoft.com/en-us/excel-help/sumifs-function-HA010047504.aspx
        ValueEval[] a2a9 = new ValueEval[] {
                new NumberEval(5),
                new NumberEval(4),
                new NumberEval(15),
                new NumberEval(3),
                new NumberEval(22),
                new NumberEval(12),
                new NumberEval(10),
                new NumberEval(33)
        };

        ValueEval[] b2b9 = new ValueEval[] {
                new StringEval("Apples"),
                new StringEval("Apples"),
                new StringEval("Artichokes"),
                new StringEval("Artichokes"),
                new StringEval("Bananas"),
                new StringEval("Bananas"),
                new StringEval("Carrots"),
                new StringEval("Carrots"),
        };

        ValueEval[] c2c9 = new ValueEval[] {
                new NumberEval(1),
                new NumberEval(2),
                new NumberEval(1),
                new NumberEval(2),
                new NumberEval(1),
                new NumberEval(2),
                new NumberEval(1),
                new NumberEval(2)
        };

        ValueEval[] args;
        // "=SUMIFS(A2:A9, B2:B9, "=A*", C2:C9, 1)"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A9", a2a9),
                EvalFactory.createAreaEval("B2:B9", b2b9),
                new StringEval("A*"),
                EvalFactory.createAreaEval("C2:C9", c2c9),
                new NumberEval(1),
        };
        confirm(20.0, args);

        // "=SUMIFS(A2:A9, B2:B9, "<>Bananas", C2:C9, 1)"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A9", a2a9),
                EvalFactory.createAreaEval("B2:B9", b2b9),
                new StringEval("<>Bananas"),
                EvalFactory.createAreaEval("C2:C9", c2c9),
                new NumberEval(1),
        };
        confirm(30.0, args);

        // a test case that returns ErrorEval.VALUE_INVALID :
        // the dimensions of the first and second criteria ranges are different
        // "=SUMIFS(A2:A9, B2:B8, "<>Bananas", C2:C9, 1)"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A9", a2a9),
                EvalFactory.createAreaEval("B2:B8", new ValueEval[] {
                        new StringEval("Apples"),
                        new StringEval("Apples"),
                        new StringEval("Artichokes"),
                        new StringEval("Artichokes"),
                        new StringEval("Bananas"),
                        new StringEval("Bananas"),
                        new StringEval("Carrots"),
                }),
                new StringEval("<>Bananas"),
                EvalFactory.createAreaEval("C2:C9", c2c9),
                new NumberEval(1),
        };
        assertEquals(ErrorEval.VALUE_INVALID, invokeSumifs(args));

	}

    /**
     *  Example 2 from
     *  http://office.microsoft.com/en-us/excel-help/sumifs-function-HA010047504.aspx
     */
    @Test
    void testExample2() {
        ValueEval[] b2e2 = new ValueEval[] {
                new NumberEval(100),
                new NumberEval(390),
                new NumberEval(8321),
                new NumberEval(500)
        };
        // 1%	0.5%	3%	4%
        ValueEval[] b3e3 = new ValueEval[] {
                new NumberEval(0.01),
                new NumberEval(0.005),
                new NumberEval(0.03),
                new NumberEval(0.04)
        };

        // 1%	1.3%	2.1%	2%
        ValueEval[] b4e4 = new ValueEval[] {
                new NumberEval(0.01),
                new NumberEval(0.013),
                new NumberEval(0.021),
                new NumberEval(0.02)
        };

        // 0.5%	3%	1%	4%
        ValueEval[] b5e5 = new ValueEval[] {
                new NumberEval(0.005),
                new NumberEval(0.03),
                new NumberEval(0.01),
                new NumberEval(0.04)
        };

        ValueEval[] args;

        // "=SUMIFS(B2:E2, B3:E3, ">3%", B4:E4, ">=2%")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("B2:E2", b2e2),
                EvalFactory.createAreaEval("B3:E3", b3e3),
                new StringEval(">0.03"), // 3% in the MSFT example
                EvalFactory.createAreaEval("B4:E4", b4e4),
                new StringEval(">=0.02"),   // 2% in the MSFT example
                EvalFactory.createAreaEval("B5:E5", b5e5),
                new StringEval(">=0.01"),   // 1% in the MSFT example
        };
        confirm(500.0, args);
    }

    /**
     *  Example 3 from
     *  http://office.microsoft.com/en-us/excel-help/sumifs-function-HA010047504.aspx
     */
    @Test
    void testExample3() {
        //3.3	0.8	5.5	5.5
        ValueEval[] b2e2 = new ValueEval[] {
                new NumberEval(3.3),
                new NumberEval(0.8),
                new NumberEval(5.5),
                new NumberEval(5.5)
        };
        // 55	39	39	57.5
        ValueEval[] b3e3 = new ValueEval[] {
                new NumberEval(55),
                new NumberEval(39),
                new NumberEval(39),
                new NumberEval(57.5)
        };

        // 6.5	19.5	6	6.5
        ValueEval[] b4e4 = new ValueEval[] {
                new NumberEval(6.5),
                new NumberEval(19.5),
                new NumberEval(6),
                new NumberEval(6.5)
        };

        ValueEval[] args;

        // "=SUMIFS(B2:E2, B3:E3, ">=40", B4:E4, "<10")"
        args = new ValueEval[]{
                EvalFactory.createAreaEval("B2:E2", b2e2),
                EvalFactory.createAreaEval("B3:E3", b3e3),
                new StringEval(">=40"),
                EvalFactory.createAreaEval("B4:E4", b4e4),
                new StringEval("<10"),
        };
        confirm(8.8, args);
    }

    /**
     *  Example 5 from
     *  http://office.microsoft.com/en-us/excel-help/sumifs-function-HA010047504.aspx
     *
     *  Criteria entered as reference and by using wildcard characters
     */
    @Test
    void testFromFile() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("sumifs.xls");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        HSSFSheet example1 = wb.getSheet("Example 1");
        HSSFCell ex1cell1 = example1.getRow(10).getCell(2);
        fe.evaluate(ex1cell1);
        assertEquals(20.0, ex1cell1.getNumericCellValue(), 0);
        HSSFCell ex1cell2 = example1.getRow(11).getCell(2);
        fe.evaluate(ex1cell2);
        assertEquals(30.0, ex1cell2.getNumericCellValue(), 0);

        HSSFSheet example2 = wb.getSheet("Example 2");
        HSSFCell ex2cell1 = example2.getRow(6).getCell(2);
        fe.evaluate(ex2cell1);
        assertEquals(500.0, ex2cell1.getNumericCellValue(), 0);
        HSSFCell ex2cell2 = example2.getRow(7).getCell(2);
        fe.evaluate(ex2cell2);
        assertEquals(8711.0, ex2cell2.getNumericCellValue(), 0);

        HSSFSheet example3 = wb.getSheet("Example 3");
        HSSFCell ex3cell = example3.getRow(5).getCell(2);
        fe.evaluate(ex3cell);
        assertEquals(8.8, ex3cell.getNumericCellValue(), 0);

        HSSFSheet example4 = wb.getSheet("Example 4");
        HSSFCell ex4cell = example4.getRow(8).getCell(2);
        fe.evaluate(ex4cell);
        assertEquals(3.5, ex4cell.getNumericCellValue(), 0);

        HSSFSheet example5 = wb.getSheet("Example 5");
        HSSFCell ex5cell = example5.getRow(8).getCell(2);
        fe.evaluate(ex5cell);
        assertEquals(625000., ex5cell.getNumericCellValue(), 0);

    }

    @Test
    void testBug56655() {
        ValueEval[] a2a9 = new ValueEval[] {
                new NumberEval(5),
                new NumberEval(4),
                new NumberEval(15),
                new NumberEval(3),
                new NumberEval(22),
                new NumberEval(12),
                new NumberEval(10),
                new NumberEval(33)
        };

        ValueEval[] args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A9", a2a9),
                ErrorEval.VALUE_INVALID,
                new StringEval("A*"),
        };

        ValueEval result = invokeSumifs(args);
        assertTrue(result instanceof ErrorEval, "Expect to have an error when an input is an invalid value, but had: " + result.getClass());

        args = new ValueEval[]{
                EvalFactory.createAreaEval("A2:A9", a2a9),
                EvalFactory.createAreaEval("A2:A9", a2a9),
                ErrorEval.VALUE_INVALID,
        };

        result = invokeSumifs(args);
        assertTrue(result instanceof ErrorEval, "Expect to have an error when an input is an invalid value, but had: " + result.getClass());
    }

    @Test
    void testBug56655b() {
/*
        setCellFormula(sheet, 0, 0, "B1*C1");
        sheet.getRow(0).createCell(1).setCellValue("A");
        setCellFormula(sheet, 1, 0, "B1*C1");
        sheet.getRow(1).createCell(1).setCellValue("A");
        setCellFormula(sheet, 0, 3, "SUMIFS(A:A,A:A,A2)");
 */
    	ValueEval[] a0a1 = new ValueEval[] {
                NumberEval.ZERO,
                NumberEval.ZERO
        };

        ValueEval[] args = new ValueEval[]{
                EvalFactory.createAreaEval("A0:A1", a0a1),
                EvalFactory.createAreaEval("A0:A1", a0a1),
                ErrorEval.VALUE_INVALID
        };

        ValueEval result = invokeSumifs(args);
        assertTrue(result instanceof ErrorEval, "Expect to have an error when an input is an invalid value, but had: " + result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testBug56655c() {
/*
        setCellFormula(sheet, 0, 0, "B1*C1");
        sheet.getRow(0).createCell(1).setCellValue("A");
        setCellFormula(sheet, 1, 0, "B1*C1");
        sheet.getRow(1).createCell(1).setCellValue("A");
        setCellFormula(sheet, 0, 3, "SUMIFS(A:A,A:A,A2)");
 */
        ValueEval[] a0a1 = new ValueEval[] {
                NumberEval.ZERO,
                NumberEval.ZERO
        };

        ValueEval[] args = new ValueEval[]{
                EvalFactory.createAreaEval("A0:A1", a0a1),
                EvalFactory.createAreaEval("A0:A1", a0a1),
                ErrorEval.NAME_INVALID
        };

        ValueEval result = invokeSumifs(args);
        assertTrue(result instanceof ErrorEval, "Expect to have an error when an input is an invalid value, but had: " + result.getClass());
        assertEquals(ErrorEval.NAME_INVALID, result);
    }
}
