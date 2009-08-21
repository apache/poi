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

package org.apache.poi.hssf.record.formula.eval;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.functions.EvalFactory;
import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * Test for {@link EqualEval}
 *
 * @author Josh Micich
 */
public final class TestEqualEval extends TestCase {
	// convenient access to namepace
	private static final EvalInstances EI = null;

	/**
	 * Test for bug observable at svn revision 692218 (Sep 2008)<br/>
	 * The value from a 1x1 area should be taken immediately, regardless of srcRow and srcCol
	 */
	public void test1x1AreaOperand() {

		ValueEval[] values = { BoolEval.FALSE, };
		ValueEval[] args = {
			EvalFactory.createAreaEval("B1:B1", values),
			BoolEval.FALSE,
		};
		ValueEval result = evaluate(EI.Equal, args, 10, 10);
		if (result instanceof ErrorEval) {
			if (result == ErrorEval.VALUE_INVALID) {
				throw new AssertionFailedError("Identified bug in evaluation of 1x1 area");
			}
		}
		assertEquals(BoolEval.class, result.getClass());
		assertTrue(((BoolEval)result).getBooleanValue());
	}
	/**
	 * Empty string is equal to blank
	 */
	public void testBlankEqualToEmptyString() {

		ValueEval[] args = {
			new StringEval(""),
			BlankEval.INSTANCE,
		};
		ValueEval result = evaluate(EI.Equal, args, 10, 10);
		assertEquals(BoolEval.class, result.getClass());
		BoolEval be = (BoolEval) result;
		if (!be.getBooleanValue()) {
			throw new AssertionFailedError("Identified bug blank/empty string equality");
		}
		assertTrue(be.getBooleanValue());
	}

	/**
	 * Test for bug 46613 (observable at svn r737248)
	 */
	public void testStringInsensitive_bug46613() {
		if (!evalStringCmp("abc", "aBc", EI.Equal)) {
			throw new AssertionFailedError("Identified bug 46613");
		}
		assertTrue(evalStringCmp("abc", "aBc", EI.Equal));
		assertTrue(evalStringCmp("ABC", "azz", EI.LessThan));
		assertTrue(evalStringCmp("abc", "AZZ", EI.LessThan));
		assertTrue(evalStringCmp("ABC", "aaa", EI.GreaterThan));
		assertTrue(evalStringCmp("abc", "AAA", EI.GreaterThan));
	}

	private static boolean evalStringCmp(String a, String b, Function cmpOp) {
		ValueEval[] args = {
			new StringEval(a),
			new StringEval(b),
		};
		ValueEval result = evaluate(cmpOp, args, 10, 20);
		assertEquals(BoolEval.class, result.getClass());
		BoolEval be = (BoolEval) result;
		return be.getBooleanValue();
	}

	/**
	 * Bug 47198 involved a formula "-A1=0" where cell A1 was 0.0.
	 * Excel evaluates "-A1=0" to TRUE, not because it thinks -0.0==0.0
	 * but because "-A1" evaluated to +0.0
	 * <p/>
	 * Note - the original diagnosis of bug 47198 was that
	 * "Excel considers -0.0 to be equal to 0.0" which is NQR
	 * See {@link TestMinusZeroResult} for more specific tests regarding -0.0.
	 */
	public void testZeroEquality_bug47198() {
		NumberEval zero = new NumberEval(0.0);
		NumberEval mZero = (NumberEval) evaluate(UnaryMinusEval.instance, new ValueEval[] { zero, }, 0, 0);
		if (Double.doubleToLongBits(mZero.getNumberValue()) == 0x8000000000000000L) {
			throw new AssertionFailedError("Identified bug 47198: unary minus should convert -0.0 to 0.0");
		}
		ValueEval[] args = { zero, mZero, };
		BoolEval result = (BoolEval) evaluate(EI.Equal, args, 0, 0);
		if (!result.getBooleanValue()) {
			throw new AssertionFailedError("Identified bug 47198: -0.0 != 0.0");
		}
	}

	public void testRounding_bug47598() {
		double x = 1+1.0028-0.9973; // should be 1.0055, but has IEEE rounding
		assertFalse(x == 1.0055);

		NumberEval a = new NumberEval(x);
		NumberEval b = new NumberEval(1.0055);
		assertEquals("1.0055", b.getStringValue());

		ValueEval[] args = { a, b, };
		BoolEval result = (BoolEval) evaluate(EI.Equal, args, 0, 0);
		if (!result.getBooleanValue()) {
			throw new AssertionFailedError("Identified bug 47598: 1+1.0028-0.9973 != 1.0055");
		}
	}

	private static ValueEval evaluate(Function oper, ValueEval[] args, int srcRowIx, int srcColIx) {
		return oper.evaluate(args, srcRowIx, (short) srcColIx);
	}
}
