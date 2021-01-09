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

import static org.apache.poi.POITestCase.assertEndsWith;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.poi.ss.tests.usermodel.BaseTestXWorkbook;
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
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public final class TestSXSSFWorkbook extends BaseTestXWorkbook {

    public TestSXSSFWorkbook() {
        super(SXSSFITestDataProvider.instance);
    }

    @AfterEach
    void tearDown(){
        ((SXSSFITestDataProvider)_testDataProvider).cleanup();
    }

    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    protected void cloneSheet() throws IOException {
        RuntimeException e = assertThrows(RuntimeException.class, () -> super.cloneSheet());
        assertEquals("Not Implemented", e.getMessage());
    }

    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    protected void sheetClone() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> super.sheetClone());
        assertEquals("Not Implemented", e.getMessage());
    }

    /**
     * Skip this test, as SXSSF doesn't update formulas on sheet name
     *  changes.
     */
    @Override
    @Disabled("SXSSF doesn't update formulas on sheet name changes, as most cells probably aren't in memory at the time")
    protected void setSheetName() {
    }

    @Test
    void existingWorkbook() throws IOException {
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
	    wb1.close();
    }

    @Test
    void useSharedStringsTable() throws Exception {
        SXSSFWorkbook wb = new SXSSFWorkbook(null, 10, false, true);

        SharedStringsTable sss = wb.getSharedStringSource();

        assertNotNull(sss);

        Row row = wb.createSheet("S1").createRow(0);

        row.createCell(0).setCellValue("A");
        row.createCell(1).setCellValue("B");
        row.createCell(2).setCellValue("A");

        XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        sss = wb.getSharedStringSource();
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
        wb.close();
    }

    @Test
    void addToExistingWorkbook() throws IOException {
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
    	wb1.close();
    }

    @Test
    void sheetdataWriter() throws IOException{
        SXSSFWorkbook wb = new SXSSFWorkbook();
        SXSSFSheet sh = wb.createSheet();
        SheetDataWriter wr = sh.getSheetDataWriter();
        assertSame(wr.getClass(), SheetDataWriter.class);
        File tmp = wr.getTempFile();
        assertStartsWith(tmp.getName(), "poi-sxssf-sheet");
        assertEndsWith(tmp.getName(), ".xml");
        assertTrue(wb.dispose());
        wb.close();

        wb = new SXSSFWorkbook();
        wb.setCompressTempFiles(true);
        sh = wb.createSheet();
        wr = sh.getSheetDataWriter();
        assertSame(wr.getClass(), GZIPSheetDataWriter.class);
        tmp = wr.getTempFile();
        assertStartsWith(tmp.getName(), "poi-sxssf-sheet-xml");
        assertEndsWith(tmp.getName(), ".gz");
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
    void gzipSheetdataWriter() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        wb.setCompressTempFiles(true);

        final int rowNum = 1000;
        final int sheetNum = 5;
        populateData(wb, 1000, 5);

        XSSFWorkbook xwb = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb);
        for(int i = 0; i < sheetNum; i++){
            Sheet sh = xwb.getSheetAt(i);
            assertEquals("sheet" + i, sh.getSheetName());
            for(int j = 0; j < rowNum; j++){
                Row row = sh.getRow(j);
                assertNotNull(row, "row[" + j + "]");
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
        wb.close();
    }

    private static void assertWorkbookDispose(SXSSFWorkbook wb)
    {
        populateData(wb, 1000, 5);

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

    private static void populateData(Workbook wb, final int rowNum, final int sheetNum) {
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
    }

    @Test
    void workbookDispose() throws IOException {
        SXSSFWorkbook wb1 = new SXSSFWorkbook();
        // the underlying writer is SheetDataWriter
        assertWorkbookDispose(wb1);
        wb1.close();

        SXSSFWorkbook wb2 = new SXSSFWorkbook();
        wb2.setCompressTempFiles(true);
        // the underlying writer is GZIPSheetDataWriter
        assertWorkbookDispose(wb2);
        wb2.close();
    }

    @Disabled("currently writing the same sheet multiple times is not supported...")
    @Test
    void bug53515() throws Exception {
        Workbook wb1 = new SXSSFWorkbook(10);
        populateWorkbook(wb1);
        saveTwice(wb1);
        Workbook wb2 = new XSSFWorkbook();
        populateWorkbook(wb2);
        saveTwice(wb2);
        wb2.close();
        wb1.close();
    }

    @Disabled("Crashes the JVM because of documented JVM behavior with concurrent writing/reading of zip-files, "
            + "see http://www.oracle.com/technetwork/java/javase/documentation/overview-156328.html")
    void bug53515a() throws Exception {
        File out = new File("Test.xlsx");
        assertTrue(!out.exists() || out.delete());
        for (int i = 0; i < 2; i++) {
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
            try (NullOutputStream out = new NullOutputStream()) {
                wb.write(out);
            } catch (Exception e) {
                throw new Exception("ERROR: failed on " + (i + 1)
                        + "th time calling " + wb.getClass().getName()
                        + ".write() with exception " + e.getMessage(), e);
            }
        }
    }

    @Test
    void closeDoesNotModifyWorkbook() throws IOException {
        final String filename = "SampleSS.xlsx";
        final File file = POIDataSamples.getSpreadSheetInstance().getFile(filename);

        // Some tests commented out because close() modifies the file
        // See bug 58779

        // String
        //wb = new SXSSFWorkbook(new XSSFWorkbook(file.getPath()));
        //assertCloseDoesNotModifyFile(filename, wb);

        // File
        //wb = new SXSSFWorkbook(new XSSFWorkbook(file));
        //assertCloseDoesNotModifyFile(filename, wb);

        // InputStream

        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook xwb = new XSSFWorkbook(fis);
             SXSSFWorkbook wb = new SXSSFWorkbook(xwb)) {
            assertCloseDoesNotModifyFile(filename, wb);
        }

        // OPCPackage
        //wb = new SXSSFWorkbook(new XSSFWorkbook(OPCPackage.open(file)));
        //assertCloseDoesNotModifyFile(filename, wb);
    }

    /**
     * Bug #59743
     *
     * this is only triggered on other files apart of sheet[1,2,...].xml
     * as those are either copied uncompressed or with the use of GZIPInputStream
     * so we use shared strings
     */
    @Test
    void testZipBombNotTriggeredOnUselessContent() throws IOException {
        try (SXSSFWorkbook swb = new SXSSFWorkbook(null, 1, true, true)) {
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

            assertDoesNotThrow(() -> swb.write(new NullOutputStream()));
            swb.dispose();
        }
    }

    /**
     * To avoid accident changes to the template, you should be able
     *  to create a SXSSFWorkbook from a read-only XSSF one, then
     *  change + save that (only). See bug #60010
     * TODO Fix this to work!
     */
    @Test
    @Disabled
    void createFromReadOnlyWorkbook() throws Exception {
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
    void test56557() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56557.xlsx");

        // Using streaming XSSFWorkbook makes the output file invalid
        wb = new SXSSFWorkbook(((XSSFWorkbook) wb));

        // Should not throw POIXMLException: java.io.IOException: Unable to parse xml bean when reading back
        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();

        wb.close();
    }

    void changeSheetNameWithSharedFormulas() {
        /* not implemented */
    }
}
