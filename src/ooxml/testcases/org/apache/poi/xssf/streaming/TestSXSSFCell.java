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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.poi.ss.usermodel.BaseTestCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 *
 */
public class TestSXSSFCell extends BaseTestCell {

    public TestSXSSFCell() {
        super(SXSSFITestDataProvider.instance);
    }


    @Override
    public void tearDown(){
        SXSSFITestDataProvider.instance.cleanup();
    }

    /**
     * this test involves evaluation of formulas which isn't supported for SXSSF
     */
    @Override
    public void testConvertStringFormulaCell() {
        try {
            super.testConvertStringFormulaCell();
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals(
                    "Unexpected type of cell: class org.apache.poi.xssf.streaming.SXSSFCell. " +
                    "Only XSSFCells can be evaluated.", e.getMessage());
        }
    }

    /**
     * this test involves evaluation of formulas which isn't supported for SXSSF
     */
    @Override
    public void testSetTypeStringOnFormulaCell() {
        try {
            super.testSetTypeStringOnFormulaCell();
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals(
                    "Unexpected type of cell: class org.apache.poi.xssf.streaming.SXSSFCell. " +
                    "Only XSSFCells can be evaluated.", e.getMessage());
        }
    }

    public void testXmlEncoding(){
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell cell = row.createCell(0);
        String sval = "\u0000\u0002\u0012<>\t\n\u00a0 &\"POI\'\u2122";
        cell.setCellValue(sval);

        wb = _testDataProvider.writeOutAndReadBack(wb);

        // invalid characters are replaced with question marks
        assertEquals("???<>\t\n\u00a0 &\"POI\'\u2122", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

    }

    public void testEncodingbeloAscii(){
        Workbook xwb = new XSSFWorkbook();
        Cell xCell = xwb.createSheet().createRow(0).createCell(0);

        Workbook swb = SXSSFITestDataProvider.instance.createWorkbook();
        Cell sCell = swb.createSheet().createRow(0).createCell(0);

        StringBuffer sb = new StringBuffer();
        // test all possible characters
        for(int i = 0; i < Character.MAX_VALUE; i++) sb.append((char)i) ;

        String str = sb.toString();

        xCell.setCellValue(str);
        assertEquals(str, xCell.getStringCellValue());
        sCell.setCellValue(str);
        assertEquals(str, sCell.getStringCellValue());

        xwb = XSSFITestDataProvider.instance.writeOutAndReadBack(xwb);
        swb = SXSSFITestDataProvider.instance.writeOutAndReadBack(swb);
        xCell = xwb.getSheetAt(0).createRow(0).createCell(0);
        sCell = swb.getSheetAt(0).createRow(0).createCell(0);

        assertEquals(xCell.getStringCellValue(), sCell.getStringCellValue());

    }

    public void testPreserveSpaces() throws IOException {
        String[] samplesWithSpaces = {
                " POI",
                "POI ",
                " POI ",
                "\nPOI",
                "\n\nPOI \n",
        };
        for(String str : samplesWithSpaces){
            Workbook swb = new SXSSFWorkbook();
            Cell sCell = swb.createSheet().createRow(0).createCell(0);
            sCell.setCellValue(str);
            assertEquals(sCell.getStringCellValue(), str);

            // read back as XSSF and check that xml:spaces="preserve" is set
            XSSFWorkbook xwb = (XSSFWorkbook)SXSSFITestDataProvider.instance.writeOutAndReadBack(swb);
            XSSFCell xCell = xwb.getSheetAt(0).getRow(0).getCell(0);

            CTRst is = xCell.getCTCell().getIs();
            XmlCursor c = is.newCursor();
            c.toNextToken();
            String t = c.getAttributeText(new QName("http://www.w3.org/XML/1998/namespace", "space"));
            c.dispose();
            assertEquals("expected xml:spaces=\"preserve\" \"" + str + "\"", "preserve", t);
        }
    }

    public void testBug55658SetNumericValue(){
        Workbook wb = new SXSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row = sh.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("some");

        cell = row.createCell(1);
        cell.setCellValue(Integer.valueOf(23));
        
        cell.setCellValue("24");

        wb = _testDataProvider.writeOutAndReadBack(wb);

        assertEquals("some", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals("24", wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());
    }
}
