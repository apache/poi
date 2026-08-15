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

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Calculates the internal rate of return.
 *
 * Syntax is IRR(values) or IRR(values,guess)
 *
 * @see <a href="http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution">Wikipedia on IRR</a>
 * @see <a href="http://office.microsoft.com/en-us/excel-help/irr-HP005209146.aspx">Excel IRR</a>
 */
public final class Irr implements Function {
    private static final int MAX_ITERATION_COUNT = 1000;
    private static final double ABSOLUTE_ACCURACY = 1E-7;
    private static final Logger LOGGER = PoiLogManager.getLogger(Irr.class);

    // Rates at or below -100% are meaningless, and NPV blows up as the rate
    // approaches -1. The grid starts just above -1 and is dense there, since
    // roots pushed toward -100% by extreme cash flows are exactly the ones
    // plain Newton-Raphson used to miss.
    private static final double[] BRACKET_GRID;
    static {
        BRACKET_GRID = new double[8 + 20 + 7];
        int n = 0;
        for (int e = 9; e >= 2; e--) {
            BRACKET_GRID[n++] = -1 + Math.pow(10, -e);
        }
        for (int i = -9; i <= 10; i++) {
            BRACKET_GRID[n++] = i / 10.0;
        }
        BRACKET_GRID[n++] = 2.0;
        BRACKET_GRID[n++] = 3.0;
        BRACKET_GRID[n++] = 5.0;
        BRACKET_GRID[n++] = 10.0;
        BRACKET_GRID[n++] = 100.0;
        BRACKET_GRID[n++] = 1000.0;
        BRACKET_GRID[n] = 10000.0;
    }

    public ValueEval evaluate(final ValueEval[] args, final int srcRowIndex, final int srcColumnIndex) {
        if(args.length == 0 || args.length > 2) {
            // Wrong number of arguments
            return ErrorEval.VALUE_INVALID;
        }

        try {
            double[] values = AggregateFunction.ValueCollector.collectValues(args[0]);
            double guess;
            if(args.length == 2) {
                guess = NumericFunction.singleOperandEvaluate(args[1], srcRowIndex, srcColumnIndex);
            } else {
                guess = 0.1d;
            }
            double result = irr(values, guess);
            NumericFunction.checkValue(result);
            return new NumberEval(result);
        } catch (EvaluationException e){
            return e.getErrorEval();
        }
    }

    /**
     * Computes the internal rate of return using an estimated irr of 10 percent.
     *
     * @param income the income values.
     * @return the irr.
     */
    public static double irr(double[] income) {
        return irr(income, 0.1d);
    }


    /**
     * Calculates IRR in two stages. First, plain Newton-Raphson from the
     * guess; for compatibility this is exactly the historical algorithm, so
     * every cash flow it already solved keeps its bit-identical result. If it
     * fails to converge (as in bug #64137, where the iteration overshoots to
     * a rate just above -100% and cannot recover) or converges to a
     * meaningless rate at or below -100%, the method falls back to a
     * bracketed Newton-Raphson (the classic "rtsafe" safeguard): the NPV
     * function is bracketed between two rates of opposite NPV sign, then
     * Newton-Raphson steps are taken as long as they stay inside the bracket
     * and converge fast enough, with bisection otherwise. The fallback cannot
     * diverge and cannot return a rate at or below -100%.
     *
     * @param values        the income values.
     * @param guess         the initial guess of irr.
     * @return the irr value. The method returns {@code Double.NaN}
     *  if no rate with zero NPV could be found
     *
     * @see <a href="http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution">
     *     http://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution</a>
     * @see <a href="http://en.wikipedia.org/wiki/Newton%27s_method">
     *     http://en.wikipedia.org/wiki/Newton%27s_method</a>
     */
    public static double irr(double[] values, double guess) {
        double result = newtonIrr(values, guess);
        if (!Double.isNaN(result) && result > -1) {
            return result;
        }
        return bracketedIrr(values, guess);
    }

    /**
     * The historical POI algorithm: unguarded Newton-Raphson from the guess.
     * Kept unchanged as the fast path so existing results stay identical.
     * Returns {@code Double.NaN} when it diverges; the "returning NaN" warn
     * logs now live in {@link #bracketedIrr(double[], double)}, where a NaN
     * really is final.
     */
    private static double newtonIrr(double[] values, double guess) {

        double x0 = guess;

        for (int i = 0; i < MAX_ITERATION_COUNT; i++) {

            // the value of the function (NPV) and its derivation can be calculated in the same loop
            final double factor = 1.0 + x0;
            double denominator = factor;
            if (denominator == 0) {
                return Double.NaN;
            }

            double fValue = values[0];
            double fDerivative = 0;
            for (int k = 1; k < values.length; k++) {
                final double value = values[k];
                fValue += value / denominator;
                denominator *= factor;
                fDerivative -= k * value / denominator;
            }

            // the essence of the Newton-Raphson Method
            if (fDerivative == 0) {
                return Double.NaN;
            }
            double x1 =  x0 - fValue/fDerivative;

            if (Math.abs(x1 - x0) <= ABSOLUTE_ACCURACY) {
                return x1;
            }

            x0 = x1;
        }
        // maximum number of iterations is exceeded
        return Double.NaN;
    }

    /**
     * Bracketed Newton-Raphson with bisection safeguard, used when
     * {@link #newtonIrr(double[], double)} diverges or leaves the domain.
     */
    private static double bracketedIrr(double[] values, double guess) {
        double[] bracket = findBracket(values, guess);
        if (bracket == null) {
            LOGGER.atWarn().log(
                    "Returning NaN because IRR found no rate in (-1, 10000] where NPV changes"
                    + " sign");
            return Double.NaN;
        }
        double lo = bracket[0];
        double flo = bracket[1];
        double hi = bracket[2];
        if (lo == hi) {
            return lo;
        }
        // orient the bracket so that npv(lo) < 0 < npv(hi)
        if (flo > 0) {
            double t = lo;
            lo = hi;
            hi = t;
        }
        double rts = (Math.min(lo, hi) < guess && guess < Math.max(lo, hi))
                ? guess : 0.5 * (lo + hi);
        double dxold = Math.abs(hi - lo);
        double dx = dxold;
        double f = npv(values, rts);
        double df = npvDerivative(values, rts);
        for (int i = 0; i < MAX_ITERATION_COUNT; i++) {
            // Newton is unusable when the derivative is 0, when the step
            // would leave the bracket, or when it is not converging fast
            // enough (would not halve the bracket).
            boolean newtonUnusable = df == 0
                    || ((rts - hi) * df - f) * ((rts - lo) * df - f) > 0
                    || Math.abs(2.0 * f) > Math.abs(dxold * df);
            if (newtonUnusable) {
                // bisection step
                dxold = dx;
                dx = 0.5 * (hi - lo);
                rts = lo + dx;
                if (lo == rts) {
                    return rts;
                }
            } else {
                // Newton-Raphson step
                dxold = dx;
                dx = f / df;
                double tmp = rts;
                rts -= dx;
                if (tmp == rts) {
                    return rts;
                }
            }
            if (Math.abs(dx) < ABSOLUTE_ACCURACY) {
                return rts;
            }
            f = npv(values, rts);
            df = npvDerivative(values, rts);
            if (f < 0) {
                lo = rts;
            } else {
                hi = rts;
            }
        }
        LOGGER.atWarn().log(
                "Returning NaN because IRR has reached max number of iterations allowed: {}",
                MAX_ITERATION_COUNT);
        return Double.NaN;
    }

    /**
     * Scans {@link #BRACKET_GRID} for two adjacent rates whose NPVs have
     * opposite signs. When several such brackets exist (multiple IRR roots),
     * the one containing the guess is preferred, then the one closest to it.
     *
     * @return {@code {lo, npv(lo), hi, npv(hi)}}, a degenerate
     *   {@code {x, 0, x, 0}} if a grid point is an exact root, or
     *   {@code null} if NPV never changes sign on the grid
     */
    private static double[] findBracket(double[] values, double guess) {
        double[] grid = BRACKET_GRID;
        if (guess > -1 && guess <= 10000 && Arrays.binarySearch(BRACKET_GRID, guess) < 0) {
            grid = Arrays.copyOf(BRACKET_GRID, BRACKET_GRID.length + 1);
            grid[grid.length - 1] = guess;
            Arrays.sort(grid);
        }
        double[] best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        double prevX = Double.NaN;
        double prevF = Double.NaN;
        for (double x : grid) {
            double fx = npv(values, x);
            if (Double.isNaN(fx)) {
                continue;
            }
            if (fx == 0) {
                return new double[]{x, 0, x, 0};
            }
            if (!Double.isNaN(prevX) && (fx < 0) != (prevF < 0)) {
                double distance = (prevX <= guess && guess <= x)
                        ? 0.0
                        : Math.min(Math.abs(guess - prevX), Math.abs(guess - x));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new double[]{prevX, prevF, x, fx};
                }
            }
            prevX = x;
            prevF = fx;
        }
        return best;
    }

    /**
     * NPV computed via powers of {@code y = 1/(1+x)} so that rates very close
     * to -1 overflow to infinity (usable for sign tests) instead of
     * underflowing a shared denominator to 0 and dividing by it.
     */
    private static double npv(double[] values, double x) {
        final double y = 1.0 / (1.0 + x);
        double f = 0;
        double p = 1;
        for (double value : values) {
            f += value * p;
            p *= y;
        }
        return f;
    }

    /** First derivative of {@link #npv(double[], double)} with respect to x. */
    private static double npvDerivative(double[] values, double x) {
        final double y = 1.0 / (1.0 + x);
        double df = 0;
        double p = y;
        for (int k = 1; k < values.length; k++) {
            p *= y;
            df -= k * values[k] * p;
        }
        return df;
    }
}
