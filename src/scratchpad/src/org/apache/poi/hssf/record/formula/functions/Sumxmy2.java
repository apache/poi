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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt; 
 *
 */
public class Sumxmy2 extends XYNumericFunction {

    
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        ValueEval retval = null;
        double[][] values = null;
        
        int checkLen = 0; // check to see that all array lengths are equal
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            values = getValues(operands, srcCellRow, srcCellCol);
            if (values==null 
                    || values[X] == null || values[Y] == null
                    || values[X].length == 0 || values[Y].length == 0
                    || values[X].length != values[Y].length) {
                retval = ErrorEval.VALUE_INVALID;
            }
        }
        
        if (retval == null) {
            double d = MathX.sumxmy2(values[X], values[Y]);
            retval = (Double.isNaN(d) || Double.isInfinite(d))
                    ? (ValueEval) ErrorEval.NUM_ERROR
                    : new NumberEval(d);
        }
        
        return retval;
    }
}
