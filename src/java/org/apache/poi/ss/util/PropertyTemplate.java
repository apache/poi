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
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * <p>
 * A {@link PropertyTemplate} is a template that can be applied to any sheet in
 * a project. It contains all the border type and color attributes needed to
 * draw all the borders for a single sheet. That template can be applied to any
 * sheet in any workbook.
 * 
 * This class requires the full spreadsheet to be in memory, so 
 * {@link org.apache.poi.xssf.streaming.SXSSFWorkbook} Spreadsheets are not
 * supported. The same {@link PropertyTemplate} can, however, be applied to both
 * {@link HSSFWorkbook} and {@link org.apache.poi.xssf.usermodel.XSSFWorkbook}
 * objects if necessary. Portions of the border that fall outside the max range
 * of the {@link Workbook} sheet are ignored.
 * </p>
 * 
 * <p>
 * This would replace {@link RegionUtil}.
 * </p>
 */
public final class PropertyTemplate {

    /**
     * This is a list of cell properties for one shot application to a range of
     * cells at a later time.
     */
    private Map<CellAddress, Map<String, Object>> _propertyTemplate;

    /**
     * Create a PropertyTemplate object
     */
    public PropertyTemplate() {
        _propertyTemplate = new HashMap<>();
    }
    
    /**
     * Create a PropertyTemplate object from another PropertyTemplate
     *
     * @param template a PropertyTemplate object
     */
    public PropertyTemplate(PropertyTemplate template) {
        this();
        for(Map.Entry<CellAddress, Map<String, Object>> entry : template.getTemplate().entrySet()) {
            _propertyTemplate.put(new CellAddress(entry.getKey()), cloneCellProperties(entry.getValue()));
        }
    }
    
    private Map<CellAddress,Map<String, Object>> getTemplate() {
        return _propertyTemplate;
    }
    
    private static Map<String, Object> cloneCellProperties(Map<String, Object> properties) {
        Map<String, Object> newProperties = new HashMap<>();
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            newProperties.put(entry.getKey(), entry.getValue());
        }
        return newProperties;
    }
    
    /**
     * Draws a group of cell borders for a cell range. The borders are not
     * applied to the cells at this time, just the template is drawn. To apply
     * the drawn borders to a sheet, use {@link #applyBorders}.
     * 
     * @param range
     *            - {@link CellRangeAddress} range of cells on which borders are
     *            drawn.
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be
     *            applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderType,
            BorderExtent extent) {
        switch (extent) {
        case NONE:
            removeBorders(range);
            break;
        case ALL:
            drawHorizontalBorders(range, borderType, BorderExtent.ALL);
            drawVerticalBorders(range, borderType, BorderExtent.ALL);
            break;
        case INSIDE:
            drawHorizontalBorders(range, borderType, BorderExtent.INSIDE);
            drawVerticalBorders(range, borderType, BorderExtent.INSIDE);
            break;
        case OUTSIDE:
            drawOutsideBorders(range, borderType, BorderExtent.ALL);
            break;
        case TOP:
            drawTopBorder(range, borderType);
            break;
        case BOTTOM:
            drawBottomBorder(range, borderType);
            break;
        case LEFT:
            drawLeftBorder(range, borderType);
            break;
        case RIGHT:
            drawRightBorder(range, borderType);
            break;
        case HORIZONTAL:
            drawHorizontalBorders(range, borderType, BorderExtent.ALL);
            break;
        case INSIDE_HORIZONTAL:
            drawHorizontalBorders(range, borderType, BorderExtent.INSIDE);
            break;
        case OUTSIDE_HORIZONTAL:
            drawOutsideBorders(range, borderType, BorderExtent.HORIZONTAL);
            break;
        case VERTICAL:
            drawVerticalBorders(range, borderType, BorderExtent.ALL);
            break;
        case INSIDE_VERTICAL:
            drawVerticalBorders(range, borderType, BorderExtent.INSIDE);
            break;
        case OUTSIDE_VERTICAL:
            drawOutsideBorders(range, borderType, BorderExtent.VERTICAL);
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     * @param color
     *            - Color index from {@link IndexedColors} used to draw the
     *            borders.
     * @param extent
     *            - {@link BorderExtent} of the borders to be
     *            applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderType,
            short color, BorderExtent extent) {
        drawBorders(range, borderType, extent);
        if (borderType != BorderStyle.NONE) {
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawTopBorder(CellRangeAddress range, BorderStyle borderType) {
        int row = range.getFirstRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            addProperty(row, i, CellUtil.BORDER_TOP, borderType);
            if (borderType == BorderStyle.NONE && row > 0) {
                addProperty(row - 1, i, CellUtil.BORDER_BOTTOM, borderType);
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawBottomBorder(CellRangeAddress range,
            BorderStyle borderType) {
        int row = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            addProperty(row, i, CellUtil.BORDER_BOTTOM, borderType);
            if (borderType == BorderStyle.NONE
                    && row < SpreadsheetVersion.EXCEL2007.getMaxRows() - 1) {
                addProperty(row + 1, i, CellUtil.BORDER_TOP, borderType);
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawLeftBorder(CellRangeAddress range,
            BorderStyle borderType) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getFirstColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            addProperty(i, col, CellUtil.BORDER_LEFT, borderType);
            if (borderType == BorderStyle.NONE && col > 0) {
                addProperty(i, col - 1, CellUtil.BORDER_RIGHT, borderType);
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     */
    private void drawRightBorder(CellRangeAddress range,
            BorderStyle borderType) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getLastColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            addProperty(i, col, CellUtil.BORDER_RIGHT, borderType);
            if (borderType == BorderStyle.NONE
                    && col < SpreadsheetVersion.EXCEL2007.getMaxColumns() - 1) {
                addProperty(i, col + 1, CellUtil.BORDER_LEFT, borderType);
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.HORIZONTAL</li>
     *            <li>BorderExtent.VERTICAL</li>
     *            </ul>
     */
    private void drawOutsideBorders(CellRangeAddress range,
            BorderStyle borderType, BorderExtent extent) {
        switch (extent) {
        case ALL:
        case HORIZONTAL:
        case VERTICAL:
            if (extent == BorderExtent.ALL || extent == BorderExtent.HORIZONTAL) {
                drawTopBorder(range, borderType);
                drawBottomBorder(range, borderType);
            }
            if (extent == BorderExtent.ALL || extent == BorderExtent.VERTICAL) {
                drawLeftBorder(range, borderType);
                drawRightBorder(range, borderType);
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL, HORIZONTAL, and VERTICAL");
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.INSIDE</li>
     *            </ul>
     */
    private void drawHorizontalBorders(CellRangeAddress range,
            BorderStyle borderType, BorderExtent extent) {
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
                    drawTopBorder(row, borderType);
                }
                if (extent == BorderExtent.ALL || i < lastRow) {
                    drawBottomBorder(row, borderType);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL and INSIDE");
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
     * @param borderType
     *            - Type of border to draw. {@link BorderStyle}.
     * @param extent
     *            - {@link BorderExtent} of the borders to be
     *            applied. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.INSIDE</li>
     *            </ul>
     */
    private void drawVerticalBorders(CellRangeAddress range,
            BorderStyle borderType, BorderExtent extent) {
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
                    drawLeftBorder(row, borderType);
                }
                if (extent == BorderExtent.ALL || i < lastCol) {
                    drawRightBorder(row, borderType);
                }
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this {@link PropertyTemplate} for the
     * specified range.
     * 
     * @parm range - {@link CellRangeAddress} range of cells to remove borders.
     */
    private void removeBorders(CellRangeAddress range) {
        Set<String> properties = new HashSet<>();
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
        for (Map.Entry<CellAddress, Map<String, Object>> entry : _propertyTemplate
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
     *            - {@link BorderExtent} of the borders for which
     *            colors are set.
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
            if (getBorderStyle(row, i,
                    CellUtil.BORDER_TOP) == BorderStyle.NONE) {
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
            if (getBorderStyle(row, i,
                    CellUtil.BORDER_BOTTOM) == BorderStyle.NONE) {
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
            if (getBorderStyle(i, col,
                    CellUtil.BORDER_LEFT) == BorderStyle.NONE) {
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
            if (getBorderStyle(i, col,
                    CellUtil.BORDER_RIGHT) == BorderStyle.NONE) {
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
     *            - {@link BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.HORIZONTAL</li>
     *            <li>BorderExtent.VERTICAL</li>
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
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL, HORIZONTAL, and VERTICAL");
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
     *            - {@link BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.INSIDE</li>
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
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL and INSIDE");
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
     *            - {@link BorderExtent} of the borders for which
     *            colors are set. Valid Values are:
     *            <ul>
     *            <li>BorderExtent.ALL</li>
     *            <li>BorderExtent.INSIDE</li>
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
                    "Unsupported PropertyTemplate.Extent, valid Extents are ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this {@link PropertyTemplate} for the
     * specified range.
     * 
     * @parm range - {@link CellRangeAddress} range of cells to remove borders.
     */
    private void removeBorderColors(CellRangeAddress range) {
        Set<String> properties = new HashSet<>();
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
     * Adds a property to this {@link PropertyTemplate} for a given cell
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
     * Adds a property to this {@link PropertyTemplate} for a given cell
     *
     * @param row
     * @param col
     * @param property
     * @param value
     */
    private void addProperty(int row, int col, String property, Object value) {
        CellAddress cell = new CellAddress(row, col);
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
        if (cellProperties == null) {
            cellProperties = new HashMap<>();
        }
        cellProperties.put(property, value);
        _propertyTemplate.put(cell, cellProperties);
    }

    /**
     * Removes a set of properties from this {@link PropertyTemplate} for a
     * given cell
     *
     * @param row
     * @param col
     * @param properties
     */
    private void removeProperties(int row, int col, Set<String> properties) {
        CellAddress cell = new CellAddress(row, col);
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
        if (cellProperties != null) {
            cellProperties.keySet().removeAll(properties);
            if (cellProperties.isEmpty()) {
                _propertyTemplate.remove(cell);
            } else {
                _propertyTemplate.put(cell, cellProperties);
            }
        }
    }

    /**
     * Retrieves the number of borders assigned to a cell
     *
     * @param cell
     */
    public int getNumBorders(CellAddress cell) {
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
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
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
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
    public BorderStyle getBorderStyle(CellAddress cell, String property) {
        BorderStyle value = BorderStyle.NONE;
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
        if (cellProperties != null) {
            Object obj = cellProperties.get(property);
            if (obj instanceof BorderStyle) {
                value = (BorderStyle) obj;
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
    public BorderStyle getBorderStyle(int row, int col, String property) {
        return getBorderStyle(new CellAddress(row, col), property);
    }

    /**
     * Retrieves the border style for a given cell
     * 
     * @param cell
     * @param property
     */
    public short getTemplateProperty(CellAddress cell, String property) {
        short value = 0;
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
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
     * @param value Potentially short value to convert
     * @return short value, or 0 if not a short
     */
    private static short getShort(Object value) {
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return 0;
    }
}
