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
 * An implementation of the SUBSTITUTE function:
 * Substitutes text in a text string with new text, some number of times.
 * @author Manda Wilson &lt; wilson at c bio dot msk cc dot org &gt;
 */
public class Substitute extends TextFunction {
	private static final int REPLACE_ALL = -1;
	
	/**
	 *Substitutes text in a text string with new text, some number of times.
	 * 
	 * @see org.apache.poi.hssf.record.formula.eval.Eval
	 */
    public Eval evaluate(Eval[] operands, int srcCellRow, short srcCellCol) {		
    	Eval retval = null;
        String oldStr = null;
        String searchStr = null;
        String newStr = null;
        int numToReplace = REPLACE_ALL;
        
        switch (operands.length) {
	        default:
	            retval = ErrorEval.VALUE_INVALID;
	        case 4:
	        	ValueEval fourthveval = singleOperandEvaluate(operands[3], srcCellRow, srcCellCol);
	        	if (fourthveval instanceof NumericValueEval) {
	        		NumericValueEval numToReplaceEval = (NumericValueEval) fourthveval;
	        		// NOTE: it is safe to cast to int here
	                // because in Excel =SUBSTITUTE("teststr","t","T",1.9) 
	                // returns Teststr 
	                // so 1.9 must be truncated to 1
		        	numToReplace = (int) numToReplaceEval.getNumberValue();
	        	} else {
	        		retval = ErrorEval.VALUE_INVALID;
	        	}
	        case 3:	
	        	// first operand is text string containing characters to replace
	            // second operand is text to find
	            // third operand is replacement text
	            ValueEval firstveval = singleOperandEvaluate(operands[0], srcCellRow, srcCellCol);
	            ValueEval secondveval = singleOperandEvaluate(operands[1], srcCellRow, srcCellCol);
	            ValueEval thirdveval = singleOperandEvaluate(operands[2], srcCellRow, srcCellCol);
	            if (firstveval instanceof StringValueEval
	            	&& secondveval instanceof StringValueEval
	            	&& thirdveval instanceof StringValueEval) {
	            	
	                StringValueEval oldStrEval = (StringValueEval) firstveval;
	                oldStr = oldStrEval.getStringValue();
	                
	                StringValueEval searchStrEval = (StringValueEval) secondveval;
	               	searchStr = searchStrEval.getStringValue();
	                
	               	StringValueEval newStrEval = (StringValueEval) thirdveval;
	               	newStr = newStrEval.getStringValue();
	            } else {
	            	retval = ErrorEval.VALUE_INVALID;
	            }
	    }
	        
        if (retval == null) {
			if (numToReplace != REPLACE_ALL && numToReplace < 1) {
				retval = ErrorEval.VALUE_INVALID;
			} else if (searchStr.length() == 0) {
				retval = new StringEval(oldStr);
			} else {
				StringBuffer strBuff = new StringBuffer();
				int startIndex = 0;
				int nextMatch = -1;
				for (int leftToReplace = numToReplace; 
					(leftToReplace > 0 || numToReplace == REPLACE_ALL) 
						&& (nextMatch = oldStr.indexOf(searchStr, startIndex)) != -1;
					leftToReplace--) {
					// store everything from end of last match to start of this match
					strBuff.append(oldStr.substring(startIndex, nextMatch));
					strBuff.append(newStr);
					startIndex = nextMatch + searchStr.length();
				}
				// store everything from end of last match to end of string
				if (startIndex < oldStr.length()) {
					strBuff.append(oldStr.substring(startIndex));
				}
				retval = new StringEval(strBuff.toString());
			}
        } 
		return retval;
    }
    
}
