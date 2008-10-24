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

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * High level representation of the style of a cell in a sheet of a workbook.
 *
 * @version 1.0-pre
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCell#setCellStyle(HSSFCellStyle)
 */

public class HSSFCellStyle
{
    private ExtendedFormatRecord format                     = null;
    private short                index                      = 0;
    private Workbook             workbook                   = null;

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

    public final static short    BORDER_HAIR              = 0x4;

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

    public final static short    BORDER_DOTTED                = 0x7;

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
    /**  Less Dots */
    public final static short     LESS_DOTS           = 17 ;
    /**  Least Dots */
    public final static short     LEAST_DOTS          = 18 ;


    /** Creates new HSSFCellStyle why would you want to do this?? */
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, HSSFWorkbook workbook)
    {
    	this(index, rec, workbook.getWorkbook());
    }
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, Workbook workbook)
    {
        this.workbook = workbook;
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
     * Return the parent style for this cell style.
     * In most cases this will be null, but in a few
     *  cases there'll be a fully defined parent.
     */
    public HSSFCellStyle getParentStyle() {
    	if(format.getParentIndex() == 0) {
    		return null;
    	}
    	return new HSSFCellStyle(
    			format.getParentIndex(),
    			workbook.getExFormatAt(format.getParentIndex()),
    			workbook
    	);
    }

    /**
     * set the data format (must be a valid format)
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */

    public void setDataFormat(short fmt)
    {
        format.setFormatIndex(fmt);
    }

    /**
     * get the index of the format
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */

    public short getDataFormat()
    {
        return format.getFormatIndex();
    }
    
    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the bound workbook
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     * @return the format string or "General" if not found
     */
    public String getDataFormatString() {
        return getDataFormatString(workbook);
    }
    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the supplied workbook
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     *
     * @return the format string or "General" if not found
     */
    public String getDataFormatString(Workbook workbook) {
    	HSSFDataFormat format = new HSSFDataFormat(workbook);
    	
        int idx = getDataFormat();
        return idx == -1 ? "General" : format.getFormat(getDataFormat());
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
        short fontindex = ((HSSFFont) font).getIndex();
        format.setFontIndex(fontindex);
    }

    /**
     * gets the index of the font for this style
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public short getFontIndex()
    {
        return format.getFontIndex();
    }
    
    /**
     * gets the font for this style
     * @param parentWorkbook The HSSFWorkbook that this style belongs to
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle#getFontIndex()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public HSSFFont getFont(HSSFWorkbook parentWorkbook) {
    	return ((HSSFWorkbook) parentWorkbook).getFontAt(getFontIndex());
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
     * @param rotation degrees (between -90 and 90 degrees)
     */

    public void setRotation(short rotation)
    {
      if ((rotation < 0)&&(rotation >= -90)) {
        //Take care of the funny 4th quadrant issue
        //The 4th quadrant (-1 to -90) is stored as (91 to 180)
        rotation = (short)(90 - rotation);
      }
      else if ((rotation < -90)  ||(rotation > 90))
        //Do not allow an incorrect rotation to be set
        throw new IllegalArgumentException("The rotation must be between -90 and 90 degrees");
        format.setRotation(rotation);
    }

    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees (between -90 and 90 degrees)
     */

    public short getRotation()
    {
      short rotation = format.getRotation();
      if (rotation > 90)
        //This is actually the 4th quadrant
        rotation = (short)(90-rotation);
      return rotation;
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
     * @param color The index of the color definition
     */
    public void setLeftBorderColor(short color)
    {
        format.setLeftBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @param color The index of the color definition
     */
    public short getLeftBorderColor()
    {
        return format.getLeftBorderPaletteIdx();
    }

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    public void setRightBorderColor(short color)
    {
        format.setRightBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @param color The index of the color definition
     */
    public short getRightBorderColor()
    {
        return format.getRightBorderPaletteIdx();
    }

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    public void setTopBorderColor(short color)
    {
        format.setTopBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the top border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @param color The index of the color definition
     */
    public short getTopBorderColor()
    {
        return format.getTopBorderPaletteIdx();
    }

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    public void setBottomBorderColor(short color)
    {
        format.setBottomBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @param color The index of the color definition
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
     * Checks if the background and foreground fills are set correctly when one
     * or the other is set to the default color.
     * <p>Works like the logic table below:</p>
     * <p>BACKGROUND   FOREGROUND</p>
     * <p>NONE         AUTOMATIC</p>
     * <p>0x41         0x40</p>
     * <p>NONE         RED/ANYTHING</p>
     * <p>0x40         0xSOMETHING</p> 
     */
    private void checkDefaultBackgroundFills() {
      if (format.getFillForeground() == org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index) {
    	  //JMH: Why +1, hell why not. I guess it made some sense to someone at the time. Doesnt
    	  //to me now.... But experience has shown that when the fore is set to AUTOMATIC then the
    	  //background needs to be incremented......
    	  if (format.getFillBackground() != (org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index+1))
    		  setFillBackgroundColor((short)(org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index+1));
      } else if (format.getFillBackground() == org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index+1)
    	  //Now if the forground changes to a non-AUTOMATIC color the background resets itself!!!
    	  if (format.getFillForeground() != org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index)
    		  setFillBackgroundColor(org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index);
    }

    /**
     * set the background fill color.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex()); 
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex()); 
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new HSSFColor.RED().getIndex());
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param bg  color
     */

    public void setFillBackgroundColor(short bg)
    {    	
        format.setFillBackground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Get the background fill color.
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillBackgroundColor()
    {
    	short result = format.getFillBackground();
    	//JMH: Do this ridiculous conversion, and let HSSFCellStyle
    	//internally migrate back and forth
    	if (result == (HSSFColor.AUTOMATIC.index+1))
    		return HSSFColor.AUTOMATIC.index;
    	else return result;
    }

    /**
     * set the foreground fill color
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param bg  color
     */
    public void setFillForegroundColor(short bg)
    {
        format.setFillForeground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Get the foreground fill color.
     * Many cells are filled with this, instead of a 
     *  background color ({@link #getFillBackgroundColor()})
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillForegroundColor()
    {
        return format.getFillForeground();
    }
    
    /**
     * Gets the name of the user defined style.
     * Returns null for built in styles, and
     *  styles where no name has been defined
     */
    public String getUserStyleName() {
    	StyleRecord sr = workbook.getStyleRecord(index);
    	if(sr == null) {
    		return null;
    	}
    	if(sr.isBuiltin()) {
    		return null;
    	}
    	return sr.getName();
    }
    
    /**
     * Sets the name of the user defined style.
     * Will complain if you try this on a built in style.
     */
    public void setUserStyleName(String styleName) {
    	StyleRecord sr = workbook.getStyleRecord(index);
    	if(sr == null) {
    		sr = workbook.createStyleRecord(index);
    	}
    	if(sr.isBuiltin()) {
    		throw new IllegalArgumentException("Unable to set user specified style names for built in styles!");
    	}
    	sr.setName(styleName);
    }

    /**
     * Verifies that this style belongs to the supplied Workbook.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToWorkbook(HSSFWorkbook wb) {
		if(wb.getWorkbook() != workbook) {
			throw new IllegalArgumentException("This Style does not belong to the supplied Workbook. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
		}
	}
    
    /**
     * Clones all the style information from another
     *  HSSFCellStyle, onto this one. This 
     *  HSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this HSSFCellStyle will be lost! 
     *  
     * The source HSSFCellStyle could be from another
     *  HSSFWorkbook if you like. This allows you to
     *  copy styles from one HSSFWorkbook to another.
     */
    public void cloneStyleFrom(HSSFCellStyle source) {
    	// First we need to clone the extended format
    	//  record
    	format.cloneStyleFrom(source.format);
    	
    	// Handle matching things if we cross workbooks
    	if(workbook != source.workbook) {
			// Then we need to clone the format string,
			//  and update the format record for this
    		short fmt = workbook.createFormat(
    				source.getDataFormatString()
    		);
    		setDataFormat(fmt);
			
			// Finally we need to clone the font,
			//  and update the format record for this
    		FontRecord fr = workbook.createNewFont();
    		fr.cloneStyleFrom(
    				source.workbook.getFontRecordAt(
    						source.getFontIndex()
    				)
    		);
    		
    		HSSFFont font = new HSSFFont(
    				(short)workbook.getFontIndex(fr), fr
    		);
    		setFont(font);
    	}    	
    }

    
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + index;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof HSSFCellStyle) {
			final HSSFCellStyle other = (HSSFCellStyle) obj;
			if (format == null) {
				if (other.format != null)
					return false;
			} else if (!format.equals(other.format))
				return false;
			if (index != other.index)
				return false;
			return true;
		}
		return false;
	}
}
