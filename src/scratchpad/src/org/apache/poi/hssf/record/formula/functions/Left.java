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
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Left extends TextFunction {

    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        Eval retval = ErrorEval.VALUE_INVALID;
        int index = 1;
        switch (operands.length) {
        default:
            break;
        case 2:
            Eval indexEval = operands[1];
            index = evaluateAsInteger(indexEval);
            if (index < 0) {
                break;
            }
        case 1:
            ValueEval veval = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
            String str = null;
            if (veval instanceof StringEval) {
                StringEval stringEval = (StringEval) veval;
                str = stringEval.getStringValue();
            }
            else if (veval instanceof BoolEval) {
                BoolEval beval = (BoolEval) veval;
                str = beval.getBooleanValue() ? "TRUE" : "FALSE";
            }
            else if (veval instanceof NumberEval) {
                NumberEval neval = (NumberEval) veval;
                str = neval.getStringValue();
            }
            if (null != str) {
                str = str.substring(0, Math.min(str.length(), index));
                retval = new StringEval(str);
            }
        }
        return retval;
    }
    
    protected int evaluateAsInteger(Eval eval) {
        int numval = -1;
        if (eval instanceof NumberEval) {
            NumberEval neval = (NumberEval) eval;
            double d = neval.getNumberValue();
            numval = (int) d;
        }
        else if (eval instanceof StringEval) {
            StringEval seval = (StringEval) eval;
            String s = seval.getStringValue();
            try { 
                double d = Double.parseDouble(s);
                numval = (int) d;
            } 
            catch (Exception e) {
            }
        }
        else if (eval instanceof BoolEval) {
            BoolEval beval = (BoolEval) eval;
            numval = beval.getBooleanValue() ? 1 : 0;
        }
        else if (eval instanceof RefEval) {
            numval = evaluateAsInteger(xlateRefEval((RefEval) eval));
        }
        return numval;
    }
    
    protected Eval xlateRefEval(RefEval reval) {
        Eval retval = reval.getInnerValueEval();
        
        if (retval instanceof RefEval) {
            retval = xlateRefEval((RefEval) retval);
        }
        return retval;
    }
}
