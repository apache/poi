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

import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests WEEKDAY(serial_number[, return_type]) excep function
 * https://support.office.com/en-us/article/WEEKDAY-function-60E44483-2ED1-439F-8BD0-E404C190949A
 */
class TestWeekdayFunc {
    private static final double TOLERANCE = 0.001;

    private void assertEvaluateEquals(double expected, double serial_number) {
        String formula = "WEEKDAY(" + serial_number + ")";
        ValueEval[] args = new ValueEval[] { new NumberEval(serial_number) };
        NumberEval result = (NumberEval) WeekdayFunc.instance.evaluate(args, 0, 0);
        assertEquals(expected, result.getNumberValue(), TOLERANCE, formula);
    }
    private void assertEvaluateEquals(double expected, double serial_number, double return_type) {
        String formula = "WEEKDAY(" + serial_number + ", " + return_type + ")";
        ValueEval[] args = new ValueEval[] { new NumberEval(serial_number), new NumberEval(return_type) };
        NumberEval result = (NumberEval) WeekdayFunc.instance.evaluate(args, 0, 0);
        assertEquals(expected, result.getNumberValue(), TOLERANCE, formula);
    }


    @Test
    void testEvaluate() {
        assertEvaluateEquals(2.0,  1.0);
        assertEvaluateEquals(2.0,  1.0, 1.0);
        assertEvaluateEquals(1.0,  1.0, 2.0);
        assertEvaluateEquals(0.0,  1.0, 3.0);
        assertEvaluateEquals(1.0, 1.0, 11.0);
        assertEvaluateEquals(7.0, 1.0, 12.0);
        assertEvaluateEquals(6.0, 1.0, 13.0);
        assertEvaluateEquals(5.0, 1.0, 14.0);
        assertEvaluateEquals(4.0, 1.0, 15.0);
        assertEvaluateEquals(3.0, 1.0, 16.0);
        assertEvaluateEquals(2.0, 1.0, 17.0);


        assertEvaluateEquals(3.0, 39448.0);
        assertEvaluateEquals(3.0, 39448.0, 1.0);
        assertEvaluateEquals(2.0, 39448.0, 2.0);
        assertEvaluateEquals(1.0, 39448.0, 3.0);
        assertEvaluateEquals(2.0, 39448.0, 11.0);
        assertEvaluateEquals(1.0, 39448.0, 12.0);
        assertEvaluateEquals(7.0, 39448.0, 13.0);
        assertEvaluateEquals(6.0, 39448.0, 14.0);
        assertEvaluateEquals(5.0, 39448.0, 15.0);
        assertEvaluateEquals(4.0, 39448.0, 16.0);
        assertEvaluateEquals(3.0, 39448.0, 17.0);
    }

    // for testing invalid invocations
    private void assertEvaluateEquals(String message, ErrorEval expected, ValueEval... args) {
        String formula = "WEEKDAY(" + StringUtil.join(args, ", ") + ")";
        ValueEval result = WeekdayFunc.instance.evaluate(args, 0, 0);
        assertEquals(expected, result, formula + ": " + message);
    }

    @Test
    void testEvaluateInvalid() {
        assertEvaluateEquals("no args",       ErrorEval.VALUE_INVALID);
        assertEvaluateEquals("too many args", ErrorEval.VALUE_INVALID, new NumberEval(1.0), new NumberEval(1.0), new NumberEval(1.0));
        assertEvaluateEquals("negative date", ErrorEval.NUM_ERROR, new NumberEval(-1.0));
        assertEvaluateEquals("cannot coerce serial_number to number", ErrorEval.VALUE_INVALID, new StringEval(""));
        assertEvaluateEquals("cannot coerce return_type to number",   ErrorEval.VALUE_INVALID, new StringEval("1"), new StringEval(""));
        assertEvaluateEquals("return_type is blank",   ErrorEval.NUM_ERROR, new StringEval("2"), BlankEval.instance);
        assertEvaluateEquals("return_type is missing", ErrorEval.NUM_ERROR, new StringEval("3"), MissingArgEval.instance);
        assertEvaluateEquals("invalid return_type",    ErrorEval.NUM_ERROR, new NumberEval(1.0), new NumberEval(18.0));
    }
}
