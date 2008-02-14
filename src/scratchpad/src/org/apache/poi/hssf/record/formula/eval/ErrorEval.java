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
public final class ErrorEval implements ValueEval {
    /**
     * Contains raw Excel error codes (as defined in OOO's excelfileformat.pdf (2.5.6)
     */
    private static final class ErrorCode {
        /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
        public static final int NULL = 0x00;
        /** <b>#DIV/0!</b> - Division by zero */
        public static final int DIV_0 = 0x07;
        /** <b>#VALUE!</b> - Wrong type of operand */
        public static final int VALUE = 0x0F; 
        /** <b>#REF!</b> - Illegal or deleted cell reference */
        public static final int REF = 0x17;  
        /** <b>#NAME?</b> - Wrong function or range name */
        public static final int NAME = 0x1D; 
        /** <b>#NUM!</b> - Value range overflow */
        public static final int NUM = 0x24; 
        /** <b>#N/A</b> - Argument or function not available */
        public static final int N_A = 0x2A;   
        
        public static final String getText(int errorCode) {
            switch(errorCode) {
                case NULL:  return "#NULL!";
                case DIV_0: return "#DIV/0!";
                case VALUE: return "#VALUE!";
                case REF:   return "#REF!";
                case NAME:  return "#NAME?";
                case NUM:   return "#NUM!";
                case N_A:   return "#N/A";
            }
            return "???";
        }
    }

    /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final ErrorEval NULL_INTERSECTION = new ErrorEval(ErrorCode.NULL); 
    /** <b>#DIV/0!</b> - Division by zero */
    public static final ErrorEval DIV_ZERO = new ErrorEval(ErrorCode.DIV_0);
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final ErrorEval VALUE_INVALID = new ErrorEval(ErrorCode.VALUE);
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final ErrorEval REF_INVALID = new ErrorEval(ErrorCode.REF);
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final ErrorEval NAME_INVALID = new ErrorEval(ErrorCode.NAME); 
    /** <b>#NUM!</b> - Value range overflow */
    public static final ErrorEval NUM_ERROR = new ErrorEval(ErrorCode.NUM);
    /** <b>#N/A</b> - Argument or function not available */
    public static final ErrorEval NA = new ErrorEval(ErrorCode.N_A);

    
    /**
     * Translates an Excel internal error code into the corresponding POI ErrorEval instance 
     * @param errorCode
     */
    public static ErrorEval valueOf(int errorCode) {
        switch(errorCode) {
            case ErrorCode.NULL: return NULL_INTERSECTION;
            case ErrorCode.DIV_0: return DIV_ZERO;
            case ErrorCode.VALUE: return VALUE_INVALID;
//            case ErrorCode.REF: return REF_INVALID;
            case ErrorCode.REF: return UNKNOWN_ERROR;
            case ErrorCode.NAME: return NAME_INVALID;
            case ErrorCode.NUM: return NUM_ERROR;
            case ErrorCode.N_A: return NA;
            
            // these cases probably shouldn't be coming through here 
            // but (as of Jan-2008) a lot of code depends on it. 
//            case -20: return UNKNOWN_ERROR;
//            case -30: return FUNCTION_NOT_IMPLEMENTED;
//            case -60: return CIRCULAR_REF_ERROR;
        }
        throw new RuntimeException("Unexpected error code (" + errorCode + ")");
    }
    
    // POI internal error codes
    public static final ErrorEval UNKNOWN_ERROR = new ErrorEval(-20);
    public static final ErrorEval FUNCTION_NOT_IMPLEMENTED = new ErrorEval(-30);
    // Note - Excel does not seem to represent this condition with an error code
    public static final ErrorEval CIRCULAR_REF_ERROR = new ErrorEval(-60); 


    private int errorCode;
    /**
     * @param errorCode an 8-bit value
     */
    private ErrorEval(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(ErrorCode.getText(errorCode));
        sb.append("]");
        return sb.toString();
    }
}
