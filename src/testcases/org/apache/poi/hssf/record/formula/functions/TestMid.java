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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
/**
 * Tests for Excel function MID()
 *
 * @author Josh Micich
 */
public final class TestMid extends TestCase {


	private static ValueEval invokeMid(ValueEval text, ValueEval startPos, ValueEval numChars) {
		ValueEval[] args = new ValueEval[] { text, startPos, numChars, };
		return TextFunction.MID.evaluate(args, -1, (short)-1);
	}

	private void confirmMid(ValueEval text, ValueEval startPos, ValueEval numChars, String expected) {
		ValueEval result = invokeMid(text, startPos, numChars);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(expected, ((StringEval)result).getStringValue());
	}

	private void confirmMid(ValueEval text, ValueEval startPos, ValueEval numChars, ErrorEval expectedError) {
		ValueEval result = invokeMid(text, startPos, numChars);
		assertEquals(ErrorEval.class, result.getClass());
		assertEquals(expectedError.getErrorCode(), ((ErrorEval)result).getErrorCode());
	}

	public void testBasic() {

		confirmMid(new StringEval("galactic"), new NumberEval(3), new NumberEval(4), "lact");
	}

	/**
	 * Valid cases where args are not precisely (string, int, int) but can be resolved OK.
	 */
	public void testUnusualArgs() {
		// startPos with fractional digits
		confirmMid(new StringEval("galactic"), new NumberEval(3.1), new NumberEval(4), "lact");

		// string startPos
		confirmMid(new StringEval("galactic"), new StringEval("3"), new NumberEval(4), "lact");

		// text (first) arg type is number, other args are strings with fractional digits
		confirmMid(new NumberEval(123456), new StringEval("3.1"), new StringEval("2.9"), "34");

		// startPos is 1x1 area ref, numChars is cell ref
		AreaEval aeStart = EvalFactory.createAreaEval("A1:A1", new ValueEval[] { new NumberEval(2), } );
		RefEval reNumChars = EvalFactory.createRefEval("B1", new NumberEval(3));
		confirmMid(new StringEval("galactic"), aeStart, reNumChars, "ala");

		confirmMid(new StringEval("galactic"), new NumberEval(3.1), BlankEval.INSTANCE, "");

		confirmMid(new StringEval("galactic"), new NumberEval(3), BoolEval.FALSE, "");
		confirmMid(new StringEval("galactic"), new NumberEval(3), BoolEval.TRUE, "l");
		confirmMid(BlankEval.INSTANCE, new NumberEval(3), BoolEval.TRUE, "");

	}

	/**
	 * Extreme values for startPos and numChars
	 */
	public void testExtremes() {
		confirmMid(new StringEval("galactic"), new NumberEval(4), new NumberEval(400), "actic");

		confirmMid(new StringEval("galactic"), new NumberEval(30), new NumberEval(4), "");
		confirmMid(new StringEval("galactic"), new NumberEval(3), new NumberEval(0), "");
	}

	/**
	 * All sorts of ways to make MID return defined errors.
	 */
	public void testErrors() {
		confirmMid(ErrorEval.NAME_INVALID, new NumberEval(3), new NumberEval(4), ErrorEval.NAME_INVALID);
		confirmMid(new StringEval("galactic"), ErrorEval.NAME_INVALID, new NumberEval(4), ErrorEval.NAME_INVALID);
		confirmMid(new StringEval("galactic"), new NumberEval(3), ErrorEval.NAME_INVALID, ErrorEval.NAME_INVALID);
		confirmMid(new StringEval("galactic"), ErrorEval.DIV_ZERO, ErrorEval.NAME_INVALID, ErrorEval.DIV_ZERO);

		confirmMid(new StringEval("galactic"), BlankEval.INSTANCE, new NumberEval(3.1), ErrorEval.VALUE_INVALID);

		confirmMid(new StringEval("galactic"), new NumberEval(0), new NumberEval(4), ErrorEval.VALUE_INVALID);
		confirmMid(new StringEval("galactic"), new NumberEval(1), new NumberEval(-1), ErrorEval.VALUE_INVALID);
	}
}
