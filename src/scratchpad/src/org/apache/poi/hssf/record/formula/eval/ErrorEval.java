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
 * Created on May 8, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * 
 */
public class ErrorEval implements ValueEval {

    private int errorCode;


    public static final ErrorEval NAME_INVALID = new ErrorEval(525);

    public static final ErrorEval VALUE_INVALID = new ErrorEval(519);

    
    // Non std error codes
    public static final ErrorEval UNKNOWN_ERROR = new ErrorEval(-20);

    public static final ErrorEval FUNCTION_NOT_IMPLEMENTED = new ErrorEval(-30);

    public static final ErrorEval REF_INVALID = new ErrorEval(-40);

    public static final ErrorEval NA = new ErrorEval(-50);
    
    public static final ErrorEval CIRCULAR_REF_ERROR = new ErrorEval(-60);
    
    public static final ErrorEval DIV_ZERO = new ErrorEval(-70);
    
    public static final ErrorEval NUM_ERROR = new ErrorEval(-80);

    private ErrorEval(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getStringValue() {
        return "Err:" + Integer.toString(errorCode);
    }

}
