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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

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
        assertContains("This is a string", xsxml);
        assertContains("<td ref=\"B2\">13</td>", xsxml);
        assertContains("<td ref=\"B3\">13.12112313</td>", xsxml);
        assertContains("<td ref=\"B4\">$   3.03</td>", xsxml);
        assertContains("<td ref=\"B5\">20%</td>", xsxml);
        assertContains("<td ref=\"B6\">13.12</td>", xsxml);
        assertContains("<td ref=\"B7\">1.23457E+14</td>", xsxml);
        assertContains("<td ref=\"B8\">1.23457E+15</td>", xsxml);

        assertContains("46/1963", xsxml);//custom format 1
        assertContains("3/128", xsxml);//custom format 2

        assertContains("<tr num=\"7>\n" +
                "\t<td ref=\"A8\">longer int</td>\n" +
                "\t<td ref=\"B8\">1.23457E+15</td>\n" +
                "\t<td ref=\"C8\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "test comment2</span></td>\n" +
                "</tr num=\"7>", xsxml);

        assertContains("<tr num=\"34>\n" +
                "\t<td ref=\"B35\">comment6<span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment6 actually in cell</span></td>\n" +
                "</tr num=\"34>", xsxml);

        assertContains("<tr num=\"64>\n" +
                "\t<td ref=\"I65\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment7 end of file</span></td>\n" +
                "</tr num=\"64>", xsxml);

        assertContains("<tr num=\"65>\n" +
                "\t<td ref=\"I66\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                "comment8 end of file</span></td>\n" +
                "</tr num=\"65>", xsxml);

        assertContains("<header tagName=\"header\">OddLeftHeader OddCenterHeader OddRightHeader</header>", xsxml);
        assertContains("<footer tagName=\"footer\">OddLeftFooter OddCenterFooter OddRightFooter</footer>", xsxml);
        assertContains(
                "<header tagName=\"evenHeader\">EvenLeftHeader EvenCenterHeader EvenRightHeader\n</header>",
                xsxml);
        assertContains(
                "<footer tagName=\"evenFooter\">EvenLeftFooter EvenCenterFooter EvenRightFooter</footer>",
                xsxml);
        assertContains(
                "<header tagName=\"firstHeader\">FirstPageLeftHeader FirstPageCenterHeader FirstPageRightHeader</header>",
                xsxml);
        assertContains(
                "<footer tagName=\"firstFooter\">FirstPageLeftFooter FirstPageCenterFooter FirstPageRightFooter</footer>",
                xsxml);

    }

    @Test
    public void testComments() throws Exception {
        List<String> sheetTexts = getSheets("comments.xlsb");
        String xsxml = sheetTexts.get(0);
        assertContains(
                "<tr num=\"0>\n" +
                        "\t<td ref=\"A1\"><span type=\"comment\" author=\"Sven Nissel\">comment top row1 (index0)</span></td>\n" +
                        "\t<td ref=\"B1\">row1</td>\n" +
                        "</tr num=\"0>",  xsxml);
        assertContains(
                "<tr num=\"1>\n" +
                        "\t<td ref=\"A2\"><span type=\"comment\" author=\"Allison, Timothy B.\">Allison, Timothy B.:\n" +
                        "comment row2 (index1)</span></td>\n" +
                        "</tr num=\"1>",
                xsxml);
        assertContains("<tr num=\"2>\n" +
                "\t<td ref=\"A3\">row3<span type=\"comment\" author=\"Sven Nissel\">comment top row3 (index2)</span></td>\n" +
                "\t<td ref=\"B3\">row3</td>\n", xsxml);

        assertContains("<tr num=\"3>\n" +
                "\t<td ref=\"A4\"><span type=\"comment\" author=\"Sven Nissel\">comment top row4 (index3)</span></td>\n" +
                "\t<td ref=\"B4\">row4</td>\n" +
                "</tr num=\"3></sheet>", xsxml);

    }

    private List<String> getSheets(String testFileName) throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream(testFileName));
        List<String> sheetTexts = new ArrayList<String>();
        XSSFBReader r = new XSSFBReader(pkg);

//        assertNotNull(r.getWorkbookData());
        //      assertNotNull(r.getSharedStringsData());
        assertNotNull(r.getXSSFBStylesTable());
        XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(pkg);
        XSSFBStylesTable xssfbStylesTable = r.getXSSFBStylesTable();
        XSSFBReader.SheetIterator it = (XSSFBReader.SheetIterator)r.getSheetsData();

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

    //This converts all [\r\n\t]+ to " "
    private void assertContains(String needle, String haystack) {
        needle = needle.replaceAll("[\r\n\t]+", " ");
        haystack = haystack.replaceAll("[\r\n\t]+", " ");
        if (haystack.indexOf(needle) < 0) {
            fail("couldn't find >"+needle+"< in: "+haystack );
        }
    }


    @Test
    public void testDate() throws Exception {
        List<String> sheets = getSheets("date.xlsb");
        assertEquals(1, sheets.size());
        assertContains("1/12/13", sheets.get(0));

    }


    private class TestSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final StringBuilder sb = new StringBuilder();

        public void startSheet(String sheetName) {
            sb.append("<sheet name=\"").append(sheetName).append(">");
        }

        public void endSheet(){
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
                sb.append("<header tagName=\""+tagName+"\">"+text+"</header>");
            } else {
                sb.append("<footer tagName=\""+tagName+"\">"+text+"</footer>");

            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
