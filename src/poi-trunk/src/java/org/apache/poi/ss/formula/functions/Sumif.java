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

import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Implementation for the Excel function SUMIF<p>
 *
 * Syntax : <br>
 *  SUMIF ( <b>range</b>, <b>criteria</b>, sum_range ) <br>
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>range</th><td>The range over which criteria is applied.  Also used for addend values when the third parameter is not present</td></tr>
 *      <tr><th>criteria</th><td>The value or expression used to filter rows from <b>range</b></td></tr>
 *      <tr><th>sum_range</th><td>Locates the top-left corner of the corresponding range of addends - values to be added (after being selected by the criteria)</td></tr>
 *    </table><br>
 * </p>
 * @author Josh Micich
 */
public final class Sumif extends Var2or3ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {

		AreaEval aeRange;
		try {
			aeRange = convertRangeArg(arg0);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return eval(srcRowIndex, srcColumnIndex, arg1, aeRange, aeRange);
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {

		AreaEval aeRange;
		AreaEval aeSum;
		try {
			aeRange = convertRangeArg(arg0);
			aeSum = createSumRange(arg2, aeRange);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return eval(srcRowIndex, srcColumnIndex, arg1, aeRange, aeSum);
	}

	private static ValueEval eval(int srcRowIndex, int srcColumnIndex, ValueEval arg1, AreaEval aeRange,
			AreaEval aeSum) {
		// TODO - junit to prove last arg must be srcColumnIndex and not srcRowIndex
		I_MatchPredicate mp = Countif.createCriteriaPredicate(arg1, srcRowIndex, srcColumnIndex);
		
		// handle empty cells
		if(mp == null) {
		    return NumberEval.ZERO;
		}

		double result = sumMatchingCells(aeRange, mp, aeSum);
		return new NumberEval(result);
	}

	private static double sumMatchingCells(AreaEval aeRange, I_MatchPredicate mp, AreaEval aeSum) {
		int height=aeRange.getHeight();
		int width= aeRange.getWidth();

		double result = 0.0;
		for (int r=0; r<height; r++) {
			for (int c=0; c<width; c++) {
				result += accumulate(aeRange, mp, aeSum, r, c);
			}
		}
		return result;
	}

	private static double accumulate(AreaEval aeRange, I_MatchPredicate mp, AreaEval aeSum, int relRowIndex,
			int relColIndex) {

		if (!mp.matches(aeRange.getRelativeValue(relRowIndex, relColIndex))) {
			return 0.0;
		}
		ValueEval addend = aeSum.getRelativeValue(relRowIndex, relColIndex);
		if (addend instanceof NumberEval) {
			return ((NumberEval)addend).getNumberValue();
		}
		// everything else (including string and boolean values) counts as zero
		return 0.0;
	}

	/**
	 * @return a range of the same dimensions as aeRange using eval to define the top left corner.
	 * @throws EvaluationException if eval is not a reference
	 */
	private static AreaEval createSumRange(ValueEval eval, AreaEval aeRange) throws EvaluationException {
		if (eval instanceof AreaEval) {
			return ((AreaEval) eval).offset(0, aeRange.getHeight()-1, 0, aeRange.getWidth()-1);
		}
		if (eval instanceof RefEval) {
			return ((RefEval)eval).offset(0, aeRange.getHeight()-1, 0, aeRange.getWidth()-1);
		}
		throw new EvaluationException(ErrorEval.VALUE_INVALID);
	}

	private static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
		if (eval instanceof AreaEval) {
			return (AreaEval) eval;
		}
		if (eval instanceof RefEval) {
			return ((RefEval)eval).offset(0, 0, 0, 0);
		}
		throw new EvaluationException(ErrorEval.VALUE_INVALID);
	}

}
