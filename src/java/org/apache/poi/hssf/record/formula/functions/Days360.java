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
package org.apache.poi.hssf.record.formula.functions;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Calculates the number of days between two dates based on a 360-day year
 * (twelve 30-day months), which is used in some accounting calculations. Use
 * this function to help compute payments if your accounting system is based on
 * twelve 30-day months.
 *
 * @author PUdalau
 */
public class Days360 extends Var2or3ArgFunction {

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        double result;
        try {
            double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            result = evaluate(d0, d1, false);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return new NumberEval(result);
    }

    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
            ValueEval arg2) {
        double result;
        try {
            double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
            ValueEval ve = OperandResolver.getSingleValue(arg2, srcRowIndex, srcColumnIndex);
            Boolean method = OperandResolver.coerceValueToBoolean(ve, false);
            result = evaluate(d0, d1, method == null ? false : method.booleanValue());
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return new NumberEval(result);
    }

    private static double evaluate(double d0, double d1, boolean method) {
        Calendar startingDate = getStartingDate(d0);
        Calendar endingDate = getEndingDateAccordingToStartingDate(d1, startingDate);
        long startingDay = startingDate.get(Calendar.MONTH) * 30 + startingDate.get(Calendar.DAY_OF_MONTH);
        long endingDay = (endingDate.get(Calendar.YEAR) - startingDate.get(Calendar.YEAR)) * 360
                + endingDate.get(Calendar.MONTH) * 30 + endingDate.get(Calendar.DAY_OF_MONTH);
        return endingDay - startingDay;
    }

    private static Calendar getDate(double date) {
        Calendar processedDate = new GregorianCalendar();
        processedDate.setTime(DateUtil.getJavaDate(date, false));
        return processedDate;
    }

    private static Calendar getStartingDate(double date) {
        Calendar startingDate = getDate(date);
        if (isLastDayOfMonth(startingDate)) {
            startingDate.set(Calendar.DAY_OF_MONTH, 30);
        }
        return startingDate;
    }

    private static Calendar getEndingDateAccordingToStartingDate(double date, Calendar startingDate) {
        Calendar endingDate = getDate(date);
        endingDate.setTime(DateUtil.getJavaDate(date, false));
        if (isLastDayOfMonth(endingDate)) {
            if (startingDate.get(Calendar.DATE) < 30) {
                endingDate = getFirstDayOfNextMonth(endingDate);
            }
        }
        return endingDate;
    }

    private static boolean isLastDayOfMonth(Calendar date) {
        Calendar clone = (Calendar) date.clone();
        clone.add(java.util.Calendar.MONTH, 1);
        clone.add(java.util.Calendar.DAY_OF_MONTH, -1);
        int lastDayOfMonth = clone.get(Calendar.DAY_OF_MONTH);
        return date.get(Calendar.DAY_OF_MONTH) == lastDayOfMonth;
    }

    private static Calendar getFirstDayOfNextMonth(Calendar date) {
        Calendar newDate = (Calendar) date.clone();
        if (date.get(Calendar.MONTH) < Calendar.DECEMBER) {
            newDate.set(Calendar.MONTH, date.get(Calendar.MONTH) + 1);
        } else {
            newDate.set(Calendar.MONTH, 1);
            newDate.set(Calendar.YEAR, date.get(Calendar.YEAR) + 1);
        }
        newDate.set(Calendar.DATE, 1);
        return newDate;
    }
}
