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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.PercentPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * Implementation of Excel formula token '%'. <p/>
 * @author Josh Micich
 */
public final class PercentEval extends NumericOperationEval {

	private PercentPtg _delegate;

	private static final ValueEvalToNumericXlator NUM_XLATOR = new ValueEvalToNumericXlator(
			(short) (ValueEvalToNumericXlator.BOOL_IS_PARSED
					| ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
					| ValueEvalToNumericXlator.STRING_IS_PARSED | ValueEvalToNumericXlator.REF_STRING_IS_PARSED));

	public PercentEval(Ptg ptg) {
		_delegate = (PercentPtg) ptg;
	}

	protected ValueEvalToNumericXlator getXlator() {
		return NUM_XLATOR;
	}

	public Eval evaluate(Eval[] args, int srcRow, short srcCol) {
		if (args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}

		ValueEval ve = singleOperandEvaluate(args[0], srcRow, srcCol);
		if (ve instanceof NumericValueEval) {
			double d0 = ((NumericValueEval) ve).getNumberValue();
			return new NumberEval(d0 / 100);
		}

		if (ve instanceof BlankEval) {
			return NumberEval.ZERO;
		}
		if (ve instanceof ErrorEval) {
			return ve;
		}
		return ErrorEval.VALUE_INVALID;
	}

	public int getNumberOfOperands() {
		return _delegate.getNumberOfOperands();
	}

	public int getType() {
		return _delegate.getType();
	}
}
