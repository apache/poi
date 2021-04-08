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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.ddf.EscherDgRecord;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.ss.usermodel.BaseTestCloneSheet;
import org.junit.jupiter.api.Test;

/**
 * Test the ability to clone a sheet.
 *  If adding new records that belong to a sheet (as opposed to a book)
 *  add that record to the sheet in the testCloneSheetBasic method.
 * @author  avik
 */
final class TestCloneSheet extends BaseTestCloneSheet {
    public TestCloneSheet() {
        super(HSSFITestDataProvider.instance);
    }

    @Test
    void testCloneSheetWithoutDrawings(){
        HSSFWorkbook b = new HSSFWorkbook();
        HSSFSheet s = b.createSheet("Test");
        HSSFSheet s2 = s.cloneSheet(b);

        assertNull(s.getDrawingPatriarch());
        assertNull(s2.getDrawingPatriarch());
        assertEquals(HSSFTestHelper.getSheetForTest(s).getRecords().size(), HSSFTestHelper.getSheetForTest(s2).getRecords().size());
    }

    @Test
    void testCloneSheetWithEmptyDrawingAggregate(){
        HSSFWorkbook b = new HSSFWorkbook();
        HSSFSheet s = b.createSheet("Test");
        HSSFPatriarch patriarch = s.createDrawingPatriarch();

        EscherAggregate agg1 = patriarch.getBoundAggregate();

        HSSFSheet s2 = s.cloneSheet(b);

        patriarch = s2.getDrawingPatriarch();

        EscherAggregate agg2 = patriarch.getBoundAggregate();

        EscherSpRecord sp1 = (EscherSpRecord) agg1.getEscherContainer().getChild(1).getChild(0).getChild(1);
        EscherSpRecord sp2 = (EscherSpRecord) agg2.getEscherContainer().getChild(1).getChild(0).getChild(1);

        assertEquals(sp1.getShapeId(), 1024);
        assertEquals(sp2.getShapeId(), 2048);

        EscherDgRecord dg = (EscherDgRecord) agg2.getEscherContainer().getChild(0);

        assertEquals(dg.getLastMSOSPID(), 2048);
        assertEquals(dg.getInstance(), 0x2);

        //everything except id and DgRecord.lastMSOSPID and DgRecord.Instance must be the same

        sp2.setShapeId(1024);
        dg.setLastMSOSPID(1024);
        dg.setInstance((short) 0x1);

        assertEquals(agg1.serialize().length, agg2.serialize().length);
        assertEquals(agg1.toXml(""), agg2.toXml(""));
        assertArrayEquals(agg1.serialize(), agg2.serialize());
    }

    @Test
    void testCloneComment() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch p = sh.createDrawingPatriarch();
        HSSFComment c = p.createComment(new HSSFClientAnchor(0,0,100,100, (short) 0,0,(short)5,5));
        c.setColumn(1);
        c.setRow(2);
        c.setString(new HSSFRichTextString("qwertyuio"));

        HSSFSheet sh2 = wb.cloneSheet(0);
        HSSFPatriarch p2 = sh2.getDrawingPatriarch();
        HSSFComment c2 = (HSSFComment) p2.getChildren().get(0);

        assertEquals(c.getString(), c2.getString());
        assertEquals(c.getRow(), c2.getRow());
        assertEquals(c.getColumn(), c2.getColumn());

        // The ShapeId is not equal?
        // assertEquals(c.getNoteRecord().getShapeId(), c2.getNoteRecord().getShapeId());

        assertArrayEquals(c2.getTextObjectRecord().serialize(), c.getTextObjectRecord().serialize());

        // ShapeId is different
        CommonObjectDataSubRecord subRecord = (CommonObjectDataSubRecord) c2.getObjRecord().getSubRecords().get(0);
        subRecord.setObjectId(1025);

        assertArrayEquals(c2.getObjRecord().serialize(), c.getObjRecord().serialize());

        // ShapeId is different
        c2.getNoteRecord().setShapeId(1025);
        assertArrayEquals(c2.getNoteRecord().serialize(), c.getNoteRecord().serialize());

        //everything except spRecord.shapeId must be the same
        assertFalse(Arrays.equals(c2.getEscherContainer().serialize(), c.getEscherContainer().serialize()));
        EscherSpRecord sp = (EscherSpRecord) c2.getEscherContainer().getChild(0);
        sp.setShapeId(1025);
        assertArrayEquals(c2.getEscherContainer().serialize(), c.getEscherContainer().serialize());

        wb.close();
    }
}
