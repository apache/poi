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

package org.apache.poi.hssf.usermodel;

import static org.apache.poi.ss.usermodel.BorderStyle.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

/**
 * Class to test cell styling functionality
 */

final class TestCellStyle {

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }


    /**
     * TEST NAME:  Test Write Sheet Font <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with fonts.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */
    @Test
    void testWriteSheetFont() throws IOException{
        File file = TempFile.createTempFile("testWriteSheetFont", ".xls");
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFFont fnt = wb.createFont();
            HSSFCellStyle cs = wb.createCellStyle();

            fnt.setColor(HSSFFont.COLOR_RED);
            fnt.setBold(true);
            cs.setFont(fnt);
            for (int rownum = 0; rownum < 100; rownum++) {
                HSSFRow r = s.createRow(rownum);

                for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                    HSSFCell c = r.createCell(cellnum);
                    c.setCellValue(rownum * 10000 + cellnum
                                           + (((double) rownum / 1000)
                            + ((double) cellnum / 10000)));
                    c = r.createCell(cellnum + 1);
                    c.setCellValue("TEST");
                    c.setCellStyle(cs);
                }
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }

            SanityChecker sanityChecker = new SanityChecker();
            sanityChecker.checkHSSFWorkbook(wb);
            assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
            assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");
        }

        // assert((s.getLastRowNum() == 99));
    }

    /**
     * Tests that is creating a file with a date or an calendar works correctly.
     */
    @Test
    void testDataStyle() throws IOException {
        File file = TempFile.createTempFile("testWriteSheetStyleDate", ".xls");
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFRow row = s.createRow(0);

            // with Date:
            HSSFCell cell = row.createCell(1);
            cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            cell.setCellStyle(cs);
            cell.setCellValue(new Date());

            // with Calendar:
            cell = row.createCell(2);
            cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            cell.setCellStyle(cs);
            Calendar cal = LocaleUtil.getLocaleCalendar();
            cell.setCellValue(cal);

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
            SanityChecker sanityChecker = new SanityChecker();
            sanityChecker.checkHSSFWorkbook(wb);

            assertEquals(0, s.getLastRowNum(), "LAST ROW");
            assertEquals(0, s.getFirstRowNum(), "FIRST ROW");
        }
    }

    @Test
    void testHashEquals() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFCellStyle cs1 = wb.createCellStyle();
            HSSFCellStyle cs2 = wb.createCellStyle();
            HSSFRow row = s.createRow(0);
            HSSFCell cell1 = row.createCell(1);
            HSSFCell cell2 = row.createCell(2);

            cs1.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/dd/yy"));

            cell1.setCellStyle(cs1);
            cell1.setCellValue(new Date());

            cell2.setCellStyle(cs2);
            cell2.setCellValue(new Date());

            assertEquals(cs1.hashCode(), cs1.hashCode());
            assertEquals(cs2.hashCode(), cs2.hashCode());
            assertEquals(cs1, cs1);
            assertEquals(cs2, cs2);

            // Change cs1, hash will alter
            int hash1 = cs1.hashCode();
            cs1.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/dd/yy"));
            assertNotEquals(hash1, cs1.hashCode());
        }
    }

    /**
     * TEST NAME:  Test Write Sheet Style <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with colors
     *             and borders.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */
    @Test
    void testWriteSheetStyle() throws IOException {
        File file = TempFile.createTempFile("testWriteSheetStyle", ".xls");
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFFont fnt = wb.createFont();
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFCellStyle cs2 = wb.createCellStyle();

            cs.setBorderBottom(THIN);
            cs.setBorderLeft(THIN);
            cs.setBorderRight(THIN);
            cs.setBorderTop(THIN);
            cs.setFillForegroundColor((short) 0xA);
            cs.setFillPattern(FillPatternType.DIAMONDS);
            fnt.setColor((short) 0xf);
            fnt.setItalic(true);
            cs2.setFillForegroundColor((short) 0x0);
            cs2.setFillPattern(FillPatternType.DIAMONDS);
            cs2.setFont(fnt);
            for (int rownum = 0; rownum < 100; rownum++) {
                HSSFRow r = s.createRow(rownum);

                for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                    HSSFCell c = r.createCell(cellnum);
                    c.setCellValue(rownum * 10000 + cellnum
                                           + (((double) rownum / 1000)
                            + ((double) cellnum / 10000)));
                    c.setCellStyle(cs);
                    c = r.createCell(cellnum + 1);
                    c.setCellValue("TEST");
                    c.setCellStyle(cs2);
                }
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
            SanityChecker sanityChecker = new SanityChecker();
            sanityChecker.checkHSSFWorkbook(wb);
            assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
            assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");
        }
    }

    /**
     * Cloning one HSSFCellStyle onto Another, same
     *  HSSFWorkbook
     */
    @Test
    void testCloneStyleSameWB() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFFont fnt = wb.createFont();
            fnt.setFontName("TestingFont");
            assertEquals(5, wb.getNumberOfFonts());

            HSSFCellStyle orig = wb.createCellStyle();
            orig.setAlignment(HorizontalAlignment.JUSTIFY);
            orig.setFont(fnt);
            orig.setDataFormat((short) 18);

            assertEquals(HorizontalAlignment.JUSTIFY, orig.getAlignment());
            assertEquals(fnt, orig.getFont(wb));
            assertEquals(18, orig.getDataFormat());

            HSSFCellStyle clone = wb.createCellStyle();
            assertNotSame(HorizontalAlignment.RIGHT, clone.getAlignment());
            assertNotSame(fnt, clone.getFont(wb));
            assertNotEquals(18, clone.getDataFormat());

            clone.cloneStyleFrom(orig);
            assertEquals(HorizontalAlignment.JUSTIFY, clone.getAlignment());
            assertEquals(fnt, clone.getFont(wb));
            assertEquals(18, clone.getDataFormat());
            assertEquals(5, wb.getNumberOfFonts());
        }
    }

    /**
     * Cloning one HSSFCellStyle onto Another, across
     *  two different HSSFWorkbooks
     */
    @Test
    void testCloneStyleDiffWB() throws IOException {
        try (HSSFWorkbook wbOrig = new HSSFWorkbook()) {

            HSSFFont fnt = wbOrig.createFont();
            fnt.setFontName("TestingFont");
            assertEquals(5, wbOrig.getNumberOfFonts());

            HSSFDataFormat fmt = wbOrig.createDataFormat();
            fmt.getFormat("MadeUpOne");
            fmt.getFormat("MadeUpTwo");

            HSSFCellStyle orig = wbOrig.createCellStyle();
            orig.setAlignment(HorizontalAlignment.RIGHT);
            orig.setFont(fnt);
            orig.setDataFormat(fmt.getFormat("Test##"));

            assertEquals(HorizontalAlignment.RIGHT, orig.getAlignment());
            assertEquals(fnt, orig.getFont(wbOrig));
            assertEquals(fmt.getFormat("Test##"), orig.getDataFormat());

            // Now a style on another workbook
            try (HSSFWorkbook wbClone = new HSSFWorkbook()) {
                assertEquals(4, wbClone.getNumberOfFonts());
                HSSFDataFormat fmtClone = wbClone.createDataFormat();

                HSSFCellStyle clone = wbClone.createCellStyle();
                assertEquals(4, wbClone.getNumberOfFonts());

                assertNotSame(HorizontalAlignment.RIGHT, clone.getAlignment());
                assertNotSame("TestingFont", clone.getFont(wbClone).getFontName());

                clone.cloneStyleFrom(orig);
                assertEquals(HorizontalAlignment.RIGHT, clone.getAlignment());
                assertEquals("TestingFont", clone.getFont(wbClone).getFontName());
                assertEquals(fmtClone.getFormat("Test##"), clone.getDataFormat());
                assertNotEquals(fmtClone.getFormat("Test##"), fmt.getFormat("Test##"));
                assertEquals(5, wbClone.getNumberOfFonts());
            }
        }
    }

    @Test
    void testStyleNames() throws IOException {
        try (HSSFWorkbook wb = openSample("WithExtendedStyles.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFCell c1 = s.getRow(0).getCell(0);
            HSSFCell c2 = s.getRow(1).getCell(0);
            HSSFCell c3 = s.getRow(2).getCell(0);

            HSSFCellStyle cs1 = c1.getCellStyle();
            HSSFCellStyle cs2 = c2.getCellStyle();
            HSSFCellStyle cs3 = c3.getCellStyle();

            assertNotNull(cs1);
            assertNotNull(cs2);
            assertNotNull(cs3);

            // Check we got the styles we'd expect
            assertEquals(10, cs1.getFont(wb).getFontHeightInPoints());
            assertEquals(9, cs2.getFont(wb).getFontHeightInPoints());
            assertEquals(12, cs3.getFont(wb).getFontHeightInPoints());

            assertEquals(15, cs1.getIndex());
            assertEquals(23, cs2.getIndex());
            assertEquals(24, cs3.getIndex());

            assertNull(cs1.getParentStyle());
            assertNotNull(cs2.getParentStyle());
            assertNotNull(cs3.getParentStyle());

            assertEquals(21, cs2.getParentStyle().getIndex());
            assertEquals(22, cs3.getParentStyle().getIndex());

            // Now check we can get style records for the parent ones
            assertNull(wb.getWorkbook().getStyleRecord(15));
            assertNull(wb.getWorkbook().getStyleRecord(23));
            assertNull(wb.getWorkbook().getStyleRecord(24));

            assertNotNull(wb.getWorkbook().getStyleRecord(21));
            assertNotNull(wb.getWorkbook().getStyleRecord(22));

            // Now check the style names
            assertNull(cs1.getUserStyleName());
            assertNull(cs2.getUserStyleName());
            assertNull(cs3.getUserStyleName());
            assertEquals("style1", cs2.getParentStyle().getUserStyleName());
            assertEquals("style2", cs3.getParentStyle().getUserStyleName());

            // now apply a named style to a new cell
            HSSFCell c4 = s.getRow(0).createCell(1);
            c4.setCellStyle(cs2);
            assertEquals("style1", c4.getCellStyle().getParentStyle().getUserStyleName());
        }
    }

    @Test
    void testGetSetBorderHair() throws IOException {
        BorderStyle[] bs = {
            HAIR, DOTTED, DASH_DOT_DOT, DASHED, THIN, MEDIUM_DASH_DOT_DOT, SLANTED_DASH_DOT,
            MEDIUM_DASH_DOT, MEDIUM_DASHED, MEDIUM, THICK, DOUBLE
        };

        try (HSSFWorkbook wb = openSample("55341_CellStyleBorder.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            for (int i = 0; i<bs.length; i++) {
                HSSFCellStyle cs = s.getRow(i).getCell(i).getCellStyle();
                assertEquals(bs[i], cs.getBorderRight());
            }
        }
    }

    @Test
    void testShrinkToFit() throws IOException {
    	// Existing file
    	try (HSSFWorkbook wb1 = openSample("ShrinkToFit.xls")) {
            HSSFSheet s = wb1.getSheetAt(0);
            HSSFRow r = s.getRow(0);
            HSSFCellStyle cs = r.getCell(0).getCellStyle();

            assertTrue(cs.getShrinkToFit());
        }

        // New file
        try (HSSFWorkbook wbOrig = new HSSFWorkbook()) {
            HSSFSheet s = wbOrig.createSheet();
            HSSFRow r = s.createRow(0);

            HSSFCellStyle cs = wbOrig.createCellStyle();
            cs.setShrinkToFit(false);
            r.createCell(0).setCellStyle(cs);

            cs = wbOrig.createCellStyle();
            cs.setShrinkToFit(true);
            r.createCell(1).setCellStyle(cs);

            // Write out, read, and check
            try (HSSFWorkbook wb = HSSFTestDataSamples.writeOutAndReadBack(wbOrig)) {
                s = wb.getSheetAt(0);
                r = s.getRow(0);
                assertFalse(r.getCell(0).getCellStyle().getShrinkToFit());
                assertTrue(r.getCell(1).getCellStyle().getShrinkToFit());
            }
        }
    }

    @Test
    void test56563() {
        Stream.of("56563a.xls", "56563b.xls").parallel().forEach(fileName -> assertDoesNotThrow(() -> {
            Random rand = new Random();
            for(int i=0; i<10; i++) {
                Thread.sleep(rand.nextInt(300));
                try (Workbook wb = openSample(fileName)) {
                    for (Row row : wb.getSheetAt(0)) {
                        for (Cell cell : row) {
                            cell.getCellStyle().getDataFormatString();
                            if (cell.getCellType() == CellType.NUMERIC) {
                                boolean isDate = DateUtil.isCellDateFormatted(cell);
                                int cid = cell.getColumnIndex();
                                assertFalse(cid > 0 && isDate, "cell " + cid + " is not a date.");
                            }
                        }
                    }
                }
            }
        }));
    }

    @Test
    void test56959() throws IOException {
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("somesheet");

            Row row = sheet.createRow(0);

            // Create a new font and alter it.
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 24);
            font.setFontName("Courier New");
            font.setItalic(true);
            font.setStrikeout(true);
            font.setColor(Font.COLOR_RED);

            CellStyle style = wb.createCellStyle();
            style.setBorderBottom(DOTTED);
            style.setFont(font);

            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue("testtext");

            Cell newCell = row.createCell(1);

            newCell.setCellStyle(style);
            newCell.setCellValue("2testtext2");

            CellStyle newStyle = newCell.getCellStyle();
            assertEquals(DOTTED, newStyle.getBorderBottom());
            assertEquals(Font.COLOR_RED, ((HSSFCellStyle) newStyle).getFont(wb).getColor());
        }
    }


    @Test
    void test58043() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFCellStyle cellStyle = wb.createCellStyle();

            assertEquals(0, cellStyle.getRotation());

            cellStyle.setRotation((short) 89);
            assertEquals(89, cellStyle.getRotation());

            cellStyle.setRotation((short) 90);
            assertEquals(90, cellStyle.getRotation());

            cellStyle.setRotation((short) -1);
            assertEquals(-1, cellStyle.getRotation());

            cellStyle.setRotation((short) -89);
            assertEquals(-89, cellStyle.getRotation());

            cellStyle.setRotation((short) -90);
            assertEquals(-90, cellStyle.getRotation());

            cellStyle.setRotation((short) -89);
            assertEquals(-89, cellStyle.getRotation());

            // values above 90 are mapped to the correct values for compatibility between HSSF and XSSF
            cellStyle.setRotation((short) 179);
            assertEquals(-89, cellStyle.getRotation());

            cellStyle.setRotation((short) 180);
            assertEquals(-90, cellStyle.getRotation());
        }
    }
}
