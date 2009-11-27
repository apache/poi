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

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestCellComment;

/**
 * Tests TestHSSFCellComment.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFComment extends BaseTestCellComment {

    @Override
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }

    public static void testDefaultShapeType() throws Exception {
        HSSFComment comment = new HSSFComment((HSSFShape)null, (HSSFAnchor)null);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
    }

    /**
     * test that we can read cell comments from an existing workbook.
     */
    public void testReadComments() {
        readComments("SimpleWithComments.xls");
    }

    /**
     * test that we can modify existing cell comments
     */
    public void testModifyComments() throws IOException {
        modifyComments("SimpleWithComments.xls");
    }

    public void testDeleteComments() throws Exception {
        deleteComments("SimpleWithComments.xls");
    }

    /**
     *  HSSFCell#findCellComment should NOT rely on the order of records
     * when matching cells and their cell comments. The correct algorithm is to map
     */
    public static void test47924() {
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
}
