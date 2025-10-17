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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIDataSamples;
import static org.apache.poi.POITestCase.assertContains;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import org.mockito.quality.Strictness;

class TestXSSFBReader {

    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    void testBasic() throws Exception {
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
    void testComments() throws Exception {
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
    void testAbsPath() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("testVarious.xlsb"))) {
            XSSFBReader r = new XSSFBReader(pkg);
            assertEquals("C:\\Users\\tallison\\Desktop\\working\\xlsb\\", r.getAbsPathMetadata());
        }
    }

    private List<String> getSheets(String testFileName) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream(testFileName))) {
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
    }

    @Test
    void testDate() throws Exception {
        List<String> sheets = getSheets("date.xlsb");
        assertEquals(1, sheets.size());
        assertContains(sheets.get(0), "1/12/13");
    }


    private static class TestSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final StringBuilder sb = new StringBuilder();

        void startSheet(String sheetName) {
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

    private static XSSFBSheetHandler.XSSFBSheetContentsHandler mockSheetContentsHandler() {
        return mock(
                XSSFBSheetHandler.XSSFBSheetContentsHandler.class,
                withSettings().strictness(Strictness.STRICT_STUBS));
    }

    private static ArgumentMatcher<XSSFComment> commentWith(String author, String text) {
        return comment -> comment != null
                && author.equals(comment.getAuthor())
                && comment.getString() != null
                && text.equals(comment.getString().toString().trim());
    }

    void testXSSFBSheetContentsHandler(String fileName,
            XSSFBSheetHandler.XSSFBSheetContentsHandler handler) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream(fileName))) {
            XSSFBReader r = new XSSFBReader(pkg);
            assertNotNull(r.getXSSFBStylesTable());
            XSSFBSharedStringsTable sst = new XSSFBSharedStringsTable(pkg);
            XSSFBStylesTable xssfbStylesTable = r.getXSSFBStylesTable();
            XSSFBReader.SheetIterator it = (XSSFBReader.SheetIterator) r.getSheetsData();

            while (it.hasNext()) {
                InputStream is = it.next();
                XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(is,
                        xssfbStylesTable,
                        it.getXSSFBSheetComments(),
                        sst,
                        handler,
                        false);
                sheetHandler.parse();
            }
        }
    }

    @Test
    void testBasicXSSFBSheetContentsHandler() throws Exception {
        XSSFBSheetHandler.XSSFBSheetContentsHandler handler = mockSheetContentsHandler();
        testXSSFBSheetContentsHandler("testVarious.xlsb", handler);
  
        InOrder ordered = inOrder(handler);
        ordered.verify(handler).startRow(0);
        ordered.verify(handler).stringCell(eq("A1"), eq("String"), isNull());
        ordered.verify(handler).stringCell(eq("B1"), eq("This is a string"), isNull());
        ordered.verify(handler).endRow(0);

        ordered.verify(handler).startRow(1);
        ordered.verify(handler).stringCell(eq("A2"), eq("integer"), isNull());
        ordered.verify(handler).doubleCell(eq("B2"), eq(13.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(1);

        ordered.verify(handler).startRow(2);
        ordered.verify(handler).stringCell(eq("A3"), eq("float"), isNull());
        ordered.verify(handler).doubleCell(eq("B3"), eq(13.1211231321d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(2);

        ordered.verify(handler).startRow(3);
        ordered.verify(handler).stringCell(eq("A4"), eq("currency"), isNull());
        ordered.verify(handler).doubleCell(eq("B4"), eq(3.03d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(3);

        ordered.verify(handler).startRow(4);
        ordered.verify(handler).stringCell(eq("A5"), eq("percent"), isNull());
        ordered.verify(handler).doubleCell(eq("B5"), eq(0.2d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(4);

        ordered.verify(handler).startRow(5);
        ordered.verify(handler).stringCell(eq("A6"), eq("float 2"), isNull());
        ordered.verify(handler).doubleCell(eq("B6"), eq(13.12131231d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(5);

        ordered.verify(handler).startRow(6);
        ordered.verify(handler).stringCell(eq("A7"), eq("long int"), isNull());
        ordered.verify(handler).doubleCell(eq("B7"), eq(1.23456789012345E14d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(6);

        ordered.verify(handler).startRow(7);
        ordered.verify(handler).stringCell(eq("A8"), eq("longer int"), isNull());
        ordered.verify(handler).doubleCell(eq("B8"), eq(1.23456789012345E15d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).stringCell(eq("C8"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(7);

        ordered.verify(handler).startRow(8);
        ordered.verify(handler).stringCell(eq("A9"), eq("fraction"), isNull());
        ordered.verify(handler).doubleCell(eq("B9"), eq(0.25d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(8);

        ordered.verify(handler).startRow(9);
        ordered.verify(handler).stringCell(eq("A10"), eq("date"), isNull());
        ordered.verify(handler).doubleCell(eq("B10"), eq(42803.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(9);

        ordered.verify(handler).startRow(10);
        ordered.verify(handler).stringCell(eq("A11"), eq("comment"), isNull());
        ordered.verify(handler).stringCell(eq("B11"), eq("contents"), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(10);

        ordered.verify(handler).startRow(11);
        ordered.verify(handler).stringCell(eq("A12"), eq("hyperlink"), isNull());
        ordered.verify(handler).stringCell(eq("B12"), eq("tika_link"), isNull());
        ordered.verify(handler).endRow(11);

        ordered.verify(handler).startRow(12);
        ordered.verify(handler).stringCell(eq("A13"), eq("formula"), isNull());
        ordered.verify(handler).doubleCell(eq("B13"), eq(4.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).doubleCell(eq("C13"), eq(2.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(12);

        ordered.verify(handler).startRow(13);
        ordered.verify(handler).stringCell(eq("A14"), eq("formulaErr"), isNull());
        ordered.verify(handler).errorCell(eq("B14"), eq(FormulaError.NAME), isNull());
        ordered.verify(handler).endRow(13);

        ordered.verify(handler).startRow(14);
        ordered.verify(handler).stringCell(eq("A15"), eq("formulaFloat"), isNull());
        ordered.verify(handler).doubleCell(eq("B15"), eq(0.5d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).stringCell(eq("D15"), eq("March"), isNull());
        ordered.verify(handler).stringCell(eq("E15"), eq("April"), isNull());
        ordered.verify(handler).endRow(14);

        ordered.verify(handler).startRow(15);
        ordered.verify(handler).stringCell(eq("A16"), eq("customFormat1"), isNull());
        ordered.verify(handler).stringCell(eq("B16"), eq("   46/1963"), isNull());
        ordered.verify(handler).stringCell(eq("C16"), eq("merchant1"), isNull());
        ordered.verify(handler).doubleCell(eq("D16"), eq(1.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).doubleCell(eq("E16"), eq(3.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(15);

        ordered.verify(handler).startRow(16);
        ordered.verify(handler).stringCell(eq("A17"), eq("customFormat2"), isNull());
        ordered.verify(handler).stringCell(eq("B17"), eq("  3/128"), isNull());
        ordered.verify(handler).stringCell(eq("C17"), eq("merchant2"), isNull());
        ordered.verify(handler).doubleCell(eq("D17"), eq(2.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).doubleCell(eq("E17"), eq(4.0d), isNull(), any(ExcelNumberFormat.class));
        ordered.verify(handler).endRow(16);

        ordered.verify(handler).startRow(20);
        ordered.verify(handler).stringCell(eq("C21"), eq("text test"), isNull());
        ordered.verify(handler).endRow(20);

        ordered.verify(handler).startRow(22);
        ordered.verify(handler).stringCell(eq("A23"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(22);

        ordered.verify(handler).startRow(23);
        ordered.verify(handler).stringCell(eq("C24"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(23);

        ordered.verify(handler).startRow(27);
        ordered.verify(handler).stringCell(eq("B28"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(27);

        ordered.verify(handler).startRow(29);
        ordered.verify(handler).stringCell(eq("B30"), eq("the"), isNull());
        ordered.verify(handler).stringCell(eq("C30"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(29);

        ordered.verify(handler).startRow(32);
        ordered.verify(handler).stringCell(eq("B33"), eq("the"), isNull());
        ordered.verify(handler).stringCell(eq("C33"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).stringCell(eq("D33"), eq("quick"), isNull());
        ordered.verify(handler).endRow(32);

        ordered.verify(handler).startRow(34);
        ordered.verify(handler).stringCell(eq("B35"), eq("comment6"), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(34);

        ordered.verify(handler).startRow(64);
        ordered.verify(handler).stringCell(eq("I65"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(64);

        ordered.verify(handler).startRow(65);
        ordered.verify(handler).stringCell(eq("I66"), isNull(), notNull(XSSFComment.class));
        ordered.verify(handler).endRow(65);

        ordered.verify(handler).headerFooter(eq("OddLeftHeader OddCenterHeader OddRightHeader"), eq(true), eq("header"));
        ordered.verify(handler).headerFooter(eq("OddLeftFooter OddCenterFooter OddRightFooter"), eq(false), eq("footer"));
        ordered.verify(handler).headerFooter(eq("EvenLeftHeader EvenCenterHeader EvenRightHeader\n"), eq(true), eq("evenHeader"));
        ordered.verify(handler).headerFooter(eq("EvenLeftFooter EvenCenterFooter EvenRightFooter"), eq(false), eq("evenFooter"));
        ordered.verify(handler).headerFooter(eq("FirstPageLeftHeader FirstPageCenterHeader FirstPageRightHeader"), eq(true), eq("firstHeader"));
        ordered.verify(handler).headerFooter(eq("FirstPageLeftFooter FirstPageCenterFooter FirstPageRightFooter"), eq(false), eq("firstFooter"));
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testCommentsXSSFBSheetContentsHandler() throws Exception {
        XSSFBSheetHandler.XSSFBSheetContentsHandler handler = mockSheetContentsHandler();
        testXSSFBSheetContentsHandler("comments.xlsb", handler);
  
        InOrder ordered = inOrder(handler);
        ordered.verify(handler).startRow(0);
        ordered.verify(handler).stringCell(eq("A1"), isNull(),
                argThat(commentWith("Sven Nissel", "comment top row1 (index0)")));
        ordered.verify(handler).stringCell(eq("B1"), eq("row1"), isNull());
        ordered.verify(handler).endRow(0); 
        ordered.verify(handler).startRow(1);
        ordered.verify(handler).stringCell(eq("A2"), isNull(),
                argThat(commentWith("Allison, Timothy B.", "Allison, Timothy B.:\ncomment row2 (index1)")));
        ordered.verify(handler).endRow(1);
        ordered.verify(handler).startRow(2);
        ordered.verify(handler).stringCell(eq("A3"), eq("row3"),
                argThat(commentWith("Sven Nissel", "comment top row3 (index2)")));
        ordered.verify(handler).stringCell(eq("B3"), eq("row3"), isNull());
        ordered.verify(handler).endRow(2);
        ordered.verify(handler).startRow(3);
        ordered.verify(handler).stringCell(eq("A4"), isNull(),
                argThat(commentWith("Sven Nissel", "comment top row4 (index3)")));
        ordered.verify(handler).stringCell(eq("B4"), eq("row4"), isNull());
        ordered.verify(handler).endRow(3);
    }

    @Test
    void testDateXSSFBSheetContentsHandler() throws Exception {
        XSSFBSheetHandler.XSSFBSheetContentsHandler handler = mockSheetContentsHandler();
        testXSSFBSheetContentsHandler("date.xlsb", handler);
  
        InOrder ordered = inOrder(handler);
        ArgumentCaptor<ExcelNumberFormat> numberFormat = ArgumentCaptor.forClass(ExcelNumberFormat.class);
        ordered.verify(handler).startRow(0);
        ordered.verify(handler).doubleCell(eq("A1"), eq(41286.0d), isNull(), numberFormat.capture());
        ordered.verify(handler).endRow(0);
        ExcelNumberFormat format = numberFormat.getValue();
        assertNotNull(format);
        assertEquals(14, format.getIdx());
        assertEquals("m/d/yy", format.getFormat());
        ordered.verifyNoMoreInteractions();
        DataFormatter df = new DataFormatter();
        assertEquals("1/12/13", df.formatRawCellContents(41286.0d, format.getIdx(), format.getFormat()));
    }
}
