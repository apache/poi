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

package org.apache.poi.ss.formula.atp;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
/**
 * Implementation of 'Analysis Toolpak' Excel function IFERROR()<br>
 *
 * Returns an error text if there is an error in the evaluation<p>
 * 
 * <b>Syntax</b><br>
 * <b>IFERROR</b>(<b>expression</b>, <b>string</b>)
 * 
 * @author Johan Karlsteen
 */
final class IfError implements FreeRefFunction {

	public static final FreeRefFunction instance = new IfError();

	private IfError() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		if (args.length != 2) {
			return ErrorEval.VALUE_INVALID;
		}

		ValueEval val;
		try {
			val = evaluateInternal(args[0], args[1], ec.getRowIndex(), ec.getColumnIndex());
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		return val;
	}

	private static ValueEval evaluateInternal(ValueEval arg, ValueEval iferror, int srcCellRow, int srcCellCol) throws EvaluationException {
		arg = WorkbookEvaluator.dereferenceResult(arg, srcCellRow, srcCellCol);
		if(arg instanceof ErrorEval) {
			return iferror;
		} else {
			return arg;
		}
	}
}
