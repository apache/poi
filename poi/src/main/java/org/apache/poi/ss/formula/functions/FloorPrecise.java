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

import java.math.RoundingMode;

import static org.apache.poi.ss.formula.functions.MathX.scaledRoundUsingBigDecimal;

/**
 * Implementation for Excel FLOOR.PRECISE() function.
 * <ul>
 *   <li>https://support.microsoft.com/en-us/office/floor-precise-function-f769b468-1452-4617-8dc3-02f842a0702e</li>
 * </ul>
 */
public final class FloorPrecise implements FreeRefFunction {

    public static final FloorPrecise instance = new FloorPrecise();

    private FloorPrecise() {}

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length == 0) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            Double xval = evaluateValue(args[0], ec.getRowIndex(), ec.getColumnIndex());
            if (xval == null) {
                return ErrorEval.VALUE_INVALID;
            }
            double multiplier = 1.0;
            if (args.length > 1) {
                Double arg1Val = evaluateValue(args[1], ec.getRowIndex(), ec.getColumnIndex());
                multiplier = arg1Val != null ? Math.abs(arg1Val.doubleValue()) : 1.0;
            }
            if (multiplier != 1.0) {
                RoundingMode mode = multiplier < 0.0 ? RoundingMode.CEILING : RoundingMode.FLOOR;
                return new NumberEval(scaledRoundUsingBigDecimal(xval, multiplier, mode));
            }
            return new NumberEval(Math.floor(xval));
        } catch (EvaluationException evaluationException) {
            return evaluationException.getErrorEval();
        }
    }

    private static Double evaluateValue(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
        ValueEval veText = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
        String strText1 = OperandResolver.coerceValueToString(veText);
        return OperandResolver.parseDouble(strText1);
    }
}