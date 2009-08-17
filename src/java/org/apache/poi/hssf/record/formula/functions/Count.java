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
import org.apache.poi.hssf.record.formula.eval.MissingArgEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Counts the number of cells that contain numeric data within
 *  the list of arguments.
 *
 * Excel Syntax
 * COUNT(value1,value2,...)
 * Value1, value2, ...   are 1 to 30 arguments representing the values or ranges to be counted.
 *
 * TODO: Check this properly matches excel on edge cases
 *  like formula cells, error cells etc
 */
public final class Count implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		int nArgs = args.length;
		if (nArgs < 1) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}

		if (nArgs > 30) {
			// too many arguments
			return ErrorEval.VALUE_INVALID;
		}

		int temp = 0;

		for(int i=0; i<nArgs; i++) {
			temp += CountUtils.countArg(args[i], predicate);

		}
		return new NumberEval(temp);
	}

	private static final I_MatchPredicate predicate = new I_MatchPredicate() {

		public boolean matches(ValueEval valueEval) {

			if(valueEval instanceof NumberEval) {
				// only numbers are counted
				return true;
			}
			if(valueEval == MissingArgEval.instance) {
				// oh yeah, and missing arguments
				return true;
			}

			// error values and string values not counted
			return false;
		}
	};
}
