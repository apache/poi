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

import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.util.ArrayList;


/**
 * Implementation for Excel LCM() function.
 * <p>
 *   <b>Syntax</b>:<br> <b>LCM  </b>(<b>number</b>, ...)<br>
 * </p>
 * <p>
 *   Returns the least common multiple of integers. The least common multiple is the smallest positive integer that is a multiple of all integer arguments number1, number2, and so on. Use LCM to add fractions with different denominators.
 * </p>
 * <p>
 *   See https://support.microsoft.com/en-us/office/lcm-function-7152b67a-8bb5-4075-ae5c-06ede5563c94
 * </p>
 */
public class Lcm implements FreeRefFunction {

    public static final Lcm instance = new Lcm();

    private static final long MAX_OUTPUT = (long)Math.pow(2, 53);

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 1) {
            return ErrorEval.VALUE_INVALID;
        } else if (args.length == 1) {
            try {
                ValueEval v1 = OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex());
                double d = OperandResolver.coerceValueToDouble(v1);
                if (isInvalidInput(d)) {
                    return ErrorEval.NUM_ERROR;
                }
                return new NumberEval((long)d);
            } catch (EvaluationException ee) {
                return ErrorEval.VALUE_INVALID;
            }
        } else {
            try {
                ArrayList<Long> evals = new ArrayList<>();
                for (ValueEval arg : args) {
                    ValueEval ve = OperandResolver.getSingleValue(arg, ec.getRowIndex(), ec.getColumnIndex());
                    double d = OperandResolver.coerceValueToDouble(ve);
                    if (isInvalidInput(d)) {
                        return ErrorEval.NUM_ERROR;
                    }
                    evals.add((long) d);
                }
                long result = evals.get(0);
                for (int i = 1; i < evals.size(); i++) {
                    result = ArithmeticUtils.lcm(result, evals.get(i));
                    if (result > MAX_OUTPUT) {
                        return ErrorEval.NUM_ERROR;
                    }
                }
                return new NumberEval(result);
            } catch (EvaluationException ee) {
                return ErrorEval.VALUE_INVALID;
            }
        }
    }

    private boolean isInvalidInput(double d) {
        return d < 0;
    }
}
