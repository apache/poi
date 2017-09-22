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

import org.apache.poi.ss.formula.eval.*;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 * @author Stephen Wolke (smwolke at geistig.com)
 */
public abstract class NumericFunction implements Function {

	static final double ZERO = 0.0;
	static final double TEN = 10.0;
	static final double LOG_10_TO_BASE_e = Math.log(TEN);

	protected static double singleOperandEvaluate(ValueEval arg, int srcRowIndex, int srcColumnIndex) throws EvaluationException {
		if (arg == null) {
			throw new IllegalArgumentException("arg must not be null");
		}
		ValueEval ve = OperandResolver.getSingleValue(arg, srcRowIndex, srcColumnIndex);
		double result = OperandResolver.coerceValueToDouble(ve);
		checkValue(result);
		return result;
	}

	/**
	 * @throws EvaluationException (#NUM!) if <tt>result</tt> is <tt>NaN</> or <tt>Infinity</tt>
	 */
	public static void checkValue(double result) throws EvaluationException {
		if (Double.isNaN(result) || Double.isInfinite(result)) {
			throw new EvaluationException(ErrorEval.NUM_ERROR);
		}
	}

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		double result;
		try {
			result = eval(args, srcCellRow, srcCellCol);
			checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}

	protected abstract double eval(ValueEval[] args, int srcCellRow, int srcCellCol) throws EvaluationException;

	/* -------------------------------------------------------------------------- */
	// intermediate sub-classes (one-arg, two-arg and multi-arg)

	public static abstract class OneArg extends Fixed1ArgFunction {
		protected OneArg() {
			// no fields to initialise
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			double result;
			try {
				double d = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				result = evaluate(d);
				checkValue(result);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return new NumberEval(result);
		}
		protected final double eval(ValueEval[] args, int srcCellRow, int srcCellCol) throws EvaluationException {
			if (args.length != 1) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			double d = singleOperandEvaluate(args[0], srcCellRow, srcCellCol);
			return evaluate(d);
		}
		protected abstract double evaluate(double d) throws EvaluationException;
	}

	public static abstract class TwoArg extends Fixed2ArgFunction {
		protected TwoArg() {
			// no fields to initialise
		}


		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			double result;
			try {
				double d0 = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				double d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
				result =  evaluate(d0, d1);
				checkValue(result);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return new NumberEval(result);
		}

		protected abstract double evaluate(double d0, double d1) throws EvaluationException;
	}

	/* -------------------------------------------------------------------------- */

	public static final Function ABS = new OneArg() {
		protected double evaluate(double d) {
			return Math.abs(d);
		}
	};
	public static final Function ACOS = new OneArg() {
		protected double evaluate(double d) {
			return Math.acos(d);
		}
	};
	public static final Function ACOSH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.acosh(d);
		}
	};
	public static final Function ASIN = new OneArg() {
		protected double evaluate(double d) {
			return Math.asin(d);
		}
	};
	public static final Function ASINH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.asinh(d);
		}
	};
	public static final Function ATAN = new OneArg() {
		protected double evaluate(double d) {
			return Math.atan(d);
		}
	};
	public static final Function ATANH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.atanh(d);
		}
	};
	public static final Function COS = new OneArg() {
		protected double evaluate(double d) {
			return Math.cos(d);
		}
	};
	public static final Function COSH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.cosh(d);
		}
	};
	public static final Function DEGREES = new OneArg() {
		protected double evaluate(double d) {
			return Math.toDegrees(d);
		}
	};
	static final NumberEval DOLLAR_ARG2_DEFAULT = new NumberEval(2.0);
	public static final Function DOLLAR = new Var1or2ArgFunction() {
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			return evaluate(srcRowIndex, srcColumnIndex, arg0, DOLLAR_ARG2_DEFAULT);
		}

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
				ValueEval arg1) {
			double val;
			double d1;
			try {
				val = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			// second arg converts to int by truncating toward zero
			int nPlaces = (int)d1;

			if (nPlaces > 127) {
				return ErrorEval.VALUE_INVALID;
			}


			// TODO - DOLLAR() function impl is NQR
			// result should be StringEval, with leading '$' and thousands separators
			// current junits are asserting incorrect behaviour
			return new NumberEval(val);
		}
	};
	public static final Function EXP = new OneArg() {
		protected double evaluate(double d) {
			return Math.pow(Math.E, d);
		}
	};
	public static final Function FACT = new OneArg() {
		protected double evaluate(double d) {
			return MathX.factorial((int)d);
		}
	};
	public static final Function INT = new OneArg() {
		protected double evaluate(double d) {
			return Math.round(d-0.5);
		}
	};
	public static final Function LN = new OneArg() {
		protected double evaluate(double d) {
			return Math.log(d);
		}
	};
	public static final Function LOG10 = new OneArg() {
		protected double evaluate(double d) {
			return Math.log(d) / LOG_10_TO_BASE_e;
		}
	};
	public static final Function RADIANS = new OneArg() {
		protected double evaluate(double d) {
			return Math.toRadians(d);
		}
	};
	public static final Function SIGN = new OneArg() {
		protected double evaluate(double d) {
			return MathX.sign(d);
		}
	};
	public static final Function SIN = new OneArg() {
		protected double evaluate(double d) {
			return Math.sin(d);
		}
	};
	public static final Function SINH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.sinh(d);
		}
	};
	public static final Function SQRT = new OneArg() {
		protected double evaluate(double d) {
			return Math.sqrt(d);
		}
	};

	public static final Function TAN = new OneArg() {
		protected double evaluate(double d) {
			return Math.tan(d);
		}
	};
	public static final Function TANH = new OneArg() {
		protected double evaluate(double d) {
			return MathX.tanh(d);
		}
	};

	/* -------------------------------------------------------------------------- */

	public static final Function ATAN2 = new TwoArg() {
		protected double evaluate(double d0, double d1) throws EvaluationException {
			if (d0 == ZERO && d1 == ZERO) {
				throw new EvaluationException(ErrorEval.DIV_ZERO);
			}
			return Math.atan2(d1, d0);
		}
	};
	public static final Function CEILING = new TwoArg() {
		protected double evaluate(double d0, double d1) {
			return MathX.ceiling(d0, d1);
		}
	};
	public static final Function COMBIN = new TwoArg() {
		protected double evaluate(double d0, double d1) throws EvaluationException {
			if (d0 > Integer.MAX_VALUE || d1 > Integer.MAX_VALUE) {
				throw new EvaluationException(ErrorEval.NUM_ERROR);
			}
			return  MathX.nChooseK((int) d0, (int) d1);
		}
	};
	public static final Function FLOOR = new TwoArg() {
		protected double evaluate(double d0, double d1) throws EvaluationException {
			if (d1 == ZERO) {
				if (d0 == ZERO) {
					return ZERO;
				}
				throw new EvaluationException(ErrorEval.DIV_ZERO);
			}
			return MathX.floor(d0, d1);
		}
	};
	public static final Function MOD = new TwoArg() {
		protected double evaluate(double d0, double d1) throws EvaluationException {
			if (d1 == ZERO) {
				throw new EvaluationException(ErrorEval.DIV_ZERO);
			}
			return MathX.mod(d0, d1);
		}
	};
	public static final Function POWER = new TwoArg() {
		protected double evaluate(double d0, double d1) {
			return Math.pow(d0, d1);
		}
	};
	public static final Function ROUND = new TwoArg() {
		protected double evaluate(double d0, double d1) {
			return MathX.round(d0, (int)d1);
		}
	};
	public static final Function ROUNDDOWN = new TwoArg() {
		protected double evaluate(double d0, double d1) {
			return MathX.roundDown(d0, (int)d1);
		}
	};
	public static final Function ROUNDUP = new TwoArg() {
		protected double evaluate(double d0, double d1) {
			return MathX.roundUp(d0, (int)d1);
		}
	};
	static final NumberEval TRUNC_ARG2_DEFAULT = new NumberEval(0);
	public static final Function TRUNC = new Var1or2ArgFunction() {

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			return evaluate(srcRowIndex, srcColumnIndex, arg0, TRUNC_ARG2_DEFAULT);
		}

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			double result;
			try {
				double d0 = singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				double d1 = singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
				double multi = Math.pow(10d,d1);
				if(d0 < 0) result = -Math.floor(-d0 * multi) / multi;
                else result = Math.floor(d0 * multi) / multi;
				checkValue(result);
			}catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return new NumberEval(result);
		}
	};

	/* -------------------------------------------------------------------------- */

	private static final class Log extends Var1or2ArgFunction {
		public Log() {
			// no instance fields
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			double result;
			try {
				double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				result = Math.log(d0) / LOG_10_TO_BASE_e;
				NumericFunction.checkValue(result);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return new NumberEval(result);
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
				ValueEval arg1) {
			double result;
			try {
				double d0 = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
				double logE = Math.log(d0);
                if (Double.compare(d1, Math.E) == 0) {
					result = logE;
				} else {
					result = logE / Math.log(d1);
				}
				NumericFunction.checkValue(result);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return new NumberEval(result);
		}
	}

	public static final Function LOG = new Log();

	static final NumberEval PI_EVAL = new NumberEval(Math.PI);
	public static final Function PI = new Fixed0ArgFunction() {
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
			return PI_EVAL;
		}
	};
	public static final Function RAND = new Fixed0ArgFunction() {
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
			return new NumberEval(Math.random());
		}
	};
    public static final Function POISSON = new Fixed3ArgFunction() {

        private final static double DEFAULT_RETURN_RESULT =1;
        
        /**
         * This checks is x = 0 and the mean = 0.
         * Excel currently returns the value 1 where as the
         * maths common implementation will error.
         * @param x  The number.
         * @param mean The mean.
         * @return If a default value should be returned.
         */
        private boolean isDefaultResult(double x, double mean) {

            if ( x == 0 && mean == 0 ) {
                return true;
            }
            return false;
        }

        private boolean checkArgument(double aDouble) throws EvaluationException {

            NumericFunction.checkValue(aDouble);

            // make sure that the number is positive
            if (aDouble < 0) {
                throw new EvaluationException(ErrorEval.NUM_ERROR);
            }
            
            return true;
        }

        private double probability(int k, double lambda) {
            return Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
        }

        private double cumulativeProbability(int x, double lambda) {
            double result = 0;
            for(int k = 0; k <= x; k++){
                result += probability(k, lambda);
            }
            return result;
        }

        /** All long-representable factorials */
        private final long[] FACTORIALS = new long[] {
                           1l,                  1l,                   2l,
                           6l,                 24l,                 120l,
                         720l,               5040l,               40320l,
                      362880l,            3628800l,            39916800l,
                   479001600l,         6227020800l,         87178291200l,
               1307674368000l,     20922789888000l,     355687428096000l,
            6402373705728000l, 121645100408832000l, 2432902008176640000l };


        public long factorial(final int n) {
            if (n < 0 || n > 20) {
                throw new IllegalArgumentException("Valid argument should be in the range [0..20]");
            }
            return FACTORIALS[n];
        }

        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1, ValueEval arg2) {

            // arguments/result for this function
            double mean=0;
            double x=0;
            boolean cumulative = ((BoolEval)arg2).getBooleanValue();
            double result=0;

            try {
				x = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
				mean = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);

                // check for default result : excel implementation for 0,0
                // is different to Math Common.
                if (isDefaultResult(x,mean)) {
                    return new NumberEval(DEFAULT_RETURN_RESULT); 
                }
                // check the arguments : as per excel function def
                checkArgument(x);
                checkArgument(mean);

                // truncate x : as per excel function def
                if ( cumulative ) {
                    result = cumulativeProbability((int)x, mean);
                } else {
                    result = probability((int)x, mean);
                }

                // check the result
                NumericFunction.checkValue(result);

			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
            
            return new NumberEval(result);

        }
    };
}
