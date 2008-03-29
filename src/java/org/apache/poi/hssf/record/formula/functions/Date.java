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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;

import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;

/**
 * @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 */
public class Date extends NumericFunction {
    /**
     * @see org.apache.poi.hssf.record.formula.functions.Function#evaluate(org.apache.poi.hssf.record.formula.eval.Eval[], int, short)
     */
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
        if (operands.length == 3) {
            ValueEval ve[] = new ValueEval[3];
            
            ve[0] = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
            ve[1] = singleOperandEvaluate(operands[1], srcCellRow, srcCellCol);
            ve[2] = singleOperandEvaluate(operands[2], srcCellRow, srcCellCol);
            
            if (validValues(ve)) {
                int year = getYear(ve[0]);
                int month = (int) ((NumericValueEval) ve[1]).getNumberValue() - 1;
                int day = (int) ((NumericValueEval) ve[2]).getNumberValue();
                
                if (year < 0 || month < 0 || day < 0) {
                    return ErrorEval.VALUE_INVALID;
                }
                
                if (year == 1900 && month == Calendar.FEBRUARY && day == 29) {
                    return new NumberEval(60.0);
                }
                
                if (year == 1900) {
                    if ((month == Calendar.JANUARY && day >= 60) ||
                        (month == Calendar.FEBRUARY && day >= 30))
                    {
                        day--;
                    }
                }
                
                Calendar c = new GregorianCalendar();
                
                c.set(year, month, day, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                
                return new NumberEval(HSSFDateUtil.getExcelDate(c.getTime(), false)); // XXX fix 1900/1904 problem
            }
        }
        
        return ErrorEval.VALUE_INVALID;
    }
    
    private int getYear(ValueEval ve) {
        int year = (int) ((NumericValueEval) ve).getNumberValue();
        
        if (year < 0) {
            return -1;
        }
        
        return year < 1900 ? 1900 + year : year;
    }
    
    private boolean validValues(ValueEval[] values) {
        for (int i = 0; i < values.length; i++) {
            ValueEval value =  values[i];
            
            if (value instanceof RefEval) {
                RefEval re = (RefEval) value;
                ValueEval ive = re.getInnerValueEval();
                
                if (ive instanceof BlankEval) {
                    value = new NumberEval(0);
                } else if (ive instanceof NumericValueEval) {
                    value = ive;
                } else {
                    return false;
                }
            }
            
            if (!(value instanceof NumericValueEval)) {
                return false;
            }
        }
        
        return true;
    }
}
