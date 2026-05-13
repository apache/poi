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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * XSSF equivalent of HSSFOptimiser.
 *
 * <p>Provides methods to optimise cell styles and fonts in XSSF workbooks
 * by remapping cells that use duplicate styles/fonts to use canonical
 * (first) occurrences.</p>
 *
 * <p>This helps avoid "style explosion" issues where Excel workbooks accumulate
 * too many style definitions, causing performance problems or file corruption.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   XSSFWorkbook workbook = new XSSFWorkbook(file);
 *   XSSFOptimiser.optimiseFonts(workbook);
 *   XSSFOptimiser.optimiseCellStyles(workbook);
 *   workbook.write(outputStream);
 * </pre>
 *
 * <p>Note: Unlike HSSFOptimiser which can remove unused styles from the binary
 * format, this implementation remaps cells to canonical styles without removing
 * entries from the XML. This is because XSSF's StylesTable maintains a separate
 * Java list from the CTStylesheet XML, and removing entries can cause
 * XmlValueDisconnectedException on save. The remapping approach prevents
 * further style explosion and is safe.</p>
 *
 * @see org.apache.poi.hssf.usermodel.HSSFOptimiser
 * @since 6.0.0
 */
public final class XSSFOptimiser {

    private XSSFOptimiser() {
        // no instances of this class
    }

    /**
     * Goes through the Workbook, optimising the cell styles by remapping
     * cells that use duplicate styles to use the canonical (first) occurrence.
     *
     * <p>Two styles are considered duplicates if they have identical properties:
     * font properties (not just font index), data format, alignment, borders,
     * fill colors, etc.</p>
     *
     * <p>For best results, call {@link #optimiseFonts(XSSFWorkbook)} first.</p>
     *
     * @param workbook The workbook to optimise
     * @return The number of cells that were remapped to canonical styles
     */
    public static int optimiseCellStyles(XSSFWorkbook workbook) {
        int numStyles = workbook.getNumCellStyles();

        if (numStyles <= 1) {
            return 0; // Nothing to optimize
        }

        // Step 1: Build signature map for duplicate detection
        Map<String, Integer> signatureToCanonical = new LinkedHashMap<>();
        Map<Integer, Integer> oldToCanonical = new HashMap<>();

        for (int i = 0; i < numStyles; i++) {
            XSSFCellStyle style = workbook.getCellStyleAt(i);
            String signature = getStyleSignature(style);

            if (signatureToCanonical.containsKey(signature)) {
                // Duplicate found - map to earlier canonical style
                oldToCanonical.put(i, signatureToCanonical.get(signature));
            } else {
                // First occurrence - this is the canonical style
                signatureToCanonical.put(signature, i);
                oldToCanonical.put(i, i);
            }
        }

        // Step 2: Remap all cells to use canonical styles
        int cellsRemapped = 0;
        for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
            XSSFSheet sheet = workbook.getSheetAt(sheetIdx);
            for (Row row : sheet) {
                if (row == null) continue;
                for (Cell cell : row) {
                    if (cell == null) continue;
                    int currentIdx = cell.getCellStyle().getIndex();
                    int canonicalIdx = oldToCanonical.getOrDefault(currentIdx, currentIdx);

                    if (canonicalIdx != currentIdx) {
                        cell.setCellStyle(workbook.getCellStyleAt(canonicalIdx));
                        cellsRemapped++;
                    }
                }
            }
        }

        return cellsRemapped;
    }

    /**
     * Goes through the Workbook, optimising the fonts by updating cell styles
     * that reference duplicate fonts to use the canonical (first) occurrence.
     *
     * <p>Two fonts are considered duplicates if they have identical properties:
     * name, size, bold, italic, strikeout, underline, color, typeOffset, charset.</p>
     *
     * @param workbook The workbook to optimise
     * @return The number of duplicate fonts found
     */
    public static int optimiseFonts(XSSFWorkbook workbook) {
        int numFonts = workbook.getNumberOfFonts();

        if (numFonts <= 1) {
            return 0; // Nothing to optimize
        }

        // Step 1: Build signature map for duplicate detection
        Map<String, Integer> signatureToCanonical = new LinkedHashMap<>();
        Map<Integer, Integer> oldToCanonical = new HashMap<>();
        int duplicatesFound = 0;

        for (int i = 0; i < numFonts; i++) {
            XSSFFont font = workbook.getFontAt(i);
            String signature = getFontSignature(font);

            if (signatureToCanonical.containsKey(signature)) {
                oldToCanonical.put(i, signatureToCanonical.get(signature));
                duplicatesFound++;
            } else {
                signatureToCanonical.put(signature, i);
                oldToCanonical.put(i, i);
            }
        }

        if (duplicatesFound == 0) {
            return 0; // No duplicates to remap
        }

        // Step 2: Update font references in cell styles' CTXf entries
        StylesTable stylesTable = workbook.getStylesSource();
        CTStylesheet ctStylesheet = stylesTable.getCTStylesheet();
        CTCellXfs ctXfs = ctStylesheet.getCellXfs();

        if (ctXfs != null) {
            for (int i = 0; i < ctXfs.sizeOfXfArray(); i++) {
                CTXf xf = ctXfs.getXfArray(i);
                if (xf.isSetFontId()) {
                    int oldFontId = Math.toIntExact(xf.getFontId());
                    int canonicalFontId = oldToCanonical.getOrDefault(oldFontId, oldFontId);
                    if (canonicalFontId != oldFontId) {
                        xf.setFontId(canonicalFontId);
                    }
                }
            }
        }

        return duplicatesFound;
    }

    /**
     * Generate a signature string for a cell style to detect duplicates.
     * Uses font PROPERTIES (not index) so styles with identical but separate fonts
     * are detected as duplicates.
     */
    private static String getStyleSignature(XSSFCellStyle style) {
        StringBuilder sb = new StringBuilder();
        // Use font properties, not index, so identical fonts are detected
        XSSFFont font = style.getFont();
        sb.append("font:").append(getFontSignature(font)).append(";");
        sb.append("fmt:").append(style.getDataFormat()).append(";");
        sb.append("align:").append(style.getAlignment()).append(";");
        sb.append("valign:").append(style.getVerticalAlignment()).append(";");
        sb.append("wrap:").append(style.getWrapText()).append(";");
        sb.append("rotation:").append(style.getRotation()).append(";");
        sb.append("indent:").append(style.getIndention()).append(";");
        sb.append("borderL:").append(style.getBorderLeft()).append(";");
        sb.append("borderR:").append(style.getBorderRight()).append(";");
        sb.append("borderT:").append(style.getBorderTop()).append(";");
        sb.append("borderB:").append(style.getBorderBottom()).append(";");
        sb.append("fillFg:").append(colorToString(style.getFillForegroundColorColor())).append(";");
        sb.append("fillBg:").append(colorToString(style.getFillBackgroundColorColor())).append(";");
        sb.append("fillPat:").append(style.getFillPattern()).append(";");
        sb.append("locked:").append(style.getLocked()).append(";");
        sb.append("hidden:").append(style.getHidden()).append(";");
        return sb.toString();
    }

    /**
     * Generate a signature string for a font to detect duplicates.
     */
    private static String getFontSignature(XSSFFont font) {
        StringBuilder sb = new StringBuilder();
        sb.append("name:").append(font.getFontName()).append(";");
        sb.append("size:").append(font.getFontHeightInPoints()).append(";");
        sb.append("bold:").append(font.getBold()).append(";");
        sb.append("italic:").append(font.getItalic()).append(";");
        sb.append("strike:").append(font.getStrikeout()).append(";");
        sb.append("underline:").append(font.getUnderline()).append(";");
        sb.append("color:").append(colorToString(font.getXSSFColor())).append(";");
        sb.append("typeOffset:").append(font.getTypeOffset()).append(";");
        sb.append("charset:").append(font.getCharSet()).append(";");
        return sb.toString();
    }

    /**
     * Convert a color to a string for comparison.
     */
    private static String colorToString(Color color) {
        if (color == null) {
            return "null";
        }
        if (color instanceof XSSFColor) {
            XSSFColor xssfColor = (XSSFColor) color;
            byte[] rgb = xssfColor.getRGB();
            if (rgb != null) {
                return String.format(Locale.ROOT, "#%02X%02X%02X", rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
            }
            return "indexed:" + xssfColor.getIndexed();
        }
        return color.toString();
    }
}
