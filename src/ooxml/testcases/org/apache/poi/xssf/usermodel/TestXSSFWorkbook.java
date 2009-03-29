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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;

public final class TestXSSFWorkbook extends BaseTestWorkbook {

    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }


	/**
	 * Tests that we can save, and then re-load a new document
	 */
	public void testSaveLoadNew() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet1 = workbook.createSheet("sheet1");
		Sheet sheet2 = workbook.createSheet("sheet2");
		workbook.createSheet("sheet3");
		
		RichTextString rts = workbook.getCreationHelper().createRichTextString("hello world");
		
		sheet1.createRow(0).createCell((short)0).setCellValue(1.2);
		sheet1.createRow(1).createCell((short)0).setCellValue(rts);
		sheet2.createRow(0);
		
		assertEquals(0, workbook.getSheetAt(0).getFirstRowNum());
		assertEquals(1, workbook.getSheetAt(0).getLastRowNum());
		assertEquals(0, workbook.getSheetAt(1).getFirstRowNum());
		assertEquals(0, workbook.getSheetAt(1).getLastRowNum());
		assertEquals(0, workbook.getSheetAt(2).getFirstRowNum());
		assertEquals(0, workbook.getSheetAt(2).getLastRowNum());
		
		File file = File.createTempFile("poi-", ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.write(out);
		out.close();
		
		// Check the package contains what we'd expect it to
		OPCPackage pkg = OPCPackage.open(file.toString());
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
		assertEquals(0, workbook.getSheetAt(2).getFirstRowNum());
		assertEquals(0, workbook.getSheetAt(2).getLastRowNum());
		
		sheet1 = workbook.getSheetAt(0);
		assertEquals(1.2, sheet1.getRow(0).getCell(0).getNumericCellValue(), 0.0001);
		assertEquals("hello world", sheet1.getRow(1).getCell(0).getRichStringCellValue().getString());
	}
	
	public void testExisting() throws Exception {
		
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("Formatting.xlsx");
		assertNotNull(workbook.getSharedStringSource());
		assertNotNull(workbook.getStylesSource());
		
		// And check a few low level bits too
		OPCPackage pkg = OPCPackage.open(HSSFTestDataSamples.openSampleFileStream("Formatting.xlsx"));
		PackagePart wbPart = 
			pkg.getPart(PackagingURIHelper.createPartName("/xl/workbook.xml"));
		
		// Links to the three sheets, shared, styles and themes
		assertTrue(wbPart.hasRelationships());
		assertEquals(6, wbPart.getRelationships().size());

	}
	
	public void testGetCellStyleAt(){
	 	XSSFWorkbook workbook = new XSSFWorkbook();
		short i = 0;
		//get default style
		CellStyle cellStyleAt = workbook.getCellStyleAt(i);
		assertNotNull(cellStyleAt);
		
		//get custom style
		StylesTable styleSource = workbook.getStylesSource();
		XSSFCellStyle customStyle = new XSSFCellStyle(styleSource);
		XSSFFont font = new XSSFFont();
		font.setFontName("Verdana");
		customStyle.setFont(font);
		int x = styleSource.putStyle(customStyle);
		cellStyleAt = workbook.getCellStyleAt((short)x);
		assertNotNull(cellStyleAt);		
	}
	
	public void testGetFontAt(){
	 	XSSFWorkbook workbook = new XSSFWorkbook();
		StylesTable styleSource = workbook.getStylesSource();
		short i = 0;
		//get default font
		Font fontAt = workbook.getFontAt(i);
		assertNotNull(fontAt);
		
		//get customized font
		XSSFFont customFont = new XSSFFont();
		customFont.setItalic(true);
		int x = styleSource.putFont(customFont);
		fontAt = workbook.getFontAt((short)x);
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
	
	public void testLoadSave() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("Formatting.xlsx");
		assertEquals(3, workbook.getNumberOfSheets());
		assertEquals("dd/mm/yyyy", workbook.getSheetAt(0).getRow(1).getCell(0).getRichStringCellValue().getString());
		assertNotNull(workbook.getSharedStringSource());
		assertNotNull(workbook.getStylesSource());
		
		// Write out, and check
		// Load up again, check all still there
		XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
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
	
	public void testStyles() {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("Formatting.xlsx");
		
		StylesTable ss = workbook.getStylesSource();
		assertNotNull(ss);
		StylesTable st = ss;
		
		// Has 8 number formats
		assertEquals(8, st._getNumberFormatSize());
		// Has 2 fonts
		assertEquals(2, st.getFonts().size());
		// Has 2 fills
		assertEquals(2, st.getFills().size());
		// Has 1 border
		assertEquals(1, st.getBorders().size());
		
		// Add two more styles
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 8, 
				st.putNumberFormat("testFORMAT"));
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 8, 
				st.putNumberFormat("testFORMAT"));
		assertEquals(StylesTable.FIRST_CUSTOM_STYLE_ID + 9, 
				st.putNumberFormat("testFORMAT2"));
		assertEquals(10, st._getNumberFormatSize());
		
		
		// Save, load back in again, and check
		workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		
		ss = workbook.getStylesSource();
		assertNotNull(ss);

		assertEquals(10, st._getNumberFormatSize());
		assertEquals(2, st.getFonts().size());
		assertEquals(2, st.getFills().size());
		assertEquals(1, st.getBorders().size());
	}
	
}
