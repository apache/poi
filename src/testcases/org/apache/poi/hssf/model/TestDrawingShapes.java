package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.ddf.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.util.HexDump;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * @author Evgeniy Berlog
 * date: 12.06.12
 */
public class TestDrawingShapes extends TestCase {

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
    public void testDrawingGroups() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("groups");
        HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
        assertEquals(patriarch.getChildren().size(), 2);
        HSSFShapeGroup group = (HSSFShapeGroup) patriarch.getChildren().get(1);
        assertEquals(3, group.getChildren().size());
        HSSFShapeGroup group1 = (HSSFShapeGroup) group.getChildren().get(0);
        assertEquals(2, group1.getChildren().size());
        group1 = (HSSFShapeGroup) group.getChildren().get(2);
        assertEquals(2, group1.getChildren().size());
    }

    public void testHSSFShapeCompatibility() {
        HSSFShape shape = new HSSFSimpleShape(null, new HSSFClientAnchor());
        assertEquals(0x08000040, shape.getLineStyleColor());
        assertEquals(0x08000009, shape.getFillColor());
        assertEquals(HSSFShape.LINEWIDTH_DEFAULT, shape.getLineWidth());
        assertEquals(HSSFShape.LINESTYLE_SOLID, shape.getLineStyle());
        assertFalse(shape.isNoFill());

        AbstractShape sp = AbstractShape.createShape(shape, 1);
        EscherContainerRecord spContainer = sp.getSpContainer();
        EscherOptRecord opt =
                spContainer.getChildById(EscherOptRecord.RECORD_ID);

        assertEquals(7, opt.getEscherProperties().size());
        assertEquals(true,
                ((EscherBoolProperty) opt.lookup(EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE)).isTrue());
        assertEquals(0x00000004,
                ((EscherSimpleProperty) opt.lookup(EscherProperties.GEOMETRY__SHAPEPATH)).getPropertyValue());
        assertEquals(0x08000009,
                ((EscherSimpleProperty) opt.lookup(EscherProperties.FILL__FILLCOLOR)).getPropertyValue());
        assertEquals(true,
                ((EscherBoolProperty) opt.lookup(EscherProperties.FILL__NOFILLHITTEST)).isTrue());
        assertEquals(0x08000040,
                ((EscherSimpleProperty) opt.lookup(EscherProperties.LINESTYLE__COLOR)).getPropertyValue());
        assertEquals(true,
                ((EscherBoolProperty) opt.lookup(EscherProperties.LINESTYLE__NOLINEDRAWDASH)).isTrue());
        assertEquals(true,
                ((EscherBoolProperty) opt.lookup(EscherProperties.GROUPSHAPE__PRINT)).isTrue());
    }

    public void testDefaultPictureSettings() {
        HSSFPicture picture = new HSSFPicture(null, new HSSFClientAnchor());
        assertEquals(picture.getLineWidth(), HSSFShape.LINEWIDTH_DEFAULT);
        assertEquals(picture.getFillColor(), HSSFShape.FILL__FILLCOLOR_DEFAULT);
        assertEquals(picture.getLineStyle(), HSSFShape.LINESTYLE_SOLID);
        assertEquals(picture.getLineStyleColor(), HSSFShape.LINESTYLE__COLOR_DEFAULT);
        assertEquals(picture.isNoFill(), false);
        assertEquals(picture.getPictureIndex(), -1);//not set yet
    }

    /**
     * No NullPointerException should appear
     */
    public void testDefaultSettingsWithEmptyContainer() {
        EscherContainerRecord container = new EscherContainerRecord();
        EscherOptRecord opt = new EscherOptRecord();
        opt.setRecordId(EscherOptRecord.RECORD_ID);
        container.addChildRecord(opt);
        ObjRecord obj = new ObjRecord();
        CommonObjectDataSubRecord cod = new CommonObjectDataSubRecord();
        cod.setObjectType(HSSFSimpleShape.OBJECT_TYPE_PICTURE);
        obj.addSubRecord(cod);
        HSSFPicture picture = new HSSFPicture(container, obj);

        assertEquals(picture.getLineWidth(), HSSFShape.LINEWIDTH_DEFAULT);
        assertEquals(picture.getFillColor(), HSSFShape.FILL__FILLCOLOR_DEFAULT);
        assertEquals(picture.getLineStyle(), HSSFShape.LINESTYLE_DEFAULT);
        assertEquals(picture.getLineStyleColor(), HSSFShape.LINESTYLE__COLOR_DEFAULT);
        assertEquals(picture.isNoFill(), HSSFShape.NO_FILL_DEFAULT);
        assertEquals(picture.getPictureIndex(), -1);//not set yet
    }

    /**
     * create a rectangle, save the workbook, read back and verify that all shape properties are there
     */
    public void testReadWriteRectangle() throws IOException {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 200, 200, (short) 2, 2, (short) 15, 15);
        anchor.setAnchorType(2);
        assertEquals(anchor.getAnchorType(), 2);

        HSSFSimpleShape rectangle = drawing.createSimpleShape(anchor);
        rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
        rectangle.setLineWidth(10000);
        rectangle.setFillColor(777);
        assertEquals(rectangle.getFillColor(), 777);
        assertEquals(10000, rectangle.getLineWidth());
        rectangle.setLineStyle(10);
        assertEquals(10, rectangle.getLineStyle());
        rectangle.setLineStyleColor(1111);
        rectangle.setNoFill(true);
        assertEquals(rectangle.getLineStyleColor(), 1111);
        assertEquals(rectangle.isNoFill(), true);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());

        HSSFSimpleShape rectangle2 =
                (HSSFSimpleShape) drawing.getChildren().get(0);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE,
                rectangle2.getShapeType());
        assertEquals(10000, rectangle2.getLineWidth());
        assertEquals(10, rectangle2.getLineStyle());
        assertEquals(anchor, rectangle2.getAnchor());
        assertEquals(rectangle2.getLineStyleColor(), 1111);
        assertEquals(rectangle2.getFillColor(), 777);
        assertEquals(rectangle2.isNoFill(), true);

        rectangle2.setFillColor(3333);
        rectangle2.setLineStyle(9);
        rectangle2.setLineStyleColor(4444);
        rectangle2.setNoFill(false);
        rectangle2.setLineWidth(77);
        rectangle2.getAnchor().setDx1(2);
        rectangle2.getAnchor().setDx2(3);
        rectangle2.getAnchor().setDy1(4);
        rectangle2.getAnchor().setDy2(5);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());
        rectangle2 = (HSSFSimpleShape) drawing.getChildren().get(0);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE, rectangle2.getShapeType());
        assertEquals(77, rectangle2.getLineWidth());
        assertEquals(9, rectangle2.getLineStyle());
        assertEquals(rectangle2.getLineStyleColor(), 4444);
        assertEquals(rectangle2.getFillColor(), 3333);
        assertEquals(rectangle2.getAnchor().getDx1(), 2);
        assertEquals(rectangle2.getAnchor().getDx2(), 3);
        assertEquals(rectangle2.getAnchor().getDy1(), 4);
        assertEquals(rectangle2.getAnchor().getDy2(), 5);
        assertEquals(rectangle2.isNoFill(), false);

        HSSFSimpleShape rect3 = drawing.createSimpleShape(new HSSFClientAnchor());
        rect3.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        drawing = wb.getSheetAt(0).getDrawingPatriarch();
        assertEquals(drawing.getChildren().size(), 2);
    }

    public void testReadExistingImage() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("pictures");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());
        HSSFPicture picture = (HSSFPicture) drawing.getChildren().get(0);

        assertEquals(picture.getPictureIndex(), 1);
        assertEquals(picture.getLineStyleColor(), HSSFShape.LINESTYLE__COLOR_DEFAULT);
        assertEquals(picture.getFillColor(), 0x5DC943);
        assertEquals(picture.getLineWidth(), HSSFShape.LINEWIDTH_DEFAULT);
        assertEquals(picture.getLineStyle(), HSSFShape.LINESTYLE_DEFAULT);
        assertEquals(picture.isNoFill(), true);

        picture.setPictureIndex(2);
        assertEquals(picture.getPictureIndex(), 2);
    }


    /* assert shape properties when reading shapes from a existing workbook */
    public void testReadExistingRectangle() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("rectangles");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());

        HSSFSimpleShape shape = (HSSFSimpleShape) drawing.getChildren().get(0);
        assertEquals(shape.isNoFill(), true);
        assertEquals(shape.getLineStyle(), HSSFShape.LINESTYLE_DASHDOTGEL);
        assertEquals(shape.getLineStyleColor(), 0x616161);
        assertEquals(HexDump.toHex(shape.getFillColor()), shape.getFillColor(), 0x2CE03D);
        assertEquals(shape.getLineWidth(), HSSFShape.LINEWIDTH_ONE_PT * 2);
    }

    public void testShapeIds() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet();
        HSSFPatriarch patriarch1 = sheet1.createDrawingPatriarch();
        for (int i = 0; i < 2; i++) {
            patriarch1.createSimpleShape(new HSSFClientAnchor());
        }

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet1 = wb.getSheetAt(0);
        patriarch1 = sheet1.getDrawingPatriarch();

        EscherAggregate agg1 = HSSFTestHelper.getEscherAggregate(patriarch1);
        // last shape ID cached in EscherDgRecord
        EscherDgRecord dg1 =
                agg1.getEscherContainer().getChildById(EscherDgRecord.RECORD_ID);
        assertEquals(1026, dg1.getLastMSOSPID());

        // iterate over shapes and check shapeId
        EscherContainerRecord spgrContainer =
                agg1.getEscherContainer().getChildContainers().get(0);
        // root spContainer + 2 spContainers for shapes
        assertEquals(3, spgrContainer.getChildRecords().size());

        EscherSpRecord sp0 =
                ((EscherContainerRecord) spgrContainer.getChild(0)).getChildById(EscherSpRecord.RECORD_ID);
        assertEquals(1024, sp0.getShapeId());

        EscherSpRecord sp1 =
                ((EscherContainerRecord) spgrContainer.getChild(1)).getChildById(EscherSpRecord.RECORD_ID);
        assertEquals(1025, sp1.getShapeId());

        EscherSpRecord sp2 =
                ((EscherContainerRecord) spgrContainer.getChild(2)).getChildById(EscherSpRecord.RECORD_ID);
        assertEquals(1026, sp2.getShapeId());
    }

    /**
     * Test get new id for shapes from existing file
     * File already have for 1 shape on each sheet, because document must contain EscherDgRecord for each sheet
     */
    public void testAllocateNewIds() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("empty.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFPatriarch patriarch = sheet.getDrawingPatriarch();

        /**
         * 2048 - main SpContainer id
         * 2049 - existing shape id
         */
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 2050);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 2051);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 2052);

        sheet = wb.getSheetAt(1);
        patriarch = sheet.getDrawingPatriarch();

        /**
         * 3072 - main SpContainer id
         * 3073 - existing shape id
         */
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 3074);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 3075);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 3076);


        sheet = wb.getSheetAt(2);
        patriarch = sheet.getDrawingPatriarch();

        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 1026);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 1027);
        assertEquals(HSSFTestHelper.allocateNewShapeId(patriarch), 1028);
    }

    public void testOpt() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        // create a sheet with a text box
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
        EscherOptRecord opt1 = HSSFTestHelper.getOptRecord(textbox);
        EscherOptRecord opt2 = textbox.getEscherContainer().getChildById(EscherOptRecord.RECORD_ID);
        assertSame(opt1, opt2);
    }
    
    public void testCorrectOrderInOptRecord(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        HSSFTextbox textbox = patriarch.createTextbox(new HSSFClientAnchor());
        EscherOptRecord opt = HSSFTestHelper.getOptRecord(textbox);    
        
        String opt1Str = opt.toXml();

        textbox.setFillColor(textbox.getFillColor());
        assertEquals(opt1Str, textbox.getEscherContainer().getChildById(EscherOptRecord.RECORD_ID).toXml());
        textbox.setLineStyle(textbox.getLineStyle());
        assertEquals(opt1Str, textbox.getEscherContainer().getChildById(EscherOptRecord.RECORD_ID).toXml());
        textbox.setLineWidth(textbox.getLineWidth());
        assertEquals(opt1Str, textbox.getEscherContainer().getChildById(EscherOptRecord.RECORD_ID).toXml());
        textbox.setLineStyleColor(textbox.getLineStyleColor());
        assertEquals(opt1Str, textbox.getEscherContainer().getChildById(EscherOptRecord.RECORD_ID).toXml());
    }

    public void testDgRecordNumShapes(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

        EscherAggregate aggregate = HSSFTestHelper.getEscherAggregate(patriarch);
        EscherDgRecord dgRecord = (EscherDgRecord) aggregate.getEscherRecord(0).getChild(0);
        assertEquals(dgRecord.getNumShapes(), 1);
    }
}
