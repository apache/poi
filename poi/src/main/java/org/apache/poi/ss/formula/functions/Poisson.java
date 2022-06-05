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

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

public class Poisson implements FreeRefFunction {

    public static final Poisson instance = new Poisson();

    private static final double DEFAULT_RETURN_RESULT = 1;

    /**
     * This checks is x = 0 and the mean = 0.
     * Excel currently returns the value 1 where as the
     * maths common implementation will error.
     * @param x  The number.
     * @param mean The mean.
     * @return If a default value should be returned.
     */
    private static boolean isDefaultResult(double x, double mean) {
        return x == 0 && mean == 0;
    }

    private static void checkArgument(double aDouble) throws EvaluationException {
        NumericFunction.checkValue(aDouble);

        // make sure that the number is positive
        if (aDouble < 0) {
            throw new EvaluationException(ErrorEval.NUM_ERROR);
        }
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        return evaluate(args, ec.getRowIndex(), ec.getColumnIndex());
    }

    public static ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length != 3) {
            return ErrorEval.VALUE_INVALID;
        }
        ValueEval arg0 = args[0];
        ValueEval arg1 = args[1];
        ValueEval arg2 = args[2];

        try {
            // arguments/result for this function
            double x;
            try {
                x = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
            } catch (EvaluationException ee) {
                return ErrorEval.VALUE_INVALID;
            }
            double mean = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);

            // check for default result : excel implementation for 0,0
            // is different to Math Common.
            if (isDefaultResult(x,mean)) {
                return new NumberEval(DEFAULT_RETURN_RESULT);
            }
            // check the arguments : as per excel function def
            checkArgument(x);
            checkArgument(mean);

            // truncate x : as per excel function def
            boolean cumulative = ((BoolEval)arg2).getBooleanValue();
            PoissonDistribution poissonDistribution = new PoissonDistribution(mean);
            double result = cumulative ?
                    poissonDistribution.cumulativeProbability((int) x) :
                    poissonDistribution.probability((int) x);

            // check the result
            NumericFunction.checkValue(result);

            return new NumberEval(result);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

}
