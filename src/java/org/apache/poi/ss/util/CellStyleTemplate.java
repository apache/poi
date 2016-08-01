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

package org.apache.poi.ss.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * <p>
 * A {@link CellStyleTemplate} is a template that can be applied to any sheet in
 * a project. It contains all the border type and color attributes needed to
 * draw all the borders for a single sheet. That template can be applied to any
 * sheet in any workbook.
 * 
 * This class requires the full spreadsheet to be in memory so
 * {@link SXSSFWorkbook} Spreadsheets are not supported. The same
 * {@link CellStyleTemplate} can, however, be applied to both
 * {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}, and Workbook objects
 * if necessary. Portions of the border that fall outside the max range of the
 * {@link HSSFWorkbook} sheet are ignored.
 * </p>
 * 
 * <p>
 * This would replace {@link RegionUtil}.
 * </p>
 */
public final class CellStyleTemplate {

    /**
     * This is a list of cell properties for one shot application to a range of
     * cells at a later time.
     */
    private Map<CellAddress, Map<String, Object>> _cellStyleTemplate;

    /**
     * Create a new Cell Style Template
     */
    public CellStyleTemplate() {
        _cellStyleTemplate = new HashMap<CellAddress, Map<String, Object>>();
    }
    
    /**
     * Create a new Cell Style Template
     * 
     *  @param other CellStyleTemplate to copy
     */
    public CellStyleTemplate(CellStyleTemplate other) {
        this();
        for (Map.Entry<CellAddress, Map<String, Object>> entry : other._cellStyleTemplate.entrySet()) {
            CellAddress ca = new CellAddress(entry.getKey());
            Map<String, Object> newProperties = new HashMap<String, Object>();
            for (Map.Entry<String, Object> property : entry.getValue().entrySet()) {
                if (property.getValue() instanceof Short) {
                    newProperties.put(property.getKey(), new Short(getShort(property.getValue())));
                } else {
                    newProperties.put(property.getKey(), property.getValue());
                }
            }
            _cellStyleTemplate.put(ca, newProperties);
        }
    }

    /**
     * Draws a group of cell borders for a cell range. The borders are not
     * applied to the cells at this time, just the template is drawn. To apply
     * the drawn borders to a sheet, use {@link #applyBorders}.
     * 
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderStyle,
            BorderExtent extent) {
        switch (extent) {
        case NONE:
            removeBorders(range);
            break;
        case ALL:
            drawHorizontalBorders(range, borderStyle, BorderExtent.ALL);
            drawVerticalBorders(range, borderStyle, BorderExtent.ALL);
            break;
        case INSIDE:
            drawHorizontalBorders(range, borderStyle, BorderExtent.INSIDE);
            drawVerticalBorders(range, borderStyle, BorderExtent.INSIDE);
            break;
        case OUTSIDE:
            drawOutsideBorders(range, borderStyle, BorderExtent.ALL);
            break;
        case TOP:
            drawTopBorder(range, borderStyle);
            break;
        case BOTTOM:
            drawBottomBorder(range, borderStyle);
            break;
        case LEFT:
            drawLeftBorder(range, borderStyle);
            break;
        case RIGHT:
            drawRightBorder(range, borderStyle);
            break;
        case HORIZONTAL:
            drawHorizontalBorders(range, borderStyle, BorderExtent.ALL);
            break;
        case INSIDE_HORIZONTAL:
            drawHorizontalBorders(range, borderStyle, BorderExtent.INSIDE);
            break;
        case OUTSIDE_HORIZONTAL:
            drawOutsideBorders(range, borderStyle, BorderExtent.HORIZONTAL);
            break;
        case VERTICAL:
            drawVerticalBorders(range, borderStyle, BorderExtent.ALL);
            break;
        case INSIDE_VERTICAL:
            drawVerticalBorders(range, borderStyle, BorderExtent.INSIDE);
            break;
        case OUTSIDE_VERTICAL:
            drawOutsideBorders(range, borderStyle, BorderExtent.VERTICAL);
            break;
        }
    }

    /**
     * Draws a group of cell borders for a cell range. The borders are not
     * applied to the cells at this time, just the template is drawn. To apply
     * the drawn borders to a sheet, use {@link #applyBorders}.
     * 
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders. Ignored if extent is {@link BorderExtent#NONE}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderStyle,
            short color, BorderExtent extent) {
        drawBorders(range, borderStyle, extent);
        if (borderStyle != BorderStyle.NONE) {
            drawBorderColors(range, color, extent);
        }
    }

    /**
     * <p>
     * Draws the top border for a range of cells
     * </p>
     * 
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawTopBorder(CellRangeAddress range, BorderStyle borderStyle) {
        int row = range.getFirstRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            addBorderStyle(row, i, CellUtil.BORDER_TOP, borderStyle);
            if (borderStyle == BorderStyle.NONE && row > 0) {
                addBorderStyle(row - 1, i, CellUtil.BORDER_BOTTOM, borderStyle);
            }
        }
    }

    /**
     * <p>
     * Draws the bottom border for a range of cells
     * </p>
     * 
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawBottomBorder(CellRangeAddress range, BorderStyle borderStyle) {
        int row = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            addBorderStyle(row, i, CellUtil.BORDER_BOTTOM, borderStyle);
            if (borderStyle == BorderStyle.NONE
                    && row < SpreadsheetVersion.EXCEL2007.getMaxRows() - 1) {
                addBorderStyle(row + 1, i, CellUtil.BORDER_TOP, borderStyle);
            }
        }
    }

    /**
     * <p>
     * Draws the left border for a range of cells
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawLeftBorder(CellRangeAddress range, BorderStyle borderStyle) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getFirstColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            addBorderStyle(i, col, CellUtil.BORDER_LEFT, borderStyle);
            if (borderStyle == BorderStyle.NONE && col > 0) {
                addBorderStyle(i, col - 1, CellUtil.BORDER_RIGHT, borderStyle);
            }
        }
    }

    /**
     * <p>
     * Draws the right border for a range of cells
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawRightBorder(CellRangeAddress range, BorderStyle borderStyle) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getLastColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            addBorderStyle(i, col, CellUtil.BORDER_RIGHT, borderStyle);
            if (borderStyle == BorderStyle.NONE
                    && col < SpreadsheetVersion.EXCEL2007.getMaxColumns() - 1) {
                addBorderStyle(i, col + 1, CellUtil.BORDER_LEFT, borderStyle);
            }
        }
    }

    /**
     * <p>
     * Draws the outside borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.HORIZONTAL</li>
     *            <li>CellBorder.Extent.VERTICAL</li>
     *            </ul>
     */
    private void drawOutsideBorders(CellRangeAddress range, BorderStyle borderStyle,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case HORIZONTAL:
        case VERTICAL:
            if (extent == BorderExtent.ALL || extent == BorderExtent.HORIZONTAL) {
                drawTopBorder(range, borderStyle);
                drawBottomBorder(range, borderStyle);
            }
            if (extent == BorderExtent.ALL || extent == BorderExtent.VERTICAL) {
                drawLeftBorder(range, borderStyle);
                drawRightBorder(range, borderStyle);
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL, HORIZONTAL, and VERTICAL");
        }
    }

    /**
     * <p>
     * Draws the horizontal borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.INSIDE</li>
     *            </ul>
     */
    private void drawHorizontalBorders(CellRangeAddress range, BorderStyle borderStyle,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstRow; i <= lastRow; i++) {
                CellRangeAddress row = new CellRangeAddress(i, i, firstCol,
                        lastCol);
                if (extent == BorderExtent.ALL || i > firstRow) {
                    drawTopBorder(row, borderStyle);
                }
                if (extent == BorderExtent.ALL || i < lastRow) {
                    drawBottomBorder(row, borderStyle);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * <p>
     * Draws the vertical borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderStyle
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.INSIDE</li>
     *            </ul>
     */
    private void drawVerticalBorders(CellRangeAddress range, BorderStyle borderStyle,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstCol; i <= lastCol; i++) {
                CellRangeAddress row = new CellRangeAddress(firstRow, lastRow,
                        i, i);
                if (extent == BorderExtent.ALL || i > firstCol) {
                    drawLeftBorder(row, borderStyle);
                }
                if (extent == BorderExtent.ALL || i < lastCol) {
                    drawRightBorder(row, borderStyle);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this {@link CellStyleTemplate} for the
     * specified range.
     * 
     * @parm range - {@link CellRangeAddress} range of cells to remove borders.
     */
    private void removeBorders(CellRangeAddress range) {
        Set<String> properties = new HashSet<String>();
        properties.add(CellUtil.BORDER_TOP);
        properties.add(CellUtil.BORDER_BOTTOM);
        properties.add(CellUtil.BORDER_LEFT);
        properties.add(CellUtil.BORDER_RIGHT);
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            for (int col = range.getFirstColumn(); col <= range
                    .getLastColumn(); col++) {
                removeProperties(row, col, properties);
            }
        }
        removeBorderColors(range);
    }

    /**
     * Applies the drawn borders to a Sheet. The borders that are applied are
     * the ones that have been drawn by the {@link #drawBorders} and
     * {@link #drawBorderColors} methods.
     *
     * @param sheet
     *            - {@link Sheet} on which to apply borders
     */
    public void applyBorders(Sheet sheet) {
        Workbook wb = sheet.getWorkbook();
        for (Map.Entry<CellAddress, Map<String, Object>> entry : _cellStyleTemplate
                .entrySet()) {
            CellAddress cellAddress = entry.getKey();
            if (cellAddress.getRow() < wb.getSpreadsheetVersion().getMaxRows()
                    && cellAddress.getColumn() < wb.getSpreadsheetVersion()
                            .getMaxColumns()) {
                Map<String, Object> properties = entry.getValue();
                Row row = CellUtil.getRow(cellAddress.getRow(), sheet);
                Cell cell = CellUtil.getCell(row, cellAddress.getColumn());
                CellUtil.setCellStyleProperties(cell, properties);
            }
        }
    }

    /**
     * Sets the color for a group of cell borders for a cell range. The borders
     * are not applied to the cells at this time, just the template is drawn. If
     * the borders do not exist, a BORDER_THIN border is used. To apply the
     * drawn borders to a sheet, use {@link #applyBorders}.
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     * @param extent
     *            - {@link BorderExtent} of the borders for which colors are set.
     */
    public void drawBorderColors(CellRangeAddress range, short color,
            BorderExtent extent) {
        switch (extent) {
        case NONE:
            removeBorderColors(range);
            break;
        case ALL:
            drawHorizontalBorderColors(range, color, BorderExtent.ALL);
            drawVerticalBorderColors(range, color, BorderExtent.ALL);
            break;
        case INSIDE:
            drawHorizontalBorderColors(range, color, BorderExtent.INSIDE);
            drawVerticalBorderColors(range, color, BorderExtent.INSIDE);
            break;
        case OUTSIDE:
            drawOutsideBorderColors(range, color, BorderExtent.ALL);
            break;
        case TOP:
            drawTopBorderColor(range, color);
            break;
        case BOTTOM:
            drawBottomBorderColor(range, color);
            break;
        case LEFT:
            drawLeftBorderColor(range, color);
            break;
        case RIGHT:
            drawRightBorderColor(range, color);
            break;
        case HORIZONTAL:
            drawHorizontalBorderColors(range, color, BorderExtent.ALL);
            break;
        case INSIDE_HORIZONTAL:
            drawHorizontalBorderColors(range, color, BorderExtent.INSIDE);
            break;
        case OUTSIDE_HORIZONTAL:
            drawOutsideBorderColors(range, color, BorderExtent.HORIZONTAL);
            break;
        case VERTICAL:
            drawVerticalBorderColors(range, color, BorderExtent.ALL);
            break;
        case INSIDE_VERTICAL:
            drawVerticalBorderColors(range, color, BorderExtent.INSIDE);
            break;
        case OUTSIDE_VERTICAL:
            drawOutsideBorderColors(range, color, BorderExtent.VERTICAL);
            break;
        }
    }

    /**
     * <p>
     * Sets the color of the top border for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     */
    private void drawTopBorderColor(CellRangeAddress range, short color) {
        int row = range.getFirstRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            if (!borderIsSet(new CellAddress(row, i), CellUtil.BORDER_TOP)) {
                drawTopBorder(new CellRangeAddress(row, row, i, i),
                        BorderStyle.THIN);
            }
            addProperty(row, i, CellUtil.TOP_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the bottom border for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     */
    private void drawBottomBorderColor(CellRangeAddress range, short color) {
        int row = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            if (!borderIsSet(new CellAddress(row, i), CellUtil.BORDER_BOTTOM)) {
                drawBottomBorder(new CellRangeAddress(row, row, i, i),
                        BorderStyle.THIN);
            }
            addProperty(row, i, CellUtil.BOTTOM_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the left border for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     */
    private void drawLeftBorderColor(CellRangeAddress range, short color) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getFirstColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            if (!borderIsSet(new CellAddress(i, col), CellUtil.BORDER_LEFT)) {
                drawLeftBorder(new CellRangeAddress(i, i, col, col),
                        BorderStyle.THIN);
            }
            addProperty(i, col, CellUtil.LEFT_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the right border for a range of cells. If the border is
     * not drawn, it defaults to BORDER_THIN
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     */
    private void drawRightBorderColor(CellRangeAddress range, short color) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getLastColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            if (!borderIsSet(new CellAddress(i, col), CellUtil.BORDER_RIGHT)) {
                drawRightBorder(new CellRangeAddress(i, i, col, col),
                        BorderStyle.THIN);
            }
            addProperty(i, col, CellUtil.RIGHT_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the outside borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.HORIZONTAL</li>
     *            <li>CellBorder.Extent.VERTICAL</li>
     *            </ul>
     */
    private void drawOutsideBorderColors(CellRangeAddress range, short color,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case HORIZONTAL:
        case VERTICAL:
            if (extent == BorderExtent.ALL || extent == BorderExtent.HORIZONTAL) {
                drawTopBorderColor(range, color);
                drawBottomBorderColor(range, color);
            }
            if (extent == BorderExtent.ALL || extent == BorderExtent.VERTICAL) {
                drawLeftBorderColor(range, color);
                drawRightBorderColor(range, color);
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL, HORIZONTAL, and VERTICAL");
        }
    }

    /**
     * <p>
     * Sets the color of the horizontal borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.INSIDE</li>
     *            </ul>
     */
    private void drawHorizontalBorderColors(CellRangeAddress range, short color,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstRow; i <= lastRow; i++) {
                CellRangeAddress row = new CellRangeAddress(i, i, firstCol,
                        lastCol);
                if (extent == BorderExtent.ALL || i > firstRow) {
                    drawTopBorderColor(row, color);
                }
                if (extent == BorderExtent.ALL || i < lastRow) {
                    drawBottomBorderColor(row, color);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * <p>
     * Sets the color of the vertical borders for a range of cells.
     * </p>
     *
     * @param range
     *            - {@link CellRangeAddress} range of cells on which colors are
     *            set.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     * @param extent
     *            - {@link CellStyleTemplate.BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>CellBorder.Extent.ALL</li>
     *            <li>CellBorder.Extent.INSIDE</li>
     *            </ul>
     */
    private void drawVerticalBorderColors(CellRangeAddress range, short color,
            BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstCol; i <= lastCol; i++) {
                CellRangeAddress row = new CellRangeAddress(firstRow, lastRow,
                        i, i);
                if (extent == BorderExtent.ALL || i > firstCol) {
                    drawLeftBorderColor(row, color);
                }
                if (extent == BorderExtent.ALL || i < lastCol) {
                    drawRightBorderColor(row, color);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported CellStyleTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this {@link CellStyleTemplate} for the
     * specified range.
     * 
     * @parm range - {@link CellRangeAddress} range of cells to remove borders.
     */
    private void removeBorderColors(CellRangeAddress range) {
        Set<String> properties = new HashSet<String>();
        properties.add(CellUtil.TOP_BORDER_COLOR);
        properties.add(CellUtil.BOTTOM_BORDER_COLOR);
        properties.add(CellUtil.LEFT_BORDER_COLOR);
        properties.add(CellUtil.RIGHT_BORDER_COLOR);
        for (int row = range.getFirstRow(); row <= range.getLastRow(); row++) {
            for (int col = range.getFirstColumn(); col <= range
                    .getLastColumn(); col++) {
                removeProperties(row, col, properties);
            }
        }
    }

    /**
     * Adds a property to this {@link CellStyleTemplate} for a given cell
     *
     * @param row
     * @param col
     * @param property
     * @param value
     */
    private void addProperty(int row, int col, String property, Object value) {
        CellAddress cell = new CellAddress(row, col);
        Map<String, Object> cellProperties = _cellStyleTemplate.get(cell);
        if (cellProperties == null) {
            cellProperties = new HashMap<String, Object>();
        }
        cellProperties.put(property, value);
        _cellStyleTemplate.put(cell, cellProperties);
    }
    
    /**
     * Adds a property to this {@link CellStyleTemplate} for a given cell
     *
     * @param row
     * @param col
     * @param property
     * @param value
     */
    private void addProperty(int row, int col, String property, short value) {
        addProperty(row, col, property, Short.valueOf(value));
    }

    /**
     * Adds a property to this {@link CellStyleTemplate} for a given cell
     *
     * @param row
     * @param col
     * @param borderProperty
     * @param borderStyle
     */
    private void addBorderStyle(int row, int col, String borderProperty, BorderStyle borderStyle) {
        addProperty(row, col, borderProperty, borderStyle);
    }

    /**
     * Removes a set of properties from this {@link CellStyleTemplate} for a
     * given cell
     *
     * @param row
     * @param col
     * @param properties
     */
    private void removeProperties(int row, int col, Set<String> properties) {
        CellAddress cell = new CellAddress(row, col);
        Map<String, Object> cellProperties = _cellStyleTemplate.get(cell);
        if (cellProperties != null) {
            cellProperties.keySet().removeAll(properties);
            if (cellProperties.isEmpty()) {
                _cellStyleTemplate.remove(cell);
            } else {
                _cellStyleTemplate.put(cell, cellProperties);
            }
        }
    }

    /**
     * Retrieves the number of borders assigned to a cell
     *
     * @param cell
     */
    public int getNumBorders(CellAddress cell) {
        Map<String, Object> cellProperties = _cellStyleTemplate.get(cell);
        if (cellProperties == null) {
            return 0;
        }

        int count = 0;
        for (String property : cellProperties.keySet()) {
            if (property.equals(CellUtil.BORDER_TOP))
                count += 1;
            if (property.equals(CellUtil.BORDER_BOTTOM))
                count += 1;
            if (property.equals(CellUtil.BORDER_LEFT))
                count += 1;
            if (property.equals(CellUtil.BORDER_RIGHT))
                count += 1;
        }
        return count;
    }

    /**
     * Retrieves the number of borders assigned to a cell
     *
     * @param row
     * @param col
     */
    public int getNumBorders(int row, int col) {
        return getNumBorders(new CellAddress(row, col));
    }

    /**
     * Retrieves the number of border colors assigned to a cell
     *
     * @param cell
     */
    public int getNumBorderColors(CellAddress cell) {
        Map<String, Object> cellProperties = _cellStyleTemplate.get(cell);
        if (cellProperties == null) {
            return 0;
        }

        int count = 0;
        for (String property : cellProperties.keySet()) {
            if (property.equals(CellUtil.TOP_BORDER_COLOR))
                count += 1;
            if (property.equals(CellUtil.BOTTOM_BORDER_COLOR))
                count += 1;
            if (property.equals(CellUtil.LEFT_BORDER_COLOR))
                count += 1;
            if (property.equals(CellUtil.RIGHT_BORDER_COLOR))
                count += 1;
        }
        return count;
    }

    /**
     * Retrieves the number of border colors assigned to a cell
     *
     * @param row
     * @param col
     */
    public int getNumBorderColors(int row, int col) {
        return getNumBorderColors(new CellAddress(row, col));
    }

    /**
     * Retrieves the border style for a given cell
     * 
     * @param cell
     * @param property
     */
    public short getTemplateProperty(CellAddress cell, String property) {
        short value = 0;
        Map<String, Object> cellProperties = _cellStyleTemplate.get(cell);
        if (cellProperties != null) {
            Object obj = cellProperties.get(property);
            if (obj != null) {
                value = getShort(obj);
            }
        }
        return value;
    }

    /**
     * Retrieves the border style for a given cell
     * 
     * @param row
     * @param col
     * @param property
     */
    public short getTemplateProperty(int row, int col, String property) {
        return getTemplateProperty(new CellAddress(row, col), property);
    }

    /**
     * Converts a Short object to a short value or 0 if the object is not a
     * Short
     *
     * @param value
     * @return short
     */
    private static short getShort(Object value) {
        if (value instanceof Short) {
            return ((Short) value).shortValue();
        }
        return 0;
    }
    
    /**
     * Returns true if the specified cell border is has been set, and is not {@link BorderStyle.NONE}
     *
     * @param cell
     *            - {@link CellAddress} of cell to test
     * @param cellBorder
     *            - String constant from {@link CellUtil} indicating which border to test
     * @return boolean
     */
    private boolean borderIsSet(CellAddress cell, String cellBorder) {
        Object borderLineStyle = getTemplateProperty(cell, cellBorder);
        return (borderLineStyle != null) && (borderLineStyle != BorderStyle.NONE);
    }
}
