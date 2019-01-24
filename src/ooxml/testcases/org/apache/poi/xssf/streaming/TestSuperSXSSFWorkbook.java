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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BaseTestXWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.NullOutputStream;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public final class TestSuperSXSSFWorkbook extends BaseTestXWorkbook {
    
    public TestSuperSXSSFWorkbook() {
        super(SXSSFITestDataProvider.instance);
    }
    
    @After
    public void tearDown() {
        ((SXSSFITestDataProvider) _testDataProvider).cleanup();
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
        } catch (RuntimeException e) {
            assertEquals("Not Implemented", e.getMessage());
        }
    }
    
    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    public void sheetClone() throws IOException {
        try {
            super.sheetClone();
            fail("expected exception");
        } catch (RuntimeException e) {
            assertEquals("Not Implemented", e.getMessage());
        }
    }
    
    /**
     * Skip this test, as SXSSF doesn't update formulas on sheet name changes.
     */
    @Override
    @Ignore("SXSSF doesn't update formulas on sheet name changes, as most cells probably aren't in memory at the time")
    @Test
    public void setSheetName() {
    }
    
    @Test
    public void existingWorkbook() throws IOException {
        XSSFWorkbook xssfWb1 = new XSSFWorkbook();
        xssfWb1.createSheet("S1");
        SuperSXSSFWorkbook wb1 = new SuperSXSSFWorkbook(xssfWb1);
        XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1);
        assertTrue(wb1.dispose());
        
        SuperSXSSFWorkbook wb2 = new SuperSXSSFWorkbook(xssfWb2);
        assertEquals(1, wb2.getNumberOfSheets());
        Sheet sheet = wb2.getStreamingSheetAt(0);
        assertNotNull(sheet);
        assertEquals("S1", sheet.getSheetName());
        assertTrue(wb2.dispose());
        xssfWb2.close();
        xssfWb1.close();
        
        wb2.close();
        wb1.close();
    }
    
    @Test
    public void useSharedStringsTable() throws Exception {
        // not supported with SuperSXSSF
    }
    
    @Test
    public void addToExistingWorkbook() throws IOException {
        XSSFWorkbook xssfWb1 = new XSSFWorkbook();
        xssfWb1.createSheet("S1");
        Sheet sheet = xssfWb1.createSheet("S2");
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellValue("value 2_1_1");
        SuperSXSSFWorkbook wb1 = new SuperSXSSFWorkbook(xssfWb1);
        XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1);
        assertTrue(wb1.dispose());
        xssfWb1.close();
        
        SuperSXSSFWorkbook wb2 = new SuperSXSSFWorkbook(xssfWb2);
        // Add a row to the existing empty sheet
        SuperSXSSFSheet ssheet1 = wb2.getStreamingSheetAt(0);
        ssheet1.setRowGenerator((ssxSheet) -> {
            Row row1_1 = ssxSheet.createRow(1);
            Cell cell1_1_1 = row1_1.createCell(1);
            cell1_1_1.setCellValue("value 1_1_1");
        });
        
        // Add a row to the existing non-empty sheet
        SuperSXSSFSheet ssheet2 = wb2.getStreamingSheetAt(1);
        ssheet2.setRowGenerator((ssxSheet) -> {
            Row row2_2 = ssxSheet.createRow(2);
            Cell cell2_2_1 = row2_2.createCell(1);
            cell2_2_1.setCellValue("value 2_2_1");
        });
        // Add a sheet with one row
        SuperSXSSFSheet ssheet3 = wb2.createSheet("S3");
        ssheet3.setRowGenerator((ssxSheet) -> {
            Row row3_1 = ssxSheet.createRow(1);
            Cell cell3_1_1 = row3_1.createCell(1);
            cell3_1_1.setCellValue("value 3_1_1");
        });
        
        XSSFWorkbook xssfWb3 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb2);
        wb2.close();
        
        assertEquals(3, xssfWb3.getNumberOfSheets());
        // Verify sheet 1
        XSSFSheet sheet1 = xssfWb3.getSheetAt(0);
        assertEquals("S1", sheet1.getSheetName());
        assertEquals(1, sheet1.getPhysicalNumberOfRows());
        XSSFRow row1_1 = sheet1.getRow(1);
        assertNotNull(row1_1);
        XSSFCell cell1_1_1 = row1_1.getCell(1);
        assertNotNull(cell1_1_1);
        assertEquals("value 1_1_1", cell1_1_1.getStringCellValue());
        // Verify sheet 2
        XSSFSheet sheet2 = xssfWb3.getSheetAt(1);
        assertEquals("S2", sheet2.getSheetName());
        assertEquals(2, sheet2.getPhysicalNumberOfRows());
        Row row2_1 = sheet2.getRow(1);
        assertNotNull(row2_1);
        Cell cell2_1_1 = row2_1.getCell(1);
        assertNotNull(cell2_1_1);
        assertEquals("value 2_1_1", cell2_1_1.getStringCellValue());
        XSSFRow row2_2 = sheet2.getRow(2);
        assertNotNull(row2_2);
        XSSFCell cell2_2_1 = row2_2.getCell(1);
        assertNotNull(cell2_2_1);
        assertEquals("value 2_2_1", cell2_2_1.getStringCellValue());
        // Verify sheet 3
        XSSFSheet sheet3 = xssfWb3.getSheetAt(2);
        assertEquals("S3", sheet3.getSheetName());
        assertEquals(1, sheet3.getPhysicalNumberOfRows());
        XSSFRow row3_1 = sheet3.getRow(1);
        assertNotNull(row3_1);
        XSSFCell cell3_1_1 = row3_1.getCell(1);
        assertNotNull(cell3_1_1);
        assertEquals("value 3_1_1", cell3_1_1.getStringCellValue());
        
        xssfWb2.close();
        xssfWb3.close();
        wb1.close();
    }
    
    @Test
    public void sheetdataWriter() throws IOException {
        SuperSXSSFWorkbook wb = new SuperSXSSFWorkbook();
        SXSSFSheet sh = wb.createSheet();
        assertSame(sh.getClass(), SuperSXSSFSheet.class);
        SheetDataWriter wr = sh.getSheetDataWriter();
        assertNull(wr);
        wb.close();
    }
    
    @Test
    public void gzipSheetdataWriter() throws IOException {
        SuperSXSSFWorkbook wb = new SuperSXSSFWorkbook();
        wb.setCompressTempFiles(true);
        
        final int rowNum = 1000;
        final int sheetNum = 5;
        populateData(wb, 1000, 5);
        
        XSSFWorkbook xwb = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        for (int i = 0; i < sheetNum; i++) {
            Sheet sh = xwb.getSheetAt(i);
            assertEquals("sheet" + i, sh.getSheetName());
            for (int j = 0; j < rowNum; j++) {
                Row row = sh.getRow(j);
                assertNotNull("row[" + j + "]", row);
                Cell cell1 = row.getCell(0);
                assertEquals(new CellReference(cell1).formatAsString(), cell1.getStringCellValue());
                
                Cell cell2 = row.getCell(1);
                assertEquals(i, (int) cell2.getNumericCellValue());
                
                Cell cell3 = row.getCell(2);
                assertEquals(j, (int) cell3.getNumericCellValue());
            }
        }
        
        assertTrue(wb.dispose());
        xwb.close();
        wb.close();
    }
    
    private static void assertWorkbookDispose(SuperSXSSFWorkbook wb) {
        populateData(wb, 1000, 5);
        
        for (Sheet sheet : wb) {
            SuperSXSSFSheet sxSheet = (SuperSXSSFSheet) sheet;
            assertNull(sxSheet.getSheetDataWriter());
        }
        
        assertTrue(wb.dispose());
        
        for (Sheet sheet : wb) {
            SuperSXSSFSheet sxSheet = (SuperSXSSFSheet) sheet;
            assertNull(sxSheet.getSheetDataWriter());
        }
    }
    
    private static void populateData(SuperSXSSFWorkbook wb, final int rowNum, final int sheetNum) {
        for (int i = 0; i < sheetNum; i++) {
            SuperSXSSFSheet sheet = wb.createSheet("sheet" + i);
            int index = i;
            sheet.setRowGenerator((sh) -> {
                for (int j = 0; j < rowNum; j++) {
                    Row row = sh.createRow(j);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(new CellReference(cell1).formatAsString());
                    
                    Cell cell2 = row.createCell(1);
                    cell2.setCellValue(index);
                    
                    Cell cell3 = row.createCell(2);
                    cell3.setCellValue(j);
                }
            });
        }
    }
    
    @Test
    public void workbookDispose() throws IOException {
        SuperSXSSFWorkbook wb1 = new SuperSXSSFWorkbook();
        // the underlying writer is SheetDataWriter
        assertWorkbookDispose(wb1);
        wb1.close();
        
        SuperSXSSFWorkbook wb2 = new SuperSXSSFWorkbook();
        wb2.setCompressTempFiles(true);
        // the underlying writer is GZIPSheetDataWriter
        assertWorkbookDispose(wb2);
        wb2.close();
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
        assertTrue(!out.exists() || out.delete());
        for (int i = 0; i < 2; i++) {
            final SXSSFWorkbook wb;
            if (out.exists()) {
                wb = new SXSSFWorkbook((XSSFWorkbook) WorkbookFactory.create(out));
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
            wb.close();
        }
        assertTrue(out.exists());
        assertTrue(out.delete());
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
                throw new Exception("ERROR: failed on " + (i + 1) + "th time calling " + wb.getClass().getName()
                        + ".write() with exception " + e.getMessage(), e);
            }
        }
    }
    
    @Test
    public void closeDoesNotModifyWorkbook() throws IOException {
        final String filename = "SampleSS.xlsx";
        final File file = POIDataSamples.getSpreadSheetInstance().getFile(filename);
        
        // Some tests commented out because close() modifies the file
        // See bug 58779
        
        // String
        // wb = new SXSSFWorkbook(new XSSFWorkbook(file.getPath()));
        // assertCloseDoesNotModifyFile(filename, wb);
        
        // File
        // wb = new SXSSFWorkbook(new XSSFWorkbook(file));
        // assertCloseDoesNotModifyFile(filename, wb);
        
        // InputStream
        
        try (FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook xwb = new XSSFWorkbook(fis);
                SXSSFWorkbook wb = new SXSSFWorkbook(xwb)) {
            assertCloseDoesNotModifyFile(filename, wb);
        }
        
        // OPCPackage
        // wb = new SXSSFWorkbook(new XSSFWorkbook(OPCPackage.open(file)));
        // assertCloseDoesNotModifyFile(filename, wb);
    }
    
    /**
     * Bug #59743
     * 
     * this is only triggered on other files apart of sheet[1,2,...].xml as those are either copied uncompressed or with
     * the use of GZIPInputStream so we use shared strings
     */
    @Test
    public void testZipBombNotTriggeredOnUselessContent() throws IOException {
        SXSSFWorkbook swb = new SXSSFWorkbook(null, 1, true, true);
        SXSSFSheet s = swb.createSheet();
        char[] useless = new char[32767];
        Arrays.fill(useless, ' ');
        
        for (int row = 0; row < 1; row++) {
            Row r = s.createRow(row);
            for (int col = 0; col < 10; col++) {
                char[] prefix = Integer.toHexString(row * 1000 + col).toCharArray();
                Arrays.fill(useless, 0, 10, ' ');
                System.arraycopy(prefix, 0, useless, 0, prefix.length);
                String ul = new String(useless);
                r.createCell(col, CellType.STRING).setCellValue(ul);
            }
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        swb.write(bos);
        swb.dispose();
        swb.close();
    }
    
    /**
     * To avoid accident changes to the template, you should be able to create a SXSSFWorkbook from a read-only XSSF
     * one, then change + save that (only). See bug #60010 TODO Fix this to work!
     */
    @Test
    @Ignore
    public void createFromReadOnlyWorkbook() throws Exception {
        String sheetName = "Test SXSSF";
        File input = XSSFTestDataSamples.getSampleFile("sample.xlsx");
        
        try (OPCPackage pkg = OPCPackage.open(input, PackageAccess.READ)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (XSSFWorkbook xssf = new XSSFWorkbook(pkg)) {
                try (SXSSFWorkbook wb = new SXSSFWorkbook(xssf, 2)) {
                    Sheet s = wb.createSheet(sheetName);
                    for (int i = 0; i < 10; i++) {
                        Row r = s.createRow(i);
                        r.createCell(0).setCellValue(true);
                        r.createCell(1).setCellValue(2.4);
                        r.createCell(2).setCellValue("Test Row " + i);
                    }
                    assertEquals(10, s.getLastRowNum());
                    
                    wb.write(bos);
                    wb.dispose();
                }
            }
            
            try (XSSFWorkbook xssf = new XSSFWorkbook(new ByteArrayInputStream(bos.toByteArray()))) {
                Sheet s = xssf.getSheet(sheetName);
                assertEquals(10, s.getLastRowNum());
                assertTrue(s.getRow(0).getCell(0).getBooleanCellValue());
                assertEquals("Test Row 9", s.getRow(9).getCell(2).getStringCellValue());
            }
        }
    }
    
    @Test
    public void test56557() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56557.xlsx");
        
        // Using streaming XSSFWorkbook makes the output file invalid
        wb = new SXSSFWorkbook(((XSSFWorkbook) wb));
        
        // Should not throw POIXMLException: java.io.IOException: Unable to parse xml bean when reading back
        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();
        
        wb.close();
    }
}
