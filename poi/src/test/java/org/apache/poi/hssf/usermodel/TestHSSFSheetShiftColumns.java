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
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestHSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestHSSFSheetShiftColumns() {
        super();
        workbook = new HSSFWorkbook();
        _testDataProvider = HSSFITestDataProvider.instance;
    }

    protected Workbook openWorkbook(String spreadsheetFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }

    protected Workbook getReadBackWorkbook(Workbook wb) {
        return HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)wb);
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void shiftMergedColumnsToMergedColumnsLeft() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void shiftMergedColumnsToMergedColumnsRight() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testBug54524() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testCommentsShifting() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testShiftWithMergedRegions() {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf,
        // so that original method from BaseTestSheetShiftColumns can be executed.
        // After removing, you can re-add 'final' keyword to specification of original method.
    }

    @Override
    @Disabled("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>")
    protected void testShiftHyperlinks() {}

    @Test
    void bug60072ShiftColumnsMovesDrawingAnchors() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);
            for (int j = 0; j < 30; j++) {
                row.createCell(j).setCellValue(j);
            }
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            int picIdx = wb.addPicture("test png data".getBytes(LocaleUtil.CHARSET_1252), HSSFWorkbook.PICTURE_TYPE_PNG);

            // shape left of the shifted area: columns 0-1
            HSSFSimpleShape shape = patriarch.createSimpleShape(new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 1, 3));
            shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
            // textbox at the start of the shifted area: columns 2-10
            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor(0, 0, 0, 0, (short) 2, 1, (short) 10, 8));
            // picture further right: columns 12-15
            HSSFPicture picture = patriarch.createPicture(new HSSFClientAnchor(0, 0, 0, 0, (short) 12, 1, (short) 15, 4), picIdx);

            // insert 3 columns at column 2
            sheet.shiftColumns(2, 29, 3);

            assertAnchorColumns(0, 1, shape.getAnchor());
            assertAnchorColumns(5, 13, textbox.getAnchor());
            assertAnchorColumns(15, 18, picture.getAnchor());

            // move columns 15-18 (the picture) left by 10; the textbox's top-left column is not in the range and stays
            sheet.shiftColumns(15, 18, -10);

            assertAnchorColumns(5, 13, textbox.getAnchor());
            assertAnchorColumns(5, 8, picture.getAnchor());

            try (HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
                HSSFPatriarch patriarchBack = wbBack.getSheetAt(0).getDrawingPatriarch();
                assertNotNull(patriarchBack);
                assertAnchorColumns(0, 1, patriarchBack.getChildren().get(0).getAnchor());
                assertAnchorColumns(5, 13, patriarchBack.getChildren().get(1).getAnchor());
                assertAnchorColumns(5, 8, patriarchBack.getChildren().get(2).getAnchor());
            }
        }
    }

    private static void assertAnchorColumns(int col1, int col2, HSSFAnchor anchor) {
        HSSFClientAnchor clientAnchor = (HSSFClientAnchor) anchor;
        assertEquals(col1, clientAnchor.getCol1(), "col1");
        assertEquals(col2, clientAnchor.getCol2(), "col2");
    }
}
