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
import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumericValueEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * Here are the general rules concerning Boolean functions:
 * <ol>
 * <li> Blanks are not either true or false
 * <li> Strings are not either true or false (even strings "true" 
 * or "TRUE" or "0" etc.)
 * <li> Numbers: 0 is false. Any other number is TRUE.
 * <li> References are evaluated and above rules apply.
 * <li> Areas: Individual cells in area are evaluated and checked to 
 * see if they are blanks, strings etc.
 * </ol>
 */
public abstract class BooleanFunction implements Function {

    protected ValueEval singleOperandEvaluate(Eval eval, int srcRow, short srcCol, boolean stringsAreBlanks) {
        ValueEval retval;
        
        if (eval instanceof RefEval) {
            RefEval re = (RefEval) eval;
            ValueEval ve = re.getInnerValueEval();
            retval = internalResolve(ve, true);
        }
        else {
            retval = internalResolve(eval, stringsAreBlanks);
        }
        
        return retval;
    }
    
    private ValueEval internalResolve(Eval ve, boolean stringsAreBlanks) {
        ValueEval retval = null;
        
        // blankeval is returned as is
        if (ve instanceof BlankEval) {
            retval = BlankEval.INSTANCE;
        }
        
        // stringeval
        else if (ve instanceof StringEval) {
            retval = stringsAreBlanks ? (ValueEval) BlankEval.INSTANCE : (StringEval) ve;
        }
        
        // bools are bools :)
        else if (ve instanceof BoolEval) {
            retval = (BoolEval) ve;
        }
        
        // convert numbers to bool
        else if (ve instanceof NumericValueEval) {
            NumericValueEval ne = (NumericValueEval) ve;
            double d = ne.getNumberValue();
            retval = Double.isNaN(d) 
                    ? (ValueEval) ErrorEval.VALUE_INVALID
                    : (d != 0) 
                        ? BoolEval.TRUE
                        : BoolEval.FALSE;
        }
        
        // since refevals
        else {
            retval = ErrorEval.VALUE_INVALID;
        }
        
        return retval;
        
    }
}
