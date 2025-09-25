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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellPropertyType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.BaseTestCellUtil;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            assertEquals(IndexedColors.AUTOMATIC.getIndex(),
                    ((XSSFColor) cell.getCellStyle().getFillBackgroundColorColor()).getIndex());

            {
                Map<CellPropertyType, Object> properties = new LinkedHashMap<>();

                properties.put(CellPropertyType.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellPropertyType.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStylePropertiesEnum(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(),
                    ((XSSFColor) cell.getCellStyle().getFillBackgroundColorColor()).getIndex());
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
            assertEquals(IndexedColors.AUTOMATIC.getIndex(),
                    ((XSSFColor) cell.getCellStyle().getFillBackgroundColorColor()).getIndex());

            {
                Map<String, Object> properties = new LinkedHashMap<>();

                properties.put(CellUtil.FILL_FOREGROUND_COLOR_COLOR, null);
                properties.put(CellUtil.FILL_PATTERN, FillPatternType.NO_FILL);

                CellUtil.setCellStyleProperties(cell, properties);
            }

            assertNull(cell.getCellStyle().getFillForegroundColorColor());
            assertEquals(IndexedColors.AUTOMATIC.getIndex(),
                    ((XSSFColor) cell.getCellStyle().getFillBackgroundColorColor()).getIndex());
        }
    }
}
