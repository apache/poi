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

import org.junit.Test;
import org.junit.Ignore;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;

/**
 * Test cases for ROUND(), ROUNDUP(), ROUNDDOWN()
 *
 * @author Josh Micich
 */
public final class TestRoundFuncs {
	// github-43
	// https://github.com/apache/poi/pull/43
    @Ignore("ROUNDUP(3987*0.2, 2) currently fails by returning 797.41")
	@Test
	public void testRoundUp() {
		assertRoundUpEquals(797.40, 3987*0.2, 2, 1e-10);
	}

	@Test
	public void testRoundDown() {
		assertRoundDownEquals(797.40, 3987*0.2, 2, 1e-10);
	}

	@Test
	public void testRound() {
		assertRoundEquals(797.40, 3987*0.2, 2, 1e-10);
	}

	@Test
	public void testRoundDownWithStringArg() {
		ValueEval strArg = new StringEval("abc");
		ValueEval[] args = { strArg, new NumberEval(2), };
		ValueEval result = NumericFunction.ROUNDDOWN.evaluate(args, -1, (short)-1);
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}

	@Test
	public void testRoundUpWithStringArg() {
		ValueEval strArg = new StringEval("abc");
		ValueEval[] args = { strArg, new NumberEval(2), };
		ValueEval result = NumericFunction.ROUNDUP.evaluate(args, -1, (short)-1);
		assertEquals(ErrorEval.VALUE_INVALID, result);
	}



    private static void assertRoundFuncEquals(Function func, double expected, double number, double places, double tolerance) {
		ValueEval[] args = { new NumberEval( number ), new NumberEval(places), };
		NumberEval result = (NumberEval) func.evaluate(args, -1, (short)-1);
		assertEquals(expected, result.getNumberValue(), tolerance);
    }

    private static void assertRoundEquals(double expected, double number, double places, double tolerance) {
		TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUND, expected, number, places, tolerance);
    }

    private static void assertRoundUpEquals(double expected, double number, double places, double tolerance) {
		TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUNDUP, expected, number, places, tolerance);
    }

    private static void assertRoundDownEquals(double expected, double number, double places, double tolerance) {
		TestRoundFuncs.assertRoundFuncEquals(NumericFunction.ROUNDDOWN, expected, number, places, tolerance);
    }
}
