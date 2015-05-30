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

import java.util.List;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.hssf.record.StyleRecord;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

/**
 * High level representation of the style of a cell in a sheet of a workbook.
 *
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
 * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
 * @see org.apache.poi.hssf.usermodel.HSSFCell#setCellStyle(HSSFCellStyle)
 */
public final class HSSFCellStyle implements CellStyle {

    private ExtendedFormatRecord format = null;
    private short index = 0;
    private InternalWorkbook workbook = null;

    /**
     * Creates a new HSSFCellStyle.
     * Why would you want to do this?
     */
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, HSSFWorkbook workbook) {
        this(index, rec, workbook.getWorkbook());
    }

    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, InternalWorkbook workbook) {
        this.workbook = workbook;
        this.index = index;
        this.format = rec;
    }

    /**
     * Gets the index within the HSSFWorkbook (sequence within the collection of ExtnededFormat objects).
     *
     * @return unique index number of the underlying record this style represents (probably you don't care
     * unless you're comparing which one is which)
     */
    public short getIndex() {
        return index;
    }

    /**
     * Return the parent style for this cell style.
     * In most cases this will be null, but in a few
     * cases there'll be a fully defined parent.
     */
    public HSSFCellStyle getParentStyle() {
        short parentIndex = format.getParentIndex();
        // parentIndex equal 0xFFF indicates no inheritance from a cell style XF (See 2.4.353 XF)
        if (parentIndex == 0 || parentIndex == 0xFFF) {
            return null;
        }
        return new HSSFCellStyle(parentIndex, workbook.getExFormatAt(parentIndex), workbook);
    }

    /**
     * Sets the data format (must be a valid format).
     *
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public void setDataFormat(short fmt) {
        format.setFormatIndex(fmt);
    }

    /**
     * Gets the index of the format.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public short getDataFormat() {
        return format.getFormatIndex();
    }

    // we keep the cached data in ThreadLocal members in order to
    // avoid multi-threading issues when different workbooks are accessed in 
    // multiple threads at the same time
    private static ThreadLocal<Short> lastDateFormat = new ThreadLocal<Short>() {
        protected Short initialValue() {
            return Short.MIN_VALUE;
        }
    };
    private static ThreadLocal<List<FormatRecord>> lastFormats = new ThreadLocal<List<FormatRecord>>();
    private static ThreadLocal<String> getDataFormatStringCache = new ThreadLocal<String>();

    /**
     * Gets the contents of the format string, by looking up
     * the DataFormat against the bound workbook.
     *
     * @return the format string or "General" if not found
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public String getDataFormatString() {
        if (getDataFormatStringCache.get() != null) {
            if (lastDateFormat.get() == getDataFormat() && workbook.getFormats().equals(lastFormats.get())) {
                return getDataFormatStringCache.get();
            }
        }

        lastFormats.set(workbook.getFormats());
        lastDateFormat.set(getDataFormat());

        getDataFormatStringCache.set(getDataFormatString(workbook));

        return getDataFormatStringCache.get();
    }

    /**
     * Gets the contents of the format string, by looking up
     * the DataFormat against the supplied workbook.
     *
     * @return the format string or "General" if not found
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public String getDataFormatString(org.apache.poi.ss.usermodel.Workbook workbook) {
        HSSFDataFormat format = new HSSFDataFormat(((HSSFWorkbook) workbook).getWorkbook());

        int idx = getDataFormat();
        return idx == -1 ? "General" : format.getFormat(getDataFormat());
    }

    /**
     * Gets the contents of the format string, by looking up
     * the DataFormat against the supplied low level workbook.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFDataFormat
     */
    public String getDataFormatString(org.apache.poi.hssf.model.InternalWorkbook workbook) {
        HSSFDataFormat format = new HSSFDataFormat(workbook);

        return format.getFormat(getDataFormat());
    }

    /**
     * Sets the font for this style.
     *
     * @param font a font object created or retreived from the HSSFWorkbook object
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createFont()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public void setFont(Font font) {
        setFont((HSSFFont) font);
    }

    public void setFont(HSSFFont font) {
        format.setIndentNotParentFont(true);
        short fontindex = font.getIndex();
        format.setFontIndex(fontindex);
    }

    /**
     * Gets the index of the font for this style.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public short getFontIndex() {
        return format.getFontIndex();
    }

    /**
     * Gets the font for this style.
     *
     * @param parentWorkbook The HSSFWorkbook that this style belongs to
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle#getFontIndex()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public HSSFFont getFont(org.apache.poi.ss.usermodel.Workbook parentWorkbook) {
        return ((HSSFWorkbook) parentWorkbook).getFontAt(getFontIndex());
    }

    /**
     * Sets the cells using this style to be hidden.
     *
     * @param hidden whether the cells using this style should be hidden
     */
    public void setHidden(boolean hidden) {
        format.setIndentNotParentCellOptions(true);
        format.setHidden(hidden);
    }

    /**
     * Gets whether the cells using this style are to be hidden.
     *
     * @return whether the cell using this style should be hidden
     */
    public boolean getHidden() {
        return format.isHidden();
    }

    /**
     * Sets the cells using this style to be locked.
     *
     * @param locked whether the cells using this style should be locked
     */
    public void setLocked(boolean locked) {
        format.setIndentNotParentCellOptions(true);
        format.setLocked(locked);
    }

    /**
     * Gets whether the cells using this style are to be locked.
     *
     * @return whether the cells using this style should be locked
     */
    public boolean getLocked() {
        return format.isLocked();
    }

    /**
     * Sets the type of horizontal alignment for the cell.
     *
     * @param align the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */
    public void setAlignment(short align) {
        format.setIndentNotParentAlignment(true);
        format.setAlignment(align);
    }

    /**
     * Gets the type of horizontal alignment for the cell.
     *
     * @return the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */
    public short getAlignment() {
        return format.getAlignment();
    }

    /**
     * Sets whether the text should be wrapped.
     *
     * @param wrapped wrap text or not
     */
    public void setWrapText(boolean wrapped) {
        format.setIndentNotParentAlignment(true);
        format.setWrapText(wrapped);
    }

    /**
     * Gets whether the text should be wrapped.
     *
     * @return wrap text or not
     */
    public boolean getWrapText() {
        return format.getWrapText();
    }

    /**
     * Sets the type of vertical alignment for the cell.
     *
     * @param align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */
    public void setVerticalAlignment(short align) {
        format.setVerticalAlignment(align);
    }

    /**
     * Gets the type of vertical alignment for the cell.
     *
     * @return the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */
    public short getVerticalAlignment() {
        return format.getVerticalAlignment();
    }

    /**
     * Sets the degree of rotation for the text in the cell.
     *
     * @param rotation degrees (between -90 and 90 degrees, of 0xff for vertical)
     */
    public void setRotation(short rotation) {
        if (rotation == 0xff) {
            // Special cases for vertically aligned text
        } else if ((rotation < 0) && (rotation >= -90)) {
            //Take care of the funny 4th quadrant issue
            //The 4th quadrant (-1 to -90) is stored as (91 to 180)
            rotation = (short) (90 - rotation);
        } else if ((rotation < -90) || (rotation > 90)) {
            //Do not allow an incorrect rotation to be set
            throw new IllegalArgumentException("The rotation must be between -90 and 90 degrees, or 0xff");
        }
        format.setRotation(rotation);
    }

    /**
     * Gets the degree of rotation for the text in the cell.
     *
     * @return rotation degrees (between -90 and 90 degrees, or 0xff for vertical)
     */
    public short getRotation() {
        short rotation = format.getRotation();
        if (rotation == 0xff) {
            // Vertical aligned special case
            return rotation;
        }
        if (rotation > 90) {
            //This is actually the 4th quadrant
            rotation = (short) (90 - rotation);
        }
        return rotation;
    }

    /**
     * Sets the number of spaces to indent the text in the cell.
     *
     * @param indent number of spaces
     */
    public void setIndention(short indent) {
        format.setIndent(indent);
    }

    /**
     * Gets the number of spaces to indent the text in the cell.
     *
     * @return number of spaces
     */
    public short getIndention() {
        return format.getIndent();
    }

    /**
     * Sets the type of border to use for the left border of the cell.
     *
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
    public void setBorderLeft(short border) {
        format.setIndentNotParentBorder(true);
        format.setBorderLeft(border);
    }

    /**
     * Gets the type of border to use for the left border of the cell.
     *
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
    public short getBorderLeft() {
        return format.getBorderLeft();
    }

    /**
     * Sets the type of border to use for the right border of the cell.
     *
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
    public void setBorderRight(short border) {
        format.setIndentNotParentBorder(true);
        format.setBorderRight(border);
    }

    /**
     * Gets the type of border to use for the right border of the cell.
     *
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
    public short getBorderRight() {
        return format.getBorderRight();
    }

    /**
     * Sets the type of border to use for the top border of the cell.
     *
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
    public void setBorderTop(short border) {
        format.setIndentNotParentBorder(true);
        format.setBorderTop(border);
    }

    /**
     * Gets the type of border to use for the top border of the cell.
     *
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
    public short getBorderTop() {
        return format.getBorderTop();
    }

    /**
     * Sets the type of border to use for the bottom border of the cell.
     *
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
    public void setBorderBottom(short border) {
        format.setIndentNotParentBorder(true);
        format.setBorderBottom(border);
    }

    /**
     * Gets the type of border to use for the bottom border of the cell.
     *
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
    public short getBorderBottom() {
        return format.getBorderBottom();
    }

    /**
     * Sets the color to use for the left border.
     *
     * @param color the index of the color definition
     */
    public void setLeftBorderColor(short color) {
        format.setLeftBorderPaletteIdx(color);
    }

    /**
     * Gets the color to use for the left border.
     *
     * @return the index of the color definition
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getLeftBorderColor() {
        return format.getLeftBorderPaletteIdx();
    }

    /**
     * Sets the color to use for the right border.
     *
     * @param color the index of the color definition
     */
    public void setRightBorderColor(short color) {
        format.setRightBorderPaletteIdx(color);
    }

    /**
     * Gets the color to use for the left border.
     *
     * @return the index of the color definition
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getRightBorderColor() {
        return format.getRightBorderPaletteIdx();
    }

    /**
     * Sets the color to use for the top border.
     *
     * @param color the index of the color definition
     */
    public void setTopBorderColor(short color) {
        format.setTopBorderPaletteIdx(color);
    }

    /**
     * Gets the color to use for the top border.
     *
     * @return the index of the color definition
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getTopBorderColor() {
        return format.getTopBorderPaletteIdx();
    }

    /**
     * Sets the color to use for the bottom border.
     *
     * @param color the index of the color definition
     */
    public void setBottomBorderColor(short color) {
        format.setBottomBorderPaletteIdx(color);
    }

    /**
     * Gets the color to use for the left border.
     *
     * @return the index of the color definition
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getBottomBorderColor() {
        return format.getBottomBorderPaletteIdx();
    }

    /**
     * Sets the fill pattern.
     * Value 1 means filling with foreground color.
     *
     * @param fp fill pattern
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
     */
    public void setFillPattern(short fp) {
        format.setAdtlFillPattern(fp);
    }

    /**
     * Gets the fill pattern.
     * Value 1 means filling with foreground color.
     *
     * @return fill pattern
     */
    public short getFillPattern() {
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
            if (format.getFillBackground() != (org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index + 1))
                setFillBackgroundColor((short) (org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index + 1));
        } else if (format.getFillBackground() == org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index + 1)
            //Now if the forground changes to a non-AUTOMATIC color the background resets itself!!!
            if (format.getFillForeground() != org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index)
                setFillBackgroundColor(org.apache.poi.hssf.util.HSSFColor.AUTOMATIC.index);
    }

    /**
     * Sets the background fill color.
     * <p/>
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
     * @param bg color
     */
    public void setFillBackgroundColor(short bg) {
        format.setFillBackground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Gets the background fill color.
     * Note - many cells are actually filled with a foreground
     * fill, not a background fill - see {@link #getFillForegroundColor()}
     *
     * @return fill color
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getFillBackgroundColor() {
        short result = format.getFillBackground();
        //JMH: Do this ridiculous conversion, and let HSSFCellStyle
        //internally migrate back and forth
        if (result == (HSSFColor.AUTOMATIC.index + 1)) {
            return HSSFColor.AUTOMATIC.index;
        }
        return result;
    }

    public HSSFColor getFillBackgroundColorColor() {
        HSSFPalette pallette = new HSSFPalette(workbook.getCustomPalette());
        return pallette.getColor(getFillBackgroundColor());
    }

    /**
     * Sets the foreground fill color.
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     *
     * @param bg color
     */
    public void setFillForegroundColor(short bg) {
        format.setFillForeground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Gets the foreground fill color.
     * Many cells are filled with this, instead of a
     * background color ({@link #getFillBackgroundColor()})
     *
     * @return fill color
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     */
    public short getFillForegroundColor() {
        return format.getFillForeground();
    }

    public HSSFColor getFillForegroundColorColor() {
        HSSFPalette pallette = new HSSFPalette(workbook.getCustomPalette());
        return pallette.getColor(getFillForegroundColor());
    }

    /**
     * Gets the name of the user defined style.
     * Returns null for built in styles, and
     * styles where no name has been defined.
     */
    public String getUserStyleName() {
        StyleRecord sr = workbook.getStyleRecord(index);
        if (sr == null) {
            return null;
        }
        if (sr.isBuiltin()) {
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
        if (sr == null) {
            sr = workbook.createStyleRecord(index);
        }
        // All Style records start as "builtin", but generally
        // only 20 and below really need to be
        if (sr.isBuiltin() && index <= 20) {
            throw new IllegalArgumentException("Unable to set user specified style names for built in styles!");
        }
        sr.setName(styleName);
    }

    /**
     * Sets if the Cell should be auto-sized by Excel
     * to shrink it to fit if the text is too long.
     */
    public void setShrinkToFit(boolean shrinkToFit) {
        format.setShrinkToFit(shrinkToFit);
    }

    /**
     * Gets if the Cell should be auto-sized by Excel
     * to shrink it to fit if this text is too long.
     */
    public boolean getShrinkToFit() {
        return format.getShrinkToFit();
    }

    /**
     * Gets the reading order, for RTL/LTR ordering of the text.
     * <p/>
     * 0 means Context (Default),
     * 1 means Left To Right,
     * and 2 means Right to Left.
     *
     * @return the reading order (0, 1, 2)
     */
    public short getReadingOrder() {
        return format.getReadingOrder();
    }

    /**
     * Sets the reading order, for RTL/LTR ordering of the text.
     * <p/>
     * 0 means Context (Default),
     * 1 means Left To Right,
     * and 2 means Right to Left.
     *
     * @param order the reading order (0, 1, 2)
     */
    public void setReadingOrder(short order) {
        format.setReadingOrder(order);
    }

    /**
     * Verifies that this style belongs to the supplied Workbook.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     * cell, to ensure the cell and the style are from the same
     * workbook (if they're not, it won't work).
     *
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToWorkbook(HSSFWorkbook wb) {
        if (wb.getWorkbook() != workbook) {
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
        }
    }

    /**
     * Clones all the style information from another
     * HSSFCellStyle, onto this one. This
     * HSSFCellStyle will then have all the same
     * properties as the source, but the two may
     * be edited independently.
     * Any stylings on this HSSFCellStyle will be lost!
     * <p/>
     * The source HSSFCellStyle could be from another
     * HSSFWorkbook if you like. This allows you to
     * copy styles from one HSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
        if (source instanceof HSSFCellStyle) {
            this.cloneStyleFrom((HSSFCellStyle) source);
        } else {
            throw new IllegalArgumentException("Can only clone from one HSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
        }
    }

    public void cloneStyleFrom(HSSFCellStyle source) {
        // First we need to clone the extended format record
        format.cloneStyleFrom(source.format);

        // Handle matching things if we cross workbooks
        if (workbook != source.workbook) {
            lastDateFormat.set(Short.MIN_VALUE);
            lastFormats.set(null);
            getDataFormatStringCache.set(null);

            // Then we need to clone the format string,
            // and update the format record for this
            short fmt = (short) workbook.createFormat(source.getDataFormatString());
            setDataFormat(fmt);

            // Finally we need to clone the font,
            // and update the format record for this
            FontRecord fr = workbook.createNewFont();
            fr.cloneStyleFrom(source.workbook.getFontRecordAt(source.getFontIndex()));

            HSSFFont font = new HSSFFont((short) workbook.getFontIndex(fr), fr);
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
