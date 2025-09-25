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

package org.apache.poi.xssf;

import org.apache.poi.ooxml.ReferenceRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.ss.usermodel.BaseTestCloneSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualPictureProperties;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPictureNonVisual;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestXSSFCloneSheet  extends BaseTestCloneSheet {
    public TestXSSFCloneSheet() {
        super(XSSFITestDataProvider.instance);
    }

    private static final String OTHER_SHEET_NAME = "Another";
    private static final String VALID_SHEET_NAME = "Sheet01";
    private XSSFWorkbook wb;

    @BeforeEach
    void setUp() {
        wb = new XSSFWorkbook();
        wb.createSheet(VALID_SHEET_NAME);
    }

    @Test
    void testCloneSheetIntStringValidName() {
        XSSFSheet cloned = wb.cloneSheet(0, OTHER_SHEET_NAME);
        assertEquals(OTHER_SHEET_NAME, cloned.getSheetName());
        assertEquals(2, wb.getNumberOfSheets());
    }

    @Test
    void testCloneSheetIntStringInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> wb.cloneSheet(0, VALID_SHEET_NAME));
        assertEquals(1, wb.getNumberOfSheets());
    }

    @Test
    void test60512() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("60512.xlsm");

        assertEquals(1, wb.getNumberOfSheets());
        Sheet sheet = wb.cloneSheet(0);
        assertNotNull(sheet);
        assertEquals(2, wb.getNumberOfSheets());


        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);
        wbBack.close();

        wb.close();
    }

    @Test
    void test61605() throws IOException {
        try (Workbook template_wb = XSSFTestDataSamples.openSampleWorkbook("61605.xlsx")) {
            Sheet template_sh = template_wb.getSheetAt(0);
            assertNotNull(template_sh);
            Sheet source_sh = template_wb.cloneSheet(0);
            assertNotNull(source_sh);
        }
    }

    @Test
    void testBug63902() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("chartTitle_withTitle.xlsx")) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFDrawing drawing = sheet.getDrawingPatriarch();
            assertEquals(1, drawing.getCharts().size());

            XSSFSheet sheet2 = workbook.cloneSheet(0, "Sheet 2");
            XSSFDrawing drawing2 = sheet2.getDrawingPatriarch();
            assertEquals(1, drawing2.getCharts().size());
            assertEquals("Sheet 2", sheet2.getSheetName());

            XDDFDataSource<?> data = drawing.getCharts().get(0).getChartSeries().get(0).getSeries(0).getCategoryData();
            XDDFDataSource<?> data2 = drawing2.getCharts().get(0).getChartSeries().get(0).getSeries(0).getCategoryData();
            assertNotEquals(data.getFormula(), data2.getFormula());
            assertEquals(sheet.getSheetName(), data.getFormula().substring(0, data.getFormula().indexOf('!')));
            assertEquals("'Sheet 2'", data2.getFormula().substring(0, data2.getFormula().indexOf('!')));
            assertEquals(
                    data.getFormula().substring(data.getFormula().indexOf('!')),
                    data2.getFormula().substring(data2.getFormula().indexOf('!'))
            );

            Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(workbook, "poi_cloned_sheet_with_chart");
            assertNotNull(wbBack);
            wbBack.close();
        }
    }

    @Test
    void test64759() throws IOException {
        try (Workbook template_wb = XSSFTestDataSamples.openSampleWorkbook("right-to-left.xlsx")) {
            Sheet template_sh = template_wb.getSheetAt(0);
            assertNotNull(template_sh);
            Sheet source_sh = template_wb.cloneSheet(0);
            assertNotNull(source_sh);
        }
    }

    @Test
    void testBug63189() throws IOException {
        try (XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("bug63189.xlsx")) {
            // given
            final String linkRelationType = PackageRelationshipTypes.HYPERLINK_PART;
            final String linkTargetUrl = "#Sheet3!A1";
            final String imageRelationType = PackageRelationshipTypes.IMAGE_PART;
            final String imageTargetUrl = "/xl/media/image1.png";

            XSSFSheet srcSheet = workbook.getSheetAt(0);
            assertEquals("CloneMe", srcSheet.getSheetName());
            XSSFDrawing drawing = srcSheet.getDrawingPatriarch();
            assertNotNull(drawing);
            assertEquals(1, drawing.getShapes().size());
            assertInstanceOf(XSSFPicture.class, drawing.getShapes().get(0));
            XSSFPicture lPic = (XSSFPicture)drawing.getShapes().get(0);
            CTPicture pic = lPic.getCTPicture();
            CTPictureNonVisual nvPicPr = pic.getNvPicPr();
            CTNonVisualDrawingProps cNvPr = nvPicPr.getCNvPr();
            assertTrue(cNvPr.isSetHlinkClick());
            CTHyperlink hlinkClick = cNvPr.getHlinkClick();
            String linkRelId = hlinkClick.getId();

            ReferenceRelationship linkRel = drawing.getReferenceRelationship(linkRelId);
            assertEquals(linkRelationType, linkRel.getRelationshipType());
            assertEquals(linkTargetUrl, linkRel.getUri().toString());

            CTNonVisualPictureProperties cNvPicPr = nvPicPr.getCNvPicPr();
            assertTrue(cNvPicPr.getPicLocks().getNoChangeAspect());

            CTBlipFillProperties blipFill = pic.getBlipFill();
            CTBlip blip = blipFill.getBlip();
            String imageRelId = blip.getEmbed();

            PackageRelationship imageRel = drawing.getRelationPartById(imageRelId).getRelationship();
            assertEquals(imageRelationType, imageRel.getRelationshipType());
            assertEquals(imageTargetUrl, imageRel.getTargetURI().toString());

            // when
            XSSFSheet clonedSheet = workbook.cloneSheet(0);

            // then
            XSSFDrawing drawing2 = clonedSheet.getDrawingPatriarch();
            assertNotNull(drawing2);
            assertEquals(1, drawing2.getShapes().size());
            assertInstanceOf(XSSFPicture.class, drawing2.getShapes().get(0));
            XSSFPicture lPic2 = (XSSFPicture)drawing2.getShapes().get(0);
            CTPicture pic2 = lPic2.getCTPicture();
            CTPictureNonVisual nvPicPr2 = pic2.getNvPicPr();
            CTNonVisualDrawingProps cNvPr2 = nvPicPr2.getCNvPr();
            assertTrue(cNvPr2.isSetHlinkClick());
            CTHyperlink hlinkClick2 = cNvPr2.getHlinkClick();
            String linkRelId2 = hlinkClick2.getId();

            ReferenceRelationship linkRel2 = drawing2.getReferenceRelationship(linkRelId2);
            assertEquals(linkRelationType, linkRel2.getRelationshipType());
            assertEquals(linkTargetUrl, linkRel2.getUri().toString());

            CTNonVisualPictureProperties cNvPicPr2 = nvPicPr2.getCNvPicPr();
            assertTrue(cNvPicPr2.getPicLocks().getNoChangeAspect());

            CTBlipFillProperties blipFill2 = pic2.getBlipFill();
            CTBlip blip2 = blipFill2.getBlip();
            String imageRelId2 = blip2.getEmbed();

            PackageRelationship imageRel2 = drawing2.getRelationPartById(imageRelId2).getRelationship();
            assertEquals(imageRelationType, imageRel2.getRelationshipType());
            assertEquals(imageTargetUrl, imageRel2.getTargetURI().toString());

            assertTrue(drawing2.removeReferenceRelationship(linkRelId2));
            assertFalse(drawing2.removeReferenceRelationship(linkRelId2));
            assertNull(drawing2.getReferenceRelationship(linkRelId2));
        }
    }
}
