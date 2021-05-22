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

package org.apache.poi.ss.formula.constant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.FormulaError;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Represents a constant error code value as encoded in a constant values array. <p>
 *
 * This class is a type-safe wrapper for a 16-bit int value performing a similar job to
 * {@code ErrorEval}.
 */
public final class ErrorConstant {
    private static final Logger LOG = LogManager.getLogger(ErrorConstant.class);
    private static final ErrorConstant NULL = new ErrorConstant(FormulaError.NULL.getCode());
    private static final ErrorConstant DIV_0 = new ErrorConstant(FormulaError.DIV0.getCode());
    private static final ErrorConstant VALUE = new ErrorConstant(FormulaError.VALUE.getCode());
    private static final ErrorConstant REF = new ErrorConstant(FormulaError.REF.getCode());
    private static final ErrorConstant NAME = new ErrorConstant(FormulaError.NAME.getCode());
    private static final ErrorConstant NUM = new ErrorConstant(FormulaError.NUM.getCode());
    private static final ErrorConstant NA = new ErrorConstant(FormulaError.NA.getCode());

    private final int _errorCode;

    private ErrorConstant(int errorCode) {
        _errorCode = errorCode;
    }

    public int getErrorCode() {
        return _errorCode;
    }

    public String getText() {
        if(FormulaError.isValidCode(_errorCode)) {
            return FormulaError.forInt(_errorCode).getString();
        }
        return "unknown error code (" + _errorCode + ")";
    }

    public static ErrorConstant valueOf(int errorCode) {
        if (FormulaError.isValidCode(errorCode)) {
            switch (FormulaError.forInt(errorCode)) {
                case NULL:  return NULL;
                case DIV0:  return DIV_0;
                case VALUE: return VALUE;
                case REF:   return REF;
                case NAME:  return NAME;
                case NUM:   return NUM;
                case NA:    return NA;
                default:    break;
            }
        }
        LOG.atWarn().log("Warning - unexpected error code ({})", box(errorCode));
        return new ErrorConstant(errorCode);
    }

    public String toString() {
        return getClass().getName() + " [" + getText() + "]";
    }
}
