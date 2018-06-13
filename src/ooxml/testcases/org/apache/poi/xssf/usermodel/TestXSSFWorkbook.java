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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.ss.usermodel.BaseTestXWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotCache;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;

public final class TestXSSFWorkbook extends BaseTestXWorkbook {

    public TestXSSFWorkbook() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * Tests that we can save, and then re-load a new document
     */
    @Test
    public void saveLoadNew() throws IOException, InvalidFormatException {
        XSSFWorkbook wb1 = new XSSFWorkbook();

        //check that the default date system is set to 1900
        CTWorkbookPr pr = wb1.getCTWorkbook().getWorkbookPr();
        assertNotNull(pr);
        assertTrue(pr.isSetDate1904());
        assertFalse("XSSF must use the 1900 date system", pr.getDate1904());

        Sheet sheet1 = wb1.createSheet("sheet1");
        Sheet sheet2 = wb1.createSheet("sheet2");
        wb1.createSheet("sheet3");

        RichTextString rts = wb1.getCreationHelper().createRichTextString("hello world");

        sheet1.createRow(0).createCell((short)0).setCellValue(1.2);
        sheet1.createRow(1).createCell((short)0).setCellValue(rts);
        sheet2.createRow(0);

        assertEquals(0, wb1.getSheetAt(0).getFirstRowNum());
        assertEquals(1, wb1.getSheetAt(0).getLastRowNum());
        assertEquals(0, wb1.getSheetAt(1).getFirstRowNum());
        assertEquals(0, wb1.getSheetAt(1).getLastRowNum());
        assertEquals(0, wb1.getSheetAt(2).getFirstRowNum());
        assertEquals(0, wb1.getSheetAt(2).getLastRowNum());

        File file = TempFile.createTempFile("poi-", ".xlsx");
        OutputStream out = new FileOutputStream(file);
        wb1.write(out);
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
        wb1.close();

        // Load back the XSSFWorkbook
        @SuppressWarnings("resource")
        XSSFWorkbook wb2 = new XSSFWorkbook(pkg);
        assertEquals(3, wb2.getNumberOfSheets());
        assertNotNull(wb2.getSheetAt(0));
        assertNotNull(wb2.getSheetAt(1));
        assertNotNull(wb2.getSheetAt(2));

        assertNotNull(wb2.getSharedStringSource());
        assertNotNull(wb2.getStylesSource());

        assertEquals(0, wb2.getSheetAt(0).getFirstRowNum());
        assertEquals(1, wb2.getSheetAt(0).getLastRowNum());
        assertEquals(0, wb2.getSheetAt(1).getFirstRowNum());
        assertEquals(0, wb2.getSheetAt(1).getLastRowNum());
        assertEquals(0, wb2.getSheetAt(2).getFirstRowNum());
        assertEquals(0, wb2.getSheetAt(2).getLastRowNum());

        sheet1 = wb2.getSheetAt(0);
        assertEquals(1.2, sheet1.getRow(0).getCell(0).getNumericCellValue(), 0.0001);
        assertEquals("hello world", sheet1.getRow(1).getCell(0).getRichStringCellValue().getString());

        pkg.close();
    }

    @Test
    public void existing() throws Exception {

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
        workbook.close();
    }

    @Test
    public void getCellStyleAt() throws IOException{
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
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
            cellStyleAt = workbook.getCellStyleAt((short) x);
            assertNotNull(cellStyleAt);
        }
    }

    @Test
    public void getFontAt() throws IOException{
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            StylesTable styleSource = workbook.getStylesSource();
            short i = 0;
            //get default font
            Font fontAt = workbook.getFontAt(i);
            assertNotNull(fontAt);

            //get customized font
            XSSFFont customFont = new XSSFFont();
            customFont.setItalic(true);
            int x = styleSource.putFont(customFont);
            fontAt = workbook.getFontAt((short) x);
            assertNotNull(fontAt);
        }
    }

    @Test
    public void getNumCellStyles() throws IOException{
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            //get default cellStyles
            assertEquals(1, workbook.getNumCellStyles());
        }
    }

    @Test
    public void loadSave() throws IOException {
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

        workbook.close();
        wb2.close();
    }

    @Test
    public void styles() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("Formatting.xlsx");

        StylesTable ss = wb1.getStylesSource();
        assertNotNull(ss);
        StylesTable st = ss;

        // Has 8 number formats
        assertEquals(8, st.getNumDataFormats());
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
        assertEquals(10, st.getNumDataFormats());


        // Save, load back in again, and check
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb1.close();

        ss = wb2.getStylesSource();
        assertNotNull(ss);

        assertEquals(10, st.getNumDataFormats());
        assertEquals(2, st.getFonts().size());
        assertEquals(2, st.getFills().size());
        assertEquals(1, st.getBorders().size());
        wb2.close();
    }

    @Test
    public void incrementSheetId() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            int sheetId = (int) wb.createSheet().sheet.getSheetId();
            assertEquals(1, sheetId);
            sheetId = (int) wb.createSheet().sheet.getSheetId();
            assertEquals(2, sheetId);

            //test file with gaps in the sheetId sequence
            try (XSSFWorkbook wbBack = XSSFTestDataSamples.openSampleWorkbook("47089.xlsm")) {
                int lastSheetId = (int) wbBack.getSheetAt(wbBack.getNumberOfSheets() - 1).sheet.getSheetId();
                sheetId = (int) wbBack.createSheet().sheet.getSheetId();
                assertEquals(lastSheetId + 1, sheetId);
            }
        }
    }

    /**
     *  Test setting of core properties such as Title and Author
     */
    @Test
    public void workbookProperties() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            POIXMLProperties props = workbook.getProperties();
            assertNotNull(props);
            //the Application property must be set for new workbooks, see Bugzilla #47559
            assertEquals("Apache POI", props.getExtendedProperties().getUnderlyingProperties().getApplication());

            PackagePropertiesPart opcProps = props.getCoreProperties().getUnderlyingProperties();
            assertNotNull(opcProps);

            opcProps.setTitleProperty("Testing Bugzilla #47460");
            assertEquals("Apache POI", opcProps.getCreatorProperty().get());
            opcProps.setCreatorProperty("poi-dev@poi.apache.org");

            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertEquals("Apache POI", wbBack.getProperties().getExtendedProperties().getUnderlyingProperties().getApplication());
            opcProps = wbBack.getProperties().getCoreProperties().getUnderlyingProperties();
            assertEquals("Testing Bugzilla #47460", opcProps.getTitleProperty().get());
            assertEquals("poi-dev@poi.apache.org", opcProps.getCreatorProperty().get());
            wbBack.close();
        }
    }

    /**
     * Verify that the attached test data was not modified. If this test method
     * fails, the test data is not working properly.
     */
    @Test
    public void bug47668() throws Exception {
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
        workbook.close();
    }

    /**
     * When deleting a sheet make sure that we adjust sheet indices of named ranges
     */
    @SuppressWarnings("deprecation")
    @Test
    public void bug47737() throws IOException {
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
        wb.close();

    }

    /**
     * Problems with XSSFWorkbook.removeSheetAt when workbook contains charts
     */
    @Test
    public void bug47813() throws IOException {
        XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("47813.xlsx");
        assertEquals(3, wb1.getNumberOfSheets());
        assertNotNull(wb1.getCalculationChain());

        assertEquals("Numbers", wb1.getSheetName(0));
        //the second sheet is of type 'chartsheet'
        assertEquals("Chart", wb1.getSheetName(1));
        assertTrue(wb1.getSheetAt(1) instanceof XSSFChartSheet);
        assertEquals("SomeJunk", wb1.getSheetName(2));

        wb1.removeSheetAt(2);
        assertEquals(2, wb1.getNumberOfSheets());
        assertNull(wb1.getCalculationChain());

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        assertEquals(2, wb2.getNumberOfSheets());
        assertNull(wb2.getCalculationChain());

        assertEquals("Numbers", wb2.getSheetName(0));
        assertEquals("Chart", wb2.getSheetName(1));
        wb2.close();
        wb1.close();
    }

    /**
     * Problems with the count of the number of styles
     *  coming out wrong
     */
    @Test
    public void bug49702() throws IOException {
        // First try with a new file
        XSSFWorkbook wb1 = new XSSFWorkbook();

        // Should have one style
        assertEquals(1, wb1.getNumCellStyles());
        wb1.getCellStyleAt((short)0);
        assertNull("Shouldn't be able to get style at 0 that doesn't exist",
                wb1.getCellStyleAt((short)1));

        // Add another one
        CellStyle cs = wb1.createCellStyle();
        cs.setDataFormat((short)11);

        // Re-check
        assertEquals(2, wb1.getNumCellStyles());
        wb1.getCellStyleAt((short)0);
        wb1.getCellStyleAt((short)1);
        assertNull("Shouldn't be able to get style at 2 that doesn't exist",
                wb1.getCellStyleAt((short)2));

        // Save and reload
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        assertEquals(2, nwb.getNumCellStyles());
        nwb.getCellStyleAt((short)0);
        nwb.getCellStyleAt((short)1);
        assertNull("Shouldn't be able to get style at 2 that doesn't exist",
                nwb.getCellStyleAt((short)2));

        // Now with an existing file
        XSSFWorkbook wb2 = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        assertEquals(3, wb2.getNumCellStyles());
        wb2.getCellStyleAt((short)0);
        wb2.getCellStyleAt((short)1);
        wb2.getCellStyleAt((short)2);
        assertNull("Shouldn't be able to get style at 3 that doesn't exist",
                wb2.getCellStyleAt((short)3));

        wb2.close();
        wb1.close();
        nwb.close();
    }

    @Test
    public void recalcId() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
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
    }

    @Test
    public void changeSheetNameWithSharedFormulas() throws IOException {
        changeSheetNameWithSharedFormulas("shared_formulas.xlsx");
    }

    @Test
    public void columnWidthPOI52233() throws Exception {
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

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            workbook.write(stream);
        }

        accessWorkbook(workbook);
        workbook.close();
    }

    private void accessWorkbook(XSSFWorkbook workbook) {
        workbook.getSheetAt(1).setColumnGroupCollapsed(4, true);
        workbook.getSheetAt(1).setColumnGroupCollapsed(4, false);

        assertEquals("hello world", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        assertEquals(2048, workbook.getSheetAt(0).getColumnWidth(0)); // <-works
    }

    @Test
    public void bug48495() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("48495.xlsx");

        assertSheetOrder(wb, "Sheet1");

        Sheet sheet = wb.getSheetAt(0);
        sheet.shiftRows(2, sheet.getLastRowNum(), 1, true, false);
        Row newRow = sheet.getRow(2);
        if (newRow == null) newRow = sheet.createRow(2);
        newRow.createCell(0).setCellValue(" Another Header");
        wb.cloneSheet(0);

        assertSheetOrder(wb, "Sheet1", "Sheet1 (2)");

        //            FileOutputStream fileOut = new FileOutputStream("/tmp/bug48495.xlsx");
//            try {
//                wb.write(fileOut);
//            } finally {
//                fileOut.close();
//            }

        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(read);
        assertSheetOrder(read, "Sheet1", "Sheet1 (2)");
        read.close();
        wb.close();
    }

    @Test
    public void bug47090a() throws IOException {
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
        assertSheetOrder(workbook, "Sheet1", "Sheet2");
        workbook.removeSheetAt(0);
        assertSheetOrder(workbook, "Sheet2");
        workbook.createSheet();
        assertSheetOrder(workbook, "Sheet2", "Sheet1");
        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertSheetOrder(read, "Sheet2", "Sheet1");
        read.close();
        workbook.close();
    }

    @Test
    public void bug47090b() throws IOException {
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
        assertSheetOrder(workbook, "Sheet1", "Sheet2");
        workbook.removeSheetAt(1);
        assertSheetOrder(workbook, "Sheet1");
        workbook.createSheet();
        assertSheetOrder(workbook, "Sheet1", "Sheet0");        // Sheet0 because it uses "Sheet" + sheets.size() as starting point!
        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertSheetOrder(read, "Sheet1", "Sheet0");
        read.close();
        workbook.close();
    }

    @Test
    public void bug47090c() throws IOException {
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("47090.xlsx");
        assertSheetOrder(workbook, "Sheet1", "Sheet2");
        workbook.removeSheetAt(0);
        assertSheetOrder(workbook, "Sheet2");
        workbook.cloneSheet(0);
        assertSheetOrder(workbook, "Sheet2", "Sheet2 (2)");
        Workbook read = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertSheetOrder(read, "Sheet2", "Sheet2 (2)");
        read.close();
        workbook.close();
    }

    @Test
    public void bug47090d() throws IOException {
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
        read.close();
        workbook.close();
    }

    @Test
    public void bug51158() throws IOException {
        // create a workbook
        final XSSFWorkbook wb1 = new XSSFWorkbook();
        XSSFSheet sheet = wb1.createSheet("Test Sheet");
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
        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb1);
        assertNotNull(wb2);
        sheet = wb2.getSheetAt(0);
        row = sheet.getRow(2);
        assertEquals("test1", row.getCell(3).getStringCellValue());
        assertNull(row.getCell(4));

        // add a new cell to the sheet
        cell = row.createCell(4);
        cell.setCellValue("test2");

        // write the second excel file
        XSSFWorkbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
        assertNotNull(wb3);
        sheet = wb3.getSheetAt(0);
        row = sheet.getRow(2);        
        
        assertEquals("test1", row.getCell(3).getStringCellValue());
        assertEquals("test2", row.getCell(4).getStringCellValue());
        wb3.close();
        wb2.close();
        wb1.close();
    }

    @Test
    public void bug51158a() throws IOException {
        // create a workbook
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Test Sheet");

            XSSFSheet sheetBack = workbook.getSheetAt(0);

            // committing twice did add the XML twice without clearing the part in between
            sheetBack.commit();

            // ensure that a memory based package part does not have lingering data from previous commit() calls
            if (sheetBack.getPackagePart() instanceof MemoryPackagePart) {
                sheetBack.getPackagePart().clear();
            }

            sheetBack.commit();

            String str = new String(IOUtils.toByteArray(sheetBack.getPackagePart().getInputStream()), "UTF-8");

            assertEquals(1, countMatches(str, "<worksheet"));
        }
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

    @Test
    public void testAddPivotCache() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            CTWorkbook ctWb = wb.getCTWorkbook();
            CTPivotCache pivotCache = wb.addPivotCache("0");
            //Ensures that pivotCaches is initiated
            assertTrue(ctWb.isSetPivotCaches());
            assertSame(pivotCache, ctWb.getPivotCaches().getPivotCacheArray(0));
            assertEquals("0", pivotCache.getId());
        }
    }

    protected void setPivotData(XSSFWorkbook wb){
        XSSFSheet sheet = wb.createSheet();

        Row row1 = sheet.createRow(0);
        // Create a cell and put a value in it.
        Cell cell = row1.createCell(0);
        cell.setCellValue("Names");
        Cell cell2 = row1.createCell(1);
        cell2.setCellValue("#");
        Cell cell7 = row1.createCell(2);
        cell7.setCellValue("Data");

        Row row2 = sheet.createRow(1);
        Cell cell3 = row2.createCell(0);
        cell3.setCellValue("Jan");
        Cell cell4 = row2.createCell(1);
        cell4.setCellValue(10);
        Cell cell8 = row2.createCell(2);
        cell8.setCellValue("Apa");

        Row row3 = sheet.createRow(2);
        Cell cell5 = row3.createCell(0);
        cell5.setCellValue("Ben");
        Cell cell6 = row3.createCell(1);
        cell6.setCellValue(9);
        Cell cell9 = row3.createCell(2);
        cell9.setCellValue("Bepa");

        AreaReference source = wb.getCreationHelper().createAreaReference("A1:B2");
        sheet.createPivotTable(source, new CellReference("H5"));
    }

    @Test
    public void testLoadWorkbookWithPivotTable() throws Exception {
        File file = TempFile.createTempFile("ooxml-pivottable", ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            setPivotData(wb);

            FileOutputStream fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.close();
        }

        try (XSSFWorkbook wb2 = (XSSFWorkbook) WorkbookFactory.create(file)) {
            assertTrue(wb2.getPivotTables().size() == 1);
        }

        assertTrue(file.delete());
    }

    @Test
    public void testAddPivotTableToWorkbookWithLoadedPivotTable() throws Exception {
        File file = TempFile.createTempFile("ooxml-pivottable", ".xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            setPivotData(wb);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
        }

        try (XSSFWorkbook wb2 = (XSSFWorkbook) WorkbookFactory.create(file)) {
            setPivotData(wb2);
            assertTrue(wb2.getPivotTables().size() == 2);
        }

        assertTrue(file.delete());
    }

    @Test
    public void testSetFirstVisibleTab_57373() throws IOException {

        try (Workbook wb = new XSSFWorkbook()) {
            /*Sheet sheet1 =*/
            wb.createSheet();
            Sheet sheet2 = wb.createSheet();
            int idx2 = wb.getSheetIndex(sheet2);
            Sheet sheet3 = wb.createSheet();
            int idx3 = wb.getSheetIndex(sheet3);

            // add many sheets so "first visible" is relevant
            for (int i = 0; i < 30; i++) {
                wb.createSheet();
            }

            wb.setFirstVisibleTab(idx2);
            wb.setActiveSheet(idx3);

            //wb.write(new FileOutputStream(new File("C:\\temp\\test.xlsx")));

            assertEquals(idx2, wb.getFirstVisibleTab());
            assertEquals(idx3, wb.getActiveSheetIndex());

            Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);

            sheet2 = wbBack.getSheetAt(idx2);
            assertNotNull(sheet2);
            sheet3 = wbBack.getSheetAt(idx3);
            assertNotNull(sheet3);
            assertEquals(idx2, wb.getFirstVisibleTab());
            assertEquals(idx3, wb.getActiveSheetIndex());
            wbBack.close();
        }
    }

    /**
     * Tests that we can save a workbook with macros and reload it.
     */
    @Test
    public void testSetVBAProject() throws Exception {
        File file;
        final byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) (i - 128);
        }

        XSSFWorkbook wb1 = new XSSFWorkbook();
        wb1.createSheet();
        wb1.setVBAProject(new ByteArrayInputStream(allBytes));
        file = TempFile.createTempFile("poi-", ".xlsm");
        OutputStream out = new FileOutputStream(file);
        wb1.write(out);
        out.close();
        wb1.close();

        // Check the package contains what we'd expect it to
        OPCPackage pkg = OPCPackage.open(file.toString());
        PackagePart wbPart = pkg.getPart(PackagingURIHelper.createPartName("/xl/workbook.xml"));
        assertTrue(wbPart.hasRelationships());
        final PackageRelationshipCollection relationships = wbPart.getRelationships().getRelationships(XSSFRelation.VBA_MACROS.getRelation());
        assertEquals(1, relationships.size());
        assertEquals(XSSFRelation.VBA_MACROS.getDefaultFileName(), relationships.getRelationship(0).getTargetURI().toString());
        PackagePart vbaPart = pkg.getPart(PackagingURIHelper.createPartName(XSSFRelation.VBA_MACROS.getDefaultFileName()));
        assertNotNull(vbaPart);
        assertFalse(vbaPart.isRelationshipPart());
        assertEquals(XSSFRelation.VBA_MACROS.getContentType(), vbaPart.getContentType());
        final byte[] fromFile = IOUtils.toByteArray(vbaPart.getInputStream());
        assertArrayEquals(allBytes, fromFile);

        // Load back the XSSFWorkbook just to check nothing explodes
        @SuppressWarnings("resource")
        XSSFWorkbook wb2 = new XSSFWorkbook(pkg);
        assertEquals(1, wb2.getNumberOfSheets());
        assertEquals(XSSFWorkbookType.XLSM, wb2.getWorkbookType());
        pkg.close();
    }

    @Test
    public void testBug54399() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("54399.xlsx");

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
          workbook.setSheetName(i, "SheetRenamed" + (i + 1));
        }

        workbook.close();
    }

    /**
     *  Iterator<XSSFSheet> XSSFWorkbook.iterator was committed in r700472 on 2008-09-30
     *  and has been replaced with Iterator<Sheet> XSSFWorkbook.iterator
     *
     *  In order to make code for looping over sheets in workbooks standard, regardless
     *  of the type of workbook (HSSFWorkbook, XSSFWorkbook, SXSSFWorkbook), the previously
     *  available Iterator<XSSFSheet> iterator and Iterator<XSSFSheet> sheetIterator
     *  have been replaced with Iterator<Sheet>  {@link Sheet#iterator} and
     *  Iterator<Sheet> {@link Workbook#sheetIterator}. This makes iterating over sheets in a workbook
     *  similar to iterating over rows in a sheet and cells in a row.
     *
     *  Note: this breaks backwards compatibility! Existing codebases will need to
     *  upgrade their code with either of the following options presented in this test case.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void bug58245_XSSFSheetIterator() throws IOException {
        final XSSFWorkbook wb = new XSSFWorkbook();
        wb.createSheet();

        // =====================================================================
        // Case 1: Existing code uses XSSFSheet for-each loop
        // =====================================================================
        // Original code (no longer valid)
        /*
        for (XSSFSheet sh : wb) {
            sh.createRow(0);
        }
        */

        // Option A:
        for (XSSFSheet sh : (Iterable<XSSFSheet>) (Iterable<? extends Sheet>) wb) {
            sh.createRow(0);
        }

        // Option B (preferred for new code):
        for (Sheet sh : wb) {
            sh.createRow(0);
        }

        // =====================================================================
        // Case 2: Existing code creates an iterator variable
        // =====================================================================
        // Original code (no longer valid)
        /*
        Iterator<XSSFSheet> it = wb.iterator();
        XSSFSheet sh = it.next();
        sh.createRow(0);
        */

        // Option A:
        {
            Iterator<XSSFSheet> it = (Iterator<XSSFSheet>) (Iterator<? extends Sheet>) wb.iterator();
            XSSFSheet sh = it.next();
            sh.createRow(0);
        }

        // Option B (preferred for new code):
        {
            Iterator<Sheet> it = wb.iterator();
            Sheet sh = it.next();
            sh.createRow(0);
        }
        wb.close();
    }

    @Test
    public void testBug56957CloseWorkbook() throws Exception {
        File file = TempFile.createTempFile("TestBug56957_", ".xlsx");
        final Date dateExp = LocaleUtil.getLocaleCalendar(2014, 10, 9).getTime();

        try {
            // as the file is written to, we make a copy before actually working on it
            FileHelper.copyFile(HSSFTestDataSamples.getSampleFile("56957.xlsx"), file);

            assertTrue(file.exists());

            // read-only mode works!
            Workbook workbook = WorkbookFactory.create(OPCPackage.open(file, PackageAccess.READ));
            Date dateAct = workbook.getSheetAt(0).getRow(0).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getDateCellValue();
            assertEquals(dateExp, dateAct);
            workbook.close();
            workbook = null;

            workbook = WorkbookFactory.create(OPCPackage.open(file, PackageAccess.READ));
            dateAct = workbook.getSheetAt(0).getRow(0).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getDateCellValue();
            assertEquals(dateExp, dateAct);
            workbook.close();
            workbook = null;

            // now check read/write mode
            workbook = WorkbookFactory.create(OPCPackage.open(file, PackageAccess.READ_WRITE));
            dateAct = workbook.getSheetAt(0).getRow(0).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getDateCellValue();
            assertEquals(dateExp, dateAct);
            workbook.close();
            workbook = null;

            workbook = WorkbookFactory.create(OPCPackage.open(file, PackageAccess.READ_WRITE));
            dateAct = workbook.getSheetAt(0).getRow(0).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK).getDateCellValue();
            assertEquals(dateExp, dateAct);
            workbook.close();
            workbook = null;
        } finally {
            assertTrue(file.exists());
            assertTrue(file.delete());
        }
    }

    @Test
    public void closeDoesNotModifyWorkbook() throws IOException, InvalidFormatException {
        final String filename = "SampleSS.xlsx";
        final File file = POIDataSamples.getSpreadSheetInstance().getFile(filename);
        Workbook wb;
        
        // Some tests commented out because close() modifies the file
        // See bug 58779
        
        // String
        //wb = new XSSFWorkbook(file.getPath());
        //assertCloseDoesNotModifyFile(filename, wb);
        
        // File
        //wb = new XSSFWorkbook(file);
        //assertCloseDoesNotModifyFile(filename, wb);
        
        // InputStream
        wb = new XSSFWorkbook(new FileInputStream(file));
        assertCloseDoesNotModifyFile(filename, wb);
        
        // OPCPackage
        //wb = new XSSFWorkbook(OPCPackage.open(file));
        //assertCloseDoesNotModifyFile(filename, wb);
    }

    @Test
    public void testCloseBeforeWrite() throws IOException {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet("somesheet");

        // test what happens if we close the Workbook before we write it out
        wb.close();

        try {
            XSSFTestDataSamples.writeOutAndReadBack(wb);
            fail("Expecting IOException here");
        } catch (RuntimeException e) {
            // expected here
            assertTrue("Had: " + e.getCause(), e.getCause() instanceof IOException);
        }
    }

    /**
     * See bug #57840 test data tables
     */
    @Test
    public void getTable() throws IOException {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithTable.xlsx");
       XSSFTable table1 = wb.getTable("Tabella1");
       assertNotNull("Tabella1 was not found in workbook", table1);
       assertEquals("Table name", "Tabella1", table1.getName());
       assertEquals("Sheet name", "Foglio1", table1.getSheetName());

       // Table lookup should be case-insensitive
       assertSame("Case insensitive table name lookup", table1, wb.getTable("TABELLA1"));

       // If workbook does not contain any data tables matching the provided name, getTable should return null
       assertNull("Null table name should not throw NPE", wb.getTable(null));
       assertNull("Should not be able to find non-existent table", wb.getTable("Foglio1"));

       // If a table is added after getTable is called it should still be reachable by XSSFWorkbook.getTable
       // This test makes sure that if any caching is done that getTable never uses a stale cache
       XSSFTable table2 = wb.getSheet("Foglio2").createTable();
       table2.setName("Table2");
       assertSame("Did not find Table2", table2, wb.getTable("Table2"));
       
       // If table name is modified after getTable is called, the table can only be found by its new name
       // This test makes sure that if any caching is done that getTable never uses a stale cache
       table1.setName("Table1");
       assertSame("Did not find Tabella1 renamed to Table1", table1, wb.getTable("TABLE1"));

       wb.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testRemoveSheet() throws IOException {
        // Test removing a sheet maintains the named ranges correctly
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");

        XSSFName sheet1Name = wb.createName();
        sheet1Name.setNameName("name1");
        sheet1Name.setSheetIndex(0);
        sheet1Name.setRefersToFormula("Sheet1!$A$1");

        XSSFName sheet2Name = wb.createName();
        sheet2Name.setNameName("name1");
        sheet2Name.setSheetIndex(1);
        sheet2Name.setRefersToFormula("Sheet2!$A$1");

        assertTrue(wb.getAllNames().contains(sheet1Name));
        assertTrue(wb.getAllNames().contains(sheet2Name));

        assertEquals(2, wb.getNames("name1").size());
        assertEquals(sheet1Name, wb.getNames("name1").get(0));
        assertEquals(sheet2Name, wb.getNames("name1").get(1));

        // Remove sheet1, we should only have sheet2Name now
        wb.removeSheetAt(0);

        assertFalse(wb.getAllNames().contains(sheet1Name));
        assertTrue(wb.getAllNames().contains(sheet2Name));
        assertEquals(1, wb.getNames("name1").size());
        assertEquals(sheet2Name, wb.getNames("name1").get(0));

        // Check by index as well for sanity
        assertEquals(1, wb.getNumberOfNames());
        assertEquals(0, wb.getNameIndex("name1"));
        assertEquals(sheet2Name, wb.getNameAt(0));

        wb.close();
    }
}
