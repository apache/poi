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

import org.apache.poi.hssf.record.formula.AddPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is a documentation of the observed behaviour of 
 * the '+' operator in Excel:
 * <ol>
 * <li> 1+TRUE = 2
 * <li> 1+FALSE = 1
 * <li> 1+"true" = #VALUE!
 * <li> 1+"1" = 2
 * <li> 1+A1 = #VALUE if A1 contains "1"
 * <li> 1+A1 = 2 if A1 contains ="1"
 * <li> 1+A1 = 2 if A1 contains TRUE or =TRUE
 * <li> 1+A1 = #VALUE! if A1 contains "TRUE" or ="TRUE"
 */
public class AddEval extends NumericOperationEval {

    private AddPtg delegate;
    private static final ValueEvalToNumericXlator NUM_XLATOR = 
        new ValueEvalToNumericXlator((short)
                ( ValueEvalToNumericXlator.BOOL_IS_PARSED 
                | ValueEvalToNumericXlator.EVALUATED_REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.EVALUATED_REF_STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_BOOL_IS_PARSED
                | ValueEvalToNumericXlator.STRING_IS_PARSED
                | ValueEvalToNumericXlator.REF_STRING_IS_PARSED
                ));

    public AddEval(Ptg ptg) {
        delegate = (AddPtg) ptg;
    }

    public ValueEvalToNumericXlator getXlator() {
        return NUM_XLATOR;
    }
    
    
    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        Eval retval = null;
        double d = 0;
        switch (operands.length) {
        default: // will rarely happen. currently the parser itself fails.
            retval = ErrorEval.UNKNOWN_ERROR;
            break;
        case 2:
            for (int i = 0, iSize = 2; retval==null && i < iSize; i++) {
                ValueEval ve = singleOperandEvaluate(operands[i], srcRow, srcCol);
                if (ve instanceof NumericValueEval) {
                    d += ((NumericValueEval) ve).getNumberValue();
                }
                else if (ve instanceof BlankEval) {
                    // do nothing
                }
                else {
                    retval = ErrorEval.VALUE_INVALID;
                }
            } // end for inside case
        } // end switch
        
        if (retval == null) {
            retval = Double.isNaN(d) ? (ValueEval) ErrorEval.VALUE_INVALID : new NumberEval(d);
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
