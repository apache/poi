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

import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.util.Internal;

/**
 * @since POI 3.15 beta 3
 */
public enum CellType {
    /**
     * Unknown type, used to represent a state prior to initialization or the
     * lack of a concrete type.
     * For internal use only.
     */
    @Internal(since="POI 3.15 beta 3")
    _NONE(-1),

    /**
     * Numeric cell type (whole numbers, fractional numbers, dates)
     */
    NUMERIC(0),
    
    /** String (text) cell type */
    STRING(1),
    
    /**
     * Formula cell type
     * @see FormulaType
     */
    FORMULA(2),
    
    /**
     * Blank cell type
     */
    BLANK(3),
    
    /**
     * Boolean cell type
     */
    BOOLEAN(4),
    
    /**
     * Error cell type
     * @see FormulaError
     */
    ERROR(5);

    /**
     * @since POI 3.15 beta 3
     * @deprecated POI 3.15 beta 3
     */
    private final int code;
    
    /**
     * @since POI 3.15 beta 3
     * @deprecated POI 3.15 beta 3
     */
    private CellType(int code) {
        this.code = code;
    }
    
    /**
     * @since POI 3.15 beta 3.
     * @deprecated POI 3.15 beta 3. Used to transition code from <code>int</code>s to <code>CellType</code>s.
     */
    public static CellType forInt(int code) {
        for (CellType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CellType code: " + code);
    }
    
    /**
     * @since POI 3.15 beta 3
     * @deprecated POI 3.15 beta 3
     */
    public int getCode() {
        return code;
    }
    
}
