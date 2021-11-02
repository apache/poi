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
 * Types of ignored workbook and worksheet error.
 * 
 * TODO Implement these for HSSF too, using FeatFormulaErr2,
 *  see bugzilla bug #46136 for details
 */
public enum IgnoredErrorType {
    /**
     * ????. Probably XSSF-only.
     */
    CALCULATED_COLUMN,
    
    /**
     * Whether to check for references to empty cells.
     * HSSF + XSSF.
     */
    EMPTY_CELL_REFERENCE,
    
    /**
     * Whether to check for calculation/evaluation errors.
     * HSSF + XSSF.
     */
    EVALUATION_ERROR,
    
    /**
     * Whether to check formulas in the range of the shared feature 
     *  that are inconsistent with formulas in neighbouring cells.
     * HSSF + XSSF.
     */
    FORMULA,
    
    /**
     * Whether to check formulas in the range of the shared feature 
     * with references to less than the entirety of a range containing 
     * continuous data.
     * HSSF + XSSF.
     */
    FORMULA_RANGE,
    
    /**
     * ????. Is this XSSF-specific the same as performDataValidation
     *  in HSSF?
     */
    LIST_DATA_VALIDATION,
    
    /**
     * Whether to check the format of string values and warn
     *  if they look to actually be numeric values.
     * HSSF + XSSF.
     */
    NUMBER_STORED_AS_TEXT,
    
    /**
     * ????. Is this XSSF-specific the same as checkDateTimeFormats
     *  in HSSF?
     */
    TWO_DIGIT_TEXT_YEAR,
    
    /**
     * Whether to check for unprotected formulas.
     * HSSF + XSSF.
     */
    UNLOCKED_FORMULA
}
