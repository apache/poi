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
package org.apache.poi.hssf.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.converter.AbstractWordUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Internal;

/**
 * Common class for {@link ExcelToFoConverter} and {@link ExcelToHtmlConverter}
 *
 * @see AbstractWordUtils
 * @since POI 3.8 beta 5
 */
@Internal
class AbstractExcelUtils {
    /*package*/ static final String EMPTY = "";
    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
    private static final int UNIT_OFFSET_LENGTH = 7;

    public static String getAlign(HorizontalAlignment alignment) {
        switch (alignment) {
            case CENTER:
            case CENTER_SELECTION:
                return "center";
            case FILL:
                // XXX: shall we support fill?
                return "";
            case JUSTIFY:
                return "justify";
            case LEFT:
                return "left";
            case RIGHT:
                return "right";
            default:
            case GENERAL:
                return "";
        }
    }

    public static String getBorderStyle(BorderStyle xlsBorder) {
        final String borderStyle;
        switch (xlsBorder) {
            case NONE:
                borderStyle = "none";
                break;
            case DASH_DOT:
            case DASH_DOT_DOT:
            case DOTTED:
            case HAIR:
            case MEDIUM_DASH_DOT:
            case MEDIUM_DASH_DOT_DOT:
            case SLANTED_DASH_DOT:
                borderStyle = "dotted";
                break;
            case DASHED:
            case MEDIUM_DASHED:
                borderStyle = "dashed";
                break;
            case DOUBLE:
                borderStyle = "double";
                break;
            default:
                borderStyle = "solid";
                break;
        }
        return borderStyle;
    }

    public static String getBorderWidth(BorderStyle xlsBorder) {
        final String borderWidth;
        switch (xlsBorder) {
            case MEDIUM_DASH_DOT:
            case MEDIUM_DASH_DOT_DOT:
            case MEDIUM_DASHED:
                borderWidth = "2pt";
                break;
            case THICK:
                borderWidth = "thick";
                break;
            default:
                borderWidth = "thin";
                break;
        }
        return borderWidth;
    }

    public static String getColor(HSSFColor color) {
        StringBuilder stringBuilder = new StringBuilder(7);
        stringBuilder.append('#');
        for (short s : color.getTriplet()) {
            if (s < 10) {
                stringBuilder.append('0');
            }

            stringBuilder.append(Integer.toHexString(s));
        }
        String result = stringBuilder.toString();

        if (result.equals("#ffffff")) {
            return "white";
        }

        if (result.equals("#c0c0c0")) {
            return "silver";
        }

        if (result.equals("#808080")) {
            return "gray";
        }

        if (result.equals("#000000")) {
            return "black";
        }

        return result;
    }

    /**
     * See <a href=
     * "http://apache-poi.1045710.n5.nabble.com/Excel-Column-Width-Unit-Converter-pixels-excel-column-width-units-td2301481.html"
     * >here</a> for Xio explanation and details
     */
    public static int getColumnWidthInPx(int widthUnits) {
        int pixels = (widthUnits / EXCEL_COLUMN_WIDTH_FACTOR)
            * UNIT_OFFSET_LENGTH;

        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
        pixels += Math.round(offsetWidthUnits
            / ((float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH));

        return pixels;
    }

    /**
     * @param mergedRanges map of sheet merged ranges built with
     *                     {@link #buildMergedRangesMap(Sheet)}
     * @return {@link CellRangeAddress} from map if cell with specified row and
     * column numbers contained in found range, {@code null} otherwise
     */
    public static CellRangeAddress getMergedRange(
        CellRangeAddress[][] mergedRanges, int rowNumber, int columnNumber) {
        CellRangeAddress[] mergedRangeRowInfo = rowNumber < mergedRanges.length ? mergedRanges[rowNumber]
            : null;

        return mergedRangeRowInfo != null
            && columnNumber < mergedRangeRowInfo.length ? mergedRangeRowInfo[columnNumber]
            : null;
    }

    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static HSSFWorkbook loadXls(File xlsFile) throws IOException {
        try (final FileInputStream inputStream = new FileInputStream(xlsFile)) {
            return new HSSFWorkbook(inputStream);
        }
    }

    public static void appendAlign(StringBuilder style, HorizontalAlignment alignment) {
        String cssAlign = getAlign(alignment);
        if (isEmpty(cssAlign)) {
            return;
        }

        style.append("text-align:");
        style.append(cssAlign);
        style.append(";");
    }

    /**
     * Creates a map (i.e. two-dimensional array) filled with ranges. Allow fast
     * retrieving {@link CellRangeAddress} of any cell, if cell is contained in
     * range.
     *
     * @see #getMergedRange(CellRangeAddress[][], int, int)
     */
    public static CellRangeAddress[][] buildMergedRangesMap(Sheet sheet) {
        CellRangeAddress[][] mergedRanges = new CellRangeAddress[1][];
        for (final CellRangeAddress cellRangeAddress : sheet.getMergedRegions()) {
            final int requiredHeight = cellRangeAddress.getLastRow() + 1;
            if (mergedRanges.length < requiredHeight) {
                mergedRanges = Arrays.copyOf(mergedRanges, requiredHeight, CellRangeAddress[][].class);
            }

            for (int r = cellRangeAddress.getFirstRow(); r <= cellRangeAddress
                .getLastRow(); r++) {
                final int requiredWidth = cellRangeAddress.getLastColumn() + 1;

                CellRangeAddress[] rowMerged = mergedRanges[r];
                if (rowMerged == null) {
                    rowMerged = new CellRangeAddress[requiredWidth];
                    mergedRanges[r] = rowMerged;
                } else {
                    final int rowMergedLength = rowMerged.length;
                    if (rowMergedLength < requiredWidth) {
                        rowMerged = mergedRanges[r] =
                            Arrays.copyOf(rowMerged, requiredWidth, CellRangeAddress[].class);
                    }
                }

                Arrays.fill(rowMerged, cellRangeAddress.getFirstColumn(),
                    cellRangeAddress.getLastColumn() + 1, cellRangeAddress);
            }
        }
        return mergedRanges;
    }
}
