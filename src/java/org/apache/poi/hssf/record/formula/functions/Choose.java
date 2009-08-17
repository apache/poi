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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 *
 * @author Josh Micich
 */
public final class Choose implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, short srcColumnIndex) {
		if (args.length < 2) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			ValueEval ev = OperandResolver.getSingleValue(args[0], srcRowIndex, srcColumnIndex);
			int ix = OperandResolver.coerceValueToInt(ev);
			if (ix < 1 || ix >= args.length) {
				return ErrorEval.VALUE_INVALID;
			}
			return OperandResolver.getSingleValue(args[ix], srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
}
