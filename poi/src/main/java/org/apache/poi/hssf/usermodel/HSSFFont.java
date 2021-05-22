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

package org.apache.poi.hssf.usermodel;

import java.util.Objects;

import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.util.Removal;

/**
 * Represents a Font used in a workbook.
 *
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createFont()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(int)
 * @see org.apache.poi.hssf.usermodel.HSSFCellStyle#setFont(HSSFFont)
 */
public final class HSSFFont implements Font {

    /**
     * Normal boldness (not bold)
     */
    static final short BOLDWEIGHT_NORMAL = 0x190;

    /**
     * Bold boldness (bold)
     */
    static final short BOLDWEIGHT_BOLD = 0x2bc;

    /**
     * Arial font
     */
    public static final String FONT_ARIAL = "Arial";


    private final FontRecord font;
    private final int index;

    /** Creates a new instance of HSSFFont */

    protected HSSFFont(int index, FontRecord rec)
    {
        font       = rec;
        this.index = index;
    }

    /**
     * set the name for the font (i.e. Arial)
     * @param name  String representing the name of the font to use
     * @see #FONT_ARIAL
     */

    @Override
    public void setFontName(String name)
    {
        font.setFontName(name);
    }

    /**
     * get the name for the font (i.e. Arial)
     * @return String representing the name of the font to use
     * @see #FONT_ARIAL
     */
    @Override
    public String getFontName()
    {
        return font.getFontName();
    }

    @Override
    public int getIndex() { return index; }

    @Deprecated
    @Removal(version = "6.0.0")
    @Override
    public int getIndexAsInt()
    {
        return index;
    }

    /**
     * set the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the setFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @param height height in 1/20ths of a point
     * @see #setFontHeightInPoints(short)
     */

    @Override
    public void setFontHeight(short height)
    {
        font.setFontHeight(height);
    }

    /**
     * set the font height
     * @param height height in the familiar unit of measure - points
     * @see #setFontHeight(short)
     */

    @Override
    public void setFontHeightInPoints(short height)
    {
        font.setFontHeight(( short ) (height * Font.TWIPS_PER_POINT));
    }

    /**
     * get the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the getFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @return short - height in 1/20ths of a point
     * @see #getFontHeightInPoints()
     */

    @Override
    public short getFontHeight()
    {
        return font.getFontHeight();
    }

    /**
     * get the font height
     * @return short - height in the familiar unit of measure - points
     * @see #getFontHeight()
     */

    @Override
    public short getFontHeightInPoints()
    {
        return ( short ) (font.getFontHeight() / Font.TWIPS_PER_POINT);
    }

    /**
     * set whether to use italics or not
     * @param italic italics or not
     */

    @Override
    public void setItalic(boolean italic)
    {
        font.setItalic(italic);
    }

    /**
     * get whether to use italics or not
     * @return italics or not
     */

    @Override
    public boolean getItalic()
    {
        return font.isItalic();
    }

    /**
     * set whether to use a strikeout horizontal line through the text or not
     * @param strikeout or not
     */

    @Override
    public void setStrikeout(boolean strikeout)
    {
        font.setStrikeout(strikeout);
    }

    /**
     * get whether to use a strikeout horizontal line through the text or not
     * @return strikeout or not
     */

    @Override
    public boolean getStrikeout()
    {
        return font.isStruckout();
    }

    /**
     * set the color for the font
     * @param color to use
     * @see #COLOR_NORMAL Note: Use this rather than HSSFColor.AUTOMATIC for default font color
     * @see #COLOR_RED
     */

    @Override
    public void setColor(short color)
    {
        font.setColorPaletteIndex(color);
    }

    /**
     * get the color for the font
     * @return color to use
     * @see #COLOR_NORMAL
     * @see #COLOR_RED
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    @Override
    public short getColor()
    {
        return font.getColorPaletteIndex();
    }

    /**
     * get the color value for the font
     */
    public HSSFColor getHSSFColor(HSSFWorkbook wb)
    {
       HSSFPalette pallette = wb.getCustomPalette();
       return pallette.getColor( getColor() );
    }

    /**
     * sets the font to be bold or not
     */
    @Override
    public void setBold(boolean bold)
    {
        if (bold)
            font.setBoldWeight(BOLDWEIGHT_BOLD);
        else
            font.setBoldWeight(BOLDWEIGHT_NORMAL);
    }

    /**
     * get if the font is bold or not
     */
    @Override
    public boolean getBold()
    {
        return font.getBoldWeight() == BOLDWEIGHT_BOLD;
    }

    /**
     * set normal,super or subscript.
     * @param offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */

    @Override
    public void setTypeOffset(short offset)
    {
        font.setSuperSubScript(offset);
    }

    /**
     * get normal,super or subscript.
     * @return offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */

    @Override
    public short getTypeOffset()
    {
        return font.getSuperSubScript();
    }

    /**
     * set type of text underlining to use
     * @param underline type
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */

    @Override
    public void setUnderline(byte underline)
    {
        font.setUnderline(underline);
    }

    /**
     * get type of text underlining to use
     * @return underlining type
     * @see #U_NONE
     * @see #U_SINGLE
     * @see #U_DOUBLE
     * @see #U_SINGLE_ACCOUNTING
     * @see #U_DOUBLE_ACCOUNTING
     */

    @Override
    public byte getUnderline()
    {
        return font.getUnderline();
    }


    /**
     * get character-set to use.
     * @return character-set
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    @Override
    public int getCharSet()
    {
        byte charset = font.getCharset();
        if(charset >= 0) {
           return charset;
        } else {
           return charset + 256;
        }
    }

    /**
     * set character-set to use.
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    @Override
    public void setCharSet(int charset)
    {
        byte cs = (byte)charset;
        if(charset > 127) {
           cs = (byte)(charset-256);
        }
        setCharSet(cs);
    }

    /**
     * set character-set to use.
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    @Override
    public void setCharSet(byte charset)
    {
        font.setCharset(charset);
    }

    public String toString()
    {
        return "org.apache.poi.hssf.usermodel.HSSFFont{" +
                 font +
                "}";
    }

    public int hashCode() {
        return Objects.hash(font,index);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof HSSFFont) {
            final HSSFFont other = (HSSFFont) obj;
            if (font == null) {
                if (other.font != null) {
                    return false;
                }
            } else if (!font.equals(other.font)) {
                return false;
            }
            return index == other.index;
        }
        return false;
    }
}
