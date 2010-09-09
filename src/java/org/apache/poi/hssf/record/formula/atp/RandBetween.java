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
package org.apache.poi.hssf.record.formula.atp;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.OperationEvaluationContext;

/**
 * Implementation of Excel 'Analysis ToolPak' function RANDBETWEEN()<br/>
 *
 * Returns a random integer number between the numbers you specify.<p/>
 *
 * <b>Syntax</b><br/>
 * <b>RANDBETWEEN</b>(<b>bottom</b>, <b>top</b>)<p/>
 *
 * <b>bottom</b> is the smallest integer RANDBETWEEN will return.<br/>
 * <b>top</b> is the largest integer RANDBETWEEN will return.<br/>

 * @author Brendan Nolan
 */
final class RandBetween implements FreeRefFunction{

	public static final FreeRefFunction instance = new RandBetween();

	private RandBetween() {
		//enforces singleton
	}

	/**
	 * Evaluate for RANDBETWEEN(). Must be given two arguments. Bottom must be greater than top.
	 * Bottom is rounded up and top value is rounded down. After rounding top has to be set greater
	 * than top.
	 * 
	 * @see org.apache.poi.hssf.record.formula.functions.FreeRefFunction#evaluate(org.apache.poi.hssf.record.formula.eval.ValueEval[], org.apache.poi.ss.formula.OperationEvaluationContext)
	 */
	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		
		double bottom, top;

		if (args.length != 2) {
			return ErrorEval.VALUE_INVALID;
		}
		
		try {
			bottom = OperandResolver.coerceValueToDouble(OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec.getColumnIndex()));
			top = OperandResolver.coerceValueToDouble(OperandResolver.getSingleValue(args[1], ec.getRowIndex(), ec.getColumnIndex()));
			if(bottom > top) {
				return ErrorEval.NUM_ERROR;
			}
		} catch (EvaluationException e) {
			return ErrorEval.VALUE_INVALID;
		}

		bottom = Math.ceil(bottom);
		top = Math.floor(top);

		if(bottom > top) {
			top = bottom;
		}
		
		return new NumberEval((bottom + (int)(Math.random() * ((top - bottom) + 1))));
		
	}
		
}
