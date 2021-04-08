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

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

class TestEDate {

    @Test
    void testEDateProperValues() {
        // verify some border-case combinations of startDate and month-increase
        checkValue(1000, 0, 1000d);
        checkValue(1, 0, 1d);
        checkValue(0, 1, 31d);
        checkValue(1, 1, 32d);
        checkValue(0, 0, /* BAD_DATE! */ -1.0d);
        checkValue(0, -2, /* BAD_DATE! */ -1.0d);
        checkValue(0, -3, /* BAD_DATE! */ -1.0d);
        checkValue(49104, 0, 49104d);
        checkValue(49104, 1, 49134d);
    }

    private void checkValue(int startDate, int monthInc, double expectedResult) {
        EDate eDate = new EDate();
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(startDate), new NumberEval(monthInc)}, null);
        assertEquals(expectedResult, result.getNumberValue(), 0);
    }

    @Test
    void testEDateInvalidValues() {
        EDate eDate = new EDate();
        ErrorEval result = (ErrorEval) eDate.evaluate(new ValueEval[]{new NumberEval(1000)}, null);
        assertEquals(FormulaError.VALUE.getCode(), result.getErrorCode(), 0);
    }

    @Test
    void testEDateIncrease() {
        EDate eDate = new EDate();
        Date startDate = new Date();
        int offset = 2;
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(DateUtil.getExcelDate(startDate)), new NumberEval(offset)}, null);
        Date resultDate = DateUtil.getJavaDate(result.getNumberValue());
        Calendar instance = LocaleUtil.getLocaleCalendar();
        instance.setTime(startDate);
        instance.add(Calendar.MONTH, offset);
        assertEquals(resultDate, instance.getTime());

    }

    @Test
    void testEDateDecrease() {
        EDate eDate = new EDate();
        Date startDate = new Date();
        int offset = -2;
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(DateUtil.getExcelDate(startDate)), new NumberEval(offset)}, null);
        Date resultDate = DateUtil.getJavaDate(result.getNumberValue());
        Calendar instance = LocaleUtil.getLocaleCalendar();
        instance.setTime(startDate);
        instance.add(Calendar.MONTH, offset);
        assertEquals(resultDate, instance.getTime());
    }

    @Test
    void testBug56688() {
        EDate eDate = new EDate();
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(1000), new RefEvalImplementation(new NumberEval(0))}, null);
        assertEquals(1000d, result.getNumberValue(), 0);
    }

    @Test
    void testRefEvalStartDate() {
        EDate eDate = new EDate();
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new RefEvalImplementation(new NumberEval(1000)), new NumberEval(0)}, null);
        assertEquals(1000d, result.getNumberValue(), 0);
    }

    @Test
    void testEDateInvalidValueEval() {
        ValueEval evaluate = new EDate().evaluate(new ValueEval[]{new ValueEval() {}, new NumberEval(0)}, null);
        assertTrue(evaluate instanceof ErrorEval);
        assertEquals(ErrorEval.VALUE_INVALID, evaluate);
    }

    @Test
    void testEDateBlankValueEval() {
        NumberEval evaluate = (NumberEval) new EDate().evaluate(new ValueEval[]{BlankEval.instance, new NumberEval(0)}, null);
        assertEquals(-1.0d, evaluate.getNumberValue(), 0);
    }

    @Test
    void testEDateBlankRefValueEval() {
        EDate eDate = new EDate();
        NumberEval result = (NumberEval) eDate.evaluate(new ValueEval[]{new RefEvalImplementation(BlankEval.instance), new NumberEval(0)}, null);
        assertEquals(-1.0d, result.getNumberValue(), 0, "0 startDate triggers BAD_DATE currently, thus -1.0!");

        result = (NumberEval) eDate.evaluate(new ValueEval[]{new NumberEval(1), new RefEvalImplementation(BlankEval.instance)}, null);
        assertEquals(1.0d, result.getNumberValue(), 0, "Blank is handled as 0 otherwise");
    }
}
