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


/**
 * @author Amol S. Deshmukh &lt; amol at apache dot org &gt;
 * The NOT boolean function. Returns negation of specified value
 * (treated as a boolean). If the specified arg is a number,
 * then it is true <=> 'number is non-zero'
 */
public final class Not implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		if (args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}
		boolean boolArgVal;
		try {
			ValueEval ve = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			Boolean b = OperandResolver.coerceValueToBoolean(ve, false);
			boolArgVal = b == null ? false : b.booleanValue();
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		return BoolEval.valueOf(!boolArgVal);
	}
}
