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

import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * 
 * Common entry point for all external functions (where 
 * <tt>AbstractFunctionPtg.field_2_fnc_index</tt> == 255)
 * 
 * @author Josh Micich
 */
final class ExternalFunction implements FreeRefFunction {

	public ValueEval evaluate(Eval[] args, int srcCellRow, short srcCellCol, HSSFWorkbook workbook, HSSFSheet sheet) {
		
		int nIncomingArgs = args.length;
		if(nIncomingArgs < 1) {
			throw new RuntimeException("function name argument missing");
		}
		
		if (!(args[0] instanceof NameEval)) {
			throw new RuntimeException("First argument should be a NameEval, but got ("
					+ args[0].getClass().getName() + ")");
		}
		NameEval functionNameEval = (NameEval) args[0];
		
		int nOutGoingArgs = nIncomingArgs -1;
		Eval[] outGoingArgs = new Eval[nOutGoingArgs];
		System.arraycopy(args, 1, outGoingArgs, 0, nOutGoingArgs);
		
		FreeRefFunction targetFunc;
		try {
			targetFunc = findTargetFunction(workbook, functionNameEval);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		
		return targetFunc.evaluate(outGoingArgs, srcCellRow, srcCellCol, workbook, sheet);
	}

	private FreeRefFunction findTargetFunction(HSSFWorkbook workbook, NameEval functionNameEval) throws EvaluationException {

		int numberOfNames = workbook.getNumberOfNames();
		
		int nameIndex = functionNameEval.getIndex();
		if(nameIndex < 0 || nameIndex >= numberOfNames) {
			throw new RuntimeException("Bad name index (" + nameIndex 
					+ "). Allowed range is (0.." + (numberOfNames-1) + ")");
		}
		
		String functionName = workbook.getNameName(nameIndex);
		if(false) {
			System.out.println("received call to external function index (" + functionName + ")");
		}
		// TODO - detect if the NameRecord corresponds to a named range, function, or something undefined
		// throw the right errors in these cases
		
		// TODO find the implementation for the external function e.g. "YEARFRAC" or "ISEVEN"
		
		throw new EvaluationException(ErrorEval.FUNCTION_NOT_IMPLEMENTED);
	}

}
