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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.poi.ss.tests.usermodel.BaseTestXSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


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

    @Disabled
    @Override
    protected void defaultColumnStyle() {
        //TODO column styles are not yet supported by XSSF
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
        assertEquals(row1, sheet.iterator().next(), "Sheet row iteratation order should be ascending");

        wb.close();
    }
}
