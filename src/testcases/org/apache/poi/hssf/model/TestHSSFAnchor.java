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

import junit.framework.TestCase;
import org.apache.poi.ddf.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.*;

/**
 * @author Evgeniy Berlog
 * @date 12.06.12
 */
public class TestHSSFAnchor extends TestCase {

    public void testDefaultValues(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor();
        assertEquals(clientAnchor.getAnchorType(), 0);
        assertEquals(clientAnchor.getCol1(), 0);
        assertEquals(clientAnchor.getCol2(), 0);
        assertEquals(clientAnchor.getDx1(), 0);
        assertEquals(clientAnchor.getDx2(), 0);
        assertEquals(clientAnchor.getDy1(), 0);
        assertEquals(clientAnchor.getDy2(), 0);
        assertEquals(clientAnchor.getRow1(), 0);
        assertEquals(clientAnchor.getRow2(), 0);

        clientAnchor = new HSSFClientAnchor(new EscherClientAnchorRecord());
        assertEquals(clientAnchor.getAnchorType(), 0);
        assertEquals(clientAnchor.getCol1(), 0);
        assertEquals(clientAnchor.getCol2(), 0);
        assertEquals(clientAnchor.getDx1(), 0);
        assertEquals(clientAnchor.getDx2(), 0);
        assertEquals(clientAnchor.getDy1(), 0);
        assertEquals(clientAnchor.getDy2(), 0);
        assertEquals(clientAnchor.getRow1(), 0);
        assertEquals(clientAnchor.getRow2(), 0);

        HSSFChildAnchor childAnchor = new HSSFChildAnchor();
        assertEquals(childAnchor.getDx1(), 0);
        assertEquals(childAnchor.getDx2(), 0);
        assertEquals(childAnchor.getDy1(), 0);
        assertEquals(childAnchor.getDy2(), 0);

        childAnchor = new HSSFChildAnchor(new EscherChildAnchorRecord());
        assertEquals(childAnchor.getDx1(), 0);
        assertEquals(childAnchor.getDx2(), 0);
        assertEquals(childAnchor.getDy1(), 0);
        assertEquals(childAnchor.getDy2(), 0);
    }

    public void testCorrectOrderInSpContainer(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("pictures");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();

        HSSFSimpleShape rectangle = (HSSFSimpleShape) drawing.getChildren().get(0);

        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(0).getRecordId(), EscherSpRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(1).getRecordId(), EscherOptRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(2).getRecordId(), EscherClientAnchorRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(3).getRecordId(), EscherClientDataRecord.RECORD_ID);

        rectangle.setAnchor(new HSSFClientAnchor());

        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(0).getRecordId(), EscherSpRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(1).getRecordId(), EscherOptRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(2).getRecordId(), EscherClientAnchorRecord.RECORD_ID);
        assertEquals(HSSFTestHelper.getEscherContainer(rectangle).getChild(3).getRecordId(), EscherClientDataRecord.RECORD_ID);
    }

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

        assertNotNull(HSSFTestHelper.getEscherAnchor(anchor));
        assertNotNull(HSSFTestHelper.getEscherContainer(rectangle));
        assertTrue(HSSFTestHelper.getEscherAnchor(anchor).equals(HSSFTestHelper.getEscherContainer(rectangle).getChildById(EscherClientAnchorRecord.RECORD_ID)));
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
        EscherClientAnchorRecord escher = (EscherClientAnchorRecord) HSSFTestHelper.getEscherAnchor(anchor);
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
        EscherChildAnchorRecord escher = (EscherChildAnchorRecord) HSSFTestHelper.getEscherAnchor(anchor);
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

    public void testEqualsToSelf(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertEquals(clientAnchor, clientAnchor);

        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertEquals(childAnchor, childAnchor);
    }

    public void testPassIncompatibleTypeIsFalse(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertNotSame(clientAnchor, "wrongType");

        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertNotSame(childAnchor, "wrongType");
    }

    public void testNullReferenceIsFalse() {
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertFalse("Passing null to equals should return false", clientAnchor.equals(null));

        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertFalse("Passing null to equals should return false", childAnchor.equals(null));
    }

    public void testEqualsIsReflexiveIsSymmetric() {
        HSSFClientAnchor clientAnchor1 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        HSSFClientAnchor clientAnchor2 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);

        assertTrue(clientAnchor1.equals(clientAnchor2));
        assertTrue(clientAnchor1.equals(clientAnchor2));

        HSSFChildAnchor childAnchor1 = new HSSFChildAnchor(0, 1, 2, 3);
        HSSFChildAnchor childAnchor2 = new HSSFChildAnchor(0, 1, 2, 3);

        assertTrue(childAnchor1.equals(childAnchor2));
        assertTrue(childAnchor2.equals(childAnchor1));
    }

    public void testEqualsValues(){
        HSSFClientAnchor clientAnchor1 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        HSSFClientAnchor clientAnchor2 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setDx1(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setDx1(0);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setDy1(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setDy1(1);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setDx2(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setDx2(2);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setDy2(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setDy2(3);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setCol1(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setCol1(4);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setRow1(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setRow1(5);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setCol2(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setCol2(6);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setRow2(10);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setRow2(7);
        assertEquals(clientAnchor1, clientAnchor2);

        clientAnchor2.setAnchorType(3);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setAnchorType(0);
        assertEquals(clientAnchor1, clientAnchor2);

        HSSFChildAnchor childAnchor1 = new HSSFChildAnchor(0, 1, 2, 3);
        HSSFChildAnchor childAnchor2 = new HSSFChildAnchor(0, 1, 2, 3);

        childAnchor1.setDx1(10);
        assertNotSame(childAnchor1, childAnchor2);
        childAnchor1.setDx1(0);
        assertEquals(childAnchor1, childAnchor2);

        childAnchor2.setDy1(10);
        assertNotSame(childAnchor1, childAnchor2);
        childAnchor2.setDy1(1);
        assertEquals(childAnchor1, childAnchor2);

        childAnchor2.setDx2(10);
        assertNotSame(childAnchor1, childAnchor2);
        childAnchor2.setDx2(2);
        assertEquals(childAnchor1, childAnchor2);

        childAnchor2.setDy2(10);
        assertNotSame(childAnchor1, childAnchor2);
        childAnchor2.setDy2(3);
        assertEquals(childAnchor1, childAnchor2);
    }

    public void testFlipped(){
        HSSFChildAnchor child = new HSSFChildAnchor(2,2,1,1);
        assertEquals(child.isHorizontallyFlipped(), true);
        assertEquals(child.isVerticallyFlipped(), true);
        assertEquals(child.getDx1(), 1);
        assertEquals(child.getDx2(), 2);
        assertEquals(child.getDy1(), 1);
        assertEquals(child.getDy2(), 2);

        child = new HSSFChildAnchor(3,3,4,4);
        assertEquals(child.isHorizontallyFlipped(), false);
        assertEquals(child.isVerticallyFlipped(), false);
        assertEquals(child.getDx1(), 3);
        assertEquals(child.getDx2(), 4);
        assertEquals(child.getDy1(), 3);
        assertEquals(child.getDy2(), 4);

        HSSFClientAnchor client = new HSSFClientAnchor(1,1,1,1, (short)4,4,(short)3,3);
        assertEquals(client.isVerticallyFlipped(), true);
        assertEquals(client.isHorizontallyFlipped(), true);
        assertEquals(client.getCol1(), 3);
        assertEquals(client.getCol2(), 4);
        assertEquals(client.getRow1(), 3);
        assertEquals(client.getRow2(), 4);

        client = new HSSFClientAnchor(1,1,1,1, (short)5,5,(short)6,6);
        assertEquals(client.isVerticallyFlipped(), false);
        assertEquals(client.isHorizontallyFlipped(), false);
        assertEquals(client.getCol1(), 5);
        assertEquals(client.getCol2(), 6);
        assertEquals(client.getRow1(), 5);
        assertEquals(client.getRow2(), 6);
    }
}
