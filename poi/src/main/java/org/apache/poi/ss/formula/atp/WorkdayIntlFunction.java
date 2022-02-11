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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Implementation of Excel 'Analysis ToolPak' function WORKDAY.INTL()<br>
 * Returns the date past a number of workdays beginning at a start date, considering an interval of holidays.
 * <p>
 * <b>Syntax</b><br>
 * <b>WORKDAY.INTL</b>(<b>startDate</b>, <b>days</b>, weekendType, holidays)
 * <p>
 * https://support.microsoft.com/en-us/office/workday-intl-function-a378391c-9ba7-4678-8a39-39611a9bf81d
 * @since POI 5.2.0
 */
final class WorkdayIntlFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new WorkdayIntlFunction(ArgumentsEvaluator.instance);

    private ArgumentsEvaluator evaluator;

    private WorkdayIntlFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    /**
     * Evaluate for WORKDAY.INTL. Given a date, a number of days, an optional weekend type
     * and an optional date or interval of holidays, determines which date it is past
     * number of parameterized workdays.
     *
     * @return {@link ValueEval} with date as its value.
     */
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 2 || args.length > 4) {
            return ErrorEval.VALUE_INVALID;
        }

        int srcCellRow = ec.getRowIndex();
        int srcCellCol = ec.getColumnIndex();

        double start;
        int days;
        int weekendType = 1;
        double[] holidays;
        try {
            start = this.evaluator.evaluateDateArg(args[0], srcCellRow, srcCellCol);
            days = (int) Math.floor(this.evaluator.evaluateNumberArg(args[1], srcCellRow, srcCellCol));
            if (args.length >= 3) {
                if (args[2] != BlankEval.instance) {
                    weekendType = (int) this.evaluator.evaluateNumberArg(args[2], srcCellRow, srcCellCol);
                }
                if (!WorkdayCalculator.instance.getValidWeekendTypes().contains(weekendType)) {
                   return ErrorEval.NUM_ERROR;
                }
            }
            ValueEval holidaysCell = args.length >= 4 ? args[3] : null;
            holidays = this.evaluator.evaluateDatesArg(holidaysCell, srcCellRow, srcCellCol);
            return new NumberEval(DateUtil.getExcelDate(
                    WorkdayCalculator.instance.calculateWorkdays(start, days, weekendType, holidays)));
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
    }

}
