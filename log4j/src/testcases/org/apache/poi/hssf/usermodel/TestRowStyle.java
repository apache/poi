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

import static org.apache.poi.hssf.HSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.junit.jupiter.api.Test;

/**
 * Class to test row styling functionality
 */
final class TestRowStyle {

    /**
     * TEST NAME:  Test Write Sheet Font <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with fonts.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    @Test
    void testWriteSheetFont() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFFont fnt = wb.createFont();
            HSSFCellStyle cs = wb.createCellStyle();

            fnt.setColor(HSSFFont.COLOR_RED);
            fnt.setBold(true);
            cs.setFont(fnt);
            for (int rownum = 0; rownum < 100; rownum++) {
                HSSFRow r = s.createRow(rownum);
                r.setRowStyle(cs);
                r.createCell(0);
            }
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                SanityChecker sanityChecker = new SanityChecker();
                sanityChecker.checkHSSFWorkbook(wb2);
                assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
                assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");
            }
        }
    }

    /**
     * Tests that is creating a file with a date or an calendar works correctly.
     */
    @Test
    void testDataStyle() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFRow row = s.createRow(0);

            // with Date:
            cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            row.setRowStyle(cs);
            row.createCell(0);


            // with Calendar:
            row = s.createRow(1);
            cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
            row.setRowStyle(cs);
            row.createCell(0);

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                SanityChecker sanityChecker = new SanityChecker();
                sanityChecker.checkHSSFWorkbook(wb2);

                assertEquals(1, s.getLastRowNum(), "LAST ROW");
                assertEquals(0, s.getFirstRowNum(), "FIRST ROW");
            }
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
     */
    @Test
    void testWriteSheetStyle() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();
            HSSFFont fnt = wb.createFont();
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFCellStyle cs2 = wb.createCellStyle();

            cs.setBorderBottom(BorderStyle.THIN);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setBorderTop(BorderStyle.THIN);
            cs.setFillForegroundColor((short) 0xA);
            cs.setFillPattern(FillPatternType.BRICKS);
            fnt.setColor((short) 0xf);
            fnt.setItalic(true);
            cs2.setFillForegroundColor((short) 0x0);
            cs2.setFillPattern(FillPatternType.BRICKS);
            cs2.setFont(fnt);
            for (int rownum = 0; rownum < 100; rownum++) {
                HSSFRow r = s.createRow(rownum);
                r.setRowStyle(cs);
                r.createCell(0);

                rownum++;
                if (rownum >= 100) break; // I feel too lazy to check if this isreqd :-/

                r = s.createRow(rownum);
                r.setRowStyle(cs2);
                r.createCell(0);
            }
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                SanityChecker sanityChecker = new SanityChecker();
                sanityChecker.checkHSSFWorkbook(wb2);
                assertEquals(99, s.getLastRowNum(), "LAST ROW == 99");
                assertEquals(0, s.getFirstRowNum(), "FIRST ROW == 0");

                s = wb2.getSheetAt(0);
                assertNotNull(s, "Sheet is not null");

                for (int rownum = 0; rownum < 100; rownum++) {
                    HSSFRow r = s.getRow(rownum);
                    assertNotNull(r, "Row is not null");

                    cs = r.getRowStyle();
                    assertNotNull(cs);
                    assertEquals(BorderStyle.THIN, cs.getBorderBottom(), "Bottom Border Style for row:");
                    assertEquals(BorderStyle.THIN, cs.getBorderLeft(), "Left Border Style for row:");
                    assertEquals(BorderStyle.THIN, cs.getBorderRight(), "Right Border Style for row:");
                    assertEquals(BorderStyle.THIN, cs.getBorderTop(), "Top Border Style for row:");
                    assertEquals(0xA, cs.getFillForegroundColor(), "FillForegroundColor for row:");
                    assertEquals(FillPatternType.BRICKS, cs.getFillPattern(), "FillPattern for row:");

                    rownum++;
                    if (rownum >= 100) break; // I feel too lazy to check if this isreqd :-/

                    r = s.getRow(rownum);
                    assertNotNull(r, "Row is not null");
                    cs2 = r.getRowStyle();
                    assertNotNull(cs2);
                    assertEquals(cs2.getFillForegroundColor(), (short) 0x0, "FillForegroundColor for row:");
                    assertEquals(cs2.getFillPattern(), FillPatternType.BRICKS, "FillPattern for row:");
                }
            }
        }
    }
}
