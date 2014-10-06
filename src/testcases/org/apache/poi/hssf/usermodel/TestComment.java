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
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.CommentShape;
import org.apache.poi.hssf.model.HSSFTestModelHelper;
import org.apache.poi.hssf.record.*;

import java.io.*;
import java.util.Arrays;

/**
 * @author Evgeniy Berlog
 * @date 26.06.12
 */
public class TestComment extends TestCase {

    public void testResultEqualsToAbstractShape() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        HSSFRow row = sh.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellComment(comment);

        CommentShape commentShape = HSSFTestModelHelper.createCommentShape(1025, comment);

        assertEquals(comment.getEscherContainer().getChildRecords().size(), 5);
        assertEquals(commentShape.getSpContainer().getChildRecords().size(), 5);

        //sp record
        byte[] expected = commentShape.getSpContainer().getChild(0).serialize();
        byte[] actual = comment.getEscherContainer().getChild(0).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        expected = commentShape.getSpContainer().getChild(2).serialize();
        actual = comment.getEscherContainer().getChild(2).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        expected = commentShape.getSpContainer().getChild(3).serialize();
        actual = comment.getEscherContainer().getChild(3).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        expected = commentShape.getSpContainer().getChild(4).serialize();
        actual = comment.getEscherContainer().getChild(4).serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        ObjRecord obj = comment.getObjRecord();
        ObjRecord objShape = commentShape.getObjRecord();
        /**shapeId = 1025 % 1024**/
        ((CommonObjectDataSubRecord)objShape.getSubRecords().get(0)).setObjectId(1);

        expected = obj.serialize();
        actual = objShape.serialize();

        TextObjectRecord tor = comment.getTextObjectRecord();
        TextObjectRecord torShape = commentShape.getTextObjectRecord();

        expected = tor.serialize();
        actual = torShape.serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));

        NoteRecord note = comment.getNoteRecord();
        NoteRecord noteShape = commentShape.getNoteRecord();
        noteShape.setShapeId(1);

        expected = note.serialize();
        actual = noteShape.serialize();

        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected, actual));
    }

    public void testAddToExistingFile() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        int idx = wb.addPicture(new byte[]{1,2,3}, HSSFWorkbook.PICTURE_TYPE_PNG);

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        comment.setColumn(5);
        comment.setString(new HSSFRichTextString("comment1"));
        comment = patriarch.createCellComment(new HSSFClientAnchor(0,0,100,100,(short)0,0,(short)10,10));
        comment.setRow(5);
        comment.setString(new HSSFRichTextString("comment2"));
        comment.setBackgroundImage(idx);
        assertEquals(comment.getBackgroundImageId(), idx);

        assertEquals(patriarch.getChildren().size(), 2);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        comment = (HSSFComment) patriarch.getChildren().get(1);
        assertEquals(comment.getBackgroundImageId(), idx);
        comment.resetBackgroundImage();
        assertEquals(comment.getBackgroundImageId(), 0);

        assertEquals(patriarch.getChildren().size(), 2);
        comment = patriarch.createCellComment(new HSSFClientAnchor());
        comment.setString(new HSSFRichTextString("comment3"));

        assertEquals(patriarch.getChildren().size(), 3);
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        comment = (HSSFComment) patriarch.getChildren().get(1);
        assertEquals(comment.getBackgroundImageId(), 0);
        assertEquals(patriarch.getChildren().size(), 3);
        assertEquals(((HSSFComment) patriarch.getChildren().get(0)).getString().getString(), "comment1");
        assertEquals(((HSSFComment) patriarch.getChildren().get(1)).getString().getString(), "comment2");
        assertEquals(((HSSFComment) patriarch.getChildren().get(2)).getString().getString(), "comment3");
    }

    public void testSetGetProperties() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        comment.setString(new HSSFRichTextString("comment1"));
        assertEquals(comment.getString().getString(), "comment1");

        comment.setAuthor("poi");
        assertEquals(comment.getAuthor(), "poi");

        comment.setColumn(3);
        assertEquals(comment.getColumn(), 3);

        comment.setRow(4);
        assertEquals(comment.getRow(), 4);

        comment.setVisible(false);
        assertEquals(comment.isVisible(), false);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        comment = (HSSFComment) patriarch.getChildren().get(0);

        assertEquals(comment.getString().getString(), "comment1");
        assertEquals("poi", comment.getAuthor());
        assertEquals(comment.getColumn(), 3);
        assertEquals(comment.getRow(), 4);
        assertEquals(comment.isVisible(), false);

        comment.setString(new HSSFRichTextString("comment12"));
        comment.setAuthor("poi2");
        comment.setColumn(32);
        comment.setRow(42);
        comment.setVisible(true);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        comment = (HSSFComment) patriarch.getChildren().get(0);

        assertEquals(comment.getString().getString(), "comment12");
        assertEquals("poi2", comment.getAuthor());
        assertEquals(comment.getColumn(), 32);
        assertEquals(comment.getRow(), 42);
        assertEquals(comment.isVisible(), true);
    }

    public void testExistingFileWithComment(){
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("comments");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());
        HSSFComment comment = (HSSFComment) drawing.getChildren().get(0);
        assertEquals(comment.getAuthor(), "evgeniy");
        assertEquals(comment.getString().getString(), "evgeniy:\npoi test");
        assertEquals(comment.getColumn(), 1);
        assertEquals(comment.getRow(), 2);
    }

    public void testFindComments(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        HSSFRow row = sh.createRow(5);
        HSSFCell cell = row.createCell(4);
        cell.setCellComment(comment);

        HSSFTestModelHelper.createCommentShape(0, comment);

        assertNotNull(sh.findCellComment(5, 4));
        assertNull(sh.findCellComment(5, 5));

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wb.getSheetAt(0);

        assertNotNull(sh.findCellComment(5, 4));
        assertNull(sh.findCellComment(5, 5));
    }

    public void testInitState(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        EscherAggregate agg = HSSFTestHelper.getEscherAggregate(patriarch);
        assertEquals(agg.getTailRecords().size(), 0);

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        assertEquals(agg.getTailRecords().size(), 1);

        HSSFSimpleShape shape = patriarch.createSimpleShape(new HSSFClientAnchor());

        assertEquals(comment.getOptRecord().getEscherProperties().size(), 10);
    }

    public void testShapeId(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());

        comment.setShapeId(2024);
        /**
         * SpRecord.id == shapeId
         * ObjRecord.id == shapeId % 1024
         * NoteRecord.id == ObjectRecord.id == shapeId % 1024
         */

        assertEquals(comment.getShapeId(), 2024);

        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) comment.getObjRecord().getSubRecords().get(0);
        assertEquals(cod.getObjectId(), 1000);
        EscherSpRecord spRecord = (EscherSpRecord) comment.getEscherContainer().getChild(0);
        assertEquals(spRecord.getShapeId(), 2024);
        assertEquals(comment.getShapeId(), 2024);
        assertEquals(comment.getNoteRecord().getShapeId(), 1000);
    }
    
    public void testAttemptToSave2CommentsWithSameCoordinates(){
        Object err = null;
        
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        patriarch.createCellComment(new HSSFClientAnchor());
        patriarch.createCellComment(new HSSFClientAnchor());
        
        try{
            HSSFTestDataSamples.writeOutAndReadBack(wb);
        } catch (IllegalStateException e){
            err = 1;
            assertEquals(e.getMessage(), "found multiple cell comments for cell $A$1");
        }
        assertNotNull(err);
    }
}
