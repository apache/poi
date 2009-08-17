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
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.CompareResult;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.LookupValueComparer;
import org.apache.poi.hssf.record.formula.functions.LookupUtils.ValueVector;

/**
 * Implementation for the MATCH() Excel function.<p/>
 *
 * <b>Syntax:</b><br/>
 * <b>MATCH</b>(<b>lookup_value</b>, <b>lookup_array</b>, match_type)<p/>
 *
 * Returns a 1-based index specifying at what position in the <b>lookup_array</b> the specified
 * <b>lookup_value</b> is found.<p/>
 *
 * Specific matching behaviour can be modified with the optional <b>match_type</b> parameter.
 *
 *    <table border="0" cellpadding="1" cellspacing="0" summary="match_type parameter description">
 *      <tr><th>Value</th><th>Matching Behaviour</th></tr>
 *      <tr><td>1</td><td>(default) find the largest value that is less than or equal to lookup_value.
 *        The lookup_array must be in ascending <i>order</i>*.</td></tr>
 *      <tr><td>0</td><td>find the first value that is exactly equal to lookup_value.
 *        The lookup_array can be in any order.</td></tr>
 *      <tr><td>-1</td><td>find the smallest value that is greater than or equal to lookup_value.
 *        The lookup_array must be in descending <i>order</i>*.</td></tr>
 *    </table>
 *
 * * Note regarding <i>order</i> - For the <b>match_type</b> cases that require the lookup_array to
 *  be ordered, MATCH() can produce incorrect results if this requirement is not met.  Observed
 *  behaviour in Excel is to return the lowest index value for which every item after that index
 *  breaks the match rule.<br>
 *  The (ascending) sort order expected by MATCH() is:<br/>
 *  numbers (low to high), strings (A to Z), boolean (FALSE to TRUE)<br/>
 *  MATCH() ignores all elements in the lookup_array with a different type to the lookup_value.
 *  Type conversion of the lookup_array elements is never performed.
 *
 *
 * @author Josh Micich
 */
public final class Match implements Function {


	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {

		double match_type = 1; // default

		switch(args.length) {
			case 3:
				try {
					match_type = evaluateMatchTypeArg(args[2], srcCellRow, srcCellCol);
				} catch (EvaluationException e) {
					// Excel/MATCH() seems to have slightly abnormal handling of errors with
					// the last parameter.  Errors do not propagate up.  Every error gets
					// translated into #REF!
					return ErrorEval.REF_INVALID;
				}
			case 2:
				break;
			default:
				return ErrorEval.VALUE_INVALID;
		}

		boolean matchExact = match_type == 0;
		// Note - Excel does not strictly require -1 and +1
		boolean findLargestLessThanOrEqual = match_type > 0;


		try {
			ValueEval lookupValue = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			ValueVector lookupRange = evaluateLookupRange(args[1]);
			int index = findIndexOfValue(lookupValue, lookupRange, matchExact, findLargestLessThanOrEqual);
			return new NumberEval(index + 1); // +1 to convert to 1-based
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static final class SingleValueVector implements ValueVector {

		private final ValueEval _value;

		public SingleValueVector(ValueEval value) {
			_value = value;
		}

		public ValueEval getItem(int index) {
			if (index != 0) {
				throw new RuntimeException("Invalid index ("
						+ index + ") only zero is allowed");
			}
			return _value;
		}

		public int getSize() {
			return 1;
		}
	}

	private static ValueVector evaluateLookupRange(ValueEval eval) throws EvaluationException {
		if (eval instanceof RefEval) {
			RefEval re = (RefEval) eval;
			return new SingleValueVector(re.getInnerValueEval());
		}
		if (eval instanceof AreaEval) {
			ValueVector result = LookupUtils.createVector((AreaEval)eval);
			if (result == null) {
				throw new EvaluationException(ErrorEval.NA);
			}
			return result;
		}

		// Error handling for lookup_range arg is also unusual
		if(eval instanceof NumericValueEval) {
			throw new EvaluationException(ErrorEval.NA);
		}
		if (eval instanceof StringEval) {
			StringEval se = (StringEval) eval;
			Double d = OperandResolver.parseDouble(se.getStringValue());
			if(d == null) {
				// plain string
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			// else looks like a number
			throw new EvaluationException(ErrorEval.NA);
		}
		throw new RuntimeException("Unexpected eval type (" + eval.getClass().getName() + ")");
	}



	private static double evaluateMatchTypeArg(ValueEval arg, int srcCellRow, short srcCellCol)
			throws EvaluationException {
		ValueEval match_type = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);

		if(match_type instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)match_type);
		}
		if(match_type instanceof NumericValueEval) {
			NumericValueEval ne = (NumericValueEval) match_type;
			return ne.getNumberValue();
		}
		if (match_type instanceof StringEval) {
			StringEval se = (StringEval) match_type;
			Double d = OperandResolver.parseDouble(se.getStringValue());
			if(d == null) {
				// plain string
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			// if the string parses as a number, it is OK
			return d.doubleValue();
		}
		throw new RuntimeException("Unexpected match_type type (" + match_type.getClass().getName() + ")");
	}

	/**
	 * @return zero based index
	 */
	private static int findIndexOfValue(ValueEval lookupValue, ValueVector lookupRange,
			boolean matchExact, boolean findLargestLessThanOrEqual) throws EvaluationException {

		LookupValueComparer lookupComparer = createLookupComparer(lookupValue, matchExact);

		int size = lookupRange.getSize();
		if(matchExact) {
			for (int i = 0; i < size; i++) {
				if(lookupComparer.compareTo(lookupRange.getItem(i)).isEqual()) {
					return i;
				}
			}
			throw new EvaluationException(ErrorEval.NA);
		}

		if(findLargestLessThanOrEqual) {
			// Note - backward iteration
			for (int i = size - 1; i>=0;  i--) {
				CompareResult cmp = lookupComparer.compareTo(lookupRange.getItem(i));
				if(cmp.isTypeMismatch()) {
					continue;
				}
				if(!cmp.isLessThan()) {
					return i;
				}
			}
			throw new EvaluationException(ErrorEval.NA);
		}

		// else - find smallest greater than or equal to
		// TODO - is binary search used for (match_type==+1) ?
		for (int i = 0; i<size; i++) {
			CompareResult cmp = lookupComparer.compareTo(lookupRange.getItem(i));
			if(cmp.isEqual()) {
				return i;
			}
			if(cmp.isGreaterThan()) {
				if(i<1) {
					throw new EvaluationException(ErrorEval.NA);
				}
				return i-1;
			}
		}

		throw new EvaluationException(ErrorEval.NA);
	}

	private static LookupValueComparer createLookupComparer(ValueEval lookupValue, boolean matchExact) {
		if (matchExact && lookupValue instanceof StringEval) {
			String stringValue = ((StringEval) lookupValue).getStringValue();
			if(isLookupValueWild(stringValue)) {
				throw new RuntimeException("Wildcard lookup values '" + stringValue + "' not supported yet");
			}

		}
		return LookupUtils.createLookupComparer(lookupValue);
	}

	private static boolean isLookupValueWild(String stringValue) {
		if(stringValue.indexOf('?') >=0 || stringValue.indexOf('*') >=0) {
			return true;
		}
		return false;
	}
}
