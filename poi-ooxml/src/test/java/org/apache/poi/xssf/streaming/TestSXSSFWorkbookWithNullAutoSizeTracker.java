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

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.tests.usermodel.BaseTestXWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.apache.poi.POITestCase.assertEndsWith;
import static org.apache.poi.POITestCase.assertStartsWith;
import static org.junit.jupiter.api.Assertions.*;

final class TestSXSSFWorkbookWithNullAutoSizeTracker extends BaseTestXWorkbook {

    TestSXSSFWorkbookWithNullAutoSizeTracker() {
        super(SXSSFITestDataProviderWithNullAutoSizeTracker.instance);
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
    public void cloneSheet() throws IOException {
        RuntimeException e = assertThrows(RuntimeException.class, super::cloneSheet);
        assertEquals("Not Implemented", e.getMessage());
    }

    /**
     * cloning of sheets is not supported in SXSSF
     */
    @Override
    @Test
    public void sheetClone() {
        RuntimeException e = assertThrows(RuntimeException.class, super::sheetClone);
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
        try (XSSFWorkbook xssfWb1 = new XSSFWorkbook()) {
            xssfWb1.createSheet("S1");
            try (SXSSFWorkbook wb1 = new SXSSFWorkbook(xssfWb1);
                 XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1)) {
                assertTrue(wb1.dispose());

                try (SXSSFWorkbook wb2 = new SXSSFWorkbook(xssfWb2)) {
                    assertEquals(1, wb2.getNumberOfSheets());
                    Sheet sheet = wb2.getSheetAt(0);
                    assertNotNull(sheet);
                    assertEquals("S1", sheet.getSheetName());
                    assertTrue(wb2.dispose());
                }
            }
        }
    }

    @Test
    void useSharedStringsTable() throws Exception {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 10, false, true)) {

            SharedStringsTable sss = wb.getSharedStringSource();

            assertNotNull(sss);

            Row row = wb.createSheet("S1").createRow(0);

            row.createCell(0).setCellValue("A");
            row.createCell(1).setCellValue("B");
            row.createCell(2).setCellValue("A");

            try (XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb)) {
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
            }
        }
    }

    @Test
    void useSharedStringsTableWithRichText() throws Exception {
        testUseSharedStringsTableWithRichText(false);
    }

    @Test
    void useSharedStringsTableWithRichTextAndCompression() throws Exception {
        testUseSharedStringsTableWithRichText(true);
    }

    @Test
    void addToExistingWorkbook() throws IOException {
        try (XSSFWorkbook xssfWb1 = new XSSFWorkbook()) {
            xssfWb1.createSheet("S1");
            Sheet sheet = xssfWb1.createSheet("S2");
            Row row = sheet.createRow(1);
            Cell cell = row.createCell(1);
            cell.setCellValue("value 2_1_1");
            try (SXSSFWorkbook wb1 = new SXSSFWorkbook(xssfWb1);
                 XSSFWorkbook xssfWb2 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb1)) {
                assertTrue(wb1.dispose());

                try (SXSSFWorkbook wb2 = new SXSSFWorkbook(xssfWb2)) {
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

                    try (XSSFWorkbook xssfWb3 = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb2)) {
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
                    }
                }
            }
        }
    }

    @Test
    void sheetdataWriter() throws IOException{
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            SXSSFSheet sh = wb.createSheet();
            SheetDataWriter wr = sh.getSheetDataWriter();
            assertSame(wr.getClass(), SheetDataWriter.class);
            File tmp = wr.getTempFile();
            assertStartsWith(tmp.getName(), "poi-sxssf-sheet");
            assertEndsWith(tmp.getName(), ".xml");
            assertTrue(wb.dispose());
        }

        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            wb.setCompressTempFiles(true);
            SXSSFSheet sh = wb.createSheet();
            SheetDataWriter wr = sh.getSheetDataWriter();
            assertSame(wr.getClass(), GZIPSheetDataWriter.class);
            File tmp = wr.getTempFile();
            assertStartsWith(tmp.getName(), "poi-sxssf-sheet-xml");
            assertEndsWith(tmp.getName(), ".gz");
            assertTrue(wb.dispose());
        }

        //Test escaping of Unicode control characters
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            wb.createSheet("S1").createRow(0).createCell(0).setCellValue("value\u0019");
            try (XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb)) {
                Cell cell = xssfWorkbook.getSheet("S1").getRow(0).getCell(0);
                assertEquals("value?", cell.getStringCellValue());
                assertTrue(wb.dispose());
            }
        }
    }

    @Test
    void gzipSheetdataWriter() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            wb.setCompressTempFiles(true);

            final int rowNum = 1000;
            final int sheetNum = 5;
            populateData(wb);

            try (XSSFWorkbook xwb = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb)) {
                for (int i = 0; i < sheetNum; i++) {
                    Sheet sh = xwb.getSheetAt(i);
                    assertEquals("sheet" + i, sh.getSheetName());
                    for (int j = 0; j < rowNum; j++) {
                        Row row = sh.getRow(j);
                        assertNotNull(row, "row[" + j + "]");
                        Cell cell1 = row.getCell(0);
                        assertEquals(new CellReference(cell1).formatAsString(), cell1.getStringCellValue());

                        Cell cell2 = row.getCell(1);
                        assertEquals(i, (int) cell2.getNumericCellValue());

                        Cell cell3 = row.getCell(2);
                        assertEquals(j, (int) cell3.getNumericCellValue());
                    }
                }

                assertTrue(wb.dispose());
            }
        }
    }

    private static void populateData(Workbook wb) {
        for(int i = 0; i < 5; i++){
            Sheet sh = wb.createSheet("sheet" + i);
            for(int j = 0; j < 1000; j++){
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void workbookDispose(boolean compressTempFiles) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            // compressTempFiles == false: the underlying writer is SheetDataWriter
            // compressTempFiles == true: the underlying writer is GZIPSheetDataWriter
            wb.setCompressTempFiles(compressTempFiles);

            populateData(wb);

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
    }

    @Test
    void bug53515() throws Exception {
        try (Workbook wb1 = new SXSSFWorkbook(10)) {
            populateWorkbook(wb1);
            assertDoesNotThrow(() -> wb1.write(NullOutputStream.INSTANCE));
            assertDoesNotThrow(() -> wb1.write(NullOutputStream.INSTANCE));
            try (Workbook wb2 = new XSSFWorkbook()) {
                populateWorkbook(wb2);
                assertDoesNotThrow(() -> wb2.write(NullOutputStream.INSTANCE));
                assertDoesNotThrow(() -> wb2.write(NullOutputStream.INSTANCE));
            }
        }
    }

    @Disabled("Crashes the JVM because of documented JVM behavior with concurrent writing/reading of zip-files, "
            + "see http://www.oracle.com/technetwork/java/javase/documentation/overview-156328.html")
    @Test
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
                    /*
                        Code explicitly invokes garbage collection. Except for specific use in benchmarking,
                        this is very dubious.

                        In the past, situations where people have explicitly invoked the garbage collector in
                        routines such as close or finalize methods has led to huge performance black holes.
                        Garbage collection can be expensive. Any situation that forces hundreds or thousands
                        of garbage collections will bring the machine to a crawl.
                     */

                    //System.gc();
                    //System.gc();
                    //System.gc();
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

            for (int row = 0; row < 10; row++) {
                Row r = s.createRow(row);
                for (int col = 0; col < 10; col++) {
                    char[] prefix = Integer.toHexString(row * 10 + col).toCharArray();
                    Arrays.fill(useless, 0, 10, ' ');
                    System.arraycopy(prefix, 0, useless, 0, prefix.length);
                    String ul = new String(useless);
                    r.createCell(col, CellType.STRING).setCellValue(ul);
                }
            }

            assertDoesNotThrow(() -> swb.write(NullOutputStream.INSTANCE));
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
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get();
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

            try (XSSFWorkbook xssf = new XSSFWorkbook(bos.toInputStream())) {
                Sheet s = xssf.getSheet(sheetName);
                assertEquals(10, s.getLastRowNum());
                assertTrue(s.getRow(0).getCell(0).getBooleanCellValue());
                assertEquals("Test Row 9", s.getRow(9).getCell(2).getStringCellValue());
            }
        }
    }


    @Test
    void test56557() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56557.xlsx");
             // Using streaming XSSFWorkbook makes the output file invalid
             Workbook wb2 = new SXSSFWorkbook(wb);
             // Should not throw POIXMLException: java.io.IOException: Unable to parse xml bean when reading back
             Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb2)
         ) {
            assertNotNull(wbBack);
        }
    }

    @Test
    void addHyperlink() throws Exception {
        try (
            SXSSFWorkbook wb = new SXSSFWorkbook();
            UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            SXSSFSheet sheet = wb.createSheet("s1");
            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell = row.createCell(0);
            cell.setCellValue("Example Website");
            XSSFHyperlink hyperlink = (XSSFHyperlink)wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("http://example.com");
            hyperlink.setCellReference("A1");
            sheet.addHyperlink(hyperlink);
            wb.write(bos);

            try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet xssfSheet = xssfWorkbook.getSheet(sheet.getSheetName());
                XSSFCell xssfCell = xssfSheet.getRow(0).getCell(0);
                assertEquals("Example Website", xssfCell.getStringCellValue());
                XSSFHyperlink xssfHyperlink = xssfCell.getHyperlink();
                assertEquals(hyperlink.getAddress(), xssfHyperlink.getAddress());
            }
        }
    }

    @Test
    void addDimension() throws IOException {
        try (
                SXSSFWorkbook wb = new SXSSFWorkbook();
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            SXSSFSheet sheet = wb.createSheet();
            sheet.createRow(2).createCell(3).setCellValue("top left");
            sheet.createRow(6).createCell(5).setCellValue("bottom right");
            assertEquals(2, sheet.getFirstRowNum());
            assertEquals(6, sheet.getLastRowNum());
            wb.write(bos);
            try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                assertEquals(CellRangeAddress.valueOf("D3:F7"), xssfSheet.getDimension());
            }
        }
    }

    @Test
    void addDimension1() throws IOException {
        try (
                SXSSFWorkbook wb = new SXSSFWorkbook(1);
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            SXSSFSheet sheet = wb.createSheet();
            sheet.createRow(2).createCell(3).setCellValue("top left");
            sheet.createRow(6).createCell(5).setCellValue("bottom right");
            assertEquals(2, sheet.getFirstRowNum());
            assertEquals(6, sheet.getLastRowNum());
            wb.write(bos);
            try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                assertEquals(CellRangeAddress.valueOf("D3:F7"), xssfSheet.getDimension());
            }
        }
    }

    @Test
    void addDimensionXSSFtoSXSSF() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            sheet.createRow(2).createCell(3).setCellValue("top left");
            sheet.createRow(6).createCell(5).setCellValue("bottom right");
            assertEquals(2, sheet.getFirstRowNum());
            assertEquals(6, sheet.getLastRowNum());
            try (
                    SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(wb);
                    UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
            ) {
                sxssfWorkbook.write(bos);
                try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                    XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                    assertEquals(CellRangeAddress.valueOf("D3:F7"), xssfSheet.getDimension());
                }
            }
        }
    }

    @Test
    void addDimensionDisabled() throws IOException {
        try (
                SXSSFWorkbook wb = new SXSSFWorkbook();
                UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()
        ) {
            wb.setShouldCalculateSheetDimensions(false);
            SXSSFSheet sheet = wb.createSheet();
            sheet.createRow(2).createCell(3).setCellValue("top left");
            sheet.createRow(6).createCell(5).setCellValue("bottom right");
            assertEquals(2, sheet.getFirstRowNum());
            assertEquals(6, sheet.getLastRowNum());
            wb.write(bos);
            try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(bos.toInputStream())) {
                XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
                assertEquals(CellRangeAddress.valueOf("A1:A1"), xssfSheet.getDimension());
            }
        }
    }

    @Override
    @Disabled("not implemented")
    protected void changeSheetNameWithSharedFormulas() {
    }

    private void testUseSharedStringsTableWithRichText(boolean compressTempFiles) throws Exception {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(null, 10, compressTempFiles, true)) {

            SharedStringsTable sss = wb.getSharedStringSource();

            assertNotNull(sss);

            XSSFFont redFont = new XSSFFont();
            redFont.setColor(new XSSFColor(new java.awt.Color(241,76,93), new DefaultIndexedColorMap()));

            Row row = wb.createSheet("S1").createRow(0);

            row.createCell(0).setCellValue("A");
            row.createCell(1).setCellValue("B");
            XSSFRichTextString rts = new XSSFRichTextString("A");
            rts.applyFont(redFont);
            row.createCell(2).setCellValue(rts);

            try (XSSFWorkbook xssfWorkbook = SXSSFITestDataProvider.instance.writeOutAndReadBack(wb)) {
                sss = wb.getSharedStringSource();
                assertEquals(3, sss.getUniqueCount());
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
                XSSFRichTextString outputRichTextString = (XSSFRichTextString) cell.getRichStringCellValue();
                XSSFFont outputFont = outputRichTextString.getFontAtIndex(0);
                assertEquals(redFont, outputFont);
            }
        }
    }

    @Test
    void disableAutoSizeTracker() throws IOException {
        try (Workbook workbook = _testDataProvider.createWorkbook(10)) {
            assertThrows(IllegalStateException.class, () -> {
                Sheet sheet = workbook.createSheet("testSheet");
                sheet.createRow(0).createCell(0).setCellValue(0);
                sheet.autoSizeColumn(0);
            });
        }
    }

}
