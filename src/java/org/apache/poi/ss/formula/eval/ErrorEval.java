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

package org.apache.poi.ss.formula.eval;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.FormulaError;

/**
 * Evaluations for formula errors
 */
public final class ErrorEval implements ValueEval {
    private static final Map<FormulaError,ErrorEval> evals = new HashMap<>();
    
    /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final ErrorEval NULL_INTERSECTION = new ErrorEval(FormulaError.NULL);
    /** <b>#DIV/0!</b> - Division by zero */
    public static final ErrorEval DIV_ZERO = new ErrorEval(FormulaError.DIV0);
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final ErrorEval VALUE_INVALID = new ErrorEval(FormulaError.VALUE);
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final ErrorEval REF_INVALID = new ErrorEval(FormulaError.REF);
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final ErrorEval NAME_INVALID = new ErrorEval(FormulaError.NAME);
    /** <b>#NUM!</b> - Value range overflow */
    public static final ErrorEval NUM_ERROR = new ErrorEval(FormulaError.NUM);
    /** <b>#N/A</b> - Argument or function not available */
    public static final ErrorEval NA = new ErrorEval(FormulaError.NA);

    // POI internal error codes
    public static final ErrorEval FUNCTION_NOT_IMPLEMENTED = new ErrorEval(FormulaError.FUNCTION_NOT_IMPLEMENTED);

    // Note - Excel does not seem to represent this condition with an error code
    public static final ErrorEval CIRCULAR_REF_ERROR = new ErrorEval(FormulaError.CIRCULAR_REF);

    /**
     * Translates an Excel internal error code into the corresponding POI ErrorEval instance
     * @param errorCode An error code listed in {@link FormulaError}
     * @throws RuntimeException If an unknown errorCode is specified
     */
    public static ErrorEval valueOf(int errorCode) {
        FormulaError error = FormulaError.forInt(errorCode);
        ErrorEval eval = evals.get(error);
        if (eval != null) {
            return eval;
        } else {
            throw new RuntimeException("Unhandled error type for code " + errorCode);
        }
    }

    /**
     * Converts error codes to text.  Handles non-standard error codes OK.  
     * For debug/test purposes (and for formatting error messages).
     * @return the String representation of the specified Excel error code.
     */
    public static String getText(int errorCode) {
        if(FormulaError.isValidCode(errorCode)) {
            return FormulaError.forInt(errorCode).getString();
        }
        // Give a special string, based on ~, to make clear this isn't a standard Excel error
        return "~non~std~err(" + errorCode + ")~";
    }

    private FormulaError _error;
    private ErrorEval(FormulaError error) {
        _error = error;
        evals.put(error, this);
    }

    public int getErrorCode() {
        return _error.getLongCode();
    }
    public String getErrorString() {
        return _error.getString();
    }
    public String toString() {
        return getClass().getName() + " [" +
                _error.getString() +
                "]";
    }
}
