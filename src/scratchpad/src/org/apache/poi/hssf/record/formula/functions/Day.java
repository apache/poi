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
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;

import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;

/**
 * @author Pavel Krupets
 */
public class Day extends NumericFunction {
    /**
     * @see org.apache.poi.hssf.record.formula.functions.Function#evaluate(org.apache.poi.hssf.record.formula.eval.Eval[], int, short)
     */
    public Eval evaluate(Eval[] operands, int srcCellRow, short
srcCellCol) {
        ValueEval retval = null;
        
        switch (operands.length) {
        default:
            retval = ErrorEval.VALUE_INVALID;
            break;
        case 1:
            ValueEval ve = singleOperandEvaluate(operands[0],
srcCellRow, srcCellCol);
            if (ve instanceof NumericValueEval) {
                NumericValueEval ne = (NumericValueEval) ve;
                if (HSSFDateUtil.isValidExcelDate(ne.getNumberValue())) {
                    java.util.Date d = HSSFDateUtil.getJavaDate(ne.getNumberValue());
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(d);
                    retval = new NumberEval(c.get(java.util.Calendar.DAY_OF_MONTH));
                } else {
                    retval = ErrorEval.NUM_ERROR;
                }
            } else if (ve instanceof BlankEval) {
                // do nothing
            } else {
                retval = ErrorEval.NUM_ERROR;
            }
        }
        return retval;
    }
}
