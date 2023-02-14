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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.DateParser;
import org.apache.poi.util.LocaleUtil;

/**
 * <p>Calculates the number of days between two dates based on a real year,
 * which is used in some accounting calculations.</p>
 *
 * <p> 
 * {@code DAYS(end_date,start_date)}
 * </p>
 *
 * <ul>
 * <li>Start_date, end_date (required):<br>
 * The two dates between which you want to know the number of days.<br>
 * If start_date occurs after end_date, the DAYS function returns a negative number.</li>
 * </ul>
 * 
 * @see <a href="https://support.office.com/en-us/article/DAYS-function-57740535-D549-4395-8728-0F07BFF0B9DF">DAYS function - Microsoft Office</a>
 */
public class Days implements FreeRefFunction {

    public static final Days instance = new Days();

    private Days() {}

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
    }

    private ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        double result;
        try {
            LocalDate d0 = getDate(arg0, srcRowIndex, srcColumnIndex);
            LocalDate d1 = getDate(arg1, srcRowIndex, srcColumnIndex);
            result = evaluate(d0, d1);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return new NumberEval(result);
    }

    private static double evaluate(LocalDate endDate, LocalDate startDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    static LocalDate getDate(ValueEval eval, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval ve = OperandResolver.getSingleValue(eval, srcRowIndex, srcColumnIndex);
        try {
            double d0 = NumericFunction.singleOperandEvaluate(ve, srcRowIndex, srcColumnIndex);
            return getDate(d0);
        } catch (Exception e) {
            String strText1 = OperandResolver.coerceValueToString(ve);
            return DateParser.parseLocalDate(strText1);
        }
    }

    private static LocalDate getDate(double date) {
        Date d = DateUtil.getJavaDate(date, false);
        return d.toInstant()
                .atZone(LocaleUtil.getUserTimeZone().toZoneId())
                .toLocalDate();
    }
}
