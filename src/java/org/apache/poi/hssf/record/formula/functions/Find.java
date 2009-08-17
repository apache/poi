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
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation of the FIND() function.<p/>
 *
 * <b>Syntax</b>:<br/>
 * <b>FIND</b>(<b>find_text</b>, <b>within_text</b>, start_num)<p/>
 *
 * FIND returns the character position of the first occurrence of <tt>find_text</tt> inside
 * <tt>within_text</tt>.  The third parameter, <tt>start_num</tt>, is optional (default=1)
 * and specifies where to start searching from.  Character positions are 1-based.<p/>
 *
 * @author Torstein Tauno Svendsen (torstei@officenet.no)
 */
public final class Find extends TextFunction {

	protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
			throws EvaluationException {

		int nArgs = args.length;
		if (nArgs < 2 || nArgs > 3) {
			return ErrorEval.VALUE_INVALID;
		}
		String needle = evaluateStringArg(args[0], srcCellRow, srcCellCol);
		String haystack = evaluateStringArg(args[1], srcCellRow, srcCellCol);
		int startpos;
		if (nArgs == 3) {
			startpos = evaluateIntArg(args[2], srcCellRow, srcCellCol);
			if (startpos <= 0) {
				return ErrorEval.VALUE_INVALID;
			}
			startpos--; // convert 1-based to zero based
		} else {
			startpos = 0;
		}
		int result = haystack.indexOf(needle, startpos);
		if (result == -1) {
			return ErrorEval.VALUE_INVALID;
		}
		return new NumberEval(result + 1);
	}
}
