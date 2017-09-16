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

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.IStabilityClassifier;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Tests for {@link Dec2Bin}
 *
 * @author cedric dot walter @ gmail dot com
 */
public final class TestDec2Bin extends TestCase {

    private static ValueEval invokeValue(String number1) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1) };
		return new Dec2Bin().evaluate(args, -1, -1);
	}

    private static ValueEval invokeBack(String number1) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1) };
        return new Bin2Dec().evaluate(args, -1, -1);
    }

    private static void confirmValue(String msg, String number1, String expected) {
		ValueEval result = invokeValue(number1);
		assertEquals("Had: " + result, StringEval.class, result.getClass());
		assertEquals(msg, expected, ((StringEval) result).getStringValue());
	}

    private static void confirmValueError(String msg, String number1, ErrorEval numError) {
        ValueEval result = invokeValue(number1);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(msg, numError, result);
    }

	public void testBasic() {
		confirmValue("Converts binary '00101' from binary (5)", "5", "101");
		confirmValue("Converts binary '1111111111' from binary (-1)", "-1",    "1111111111");
		confirmValue("Converts binary '1111111110' from binary (-2)", "-2",    "1111111110");
        confirmValue("Converts binary '0111111111' from binary (511)", "511",   "111111111");
        confirmValue("Converts binary '1000000000' from binary (511)", "-512", "1000000000");
	}

    public void testErrors() {
        confirmValueError("fails for >= 512 or < -512","512", ErrorEval.NUM_ERROR);
        confirmValueError("fails for >= 512 or < -512","-513", ErrorEval.NUM_ERROR);
        confirmValueError("not a valid decimal number","GGGGGGG", ErrorEval.VALUE_INVALID);
        confirmValueError("not a valid decimal number","3.14159a", ErrorEval.VALUE_INVALID);
    }

    public void testEvalOperationEvaluationContext() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0) };
        ValueEval result = new Dec2Bin().evaluate(args, ctx);

        assertEquals(StringEval.class, result.getClass());
        assertEquals("1101", ((StringEval) result).getStringValue());
    }

    public void testEvalOperationEvaluationContextFails() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ErrorEval.VALUE_INVALID };
        ValueEval result = new Dec2Bin().evaluate(args, ctx);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private OperationEvaluationContext createContext() {
        HSSFWorkbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("13.43");
        cell = row.createCell(1);
        cell.setCellValue("8");
        cell = row.createCell(2);
        cell.setCellValue("-8");
        cell = row.createCell(3);
        cell.setCellValue("1");

        HSSFEvaluationWorkbook workbook = HSSFEvaluationWorkbook.create(wb);
        WorkbookEvaluator workbookEvaluator = new WorkbookEvaluator(workbook, new IStabilityClassifier() {
            
            @Override
            public boolean isCellFinal(int sheetIndex, int rowIndex, int columnIndex) {
                return true;
            }
        }, null);
        return new OperationEvaluationContext(workbookEvaluator,
                workbook, 0, 0, 0, null);
    }

    public void testRefs() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals("Had: " + result, StringEval.class, result.getClass());
        assertEquals("1101", ((StringEval) result).getStringValue());
    }

    public void testWithPlacesIntInt() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals("Had: " + result, StringEval.class, result.getClass());
        // TODO: documentation and behavior do not match here!
        assertEquals("1101", ((StringEval) result).getStringValue());
    }

    public void testWithPlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Bin().evaluate(args, ctx);

        assertEquals("Had: " + result, StringEval.class, result.getClass());
        // TODO: documentation and behavior do not match here!
        assertEquals("1101", ((StringEval) result).getStringValue());
    }

    public void testWithToShortPlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 3) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    public void testWithTooManyParamsIntInt() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    public void testWithTooManyParams() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 1), ctx.getRefEval(0, 1) };
        ValueEval result = new Dec2Bin().evaluate(args, ctx);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    public void testWithErrorPlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ErrorEval.NULL_INTERSECTION };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NULL_INTERSECTION, result);
    }

    public void testWithNegativePlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(0, 2) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    public void testWithZeroPlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), new NumberEval(0.0) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

    public void testWithEmptyPlaces() {
        OperationEvaluationContext ctx = createContext();
        
        ValueEval[] args = new ValueEval[] { ctx.getRefEval(0, 0), ctx.getRefEval(1, 0) };
        ValueEval result = new Dec2Bin().evaluate(args, -1, -1);

        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }
    
    public void testBackAndForth() {
        for (int i = -512; i < 512; i++) {
            ValueEval result = invokeValue(Integer.toString(i));
            assertEquals("Had: " + result, StringEval.class,
                    result.getClass());

            ValueEval back = invokeBack(((StringEval) result).getStringValue());
            assertEquals("Had: " + back, NumberEval.class,
                    back.getClass());

            assertEquals(Integer.toString(i),
                    ((NumberEval) back).getStringValue());
        }
    }
}
