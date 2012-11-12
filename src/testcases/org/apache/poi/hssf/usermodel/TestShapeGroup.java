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

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherSpgrRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.EscherAggregate;

import java.lang.reflect.Field;

/**
 * @author Evgeniy Berlog
 * @date 29.06.12
 */
public class TestShapeGroup extends TestCase{

    public void testSetGetCoordinates(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
        assertEquals(group.getX1(), 0);
        assertEquals(group.getY1(), 0);
        assertEquals(group.getX2(), 1023);
        assertEquals(group.getY2(), 255);

        group.setCoordinates(1,2,3,4);

        assertEquals(group.getX1(), 1);
        assertEquals(group.getY1(), 2);
        assertEquals(group.getX2(), 3);
        assertEquals(group.getY2(), 4);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        group = (HSSFShapeGroup) patriarch.getChildren().get(0);
        assertEquals(group.getX1(), 1);
        assertEquals(group.getY1(), 2);
        assertEquals(group.getX2(), 3);
        assertEquals(group.getY2(), 4);
    }

    public void testAddToExistingFile(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        HSSFShapeGroup group1 = patriarch.createGroup(new HSSFClientAnchor());
        HSSFShapeGroup group2 = patriarch.createGroup(new HSSFClientAnchor());

        group1.setCoordinates(1,2,3,4);
        group2.setCoordinates(5,6,7,8);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 2);

        HSSFShapeGroup group3 = patriarch.createGroup(new HSSFClientAnchor());
        group3.setCoordinates(9,10,11,12);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        assertEquals(patriarch.getChildren().size(), 3);
    }

    public void testModify() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        // create a sheet with a text box
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFShapeGroup group1 = patriarch.createGroup(new
                HSSFClientAnchor(0,0,0,0,
                (short)0, 0, (short)15, 25));
        group1.setCoordinates(0, 0, 792, 612);

        HSSFTextbox textbox1 = group1.createTextbox(new
                HSSFChildAnchor(100, 100, 300, 300));
        HSSFRichTextString rt1 = new HSSFRichTextString("Hello, World!");
        textbox1.setString(rt1);

        // write, read back and check that our text box is there
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();
        assertEquals(1, patriarch.getChildren().size());

        group1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
        assertEquals(1, group1.getChildren().size());
        textbox1 = (HSSFTextbox)group1.getChildren().get(0);
        assertEquals("Hello, World!", textbox1.getString().getString());

        // modify anchor
        assertEquals(new HSSFChildAnchor(100, 100, 300, 300),
                textbox1.getAnchor());
        HSSFChildAnchor newAnchor = new HSSFChildAnchor(200,200, 400, 400);
        textbox1.setAnchor(newAnchor);
        // modify text
        textbox1.setString(new HSSFRichTextString("Hello, World! (modified)"));

        // add a new text box
        HSSFTextbox textbox2 = group1.createTextbox(new
                HSSFChildAnchor(400, 400, 600, 600));
        HSSFRichTextString rt2 = new HSSFRichTextString("Hello, World-2");
        textbox2.setString(rt2);
        assertEquals(2, group1.getChildren().size());

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();
        assertEquals(1, patriarch.getChildren().size());

        group1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
        assertEquals(2, group1.getChildren().size());
        textbox1 = (HSSFTextbox)group1.getChildren().get(0);
        assertEquals("Hello, World! (modified)",
                textbox1.getString().getString());
        assertEquals(new HSSFChildAnchor(200,200, 400, 400),
                textbox1.getAnchor());

        textbox2 = (HSSFTextbox)group1.getChildren().get(1);
        assertEquals("Hello, World-2", textbox2.getString().getString());
        assertEquals(new HSSFChildAnchor(400, 400, 600, 600),
                textbox2.getAnchor());

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();
        group1 = (HSSFShapeGroup)patriarch.getChildren().get(0);
        textbox1 = (HSSFTextbox)group1.getChildren().get(0);
        textbox2 = (HSSFTextbox)group1.getChildren().get(1);
        HSSFTextbox textbox3 = group1.createTextbox(new
                HSSFChildAnchor(400,200, 600, 400));
        HSSFRichTextString rt3 = new HSSFRichTextString("Hello, World-3");
        textbox3.setString(rt3);
    }

    public void testAddShapesToGroup(){
        HSSFWorkbook wb = new HSSFWorkbook();

        // create a sheet with a text box
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
        int index = wb.addPicture(new byte[]{1,2,3}, HSSFWorkbook.PICTURE_TYPE_JPEG);
        group.createPicture(new HSSFChildAnchor(), index);
        HSSFPolygon polygon = group.createPolygon(new HSSFChildAnchor());
        polygon.setPoints(new int[]{1,100, 1}, new int[]{1, 50, 100});
        group.createTextbox(new HSSFChildAnchor());
        group.createShape(new HSSFChildAnchor());

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();
        assertEquals(1, patriarch.getChildren().size());

        assertTrue(patriarch.getChildren().get(0) instanceof HSSFShapeGroup);
        group = (HSSFShapeGroup) patriarch.getChildren().get(0);

        assertEquals(group.getChildren().size(), 4);

        assertTrue(group.getChildren().get(0) instanceof HSSFPicture);
        assertTrue(group.getChildren().get(1) instanceof HSSFPolygon);
        assertTrue(group.getChildren().get(2) instanceof HSSFTextbox);
        assertTrue(group.getChildren().get(3) instanceof HSSFSimpleShape);

        HSSFShapeGroup group2 = patriarch.createGroup(new HSSFClientAnchor());

        index = wb.addPicture(new byte[]{2,2,2}, HSSFWorkbook.PICTURE_TYPE_JPEG);
        group2.createPicture(new HSSFChildAnchor(), index);
        polygon = group2.createPolygon(new HSSFChildAnchor());
        polygon.setPoints(new int[]{1,100, 1}, new int[]{1, 50, 100});
        group2.createTextbox(new HSSFChildAnchor());
        group2.createShape(new HSSFChildAnchor());
        group2.createShape(new HSSFChildAnchor());

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();
        assertEquals(2, patriarch.getChildren().size());

        group = (HSSFShapeGroup) patriarch.getChildren().get(1);

        assertEquals(group.getChildren().size(), 5);

        assertTrue(group.getChildren().get(0) instanceof HSSFPicture);
        assertTrue(group.getChildren().get(1) instanceof HSSFPolygon);
        assertTrue(group.getChildren().get(2) instanceof HSSFTextbox);
        assertTrue(group.getChildren().get(3) instanceof HSSFSimpleShape);
        assertTrue(group.getChildren().get(4) instanceof HSSFSimpleShape);

        group.getShapeId();
    }

    public void testSpgrRecord(){
        HSSFWorkbook wb = new HSSFWorkbook();

        // create a sheet with a text box
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());
        assertSame(((EscherContainerRecord)group.getEscherContainer().getChild(0)).getChildById(EscherSpgrRecord.RECORD_ID), getSpgrRecord(group));
    }

    private static EscherSpgrRecord getSpgrRecord(HSSFShapeGroup group) {
        Field spgrField = null;
        try {
            spgrField = group.getClass().getDeclaredField("_spgrRecord");
            spgrField.setAccessible(true);
            return (EscherSpgrRecord) spgrField.get(group);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void testClearShapes(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        HSSFShapeGroup group = patriarch.createGroup(new HSSFClientAnchor());

        group.createShape(new HSSFChildAnchor());
        group.createShape(new HSSFChildAnchor());

        EscherAggregate agg = HSSFTestHelper.getEscherAggregate(patriarch);

        assertEquals(agg.getShapeToObjMapping().size(), 5);
        assertEquals(agg.getTailRecords().size(), 0);
        assertEquals(group.getChildren().size(), 2);

        group.clear();

        assertEquals(agg.getShapeToObjMapping().size(), 1);
        assertEquals(agg.getTailRecords().size(), 0);
        assertEquals(group.getChildren().size(), 0);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        patriarch = sheet.getDrawingPatriarch();

        group = (HSSFShapeGroup) patriarch.getChildren().get(0);

        assertEquals(agg.getShapeToObjMapping().size(), 1);
        assertEquals(agg.getTailRecords().size(), 0);
        assertEquals(group.getChildren().size(), 0);
    }
}
