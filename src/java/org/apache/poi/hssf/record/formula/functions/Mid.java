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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * An implementation of the MID function<br/> MID returns a specific number of
 * characters from a text string, starting at the specified position.<p/>
 * 
 * <b>Syntax<b>:<br/> <b>MID</b>(<b>text</b>, <b>start_num</b>,
 * <b>num_chars</b>)<br/>
 * 
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public class Mid implements Function {
	/**
	 * Returns a specific number of characters from a text string, starting at
	 * the position you specify, based on the number of characters you specify.
	 * 
	 * @see org.apache.poi.hssf.record.formula.eval.Eval
	 */
	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		if (args.length != 3) {
			return ErrorEval.VALUE_INVALID;
		}

		String text;
		int startIx; // zero based
		int numChars;

		try {
			ValueEval evText = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			text = OperandResolver.coerceValueToString(evText);
			int startCharNum = evaluateNumberArg(args[1], srcCellRow, srcCellCol);
			numChars = evaluateNumberArg(args[2], srcCellRow, srcCellCol);
			startIx = startCharNum - 1; // convert to zero-based
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		int len = text.length();
		if (startIx < 0) {
			return ErrorEval.VALUE_INVALID;
		}
		if (numChars < 0) {
			return ErrorEval.VALUE_INVALID;
		}
		if (numChars < 0 || startIx > len) {
			return new StringEval("");
		}
		int endIx = startIx + numChars;
		if (endIx > len) {
			endIx = len;
		}
		String result = text.substring(startIx, endIx);
		return new StringEval(result);

	}

	private static int evaluateNumberArg(Eval arg, int srcCellRow, short srcCellCol) throws EvaluationException {
		ValueEval ev = OperandResolver.getSingleValue(arg, srcCellRow, srcCellCol);
		// Note - for start_num arg, blank/zero causes error(#VALUE!),
		// but for num_chars causes empty string to be returned.
		return OperandResolver.coerceValueToInt(ev);
	}
}