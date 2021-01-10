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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.apache.poi.ss.formula.functions.Delta}
 *
 * @author cedric dot walter @ gmail dot com
 */
final class TestDelta {

	private static ValueEval invokeValue(String number1, String number2) {
		ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2), };
		return new Delta().evaluate(args, -1, -1);
	}

	private static void confirmValue(String number1, String number2, double expected) {
		ValueEval result = invokeValue(number1, number2);
		assertEquals(NumberEval.class, result.getClass());
		assertEquals(expected, ((NumberEval) result).getNumberValue(), 0.0);
	}

    private static void confirmValueError(String number1, String number2) {
        ValueEval result = invokeValue(number1, number2);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

	@Test
	void testBasic() {
		confirmValue("5","4", 0); // Checks whether 5 equals 4 (0)
		confirmValue("5","5", 1); // Checks whether 5 equals 5 (1)

        confirmValue("0.5","0", 0); // Checks whether 0.5 equals 0 (0)
        confirmValue("0.50","0.5", 1);
        confirmValue("0.5000000000","0.5", 1);
	}

	@Test
    void testErrors() {
        confirmValueError("A1","B2");
        confirmValueError("AAAA","BBBB");
    }
}
