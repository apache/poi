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

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * An implementation of the MID function:
 * Returns a specific number of characters from a text string, 
 * starting at the position you specify, based on the number 
 * of characters you specify.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public class Mid extends TextFunction {
	/**
	 * Returns a specific number of characters from a text string, 
	 * starting at the position you specify, based on the number 
	 * of characters you specify.
	 * 
	 * @see org.apache.poi.hssf.record.formula.eval.Eval
	 */
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {		
    	Eval retval = null;
        String str = null;
        int startNum = 0;
        int numChars = 0;
        
        switch (operands.length) {
	        default:
	            retval = ErrorEval.VALUE_INVALID;
	        case 3:
	        	// first operand is text string containing characters to extract
	            // second operand is position of first character to extract
	            // third operand is the number of characters to return
	            ValueEval firstveval = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
	            ValueEval secondveval = singleOperandEvaluate(operands[1], srcCellRow, srcCellCol);
	            ValueEval thirdveval = singleOperandEvaluate(operands[2], srcCellRow, srcCellCol);
	            if (firstveval instanceof StringValueEval
	            	&& secondveval instanceof NumericValueEval
	            	&& thirdveval instanceof NumericValueEval) {
	            	
	                StringValueEval strEval = (StringValueEval) firstveval;
	                str = strEval.getStringValue();
	                
	                NumericValueEval startNumEval = (NumericValueEval) secondveval;
	                // NOTE: it is safe to cast to int here
	                // because in Excel =MID("test", 1, 1.7) returns t 
	                // so 1.7 must be truncated to 1
	                // and =MID("test", 1.9, 2) returns te 
	                // so 1.9 must be truncated to 1
	                startNum = (int) startNumEval.getNumberValue();
	                
	                NumericValueEval numCharsEval = (NumericValueEval) thirdveval;
	                numChars = (int) numCharsEval.getNumberValue();
	                
	            } else {
	            	retval = ErrorEval.VALUE_INVALID;
	            }
	    }
	        
        if (retval == null) {
			if (startNum < 1 || numChars < 0) {
				retval = ErrorEval.VALUE_INVALID;
			} else if (startNum > str.length() || numChars == 0) {
				retval = BlankEval.INSTANCE;
			} else if (startNum + numChars > str.length()) {
				retval = new StringEval(str.substring(startNum - 1));
			} else {
				retval = new StringEval(str.substring(startNum - 1, numChars));
			} 
        } 
		return retval;
    }

}
