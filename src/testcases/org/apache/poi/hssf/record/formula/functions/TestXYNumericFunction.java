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

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
/**
 * Tests for Excel functions SUMX2MY2(), SUMX2PY2(), SUMXMY2()
 *
 * @author Josh Micich
 */
public final class TestXYNumericFunction extends TestCase {
	private static final Function SUM_SQUARES = new Sumx2py2();
	private static final Function DIFF_SQUARES = new Sumx2my2();
	private static final Function SUM_SQUARES_OF_DIFFS = new Sumxmy2();

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
		confirmError(SUM_SQUARES, xArray, yArray, expectedError);
		confirmError(DIFF_SQUARES, xArray, yArray, expectedError);
		confirmError(SUM_SQUARES_OF_DIFFS, xArray, yArray, expectedError);
	}

	public void testBasic() {
		ValueEval[] xValues = {
			new NumberEval(1),
			new NumberEval(2),
		};
		ValueEval areaEvalX = createAreaEval(xValues);
		confirm(SUM_SQUARES, areaEvalX, areaEvalX, 10.0);
		confirm(DIFF_SQUARES, areaEvalX, areaEvalX, 0.0);
		confirm(SUM_SQUARES_OF_DIFFS, areaEvalX, areaEvalX, 0.0);

		ValueEval[] yValues = {
			new NumberEval(3),
			new NumberEval(4),
		};
		ValueEval areaEvalY = createAreaEval(yValues);
		confirm(SUM_SQUARES, areaEvalX, areaEvalY, 30.0);
		confirm(DIFF_SQUARES, areaEvalX, areaEvalY, -20.0);
		confirm(SUM_SQUARES_OF_DIFFS, areaEvalX, areaEvalY, 8.0);
	}

	/**
	 * number of items in array is not limited to 30
	 */
	public void testLargeArrays() {
		ValueEval[] xValues = createMockNumberArray(100, 3);
		ValueEval[] yValues = createMockNumberArray(100, 2);

		confirm(SUM_SQUARES, createAreaEval(xValues), createAreaEval(yValues), 1300.0);
		confirm(DIFF_SQUARES, createAreaEval(xValues), createAreaEval(yValues), 500.0);
		confirm(SUM_SQUARES_OF_DIFFS, createAreaEval(xValues), createAreaEval(yValues), 100.0);
	}


	private ValueEval[] createMockNumberArray(int size, double value) {
		ValueEval[] result = new ValueEval[size];
		for (int i = 0; i < result.length; i++) {
			result[i] = new NumberEval(value);
		}
		return result;
	}

	private static ValueEval createAreaEval(ValueEval[] values) {
		String refStr = "A1:A" + values.length;
		return EvalFactory.createAreaEval(refStr, values);
	}

	public void testErrors() {
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
		confirmError(areaEvalX, areaEvalY, ErrorEval.REF_INVALID);
		confirmError(areaEvalY, areaEvalX, ErrorEval.NULL_INTERSECTION);
	}
}
