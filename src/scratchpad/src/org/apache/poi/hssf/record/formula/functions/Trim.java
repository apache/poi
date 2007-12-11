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

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * An implementation of the TRIM function:
 * Removes leading and trailing spaces from value if evaluated operand
 *  value is string.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public class Trim extends TextFunction {

	/**
	 * Removes leading and trailing spaces from value if evaluated 
	 *  operand value is string.
	 * Returns StringEval only if evaluated operand is of type string 
	 *  (and is not blank or null) or number. If evaluated operand is 
	 *  of type string and is blank or null, or if evaluated operand is 
	 *  of type blank, returns BlankEval.  Otherwise returns ErrorEval.
	 * 
	 * @see org.apache.poi.hssf.record.formula.eval.Eval
	 */
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {
    	Eval retval = ErrorEval.VALUE_INVALID;
        String str = null;
        
        switch (operands.length) {
	        default:
	            break;
	        case 1:
	            ValueEval veval = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
	            if (veval instanceof StringValueEval) {
	                StringValueEval sve = (StringValueEval) veval;
	                str = sve.getStringValue();
	                if (str == null || str.trim().equals("")) {
	                	return BlankEval.INSTANCE;
	                }
	            }
	            else if (veval instanceof NumberEval) {
	                NumberEval neval = (NumberEval) veval;
	                str = neval.getStringValue();
	            } 
	            else if (veval instanceof BlankEval) {
	            	return BlankEval.INSTANCE;
	            }
	    }
	        
        if (str != null) {
            retval = new StringEval(str.trim());
        } 
        return retval;
    }
}
