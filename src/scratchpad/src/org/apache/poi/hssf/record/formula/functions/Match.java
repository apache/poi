/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

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
	
	private static final class EvalEx extends Exception {
		private final ErrorEval _error;

		public EvalEx(ErrorEval error) {
			_error = error;
		}
		public ErrorEval getError() {
			return _error;
		}
	}
	

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		
		double match_type = 1; // default
		
		switch(args.length) {
			case 3:
				try {
					match_type = evaluateMatchTypeArg(args[2], srcCellRow, srcCellCol);
				} catch (EvalEx e) {
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
			ValueEval lookupValue = evaluateLookupValue(args[0], srcCellRow, srcCellCol);
			ValueEval[] lookupRange = evaluateLookupRange(args[1]);
			int index = findIndexOfValue(lookupValue, lookupRange, matchExact, findLargestLessThanOrEqual);
			return new NumberEval(index + 1); // +1 to convert to 1-based
		} catch (EvalEx e) {
			return e.getError();
		}
	}

	private static ValueEval chooseSingleElementFromArea(AreaEval ae, 
			int srcCellRow, short srcCellCol) throws EvalEx {
		if (ae.isColumn()) {
			if(ae.isRow()) {
				return ae.getValues()[0];
			}
			if(!ae.containsRow(srcCellRow)) {
				throw new EvalEx(ErrorEval.VALUE_INVALID);
			}
			return ae.getValueAt(srcCellRow, ae.getFirstColumn());
		}
		if(!ae.isRow()) {
			throw new EvalEx(ErrorEval.VALUE_INVALID);
		}
		if(!ae.containsColumn(srcCellCol)) {
			throw new EvalEx(ErrorEval.VALUE_INVALID);
		}
		return ae.getValueAt(ae.getFirstRow(), srcCellCol);
		
	}

	private static ValueEval evaluateLookupValue(Eval eval, int srcCellRow, short srcCellCol)
			throws EvalEx {
		if (eval instanceof RefEval) {
			RefEval re = (RefEval) eval;
			return re.getInnerValueEval();
		}
		if (eval instanceof AreaEval) {
			return chooseSingleElementFromArea((AreaEval) eval, srcCellRow, srcCellCol);
		}
		if (eval instanceof ValueEval) {
			return (ValueEval) eval;
		}
		throw new RuntimeException("Unexpected eval type (" + eval.getClass().getName() + ")");
	}


	private static ValueEval[] evaluateLookupRange(Eval eval) throws EvalEx {
		if (eval instanceof RefEval) {
			RefEval re = (RefEval) eval;
			return new ValueEval[] { re.getInnerValueEval(), };
		}
		if (eval instanceof AreaEval) {
			AreaEval ae = (AreaEval) eval;
			if(!ae.isColumn() && !ae.isRow()) {
				throw new EvalEx(ErrorEval.NA);
			}
			return ae.getValues();
		}
		
		// Error handling for lookup_range arg is also unusual
		if(eval instanceof NumericValueEval) {
			throw new EvalEx(ErrorEval.NA);
		}
		if (eval instanceof StringEval) {
			StringEval se = (StringEval) eval;
			Double d = parseDouble(se.getStringValue());
			if(d == null) {
				// plain string
				throw new EvalEx(ErrorEval.VALUE_INVALID);
			}
			// else looks like a number
			throw new EvalEx(ErrorEval.NA);
		}
		throw new RuntimeException("Unexpected eval type (" + eval.getClass().getName() + ")");
	}


	private static Double parseDouble(String stringValue) {
		// TODO find better home for parseDouble
		return Countif.parseDouble(stringValue);
	}



	private static double evaluateMatchTypeArg(Eval arg, int srcCellRow, short srcCellCol) 
			throws EvalEx {
		Eval match_type = arg;
		if(arg instanceof AreaEval) {
			AreaEval ae = (AreaEval) arg;
			// an area ref can work as a scalar value if it is 1x1
			if(ae.isColumn() &&  ae.isRow()) {
				match_type = ae.getValues()[0];
			} else {
				match_type = chooseSingleElementFromArea(ae, srcCellRow, srcCellCol);
			}
		}
		
		if(match_type instanceof RefEval) {
			RefEval re = (RefEval) match_type;
			match_type = re.getInnerValueEval();
		}
		if(match_type instanceof ErrorEval) {
			throw new EvalEx((ErrorEval)match_type);
		}
		if(match_type instanceof NumericValueEval) {
			NumericValueEval ne = (NumericValueEval) match_type;
			return ne.getNumberValue();
		}
		if (match_type instanceof StringEval) {
			StringEval se = (StringEval) match_type;
			Double d = parseDouble(se.getStringValue());
			if(d == null) {
				// plain string
				throw new EvalEx(ErrorEval.VALUE_INVALID);
			}
			// if the string parses as a number, it is ok
			return d.doubleValue();
		}
		throw new RuntimeException("Unexpected match_type type (" + match_type.getClass().getName() + ")");
	}
	
	/**
	 * @return zero based index
	 */
	private static int findIndexOfValue(ValueEval lookupValue, ValueEval[] lookupRange,
			boolean matchExact, boolean findLargestLessThanOrEqual) throws EvalEx {
		// TODO - wildcard matching when matchExact and lookupValue is text containing * or ?
		if(matchExact) {
			for (int i = 0; i < lookupRange.length; i++) {
				ValueEval lri = lookupRange[i];
				if(lri.getClass() != lookupValue.getClass()) {
					continue;
				}
				if(compareValues(lookupValue, lri) == 0) {
					return i;
				}
			}
		} else {
			// Note - backward iteration
			if(findLargestLessThanOrEqual) {
				for (int i = lookupRange.length - 1; i>=0;  i--) {
					ValueEval lri = lookupRange[i];
					if(lri.getClass() != lookupValue.getClass()) {
						continue;
					}
					int cmp = compareValues(lookupValue, lri);
					if(cmp == 0) {
						return i;
					}
					if(cmp > 0) {
						return i;
					}
				}
			} else {
				// find smallest greater than or equal to
				for (int i = 0; i<lookupRange.length; i++) {
					ValueEval lri = lookupRange[i];
					if(lri.getClass() != lookupValue.getClass()) {
						continue;
					}
					int cmp = compareValues(lookupValue, lri);
					if(cmp == 0) {
						return i;
					}
					if(cmp > 0) {
						if(i<1) {
							throw new EvalEx(ErrorEval.NA);
						}
						return i-1;
					}
				}
				
			}
		}

		throw new EvalEx(ErrorEval.NA);
	}


	/**
	 * This method can only compare a pair of <tt>NumericValueEval</tt>s, <tt>StringEval</tt>s
	 * or <tt>BoolEval</tt>s
	 * @return negative for a&lt;b, positive for a&gt;b and 0 for a = b
	 */
	private static int compareValues(ValueEval a, ValueEval b) {
		if (a instanceof StringEval) {
			StringEval sa = (StringEval) a;
			StringEval sb = (StringEval) b;
			return sa.getStringValue().compareToIgnoreCase(sb.getStringValue());
		}
		if (a instanceof NumericValueEval) {
			NumericValueEval na = (NumericValueEval) a;
			NumericValueEval nb = (NumericValueEval) b;
			return Double.compare(na.getNumberValue(), nb.getNumberValue());
		}
		if (a instanceof BoolEval) {
			boolean ba = ((BoolEval) a).getBooleanValue();
			boolean bb = ((BoolEval) b).getBooleanValue();
			if(ba == bb) {
				return 0;
			}
			// TRUE > FALSE
			if(ba) {
				return +1;
			}
			return -1;
		}
		throw new RuntimeException("bad eval type (" + a.getClass().getName() + ")");
	}
}
