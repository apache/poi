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
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.UnaryPlusPtg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class UnaryPlusEval implements OperationEval /*extends NumericOperationEval*/ {

    private UnaryPlusPtg delegate;
    
    /* 
     * COMMENT FOR COMMENTED CODE IN THIS FILE
     * 
     * In excel the programmer seems to not have cared to
     * think about how strings were handled in other numeric
     * operations when he/she was implementing this operation :P
     * 
     * Here's what I mean:
     * 
     * Q. If the formula -"hello" evaluates to #VALUE! in excel, what should
     * the formula +"hello" evaluate to?
     * 
     * A. +"hello" evaluates to "hello" (what the...?)
     * 
     */

    
//    private static final ValueEvalToNumericXlator NUM_XLATOR = 
//        new ValueEvalToNumericXlator((short)
//                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
//                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
//                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
//                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
//                | ValueEvalToNumericXlator.STRING_IS_PARSED
//                ));


    public UnaryPlusEval(Ptg ptg) {
        this.delegate = (UnaryPlusPtg) ptg;
    }
    
//    protected ValueEvalToNumericXlator getXlator() {
//        return NUM_XLATOR;
//    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.UNKNOWN_ERROR;
            break;
        case 1:

//            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
//            if (ve instanceof NumericValueEval) {
//                d = ((NumericValueEval) ve).getNumberValue();
//            }
//            else if (ve instanceof BlankEval) {
//                // do nothing
//            }
//            else if (ve instanceof ErrorEval) {
//                retval = ve;
//            }
            if (operands[0] instanceof RefEval) {
                RefEval re = (RefEval) operands[0];
                retval = re.getInnerValueEval();
            }
            else if (operands[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[0];
                if (ae.contains(srcRow, srcCol)) { // circular ref!
                    retval = ErrorEval.CIRCULAR_REF_ERROR;
                }
                else if (ae.isRow()) {
                    if (ae.containsColumn(srcCol)) {
                        ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                        if (ve instanceof RefEval) {
                            ve = ((RefEval) ve).getInnerValueEval();
                        }
                        retval = ve;
                    }
                    else {
                        retval = ErrorEval.VALUE_INVALID;
                    }
                }
                else if (ae.isColumn()) {
                    if (ae.containsRow(srcRow)) {
                        ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                        if (ve instanceof RefEval) {
                            ve = ((RefEval) ve).getInnerValueEval();
                        }
                        retval = ve;
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
                retval = (ValueEval) operands[0];
            }
        }
        
        if (retval instanceof BlankEval) {
            retval = new NumberEval(0);
        }

        return retval;
    }

    public int getNumberOfOperands() {
        return delegate.getNumberOfOperands();
    }

    public int getType() {
        return delegate.getType();
    }

}
