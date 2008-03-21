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

package org.apache.poi.xssf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;

import org.openxml4j.opc.ContentTypes;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagingURIHelper;

public class TestXSSFWorkbook extends TestCase {
    public TestXSSFWorkbook(String name) {
		super(name);
		
		// Use system out logger
	    System.setProperty(
	            "org.apache.poi.util.POILogger",
	            "org.apache.poi.util.SystemOutLogger"
	    );
	}

	public void testGetSheetIndex() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertEquals(0, workbook.getSheetIndex(sheet1));
        assertEquals(0, workbook.getSheetIndex("sheet1"));
        assertEquals(1, workbook.getSheetIndex(sheet2));
        assertEquals(1, workbook.getSheetIndex("sheet2"));
        assertEquals(-1, workbook.getSheetIndex("noSheet"));
    }
    
    public void testSetSheetOrder() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertSame(sheet1, workbook.getSheetAt(0));
        assertSame(sheet2, workbook.getSheetAt(1));
        workbook.setSheetOrder("sheet2", 0);
        assertSame(sheet2, workbook.getSheetAt(0));
        assertSame(sheet1, workbook.getSheetAt(1));
        // Test reordering of CTSheets
        CTWorkbook ctwb = workbook.getWorkbook();
        CTSheet[] ctsheets = ctwb.getSheets().getSheetArray();
        assertEquals("sheet2", ctsheets[0].getName());
        assertEquals("sheet1", ctsheets[1].getName());
        
        // Borderline case: only one sheet
        workbook = new XSSFWorkbook();
        sheet1 = workbook.createSheet("sheet1");
        assertSame(sheet1, workbook.getSheetAt(0));
        workbook.setSheetOrder("sheet1", 0);
        assertSame(sheet1, workbook.getSheetAt(0));
    }
    
    public void testSetSelectedTab() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertEquals(-1, workbook.getSelectedTab());
        workbook.setSelectedTab((short) 0);
        assertEquals(0, workbook.getSelectedTab());
        workbook.setSelectedTab((short) 1);
        assertEquals(1, workbook.getSelectedTab());
    }
    
    public void testSetSheetName() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        assertEquals("sheet1", workbook.getSheetName(0));
        workbook.setSheetName(0, "sheet2");
        assertEquals("sheet2", workbook.getSheetName(0));
    }
    
    public void testCloneSheet() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet");
        Sheet sheet2 = workbook.cloneSheet(0);
        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals("sheet(1)", workbook.getSheetName(1));
        workbook.setSheetName(1, "clonedsheet");
        Sheet sheet3 = workbook.cloneSheet(1);
        assertEquals(3, workbook.getNumberOfSheets());
        assertEquals("clonedsheet(1)", workbook.getSheetName(2));
    }
    
    public void testGetSheetByName() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        assertSame(sheet1, workbook.getSheet("sheet1"));
        assertSame(sheet2, workbook.getSheet("sheet2"));
        assertNull(workbook.getSheet("nosheet"));
    }
    
    public void testRemoveSheetAt() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        Sheet sheet3 = workbook.createSheet("sheet3");
        workbook.removeSheetAt(1);
        assertEquals(2, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(1));
        workbook.removeSheetAt(0);
        assertEquals(1, workbook.getNumberOfSheets());
        assertEquals("sheet3", workbook.getSheetName(0));
        workbook.removeSheetAt(0);
        assertEquals(0, workbook.getNumberOfSheets());
    }
    
    /**
     * Tests that we can save a new document
     */
    public void testSaveNew() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        Sheet sheet3 = workbook.createSheet("sheet3");
        File file = File.createTempFile("poi-", ".xlsx");
        System.out.println("Saving newly created file to " + file.getAbsolutePath());
        OutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
    }

    /**
     * Tests that we can save, and then re-load a new document
     */
    public void testSaveLoadNew() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("sheet1");
        Sheet sheet2 = workbook.createSheet("sheet2");
        Sheet sheet3 = workbook.createSheet("sheet3");
        
        sheet1.createRow(0);
        sheet1.createRow(1);
        sheet2.createRow(0);
        
        assertEquals(0, workbook.getSheetAt(0).getFirstRowNum());
        assertEquals(1, workbook.getSheetAt(0).getLastRowNum());
        assertEquals(0, workbook.getSheetAt(1).getFirstRowNum());
        assertEquals(0, workbook.getSheetAt(1).getLastRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getFirstRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getLastRowNum());
        
        File file = File.createTempFile("poi-", ".xlsx");
        OutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        
        // Check the package contains what we'd expect it to
        Package pkg = Package.open(file.toString());
        PackagePart wbRelPart = 
        	pkg.getPart(PackagingURIHelper.createPartName("/xl/_rels/workbook.xml.rels"));
        assertNotNull(wbRelPart);
        assertTrue(wbRelPart.isRelationshipPart());
        assertEquals(ContentTypes.RELATIONSHIPS_PART, wbRelPart.getContentType());
        
        PackagePart wbPart = 
        	pkg.getPart(PackagingURIHelper.createPartName("/xl/workbook.xml"));
        // Links to the three sheets
        assertTrue(wbPart.hasRelationships());
        assertEquals(3, wbPart.getRelationships().size());
        
        // Load back the XSSFWorkbook
        workbook = new XSSFWorkbook(pkg);
        assertEquals(3, workbook.getNumberOfSheets());
        assertNotNull(workbook.getSheetAt(0));
        assertNotNull(workbook.getSheetAt(1));
        assertNotNull(workbook.getSheetAt(2));
        
        assertEquals(0, workbook.getSheetAt(0).getFirstRowNum());
        assertEquals(1, workbook.getSheetAt(0).getLastRowNum());
        assertEquals(0, workbook.getSheetAt(1).getFirstRowNum());
        assertEquals(0, workbook.getSheetAt(1).getLastRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getFirstRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getLastRowNum());
    }
    
    public void testExisting() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "Formatting.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		assertNotNull(workbook.getSharedStringSource());
		assertNotNull(workbook.getStylesSource());
		
		// And check a few low level bits too
		Package pkg = Package.open(xml.toString());
        PackagePart wbPart = 
        	pkg.getPart(PackagingURIHelper.createPartName("/xl/workbook.xml"));
        
        // Links to the three sheets, shared, styles and themes
        assertTrue(wbPart.hasRelationships());
        assertEquals(6, wbPart.getRelationships().size());

    }
    
    public void testLoadSave() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "Formatting.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		assertEquals(3, workbook.getNumberOfSheets());
		assertEquals("dd/mm/yyyy", workbook.getSheetAt(0).getRow(1).getCell(0).getRichStringCellValue().getString());
		assertNotNull(workbook.getSharedStringSource());
		assertNotNull(workbook.getStylesSource());
		
		// Write out, and check
		File tmpFile = File.createTempFile("poi-tmp", ".xlsx");
		workbook.write(new FileOutputStream(tmpFile));
		
		// Load up again, check all still there
		XSSFWorkbook wb2 = new XSSFWorkbook(tmpFile.toString());
		assertEquals(3, wb2.getNumberOfSheets());
		assertNotNull(wb2.getSheetAt(0));
		assertNotNull(wb2.getSheetAt(1));
		assertNotNull(wb2.getSheetAt(2));
		
		assertEquals("dd/mm/yyyy", wb2.getSheetAt(0).getRow(1).getCell(0).getRichStringCellValue().getString());
		assertEquals("yyyy/mm/dd", wb2.getSheetAt(0).getRow(2).getCell(0).getRichStringCellValue().getString());
		assertEquals("yyyy-mm-dd", wb2.getSheetAt(0).getRow(3).getCell(0).getRichStringCellValue().getString());
		assertEquals("yy/mm/dd", wb2.getSheetAt(0).getRow(4).getCell(0).getRichStringCellValue().getString());
		assertNotNull(wb2.getSharedStringSource());
		assertNotNull(wb2.getStylesSource());
    }
    
    public void testStyles() throws Exception {
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "Formatting.xlsx"
		);
		assertTrue(xml.exists());
    	
		XSSFWorkbook workbook = new XSSFWorkbook(xml.toString());
		
		StylesSource ss = workbook.getStylesSource();
		assertNotNull(ss);
		assertTrue(ss instanceof StylesTable);
		StylesTable st = (StylesTable)ss;
		
		// Has 8 number formats
		assertEquals(8, st._getNumberFormatSize());
		// Has 2 fonts
		assertEquals(2, st._getFontsSize());
		// Has 2 fills
		assertEquals(2, st._getFillsSize());
		// Has 1 border
		assertEquals(1, st._getBordersSize());
		
		// Add two more styles
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 8, 
				st.putNumberFormat("testFORMAT"));
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 8, 
				st.putNumberFormat("testFORMAT"));
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 9, 
				st.putNumberFormat("testFORMAT2"));
		assertEquals(10, st._getNumberFormatSize());
		
		// Save, load back in again, and check
		// TODO
    }
}
