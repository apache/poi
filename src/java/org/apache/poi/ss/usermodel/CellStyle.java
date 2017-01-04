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
     * general (normal) horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#GENERAL} instead.
     */
    @Removal(version="3.17")
    short ALIGN_GENERAL = 0x0; //HorizontalAlignment.GENERAL.getCode();

    /**
     * left-justified horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#LEFT} instead.
     */
    @Removal(version="3.17")
    short ALIGN_LEFT = 0x1; //HorizontalAlignment.LEFT.getCode();

    /**
     * center horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#CENTER} instead.
     */
    @Removal(version="3.17")
    short ALIGN_CENTER = 0x2; //HorizontalAlignment.CENTER.getCode();

    /**
     * right-justified horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#RIGHT} instead.
     */
    @Removal(version="3.17")
    short ALIGN_RIGHT = 0x3; //HorizontalAlignment.RIGHT.getCode();

    /**
     * fill? horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#FILL} instead.
     */
    @Removal(version="3.17")
    short ALIGN_FILL = 0x4; //HorizontalAlignment.FILL.getCode();

    /**
     * justified horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#JUSTIFY} instead.
     */
    @Removal(version="3.17")
    short ALIGN_JUSTIFY = 0x5; //HorizontalAlignment.JUSTIFY.getCode();

    /**
     * center-selection? horizontal alignment
     * @deprecated POI 3.15 beta 3. Use {@link HorizontalAlignment#CENTER_SELECTION} instead.
     */
    @Removal(version="3.17")
    short ALIGN_CENTER_SELECTION = 0x6; //HorizontalAlignment.CENTER_SELECTION.getCode();

    /**
     * top-aligned vertical alignment
     * @deprecated POI 3.15 beta 3. Use {@link VerticalAlignment#TOP} instead.
     */
    @Removal(version="3.17")
    short VERTICAL_TOP = 0x0; //VerticalAlignment.TOP.getCode();

    /**
     * center-aligned vertical alignment
     * @deprecated POI 3.15 beta 3. Use {@link VerticalAlignment#CENTER} instead.
     */
    @Removal(version="3.17")
    short VERTICAL_CENTER = 0x1; //VerticalAlignment.CENTER.getCode();

    /**
     * bottom-aligned vertical alignment
     * @deprecated POI 3.15 beta 3. Use {@link VerticalAlignment#BOTTOM} instead.
     */
    @Removal(version="3.17")
    short VERTICAL_BOTTOM = 0x2; //VerticalAlignment.BOTTOM.getCode();

    /**
     * vertically justified vertical alignment
     * @deprecated POI 3.15 beta 3. Use {@link VerticalAlignment#JUSTIFY} instead.
     */
    @Removal(version="3.17")
    short VERTICAL_JUSTIFY = 0x3; //VerticalAlignment.JUSTIFY.getCode();

    /**
     * No border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#NONE} instead.
     */
    @Removal(version="3.17")
    short BORDER_NONE = 0x0; //BorderStyle.NONE.getCode();

    /**
     * Thin border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#THIN} instead.
     */
    @Removal(version="3.17")
    short BORDER_THIN = 0x1; //BorderStyle.THIN.getCode();

    /**
     * Medium border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM} instead.
     */
    @Removal(version="3.17")
    short BORDER_MEDIUM = 0x2; //BorderStyle.MEDIUM.getCode();

    /**
     * dash border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASHED} instead.
     */
    @Removal(version="3.17")
    short BORDER_DASHED = 0x3; //BorderStyle.DASHED.getCode();

    /**
     * dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DOTTED} instead.
     */
    @Removal(version="3.17")
    short BORDER_DOTTED = 0x4; //BorderStyle.DOTTED.getCode();

    /**
     * Thick border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#THICK} instead.
     */
    @Removal(version="3.17")
    short BORDER_THICK = 0x5; //BorderStyle.THICK.getCode();

    /**
     * double-line border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DOUBLE} instead.
     */
    @Removal(version="3.17")
    short BORDER_DOUBLE = 0x6; //BorderStyle.DOUBLE.getCode();

    /**
     * hair-line border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#HAIR} instead.
     */
    @Removal(version="3.17")
    short BORDER_HAIR = 0x7; //BorderStyle.HAIR.getCode();

    /**
     * Medium dashed border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASHED} instead.
     */
    @Removal(version="3.17")
    short BORDER_MEDIUM_DASHED = 0x8; //BorderStyle.MEDIUM_DASHED.getCode();

    /**
     * dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASH_DOT} instead.
     */
    @Removal(version="3.17")
    short BORDER_DASH_DOT = 0x9; //BorderStyle.DASH_DOT.getCode();

    /**
     * medium dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASH_DOT} instead.
     */
    @Removal(version="3.17")
    short BORDER_MEDIUM_DASH_DOT = 0xA; //BorderStyle.MEDIUM_DASH_DOT.getCode();

    /**
     * dash-dot-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#DASH_DOT_DOT} instead.
     */
    @Removal(version="3.17")
    short BORDER_DASH_DOT_DOT = 0xB; //BorderStyle.DASH_DOT_DOT.getCode();

    /**
     * medium dash-dot-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#MEDIUM_DASH_DOT_DOT} instead.
     */
    @Removal(version="3.17")
    short BORDER_MEDIUM_DASH_DOT_DOT = 0xC; //BorderStyle.MEDIUM_DASH_DOT_DOT.getCode();

    /**
     * slanted dash-dot border
     * @deprecated 3.15 beta 2. Use {@link BorderStyle#SLANTED_DASH_DOT} instead.
     */
    @Removal(version="3.17")
    short BORDER_SLANTED_DASH_DOT = 0xD; //BorderStyle.SLANTED_DASH_DOT.getCode();

    /** 
     * Fill Pattern: No background
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#NO_FILL} instead.
     */
    @Removal(version="3.17")
    short NO_FILL = 0; //FillPatternType.NO_FILL.getCode();

    /**
     * Fill Pattern: Solidly filled
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SOLID_FOREGROUND} instead.
     */
    @Removal(version="3.17")
    short SOLID_FOREGROUND = 1; //FillPatternType.SOLID_FOREGROUND.getCode();

    /**
     * Fill Pattern: Small fine dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#FINE_DOTS} instead.
     */
    @Removal(version="3.17")
    short FINE_DOTS = 2; //FillPatternType.FINE_DOTS.getCode();

    /**
     * Fill Pattern: Wide dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#ALT_BARS} instead.
     */
    @Removal(version="3.17")
    short ALT_BARS = 3; //FillPatternType.ALT_BARS.getCode();

    /**
     * Fill Pattern: Sparse dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SPARSE_DOTS} instead.
     */
    @Removal(version="3.17")
    short SPARSE_DOTS = 4; //FillPatternType.SPARSE_DOTS.getCode();

    /**
     * Fill Pattern: Thick horizontal bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_HORZ_BANDS} instead.
     */
    @Removal(version="3.17")
    short THICK_HORZ_BANDS = 5; //FillPatternType.THICK_HORZ_BANDS.getCode();

    /**
     * Fill Pattern: Thick vertical bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_VERT_BANDS} instead.
     */
    @Removal(version="3.17")
    short THICK_VERT_BANDS = 6; //FillPatternType.THICK_VERT_BANDS.getCode();

    /**
     * Fill Pattern: Thick backward facing diagonals
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_BACKWARD_DIAG} instead.
     */
    @Removal(version="3.17")
    short THICK_BACKWARD_DIAG = 7; //FillPatternType.THICK_BACKWARD_DIAG.getCode();

    /**
     * Fill Pattern: Thick forward facing diagonals
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THICK_FORWARD_DIAG} instead.
     */
    @Removal(version="3.17")
    short THICK_FORWARD_DIAG = 8; //FillPatternType.THICK_FORWARD_DIAG.getCode();

    /**
     * Fill Pattern: Large spots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#BIG_SPOTS} instead.
     */
    @Removal(version="3.17")
    short BIG_SPOTS = 9; //FillPatternType.BIG_SPOTS.getCode();

    /**
     * Fill Pattern: Brick-like layout
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#BRICKS} instead.
     */
    @Removal(version="3.17")
    short BRICKS = 10; //FillPatternType.BRICKS.getCode();

    /**
     * Fill Pattern: Thin horizontal bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_HORZ_BANDS} instead.
     */
    @Removal(version="3.17")
    short THIN_HORZ_BANDS = 11; //FillPatternType.THIN_HORZ_BANDS.getCode();

    /**
     * Fill Pattern: Thin vertical bands
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_VERT_BANDS} instead.
     */
    @Removal(version="3.17")
    short THIN_VERT_BANDS = 12; //FillPatternType.THIN_VERT_BANDS.getCode();

    /**
     * Fill Pattern: Thin backward diagonal
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_BACKWARD_DIAG} instead.
     */
    @Removal(version="3.17")
    short THIN_BACKWARD_DIAG = 13; //FillPatternType.THIN_BACKWARD_DIAG.getCode();

    /**
     * Fill Pattern: Thin forward diagonal
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#THIN_FORWARD_DIAG} instead.
     */
    @Removal(version="3.17")
    short THIN_FORWARD_DIAG = 14; //FillPatternType.THIN_FORWARD_DIAG.getCode();

    /**
     * Fill Pattern: Squares
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#SQUARES} instead.
     */
    @Removal(version="3.17")
    short SQUARES = 15; //FillPatternType.SQUARES.getCode();

    /**
     * Fill Pattern: Diamonds
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#DIAMONDS} instead.
     */
    @Removal(version="3.17")
    short DIAMONDS = 16; //FillPatternType.DIAMONDS.getCode();

    /**
     * Fill Pattern: Less Dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#LESS_DOTS} instead.
     */
    @Removal(version="3.17")
    short LESS_DOTS = 17; //FillPatternType.LESS_DOTS.getCode();

    /**
     * Fill Pattern: Least Dots
     * @deprecated 3.15 beta 3. Use {@link FillPatternType#LEAST_DOTS} instead.
     */
    @Removal(version="3.17")
    short LEAST_DOTS = 18; //FillPatternType.LEAST_DOTS.getCode();

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
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     * @deprecated POI 3.15 beta 3. Use {@link #setAlignment(HorizontalAlignment)} instead.
     */
    void setAlignment(short align);
    /**
     * set the type of horizontal alignment for the cell
     * @param align - the type of alignment
     */
    void setAlignment(HorizontalAlignment align);

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
     * @deprecated POI 3.15 beta 3. Use {@link #getAlignmentEnum()} instead.
     */
    short getAlignment();
    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     */
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
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     * @deprecated POI 3.15 beta 3. Use {@link #setVerticalAlignment(VerticalAlignment)} instead.
     */
    void setVerticalAlignment(short align);
    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     */
    void setVerticalAlignment(VerticalAlignment align);

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     * @deprecated POI 3.15 beta 3. Use {@link #getVerticalAlignmentEnum()} instead.
     */
    short getVerticalAlignment();
    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     */
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
    @Removal(version="3.17")
    void setBorderLeft(short border);
    
    /**
     * set the type of border to use for the left border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderLeft(BorderStyle border);

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @deprecated POI 3.15. Use {@link #getBorderLeftEnum()} instead.
     * This will return a BorderStyle enum in the future.
     */
    short getBorderLeft();
    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @since POI 3.15
     */
    BorderStyle getBorderLeftEnum();

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
    @Removal(version="3.17")
    void setBorderRight(short border);
    
    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderRight(BorderStyle border);

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @deprecated POI 3.15. Use {@link #getBorderRightEnum()} instead.
     * This will return a BorderStyle enum in the future.
     */
    short getBorderRight();
    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @since POI 3.15
     */
    BorderStyle getBorderRightEnum();

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
    @Removal(version="3.17")
    void setBorderTop(short border);
    
    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderTop(BorderStyle border);

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @deprecated POI 3.15. Use {@link #getBorderTopEnum()} instead.
     * This will return a BorderStyle enum in the future.
     */
    short getBorderTop();
    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @since POI 3.15
     */
    BorderStyle getBorderTopEnum();

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
    @Removal(version="3.17")
    void setBorderBottom(short border);
    
    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     * @since POI 3.15
     */
    void setBorderBottom(BorderStyle border);

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @deprecated POI 3.15. Use {@link #getBorderBottomEnum()} instead.
     * This will return a BorderStyle enum in the future.
     */
    short getBorderBottom();
    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @since POI 3.15
     */
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
