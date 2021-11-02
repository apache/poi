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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link TDistLt}
 */
final class TestTDistLt {

    private static final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testBasic() {
        //https://support.microsoft.com/en-us/office/t-dist-rt-function-20a30020-86f9-4b35-af1f-7ef6ae683eda
        confirmValue("60", "1", "TRUE", 0.99469533, 0.000001);
        confirmValue("-60", "1", "TRUE", 0.005304674, 0.000001);
        confirmValue("-60", "1.9", "TRUE", 0.005304674, 0.000001);
        confirmValue("8", "3", "FALSE", 0.00073691, 0.000001);
        confirmValue("-8", "3", "FALSE", 0.00073691, 0.000001);
        confirmValue("-8", "3.9", "FALSE", 0.00073691, 0.000001);
    }

    @Test
    void testInvalid() {
        confirmInvalidError("A1","B2", "C3");
        confirmInvalidError("5.968191467","B2", "FALSE");
        confirmInvalidError("A1","8", "TRUE");
        confirmInvalidError("5.968191467","8", "");
    }

    @Test
    void testNumError() {
        confirmNumError("-5.968191467", "-8", "TRUE");
    }

    private static ValueEval invokeValue(String number1, String number2, String value3) {
        ValueEval[] args = new ValueEval[] { new StringEval(number1), new StringEval(number2), new StringEval(value3) };
        return TDistLt.instance.evaluate(args, ec);
    }

    private static void confirmValue(String number1, String number2, String value3, double expected) {
        confirmValue(number1, number2, value3, expected, 0.0);
    }

    private static void confirmValue(String number1, String number2, String value3, double expected, double tolerance) {
        ValueEval result = invokeValue(number1, number2, value3);
        assertEquals(NumberEval.class, result.getClass());
        assertEquals(expected, ((NumberEval) result).getNumberValue(), tolerance);
    }

    private static void confirmInvalidError(String number1, String number2, String value3) {
        ValueEval result = invokeValue(number1, number2, value3);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.VALUE_INVALID, result);
    }

    private static void confirmNumError(String number1, String number2, String value3) {
        ValueEval result = invokeValue(number1, number2, value3);
        assertEquals(ErrorEval.class, result.getClass());
        assertEquals(ErrorEval.NUM_ERROR, result);
    }

}
