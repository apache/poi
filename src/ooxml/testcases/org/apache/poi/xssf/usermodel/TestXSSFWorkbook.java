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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.xssf.model.StylesTable;
import org.openxml4j.opc.ContentTypes;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;

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
        
        RichTextString rts = workbook.getCreationHelper().createRichTextString("hello world");
        
        sheet1.createRow(0).createCell((short)0).setCellValue(1.2);
        sheet1.createRow(1).createCell((short)0).setCellValue(rts);
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
        // Links to the three sheets, shared strings and styles
        assertTrue(wbPart.hasRelationships());
        assertEquals(5, wbPart.getRelationships().size());
        
        // Load back the XSSFWorkbook
        workbook = new XSSFWorkbook(pkg);
        assertEquals(3, workbook.getNumberOfSheets());
        assertNotNull(workbook.getSheetAt(0));
        assertNotNull(workbook.getSheetAt(1));
        assertNotNull(workbook.getSheetAt(2));
        
        assertNotNull(workbook.getSharedStringSource());
        assertNotNull(workbook.getStylesSource());
        
        assertEquals(0, workbook.getSheetAt(0).getFirstRowNum());
        assertEquals(1, workbook.getSheetAt(0).getLastRowNum());
        assertEquals(0, workbook.getSheetAt(1).getFirstRowNum());
        assertEquals(0, workbook.getSheetAt(1).getLastRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getFirstRowNum());
        assertEquals(-1, workbook.getSheetAt(2).getLastRowNum());
        
        sheet1 = workbook.getSheetAt(0);
        assertEquals(1.2, sheet1.getRow(0).getCell(0).getNumericCellValue(), 0.0001);
        assertEquals("hello world", sheet1.getRow(1).getCell(0).getRichStringCellValue().getString());
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
    
    public void testFindFont(){
        //get dafault font and check against default value
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	Font fontFind=workbook.findFont(Font.BOLDWEIGHT_NORMAL, Font.COLOR_NORMAL, (short)11, "Calibri", false, false, Font.SS_NONE, Font.U_NONE);
    	assertNotNull(fontFind);    	
    	
    	//get default font, then change 2 values and check against different values (height changes)
    	Font font=workbook.createFont();
    	font.setBoldweight(Font.BOLDWEIGHT_BOLD);
    	font.setUnderline(Font.U_DOUBLE);
    	StylesSource styleSource=new StylesTable();
    	long index=styleSource.putFont(font);
    	System.out.println("index="+index);
    	workbook.setStylesSource(styleSource);
    	fontFind=workbook.findFont(Font.BOLDWEIGHT_BOLD, Font.COLOR_NORMAL, (short)15, "Calibri", false, false, Font.SS_NONE, Font.U_DOUBLE);
        assertNull(fontFind);
    }

    public void testGetCellStyleAt(){
     	XSSFWorkbook workbook = new XSSFWorkbook();
        short i = 0;
        //get default style
        CellStyle cellStyleAt = workbook.getCellStyleAt(i);
        assertNotNull(cellStyleAt);
        
        //get custom style
        StylesSource styleSource = workbook.getStylesSource();
        CellStyle customStyle = new XSSFCellStyle(styleSource);
        Font font = new XSSFFont();
        font.setFontName("Verdana");
        customStyle.setFont(font);
        Long x = styleSource.putStyle(customStyle);
        cellStyleAt = workbook.getCellStyleAt(x.shortValue());
        assertNotNull(cellStyleAt);        
    }
    
    public void testGetFontAt(){
     	XSSFWorkbook workbook = new XSSFWorkbook();
        StylesSource styleSource = workbook.getStylesSource();
        short i = 0;
        //get default font
        Font fontAt = workbook.getFontAt(i);
        assertNotNull(fontAt);
        
        //get customized font
        Font customFont = new XSSFFont();
        customFont.setItalic(true);
        Long x = styleSource.putFont(customFont);
        fontAt = workbook.getFontAt(x.shortValue());
        assertNotNull(fontAt);
    }
    
    public void testGetNumCellStyles(){
     	XSSFWorkbook workbook = new XSSFWorkbook();
        short i = workbook.getNumCellStyles();
        //get default cellStyles
        assertEquals(1, i);
        //get wrong value
        assertNotSame(2, i);        
    }
    
    public void testGetDisplayedTab(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        short i = (short) workbook.getFirstVisibleTab();
        //get default diplayedTab
        assertEquals(0, i);        
    }
    
    public void testSetDisplayedTab(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.setFirstVisibleTab(new Integer(1).shortValue());
        short i = (short) workbook.getFirstVisibleTab();
        //0 (defualt value) is not longer set
        assertNotSame(0, i);
        //1 is the default tab
        assertEquals(1, i);
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        workbook = new XSSFWorkbook(Package.open(bais));
        
        ss = workbook.getStylesSource();
		assertNotNull(ss);
		assertTrue(ss instanceof StylesTable);
		st = (StylesTable)ss;
		
		assertEquals(10, st._getNumberFormatSize());
		assertEquals(2, st._getFontsSize());
		assertEquals(2, st._getFillsSize());
		assertEquals(1, st._getBordersSize());
    }
    
    public void testNamedRanges() throws Exception {
    	// First up, a new file
        XSSFWorkbook workbook = new XSSFWorkbook();
        assertEquals(0, workbook.getNumberOfNames());
        
        Name nameA = workbook.createName();
        nameA.setReference("A2");
        nameA.setNameName("ForA2");
        
        XSSFName nameB = workbook.createName();
        nameB.setReference("B3");
        nameB.setNameName("ForB3");
        nameB.setComment("B3 Comment");
        
        // Save and re-load
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        workbook = new XSSFWorkbook(Package.open(bais));
        
        assertEquals(2, workbook.getNumberOfNames());
        assertEquals("A2", workbook.getNameAt(0).getReference());
        assertEquals("ForA2", workbook.getNameAt(0).getNameName());
        assertNull(workbook.getNameAt(0).getComment());
        
        assertEquals("B3", workbook.getNameAt(1).getReference());
        assertEquals("ForB3", workbook.getNameAt(1).getNameName());
        assertEquals("B3 Comment", workbook.getNameAt(1).getComment());
        
        assertEquals("ForA2", workbook.getNameName(0));
        assertEquals(1, workbook.getNameIndex("ForB3"));
        assertEquals(-1, workbook.getNameIndex("ForB3!!"));
    	
        
        // Now, an existing file with named ranges
		File xml = new File(
				System.getProperty("HSSF.testdata.path") +
				File.separator + "WithVariousData.xlsx"
		);
		assertTrue(xml.exists());
    	
		workbook = new XSSFWorkbook(xml.toString());
		
        assertEquals(2, workbook.getNumberOfNames());
        assertEquals("Sheet1!$A$2:$A$7", workbook.getNameAt(0).getReference());
        assertEquals("AllANumbers", workbook.getNameAt(0).getNameName());
        assertEquals("All the numbers in A", workbook.getNameAt(0).getComment());
        
        assertEquals("Sheet1!$B$2:$B$7", workbook.getNameAt(1).getReference());
        assertEquals("AllBStrings", workbook.getNameAt(1).getNameName());
        assertEquals("All the strings in B", workbook.getNameAt(1).getComment());
        
        // Tweak, save, and re-check
        workbook.getNameAt(1).setNameName("BStringsFun");
        
        baos = new ByteArrayOutputStream();
        workbook.write(baos);
        bais = new ByteArrayInputStream(baos.toByteArray());
        workbook = new XSSFWorkbook(Package.open(bais));
		
        assertEquals(2, workbook.getNumberOfNames());
        assertEquals("Sheet1!$A$2:$A$7", workbook.getNameAt(0).getReference());
        assertEquals("AllANumbers", workbook.getNameAt(0).getNameName());
        assertEquals("All the numbers in A", workbook.getNameAt(0).getComment());
        
        assertEquals("Sheet1!$B$2:$B$7", workbook.getNameAt(1).getReference());
        assertEquals("BStringsFun", workbook.getNameAt(1).getNameName());
        assertEquals("All the strings in B", workbook.getNameAt(1).getComment());
    }
}
