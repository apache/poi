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
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Calculates the net present value of an investment by using a discount rate
 * and a series of future payments (negative values) and income (positive
 * values). Minimum 2 arguments, first arg is the rate of discount over the
 * length of one period others up to 254 arguments representing the payments and
 * income.
 *
 * @author SPetrakovsky
 */
public final class Npv implements Function2Arg, Function3Arg, Function4Arg {


	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		double result;
		try {
			double rate = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
			double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
			result = evaluate(rate, d1);
			NumericFunction.checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		double result;
		try {
			double rate = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
			double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
			double d2 = NumericFunction.singleOperandEvaluate(arg2, srcRowIndex, srcColumnIndex);
			result = evaluate(rate, d1, d2);
			NumericFunction.checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2, ValueEval arg3) {
		double result;
		try {
			double rate = NumericFunction.singleOperandEvaluate(arg0, srcRowIndex, srcColumnIndex);
			double d1 = NumericFunction.singleOperandEvaluate(arg1, srcRowIndex, srcColumnIndex);
			double d2 = NumericFunction.singleOperandEvaluate(arg2, srcRowIndex, srcColumnIndex);
			double d3 = NumericFunction.singleOperandEvaluate(arg3, srcRowIndex, srcColumnIndex);
			result = evaluate(rate, d1, d2, d3);
			NumericFunction.checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		int nArgs = args.length;
		if (nArgs<2) {
			return ErrorEval.VALUE_INVALID;
		}
		int np = nArgs-1;
		double[] ds = new double[np];
		double result;
		try {
			double rate = NumericFunction.singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
			for (int i = 0; i < ds.length; i++) {
				ds[i] =  NumericFunction.singleOperandEvaluate(args[i+1], srcRowIndex, srcColumnIndex);
			}
			result = evaluate(rate, ds);
			NumericFunction.checkValue(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new NumberEval(result);
	}

	private static double evaluate(double rate, double...ds) {
		double sum = 0;
		for (int i = 0; i < ds.length; i++) {
			sum += ds[i] / Math.pow(rate + 1, i);
		}
		return sum;
	}
}
