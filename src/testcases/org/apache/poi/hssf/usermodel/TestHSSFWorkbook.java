/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.poi.hssf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.util.TempFile;
/**
 *
 */
public final class TestHSSFWorkbook extends TestCase {
    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    public void testSetRepeatingRowsAndColumns() {
        // Test bug 29747
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet();
        b.createSheet();
        b.createSheet();
        b.setRepeatingRowsAndColumns( 2, 0,1,-1,-1 );
        NameRecord nameRecord = b.getWorkbook().getNameRecord( 0 );
        assertEquals( 3, nameRecord.getIndexToSheet() );
    }

    public void testCaseInsensitiveNames() {
        HSSFWorkbook b = new HSSFWorkbook( );
        HSSFSheet originalSheet = b.createSheet("Sheet1");
        HSSFSheet fetchedSheet = b.getSheet("sheet1");
        if(fetchedSheet == null) {
            throw new AssertionFailedError("Identified bug 44892");
        }
        assertEquals(originalSheet, fetchedSheet);
        try {
            b.createSheet("sHeeT1");
            fail("should have thrown exceptiuon due to duplicate sheet name");
        } catch (IllegalArgumentException e) {
            // expected during successful test
            assertEquals("The workbook already contains a sheet of this name", e.getMessage());
        }
    }

    public void testDuplicateNames() {
        HSSFWorkbook b = new HSSFWorkbook( );
        b.createSheet("Sheet1");
        b.createSheet();
        b.createSheet("name1");
        try
        {
            b.createSheet("name1");
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }
        b.createSheet();
        try
        {
            b.setSheetName( 3,  "name1" );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        try
        {
            b.setSheetName( 3,  "name1" );
            fail();
        }
        catch ( IllegalArgumentException pass )
        {
        }

        b.setSheetName( 3,  "name2" );
        b.setSheetName( 3,  "name2" );
        b.setSheetName( 3,  "name2" );

        HSSFWorkbook c = new HSSFWorkbook( );
        c.createSheet("Sheet1");
        c.createSheet("Sheet2");
        c.createSheet("Sheet3");
        c.createSheet("Sheet4");

    }

    public void testWindowOneDefaults() {
        HSSFWorkbook b = new HSSFWorkbook( );
        try {
            assertEquals(b.getActiveSheetIndex(), 0);
            assertEquals(b.getFirstVisibleTab(), 0);
        } catch (NullPointerException npe) {
            fail("WindowOneRecord in Workbook is probably not initialized");
        }
    }

    public void testSheetSelection() {
        HSSFWorkbook b = new HSSFWorkbook();
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");
        b.setActiveSheet(1);
        b.setSelectedTab(1);
        b.setFirstVisibleTab(1);
        assertEquals(1, b.getActiveSheetIndex());
        assertEquals(1, b.getFirstVisibleTab());
    }

    public void testSheetClone() {
        // First up, try a simple file
        HSSFWorkbook b = new HSSFWorkbook();
        assertEquals(0, b.getNumberOfSheets());
        b.createSheet("Sheet One");
        b.createSheet("Sheet Two");

        assertEquals(2, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(3, b.getNumberOfSheets());

        // Now try a problem one with drawing records in it
        b = openSample("SheetWithDrawing.xls");
        assertEquals(1, b.getNumberOfSheets());
        b.cloneSheet(0);
        assertEquals(2, b.getNumberOfSheets());
    }

    public void testReadWriteWithCharts() {
        HSSFWorkbook b;
        HSSFSheet s;

        // Single chart, two sheets
        b = openSample("44010-SingleChart.xls");
        assertEquals(2, b.getNumberOfSheets());
        assertEquals("Graph2", b.getSheetName(1));
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());

        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b.getSheetAt(0).getDrawingPatriarch());
        assertNull(b.getSheetAt(1).getDrawingPatriarch());
        assertFalse(b.getSheetAt(0).getDrawingPatriarch().containsChart());

        // We've now called getDrawingPatriarch() so
        //  everything will be all screwy
        // So, start again
        b = openSample("44010-SingleChart.xls");

        b = writeRead(b);
        assertEquals(2, b.getNumberOfSheets());
        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());


        // Two charts, three sheets
        b = openSample("44010-TwoCharts.xls");
        assertEquals(3, b.getNumberOfSheets());

        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());

        // Has chart on 1st sheet??
        // FIXME
        assertNotNull(b.getSheetAt(0).getDrawingPatriarch());
        assertNull(b.getSheetAt(1).getDrawingPatriarch());
        assertNull(b.getSheetAt(2).getDrawingPatriarch());
        assertFalse(b.getSheetAt(0).getDrawingPatriarch().containsChart());

        // We've now called getDrawingPatriarch() so
        //  everything will be all screwy
        // So, start again
        b = openSample("44010-TwoCharts.xls");

        b = writeRead(b);
        assertEquals(3, b.getNumberOfSheets());

        s = b.getSheetAt(1);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
        s = b.getSheetAt(2);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(8, s.getLastRowNum());
    }

    private static HSSFWorkbook writeRead(HSSFWorkbook b) {
        return HSSFTestDataSamples.writeOutAndReadBack(b);
    }


    public void testSelectedSheet_bug44523() {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        confirmActiveSelected(sheet1, true);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        wb.setSelectedTab(1);

        // Demonstrate bug 44525:
        // Well... not quite, since isActive + isSelected were also added in the same bug fix
        if (sheet1.isSelected()) {
            throw new AssertionFailedError("Identified bug 44525 a");
        }
        wb.setActiveSheet(1);
        if (sheet1.isActive()) {
            throw new AssertionFailedError("Identified bug 44525 b");
        }

        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, true);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);
    }

    public void testSelectMultiple() {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");
        HSSFSheet sheet5 = wb.createSheet("Sheet5");
        HSSFSheet sheet6 = wb.createSheet("Sheet6");

        wb.setSelectedTabs(new int[] { 0, 2, 3});

        assertEquals(true, sheet1.isSelected());
        assertEquals(false, sheet2.isSelected());
        assertEquals(true, sheet3.isSelected());
        assertEquals(true, sheet4.isSelected());
        assertEquals(false, sheet5.isSelected());
        assertEquals(false, sheet6.isSelected());

        wb.setSelectedTabs(new int[] { 1, 3, 5});

        assertEquals(false, sheet1.isSelected());
        assertEquals(true, sheet2.isSelected());
        assertEquals(false, sheet3.isSelected());
        assertEquals(true, sheet4.isSelected());
        assertEquals(false, sheet5.isSelected());
        assertEquals(true, sheet6.isSelected());

        assertEquals(true, sheet1.isActive());
        assertEquals(false, sheet2.isActive());


        assertEquals(true, sheet1.isActive());
        assertEquals(false, sheet3.isActive());
        wb.setActiveSheet(2);
        assertEquals(false, sheet1.isActive());
        assertEquals(true, sheet3.isActive());

        if (false) { // helpful if viewing this workbook in excel:
            sheet1.createRow(0).createCell((short)0).setCellValue(new HSSFRichTextString("Sheet1"));
            sheet2.createRow(0).createCell((short)0).setCellValue(new HSSFRichTextString("Sheet2"));
            sheet3.createRow(0).createCell((short)0).setCellValue(new HSSFRichTextString("Sheet3"));
            sheet4.createRow(0).createCell((short)0).setCellValue(new HSSFRichTextString("Sheet4"));

            try {
                File fOut = TempFile.createTempFile("sheetMultiSelect", ".xls");
                FileOutputStream os = new FileOutputStream(fOut);
                wb.write(os);
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void testActiveSheetAfterDelete_bug40414() {
        HSSFWorkbook wb=new HSSFWorkbook();
        HSSFSheet sheet0 = wb.createSheet("Sheet0");
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("Sheet3");
        HSSFSheet sheet4 = wb.createSheet("Sheet4");

        // confirm default activation/selection
        confirmActiveSelected(sheet0, true);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, false);
        confirmActiveSelected(sheet4, false);

        wb.setActiveSheet(3);
        wb.setSelectedTab(3);

        confirmActiveSelected(sheet0, false);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet3, true);
        confirmActiveSelected(sheet4, false);

        wb.removeSheetAt(3);
        // after removing the only active/selected sheet, another should be active/selected in its place
        if (!sheet4.isSelected()) {
            throw new AssertionFailedError("identified bug 40414 a");
        }
        if (!sheet4.isActive()) {
            throw new AssertionFailedError("identified bug 40414 b");
        }

        confirmActiveSelected(sheet0, false);
        confirmActiveSelected(sheet1, false);
        confirmActiveSelected(sheet2, false);
        confirmActiveSelected(sheet4, true);

        sheet3 = sheet4; // re-align local vars in this test case

        // Some more cases of removing sheets

        // Starting with a multiple selection, and different active sheet
        wb.setSelectedTabs(new int[] { 1, 3, });
        wb.setActiveSheet(2);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet1, false, true);
        confirmActiveSelected(sheet2, true,  false);
        confirmActiveSelected(sheet3, false, true);

        // removing a sheet that is not active, and not the only selected sheet
        wb.removeSheetAt(3);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet1, false, true);
        confirmActiveSelected(sheet2, true,  false);

        // removing the only selected sheet
        wb.removeSheetAt(1);
        confirmActiveSelected(sheet0, false, false);
        confirmActiveSelected(sheet2, true,  true);

        // The last remaining sheet should always be active+selected
        wb.removeSheetAt(1);
        confirmActiveSelected(sheet0, true,  true);
    }

    private static void confirmActiveSelected(HSSFSheet sheet, boolean expected) {
        confirmActiveSelected(sheet, expected, expected);
    }


    private static void confirmActiveSelected(HSSFSheet sheet,
            boolean expectedActive, boolean expectedSelected) {
        assertEquals("active", expectedActive, sheet.isActive());
        assertEquals("selected", expectedSelected, sheet.isSelected());
    }
}
