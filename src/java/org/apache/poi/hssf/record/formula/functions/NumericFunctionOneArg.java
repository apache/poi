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

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public abstract class NumericFunctionOneArg implements Function {

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		if (args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}
		try {
			ValueEval ve = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			double d = OperandResolver.coerceValueToDouble(ve);
			if (Double.isNaN(d) || Double.isInfinite(d)) {
				return ErrorEval.NUM_ERROR;
			}
			double result = evaluate(d);
			if (Double.isNaN(result) || Double.isInfinite(result)) {
				return ErrorEval.NUM_ERROR;
			}
			return new NumberEval(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract double evaluate(double d);

	public static final Function ABS = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.abs(d);
		}
	};
	public static final Function ACOS = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.acos(d);
		}
	};
	public static final Function ACOSH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.acosh(d);
		}
	};
	public static final Function ASIN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.asin(d);
		}
	};
	public static final Function ASINH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.asinh(d);
		}
	};
	public static final Function ATAN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.atan(d);
		}
	};
	public static final Function ATANH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.atanh(d);
		}
	};
	public static final Function COS = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.cos(d);
		}
	};
	public static final Function COSH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.cosh(d);
		}
	};
	public static final Function DEGREES = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.toDegrees(d);
		}
	};
	public static final Function DOLLAR = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return d;
		}
	};
	public static final Function EXP = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.pow(Math.E, d);
		}
	};
	public static final Function FACT = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.factorial((int)d);
		}
	};
	public static final Function INT = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.round(d-0.5);
		}
	};
	public static final Function LN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.log(d);
		}
	};
    static final double LOG_10_TO_BASE_e = Math.log(10);
	public static final Function LOG10 = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.log(d) / LOG_10_TO_BASE_e;
		}
	};
	public static final Function RADIANS = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.toRadians(d);
		}
	};
	public static final Function SIGN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.sign(d);
		}
	};
	public static final Function SIN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.sin(d);
		}
	};
	public static final Function SINH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.sinh(d);
		}
	};
	public static final Function SQRT = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.sqrt(d);
		}
	};
	
	public static final Function TAN = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return Math.tan(d);
		}
	};
	public static final Function TANH = new NumericFunctionOneArg() {
		protected double evaluate(double d) {
			return MathX.tanh(d);
		}
	};
}
