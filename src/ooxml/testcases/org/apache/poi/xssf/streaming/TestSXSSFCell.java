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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.poi.ss.usermodel.BaseTestXCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlCursor;
import org.junit.AfterClass;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

/**
 * Tests various functionality having to do with {@link SXSSFCell}.  For instance support for
 * particular datatypes, etc.
 */
public class TestSXSSFCell extends BaseTestXCell {

    public TestSXSSFCell() {
        super(SXSSFITestDataProvider.instance);
    }

    @AfterClass
    public static void tearDownClass(){
        SXSSFITestDataProvider.instance.cleanup();
    }

    @Test
    public void testPreserveSpaces() throws IOException {
        String[] samplesWithSpaces = {
                " POI",
                "POI ",
                " POI ",
                "\nPOI",
                "\n\nPOI \n",
        };
        for (String str : samplesWithSpaces) {
            Workbook swb = _testDataProvider.createWorkbook();
            Cell sCell = swb.createSheet().createRow(0).createCell(0);
            sCell.setCellValue(str);
            assertEquals(sCell.getStringCellValue(), str);

            // read back as XSSF and check that xml:spaces="preserve" is set
            XSSFWorkbook xwb = (XSSFWorkbook)_testDataProvider.writeOutAndReadBack(swb);
            XSSFCell xCell = xwb.getSheetAt(0).getRow(0).getCell(0);

            CTRst is = xCell.getCTCell().getIs();
            assertNotNull(is);
            XmlCursor c = is.newCursor();
            c.toNextToken();
            String t = c.getAttributeText(new QName("http://www.w3.org/XML/1998/namespace", "space"));
            c.dispose();
            assertEquals("expected xml:spaces=\"preserve\" \"" + str + "\"", "preserve", t);
            xwb.close();
            swb.close();
        }
    }

    @Test
    public void test62216() throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Cell instance = wb.createSheet().createRow(0).createCell(0);
            String formula = "2";
            instance.setCellFormula(formula);
            instance.setCellErrorValue(FormulaError.NAME.getCode());

            assertEquals(formula, instance.getCellFormula());
        }
    }
}
