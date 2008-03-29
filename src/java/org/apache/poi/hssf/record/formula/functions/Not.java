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
 * Created on May 9, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;


/**
 * @author Amol S. Deshmukh &lt; amol at apache dot org &gt;
 * The NOT boolean function. Returns negation of specified value
 * (treated as a boolean). If the specified arg is a number,
 * then it is true <=> 'number is non-zero'
 */
public class Not extends BooleanFunction {
    
  
    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        boolean b = true;
        ValueEval tempVe = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            if (operands[0] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[0];
                if (ae.isRow() && ae.containsColumn(srcCol)) {
                    ValueEval ve = ae.getValueAt(ae.getFirstRow(), srcCol);
                    tempVe = singleOperandEvaluate(ve);
                } else if (ae.isColumn() && ae.containsRow(srcRow)) {
                    ValueEval ve = ae.getValueAt(srcRow, ae.getFirstColumn());
                    tempVe = singleOperandEvaluate(ve);
                } else {
                    retval = ErrorEval.VALUE_INVALID;
                }            
            }
            else {
                tempVe = singleOperandEvaluate(operands[0]);
                if (tempVe instanceof StringEval) {
                    retval = ErrorEval.VALUE_INVALID;
                }
                else if (tempVe instanceof ErrorEval) {
                    retval = tempVe;
                }
            }
        }
        
        if (retval == null) { // if no error
            if (tempVe instanceof BoolEval) {
                b = b && ((BoolEval) tempVe).getBooleanValue();
            }
            else if (tempVe instanceof StringEval) {
                retval = ErrorEval.VALUE_INVALID;
            }
            else if (tempVe instanceof ErrorEval) {
                retval = tempVe;
            }
            retval = b ? BoolEval.FALSE : BoolEval.TRUE;
        }
        
        return retval;
    }
    
    
    protected ValueEval singleOperandEvaluate(Eval ve) {
        ValueEval retval = ErrorEval.VALUE_INVALID;
        if (ve instanceof RefEval) {
            RefEval re = (RefEval) ve;
            retval = singleOperandEvaluate(re.getInnerValueEval());
        }
        else if (ve instanceof BoolEval) {
            retval = (BoolEval) ve;
        }
        else if (ve instanceof NumberEval) {
            NumberEval ne = (NumberEval) ve;
            retval = ne.getNumberValue() != 0 ? BoolEval.TRUE : BoolEval.FALSE;
        }
        else if (ve instanceof StringEval) {
            StringEval se = (StringEval) ve;
            String str = se.getStringValue();
            retval = str.equalsIgnoreCase("true")
                    ? BoolEval.TRUE
                    : str.equalsIgnoreCase("false")
                            ? BoolEval.FALSE
                            : (ValueEval) ErrorEval.VALUE_INVALID;
        }
        else if (ve instanceof BlankEval) {
            retval = BoolEval.FALSE;
        }
        else {
            retval = ErrorEval.VALUE_INVALID;
        }
        return retval;
    }
}
