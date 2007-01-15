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
/*
 * Created on May 22, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class TextFunction implements Function {
    
    protected static final String EMPTY_STRING = "";
    
    protected ValueEval singleOperandEvaluate(Eval eval, int srcRow, short srcCol) {
        ValueEval retval;
        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            if (ae.contains(srcRow, srcCol)) { // circular ref!
                retval = ErrorEval.CIRCULAR_REF_ERROR;
            }
            else if (ae.isRow()) {
                if (ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    retval = attemptXlateToText(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    retval = attemptXlateToText(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else {
                retval = ErrorEval.VALUE_INVALID;
            }
        }
        else {
            retval = attemptXlateToText((ValueEval) eval);
        }
        return retval;
    }

    
    /**
     * converts from Different ValueEval types to StringEval.
     * Note: AreaEvals are not handled, if arg is an AreaEval,
     * the returned value is ErrorEval.VALUE_INVALID
     * @param ve
     * @return
     */
    protected ValueEval attemptXlateToText(ValueEval ve) {
        ValueEval retval;
        if (ve instanceof StringValueEval) {
            retval = ve;
        }
        else if (ve instanceof RefEval) {
            RefEval re = (RefEval) ve;
            ValueEval ive = re.getInnerValueEval();
            if (ive instanceof StringValueEval) {
                retval = ive;
            }
            else if (ive instanceof BlankEval) {
                retval = ive;
            }
            else {
                retval = ErrorEval.VALUE_INVALID;
            }
        }
        else if (ve instanceof BlankEval) {
            retval = ve;
        }
        else {
            retval = ErrorEval.VALUE_INVALID;
        }
        return retval;
    }
}
