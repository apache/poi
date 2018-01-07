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

package org.apache.poi.xssf.eventusermodel;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.junit.Test;

public class TestXSSFBReader {

    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void testBasic() throws Exception {
        List<String> sheetTexts = getSheets("testVarious.xlsb");

        assertEquals(1, sheetTexts.size());
        String xsxml = sheetTexts.get(0);
        assertContains(xsxml, "This is a string");
        assertContains(xsxml, "<td ref=\"B2\">13</td>");
        assertContains(xsxml, "<td ref=\"B3\">13.12112313</td>");
        assertContains(xsxml, "<td ref=\"B4\">$   3.03</td>");
        assertContains(xsxml, "<td ref=\"B5\">20%</td>");
        assertContains(xsxml, "<td ref=\"B6\">13.12</td>");
        assertContains(xsxml, "<td ref=\"B7\">1.23457E+14</td>");
        assertContains(xsxml, "<td ref=\"B8\">1.23457E+15</td>");

        assertContains(xsxml, "46/1963");//custom format 1
        assertContains(xsxml, "3/128");//custom format 2

        assertContains(xsxml, "<tr num=\"7>\n" +
                "\t<td ref=\"A8\">longer int</td>\n" +
                "\t<td ref=\"B8\">1.23457E+15</td>\n" +
                "\t<td ref=\"C8\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "test comment2</span></td>\n" +
                "</tr num=\"7>");

        assertContains(xsxml, "<tr num=\"34>\n" +
                "\t<td ref=\"B35\">comment6<span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment6 actually in cell</span></td>\n" +
                "</tr num=\"34>");

        assertContains(xsxml, "<tr num=\"64>\n" +
                "\t<td ref=\"I65\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment7 end of file</span></td>\n" +
                "</tr num=\"64>");

        assertContains(xsxml, "<tr num=\"65>\n" +
                "\t<td ref=\"I66\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment8 end of file</span></td>\n" +
                "</tr num=\"65>");

        assertContains(xsxml,
                "<header tagName=\"header\">OddLeftHeader OddCenterHeader OddRightHeader</header>");
        assertContains(xsxml,
                "<footer tagName=\"footer\">OddLeftFooter OddCenterFooter OddRightFooter</footer>");
        assertContains(xsxml,
                "<header tagName=\"evenHeader\">EvenLeftHeader EvenCenterHeader EvenRightHeader\n</header>");
        assertContains(xsxml,
                "<footer tagName=\"evenFooter\">EvenLeftFooter EvenCenterFooter EvenRightFooter</footer>");
        assertContains(xsxml,
                "<header tagName=\"firstHeader\">FirstPageLeftHeader FirstPageCenterHeader FirstPageRightHeader</header>");
        assertContains(xsxml,
                "<footer tagName=\"firstFooter\">FirstPageLeftFooter FirstPageCenterFooter FirstPageRightFooter</footer>");

    }

    @Test
    public void testComments() throws Exception {
        List<String> sheetTexts = getSheets("comments.xlsb");
        String xsxml = sheetTexts.get(0);
        assertContains(xsxml,
                "<tr num=\"0>\n" +
                        "\t<td ref=\"A1\"><span type=\"comment\" author=\"Sven Nissel\">comment top row1 (index0)</span></td>\n" +
                        "\t<td ref=\"B1\">row1</td>\n" +
                        "</tr num=\"0>");
        assertContains(xsxml,
                "<tr num=\"1>\n" +
                        "\t<td ref=\"A2\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                        "comment row2 (index1)</span></td>\n" +
                        "</tr num=\"1>");
        assertContains(xsxml, "<tr num=\"2>\n" +
                "\t<td ref=\"A3\">row3<span type=\"comment\" author=\"Sven Nissel\">comment top row3 (index2)</span></td>\n" +
                "\t<td ref=\"B3\">row3</td>\n");

        assertContains(xsxml, "<tr num=\"3>\n" +
                "\t<td ref=\"A4\"><span type=\"comment\" author=\"Sven Nissel\">comment top row4 (index3)</span></td>\n" +
                "\t<td ref=\"B4\">row4</td>\n" +
                "</tr num=\"3></sheet>");

    }

    @Test
    public void testAbsPath() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("testVarious.xlsb"));
        XSSFBReader r = new XSSFBReader(pkg);
        assertEquals("C:\\Users\\tallison\\Desktop\\working\\xlsb\\", r.getAbsPathMetadata());
    }

    private List<String> getSheets(String testFileName) throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream(testFileName));
        List<String> sheetTexts = new ArrayList<>();
        XSSFBReader r = new XSSFBReader(pkg);

//        assertNotNull(r.getWorkbookData());
        //      assertNotNull(r.getSharedStringsData());
        assertNotNull(r.getXSSFBStylesTable());
        XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(pkg);
        XSSFBStylesTable xssfbStylesTable = r.getXSSFBStylesTable();
        XSSFBReader.SheetIterator it = (XSSFBReader.SheetIterator) r.getSheetsData();

        while (it.hasNext()) {
            InputStream is = it.next();
            String name = it.getSheetName();
            TestSheetHandler testSheetHandler = new TestSheetHandler();
            testSheetHandler.startSheet(name);
            XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(is,
                    xssfbStylesTable,
                    it.getXSSFBSheetComments(),
                    sst, testSheetHandler,
                    new DataFormatter(),
                    false);
            sheetHandler.parse();
            testSheetHandler.endSheet();
            sheetTexts.add(testSheetHandler.toString());
        }
        return sheetTexts;

    }

    @Test
    public void testDate() throws Exception {
        List<String> sheets = getSheets("date.xlsb");
        assertEquals(1, sheets.size());
        assertContains(sheets.get(0), "1/12/13");
    }


    private class TestSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final StringBuilder sb = new StringBuilder();

        public void startSheet(String sheetName) {
            sb.append("<sheet name=\"").append(sheetName).append(">");
        }

        public void endSheet() {
            sb.append("</sheet>");
        }

        @Override
        public void startRow(int rowNum) {
            sb.append("\n<tr num=\"").append(rowNum).append(">");
        }

        @Override
        public void endRow(int rowNum) {
            sb.append("\n</tr num=\"").append(rowNum).append(">");
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            formattedValue = (formattedValue == null) ? "" : formattedValue;
            if (comment == null) {
                sb.append("\n\t<td ref=\"").append(cellReference).append("\">").append(formattedValue).append("</td>");
            } else {
                sb.append("\n\t<td ref=\"").append(cellReference).append("\">")
                        .append(formattedValue)
                        .append("<span type=\"comment\" author=\"")
                        .append(comment.getAuthor()).append("\">")
                        .append(comment.getString().toString().trim()).append("</span>")
                        .append("</td>");
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            if (isHeader) {
                sb.append("<header tagName=\"").append(tagName).append("\">").append(text).append("</header>");
            } else {
                sb.append("<footer tagName=\"").append(tagName).append("\">").append(text).append("</footer>");

            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
