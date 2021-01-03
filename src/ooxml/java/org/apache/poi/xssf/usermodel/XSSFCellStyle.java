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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.ReadingOrder;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;

/**
 *
 * High level representation of the the possible formatting information for the contents of the cells on a sheet in a
 * SpreadsheetML document.
 *
 * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createCellStyle()
 * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getCellStyleAt(int)
 * @see org.apache.poi.xssf.usermodel.XSSFCell#setCellStyle(org.apache.poi.ss.usermodel.CellStyle)
 */
public class XSSFCellStyle implements CellStyle, Duplicatable {

    private int _cellXfId;
    private final StylesTable _stylesSource;
    private CTXf _cellXf;
    private final CTXf _cellStyleXf;
    private XSSFFont _font;
    private XSSFCellAlignment _cellAlignment;
    private ThemesTable _theme;

    /**
     * Creates a Cell Style from the supplied parts
     * @param cellXfId The main XF for the cell. Must be a valid 0-based index into the XF table
     * @param cellStyleXfId Optional, style xf. A value of <code>-1</code> means no xf.
     * @param stylesSource Styles Source to work off
     */
    public XSSFCellStyle(int cellXfId, int cellStyleXfId, StylesTable stylesSource, ThemesTable theme) {
        _cellXfId = cellXfId;
        _stylesSource = stylesSource;
        _cellXf = stylesSource.getCellXfAt(this._cellXfId);
        _cellStyleXf = cellStyleXfId == -1 ? null : stylesSource.getCellStyleXfAt(cellStyleXfId);
        _theme = theme;
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    @Internal
    public CTXf getCoreXf() {
        return _cellXf;
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    @Internal
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
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook Styles Source. Are you trying to assign a style from one workbook to the cell of a different workbook?");
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
    @Override
    public void cloneStyleFrom(CellStyle source) {
        if(source instanceof XSSFCellStyle) {
            XSSFCellStyle src = (XSSFCellStyle)source;

            // Is it on our Workbook?
            if(src._stylesSource == _stylesSource) {
               // Nice and easy
               _cellXf.set(src.getCoreXf());
               _cellStyleXf.set(src.getStyleXf());
            } else {
               // Copy the style
               try {
                  // Remove any children off the current style, to
                  //  avoid orphaned nodes
                  if(_cellXf.isSetAlignment())
                     _cellXf.unsetAlignment();
                  if(_cellXf.isSetExtLst())
                     _cellXf.unsetExtLst();

                  // Create a new Xf with the same contents
                  _cellXf = CTXf.Factory.parse(
                        src.getCoreXf().toString(), DEFAULT_XML_OPTIONS
                  );

                  // bug 56295: ensure that the fills is available and set correctly
                  CTFill fill = CTFill.Factory.parse(
                          src.getCTFill().toString(), DEFAULT_XML_OPTIONS
                          );
                  addFill(fill);

                  // bug 58084: set borders correctly
                  CTBorder border = CTBorder.Factory.parse(
                          src.getCTBorder().toString(), DEFAULT_XML_OPTIONS
                          );
                  addBorder(border);

                  // Swap it over
                  _stylesSource.replaceCellXfAt(_cellXfId, _cellXf);
               } catch(XmlException e) {
                  throw new POIXMLException(e);
               }

               // Copy the format
               String fmt = src.getDataFormatString();
               setDataFormat(
                     (new XSSFDataFormat(_stylesSource)).getFormat(fmt)
               );

               // Copy the font
               try {
                  CTFont ctFont = CTFont.Factory.parse(
                        src.getFont().getCTFont().toString(), DEFAULT_XML_OPTIONS
                  );
                  XSSFFont font = new XSSFFont(ctFont);
                  font.registerTo(_stylesSource);
                  setFont(font);
               } catch(XmlException e) {
                  throw new POIXMLException(e);
               }
            }

            // Clear out cached details
            _font = null;
            _cellAlignment = null;
        } else {
            throw new IllegalArgumentException("Can only clone from one XSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
        }
    }

    private void addFill(CTFill fill) {
        int idx = _stylesSource.putFill(new XSSFCellFill(fill,_stylesSource.getIndexedColors()));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    private void addBorder(CTBorder border) {
        int idx = _stylesSource.putBorder(new XSSFCellBorder(border, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    @Override
    public HorizontalAlignment getAlignment() {
        if(!_cellXf.getApplyAlignment()) return HorizontalAlignment.GENERAL;

        CTCellAlignment align = _cellXf.getAlignment();
        if(align != null && align.isSetHorizontal()) {
            return HorizontalAlignment.forInt(align.getHorizontal().intValue()-1);
        }
        return HorizontalAlignment.GENERAL;
    }

    @Override
    public BorderStyle getBorderBottom() {
        if(!_cellXf.getApplyBorder()) return BorderStyle.NONE;

        int idx = Math.toIntExact(_cellXf.getBorderId());
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom().getStyle() : null;
        if (ptrn == null) {
            return BorderStyle.NONE;
        }
        return BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    @Override
    public BorderStyle getBorderLeft() {
        if(!_cellXf.getApplyBorder()) return BorderStyle.NONE;

        int idx = Math.toIntExact(_cellXf.getBorderId());
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
        if (ptrn == null) {
            return BorderStyle.NONE;
        }
        return BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    @Override
    public BorderStyle getBorderRight() {
        if(!_cellXf.getApplyBorder()) return BorderStyle.NONE;

        int idx = Math.toIntExact(_cellXf.getBorderId());
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight().getStyle() : null;
        if (ptrn == null) {
            return BorderStyle.NONE;
        }
        return BorderStyle.valueOf((short)(ptrn.intValue() - 1));
    }

    @Override
    public BorderStyle getBorderTop() {
        if(!_cellXf.getApplyBorder()) return BorderStyle.NONE;

        int idx = Math.toIntExact(_cellXf.getBorderId());
        CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
        STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle() : null;
        if (ptrn == null) {
            return BorderStyle.NONE;
        }
        return BorderStyle.valueOf((short) (ptrn.intValue() - 1));
    }

    /**
     * Get the color to use for the bottom border
     * <br>
     * Color is optional. When missing, IndexedColors.AUTOMATIC is implied.
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
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

        int idx = Math.toIntExact(_cellXf.getBorderId());
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.BOTTOM);
    }

    /**
     * Get the index of the number format (numFmt) record used by this cell format.
     *
     * @return the index of the number format
     */
    @Override
    public short getDataFormat() {
        return (short)_cellXf.getNumFmtId();
    }

    /**
     * Get the contents of the format string, by looking up
     * the StylesSource
     *
     * @return the number format string
     */
    @Override
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
    @Override
    public short getFillBackgroundColor() {
        XSSFColor clr = getFillBackgroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }

    @Override
    public XSSFColor getFillBackgroundColorColor() {
       return getFillBackgroundXSSFColor();
    }

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @see org.apache.poi.xssf.usermodel.XSSFColor#getRGB()
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillBackgroundXSSFColor() {
        // bug 56295: handle missing applyFill attribute as "true" because Excel does as well
        if(_cellXf.isSetApplyFill() && !_cellXf.getApplyFill()) return null;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        XSSFColor fillBackgroundColor = fg.getFillBackgroundColor();
        if (fillBackgroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillBackgroundColor);
        }
        return fillBackgroundColor;
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
    @Override
    public short getFillForegroundColor() {
        XSSFColor clr = getFillForegroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }

    @Override
    public XSSFColor getFillForegroundColorColor() {
       return getFillForegroundXSSFColor();
    }

    /**
     * Get the foreground fill color.
     *
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillForegroundXSSFColor() {
        // bug 56295: handle missing applyFill attribute as "true" because Excel does as well
        if(_cellXf.isSetApplyFill() && !_cellXf.getApplyFill()) return null;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        XSSFColor fillForegroundColor = fg.getFillForegroundColor();
        if (fillForegroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillForegroundColor);
        }
        return fillForegroundColor;
    }

    @Override
    public FillPatternType getFillPattern() {
        // bug 56295: handle missing applyFill attribute as "true" because Excel does as well
        if(_cellXf.isSetApplyFill() && !_cellXf.getApplyFill()) return FillPatternType.NO_FILL;

        int fillIndex = (int)_cellXf.getFillId();
        XSSFCellFill fill = _stylesSource.getFillAt(fillIndex);

        STPatternType.Enum ptrn = fill.getPatternType();
        if(ptrn == null) return FillPatternType.NO_FILL;
        return FillPatternType.forInt(ptrn.intValue() - 1);
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
     * @return font index
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getFontAt(int)
     * @since 5.0.0 (used to return a short value)
     */
    @Override
    public int getFontIndex() {
        return getFontId();
    }

    /**
     * Gets the index of the font for this style
     *
     * @return font index
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getFontAt(int)
     * @deprecated use {@link #getFontIndex()} instead
     * @since 4.0.0
     */
    @Deprecated
    @Removal(version = "6.0.0")
    @Override
    public int getFontIndexAsInt() {
        return getFontId();
    }

    /**
     * Get whether the cell's using this style are to be hidden
     *
     * @return boolean -  whether the cell using this style is hidden
     */
    @Override
    public boolean getHidden() {
        return _cellXf.isSetProtection() && _cellXf.getProtection().isSetHidden() && _cellXf.getProtection().getHidden();
    }

    /**
     * Get the number of spaces to indent the text in the cell
     *
     * @return indent - number of spaces
     */
    @Override
    public short getIndention() {
        CTCellAlignment align = _cellXf.getAlignment();
        return (short)(align == null ? 0 : align.getIndent());
    }

    /**
     * Get the index within the StylesTable (sequence within the collection of CTXf elements)
     *
     * @return unique index number of the underlying record this style represents, as a short (may wrap)
     */
    @Override
    public short getIndex() {
        return (short)this._cellXfId;
    }

    /**
     * Workaround for places where we need to support more than 32767 cell styles, ideally
     * the main getIndex() and others would return int, not short, but that would affect some
     * public APIs
     *
     * @return unique index number of the underlying record this style represents, as an int (always positive)
     */
    protected int getUIndex() {
        return this._cellXfId;
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
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

        int idx = Math.toIntExact(_cellXf.getBorderId());
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.LEFT);
    }

    /**
     * Get whether the cell's using this style are locked
     *
     * @return whether the cell using this style are locked
     */
    @Override
    public boolean getLocked() {
        return !_cellXf.isSetProtection() || !_cellXf.getProtection().isSetLocked() || _cellXf.getProtection().getLocked();
    }

    /**
     * Is "Quote Prefix" or "123 Prefix" enabled for the cell?
     */
    @Override
    public boolean getQuotePrefixed() {
        return _cellXf.getQuotePrefix();
    }

    /**
     * Get the color to use for the right border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
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

        int idx = Math.toIntExact(_cellXf.getBorderId());
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.RIGHT);
    }

    /**
     * Get the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @return rotation degrees (between 0 and 180 degrees)
     */
    @Override
    public short getRotation() {
        CTCellAlignment align = _cellXf.getAlignment();
        return align == null || align.getTextRotation() == null ? 0 : align.getTextRotation().shortValue();
    }

    @Override
    public boolean getShrinkToFit() {
        CTCellAlignment align = _cellXf.getAlignment();
        return align != null && align.getShrinkToFit();
    }

    /**
     * Get the color to use for the top border
     *
     * @return the index of the color definition, default value is {@link org.apache.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
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

        int idx = Math.toIntExact(_cellXf.getBorderId());
        XSSFCellBorder border = _stylesSource.getBorderAt(idx);

        return border.getBorderColor(BorderSide.TOP);
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        if(!_cellXf.getApplyAlignment()) return VerticalAlignment.BOTTOM;

        CTCellAlignment align = _cellXf.getAlignment();
        if(align != null && align.isSetVertical()) {
            return VerticalAlignment.forInt(align.getVertical().intValue()-1);
        }
        return VerticalAlignment.BOTTOM;
    }

    /**
     * Whether the text should be wrapped
     *
     * @return  a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    @Override
    public boolean getWrapText() {
        CTCellAlignment align = _cellXf.getAlignment();
        return align != null && align.getWrapText();
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     */
    @Override
    public void setAlignment(HorizontalAlignment align) {
        _cellXf.setApplyAlignment(true);

        getCellAlignment().setHorizontal(align);
    }

    /**
     * Set the type of border to use for the bottom border of the cell
     *
     * @param border - type of border to use
     * @see org.apache.poi.ss.usermodel.BorderStyle
     * @since POI 3.15
     */
    @Override
    public void setBorderBottom(BorderStyle border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if(border == BorderStyle.NONE) ct.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme, _stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the left border of the cell
      *
     * @param border the type of border to use
     * @since POI 3.15
     */
    @Override
    public void setBorderLeft(BorderStyle border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if(border == BorderStyle.NONE) ct.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme, _stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the right border of the cell
      *
     * @param border the type of border to use
     * @since POI 3.15
     */
    @Override
    public void setBorderRight(BorderStyle border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if(border == BorderStyle.NONE) ct.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of border to use for the top border of the cell
     *
     * @param border the type of border to use
     * @since POI 3.15
     */
    @Override
    public void setBorderTop(BorderStyle border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if(border == BorderStyle.NONE) ct.unsetTop();
        else pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the color to use for the bottom border
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
    public void setBottomBorderColor(short color) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
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

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the index of a data format
     *
     * @param fmt the index of a data format
     */
    @Override
    public void setDataFormat(short fmt) {
        // XSSF supports >32,767 formats
        setDataFormat(fmt&0xffff);
    }
    /**
     * Set the index of a data format
     *
     * @param fmt the index of a data format
     */
    public void setDataFormat(int fmt) {
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
            if(ptrn != null && ptrn.isSetBgColor()) ptrn.unsetBgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setBgColor(color.getCTColor());
        }

        addFill(ct);
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
    @Override
    public void setFillBackgroundColor(short bg) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
        clr.setIndexed(bg);
        setFillBackgroundColor(clr);
    }

    /**
    * Set the foreground fill color represented as a {@link XSSFColor} value.
     * <br>
    * <i>Note: Ensure Foreground color is set prior to background color.</i>
    * @param color the color to use
    * @see #setFillBackgroundColor(org.apache.poi.xssf.usermodel.XSSFColor) )
    */
    public void setFillForegroundColor(XSSFColor color) {
        CTFill ct = getCTFill();

        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null && ptrn.isSetFgColor()) ptrn.unsetFgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setFgColor(color.getCTColor());
        }

        addFill(ct);
    }

    /**
     * Set the foreground fill color as a indexed color value
     * <br>
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param fg the color to use
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
    public void setFillForegroundColor(short fg) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
        clr.setIndexed(fg);
        setFillForegroundColor(clr);
    }

    /**
     * Get a <b>copy</b> of the currently used CTFill, if none is used, return a new instance.
     */
    private CTFill getCTFill(){
        CTFill ct;
        // bug 56295: handle missing applyFill attribute as "true" because Excel does as well
        if(!_cellXf.isSetApplyFill() || _cellXf.getApplyFill()) {
            int fillIndex = (int)_cellXf.getFillId();
            XSSFCellFill cf = _stylesSource.getFillAt(fillIndex);

            ct = (CTFill)cf.getCTFill().copy();
        } else {
            ct = CTFill.Factory.newInstance();
        }
        return ct;
    }

    /**
     * Set reading order for the cell
     *
     * @param order - the reading order
     */
    public void setReadingOrder(ReadingOrder order) {
        getCellAlignment().setReadingOrder(order);
    }

    /**
     * Get reading order of the cell
     *
     * @return ReadingOrder - the reading order
     */
    public ReadingOrder getReadingOrder() {
        return getCellAlignment().getReadingOrder();
    }

    /**
     * Get a <b>copy</b> of the currently used CTBorder, if none is used, return a new instance.
     */
    private CTBorder getCTBorder(){
        CTBorder ct;
        if(_cellXf.getApplyBorder()) {
            int idx = Math.toIntExact(_cellXf.getBorderId());
            XSSFCellBorder cf = _stylesSource.getBorderAt(idx);

            ct = (CTBorder)cf.getCTBorder().copy();
        } else {
            ct = CTBorder.Factory.newInstance();
        }
        return ct;
    }

    /**
     * This element is used to specify cell fill information for pattern and solid color cell fills. For solid cell fills (no pattern),
     * foreground color is used is used. For cell fills with patterns specified, then the cell fill color is specified by the background color element.
     *
     * @param pattern the fill pattern to use
     * @see #setFillBackgroundColor(XSSFColor)
     * @see #setFillForegroundColor(XSSFColor)
     * @see org.apache.poi.ss.usermodel.FillPatternType
     */
    @Override
    public void setFillPattern(FillPatternType pattern) {
        CTFill ct = getCTFill();
        CTPatternFill ctptrn = ct.isSetPatternFill() ? ct.getPatternFill() : ct.addNewPatternFill();
        if (pattern == FillPatternType.NO_FILL && ctptrn.isSetPatternType()) {
            ctptrn.unsetPatternType();
        } else {
            ctptrn.setPatternType(STPatternType.Enum.forInt(pattern.getCode() + 1));
        }

        addFill(ct);
    }

    /**
     * Set the font for this style
     *
     * @param font  a font object created or retrieved from the XSSFWorkbook object
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createFont()
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#getFontAt(int)
     */
    @Override
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
    @Override
    public void setHidden(boolean hidden) {
        if (!_cellXf.isSetProtection()) {
             _cellXf.addNewProtection();
         }
        _cellXf.getProtection().setHidden(hidden);
    }

    /**
     * Set the number of spaces to indent the text in the cell
     *
     * @param indent - number of spaces
     */
    @Override
    public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
    }

    /**
     * Set the color to use for the left border as a indexed color value
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
    public void setLeftBorderColor(short color) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
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

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the cell's using this style to be locked
     *
     * @param locked -  whether the cell using this style should be locked
     */
    @Override
    public void setLocked(boolean locked) {
        if (!_cellXf.isSetProtection()) {
             _cellXf.addNewProtection();
         }
        _cellXf.getProtection().setLocked(locked);
    }

    /**
     * Turn on or off "Quote Prefix" or "123 Prefix" for the style,
     *  which is used to tell Excel that the thing which looks like
     *  a number or a formula shouldn't be treated as on.
     */
    @Override
    public void setQuotePrefixed(boolean quotePrefix) {
        _cellXf.setQuotePrefix(quotePrefix);
    }

    /**
     * Set the color to use for the right border
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
    public void setRightBorderColor(short color) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
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

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * Note: HSSF uses values from -90 to 90 degrees, whereas XSSF
     * uses values from 0 to 180 degrees. The implementations of this method will map between these two value-ranges
     * accordingly, however the corresponding getter is returning values in the range mandated by the current type
     * of Excel file-format that this CellStyle is applied to.
     *
     * @param rotation - the rotation degrees (between 0 and 180 degrees)
     */
    @Override
    public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
    }


    /**
     * Set the color to use for the top border
     *
     * @param color the index of the color definition
     * @see org.apache.poi.ss.usermodel.IndexedColors
     */
    @Override
    public void setTopBorderColor(short color) {
        XSSFColor clr = XSSFColor.from(CTColor.Factory.newInstance(), _stylesSource.getIndexedColors());
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

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme,_stylesSource.getIndexedColors()));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - the type of alignment
     */
    public void setVerticalAlignment(VerticalAlignment align) {
        _cellXf.setApplyAlignment(true);

        getCellAlignment().setVertical(align);
    }

    /**
     * Set whether the text should be wrapped.
     * <p>
     * Setting this flag to <code>true</code> make all content visible
     * within a cell by displaying it on multiple lines
     * </p>
     *
     * @param wrapped a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    @Override
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

    @Override
    public void setShrinkToFit(boolean shrinkToFit) {
        getCellAlignment().setShrinkToFit(shrinkToFit);
    }

    private int getFontId() {
        if (_cellXf.isSetFontId()) {
            return (int) _cellXf.getFontId();
        }
        return (int) _cellStyleXf.getFontId();
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
    @Override
    public int hashCode(){
        return _cellXf.toString().hashCode();
    }

    /**
     * Checks is the supplied style is equal to this style
     *
     * @param o the style to check
     * @return true if the supplied style is equal to this style
     */
    @Override
    public boolean equals(Object o){
        if(!(o instanceof XSSFCellStyle)) return false;

        XSSFCellStyle cf = (XSSFCellStyle)o;
        return _cellXf.toString().equals(cf.getCoreXf().toString());
    }

    /**
     * Make a copy of this style. The underlying CTXf bean is cloned,
     * the references to fills and borders remain.
     *
     * @return a copy of this style
     */
    @Override
    public XSSFCellStyle copy(){
        CTXf xf = (CTXf)_cellXf.copy();

        int xfSize = _stylesSource._getStyleXfsSize();
        int indexXf = _stylesSource.putCellXf(xf);
        return new XSSFCellStyle(indexXf-1, xfSize-1, _stylesSource, _theme);
    }
}
