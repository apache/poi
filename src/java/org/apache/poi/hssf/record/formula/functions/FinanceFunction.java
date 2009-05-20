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

import org.apache.poi.hssf.record.formula.eval.EvaluationException;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * Super class for all Evals for financial function evaluation.
 *
 */
public abstract class FinanceFunction extends NumericFunction.MultiArg {

	protected FinanceFunction() {
		super (3, 5);
	}

	protected double evaluate(double[] ds) throws EvaluationException {
		// All finance functions have 3 to 5 args, first 4 are numbers, last is boolean
		// default for last 2 args are 0.0 and false
		// Text boolean literals are not valid for the last arg

		double arg3 = 0.0;
		double arg4 = 0.0;

		switch(ds.length) {
			case 5:
				arg4 = ds[4];
			case 4:
				arg3 = ds[3];
			case 3:
				break;
			default:
				throw new IllegalStateException("Wrong number of arguments");
		}
		return evaluate(ds[0], ds[1], ds[2], arg3, arg4!=0.0);
	}

	protected abstract double evaluate(double rate, double arg1, double arg2, double arg3, boolean type) throws EvaluationException ;


	public static final Function FV = new FinanceFunction() {
		protected double evaluate(double rate, double arg1, double arg2, double arg3, boolean type) {
			return FinanceLib.fv(rate, arg1, arg2, arg3, type);
		}
	};
	public static final Function NPER = new FinanceFunction() {
		protected double evaluate(double rate, double arg1, double arg2, double arg3, boolean type) {
			return FinanceLib.nper(rate, arg1, arg2, arg3, type);
		}
	};
	public static final Function PMT = new FinanceFunction() {
		protected double evaluate(double rate, double arg1, double arg2, double arg3, boolean type) {
			return FinanceLib.pmt(rate, arg1, arg2, arg3, type);
		}
	};
	public static final Function PV = new FinanceFunction() {
		protected double evaluate(double rate, double arg1, double arg2, double arg3, boolean type) {
			return FinanceLib.pv(rate, arg1, arg2, arg3, type);
		}
	};
}
