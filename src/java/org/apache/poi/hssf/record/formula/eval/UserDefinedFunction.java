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
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.NotImplementedException;
/**
 *
 * Common entry point for all user-defined (non-built-in) functions (where
 * <tt>AbstractFunctionPtg.field_2_fnc_index</tt> == 255)
 *
 * @author Josh Micich
 * @author Petr Udalau - Improved resolving of UDFs through the ToolPacks.
 */
final class UserDefinedFunction implements FreeRefFunction {

	public static final FreeRefFunction instance = new UserDefinedFunction();

	private UserDefinedFunction() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		int nIncomingArgs = args.length;
		if(nIncomingArgs < 1) {
			throw new RuntimeException("function name argument missing");
		}

		ValueEval nameArg = args[0];
		String functionName;
		if (nameArg instanceof NameEval) {
			functionName = ((NameEval) nameArg).getFunctionName();
		} else if (nameArg instanceof NameXEval) {
			functionName = ec.getWorkbook().resolveNameXText(((NameXEval) nameArg).getPtg());
		} else {
			throw new RuntimeException("First argument should be a NameEval, but got ("
					+ nameArg.getClass().getName() + ")");
		}
		FreeRefFunction targetFunc = ec.findUserDefinedFunction(functionName);
		if (targetFunc == null) {
			throw new NotImplementedException(functionName);
		}
		int nOutGoingArgs = nIncomingArgs -1;
		ValueEval[] outGoingArgs = new ValueEval[nOutGoingArgs];
		System.arraycopy(args, 1, outGoingArgs, 0, nOutGoingArgs);
		return targetFunc.evaluate(outGoingArgs, ec);
	}
}
