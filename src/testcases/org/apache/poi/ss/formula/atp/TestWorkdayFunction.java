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

import static java.util.Calendar.APRIL;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MAY;
import static java.util.Calendar.NOVEMBER;
import static java.util.Calendar.OCTOBER;
import static java.util.Calendar.SEPTEMBER;
import static org.apache.poi.ss.formula.eval.ErrorEval.VALUE_INVALID;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * @author jfaenomoto@gmail.com
 */
@SuppressWarnings("deprecation") // YK: heavily uses deprecated {@link java.util.Date(int year, int month, int date)}
public class TestWorkdayFunction extends TestCase {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

    private static final String STARTING_DATE = formatter.format(new Date(108, OCTOBER, 1));

    private static final String FIRST_HOLIDAY = formatter.format(new Date(108, NOVEMBER, 26));

    private static final String SECOND_HOLIDAY = formatter.format(new Date(108, DECEMBER, 4));

    private static final String THIRD_HOLIDAY = formatter.format(new Date(109, JANUARY, 21));

    private static final String RETROATIVE_HOLIDAY = formatter.format(new Date(108, SEPTEMBER, 29));

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 1, 1, 1, null);

    public void testFailWhenNoArguments() {
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(new ValueEval[0], null));
    }

    public void testFailWhenLessThan2Arguments() {
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(new ValueEval[1], null));
    }

    public void testFailWhenMoreThan3Arguments() {
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(new ValueEval[4], null));
    }

    public void testFailWhenArgumentsAreNotDatesNorNumbers() {
        assertEquals(VALUE_INVALID, WorkdayFunction.instance.evaluate(
                new ValueEval[]{ new StringEval("Potato"), new StringEval("Cucumber") }, EC));
    }

    public void testReturnWorkdays() {
        assertEquals(new Date(109, APRIL, 30), DateUtil.getJavaDate(((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new NumberEval(151) }, EC)).getNumberValue()));
    }

    public void testReturnWorkdaysSpanningAWeekendSubtractingDays() {
    	String startDate = "2013/09/30";
    	int days = -1;
    	String expectedWorkDay = "2013/09/27";
		StringEval stringEval = new StringEval(startDate);
		double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                stringEval, new NumberEval(days) }, EC)).getNumberValue();
		assertEquals(expectedWorkDay, formatter.format(DateUtil.getJavaDate(numberValue)));
    }
    
    public void testReturnWorkdaysSpanningAWeekendAddingDays() {
    	String startDate = "2013/09/27";
    	int days = 1;
    	String expectedWorkDay = "2013/09/30";
		StringEval stringEval = new StringEval(startDate);
		double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                stringEval, new NumberEval(days) }, EC)).getNumberValue();
		assertEquals(expectedWorkDay, formatter.format(DateUtil.getJavaDate(numberValue)));
    }
    
    public void testReturnWorkdaysWhenStartIsWeekendAddingDays() {
    	String startDate = "2013/10/06";
    	int days = 1;
    	String expectedWorkDay = "2013/10/07";
		StringEval stringEval = new StringEval(startDate);
		double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                stringEval, new NumberEval(days) }, EC)).getNumberValue();
		assertEquals(expectedWorkDay, formatter.format(DateUtil.getJavaDate(numberValue)));
    }
    
    public void testReturnWorkdaysWhenStartIsWeekendSubtractingDays() {
    	String startDate = "2013/10/06";
    	int days = -1;
    	String expectedWorkDay = "2013/10/04";
		StringEval stringEval = new StringEval(startDate);
		double numberValue = ((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                stringEval, new NumberEval(days) }, EC)).getNumberValue();
		assertEquals(expectedWorkDay, formatter.format(DateUtil.getJavaDate(numberValue)));
    }

    public void testReturnWorkdaysWithDaysTruncated() {
        assertEquals(new Date(109, APRIL, 30), DateUtil.getJavaDate(((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new NumberEval(151.99999) }, EC)).getNumberValue()));
    }

    public void testReturnRetroativeWorkday() {
        assertEquals(new Date(108, SEPTEMBER, 23), DateUtil.getJavaDate(((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new NumberEval(-5), new StringEval(RETROATIVE_HOLIDAY.toString()) }, EC))
                .getNumberValue()));
    }

    public void testReturnNetworkdaysWithManyHolidays() {
        assertEquals(new Date(109, MAY, 5), DateUtil.getJavaDate(((NumberEval) WorkdayFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new NumberEval(151),
                new MockAreaEval(FIRST_HOLIDAY, SECOND_HOLIDAY, THIRD_HOLIDAY) }, EC)).getNumberValue()));
    }

    private class MockAreaEval extends AreaEvalBase {

        private List<ValueEval> holidays;

        public MockAreaEval(String... holidays) {
            this(0, 0, 0, holidays.length - 1);
            this.holidays = new ArrayList<ValueEval>();
            for (String holiday : holidays) {
                this.holidays.add(new StringEval(holiday));
            }
        }

        protected MockAreaEval(int firstRow, int firstColumn, int lastRow, int lastColumn) {
            super(firstRow, firstColumn, lastRow, lastColumn);
        }

        @Override
        public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {
            return this.holidays.get(relativeColumnIndex);
        }

        public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
            return null;
        }

        public TwoDEval getColumn(int columnIndex) {
            return null;
        }

        public TwoDEval getRow(int rowIndex) {
            return null;
        }

    }
}
