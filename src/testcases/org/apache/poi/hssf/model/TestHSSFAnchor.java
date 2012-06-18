package org.apache.poi.hssf.model;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.hssf.usermodel.*;

/**
 * @author Evgeniy Berlog
 * @date 12.06.12
 */
public class TestHSSFAnchor extends TestCase {

    public void testCreateClientAnchorFromContainer(){
        EscherContainerRecord container = new EscherContainerRecord();
        EscherClientAnchorRecord escher = new EscherClientAnchorRecord();
        escher.setFlag((short) 3);
        escher.setCol1((short)11);
        escher.setCol2((short)12);
        escher.setRow1((short)13);
        escher.setRow2((short) 14);
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);
        container.addChildRecord(escher);

        HSSFClientAnchor anchor = (HSSFClientAnchor) HSSFAnchor.createAnchorFromEscher(container);
        assertEquals(anchor.getCol1(), 11);
        assertEquals(escher.getCol1(), 11);
        assertEquals(anchor.getCol2(), 12);
        assertEquals(escher.getCol2(), 12);
        assertEquals(anchor.getRow1(), 13);
        assertEquals(escher.getRow1(), 13);
        assertEquals(anchor.getRow2(), 14);
        assertEquals(escher.getRow2(), 14);
        assertEquals(anchor.getDx1(), 15);
        assertEquals(escher.getDx1(), 15);
        assertEquals(anchor.getDx2(), 16);
        assertEquals(escher.getDx2(), 16);
        assertEquals(anchor.getDy1(), 17);
        assertEquals(escher.getDy1(), 17);
        assertEquals(anchor.getDy2(), 18);
        assertEquals(escher.getDy2(), 18);
    }

    public void testCreateChildAnchorFromContainer(){
        EscherContainerRecord container = new EscherContainerRecord();
        EscherChildAnchorRecord escher = new EscherChildAnchorRecord();
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);
        container.addChildRecord(escher);

        HSSFChildAnchor anchor = (HSSFChildAnchor) HSSFAnchor.createAnchorFromEscher(container);
        assertEquals(anchor.getDx1(), 15);
        assertEquals(escher.getDx1(), 15);
        assertEquals(anchor.getDx2(), 16);
        assertEquals(escher.getDx2(), 16);
        assertEquals(anchor.getDy1(), 17);
        assertEquals(escher.getDy1(), 17);
        assertEquals(anchor.getDy2(), 18);
        assertEquals(escher.getDy2(), 18);
    }

    public void testShapeEscherMustHaveAnchorRecord(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 200, 200, (short)2, 2, (short)15, 15);
        anchor.setAnchorType(2);

        HSSFSimpleShape rectangle = drawing.createSimpleShape(anchor);
        rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

        rectangle.setAnchor(anchor);

        assertNotNull(anchor.getEscherAnchor());
        assertNotNull(rectangle.getEscherContainer());
        assertTrue(anchor.getEscherAnchor().equals(rectangle.getEscherContainer().getChildById(EscherClientAnchorRecord.RECORD_ID)));
    }

    public void testClientAnchorFromEscher(){
        EscherClientAnchorRecord escher = new EscherClientAnchorRecord();
        escher.setCol1((short)11);
        escher.setCol2((short)12);
        escher.setRow1((short)13);
        escher.setRow2((short) 14);
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);

        HSSFClientAnchor anchor = new HSSFClientAnchor(escher);
        assertEquals(anchor.getCol1(), 11);
        assertEquals(escher.getCol1(), 11);
        assertEquals(anchor.getCol2(), 12);
        assertEquals(escher.getCol2(), 12);
        assertEquals(anchor.getRow1(), 13);
        assertEquals(escher.getRow1(), 13);
        assertEquals(anchor.getRow2(), 14);
        assertEquals(escher.getRow2(), 14);
        assertEquals(anchor.getDx1(), 15);
        assertEquals(escher.getDx1(), 15);
        assertEquals(anchor.getDx2(), 16);
        assertEquals(escher.getDx2(), 16);
        assertEquals(anchor.getDy1(), 17);
        assertEquals(escher.getDy1(), 17);
        assertEquals(anchor.getDy2(), 18);
        assertEquals(escher.getDy2(), 18);
    }

    public void testClientAnchorFromScratch(){
        HSSFClientAnchor anchor = new HSSFClientAnchor();
        EscherClientAnchorRecord escher = (EscherClientAnchorRecord) anchor.getEscherAnchor();
        anchor.setAnchor((short)11, 12, 13, 14, (short)15, 16, 17, 18);

        assertEquals(anchor.getCol1(), 11);
        assertEquals(escher.getCol1(), 11);
        assertEquals(anchor.getCol2(), 15);
        assertEquals(escher.getCol2(), 15);
        assertEquals(anchor.getRow1(), 12);
        assertEquals(escher.getRow1(), 12);
        assertEquals(anchor.getRow2(), 16);
        assertEquals(escher.getRow2(), 16);
        assertEquals(anchor.getDx1(), 13);
        assertEquals(escher.getDx1(), 13);
        assertEquals(anchor.getDx2(), 17);
        assertEquals(escher.getDx2(), 17);
        assertEquals(anchor.getDy1(), 14);
        assertEquals(escher.getDy1(), 14);
        assertEquals(anchor.getDy2(), 18);
        assertEquals(escher.getDy2(), 18);

        anchor.setCol1(111);
        assertEquals(anchor.getCol1(), 111);
        assertEquals(escher.getCol1(), 111);
        anchor.setCol2(112);
        assertEquals(anchor.getCol2(), 112);
        assertEquals(escher.getCol2(), 112);
        anchor.setRow1(113);
        assertEquals(anchor.getRow1(), 113);
        assertEquals(escher.getRow1(), 113);
        anchor.setRow2(114);
        assertEquals(anchor.getRow2(), 114);
        assertEquals(escher.getRow2(), 114);
        anchor.setDx1(115);
        assertEquals(anchor.getDx1(), 115);
        assertEquals(escher.getDx1(), 115);
        anchor.setDx2(116);
        assertEquals(anchor.getDx2(), 116);
        assertEquals(escher.getDx2(), 116);
        anchor.setDy1(117);
        assertEquals(anchor.getDy1(), 117);
        assertEquals(escher.getDy1(), 117);
        anchor.setDy2(118);
        assertEquals(anchor.getDy2(), 118);
        assertEquals(escher.getDy2(), 118);
    }

    public void testChildAnchorFromEscher(){
        EscherChildAnchorRecord escher = new EscherChildAnchorRecord();
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);

        HSSFChildAnchor anchor = new HSSFChildAnchor(escher);
        assertEquals(anchor.getDx1(), 15);
        assertEquals(escher.getDx1(), 15);
        assertEquals(anchor.getDx2(), 16);
        assertEquals(escher.getDx2(), 16);
        assertEquals(anchor.getDy1(), 17);
        assertEquals(escher.getDy1(), 17);
        assertEquals(anchor.getDy2(), 18);
        assertEquals(escher.getDy2(), 18);
    }

    public void testChildAnchorFromScratch(){
        HSSFChildAnchor anchor = new HSSFChildAnchor();
        EscherChildAnchorRecord escher = (EscherChildAnchorRecord) anchor.getEscherAnchor();
        anchor.setAnchor(11, 12, 13, 14);

        assertEquals(anchor.getDx1(), 11);
        assertEquals(escher.getDx1(), 11);
        assertEquals(anchor.getDx2(), 13);
        assertEquals(escher.getDx2(), 13);
        assertEquals(anchor.getDy1(), 12);
        assertEquals(escher.getDy1(), 12);
        assertEquals(anchor.getDy2(), 14);
        assertEquals(escher.getDy2(), 14);

        anchor.setDx1(115);
        assertEquals(anchor.getDx1(), 115);
        assertEquals(escher.getDx1(), 115);
        anchor.setDx2(116);
        assertEquals(anchor.getDx2(), 116);
        assertEquals(escher.getDx2(), 116);
        anchor.setDy1(117);
        assertEquals(anchor.getDy1(), 117);
        assertEquals(escher.getDy1(), 117);
        anchor.setDy2(118);
        assertEquals(anchor.getDy2(), 118);
        assertEquals(escher.getDy2(), 118);
    }
}
