/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import org.apache.poi.ss.tests.usermodel.BaseTestXRow;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for XSSFRow
 */
public final class TestSXSSFRow extends BaseTestXRow {

    public TestSXSSFRow() {
        super(SXSSFITestDataProvider.instance);
    }


    @AfterEach
    void tearDown() {
        ((SXSSFITestDataProvider) _testDataProvider).cleanup();
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030#c1>")
    protected void testCellShiftingRight(){
        // Remove when SXSSFRow.shiftCellsRight() is implemented.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030#c1>")
    protected void testCellShiftingLeft(){
        // Remove when SXSSFRow.shiftCellsLeft() is implemented.
    }

    @Test
    void testCellColumn() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            SXSSFSheet sheet = wb.createSheet();
            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell5 = row.createCell(5);
            assertEquals(5, cell5.getColumnIndex());
        }
    }

    @Test
    void testSetRowStylePropagatedToCells() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            SXSSFSheet sheet = wb.createSheet("test");

            // create a bold style
            XSSFCellStyle boldStyle = (XSSFCellStyle) wb.createCellStyle();
            XSSFFont boldFont = (XSSFFont) wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // apply style to row, then create cells
            SXSSFRow row = sheet.createRow(0);
            row.setRowStyle(boldStyle);

            SXSSFCell cell0 = row.createCell(0);
            cell0.setCellValue("Header A");
            SXSSFCell cell1 = row.createCell(1);
            cell1.setCellValue("Header B");

            // cells without explicit style should inherit the row style via getCellStyle()
            CellStyle cellStyle0 = cell0.getCellStyle();
            assertNotNull(cellStyle0);
            assertTrue(wb.getFontAt(cellStyle0.getFontIndex()).getBold(),
                    "cell should inherit bold font from row style");

            CellStyle cellStyle1 = cell1.getCellStyle();
            assertNotNull(cellStyle1);
            assertTrue(wb.getFontAt(cellStyle1.getFontIndex()).getBold(),
                    "cell should inherit bold font from row style");

            // a cell with an explicit style should NOT be overridden by row style
            CellStyle plainStyle = wb.createCellStyle();
            SXSSFCell cell2 = row.createCell(2);
            cell2.setCellStyle(plainStyle);
            cell2.setCellValue("Plain");

            CellStyle cellStyle2 = cell2.getCellStyle();
            assertFalse(wb.getFontAt(cellStyle2.getFontIndex()).getBold(),
                    "cell with explicit non-bold style should remain non-bold");
        }
    }

    @Test
    void testSetRowStylePropagatedAfterWrite() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            SXSSFSheet sheet = wb.createSheet("test");

            // create a bold style
            XSSFCellStyle boldStyle = (XSSFCellStyle) wb.createCellStyle();
            XSSFFont boldFont = (XSSFFont) wb.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            // apply style to row, then create cells
            SXSSFRow row = sheet.createRow(0);
            row.setRowStyle(boldStyle);
            row.createCell(0).setCellValue("Column A");
            row.createCell(1).setCellValue("Column B");
            row.createCell(2).setCellValue("Column C");

            // write and read back — SXSSFITestDataProvider returns XSSFWorkbook
            XSSFWorkbook wb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);

            XSSFSheet sheet2 = wb2.getSheet("test");
            XSSFRow row2 = sheet2.getRow(0);
            assertNotNull(row2);

            for (int i = 0; i < 3; i++) {
                XSSFCell cell = row2.getCell(i);
                assertNotNull(cell, "cell " + i + " should exist after read-back");
                XSSFCellStyle style = cell.getCellStyle();
                assertNotNull(style, "cell " + i + " should have a style after read-back");
                assertTrue(wb2.getFontAt(style.getFontIndex()).getBold(),
                        "cell " + i + " should be bold after write/read-back");
            }

            wb2.close();
        }
    }

}
