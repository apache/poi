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
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Implementation of Excel HYPERLINK function.<p/>
 *
 * In Excel this function has special behaviour - it causes the displayed cell value to behave like
 * a hyperlink in the GUI. From an evaluation perspective however, it is very simple.<p/>
 *
 * <b>Syntax</b>:<br/>
 * <b>HYPERLINK</b>(<b>link_location</b>, friendly_name)<p/>
 *
 * <b>link_location</b> The URL of the hyperlink <br/>
 * <b>friendly_name</b> (optional) the value to display<p/>
 *
 *  Returns last argument.  Leaves type unchanged (does not convert to {@link StringEval}).

 * @author Wayne Clingingsmith
 */
public final class Hyperlink implements Function {

	public ValueEval evaluate(ValueEval[] operands, int srcRow, short srcCol) {
		int lastArgIx = operands.length - 1;
		if (lastArgIx < 0 || lastArgIx > 1) {
			return ErrorEval.VALUE_INVALID;
		}

		try {
			return OperandResolver.getSingleValue(operands[lastArgIx], srcRow, srcCol);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}
}
