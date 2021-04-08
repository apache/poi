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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * An implementation of the Excel REPLACE() function:<p>
 * Replaces part of a text string based on the number of characters
 * you specify, with another text string.<br>
 *
 * <b>Syntax</b>:<br>
 * <b>REPLACE</b>(<b>oldText</b>, <b>startNum</b>, <b>numChars</b>, <b>newText</b>)<p>
 *
 * <b>oldText</b>  The text string containing characters to replace<br>
 * <b>startNum</b> The position of the first character to replace (1-based)<br>
 * <b>numChars</b> The number of characters to replace<br>
 * <b>newText</b> The new text value to replace the removed section<br>
 *
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public final class Replace extends Fixed4ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2, ValueEval arg3) {

		String oldStr;
		int startNum;
		int numChars;
		String newStr;
		try {
			oldStr = TextFunction.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
			startNum = TextFunction.evaluateIntArg(arg1, srcRowIndex, srcColumnIndex);
			numChars = TextFunction.evaluateIntArg(arg2, srcRowIndex, srcColumnIndex);
			newStr = TextFunction.evaluateStringArg(arg3, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		if (startNum < 1 || numChars < 0) {
			return ErrorEval.VALUE_INVALID;
		}
		StringBuilder strBuff = new StringBuilder(oldStr);
		// remove any characters that should be replaced
		if (startNum <= oldStr.length() && numChars != 0) {
			strBuff.delete(startNum - 1, startNum - 1 + numChars);
		}
		// now insert (or append) newStr
		if (startNum > strBuff.length()) {
			strBuff.append(newStr);
		} else {
			strBuff.insert(startNum - 1, newStr);
		}
		return new StringEval(strBuff.toString());
	}
}
