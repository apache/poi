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
 * Tests for Excel function LEN()
 *
 * @author Josh Micich
 */
public final class TestLen extends TestCase {

	private static ValueEval invokeLen(ValueEval text) {
		ValueEval[] args = new ValueEval[] { text, };
		return TextFunction.LEN.evaluate(args, -1, (short)-1);
	}

	private void confirmLen(ValueEval text, int expected) {
		ValueEval result = invokeLen(text);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval)result).getNumberValue(), 0);
	}

	private void confirmLen(ValueEval text, ErrorEval expectedError) {
		ValueEval result = invokeLen(text);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(expectedError.getErrorCode(), ((ErrorEval)result).getErrorCode());
	}

	public void testBasic() {

		confirmLen(new StringEval("galactic"), 8);
	}

	/**
	 * Valid cases where text arg is not exactly a string
	 */
	public void testUnusualArgs() {

		// text (first) arg type is number, other args are strings with fractional digits
		confirmLen(new NumberEval(123456), 6);
		confirmLen(BoolEval.FALSE, 5);
		confirmLen(BoolEval.TRUE, 4);
		confirmLen(BlankEval.INSTANCE, 0);
	}

	public void testErrors() {
		confirmLen(ErrorEval.NAME_INVALID, ErrorEval.NAME_INVALID);
	}
}
