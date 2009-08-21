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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;


/**
 *
 * High level representation of the the possible formatting information for the contents of the cells on a sheet in a
 * SpreadsheetML document.
 *
 * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createCellStyle()
 * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getCellStyleAt(short)
 * @see org.apache.poi.xssf.usermodel.XSSFCell#setCellStyle(org.apache.poi.ss.usermodel.CellStyle)
 */
public class XSSFCellStyle implements CellStyle {

    private int _cellXfId;
    private StylesTable _stylesSource;
    private CTXf _cellXf;
    private CTXf _cellStyleXf;
    private XSSFFont _font;
    private XSSFCellAlignment _cellAlignment;

    /**
     * Creates a Cell Style from the supplied parts
     * @param cellXfId The main XF for the cell
     * @param cellStyleXfId Optional, style xf
     * @param stylesSource Styles Source to work off
     */
    public XSSFCellStyle(int cellXfId, int cellStyleXfId, StylesTable stylesSource) {
        _cellXfId = cellXfId;
        _stylesSource = stylesSource;
        _cellXf = stylesSource.getCellXfAt(this._cellXfId);
        _cellStyleXf = stylesSource.getCellStyleXfAt(cellStyleXfId);
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getCoreXf() {
        return _cellXf;
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getStyleXf() {
        return _cellStyleXf;
    }

    /**
     * Creates an empty Cell Style
     */
    public XSSFCellStyle(StylesTable stylesSource) {
        _stylesSource = stylesSource;
        // We need a new CTXf for the main styles
        // TODO decide on a style ctxf
        _cellXf = CTXf.Factory.newInstance();
        _cellStyleXf = null;
    }

    /**
     * Verifies that this style belongs to the supplied Workbook
     *  Styles Source.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToStylesSource(StylesTable src) {
        if(this._stylesSource != src) {
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook Stlyes Source. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
        }
    }

    /**
     * Clones all the style information from another
     *  XSSFCellStyle, onto this one. This
     *  XSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this XSSFCellStyle will be lost!
     *
     * The source XSSFCellStyle could be from another
     *  XSSFWorkbook if you like. This allows you to
     *  copy styles from one XSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
        if(source instanceof XSSFCellStyle) {
            XSSFCellStyle src = (XSSFCellStyle)source;
            _cellXf.set(src.getCoreXf());
            _cellStyleXf.set(src.getStyleXf());
        } else {
            throw new IllegalArgumentException("Can only clone from one XSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
        }
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return short - the type of alignment
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_GENERAL
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_LEFT
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_CENTER
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_RIGHT
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_FILL
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_JUSTIFY
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_CENTER_SELECTION
     */
    public short getAlignment() {
        return (short)(getAlignmentEnum().ordinal());
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return HorizontalAlignment - the type of alignment
     * @see org.apache.poi.ss.usermodel.HorizontalAlignment
     */
    public HorizontalAlignment getAlignmentEnum() {
        CTCellAlignment align = _cellXf.getAlignment();
        if(align != null && align.isSetHorizontal()) {
            return HorizontalAlignment.values()[align.getHorizontal().intValue()-1];
        }
        return HorizontalAlignment.GENERAL;
    }

    /**
     * Get the type of border to use for the bottom border of the cell
     *
     * @return short - border type
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderBottom() {
        if(!_cellXf.getApplyBorder()) return BORDER_NONE;

        int idx = (int)_cellXf.getBorderId();
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    /**
     * Get the type of border to use for the bottom border of the cell
     *
     * @return border type as Java enum
     * @see BorderStyle
     */
    public BorderStyle getBorderBottomEnum() {
        int style  = getBorderBottom();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the left border of the cell
     *
     * @return short - border type, default value is {@link org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderLeft() {
        if(!_cellXf.getApplyBorder()) return BORDER_NONE;

        int idx = (int)_cellXf.getBorderId();
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    /**
     * Get the type of border to use for the left border of the cell
     *
     * @return border type, default value is {@link org.apache.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderLeftEnum() {
        int style  = getBorderLeft();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the right border of the cell
     *
     * @return short - border type, default value is {@link org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderRight() {
        if(!_cellXf.getApplyBorder()) return BORDER_NONE;

        int idx = (int)_cellXf.getBorderId();
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

    /**
     * Get the type of border to use for the right border of the cell
     *
     * @return border type, default value is {@link org.apache.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderRightEnum() {
        int style  = getBorderRight();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the top border of the cell
     *
     * @return short - border type, default value is {@link org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle #BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderTop() {
        if(!_cellXf.getApplyBorder()) return BORDER_NONE;

        int idx = (int)_cellXf.getBorderId();
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle() : null;
        return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    }

     /**
     * Get the type of border to use for the top border of the cell
     *
     * @return border type, default value is {@link org.apache.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderTopEnum() {
         int style  = getBorderTop();
         return BorderStyle.values()[style];
    }

    /**
     * Get the color to use for the bottom border
     * <br/>
     * Color is optional. When missing, IndexedColors.AUTOMATIC is implied.
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public short getBottomBorderColor() {
        XSSFColor clr = getBottomBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the bottom border as a {@link XSSFColor}
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getBottomBorderXSSFColor() {
        if(!_cellXf.getApplyBorder()) return null;

        int idx = (int)_cellXf.getBorderId();
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.BOTTOM);
    }

    /**
     * Get the index of the number format (numFmt) record used by this cell format.
     *
     * @return the index of the number format
     */
    public short getDataFormat() {
        return (short)_cellXf.getNumFmtId();
    }

    /**
     * Get the contents of the format string, by looking up
     * the StylesSource
     *
     * @return the number format string
     */
    public String getDataFormatString() {
        int idx = getDataFormat();
        return new XSSFDataFormat(_stylesSource).getFormat((short)idx);
    }

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @return fill color, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public short getFillBackgroundColor() {
        XSSFColor clr = getFillBackgroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @see org.apache.poi.xssf.usermodel.XSSFColor#getRgb()
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillBackgroundXSSFColor() {
        if(!_cellXf.getApplyFill()) return null;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        return fg.getFillBackgroundColor();
    }

    /**
     * Get the foreground fill color.
     * <p>
     * Many cells are filled with this, instead of a
     *  background color ({@link #getFillBackgroundColor()})
     * </p>
     * @see IndexedColors
     * @return fill color, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     */
    public short getFillForegroundColor() {
        XSSFColor clr = getFillForegroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }

    /**
     * Get the foreground fill color.
     *
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillForegroundXSSFColor() {
        if(!_cellXf.getApplyFill()) return null;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        return fg.getFillForegroundColor();
    }

    /**
     * Get the fill pattern
     * @return fill pattern, default value is {@link org.apache.poi.ss.usermodel.CellStyle#NO_FILL}
     *
     * @see org.apache.poi.ss.usermodel.CellStyle#NO_FILL
     * @see org.apache.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND
     * @see org.apache.poi.ss.usermodel.CellStyle#FINE_DOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#ALT_BARS
     * @see org.apache.poi.ss.usermodel.CellStyle#SPARSE_DOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_HORZ_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_VERT_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_BACKWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_FORWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#BIG_SPOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#BRICKS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_HORZ_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_VERT_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_BACKWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_FORWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#SQUARES
     * @see org.apache.poi.ss.usermodel.CellStyle#DIAMONDS
     */
    public short getFillPattern() {
        if(!_cellXf.getApplyFill()) return 0;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fill = _stylesSource.getFillAt(fillIndex);

        STPatternType.Enum ptrn = fill.getPatternType();
        if(ptrn == null) return CellStyle.NO_FILL;
        return (short)(ptrn.intValue() - 1);
    }

    /**
     * Get the fill pattern
     *
     * @return the fill pattern, default value is {@link org.apache.poi.ss.usermodel.FillPatternType#NO_FILL}
     */
    public FillPatternType getFillPatternEnum() {
        int style  = getFillPattern();
        return FillPatternType.values()[style];
    }

    /**
    * Gets the font for this style
    * @return Font - font
    */
    public XSSFFont getFont() {
        if (_font == null) {
            _font = _stylesSource.getFontAt(getFontId());
        }
        return _font;
    }

    /**
     * Gets the index of the font for this style
     *
     * @return short - font index
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getFontAt(short)
     */
    public short getFontIndex() {
        return (short) getFontId();
    }

    /**
     * Get whether the cell's using this style are to be hidden
     *
     * @return boolean -  whether the cell using this style is hidden
     */
    public boolean getHidden() {
        return getCellProtection().getHidden();
    }

    /**
     * Get the number of spaces to indent the text in the cell
     *
     * @return indent - number of spaces
     */
    public short getIndention() {
        CTCellAlignment align = _cellXf.getAlignment();
        return (short)(align == null ? 0 : align.getIndent());
    }

    /**
     * Get the index within the StylesTable (sequence within the collection of CTXf elements)
     *
     * @return unique index number of the underlying record this style represents
     */
    public short getIndex() {
        return (short)this._cellXfId;
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public short getLeftBorderColor() {
        XSSFColor clr = getLeftBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getLeftBorderXSSFColor() {
        if(!_cellXf.getApplyBorder()) return null;

        int idx = (int)_cellXf.getBorderId();
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.LEFT);
    }

    /**
     * Get whether the cell's using this style are locked
     *
     * @return whether the cell using this style are locked
     */
    public boolean getLocked() {
        return getCellProtection().getLocked();
    }

    /**
     * Get the color to use for the right border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public short getRightBorderColor() {
        XSSFColor clr = getRightBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }
    /**
     * Get the color to use for the right border
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getRightBorderXSSFColor() {
        if(!_cellXf.getApplyBorder()) return null;

        int idx = (int)_cellXf.getBorderId();
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.RIGHT);
    }

    /**
     * Get the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @return rotation degrees (between 0 and 180 degrees)
     */
    public short getRotation() {
        CTCellAlignment align = _cellXf.getAlignment();
        return (short)(align == null ? 0 : align.getTextRotation());
    }

    /**
     * Get the color to use for the top border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public short getTopBorderColor() {
        XSSFColor clr = getTopBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the top border
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getTopBorderXSSFColor() {
        if(!_cellXf.getApplyBorder()) return null;

        int idx = (int)_cellXf.getBorderId();
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.TOP);
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return align the type of alignment, default value is {@link org.apache.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM}
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_TOP
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_CENTER
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_JUSTIFY
     */
    public short getVerticalAlignment() {
        return (short) (getVerticalAlignmentEnum().ordinal());
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return the type of alignment, default value is {@link org.apache.poi.ss.usermodel.VerticalAlignment#BOTTOM}
     * @see org.apache.poi.ss.usermodel.VerticalAlignment
     */
    public VerticalAlignment getVerticalAlignmentEnum() {
        CTCellAlignment align = _cellXf.getAlignment();
        if(align != null && align.isSetVertical()) {
            return VerticalAlignment.values()[align.getVertical().intValue()-1];
        }
        return VerticalAlignment.BOTTOM;
    }

    /**
     * Whether the text should be wrapped
     *
     * @return  a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public boolean getWrapText() {
        CTCellAlignment align = _cellXf.getAlignment();
        return align != null && align.getWrapText();
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_GENERAL
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_LEFT
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_CENTER
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_RIGHT
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_FILL
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_JUSTIFY
     * @see org.apache.poi.ss.usermodel.CellStyle#ALIGN_CENTER_SELECTION
     */
    public void setAlignment(short align) {
        getCellAlignment().setHorizontal(HorizontalAlignment.values()[align]);
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see org.apache.poi.ss.usermodel.HorizontalAlignment
     */
    public void setAlignment(HorizontalAlignment align) {
        setAlignment((short)align.ordinal());
    }

    /**
     * Set the type of border to use for the bottom border of the cell
     *
     * @param border the type of border to use
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public void setBorderBottom(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if(border == BORDER_NONE) ct.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of border to use for the bottom border of the cell
     *
     * @param border - type of border to use
     * @see org.apache.poi.ss.usermodel.BorderStyle
     */
    public void setBorderBottom(BorderStyle border) {
	    setBorderBottom((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the left border of the cell
     * @param border the type of border to use
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public void setBorderLeft(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if(border == BORDER_NONE) ct.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the left border of the cell
      *
     * @param border the type of border to use
     */
    public void setBorderLeft(BorderStyle border) {
	    setBorderLeft((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the right border of the cell
     *
     * @param border the type of border to use
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
   public void setBorderRight(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if(border == BORDER_NONE) ct.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the right border of the cell
      *
     * @param border the type of border to use
     */
    public void setBorderRight(BorderStyle border) {
	    setBorderRight((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the top border of the cell
     *
     * @param border the type of border to use
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.apache.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
   public void setBorderTop(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if(border == BORDER_NONE) ct.unsetTop();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of border to use for the top border of the cell
     *
     * @param border the type of border to use
     */
    public void setBorderTop(BorderStyle border) {
	    setBorderTop((short)border.ordinal());
    }

    /**
     * Set the color to use for the bottom border
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setBottomBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setBottomBorderColor(clr);
    }

    /**
     * Set the color to use for the bottom border
     *
     * @param color the color to use, null means no color
     */
    public void setBottomBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetBottom()) return;

        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the index of a data format
     *
     * @param fmt the index of a data format
     */
    public void setDataFormat(short fmt) {
        _cellXf.setApplyNumberFormat(true);
        _cellXf.setNumFmtId(fmt);
    }

    /**
     * Set the background fill color represented as a {@link XSSFColor} value.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundXSSFColor(new XSSFColor(java.awt.Color.RED));
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.BLUE));
     * cs.setFillBackgroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param color - the color to use
     */
    public void setFillBackgroundColor(XSSFColor color) {
        CTFill ct = getCTFill();
        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetBgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setBgColor(color.getCTColor());
        }

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * Set the background fill color represented as a indexed color value.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundXSSFColor(IndexedColors.RED.getIndex());
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(IndexedColors.BLUE.getIndex());
     * cs.setFillBackgroundColor(IndexedColors.RED.getIndex());
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(IndexedColors.RED.getIndex());
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param bg - the color to use
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setFillBackgroundColor(short bg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(bg);
        setFillBackgroundColor(clr);
    }

    /**
    * Set the foreground fill color represented as a {@link XSSFColor} value.
     * <br/>
    * <i>Note: Ensure Foreground color is set prior to background color.</i>
    * @param color the color to use
    * @see #setFillBackgroundColor(org.apache.poi.xssf.usermodel.XSSFColor) )
    */
    public void setFillForegroundColor(XSSFColor color) {
        CTFill ct = getCTFill();

        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetFgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setFgColor(color.getCTColor());
        }

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * Set the foreground fill color as a indexed color value
     * <br/>
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param fg the color to use
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setFillForegroundColor(short fg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(fg);
        setFillForegroundColor(clr);
    }

    /**
     * Get a <b>copy</b> of the currently used CTFill, if none is used, return a new instance.
     */
    private CTFill getCTFill(){
        CTFill ct;
        if(_cellXf.getApplyFill()) {
            int fillIndex = (int)_cellXf.getFillId();
            XSSFCellFill cf = _stylesSource.getFillAt(fillIndex);

            ct = (CTFill)cf.getCTFill().copy();
        } else {
            ct = CTFill.Factory.newInstance();
        }
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTBorder, if none is used, return a new instance.
     */
    private CTBorder getCTBorder(){
        CTBorder ct;
        if(_cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            XSSFCellBorder cf = _stylesSource.getBorderAt(idx);

            ct = (CTBorder)cf.getCTBorder().copy();
        } else {
            ct = CTBorder.Factory.newInstance();
        }
        return ct;
    }

    /**
     * This element is used to specify cell fill information for pattern and solid color cell fills.
     * For solid cell fills (no pattern),  foregorund color is used.
     * For cell fills with patterns specified, then the cell fill color is specified by the background color.
     *
     * @see org.apache.poi.ss.usermodel.CellStyle#NO_FILL
     * @see org.apache.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND
     * @see org.apache.poi.ss.usermodel.CellStyle#FINE_DOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#ALT_BARS
     * @see org.apache.poi.ss.usermodel.CellStyle#SPARSE_DOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_HORZ_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_VERT_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_BACKWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#THICK_FORWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#BIG_SPOTS
     * @see org.apache.poi.ss.usermodel.CellStyle#BRICKS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_HORZ_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_VERT_BANDS
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_BACKWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#THIN_FORWARD_DIAG
     * @see org.apache.poi.ss.usermodel.CellStyle#SQUARES
     * @see org.apache.poi.ss.usermodel.CellStyle#DIAMONDS
     * @see #setFillBackgroundColor(short)
     * @see #setFillForegroundColor(short)
     * @param fp  fill pattern (set to {@link org.apache.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND} to fill w/foreground color)
     */
   public void setFillPattern(short fp) {
        CTFill ct = getCTFill();
        CTPatternFill ptrn = ct.isSetPatternFill() ? ct.getPatternFill() : ct.addNewPatternFill();
        if(fp == NO_FILL && ptrn.isSetPatternType()) ptrn.unsetPatternType();
        else ptrn.setPatternType(STPatternType.Enum.forInt(fp + 1));

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * This element is used to specify cell fill information for pattern and solid color cell fills. For solid cell fills (no pattern),
     * foreground color is used is used. For cell fills with patterns specified, then the cell fill color is specified by the background color element.
     *
     * @param ptrn the fill pattern to use
     * @see #setFillBackgroundColor(short)
     * @see #setFillForegroundColor(short)
     * @see org.apache.poi.ss.usermodel.FillPatternType
     */
    public void setFillPattern(FillPatternType ptrn) {
	    setFillPattern((short)ptrn.ordinal());
    }

    /**
     * Set the font for this style
     *
     * @param font  a font object created or retreived from the XSSFWorkbook object
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createFont()
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getFontAt(short)
     */
    public void setFont(Font font) {
        if(font != null){
            long index = font.getIndex();
            this._cellXf.setFontId(index);
            this._cellXf.setApplyFont(true);
        } else {
            this._cellXf.setApplyFont(false);
        }
    }

    /**
     * Set the cell's using this style to be hidden
     *
     * @param hidden - whether the cell using this style should be hidden
     */
    public void setHidden(boolean hidden) {
        getCellProtection().setHidden(hidden);
    }

    /**
     * Set the number of spaces to indent the text in the cell
     *
     * @param indent - number of spaces
     */
    public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
    }

    /**
     * Set the color to use for the left border as a indexed color value
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setLeftBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setLeftBorderColor(clr);
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setLeftBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetLeft()) return;

        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the cell's using this style to be locked
     *
     * @param locked -  whether the cell using this style should be locked
     */
    public void setLocked(boolean locked) {
        getCellProtection().setLocked(locked);
    }

    /**
     * Set the color to use for the right border
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setRightBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setRightBorderColor(clr);
    }

    /**
     * Set the color to use for the right border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setRightBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetRight()) return;

        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @param rotation - the rotation degrees (between 0 and 180 degrees)
     */
    public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
    }


    /**
     * Set the color to use for the top border
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    public void setTopBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setTopBorderColor(clr);
    }

    /**
     * Set the color to use for the top border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setTopBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetTop()) return;

        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - align the type of alignment
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_TOP
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_CENTER
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM
     * @see org.apache.poi.ss.usermodel.CellStyle#VERTICAL_JUSTIFY
     * @see org.apache.poi.ss.usermodel.VerticalAlignment
     */
    public void setVerticalAlignment(short align) {
        getCellAlignment().setVertical(VerticalAlignment.values()[align]);
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - the type of alignment
     */
    public void setVerticalAlignment(VerticalAlignment align) {
        getCellAlignment().setVertical(align);
    }

    /**
     * Set whether the text should be wrapped.
     * <p>
     * Setting this flag to <code>true</code> make all content visible
     * whithin a cell by displaying it on multiple lines
     * </p>
     *
     * @param wrapped a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public void setWrapText(boolean wrapped) {
        getCellAlignment().setWrapText(wrapped);
    }

    /**
     * Gets border color
     *
     * @param side the border side
     * @return the used color
     */
    public XSSFColor getBorderColor(BorderSide side) {
        switch(side){
            case BOTTOM:
                return getBottomBorderXSSFColor();
            case RIGHT:
                return getRightBorderXSSFColor();
            case TOP:
                return getTopBorderXSSFColor();
            case LEFT:
                return getLeftBorderXSSFColor();
            default:
                throw new IllegalArgumentException("Unknown border: " + side);
        }
    }

    /**
     * Set the color to use for the selected border
     *
     * @param side - where to apply the color definition
     * @param color - the color to use
     */
    public void setBorderColor(BorderSide side, XSSFColor color) {
        switch(side){
            case BOTTOM:
                setBottomBorderColor(color);
                break;
            case RIGHT:
                setRightBorderColor(color);
                break;
            case TOP:
                setTopBorderColor(color);
                break;
            case LEFT:
                setLeftBorderColor(color);
                break;
        }
    }
    private int getFontId() {
        if (_cellXf.isSetFontId()) {
            return (int) _cellXf.getFontId();
        }
        return (int) _cellStyleXf.getFontId();
    }

    /**
     * get a cellProtection from the supplied XML definition
     * @return CTCellProtection
     */
    private CTCellProtection getCellProtection() {
        if (_cellXf.getProtection() == null) {
            _cellXf.addNewProtection();
        }
        return _cellXf.getProtection();
    }

    /**
     * get the cellAlignment object to use for manage alignment
     * @return XSSFCellAlignment - cell alignment
     */
    protected XSSFCellAlignment getCellAlignment() {
        if (this._cellAlignment == null) {
            this._cellAlignment = new XSSFCellAlignment(getCTCellAlignment());
        }
        return this._cellAlignment;
    }

    /**
     * Return the CTCellAlignment instance for alignment
     *
     * @return CTCellAlignment
     */
    private CTCellAlignment getCTCellAlignment() {
        if (_cellXf.getAlignment() == null) {
            _cellXf.setAlignment(CTCellAlignment.Factory.newInstance());
        }
        return _cellXf.getAlignment();
    }

    /**
     * Returns a hash code value for the object. The hash is derived from the underlying CTXf bean.
     *
     * @return the hash code value for this style
     */
    public int hashCode(){
        return _cellXf.toString().hashCode();
    }

    /**
     * Checks is the supplied style is equal to this style
     *
     * @param o the style to check
     * @return true if the supplied style is equal to this style
     */
    public boolean equals(Object o){
        if(o == null || !(o instanceof XSSFCellStyle)) return false;

        XSSFCellStyle cf = (XSSFCellStyle)o;
        return _cellXf.toString().equals(cf.getCoreXf().toString());
    }

    /**
     * Make a copy of this style. The underlying CTXf bean is cloned,
     * the references to fills and borders remain.
     *
     * @return a copy of this style
     */
    public Object clone(){
        CTXf xf = (CTXf)_cellXf.copy();

        int xfSize = _stylesSource._getStyleXfsSize();
        int indexXf = _stylesSource.putCellXf(xf);
        return new XSSFCellStyle(indexXf-1, xfSize-1, _stylesSource);
    }

}
