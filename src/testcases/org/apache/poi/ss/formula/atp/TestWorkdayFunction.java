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


package org.apache.poi.ss.formula.atp;


import static org.apache.poi.ss.formula.eval.ErrorEval.VALUE_INVALID;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

public class TestWorkdayFunction {

    private static final String STARTING_DATE      = "2008/10/01";
    private static final String FIRST_HOLIDAY      = "2008/11/26";
    private static final String SECOND_HOLIDAY     = "2008/12/04";
    private static final String THIRD_HOLIDAY      = "2009/01/21";
    private static final String RETROATIVE_HOLIDAY = "2008/09/29";

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 1, 1, 1, null);

    @Test
    public void testFailWhenNoArguments() {
        ValueEval ve[] = new ValueEval[0];
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(ve, null));
    }

    @Test
    public void testFailWhenLessThan2Arguments() {
        ValueEval ve[] = new ValueEval[1];
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(ve, null));
    }

    @Test
    public void testFailWhenMoreThan3Arguments() {
        ValueEval ve[] = new ValueEval[4];
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(ve, null));
    }

    @Test
    public void testFailWhenArgumentsAreNotDatesNorNumbers() {
        ValueEval ve[] = { new StringEval("Potato"), new StringEval("Cucumber") };
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(ve, EC));
    }

    @Test
    public void testReturnWorkdays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2009, 3, 30);
        Date expDate = expCal.getTime();
        ValueEval ve[] = { new StringEval(STARTING_DATE), new NumberEval(151) };
        Date actDate = DateUtil.getJavaDate(((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue());
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnWorkdaysSpanningAWeekendSubtractingDays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2013, 8, 27);
        Date expDate = expCal.getTime();

    	ValueEval ve[] = { new StringEval("2013/09/30"), new NumberEval(-1) };
		double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
		assertEquals(41544.0, numberValue, 0);
		
		Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnWorkdaysSpanningAWeekendAddingDays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2013, 8, 30);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = { new StringEval("2013/09/27"), new NumberEval(1) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        assertEquals(41547.0, numberValue, 0);

        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnWorkdaysWhenStartIsWeekendAddingDays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2013, 9, 7);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = { new StringEval("2013/10/06"), new NumberEval(1) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        assertEquals(41554.0, numberValue, 0);

        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnWorkdaysWhenStartIsWeekendSubtractingDays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2013, 9, 4);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = { new StringEval("2013/10/06"), new NumberEval(-1) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        assertEquals(41551.0, numberValue, 0);

        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnWorkdaysWithDaysTruncated() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2009, 3, 30);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = { new StringEval(STARTING_DATE), new NumberEval(151.99999) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        
        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnRetroativeWorkday() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2008, 8, 23);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = { new StringEval(STARTING_DATE), new NumberEval(-5), new StringEval(RETROATIVE_HOLIDAY) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        
        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    @Test
    public void testReturnNetworkdaysWithManyHolidays() {
        Calendar expCal = LocaleUtil.getLocaleCalendar(2009, 4, 5);
        Date expDate = expCal.getTime();
        
        ValueEval ve[] = {
            new StringEval(STARTING_DATE), new NumberEval(151),
            new MockAreaEval(FIRST_HOLIDAY, SECOND_HOLIDAY, THIRD_HOLIDAY) };
        double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(ve, EC)).getNumberValue();
        
        Date actDate = DateUtil.getJavaDate(numberValue);
        assertEquals(expDate, actDate);
    }

    private class MockAreaEval extends AreaEvalBase {

        private List<ValueEval> holidays;

        public MockAreaEval(String... holidays) {
            this(0, 0, 0, holidays.length - 1);
            this.holidays = new ArrayList<>();
            for (String holiday : holidays) {
                this.holidays.add(new StringEval(holiday));
            }
        }

        protected MockAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn) {
            super(firstRow, firstColumn, lastRow, lastColumn);
        }

        @Override
        public ValueEval getRelativeValue(int sheetIndex, int relativeRowIndex, int relativeColumnIndex) {
            return this.holidays.get(relativeColumnIndex);
        }
        @Override
        public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
            return getRelativeValue(-1, relativeRowIndex, relativeColumnIndex);
        }

        @Override
        public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
            return null;
        }

        @Override
        public TwoDEval getColumn(int columnIndex) {
            return null;
        }

        @Override
        public TwoDEval getRow(int rowIndex) {
            return null;
        }

    }
}
