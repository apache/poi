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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * Test diffrent types of Excel hyperlinks
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestHyperlink extends TestCase {

    protected abstract ITestDataProvider getTestDataProvider();

    public void testBasicTypes(){
        Workbook wb = getTestDataProvider().createWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();

        Cell cell;
        Hyperlink link;
        Sheet sheet = wb.createSheet("Hyperlinks");

        //URL
        cell = sheet.createRow(0).createCell((short) 0);
        cell.setCellValue("URL Link");
        link = createHelper.createHyperlink(Hyperlink.LINK_URL);
        link.setAddress("http://poi.apache.org/");
        cell.setHyperlink(link);

        //link to a file in the current directory
        cell = sheet.createRow(1).createCell((short) 0);
        cell.setCellValue("File Link");
        link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
        link.setAddress("hyperinks-beta4-dump.txt");
        cell.setHyperlink(link);

        //e-mail link
        cell = sheet.createRow(2).createCell((short) 0);
        cell.setCellValue("Email Link");
        link = createHelper.createHyperlink(Hyperlink.LINK_EMAIL);
        //note, if subject contains white spaces, make sure they are url-encoded
        link.setAddress("mailto:poi@apache.org?subject=Hyperlinks");
        cell.setHyperlink(link);

        //link to a place in this workbook

        //create a target sheet and cell
        Sheet sheet2 = wb.createSheet("Target Sheet");
        sheet2.createRow(0).createCell((short) 0).setCellValue("Target Cell");

        cell = sheet.createRow(3).createCell((short) 0);
        cell.setCellValue("Worksheet Link");
        link = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
        link.setAddress("'Target Sheet'!A1");
        cell.setHyperlink(link);

        wb = getTestDataProvider().writeOutAndReadBack(wb);

        sheet = wb.getSheetAt(0);
        link = sheet.getRow(0).getCell(0).getHyperlink();

        assertEquals("http://poi.apache.org/", link.getAddress());
        link = sheet.getRow(1).getCell(0).getHyperlink();
        assertEquals("hyperinks-beta4-dump.txt", link.getAddress());
        link = sheet.getRow(2).getCell(0).getHyperlink();
        assertEquals("mailto:poi@apache.org?subject=Hyperlinks", link.getAddress());
        link = sheet.getRow(3).getCell(0).getHyperlink();
        assertEquals("'Target Sheet'!A1", link.getAddress());
	}
}