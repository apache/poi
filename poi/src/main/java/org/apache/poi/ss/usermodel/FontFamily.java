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
 * The font family this font belongs to. A font family is a set of fonts having common stroke width and serif
 * characteristics. The font name overrides when there are conflicting values.
 *
 * @author Gisella Bronzetti
 */
public enum FontFamily {

    NOT_APPLICABLE(0),
    ROMAN(1),
    SWISS(2),
    MODERN(3),
    SCRIPT(4),
    DECORATIVE(5);

    private int family;

    private FontFamily(int value) {
        family = value;
    }

    /**
     * Returns index of this font family
     *
     * @return index of this font family
     */
    public int getValue() {
        return family;
    }

    private static FontFamily[] _table = new FontFamily[6];

    static {
        for (FontFamily c : values()) {
            _table[c.getValue()] = c;
        }
    }

    public static FontFamily valueOf(int family) {
        return _table[family];
    }
}
