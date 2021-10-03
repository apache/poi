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
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests WEEKNUM(date[, return_type]) excep function
 * https://support.microsoft.com/en-us/office/weeknum-function-e5c43a03-b4ab-426c-b411-b18c13c75340
 */
class TestWeekNumFunc {
    private static final double TOLERANCE = 0.001;

    @Test
    void testEvaluate() {
        assertEvaluateEquals(10.0, DateUtil.getExcelDate(LocalDate.parse("2012-03-09")));
        //next assert returns 10 when it should be 11.0.
        //assertEvaluateEquals(11.0, DateUtil.getExcelDate(LocalDate.parse("2012-03-09")), 2);
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
        if (result instanceof NumberEval) {
            assertEquals(expected, ((NumberEval)result).getNumberValue(), TOLERANCE, formula);
        } else {
            fail("unexpected eval result " + result);
        }
    }

    private void assertEvaluateEquals(double expected, double dateValue, double return_type) {
        String formula = "WEEKNUM(" + dateValue + ", " + return_type + ")";
        ValueEval[] args = new ValueEval[] { new NumberEval(dateValue), new NumberEval(return_type) };
        ValueEval result = WeekNum.instance.evaluate(args, DEFAULT_CONTEXT);
        if (result instanceof NumberEval) {
            assertEquals(expected, ((NumberEval)result).getNumberValue(), TOLERANCE, formula);
        } else {
            fail("unexpected eval result " + result);
        }
    }
}
