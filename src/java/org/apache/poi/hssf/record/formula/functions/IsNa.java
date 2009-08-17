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

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.ErrorConstants;

/**
 * Implementation for Excel ISNA() function.<p/>
 *
 * <b>Syntax</b>:<br/>
 * <b>ISNA</b>(<b>value</b>)<p/>
 *
 * <b>value</b>  The value to be tested<br/>
 * <br/>
 * Returns <tt>TRUE</tt> if the specified value is '#N/A', <tt>FALSE</tt> otherwise.
 *
 * @author Josh Micich
 */
public final class IsNa implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		if(args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}
		ValueEval arg = args[0];

		try {
			OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			if (e.getErrorEval().getErrorCode() == ErrorConstants.ERROR_NA) {
				return BoolEval.TRUE;
			}
		}
		return BoolEval.FALSE;
	}
}
