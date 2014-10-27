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

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
import org.apache.poi.ss.formula.OperationEvaluationContext;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.ErrorConstants;

public class TestEOMonth extends TestCase{

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

    public void testEOMonthProperValues() {
        // verify some border-case combinations of startDate and month-increase
        checkValue(DATE_1900_01_01, 0, DATE_1900_01_31);
        checkValue(DATE_1900_01_01, 1, DATE_1900_02_28);
        checkValue(DATE_1902_09_26, 0, DATE_1902_09_30);
        checkValue(DATE_2034_06_09, 0, DATE_2034_06_30);
        checkValue(DATE_2034_06_09, 1, DATE_2034_07_31);
    }

    public void testEOMonthBadDateValues() {
        checkValue(0.0, -2, BAD_DATE);
        checkValue(0.0, -3, BAD_DATE);
        checkValue(DATE_1900_01_31, -1, BAD_DATE);
    }

    private void checkValue(double startDate, int monthInc, double expectedResult) {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(startDate), new NumberEval(monthInc)}, ec);
        assertEquals(expectedResult, result.getNumberValue());
    }

    public void testEOMonthZeroDate() {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(0), new NumberEval(0)}, ec);
        assertEquals("0 startDate is 1900-01-00", DATE_1900_01_31, result.getNumberValue());

        result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(0), new NumberEval(1)}, ec);
        assertEquals("0 startDate is 1900-01-00", DATE_1900_02_28, result.getNumberValue());
    }

    public void testEOMonthInvalidArguments() {
        ValueEval result = eOMonth.evaluate(new ValueEval[] {new NumberEval(DATE_1902_09_26)}, ec);
        assertTrue(result instanceof ErrorEval);
        assertEquals(ErrorConstants.ERROR_VALUE, ((ErrorEval) result).getErrorCode());

        result = eOMonth.evaluate(new ValueEval[] {new StringEval("a"), new StringEval("b")}, ec);
        assertTrue(result instanceof ErrorEval);
        assertEquals(ErrorConstants.ERROR_VALUE, ((ErrorEval) result).getErrorCode());
    }

    public void testEOMonthIncrease() {
        checkOffset(new Date(), 2);
    }

    public void testEOMonthDecrease() {
        checkOffset(new Date(), -2);
    }

    private void checkOffset(Date startDate, int offset) {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(DateUtil.getExcelDate(startDate)), new NumberEval(offset)}, ec);
        Date resultDate = DateUtil.getJavaDate(result.getNumberValue());
        Calendar instance = Calendar.getInstance();
        instance.setTime(startDate);
        instance.add(Calendar.MONTH, offset);
        instance.add(Calendar.MONTH, 1);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.add(Calendar.DAY_OF_MONTH, -1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        assertEquals(instance.getTime(), resultDate);
    }

    public void testBug56688() {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(DATE_1902_09_26), new RefEvalImplementation(new NumberEval(0))}, ec);
        assertEquals(DATE_1902_09_30, result.getNumberValue());
    }

    public void testRefEvalStartDate() {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new RefEvalImplementation(new NumberEval(DATE_1902_09_26)), new NumberEval(0)}, ec);
        assertEquals(DATE_1902_09_30, result.getNumberValue());
    }

    public void testEOMonthBlankValueEval() {
        NumberEval evaluate = (NumberEval) eOMonth.evaluate(new ValueEval[] {BlankEval.instance, new NumberEval(0)}, ec);
        assertEquals("Blank is handled as 0", DATE_1900_01_31, evaluate.getNumberValue());
    }

    public void testEOMonthBlankRefValueEval() {
        NumberEval result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new RefEvalImplementation(BlankEval.instance), new NumberEval(1)}, ec);
        assertEquals("Blank is handled as 0",
                DATE_1900_02_28, result.getNumberValue());

        result = (NumberEval) eOMonth.evaluate(new ValueEval[] {new NumberEval(1), new RefEvalImplementation(BlankEval.instance)}, ec);
        assertEquals("Blank is handled as 0",
                DATE_1900_01_31, result.getNumberValue());
    }
}
