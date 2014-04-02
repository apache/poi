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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Calculates the net present value of an investment by using a discount rate
 * and a series of future payments (negative values) and income (positive
 * values). Minimum 2 arguments, first arg is the rate of discount over the
 * length of one period others up to 254 arguments representing the payments and
 * income.
 *
 * @author SPetrakovsky
 * @author Marcel May
 */
public final class Npv implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		int nArgs = args.length;
		if (nArgs < 2) {
			return ErrorEval.VALUE_INVALID;
		}

        try {
			double rate = NumericFunction.singleOperandEvaluate(args[0], srcRowIndex, srcColumnIndex);
            // convert tail arguments into an array of doubles
            ValueEval[] vargs = new ValueEval[args.length-1];
            System.arraycopy(args, 1, vargs, 0, vargs.length);
            double[] values = AggregateFunction.ValueCollector.collectValues(vargs);

            double result = FinanceLib.npv(rate, values);
			NumericFunction.checkValue(result);
            return new NumberEval(result);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
}
