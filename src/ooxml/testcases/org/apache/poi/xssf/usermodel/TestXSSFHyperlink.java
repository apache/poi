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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public final class TestXSSFHyperlink extends BaseTestHyperlink {
    public TestXSSFHyperlink() {
        super(XSSFITestDataProvider.instance);
    }

    @Test
    public void testLoadExisting() {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
        assertEquals(3, workbook.getNumberOfSheets());

        XSSFSheet sheet = workbook.getSheetAt(0);

        // Check the hyperlinks
        assertEquals(4, sheet.getNumHyperlinks());
        doTestHyperlinkContents(sheet);
    }

    @Test
    public void testCreate() throws Exception {
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
    public void testInvalidURLs() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCreationHelper createHelper = workbook.getCreationHelper();

        String[] invalidURLs = {
                "http:\\apache.org",
                "www.apache .org",
                "c:\\temp",
                "\\poi"};
        for(String s : invalidURLs){
            try {
                createHelper.createHyperlink(HyperlinkType.URL).setAddress(s);
                fail("expected IllegalArgumentException: " + s);
            } catch (IllegalArgumentException e){
                // expected here
            }
        }
        workbook.close();
    }

    @Test
    public void testLoadSave() {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("WithMoreVariousData.xlsx");
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
        assertEquals(null,
                sheet.getRow(3).getCell(2).getHyperlink().getLabel());
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
        assertEquals(null,
                sheet.getRow(15).getCell(2).getHyperlink().getLabel());
        assertEquals("WithVariousData.xlsx",
                sheet.getRow(15).getCell(2).getHyperlink().getAddress());

        // Last is a mailto
        assertEquals(HyperlinkType.EMAIL,
                sheet.getRow(16).getCell(2).getHyperlink().getType());
        assertEquals(null,
                sheet.getRow(16).getCell(2).getHyperlink().getLabel());
        assertEquals("mailto:dev@poi.apache.org?subject=XSSF%20Hyperlinks",
                sheet.getRow(16).getCell(2).getHyperlink().getAddress());
    }

    @Test
    public void test52716() {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("52716.xlsx");
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

    @Test
    public void test53734() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53734.xlsx");
        XSSFHyperlink link = wb.getSheetAt(0).getRow(0).getCell(0).getHyperlink();
        assertEquals("javascript:///", link.getAddress());

        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        link = wb.getSheetAt(0).getRow(0).getCell(0).getHyperlink();
        assertEquals("javascript:///", link.getAddress());
    }

    @Test
    public void test53282() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53282.xlsx");
        XSSFHyperlink link = wb.getSheetAt(0).getRow(0).getCell(14).getHyperlink();
        assertEquals("mailto:nobody@nowhere.uk%C2%A0", link.getAddress());

        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        link = wb.getSheetAt(0).getRow(0).getCell(14).getHyperlink();
        assertEquals("mailto:nobody@nowhere.uk%C2%A0", link.getAddress());
    }
    
    @Override
    public XSSFHyperlink copyHyperlink(Hyperlink link) {
        return new XSSFHyperlink(link);
    }
    
    @Test
    public void testCopyHSSFHyperlink() throws IOException {
        HSSFWorkbook hssfworkbook = new HSSFWorkbook();
        HSSFHyperlink hlink = hssfworkbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
        hlink.setAddress("http://poi.apache.org/");
        hlink.setFirstColumn(3);
        hlink.setFirstRow(2);
        hlink.setLastColumn(5);
        hlink.setLastRow(6);
        hlink.setLabel("label");
        XSSFHyperlink xlink = new XSSFHyperlink(hlink);
        
        assertEquals("http://poi.apache.org/", xlink.getAddress());
        assertEquals(new CellReference(2, 3), new CellReference(xlink.getCellRef()));
        // Are HSSFHyperlink.label and XSSFHyperlink.tooltip the same? If so, perhaps one of these needs renamed for a consistent Hyperlink interface
        // assertEquals("label", xlink.getTooltip());
        
        hssfworkbook.close();
    }
    
    /* bug 59775: XSSFHyperlink has wrong type if it contains a location (CTHyperlink#getLocation)
     * URLs with a hash mark (#) are still URL hyperlinks, not document links
     */
    @Test
    public void testURLsWithHashMark() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("59775.xlsx");
        XSSFSheet sh = wb.getSheetAt(0);
        CellAddress A2 = new CellAddress("A2");
        CellAddress A3 = new CellAddress("A3");
        CellAddress A4 = new CellAddress("A4");
        CellAddress A7 = new CellAddress("A7");
        
        XSSFHyperlink link = sh.getHyperlink(A2);
        assertEquals("address", "A2", link.getCellRef());
        assertEquals("link type", HyperlinkType.URL, link.getType());
        assertEquals("link target", "http://twitter.com/#!/apacheorg", link.getAddress());
        
        link = sh.getHyperlink(A3);
        assertEquals("address", "A3", link.getCellRef());
        assertEquals("link type", HyperlinkType.URL, link.getType());
        assertEquals("link target", "http://www.bailii.org/databases.html#ie", link.getAddress());
        
        link = sh.getHyperlink(A4);
        assertEquals("address", "A4", link.getCellRef());
        assertEquals("link type", HyperlinkType.URL, link.getType());
        assertEquals("link target", "https://en.wikipedia.org/wiki/Apache_POI#See_also", link.getAddress());
        
        link = sh.getHyperlink(A7);
        assertEquals("address", "A7", link.getCellRef());
        assertEquals("link type", HyperlinkType.DOCUMENT, link.getType());
        assertEquals("link target", "Sheet1", link.getAddress());
        
        wb.close();
    }

    @Test
    public void test() throws IOException {
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
}
