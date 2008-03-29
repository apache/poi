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
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.StringValueEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * An implementation of the REPLACE function:
 * Replaces part of a text string based on the number of characters 
 * you specify, with another text string.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public class Replace extends TextFunction {

	/**
	 * Replaces part of a text string based on the number of characters 
	 * you specify, with another text string.
	 * 
	 * @see org.apache.poi.hssf.record.formula.eval.Eval
	 */
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {		
    	Eval retval = null;
        String oldStr = null;
        String newStr = null;
        int startNum = 0;
        int numChars = 0;
        
        switch (operands.length) {
	        default:
	            retval = ErrorEval.VALUE_INVALID;
	        case 4:	        
	        	// first operand is text string containing characters to replace
	            // second operand is position of first character to replace
	            // third operand is the number of characters in the old string
	            // you want to replace with new string
	            // fourth operand is the new string
	            ValueEval firstveval = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
	            ValueEval secondveval = singleOperandEvaluate(operands[1], srcCellRow, srcCellCol);
	            ValueEval thirdveval = singleOperandEvaluate(operands[2], srcCellRow, srcCellCol);
	            ValueEval fourthveval = singleOperandEvaluate(operands[3], srcCellRow, srcCellCol);
	            if (firstveval instanceof StringValueEval
	            	&& secondveval instanceof NumericValueEval
	            	&& thirdveval instanceof NumericValueEval
	            	&& fourthveval instanceof StringValueEval) {
	            	
	                StringValueEval oldStrEval = (StringValueEval) firstveval;
	                oldStr = oldStrEval.getStringValue();
	                
	                NumericValueEval startNumEval = (NumericValueEval) secondveval;
	                // NOTE: it is safe to cast to int here
	                // because in Excel =REPLACE("task", 2.7, 3, "est") 
	                // returns test 
	                // so 2.7 must be truncated to 2
	                // and =REPLACE("task", 1, 1.9, "") returns ask 
	                // so 1.9 must be truncated to 1
	                startNum = (int) startNumEval.getNumberValue();
	                
	                NumericValueEval numCharsEval = (NumericValueEval) thirdveval;
	                numChars = (int) numCharsEval.getNumberValue();
	                             
	                StringValueEval newStrEval = (StringValueEval) fourthveval;
	                newStr = newStrEval.getStringValue();
	            } else {
	            	retval = ErrorEval.VALUE_INVALID;
	            }
	    }
	        
        if (retval == null) {
			if (startNum < 1 || numChars < 0) {
				retval = ErrorEval.VALUE_INVALID;
			} else {
				StringBuffer strBuff = new StringBuffer(oldStr);
				// remove any characters that should be replaced
				if (startNum <= oldStr.length() && numChars != 0) {
					strBuff.delete(startNum - 1, startNum - 1 + numChars);
				} 
				// now insert (or append) newStr
				if (startNum > strBuff.length()) {
					strBuff.append(newStr);
				} else {
					strBuff.insert(startNum - 1, newStr);
				}
				retval = new StringEval(strBuff.toString());
			}
        } 
		return retval;
    }

}
