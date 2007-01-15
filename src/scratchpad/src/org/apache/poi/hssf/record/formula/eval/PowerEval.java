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
import org.apache.poi.hssf.record.formula.PowerPtg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class PowerEval extends NumericOperationEval {

    private PowerPtg delegate;

    private static final ValueEvalToNumericXlator NUM_XLATOR = 
        new ValueEvalToNumericXlator((short)
                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_STRING_IS_PARSED
                ));

    public PowerEval(Ptg ptg) {
        delegate = (PowerPtg) ptg;
    }
    
    protected ValueEvalToNumericXlator getXlator() {
        return NUM_XLATOR;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        Eval retval = null;
        double d0 = 0;
        double d1 = 0;
        
        switch (operands.length) {
        default: // will rarely happen. currently the parser itself fails.
            retval = ErrorEval.UNKNOWN_ERROR;
            break;
        case 2:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                d0 = ((NumericValueEval) ve).getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.VALUE_INVALID;
            }
            
            if (retval == null) { // no error yet
                ve = singleOperandEvaluate(operands[1], srcRow, srcCol);
                if (ve instanceof NumericValueEval) {
                    d1 = ((NumericValueEval) ve).getNumberValue();
                }
                else if (ve instanceof BlankEval) {
                    // do nothing
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            }
        } // end switch

        if (retval == null) {
            double p = Math.pow(d0, d1);
            retval = (Double.isNaN(p)) 
                    ? (ValueEval) ErrorEval.VALUE_INVALID 
                    : new NumberEval(p);
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
