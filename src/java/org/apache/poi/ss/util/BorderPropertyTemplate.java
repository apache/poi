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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Beta;

/**
 * <p>
 * A BorderPropertyTemplate is a template that can be applied to any sheet in
 * a project. It contains all the border type and color attributes needed to
 * draw all the borders for a single sheet. That template can be applied to any
 * sheet in any workbook.
 * 
 * This class requires the full spreadsheet to be in memory so
 * {@link SWorkbook} Spreadsheets are not supported. The same
 * BorderPropertyTemplate can, however, be applied to both
 * {@link org.apache.poi.hssf.usermodel.HSSFWorkbook}, and Workbook objects
 * if necessary. Portions of the border that fall outside the max range of the
 * {@link HSSFWorkbook} sheet are ignored.
 * </p>
 * 
 * <p>
 * This may be merged with {@link RegionUtil} in the future.
 * </p>
 */
@Beta
public final class BorderPropertyTemplate {

    /**
     * Provides various extents of the properties being added to the template
     * Note that the Border BorderExtent differs from a BorderStyle. A BorderStyle 
     * refers to the border around a single cell while a BorderExtent refers to
     * borders around and through an area of cells.
     */
    public enum BorderExtent {
        /**
         * No properties defined. This can be used to remove existing
         * properties.
         */
        NONE,

        /**
         * All borders, that is top, bottom, left and right, including interior
         * borders for the range. Does not include diagonals which are different
         * and not implemented here.
         */
        ALL,

        /**
         * All inside borders. This is top, bottom, left, and right borders, but
         * restricted to the interior borders for the range. For a range of one
         * cell, this will produce no borders.
         */
        INSIDE,

        /**
         * All outside borders. That is top, bottom, left and right borders that
         * bound the range only.
         */
        OUTSIDE,

        /**
         * This is just the top border for the range. No interior borders will
         * be produced.
         */
        TOP,

        /**
         * This is just the bottom border for the range. No interior borders
         * will be produced.
         */
        BOTTOM,

        /**
         * This is just the left border for the range, no interior borders will
         * be produced.
         */
        LEFT,

        /**
         * This is just the right border for the range, no interior borders will
         * be produced.
         */
        RIGHT,

        /**
         * This is all horizontal borders for the range, including interior and
         * outside borders.
         */
        HORIZONTAL,

        /**
         * This is just the interior horizontal borders for the range.
         */
        INSIDE_HORIZONTAL,

        /**
         * This is just the outside horizontal borders for the range.
         */
        OUTSIDE_HORIZONTAL,

        /**
         * This is all vertical borders for the range, including interior and
         * outside borders.
         */
        VERTICAL,

        /**
         * This is just the interior vertical borders for the range.
         */
        INSIDE_VERTICAL,

        /**
         * This is just the outside vertical borders for the range.
         */
        OUTSIDE_VERTICAL
    }
    
    /**
     * A set of the border color property names 
     */
    private static final Set<String> BORDER_COLOR_PROPERTY_NAMES;
    static {
        Set<String> properties = new HashSet<String>();
        properties.add(CellUtil.TOP_BORDER_COLOR);
        properties.add(CellUtil.BOTTOM_BORDER_COLOR);
        properties.add(CellUtil.LEFT_BORDER_COLOR);
        properties.add(CellUtil.RIGHT_BORDER_COLOR);
        BORDER_COLOR_PROPERTY_NAMES = Collections.unmodifiableSet(properties);
    }
    
    /**
     * A set of the border direction property names
     */
    private static final Set<String> BORDER_DIRECTION_PROPERTY_NAMES;
    static {
        Set<String> properties = new HashSet<String>();
        properties.add(CellUtil.BORDER_TOP);
        properties.add(CellUtil.BORDER_BOTTOM);
        properties.add(CellUtil.BORDER_LEFT);
        properties.add(CellUtil.BORDER_RIGHT);
        BORDER_DIRECTION_PROPERTY_NAMES = Collections.unmodifiableSet(properties);
    }
    
    // this must be greater than or equal to the workbooks used in #applyBorders
    private static final SpreadsheetVersion _ss = SpreadsheetVersion.EXCEL2007;

    /**
     * This is a list of cell properties for one shot application to a range of
     * cells at a later time.
     */
    private final Map<CellAddress, Map<String, Object>> _propertyTemplate;

    /**
     *
     */
    public BorderPropertyTemplate() {
        _propertyTemplate = new HashMap<CellAddress, Map<String, Object>>();
    }

    /**
     * Draws a group of cell borders for a cell range. The borders are not
     * applied to the cells at this time, just the template is drawn. To apply
     * the drawn borders to a sheet, use {@link #applyBorders}.
     * 
     * @param range  range of cells on which borders are drawn.
     * @param borderType Type of border to draw.
     * @param extent  BorderExtent of the borders to be applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderType, BorderExtent extent) {
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
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     * @param extent  BorderExtent of the borders to be applied.
     */
    public void drawBorders(CellRangeAddress range, BorderStyle borderType, short color, BorderExtent extent) {
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
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     */
    private void drawTopBorder(CellRangeAddress range, BorderStyle borderType) {
        int row = range.getFirstRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        boolean addBottom = (borderType == BorderStyle.NONE && row > 0);
        for (int i = firstCol; i <= lastCol; i++) {
            addProperty(row, i, CellUtil.BORDER_TOP, borderType);
            if (addBottom) {
                addProperty(row - 1, i, CellUtil.BORDER_BOTTOM, borderType);
            }
        }
    }

    /**
     * <p>
     * Draws the bottom border for a range of cells
     * </p>
     * 
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     */
    private void drawBottomBorder(CellRangeAddress range, BorderStyle borderType) {
        int row = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        boolean addTop = (borderType == BorderStyle.NONE && row < _ss.getLastRowIndex());
        for (int i = firstCol; i <= lastCol; i++) {
            addProperty(row, i, CellUtil.BORDER_BOTTOM, borderType);
            if (addTop) {
                addProperty(row + 1, i, CellUtil.BORDER_TOP, borderType);
            }
        }
    }

    /**
     * <p>
     * Draws the left border for a range of cells
     * </p>
     *
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     */
    private void drawLeftBorder(CellRangeAddress range, BorderStyle borderType) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getFirstColumn();
        boolean addRight = (borderType == BorderStyle.NONE && col > 0);
        for (int i = firstRow; i <= lastRow; i++) {
            addProperty(i, col, CellUtil.BORDER_LEFT, borderType);
            if (addRight) {
                addProperty(i, col - 1, CellUtil.BORDER_RIGHT, borderType);
            }
        }
    }

    /**
     * <p>
     * Draws the right border for a range of cells
     * </p>
     *
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     */
    private void drawRightBorder(CellRangeAddress range, BorderStyle borderType) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getLastColumn();
        boolean addLeft = (borderType == BorderStyle.NONE && col < _ss.getLastColumnIndex());
        for (int i = firstRow; i <= lastRow; i++) {
            addProperty(i, col, CellUtil.BORDER_RIGHT, borderType);
            if (addLeft) {
                addProperty(i, col + 1, CellUtil.BORDER_LEFT, borderType);
            }
        }
    }

    /**
     * <p>
     * Draws the outside borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     * @param extent  BorderExtent of the borders to be applied. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.HORIZONTAL</li>
     *            <li>Extent.VERTICAL</li>
     *            </ul>
     */
    private void drawOutsideBorders(CellRangeAddress range, BorderStyle borderType, BorderExtent extent) {
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
                    "Illegal BorderExtent. Allowed: ALL, HORIZONTAL, and VERTICAL");
        }
    }

    /**
     * <p>
     * Draws the horizontal borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     * @param extent  BorderExtent of the borders to be applied. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.INSIDE</li>
     *            </ul>
     */
    private void drawHorizontalBorders(CellRangeAddress range, BorderStyle borderType, BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstRow; i <= lastRow; i++) {
                CellRangeAddress row = new CellRangeAddress(i, i, firstCol, lastCol);
                if (extent == BorderExtent.ALL || i > firstRow) {
                    drawTopBorder(row, borderType);
                }
                if (extent == BorderExtent.ALL || i < lastRow) {
                    drawBottomBorder(row, borderType);
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Illegal BorderExtent. Allowed: ALL and INSIDE");
        }
    }

    /**
     * <p>
     * Draws the vertical borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which borders are drawn.
     * @param borderType  Type of border to draw.
     * @param extent  BorderExtent of the borders to be applied. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.INSIDE</li>
     *            </ul>
     */
    private void drawVerticalBorders(CellRangeAddress range, BorderStyle borderType, BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstCol; i <= lastCol; i++) {
                CellRangeAddress row = new CellRangeAddress(firstRow, lastRow, i, i);
                if (extent == BorderExtent.ALL || i > firstCol) {
                    drawLeftBorder(row, borderType);
                }
                if (extent == BorderExtent.ALL || i < lastCol) {
                    drawRightBorder(row, borderType);
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Illegal BorderExtent. Allowed: ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this BorderPropertyTemplate for the
     * specified range.
     * 
     * @param range - range of cells to remove borders.
     */
    private void removeBorders(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                removeProperties(row, col, BORDER_DIRECTION_PROPERTY_NAMES);
            }
        }
        removeBorderColors(range);
    }

    /**
     * Applies the drawn borders to a Sheet. The borders that are applied are
     * the ones that have been drawn by the {@link #drawBorders} and
     * {@link #drawBorderColors} methods.
     *
     * @param sheet  Sheet on which to apply borders
     */
    public void applyBorders(Sheet sheet) {
        SpreadsheetVersion ss = sheet.getWorkbook().getSpreadsheetVersion();
        int lastValidRow = ss.getLastRowIndex();
        int lastValidCol = ss.getLastColumnIndex();
        for (Entry<CellAddress, Map<String, Object>> entry : _propertyTemplate.entrySet()) {
            CellAddress cellAddress = entry.getKey();
            int r = cellAddress.getRow();
            int c = cellAddress.getColumn();
            if (r <= lastValidRow && c <= lastValidCol) {
                Map<String, Object> properties = entry.getValue();
                Row row = CellUtil.getRow(r, sheet);
                Cell cell = CellUtil.getCell(row, c);
                CellUtil.setCellStyleProperties(cell, properties);
            }
        }
    }

    /**
     * Sets the color for a group of cell borders for a cell range. The borders
     * are not applied to the cells at this time, just the template is drawn. If
     * the borders do not exist, a {@link BorderStyle#THIN} border is used. To apply the
     * drawn borders to a sheet, use {@link #applyBorders}.
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     * @param extent  BorderExtent of the borders for which colors are set.
     */
    public void drawBorderColors(CellRangeAddress range, short color, BorderExtent extent) {
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
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     */
    private void drawTopBorderColor(CellRangeAddress range, short color) {
        int row = range.getFirstRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            // if BORDER_TOP is not set on BorderPropertyTemplate, make a thin border so that there's something to color
            if (getTemplateProperty(row, i, CellUtil.BORDER_TOP) == null) {
                drawTopBorder(new CellRangeAddress(row, row, i, i), BorderStyle.THIN);
            }
            addProperty(row, i, CellUtil.TOP_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the bottom border for a range of cells.
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     */
    private void drawBottomBorderColor(CellRangeAddress range, short color) {
        int row = range.getLastRow();
        int firstCol = range.getFirstColumn();
        int lastCol = range.getLastColumn();
        for (int i = firstCol; i <= lastCol; i++) {
            // if BORDER_BOTTOM is not set on BorderPropertyTemplate, make a thin border so that there's something to color
            if (getTemplateProperty(row, i, CellUtil.BORDER_BOTTOM) == null) {
                drawBottomBorder(new CellRangeAddress(row, row, i, i), BorderStyle.THIN);
            }
            addProperty(row, i, CellUtil.BOTTOM_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the left border for a range of cells.
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     */
    private void drawLeftBorderColor(CellRangeAddress range, short color) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getFirstColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            // if BORDER_LEFT is not set on BorderPropertyTemplate, make a thin border so that there's something to color
            if (getTemplateProperty(i, col, CellUtil.BORDER_LEFT) == null) {
                drawLeftBorder(new CellRangeAddress(i, i, col, col), BorderStyle.THIN);
            }
            addProperty(i, col, CellUtil.LEFT_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the right border for a range of cells. If the border is
     * not drawn, it defaults to {@link BorderStyle#THIN}
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     */
    private void drawRightBorderColor(CellRangeAddress range, short color) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int col = range.getLastColumn();
        for (int i = firstRow; i <= lastRow; i++) {
            // if BORDER_RIGHT is not set on BorderPropertyTemplate, make a thin border so that there's something to color
            if (getTemplateProperty(i, col, CellUtil.BORDER_RIGHT) == null) {
                drawRightBorder(new CellRangeAddress(i, i, col, col), BorderStyle.THIN);
            }
            addProperty(i, col, CellUtil.RIGHT_BORDER_COLOR, color);
        }
    }

    /**
     * <p>
     * Sets the color of the outside borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     * @param extent  BorderExtent of the borders for which colors are set. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.HORIZONTAL</li>
     *            <li>Extent.VERTICAL</li>
     *            </ul>
     */
    private void drawOutsideBorderColors(CellRangeAddress range, short color, BorderExtent extent) {
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
                    "Illegal BorderExtent. Allowed: ALL, HORIZONTAL, and VERTICAL");
        }
    }

    /**
     * <p>
     * Sets the color of the horizontal borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     * @param extent  BorderExtent of the borders for which colors are set. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.INSIDE</li>
     *            </ul>
     */
    private void drawHorizontalBorderColors(CellRangeAddress range, short color, BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstRow; i <= lastRow; i++) {
                CellRangeAddress row = new CellRangeAddress(i, i, firstCol, lastCol);
                if (extent == BorderExtent.ALL || i > firstRow) {
                    drawTopBorderColor(row, color);
                }
                if (extent == BorderExtent.ALL || i < lastRow) {
                    drawBottomBorderColor(row, color);
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Illegal BorderExtent. Allowed: ALL and INSIDE");
        }
    }

    /**
     * <p>
     * Sets the color of the vertical borders for a range of cells.
     * </p>
     *
     * @param range  range of cells on which colors are set.
     * @param color  Color index from {@link IndexedColors} used to draw the borders.
     * @param extent  BorderExtent of the borders for which colors are set. Valid Values are:
     *            <ul>
     *            <li>Extent.ALL</li>
     *            <li>Extent.INSIDE</li>
     *            </ul>
     */
    private void drawVerticalBorderColors(CellRangeAddress range, short color, BorderExtent extent) {
        switch (extent) {
        case ALL:
        case INSIDE:
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            int firstCol = range.getFirstColumn();
            int lastCol = range.getLastColumn();
            for (int i = firstCol; i <= lastCol; i++) {
                CellRangeAddress row = new CellRangeAddress(firstRow, lastRow, i, i);
                if (extent == BorderExtent.ALL || i > firstCol) {
                    drawLeftBorderColor(row, color);
                }
                if (extent == BorderExtent.ALL || i < lastCol) {
                    drawRightBorderColor(row, color);
                }
            }
            break;
        default:
            throw new IllegalArgumentException("Illegal BorderExtent. Allowed: ALL and INSIDE");
        }
    }

    /**
     * Removes all border properties from this BorderPropertyTemplate for the
     * specified range.
     * 
     * @param range - range of cells to remove borders.
     */
    private void removeBorderColors(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int lastRow = range.getLastRow();
        int firstColumn = range.getFirstColumn();
        int lastColumn = range.getLastColumn();
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstColumn; col <= lastColumn; col++) {
                removeProperties(row, col, BORDER_COLOR_PROPERTY_NAMES);
            }
        }
    }

    /**
     * Adds a property to this BorderPropertyTemplate for a given cell
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
            cellProperties = new HashMap<String, Object>();
        }
        cellProperties.put(property, value);
        _propertyTemplate.put(cell, cellProperties);
    }

    /**
     * Removes a set of properties from this BorderPropertyTemplate for a
     * given cell
     *
     * @param row the row index of the cell to remove properties from
     * @param col the column index of the cell to remove properties from
     * @param properties a list of the property names to remove from the cell
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
     * Retrieves the number of borders assigned to a cell (a value between 0 and 4)
     *
     * @param cell the cell to count the number of borders on
     */
    public int getNumBorders(CellAddress cell) {
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
        if (cellProperties == null) {
            return 0;
        }

        int count = 0;
        if (cellProperties.containsKey(CellUtil.BORDER_TOP)) count++;
        if (cellProperties.containsKey(CellUtil.BORDER_LEFT)) count++;
        if (cellProperties.containsKey(CellUtil.BORDER_RIGHT)) count++;
        if (cellProperties.containsKey(CellUtil.BORDER_BOTTOM)) count++;
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
        if (cellProperties.containsKey(CellUtil.TOP_BORDER_COLOR)) count++;
        if (cellProperties.containsKey(CellUtil.LEFT_BORDER_COLOR)) count++;
        if (cellProperties.containsKey(CellUtil.RIGHT_BORDER_COLOR)) count++;
        if (cellProperties.containsKey(CellUtil.BOTTOM_BORDER_COLOR)) count++;
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
    public Object getTemplateProperty(CellAddress cell, String property) {
        Map<String, Object> cellProperties = _propertyTemplate.get(cell);
        if (cellProperties != null) {
            return cellProperties.get(property);
        }
        return null;
    }

    /**
     * Retrieves the border style for a given cell
     * 
     * @param row
     * @param col
     * @param property
     */
    public Object getTemplateProperty(int row, int col, String property) {
        return getTemplateProperty(new CellAddress(row, col), property);
    }
}
