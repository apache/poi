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

package org.apache.poi.ss.tests.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellPropertyType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.BaseTestCellUtil;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestXSSFCellUtil extends BaseTestCellUtil {
    public TestXSSFCellUtil() {
        super(XSSFITestDataProvider.instance);
    }

    @Test
    public void testSetForegroundColorCellStylePropertyByEnum() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("AAAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
        }
    }

    @Test
    public void testSetForegroundColorCellStyleProperty() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("AAAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellUtil.FILL_FOREGROUND_COLOR_COLOR, color);

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
        }
    }

    @Test
    public void testSetForegroundColorCellStylePropertyToNullByEnum() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("AAAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, null);

            assertNotEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(), cell.getCellStyle().getFillForegroundColor());
        }
    }

    @Test
    public void testSetForegroundColorCellStylePropertyToNull() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("AAAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellUtil.FILL_FOREGROUND_COLOR_COLOR, color);

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());

            CellUtil.setCellStyleProperty(
                    cell, CellUtil.FILL_FOREGROUND_COLOR_COLOR, null);

            assertNotEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(), cell.getCellStyle().getFillForegroundColor());
        }
    }

    @Test
    public void testSetForegroundColorCellStylePropertiesToNullByEnum() throws IOException, DecoderException {

        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FF0000"));

            {
                final Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }
            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, cell.getCellStyle().getFillPattern());

            {
                final Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }
            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(), cell.getCellStyle().getFillForegroundColor());
            assertEquals(FillPatternType.NO_FILL, cell.getCellStyle().getFillPattern());
        }
    }

    @Test
    public void testSetForegroundColorCellStylePropertiesToNull() throws IOException, DecoderException {

        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FF0000"));

            {
                final Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStyleProperties(cell, properties);
            }
            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, cell.getCellStyle().getFillPattern());

            {
                final Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStyleProperties(cell, properties);
            }
            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(), cell.getCellStyle().getFillForegroundColor());
            assertEquals(FillPatternType.NO_FILL, cell.getCellStyle().getFillPattern());
        }
    }


    @Test
    public void testBug66052WithWorkaroundByEnum() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FFAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellPropertyType.FILL_BACKGROUND_COLOR_COLOR, null); // WORKAROUND
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }

            assertNotNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellPropertyType.FILL_BACKGROUND_COLOR_COLOR, null); // WORKAROUND
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());
        }
    }

    @Test
    public void testBug66052WithWorkaround() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FFAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellUtil.FILL_BACKGROUND_COLOR_COLOR, null); // WORKAROUND
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStyleProperties(cell, properties);
            }

            assertNotNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellUtil.FILL_BACKGROUND_COLOR_COLOR, null); // WORKAROUND
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStyleProperties(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());
        }
    }

    @Test
    public void testBug66052WithoutWorkaroundByEnum() throws IOException, DecoderException {

        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FFAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            // no background color was set, so none is written (bug 69463)
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            // no background color was set, so none is written (bug 69463)
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());
        }
    }

    @Test
    public void testBug66052WithoutWorkaround() throws IOException, DecoderException {

        try (Workbook workbook = new XSSFWorkbook()) {

            final Sheet sheet = workbook.createSheet("Sheet");
            final Row row = sheet.createRow(0);
            final Cell cell = row.createCell(0);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FFAAAA"));

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, color);
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);

                CellUtil.setCellStyleProperties(cell, properties);
            }

            assertEquals(color, cell.getCellStyle().getFillForegroundColorColor());
            // no background color was set, so none is written (bug 69463)
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());

            {
                Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStyleProperties(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            // no background color was set, so none is written (bug 69463)
            assertNull(cell.getCellStyle().getFillBackgroundColorColor());
        }
    }

    /**
     * Bug 69463: a cell without a fill got a fill with automatic fore- and background colors but no
     * pattern, which Excel renders as a black cell while it is being edited
     */
    @Test
    void testNoFillCellStaysWithoutFillColorsBug69463() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("Sheet");
            final Cell cell = sheet.createRow(1).createCell(1);
            final CellStyle original = workbook.createCellStyle();
            original.setBorderTop(BorderStyle.THIN);
            cell.setCellStyle(original);

            CellUtil.setCellStyleProperties(cell, Map.of(CellUtil.LOCKED, false));

            XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
            assertFalse(style.getLocked());
            assertEquals(BorderStyle.THIN, style.getBorderTop());
            assertEquals(FillPatternType.NO_FILL, style.getFillPattern());
            assertNull(style.getFillForegroundColorColor());
            assertNull(style.getFillBackgroundColorColor());
            CTPatternFill patternFill = workbook.getStylesSource()
                    .getFillAt((int) style.getCoreXf().getFillId()).getCTFill().getPatternFill();
            assertFalse(patternFill.isSetFgColor());
            assertFalse(patternFill.isSetBgColor());
        }
    }

    /**
     * Bug 69463: the fill of a cell with a solid theme color, as Excel writes it, is kept as is
     */
    @Test
    void testSolidThemeFillIsKeptBug69463() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("Sheet");
            final Cell cell = sheet.createRow(1).createCell(0);
            final XSSFCellStyle original = workbook.createCellStyle();
            original.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            final XSSFColor themeColor = new XSSFColor(workbook.getStylesSource().getIndexedColors());
            themeColor.setTheme(0);
            original.setFillForegroundColor(themeColor);
            original.setFillBackgroundColor(IndexedColors.AUTOMATIC.getIndex());
            cell.setCellStyle(original);

            CellUtil.setCellStyleProperties(cell, Map.of(CellUtil.LOCKED, false));

            XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
            assertNotEquals(original, style);
            assertFalse(style.getLocked());
            assertEquals(FillPatternType.SOLID_FOREGROUND, style.getFillPattern());
            assertEquals(themeColor, style.getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(), style.getFillBackgroundColorColor().getIndex());
        }
    }

    /**
     * Bug 69366: the indexed color derived from an RGB color is 0, not the automatic color a cell
     * without a fill reports, which must not stop the style with that RGB color from being found
     */
    @Test
    void testRgbFillColorReusesStyleBug69366() throws IOException, DecoderException {
        try (Workbook workbook = new XSSFWorkbook()) {
            final Row row = workbook.createSheet("Sheet").createRow(0);
            final Cell cell1 = row.createCell(0);
            final Cell cell2 = row.createCell(1);
            final Cell cell3 = row.createCell(2);
            final XSSFColor color = new XSSFColor(Hex.decodeHex("FFAAAA"));
            final XSSFColor other = new XSSFColor(Hex.decodeHex("AAAAFF"));
            Map<CellPropertyType, Object> properties = new LinkedHashMap<>();
            properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.SOLID_FOREGROUND);
            properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, color);

            CellUtil.setCellStylePropertiesEnum(cell1, properties);
            int numStyles = workbook.getNumCellStyles();

            CellUtil.setCellStylePropertiesEnum(cell1, properties);
            assertEquals(numStyles, workbook.getNumCellStyles());
            CellUtil.setCellStylePropertiesEnum(cell2, properties);
            assertEquals(numStyles, workbook.getNumCellStyles());
            assertEquals(cell1.getCellStyle().getIndex(), cell2.getCellStyle().getIndex());
            assertEquals(color, cell2.getCellStyle().getFillForegroundColorColor());

            properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, other);
            CellUtil.setCellStylePropertiesEnum(cell3, properties);
            assertEquals(numStyles + 1, workbook.getNumCellStyles());
            assertEquals(other, cell3.getCellStyle().getFillForegroundColorColor());
        }
    }
}
