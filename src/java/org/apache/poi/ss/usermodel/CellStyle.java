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
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#NONE} instead.
     */
    short BORDER_NONE = 0x0;

    /**
     * Thin border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#THIN} instead.
     */
    short BORDER_THIN = 0x1;

    /**
     * Medium border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM} instead.
     */
    short BORDER_MEDIUM = 0x2;

    /**
     * dash border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASHED} instead.
     */
    short BORDER_DASHED = 0x3;

    /**
     * dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DOTTED} instead.
     */
    short BORDER_DOTTED = 0x4;

    /**
     * Thick border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#THICK} instead.
     */
    short BORDER_THICK = 0x5;

    /**
     * double-line border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DOUBLE} instead.
     */
    short BORDER_DOUBLE = 0x6;

    /**
     * hair-line border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#HAIR} instead.
     */
    short BORDER_HAIR = 0x7;

    /**
     * Medium dashed border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASHED} instead.
     */
    short BORDER_MEDIUM_DASHED = 0x8;

    /**
     * dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASH_DOT} instead.
     */
    short BORDER_DASH_DOT = 0x9;

    /**
     * medium dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASH_DOT} instead.
     */
    short BORDER_MEDIUM_DASH_DOT = 0xA;

    /**
     * dash-dot-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASH_DOT_DOT} instead.
     */
    short BORDER_DASH_DOT_DOT = 0xB;

    /**
     * medium dash-dot-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASH_DOT_DOT} instead.
     */
    short BORDER_MEDIUM_DASH_DOT_DOT = 0xC;

    /**
     * slanted dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#SLANTED_DASH_DOT} instead.
     */
    short BORDER_SLANTED_DASH_DOT = 0xD;

    /** 
     * Fill Pattern: No background
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#NO_FILL} instead.
     */
    short NO_FILL = FillPatternType.NO_FILL.getCode();

    /**
     * Fill Pattern: Solidly filled
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SOLID_FOREGROUND} instead.
     */
    short SOLID_FOREGROUND = FillPatternType.SOLID_FOREGROUND.getCode();

    /**
     * Fill Pattern: Small fine dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#FINE_DOTS} instead.
     */
    short FINE_DOTS = FillPatternType.FINE_DOTS.getCode();

    /**
     * Fill Pattern: Wide dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#ALT_BARS} instead.
     */
    short ALT_BARS = FillPatternType.ALT_BARS.getCode();

    /**
     * Fill Pattern: Sparse dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SPARSE_DOTS} instead.
     */
    short SPARSE_DOTS = FillPatternType.SPARSE_DOTS.getCode();

    /**
     * Fill Pattern: Thick horizontal bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_HORZ_BANDS} instead.
     */
    short THICK_HORZ_BANDS = FillPatternType.THICK_HORZ_BANDS.getCode();

    /**
     * Fill Pattern: Thick vertical bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_VERT_BANDS} instead.
     */
    short THICK_VERT_BANDS = FillPatternType.THICK_VERT_BANDS.getCode();

    /**
     * Fill Pattern: Thick backward facing diagonals
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#NO_FILL} instead.
     */
    short THICK_BACKWARD_DIAG = 7;

    /**
     * Fill Pattern: Thick forward facing diagonals
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#NO_FILL} instead.
     */
    short THICK_FORWARD_DIAG = FillPatternType.THICK_FORWARD_DIAG.getCode();

    /**
     * Fill Pattern: Large spots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#BIG_SPOTS} instead.
     */
    short BIG_SPOTS = FillPatternType.BIG_SPOTS.getCode();

    /**
     * Fill Pattern: Brick-like layout
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#BRICKS} instead.
     */
    short BRICKS = FillPatternType.BRICKS.getCode();

    /**
     * Fill Pattern: Thin horizontal bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_HORZ_BANDS} instead.
     */
    short THIN_HORZ_BANDS = FillPatternType.THIN_HORZ_BANDS.getCode();

    /**
     * Fill Pattern: Thin vertical bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_VERT_BANDS} instead.
     */
    short THIN_VERT_BANDS = FillPatternType.THIN_VERT_BANDS.getCode();

    /**
     * Fill Pattern: Thin backward diagonal
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_BACKWARD_DIAG} instead.
     */
    short THIN_BACKWARD_DIAG = FillPatternType.THIN_BACKWARD_DIAG.getCode();

    /**
     * Fill Pattern: Thin forward diagonal
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_FORWARD_DIAG} instead.
     */
    short THIN_FORWARD_DIAG = FillPatternType.THIN_FORWARD_DIAG.getCode();

    /**
     * Fill Pattern: Squares
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SQUARES} instead.
     */
    short SQUARES = FillPatternType.SQUARES.getCode();

    /**
     * Fill Pattern: Diamonds
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#DIAMONDS} instead.
     */
    short DIAMONDS = FillPatternType.DIAMONDS.getCode();

    /**
     * Fill Pattern: Less Dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#LESS_DOTS} instead.
     */
    short LESS_DOTS = FillPatternType.LESS_DOTS.getCode();

    /**
     * Fill Pattern: Least Dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#LEAST_DOTS} instead.
     */
    short LEAST_DOTS = FillPatternType.LEAST_DOTS.getCode();

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
     * @deprecated 3.15 beta 2. Use {@link #setBorderLeft(BorderStyle)} instead
     */
    void setBorderLeft(short border);
    
    /**
     * set the type of border to use for the left border of the cell
     * @param border type
     */
    void setBorderLeft(BorderStyle border);

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     */
    BorderStyle getBorderLeft();

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
     * @deprecated 3.15 beta 2. Use {@link #setBorderRight(BorderStyle)} instead
     */
    void setBorderRight(short border);
    
    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     */
    void setBorderRight(BorderStyle border);

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     */
    BorderStyle getBorderRight();

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
     * @deprecated 3.15 beta 2. Use {@link #setBorderTop(BorderStyle)} instead
     */
    void setBorderTop(short border);
    
    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     */
    void setBorderTop(BorderStyle border);

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     */
    BorderStyle getBorderTop();

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
     * @deprecated 3.15 beta 2. Use {@link #setBorderBottom(BorderStyle)} instead.
     */
    void setBorderBottom(short border);
    
    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     */
    void setBorderBottom(BorderStyle border);

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     */
    BorderStyle getBorderBottom();

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
     * @deprecated POI 3.15 beta 3. Use {@link #setFillPattern(FillPatternType)} instead.
     */
    void setFillPattern(short fp);
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
     * @param fp  fill pattern (set to {@link FillPatternType#SOLID_FOREGROUND} to fill w/foreground color)
     * @since POI 3.15 beta 3
     */
    void setFillPattern(FillPatternType fp);

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     * @deprecated POI 3.15 beta 3. This method will return {@link FillPatternType} in the future. Use {@link #setFillPattern(FillPatternType)} instead.
     */
    short getFillPattern();
    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     * @since POI 3.15 beta 3
     */
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
