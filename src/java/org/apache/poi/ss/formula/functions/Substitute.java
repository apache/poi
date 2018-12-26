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
 * An implementation of the SUBSTITUTE function:<p>
 * Substitutes text in a text string with new text, some number of times.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public final class Substitute extends Var3or4ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2) {
		String result;
		try {
			String oldStr = TextFunction.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
			String searchStr = TextFunction.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
			String newStr = TextFunction.evaluateStringArg(arg2, srcRowIndex, srcColumnIndex);

			result = replaceAllOccurrences(oldStr, searchStr, newStr);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new StringEval(result);
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1,
			ValueEval arg2, ValueEval arg3) {
		String result;
		try {
			String oldStr = TextFunction.evaluateStringArg(arg0, srcRowIndex, srcColumnIndex);
			String searchStr = TextFunction.evaluateStringArg(arg1, srcRowIndex, srcColumnIndex);
			String newStr = TextFunction.evaluateStringArg(arg2, srcRowIndex, srcColumnIndex);

			int instanceNumber = TextFunction.evaluateIntArg(arg3, srcRowIndex, srcColumnIndex);
			if (instanceNumber < 1) {
				return ErrorEval.VALUE_INVALID;
			}
			result = replaceOneOccurrence(oldStr, searchStr, newStr, instanceNumber);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		return new StringEval(result);
	}

	private static String replaceAllOccurrences(String oldStr, String searchStr, String newStr) {
		StringBuffer sb = new StringBuffer();
		int startIndex = 0;
		int nextMatch = -1;
		while (true) {
			nextMatch = oldStr.indexOf(searchStr, startIndex);
			if (nextMatch < 0) {
				// store everything from end of last match to end of string
				sb.append(oldStr.substring(startIndex));
				return sb.toString();
			}
			// store everything from end of last match to start of this match
			sb.append(oldStr, startIndex, nextMatch);
			sb.append(newStr);
			startIndex = nextMatch + searchStr.length();
		}
	}

	private static String replaceOneOccurrence(String oldStr, String searchStr, String newStr, int instanceNumber) {
		if (searchStr.length() < 1) {
			return oldStr;
		}
		int startIndex = 0;
		int nextMatch = -1;
		int count=0;
		while (true) {
			nextMatch = oldStr.indexOf(searchStr, startIndex);
			if (nextMatch < 0) {
				// not enough occurrences found - leave unchanged
				return oldStr;
			}
			count++;
			if (count == instanceNumber) {
				StringBuffer sb = new StringBuffer(oldStr.length() + newStr.length());
				sb.append(oldStr, 0, nextMatch);
				sb.append(newStr);
				sb.append(oldStr.substring(nextMatch + searchStr.length()));
				return sb.toString();
			}
			startIndex = nextMatch + searchStr.length();
		}
	}
}
