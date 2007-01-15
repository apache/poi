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
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author
 *  
 */
public class Or extends BooleanFunction {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        ValueEval retval = null;
        boolean b = false;
        boolean atleastOneNonBlank = false;
        
        /*
         * Note: do not abort the loop if b is true, since we could be
         * dealing with errorevals later. 
         */
        outer:
        for (int i=0, iSize=operands.length; i<iSize; i++) {
            if (operands[i] instanceof AreaEval) {
                AreaEval ae = (AreaEval) operands[i];
                ValueEval[] values = ae.getValues();
                for (int j=0, jSize=values.length; j<jSize; j++) {
                    ValueEval tempVe = singleOperandEvaluate(values[j], srcRow, srcCol, true);
                    if (tempVe instanceof BoolEval) {
                        b = b || ((BoolEval) tempVe).getBooleanValue();
                        atleastOneNonBlank = true;
                    }
                    else if (tempVe instanceof ErrorEval) {
                        retval = tempVe;
                        break outer;
                    }
                }
            }
            else {
                ValueEval tempVe = singleOperandEvaluate(operands[i], srcRow, srcCol, false);
                if (tempVe instanceof BoolEval) {
                    b = b || ((BoolEval) tempVe).getBooleanValue();
                    atleastOneNonBlank = true;
                }
                else if (tempVe instanceof ErrorEval) {
                    retval = tempVe;
                    break outer;
                }
            }
        }
        
        if (!atleastOneNonBlank) {
            retval = ErrorEval.VALUE_INVALID;
        }
        
        if (retval == null) { // if no error
            retval = b ? BoolEval.TRUE : BoolEval.FALSE;
        }
        
        return retval;
    }

}
