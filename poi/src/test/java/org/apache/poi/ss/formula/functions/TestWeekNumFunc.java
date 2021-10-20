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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests WEEKNUM(date[, return_type]) excel function
 * https://support.microsoft.com/en-us/office/weeknum-function-e5c43a03-b4ab-426c-b411-b18c13c75340
 */
class TestWeekNumFunc {
    private static final double TOLERANCE = 0.001;

    @Test
    void testEvaluate() {
        double date = DateUtil.getExcelDate(LocalDate.parse("2012-03-09"));
        assertEvaluateEquals(10.0, date);
        assertEvaluateEquals(10.0, date, 1);
        assertEvaluateEquals(11.0, date, 2);
        assertEvaluateEquals(11.0, date, 11);
        assertEvaluateEquals(11.0, date, 12);
        assertEvaluateEquals(11.0, date, 13);
        assertEvaluateEquals(11.0, date, 14);
        assertEvaluateEquals(11.0, date, 15);
        assertEvaluateEquals(10.0, date, 16);
        assertEvaluateEquals(10.0, date, 17);
        assertEvaluateEquals(10.0, date, 21);
    }

    @Test
    void testBug65606() {
        double date = DateUtil.getExcelDate(LocalDate.parse("2021-09-27"));
        assertEvaluateEquals(40.0, date);
        assertEvaluateEquals(39.0, date, 21);
    }

    @Test
    void testDateTime() {
        double date = DateUtil.getExcelDate(LocalDateTime.parse("2021-09-27T09:45:00"));
        assertEvaluateEquals(40.0, date);
    }

    @Test
    void testEvaluateInvalid() {
        assertEvaluateEquals("no args",       ErrorEval.VALUE_INVALID);
        assertEvaluateEquals("too many args", ErrorEval.VALUE_INVALID, new NumberEval(1.0), new NumberEval(1.0), new NumberEval(1.0));
        assertEvaluateEquals("negative date", ErrorEval.NUM_ERROR, new NumberEval(-1.0));
        assertEvaluateEquals("cannot coerce serial_number to number", ErrorEval.VALUE_INVALID, new StringEval(""));
        assertEvaluateEquals("cannot coerce return_type to number",   ErrorEval.NUM_ERROR, new NumberEval(1.0), new StringEval(""));
        assertEvaluateEquals("return_type is blank",   ErrorEval.NUM_ERROR, new StringEval("2"), BlankEval.instance);
        assertEvaluateEquals("invalid return_type",    ErrorEval.NUM_ERROR, new NumberEval(1.0), new NumberEval(18.0));
    }


    private static final OperationEvaluationContext DEFAULT_CONTEXT =
            new OperationEvaluationContext(null, null, 0, 1, 0, null);

    // for testing invalid invocations
    private void assertEvaluateEquals(String message, ErrorEval expected, ValueEval... args) {
        String formula = "WEEKNUM(" + StringUtil.join(args, ", ") + ")";
        ValueEval result = WeekNum.instance.evaluate(args, DEFAULT_CONTEXT);
        assertEquals(expected, result, formula + ": " + message);
    }

    private void assertEvaluateEquals(double expected, double dateValue) {
        String formula = "WEEKNUM(" + dateValue + ")";
        ValueEval[] args = new ValueEval[] { new NumberEval(dateValue) };
        ValueEval result = WeekNum.instance.evaluate(args, DEFAULT_CONTEXT);
        assertTrue(result instanceof NumberEval);
        assertEquals(expected, ((NumberEval)result).getNumberValue(), TOLERANCE, formula);
    }

    private void assertEvaluateEquals(double expected, double dateValue, double return_type) {
        String formula = "WEEKNUM(" + dateValue + ", " + return_type + ")";
        ValueEval[] args = new ValueEval[] { new NumberEval(dateValue), new NumberEval(return_type) };
        ValueEval result = WeekNum.instance.evaluate(args, DEFAULT_CONTEXT);
        assertTrue(result instanceof NumberEval);
        assertEquals(expected, ((NumberEval)result).getNumberValue(), TOLERANCE, formula);
    }
}
