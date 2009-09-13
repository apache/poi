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
 * Tests for Excel function TRIM()
 *
 * @author Josh Micich
 */
public final class TestTrim extends TestCase {


	private static ValueEval invokeTrim(ValueEval text) {
		ValueEval[] args = new ValueEval[] { text, };
		return TextFunction.TRIM.evaluate(args, -1, (short)-1);
	}

	private void confirmTrim(ValueEval text, String expected) {
		ValueEval result = invokeTrim(text);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(expected, ((StringEval)result).getStringValue());
	}

	private void confirmTrim(ValueEval text, ErrorEval expectedError) {
		ValueEval result = invokeTrim(text);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(expectedError.getErrorCode(), ((ErrorEval)result).getErrorCode());
	}

	public void testBasic() {

		confirmTrim(new StringEval(" hi "), "hi");
		confirmTrim(new StringEval("hi "), "hi");
		confirmTrim(new StringEval("  hi"), "hi");
		confirmTrim(new StringEval(" hi there  "), "hi there");
		confirmTrim(new StringEval(""), "");
		confirmTrim(new StringEval("   "), "");
	}

	/**
	 * Valid cases where text arg is not exactly a string
	 */
	public void testUnusualArgs() {

		// text (first) arg type is number, other args are strings with fractional digits
		confirmTrim(new NumberEval(123456), "123456");
		confirmTrim(BoolEval.FALSE, "FALSE");
		confirmTrim(BoolEval.TRUE, "TRUE");
		confirmTrim(BlankEval.INSTANCE, "");
	}

	public void testErrors() {
		confirmTrim(ErrorEval.NAME_INVALID, ErrorEval.NAME_INVALID);
	}
}
