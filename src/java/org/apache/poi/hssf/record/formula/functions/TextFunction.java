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

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class TextFunction implements Function {

	protected static final String EMPTY_STRING = "";

	protected static final String evaluateStringArg(ValueEval eval, int srcRow, short srcCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(eval, srcRow, srcCol);
		return OperandResolver.coerceValueToString(ve);
	}
	protected static final int evaluateIntArg(ValueEval arg, int srcCellRow, short srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		return OperandResolver.coerceValueToInt(ve);
	}

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		try {
			return evaluateFunc(args, srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol) throws EvaluationException;

	/* ---------------------------------------------------------------------- */

	private static abstract class SingleArgTextFunc extends TextFunction {

		protected SingleArgTextFunc() {
			// no fields to initialise
		}
		protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
				throws EvaluationException {
			if (args.length != 1) {
				return ErrorEval.VALUE_INVALID;
			}
			String arg = evaluateStringArg(args[0], srcCellRow, srcCellCol);
			return evaluate(arg);
		}
		protected abstract ValueEval evaluate(String arg);
	}

	public static final Function LEN = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new NumberEval(arg.length());
		}
	};
	public static final Function LOWER = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new StringEval(arg.toLowerCase());
		}
	};
	public static final Function UPPER = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new StringEval(arg.toUpperCase());
		}
	};
	/**
	 * An implementation of the TRIM function:
	 * Removes leading and trailing spaces from value if evaluated operand
	 *  value is string.
	 * Author: Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
	 */
	public static final Function TRIM = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new StringEval(arg.trim());
		}
	};

	/**
	 * An implementation of the MID function<br/>
	 * MID returns a specific number of
	 * characters from a text string, starting at the specified position.<p/>
	 *
	 * <b>Syntax<b>:<br/> <b>MID</b>(<b>text</b>, <b>start_num</b>,
	 * <b>num_chars</b>)<br/>
	 *
	 * Author: Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
	 */
	public static final Function MID = new TextFunction() {

		protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
				throws EvaluationException {
			if (args.length != 3) {
				return ErrorEval.VALUE_INVALID;
			}

			String text = evaluateStringArg(args[0], srcCellRow, srcCellCol);
			int startCharNum = evaluateIntArg(args[1], srcCellRow, srcCellCol);
			int numChars = evaluateIntArg(args[2], srcCellRow, srcCellCol);
			int startIx = startCharNum - 1; // convert to zero-based

			// Note - for start_num arg, blank/zero causes error(#VALUE!),
			// but for num_chars causes empty string to be returned.
			if (startIx < 0) {
				return ErrorEval.VALUE_INVALID;
			}
			if (numChars < 0) {
				return ErrorEval.VALUE_INVALID;
			}
			int len = text.length();
			if (numChars < 0 || startIx > len) {
				return new StringEval("");
			}
			int endIx = Math.min(startIx + numChars, len);
			String result = text.substring(startIx, endIx);
			return new StringEval(result);
		}
	};

	private static final class LeftRight extends TextFunction {

		private final boolean _isLeft;
		protected LeftRight(boolean isLeft) {
			_isLeft = isLeft;
		}
		protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
				throws EvaluationException {
			if (args.length != 2) {
				return ErrorEval.VALUE_INVALID;
			}
			String arg = evaluateStringArg(args[0], srcCellRow, srcCellCol);
			int index = evaluateIntArg(args[1], srcCellRow, srcCellCol);

			String result;
			if (_isLeft) {
				result = arg.substring(0, Math.min(arg.length(), index));
			} else {
				result = arg.substring(Math.max(0, arg.length()-index));
			}
			return new StringEval(result);
		}
	}

	public static final Function LEFT = new LeftRight(true);
	public static final Function RIGHT = new LeftRight(false);

	public static final Function CONCATENATE = new TextFunction() {

		protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
				throws EvaluationException {
			StringBuffer sb = new StringBuffer();
			for (int i=0, iSize=args.length; i<iSize; i++) {
				sb.append(evaluateStringArg(args[i], srcCellRow, srcCellCol));
			}
			return new StringEval(sb.toString());
		}
	};

	public static final Function EXACT = new TextFunction() {

		protected ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, short srcCellCol)
				throws EvaluationException {
			if (args.length != 2) {
				return ErrorEval.VALUE_INVALID;
			}

			String s0 = evaluateStringArg(args[0], srcCellRow, srcCellCol);
			String s1 = evaluateStringArg(args[1], srcCellRow, srcCellCol);
			return BoolEval.valueOf(s0.equals(s1));
		}
	};
}
