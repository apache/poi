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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.MissingArgEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Tests for the INDEX() function.</p>
 *
 * This class contains just a few specific cases that directly invoke {@link Index},
 * with minimum overhead.<br/>
 * Another test: {@link TestIndexFunctionFromSpreadsheet} operates from a higher level
 * and has far greater coverage of input permutations.<br/>
 *
 * @author Josh Micich
 */
public final class TestIndex extends TestCase {

	private static final Index FUNC_INST = new Index();
	private static final double[] TEST_VALUES0 = {
			1, 2,
			3, 4,
			5, 6,
			7, 8,
			9, 10,
			11, 12,
	};

	/**
	 * For the case when the first argument to INDEX() is an area reference
	 */
	public void testEvaluateAreaReference() {

		double[] values = TEST_VALUES0;
		confirmAreaEval("C1:D6", values, 4, 1, 7);
		confirmAreaEval("C1:D6", values, 6, 2, 12);
		confirmAreaEval("C1:D6", values, 3, 1, 5);

		// now treat same data as 3 columns, 4 rows
		confirmAreaEval("C10:E13", values, 2, 2, 5);
		confirmAreaEval("C10:E13", values, 4, 1, 10);
	}

	/**
	 * @param areaRefString in Excel notation e.g. 'D2:E97'
	 * @param dValues array of evaluated values for the area reference
	 * @param rowNum 1-based
	 * @param colNum 1-based, pass -1 to signify argument not present
	 */
	private static void confirmAreaEval(String areaRefString, double[] dValues,
			int rowNum, int colNum, double expectedResult) {
		ValueEval[] values = new ValueEval[dValues.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = new NumberEval(dValues[i]);
		}
		AreaEval arg0 = EvalFactory.createAreaEval(areaRefString, values);

		ValueEval[] args;
		if (colNum > 0) {
			args = new ValueEval[] { arg0, new NumberEval(rowNum), new NumberEval(colNum), };
		} else {
			args = new ValueEval[] { arg0, new NumberEval(rowNum), };
		}

		double actual = NumericFunctionInvoker.invoke(FUNC_INST, args);
		assertEquals(expectedResult, actual, 0D);
	}

	/**
	 * Tests expressions like "INDEX(A1:C1,,2)".<br/>
	 * This problem was found while fixing bug 47048 and is observable up to svn r773441.
	 */
	public void testMissingArg() {
		ValueEval[] values = {
				new NumberEval(25.0),
				new NumberEval(26.0),
				new NumberEval(28.0),
		};
		AreaEval arg0 = EvalFactory.createAreaEval("A10:C10", values);
		ValueEval[] args = new ValueEval[] { arg0, MissingArgEval.instance, new NumberEval(2), };
		ValueEval actualResult;
		try {
			actualResult = FUNC_INST.evaluate(args, 1, (short)1);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Unexpected arg eval type (org.apache.poi.hssf.record.formula.eval.MissingArgEval")) {
				throw new AssertionFailedError("Identified bug 47048b - INDEX() should support missing-arg");
			}
			throw e;
		}
		assertEquals(NumberEval.class, actualResult.getClass());
		assertEquals(26.0, ((NumberEval)actualResult).getNumberValue(), 0.0);
	}
}
