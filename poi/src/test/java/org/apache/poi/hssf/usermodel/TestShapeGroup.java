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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.POITestCase;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.EscherAggregate;
import org.junit.jupiter.api.Test;

class TestShapeGroup {

    @Test
    void testSetGetCoordinates() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            HSSFPatriarch patriarch = sh.createDrawingPatriarch();
            HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
            assertEquals(0, group.getX1());
            assertEquals(0, group.getY1());
            assertEquals(1023, group.getX2());
            assertEquals(255, group.getY2());

            group.setCoordinates(1, 2, 3, 4);

            assertEquals(1, group.getX1());
            assertEquals(2, group.getY1());
            assertEquals(3, group.getX2());
            assertEquals(4, group.getY2());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                patriarch = sh.getDrawingPatriarch();

                group = (HSSFShapeGroup) patriarch.getChildren().get(0);
                assertEquals(1, group.getX1());
                assertEquals(2, group.getY1());
                assertEquals(3, group.getX2());
                assertEquals(4, group.getY2());
            }
        }
    }

    @Test
    void testAddToExistingFile() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sh = wb1.createSheet();
            HSSFPatriarch patriarch = sh.createDrawingPatriarch();
            HSSFShapeGroup group1 = patriarch.createGroup(new HSSFClientAnchor());
            HSSFShapeGroup group2 = patriarch.createGroup(new HSSFClientAnchor());

            group1.setCoordinates(1, 2, 3, 4);
            group2.setCoordinates(5, 6, 7, 8);

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sh = wb2.getSheetAt(0);
                patriarch = sh.getDrawingPatriarch();

                assertEquals(2, patriarch.getChildren().size());

                HSSFShapeGroup group3 = patriarch.createGroup(new HSSFClientAnchor());
                group3.setCoordinates(9, 10, 11, 12);

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sh = wb3.getSheetAt(0);
                    patriarch = sh.getDrawingPatriarch();

                    assertEquals(3, patriarch.getChildren().size());
                }
            }
        }
    }

    @Test
    void testModify() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {

            // create a sheet with a text box
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFShapeGroup group1 = patriarch.createGroup(new
                    HSSFClientAnchor(0, 0, 0, 0,
                    (short) 0, 0, (short) 15, 25));
            group1.setCoordinates(0, 0, 792, 612);

            HSSFTextbox textbox1 = group1.createTextbox(new
                    HSSFChildAnchor(100, 100, 300, 300));
            HSSFRichTextString rt1 = new HSSFRichTextString("Hello, World!");
            textbox1.setString(rt1);

            // write, read back and check that our text box is there
            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();
                assertEquals(1, patriarch.getChildren().size());

                group1 = (HSSFShapeGroup) patriarch.getChildren().get(0);
                assertEquals(1, group1.getChildren().size());
                textbox1 = (HSSFTextbox) group1.getChildren().get(0);
                assertEquals("Hello, World!", textbox1.getString().getString());

                // modify anchor
                assertEquals(new HSSFChildAnchor(100, 100, 300, 300), textbox1.getAnchor());
                HSSFChildAnchor newAnchor = new HSSFChildAnchor(200, 200, 400, 400);
                textbox1.setAnchor(newAnchor);
                // modify text
                textbox1.setString(new HSSFRichTextString("Hello, World! (modified)"));

                // add a new text box
                HSSFTextbox textbox2 = group1.createTextbox(new HSSFChildAnchor(400, 400, 600, 600));
                HSSFRichTextString rt2 = new HSSFRichTextString("Hello, World-2");
                textbox2.setString(rt2);
                assertEquals(2, group1.getChildren().size());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sheet = wb3.getSheetAt(0);
                    patriarch = sheet.getDrawingPatriarch();
                    assertEquals(1, patriarch.getChildren().size());

                    group1 = (HSSFShapeGroup) patriarch.getChildren().get(0);
                    assertEquals(2, group1.getChildren().size());
                    textbox1 = (HSSFTextbox) group1.getChildren().get(0);
                    assertEquals("Hello, World! (modified)", textbox1.getString().getString());
                    assertEquals(new HSSFChildAnchor(200, 200, 400, 400), textbox1.getAnchor());

                    textbox2 = (HSSFTextbox) group1.getChildren().get(1);
                    assertEquals("Hello, World-2", textbox2.getString().getString());
                    assertEquals(new HSSFChildAnchor(400, 400, 600, 600), textbox2.getAnchor());

                    try (HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb3)) {
                        sheet = wb4.getSheetAt(0);
                        patriarch = sheet.getDrawingPatriarch();
                        group1 = (HSSFShapeGroup) patriarch.getChildren().get(0);
                        textbox1 = (HSSFTextbox) group1.getChildren().get(0);
                        textbox2 = (HSSFTextbox) group1.getChildren().get(1);
                        HSSFTextbox textbox3 = group1.createTextbox(new HSSFChildAnchor(400, 200, 600, 400));
                        HSSFRichTextString rt3 = new HSSFRichTextString("Hello, World-3");
                        textbox3.setString(rt3);
                    }
                }
            }
        }
    }

    @Test
    void testAddShapesToGroup() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {

            // create a sheet with a text box
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
            int index = wb1.addPicture(new byte[]{1, 2, 3}, HSSFWorkbook.PICTURE_TYPE_JPEG);
            group.createPicture(new HSSFChildAnchor(), index);
            HSSFPolygon polygon = group.createPolygon(new HSSFChildAnchor());
            polygon.setPoints(new int[]{1, 100, 1}, new int[]{1, 50, 100});
            group.createTextbox(new HSSFChildAnchor());
            group.createShape(new HSSFChildAnchor());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();
                assertEquals(1, patriarch.getChildren().size());

                assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
                group = (HSSFShapeGroup) patriarch.getChildren().get(0);

                assertEquals(4, group.getChildren().size());

                assertTrue(group.getChildren().get(0) instanceof HSSFPicture);
                assertTrue(group.getChildren().get(1) instanceof HSSFPolygon);
                assertTrue(group.getChildren().get(2) instanceof HSSFTextbox);
                assertTrue(group.getChildren().get(3) instanceof HSSFSimpleShape);

                HSSFShapeGroup group2 = patriarch.createGroup(new HSSFClientAnchor());

                index = wb2.addPicture(new byte[]{2, 2, 2}, HSSFWorkbook.PICTURE_TYPE_JPEG);
                group2.createPicture(new HSSFChildAnchor(), index);
                polygon = group2.createPolygon(new HSSFChildAnchor());
                polygon.setPoints(new int[]{1, 100, 1}, new int[]{1, 50, 100});
                group2.createTextbox(new HSSFChildAnchor());
                group2.createShape(new HSSFChildAnchor());
                group2.createShape(new HSSFChildAnchor());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sheet = wb3.getSheetAt(0);
                    patriarch = sheet.getDrawingPatriarch();
                    assertEquals(2, patriarch.getChildren().size());

                    group = (HSSFShapeGroup) patriarch.getChildren().get(1);

                    assertEquals(5, group.getChildren().size());

                    assertTrue(group.getChildren().get(0) instanceof HSSFPicture);
                    assertTrue(group.getChildren().get(1) instanceof HSSFPolygon);
                    assertTrue(group.getChildren().get(2) instanceof HSSFTextbox);
                    assertTrue(group.getChildren().get(3) instanceof HSSFSimpleShape);
                    assertTrue(group.getChildren().get(4) instanceof HSSFSimpleShape);

                    group.getShapeId();
                }
            }
        }
    }

    @Test
    void testSpgrRecord() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            // create a sheet with a text box
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
            assertSame(((EscherContainerRecord) group.getEscherContainer().getChild(0)).getChildById(EscherSpgrRecord.RECORD_ID), getSpgrRecord(group));
        }
    }

    private static EscherSpgrRecord getSpgrRecord(HSSFShapeGroup group) {
        return POITestCase.getFieldValue(HSSFShapeGroup.class, group, EscherSpgrRecord.class, "_spgrRecord");
    }

    @Test
    void testClearShapes() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());

            group.createShape(new HSSFChildAnchor());
            group.createShape(new HSSFChildAnchor());

            EscherAggregate agg = HSSFTestHelper.getEscherAggregate(patriarch);

            assertEquals(5, agg.getShapeToObjMapping().size());
            assertEquals(0, agg.getTailRecords().size());
            assertEquals(2, group.getChildren().size());

            group.clear();

            assertEquals(1, agg.getShapeToObjMapping().size());
            assertEquals(0, agg.getTailRecords().size());
            assertEquals(0, group.getChildren().size());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();

                group = (HSSFShapeGroup) patriarch.getChildren().get(0);

                assertEquals(1, agg.getShapeToObjMapping().size());
                assertEquals(0, agg.getTailRecords().size());
                assertEquals(0, group.getChildren().size());
            }
        }
    }
}
