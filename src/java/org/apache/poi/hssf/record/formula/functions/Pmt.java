/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation for the PMT() Excel function.<p/>
 * 
 * <b>Syntax:</b><br/>
 * <b>PMT</b>(<b>rate</b>, <b>nper</b>, <b>pv</b>, fv, type)<p/>
 * 
 * Returns the constant repayment amount required for a loan assuming a constant interest rate.<p/>
 * 
 * <b>rate</b> the loan interest rate.<br/>
 * <b>nper</b> the number of loan repayments.<br/>
 * <b>pv</b> the present value of the future payments (or principle).<br/>
 * <b>fv</b> the future value (default zero) surplus cash at the end of the loan lifetime.<br/>
 * <b>type</b> whether payments are due at the beginning(1) or end(0 - default) of each payment period.<br/>
 * 
 */
public final class Pmt extends FinanceFunction {

	public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
		
		if(args.length < 3 || args.length > 5) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			// evaluate first three (always present) args
			double rate = evalArg(args[0], srcRow, srcCol);
			double nper = evalArg(args[1], srcRow, srcCol);
			double pv  = evalArg(args[2], srcRow, srcCol);
			double fv = 0;
			boolean arePaymentsAtPeriodBeginning = false;

			switch (args.length) {
				case 5:
					ValueEval ve = singleOperandNumericAsBoolean(args[4], srcRow, srcCol);
					if (ve instanceof ErrorEval) { 
						return ve;
					}
					arePaymentsAtPeriodBeginning = ((BoolEval) ve).getBooleanValue();
				case 4:
					fv = evalArg(args[3], srcRow, srcCol);
			}
			double d = FinanceLib.pmt(rate, nper, pv, fv, arePaymentsAtPeriodBeginning);
			if (Double.isNaN(d)) {
				return ErrorEval.VALUE_INVALID;
			}
			if (Double.isInfinite(d)) {
				return ErrorEval.NUM_ERROR;
			}
			return new NumberEval(d);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private double evalArg(Eval arg, int srcRow, short srcCol) throws EvaluationException {
		ValueEval ve = singleOperandEvaluate(arg, srcRow, srcCol);
		if(ve instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) ve);
		}
		if (ve instanceof NumericValueEval) {
			return ((NumericValueEval) ve).getNumberValue();
		}
		throw new EvaluationException(ErrorEval.VALUE_INVALID); 
	}
}
