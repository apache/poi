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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestHyperlink;

/**
 * Tests HSSFHyperlink.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFHyperlink extends BaseTestHyperlink {

    @Override
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }
    /**
     * Test that we can read hyperlinks.
     */
    public void testRead() {

        HSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("HyperlinksOnManySheets.xls");

        HSSFSheet sheet;
        HSSFCell cell;
        HSSFHyperlink link;

        sheet = wb.getSheet("WebLinks");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("POI", link.getLabel());
        assertEquals("POI", cell.getRichStringCellValue().getString());
        assertEquals("http://poi.apache.org/", link.getAddress());

        cell = sheet.getRow(8).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("HSSF", link.getLabel());
        assertEquals("HSSF", cell.getRichStringCellValue().getString());
        assertEquals("http://poi.apache.org/hssf/", link.getAddress());

        sheet = wb.getSheet("Emails");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("dev", link.getLabel());
        assertEquals("dev", cell.getRichStringCellValue().getString());
        assertEquals("mailto:dev@poi.apache.org", link.getAddress());

        sheet = wb.getSheet("Internal");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("Link To First Sheet", link.getLabel());
        assertEquals("Link To First Sheet", cell.getRichStringCellValue().getString());
        assertEquals("WebLinks!A1", link.getTextMark());
        assertEquals("WebLinks!A1", link.getAddress());
    }

    public void testModify() {
        HSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("HyperlinksOnManySheets.xls");

        HSSFSheet sheet;
        HSSFCell cell;
        HSSFHyperlink link;

        sheet = wb.getSheet("WebLinks");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        //modify the link
        link.setAddress("www.apache.org");

        wb = getTestDataProvider().writeOutAndReadBack(wb);
        sheet = wb.getSheet("WebLinks");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("www.apache.org", link.getAddress());

    }

    /**
     * HSSF-specific ways of creating links to a place in workbook.
     * You can set the target  in two ways:
     *  link.setTextMark("'Target Sheet-1'!A1"); //HSSF-specific
     *  or
     *  link.setAddress("'Target Sheet-1'!A1"); //common between XSSF and HSSF
     */
    public void testCreateDocumentLink() {
        HSSFWorkbook wb = getTestDataProvider().createWorkbook();

        //link to a place in this workbook
        HSSFHyperlink link;
        HSSFCell cell;
        HSSFSheet sheet = wb.createSheet("Hyperlinks");

        //create a target sheet and cell
        HSSFSheet sheet2 = wb.createSheet("Target Sheet");
        sheet2.createRow(0).createCell(0).setCellValue("Target Cell");

        //cell A1 has a link to 'Target Sheet-1'!A1
        cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("Worksheet Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
        link.setTextMark("'Target Sheet'!A1");
        cell.setHyperlink(link);

        //cell B1 has a link to cell A1 on the same sheet
        cell = sheet.createRow(1).createCell(0);
        cell.setCellValue("Worksheet Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
        link.setAddress("'Hyperlinks'!A1");
        cell.setHyperlink(link);

        wb = getTestDataProvider().writeOutAndReadBack(wb);
        sheet = wb.getSheet("Hyperlinks");

        cell = sheet.getRow(0).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("'Target Sheet'!A1", link.getTextMark());
        assertEquals("'Target Sheet'!A1", link.getAddress());

        cell = sheet.getRow(1).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("'Hyperlinks'!A1", link.getTextMark());
        assertEquals("'Hyperlinks'!A1", link.getAddress());
    }

    public void testCloneSheet() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("HyperlinksOnManySheets.xls");

        HSSFCell cell;
        HSSFHyperlink link;

        HSSFSheet sheet = wb.cloneSheet(0);

        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("http://poi.apache.org/", link.getAddress());

        cell = sheet.getRow(8).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("http://poi.apache.org/hssf/", link.getAddress());
    }

    public void testCreate() {
        HSSFWorkbook wb = getTestDataProvider().createWorkbook();

        HSSFHyperlink link;
        HSSFCell cell;
        HSSFSheet sheet = wb.createSheet("Hyperlinks");

        cell = sheet.createRow(1).createCell(0);
        cell.setCellValue("File Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_FILE);
        link.setAddress("testfolder\\test.PDF");
        cell.setHyperlink(link);

        wb = getTestDataProvider().writeOutAndReadBack(wb);
        sheet = wb.getSheet("Hyperlinks");

        cell = sheet.getRow(1).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("testfolder\\test.PDF", link.getAddress());
    }

    /**
     * Test that HSSFSheet#shiftRows moves hyperlinks,
     * see bugs #46445 and #29957
     */
    public void testShiftRows(){
        HSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("46445.xls");


        HSSFSheet sheet = wb.getSheetAt(0);

        //verify existing hyperlink in A3
        HSSFCell cell1 = sheet.getRow(2).getCell(0);
        HSSFHyperlink link1 = cell1.getHyperlink();
        assertNotNull(link1);
        assertEquals(2, link1.getFirstRow());
        assertEquals(2, link1.getLastRow());

        //assign a hyperlink to A4
        HSSFHyperlink link2 = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
        link2.setAddress("Sheet2!A2");
        HSSFCell cell2 = sheet.getRow(3).getCell(0);
        cell2.setHyperlink(link2);
        assertEquals(3, link2.getFirstRow());
        assertEquals(3, link2.getLastRow());

        //move the 3rd row two rows down
        sheet.shiftRows(sheet.getFirstRowNum(), sheet.getLastRowNum(), 2);

        //cells A3 and A4 don't contain hyperlinks anymore
        assertNull(sheet.getRow(2).getCell(0).getHyperlink());
        assertNull(sheet.getRow(3).getCell(0).getHyperlink());

        //the first hypelink now belongs to A5
        HSSFHyperlink link1_shifted = sheet.getRow(2+2).getCell(0).getHyperlink();
        assertNotNull(link1_shifted);
        assertEquals(4, link1_shifted.getFirstRow());
        assertEquals(4, link1_shifted.getLastRow());

        //the second hypelink now belongs to A6
        HSSFHyperlink link2_shifted = sheet.getRow(3+2).getCell(0).getHyperlink();
        assertNotNull(link2_shifted);
        assertEquals(5, link2_shifted.getFirstRow());
        assertEquals(5, link2_shifted.getLastRow());
    }
}
