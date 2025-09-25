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
import static org.apache.poi.POITestCase.assertNotContained;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Tests for {@link XSSFReader}
 */
public final class TestXSSFReader {

    private static final POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    void testGetBits() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"))) {

            XSSFReader r = new XSSFReader(pkg);

            assertNotNull(r.getWorkbookData());
            assertNotNull(r.getSharedStringsData());
            assertNotNull(r.getStylesData());
            assertNotNull(r.getStylesTable());

            SharedStrings sst1 = r.getSharedStringsTable();
            assertNotNull(sst1);
            assertTrue(sst1 instanceof SharedStringsTable, "instanceof SharedStringsTable");

            assertFalse(r.useReadOnlySharedStringsTable(), "useReadOnlySharedStringsTable defaults to false");
            r.setUseReadOnlySharedStringsTable(true);
            assertTrue(r.useReadOnlySharedStringsTable(), "useReadOnlySharedStringsTable changed to true");
            SharedStrings sst2 = r.getSharedStringsTable();
            assertNotNull(sst2);
            assertTrue(sst2 instanceof ReadOnlySharedStringsTable, "instanceof ReadOnlySharedStringsTable");
        }
    }

    @Test
    void testStyles() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"))) {

            XSSFReader r = new XSSFReader(pkg);

            assertEquals(3, r.getStylesTable().getFonts().size());
            assertEquals(0, r.getStylesTable().getNumDataFormats());

            // The Styles Table should have the themes associated with it too
            assertNotNull(r.getStylesTable().getTheme());

            // Check we get valid data for the two
            assertNotNull(r.getStylesData());
            assertNotNull(r.getThemesData());
        }
    }

    @Test
    void testStrings() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"))) {

            XSSFReader r = new XSSFReader(pkg);

            assertEquals(11, r.getSharedStringsTable().getCount());
            assertEquals("Test spreadsheet", r.getSharedStringsTable().getItemAt(0).toString());
        }
    }

    @Test
    void testSheets() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"))) {

            XSSFReader r = new XSSFReader(pkg);
            byte[] data = new byte[4096];

            // By r:id
            assertNotNull(r.getSheet("rId2"));
            int read = IOUtils.readFully(r.getSheet("rId2"), data);
            assertEquals(974, read);

            // All
            Iterator<InputStream> it = r.getSheetsData();

            int count = 0;
            while (it.hasNext()) {
                count++;
                InputStream inp = it.next();
                assertNotNull(inp);
                read = IOUtils.readFully(inp, data);
                inp.close();

                assertTrue(read > 400);
                assertTrue(read < 1500);
            }
            assertEquals(3, count);
        }
    }

    /**
     * Check that the sheet iterator returns sheets in the logical order
     * (as they are defined in the workbook.xml)
     */
    @Test
    void testOrderOfSheets() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("reordered_sheets.xlsx"))) {

            XSSFReader r = new XSSFReader(pkg);

            String[] sheetNames = {"Sheet4", "Sheet2", "Sheet3", "Sheet1"};
            XSSFReader.SheetIterator it = r.getSheetIterator();

            int count = 0;
            while (it.hasNext()) {
                InputStream inp = it.next();
                assertNotNull(inp);
                inp.close();

                assertEquals(sheetNames[count], it.getSheetName());
                count++;
            }
            assertEquals(4, count);
        }
    }

    @Test
    void testComments() throws Exception {
      try (OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("comments.xlsx")) {
          XSSFReader r = new XSSFReader(pkg);
          XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

          int count = 0;
          while (it.hasNext()) {
              count++;
              InputStream inp = it.next();
              inp.close();

              if (count == 1) {
                  assertNotNull(it.getSheetComments());
                  Comments ct = it.getSheetComments();
                  assertEquals(1, ct.getNumberOfAuthors());
                  assertEquals(3, ct.getNumberOfComments());
              } else {
                  assertNull(it.getSheetComments());
              }
          }
          assertEquals(3, count);
      }
    }

   /**
    * Iterating over a workbook with chart sheets in it, using the
    *  XSSFReader method
    */
   @Test
   void test50119() throws Exception {
      try (OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("WithChartSheet.xlsx")) {
          XSSFReader r = new XSSFReader(pkg);
          assertEquals("bxdf4aa1n9VLkn/4++RNhoygSelxWDM2Can1m9TLlTw=", hash(r));
      }
   }

    /**
     * Test text extraction from text box using getShapes()
     */
    @Test
    void testShapes() throws Exception {
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("WithTextBox.xlsx")) {
            XSSFReader r = new XSSFReader(pkg);
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

            String text = getShapesString(it);
            assertContains(text, "Line 1");
            assertContains(text, "Line 2");
            assertContains(text, "Line 3");
        }
    }

    private String getShapesString(XSSFReader.SheetIterator it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            assertNotNull(it.next());
            List<XSSFShape> shapes = it.getShapes();
            if (shapes != null) {
                for (XSSFShape shape : shapes) {
                    if (shape instanceof XSSFSimpleShape) {
                        String t = ((XSSFSimpleShape) shape).getText();
                        sb.append(t).append('\n');
                    }
                }
            }
        }
        return sb.toString();
    }

    @Test
    void testBug57914() throws Exception {
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("57914.xlsx")) {
            // for now expect this to fail, when we fix 57699, this one should fail so we know we should adjust
            // this test as well
            POIXMLException e = assertThrows(POIXMLException.class, () -> {
                final XSSFReader r = new XSSFReader(pkg);

                XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

                String text = getShapesString(it);
                assertContains(text, "Line 1");
                assertContains(text, "Line 2");
                assertContains(text, "Line 3");
            });
            assertContains(e.getMessage(), "57699");
        }
    }

    /**
     * NPE from XSSFReader$SheetIterator.<init> on XLSX files generated by
     * the openpyxl library
     */
    @Test
    void test58747() throws Exception {
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("58747.xlsx")) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            assertNotNull(strings);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            assertNotNull(styles);

            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            assertTrue(iter.hasNext());
            assertNotNull(iter.next());

            assertFalse(iter.hasNext());
            assertEquals("Orders", iter.getSheetName());
        }
    }

    /**
     * NPE when sheet has no relationship id in the workbook
     * 60825
     */
    @Test
    void testSheetWithNoRelationshipId() throws Exception {
        try (OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("60825.xlsx")) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            assertNotNull(strings);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            assertNotNull(styles);

            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            assertNotNull(iter.next());
            assertFalse(iter.hasNext());
        }
    }

    /**
     * bug 61304: Call to XSSFReader.getSheetsData() returns duplicate sheets.
     *
     * The problem seems to be caused only by those xlsx files which have a specific
     * order of the attributes inside the &lt;sheet&gt; tag of workbook.xml
     *
     * Example (which causes the problems):
     * &lt;sheet name="Sheet6" r:id="rId6" sheetId="4"/&gt;
     *
     * While this one works correctly:
     * &lt;sheet name="Sheet6" sheetId="4" r:id="rId6"/&gt;
     */
    @Test
    void test61034() throws Exception {
        try (OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("61034.xlsx")) {
            XSSFReader reader = new XSSFReader(pkg);
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
            Set<String> seen = new HashSet<>();
            while (iter.hasNext()) {
                InputStream stream = iter.next();
                String sheetName = iter.getSheetName();
                assertNotContained(seen, sheetName);
                seen.add(sheetName);
                stream.close();
            }
        }
    }

    @Disabled("until we fix issue https://bz.apache.org/bugzilla/show_bug.cgi?id=61701")
    @Test
    void test61701() throws Exception {
        try(Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("simple-table-named-range.xlsx")) {
            Name name = workbook.getName("total");
            System.out.println("workbook.getName(\"total\").getSheetName() returned: " + name.getSheetName());
        }
    }

    @Test
    void test64420() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("64420.xlsm"))) {
            XSSFReader reader = new XSSFReader(pkg);
            assertEquals("U/j5UN7LN8wH6Gw/gsn6pCMASz+Nb1euCsFtC8tAPm0=", hash(reader));
        }
    }

    @Test
    void test66612() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("xlmmacro.xlsm"))) {
            XSSFReader reader = new XSSFReader(pkg);
            assertEquals("OCHSA0XBnVkZrG5OECromBJH43QoLIEZLit3eDix+rs=", hash(reader));
        }
    }

    @Test
    void testStrictOoxmlNotAllowed() {
        assertThrows(POIXMLException.class, () -> {
            try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("sample.strict.xlsx"))) {
                XSSFReader reader = new XSSFReader(pkg);
                assertNotNull(reader);
            }
        });
        assertThrows(POIXMLException.class, () -> {
            try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("sample.strict.xlsx"))) {
                XSSFReader reader = new XSSFReader(pkg, false);
                assertNotNull(reader);
            }
        });
    }

    @Test
    void testStrictOoxmlAllowed() throws Exception {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("sample.strict.xlsx"))) {
            XSSFReader reader = new XSSFReader(pkg, true);
            assertNotNull(reader.pkg);
        }
    }

    @Test
    void testBug65676() throws Exception {
        try (UnsynchronizedByteArrayOutputStream output = UnsynchronizedByteArrayOutputStream.builder().get()) {
            try(Workbook wb = new SXSSFWorkbook()) {
                Row r = wb.createSheet("Sheet").createRow(0);
                r.createCell(0).setCellValue(1.2); /* A1: Number 1.2 */
                r.createCell(1).setCellValue("ABC"); /* B1: Inline string "ABC" */
                wb.write(output);
            }
            /* Minimal stream reader processor */
            XSSFSheetXMLHandler.SheetContentsHandler reader = new XSSFSheetXMLHandler.SheetContentsHandler() {
                @Override public void startRow(int rowNum) {}
                @Override public void endRow(int rowNum) {}
                @Override public void cell(String cellReference,
                                           String formattedValue, XSSFComment comment) {
                    if (cellReference.equals("A1")) {
                        assertEquals("1.2", formattedValue);
                    } else if (cellReference.equals("B1")) {
                        assertEquals("ABC", formattedValue);
                    } else {
                        fail("Unexpected cell " + cellReference);
                    }
                }
            };

            /* Stream reading workbook from byte array */
            try (OPCPackage xlsxPackage = OPCPackage.open(output.toInputStream())) {
                XSSFReader xssfReader = new XSSFReader(xlsxPackage);
                try (InputStream stream = xssfReader.getSheetsData().next()) {
                    XMLReader sheetParser = XMLHelper.newXMLReader();
                    sheetParser.setContentHandler(new XSSFSheetXMLHandler(
                            xssfReader.getStylesTable(),
                            null,
                            new ReadOnlySharedStringsTable(xlsxPackage),
                            reader,
                            new DataFormatter(),
                            false
                    ));
                    sheetParser.parse(new InputSource(stream));
                }
            }
        }

    }

    private static String hash(XSSFReader reader) throws IOException {
        Iterable<InputStream> iter = () -> {
            try {
                return reader.getSheetsData();
            } catch (IOException | InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        };

        MessageDigest md = CryptoFunctions.getMessageDigest(HashAlgorithm.sha256);
        for (InputStream is : iter) {
            md.update(IOUtils.toByteArray(is));
        }

        return Base64.getEncoder().encodeToString(md.digest());
    }

    @Test
    public void testMissingLiteFile() throws IOException, OpenXML4JException {
        try (OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream(
                "clusterfuzz-testcase-minimized-XLSX2CSVFuzzer-6594557414080512.xlsx"))) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            assertThrows(IOException.class,
                    xssfReader::getStylesTable,
                    "The file is broken, but triggers loading of some additional resources which would "
                            + "be missing in the ooxml-lite package otherwise.");
        }
    }
}
