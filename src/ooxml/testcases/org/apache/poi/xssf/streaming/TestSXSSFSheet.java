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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestXSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Test;


public final class TestSXSSFSheet extends BaseTestXSheet {

    public TestSXSSFSheet() {
        super(SXSSFITestDataProvider.instance);
    }


    @After
    public void tearDown(){
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
    public void cloneSheet() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Not Implemented");
        super.cloneSheet();
    }

    @Override
    @Test
    public void cloneSheetMultipleTimes() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Not Implemented");
        super.cloneSheetMultipleTimes();
    }
    
    /**
     * shifting rows is not supported in SXSSF
     */
    @Override
    @Test
    public void shiftMerged() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Not Implemented");
        super.shiftMerged();
    }

    /**
     *  Bug 35084: cloning cells with formula
     *
     *  The test is disabled because cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    public void bug35084() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Not Implemented");
        super.bug35084();
    }

    @Override
    @Test
    public void getCellComment() throws IOException {
        // TODO: reading cell comments via Sheet does not work currently as it tries 
        // to access the underlying sheet for this, but comments are stored as
        // properties on Cells...
    }
    
    @Override
    @Test
    public void defaultColumnStyle() {
        //TODO column styles are not yet supported by XSSF
    }

    @Test
    public void overrideFlushedRows() throws IOException {
        Workbook wb = new SXSSFWorkbook(3);
        try {
            Sheet sheet = wb.createSheet();
    
            sheet.createRow(1);
            sheet.createRow(2);
            sheet.createRow(3);
            sheet.createRow(4);
    
            thrown.expect(Throwable.class);
            thrown.expectMessage("Attempting to write a row[1] in the range [0,1] that is already written to disk.");
            sheet.createRow(1);
        } finally {
            wb.close();
        }
    }

    @Test
    public void overrideRowsInTemplate() throws IOException {
        XSSFWorkbook template = new XSSFWorkbook();
        template.createSheet().createRow(1);

        Workbook wb = new SXSSFWorkbook(template);
        try {
            Sheet sheet = wb.getSheetAt(0);
    
            try {
                sheet.createRow(1);
                fail("expected exception");
            } catch (Throwable e){
                assertEquals("Attempting to write a row[1] in the range [0,1] that is already written to disk.", e.getMessage());
            }
            try {
                sheet.createRow(0);
                fail("expected exception");
            } catch (Throwable e){
                assertEquals("Attempting to write a row[0] in the range [0,1] that is already written to disk.", e.getMessage());
            }
            sheet.createRow(2);
        } finally {
            wb.close();
            template.close();
        }
    }

    @Test
    public void changeRowNum() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(3);
        SXSSFSheet sheet = wb.createSheet();
        SXSSFRow row0 = sheet.createRow(0);
        SXSSFRow row1 = sheet.createRow(1);
        sheet.changeRowNum(row0, 2);
        
        assertEquals("Row 1 knows its row number", 1, row1.getRowNum());
        assertEquals("Row 2 knows its row number", 2, row0.getRowNum());
        assertEquals("Sheet knows Row 1's row number", 1, sheet.getRowNum(row1));
        assertEquals("Sheet knows Row 2's row number", 2, sheet.getRowNum(row0));
        assertEquals("Sheet row iteratation order should be ascending", row1, sheet.iterator().next());
        
        wb.close();
    }
}
