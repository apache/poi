
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * HSSFCellStyle.java
 *
 * Created on September 30, 2001, 3:47 PM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.ExtendedFormatRecord;

/**
 * High level representation of the style of a cell in a sheet of a workbook.
 *
 * @version 1.0-pre
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCell#setCellStyle(HSSFCellStyle)
 */

public class HSSFCellStyle
    implements HSSFColorConstants
{
    private ExtendedFormatRecord format                     = null;
    private short                index                      = 0;
    private short                fontindex                  = 0;

    /**
     * general (normal) horizontal alignment
     */

    public final static short    ALIGN_GENERAL              = 0x0;

    /**
     * left-justified horizontal alignment
     */

    public final static short    ALIGN_LEFT                 = 0x1;

    /**
     * center horizontal alignment
     */

    public final static short    ALIGN_CENTER               = 0x2;

    /**
     * right-justified horizontal alignment
     */

    public final static short    ALIGN_RIGHT                = 0x3;

    /**
     * fill? horizontal alignment
     */

    public final static short    ALIGN_FILL                 = 0x4;

    /**
     * justified horizontal alignment
     */

    public final static short    ALIGN_JUSTIFY              = 0x5;

    /**
     * center-selection? horizontal alignment
     */

    public final static short    ALIGN_CENTER_SELECTION     = 0x6;

    /**
     * top-aligned vertical alignment
     */

    public final static short    VERTICAL_TOP               = 0x0;

    /**
     * center-aligned vertical alignment
     */

    public final static short    VERTICAL_CENTER            = 0x1;

    /**
     * bottom-aligned vertical alignment
     */

    public final static short    VERTICAL_BOTTOM            = 0x2;

    /**
     * vertically justified vertical alignment
     */

    public final static short    VERTICAL_JUSTIFY           = 0x3;

    /**
     * No border
     */

    public final static short    BORDER_NONE                = 0x0;

    /**
     * Thin border
     */

    public final static short    BORDER_THIN                = 0x1;

    /**
     * Medium border
     */

    public final static short    BORDER_MEDIUM              = 0x2;

    /**
     * dash border
     */

    public final static short    BORDER_DASHED              = 0x3;

    /**
     * dot border
     */

    public final static short    BORDER_DOTTED              = 0x4;

    /**
     * Thick border
     */

    public final static short    BORDER_THICK               = 0x5;

    /**
     * double-line border
     */

    public final static short    BORDER_DOUBLE              = 0x6;

    /**
     * hair-line border
     */

    public final static short    BORDER_HAIR                = 0x7;

    /**
     * Medium dashed border
     */

    public final static short    BORDER_MEDIUM_DASHED       = 0x8;

    /**
     * dash-dot border
     */

    public final static short    BORDER_DASH_DOT            = 0x9;

    /**
     * medium dash-dot border
     */

    public final static short    BORDER_MEDIUM_DASH_DOT     = 0xA;

    /**
     * dash-dot-dot border
     */

    public final static short    BORDER_DASH_DOT_DOT        = 0xB;

    /**
     * medium dash-dot-dot border
     */

    public final static short    BORDER_MEDIUM_DASH_DOT_DOT = 0xC;

    /**
     * slanted dash-dot border
     */

    public final static short    BORDER_SLANTED_DASH_DOT    = 0xD;

    /**  No background */
    public final static short     NO_FILL             = 0  ;
    /**  Solidly filled */
    public final static short     SOLID_FOREGROUND    = 1  ;
    /**  Small fine dots */
    public final static short     FINE_DOTS           = 2  ;
    /**  Wide dots */
    public final static short     ALT_BARS            = 3  ;
    /**  Sparse dots */
    public final static short     SPARSE_DOTS         = 4  ;
    /**  Thick horizontal bands */
    public final static short     THICK_HORZ_BANDS    = 5  ;
    /**  Thick vertical bands */
    public final static short     THICK_VERT_BANDS    = 6  ;
    /**  Thick backward facing diagonals */
    public final static short     THICK_BACKWARD_DIAG = 7  ;
    /**  Thick forward facing diagonals */
    public final static short     THICK_FORWARD_DIAG  = 8  ;
    /**  Large spots */
    public final static short     BIG_SPOTS           = 9  ;
    /**  Brick-like layout */
    public final static short     BRICKS              = 10 ;
    /**  Thin horizontal bands */
    public final static short     THIN_HORZ_BANDS     = 11 ;
    /**  Thin vertical bands */
    public final static short     THIN_VERT_BANDS     = 12 ;
    /**  Thin backward diagonal */
    public final static short     THIN_BACKWARD_DIAG  = 13 ;
    /**  Thin forward diagonal */
    public final static short     THIN_FORWARD_DIAG   = 14 ;
    /**  Squares */
    public final static short     SQUARES             = 15 ;
    /**  Diamonds */
    public final static short     DIAMONDS            = 16 ;


    /** Creates new HSSFCellStyle why would you want to do this?? */

    protected HSSFCellStyle(short index, ExtendedFormatRecord rec)
    {
        this.index = index;
        format     = rec;
    }

    /**
     * get the index within the HSSFWorkbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */

    public short getIndex()
    {
        return index;
    }

    /**
     * set the data format (only builtin formats are supported)
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */

    public void setDataFormat(short fmt)
    {
        format.setFormatIndex(fmt);
    }

    /**
     * get the index of the built in format
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */

    public short getDataFormat()
    {
        return format.getFormatIndex();
    }

    /**
     * set the font for this style
     * @param font  a font object created or retreived from the HSSFWorkbook object
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createFont()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */

    public void setFont(HSSFFont font)
    {
        format.setIndentNotParentFont(true);
        fontindex = font.getIndex();
        format.setFontIndex(fontindex);
    }

    public short getFontIndex()
    {
        return format.getFontIndex();
    }

    /**
     * set the cell's using this style to be hidden
     * @param hidden - whether the cell using this style should be hidden
     */

    public void setHidden(boolean hidden)
    {
        format.setIndentNotParentCellOptions(true);
        format.setHidden(hidden);
    }

    /**
     * get whether the cell's using this style are to be hidden
     * @return hidden - whether the cell using this style should be hidden
     */

    public boolean getHidden()
    {
        return format.isHidden();
    }

    /**
     * set the cell's using this style to be locked
     * @param locked - whether the cell using this style should be locked
     */

    public void setLocked(boolean locked)
    {
        format.setIndentNotParentCellOptions(true);
        format.setLocked(locked);
    }

    /**
     * get whether the cell's using this style are to be locked
     * @return hidden - whether the cell using this style should be locked
     */

    public boolean getLocked()
    {
        return format.isLocked();
    }

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

    public void setAlignment(short align)
    {
        format.setIndentNotParentAlignment(true);
        format.setAlignment(align);
    }

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

    public short getAlignment()
    {
        return format.getAlignment();
    }

    /**
     * get whether this cell is to be part of a merged block of cells
     *
     * @returns merged or not
     */

//    public boolean getMergeCells()
//    {
//        return format.getMergeCells();
//    }

    /**
     * set whether this cell is to be part of a merged block of cells
     *
     * @param merge  merged or not
     */

//    public void setMergeCells(boolean merge)
//    {
//        format.setMergeCells(merge);
//    }

    /**
     * set whether the text should be wrapped
     * @param wrapped  wrap text or not
     */

    public void setWrapText(boolean wrapped)
    {
        format.setIndentNotParentAlignment(true);
        format.setWrapText(wrapped);
    }

    /**
     * get whether the text should be wrapped
     * @return wrap text or not
     */

    public boolean getWrapText()
    {
        return format.getWrapText();
    }

    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    public void setVerticalAlignment(short align)
    {
        format.setVerticalAlignment(align);
    }

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    public short getVerticalAlignment()
    {
        return format.getVerticalAlignment();
    }

    /**
     * set the degree of rotation for the text in the cell
     * @param rotation degrees
     */

    public void setRotation(short rotation)
    {
        format.setRotation(rotation);
    }

    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees
     */

    public short getRotation()
    {
        return format.getRotation();
    }

    /**
     * set the number of spaces to indent the text in the cell
     * @param indent - number of spaces
     */

    public void setIndention(short indent)
    {
        format.setIndent(indent);
    }

    /**
     * get the number of spaces to indent the text in the cell
     * @return indent - number of spaces
     */

    public short getIndention()
    {
        return format.getIndent();
    }

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

    public void setBorderLeft(short border)
    {
        format.setIndentNotParentBorder(true);
        format.setBorderLeft(border);
    }

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

    public short getBorderLeft()
    {
        return format.getBorderLeft();
    }

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

    public void setBorderRight(short border)
    {
        format.setIndentNotParentBorder(true);
        format.setBorderRight(border);
    }

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

    public short getBorderRight()
    {
        return format.getBorderRight();
    }

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

    public void setBorderTop(short border)
    {
        format.setIndentNotParentBorder(true);
        format.setBorderTop(border);
    }

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

    public short getBorderTop()
    {
        return format.getBorderTop();
    }

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

    public void setBorderBottom(short border)
    {
        format.setIndentNotParentBorder(true);
        format.setBorderBottom(border);
    }

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

    public short getBorderBottom()
    {
        return format.getBorderBottom();
    }

    /**
     * set the color to use for the left border
     * @param color
     */

    public void setLeftBorderColor(short color)
    {
        format.setLeftBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @return color
     */

    public short getLeftBorderColor()
    {
        return format.getLeftBorderPaletteIdx();
    }

    /**
     * set the color to use for the right border
     * @param color
     */

    public void setRightBorderColor(short color)
    {
        format.setRightBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @return color
     */

    public short getRightBorderColor()
    {
        return format.getRightBorderPaletteIdx();
    }

    /**
     * set the color to use for the top border
     * @param color
     */

    public void setTopBorderColor(short color)
    {
        format.setTopBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the top border
     * @return color
     */

    public short getTopBorderColor()
    {
        return format.getTopBorderPaletteIdx();
    }

    /**
     * set the color to use for the bottom border
     * @param color
     */

    public void setBottomBorderColor(short color)
    {
        format.setBottomBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @return color
     */

    public short getBottomBorderColor()
    {
        return format.getBottomBorderPaletteIdx();
    }

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
    public void setFillPattern(short fp)
    {
        format.setAdtlFillPattern(fp);
    }

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     */

    public short getFillPattern()
    {
        return format.getAdtlFillPattern();
    }

    /**
     * set the background fill color.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundColor(HSSFCellStyle.RED);
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.SOLID_FILL );
     * cs.setFillForgroundColor(HSSFSeCellStyle.RED);
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param bg  color
     */

    public void setFillBackgroundColor(short bg)
    {
        format.setFillBackground(bg);
    }

    /**
     * get the background fill color
     * @return fill color
     */

    public short getFillBackgroundColor()
    {
        return format.getFillBackground();
    }

    /**
     * set the foreground fill color
     * @param bg  color
     */

    public void setFillForegroundColor(short bg)
    {
        format.setFillForeground(bg);
    }

    /**
     * get the foreground fill color
     * @return fill color
     */

    public short getFillForegroundColor()
    {
        return format.getFillForeground();
    }

}
