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
 * The enumeration value indicating the line style of a border in a cell,
 * i.e., whether it is bordered dash dot, dash dot dot, dashed, dotted, double, hair, medium, 
 * medium dash dot, medium dash dot dot, medium dashed, none, slant dash dot, thick or thin.
 */
 public enum BorderStyle {

    /**
     * No border (default)
     */
    NONE(0x0),

    /**
     * Thin border
     */
    THIN(0x1),

    /**
     * Medium border
     */
    MEDIUM(0x2),

    /**
     * dash border
     */
    DASHED(0x3),

    /**
     * dot border
     */
    DOTTED(0x4),

    /**
     * Thick border
     */
    THICK(0x5),

    /**
     * double-line border
     */
    DOUBLE(0x6),

    /**
     * hair-line border
     */
    HAIR(0x7),

    /**
     * Medium dashed border
     */
    MEDIUM_DASHED(0x8),

    /**
     * dash-dot border
     */
    DASH_DOT(0x9),

    /**
     * medium dash-dot border
     */
    MEDIUM_DASH_DOT(0xA),

    /**
     * dash-dot-dot border
     */
    DASH_DOT_DOT(0xB),

    /**
     * medium dash-dot-dot border
     */
    MEDIUM_DASH_DOT_DOT(0xC),

    /**
     * slanted dash-dot border
     */
    SLANTED_DASH_DOT(0xD);
    
    private final short code;

    private BorderStyle(int code) {
        this.code = (short)code;
    }

    public short getCode() {
        return code;
    }
    
    private static final BorderStyle[] _table = new BorderStyle[0xD + 1];
    static {
        for (BorderStyle c : values()) {
            _table[c.getCode()] = c;
        }
    }
    
    public static BorderStyle valueOf(short code) {
        return _table[code];
    }
}
