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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Implements the Excel Rate function
 */
public class Rate implements Function {
    private static final Logger LOG = LogManager.getLogger(Rate.class);

    @Override
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 3) { //First 3 parameters are mandatory
            return ErrorEval.VALUE_INVALID;
        }

        double periods, payment, present_val, future_val = 0, type = 0, estimate = 0.1, rate;

        try {
            ValueEval v1 = OperandResolver.getSingleValue(args[0], srcRowIndex, srcColumnIndex);
            ValueEval v2 = OperandResolver.getSingleValue(args[1], srcRowIndex, srcColumnIndex);
            ValueEval v3 = OperandResolver.getSingleValue(args[2], srcRowIndex, srcColumnIndex);
            ValueEval v4 = null;
            if (args.length >= 4)
                v4 = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
            ValueEval v5 = null;
            if (args.length >= 5)
                v5 = OperandResolver.getSingleValue(args[4], srcRowIndex, srcColumnIndex);
            ValueEval v6 = null;
            if (args.length >= 6)
                v6 = OperandResolver.getSingleValue(args[5], srcRowIndex, srcColumnIndex);

            periods = OperandResolver.coerceValueToDouble(v1);
            payment = OperandResolver.coerceValueToDouble(v2);
            present_val = OperandResolver.coerceValueToDouble(v3);
            if (args.length >= 4)
                future_val = OperandResolver.coerceValueToDouble(v4);
            if (args.length >= 5)
                type = OperandResolver.coerceValueToDouble(v5);
            if (args.length >= 6)
                estimate = OperandResolver.coerceValueToDouble(v6);
            rate = calculateRate(periods, payment, present_val, future_val, type, estimate);

            checkValue(rate);
        } catch (EvaluationException e) {
            LOG.atError().withThrowable(e).log("Can't evaluate rate function");
            return e.getErrorEval();
        }

        return new NumberEval(rate);
    }

    private static double _g_div_gp(double r, double n, double p, double x, double y, double w) {
        double t1 = Math.pow(r+1, n);
        double t2 = Math.pow(r+1, n-1);
        return (y + t1*x + p*(t1 - 1)*(r*w + 1)/r) /
                (n*t2*x - p*(t1 - 1)*(r*w + 1)/(Math.pow(r, 2) + n*p*t2*(r*w + 1)/r +
                        p*(t1 - 1)*w/r));
    }

    /**
     * Compute the rate of interest per period.
     *
     * The implementation was ported from the NumPy library,
     * see https://github.com/numpy/numpy-financial/blob/d02edfb65dcdf23bd571c2cded7fcd4a0528c6af/numpy_financial/_financial.py#L602
     *
     *
     * @param nper Number of compounding periods
     * @param pmt Payment
     * @param pv Present Value
     * @param fv Future value
     * @param type When payments are due ('begin' (1) or 'end' (0))
     * @param guess Starting guess for solving the rate of interest
     * @return rate of interest per period or NaN if the solution didn't converge
     */
    static double calculateRate(double nper, double pmt, double pv, double fv, double type, double guess){
        double tol = 1e-8;
        double maxiter = 100;

        double rn = guess;
        int iter = 0;
        boolean close = false;
        while (iter < maxiter && !close){
            double rnp1 = rn - _g_div_gp(rn, nper, pmt, pv, fv, type);
            double diff = Math.abs(rnp1 - rn);
            close = diff < tol;
            iter += 1;
            rn = rnp1;

        }
        if(!close)
            return Double.NaN;
        else {
            return rn;
        }
    }

    /**
     * Excel does not support infinities and NaNs, rather, it gives a #NUM! error in these cases
     *
     * @throws EvaluationException (#NUM!) if {@code result} is {@code NaN} or {@code Infinity}
     */
    static void checkValue(double result) throws EvaluationException {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new EvaluationException(ErrorEval.NUM_ERROR);
        }
    }
}
