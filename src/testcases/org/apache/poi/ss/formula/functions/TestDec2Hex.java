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

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Dec2Hex}
 *
 * @author cedric dot walter @ gmail dot com
 */
final class TestDec2Hex {

	private static ValueEval invokeValue(String number1, String number2) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2), };
		return new Dec2Hex().evaluate(args, -1, -1);
	}

    private static ValueEval invokeBack(String number1) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1) };
        return new Hex2Dec().evaluate(args, -1, -1);
    }

    private static ValueEval invokeValue(String number1) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1), };
		return new Dec2Hex().evaluate(args, -1, -1);
	}

	private static void confirmValue(String msg, String number1, String number2, String expected) {
		ValueEval result = invokeValue(number1, number2);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(expected, ((StringEval) result).getStringValue(), msg);
	}

    private static void confirmValue(String msg, String number1, String expected) {
		ValueEval result = invokeValue(number1);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(expected, ((StringEval) result).getStringValue(), msg);
	}

    private static void confirmValueError(String msg, String number1, String number2, ErrorEval numError) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(numError, result, msg);
    }

    @Test
	void testBasic() {
		confirmValue("Converts decimal 100 to hexadecimal with 0 characters (64)", "100","0", "64");
		confirmValue("Converts decimal 100 to hexadecimal with 4 characters (0064)", "100","4", "0064");
		confirmValue("Converts decimal 100 to hexadecimal with 5 characters (0064)", "100","5", "00064");
		confirmValue("Converts decimal 100 to hexadecimal with 10 (default) characters", "100","10", "0000000064");
		confirmValue("If argument places contains a decimal value, dec2hex ignores the numbers to the right side of the decimal point.", "100","10.0", "0000000064");

		confirmValue("Converts decimal -54 to hexadecimal, 2 is ignored","-54", "2",  "FFFFFFFFCA");
		confirmValue("places is optionnal","-54", "FFFFFFFFCA");

		confirmValue("Converts normal decimal number to hexadecimal", "100", "64");

		String maxInt = Integer.toString(Integer.MAX_VALUE);
        assertEquals("2147483647", maxInt);
        confirmValue("Converts INT_MAX to hexadecimal", maxInt, "7FFFFFFF");

        String minInt = Integer.toString(Integer.MIN_VALUE);
        assertEquals("-2147483648", minInt);
        confirmValue("Converts INT_MIN to hexadecimal", minInt, "FF80000000");

        String maxIntPlusOne = Long.toString(((long)Integer.MAX_VALUE)+1);
        assertEquals("2147483648", maxIntPlusOne);
        confirmValue("Converts INT_MAX + 1 to hexadecimal", maxIntPlusOne, "80000000");

        String maxLong = Long.toString(549755813887L);
        assertEquals("549755813887", maxLong);
        confirmValue("Converts the max supported value to hexadecimal", maxLong, "7FFFFFFFFF");

        String minLong = Long.toString(-549755813888L);
        assertEquals("-549755813888", minLong);
        confirmValue("Converts the min supported value to hexadecimal", minLong, "FF80000000");
	}

    @Test
    void testErrors() {
        confirmValueError("Out of range min number","-549755813889","0", ErrorEval.NUM_ERROR);
        confirmValueError("Out of range max number","549755813888","0", ErrorEval.NUM_ERROR);

        confirmValueError("negative places not allowed","549755813888","-10", ErrorEval.NUM_ERROR);
        confirmValueError("non number places not allowed","ABCDEF","0", ErrorEval.VALUE_INVALID);
    }

    @Test
    void testEvalOperationEvaluationContextFails() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ErrorEval.VALUE_INVALID };
        ValueEval result = new Dec2Hex().evaluate(args, ctx);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private OperationEvaluationContext createContext() {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("123.43");
        cell = row.createCell(1);
        cell.setCellValue("8");
        cell = row.createCell(2);
        cell.setCellValue("-8");

        HSSFEvaluationWorkbook workbook = HSSFEvaluationWorkbook.create(wb);
        WorkbookEvaluator workbookEvaluator = new WorkbookEvaluator(workbook, (sheetIndex, rowIndex, columnIndex) -> true, null);
        return new OperationEvaluationContext(workbookEvaluator, workbook, 0, 0, 0, null);
    }

    @Test
    void testRefs() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0) };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(StringEval.class, result.getClass(), "Had: " + result);
        assertEquals("7B", ((StringEval) result).getStringValue());
    }

    @Test
    void testWithPlacesIntInt() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(StringEval.class, result.getClass(), "Had: " + result);
        assertEquals("0000007B", ((StringEval) result).getStringValue());
    }

    @Test
    void testWithPlaces() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Hex().evaluate(args, ctx);

        assertEquals(StringEval.class, result.getClass(), "Had: " + result);
        assertEquals("0000007B", ((StringEval) result).getStringValue());
    }

    @Test
    void testWithTooManyParamsIntInt() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testWithTooManyParams() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Hex().evaluate(args, ctx);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testWithErrorPlaces() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ErrorEval.NULL_INTERSECTION };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NULL_INTERSECTION, result);
    }

    @Test
    void testWithNegativePlaces() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 2) };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    @Test
    void testWithEmptyPlaces() {
        OperationEvaluationContext ctx = createContext();

        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(1, 0) };
        ValueEval result = new Dec2Hex().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    @Test
    void testBackAndForth() {
        for (int i = -512; i < 512; i++) {
            ValueEval result = invokeValue(Integer.toString(i));
            assertEquals(StringEval.class, result.getClass(), "Had: " + result);

            ValueEval back = invokeBack(((StringEval) result).getStringValue());
            assertEquals(NumberEval.class, back.getClass(), "Had: " + back);

            assertEquals(Integer.toString(i), ((NumberEval) back).getStringValue());
        }
    }
}
