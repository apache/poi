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
import java.util.List;
import java.util.zip.CRC32;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr;

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
		XSSFWorkbook wb = getTestDataProvider().createWorkbook();
		int sheetId = (int)wb.createSheet().sheet.getSheetId();
		assertEquals(1, sheetId);
		sheetId = (int)wb.createSheet().sheet.getSheetId();
		assertEquals(2, sheetId);

		//test file with gaps in the sheetId sequence
		wb = getTestDataProvider().openSampleWorkbook("47089.xlsm");
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
	public void test47668() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("47668.xlsx");
		List<XSSFPictureData> allPictures = workbook.getAllPictures();
		assertEquals(2, allPictures.size());

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
     * Problems with XSSFWorkbook.removeSheetAt when workbook contains chart
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
}
