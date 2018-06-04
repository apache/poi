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
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link XSSFReader}
 */
public final class TestXSSFReader {

    private static POIDataSamples _ssTests = POIDataSamples.getSpreadSheetInstance();

    @Test
    public void testGetBits() throws Exception {
		OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));

		XSSFReader r = new XSSFReader(pkg);

        assertNotNull(r.getWorkbookData());
		assertNotNull(r.getSharedStringsData());
		assertNotNull(r.getStylesData());

		assertNotNull(r.getSharedStringsTable());
		assertNotNull(r.getStylesTable());
	}

    @Test
	public void testStyles() throws Exception {
		OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));

		XSSFReader r = new XSSFReader(pkg);

		assertEquals(3, r.getStylesTable().getFonts().size());
		assertEquals(0, r.getStylesTable().getNumDataFormats());
		
		// The Styles Table should have the themes associated with it too
		assertNotNull(r.getStylesTable().getTheme());
		
		// Check we get valid data for the two
		assertNotNull(r.getStylesData());
      assertNotNull(r.getThemesData());
	}

    @Test
	public void testStrings() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));

		XSSFReader r = new XSSFReader(pkg);

		assertEquals(11, r.getSharedStringsTable().getItems().size());
		assertEquals("Test spreadsheet", new XSSFRichTextString(r.getSharedStringsTable().getEntryAt(0)).toString());
	}

    @Test
	public void testSheets() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("SampleSS.xlsx"));

		XSSFReader r = new XSSFReader(pkg);
		byte[] data = new byte[4096];

		// By r:id
		assertNotNull(r.getSheet("rId2"));
		int read = IOUtils.readFully(r.getSheet("rId2"), data);
		assertEquals(974, read);

		// All
		Iterator<InputStream> it = r.getSheetsData();

		int count = 0;
		while(it.hasNext()) {
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

	/**
	 * Check that the sheet iterator returns sheets in the logical order
	 * (as they are defined in the workbook.xml)
	 */
    @Test
	public void testOrderOfSheets() throws Exception {
        OPCPackage pkg = OPCPackage.open(_ssTests.openResourceAsStream("reordered_sheets.xlsx"));

		XSSFReader r = new XSSFReader(pkg);

		String[] sheetNames = {"Sheet4", "Sheet2", "Sheet3", "Sheet1"};
		XSSFReader.SheetIterator it = (XSSFReader.SheetIterator)r.getSheetsData();

		int count = 0;
		while(it.hasNext()) {
			InputStream inp = it.next();
			assertNotNull(inp);
			inp.close();

			assertEquals(sheetNames[count], it.getSheetName());
			count++;
		}
		assertEquals(4, count);
	}

    @Test
	public void testComments() throws Exception {
      OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("comments.xlsx");
      XSSFReader r = new XSSFReader(pkg);
      XSSFReader.SheetIterator it = (XSSFReader.SheetIterator)r.getSheetsData();
      
      int count = 0;
      while(it.hasNext()) {
         count++;
         InputStream inp = it.next();
         inp.close();

         if(count == 1) {
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
   
   /**
    * Iterating over a workbook with chart sheets in it, using the
    *  XSSFReader method
    * @throws Exception
    */
   @Test
   public void test50119() throws Exception {
      OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("WithChartSheet.xlsx");
      XSSFReader r = new XSSFReader(pkg);
      XSSFReader.SheetIterator it = (XSSFReader.SheetIterator)r.getSheetsData();
      
      while(it.hasNext())
      {
          InputStream stream = it.next();
          stream.close();
      }
   }

    /**
     * Test text extraction from text box using getShapes()
     *
     * @throws Exception
     */
    @Test
    public void testShapes() throws Exception {
        OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("WithTextBox.xlsx");
        XSSFReader r = new XSSFReader(pkg);
        XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

        String text = getShapesString(it);
        assertContains(text, "Line 1");
        assertContains(text, "Line 2");
        assertContains(text, "Line 3");
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
    public void testBug57914() throws Exception {
        OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("57914.xlsx");
        final XSSFReader r;

        // for now expect this to fail, when we fix 57699, this one should fail so we know we should adjust
        // this test as well
        try {
            r = new XSSFReader(pkg);
            fail("This will fail until bug 57699 is fixed");
        } catch (POIXMLException e) {
            assertContains(e.getMessage(), "57699");
            return;
        }

        XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) r.getSheetsData();

        String text = getShapesString(it);
        assertContains(text, "Line 1");
        assertContains(text, "Line 2");
        assertContains(text, "Line 3");
    }

   /**
    * NPE from XSSFReader$SheetIterator.<init> on XLSX files generated by
    *  the openpyxl library
    */
   @Test
   public void test58747() throws Exception {
       OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("58747.xlsx");
       ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
       assertNotNull(strings);
       XSSFReader reader = new XSSFReader(pkg);
       StylesTable styles = reader.getStylesTable();
       assertNotNull(styles);

       XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
       assertEquals(true, iter.hasNext());
       iter.next();

       assertEquals(false, iter.hasNext());
       assertEquals("Orders", iter.getSheetName());
       
       pkg.close();
   }

    /**
     * NPE when sheet has no relationship id in the workbook
     * 60825
     */
    @Test
    public void testSheetWithNoRelationshipId() throws Exception {
        OPCPackage pkg =  XSSFTestDataSamples.openSamplePackage("60825.xlsx");
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
        assertNotNull(strings);
        XSSFReader reader = new XSSFReader(pkg);
        StylesTable styles = reader.getStylesTable();
        assertNotNull(styles);

        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
        assertNotNull(iter.next());
        assertFalse(iter.hasNext());

        pkg.close();
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
    public void test61034() throws Exception {
        OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("61034.xlsx");
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
        pkg.close();
    }

    @Test
    @Ignore("until we fix issue https://bz.apache.org/bugzilla/show_bug.cgi?id=61701")
    public void test61701() throws Exception {
        try(Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("simple-table-named-range.xlsx")) {
            Name name = workbook.getName("total");
            System.out.println("workbook.getName(\"total\").getSheetName() returned: " + name.getSheetName());
        }
    }
}
