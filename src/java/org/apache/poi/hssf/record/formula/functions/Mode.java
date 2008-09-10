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
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Mode extends MultiOperandNumericFunction {
	private static final ValueEvalToNumericXlator DEFAULT_NUM_XLATOR =
		new ValueEvalToNumericXlator(0);

	/**
	 * this is the default impl for the factory method getXlator
	 * of the super class NumericFunction. Subclasses can override this method
	 * if they desire to return a different ValueEvalToNumericXlator instance
	 * than the default.
	 */
	protected ValueEval attemptXlateToNumeric(ValueEval ve) {
		return DEFAULT_NUM_XLATOR.attemptXlateToNumeric(ve);
	}

	protected double evaluate(double[] values) throws EvaluationException {
		double d = StatsLib.mode(values);
		if (Double.isNaN(d)) {
			// TODO - StatsLib is returning NaN to denote 'no duplicate values'
			throw new EvaluationException(ErrorEval.NA);
		}
		return d;
	}
}
