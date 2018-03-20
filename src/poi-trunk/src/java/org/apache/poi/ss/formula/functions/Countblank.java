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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Implementation for the function COUNTBLANK
 * <p>
 *  Syntax: COUNTBLANK ( range )
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>range&nbsp;&nbsp;&nbsp;</th><td>is the range of cells to count blanks</td></tr>
 *    </table>
 * </p>
 *
 * @author Mads Mohr Christensen
 */
public final class Countblank extends Fixed1ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {

		double result;
		if (arg0 instanceof RefEval) {
			result = CountUtils.countMatchingCellsInRef((RefEval) arg0, predicate);
		} else if (arg0 instanceof ThreeDEval) {
			result = CountUtils.countMatchingCellsInArea((ThreeDEval) arg0, predicate);
		} else {
			throw new IllegalArgumentException("Bad range arg type (" + arg0.getClass().getName() + ")");
		}
		return new NumberEval(result);
	}

	private static final I_MatchPredicate predicate = new I_MatchPredicate() {

		public boolean matches(ValueEval valueEval) {
			// Note - only BlankEval counts
			return valueEval == BlankEval.instance ||
					// see https://support.office.com/en-us/article/COUNTBLANK-function-6a92d772-675c-4bee-b346-24af6bd3ac22
					// "Cells with formulas that return "" (empty text) are also counted."
					(valueEval instanceof StringEval && ((StringEval)valueEval).getStringValue().isEmpty());
		}
	};
}
