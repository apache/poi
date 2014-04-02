/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.usermodel;

/**
 * Contains raw Excel error codes (as defined in OOO's excelfileformat.pdf (2.5.6)
 * 
 * @author  Michael Harhen
 */
public class ErrorConstants {
    protected ErrorConstants() {
        // no instances of this class
    }
    
    /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final int ERROR_NULL = 0x00;
    /** <b>#DIV/0!</b> - Division by zero */
    public static final int ERROR_DIV_0 = 0x07;
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final int ERROR_VALUE = 0x0F; 
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final int ERROR_REF = 0x17;  
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final int ERROR_NAME = 0x1D; 
    /** <b>#NUM!</b> - Value range overflow */
    public static final int ERROR_NUM = 0x24; 
    /** <b>#N/A</b> - Argument or function not available */
    public static final int ERROR_NA = 0x2A;
    
    
    /**
     * @return Standard Excel error literal for the specified error code. 
     * @throws IllegalArgumentException if the specified error code is not one of the 7 
     * standard error codes
     */
    public static final String getText(int errorCode) {
        switch(errorCode) {
            case ERROR_NULL:  return "#NULL!";
            case ERROR_DIV_0: return "#DIV/0!";
            case ERROR_VALUE: return "#VALUE!";
            case ERROR_REF:   return "#REF!";
            case ERROR_NAME:  return "#NAME?";
            case ERROR_NUM:   return "#NUM!";
            case ERROR_NA:    return "#N/A";
        }
        throw new IllegalArgumentException("Bad error code (" + errorCode + ")");
    }
    
    /**
     * @return <code>true</code> if the specified error code is a standard Excel error code. 
     */
    public static final boolean isValidCode(int errorCode) {
        // This method exists because it would be bad to force clients to catch 
        // IllegalArgumentException if there were potential for passing an invalid error code.  
        switch(errorCode) {
            case ERROR_NULL:
            case ERROR_DIV_0:
            case ERROR_VALUE:
            case ERROR_REF:
            case ERROR_NAME:
            case ERROR_NUM:
            case ERROR_NA:
                return true;
        }
        return false;
    }
}
