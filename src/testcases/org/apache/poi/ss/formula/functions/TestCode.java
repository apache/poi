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
 * Tests for {@link Code}
 *
 * @author cedric dot walter @ gmail dot com
 */
public class TestCode extends TestCase
{
    private static ValueEval invokeValue(String number1) {
        ValueEval[] args = new ValueEval[]{new StringEval(number1),};
        return new Code().evaluate(args, -1, -1);
    }

    private static void confirmValue(String msg, String number1, String expected) {
        ValueEval result = invokeValue(number1);
        assertEquals(StringEval.class, result.getClass());
        assertEquals(msg, expected, ((StringEval) result).getStringValue());
    }

    private static void confirmValueError(String msg, String number1, ErrorEval numError) {
        ValueEval result = invokeValue(number1);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(msg, numError, result);
    }


    public void testBasic() {
        confirmValue("Displays the numeric code for A (65)", "A", "65");
        confirmValue("Displays the numeric code for the first character in text ABCDEFGHI (65)", "ABCDEFGHI", "65");

        confirmValue("Displays the numeric code for ! (33)", "!", "33");
    }

    public void testErrors() {
        confirmValueError("Empty text", "", ErrorEval.VALUE_INVALID);
    }
}
