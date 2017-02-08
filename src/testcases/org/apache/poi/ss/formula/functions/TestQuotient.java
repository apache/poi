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
 * Tests for {@link Quotient}
 *
 * @author cedric dot walter @ gmail dot com
 */
public class TestQuotient extends TestCase {
    private static ValueEval invokeValue(String numerator, String denominator) {
        ValueEval[] args = new ValueEval[]{new StringEval(numerator), new StringEval(denominator)};
        return new Quotient().evaluate(args, -1, -1);
    }

    private static void confirmValue(String msg, String numerator, String denominator, String expected) {
        ValueEval result = invokeValue(numerator, denominator);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(msg, expected, ((NumberEval) result).getStringValue());
    }

    private static void confirmValueError(String msg, String numerator, String denominator, ErrorEval numError) {
        ValueEval result = invokeValue(numerator, denominator);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(msg, numError, result);
    }

    public void testBasic() {
        confirmValue("Integer portion of 5/2 (2)", "5", "2", "2");
        confirmValue("Integer portion of 4.5/3.1 (1)", "4.5", "3.1", "1");

        confirmValue("Integer portion of -10/3 (-3)", "-10", "3", "-3");
        confirmValue("Integer portion of -5.5/2 (-2)", "-5.5", "2", "-2");

        confirmValue("Integer portion of Pi/Avogadro (0)", "3.14159", "6.02214179E+23", "0");
    }

    public void testErrors() {
        confirmValueError("numerator is nonnumeric", "ABCD", "", ErrorEval.VALUE_INVALID);
        confirmValueError("denominator is nonnumeric", "", "ABCD", ErrorEval.VALUE_INVALID);

        confirmValueError("dividing by zero", "3.14159", "0", ErrorEval.DIV_ZERO);
    }
}