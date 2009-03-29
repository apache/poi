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

import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.ss.usermodel.Font;

/**
 * Represents a Font used in a workbook.
 *
 *
 * @author  Andrew C. Oliver
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createFont()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCellStyle#setFont(HSSFFont)
 */
public final class HSSFFont implements Font {

    /**
     * Arial font
     */

    public final static String FONT_ARIAL = "Arial";


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
     * @param height height in 1/20ths of a point
     * @see #setFontHeightInPoints(short)
     */

    public void setFontHeight(short height)
    {
        font.setFontHeight(height);
    }

    /**
     * set the font height
     * @param height height in the familiar unit of measure - points
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
     * @param italic italics or not
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
     * @see #COLOR_NORMAL Note: Use this rather than HSSFColor.AUTOMATIC for default font color
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
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
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


    /**
     * get character-set to use.
     * @return character-set
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
    public byte getCharSet()
    {
        return font.getCharset();
    }

    /**
     * set character-set to use.
     * @see #ANSI_CHARSET
     * @see #DEFAULT_CHARSET
     * @see #SYMBOL_CHARSET
     */
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((font == null) ? 0 : font.hashCode());
		result = prime * result + index;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof HSSFFont) {
			final HSSFFont other = (HSSFFont) obj;
			if (font == null) {
				if (other.font != null)
					return false;
			} else if (!font.equals(other.font))
				return false;
			if (index != other.index)
				return false;
			return true;
		}
		return false;
	}
}
