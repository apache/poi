/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


/*
 * HSSFFont.java
 *
 * Created on December 9, 2001, 10:34 AM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.FontRecord;

/**
 * Represents a Font used in a workbook.
 *
 * @version 1.0-pre
 * @author  Andrew C. Oliver
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createFont()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCellStyle#setFont(HSSFFont)
 */

public class HSSFFont
{

    /**
     * Arial font
     */

    public final static String FONT_ARIAL          = "Arial";

    /**
     * Normal boldness (not bold)
     */

    public final static short  BOLDWEIGHT_NORMAL   = 0x190;

    /**
     * Bold boldness (bold)
     */

    public final static short  BOLDWEIGHT_BOLD     = 0x2bc;

    /**
     * normal type of black color
     */

    public final static short  COLOR_NORMAL        = 0x7fff;

    /**
     * Dark Red color
     */

    public final static short  COLOR_RED           = 0xa;

    /**
     * no type offsetting (not super or subscript)
     */

    public final static short  SS_NONE             = 0;

    /**
     * superscript
     */

    public final static short  SS_SUPER            = 1;

    /**
     * subscript
     */

    public final static short  SS_SUB              = 2;

    /**
     * not underlined
     */

    public final static byte   U_NONE              = 0;

    /**
     * single (normal) underline
     */

    public final static byte   U_SINGLE            = 1;

    /**
     * double underlined
     */

    public final static byte   U_DOUBLE            = 2;

    /**
     * accounting style single underline
     */

    public final static byte   U_SINGLE_ACCOUNTING = 0x21;

    /**
     * accounting style double underline
     */

    public final static byte   U_DOUBLE_ACCOUNTING = 0x22;
    private FontRecord         font;
    private short              index;

    /** Creates a new instance of HSSFFont */

    protected HSSFFont(short index, FontRecord rec)
    {
        font       = rec;
        this.index = index;
    }

    /**
     * set the name for the font (i.e. Arial)
     * @param name  String representing the name of the font to use
     * @see #FONT_ARIAL
     */

    public void setFontName(String name)
    {
        font.setFontName(name);
        font.setFontNameLength(( byte ) name.length());
    }

    /**
     * get the name for the font (i.e. Arial)
     * @return String representing the name of the font to use
     * @see #FONT_ARIAL
     */

    public String getFontName()
    {
        return font.getFontName();
    }

    /**
     * get the index within the HSSFWorkbook (sequence within the collection of Font objects)
     * @return unique index number of the underlying record this Font represents (probably you don't care
     *  unless you're comparing which one is which)
     */

    public short getIndex()
    {
        return index;
    }

    /**
     * set the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the setFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @param short - height in 1/20ths of a point
     * @see #setFontHeightInPoints(short)
     */

    public void setFontHeight(short height)
    {
        font.setFontHeight(height);
    }

    /**
     * set the font height
     * @param short - height in the familiar unit of measure - points
     * @see #setFontHeight(short)
     */

    public void setFontHeightInPoints(short height)
    {
        font.setFontHeight(( short ) (height * 20));
    }

    /**
     * get the font height in unit's of 1/20th of a point.  Maybe you might want to
     * use the getFontHeightInPoints which matches to the familiar 10, 12, 14 etc..
     * @return short - height in 1/20ths of a point
     * @see #getFontHeightInPoints()
     */

    public short getFontHeight()
    {
        return font.getFontHeight();
    }

    /**
     * get the font height
     * @return short - height in the familiar unit of measure - points
     * @see #getFontHeight()
     */

    public short getFontHeightInPoints()
    {
        return ( short ) (font.getFontHeight() / 20);
    }

    /**
     * set whether to use italics or not
     * @param italics or not
     */

    public void setItalic(boolean italic)
    {
        font.setItalic(italic);
    }

    /**
     * get whether to use italics or not
     * @return italics or not
     */

    public boolean getItalic()
    {
        return font.isItalic();
    }

    /**
     * set whether to use a strikeout horizontal line through the text or not
     * @param strikeout or not
     */

    public void setStrikeout(boolean strikeout)
    {
        font.setStrikeout(strikeout);
    }

    /**
     * get whether to use a strikeout horizontal line through the text or not
     * @return strikeout or not
     */

    public boolean getStrikeout()
    {
        return font.isStruckout();
    }

    /**
     * set the color for the font
     * @param color to use
     * @see #COLOR_NORMAL
     * @see #COLOR_RED
     */

    public void setColor(short color)
    {
        font.setColorPaletteIndex(color);
    }

    /**
     * get the color for the font
     * @return color to use
     * @see #COLOR_NORMAL
     * @see #COLOR_RED
     */

    public short getColor()
    {
        return font.getColorPaletteIndex();
    }

    /**
     * set the boldness to use
     * @param boldweight
     * @see #BOLDWEIGHT_NORMAL
     * @see #BOLDWEIGHT_BOLD
     */

    public void setBoldweight(short boldweight)
    {
        font.setBoldWeight(boldweight);
    }

    /**
     * get the boldness to use
     * @return boldweight
     * @see #BOLDWEIGHT_NORMAL
     * @see #BOLDWEIGHT_BOLD
     */

    public short getBoldweight()
    {
        return font.getBoldWeight();
    }

    /**
     * set normal,super or subscript.
     * @param offset type to use (none,super,sub)
     * @see #SS_NONE
     * @see #SS_SUPER
     * @see #SS_SUB
     */

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

    public byte getUnderline()
    {
        return font.getUnderline();
    }

    public String toString()
    {
        return "org.apache.poi.hssf.usermodel.HSSFFont{" +
                 font +
                "}";
    }


}
