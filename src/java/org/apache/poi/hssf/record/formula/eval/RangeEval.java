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
 * 
 * @author Josh Micich 
 */
public final class RangeEval implements OperationEval {

	public static final OperationEval instance = new RangeEval();
	
	private RangeEval() {
		// enforces singleton
	}

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		if(args.length != 2) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			RefEval reA = evaluateRef(args[0]);
			RefEval reB = evaluateRef(args[1]);
			return resolveRange(reA, reB);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static AreaEval resolveRange(RefEval reA, RefEval reB) {
		
		int height = reB.getRow() - reA.getRow();
		int width = reB.getColumn() - reA.getColumn();
		
		return reA.offset(0, height, 0, width);
	}

	private static RefEval evaluateRef(Eval arg) throws EvaluationException {
		if (arg instanceof RefEval) {
			return (RefEval) arg;
		}
		if (arg instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)arg);
		}
		throw new IllegalArgumentException("Unexpected ref arg class (" + arg.getClass().getName() + ")");
	}

	public int getNumberOfOperands() {
		return 2;
	}
}
