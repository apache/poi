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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.POITestCase;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BaseTestWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public final class TestSXSSFWorkbook extends BaseTestWorkbook {
    private static final SXSSFITestDataProvider _testDataProvider = SXSSFITestDataProvider.instance;

    public TestSXSSFWorkbook() {
		super(_testDataProvider);
	}

    @After
    public void tearDown(){
        _testDataProvider.cleanup();
    }

    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    public void cloneSheet() throws IOException {
        try {
            super.cloneSheet();
            fail("expected exception");
        } catch (RuntimeException e){
            assertEquals("NotImplemented", e.getMessage());
        }
    }

    /**
     * Skip this test, as SXSSF doesn't update formulas on sheet name
     *  changes.
     */
    @Override
    @Test
    public void setSheetName() {
        Assume.assumeTrue("SXSSF doesn't update formulas on sheet name changes, as most cells probably aren't in memory at the time", false);
    }

    @Test
    public void existingWorkbook() throws IOException {
    	XSSFWorkbook xssfWb1 = new XSSFWorkbook();
    	xssfWb1.createSheet("S1");
        SXSSFWorkbook wb1 = new SXSSFWorkbook(xssfWb1);
    	XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1);
    	assertTrue(wb1.dispose());

        SXSSFWorkbook wb2 = new SXSSFWorkbook(xssfWb2);
    	assertEquals(1, wb2.getNumberOfSheets());
    	Sheet sheet  = wb2.getSheetAt(0);
    	assertNotNull(sheet);
    	assertEquals("S1", sheet.getSheetName());
	    assertTrue(wb2.dispose());
	    xssfWb2.close();
	    xssfWb1.close();
	    
	    wb2.close();
    }

    @Test
    public void useSharedStringsTable() throws Exception {
        SXSSFWorkbook wb = new SXSSFWorkbook(null, 10, false, true);

        SharedStringsTable sss =  POITestCase.getFieldValue(SXSSFWorkbook.class, wb, SharedStringsTable.class, "_sharedStringSource");
        
        assertNotNull(sss);

        Row row = wb.createSheet("S1").createRow(0);

        row.createCell(0).setCellValue("A");
        row.createCell(1).setCellValue("B");
        row.createCell(2).setCellValue("A");

        XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        sss = POITestCase.getFieldValue(SXSSFWorkbook.class, wb, SharedStringsTable.class, "_sharedStringSource");
        assertEquals(2, sss.getUniqueCount());
        assertTrue(wb.dispose());

        Sheet sheet1 = xssfWorkbook.getSheetAt(0);
        assertEquals("S1", sheet1.getSheetName());
        assertEquals(1, sheet1.getPhysicalNumberOfRows());
        row = sheet1.getRow(0);
        assertNotNull(row);
        Cell cell = row.getCell(0);
        assertNotNull(cell);
        assertEquals("A", cell.getStringCellValue());
        cell = row.getCell(1);
        assertNotNull(cell);
        assertEquals("B", cell.getStringCellValue());
        cell = row.getCell(2);
        assertNotNull(cell);
        assertEquals("A", cell.getStringCellValue());

        xssfWorkbook.close();
    }

    @Test
    public void addToExistingWorkbook() throws IOException {
    	XSSFWorkbook xssfWb1 = new XSSFWorkbook();
    	xssfWb1.createSheet("S1");
    	Sheet sheet = xssfWb1.createSheet("S2");
    	Row row = sheet.createRow(1);
    	Cell cell = row.createCell(1);
    	cell.setCellValue("value 2_1_1");
        SXSSFWorkbook wb1 = new SXSSFWorkbook(xssfWb1);
    	XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1);
        assertTrue(wb1.dispose());
        xssfWb1.close();

        SXSSFWorkbook wb2 = new SXSSFWorkbook(xssfWb2);
    	// Add a row to the existing empty sheet
    	Sheet sheet1 = wb2.getSheetAt(0);
    	Row row1_1 = sheet1.createRow(1);
    	Cell cell1_1_1 = row1_1.createCell(1);
    	cell1_1_1.setCellValue("value 1_1_1");

    	// Add a row to the existing non-empty sheet
    	Sheet sheet2 = wb2.getSheetAt(1);
    	Row row2_2 = sheet2.createRow(2);
    	Cell cell2_2_1 = row2_2.createCell(1);
    	cell2_2_1.setCellValue("value 2_2_1");

    	// Add a sheet with one row
    	Sheet sheet3 = wb2.createSheet("S3");
    	Row row3_1 = sheet3.createRow(1);
    	Cell cell3_1_1 = row3_1.createCell(1);
    	cell3_1_1.setCellValue("value 3_1_1");

    	XSSFWorkbook xssfWb3 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb2);
    	wb2.close();
    	
    	assertEquals(3, xssfWb3.getNumberOfSheets());
    	// Verify sheet 1
    	sheet1 = xssfWb3.getSheetAt(0);
    	assertEquals("S1", sheet1.getSheetName());
    	assertEquals(1, sheet1.getPhysicalNumberOfRows());
    	row1_1 = sheet1.getRow(1);
    	assertNotNull(row1_1);
    	cell1_1_1 = row1_1.getCell(1);
    	assertNotNull(cell1_1_1);
    	assertEquals("value 1_1_1", cell1_1_1.getStringCellValue());
    	// Verify sheet 2
    	sheet2 = xssfWb3.getSheetAt(1);
    	assertEquals("S2", sheet2.getSheetName());
    	assertEquals(2, sheet2.getPhysicalNumberOfRows());
    	Row row2_1 = sheet2.getRow(1);
    	assertNotNull(row2_1);
    	Cell cell2_1_1 = row2_1.getCell(1);
    	assertNotNull(cell2_1_1);
    	assertEquals("value 2_1_1", cell2_1_1.getStringCellValue());
    	row2_2 = sheet2.getRow(2);
    	assertNotNull(row2_2);
    	cell2_2_1 = row2_2.getCell(1);
    	assertNotNull(cell2_2_1);
    	assertEquals("value 2_2_1", cell2_2_1.getStringCellValue());
    	// Verify sheet 3
    	sheet3 = xssfWb3.getSheetAt(2);
    	assertEquals("S3", sheet3.getSheetName());
    	assertEquals(1, sheet3.getPhysicalNumberOfRows());
    	row3_1 = sheet3.getRow(1);
    	assertNotNull(row3_1);
    	cell3_1_1 = row3_1.getCell(1);
    	assertNotNull(cell3_1_1);
    	assertEquals("value 3_1_1", cell3_1_1.getStringCellValue());
    	
        xssfWb2.close();
    	xssfWb3.close();
    }

    @Test
    public void sheetdataWriter() throws IOException{
        SXSSFWorkbook wb = new SXSSFWorkbook();
        SXSSFSheet sh = wb.createSheet();
        SheetDataWriter wr = sh.getSheetDataWriter();
        assertTrue(wr.getClass() == SheetDataWriter.class);
        File tmp = wr.getTempFile();
        assertTrue(tmp.getName().startsWith("poi-sxssf-sheet"));
        assertTrue(tmp.getName().endsWith(".xml"));
        assertTrue(wb.dispose());
        wb.close();

        wb = new SXSSFWorkbook();
        wb.setCompressTempFiles(true);
        sh = wb.createSheet();
        wr = sh.getSheetDataWriter();
        assertTrue(wr.getClass() == GZIPSheetDataWriter.class);
        tmp = wr.getTempFile();
        assertTrue(tmp.getName().startsWith("poi-sxssf-sheet-xml"));
        assertTrue(tmp.getName().endsWith(".gz"));
        assertTrue(wb.dispose());
        wb.close();

        //Test escaping of Unicode control characters
        wb = new SXSSFWorkbook();
        wb.createSheet("S1").createRow(0).createCell(0).setCellValue("value\u0019");
        XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        Cell cell = xssfWorkbook.getSheet("S1").getRow(0).getCell(0);
        assertEquals("value?", cell.getStringCellValue());

        assertTrue(wb.dispose());
        wb.close();
        xssfWorkbook.close();
    }

    @Test
    public void gzipSheetdataWriter() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        wb.setCompressTempFiles(true);
        int rowNum = 1000;
        int sheetNum = 5;
        for(int i = 0; i < sheetNum; i++){
            Sheet sh = wb.createSheet("sheet" + i);
            for(int j = 0; j < rowNum; j++){
                Row row = sh.createRow(j);
                Cell cell1 = row.createCell(0);
                cell1.setCellValue(new CellReference(cell1).formatAsString());

                Cell cell2 = row.createCell(1);
                cell2.setCellValue(i);

                Cell cell3 = row.createCell(2);
                cell3.setCellValue(j);
            }
        }

        XSSFWorkbook xwb = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        for(int i = 0; i < sheetNum; i++){
            Sheet sh = xwb.getSheetAt(i);
            assertEquals("sheet" + i, sh.getSheetName());
            for(int j = 0; j < rowNum; j++){
                Row row = sh.getRow(j);
                assertNotNull("row[" + j + "]", row);
                Cell cell1 = row.getCell(0);
                assertEquals(new CellReference(cell1).formatAsString(), cell1.getStringCellValue());

                Cell cell2 = row.getCell(1);
                assertEquals(i, (int)cell2.getNumericCellValue());

                Cell cell3 = row.getCell(2);
                assertEquals(j, (int)cell3.getNumericCellValue());
            }
        }

        assertTrue(wb.dispose());
        xwb.close();
    }

    static void assertWorkbookDispose(SXSSFWorkbook wb)
    {
        int rowNum = 1000;
        int sheetNum = 5;
        for(int i = 0; i < sheetNum; i++){
            Sheet sh = wb.createSheet("sheet" + i);
            for(int j = 0; j < rowNum; j++){
                Row row = sh.createRow(j);
                Cell cell1 = row.createCell(0);
                cell1.setCellValue(new CellReference(cell1).formatAsString());

                Cell cell2 = row.createCell(1);
                cell2.setCellValue(i);

                Cell cell3 = row.createCell(2);
                cell3.setCellValue(j);
            }
        }

        for (Sheet sheet : wb) {
            SXSSFSheet sxSheet = (SXSSFSheet) sheet;
            assertTrue(sxSheet.getSheetDataWriter().getTempFile().exists());
        }

        assertTrue(wb.dispose());

        for (Sheet sheet : wb) {
            SXSSFSheet sxSheet = (SXSSFSheet) sheet;
            assertFalse(sxSheet.getSheetDataWriter().getTempFile().exists());
        }
    }

    @Test
    public void workbookDispose()
    {
        SXSSFWorkbook wb1 = new SXSSFWorkbook();
        // the underlying writer is SheetDataWriter
        assertWorkbookDispose(wb1);

        SXSSFWorkbook wb2 = new SXSSFWorkbook();
        wb2.setCompressTempFiles(true);
        // the underlying writer is GZIPSheetDataWriter
        assertWorkbookDispose(wb2);

    }

    @Ignore("currently writing the same sheet multiple times is not supported...")
    @Test
    public void bug53515() throws Exception {
        Workbook wb1 = new SXSSFWorkbook(10);
        populateWorkbook(wb1);
        saveTwice(wb1);
        Workbook wb2 = new XSSFWorkbook();
        populateWorkbook(wb2);
        saveTwice(wb2);
        wb2.close();
        wb1.close();
    }

    @Ignore("Crashes the JVM because of documented JVM behavior with concurrent writing/reading of zip-files, "
            + "see http://www.oracle.com/technetwork/java/javase/documentation/overview-156328.html")
    @Test
    public void bug53515a() throws Exception {
        File out = new File("Test.xlsx");
        out.delete();
        for (int i = 0; i < 2; i++) {
            System.out.println("Iteration " + i);
            final SXSSFWorkbook wb;
            if (out.exists()) {
                wb = new SXSSFWorkbook(
                        (XSSFWorkbook) WorkbookFactory.create(out));
            } else {
                wb = new SXSSFWorkbook(10);
            }

            try {
                FileOutputStream outSteam = new FileOutputStream(out);
                if (i == 0) {
                    populateWorkbook(wb);
                } else {
                    System.gc();
                    System.gc();
                    System.gc();
                }

                wb.write(outSteam);
                // assertTrue(wb.dispose());
                outSteam.close();
            } finally {
                assertTrue(wb.dispose());
            }
        }
        out.delete();
    }

    private static void populateWorkbook(Workbook wb) {
        Sheet sh = wb.createSheet();
        for (int rownum = 0; rownum < 100; rownum++) {
            Row row = sh.createRow(rownum);
            for (int cellnum = 0; cellnum < 10; cellnum++) {
                Cell cell = row.createCell(cellnum);
                String address = new CellReference(cell).formatAsString();
                cell.setCellValue(address);
            }
        }
    }

    private static void saveTwice(Workbook wb) throws Exception {
        for (int i = 0; i < 2; i++) {
            try {
                NullOutputStream out = new NullOutputStream();
                wb.write(out);
                out.close();
            } catch (Exception e) {
                throw new Exception("ERROR: failed on " + (i + 1)
                        + "th time calling " + wb.getClass().getName()
                        + ".write() with exception " + e.getMessage(), e);
            }
        }
    }

    @Ignore("Just a local test for http://stackoverflow.com/questions/33627329/apache-poi-streaming-api-using-xssf-template")
    @Test
    public void testTemplateFile() throws IOException {
        XSSFWorkbook workBook = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        SXSSFWorkbook streamingWorkBook = new SXSSFWorkbook(workBook,10);
        Sheet sheet = streamingWorkBook.getSheet("Sheet1");
        for(int rowNum = 10;rowNum < 1000000;rowNum++) {
            Row row = sheet.createRow(rowNum);
            for(int cellNum = 0;cellNum < 700;cellNum++) {
                Cell cell = row.createCell(cellNum);
                cell.setCellValue("somevalue");
            }
            
            if(rowNum % 100 == 0) {
                System.out.print(".");
                if(rowNum % 10000 == 0) {
                    System.out.println(rowNum);
                }
            }
        }

        streamingWorkBook.write(new FileOutputStream("C:\\temp\\streaming.xlsx"));
        
        streamingWorkBook.close();
        workBook.close();
    }
    
    @Test
    @Override
    public void getSpreadsheetVersion() throws IOException {
        verifySpreadsheetVersion(SpreadsheetVersion.EXCEL2007);
    }
}
