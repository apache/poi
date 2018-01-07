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

import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.util.Locale;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 * @author Stephen Wolke (smwolke at geistig.com)
 */
public abstract class TextFunction implements Function {
	protected static final DataFormatter formatter = new DataFormatter();

	protected static String evaluateStringArg(ValueEval eval, int srcRow, int srcCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(eval, srcRow, srcCol);
		return OperandResolver.coerceValueToString(ve);
	}
	protected static int evaluateIntArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		return OperandResolver.coerceValueToInt(ve);
	}
	
	protected static double evaluateDoubleArg(ValueEval arg, int srcCellRow, int srcCellCol) throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		return OperandResolver.coerceValueToDouble(ve);
	}

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		try {
			return evaluateFunc(args, srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract ValueEval evaluateFunc(ValueEval[] args, int srcCellRow, int srcCellCol) throws EvaluationException;

	/* ---------------------------------------------------------------------- */

	private static abstract class SingleArgTextFunc extends Fixed1ArgFunction {

		protected SingleArgTextFunc() {
			// no fields to initialise
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			String arg;
			try {
				arg = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return evaluate(arg);
		}
		protected abstract ValueEval evaluate(String arg);
	}

    /**
     * Returns the character specified by a number.
     */
    public static final Function CHAR = new Fixed1ArgFunction() {
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
            int arg;
            try {
                arg = evaluateIntArg(arg0, srcRowIndex, srcColumnIndex);
                if (arg < 0 || arg >= 256) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }

            } catch (EvaluationException e) {
                return e.getErrorEval();
            }
            return new StringEval(String.valueOf((char)arg));
        }
    };

	public static final Function LEN = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new NumberEval(arg.length());
		}
	};
	public static final Function LOWER = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new StringEval(arg.toLowerCase(Locale.ROOT));
		}
	};
	public static final Function UPPER = new SingleArgTextFunc() {
		protected ValueEval evaluate(String arg) {
			return new StringEval(arg.toUpperCase(Locale.ROOT));
		}
	};

	/**
	 * Implementation of the PROPER function:
     * Normalizes all words (separated by non-word characters) by
     * making the first letter upper and the rest lower case.
     * 
     * This is nearly equivalent to toTitleCase if the Java language had it
	 */
	public static final Function PROPER = new SingleArgTextFunc() {
		protected ValueEval evaluate(String text) {
			StringBuilder sb = new StringBuilder();
			boolean shouldMakeUppercase = true;
			for(final char ch : text.toCharArray()) {

				// Note: we are using String.toUpperCase() here on purpose as it handles certain things
				// better than Character.toUpperCase(), e.g. German "scharfes s" is translated
				// to "SS" (i.e. two characters), if uppercased properly!
				if (shouldMakeUppercase) {
					sb.append(String.valueOf(ch).toUpperCase(Locale.ROOT));
				}
				else {
					sb.append(String.valueOf(ch).toLowerCase(Locale.ROOT));
				}
				shouldMakeUppercase = !Character.isLetter(ch);
			}
			return new StringEval(sb.toString());
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
	 * An implementation of the CLEAN function:
	 * In Excel, the Clean function removes all non-printable characters from a string.
     *
	 * Author: Aniket Banerjee(banerjee@google.com)
	 */
    public static final Function CLEAN = new SingleArgTextFunc() {
        protected ValueEval evaluate(String arg) {
            StringBuilder result = new StringBuilder();
            for (final char c : arg.toCharArray()) {
                if (isPrintable(c)) {
                    result.append(c);
                }
            }
            return new StringEval(result.toString());
        }

        /**
         * From Excel docs: The CLEAN function was designed to remove the first 32 nonprinting characters
         * in the 7-bit ASCII code (values 0 through 31) from text. In the Unicode character set,
         * there are additional nonprinting characters (values 127, 129, 141, 143, 144, and 157). By itself,
         * the CLEAN function does not remove these additional  nonprinting characters. To do this task,
         * use the SUBSTITUTE function to replace the higher value Unicode characters with the 7-bit ASCII
         * characters for which the TRIM and CLEAN functions were designed.
         *
         * @param c the character to test
         * @return  whether the character is printable
         */
        private boolean isPrintable(char c){
			return c >= 32;
        }
    };

    /**
	 * An implementation of the MID function<br>
	 * MID returns a specific number of
	 * characters from a text string, starting at the specified position.<p>
	 *
	 * <b>Syntax<b>:<br> <b>MID</b>(<b>text</b>, <b>start_num</b>,
	 * <b>num_chars</b>)<br>
	 *
	 * Author: Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
	 */
	public static final Function MID = new Fixed3ArgFunction() {

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
				ValueEval arg1, ValueEval arg2) {
			String text;
			int startCharNum;
			int numChars;
			try {
				text = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
				startCharNum = evaluateIntArg(arg1, srcRowIndex, srcColumnIndex);
				numChars = evaluateIntArg(arg2, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
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
			if (startIx > len) {
				return new StringEval("");
			}
			int endIx = Math.min(startIx + numChars, len);
			String result = text.substring(startIx, endIx);
			return new StringEval(result);
		}
	};

	private static final class LeftRight extends Var1or2ArgFunction {
		private static final ValueEval DEFAULT_ARG1 = new NumberEval(1.0);
		private final boolean _isLeft;
		protected LeftRight(boolean isLeft) {
			_isLeft = isLeft;
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			return evaluate(srcRowIndex, srcColumnIndex, arg0, DEFAULT_ARG1);
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
				ValueEval arg1) {
			String arg;
			int index;
			try {
				arg = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
				index = evaluateIntArg(arg1, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			
			if(index < 0) {
				return ErrorEval.VALUE_INVALID;
			}
			
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

	public static final Function CONCATENATE = new Function() {

		public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
			StringBuilder sb = new StringBuilder();
			for (ValueEval arg : args) {
				try {
					sb.append(evaluateStringArg(arg, srcRowIndex, srcColumnIndex));
				} catch (EvaluationException e) {
					return e.getErrorEval();
				}
			}
			return new StringEval(sb.toString());
		}
	};

	public static final Function EXACT = new Fixed2ArgFunction() {

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0,
				ValueEval arg1) {
			String s0;
			String s1;
			try {
				s0 = evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
				s1 = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return BoolEval.valueOf(s0.equals(s1));
		}
	};

	/**
	 * An implementation of the TEXT function<br>
	 * TEXT returns a number value formatted with the given number formatting string. 
	 * This function is not a complete implementation of the Excel function, but
	 *  handles most of the common cases. All work is passed down to 
	 *  {@link DataFormatter} to be done, as this works much the same as the
	 *  display focused work that that does. 
	 *
	 * <b>Syntax<b>:<br> <b>TEXT</b>(<b>value</b>, <b>format_text</b>)<br>
	 */
	public static final Function TEXT = new Fixed2ArgFunction() {

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			double s0;
			String s1;
			try {
				s0 = evaluateDoubleArg(arg0, srcRowIndex, srcColumnIndex);
				s1 = evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			
			try {
            // Ask DataFormatter to handle the String for us
			   String formattedStr = formatter.formatRawCellContents(s0, -1, s1);
				return new StringEval(formattedStr);
			} catch (Exception e) {
				return ErrorEval.VALUE_INVALID;
			}
		}
	};
	
	private static final class SearchFind extends Var2or3ArgFunction {

		private final boolean _isCaseSensitive;

		public SearchFind(boolean isCaseSensitive) {
			_isCaseSensitive = isCaseSensitive;
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
			try {
				String needle = TextFunction.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
				String haystack = TextFunction.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
				return eval(haystack, needle, 0);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}
		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
				ValueEval arg2) {
			try {
				String needle = TextFunction.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
				String haystack = TextFunction.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
				// evaluate third arg and convert from 1-based to 0-based index
				int startpos = TextFunction.evaluateIntArg(arg2, srcRowIndex, srcColumnIndex) - 1;
				if (startpos < 0) {
					return ErrorEval.VALUE_INVALID;
				}
				return eval(haystack, needle, startpos);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
		}
		private ValueEval eval(String haystack, String needle, int startIndex) {
			int result;
			if (_isCaseSensitive) {
				result = haystack.indexOf(needle, startIndex);
			} else {
				result = haystack.toUpperCase(Locale.ROOT)
                       .indexOf(needle.toUpperCase(Locale.ROOT), startIndex);
			}
			if (result == -1) {
				return ErrorEval.VALUE_INVALID;
			}
			return new NumberEval(result + 1);
		}
	}
	/**
	 * Implementation of the FIND() function.<p>
	 *
	 * <b>Syntax</b>:<br>
	 * <b>FIND</b>(<b>find_text</b>, <b>within_text</b>, start_num)<p>
	 *
	 * FIND returns the character position of the first (case sensitive) occurrence of
	 * <tt>find_text</tt> inside <tt>within_text</tt>.  The third parameter,
	 * <tt>start_num</tt>, is optional (default=1) and specifies where to start searching
	 * from.  Character positions are 1-based.<p>
	 *
	 * Author: Torstein Tauno Svendsen (torstei@officenet.no)
	 */
	public static final Function FIND = new SearchFind(true);
	/**
	 * Implementation of the FIND() function.<p>
	 *
	 * <b>Syntax</b>:<br>
	 * <b>SEARCH</b>(<b>find_text</b>, <b>within_text</b>, start_num)<p>
	 *
	 * SEARCH is a case-insensitive version of FIND()
	 */
	public static final Function SEARCH = new SearchFind(false);
}
