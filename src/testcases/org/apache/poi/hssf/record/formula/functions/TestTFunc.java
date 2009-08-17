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

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Test cases for Excel function T()
 *
 * @author Josh Micich
 */
public final class TestTFunc extends TestCase {

	/**
	 * @return the result of calling function T() with the specified argument
	 */
	private static ValueEval invokeT(ValueEval arg) {
		ValueEval[] args = { arg, };
		ValueEval result = new T().evaluate(args, -1, (short)-1);
		assertNotNull("result may never be null", result);
		return result;
	}
	/**
	 * Simulates call: T(A1)
	 * where cell A1 has the specified innerValue
	 */
	private ValueEval invokeTWithReference(ValueEval innerValue) {
		ValueEval arg = EvalFactory.createRefEval("$B$2", innerValue);
		return invokeT(arg);
	}

	private static void confirmText(String text) {
		ValueEval arg = new StringEval(text);
		ValueEval eval = invokeT(arg);
		StringEval se = (StringEval) eval;
		assertEquals(text, se.getStringValue());
	}

	public void testTextValues() {

		confirmText("abc");
		confirmText("");
		confirmText(" ");
		confirmText("~");
		confirmText("123");
		confirmText("TRUE");
	}

	private static void confirmError(ValueEval arg) {
		ValueEval eval = invokeT(arg);
		assertTrue(arg == eval);
	}

	public void testErrorValues() {

		confirmError(ErrorEval.VALUE_INVALID);
		confirmError(ErrorEval.NA);
		confirmError(ErrorEval.REF_INVALID);
	}

	private static void confirmString(ValueEval eval, String expected) {
		assertTrue(eval instanceof StringEval);
		assertEquals(expected, ((StringEval)eval).getStringValue());
	}

	private static void confirmOther(ValueEval arg) {
		ValueEval eval = invokeT(arg);
		confirmString(eval, "");
	}

	public void testOtherValues() {
		confirmOther(new NumberEval(2));
		confirmOther(BoolEval.FALSE);
		confirmOther(BlankEval.INSTANCE);  // can this particular case be verified?
	}

	public void testRefValues() {
		ValueEval eval;

		eval = invokeTWithReference(new StringEval("def"));
		confirmString(eval, "def");
		eval = invokeTWithReference(new StringEval(" "));
		confirmString(eval, " ");

		eval = invokeTWithReference(new NumberEval(2));
		confirmString(eval, "");
		eval = invokeTWithReference(BoolEval.TRUE);
		confirmString(eval, "");

		eval = invokeTWithReference(ErrorEval.NAME_INVALID);
		assertTrue(eval == ErrorEval.NAME_INVALID);
	}
}
