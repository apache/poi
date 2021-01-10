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

import static org.apache.poi.ss.formula.eval.ErrorEval.NAME_INVALID;
import static org.apache.poi.ss.formula.eval.ErrorEval.VALUE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.AreaEvalBase;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

class TestNetworkdaysFunction {

    private static final String STARTING_DATE = "2008/10/01";
    private static final String END_DATE = "2009/03/01";
    private static final String FIRST_HOLIDAY = "2008/11/26";
    private static final String SECOND_HOLIDAY = "2008/12/04";
    private static final String THIRD_HOLIDAY = "2009/01/21";

    private static final OperationEvaluationContext EC = new OperationEvaluationContext(null, null, 1, 1, 1, null);

    @Test
    void testFailWhenNoArguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[0], null));
    }

    @Test
    void testFailWhenLessThan2Arguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[1], null));
    }

    @Test
    void testFailWhenMoreThan3Arguments() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[4], null));
    }

    @Test
    void testFailWhenArgumentsAreNotDates() {
        assertEquals(VALUE_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[]{ new StringEval("Potato"),
                new StringEval("Cucumber") }, EC));
    }

    @Test
    void testFailWhenStartDateAfterEndDate() {
        assertEquals(NAME_INVALID, NetworkdaysFunction.instance.evaluate(new ValueEval[]{ new StringEval(END_DATE),
                new StringEval(STARTING_DATE) }, EC));
    }

    @Test
    void testReturnNetworkdays() {
        assertEquals(108, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE), new StringEval(END_DATE) }, EC)).getNumberValue());
    }

    @Test
    void testReturnNetworkdaysWithAHoliday() {
        assertEquals(107, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE), new StringEval(END_DATE), new StringEval(FIRST_HOLIDAY) },
                EC)).getNumberValue());
    }

    @Test
    void testReturnNetworkdaysWithManyHolidays() {
        assertEquals(105, (int) ((NumericValueEval) NetworkdaysFunction.instance.evaluate(new ValueEval[]{
                new StringEval(STARTING_DATE), new StringEval(END_DATE),
                new MockAreaEval(FIRST_HOLIDAY, SECOND_HOLIDAY, THIRD_HOLIDAY)}, EC)).getNumberValue());
    }

    private static class MockAreaEval extends AreaEvalBase {

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
