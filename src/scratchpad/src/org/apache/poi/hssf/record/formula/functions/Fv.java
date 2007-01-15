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
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author 
 *
 */
public class Fv extends FinanceFunction {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        double rate = 0, nper = 0, pmt = 0, pv = 0, d = 0;
        boolean type = false;
        ValueEval retval = null;
        ValueEval ve = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 5:
            ve = singleOperandNumericAsBoolean(operands[4], srcRow, srcCol);
            if (ve instanceof ErrorEval) { retval = ErrorEval.VALUE_INVALID; break; }
            type = ((BoolEval) ve).getBooleanValue();
        case 4:
            ve = singleOperandEvaluate(operands[3], srcRow, srcCol);
            if (ve instanceof NumericValueEval) pv   = ((NumericValueEval) ve).getNumberValue();
            else { retval = ErrorEval.VALUE_INVALID; break; }
        case 3:
            ve = singleOperandEvaluate(operands[1], srcRow, srcCol);
            if (ve instanceof NumericValueEval) nper = ((NumericValueEval) ve).getNumberValue();
            else { retval = ErrorEval.VALUE_INVALID; break; }

            ve = singleOperandEvaluate(operands[2], srcRow, srcCol);
            if (ve instanceof NumericValueEval) pmt  = ((NumericValueEval) ve).getNumberValue();
            else { retval = ErrorEval.VALUE_INVALID; break; }

            ve = singleOperandEvaluate(operands[0], srcRow, srcCol);
            if (ve instanceof NumericValueEval) rate = ((NumericValueEval) ve).getNumberValue();
            else { retval = ErrorEval.VALUE_INVALID; break; }
        }
        
        if (retval == null) {
            d = FinanceLib.fv(rate, nper, pmt, pv, type);
            retval = (Double.isNaN(d))
                    ? (ValueEval) ErrorEval.VALUE_INVALID
                    : (Double.isInfinite(d)) 
                        ? (ValueEval) ErrorEval.NUM_ERROR 
                        : new NumberEval(d);
        }
        return retval;
    }

}
