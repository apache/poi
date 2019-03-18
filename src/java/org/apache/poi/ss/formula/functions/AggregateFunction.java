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

import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public abstract class AggregateFunction extends MultiOperandNumericFunction {

    private static final class LargeSmall extends Fixed2ArgFunction {
        private final boolean _isLarge;
        protected LargeSmall(boolean isLarge) {
            _isLarge = isLarge;
        }

        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
                                  ValueEval arg1) {
            double dn;
            try {
                ValueEval ve1 = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex);
                dn = OperandResolver.coerceValueToDouble(ve1);
            } catch (EvaluationException e1) {
                // all errors in the second arg translate to #VALUE!
                return ErrorEval.VALUE_INVALID;
            }
            // weird Excel behaviour on second arg
            if (dn < 1.0) {
                // values between 0.0 and 1.0 result in #NUM!
                return ErrorEval.NUM_ERROR;
            }
            // all other values are rounded up to the next integer
            int k = (int) Math.ceil(dn);

            double result;
            try {
                double[] ds = ValueCollector.collectValues(arg0);
                if (k > ds.length) {
                    return ErrorEval.NUM_ERROR;
                }
                result = _isLarge ? StatsLib.kthLargest(ds, k) : StatsLib.kthSmallest(ds, k);
                NumericFunction.checkValue(result);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }

            return new NumberEval(result);
        }
    }

    /**
     *  Returns the k-th percentile of values in a range. You can use this function to establish a threshold of
     *  acceptance. For example, you can decide to examine candidates who score above the 90th percentile.
     *
     *  PERCENTILE(array,k)
     *  Array     is the array or range of data that defines relative standing.
     *  K     is the percentile value in the range 0..1, inclusive.
     *
     * <strong>Remarks</strong>
     * <ul>
     *     <li>if array is empty or contains more than 8,191 data points, PERCENTILE returns the #NUM! error value.</li>
     *     <li>If k is nonnumeric, PERCENTILE returns the #VALUE! error value.</li>
     *     <li>If k is < 0 or if k > 1, PERCENTILE returns the #NUM! error value.</li>
     *     <li>If k is not a multiple of 1/(n - 1), PERCENTILE interpolates to determine the value at the k-th percentile.</li>
     * </ul>
     */
    private static final class Percentile extends Fixed2ArgFunction {

        protected Percentile() {
        }

        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
                                  ValueEval arg1) {
            double dn;
            try {
                ValueEval ve1 = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex);
                dn = OperandResolver.coerceValueToDouble(ve1);
            } catch (EvaluationException e1) {
                // all errors in the second arg translate to #VALUE!
                return ErrorEval.VALUE_INVALID;
            }
            if (dn < 0 || dn > 1) { // has to be percentage
                return ErrorEval.NUM_ERROR;
            }

            double result;
            try {
                double[] ds = ValueCollector.collectValues(arg0);
                int N = ds.length;

                if (N == 0 || N > 8191) {
                    return ErrorEval.NUM_ERROR;
                }

                double n = (N - 1) * dn + 1;
                if (n == 1d) {
                    result = StatsLib.kthSmallest(ds, 1);
                } else if (Double.compare(n, N) == 0) {
                    result = StatsLib.kthLargest(ds, 1);
                } else {
                    int k = (int) n;
                    double d = n - k;
                    result = StatsLib.kthSmallest(ds, k) + d
                            * (StatsLib.kthSmallest(ds, k + 1) - StatsLib.kthSmallest(ds, k));
                }

                NumericFunction.checkValue(result);
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }

            return new NumberEval(result);
        }
    }

    static final class ValueCollector extends MultiOperandNumericFunction {
        private static final ValueCollector instance = new ValueCollector();
        public ValueCollector() {
            super(false, false);
        }
        public static double[] collectValues(ValueEval...operands) throws EvaluationException {
            return instance.getNumberArray(operands);
        }
        protected double evaluate(double[] values) {
            throw new IllegalStateException("should not be called");
        }
    }

    protected AggregateFunction() {
        super(false, false);
    }

    /**
     * Create an instance to use in the {@link Subtotal} function.
     *
     * <p>
     *     If there are other subtotals within argument refs (or nested subtotals),
     *     these nested subtotals are ignored to avoid double counting.
     * </p>
     *
     * @param   func  the function to wrap
     * @return  wrapped instance. The actual math is delegated to the argument function.
     */
    /*package*/ static Function subtotalInstance(Function func, boolean countHiddenRows) {
        final AggregateFunction arg = (AggregateFunction)func;
        return new AggregateFunction() {
            @Override
            protected double evaluate(double[] values) throws EvaluationException {
                return arg.evaluate(values);
            }

            /**
             *  ignore nested subtotals.
             */
            @Override
            public boolean isSubtotalCounted(){
                return false;
            }

            public boolean isHiddenRowCounted() {
                return countHiddenRows;
            }
        };
    }

    public static final Function AVEDEV = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return StatsLib.avedev(values);
        }
    };
    public static final Function AVERAGE = new AggregateFunction() {
        protected double evaluate(double[] values) throws EvaluationException {
            if (values.length < 1) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            return MathX.average(values);
        }
    };
    public static final Function DEVSQ = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return StatsLib.devsq(values);
        }
    };
    public static final Function LARGE = new LargeSmall(true);
    public static final Function MAX = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return values.length > 0 ? MathX.max(values) : 0;
        }
    };
    public static final Function MEDIAN = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return StatsLib.median(values);
        }
    };
    public static final Function MIN = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return values.length > 0 ? MathX.min(values) : 0;
        }
    };

    public static final Function PERCENTILE = new Percentile();

    public static final Function PRODUCT = new Product();
    public static final Function SMALL = new LargeSmall(false);
    public static final Function STDEV = new AggregateFunction() {
        protected double evaluate(double[] values) throws EvaluationException {
            if (values.length < 1) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            return StatsLib.stdev(values);
        }
    };
    public static final Function SUM = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return MathX.sum(values);
        }
    };
    public static final Function SUMSQ = new AggregateFunction() {
        protected double evaluate(double[] values) {
            return MathX.sumsq(values);
        }
    };
    public static final Function VAR = new AggregateFunction() {
        protected double evaluate(double[] values) throws EvaluationException {
            if (values.length < 1) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            return StatsLib.var(values);
        }
    };
    public static final Function VARP = new AggregateFunction() {
        protected double evaluate(double[] values) throws EvaluationException {
            if (values.length < 1) {
                throw new EvaluationException(ErrorEval.DIV_ZERO);
            }
            return StatsLib.varp(values);
        }
    };
    public static final Function GEOMEAN = new Geomean();

    private static class Product extends AggregateFunction {
        Product() {
            setMissingArgPolicy(Policy.SKIP);
        }

        @Override
        protected double evaluate(double[] values) throws EvaluationException {
            return MathX.product(values);
        }
    }

    private static class Geomean extends AggregateFunction {
        Geomean() {
            setMissingArgPolicy(Policy.COERCE);
        }

        @Override
        protected double evaluate(double[] values) throws EvaluationException {
            // The library implementation returns 0 for an input sequence like [1, 0]. So this check is necessary.
            for (double value: values) {
                if (value <= 0) {
                    throw new EvaluationException(ErrorEval.NUM_ERROR);
                }
            }
            return new GeometricMean().evaluate(values, 0, values.length);
        }
    }
}
