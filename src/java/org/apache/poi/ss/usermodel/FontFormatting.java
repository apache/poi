/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

/**
 * High level representation for Font Formatting component
 * of Conditional Formatting settings
 */
public interface FontFormatting {
    /** Escapement type - None */
    public final static short SS_NONE  = 0;
    /** Escapement type - Superscript */
    public final static short SS_SUPER = 1;
    /** Escapement type - Subscript */
    public final static short SS_SUB   = 2;

    /** Underline type - None */
    public final static byte U_NONE              = 0;
    /** Underline type - Single */
    public final static byte U_SINGLE            = 1;
    /** Underline type - Double */
    public final static byte U_DOUBLE            = 2;
    /**  Underline type - Single Accounting */
    public final static byte U_SINGLE_ACCOUNTING = 0x21;
    /** Underline type - Double Accounting */
    public final static byte U_DOUBLE_ACCOUNTING = 0x22;

    /**
     * get the type of super or subscript for the font
     *
     * @return super or subscript option
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */
    short getEscapementType();

    /**
     * set the escapement type for the font
     *
     * @param escapementType  super or subscript option
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */
    void setEscapementType(short escapementType);

    /**
     * @return font colour index, or 0 if not indexed (XSSF only)
     */
    short getFontColorIndex();

    /**
     * Sets the indexed colour to use
     * @param color font colour index
     */
    void setFontColorIndex(short color);
    
    /**
     * @return The colour of the font, or null if no colour applied
     */
    Color getFontColor();
    
    /**
     * Sets the colour to use
     * @param color font colour to use
     */
    void setFontColor(Color color);

    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20); or -1 if not modified
     */
    int getFontHeight();

    /**
     * Sets the height of the font in 1/20th point units
     *
     * @param height the height in twips (in points/20)
     */
    void setFontHeight(int height);

    /**
     * get the type of underlining for the font
     *
     * @return font underlining type
     *
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */
    short getUnderlineType();

    /**
     * set the type of underlining type for the font
     *
     * @param underlineType  super or subscript option
     *
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */
    void setUnderlineType(short underlineType);

    /**
     * get whether the font weight is set to bold or not
     *
     * @return bold - whether the font is bold or not
     */
    boolean isBold();

    /**
     * @return true if font style was set to <i>italic</i>
     */
    boolean isItalic();


    /**
     * @return true if font strikeout is on
     */
    boolean isStruckout();

    /**
     * set font style options.
     *
     * @param italic - if true, set posture style to italic, otherwise to normal
     * @param bold if true, set font weight to bold, otherwise to normal
     */
    void setFontStyle(boolean italic, boolean bold);

    /**
     * set font style options to default values (non-italic, non-bold)
     */
    void resetFontStyle();
}
