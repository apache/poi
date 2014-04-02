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
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Tests for {@link org.apache.poi.ss.formula.functions.Oct2Dec}
 *
 * @author cedric dot walter @ gmail dot com
 */
public final class TestOct2Dec extends TestCase {

    private static ValueEval invokeValue(String number1) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1) };
		return new Oct2Dec().evaluate(args, -1, -1);
	}

    private static void confirmValue(String msg, String number1, String expected) {
		ValueEval result = invokeValue(number1);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(msg, expected, ((NumberEval) result).getStringValue());
	}

    private static void confirmValueError(String msg, String number1, ErrorEval numError) {
        ValueEval result = invokeValue(number1);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(msg, numError, result);
    }

	public void testBasic() {
		confirmValue("Converts octal '' to decimal (0)", "", "0");
		confirmValue("Converts octal 54 to decimal (44)", "54", "44");
		confirmValue("Converts octal 7777777533 to decimal (-165)", "7777777533", "-165");
		confirmValue("Converts octal 7000000000 to decimal (-134217728)", "7000000000", "-134217728");
		confirmValue("Converts octal 7776667533 to decimal (-299173)", "7776667533", "-299173");
	}

    public void testErrors() {
        confirmValueError("not a valid octal number","ABCDEFGH", ErrorEval.NUM_ERROR);
        confirmValueError("not a valid octal number","99999999", ErrorEval.NUM_ERROR);
        confirmValueError("not a valid octal number","3.14159", ErrorEval.NUM_ERROR);
    }
}
