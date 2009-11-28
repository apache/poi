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

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.MissingArgEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.OperationEvaluationContext;

/**
 * Implementation for Excel function INDIRECT<p/>
 *
 * INDIRECT() returns the cell or area reference denoted by the text argument.<p/>
 *
 * <b>Syntax</b>:</br>
 * <b>INDIRECT</b>(<b>ref_text</b>,isA1Style)<p/>
 *
 * <b>ref_text</b> a string representation of the desired reference as it would
 * normally be written in a cell formula.<br/>
 * <b>isA1Style</b> (default TRUE) specifies whether the ref_text should be
 * interpreted as A1-style or R1C1-style.
 *
 * @author Josh Micich
 */
public final class Indirect implements FreeRefFunction {

	public static final FreeRefFunction instance = new Indirect();

	private Indirect() {
		// enforce singleton
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		if (args.length < 1) {
			return ErrorEval.VALUE_INVALID;
		}

		boolean isA1style;
		String text;
		try {
			ValueEval ve = OperandResolver.getSingleValue(args[0], ec.getRowIndex(), ec
					.getColumnIndex());
			text = OperandResolver.coerceValueToString(ve);
			switch (args.length) {
				case 1:
					isA1style = true;
					break;
				case 2:
					isA1style = evaluateBooleanArg(args[1], ec);
					break;
				default:
					return ErrorEval.VALUE_INVALID;
			}
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		return evaluateIndirect(ec, text, isA1style);
	}

	private static boolean evaluateBooleanArg(ValueEval arg, OperationEvaluationContext ec)
			throws EvaluationException {
		ValueEval ve = OperandResolver.getSingleValue(arg, ec.getRowIndex(), ec.getColumnIndex());

		if (ve == BlankEval.instance || ve == MissingArgEval.instance) {
			return false;
		}
		// numeric quantities follow standard boolean conversion rules
		// for strings, only "TRUE" and "FALSE" (case insensitive) are valid
		return OperandResolver.coerceValueToBoolean(ve, false).booleanValue();
	}

	private static ValueEval evaluateIndirect(OperationEvaluationContext ec, String text,
			boolean isA1style) {
		// Search backwards for '!' because sheet names can contain '!'
		int plingPos = text.lastIndexOf('!');

		String workbookName;
		String sheetName;
		String refText; // whitespace around this gets trimmed OK
		if (plingPos < 0) {
			workbookName = null;
			sheetName = null;
			refText = text;
		} else {
			String[] parts = parseWorkbookAndSheetName(text.subSequence(0, plingPos));
			if (parts == null) {
				return ErrorEval.REF_INVALID;
			}
			workbookName = parts[0];
			sheetName = parts[1];
			refText = text.substring(plingPos + 1);
		}

		String refStrPart1;
		String refStrPart2;

		int colonPos = refText.indexOf(':');
		if (colonPos < 0) {
			refStrPart1 = refText.trim();
			refStrPart2 = null;
		} else {
			refStrPart1 = refText.substring(0, colonPos).trim();
			refStrPart2 = refText.substring(colonPos + 1).trim();
		}
		return ec.getDynamicReference(workbookName, sheetName, refStrPart1, refStrPart2, isA1style);
	}

	/**
	 * @return array of length 2: {workbookName, sheetName,}.  Second element will always be
	 * present.  First element may be null if sheetName is unqualified.
	 * Returns <code>null</code> if text cannot be parsed.
	 */
	private static String[] parseWorkbookAndSheetName(CharSequence text) {
		int lastIx = text.length() - 1;
		if (lastIx < 0) {
			return null;
		}
		if (canTrim(text)) {
			return null;
		}
		char firstChar = text.charAt(0);
		if (Character.isWhitespace(firstChar)) {
			return null;
		}
		if (firstChar == '\'') {
			// workbookName or sheetName needs quoting
			// quotes go around both
			if (text.charAt(lastIx) != '\'') {
				return null;
			}
			firstChar = text.charAt(1);
			if (Character.isWhitespace(firstChar)) {
				return null;
			}
			String wbName;
			int sheetStartPos;
			if (firstChar == '[') {
				int rbPos = text.toString().lastIndexOf(']');
				if (rbPos < 0) {
					return null;
				}
				wbName = unescapeString(text.subSequence(2, rbPos));
				if (wbName == null || canTrim(wbName)) {
					return null;
				}
				sheetStartPos = rbPos + 1;
			} else {
				wbName = null;
				sheetStartPos = 1;
			}

			// else - just sheet name
			String sheetName = unescapeString(text.subSequence(sheetStartPos, lastIx));
			if (sheetName == null) { // note - when quoted, sheetName can
									 // start/end with whitespace
				return null;
			}
			return new String[] { wbName, sheetName, };
		}

		if (firstChar == '[') {
			int rbPos = text.toString().lastIndexOf(']');
			if (rbPos < 0) {
				return null;
			}
			CharSequence wbName = text.subSequence(1, rbPos);
			if (canTrim(wbName)) {
				return null;
			}
			CharSequence sheetName = text.subSequence(rbPos + 1, text.length());
			if (canTrim(sheetName)) {
				return null;
			}
			return new String[] { wbName.toString(), sheetName.toString(), };
		}
		// else - just sheet name
		return new String[] { null, text.toString(), };
	}

	/**
	 * @return <code>null</code> if there is a syntax error in any escape sequence
	 * (the typical syntax error is a single quote character not followed by another).
	 */
	private static String unescapeString(CharSequence text) {
		int len = text.length();
		StringBuilder sb = new StringBuilder(len);
		int i = 0;
		while (i < len) {
			char ch = text.charAt(i);
			if (ch == '\'') {
				// every quote must be followed by another
				i++;
				if (i >= len) {
					return null;
				}
				ch = text.charAt(i);
				if (ch != '\'') {
					return null;
				}
			}
			sb.append(ch);
			i++;
		}
		return sb.toString();
	}

	private static boolean canTrim(CharSequence text) {
		int lastIx = text.length() - 1;
		if (lastIx < 0) {
			return false;
		}
		if (Character.isWhitespace(text.charAt(0))) {
			return true;
		}
		if (Character.isWhitespace(text.charAt(lastIx))) {
			return true;
		}
		return false;
	}
}
