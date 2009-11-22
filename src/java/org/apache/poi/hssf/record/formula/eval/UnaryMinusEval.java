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

import org.apache.poi.hssf.record.formula.functions.Fixed1ArgFunction;
import org.apache.poi.hssf.record.formula.functions.Function;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public final class UnaryMinusEval extends Fixed1ArgFunction {

	public static final Function instance = new UnaryMinusEval();

	private UnaryMinusEval() {
		// enforce singleton
	}

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
		double d;
		try {
			ValueEval ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
			d = OperandResolver.coerceValueToDouble(ve);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
		if (d == 0.0) { // this '==' matches +0.0 and -0.0
			return NumberEval.ZERO;
		}
		return new NumberEval(-d);
	}
}
