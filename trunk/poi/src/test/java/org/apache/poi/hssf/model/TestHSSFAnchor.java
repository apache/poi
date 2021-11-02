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

import static org.apache.poi.hssf.usermodel.HSSFTestHelper.getEscherContainer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ddf.EscherChildAnchorRecord;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFAnchor;
import org.apache.poi.hssf.usermodel.HSSFChildAnchor;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFTestHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.junit.jupiter.api.Test;

class TestHSSFAnchor {

    @Test
    void testDefaultValues(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor();
        assertEquals(AnchorType.MOVE_AND_RESIZE, clientAnchor.getAnchorType());
        assertEquals(0, clientAnchor.getCol1());
        assertEquals(0, clientAnchor.getCol2());
        assertEquals(0, clientAnchor.getDx1());
        assertEquals(0, clientAnchor.getDx2());
        assertEquals(0, clientAnchor.getDy1());
        assertEquals(0, clientAnchor.getDy2());
        assertEquals(0, clientAnchor.getRow1());
        assertEquals(0, clientAnchor.getRow2());

        clientAnchor = new HSSFClientAnchor(new EscherClientAnchorRecord());
        assertEquals(AnchorType.MOVE_AND_RESIZE, clientAnchor.getAnchorType());
        assertEquals(0, clientAnchor.getCol1());
        assertEquals(0, clientAnchor.getCol2());
        assertEquals(0, clientAnchor.getDx1());
        assertEquals(0, clientAnchor.getDx2());
        assertEquals(0, clientAnchor.getDy1());
        assertEquals(0, clientAnchor.getDy2());
        assertEquals(0, clientAnchor.getRow1());
        assertEquals(0, clientAnchor.getRow2());

        HSSFChildAnchor childAnchor = new HSSFChildAnchor();
        assertEquals(0, childAnchor.getDx1());
        assertEquals(0, childAnchor.getDx2());
        assertEquals(0, childAnchor.getDy1());
        assertEquals(0, childAnchor.getDy2());

        childAnchor = new HSSFChildAnchor(new EscherChildAnchorRecord());
        assertEquals(0, childAnchor.getDx1());
        assertEquals(0, childAnchor.getDx2());
        assertEquals(0, childAnchor.getDy1());
        assertEquals(0, childAnchor.getDy2());
    }

    @Test
    void testCorrectOrderInSpContainer() throws IOException {
        int[] expIds = {
            EscherSpRecord.RECORD_ID,
            EscherOptRecord.RECORD_ID,
            EscherClientAnchorRecord.RECORD_ID,
            EscherClientDataRecord.RECORD_ID
        };

        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls")) {
            HSSFSheet sheet = wb.getSheet("pictures");
            HSSFPatriarch drawing = sheet.getDrawingPatriarch();

            HSSFSimpleShape rectangle = (HSSFSimpleShape) drawing.getChildren().get(0);
            int[] act1Ids = getEscherContainer(rectangle).getChildRecords().stream().mapToInt(EscherRecord::getRecordId).toArray();
            assertArrayEquals(expIds, act1Ids);

            rectangle.setAnchor(new HSSFClientAnchor());
            int[] act2Ids = getEscherContainer(rectangle).getChildRecords().stream().mapToInt(EscherRecord::getRecordId).toArray();
            assertArrayEquals(expIds, act2Ids);
        }
    }

    @Test
    void testCreateClientAnchorFromContainer(){
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
        assertNotNull(anchor);
        assertEquals(11, anchor.getCol1());
        assertEquals(11, escher.getCol1());
        assertEquals(12, anchor.getCol2());
        assertEquals(12, escher.getCol2());
        assertEquals(13, anchor.getRow1());
        assertEquals(13, escher.getRow1());
        assertEquals(14, anchor.getRow2());
        assertEquals(14, escher.getRow2());
        assertEquals(15, anchor.getDx1());
        assertEquals(15, escher.getDx1());
        assertEquals(16, anchor.getDx2());
        assertEquals(16, escher.getDx2());
        assertEquals(17, anchor.getDy1());
        assertEquals(17, escher.getDy1());
        assertEquals(18, anchor.getDy2());
        assertEquals(18, escher.getDy2());
    }

    @Test
    void testCreateChildAnchorFromContainer(){
        EscherContainerRecord container = new EscherContainerRecord();
        EscherChildAnchorRecord escher = new EscherChildAnchorRecord();
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);
        container.addChildRecord(escher);

        HSSFChildAnchor anchor = (HSSFChildAnchor) HSSFAnchor.createAnchorFromEscher(container);
        assertNotNull(anchor);
        assertEquals(15, anchor.getDx1());
        assertEquals(15, escher.getDx1());
        assertEquals(16, anchor.getDx2());
        assertEquals(16, escher.getDx2());
        assertEquals(17, anchor.getDy1());
        assertEquals(17, escher.getDy1());
        assertEquals(18, anchor.getDy2());
        assertEquals(18, escher.getDy2());
    }

    @Test
    void testShapeEscherMustHaveAnchorRecord(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 200, 200, (short)2, 2, (short)15, 15);
        anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);

        HSSFSimpleShape rectangle = drawing.createSimpleShape(anchor);
        rectangle.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);

        rectangle.setAnchor(anchor);

        assertNotNull(HSSFTestHelper.getEscherAnchor(anchor));
        assertNotNull(getEscherContainer(rectangle));
        assertEquals(HSSFTestHelper.getEscherAnchor(anchor), getEscherContainer(rectangle).getChildById(EscherClientAnchorRecord.RECORD_ID));
    }

    @Test
    void testClientAnchorFromEscher(){
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
        assertEquals(11, anchor.getCol1());
        assertEquals(11, escher.getCol1());
        assertEquals(12, anchor.getCol2());
        assertEquals(12, escher.getCol2());
        assertEquals(13, anchor.getRow1());
        assertEquals(13, escher.getRow1());
        assertEquals(14, anchor.getRow2());
        assertEquals(14, escher.getRow2());
        assertEquals(15, anchor.getDx1());
        assertEquals(15, escher.getDx1());
        assertEquals(16, anchor.getDx2());
        assertEquals(16, escher.getDx2());
        assertEquals(17, anchor.getDy1());
        assertEquals(17, escher.getDy1());
        assertEquals(18, anchor.getDy2());
        assertEquals(18, escher.getDy2());
    }

    @Test
    void testClientAnchorFromScratch(){
        HSSFClientAnchor anchor = new HSSFClientAnchor();
        EscherClientAnchorRecord escher = (EscherClientAnchorRecord) HSSFTestHelper.getEscherAnchor(anchor);
        anchor.setAnchor((short)11, 12, 13, 14, (short)15, 16, 17, 18);

        assertEquals(11, anchor.getCol1());
        assertEquals(11, escher.getCol1());
        assertEquals(15, anchor.getCol2());
        assertEquals(15, escher.getCol2());
        assertEquals(12, anchor.getRow1());
        assertEquals(12, escher.getRow1());
        assertEquals(16, anchor.getRow2());
        assertEquals(16, escher.getRow2());
        assertEquals(13, anchor.getDx1());
        assertEquals(13, escher.getDx1());
        assertEquals(17, anchor.getDx2());
        assertEquals(17, escher.getDx2());
        assertEquals(14, anchor.getDy1());
        assertEquals(14, escher.getDy1());
        assertEquals(18, anchor.getDy2());
        assertEquals(18, escher.getDy2());

        anchor.setCol1(111);
        assertEquals(111, anchor.getCol1());
        assertEquals(111, escher.getCol1());
        anchor.setCol2(112);
        assertEquals(112, anchor.getCol2());
        assertEquals(112, escher.getCol2());
        anchor.setRow1(113);
        assertEquals(113, anchor.getRow1());
        assertEquals(113, escher.getRow1());
        anchor.setRow2(114);
        assertEquals(114, anchor.getRow2());
        assertEquals(114, escher.getRow2());
        anchor.setDx1(115);
        assertEquals(115, anchor.getDx1());
        assertEquals(115, escher.getDx1());
        anchor.setDx2(116);
        assertEquals(116, anchor.getDx2());
        assertEquals(116, escher.getDx2());
        anchor.setDy1(117);
        assertEquals(117, anchor.getDy1());
        assertEquals(117, escher.getDy1());
        anchor.setDy2(118);
        assertEquals(118, anchor.getDy2());
        assertEquals(118, escher.getDy2());
    }

    @Test
    void testChildAnchorFromEscher(){
        EscherChildAnchorRecord escher = new EscherChildAnchorRecord();
        escher.setDx1((short) 15);
        escher.setDx2((short) 16);
        escher.setDy1((short) 17);
        escher.setDy2((short) 18);

        HSSFChildAnchor anchor = new HSSFChildAnchor(escher);
        assertEquals(15, anchor.getDx1());
        assertEquals(15, escher.getDx1());
        assertEquals(16, anchor.getDx2());
        assertEquals(16, escher.getDx2());
        assertEquals(17, anchor.getDy1());
        assertEquals(17, escher.getDy1());
        assertEquals(18, anchor.getDy2());
        assertEquals(18, escher.getDy2());
    }

    @Test
    void testChildAnchorFromScratch(){
        HSSFChildAnchor anchor = new HSSFChildAnchor();
        EscherChildAnchorRecord escher = (EscherChildAnchorRecord) HSSFTestHelper.getEscherAnchor(anchor);
        anchor.setAnchor(11, 12, 13, 14);

        assertEquals(11, anchor.getDx1());
        assertEquals(11, escher.getDx1());
        assertEquals(13, anchor.getDx2());
        assertEquals(13, escher.getDx2());
        assertEquals(12, anchor.getDy1());
        assertEquals(12, escher.getDy1());
        assertEquals(14, anchor.getDy2());
        assertEquals(14, escher.getDy2());

        anchor.setDx1(115);
        assertEquals(115, anchor.getDx1());
        assertEquals(115, escher.getDx1());
        anchor.setDx2(116);
        assertEquals(116, anchor.getDx2());
        assertEquals(116, escher.getDx2());
        anchor.setDy1(117);
        assertEquals(117, anchor.getDy1());
        assertEquals(117, escher.getDy1());
        anchor.setDy2(118);
        assertEquals(118, anchor.getDy2());
        assertEquals(118, escher.getDy2());
    }

    @Test
    void testEqualsToSelf(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertEquals(clientAnchor, clientAnchor);

        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertEquals(childAnchor, childAnchor);
    }

    @Test
    void testPassIncompatibleTypeIsFalse(){
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertNotEquals(clientAnchor, childAnchor);
    }

    @Test
    void testNullReferenceIsFalse() {
        HSSFClientAnchor clientAnchor = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        assertNotNull(clientAnchor, "Passing null to equals should return false");

        HSSFChildAnchor childAnchor = new HSSFChildAnchor(0, 1, 2, 3);
        assertNotNull(childAnchor, "Passing null to equals should return false");
    }

    @Test
    void testEqualsIsReflexiveIsSymmetric() {
        HSSFClientAnchor clientAnchor1 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);
        HSSFClientAnchor clientAnchor2 = new HSSFClientAnchor(0, 1, 2, 3, (short)4, 5, (short)6, 7);

        assertEquals(clientAnchor1, clientAnchor2);
        assertEquals(clientAnchor1, clientAnchor2);

        HSSFChildAnchor childAnchor1 = new HSSFChildAnchor(0, 1, 2, 3);
        HSSFChildAnchor childAnchor2 = new HSSFChildAnchor(0, 1, 2, 3);

        assertEquals(childAnchor1, childAnchor2);
        assertEquals(childAnchor2, childAnchor1);
    }

    @Test
    void testEqualsValues(){
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

        clientAnchor2.setAnchorType(AnchorType.DONT_MOVE_AND_RESIZE);
        assertNotSame(clientAnchor1, clientAnchor2);
        clientAnchor2.setAnchorType(AnchorType.MOVE_AND_RESIZE);
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

    @Test
    void testFlipped(){
        HSSFChildAnchor child = new HSSFChildAnchor(2,2,1,1);
        assertTrue(child.isHorizontallyFlipped());
        assertTrue(child.isVerticallyFlipped());
        assertEquals(1, child.getDx1());
        assertEquals(2, child.getDx2());
        assertEquals(1, child.getDy1());
        assertEquals(2, child.getDy2());

        child = new HSSFChildAnchor(3,3,4,4);
        assertFalse(child.isHorizontallyFlipped());
        assertFalse(child.isVerticallyFlipped());
        assertEquals(3, child.getDx1());
        assertEquals(4, child.getDx2());
        assertEquals(3, child.getDy1());
        assertEquals(4, child.getDy2());

        HSSFClientAnchor client = new HSSFClientAnchor(1,1,1,1, (short)4,4,(short)3,3);
        assertTrue(client.isVerticallyFlipped());
        assertTrue(client.isHorizontallyFlipped());
        assertEquals(3, client.getCol1());
        assertEquals(4, client.getCol2());
        assertEquals(3, client.getRow1());
        assertEquals(4, client.getRow2());

        client = new HSSFClientAnchor(1,1,1,1, (short)5,5,(short)6,6);
        assertFalse(client.isVerticallyFlipped());
        assertFalse(client.isHorizontallyFlipped());
        assertEquals(5, client.getCol1());
        assertEquals(6, client.getCol2());
        assertEquals(5, client.getRow1());
        assertEquals(6, client.getRow2());
    }
}
