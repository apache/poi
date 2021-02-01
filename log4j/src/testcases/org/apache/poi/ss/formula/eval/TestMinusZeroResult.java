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

package org.apache.poi.ss.formula.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.functions.Function;
import org.junit.jupiter.api.Test;

/**
 * IEEE 754 defines a quantity '-0.0' which is distinct from '0.0'.
 * Negative zero is not easy to observe in Excel, since it is usually converted to 0.0.
 * (Note - the results of XLL add-in functions don't seem to be converted, so they are one
 * reliable avenue to observe Excel's treatment of '-0.0' as an operand.)
 * <p>
 * POI attempts to emulate Excel faithfully, so this class tests
 * two aspects of '-0.0' in formula evaluation:
 * <ol>
 * <li>For most operation results '-0.0' is converted to '0.0'.</li>
 * <li>Comparison operators have slightly different rules regarding '-0.0'.</li>
 * </ol>
 */
final class TestMinusZeroResult {
	private static final double MINUS_ZERO = -0.0;

	@Test
	void testSimpleOperators() {

		// unary plus is a no-op
		checkEval(MINUS_ZERO, UnaryPlusEval.instance, MINUS_ZERO);

		// most simple operators convert -0.0 to +0.0
		checkEval(0.0, EvalInstances.UnaryMinus, 0.0);
		checkEval(0.0, EvalInstances.Percent, MINUS_ZERO);
		checkEval(0.0, EvalInstances.Multiply, MINUS_ZERO, 1.0);
		checkEval(0.0, EvalInstances.Divide, MINUS_ZERO, 1.0);
		checkEval(0.0, EvalInstances.Power, MINUS_ZERO, 1.0);

		// but SubtractEval does not convert -0.0, so '-' and '+' work like java
		checkEval(MINUS_ZERO, EvalInstances.Subtract, MINUS_ZERO, 0.0); // this is the main point of bug 47198
		checkEval(0.0, EvalInstances.Add, MINUS_ZERO, 0.0);
	}

	/**
	 * These results are hard to see in Excel (since -0.0 is usually converted to +0.0 before it
	 * gets to the comparison operator)
	 */
	@Test
	void testComparisonOperators() {
		checkEval(false, EvalInstances.Equal, 0.0, MINUS_ZERO);
		checkEval(true, EvalInstances.GreaterThan, 0.0, MINUS_ZERO);
		checkEval(true, EvalInstances.LessThan, MINUS_ZERO, 0.0);
	}

	@Test
	void testTextRendering() {
		confirmTextRendering(MINUS_ZERO);
		// sub-normal negative numbers also display as '-0'
		confirmTextRendering(Double.longBitsToDouble(0x8000100020003000L));
	}

	/**
	 * Uses {@link ConcatEval} to force number-to-text conversion
	 */
	private static void confirmTextRendering(double d) {
		ValueEval[] args = { StringEval.EMPTY_INSTANCE, new NumberEval(d), };
		StringEval se = (StringEval) EvalInstances.Concat.evaluate(args, -1, (short)-1);
		String result = se.getStringValue();
		assertEquals("-0", result);
	}

	private static void checkEval(double expectedResult, Function instance, double... dArgs) {
		NumberEval result = (NumberEval) evaluate(instance, dArgs);
		assertDouble(expectedResult, result.getNumberValue());
	}
	private static void checkEval(boolean expectedResult, Function instance, double... dArgs) {
		BoolEval result = (BoolEval) evaluate(instance, dArgs);
		assertEquals(expectedResult, result.getBooleanValue());
	}
	private static ValueEval evaluate(Function instance, double... dArgs) {
		ValueEval[] evalArgs;
		evalArgs = new ValueEval[dArgs.length];
		for (int i = 0; i < evalArgs.length; i++) {
			evalArgs[i] = new NumberEval(dArgs[i]);
		}
        return instance.evaluate(evalArgs, -1, (short)-1);
	}

	/**
	 * Not really a POI test - just shows similar behaviour of '-0.0' in Java.
	 */
	@Test
	void testJava() {
		assertEquals(0x8000000000000000L, Double.doubleToLongBits(MINUS_ZERO));

		// The simple operators consider all zeros to be the same
		//noinspection SimplifiableJUnitAssertion,ConstantConditions
		assertTrue(MINUS_ZERO == MINUS_ZERO);
		//noinspection SimplifiableJUnitAssertion,ConstantConditions
		assertTrue(MINUS_ZERO == +0.0);
		//noinspection ConstantConditions
		assertFalse(MINUS_ZERO < +0.0);

		// Double.compare() considers them different
		assertTrue(Double.compare(MINUS_ZERO, +0.0) < 0);

		// multiplying zero by any negative quantity yields minus zero
		assertDouble(MINUS_ZERO, 0.0*-1);
		assertDouble(MINUS_ZERO, 0.0*-1e300);
		assertDouble(MINUS_ZERO, 0.0*-1e-300);

		// minus zero can be produced as a result of underflow
		assertDouble(MINUS_ZERO, -1e-300 / 1e100);

		// multiplying or dividing minus zero by a positive quantity yields minus zero
		assertDouble(MINUS_ZERO, MINUS_ZERO * 1.0);
		assertDouble(MINUS_ZERO, MINUS_ZERO / 1.0);

		// subtracting positive zero gives minus zero
		assertDouble(MINUS_ZERO, MINUS_ZERO - 0.0);
		// BUT adding positive zero gives positive zero
		assertDouble(0.0, MINUS_ZERO + 0.0);  // <<----
	}

	/**
	 * Just so there is no ambiguity.  The two double values have to be exactly equal
	 */
	private static void assertDouble(double a, double b) {
		long bitsA = Double.doubleToLongBits(a);
		long bitsB = Double.doubleToLongBits(b);
		assertEquals(bitsA, bitsB);
	}
}
