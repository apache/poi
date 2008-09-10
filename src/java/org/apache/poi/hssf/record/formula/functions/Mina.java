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

import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt; 
 *
 */
public final class Mina extends MultiOperandNumericFunction {
    private static final ValueEvalToNumericXlator DEFAULT_NUM_XLATOR =
        new ValueEvalToNumericXlator((short) (
                  ValueEvalToNumericXlator.REF_BOOL_IS_PARSED  
                 | ValueEvalToNumericXlator.BLANK_IS_PARSED
                ));
    
	protected ValueEval attemptXlateToNumeric(ValueEval ve) {
		return DEFAULT_NUM_XLATOR.attemptXlateToNumeric(ve);
	}

    public double evaluate(double[] values) {
        return values.length > 0 ? MathX.min(values) : 0;
    }
}
