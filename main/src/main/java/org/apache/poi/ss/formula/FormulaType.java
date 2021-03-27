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

package org.apache.poi.ss.formula;

import org.apache.poi.util.Internal;

/**
 * Enumeration of various formula types.
 * 
 * See Sections 3 and 4.8 of https://www.openoffice.org/sc/excelfileformat.pdf
 */
@Internal
public enum FormulaType {
    /** Regular cell formula */
    CELL(true),
    
    /**
     * A Shared Formula ("{=SUM(A1:E1*{1,2,3,4,5}}")
     * 
     * Similar to an array formula, but stored in a SHAREDFMLA instead of ARRAY record as a file size optimization.
     * See Section 4.8 of https://www.openoffice.org/sc/excelfileformat.pdf
     */
    SHARED(true),
    
    /**
     * An Array formula ("{=SUM(A1:E1*{1,2,3,4,5}}")
     * https://support.office.com/en-us/article/Guidelines-and-examples-of-array-formulas-7D94A64E-3FF3-4686-9372-ECFD5CAA57C7
     */
    ARRAY(false),
    
    /** Conditional formatting */
    CONDFORMAT(true),
    
    /** Named range */
    NAMEDRANGE(false),
    
    /**
     * This constant is currently very specific.  The exact differences from general data
     * validation formulas or conditional format formulas is not known yet
     */
    DATAVALIDATION_LIST(false);
    
    /** formula is expected to return a single value vs. multiple values */
    private final boolean isSingleValue ;
    /**
     * @since POI 3.15 beta 3.
     */
    private FormulaType(boolean singleValue) {
        this.isSingleValue = singleValue;
    }
    
    /**
     * @return true if this formula type only returns single values, false if it can return multiple values (arrays, ranges, etc.)
     */
    public boolean isSingleValue() {
        return isSingleValue;
    }
    
    /**
     * Used to transition from <code>int</code>s (possibly stored in the Excel file) to <code>FormulaType</code>s.
     * @param code 
     * @return FormulaType
     * @throws IllegalArgumentException if code is out of range
     * @since POI 3.15 beta 3.
     */
    public static FormulaType forInt(int code) {
        if (code >= 0 && code < values().length) {
            return values()[code];
        }
        throw new IllegalArgumentException("Invalid FormulaType code: " + code);
    }
}
