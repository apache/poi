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

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

class TestEOMonth {

    private static final double BAD_DATE = -1.0;

    private static final double DATE_1900_01_01 = 1.0;
    private static final double DATE_1900_01_31 = 31.0;
    private static final double DATE_1900_02_28 = 59.0;
    private static final double DATE_1902_09_26 = 1000.0;
    private static final double DATE_1902_09_30 = 1004.0;
    private static final double DATE_2034_06_09 = 49104.0;
    private static final double DATE_2034_06_30 = 49125.0;
    private static final double DATE_2034_07_31 = 49156.0;

    private final FreeRefFunction eOMonth = EOMonth.instance;
    private final OperationEvaluationContext ec = new OperationEvaluationContext(null, null, 0, 0, 0, null);

    @Test
    void testEOMonthProperValues() {
        // verify some border-case combinations of startDate and month-increase
        checkValue(DATE_1900_01_01, 0, DATE_1900_01_31);
        checkValue(DATE_1900_01_01, 1, DATE_1900_02_28);
        checkValue(DATE_1902_09_26, 0, DATE_1902_09_30);
        checkValue(DATE_2034_06_09, 0, DATE_2034_06_30);
        checkValue(DATE_2034_06_09, 1, DATE_2034_07_31);
    }

    @Test
    void testEOMonthBadDateValues() {
        checkValue(0.0, -2, BAD_DATE);
        checkValue(0.0, -3, BAD_DATE);
        checkValue(DATE_1900_01_31, -1, BAD_DATE);
    }

    private void checkValue(double startDate, int monthInc, double expectedResult) {
        ValueEval[] ve = {new NumberEval(startDate), new NumberEval(monthInc)};
        NumberEval result = (NumberEval) eOMonth.evaluate(ve, ec);
        assertEquals(expectedResult, result.getNumberValue(), 0);
    }

    @Test
    void testEOMonthZeroDate() {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(0), new NumberEval(0)}, ec);
        assertEquals(DATE_1900_01_31, result.getNumberValue(), 0, "0 startDate is 1900-01-00");

        result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(0), new NumberEval(1)}, ec);
        assertEquals(DATE_1900_02_28, result.getNumberValue(), 0, "0 startDate is 1900-01-00");
    }

    @Test
    void testEOMonthInvalidArguments() {
        ValueEval result = eOMonth.evaluate(new ValueEval[] {new NumberEval(DATE_1902_09_26)}, ec);
        assertTrue(result instanceof ErrorEval);
        assertEquals(FormulaError.VALUE.getCode(), ((ErrorEval) result).getErrorCode(), 0);

        result = eOMonth.evaluate(new ValueEval[] {new StringEval("a"), new StringEval("b")}, ec);
        assertTrue(result instanceof ErrorEval);
        assertEquals(FormulaError.VALUE.getCode(), ((ErrorEval) result).getErrorCode(), 0);
    }

    @Test
    void checkOffset() {
        for (int offset=-12; offset<=12; offset++) {
            Calendar cal = LocaleUtil.getLocaleCalendar();
            Date startDate = cal.getTime();

            cal.add(Calendar.MONTH, offset);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.clear(Calendar.HOUR);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            Date expDate = cal.getTime();

            ValueEval[] ve = {
                    new NumberEval(DateUtil.getExcelDate(startDate)),
                    new NumberEval(offset)
            };
            NumberEval result = (NumberEval) eOMonth.evaluate(ve, ec);
            Date actDate = DateUtil.getJavaDate(result.getNumberValue());

            assertEquals(expDate, actDate);
        }
    }

    @Test
    void testBug56688() {
        ValueEval[] ve = {new NumberEval(DATE_1902_09_26), new RefEvalImplementation(new NumberEval(0))};
        NumberEval result = (NumberEval) eOMonth.evaluate(ve, ec);
        assertEquals(DATE_1902_09_30, result.getNumberValue(), 0);
    }

    @Test
    void testRefEvalStartDate() {
        ValueEval[] ve = {new RefEvalImplementation(new NumberEval(DATE_1902_09_26)), new NumberEval(0)};
        NumberEval result = (NumberEval) eOMonth.evaluate(ve, ec);
        assertEquals(DATE_1902_09_30, result.getNumberValue(), 0);
    }

    @Test
    void testEOMonthBlankValueEval() {
        NumberEval evaluate = (NumberEval) eOMonth.evaluate(new ValueEval[] {BlankEval.instance, new NumberEval(0)}, ec);
        assertEquals(DATE_1900_01_31, evaluate.getNumberValue(), 0, "Blank is handled as 0");
    }

    @Test
    void testEOMonthBlankRefValueEval() {
        ValueEval[] ve1 = {new RefEvalImplementation(BlankEval.instance), new NumberEval(1)};
        NumberEval result = (NumberEval) eOMonth.evaluate(ve1, ec);
        assertEquals(DATE_1900_02_28, result.getNumberValue(), 0, "Blank is handled as 0");

        ValueEval[] ve2 = {new NumberEval(1), new RefEvalImplementation(BlankEval.instance)};
        result = (NumberEval) eOMonth.evaluate(ve2, ec);
        assertEquals(DATE_1900_01_31, result.getNumberValue(), 0, "Blank is handled as 0");
    }
}
