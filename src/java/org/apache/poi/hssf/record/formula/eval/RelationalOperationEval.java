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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.functions.Fixed2ArgFunction;
import org.apache.poi.hssf.record.formula.functions.Function;
import org.apache.poi.ss.util.NumberComparer;

/**
 * Base class for all comparison operator evaluators
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public abstract class RelationalOperationEval extends Fixed2ArgFunction {

	/**
	 * Converts a standard compare result (-1, 0, 1) to <code>true</code> or <code>false</code>
	 * according to subclass' comparison type.
	 */
	protected abstract boolean convertComparisonResult(int cmpResult);

	/**
	 * This is a description of how the relational operators apply in MS Excel.
	 * Use this as a guideline when testing/implementing the evaluate methods
	 * for the relational operators Evals.
	 *
	 * <pre>
	 * Bool.TRUE > any number.
	 * Bool > any string. ALWAYS
	 * Bool.TRUE > Bool.FALSE
	 * Bool.FALSE == Blank
	 *
	 * Strings are never converted to numbers or booleans
	 * String > any number. ALWAYS
	 * Non-empty String > Blank
	 * Empty String == Blank
	 * String are sorted dictionary wise
	 *
	 * Blank > Negative numbers
	 * Blank == 0
	 * Blank < Positive numbers
	 * </pre>
	 */
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {

		ValueEval vA;
		ValueEval vB;
		try {
			vA = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			vB = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		int cmpResult = doCompare(vA, vB);
		boolean result = convertComparisonResult(cmpResult);
		return BoolEval.valueOf(result);
	}

	private static int doCompare(ValueEval va, ValueEval vb) {
		// special cases when one operand is blank
		if (va == BlankEval.instance) {
			return compareBlank(vb);
		}
		if (vb == BlankEval.instance) {
			return -compareBlank(va);
		}

		if (va instanceof BoolEval) {
			if (vb instanceof BoolEval) {
				BoolEval bA = (BoolEval) va;
				BoolEval bB = (BoolEval) vb;
				if (bA.getBooleanValue() == bB.getBooleanValue()) {
					return 0;
				}
				return bA.getBooleanValue() ? 1 : -1;
			}
			return 1;
		}
		if (vb instanceof BoolEval) {
			return -1;
		}
		if (va instanceof StringEval) {
			if (vb instanceof StringEval) {
				StringEval sA = (StringEval) va;
				StringEval sB = (StringEval) vb;
				return sA.getStringValue().compareToIgnoreCase(sB.getStringValue());
			}
			return 1;
		}
		if (vb instanceof StringEval) {
			return -1;
		}
		if (va instanceof NumberEval) {
			if (vb instanceof NumberEval) {
				NumberEval nA = (NumberEval) va;
				NumberEval nB = (NumberEval) vb;
				return NumberComparer.compare(nA.getNumberValue(), nB.getNumberValue());
			}
		}
		throw new IllegalArgumentException("Bad operand types (" + va.getClass().getName() + "), ("
				+ vb.getClass().getName() + ")");
	}

	private static int compareBlank(ValueEval v) {
		if (v == BlankEval.instance) {
			return 0;
		}
		if (v instanceof BoolEval) {
			BoolEval boolEval = (BoolEval) v;
			return boolEval.getBooleanValue() ? -1 : 0;
		}
		if (v instanceof NumberEval) {
			NumberEval ne = (NumberEval) v;
			return NumberComparer.compare(0.0, ne.getNumberValue());
		}
		if (v instanceof StringEval) {
			StringEval se = (StringEval) v;
			return se.getStringValue().length() < 1 ? 0 : -1;
		}
		throw new IllegalArgumentException("bad value class (" + v.getClass().getName() + ")");
	}

	public static final Function EqualEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult == 0;
		}
	};
	public static final Function GreaterEqualEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult >= 0;
		}
	};
	public static final Function GreaterThanEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult > 0;
		}
	};
	public static final Function LessEqualEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult <= 0;
		}
	};
	public static final Function LessThanEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult < 0;
		}
	};
	public static final Function NotEqualEval = new RelationalOperationEval() {
		protected boolean convertComparisonResult(int cmpResult) {
			return cmpResult != 0;
		}
	};
}
