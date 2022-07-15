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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.ss.usermodel.BaseTestCloneSheet;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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

}
