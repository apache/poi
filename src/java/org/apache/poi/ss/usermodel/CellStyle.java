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

public interface CellStyle {

    /**
     * general (normal) horizontal alignment
     */

    short ALIGN_GENERAL = 0x0;

    /**
     * left-justified horizontal alignment
     */

    short ALIGN_LEFT = 0x1;

    /**
     * center horizontal alignment
     */

    short ALIGN_CENTER = 0x2;

    /**
     * right-justified horizontal alignment
     */

    short ALIGN_RIGHT = 0x3;

    /**
     * fill? horizontal alignment
     */

    short ALIGN_FILL = 0x4;

    /**
     * justified horizontal alignment
     */

    short ALIGN_JUSTIFY = 0x5;

    /**
     * center-selection? horizontal alignment
     */

    short ALIGN_CENTER_SELECTION = 0x6;

    /**
     * top-aligned vertical alignment
     */

    short VERTICAL_TOP = 0x0;

    /**
     * center-aligned vertical alignment
     */

    short VERTICAL_CENTER = 0x1;

    /**
     * bottom-aligned vertical alignment
     */

    short VERTICAL_BOTTOM = 0x2;

    /**
     * vertically justified vertical alignment
     */

    short VERTICAL_JUSTIFY = 0x3;

    /**
     * No border
     */

    short BORDER_NONE = 0x0;

    /**
     * Thin border
     */

    short BORDER_THIN = 0x1;

    /**
     * Medium border
     */

    short BORDER_MEDIUM = 0x2;

    /**
     * dash border
     */

    short BORDER_DASHED = 0x3;

    /**
     * dot border
     */

    short BORDER_HAIR = 0x7;

    /**
     * Thick border
     */

    short BORDER_THICK = 0x5;

    /**
     * double-line border
     */

    short BORDER_DOUBLE = 0x6;

    /**
     * hair-line border
     */

    short BORDER_DOTTED = 0x4;

    /**
     * Medium dashed border
     */

    short BORDER_MEDIUM_DASHED = 0x8;

    /**
     * dash-dot border
     */

    short BORDER_DASH_DOT = 0x9;

    /**
     * medium dash-dot border
     */

    short BORDER_MEDIUM_DASH_DOT = 0xA;

    /**
     * dash-dot-dot border
     */

    short BORDER_DASH_DOT_DOT = 0xB;

    /**
     * medium dash-dot-dot border
     */

    short BORDER_MEDIUM_DASH_DOT_DOT = 0xC;

    /**
     * slanted dash-dot border
     */

    short BORDER_SLANTED_DASH_DOT = 0xD;

    /**  No background */
    short NO_FILL = 0;

    /**  Solidly filled */
    short SOLID_FOREGROUND = 1;

    /**  Small fine dots */
    short FINE_DOTS = 2;

    /**  Wide dots */
    short ALT_BARS = 3;

    /**  Sparse dots */
    short SPARSE_DOTS = 4;

    /**  Thick horizontal bands */
    short THICK_HORZ_BANDS = 5;

    /**  Thick vertical bands */
    short THICK_VERT_BANDS = 6;

    /**  Thick backward facing diagonals */
    short THICK_BACKWARD_DIAG = 7;

    /**  Thick forward facing diagonals */
    short THICK_FORWARD_DIAG = 8;

    /**  Large spots */
    short BIG_SPOTS = 9;

    /**  Brick-like layout */
    short BRICKS = 10;

    /**  Thin horizontal bands */
    short THIN_HORZ_BANDS = 11;

    /**  Thin vertical bands */
    short THIN_VERT_BANDS = 12;

    /**  Thin backward diagonal */
    short THIN_BACKWARD_DIAG = 13;

    /**  Thin forward diagonal */
    short THIN_FORWARD_DIAG = 14;

    /**  Squares */
    short SQUARES = 15;

    /**  Diamonds */
    short DIAMONDS = 16;

    /**  Less Dots */
    short LESS_DOTS = 17;

    /**  Least Dots */
    short LEAST_DOTS = 18;

    /**
     * get the index within the Workbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */

    short getIndex();

    /**
     * set the data format (must be a valid format)
     * @see DataFormat
     */

    void setDataFormat(short fmt);

    /**
     * get the index of the format
     * @see DataFormat
     */
    short getDataFormat();

    /**
     * Get the format string
     */
    public String getDataFormatString();

    /**
     * set the font for this style
     * @param font  a font object created or retrieved from the Workbook object
     * @see Workbook#createFont()
     * @see Workbook#getFontAt(short)
     */

    void setFont(Font font);

    /**
     * gets the index of the font for this style
     * @see Workbook#getFontAt(short)
     */
    short getFontIndex();

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
     * set the type of horizontal alignment for the cell
     * @param align - the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */

    void setAlignment(short align);

    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */

    short getAlignment();

    /**
     * Set whether the text should be wrapped.
     * Setting this flag to <code>true</code> make all content visible
     * whithin a cell by displaying it on multiple lines
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
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    void setVerticalAlignment(short align);

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    short getVerticalAlignment();

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
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    void setBorderLeft(short border);

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    short getBorderLeft();

    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    void setBorderRight(short border);

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    short getBorderRight();

    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    void setBorderTop(short border);

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    short getBorderTop();

    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */

    void setBorderBottom(short border);

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    short getBorderBottom();

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
     * @return hhe index of the color definition
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
     * @see #NO_FILL
     * @see #SOLID_FOREGROUND
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @param fp  fill pattern (set to 1 to fill w/foreground color)
     */
    void setFillPattern(short fp);

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     */

    short getFillPattern();

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
    public void cloneStyleFrom(CellStyle source);
    
    /**
     * Controls if the Cell should be auto-sized
     *  to shrink to fit if the text is too long
     */
    public void setShrinkToFit(boolean shrinkToFit);

    /**
     * Should the Cell be auto-sized by Excel to shrink
     *  it to fit if this text is too long?
     */
    public boolean getShrinkToFit();
}