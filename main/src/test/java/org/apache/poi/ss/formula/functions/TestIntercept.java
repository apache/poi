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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Test for Excel function INTERCEPT()
 *
 * @author Johan Karlsteen
 */
final class TestIntercept {
	private static final Function INTERCEPT = new Intercept();

	private static ValueEval invoke(Function function, ValueEval xArray, ValueEval yArray) {
		ValueEval[] args = new ValueEval[] { xArray, yArray, };
		return function.evaluate(args, -1, (short)-1);
	}

	private void confirm(Function function, ValueEval xArray, ValueEval yArray, double expected) {
		ValueEval result = invoke(function, xArray, yArray);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval)result).getNumberValue(), 0);
	}
	private void confirmError(Function function, ValueEval xArray, ValueEval yArray, ErrorEval expectedError) {
		ValueEval result = invoke(function, xArray, yArray);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(expectedError.getErrorCode(), ((ErrorEval)result).getErrorCode());
	}

	private void confirmError(ValueEval xArray, ValueEval yArray, ErrorEval expectedError) {
		confirmError(INTERCEPT, xArray, yArray, expectedError);
	}

	@Test
	void testBasic() {
		Double exp = Math.pow(10, 7.5);
		ValueEval[] yValues = {
			new NumberEval(3+exp),
			new NumberEval(4+exp),
			new NumberEval(2+exp),
			new NumberEval(5+exp),
			new NumberEval(4+exp),
			new NumberEval(7+exp),
		};
		ValueEval areaEvalY = createAreaEval(yValues);

		ValueEval[] xValues = {
			new NumberEval(1),
			new NumberEval(2),
			new NumberEval(3),
			new NumberEval(4),
			new NumberEval(5),
			new NumberEval(6),
		};
		ValueEval areaEvalX = createAreaEval(xValues);
		confirm(INTERCEPT, areaEvalX, areaEvalY, -24516534.39905822);
		// Excel 2010 gives -24516534.3990583
	}

	/**
	 * number of items in array is not limited to 30
	 */
	@Test
	void testLargeArrays() {
		ValueEval[] yValues = createMockNumberArray(100, 3); // [1,2,0,1,2,0,...,0,1]
		yValues[0] = new NumberEval(2.0); // Changes first element to 2
		ValueEval[] xValues = createMockNumberArray(100, 101); // [1,2,3,4,...,99,100]

		confirm(INTERCEPT, createAreaEval(xValues), createAreaEval(yValues), 51.74384236453202);
		// Excel 2010 gives 51.74384236453200
	}

	private ValueEval[] createMockNumberArray(int size, double value) {
		ValueEval[] result = new ValueEval[size];
		for (int i = 0; i < result.length; i++) {
			result[i] = new NumberEval((i+1)%value);
		}
		return result;
	}

	private static ValueEval createAreaEval(ValueEval[] values) {
		String refStr = "A1:A" + values.length;
		return EvalFactory.createAreaEval(refStr, values);
	}

	@Test
	void testErrors() {
		ValueEval[] xValues = {
				ErrorEval.REF_INVALID,
				new NumberEval(2),
		};
		ValueEval areaEvalX = createAreaEval(xValues);
		ValueEval[] yValues = {
				new NumberEval(2),
				ErrorEval.NULL_INTERSECTION,
		};
		ValueEval areaEvalY = createAreaEval(yValues);
		ValueEval[] zValues = { // wrong size
				new NumberEval(2),
		};
		ValueEval areaEvalZ = createAreaEval(zValues);

		// if either arg is an error, that error propagates
		confirmError(ErrorEval.REF_INVALID, ErrorEval.NAME_INVALID, ErrorEval.REF_INVALID);
		confirmError(areaEvalX, ErrorEval.NAME_INVALID, ErrorEval.NAME_INVALID);
		confirmError(ErrorEval.NAME_INVALID, areaEvalX, ErrorEval.NAME_INVALID);

		// array sizes must match
		confirmError(areaEvalX, areaEvalZ, ErrorEval.NA);
		confirmError(areaEvalZ, areaEvalY, ErrorEval.NA);

		// any error in an array item propagates up
		confirmError(areaEvalX, areaEvalX, ErrorEval.REF_INVALID);

		// search for errors array by array, not pair by pair
		confirmError(areaEvalX, areaEvalY, ErrorEval.NULL_INTERSECTION);
		confirmError(areaEvalY, areaEvalX, ErrorEval.REF_INVALID);
	}

    /**
     *  Example from
     *  http://office.microsoft.com/en-us/excel-help/intercept-function-HP010062512.aspx?CTT=5&origin=HA010277524
     */
	@Test
    void testFromFile() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("intercept.xls");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        HSSFSheet example1 = wb.getSheet("Example 1");
        HSSFCell a8 = example1.getRow(7).getCell(0);
        assertEquals("INTERCEPT(A2:A6,B2:B6)", a8.getCellFormula());
        fe.evaluate(a8);
        assertEquals(0.048387097, a8.getNumericCellValue(), 0.000000001);

    }
}