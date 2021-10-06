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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Implementation for Excel WeekNum() function.
 * <p>
 * <b>Syntax</b>:<br> <b>WeekNum  </b>(<b>Serial_num</b>,<b>Return_type</b>)<br>
 * <p>
 * Returns a number that indicates where the week falls numerically within a year.
 * <p>
 * Serial_num     is a date within the week. Dates should be entered by using the DATE function,
 * or as results of other formulas or functions. For example, use DATE(2008,5,23)
 * for the 23rd day of May, 2008. Problems can occur if dates are entered as text.
 * Return_type     is a number that determines on which day the week begins. The default is 1.
 * 1    Week begins on Sunday. Weekdays are numbered 1 through 7.
 * 2    Week begins on Monday. Weekdays are numbered 1 through 7.
 */
public class WeekNum extends Fixed2ArgFunction implements FreeRefFunction {
    public static final FreeRefFunction instance = new WeekNum();
    private static final NumberEval DEFAULT_RETURN_TYPE = new NumberEval(1);
    private static final HashSet<Integer> VALID_RETURN_TYPES = new HashSet<>(
            Arrays.asList(1, 2, 11, 12, 13, 14, 15, 16, 17, 21));

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval serialNumVE, ValueEval returnTypeVE) {
        double serialNum;
        try {
            serialNum = NumericFunction.singleOperandEvaluate(serialNumVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
        LocalDate localDate;
        try {
            Date dateToConvert = DateUtil.getJavaDate(serialNum, false);
            localDate = dateToConvert.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (Exception e) {
            return ErrorEval.NUM_ERROR;
        }
        int returnType;
        try {
            ValueEval ve = OperandResolver.getSingleValue(returnTypeVE, srcRowIndex, srcColumnIndex);
            if (ve instanceof MissingArgEval) {
                returnType = (int)DEFAULT_RETURN_TYPE.getNumberValue();
            } else {
                returnType = OperandResolver.coerceValueToInt(ve);
            }
        } catch (EvaluationException e) {
            return ErrorEval.NUM_ERROR;
        }

        if (!VALID_RETURN_TYPES.contains(returnType)) {
            return ErrorEval.NUM_ERROR;
        }

        return new NumberEval(this.getWeekNo(localDate, returnType));
    }

    private WeekFields SUNDAY_START = WeekFields.of(DayOfWeek.SUNDAY, 1);
    private WeekFields MONDAY_START = WeekFields.of(DayOfWeek.MONDAY, 1);
    private WeekFields TUESDAY_START = WeekFields.of(DayOfWeek.TUESDAY, 1);
    private WeekFields WEDNESDAY_START = WeekFields.of(DayOfWeek.WEDNESDAY, 1);
    private WeekFields THURSDAY_START = WeekFields.of(DayOfWeek.THURSDAY, 1);
    private WeekFields FRIDAY_START = WeekFields.of(DayOfWeek.FRIDAY, 1);
    private WeekFields SATURDAY_START = WeekFields.of(DayOfWeek.SATURDAY, 1);

    public int getWeekNo(LocalDate date, int weekStartOn) {
        if (weekStartOn == 1 || weekStartOn == 17) {
            return date.get(SUNDAY_START.weekOfYear());
        } else if (weekStartOn == 2 || weekStartOn == 11) {
            return date.get(MONDAY_START.weekOfYear());
        } else if (weekStartOn == 12) {
            return date.get(TUESDAY_START.weekOfYear());
        } else if (weekStartOn == 13) {
            return date.get(WEDNESDAY_START.weekOfYear());
        } else if (weekStartOn == 14) {
            return date.get(THURSDAY_START.weekOfYear());
        } else if (weekStartOn == 15) {
            return date.get(FRIDAY_START.weekOfYear());
        } else if (weekStartOn == 16) {
            return date.get(SATURDAY_START.weekOfYear());
        } else {
            return date.get(WeekFields.ISO.weekOfYear());
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length == 1) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], DEFAULT_RETURN_TYPE);
        } else if (args.length == 2) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
        }
        return ErrorEval.VALUE_INVALID;
    }
}
