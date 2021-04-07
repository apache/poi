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

package org.apache.poi.xssf.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class TestStylesTable {
    private static final String testFile = "Formatting.xlsx";
    private static final String customDataFormat = "YYYY-mm-dd";

    @BeforeAll
    public static void assumeCustomDataFormatIsNotBuiltIn() {
        assertEquals(-1, BuiltinFormats.getBuiltinFormat(customDataFormat));
    }

    @Test
    void testCreateNew() {
        StylesTable st = new StylesTable();

        // Check defaults
        assertNotNull(st.getCTStylesheet());
        assertEquals(1, st._getXfsSize());
        assertEquals(1, st._getStyleXfsSize());
        assertEquals(0, st.getNumDataFormats());
    }

    @Test
    void testCreateSaveLoad() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable st = wb.getStylesSource();

            assertNotNull(st.getCTStylesheet());
            assertEquals(1, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(0, st.getNumDataFormats());

            st = XSSFTestDataSamples.writeOutAndReadBack(wb).getStylesSource();

            assertNotNull(st.getCTStylesheet());
            assertEquals(1, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(0, st.getNumDataFormats());

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
        }
    }

    @Test
    void testLoadExisting() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook(testFile)) {
            assertNotNull(workbook.getStylesSource());

            StylesTable st = workbook.getStylesSource();

            doTestExisting(st);

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(workbook));
        }
    }

    @Test
    void testLoadSaveLoad() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook(testFile)) {
            assertNotNull(workbook.getStylesSource());

            StylesTable st = workbook.getStylesSource();
            doTestExisting(st);

            st = XSSFTestDataSamples.writeOutAndReadBack(workbook).getStylesSource();
            doTestExisting(st);
        }
    }

    void doTestExisting(StylesTable st) {
        // Check contents
        assertNotNull(st.getCTStylesheet());
        assertEquals(11, st._getXfsSize());
        assertEquals(1, st._getStyleXfsSize());
        assertEquals(8, st.getNumDataFormats());

        assertEquals(2, st.getFonts().size());
        assertEquals(2, st.getFills().size());
        assertEquals(1, st.getBorders().size());

        assertEquals("yyyy/mm/dd", st.getNumberFormatAt((short)165));
        assertEquals("yy/mm/dd", st.getNumberFormatAt((short)167));

        assertNotNull(st.getStyleAt(0));
        assertNotNull(st.getStyleAt(1));
        assertNotNull(st.getStyleAt(2));

        assertEquals(0, st.getStyleAt(0).getDataFormat());
        assertEquals(14, st.getStyleAt(1).getDataFormat());
        assertEquals(0, st.getStyleAt(2).getDataFormat());
        assertEquals(165, st.getStyleAt(3).getDataFormat());

        assertEquals("yyyy/mm/dd", st.getStyleAt(3).getDataFormatString());
        assertEquals("[]", st.getExplicitTableStyleNames().toString());
    }

    @Test
    void populateNew() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable st = wb.getStylesSource();

            assertNotNull(st.getCTStylesheet());
            assertEquals(1, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(0, st.getNumDataFormats());

            int nf1 = st.putNumberFormat("yyyy-mm-dd");
            int nf2 = st.putNumberFormat("yyyy-mm-DD");
            assertEquals(nf1, st.putNumberFormat("yyyy-mm-dd"));

            st.putStyle(new XSSFCellStyle(st));

            // Save and re-load
            st = XSSFTestDataSamples.writeOutAndReadBack(wb).getStylesSource();

            assertNotNull(st.getCTStylesheet());
            assertEquals(2, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(2, st.getNumDataFormats());

            assertEquals("yyyy-mm-dd", st.getNumberFormatAt((short) nf1));
            assertEquals(nf1, st.putNumberFormat("yyyy-mm-dd"));
            assertEquals(nf2, st.putNumberFormat("yyyy-mm-DD"));

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
        }
    }

    @Test
    void populateExisting() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook(testFile)) {
            assertNotNull(workbook.getStylesSource());

            StylesTable st = workbook.getStylesSource();
            assertEquals(11, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(8, st.getNumDataFormats());

            int nf1 = st.putNumberFormat("YYYY-mm-dd");
            int nf2 = st.putNumberFormat("YYYY-mm-DD");
            assertEquals(nf1, st.putNumberFormat("YYYY-mm-dd"));

            st = XSSFTestDataSamples.writeOutAndReadBack(workbook).getStylesSource();

            assertEquals(11, st._getXfsSize());
            assertEquals(1, st._getStyleXfsSize());
            assertEquals(10, st.getNumDataFormats());

            assertEquals("YYYY-mm-dd", st.getNumberFormatAt((short) nf1));
            assertEquals(nf1, st.putNumberFormat("YYYY-mm-dd"));
            assertEquals(nf2, st.putNumberFormat("YYYY-mm-DD"));

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(workbook));
        }
    }

    @Test
    void exceedNumberFormatLimit() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable styles = wb.getStylesSource();
            for (int i = 0; i < styles.getMaxNumberOfDataFormats(); i++) {
                styles.putNumberFormat("\"test" + i + " \"0");
            }
            IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> styles.putNumberFormat("\"anotherformat \"0"));
            assertTrue(e.getMessage().startsWith("The maximum number of Data Formats was exceeded."));
        }
    }

    private static <K,V> void assertNotContainsKey(Map<K,V> map, K key) {
        assertFalse(map.containsKey(key));
    }
    private static <K,V> void assertNotContainsValue(Map<K,V> map, V value) {
        assertFalse(map.containsValue(value));
    }

    @Test
    void removeNumberFormat() throws IOException {
        try (XSSFWorkbook wb1 = new XSSFWorkbook()) {
            final String fmt = customDataFormat;
            final short fmtIdx = (short) wb1.getStylesSource().putNumberFormat(fmt);

            Cell cell = wb1.createSheet("test").createRow(0).createCell(0);
            cell.setCellValue(5.25);
            CellStyle style = wb1.createCellStyle();
            style.setDataFormat(fmtIdx);
            cell.setCellStyle(style);

            assertEquals(fmt, cell.getCellStyle().getDataFormatString());
            assertEquals(fmt, wb1.getStylesSource().getNumberFormatAt(fmtIdx));

            // remove the number format from the workbook
            assertTrue(wb1.getStylesSource().removeNumberFormat(fmt), "The format is removed on first call");
            assertThrows(IllegalStateException.class, () -> wb1.getStylesSource().removeNumberFormat(fmt));

            // number format in CellStyles should be restored to default number format
            final short defaultFmtIdx = 0;
            final String defaultFmt = BuiltinFormats.getBuiltinFormat(0);
            assertEquals(defaultFmtIdx, style.getDataFormat());
            assertEquals(defaultFmt, style.getDataFormatString());

            // The custom number format should be entirely removed from the workbook
            Map<Short, String> numberFormats = wb1.getStylesSource().getNumberFormats();
            assertNotContainsKey(numberFormats, fmtIdx);
            assertNotContainsValue(numberFormats, fmt);

            // The default style shouldn't be added back to the styles source because it's built-in
            assertEquals(0, wb1.getStylesSource().getNumDataFormats());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutCloseAndReadBack(wb1)) {

                cell = wb2.getSheet("test").getRow(0).getCell(0);
                style = cell.getCellStyle();

                // number format in CellStyles should be restored to default number format
                assertEquals(defaultFmtIdx, style.getDataFormat());
                assertEquals(defaultFmt, style.getDataFormatString());

                // The custom number format should be entirely removed from the workbook
                numberFormats = wb2.getStylesSource().getNumberFormats();
                assertNotContainsKey(numberFormats, fmtIdx);
                assertNotContainsValue(numberFormats, fmt);

                // The default style shouldn't be added back to the styles source because it's built-in
                assertEquals(0, wb2.getStylesSource().getNumDataFormats());
            }
        }
    }

    @Test
    void maxNumberOfDataFormats() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable styles = wb.getStylesSource();

            // Check default limit
            int n = styles.getMaxNumberOfDataFormats();
            // https://support.office.com/en-us/article/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
            assertTrue(200 <= n);
            assertTrue(n <= 250);

            // Check upper limit
            n = Integer.MAX_VALUE;
            styles.setMaxNumberOfDataFormats(n);
            assertEquals(n, styles.getMaxNumberOfDataFormats());

            // Check negative (illegal) limits
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> styles.setMaxNumberOfDataFormats(-1),
                "Expected to get an IllegalArgumentException(\"Maximum Number of Data Formats must be greater than or equal to 0\")");
            assertTrue(e.getMessage().startsWith("Maximum Number of Data Formats must be greater than or equal to 0"));
        }
    }

    @Test
    void addDataFormatsBeyondUpperLimit() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable styles = wb.getStylesSource();
            styles.setMaxNumberOfDataFormats(0);

            // Try adding a format beyond the upper limit
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> styles.putNumberFormat("\"test \"0"));
            assertTrue(e.getMessage().startsWith("The maximum number of Data Formats was exceeded."));
        }
    }

    @Test
    void decreaseUpperLimitBelowCurrentNumDataFormats() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            StylesTable styles = wb.getStylesSource();
            styles.putNumberFormat(customDataFormat);

            // Try decreasing the upper limit below the current number of formats
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> styles.setMaxNumberOfDataFormats(0));
            assertTrue(e.getMessage().startsWith("Cannot set the maximum number of data formats less than the current quantity."));
        }
    }

    @Test
    void testLoadWithAlternateContent() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("style-alternate-content.xlsx")) {
            assertNotNull(workbook.getStylesSource());

            StylesTable st = workbook.getStylesSource();
            assertNotNull(st);

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(workbook));
        }
    }

    @Test
    void testReplaceStyle() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("style-alternate-content.xlsx")) {
            assertNotNull(workbook.getStylesSource());

            StylesTable st = workbook.getStylesSource();
            assertNotNull(st);

            st.replaceCellStyleXfAt(0, st.getCellStyleXfAt(1));
            st.replaceCellStyleXfAt(1, st.getCellStyleXfAt(1));

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(workbook));
        }
    }
}
