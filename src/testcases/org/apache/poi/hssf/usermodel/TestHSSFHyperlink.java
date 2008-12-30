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

import junit.framework.TestCase;

import java.io.*;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Tests HSSFHyperlink.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFHyperlink extends TestCase {

    /**
     * Test that we can read hyperlinks.
     */
    public void testRead() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("HyperlinksOnManySheets.xls");

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
    }

    public void testModify() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("HyperlinksOnManySheets.xls");

        HSSFSheet sheet;
        HSSFCell cell;
        HSSFHyperlink link;

        sheet = wb.getSheet("WebLinks");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        //modify the link
        link.setAddress("www.apache.org");

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = wb.getSheet("WebLinks");
        cell = sheet.getRow(4).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("www.apache.org", link.getAddress());

    }

    public void testCreate() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFCell cell;
        HSSFSheet sheet = wb.createSheet("Hyperlinks");

        //URL
        cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("URL Link");
        HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
        link.setAddress("http://poi.apache.org/");
        cell.setHyperlink(link);

        //link to a file in the current directory
        cell = sheet.createRow(1).createCell(0);
        cell.setCellValue("File Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_FILE);
        link.setAddress("link1.xls");
        cell.setHyperlink(link);

        //e-mail link
        cell = sheet.createRow(2).createCell(0);
        cell.setCellValue("Email Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_EMAIL);
        //note, if subject contains white spaces, make sure they are url-encoded
        link.setAddress("mailto:poi@apache.org?subject=Hyperlinks");
        cell.setHyperlink(link);

        //link to a place in this workbook

        //create a target sheet and cell
        HSSFSheet sheet2 = wb.createSheet("Target Sheet");
        sheet2.createRow(0).createCell(0).setCellValue("Target Cell");

        cell = sheet.createRow(3).createCell(0);
        cell.setCellValue("Worksheet Link");
        link = new HSSFHyperlink(HSSFHyperlink.LINK_DOCUMENT);
        link.setTextMark("'Target Sheet'!A1");
        cell.setHyperlink(link);

        //serialize and read again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = wb.getSheet("Hyperlinks");
        cell = sheet.getRow(0).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("http://poi.apache.org/", link.getAddress());

        cell = sheet.getRow(1).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("link1.xls", link.getAddress());

        cell = sheet.getRow(2).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("mailto:poi@apache.org?subject=Hyperlinks", link.getAddress());

        cell = sheet.getRow(3).getCell(0);
        link = cell.getHyperlink();
        assertNotNull(link);
        assertEquals("'Target Sheet'!A1", link.getTextMark());
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

    /**
     * Test that HSSFSheet#shiftRows moves hyperlinks,
     * see bugs #46445 and #29957
     */
    public void testShiftRows(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("46445.xls");


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
