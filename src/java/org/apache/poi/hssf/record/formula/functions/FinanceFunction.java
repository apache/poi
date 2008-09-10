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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEvalToNumericXlator;


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * Super class for all Evals for financial function evaluation.
 * 
 */
public abstract class FinanceFunction implements Function {
    private static final ValueEvalToNumericXlator DEFAULT_NUM_XLATOR =
        new ValueEvalToNumericXlator((short) (0
                | ValueEvalToNumericXlator.BOOL_IS_PARSED  
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED  
                | ValueEvalToNumericXlator.STRING_IS_PARSED  
                | ValueEvalToNumericXlator.REF_STRING_IS_PARSED  
                | ValueEvalToNumericXlator.BLANK_IS_PARSED  
                | ValueEvalToNumericXlator.REF_BLANK_IS_PARSED  
              //| ValueEvalToNumericXlator.REF_STRING_TO_BOOL_IS_PARSED  
              //| ValueEvalToNumericXlator.STRING_IS_INVALID_VALUE  
              //| ValueEvalToNumericXlator.REF_STRING_IS_INVALID_VALUE
                ));
    
    /**
     * this is the default impl of the factory(ish) method getXlator.
     * Subclasses can override this method
     * if they desire to return a different ValueEvalToNumericXlator instance
     * than the default.
     */
    protected final ValueEvalToNumericXlator getXlator() {
        return DEFAULT_NUM_XLATOR;
    }
    
    protected final ValueEval singleOperandNumericAsBoolean(Eval eval, int srcRow, short srcCol) {
        ValueEval retval = null;
        retval = singleOperandEvaluate(eval, srcRow, srcCol);
        if (retval instanceof NumericValueEval) {
            NumericValueEval nve = (NumericValueEval) retval;
            retval = (nve.getNumberValue() == 0)
                    ? BoolEval.FALSE
                    : BoolEval.TRUE;
        }
        else {
            retval = ErrorEval.VALUE_INVALID;
        }
        return retval;
    }
    
    protected final ValueEval singleOperandEvaluate(Eval eval, int srcRow, short srcCol) {

        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            if (ae.contains(srcRow, srcCol)) { // circular ref!
                return ErrorEval.CIRCULAR_REF_ERROR;
            }
            if (ae.isRow()) {
                if (ae.isColumn()) {
                	return ae.getRelativeValue(0, 0);
                }
                if (ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    ve = getXlator().attemptXlateToNumeric(ve);
                    return getXlator().attemptXlateToNumeric(ve);
                }
                return ErrorEval.VALUE_INVALID;
            }
            if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    return getXlator().attemptXlateToNumeric(ve);
                }
                return ErrorEval.VALUE_INVALID;
            }
            return ErrorEval.VALUE_INVALID;
        }
        return getXlator().attemptXlateToNumeric((ValueEval) eval);
    }
    
}
