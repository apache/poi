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

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestCellComment;
import org.apache.poi.ss.usermodel.ClientAnchor;

/**
 * Tests TestHSSFCellComment.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFComment extends BaseTestCellComment {

    public TestHSSFComment() {
        super(HSSFITestDataProvider.instance);
    }

    public void testDefaultShapeType() {
        HSSFComment comment = new HSSFComment((HSSFShape)null, new HSSFClientAnchor());
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
    }

    /**
     *  HSSFCell#findCellComment should NOT rely on the order of records
     * when matching cells and their cell comments. The correct algorithm is to map
     */
    public void test47924() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("47924.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFCell cell;
        HSSFComment comment;

        cell = sheet.getRow(0).getCell(0);
        comment = cell.getCellComment();
        assertEquals("a1", comment.getString().getString());

        cell = sheet.getRow(1).getCell(0);
        comment = cell.getCellComment();
        assertEquals("a2", comment.getString().getString());

        cell = sheet.getRow(2).getCell(0);
        comment = cell.getCellComment();
        assertEquals("a3", comment.getString().getString());

        cell = sheet.getRow(2).getCell(2);
        comment = cell.getCellComment();
        assertEquals("c3", comment.getString().getString());

        cell = sheet.getRow(4).getCell(1);
        comment = cell.getCellComment();
        assertEquals("b5", comment.getString().getString());

        cell = sheet.getRow(5).getCell(2);
        comment = cell.getCellComment();
        assertEquals("c6", comment.getString().getString());
    }

    public void testGetClientAnchor() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFPatriarch drawing = sheet.createDrawingPatriarch();
        HSSFComment comment;
        ClientAnchor anchor;

        comment = drawing.createCellComment(new HSSFClientAnchor(101, 102, 103, 104, (short) 1, 2, (short) 3, 4));
        anchor = comment.getClientAnchor();
        assertEquals(101, anchor.getDx1());
        assertEquals(102, anchor.getDy1());
        assertEquals(103, anchor.getDx2());
        assertEquals(104, anchor.getDy2());
        assertEquals(1, anchor.getCol1());
        assertEquals(2, anchor.getRow1());
        assertEquals(3, anchor.getCol2());
        assertEquals(4, anchor.getRow2());

        comment = drawing.createCellComment(new HSSFClientAnchor());
        anchor = comment.getClientAnchor();
        assertEquals(0, anchor.getDx1());
        assertEquals(0, anchor.getDy1());
        assertEquals(0, anchor.getDx2());
        assertEquals(0, anchor.getDy2());
        assertEquals(0, anchor.getCol1());
        assertEquals(0, anchor.getRow1());
        assertEquals(0, anchor.getCol2());
        assertEquals(0, anchor.getRow2());
    }
}
