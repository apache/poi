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

package org.apache.poi.ss.formula.ptg;

import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

public final class ErrPtg extends ScalarConstantPtg {

    /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final ErrPtg NULL_INTERSECTION = new ErrPtg(FormulaError.NULL.getCode());
    /** <b>#DIV/0!</b> - Division by zero */
    public static final ErrPtg DIV_ZERO = new ErrPtg(FormulaError.DIV0.getCode());
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final ErrPtg VALUE_INVALID = new ErrPtg(FormulaError.VALUE.getCode());
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final ErrPtg REF_INVALID = new ErrPtg(FormulaError.REF.getCode());
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final ErrPtg NAME_INVALID = new ErrPtg(FormulaError.NAME.getCode());
    /** <b>#NUM!</b> - Value range overflow */
    public static final ErrPtg NUM_ERROR = new ErrPtg(FormulaError.NUM.getCode());
    /** <b>#N/A</b> - Argument or function not available */
    public static final ErrPtg N_A = new ErrPtg(FormulaError.NA.getCode());


    public static final short sid  = 0x1c;
    private static final int  SIZE = 2;
    private final int field_1_error_code;

    /** Creates new ErrPtg */

    private ErrPtg(int errorCode) {
        if(!FormulaError.isValidCode(errorCode)) {
            throw new IllegalArgumentException("Invalid error code (" + errorCode + ")");
        }
        field_1_error_code = errorCode;
    }

    public static ErrPtg read(LittleEndianInput in)  {
        return valueOf(in.readByte());
    }

    public void write(LittleEndianOutput out) {
        out.writeByte(sid + getPtgClass());
        out.writeByte(field_1_error_code);
    }

    public String toFormulaString() {
        return FormulaError.forInt(field_1_error_code).getString();
    }

    public int getSize() {
        return SIZE;
    }

    public int getErrorCode() {
        return field_1_error_code;
    }

    public static ErrPtg valueOf(int code) {
        switch(FormulaError.forInt(code)) {
            case DIV0: return DIV_ZERO;
            case NA: return N_A;
            case NAME: return NAME_INVALID;
            case NULL: return NULL_INTERSECTION;
            case NUM: return NUM_ERROR;
            case REF: return REF_INVALID;
            case VALUE: return VALUE_INVALID;
            default:
                throw new RuntimeException("Unexpected error code (" + code + ")");
        }
    }
}
