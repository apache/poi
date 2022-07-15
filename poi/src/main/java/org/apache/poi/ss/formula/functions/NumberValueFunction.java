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

import java.util.Locale;
import java.text.DecimalFormatSymbols;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.util.LocaleUtil;

/**
 /**
 * Implementation for the NUMBERVALUE() Excel function.<p>
 *
 * https://support.microsoft.com/en-us/office/numbervalue-function-1b05c8cf-2bfa-4437-af70-596c7ea7d879
 */
public final class NumberValueFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new NumberValueFunction();

    private NumberValueFunction() {}

    @Override
    public ValueEval evaluate( ValueEval[] args, OperationEvaluationContext ec ) {

        Locale locale = LocaleUtil.getUserLocale();
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale);

        String text = null;
        //If the Decimal_separator and Group_separator arguments are not specified, separators from the current locale are used.
        String decSep = String.valueOf(decimalFormatSymbols.getDecimalSeparator());
        String groupSep = String.valueOf(decimalFormatSymbols.getGroupingSeparator());

        double result = Double.NaN;
        ValueEval v1 = null;
        ValueEval v2 = null;
        ValueEval v3 = null;

        try {
            if (args.length == 1) {
                v1 = OperandResolver.getSingleValue( args[0], ec.getRowIndex(), ec.getColumnIndex());
                text = OperandResolver.coerceValueToString(v1);
            } else if (args.length == 2) {
                v1 = OperandResolver.getSingleValue( args[0], ec.getRowIndex(), ec.getColumnIndex());
                v2 = OperandResolver.getSingleValue( args[1], ec.getRowIndex(), ec.getColumnIndex());
                text = OperandResolver.coerceValueToString(v1);
                decSep = OperandResolver.coerceValueToString(v2).substring(0, 1); //If multiple characters are used in the Decimal_separator or Group_separator arguments, only the first character is used.
            } else if (args.length == 3) {
                v1 = OperandResolver.getSingleValue( args[0], ec.getRowIndex(), ec.getColumnIndex());
                v2 = OperandResolver.getSingleValue( args[1], ec.getRowIndex(), ec.getColumnIndex());
                v3 = OperandResolver.getSingleValue( args[2], ec.getRowIndex(), ec.getColumnIndex());
                text = OperandResolver.coerceValueToString(v1);
                decSep = OperandResolver.coerceValueToString(v2).substring(0, 1); //If multiple characters are used in the Decimal_separator or Group_separator arguments, only the first character is used.
                groupSep = OperandResolver.coerceValueToString(v3).substring(0, 1); //If multiple characters are used in the Decimal_separator or Group_separator arguments, only the first character is used.
            }
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }

        if("".equals(text) || text == null) text = "0"; //If an empty string ("") is specified as the Text argument, the result is 0.

        text = text.replace(" ", ""); //Empty spaces in the Text argument are ignored, even in the middle of the argument. For example, " 3 000 " is returned as 3000.
        String[] parts = text.split("["+decSep+"]");
        String sigPart = "";
        String decPart = "";
        if (parts.length > 2) return ErrorEval.VALUE_INVALID; //If a decimal separator is used more than once in the Text argument, NUMBERVALUE returns the #VALUE! error value.
        if (parts.length > 1) {
            sigPart = parts[0];
            decPart = parts[1];
            if (decPart.contains(groupSep)) return ErrorEval.VALUE_INVALID; //If the group separator occurs after the decimal separator in the Text argument, NUMBERVALUE returns the #VALUE! error value.
            sigPart = sigPart.replace(groupSep, ""); //If the group separator occurs before the decimal separator in the Text argument , the group separator is ignored.
            text = sigPart + "." + decPart;
        } else if (parts.length > 0) {
            sigPart = parts[0];
            sigPart = sigPart.replace(groupSep, ""); //If the group separator occurs before the decimal separator in the Text argument , the group separator is ignored.
            text = sigPart;
        }

        //If the Text argument ends in one or more percent signs (%), they are used in the calculation of the result.
        //Multiple percent signs are additive if they are used in the Text argument just as they are if they are used in a formula.
        //For example, =NUMBERVALUE("9%%") returns the same result (0.0009) as the formula =9%%.
        int countPercent = 0;
        while (text.endsWith("%")) {
            countPercent++;
            text = text.substring(0, text.length()-1);
        }

        try {
            result = Double.parseDouble(text);
            result = result / Math.pow(100, countPercent); //If the Text argument ends in one or more percent signs (%), they are used in the calculation of the result.
            checkValue(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        } catch (Exception e) {
            return ErrorEval.VALUE_INVALID; //If any of the arguments are not valid, NUMBERVALUE returns the #VALUE! error value.
        }

        return new NumberEval(result);

    }

    private static void checkValue(double result) throws EvaluationException {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new EvaluationException(ErrorEval.NUM_ERROR);
        }
    }
}
