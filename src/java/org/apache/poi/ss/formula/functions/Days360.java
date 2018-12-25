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

import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;

/**
 * <p>Calculates the number of days between two dates based on a 360-day year
 * (twelve 30-day months), which is used in some accounting calculations. Use
 * this function to help compute payments if your accounting system is based on
 * twelve 30-day months.<p>
 * 
 * {@code DAYS360(start_date,end_date,[method])}
 * 
 * <ul>
 * <li>Start_date, end_date (required):<br>
 * The two dates between which you want to know the number of days.<br>
 * If start_date occurs after end_date, the DAYS360 function returns a negative number.</li>
 * 
 * <li>Method (optional):<br>
 * A logical value that specifies whether to use the U.S. or European method in the calculation</li>
 * 
 * <li>Method set to false or omitted:<br>
 * the DAYS360 function uses the U.S. (NASD) method. If the starting date is the 31st of a month,
 * it becomes equal to the 30th of the same month. If the ending date is the 31st of a month and
 * the starting date is earlier than the 30th of a month, the ending date becomes equal to the
 * 1st of the next month, otherwise the ending date becomes equal to the 30th of the same month.
 * The month February and leap years are handled in the following way:<br>
 * On a non-leap year the function {@code =DAYS360("2/28/93", "3/1/93", FALSE)} returns 1 day
 * because the DAYS360 function ignores the extra days added to February.<br>
 * On a leap year the function {@code =DAYS360("2/29/96","3/1/96", FALSE)} returns 1 day for
 * the same reason.</li>
 * 
 * <li>Method Set to true:<br>
 * When you set the method parameter to TRUE, the DAYS360 function uses the European method.
 * Starting dates or ending dates that occur on the 31st of a month become equal to the 30th of
 * the same month. The month February and leap years are handled in the following way:<br>
 * On a non-leap year the function {@code =DAYS360("2/28/93", "3/1/93", TRUE)} returns
 * 3 days because the DAYS360 function is counting the extra days added to February to give
 * February 30 days.<br>
 * On a leap year the function {@code =DAYS360("2/29/96", "3/1/96", TRUE)} returns
 * 2 days for the same reason.</li>
 * </ul>
 * 
 * @see <a href="https://support.microsoft.com/en-us/kb/235575">DAYS360 Function Produces Different Values Depending on the Version of Excel</a>
 */
public class Days360 extends Var2or3ArgFunction {
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        try {
            double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            return new NumberEval(evaluate(d0, d1, false));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
            ValueEval arg2) {
        try {
            double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            ValueEval ve = OperandResolver.getSingleValue(arg2, srcRowIndex, srcColumnIndex);
            Boolean method = OperandResolver.coerceValueToBoolean(ve, false);
            return new NumberEval(evaluate(d0, d1, method != null && method.booleanValue()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private static double evaluate(double d0, double d1, boolean method) {
        Calendar realStart = getDate(d0);
        Calendar realEnd = getDate(d1);
        int[] startingDate = getStartingDate(realStart, method);
        int[] endingDate = getEndingDate(realEnd, startingDate, method);
        return
            (endingDate[0]*360+endingDate[1]*30+endingDate[2])-
            (startingDate[0]*360+startingDate[1]*30+startingDate[2]);
    }

    private static Calendar getDate(double date) {
        Calendar processedDate = LocaleUtil.getLocaleCalendar();
        processedDate.setTime(DateUtil.getJavaDate(date, false));
        return processedDate;
    }

    private static int[] getStartingDate(Calendar realStart, boolean method) {
        int yyyy = realStart.get(Calendar.YEAR);
        int mm = realStart.get(Calendar.MONTH);
        int dd = Math.min(30, realStart.get(Calendar.DAY_OF_MONTH));
        
        if (!method && isLastDayOfMonth(realStart)) dd = 30;
        
        return new int[]{yyyy,mm,dd};
    }

    private static int[] getEndingDate(Calendar realEnd, int[] startingDate, boolean method) {
        int yyyy = realEnd.get(Calendar.YEAR);
        int mm = realEnd.get(Calendar.MONTH);
        int dd = Math.min(30, realEnd.get(Calendar.DAY_OF_MONTH));

        if (!method && realEnd.get(Calendar.DAY_OF_MONTH) == 31) {
            if (startingDate[2] < 30) {
                realEnd.set(Calendar.DAY_OF_MONTH, 1);
                realEnd.add(Calendar.MONTH, 1);
                yyyy = realEnd.get(Calendar.YEAR);
                mm = realEnd.get(Calendar.MONTH);
                dd = 1;
            } else {
                dd = 30;
            }
        }

        return new int[]{yyyy,mm,dd};
    }
    
    private static boolean isLastDayOfMonth(Calendar date) {
        int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
        int lastDayOfMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
        return (dayOfMonth == lastDayOfMonth);
    }
}
