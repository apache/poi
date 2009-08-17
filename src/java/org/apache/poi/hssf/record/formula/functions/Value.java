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
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation for Excel VALUE() function.<p/>
 *
 * <b>Syntax</b>:<br/> <b>VALUE</b>(<b>text</b>)<br/>
 *
 * Converts the text argument to a number. Leading and/or trailing whitespace is
 * ignored. Currency symbols and thousands separators are stripped out.
 * Scientific notation is also supported. If the supplied text does not convert
 * properly the result is <b>#VALUE!</b> error. Blank string converts to zero.
 *
 * @author Josh Micich
 */
public final class Value implements Function {

	/** "1,0000" is valid, "1,00" is not */
	private static final int MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR = 4;
	private static final Double ZERO = new Double(0.0);

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {
		if (args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}
		ValueEval veText;
		try {
			veText = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		String strText = OperandResolver.coerceValueToString(veText);
		Double result = convertTextToNumber(strText);
		if (result == null) {
			return ErrorEval.VALUE_INVALID;
		}
		return new NumberEval(result.doubleValue());
	}

	/**
	 * TODO see if the same functionality is needed in {@link OperandResolver#parseDouble(String)}
	 *
	 * @return <code>null</code> if there is any problem converting the text
	 */
	private static Double convertTextToNumber(String strText) {
		boolean foundCurrency = false;
		boolean foundUnaryPlus = false;
		boolean foundUnaryMinus = false;

		int len = strText.length();
		int i;
		for (i = 0; i < len; i++) {
			char ch = strText.charAt(i);
			if (Character.isDigit(ch) || ch == '.') {
				break;
			}
			switch (ch) {
				case ' ':
					// intervening spaces between '$', '-', '+' are OK
					continue;
				case '$':
					if (foundCurrency) {
						// only one currency symbols is allowed
						return null;
					}
					foundCurrency = true;
					continue;
				case '+':
					if (foundUnaryMinus || foundUnaryPlus) {
						return null;
					}
					foundUnaryPlus = true;
					continue;
				case '-':
					if (foundUnaryMinus || foundUnaryPlus) {
						return null;
					}
					foundUnaryMinus = true;
					continue;
				default:
					// all other characters are illegal
					return null;
			}
		}
		if (i >= len) {
			// didn't find digits or '.'
			if (foundCurrency || foundUnaryMinus || foundUnaryPlus) {
				return null;
			}
			return ZERO;
		}

		// remove thousands separators

		boolean foundDecimalPoint = false;
		int lastThousandsSeparatorIndex = Short.MIN_VALUE;

		StringBuffer sb = new StringBuffer(len);
		for (; i < len; i++) {
			char ch = strText.charAt(i);
			if (Character.isDigit(ch)) {
				sb.append(ch);
				continue;
			}
			switch (ch) {
				case ' ':
					String remainingText = strText.substring(i);
					if (remainingText.trim().length() > 0) {
						// intervening spaces not allowed once the digits start
						return null;
					}
					break;
				case '.':
					if (foundDecimalPoint) {
						return null;
					}
					if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
						return null;
					}
					foundDecimalPoint = true;
					sb.append('.');
					continue;
				case ',':
					if (foundDecimalPoint) {
						// thousands separators not allowed after '.' or 'E'
						return null;
					}
					int distanceBetweenThousandsSeparators = i - lastThousandsSeparatorIndex;
					// as long as there are 3 or more digits between
					if (distanceBetweenThousandsSeparators < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
						return null;
					}
					lastThousandsSeparatorIndex = i;
					// don't append ','
					continue;

				case 'E':
				case 'e':
					if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
						return null;
					}
					// append rest of strText and skip to end of loop
					sb.append(strText.substring(i));
					i = len;
					break;
				default:
					// all other characters are illegal
					return null;
			}
		}
		if (!foundDecimalPoint) {
			if (i - lastThousandsSeparatorIndex < MIN_DISTANCE_BETWEEN_THOUSANDS_SEPARATOR) {
				return null;
			}
		}
		double d;
		try {
			d = Double.parseDouble(sb.toString());
		} catch (NumberFormatException e) {
			// still a problem parsing the number - probably out of range
			return null;
		}
		return new Double(foundUnaryMinus ? -d : d);
	}
}
