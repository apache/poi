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
 * Created on May 14, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class NumericOperationEval implements OperationEval {

    protected abstract ValueEvalToNumericXlator getXlator();
    
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
                    ve = getXlator().attemptXlateToNumeric(ve);
                    retval = getXlator().attemptXlateToNumeric(ve);
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
            else if (ae.isColumn()) {
                if (ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    retval = getXlator().attemptXlateToNumeric(ve);
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
            retval = getXlator().attemptXlateToNumeric((ValueEval) eval);
        }
        return retval;
    }
}
