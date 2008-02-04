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
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation for the function COUNTIF<p/>
 * 
 * Syntax: COUNTIF ( range, criteria )
 *    <table border="0" cellpadding="1" cellspacing="0" summary="Parameter descriptions">
 *      <tr><th>range&nbsp;&nbsp;&nbsp;</th><td>is the range of cells to be counted based on the criteria</td></tr>
 *      <tr><th>criteria</th><td>is used to determine which cells to count</td></tr>
 *    </table>
 * <p/>
 * 
 * @author Josh Micich
 */
public final class Countif implements Function {
	
	/**
	 * Common interface for the matching criteria.
	 */
	private interface I_MatchPredicate {
		boolean matches(Eval x);
	}
	
	private static final class NumberMatcher implements I_MatchPredicate {

		private final double _value;

		public NumberMatcher(double value) {
			_value = value;
		}

		public boolean matches(Eval x) {
			if(x instanceof StringEval) {
				// if the target(x) is a string, but parses as a number
				// it may still count as a match
				StringEval se = (StringEval)x;
				Double val = parseDouble(se.getStringValue());
				if(val == null) {
					// x is text that is not a number
					return false;
				}
				return val.doubleValue() == _value;
			}
			if(!(x instanceof NumberEval)) {
				return false;
			}
			NumberEval ne = (NumberEval) x;
			return ne.getNumberValue() == _value;
		}
	}
	private static final class BooleanMatcher implements I_MatchPredicate {

		private final boolean _value;

		public BooleanMatcher(boolean value) {
			_value = value;
		}

		public boolean matches(Eval x) {
			if(x instanceof StringEval) {
				StringEval se = (StringEval)x;
				Boolean val = parseBoolean(se.getStringValue());
				if(val == null) {
					// x is text that is not a boolean
					return false;
				}
				if (true) { // change to false to observe more intuitive behaviour
					// Note - Unlike with numbers, it seems that COUNTA never matches 
					// boolean values when the target(x) is a string
					return false;
				}
				return val.booleanValue() == _value;
			}
			if(!(x instanceof BoolEval)) {
				return false;
			}
			BoolEval be = (BoolEval) x;
			return be.getBooleanValue() == _value;
		}
	}
	private static final class StringMatcher implements I_MatchPredicate {

		private final String _value;

		public StringMatcher(String value) {
			_value = value;
		}

		public boolean matches(Eval x) {
			if(!(x instanceof StringEval)) {
				return false;
			}
			StringEval se = (StringEval) x;
			return se.getStringValue() == _value;
		}
	}

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		switch(args.length) {
			case 2:
				// expected
				break;
			default:
				// TODO - it doesn't seem to be possible to enter COUNTIF() into Excel with the wrong arg count
				// perhaps this should be an exception
				return ErrorEval.VALUE_INVALID;
		}
		
		AreaEval range = (AreaEval) args[0];
		Eval criteriaArg = args[1];
		if(criteriaArg instanceof RefEval) {
			// criteria is not a literal value, but a cell reference
			// for example COUNTIF(B2:D4, E1)
			RefEval re = (RefEval)criteriaArg;
			criteriaArg = re.getInnerValueEval();
		} else {
			// other non literal tokens such as function calls, have been fully evaluated
			// for example COUNTIF(B2:D4, COLUMN(E1))
		}
		I_MatchPredicate mp = createCriteriaPredicate(criteriaArg);
		return countMatchingCellsInArea(range, mp);
	}
	/**
	 * @return the number of evaluated cells in the range that match the specified criteria
	 */
	private Eval countMatchingCellsInArea(AreaEval range, I_MatchPredicate criteriaPredicate) {
		ValueEval[] values = range.getValues();
		int result = 0;
		for (int i = 0; i < values.length; i++) {
			if(criteriaPredicate.matches(values[i])) {
				result++;
			}
		}
		return new NumberEval(result);
	}
	
	private static I_MatchPredicate createCriteriaPredicate(Eval evaluatedCriteriaArg) {
		if(evaluatedCriteriaArg instanceof NumberEval) {
			return new NumberMatcher(((NumberEval)evaluatedCriteriaArg).getNumberValue());
		}
		if(evaluatedCriteriaArg instanceof BoolEval) {
			return new BooleanMatcher(((BoolEval)evaluatedCriteriaArg).getBooleanValue());
		}
		
		if(evaluatedCriteriaArg instanceof StringEval) {
			return createGeneralMatchPredicate((StringEval)evaluatedCriteriaArg);
		}
		throw new RuntimeException("Unexpected type for criteria (" 
				+ evaluatedCriteriaArg.getClass().getName() + ")");
	}

	/**
	 * When the second argument is a string, many things are possible
	 */
	private static I_MatchPredicate createGeneralMatchPredicate(StringEval stringEval) {
		String value = stringEval.getStringValue();
		char firstChar = value.charAt(0);
		Boolean booleanVal = parseBoolean(value);
		if(booleanVal != null) {
			return new BooleanMatcher(booleanVal.booleanValue());
		}
		
		Double doubleVal = parseDouble(value);
		if(doubleVal != null) {
			return new NumberMatcher(doubleVal.doubleValue());
		}
		switch(firstChar) {
			case '>':
			case '<':
			case '=':
				throw new RuntimeException("Incomplete code - criteria expressions such as '"
						+ value + "' not supported yet");
		}
		
		//else - just a plain string with no interpretation.
		return new StringMatcher(value);
	}

	/**
	 * Under certain circumstances COUNTA will equate a plain number with a string representation of that number
	 */
	/* package */ static Double parseDouble(String strRep) {
		if(!Character.isDigit(strRep.charAt(0))) {
			// avoid using NumberFormatException to tell when string is not a number
			return null;
		}
		// TODO - support notation like '1E3' (==1000)
		
		double val;
		try {
			val = Double.parseDouble(strRep);
		} catch (NumberFormatException e) {
			return null;
		}
		return new Double(val);
	}
	/**
	 * Boolean literals ('TRUE', 'FALSE') treated similarly but NOT same as numbers. 
	 */
	/* package */ static Boolean parseBoolean(String strRep) {
		switch(strRep.charAt(0)) {
			case 't':
			case 'T':
				if("TRUE".equalsIgnoreCase(strRep)) {
					return Boolean.TRUE;
				}
				break;
			case 'f':
			case 'F':
				if("FALSE".equalsIgnoreCase(strRep)) {
					return Boolean.FALSE;
				}
				break;
		}
		return null;
	}
}
