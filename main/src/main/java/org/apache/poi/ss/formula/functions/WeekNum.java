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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * Implementation for Excel WeekNum() function.<p>
 * <p>
 * <b>Syntax</b>:<br> <b>WeekNum  </b>(<b>Serial_num</b>,<b>Return_type</b>)<br>
 * <p>
 * Returns a number that indicates where the week falls numerically within a year.
 * <p>
 * <p>
 * Serial_num     is a date within the week. Dates should be entered by using the DATE function,
 * or as results of other formulas or functions. For example, use DATE(2008,5,23)
 * for the 23rd day of May, 2008. Problems can occur if dates are entered as text.
 * Return_type     is a number that determines on which day the week begins. The default is 1.
 * 1	Week begins on Sunday. Weekdays are numbered 1 through 7.
 * 2	Week begins on Monday. Weekdays are numbered 1 through 7.
 */
public class WeekNum extends Fixed2ArgFunction implements FreeRefFunction {
    public static final FreeRefFunction instance = new WeekNum();

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval serialNumVE, ValueEval returnTypeVE) {
        double serialNum;
        try {
            serialNum = NumericFunction.singleOperandEvaluate(serialNumVE, srcRowIndex, srcColumnIndex);
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
        Calendar serialNumCalendar = LocaleUtil.getLocaleCalendar();
        serialNumCalendar.setTime(DateUtil.getJavaDate(serialNum, false));

        int returnType;
        try {
            ValueEval ve = OperandResolver.getSingleValue(returnTypeVE, srcRowIndex, srcColumnIndex);
            returnType = OperandResolver.coerceValueToInt(ve);
        } catch (EvaluationException e) {
            return ErrorEval.NUM_ERROR;
        }

        if (returnType != 1 && returnType != 2) {
            return ErrorEval.NUM_ERROR;
        }

        return new NumberEval(this.getWeekNo(serialNumCalendar, returnType));
    }

    public int getWeekNo(Calendar cal, int weekStartOn) {
        if (weekStartOn == 1) {
            cal.setFirstDayOfWeek(Calendar.SUNDAY);
        } else {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
        }
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length == 2) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
        }
        return ErrorEval.VALUE_INVALID;
    }
}
