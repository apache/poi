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

/**
 * @author Josh Micich
 */
abstract class TwoOperandNumericOperation implements OperationEval {

	public final int getType() {
    	// TODO - remove
        throw new RuntimeException("obsolete code should not be called");
    }
    protected final double singleOperandEvaluate(Eval arg, int srcCellRow, short srcCellCol) throws EvaluationException {
    	ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		if (ve instanceof BlankEval) {
			return 0.0;
		}
   		return OperandResolver.coerceValueToDouble(ve);
    }
    
    public final Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		double result;
		try {
			double d0 = singleOperandEvaluate(args[0], srcCellRow, srcCellCol);
			double d1 = singleOperandEvaluate(args[1], srcCellRow, srcCellCol);
			result = evaluate(d0, d1);
			if (Double.isNaN(result) || Double.isInfinite(result)) {
				return ErrorEval.NUM_ERROR;
			}
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
    	return new NumberEval(result);
    }
	protected abstract double evaluate(double d0, double d1) throws EvaluationException;
	public final int getNumberOfOperands() {
		return 2;
	}
}
