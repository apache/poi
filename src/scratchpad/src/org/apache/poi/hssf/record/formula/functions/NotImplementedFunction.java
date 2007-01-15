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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;

/**
 * 
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the default implementation of a Function class. 
 * The default behaviour is to return a non-standard ErrorEval
 * "ErrorEval.FUNCTION_NOT_IMPLEMENTED". This error should alert 
 * the user that the formula contained a function that is not
 * yet implemented.
 */
public class NotImplementedFunction implements Function {

    public Eval evaluate(Eval[] operands, int srcRow, short srcCol) {
        return ErrorEval.FUNCTION_NOT_IMPLEMENTED;
    }

}
