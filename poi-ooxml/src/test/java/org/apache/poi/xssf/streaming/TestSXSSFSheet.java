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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.ss.tests.usermodel.BaseTestXSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;


public final class TestSXSSFSheet extends BaseTestXSheet {

    public TestSXSSFSheet() {
        super(SXSSFITestDataProvider.instance);
    }


    @AfterEach
    void tearDown(){
        SXSSFITestDataProvider.instance.cleanup();
    }

    @Override
    protected void trackColumnsForAutoSizingIfSXSSF(Sheet sheet) {
        SXSSFSheet sxSheet = (SXSSFSheet) sheet;
        sxSheet.trackAllColumnsForAutoSizing();
    }


    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    protected void cloneSheet() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> super.cloneSheet());
        assertEquals("Not Implemented", ex.getMessage());
    }

    @Override
    @Test
    protected void cloneSheetMultipleTimes() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> super.cloneSheetMultipleTimes());
        assertEquals("Not Implemented", ex.getMessage());
    }

    /**
     * shifting rows is not supported in SXSSF
     */
    @Override
    @Test
    protected void shiftMerged() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> super.shiftMerged());
        assertEquals("Not Implemented", ex.getMessage());
    }

    /**
     *  Bug 35084: cloning cells with formula
     *
     *  The test is disabled because cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    protected void bug35084() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> super.bug35084());
        assertEquals("Not Implemented", ex.getMessage());
    }

    @Override
    protected void getCellComment() {
        // TODO: reading cell comments via Sheet does not work currently as it tries
        // to access the underlying sheet for this, but comments are stored as
        // properties on Cells...
    }

    @Test
    void overrideFlushedRows() throws IOException {
        try (Workbook wb = new SXSSFWorkbook(3)) {
            Sheet sheet = wb.createSheet();

            sheet.createRow(1);
            sheet.createRow(2);
            sheet.createRow(3);
            sheet.createRow(4);

            Throwable ex = assertThrows(Throwable.class, () -> sheet.createRow(1));
            assertEquals("Attempting to write a row[1] in the range [0,1] that is already written to disk.", ex.getMessage());
        }
    }

    @Test
    void flushBufferedDaat() throws IOException {
        try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            try (SXSSFWorkbook wb = new SXSSFWorkbook(1)) {
                SXSSFSheet sheet = wb.createSheet("my-sheet");

                sheet.createRow(1).createCell(0).setCellValue(1);
                sheet.createRow(2).createCell(0).setCellValue(2);
                sheet.createRow(3).createCell(0).setCellValue(3);

                sheet.flushBufferedData();

                sheet.createRow(4).createCell(0).setCellValue(4);
                sheet.createRow(5).createCell(0).setCellValue(5);
                sheet.createRow(6).createCell(0).setCellValue(6);

                wb.write(bos);
            }
            try (XSSFWorkbook wb = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet sheet = wb.getSheet("my-sheet");
                assertEquals(3, sheet.getRow(3).getCell(0).getNumericCellValue());
                assertEquals(6, sheet.getRow(6).getCell(0).getNumericCellValue());
            }
        }
    }

    @Test
    void overrideRowsInTemplate() throws IOException {
        try (XSSFWorkbook template = new XSSFWorkbook()) {
            template.createSheet().createRow(1);
            try (Workbook wb = new SXSSFWorkbook(template);) {
                Sheet sheet = wb.getSheetAt(0);
                Throwable e;
                e = assertThrows(Throwable.class, () -> sheet.createRow(1));
                assertEquals("Attempting to write a row[1] in the range [0,1] that is already written to disk.", e.getMessage());

                e = assertThrows(Throwable.class, () -> sheet.createRow(0));
                assertEquals("Attempting to write a row[0] in the range [0,1] that is already written to disk.", e.getMessage());

                sheet.createRow(2);
            }
        }
    }

    @Test
    void changeRowNum() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(3);
        SXSSFSheet sheet = wb.createSheet();
        SXSSFRow row0 = sheet.createRow(0);
        SXSSFRow row1 = sheet.createRow(1);
        sheet.changeRowNum(row0, 2);

        assertEquals(1, row1.getRowNum(), "Row 1 knows its row number");
        assertEquals(2, row0.getRowNum(), "Row 2 knows its row number");
        assertEquals(1, sheet.getRowNum(row1), "Sheet knows Row 1's row number");
        assertEquals(2, sheet.getRowNum(row0), "Sheet knows Row 2's row number");
        assertEquals(row1, sheet.iterator().next(), "Sheet row iteration order should be ascending");
        sheet.spliterator().tryAdvance(row ->
                assertEquals(row1, row, "Sheet row iteration order should be ascending"));

        wb.close();
    }

    @Test
    void groupRow() throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet();

            // XSSF code can group rows even if there are no XSSFRows yet, SXSSFWorkbook needs the rows to exist first
            for (int i = 0; i < 20; i++) {
                sheet.createRow(i);
            }

            //one level
            sheet.groupRow(9, 10);

            try(UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                workbook.write(bos);
                try(XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                    CTRow ctrow = xssfSheet.getRow(9).getCTRow();

                    assertNotNull(ctrow);
                    assertEquals(10, ctrow.getR());
                    assertEquals(1, ctrow.getOutlineLevel());
                    assertEquals(1, xssfSheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());
                }
            }
        }
    }

    @Test
    void groupRow2Levels() throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            SXSSFSheet sheet = workbook.createSheet();

            // XSSF code can group rows even if there are no XSSFRows yet, SXSSFWorkbook needs the rows to exist first
            for (int i = 0; i < 20; i++) {
                sheet.createRow(i);
            }

            //one level
            sheet.groupRow(9, 10);
            //two level
            sheet.groupRow(10, 13);

            try(UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                workbook.write(bos);
                try(XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                    CTRow ctrow = xssfSheet.getRow(9).getCTRow();

                    assertNotNull(ctrow);
                    assertEquals(10, ctrow.getR());
                    assertEquals(1, ctrow.getOutlineLevel());

                    ctrow = xssfSheet.getRow(10).getCTRow();
                    assertNotNull(ctrow);
                    assertEquals(11, ctrow.getR());
                    assertEquals(2, ctrow.getOutlineLevel());
                    assertEquals(2, xssfSheet.getCTWorksheet().getSheetFormatPr().getOutlineLevelRow());
                }
            }
        }
    }
}
