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
 * Created on May 6, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class Atan2 extends NumericFunction {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double d0 = 0;
        double d1 = 0;
        ValueEval retval = null;

        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 2:
            ValueEval ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                d0 = ne.getNumberValue();
            }
            else if (ve instanceof BlankEval) {
                // do nothing
            }
            else {
                retval = ErrorEval.NUM_ERROR;
            }

            if (retval == null) {
                ve = singleOperandEvaluate(operands[1], srcRow, srcCol);
                if (ve instanceof NumericValueEval) {
                    NumericValueEval ne = (NumericValueEval) ve;
                    d1 = ne.getNumberValue();
                }
                else if (ve instanceof BlankEval) {
                    // do nothing
                }
                else {
                    retval = ErrorEval.NUM_ERROR;
                }
            }
        }

        if (retval == null) {
            double d = (d0 == d1 && d1 == 0)
                    ? Double.NaN
                    : Math.atan2(d1, d0);
            retval = (Double.isNaN(d) || Double.isInfinite(d))
                    ? (ValueEval) ErrorEval.NUM_ERROR
                    : new NumberEval(d);
        }
        return retval;
    }

}
