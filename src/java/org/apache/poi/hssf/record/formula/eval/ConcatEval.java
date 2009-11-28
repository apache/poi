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

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public final class ConcatEval  extends Fixed2ArgFunction {

	public static final Function instance = new ConcatEval();

	private ConcatEval() {
		// enforce singleton
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		ValueEval ve0;
		ValueEval ve1;
		try {
			ve0 = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			ve1 = OperandResolver.getSingleValue(arg1, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(getText(ve0));
		sb.append(getText(ve1));
		return new StringEval(sb.toString());
	}

	private Object getText(ValueEval ve) {
		if (ve instanceof StringValueEval) {
			StringValueEval sve = (StringValueEval) ve;
			return sve.getStringValue();
		}
		if (ve == BlankEval.instance) {
			return "";
		}
		throw new IllegalAccessError("Unexpected value type ("
					+ ve.getClass().getName() + ")");
	}
}
