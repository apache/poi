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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * Implementation for the Excel EOMONTH() function.<p>
 * <p>
 * EOMONTH() returns the date of the last day of a month..<p>
 * <p>
 * <b>Syntax</b>:<br>
 * <b>EOMONTH</b>(<b>start_date</b>,<b>months</b>)<p>
 * <p>
 * <b>start_date</b> is the starting date of the calculation
 * <b>months</b> is the number of months to be added to <b>start_date</b>,
 * to give a new date. For this new date, <b>EOMONTH</b> returns the date of
 * the last day of the month. <b>months</b> may be positive (in the future),
 * zero or negative (in the past).
 */
public class EOMonth implements FreeRefFunction {
    public static final FreeRefFunction instance = new EOMonth();

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }

        try {
            double startDateAsNumber = NumericFunction.singleOperandEvaluate(args[0], ec.getRowIndex(), ec.getColumnIndex());
            int months = (int) NumericFunction.singleOperandEvaluate(args[1], ec.getRowIndex(), ec.getColumnIndex());

            // Excel treats date 0 as 1900-01-00; EOMONTH results in 1900-01-31
            if (startDateAsNumber >= 0.0 && startDateAsNumber < 1.0) {
                startDateAsNumber = 1.0;
            }

            Date startDate = DateUtil.getJavaDate(startDateAsNumber, false);

            Calendar cal = LocaleUtil.getLocaleCalendar();
            cal.setTime(startDate);
            cal.clear(Calendar.HOUR);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.add(Calendar.MONTH, months + 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);

            return new NumberEval(DateUtil.getExcelDate(cal.getTime()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
