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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

            assertNotNull(r.getSharedStringsTable());
            assertNotNull(r.getStylesTable());
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

            assertEquals(11, r.getSharedStringsTable().getSharedStringItems().size());
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
            XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

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
                  CommentsTable ct = it.getSheetComments();
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
            it.next();
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
    *  the openpyxl library
    */
   @Test
   void test58747() throws Exception {
       try (OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("58747.xlsx")) {
           ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
           assertNotNull(strings);
           XSSFReader reader = new XSSFReader(pkg);
           StylesTable styles = reader.getStylesTable();
           assertNotNull(styles);

           XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
           assertTrue(iter.hasNext());
           iter.next();

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

        return Base64.encodeBase64String(md.digest());
    }
}
