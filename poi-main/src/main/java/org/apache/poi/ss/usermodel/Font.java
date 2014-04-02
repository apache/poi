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


public interface Font {
    /**
     * Normal boldness (not bold)
     */

    public final static short BOLDWEIGHT_NORMAL = 0x190;

    /**
     * Bold boldness (bold)
     */

    public final static short BOLDWEIGHT_BOLD = 0x2bc;

    /**
     * normal type of black color.
     */

    public final static short COLOR_NORMAL = 0x7fff;

    /**
     * Dark Red color
     */

    public final static short COLOR_RED = 0xa;

    /**
     * no type offsetting (not super or subscript)
     */

    public final static short SS_NONE = 0;

    /**
     * superscript
     */

    public final static short SS_SUPER = 1;

    /**
     * subscript
     */

    public final static short SS_SUB = 2;

    /**
     * not underlined
     */

    public final static byte U_NONE = 0;

    /**
     * single (normal) underline
     */

    public final static byte U_SINGLE = 1;

    /**
     * double underlined
     */

    public final static byte U_DOUBLE = 2;

    /**
     * accounting style single underline
     */

    public final static byte U_SINGLE_ACCOUNTING = 0x21;

    /**
     * accounting style double underline
     */

    public final static byte U_DOUBLE_ACCOUNTING = 0x22;

    /**
     * ANSI character set
     */
    public final static byte ANSI_CHARSET = 0;

    /**
     * Default character set.
     */
    public final static byte DEFAULT_CHARSET = 1;

    /**
     * Symbol character set
     */
    public final static byte SYMBOL_CHARSET = 2;

    /**
     * set the name for the font (i.e. Arial)
     * @param name  String representing the name of the font to use
     */

    void setFontName(String name);

    /**
     * get the name for the font (i.e. Arial)
     * @return String representing the name of the font to use
     */

    String getFontName();

    /**
     * set the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the setFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @param height height in 1/20ths of a point
     * @see #setFontHeightInPoints(short)
     */

    void setFontHeight(short height);

    /**
     * set the font height
     * @param height height in the familiar unit of measure - points
     * @see #setFontHeight(short)
     */

    void setFontHeightInPoints(short height);

    /**
     * get the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the getFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @return short - height in 1/20ths of a point
     * @see #getFontHeightInPoints()
     */

    short getFontHeight();

    /**
     * get the font height
     * @return short - height in the familiar unit of measure - points
     * @see #getFontHeight()
     */

    short getFontHeightInPoints();

    /**
     * set whether to use italics or not
     * @param italic italics or not
     */

    void setItalic(boolean italic);

    /**
     * get whether to use italics or not
     * @return italics or not
     */

    boolean getItalic();

    /**
     * set whether to use a strikeout horizontal line through the text or not
     * @param strikeout or not
     */

    void setStrikeout(boolean strikeout);

    /**
     * get whether to use a strikeout horizontal line through the text or not
     * @return strikeout or not
     */

    boolean getStrikeout();

    /**
     * set the color for the font
     * @param color to use
     * @see #COLOR_NORMAL Note: Use this rather than HSSFColor.AUTOMATIC for default font color
     * @see #COLOR_RED
     */

    void setColor(short color);

    /**
     * get the color for the font
     * @return color to use
     * @see #COLOR_NORMAL
     * @see #COLOR_RED
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    short getColor();

    /**
     * set normal,super or subscript.
     * @param offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */

    void setTypeOffset(short offset);

    /**
     * get normal,super or subscript.
     * @return offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */

    short getTypeOffset();

    /**
     * set type of text underlining to use
     * @param underline type
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */

    void setUnderline(byte underline);

    /**
     * get type of text underlining to use
     * @return underlining type
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */

    byte getUnderline();

    /**
     * get character-set to use.
     * @return character-set
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    int getCharSet();

    /**
     * set character-set to use.
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    void setCharSet(byte charset);
    /**
     * set character-set to use.
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    void setCharSet(int charset);

    /**
     * get the index within the XSSFWorkbook (sequence within the collection of Font objects)
     * 
     * @return unique index number of the underlying record this Font represents (probably you don't care
     *  unless you're comparing which one is which)
     */
    public short getIndex();

    public void setBoldweight(short boldweight);

    public short getBoldweight();


}