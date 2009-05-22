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

/**
 * Test for {@link EqualEval}
 *
 * @author Josh Micich
 */
public final class TestEqualEval extends TestCase {

	/**
	 * Test for bug observable at svn revision 692218 (Sep 2008)<br/>
	 * The value from a 1x1 area should be taken immediately, regardless of srcRow and srcCol
	 */
	public void test1x1AreaOperand() {

		ValueEval[] values = { BoolEval.FALSE, };
		Eval[] args = {
			EvalFactory.createAreaEval("B1:B1", values),
			BoolEval.FALSE,
		};
		Eval result = EqualEval.instance.evaluate(args, 10, (short)20);
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

		Eval[] args = {
			new StringEval(""),
			BlankEval.INSTANCE,
		};
		Eval result = EqualEval.instance.evaluate(args, 10, (short)20);
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
		if (!evalStringCmp("abc", "aBc", EqualEval.instance)) {
			throw new AssertionFailedError("Identified bug 46613");
		}
		assertTrue(evalStringCmp("abc", "aBc", EqualEval.instance));
		assertTrue(evalStringCmp("ABC", "azz", LessThanEval.instance));
		assertTrue(evalStringCmp("abc", "AZZ", LessThanEval.instance));
		assertTrue(evalStringCmp("ABC", "aaa", GreaterThanEval.instance));
		assertTrue(evalStringCmp("abc", "AAA", GreaterThanEval.instance));
	}

	private static boolean evalStringCmp(String a, String b, OperationEval cmpOp) {
		Eval[] args = {
			new StringEval(a),
			new StringEval(b),
		};
		Eval result = cmpOp.evaluate(args, 10, (short)20);
		assertEquals(BoolEval.class, result.getClass());
		BoolEval be = (BoolEval) result;
		return be.getBooleanValue();
	}

	/**
	 * Excel considers -0.0 to be equal to 0.0
	 */
	public void testZeroEquality_bug47198() {
		NumberEval zero = new NumberEval(0.0);
		NumberEval mZero = (NumberEval) UnaryMinusEval.instance.evaluate(new Eval[] { zero, }, 0,
				(short) 0);
		Eval[] args = { zero, mZero, };
		BoolEval result = (BoolEval) EqualEval.instance.evaluate(args, 0, (short) 0);
		if (!result.getBooleanValue()) {
			throw new AssertionFailedError("Identified bug 47198: -0.0 != 0.0");
		}
	}
}
