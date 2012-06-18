package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.ddf.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.util.HexDump;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * @author Evgeniy Berlog
 * date: 12.06.12
 */
public class TestDrawingShapes extends TestCase{

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
    public void testDrawingGroups(){
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
        HSSFShape shape = new HSSFSimpleShape(null, new  HSSFClientAnchor());
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
                ((EscherBoolProperty)opt.lookup(EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE)).isTrue());
        assertEquals(0x00000004,
                ((EscherSimpleProperty)opt.lookup(EscherProperties.GEOMETRY__SHAPEPATH)).getPropertyValue());
        assertEquals(0x08000009,
                ((EscherSimpleProperty)opt.lookup(EscherProperties.FILL__FILLCOLOR)).getPropertyValue());
        assertEquals(true,
                ((EscherBoolProperty)opt.lookup(EscherProperties.FILL__NOFILLHITTEST)).isTrue());
        assertEquals(0x08000040,
                ((EscherSimpleProperty)opt.lookup(EscherProperties.LINESTYLE__COLOR)).getPropertyValue());
        assertEquals(true,
                ((EscherBoolProperty)opt.lookup(EscherProperties.LINESTYLE__NOLINEDRAWDASH)).isTrue());
        assertEquals(true,
                ((EscherBoolProperty)opt.lookup(EscherProperties.GROUPSHAPE__PRINT)).isTrue());
    }
    /**
     * create a rectangle, save the workbook, read back and verify that all shape properties are there
     */
    public void testReadWriteRectangle() throws IOException {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 200, 200, (short)2, 2, (short)15, 15);
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
                (HSSFSimpleShape)drawing.getChildren().get(0);
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
        rectangle2 = (HSSFSimpleShape)drawing.getChildren().get(0);
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
    }


    /* assert shape properties when reading shapes from a existing workbook */
    public void testReadExistingRectangle() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("rectangles");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());

        for(HSSFShape shape : drawing.getChildren()){
            assertEquals(shape.isNoFill(), true);
            assertEquals(shape.getLineStyle(), HSSFShape.LINESTYLE_DASHDOTGEL);
            assertEquals(shape.getLineStyleColor(), 0x616161);
            assertEquals(HexDump.toHex(shape.getFillColor()), shape.getFillColor(), 0x2CE03D);
            assertEquals(shape.getLineWidth(), HSSFShape.LINEWIDTH_ONE_PT*2);
        }
    }
}
