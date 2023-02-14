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

import java.util.List;

import static org.apache.poi.ss.formula.functions.ArrayFunctionUtils.getNumberArrays;

/**
 * Implementation for Excel FORECAST() and FORECAST.LINEAR() functions.
 * <p>
 *   <b>Syntax</b>:<br> <b>FORECAST </b>(<b>number</b>, <b>array1</b>, <b>array2</b>)<br>
 * </p>
 * <p>
 *   See https://support.microsoft.com/en-us/office/forecast-and-forecast-linear-functions-50ca49c9-7b40-4892-94e4-7ad38bbeda99
 * </p>
 */
public class Forecast extends Fixed3ArgFunction implements FreeRefFunction {

    public static final Forecast instance = new Forecast();

    private Forecast() {}

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1, ValueEval arg2) {
        try {
            final Double x = evaluateValue(arg0, srcRowIndex, srcColumnIndex);
            if (x == null || x.isNaN() || x.isInfinite()) {
                return ErrorEval.VALUE_INVALID;
            }
            final List<DoubleList> arrays = getNumberArrays(arg1, arg2);
            final double[] arrY = arrays.get(0).toArray();
            final double[] arrX = arrays.get(1).toArray();
            final double averageY = MathX.average(arrY);
            final double averageX = MathX.average(arrX);
            double bnum = 0;
            double bdem = 0;
            final int len = arrY.length;
            for (int i = 0; i < len; i++) {
                double diff0 = arrX[i] - averageX;
                bnum += diff0 * (arrY[i] - averageY);
                bdem += Math.pow(diff0, 2);
            }
            if (bdem == 0) {
                return ErrorEval.DIV_ZERO;
            }

            final double b = bnum / bdem;
            final double a = averageY - (b * averageX);
            final double res = a + (b * x);
            return new NumberEval(res);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        } catch (Exception e) {
            return ErrorEval.NA;
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 3) {
            return ErrorEval.VALUE_INVALID;
        }
        return evaluate(ec.getRowIndex(), ec.getColumnIndex(), args[0], args[1], args[2]);
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}
