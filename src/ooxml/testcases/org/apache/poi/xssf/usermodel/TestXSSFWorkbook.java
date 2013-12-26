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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.ss.usermodel.BaseTestWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;

public final class TestXSSFWorkbook extends BaseTestWorkbook {

	public TestXSSFWorkbook() {
		super(XSSFITestDataProvider.instance);
	}

	/**
	 * Tests that we can save, and then re-load a new document
	 */
	public void testSaveLoadNew() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();

		//check that the default date system is set to 1900
		CTWorkbookPr pr = workbook.getCTWorkbook().getWorkbookPr();
		assertNotNull(pr);
		assertTrue(pr.isSetDate1904());
		assertFalse("XSSF must use the 1900 date system", pr.getDate1904());

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

		File file = TempFile.createTempFile("poi-", ".xlsx");
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

		pkg.close();
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

		pkg.close();
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

	public void testIncrementSheetId() {
		XSSFWorkbook wb = new XSSFWorkbook();
		int sheetId = (int)wb.createSheet().sheet.getSheetId();
		assertEquals(1, sheetId);
		sheetId = (int)wb.createSheet().sheet.getSheetId();
		assertEquals(2, sheetId);

		//test file with gaps in the sheetId sequence
		wb = XSSFTestDataSamples.openSampleWorkbook("47089.xlsm");
		int lastSheetId = (int)wb.getSheetAt(wb.getNumberOfSheets() - 1).sheet.getSheetId();
		sheetId = (int)wb.createSheet().sheet.getSheetId();
		assertEquals(lastSheetId+1, sheetId);
	}

	/**
	 *  Test setting of core properties such as Title and Author
	 */
	public void testWorkbookProperties() {
		XSSFWorkbook workbook = new XSSFWorkbook();
		POIXMLProperties props = workbook.getProperties();
		assertNotNull(props);
		//the Application property must be set for new workbooks, see Bugzilla #47559
		assertEquals("Apache POI", props.getExtendedProperties().getUnderlyingProperties().getApplication());

		PackagePropertiesPart opcProps = props.getCoreProperties().getUnderlyingProperties();
		assertNotNull(opcProps);

		opcProps.setTitleProperty("Testing Bugzilla #47460");
		assertEquals("Apache POI", opcProps.getCreatorProperty().getValue());
		opcProps.setCreatorProperty("poi-dev@poi.apache.org");

		workbook = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertEquals("Apache POI", workbook.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
		opcProps = workbook.getProperties().getCoreProperties().getUnderlyingProperties();
		assertEquals("Testing Bugzilla #47460", opcProps.getTitleProperty().getValue());
		assertEquals("poi-dev@poi.apache.org", opcProps.getCreatorProperty().getValue());
	}

	/**
	 * Verify that the attached test data was not modified. If this test method
	 * fails, the test data is not working properly.
	 */
	public void testBug47668() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("47668.xlsx");
		List<XSSFPictureData> allPictures = workbook.getAllPictures();
		assertEquals(1, allPictures.size());

		PackagePartName imagePartName = PackagingURIHelper
				.createPartName("/xl/media/image1.jpeg");
		PackagePart imagePart = workbook.getPackage().getPart(imagePartName);
		assertNotNull(imagePart);

		for (XSSFPictureData pictureData : allPictures) {
			PackagePart picturePart = pictureData.getPackagePart();
			assertSame(imagePart, picturePart);
		}

		XSSFSheet sheet0 = workbook.getSheetAt(0);
		XSSFDrawing drawing0 = sheet0.createDrawingPatriarch();
		XSSFPictureData pictureData0 = (XSSFPictureData) drawing0.getRelations().get(0);
		byte[] data0 = pictureData0.getData();
		CRC32 crc0 = new CRC32();
		crc0.update(data0);

		XSSFSheet sheet1 = workbook.getSheetAt(1);
		XSSFDrawing drawing1 = sheet1.createDrawingPatriarch();
		XSSFPictureData pictureData1 = (XSSFPictureData) drawing1.getRelations().get(0);
		byte[] data1 = pictureData1.getData();
		CRC32 crc1 = new CRC32();
		crc1.update(data1);

		assertEquals(crc0.getValue(), crc1.getValue());
	}

	/**
	 * When deleting a sheet make sure that we adjust sheet indices of named ranges
	 */
	public void testBug47737() {
		XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47737.xlsx");
		assertEquals(2, wb.getNumberOfNames());
		assertNotNull(wb.getCalculationChain());

		XSSFName nm0 = wb.getNameAt(0);
		assertTrue(nm0.getCTName().isSetLocalSheetId());
		assertEquals(0, nm0.getCTName().getLocalSheetId());

		XSSFName nm1 = wb.getNameAt(1);
		assertTrue(nm1.getCTName().isSetLocalSheetId());
		assertEquals(1, nm1.getCTName().getLocalSheetId());

		wb.removeSheetAt(0);
		assertEquals(1, wb.getNumberOfNames());
		XSSFName nm2 = wb.getNameAt(0);
		assertTrue(nm2.getCTName().isSetLocalSheetId());
		assertEquals(0, nm2.getCTName().getLocalSheetId());
		//calculation chain is removed as well
		assertNull(wb.getCalculationChain());

	}

	/**
	 * Problems with XSSFWorkbook.removeSheetAt when workbook contains charts
	 */
	public void testBug47813() {
		XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47813.xlsx");
		assertEquals(3, wb.getNumberOfSheets());
		assertNotNull(wb.getCalculationChain());

		assertEquals("Numbers", wb.getSheetName(0));
		//the second sheet is of type 'chartsheet'
		assertEquals("Chart", wb.getSheetName(1));
		assertTrue(wb.getSheetAt(1) instanceof XSSFChartSheet);
		assertEquals("SomeJunk", wb.getSheetName(2));

		wb.removeSheetAt(2);
		assertEquals(2, wb.getNumberOfSheets());
		assertNull(wb.getCalculationChain());

		wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
		assertEquals(2, wb.getNumberOfSheets());
		assertNull(wb.getCalculationChain());

		assertEquals("Numbers", wb.getSheetName(0));
		assertEquals("Chart", wb.getSheetName(1));
	}

	/**
	 * Problems with the count of the number of styles
	 *  coming out wrong
	 */
	public void testBug49702() throws Exception {
	    // First try with a new file
	    XSSFWorkbook wb = new XSSFWorkbook();

	    // Should have one style
	    assertEquals(1, wb.getNumCellStyles());
	    wb.getCellStyleAt((short)0);
	    try {
	        wb.getCellStyleAt((short)1);
	        fail("Shouldn't be able to get style at 1 that doesn't exist");
	    } catch(IndexOutOfBoundsException e) {}

	    // Add another one
	    CellStyle cs = wb.createCellStyle();
	    cs.setDataFormat((short)11);

	    // Re-check
	    assertEquals(2, wb.getNumCellStyles());
	    wb.getCellStyleAt((short)0);
	    wb.getCellStyleAt((short)1);
	    try {
	        wb.getCellStyleAt((short)2);
	        fail("Shouldn't be able to get style at 2 that doesn't exist");
	    } catch(IndexOutOfBoundsException e) {}

	    // Save and reload
	    XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
	    assertEquals(2, nwb.getNumCellStyles());
	    nwb.getCellStyleAt((short)0);
	    nwb.getCellStyleAt((short)1);
	    try {
	        nwb.getCellStyleAt((short)2);
	        fail("Shouldn't be able to get style at 2 that doesn't exist");
	    } catch(IndexOutOfBoundsException e) {}

	    // Now with an existing file
	    wb = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
	    assertEquals(3, wb.getNumCellStyles());
	    wb.getCellStyleAt((short)0);
	    wb.getCellStyleAt((short)1);
	    wb.getCellStyleAt((short)2);
	    try {
	        wb.getCellStyleAt((short)3);
	        fail("Shouldn't be able to get style at 3 that doesn't exist");
	    } catch(IndexOutOfBoundsException e) {}
	}

    public void testRecalcId() {
        XSSFWorkbook wb = new XSSFWorkbook();
        assertFalse(wb.getForceFormulaRecalculation());
        CTWorkbook ctWorkbook = wb.getCTWorkbook();
        assertFalse(ctWorkbook.isSetCalcPr());

        wb.setForceFormulaRecalculation(true); // resets the EngineId flag to zero

        CTCalcPr calcPr = ctWorkbook.getCalcPr();
        assertNotNull(calcPr);
        assertEquals(0, (int) calcPr.getCalcId());

        calcPr.setCalcId(100);
        assertTrue(wb.getForceFormulaRecalculation());

        wb.setForceFormulaRecalculation(true); // resets the EngineId flag to zero
        assertEquals(0, (int) calcPr.getCalcId());
        assertFalse(wb.getForceFormulaRecalculation());

        // calcMode="manual" is unset when forceFormulaRecalculation=true
        calcPr.setCalcMode(STCalcMode.MANUAL);
        wb.setForceFormulaRecalculation(true);
        assertEquals(STCalcMode.AUTO, calcPr.getCalcMode());

    }

    public void testChangeSheetNameWithSharedFormulas() {
        changeSheetNameWithSharedFormulas("shared_formulas.xlsx");
    }

    public void testSetTabColor() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet();
        assertTrue(sh.getCTWorksheet().getSheetPr() == null || !sh.getCTWorksheet().getSheetPr().isSetTabColor());
        sh.setTabColor(IndexedColors.RED.index);
        assertTrue(sh.getCTWorksheet().getSheetPr().isSetTabColor());
        assertEquals(IndexedColors.RED.index,
                sh.getCTWorksheet().getSheetPr().getTabColor().getIndexed());
    }

	public void testColumnWidthPOI52233() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("hello world");

		sheet = workbook.createSheet();
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);
       
        sheet.groupColumn((short) 4, (short) 5);

        accessWorkbook(workbook);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			workbook.write(stream);
		} finally {
			stream.close();
		}

		accessWorkbook(workbook);
	}

	private void accessWorkbook(XSSFWorkbook workbook) {
		workbook.getSheetAt(1).setColumnGroupCollapsed(4, true);
		workbook.getSheetAt(1).setColumnGroupCollapsed(4, false);

		assertEquals("hello world", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
		assertEquals(2048, workbook.getSheetAt(0).getColumnWidth(0)); // <-works
	}


	public void testBug48495() {
		try {
			Workbook wb = XSSFTestDataSamples.openSampleWorkbook("48495.xlsx");
			
			assertSheetOrder(wb, "Sheet1");
			
			Sheet sheet = wb.getSheetAt(0);
			sheet.shiftRows(2, sheet.getLastRowNum(), 1, true, false);
			Row newRow = sheet.getRow(2);
			if (newRow == null) newRow = sheet.createRow(2);
			newRow.createCell(0).setCellValue(" Another Header");
			wb.cloneSheet(0);

			assertSheetOrder(wb, "Sheet1", "Sheet1 (2)");

			//		    FileOutputStream fileOut = new FileOutputStream("/tmp/bug48495.xlsx");
//		    try {
//		    	wb.write(fileOut);
//		    } finally {
//		    	fileOut.close();
//		    }
			
			Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
			assertNotNull(read);
			assertSheetOrder(read, "Sheet1", "Sheet1 (2)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
	
	public void testBug47090a() {
	    Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
		assertSheetOrder(workbook, "Sheet1", "Sheet2");
	    workbook.removeSheetAt(0);
		assertSheetOrder(workbook, "Sheet2");
	    workbook.createSheet();
		assertSheetOrder(workbook, "Sheet2", "Sheet1");
	    Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertSheetOrder(read, "Sheet2", "Sheet1");
	}
	
	public void testBug47090b() {
	    Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
	    assertSheetOrder(workbook, "Sheet1", "Sheet2");
	    workbook.removeSheetAt(1);
		assertSheetOrder(workbook, "Sheet1");
	    workbook.createSheet();
		assertSheetOrder(workbook, "Sheet1", "Sheet0");		// Sheet0 because it uses "Sheet" + sheets.size() as starting point!
	    Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertSheetOrder(read, "Sheet1", "Sheet0");
	}

	public void testBug47090c() {
	    Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
	    assertSheetOrder(workbook, "Sheet1", "Sheet2");
	    workbook.removeSheetAt(0);
		assertSheetOrder(workbook, "Sheet2");
	    workbook.cloneSheet(0);	
		assertSheetOrder(workbook, "Sheet2", "Sheet2 (2)");
	    Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertSheetOrder(read, "Sheet2", "Sheet2 (2)");
	}
	
	public void testBug47090d() {
	    Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
	    assertSheetOrder(workbook, "Sheet1", "Sheet2");
	    workbook.createSheet();
		assertSheetOrder(workbook, "Sheet1", "Sheet2", "Sheet0");
	    workbook.removeSheetAt(0);
		assertSheetOrder(workbook, "Sheet2", "Sheet0");
	    workbook.createSheet();	
		assertSheetOrder(workbook, "Sheet2", "Sheet0", "Sheet1");
	    Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
		assertSheetOrder(read, "Sheet2", "Sheet0", "Sheet1");
	}
	
	public void testBug51158() throws IOException {
        // create a workbook
        final XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Test Sheet");
        XSSFRow row = sheet.createRow(2);
        XSSFCell cell = row.createCell(3);
        cell.setCellValue("test1");

        //XSSFCreationHelper helper = workbook.getCreationHelper();
        //cell.setHyperlink(helper.createHyperlink(0));

        XSSFComment comment = sheet.createDrawingPatriarch().createCellComment(new XSSFClientAnchor());
        assertNotNull(comment);
        comment.setString("some comment");

//        CellStyle cs = workbook.createCellStyle();
//        cs.setShrinkToFit(false);
//        row.createCell(0).setCellStyle(cs);

        // write the first excel file
        XSSFWorkbook readBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertNotNull(readBack);
        assertEquals("test1", readBack.getSheetAt(0).getRow(2).getCell(3).getStringCellValue());
        assertNull(readBack.getSheetAt(0).getRow(2).getCell(4));

        // add a new cell to the sheet
        cell = row.createCell(4);
        cell.setCellValue("test2");

        // write the second excel file
        readBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertNotNull(readBack);
        assertEquals("test1", readBack.getSheetAt(0).getRow(2).getCell(3).getStringCellValue());
        assertEquals("test2", readBack.getSheetAt(0).getRow(2).getCell(4).getStringCellValue());
	}
	
	public void testBug51158a() throws IOException {
        // create a workbook
        final XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Test Sheet");

        XSSFSheet sheetBack = workbook.getSheetAt(0);

        // committing twice did add the XML twice without clearing the part in between
        sheetBack.commit();

        // ensure that a memory based package part does not have lingering data from previous commit() calls
        if(sheetBack.getPackagePart() instanceof MemoryPackagePart) {
            ((MemoryPackagePart)sheetBack.getPackagePart()).clear();
        }

        sheetBack.commit();

        String str = new String(IOUtils.toByteArray(sheetBack.getPackagePart().getInputStream()));
        System.out.println(str);
        
        assertEquals(1, countMatches(str, "<worksheet"));
    }	
	
    private static final int INDEX_NOT_FOUND = -1;
    
    private static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static int countMatches(CharSequence str, CharSequence sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = indexOf(str, sub, idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }
    
    private static int indexOf(CharSequence cs, CharSequence searchChar, int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
    }
}
