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

import org.apache.poi.hssf.record.formula.ConcatPtg;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *  
 */
public class ConcatEval extends StringOperationEval {

    private ConcatPtg delegate;

    public ConcatEval(Ptg ptg) {
        this.delegate = (ConcatPtg) ptg;
    }

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        Eval retval = null;
        StringBuffer sb = null;
        
        switch (operands.length) {
        default: // paranoid check :)
            retval = ErrorEval.UNKNOWN_ERROR;
            break;
        case 2:
            sb = new StringBuffer();
            for (int i = 0, iSize = 2; retval == null && i < iSize; i++) { 
                
                ValueEval ve = singleOperandEvaluate(operands[i], srcRow, srcCol);
                if (ve instanceof StringValueEval) {
                    StringValueEval sve = (StringValueEval) ve;
                    sb.append(sve.getStringValue());
                }
                else if (ve instanceof BlankEval) {
                    // do nothing
                }
                else { // must be an error eval
                    retval = ve;
                }
            }
        }
        
        if (retval == null) {
            retval = new StringEval(sb.toString());
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
