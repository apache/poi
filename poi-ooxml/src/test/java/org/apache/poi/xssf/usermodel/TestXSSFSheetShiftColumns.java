/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xssf.usermodel;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestXSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestXSSFSheetShiftColumns() {
        super(); 
        workbook = new XSSFWorkbook();
        _testDataProvider = XSSFITestDataProvider.instance; 
    }

    protected Workbook openWorkbook(String spreadsheetFileName) throws IOException {
        return XSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }

    protected Workbook getReadBackWorkbook(Workbook wb) {
        return XSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    @Test
    public void testBug69154() throws Exception {
        // this does not appear to work for HSSF but let's get it working for XSSF anyway
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < 4; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < 6; j++) {
                    String value = new CellAddress(i, j).formatAsString();
                    row.createCell(j).setCellValue(value);
                }
            }
            final int firstRow = 1; // worked with 0, but failed with 1!
            final int secondRow = firstRow + 1;
            sheet.addMergedRegion(new CellRangeAddress(firstRow, secondRow, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(firstRow, firstRow, 1, 2));
            sheet.addMergedRegion(new CellRangeAddress(firstRow, secondRow, 3, 3));
            assertEquals(3, sheet.getNumMergedRegions());
            sheet.shiftColumns(2, 5, -1);
            assertEquals(2, sheet.getNumMergedRegions());
        }
    }

    @Test
    void bug60072ShiftColumnsMovesDrawingAnchors() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            for (int j = 0; j < 30; j++) {
                row.createCell(j).setCellValue(j);
            }
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            int picIdx = wb.addPicture("test jpeg data".getBytes(LocaleUtil.CHARSET_1252), XSSFWorkbook.PICTURE_TYPE_JPEG);

            // shape left of the shifted area: columns 0-1
            XSSFSimpleShape shape = drawing.createSimpleShape(new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 1, 3));
            // chart at the start of the shifted area: columns 2-10
            XSSFChart chart = drawing.createChart(new XSSFClientAnchor(0, 0, 0, 0, 2, 1, 10, 8));
            // picture further right: columns 12-15
            XSSFPicture picture = drawing.createPicture(new XSSFClientAnchor(0, 0, 0, 0, 12, 1, 15, 4), picIdx);

            // insert 3 columns at column 2
            sheet.shiftColumns(2, 29, 3);

            assertAnchorColumns(0, 1, shape.getAnchor());
            assertAnchorColumns(5, 13, chart.getGraphicFrame().getAnchor());
            assertAnchorColumns(15, 18, picture.getAnchor());

            // move columns 15-18 (the picture) left by 10; the chart's top-left column is not in the range and stays
            sheet.shiftColumns(15, 18, -10);

            assertAnchorColumns(5, 13, chart.getGraphicFrame().getAnchor());
            assertAnchorColumns(5, 8, picture.getAnchor());

            try (XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                XSSFDrawing drawingBack = wbBack.getSheetAt(0).getDrawingPatriarch();
                assertNotNull(drawingBack);
                assertAnchorColumns(0, 1, drawingBack.getShapes().get(0).getAnchor());
                assertAnchorColumns(5, 13, drawingBack.getShapes().get(1).getAnchor());
                assertAnchorColumns(5, 8, drawingBack.getShapes().get(2).getAnchor());
            }
        }
    }

    private static void assertAnchorColumns(int col1, int col2, XSSFAnchor anchor) {
        XSSFClientAnchor clientAnchor = (XSSFClientAnchor) anchor;
        assertEquals(col1, clientAnchor.getCol1(), "col1");
        assertEquals(col2, clientAnchor.getCol2(), "col2");
    }
}
