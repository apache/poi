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
 * the different types of possible underline formatting
 *
 * @author Gisella Bronzetti
 */
public enum FontUnderline {

    /**
     * Single-line underlining under each character in the cell.
     * The underline is drawn through the descenders of
     * characters such as g and p..
     */
    SINGLE(1),

    /**
     * Double-line underlining under each character in the
     * cell. underlines are drawn through the descenders of
     * characters such as g and p.
     */
    DOUBLE(2),

    /**
     * Single-line accounting underlining under each
     * character in the cell. The underline is drawn under the
     * descenders of characters such as g and p.
     */
    SINGLE_ACCOUNTING(3),

    /**
     * Double-line accounting underlining under each
     * character in the cell. The underlines are drawn under
     * the descenders of characters such as g and p.
     */
    DOUBLE_ACCOUNTING(4),

    /**
     * No underline.
     */
    NONE(5);

    private int value;


    private FontUnderline(int val) {
        value = val;
    }

    public int getValue() {
        return value;
    }

    public byte getByteValue() {
        switch (this) {
            case DOUBLE:
                return Font.U_DOUBLE;
            case DOUBLE_ACCOUNTING:
                return Font.U_DOUBLE_ACCOUNTING;
            case SINGLE_ACCOUNTING:
                return Font.U_SINGLE_ACCOUNTING;
            case NONE:
                return Font.U_NONE;
            case SINGLE:
                return Font.U_SINGLE;
            default:
                return Font.U_SINGLE;
        }
    }

    private static FontUnderline[] _table = new FontUnderline[6];
    static {
        for (FontUnderline c : values()) {
            _table[c.getValue()] = c;
        }
    }

    public static FontUnderline valueOf(int value){
        return _table[value];
    }

    public static FontUnderline valueOf(byte value){
        FontUnderline val;
        switch (value) {
            case Font.U_DOUBLE:
                val = FontUnderline.DOUBLE;
                break;
            case Font.U_DOUBLE_ACCOUNTING:
                val = FontUnderline.DOUBLE_ACCOUNTING;
                break;
            case Font.U_SINGLE_ACCOUNTING:
                val = FontUnderline.SINGLE_ACCOUNTING;
                break;
            case Font.U_SINGLE:
                val = FontUnderline.SINGLE;
                break;
            default:
                val = FontUnderline.NONE;
                break;
        }
        return val;
    }

}
