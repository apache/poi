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

package org.apache.poi.hssf.model;

import static org.apache.poi.hssf.usermodel.HSSFTestHelper.getEscherAggregate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import org.apache.poi.ddf.EscherBoolProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperty;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.usermodel.HSSFAnchor;
import org.apache.poi.hssf.usermodel.HSSFChildAnchor;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPolygon;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFShapeGroup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTestHelper;
import org.apache.poi.hssf.usermodel.HSSFTextbox;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;


/**
 * Test escher drawing
 *
 * optionally the system setting "poi.deserialize.escher" can be set to {@code true}
 */
class TestDrawingShapes {
    /**
     * HSSFShape tree bust be built correctly
     * Check file with such records structure:
     * -patriarch
     * --shape
     * --group
     * ---group
     * ----shape
     * ----shape
     * ---shape
     * ---group
     * ----shape
     * ----shape
     */
    @Test
    void testDrawingGroups() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("groups");
            HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
            assertEquals(2, patriarch.getChildren().size());
            HSSFShapeGroup group = (HSSFShapeGroup) patriarch.getChildren().get(1);
            assertEquals(3, group.getChildren().size());
            HSSFShapeGroup group1 = (HSSFShapeGroup) group.getChildren().get(0);
            assertEquals(2, group1.getChildren().size());
            group1 = (HSSFShapeGroup) group.getChildren().get(2);
            assertEquals(2, group1.getChildren().size());
        }
    }

    @Test
    void testHSSFShapeCompatibility() {
        HSSFSimpleShape shape = new HSSFSimpleShape(null, new HSSFClientAnchor());
        shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
        assertEquals(0x08000040, shape.getLineStyleColor());
        assertEquals(0x08000009, shape.getFillColor());
        assertEquals(HSSFShape.LINEWIDTH_DEFAULT, shape.getLineWidth());
        assertEquals(HSSFShape.LINESTYLE_SOLID, shape.getLineStyle());
        assertFalse(shape.isNoFill());

        EscherOptRecord opt = shape.getOptRecord();

        assertEquals(7, opt.getEscherProperties().size());
        assertNotEquals(0, ((EscherSimpleProperty) opt.lookup(EscherPropertyTypes.GROUPSHAPE__FLAGS)).getPropertyValue());
        assertTrue(((EscherBoolProperty) opt.lookup(EscherPropertyTypes.LINESTYLE__NOLINEDRAWDASH)).isTrue());
        assertEquals(0x00000004, ((EscherSimpleProperty) opt.lookup(EscherPropertyTypes.GEOMETRY__SHAPEPATH)).getPropertyValue());
        assertNull(opt.lookup(EscherPropertyTypes.TEXT__SIZE_TEXT_TO_FIT_SHAPE));
    }

    @Test
    void testDefaultPictureSettings() {
        HSSFPicture picture = new HSSFPicture(null, new HSSFClientAnchor());
        assertEquals(HSSFShape.LINEWIDTH_DEFAULT, picture.getLineWidth());
        assertEquals(HSSFShape.FILL__FILLCOLOR_DEFAULT, picture.getFillColor());
        assertEquals(HSSFShape.LINESTYLE_NONE, picture.getLineStyle());
        assertEquals(HSSFShape.LINESTYLE__COLOR_DEFAULT, picture.getLineStyleColor());
        assertFalse(picture.isNoFill());
        // not set yet
        assertEquals(-1, picture.getPictureIndex());
    }

    /**
     * No NullPointerException should appear
     */
    @Test
    void testDefaultSettingsWithEmptyContainer() {
        EscherContainerRecord container = new EscherContainerRecord();
        EscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        container.addChildRecord(opt);
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord cod = new CommonObjectDataSubRecord();
        cod.setObjectType(HSSFSimpleShape.OBJECT_TYPE_PICTURE);
        obj.addSubRecord(cod);
        HSSFPicture picture = new HSSFPicture(container, obj);

        assertEquals(HSSFShape.LINEWIDTH_DEFAULT, picture.getLineWidth());
        assertEquals(HSSFShape.FILL__FILLCOLOR_DEFAULT, picture.getFillColor());
        assertEquals(HSSFShape.LINESTYLE_DEFAULT, picture.getLineStyle());
        assertEquals(HSSFShape.LINESTYLE__COLOR_DEFAULT, picture.getLineStyleColor());
        assertEquals(HSSFShape.NO_FILL_DEFAULT, picture.isNoFill());
        //not set yet
        assertEquals(-1, picture.getPictureIndex());
    }

    /**
     * create a rectangle, save the workbook, read back and verify that all shape properties are there
     */
    @Test
    void testReadWriteRectangle() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();

            HSSFPatriarch drawing = sheet.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 50, 50, (short) 2, 2, (short) 4, 4);
            anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
            assertEquals(AnchorType.MOVE_DONT_RESIZE, anchor.getAnchorType());
            anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
            assertEquals(AnchorType.MOVE_DONT_RESIZE, anchor.getAnchorType());

            HSSFSimpleShape rectangle = drawing.createSimpleShape(anchor);
            rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
            rectangle.setLineWidth(10000);
            rectangle.setFillColor(777);
            assertEquals(777, rectangle.getFillColor());
            assertEquals(10000, rectangle.getLineWidth());
            rectangle.setLineStyle(10);
            assertEquals(10, rectangle.getLineStyle());
            assertEquals(HSSFSimpleShape.WRAP_SQUARE, rectangle.getWrapText());
            rectangle.setLineStyleColor(1111);
            rectangle.setNoFill(true);
            rectangle.setWrapText(HSSFSimpleShape.WRAP_NONE);
            rectangle.setString(new HSSFRichTextString("teeeest"));
            assertEquals(1111, rectangle.getLineStyleColor());
            EscherContainerRecord escherContainer = HSSFTestHelper.getEscherContainer(rectangle);
            assertNotNull(escherContainer);
            EscherRecord childById = escherContainer.getChildById(EscherOptRecord.RECORD_ID);
            assertNotNull(childById);
            EscherProperty lookup = ((EscherOptRecord) childById).lookup(EscherPropertyTypes.TEXT__TEXTID);
            assertNotNull(lookup);
            assertEquals("teeeest".hashCode(), ((EscherSimpleProperty) lookup).getPropertyValue());
            assertTrue(rectangle.isNoFill());
            assertEquals(HSSFSimpleShape.WRAP_NONE, rectangle.getWrapText());
            assertEquals("teeeest", rectangle.getString().getString());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                drawing = sheet.getDrawingPatriarch();
                assertEquals(1, drawing.getChildren().size());

                HSSFSimpleShape rectangle2 = (HSSFSimpleShape) drawing.getChildren().get(0);
                assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, rectangle2.getShapeType());
                assertEquals(10000, rectangle2.getLineWidth());
                assertEquals(10, rectangle2.getLineStyle());
                assertEquals(anchor, rectangle2.getAnchor());
                assertEquals(1111, rectangle2.getLineStyleColor());
                assertEquals(777, rectangle2.getFillColor());
                assertTrue(rectangle2.isNoFill());
                assertEquals("teeeest", rectangle2.getString().getString());
                assertEquals(HSSFSimpleShape.WRAP_NONE, rectangle.getWrapText());

                rectangle2.setFillColor(3333);
                rectangle2.setLineStyle(9);
                rectangle2.setLineStyleColor(4444);
                rectangle2.setNoFill(false);
                rectangle2.setLineWidth(77);
                rectangle2.getAnchor().setDx1(2);
                rectangle2.getAnchor().setDx2(3);
                rectangle2.getAnchor().setDy1(4);
                rectangle2.getAnchor().setDy2(5);
                rectangle.setWrapText(HSSFSimpleShape.WRAP_BY_POINTS);
                rectangle2.setString(new HSSFRichTextString("test22"));

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sheet = wb3.getSheetAt(0);
                    drawing = sheet.getDrawingPatriarch();
                    assertEquals(1, drawing.getChildren().size());
                    rectangle2 = (HSSFSimpleShape) drawing.getChildren().get(0);
                    assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, rectangle2.getShapeType());
                    assertEquals(HSSFSimpleShape.WRAP_BY_POINTS, rectangle.getWrapText());
                    assertEquals(77, rectangle2.getLineWidth());
                    assertEquals(9, rectangle2.getLineStyle());
                    assertEquals(4444, rectangle2.getLineStyleColor());
                    assertEquals(3333, rectangle2.getFillColor());
                    assertEquals(2, rectangle2.getAnchor().getDx1());
                    assertEquals(3, rectangle2.getAnchor().getDx2());
                    assertEquals(4, rectangle2.getAnchor().getDy1());
                    assertEquals(5, rectangle2.getAnchor().getDy2());
                    assertFalse(rectangle2.isNoFill());
                    assertEquals("test22", rectangle2.getString().getString());

                    HSSFSimpleShape rect3 = drawing.createSimpleShape(new HSSFClientAnchor());
                    rect3.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
                    try (HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb3)) {
                        drawing = wb4.getSheetAt(0).getDrawingPatriarch();
                        assertEquals(2, drawing.getChildren().size());
                    }
                }
            }
        }
    }

    @Test
    void testReadExistingImage() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("pictures");
            HSSFPatriarch drawing = sheet.getDrawingPatriarch();
            assertEquals(1, drawing.getChildren().size());
            HSSFPicture picture = (HSSFPicture) drawing.getChildren().get(0);

            assertEquals(2, picture.getPictureIndex());
            assertEquals(HSSFShape.LINESTYLE__COLOR_DEFAULT, picture.getLineStyleColor());
            assertEquals(0x5DC943, picture.getFillColor());
            assertEquals(HSSFShape.LINEWIDTH_DEFAULT, picture.getLineWidth());
            assertEquals(HSSFShape.LINESTYLE_DEFAULT, picture.getLineStyle());
            assertFalse(picture.isNoFill());

            picture.setPictureIndex(2);
            assertEquals(2, picture.getPictureIndex());
        }
    }


    /* assert shape properties when reading shapes from an existing workbook */
    @Test
    void testReadExistingRectangle() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("rectangles");
            HSSFPatriarch drawing = sheet.getDrawingPatriarch();
            assertEquals(1, drawing.getChildren().size());

            HSSFSimpleShape shape = (HSSFSimpleShape) drawing.getChildren().get(0);
            assertFalse(shape.isNoFill());
            assertEquals(HSSFShape.LINESTYLE_DASHDOTGEL, shape.getLineStyle());
            assertEquals(0x616161, shape.getLineStyleColor());
            assertEquals(0x2CE03D, shape.getFillColor());
            assertEquals(HSSFShape.LINEWIDTH_ONE_PT * 2, shape.getLineWidth());
            assertEquals("POItest", shape.getString().getString());
            assertEquals(27, shape.getRotationDegree());
        }
    }

    @Test
    void testShapeIds() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet1 = wb1.createSheet();
            HSSFPatriarch patriarch1 = sheet1.createDrawingPatriarch();
            for (int i = 0; i < 2; i++) {
                patriarch1.createSimpleShape(new HSSFClientAnchor());
            }

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet1 = wb2.getSheetAt(0);
                patriarch1 = sheet1.getDrawingPatriarch();

                EscherAggregate agg1 = getEscherAggregate(patriarch1);
                // last shape ID cached in EscherDgRecord
                EscherDgRecord dg1 = agg1.getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);
                assertNotNull(dg1);
                assertEquals(1026, dg1.getLastMSOSPID());

                // iterate over shapes and check shapeId
                EscherContainerRecord spgrContainer =
                    agg1.getEscherContainer().getChildContainers().get(0);
                // root spContainer + 2 spContainers for shapes
                assertEquals(3, spgrContainer.getChildCount());

                EscherSpRecord sp0 =
                    ((EscherContainerRecord) spgrContainer.getChild(0)).getChildById(EscherSpRecord.RECORD_ID);
                assertNotNull(sp0);
                assertEquals(1024, sp0.getShapeId());

                EscherSpRecord sp1 =
                    ((EscherContainerRecord) spgrContainer.getChild(1)).getChildById(EscherSpRecord.RECORD_ID);
                assertNotNull(sp1);
                assertEquals(1025, sp1.getShapeId());

                EscherSpRecord sp2 =
                    ((EscherContainerRecord) spgrContainer.getChild(2)).getChildById(EscherSpRecord.RECORD_ID);
                assertNotNull(sp2);
                assertEquals(1026, sp2.getShapeId());
            }
        }
    }

    /**
     * Test get new id for shapes from existing file
     * File already have for 1 shape on each sheet, because document must contain EscherDgRecord for each sheet
     */
    @Test
    void testAllocateNewIds() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("empty.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFPatriarch patriarch = sheet.getDrawingPatriarch();

            // 2048 - main SpContainer id
            // 2049 - existing shape id
            assertEquals(2050, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(2051, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(2052, HSSFTestHelper.allocateNewShapeId(patriarch));

            sheet = wb.getSheetAt(1);
            patriarch = sheet.getDrawingPatriarch();

            // 3072 - main SpContainer id
            // 3073 - existing shape id
            assertEquals(3074, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(3075, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(3076, HSSFTestHelper.allocateNewShapeId(patriarch));


            sheet = wb.getSheetAt(2);
            patriarch = sheet.getDrawingPatriarch();

            assertEquals(1026, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(1027, HSSFTestHelper.allocateNewShapeId(patriarch));
            assertEquals(1028, HSSFTestHelper.allocateNewShapeId(patriarch));
        }
    }

    @Test
    void testOpt() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            // create a sheet with a text box
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
            EscherOptRecord opt1 = HSSFTestHelper.getOptRecord(textbox);
            EscherOptRecord opt2 = HSSFTestHelper.getEscherContainer(textbox).getChildById(EscherOptRecord.RECORD_ID);
            assertSame(opt1, opt2);
        }
    }

    @Test
    void testCorrectOrderInOptRecord() throws IOException{
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
            EscherOptRecord opt = HSSFTestHelper.getOptRecord(textbox);

            String opt1Str = opt.toXml();

            textbox.setFillColor(textbox.getFillColor());
            EscherContainerRecord container = HSSFTestHelper.getEscherContainer(textbox);
            EscherOptRecord optRecord = container.getChildById(EscherOptRecord.RECORD_ID);
            assertNotNull(optRecord);
            assertEquals(opt1Str, optRecord.toXml());
            textbox.setLineStyle(textbox.getLineStyle());
            assertEquals(opt1Str, optRecord.toXml());
            textbox.setLineWidth(textbox.getLineWidth());
            assertEquals(opt1Str, optRecord.toXml());
            textbox.setLineStyleColor(textbox.getLineStyleColor());
            assertEquals(opt1Str, optRecord.toXml());
        }
    }

    @Test
    void testDgRecordNumShapes() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            EscherAggregate aggregate = getEscherAggregate(patriarch);
            EscherDgRecord dgRecord = (EscherDgRecord) aggregate.getEscherRecord(0).getChild(0);
            assertEquals(1, dgRecord.getNumShapes());
        }
    }

    @Test
    void testTextForSimpleShape() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFSimpleShape shape = patriarch.createSimpleShape(new HSSFClientAnchor());
            shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

            EscherAggregate agg = getEscherAggregate(patriarch);
            assertEquals(2, agg.getShapeToObjMapping().size());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();

                shape = (HSSFSimpleShape) patriarch.getChildren().get(0);

                agg = getEscherAggregate(patriarch);
                assertEquals(2, agg.getShapeToObjMapping().size());

                shape.setString(new HSSFRichTextString("string1"));
                assertEquals("string1", shape.getString().getString());

                assertNotNull(HSSFTestHelper.getEscherContainer(shape).getChildById(EscherTextboxRecord.RECORD_ID));
                assertEquals(2, agg.getShapeToObjMapping().size());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
                    HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb3)) {
                    sheet = wb4.getSheetAt(0);
                    patriarch = sheet.getDrawingPatriarch();
                    shape = (HSSFSimpleShape) patriarch.getChildren().get(0);
                    assertNotNull(HSSFTestHelper.getTextObjRecord(shape));
                    assertEquals("string1", shape.getString().getString());
                    assertNotNull(HSSFTestHelper.getEscherContainer(shape).getChildById(EscherTextboxRecord.RECORD_ID));
                    assertEquals(2, agg.getShapeToObjMapping().size());
                }
            }

        }
    }

    @Test
    void testRemoveShapes() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet1 = wb1.createSheet();
            HSSFPatriarch patriarch1 = sheet1.createDrawingPatriarch();

            HSSFSimpleShape rectangle = patriarch1.createSimpleShape(new HSSFClientAnchor());
            rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

            int idx = wb1.addPicture(new byte[]{1, 2, 3}, Workbook.PICTURE_TYPE_JPEG);
            patriarch1.createPicture(new HSSFClientAnchor(), idx);

            patriarch1.createCellComment(new HSSFClientAnchor());

            HSSFPolygon polygon1 = patriarch1.createPolygon(new HSSFClientAnchor());
            polygon1.setPoints(new int[]{1, 2}, new int[]{2, 3});

            patriarch1.createTextbox(new HSSFClientAnchor());

            HSSFShapeGroup group1 = patriarch1.createGroup(new HSSFClientAnchor());
            group1.createTextbox(new HSSFChildAnchor());
            group1.createPicture(new HSSFChildAnchor(), idx);

            assertEquals(6, patriarch1.getChildren().size());
            assertEquals(2, group1.getChildren().size());

            assertEquals(12, getEscherAggregate(patriarch1).getShapeToObjMapping().size());
            assertEquals(1, getEscherAggregate(patriarch1).getTailRecords().size());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                HSSFSheet sheet2 = wb2.getSheetAt(0);
                HSSFPatriarch patriarch2 = sheet2.getDrawingPatriarch();

                assertEquals(12, getEscherAggregate(patriarch2).getShapeToObjMapping().size());
                assertEquals(1, getEscherAggregate(patriarch2).getTailRecords().size());

                assertEquals(6, patriarch2.getChildren().size());

                HSSFShapeGroup group2 = (HSSFShapeGroup) patriarch2.getChildren().get(5);
                group2.removeShape(group2.getChildren().get(0));

                assertEquals(10, getEscherAggregate(patriarch2).getShapeToObjMapping().size());
                assertEquals(1, getEscherAggregate(patriarch2).getTailRecords().size());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    HSSFSheet sheet3 = wb3.getSheetAt(0);
                    HSSFPatriarch patriarch3 = sheet3.getDrawingPatriarch();

                    assertEquals(10, getEscherAggregate(patriarch3).getShapeToObjMapping().size());
                    assertEquals(1, getEscherAggregate(patriarch3).getTailRecords().size());

                    HSSFShapeGroup group3 = (HSSFShapeGroup) patriarch3.getChildren().get(5);
                    patriarch3.removeShape(group3);

                    assertEquals(8, getEscherAggregate(patriarch3).getShapeToObjMapping().size());
                    assertEquals(1, getEscherAggregate(patriarch3).getTailRecords().size());

                    try (HSSFWorkbook wb4 = HSSFTestDataSamples.writeOutAndReadBack(wb3)) {
                        HSSFSheet sheet4 = wb4.getSheetAt(0);
                        HSSFPatriarch patriarch4 = sheet4.getDrawingPatriarch();

                        assertEquals(8, getEscherAggregate(patriarch4).getShapeToObjMapping().size());
                        assertEquals(1, getEscherAggregate(patriarch4).getTailRecords().size());
                        assertEquals(5, patriarch4.getChildren().size());

                        HSSFShape shape4 = patriarch4.getChildren().get(0);
                        patriarch4.removeShape(shape4);

                        assertEquals(6, getEscherAggregate(patriarch4).getShapeToObjMapping().size());
                        assertEquals(1, getEscherAggregate(patriarch4).getTailRecords().size());
                        assertEquals(4, patriarch4.getChildren().size());

                        try (HSSFWorkbook wb5 = HSSFTestDataSamples.writeOutAndReadBack(wb4)) {
                            HSSFSheet sheet5 = wb5.getSheetAt(0);
                            HSSFPatriarch patriarch5 = sheet5.getDrawingPatriarch();

                            assertEquals(6, getEscherAggregate(patriarch5).getShapeToObjMapping().size());
                            assertEquals(1, getEscherAggregate(patriarch5).getTailRecords().size());
                            assertEquals(4, patriarch5.getChildren().size());

                            HSSFPicture picture5 = (HSSFPicture) patriarch5.getChildren().get(0);
                            patriarch5.removeShape(picture5);

                            assertEquals(5, getEscherAggregate(patriarch5).getShapeToObjMapping().size());
                            assertEquals(1, getEscherAggregate(patriarch5).getTailRecords().size());
                            assertEquals(3, patriarch5.getChildren().size());

                            try (HSSFWorkbook wb6 = HSSFTestDataSamples.writeOutAndReadBack(wb5)) {
                                HSSFSheet sheet6 = wb6.getSheetAt(0);
                                HSSFPatriarch patriarch6 = sheet6.getDrawingPatriarch();

                                assertEquals(5, getEscherAggregate(patriarch6).getShapeToObjMapping().size());
                                assertEquals(1, getEscherAggregate(patriarch6).getTailRecords().size());
                                assertEquals(3, patriarch6.getChildren().size());

                                HSSFComment comment6 = (HSSFComment) patriarch6.getChildren().get(0);
                                patriarch6.removeShape(comment6);

                                assertEquals(3, getEscherAggregate(patriarch6).getShapeToObjMapping().size());
                                assertEquals(0, getEscherAggregate(patriarch6).getTailRecords().size());
                                assertEquals(2, patriarch6.getChildren().size());

                                try (HSSFWorkbook wb7 = HSSFTestDataSamples.writeOutAndReadBack(wb6)) {
                                    HSSFSheet sheet7 = wb7.getSheetAt(0);
                                    HSSFPatriarch patriarch7 = sheet7.getDrawingPatriarch();

                                    assertEquals(3, getEscherAggregate(patriarch7).getShapeToObjMapping().size());
                                    assertEquals(0, getEscherAggregate(patriarch7).getTailRecords().size());
                                    assertEquals(2, patriarch7.getChildren().size());

                                    HSSFPolygon polygon7 = (HSSFPolygon) patriarch7.getChildren().get(0);
                                    patriarch7.removeShape(polygon7);

                                    assertEquals(2, getEscherAggregate(patriarch7).getShapeToObjMapping().size());
                                    assertEquals(0, getEscherAggregate(patriarch7).getTailRecords().size());
                                    assertEquals(1, patriarch7.getChildren().size());

                                    try (HSSFWorkbook wb8 = HSSFTestDataSamples.writeOutAndReadBack(wb7)) {
                                        HSSFSheet sheet8 = wb8.getSheetAt(0);
                                        HSSFPatriarch patriarch8 = sheet8.getDrawingPatriarch();

                                        assertEquals(2, getEscherAggregate(patriarch8).getShapeToObjMapping().size());
                                        assertEquals(0, getEscherAggregate(patriarch8).getTailRecords().size());
                                        assertEquals(1, patriarch8.getChildren().size());

                                        HSSFTextbox textbox8 = (HSSFTextbox) patriarch8.getChildren().get(0);
                                        patriarch8.removeShape(textbox8);

                                        assertEquals(0, getEscherAggregate(patriarch8).getShapeToObjMapping().size());
                                        assertEquals(0, getEscherAggregate(patriarch8).getTailRecords().size());
                                        assertEquals(0, patriarch8.getChildren().size());

                                        try (HSSFWorkbook wb9 = HSSFTestDataSamples.writeOutAndReadBack(wb8)) {
                                            HSSFSheet sheet9 = wb9.getSheetAt(0);
                                            HSSFPatriarch patriarch9 = sheet9.getDrawingPatriarch();

                                            assertEquals(0, getEscherAggregate(patriarch9).getShapeToObjMapping().size());
                                            assertEquals(0, getEscherAggregate(patriarch9).getTailRecords().size());
                                            assertEquals(0, patriarch9.getChildren().size());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void testShapeFlip() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFSimpleShape rectangle = patriarch.createSimpleShape(new HSSFClientAnchor());
            rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

            assertFalse(rectangle.isFlipVertical());
            assertFalse(rectangle.isFlipHorizontal());

            rectangle.setFlipVertical(true);
            assertTrue(rectangle.isFlipVertical());
            rectangle.setFlipHorizontal(true);
            assertTrue(rectangle.isFlipHorizontal());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();

                rectangle = (HSSFSimpleShape) patriarch.getChildren().get(0);

                assertTrue(rectangle.isFlipHorizontal());
                rectangle.setFlipHorizontal(false);
                assertFalse(rectangle.isFlipHorizontal());

                assertTrue(rectangle.isFlipVertical());
                rectangle.setFlipVertical(false);
                assertFalse(rectangle.isFlipVertical());

                try (HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2)) {
                    sheet = wb3.getSheetAt(0);
                    patriarch = sheet.getDrawingPatriarch();

                    rectangle = (HSSFSimpleShape) patriarch.getChildren().get(0);

                    assertFalse(rectangle.isFlipVertical());
                    assertFalse(rectangle.isFlipHorizontal());
                }
            }
        }
    }

    @Test
    void testRotation() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFSimpleShape rectangle = patriarch.createSimpleShape(new HSSFClientAnchor(0, 0, 100, 100, (short) 0, 0, (short) 5, 5));
            rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

            assertEquals(0, rectangle.getRotationDegree());
            rectangle.setRotationDegree((short) 45);
            assertEquals(45, rectangle.getRotationDegree());
            rectangle.setFlipHorizontal(true);

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();
                rectangle = (HSSFSimpleShape) patriarch.getChildren().get(0);
                assertEquals(45, rectangle.getRotationDegree());
                rectangle.setRotationDegree((short) 30);
                assertEquals(30, rectangle.getRotationDegree());

                patriarch.setCoordinates(0, 0, 10, 10);
                rectangle.setString(new HSSFRichTextString("1234"));
            }
        }
    }

    @SuppressWarnings("unused")
    @Test
    void testShapeContainerImplementsIterable() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFSimpleShape s1 = patriarch.createSimpleShape(new HSSFClientAnchor());
            HSSFSimpleShape s2 = patriarch.createSimpleShape(new HSSFClientAnchor());

            Iterator<HSSFShape> iter = patriarch.iterator();
            assertEquals(s1, iter.next());
            assertEquals(s2, iter.next());
            assertFalse(iter.hasNext());

            Spliterator<HSSFShape> spliter = patriarch.spliterator();
            spliter.tryAdvance(s -> assertEquals(s1, s));
            spliter.tryAdvance(s -> assertEquals(s2, s));
            assertFalse(spliter.tryAdvance(s -> fail()));
        }
    }

    @Test
    void testClearShapesForPatriarch() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            patriarch.createSimpleShape(new HSSFClientAnchor());
            patriarch.createSimpleShape(new HSSFClientAnchor());
            patriarch.createCellComment(new HSSFClientAnchor());

            EscherAggregate agg = getEscherAggregate(patriarch);

            assertEquals(6, agg.getShapeToObjMapping().size());
            assertEquals(1, agg.getTailRecords().size());
            assertEquals(3, patriarch.getChildren().size());

            patriarch.clear();

            assertEquals(0, agg.getShapeToObjMapping().size());
            assertEquals(0, agg.getTailRecords().size());
            assertEquals(0, patriarch.getChildren().size());

            try (HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1)) {
                sheet = wb2.getSheetAt(0);
                patriarch = sheet.getDrawingPatriarch();

                assertEquals(0, agg.getShapeToObjMapping().size());
                assertEquals(0, agg.getTailRecords().size());
                assertEquals(0, patriarch.getChildren().size());
            }
        }
    }

    @Test
    void testBug45312() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet();
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            HSSFClientAnchor a1 = new HSSFClientAnchor();
            a1.setAnchor( (short)1, 1, 0, 0, (short) 1, 1, 512, 100);
            HSSFSimpleShape shape1 = patriarch.createSimpleShape(a1);
            shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);

            HSSFClientAnchor a2 = new HSSFClientAnchor();
            a2.setAnchor( (short)1, 1, 512, 0, (short) 1, 1, 1024, 100);
            HSSFSimpleShape shape2 = patriarch.createSimpleShape(a2);
            shape2.setFlipVertical(true);
            shape2.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);

            HSSFClientAnchor a3 = new HSSFClientAnchor();
            a3.setAnchor( (short)2, 2, 0, 0, (short) 2, 2, 512, 100);
            HSSFSimpleShape shape3 = patriarch.createSimpleShape(a3);
            shape3.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);

            HSSFClientAnchor a4 = new HSSFClientAnchor();
            a4.setAnchor( (short)2, 2, 0, 100, (short) 2, 2, 512, 200);
            HSSFSimpleShape shape4 = patriarch.createSimpleShape(a4);
            shape4.setFlipHorizontal(true);
            shape4.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);

            checkWorkbookBack(wb);
        }
    }

    private void checkWorkbookBack(HSSFWorkbook wb) throws IOException {
        try (HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb)) {
            assertNotNull(wbBack);

            HSSFSheet sheetBack = wbBack.getSheetAt(0);
            assertNotNull(sheetBack);

            HSSFPatriarch patriarchBack = sheetBack.getDrawingPatriarch();
            assertNotNull(patriarchBack);

            List<HSSFShape> children = patriarchBack.getChildren();
            assertEquals(4, children.size());
            HSSFShape hssfShape = children.get(0);
            assertTrue(hssfShape instanceof HSSFSimpleShape);
            HSSFAnchor anchor = hssfShape.getAnchor();
            assertTrue(anchor instanceof HSSFClientAnchor);
            assertEquals(0, anchor.getDx1());
            assertEquals(512, anchor.getDx2());
            assertEquals(0, anchor.getDy1());
            assertEquals(100, anchor.getDy2());
            HSSFClientAnchor cAnchor = (HSSFClientAnchor) anchor;
            assertEquals(1, cAnchor.getCol1());
            assertEquals(1, cAnchor.getCol2());
            assertEquals(1, cAnchor.getRow1());
            assertEquals(1, cAnchor.getRow2());

            hssfShape = children.get(1);
            assertTrue(hssfShape instanceof HSSFSimpleShape);
            anchor = hssfShape.getAnchor();
            assertTrue(anchor instanceof HSSFClientAnchor);
            assertEquals(512, anchor.getDx1());
            assertEquals(1024, anchor.getDx2());
            assertEquals(0, anchor.getDy1());
            assertEquals(100, anchor.getDy2());
            cAnchor = (HSSFClientAnchor) anchor;
            assertEquals(1, cAnchor.getCol1());
            assertEquals(1, cAnchor.getCol2());
            assertEquals(1, cAnchor.getRow1());
            assertEquals(1, cAnchor.getRow2());

            hssfShape = children.get(2);
            assertTrue(hssfShape instanceof HSSFSimpleShape);
            anchor = hssfShape.getAnchor();
            assertTrue(anchor instanceof HSSFClientAnchor);
            assertEquals(0, anchor.getDx1());
            assertEquals(512, anchor.getDx2());
            assertEquals(0, anchor.getDy1());
            assertEquals(100, anchor.getDy2());
            cAnchor = (HSSFClientAnchor) anchor;
            assertEquals(2, cAnchor.getCol1());
            assertEquals(2, cAnchor.getCol2());
            assertEquals(2, cAnchor.getRow1());
            assertEquals(2, cAnchor.getRow2());

            hssfShape = children.get(3);
            assertTrue(hssfShape instanceof HSSFSimpleShape);
            anchor = hssfShape.getAnchor();
            assertTrue(anchor instanceof HSSFClientAnchor);
            assertEquals(0, anchor.getDx1());
            assertEquals(512, anchor.getDx2());
            assertEquals(100, anchor.getDy1());
            assertEquals(200, anchor.getDy2());
            cAnchor = (HSSFClientAnchor) anchor;
            assertEquals(2, cAnchor.getCol1());
            assertEquals(2, cAnchor.getCol2());
            assertEquals(2, cAnchor.getRow1());
            assertEquals(2, cAnchor.getRow2());
        }
    }
}
