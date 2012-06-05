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

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.NOVEMBER;
import static java.util.Calendar.OCTOBER;
import static org.apache.poi.ss.formula.eval.ErrorEval.NAME_INVALID;
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
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * @author jfaenomoto@gmail.com
 */
@SuppressWarnings("deprecation") // YK: uses deprecated {@link java.util.Date(int year, int month, int date)}
public class TestNetworkdaysFunction extends TestCase {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

    private static final String STARTING_DATE = formatter.format(new Date(108, OCTOBER, 1));

    private static final String END_DATE = formatter.format(new Date(109, MARCH, 1));

    private static final String FIRST_HOLIDAY = formatter.format(new Date(108, NOVEMBER, 26));

    private static final String SECOND_HOLIDAY = formatter.format(new Date(108, DECEMBER, 4));

    private static final String THIRD_HOLIDAY = formatter.format(new Date(109, JANUARY, 21));

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 1, 1, 1, null);

    public void testFailWhenNoArguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[0], null));
    }

    public void testFailWhenLessThan2Arguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[1], null));
    }

    public void testFailWhenMoreThan3Arguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[4], null));
    }

    public void testFailWhenArgumentsAreNotDates() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[]{ new StringEval("Potato"),
                new StringEval("Cucumber") }, EC));
    }

    public void testFailWhenStartDateAfterEndDate() {
        assertEquals(NAME_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[]{ new StringEval(END_DATE.toString()),
                new StringEval(STARTING_DATE.toString()) }, EC));
    }

    public void testReturnNetworkdays() {
        assertEquals(108, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new StringEval(END_DATE.toString()) }, EC)).getNumberValue());
    }

    public void testReturnNetworkdaysWithAHoliday() {
        assertEquals(107, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new StringEval(END_DATE.toString()), new StringEval(FIRST_HOLIDAY.toString()) },
                EC)).getNumberValue());
    }

    public void testReturnNetworkdaysWithManyHolidays() {
        assertEquals(105, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE.toString()), new StringEval(END_DATE.toString()),
                new MockAreaEval(FIRST_HOLIDAY, SECOND_HOLIDAY, THIRD_HOLIDAY) }, EC)).getNumberValue());
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
