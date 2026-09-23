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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implementation for Excel DOLLARDE() function.
 * <p>
 * https://support.microsoft.com/en-us/office/dollarde-function-db85aab0-1677-428a-9dfd-a38476693427
 */
public final class DollarDe extends Fixed2ArgFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new DollarDe();

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg1, ValueEval arg2) {
        try {
            Double number1 = evaluateValue(arg1, srcRowIndex, srcColumnIndex);
            if (number1 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            Double number2 = evaluateValue(arg2, srcRowIndex, srcColumnIndex);
            if (number2 == null) {
                return ErrorEval.VALUE_INVALID;
            }
            int fraction = number2.intValue();
            if (fraction < 0) {
                return ErrorEval.NUM_ERROR;
            } else if (fraction == 0) {
                return ErrorEval.DIV_ZERO;
            }
            int fractionLength = String.valueOf(fraction).length();

            boolean negative = number1 < 0;
            double absolute = Math.abs(number1);
            double valueInteger = Math.floor(absolute);
            double valueFractional = absolute - valueInteger;
            if (valueFractional == 0.0) {
                return new NumberEval(negative ? -valueInteger : valueInteger);
            }

            double result = valueInteger + valueFractional * Math.pow(10, fractionLength) / fraction;
            NumericFunction.checkValue(result);
            return new NumberEval(negative ? -result : result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
         if (args.length == 2) {
            return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1]);
        }

        return ErrorEval.VALUE_INVALID;
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}