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
 * Implementation for Excel FLOOR.MATH() function.
 * <ul>
 *   <li>https://support.microsoft.com/en-us/office/floor-math-function-c302b599-fbdb-4177-ba19-2c2b1249a2f5</li>
 * </ul>
 */
public final class FloorMath implements FreeRefFunction {

    public static final FloorMath instance = new FloorMath();

    private FloorMath() {}

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
                multiplier = arg1Val != null ? arg1Val.doubleValue() : 1.0;
            }
            boolean roundNegativeNumsDown = false;
            if (args.length > 2) {
                Double arg2Val = evaluateValue(args[2], ec.getRowIndex(), ec.getColumnIndex());
                roundNegativeNumsDown = arg2Val != null && arg2Val.doubleValue() < 0.0;
            }
            if (roundNegativeNumsDown && xval < 0.0) {
                if (multiplier != 1.0) {
                    RoundingMode mode = multiplier < 0.0 ? RoundingMode.FLOOR : RoundingMode.CEILING;
                    return new NumberEval(scaledRoundUsingBigDecimal(xval, multiplier, mode));
                }
                return new NumberEval(Math.ceil(xval));
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