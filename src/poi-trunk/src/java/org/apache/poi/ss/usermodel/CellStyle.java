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

import org.apache.poi.util.Removal;

public interface CellStyle {

    /**
     * get the index within the Workbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */
    short getIndex();

    /**
     * set the data format (must be a valid format). Built in formats are defined at {@link BuiltinFormats}.
     * @see DataFormat
     */
    void setDataFormat(short fmt);

    /**
     * get the index of the data format. Built in formats are defined at {@link BuiltinFormats}.
     * @see DataFormat
     */
    short getDataFormat();

    /**
     * Get the format string
     */
    String getDataFormatString();

    /**
     * set the font for this style
     * @param font  a font object created or retrieved from the Workbook object
     * @see Workbook#createFont()
     * @see Workbook#getFontAt(int)
     */
    void setFont(Font font);

    /**
     * gets the index of the font for this style
     * @see Workbook#getFontAt(short)
     * @deprecated use <code>getFontIndexAsInt()</code> instead
     */
    @Removal(version = "4.2")
    short getFontIndex();

    /**
     * gets the index of the font for this style
     * @see Workbook#getFontAt(int)
     * @since 4.0.0
     */
    int getFontIndexAsInt();

    /**
     * set the cell's using this style to be hidden
     * @param hidden - whether the cell using this style should be hidden
     */
    void setHidden(boolean hidden);

    /**
     * get whether the cell's using this style are to be hidden
     * @return hidden - whether the cell using this style should be hidden
     */
    boolean getHidden();

    /**
     * set the cell's using this style to be locked
     * @param locked - whether the cell using this style should be locked
     */
    void setLocked(boolean locked);

    /**
     * get whether the cell's using this style are to be locked
     * @return hidden - whether the cell using this style should be locked
     */
    boolean getLocked();
    
    /**
     * Turn on or off "Quote Prefix" or "123 Prefix" for the style,
     *  which is used to tell Excel that the thing which looks like
     *  a number or a formula shouldn't be treated as on.
     * Turning this on is somewhat (but not completely, see {@link IgnoredErrorType})
     *  like prefixing the cell value with a ' in Excel
     */
    void setQuotePrefixed(boolean quotePrefix);
    
    /**
     * Is "Quote Prefix" or "123 Prefix" enabled for the cell?
     * Having this on is somewhat (but not completely, see {@link IgnoredErrorType})
     *  like prefixing the cell value with a ' in Excel
     */
    boolean getQuotePrefixed();

    /**
     * set the type of horizontal alignment for the cell
     * @param align - the type of alignment
     */
    void setAlignment(HorizontalAlignment align);

    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     */
    HorizontalAlignment getAlignment();

    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     * @deprecated use <code>getAlignment()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    HorizontalAlignment getAlignmentEnum();

    /**
     * Set whether the text should be wrapped.
     * Setting this flag to <code>true</code> make all content visible
     * within a cell by displaying it on multiple lines
     *
     * @param wrapped  wrap text or not
     */
    void setWrapText(boolean wrapped);

    /**
     * get whether the text should be wrapped
     * @return wrap text or not
     */
    boolean getWrapText();

    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     */
    void setVerticalAlignment(VerticalAlignment align);

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     */
    VerticalAlignment getVerticalAlignment();

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @deprecated use <code>getVerticalAlignment()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    VerticalAlignment getVerticalAlignmentEnum();

    /**
     * set the degree of rotation for the text in the cell.
     *
     * Note: HSSF uses values from -90 to 90 degrees, whereas XSSF 
     * uses values from 0 to 180 degrees. The implementations of this method will map between these two value-ranges 
     * accordingly, however the corresponding getter is returning values in the range mandated by the current type
     * of Excel file-format that this CellStyle is applied to.
     *
     * @param rotation degrees (see note above)
     */
    void setRotation(short rotation);

    /**
     * get the degree of rotation for the text in the cell.
     *
     * Note: HSSF uses values from -90 to 90 degrees, whereas XSSF 
     * uses values from 0 to 180 degrees. The implementations of this method will map between these two value-ranges 
     * value-range as used by the type of Excel file-format that this CellStyle is applied to.
     *
     * @return rotation degrees (see note above)
     */
    short getRotation();

    /**
     * set the number of spaces to indent the text in the cell
     * @param indent - number of spaces
     */
    void setIndention(short indent);

    /**
     * get the number of spaces to indent the text in the cell
     * @return indent - number of spaces
     */
    short getIndention();

    /**
     * set the type of border to use for the left border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderLeft(BorderStyle border);

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @since POI 4.0.0
     */
    BorderStyle getBorderLeft();

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @since POI 3.15
     * @deprecated use <code>getBorderLeft()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderLeftEnum();

    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderRight(BorderStyle border);

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @since POI 4.0.0
     */
    BorderStyle getBorderRight();

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @since POI 3.15
     * @deprecated use <code>getBorderRight()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderRightEnum();

    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderTop(BorderStyle border);

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @since POI 4.0.0
     */
    BorderStyle getBorderTop();

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @since POI 3.15
     * @deprecated use <code>getBorderTop()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderTopEnum();

    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderBottom(BorderStyle border);

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @since POI 4.0.0
     */
    BorderStyle getBorderBottom();

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @since POI 3.15
     * @deprecated use <code>getBorderBottom()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    BorderStyle getBorderBottomEnum();

    /**
     * set the color to use for the left border
     * @param color The index of the color definition
     */
    void setLeftBorderColor(short color);

    /**
     * get the color to use for the left border
     */
    short getLeftBorderColor();

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    void setRightBorderColor(short color);

    /**
     * get the color to use for the left border
     * @return the index of the color definition
     */
    short getRightBorderColor();

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    void setTopBorderColor(short color);

    /**
     * get the color to use for the top border
     * @return the index of the color definition
     */
    short getTopBorderColor();

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    void setBottomBorderColor(short color);

    /**
     * get the color to use for the left border
     * @return the index of the color definition
     */
    short getBottomBorderColor();

    /**
     * setting to one fills the cell with the foreground color... No idea about
     * other values
     *
     * @param fp  fill pattern (set to {@link FillPatternType#SOLID_FOREGROUND} to fill w/foreground color)
     * @since POI 3.15 beta 3
     */
    void setFillPattern(FillPatternType fp);

    /**
     * Get the fill pattern
     *
     * @return the fill pattern, default value is {@link FillPatternType#NO_FILL}
     * @since POI 4.0.0
     */
    FillPatternType getFillPattern();

    /**
     * Get the fill pattern
     *
     * @return the fill pattern, default value is {@link FillPatternType#NO_FILL}
     * @since POI 3.15 beta 3
     * @deprecated use <code>getFillPattern()</code> instead
     */
    @Removal(version = "4.2")
    @Deprecated
    FillPatternType getFillPatternEnum();

    /**
     * set the background fill color.
     *
     * @param bg  color
     */
    void setFillBackgroundColor(short bg);

    /**
     * get the background fill color, if the fill
     *  is defined with an indexed color.
     * @return fill color index, or 0 if not indexed (XSSF only)
     */
    short getFillBackgroundColor();
    
    /**
     * Gets the color object representing the current
     *  background fill, resolving indexes using
     *  the supplied workbook.
     * This will work for both indexed and rgb
     *  defined colors. 
     */
    Color getFillBackgroundColorColor();

    /**
     * set the foreground fill color
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param bg  color
     */
    void setFillForegroundColor(short bg);

    /**
     * get the foreground fill color, if the fill  
     *  is defined with an indexed color.
     * @return fill color, or 0 if not indexed (XSSF only)
     */
    short getFillForegroundColor();
    
    /**
     * Gets the color object representing the current
     *  foreground fill, resolving indexes using
     *  the supplied workbook.
     * This will work for both indexed and rgb
     *  defined colors. 
     */
    Color getFillForegroundColorColor();

    /**
     * Clones all the style information from another
     *  CellStyle, onto this one. This 
     *  CellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this CellStyle will be lost! 
     *  
     * The source CellStyle could be from another
     *  Workbook if you like. This allows you to
     *  copy styles from one Workbook to another.
     *
     * However, both of the CellStyles will need
     *  to be of the same type (HSSFCellStyle or
     *  XSSFCellStyle)
     */
    void cloneStyleFrom(CellStyle source);
    
    /**
     * Controls if the Cell should be auto-sized
     *  to shrink to fit if the text is too long
     */
    void setShrinkToFit(boolean shrinkToFit);

    /**
     * Should the Cell be auto-sized by Excel to shrink
     *  it to fit if this text is too long?
     */
    boolean getShrinkToFit();
}
