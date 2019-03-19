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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Various utility functions that make working with a cells and rows easier. The various methods
 * that deal with style's allow you to create your CellStyles as you need them. When you apply a
 * style change to a cell, the code will attempt to see if a style already exists that meets your
 * needs. If not, then it will create a new style. This is to prevent creating too many styles.
 * there is an upper limit in Excel on the number of styles that can be supported.
 *
 *@author Eric Pugh epugh@upstate.com
 *@author (secondary) Avinash Kewalramani akewalramani@accelrys.com
 */
public final class CellUtil {
    
    private static final POILogger log = POILogFactory.getLogger(CellUtil.class);

    // FIXME: Move these constants into an enum
    public static final String ALIGNMENT = "alignment";
    public static final String BORDER_BOTTOM = "borderBottom";
    public static final String BORDER_LEFT = "borderLeft";
    public static final String BORDER_RIGHT = "borderRight";
    public static final String BORDER_TOP = "borderTop";
    public static final String BOTTOM_BORDER_COLOR = "bottomBorderColor";
    public static final String LEFT_BORDER_COLOR = "leftBorderColor";
    public static final String RIGHT_BORDER_COLOR = "rightBorderColor";
    public static final String TOP_BORDER_COLOR = "topBorderColor";
    public static final String DATA_FORMAT = "dataFormat";
    public static final String FILL_BACKGROUND_COLOR = "fillBackgroundColor";
    public static final String FILL_FOREGROUND_COLOR = "fillForegroundColor";
    public static final String FILL_PATTERN = "fillPattern";
    public static final String FONT = "font";
    public static final String HIDDEN = "hidden";
    public static final String INDENTION = "indention";
    public static final String LOCKED = "locked";
    public static final String ROTATION = "rotation";
    public static final String VERTICAL_ALIGNMENT = "verticalAlignment";
    public static final String WRAP_TEXT = "wrapText";
    
    private static final Set<String> shortValues = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    BOTTOM_BORDER_COLOR,
                    LEFT_BORDER_COLOR,
                    RIGHT_BORDER_COLOR,
                    TOP_BORDER_COLOR,
                    FILL_FOREGROUND_COLOR,
                    FILL_BACKGROUND_COLOR,
                    INDENTION,
                    DATA_FORMAT,
                    ROTATION
            )));
    private static final Set<String> intValues = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    FONT
            )));
    private static final Set<String> booleanValues = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    LOCKED,
                    HIDDEN,
                    WRAP_TEXT
            )));
    private static final Set<String> borderTypeValues = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    BORDER_BOTTOM,
                    BORDER_LEFT,
                    BORDER_RIGHT,
                    BORDER_TOP
            )));


    private static UnicodeMapping[] unicodeMappings;

    private static final class UnicodeMapping {

        public final String entityName;
        public final String resolvedValue;

        public UnicodeMapping(String pEntityName, String pResolvedValue) {
            entityName = "&" + pEntityName + ";";
            resolvedValue = pResolvedValue;
        }
    }

    private CellUtil() {
        // no instances of this class
    }

    /**
     * Get a row from the spreadsheet, and create it if it doesn't exist.
     *
     * @param rowIndex The 0 based row number
     * @param sheet The sheet that the row is part of.
     * @return The row indicated by the rowCounter
     */
    public static Row getRow(int rowIndex, Sheet sheet) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }


    /**
     * Get a specific cell from a row. If the cell doesn't exist, then create it.
     *
     * @param row The row that the cell is part of
     * @param columnIndex The column index that the cell is in.
     * @return The cell indicated by the column.
     */
    public static Cell getCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }


    /**
     * Creates a cell, gives it a value, and applies a style if provided
     *
     * @param  row     the row to create the cell in
     * @param  column  the column index to create the cell in
     * @param  value   The value of the cell
     * @param  style   If the style is not null, then set
     * @return         A new Cell
     */
    public static Cell createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = getCell(row, column);

        cell.setCellValue(cell.getRow().getSheet().getWorkbook().getCreationHelper()
                .createRichTextString(value));
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }


    /**
     * Create a cell, and give it a value.
     *
     *@param  row     the row to create the cell in
     *@param  column  the column index to create the cell in
     *@param  value   The value of the cell
     *@return         A new Cell.
     */
    public static Cell createCell(Row row, int column, String value) {
        return createCell(row, column, value, null);
    }

    /**
     * Take a cell, and align it.
     * 
     * This is superior to cell.getCellStyle().setAlignment(align) because
     * this method will not modify the CellStyle object that may be referenced
     * by multiple cells. Instead, this method will search for existing CellStyles
     * that match the desired CellStyle, creating a new CellStyle with the desired
     * style if no match exists.
     *
     * @param cell the cell to set the alignment for
     * @param align the horizontal alignment to use.
     *
     * @see HorizontalAlignment for alignment options
     * @since POI 3.15 beta 3
     */
    public static void setAlignment(Cell cell, HorizontalAlignment align) {
        setCellStyleProperty(cell, ALIGNMENT, align);
    }
    
    /**
     * Take a cell, and vertically align it.
     * 
     * This is superior to cell.getCellStyle().setVerticalAlignment(align) because
     * this method will not modify the CellStyle object that may be referenced
     * by multiple cells. Instead, this method will search for existing CellStyles
     * that match the desired CellStyle, creating a new CellStyle with the desired
     * style if no match exists.
     *
     * @param cell the cell to set the alignment for
     * @param align the vertical alignment to use.
     *
     * @see VerticalAlignment for alignment options
     * @since POI 3.15 beta 3
     */
    public static void setVerticalAlignment(Cell cell, VerticalAlignment align) {
        setCellStyleProperty(cell, VERTICAL_ALIGNMENT, align);
    }
    
    /**
     * Take a cell, and apply a font to it
     *
     * @param cell the cell to set the alignment for
     * @param font The Font that you want to set.
     * @throws IllegalArgumentException if <tt>font</tt> and <tt>cell</tt> do not belong to the same workbook
     */
    public static void setFont(Cell cell, Font font) {
        // Check if font belongs to workbook
        Workbook wb = cell.getSheet().getWorkbook();
        final int fontIndex = font.getIndexAsInt();
        if (!wb.getFontAt(fontIndex).equals(font)) {
            throw new IllegalArgumentException("Font does not belong to this workbook");
        }

        // Check if cell belongs to workbook
        // (checked in setCellStyleProperty)

        setCellStyleProperty(cell, FONT, fontIndex);
    }

    /**
     * <p>This method attempts to find an existing CellStyle that matches the <code>cell</code>'s 
     * current style plus styles properties in <code>properties</code>. A new style is created if the
     * workbook does not contain a matching style.</p>
     * 
     * <p>Modifies the cell style of <code>cell</code> without affecting other cells that use the
     * same style.</p>
     * 
     * <p>This is necessary because Excel has an upper limit on the number of styles that it supports.</p>
     * 
     * <p>This function is more efficient than multiple calls to
     * {@link #setCellStyleProperty(org.apache.poi.ss.usermodel.Cell, String, Object)}
     * if adding multiple cell styles.</p>
     * 
     * <p>For performance reasons, if this is the only cell in a workbook that uses a cell style,
     * this method does NOT remove the old style from the workbook.
     * <!-- NOT IMPLEMENTED: Unused styles should be
     * pruned from the workbook with [@link #removeUnusedCellStyles(Workbook)] or
     * [@link #removeStyleFromWorkbookIfUnused(CellStyle, Workbook)]. -->
     * </p>
     *
     * @param cell The cell to change the style of
     * @param properties The properties to be added to a cell style, as {propertyName: propertyValue}.
     * @since POI 3.14 beta 2
     */
    public static void setCellStyleProperties(Cell cell, Map<String, Object> properties) {
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle originalStyle = cell.getCellStyle();
        CellStyle newStyle = null;
        Map<String, Object> values = getFormatProperties(originalStyle);
        putAll(properties, values);

        // index seems like what index the cellstyle is in the list of styles for a workbook.
        // not good to compare on!
        int numberCellStyles = workbook.getNumCellStyles();

        for (int i = 0; i < numberCellStyles; i++) {
            CellStyle wbStyle = workbook.getCellStyleAt(i);
            Map<String, Object> wbStyleMap = getFormatProperties(wbStyle);

            // the desired style already exists in the workbook. Use the existing style.
            if (wbStyleMap.equals(values)) {
                newStyle = wbStyle;
                break;
            }
        }

        // the desired style does not exist in the workbook. Create a new style with desired properties.
        if (newStyle == null) {
            newStyle = workbook.createCellStyle();
            setFormatProperties(newStyle, workbook, values);
        }

        cell.setCellStyle(newStyle);
    }

    /**
     * <p>This method attempts to find an existing CellStyle that matches the <code>cell</code>'s
     * current style plus a single style property <code>propertyName</code> with value
     * <code>propertyValue</code>.
     * A new style is created if the workbook does not contain a matching style.</p>
     * 
     * <p>Modifies the cell style of <code>cell</code> without affecting other cells that use the
     * same style.</p>
     * 
     * <p>If setting more than one cell style property on a cell, use
     * {@link #setCellStyleProperties(org.apache.poi.ss.usermodel.Cell, Map)},
     * which is faster and does not add unnecessary intermediate CellStyles to the workbook.</p>
     * 
     * @param cell The cell that is to be changed.
     * @param propertyName The name of the property that is to be changed.
     * @param propertyValue The value of the property that is to be changed.
     */
    public static void setCellStyleProperty(Cell cell, String propertyName, Object propertyValue) {
        Map<String, Object> property = Collections.singletonMap(propertyName, propertyValue);
        setCellStyleProperties(cell, property);
    }

    /**
     * Returns a map containing the format properties of the given cell style.
     * The returned map is not tied to <code>style</code>, so subsequent changes
     * to <code>style</code> will not modify the map, and changes to the returned
     * map will not modify the cell style. The returned map is mutable.
     *
     * @param style cell style
     * @return map of format properties (String -> Object)
     * @see #setFormatProperties(org.apache.poi.ss.usermodel.CellStyle, org.apache.poi.ss.usermodel.Workbook, java.util.Map)
     */
    private static Map<String, Object> getFormatProperties(CellStyle style) {
        Map<String, Object> properties = new HashMap<>();
        put(properties, ALIGNMENT, style.getAlignment());
        put(properties, VERTICAL_ALIGNMENT, style.getVerticalAlignment());
        put(properties, BORDER_BOTTOM, style.getBorderBottom());
        put(properties, BORDER_LEFT, style.getBorderLeft());
        put(properties, BORDER_RIGHT, style.getBorderRight());
        put(properties, BORDER_TOP, style.getBorderTop());
        put(properties, BOTTOM_BORDER_COLOR, style.getBottomBorderColor());
        put(properties, DATA_FORMAT, style.getDataFormat());
        put(properties, FILL_PATTERN, style.getFillPattern());
        put(properties, FILL_FOREGROUND_COLOR, style.getFillForegroundColor());
        put(properties, FILL_BACKGROUND_COLOR, style.getFillBackgroundColor());
        put(properties, FONT, style.getFontIndexAsInt());
        put(properties, HIDDEN, style.getHidden());
        put(properties, INDENTION, style.getIndention());
        put(properties, LEFT_BORDER_COLOR, style.getLeftBorderColor());
        put(properties, LOCKED, style.getLocked());
        put(properties, RIGHT_BORDER_COLOR, style.getRightBorderColor());
        put(properties, ROTATION, style.getRotation());
        put(properties, TOP_BORDER_COLOR, style.getTopBorderColor());
        put(properties, WRAP_TEXT, style.getWrapText());
        return properties;
    }
    
    /**
     * Copies the entries in src to dest, using the preferential data type
     * so that maps can be compared for equality
     *
     * @param src the property map to copy from (read-only)
     * @param dest the property map to copy into
     * @since POI 3.15 beta 3
     */
    private static void putAll(final Map<String, Object> src, Map<String, Object> dest) {
        for (final String key : src.keySet()) {
            if (shortValues.contains(key)) {
                dest.put(key, getShort(src, key));
            } else if (intValues.contains(key)) {
                dest.put(key, getInt(src, key));
            } else if (booleanValues.contains(key)) {
                dest.put(key, getBoolean(src, key));
            } else if (borderTypeValues.contains(key)) {
                dest.put(key, getBorderStyle(src, key));
            } else if (ALIGNMENT.equals(key)) {
                dest.put(key, getHorizontalAlignment(src, key));
            } else if (VERTICAL_ALIGNMENT.equals(key)) {
                dest.put(key, getVerticalAlignment(src, key));
            } else if (FILL_PATTERN.equals(key)) {
                dest.put(key, getFillPattern(src, key));
            } else {
                if (log.check(POILogger.INFO)) {
                    log.log(POILogger.INFO, "Ignoring unrecognized CellUtil format properties key: " + key);
                }
            }
        }
    }

    /**
     * Sets the format properties of the given style based on the given map.
     *
     * @param style cell style
     * @param workbook parent workbook
     * @param properties map of format properties (String -> Object)
     * @see #getFormatProperties(CellStyle)
     */
    private static void setFormatProperties(CellStyle style, Workbook workbook, Map<String, Object> properties) {
        style.setAlignment(getHorizontalAlignment(properties, ALIGNMENT));
        style.setVerticalAlignment(getVerticalAlignment(properties, VERTICAL_ALIGNMENT));
        style.setBorderBottom(getBorderStyle(properties, BORDER_BOTTOM));
        style.setBorderLeft(getBorderStyle(properties, BORDER_LEFT));
        style.setBorderRight(getBorderStyle(properties, BORDER_RIGHT));
        style.setBorderTop(getBorderStyle(properties, BORDER_TOP));
        style.setBottomBorderColor(getShort(properties, BOTTOM_BORDER_COLOR));
        style.setDataFormat(getShort(properties, DATA_FORMAT));
        style.setFillPattern(getFillPattern(properties, FILL_PATTERN));
        style.setFillForegroundColor(getShort(properties, FILL_FOREGROUND_COLOR));
        style.setFillBackgroundColor(getShort(properties, FILL_BACKGROUND_COLOR));
        style.setFont(workbook.getFontAt(getInt(properties, FONT)));
        style.setHidden(getBoolean(properties, HIDDEN));
        style.setIndention(getShort(properties, INDENTION));
        style.setLeftBorderColor(getShort(properties, LEFT_BORDER_COLOR));
        style.setLocked(getBoolean(properties, LOCKED));
        style.setRightBorderColor(getShort(properties, RIGHT_BORDER_COLOR));
        style.setRotation(getShort(properties, ROTATION));
        style.setTopBorderColor(getShort(properties, TOP_BORDER_COLOR));
        style.setWrapText(getBoolean(properties, WRAP_TEXT));
    }

    /**
     * Utility method that returns the named short value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return zero if the property does not exist, or is not a {@link Short}
     *         otherwise the property value
     */
    private static short getShort(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return 0;
    }

    /**
     * Utility method that returns the named int value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return zero if the property does not exist, or is not a {@link Integer}
     *         otherwise the property value
     */
    private static int getInt(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    /**
     * Utility method that returns the named BorderStyle value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return Border style if set, otherwise {@link BorderStyle#NONE}
     */
    private static BorderStyle getBorderStyle(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        BorderStyle border;
        if (value instanceof BorderStyle) {
            border = (BorderStyle) value;
        }
        // @deprecated 3.15 beta 2. getBorderStyle will only work on BorderStyle enums instead of codes in the future.
        else if (value instanceof Short) {
            if (log.check(POILogger.WARN)) {
                log.log(POILogger.WARN, "Deprecation warning: CellUtil properties map uses Short values for "
                        + name + ". Should use BorderStyle enums instead.");
            }
            short code = ((Short) value).shortValue();
            border = BorderStyle.valueOf(code);
        }
        else if (value == null) {
            border = BorderStyle.NONE;
        }
        else {
            throw new RuntimeException("Unexpected border style class. Must be BorderStyle or Short (deprecated).");
        }
        return border;
    }
    
    /**
     * Utility method that returns the named FillPatternType value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return FillPatternType style if set, otherwise {@link FillPatternType#NO_FILL}
     * @since POI 3.15 beta 3
     */
    private static FillPatternType getFillPattern(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        FillPatternType pattern;
        if (value instanceof FillPatternType) {
            pattern = (FillPatternType) value;
        }
        // @deprecated 3.15 beta 2. getFillPattern will only work on FillPatternType enums instead of codes in the future.
        else if (value instanceof Short) {
            if (log.check(POILogger.WARN)) {
                log.log(POILogger.WARN, "Deprecation warning: CellUtil properties map uses Short values for "
                        + name + ". Should use FillPatternType enums instead.");
            }
            short code = ((Short) value).shortValue();
            pattern = FillPatternType.forInt(code);
        }
        else if (value == null) {
            pattern = FillPatternType.NO_FILL;
        }
        else {
            throw new RuntimeException("Unexpected fill pattern style class. Must be FillPatternType or Short (deprecated).");
        }
        return pattern;
    }
    
    /**
     * Utility method that returns the named HorizontalAlignment value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return HorizontalAlignment style if set, otherwise {@link HorizontalAlignment#GENERAL}
     * @since POI 3.15 beta 3
     */
    private static HorizontalAlignment getHorizontalAlignment(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        HorizontalAlignment align;
        if (value instanceof HorizontalAlignment) {
            align = (HorizontalAlignment) value;
        }
        // @deprecated 3.15 beta 2. getHorizontalAlignment will only work on HorizontalAlignment enums instead of codes in the future.
        else if (value instanceof Short) {
            if (log.check(POILogger.WARN)) {
                log.log(POILogger.WARN, "Deprecation warning: CellUtil properties map used a Short value for "
                        + name + ". Should use HorizontalAlignment enums instead.");
            }
            short code = ((Short) value).shortValue();
            align = HorizontalAlignment.forInt(code);
        }
        else if (value == null) {
            align = HorizontalAlignment.GENERAL;
        }
        else {
            throw new RuntimeException("Unexpected horizontal alignment style class. Must be HorizontalAlignment or Short (deprecated).");
        }
        return align;
    }
    
    /**
     * Utility method that returns the named VerticalAlignment value from the given map.
     *
     * @param properties map of named properties (String -> Object)
     * @param name property name
     * @return VerticalAlignment style if set, otherwise {@link VerticalAlignment#BOTTOM}
     * @since POI 3.15 beta 3
     */
    private static VerticalAlignment getVerticalAlignment(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        VerticalAlignment align;
        if (value instanceof VerticalAlignment) {
            align = (VerticalAlignment) value;
        }
        // @deprecated 3.15 beta 2. getVerticalAlignment will only work on VerticalAlignment enums instead of codes in the future.
        else if (value instanceof Short) {
            if (log.check(POILogger.WARN)) {
                log.log(POILogger.WARN, "Deprecation warning: CellUtil properties map used a Short value for "
                        + name + ". Should use VerticalAlignment enums instead.");
            }
            short code = ((Short) value).shortValue();
            align = VerticalAlignment.forInt(code);
        }
        else if (value == null) {
            align = VerticalAlignment.BOTTOM;
        }
        else {
            throw new RuntimeException("Unexpected vertical alignment style class. Must be VerticalAlignment or Short (deprecated).");
        }
        return align;
    }

    /**
     * Utility method that returns the named boolean value from the given map.
     *
     * @param properties map of properties (String -> Object)
     * @param name property name
     * @return false if the property does not exist, or is not a {@link Boolean},
     *         true otherwise
     */
    private static boolean getBoolean(Map<String, Object> properties, String name) {
        Object value = properties.get(name);
        //noinspection SimplifiableIfStatement
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        return false;
    }

    /**
     * Utility method that puts the given value to the given map.
     *
     * @param properties map of properties (String -> Object)
     * @param name property name
     * @param value property value
     */
    private static void put(Map<String, Object> properties, String name, Object value) {
        properties.put(name, value);
    }

    /**
     *  Looks for text in the cell that should be unicode, like &alpha; and provides the
     *  unicode version of it.
     *
     *@param  cell  The cell to check for unicode values
     *@return       translated to unicode
     */
    public static Cell translateUnicodeValues(Cell cell) {

        String s = cell.getRichStringCellValue().getString();
        boolean foundUnicode = false;
        String lowerCaseStr = s.toLowerCase(Locale.ROOT);

        for (UnicodeMapping entry : unicodeMappings) {
            String key = entry.entityName;
            if (lowerCaseStr.contains(key)) {
                s = s.replaceAll(key, entry.resolvedValue);
                foundUnicode = true;
            }
        }
        if (foundUnicode) {
            cell.setCellValue(cell.getRow().getSheet().getWorkbook().getCreationHelper()
                    .createRichTextString(s));
        }
        return cell;
    }

    static {
        unicodeMappings = new UnicodeMapping[] {
            um("alpha",   "\u03B1" ),
            um("beta",    "\u03B2" ),
            um("gamma",   "\u03B3" ),
            um("delta",   "\u03B4" ),
            um("epsilon", "\u03B5" ),
            um("zeta",    "\u03B6" ),
            um("eta",     "\u03B7" ),
            um("theta",   "\u03B8" ),
            um("iota",    "\u03B9" ),
            um("kappa",   "\u03BA" ),
            um("lambda",  "\u03BB" ),
            um("mu",      "\u03BC" ),
            um("nu",      "\u03BD" ),
            um("xi",      "\u03BE" ),
            um("omicron", "\u03BF" ),
        };
    }

    private static UnicodeMapping um(String entityName, String resolvedValue) {
        return new UnicodeMapping(entityName, resolvedValue);
    }
}
