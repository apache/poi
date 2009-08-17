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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation for Excel COLUMNS function.
 *
 * @author Josh Micich
 */
public final class Columns implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		switch(args.length) {
			case 1:
				// expected
				break;
			case 0:
				// too few arguments
				return ErrorEval.VALUE_INVALID;
			default:
				// too many arguments
				return ErrorEval.VALUE_INVALID;
		}
		ValueEval firstArg = args[0];

		int result;
		if (firstArg instanceof AreaEval) {
			AreaEval ae = (AreaEval) firstArg;
			result = ae.getLastColumn() - ae.getFirstColumn() + 1;
		} else if (firstArg instanceof RefEval) {
			result = 1;
		} else { // anything else is not valid argument
			return ErrorEval.VALUE_INVALID;
		}
		return new NumberEval(result);
	}
}
