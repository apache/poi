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
    CELL(0),
    
    /**
     * A Shared Formula ("{=SUM(A1:E1*{1,2,3,4,5}}")
     * 
     * Similar to an array formula, but stored in a SHAREDFMLA instead of ARRAY record as a file size optimization.
     * See Section 4.8 of https://www.openoffice.org/sc/excelfileformat.pdf
     */
    SHARED(1),
    
    /**
     * An Array formula ("{=SUM(A1:E1*{1,2,3,4,5}}")
     * https://support.office.com/en-us/article/Guidelines-and-examples-of-array-formulas-7D94A64E-3FF3-4686-9372-ECFD5CAA57C7
     */
    ARRAY(2),
    
    /** Conditional formatting */
    CONDFORMAT(3),
    
    /** Named range */
    NAMEDRANGE(4),
    
    /**
     * This constant is currently very specific.  The exact differences from general data
     * validation formulas or conditional format formulas is not known yet
     */
    DATAVALIDATION_LIST(5);
    
    /** @deprecated POI 3.15 beta 3. */
    private final int code;
    /**
     * @since POI 3.15 beta 3.
     * @deprecated POI 3.15 beta 3.
     * Formula type code doesn't mean anything. Only in this class for transitioning from a class with int constants to a true enum.
     * Remove hard-coded numbers from the enums above. */
    private FormulaType(int code) {
        this.code = code;
    }
    
    /**
     * @since POI 3.15 beta 3.
     * @deprecated POI 3.15 beta 3. Used to transition code from <code>int</code>s to <code>FormulaType</code>s.
     */
    public static FormulaType forInt(int code) {
        for (FormulaType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid FormulaType code: " + code);
    }
}
