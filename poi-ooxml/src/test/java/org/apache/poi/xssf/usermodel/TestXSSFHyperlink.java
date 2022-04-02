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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public final class TestXSSFHyperlink extends BaseTestHyperlink {
    public TestXSSFHyperlink() {
        super(XSSFITestDataProvider.instance);
    }

    @Test
    void testLoadExisting() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx")) {
            assertEquals(3, workbook.getNumberOfSheets());

            XSSFSheet sheet = workbook.getSheetAt(0);

            // Check the hyperlinks
            assertEquals(4, sheet.getNumHyperlinks());
            doTestHyperlinkContents(sheet);
        }
    }


    @Test
    public void readSharedHyperlink() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("sharedhyperlink.xlsx")) {
            XSSFSheet sheet = wb.getSheetAt(0);

            XSSFHyperlink hyperlink3 = sheet.getHyperlink(new CellAddress("A3"));
            XSSFHyperlink hyperlink4 = sheet.getHyperlink(new CellAddress("A4"));
            XSSFHyperlink hyperlink5 = sheet.getHyperlink(new CellAddress("A5"));
            assertNotNull(hyperlink3, "hyperlink found?");
            assertEquals(hyperlink3, hyperlink4);
            assertEquals(hyperlink3, hyperlink5);

            assertEquals("A3:A5", hyperlink3.getCellRef());
            assertEquals(0, hyperlink3.getFirstColumn());
            assertEquals(0, hyperlink3.getLastColumn());
            assertEquals(2, hyperlink3.getFirstRow());
            assertEquals(4, hyperlink3.getLastRow());
        }
    }

    @Test
    void testCreate() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow row = sheet.createRow(0);
        XSSFCreationHelper createHelper = workbook.getCreationHelper();

        String[] urls = {
                "http://apache.org",
                "www.apache.org",
                "/temp",
                "c:/temp",
                "http://apache.org/default.php?s=isTramsformed&submit=Search&la=*&li=*"};
        for(int i = 0; i < urls.length; i++){
            String s = urls[i];
            XSSFHyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(s);

            XSSFCell cell = row.createCell(i);
            cell.setHyperlink(link);
        }
        workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        PackageRelationshipCollection rels = sheet.getPackagePart().getRelationships();
        assertEquals(urls.length, rels.size());
        for(int i = 0; i < rels.size(); i++){
            PackageRelationship rel = rels.getRelationship(i);
            // there should be a relationship for each URL
            assertNotNull(rel);
            assertEquals(urls[i], rel.getTargetURI().toString());
        }

        // Bugzilla 53041: Hyperlink relations are duplicated when saving XSSF file
        workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);
        rels = sheet.getPackagePart().getRelationships();
        assertEquals(urls.length, rels.size());
        for(int i = 0; i < rels.size(); i++){
            PackageRelationship rel = rels.getRelationship(i);
            // there should be a relationship for each URL
            assertNotNull(rel);
            assertEquals(urls[i], rel.getTargetURI().toString());
        }
    }

    @Test
    void testInvalidURLs() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFCreationHelper createHelper = workbook.getCreationHelper();

            String[] invalidURLs = {
                "http:\\apache.org",
                "www.apache .org",
                "c:\\temp",
                "\\poi"};
            for (String s : invalidURLs) {
                assertThrows(IllegalArgumentException.class,
                    () -> createHelper.createHyperlink(HyperlinkType.URL).setAddress(s));
            }
        }
    }

    @Test
    void testLoadSave() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx")) {
            CreationHelper createHelper = workbook.getCreationHelper();
            assertEquals(3, workbook.getNumberOfSheets());
            XSSFSheet sheet = workbook.getSheetAt(0);

            // Check hyperlinks
            assertEquals(4, sheet.getNumHyperlinks());
            doTestHyperlinkContents(sheet);


            // Write out, and check

            // Load up again, check all links still there
            XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertEquals(3, wb2.getNumberOfSheets());
            assertNotNull(wb2.getSheetAt(0));
            assertNotNull(wb2.getSheetAt(1));
            assertNotNull(wb2.getSheetAt(2));

            sheet = wb2.getSheetAt(0);


            // Check hyperlinks again
            assertEquals(4, sheet.getNumHyperlinks());
            doTestHyperlinkContents(sheet);


            // Add one more, and re-check
            Row r17 = sheet.createRow(17);
            Cell r17c = r17.createCell(2);

            Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("http://poi.apache.org/spreadsheet/");
            hyperlink.setLabel("POI SS Link");
            r17c.setHyperlink(hyperlink);

            assertEquals(5, sheet.getNumHyperlinks());
            doTestHyperlinkContents(sheet);

            assertEquals(HyperlinkType.URL,
                    sheet.getRow(17).getCell(2).getHyperlink().getType());
            assertEquals("POI SS Link",
                    sheet.getRow(17).getCell(2).getHyperlink().getLabel());
            assertEquals("http://poi.apache.org/spreadsheet/",
                    sheet.getRow(17).getCell(2).getHyperlink().getAddress());


            // Save and re-load once more

            XSSFWorkbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
            assertEquals(3, wb3.getNumberOfSheets());
            assertNotNull(wb3.getSheetAt(0));
            assertNotNull(wb3.getSheetAt(1));
            assertNotNull(wb3.getSheetAt(2));

            sheet = wb3.getSheetAt(0);

            assertEquals(5, sheet.getNumHyperlinks());
            doTestHyperlinkContents(sheet);

            assertEquals(HyperlinkType.URL,
                    sheet.getRow(17).getCell(2).getHyperlink().getType());
            assertEquals("POI SS Link",
                    sheet.getRow(17).getCell(2).getHyperlink().getLabel());
            assertEquals("http://poi.apache.org/spreadsheet/",
                    sheet.getRow(17).getCell(2).getHyperlink().getAddress());
        }
    }

    @Test
    void testCopyHSSFHyperlink() throws IOException {
        try (HSSFWorkbook hssfworkbook = new HSSFWorkbook()) {
            HSSFHyperlink hlink = hssfworkbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            hlink.setAddress("http://poi.apache.org/");
            hlink.setFirstColumn(3);
            hlink.setFirstRow(2);
            hlink.setLastColumn(3);
            hlink.setLastRow(2);
            hlink.setLabel("label");
            XSSFHyperlink xlink = new XSSFHyperlink(hlink);

            assertEquals("http://poi.apache.org/", xlink.getAddress());
            assertEquals(new CellReference(2, 3), new CellReference(xlink.getCellRef()));
            // Are HSSFHyperlink.label and XSSFHyperlink.tooltip the same? If so, perhaps one of these needs renamed for a consistent Hyperlink interface
            // assertEquals("label", xlink.getTooltip());
        }
    }

    @Test
    void setHyperlinkOnCellFromHSSF() throws Exception {
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook()) {
            XSSFSheet xssfSheet = xssfWorkbook.createSheet();
            XSSFRow xssfRow = xssfSheet.createRow(0);
            XSSFCell xssfCell = xssfRow.createCell(0);
            xssfCell.setCellValue("link");
            HSSFHyperlink hssfHyperlink = new HSSFHyperlink(HyperlinkType.URL) {

            };
            hssfHyperlink.setLabel("label");
            hssfHyperlink.setAddress("https://example.com/index.html#label");
            hssfHyperlink.setFirstColumn(2);
            hssfHyperlink.setLastColumn(3);
            hssfHyperlink.setFirstRow(4);
            hssfHyperlink.setLastRow(5);
            xssfCell.setHyperlink(hssfHyperlink);
            XSSFHyperlink xssfHyperlink = xssfCell.getHyperlink();
            assertEquals(hssfHyperlink.getType(), xssfHyperlink.getType());
            assertEquals(hssfHyperlink.getAddress(), xssfHyperlink.getAddress());
            assertEquals(hssfHyperlink.getLabel(), xssfHyperlink.getLabel());
            assertEquals("A1", xssfHyperlink.getCellRef());
        }
    }

    @Test
    void setHyperlinkOnSheetFromHSSF() throws Exception {
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook()) {
            XSSFSheet xssfSheet = xssfWorkbook.createSheet();
            XSSFRow xssfRow = xssfSheet.createRow(0);
            XSSFCell xssfCell = xssfRow.createCell(0);
            xssfCell.setCellValue("link");
            HSSFHyperlink hssfHyperlink = new HSSFHyperlink(HyperlinkType.URL) {

            };
            hssfHyperlink.setLabel("label");
            hssfHyperlink.setAddress("https://example.com/index.html#label");
            hssfHyperlink.setFirstColumn(2);
            hssfHyperlink.setLastColumn(3);
            hssfHyperlink.setFirstRow(4);
            hssfHyperlink.setLastRow(5);
            XSSFHyperlink xssfHyperlink = new XSSFHyperlink(hssfHyperlink);
            xssfSheet.addHyperlink(xssfHyperlink);
            assertEquals(hssfHyperlink.getType(), xssfHyperlink.getType());
            assertEquals(hssfHyperlink.getAddress(), xssfHyperlink.getAddress());
            assertEquals(hssfHyperlink.getLabel(), xssfHyperlink.getLabel());
            assertEquals(hssfHyperlink.getFirstColumn(), xssfHyperlink.getFirstColumn());
            assertEquals(hssfHyperlink.getLastColumn(), xssfHyperlink.getLastColumn());
            assertEquals(hssfHyperlink.getFirstRow(), xssfHyperlink.getFirstRow());
            assertEquals(hssfHyperlink.getLastRow(), xssfHyperlink.getLastRow());
        }
    }

    /**
     * Only for WithMoreVariousData.xlsx !
     */
    private static void doTestHyperlinkContents(XSSFSheet sheet) {
        assertNotNull(sheet.getRow(3).getCell(2).getHyperlink());
        assertNotNull(sheet.getRow(14).getCell(2).getHyperlink());
        assertNotNull(sheet.getRow(15).getCell(2).getHyperlink());
        assertNotNull(sheet.getRow(16).getCell(2).getHyperlink());

        // First is a link to poi
        assertEquals(HyperlinkType.URL,
                sheet.getRow(3).getCell(2).getHyperlink().getType());
        assertNull(sheet.getRow(3).getCell(2).getHyperlink().getLabel());
        assertEquals("http://poi.apache.org/",
                sheet.getRow(3).getCell(2).getHyperlink().getAddress());

        // Next is an internal doc link
        assertEquals(HyperlinkType.DOCUMENT,
                sheet.getRow(14).getCell(2).getHyperlink().getType());
        assertEquals("Internal hyperlink to A2",
                sheet.getRow(14).getCell(2).getHyperlink().getLabel());
        assertEquals("Sheet1!A2",
                sheet.getRow(14).getCell(2).getHyperlink().getAddress());

        // Next is a file
        assertEquals(HyperlinkType.FILE,
                sheet.getRow(15).getCell(2).getHyperlink().getType());
        assertNull(sheet.getRow(15).getCell(2).getHyperlink().getLabel());
        assertEquals("WithVariousData.xlsx",
                sheet.getRow(15).getCell(2).getHyperlink().getAddress());

        // Last is a mailto
        assertEquals(HyperlinkType.EMAIL,
                sheet.getRow(16).getCell(2).getHyperlink().getType());
        assertNull(sheet.getRow(16).getCell(2).getHyperlink().getLabel());
        assertEquals("mailto:dev@poi.apache.org?subject=XSSF%20Hyperlinks",
                sheet.getRow(16).getCell(2).getHyperlink().getAddress());
    }

    @Test
    void test52716() throws IOException {
        try (XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("52716.xlsx")) {
            XSSFSheet sh1 = wb1.getSheetAt(0);

            XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
            XSSFSheet sh2 = wb2.getSheetAt(0);

            assertEquals(sh1.getNumberOfComments(), sh2.getNumberOfComments());
            XSSFHyperlink l1 = sh1.getHyperlink(0, 1);
            assertEquals(HyperlinkType.DOCUMENT, l1.getType());
            assertEquals("B1", l1.getCellRef());
            assertEquals("Sort on Titel", l1.getTooltip());

            XSSFHyperlink l2 = sh2.getHyperlink(0, 1);
            assertEquals(l1.getTooltip(), l2.getTooltip());
            assertEquals(HyperlinkType.DOCUMENT, l2.getType());
            assertEquals("B1", l2.getCellRef());
        }
    }

    @Test
    void test53734() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53734.xlsx")) {
            Hyperlink link = wb.getSheetAt(0).getRow(0).getCell(0).getHyperlink();
            assertEquals("javascript:///", link.getAddress());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                link = wb2.getSheetAt(0).getRow(0).getCell(0).getHyperlink();
                assertEquals("javascript:///", link.getAddress());
            }
        }
    }

    @Test
    void test53282() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53282.xlsx")) {
            Hyperlink link = wb.getSheetAt(0).getRow(0).getCell(14).getHyperlink();
            assertEquals("mailto:nobody@nowhere.uk%C2%A0", link.getAddress());

            try (XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                link = wb2.getSheetAt(0).getRow(0).getCell(14).getHyperlink();
                assertEquals("mailto:nobody@nowhere.uk%C2%A0", link.getAddress());
            }
        }
    }

    @Override
    public XSSFHyperlink copyHyperlink(Hyperlink link) {
        return new XSSFHyperlink(link);
    }

    /* bug 59775: XSSFHyperlink has wrong type if it contains a location (CTHyperlink#getLocation)
     * URLs with a hash mark (#) are still URL hyperlinks, not document links
     */
    @Test
    void testURLsWithHashMark() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("59775.xlsx");
        XSSFSheet sh = wb.getSheetAt(0);
        CellAddress A2 = new CellAddress("A2");
        CellAddress A3 = new CellAddress("A3");
        CellAddress A4 = new CellAddress("A4");
        CellAddress A7 = new CellAddress("A7");

        XSSFHyperlink link = sh.getHyperlink(A2);
        assertEquals("A2", link.getCellRef(), "address");
        assertEquals(HyperlinkType.URL, link.getType(), "link type");
        assertEquals("http://twitter.com/#!/apacheorg", link.getAddress(), "link target");

        link = sh.getHyperlink(A3);
        assertEquals("A3", link.getCellRef(), "address");
        assertEquals(HyperlinkType.URL, link.getType(), "link type");
        assertEquals("http://www.bailii.org/databases.html#ie", link.getAddress(), "link target");

        link = sh.getHyperlink(A4);
        assertEquals("A4", link.getCellRef(), "address");
        assertEquals(HyperlinkType.URL, link.getType(), "link type");
        assertEquals("https://en.wikipedia.org/wiki/Apache_POI#See_also", link.getAddress(), "link target");

        link = sh.getHyperlink(A7);
        assertEquals("A7", link.getCellRef(), "address");
        assertEquals(HyperlinkType.DOCUMENT, link.getType(), "link type");
        assertEquals("Sheet1", link.getAddress(), "link target");

        wb.close();
    }

    @Test
    void testCellStyle() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();

        CreationHelper createHelper = wb.getCreationHelper();
        XSSFCellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();

        Sheet sheet = wb.createSheet("test");

        Row rowDet = sheet.createRow(0);

        Cell cellDet = rowDet.createCell(7);
        cellDet.setCellValue("http://www.google.at");
        //set up style to be able to create hyperlinks
            hlinkFont.setColor(IndexedColors.BLUE.getIndex());
            hlinkStyle.setFont(hlinkFont);
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress("http://www.example.com");
            cellDet.setHyperlink(link);
            cellDet.setCellStyle(hlinkStyle);

        //set up style to be able to create hyperlinks
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        link = createHelper.createHyperlink(HyperlinkType.URL);

        //string for hyperlink
        cellDet = rowDet.createCell(13);
        cellDet.setCellValue("http://www.other.com");
        link.setAddress("http://www.gmx.at");
        cellDet.setHyperlink(link);
        cellDet.setCellStyle(hlinkStyle);

        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);

        assertNotNull(wbBack.getSheetAt(0).getRow(0).getCell(7).getHyperlink());
        assertNotNull(wbBack.getSheetAt(0).getRow(0).getCell(13).getHyperlink());

        wb.close();
        wbBack.close();
    }

    @Test
    void testChangeReference() {
        XSSFHyperlink hyperlink = new XSSFHyperlink(HyperlinkType.URL);
        hyperlink.setCellReference("B2");
        assertEquals(1, hyperlink.getFirstRow());
        assertEquals(1, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setFirstRow(0);
        assertEquals("B1:B2", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(1, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setLastRow(2);
        assertEquals("B1:B3", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setFirstColumn(0);
        assertEquals("A1:B3", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(0, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setLastColumn(2);
        assertEquals("A1:C3", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(0, hyperlink.getFirstColumn());
        assertEquals(2, hyperlink.getLastColumn());
        hyperlink.setFirstColumn(2);
        hyperlink.setFirstRow(2);
        assertEquals("C3", hyperlink.getCellRef());
        assertEquals(2, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(2, hyperlink.getFirstColumn());
        assertEquals(2, hyperlink.getLastColumn());
    }

    @Test
    void testChangeRowsAndColumns() {
        XSSFHyperlink hyperlink = new XSSFHyperlink(HyperlinkType.URL);
        hyperlink.setCellReference("B2");
        hyperlink.setLastRow(0);
        assertEquals("B1", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(0, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setLastColumn(0);
        assertEquals("A1", hyperlink.getCellRef());
        assertEquals(0, hyperlink.getFirstRow());
        assertEquals(0, hyperlink.getLastRow());
        assertEquals(0, hyperlink.getFirstColumn());
        assertEquals(0, hyperlink.getLastColumn());
        hyperlink.setFirstRow(1);
        assertEquals("A2", hyperlink.getCellRef());
        assertEquals(1, hyperlink.getFirstRow());
        assertEquals(1, hyperlink.getLastRow());
        assertEquals(0, hyperlink.getFirstColumn());
        assertEquals(0, hyperlink.getLastColumn());
        hyperlink.setFirstColumn(1);
        assertEquals("B2", hyperlink.getCellRef());
        assertEquals(1, hyperlink.getFirstRow());
        assertEquals(1, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setLastRow(2);
        assertEquals("B2:B3", hyperlink.getCellRef());
        assertEquals(1, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(1, hyperlink.getLastColumn());
        hyperlink.setLastColumn(2);
        assertEquals("B2:C3", hyperlink.getCellRef());
        assertEquals(1, hyperlink.getFirstRow());
        assertEquals(2, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(2, hyperlink.getLastColumn());
    }

    @Test
    void testRemoveSharedHyperlinkFromOneCell() throws IOException {
        testRemoveSharedHyperlinkFromOneCell("A1:E5", new CellAddress("C3"));
        testRemoveSharedHyperlinkFromOneCell("A1:E5", new CellAddress("A1"));
        testRemoveSharedHyperlinkFromOneCell("A1:E5", new CellAddress("E5"));
        testRemoveSharedHyperlinkFromOneCell("A1:E5", new CellAddress("E1"));
        testRemoveSharedHyperlinkFromOneCell("A1:E5", new CellAddress("A5"));
        testRemoveSharedHyperlinkFromOneCell("D3:D5", new CellAddress("D3"));
        testRemoveSharedHyperlinkFromOneCell("D3:D5", new CellAddress("D4"));
        testRemoveSharedHyperlinkFromOneCell("D3:D5", new CellAddress("D5"));
    }

    @Test
    void testRemoveSharedHyperlinkFromOneCellWithCellRefs() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFHyperlink hyperlink = new XSSFHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("https://poi.apache.org");
            hyperlink.setLocation("poi-location");
            hyperlink.setLabel("poi-label");
            hyperlink.setCellReference("A1:E5");
            sheet.addHyperlink(hyperlink);
            CellAddress cellAddress = new CellAddress("C3");
            sheet.removeHyperlink(cellAddress.getRow(), cellAddress.getColumn());
            assertNull(sheet.getHyperlink(cellAddress), "cell " + cellAddress.formatAsString() + "should no longer has a hyperlink");
            ArrayList<String> newRefs = new ArrayList<>();
            for (XSSFHyperlink testHyperlink : sheet.getHyperlinkList()) {
                newRefs.add(testHyperlink.getCellRef());
            }
            assertEquals(4, newRefs.size());
            HashSet<String> set = new HashSet<>(newRefs);
            assertTrue(set.contains("A1:B5"), "contains A1:B5");
            assertTrue(set.contains("D1:E5"), "contains D1:E5");
            assertTrue(set.contains("C1:C2"), "contains C1:C2");
            assertTrue(set.contains("C4:C5"), "contains C4:C5");
        }
    }

    private void testRemoveSharedHyperlinkFromOneCell(String area, CellAddress cellAddress) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFHyperlink hyperlink = new XSSFHyperlink(HyperlinkType.URL);
            hyperlink.setAddress("https://poi.apache.org");
            hyperlink.setLocation("poi-location");
            hyperlink.setLabel("poi-label");
            hyperlink.setCellReference(area);
            sheet.addHyperlink(hyperlink);
            AreaReference areaRef = new AreaReference(hyperlink.getCellRef(), SpreadsheetVersion.EXCEL2007);
            for (CellReference cellRef : areaRef.getAllReferencedCells()) {
                XSSFHyperlink testHyperlink = sheet.getHyperlink(cellRef.getRow(), cellRef.getCol());
                assertEquals(hyperlink, testHyperlink, "cell " + cellRef.formatAsString() + "has hyperlink?");
            }
            sheet.removeHyperlink(cellAddress.getRow(), cellAddress.getColumn());
            assertNull(sheet.getHyperlink(cellAddress), "cell " + cellAddress.formatAsString() + "should no longer has a hyperlink");
            for (CellReference cellRef : areaRef.getAllReferencedCells()) {
                if (cellRef.formatAsString().equals(cellAddress.formatAsString())) {
                    //ignore
                } else {
                    XSSFHyperlink testHyperlink = sheet.getHyperlink(cellRef.getRow(), cellRef.getCol());
                    assertEquals(hyperlink.getAddress(), testHyperlink.getAddress(), "cell " + cellRef.formatAsString() + "has hyperlink with right address?");
                    assertEquals(hyperlink.getLocation(), testHyperlink.getLocation(), "cell " + cellRef.formatAsString() + "has hyperlink with right location?");
                    assertEquals(hyperlink.getLabel(), testHyperlink.getLabel(), "cell " + cellRef.formatAsString() + "has hyperlink with right label?");
                }
            }
        }
    }

}
