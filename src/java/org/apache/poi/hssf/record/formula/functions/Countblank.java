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
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.CountUtils.I_MatchPredicate;

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
			result = CountUtils.countMatchingCell((RefEval) arg0, predicate);
		} else if (arg0 instanceof AreaEval) {
			result = CountUtils.countMatchingCellsInArea((AreaEval) arg0, predicate);
		} else {
			throw new IllegalArgumentException("Bad range arg type (" + arg0.getClass().getName() + ")");
		}
		return new NumberEval(result);
	}

	private static final I_MatchPredicate predicate = new I_MatchPredicate() {

		public boolean matches(ValueEval valueEval) {
			// Note - only BlankEval counts
			return valueEval == BlankEval.instance;
		}
	};
}
