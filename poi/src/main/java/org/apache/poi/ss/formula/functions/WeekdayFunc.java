/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import java.util.Calendar;

import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.MissingArgEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.DateUtil;


/**
 * Implementation for the Excel function WEEKDAY
 */
public final class WeekdayFunc implements Function {
//or:  extends Var1or2ArgFunction {

    public static final Function instance = new WeekdayFunc();

    private WeekdayFunc() {
        // no fields to initialise
    }

    /* for Var1or2ArgFunction:
    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
    }
    */


    /**
     * Perform WEEKDAY(date, returnOption) function.
     * <p>
     *     https://support.microsoft.com/en-us/office/weekday-function-60e44483-2ed1-439f-8bd0-e404c190949a
     * </p>
     */
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        try {
            if (args.length < 1  ||  args.length > 2) {
                return ErrorEval.VALUE_INVALID;
            }

            // extract first parameter
            ValueEval serialDateVE = OperandResolver.getSingleValue(args[0], srcRowIndex, srcColumnIndex);
            double serialDate = OperandResolver.coerceValueToDouble(serialDateVE);
            if (!DateUtil.isValidExcelDate(serialDate)) {
                return ErrorEval.NUM_ERROR;                     // EXCEL uses this and no VALUE_ERROR
            }
            Calendar date = DateUtil.getJavaCalendar(serialDate, false);        // (XXX 1904-windowing not respected)
            int weekday = date.get(Calendar.DAY_OF_WEEK);       // => sunday = 1, monday = 2, ..., saturday = 7

            // extract second parameter
            int returnOption = 1;                   // default value
            if (args.length == 2) {
                ValueEval ve = OperandResolver.getSingleValue(args[1], srcRowIndex, srcColumnIndex);
                if (ve == MissingArgEval.instance  ||  ve == BlankEval.instance) {
                    return ErrorEval.NUM_ERROR;     // EXCEL uses this and no VALUE_ERROR
                }
                returnOption = OperandResolver.coerceValueToInt(ve);
                if (returnOption == 2) {
                    returnOption = 11;              // both mean the same
                }
            } // if

            // perform calculation
            double result;
            if (returnOption == 1) {
                result = weekday;
            // value 2 is handled above (as value 11)
            } else if (returnOption == 3) {
                result = (weekday + 6 - 1) % 7;
            } else if (returnOption >= 11  &&  returnOption <= 17) {
                // rotate in the value range 1 to 7
                result = (weekday + 6 - (returnOption - 10)) % 7 + 1.;
            } else {
                // EXCEL uses this and no VALUE_ERROR
                return ErrorEval.NUM_ERROR;
            }

            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    } // evaluate()

}
