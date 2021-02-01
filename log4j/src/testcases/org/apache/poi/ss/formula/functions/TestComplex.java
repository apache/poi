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
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Complex}
 *
 * @author cedric dot walter @ gmail dot com
 */
class TestComplex {
    private static ValueEval invokeValue(String real_num, String i_num, String suffix) {
        ValueEval[] args = new ValueEval[]{new StringEval(real_num), new StringEval(i_num), new StringEval(suffix)};
        return new Complex().evaluate(args, -1, -1);
    }

    private static void confirmValue(String msg, String real_num, String i_num, String suffix, String expected) {
        ValueEval result = invokeValue(real_num, i_num, suffix);
        assertEquals(StringEval.class, result.getClass());
        assertEquals(expected, ((StringEval) result).getStringValue(), msg);
    }

    private static void confirmValueError(String msg, String real_num, String i_num, String suffix, ErrorEval numError) {
        ValueEval result = invokeValue(real_num, i_num, suffix);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(numError, result, msg);
    }

    @Test
    void testBasic() {
        confirmValue("Complex number with 3 and 4 as the real and imaginary coefficients (3 + 4i)", "3","4", "", "3+4i");
        confirmValue("Complex number with 3 and 4 as the real and imaginary coefficients, and j as the suffix (3 + 4j)", "3","4", "j", "3+4j");

        confirmValue("Complex number with 0 and 1 as the real and imaginary coefficients (i)", "0","1", "", "i");
        confirmValue("Complex number with 1 and 0 as the real and imaginary coefficients (1)", "1","0", "", "1");

        confirmValue("Complex number with 2 and 3 as the real and imaginary coefficients (2 + 3i)", "2","3", "", "2+3i");
        confirmValue("Complex number with -2 and -3 as the real and imaginary coefficients (-2-3i)", "-2","-3", "", "-2-3i");

        confirmValue("Complex number with -2 and -3 as the real and imaginary coefficients (-0.5-3.2i)", "-0.5","-3.2", "", "-0.5-3.2i");
    }

    @Test
    void testErrors() {
        confirmValueError("argument is nonnumeric", "ABCD", "","", ErrorEval.VALUE_INVALID);
        confirmValueError("argument is nonnumeric", "1", "ABCD","", ErrorEval.VALUE_INVALID);
        confirmValueError("f suffix is neither \"i\" nor \"j\"", "1", "1","k", ErrorEval.VALUE_INVALID);

        confirmValueError("never use \"I\" ", "1", "1","I", ErrorEval.VALUE_INVALID);
        confirmValueError("never use \"J\" ", "1", "1","J", ErrorEval.VALUE_INVALID);
    }
}