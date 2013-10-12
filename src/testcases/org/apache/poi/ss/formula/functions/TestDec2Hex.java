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

import junit.framework.TestCase;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Tests for {@link Dec2Hex}
 *
 * @author cedric dot walter @ gmail dot com
 */
public final class TestDec2Hex extends TestCase {

	private static ValueEval invokeValue(String number1, String number2) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2), };
		return new Dec2Hex().evaluate(args, -1, -1);
	}

    private static ValueEval invokeValue(String number1) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1), };
		return new Dec2Hex().evaluate(args, -1, -1);
	}

	private static void confirmValue(String msg, String number1, String number2, String expected) {
		ValueEval result = invokeValue(number1, number2);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(msg, expected, ((StringEval) result).getStringValue());
	}

    private static void confirmValue(String msg, String number1, String expected) {
		ValueEval result = invokeValue(number1);
		assertEquals(StringEval.class, result.getClass());
		assertEquals(msg, expected, ((StringEval) result).getStringValue());
	}

    private static void confirmValueError(String msg, String number1, String number2, ErrorEval numError) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(msg, numError, result);
    }

	public void testBasic() {
		confirmValue("Converts decimal 100 to hexadecimal with 0 characters (64)", "100","0", "64");
		confirmValue("Converts decimal 100 to hexadecimal with 4 characters (0064)", "100","4", "0064");
		confirmValue("Converts decimal 100 to hexadecimal with 5 characters (0064)", "100","5", "00064");
		confirmValue("Converts decimal 100 to hexadecimal with 10 (default) characters", "100","10", "0000000064");
		confirmValue("If argument places contains a decimal value, dec2hex ignores the numbers to the right side of the decimal point.", "100","10.0", "0000000064");

		confirmValue("Converts decimal -54 to hexadecimal, 2 is ignored","-54", "2",  "FFFFFFFFCA");
		confirmValue("places is optionnal","-54", "FFFFFFFFCA");
	}

    public void testErrors() {
        confirmValueError("Out of range min number","-549755813889","0", ErrorEval.NUM_ERROR);
        confirmValueError("Out of range max number","549755813888","0", ErrorEval.NUM_ERROR);

        confirmValueError("negative places not allowed","549755813888","-10", ErrorEval.NUM_ERROR);
        confirmValueError("non number places not allowed","ABCDEF","0", ErrorEval.VALUE_INVALID);
    }
}
