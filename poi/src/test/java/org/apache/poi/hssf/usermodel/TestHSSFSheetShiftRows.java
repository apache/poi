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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftRows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class TestHSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestHSSFSheetShiftRows() {
        super(HSSFITestDataProvider.instance);
    }

    @Test
    public void testBug69021() throws IOException {
        try (HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("bug69021.xls")) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 2;
            sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1);
            Row row = sheet.createRow(rowIndex);
            row.createCell(0).setCellValue("switch");
            HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(workbook);
            assertNotNull(wbBack);
        }
    }

    @Test
    void bug60072ShiftRowsMovesDrawingAnchors() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            for (int i = 0; i < 30; i++) {
                sheet.createRow(i).createCell(0).setCellValue(i);
            }
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            int picIdx = wb.addPicture("test png data".getBytes(LocaleUtil.CHARSET_1252), HSSFWorkbook.PICTURE_TYPE_PNG);

            // shape above the shifted area: rows 0-1
            HSSFSimpleShape shape = patriarch.createSimpleShape(new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 3, 1));
            shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
            // textbox at the start of the shifted area: rows 2-10
            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor(0, 0, 0, 0, (short) 1, 2, (short) 8, 10));
            // picture further down: rows 12-15
            HSSFPicture picture = patriarch.createPicture(new HSSFClientAnchor(0, 0, 0, 0, (short) 1, 12, (short) 4, 15), picIdx);
            // a comment on row 20 is moved with its cell, not as a shape
            HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 20, (short) 5, 22));
            comment.setRow(20);
            comment.setColumn(0);

            // insert 3 rows at row 2
            sheet.shiftRows(2, sheet.getLastRowNum(), 3);

            assertAnchorRows(0, 1, shape.getAnchor());
            assertAnchorRows(5, 13, textbox.getAnchor());
            assertAnchorRows(15, 18, picture.getAnchor());
            assertEquals(23, comment.getRow());

            // move rows 15-18 (the picture) up by 10; the textbox's top-left row is not in the range and stays
            sheet.shiftRows(15, 18, -10);

            assertAnchorRows(5, 13, textbox.getAnchor());
            assertAnchorRows(5, 8, picture.getAnchor());
            assertEquals(23, comment.getRow());

            try (HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                HSSFPatriarch patriarchBack = wbBack.getSheetAt(0).getDrawingPatriarch();
                assertNotNull(patriarchBack);
                assertAnchorRows(0, 1, patriarchBack.getChildren().get(0).getAnchor());
                assertAnchorRows(5, 13, patriarchBack.getChildren().get(1).getAnchor());
                assertAnchorRows(5, 8, patriarchBack.getChildren().get(2).getAnchor());
            }
        }
    }

    private static void assertAnchorRows(int row1, int row2, HSSFAnchor anchor) {
        HSSFClientAnchor clientAnchor = (HSSFClientAnchor) anchor;
        assertEquals(row1, clientAnchor.getRow1(), "row1");
        assertEquals(row2, clientAnchor.getRow2(), "row2");
    }
}
