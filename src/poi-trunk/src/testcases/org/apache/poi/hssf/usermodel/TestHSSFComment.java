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

import static org.apache.poi.poifs.storage.RawDataUtil.decompress;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.ss.usermodel.BaseTestCellComment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

/**
 * Tests TestHSSFCellComment.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFComment extends BaseTestCellComment {

    public TestHSSFComment() {
        super(HSSFITestDataProvider.instance);
    }

    @Test
    public void defaultShapeType() {
        HSSFComment comment = new HSSFComment(null, new HSSFClientAnchor());
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
    }

    /**
     *  HSSFCell#findCellComment should NOT rely on the order of records
     * when matching cells and their cell comments. The correct algorithm is to map
     */
    @Test
    public void bug47924() throws IOException {
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
        
        wb.close();
    }
    
    @Test
    public void testBug56380InsertComments() throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        int noOfRows = 1025;
        String comment = "c";
        
        for(int i = 0; i < noOfRows; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);
            insertComment(drawing, cell, comment + i);
        }

        // assert that the comments are created properly before writing
        checkComments(sheet, noOfRows, comment);

        /*// store in temp-file
        OutputStream fs = new FileOutputStream("/tmp/56380.xls");
        try {
            sheet.getWorkbook().write(fs);
        } finally {
            fs.close();
        }*/
        
        // save and recreate the workbook from the saved file
        HSSFWorkbook workbookBack = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet = workbookBack.getSheetAt(0);
        
        // assert that the comments are created properly after reading back in
        checkComments(sheet, noOfRows, comment);
        
        workbook.close();
        workbookBack.close();
    }

    @Test
    public void testBug56380InsertTooManyComments() throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            String comment = "c";

            for (int rowNum = 0; rowNum < 258; rowNum++) {
                sheet.createRow(rowNum);
            }

            // should still work, for some reason DrawingManager2.allocateShapeId() skips the first 1024...
            for (int count = 1025; count < 65535; count++) {
                int rowNum = count / 255;
                int cellNum = count % 255;
                Cell cell = sheet.getRow(rowNum).createCell(cellNum);
                try {
                    Comment commentObj = insertComment(drawing, cell, comment + cellNum);

                    assertEquals(count, ((HSSFComment) commentObj).getNoteRecord().getShapeId());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("While adding shape number " + count, e);
                }
            }

            // this should now fail to insert
            Row row = sheet.createRow(257);
            Cell cell = row.createCell(0);
            insertComment(drawing, cell, comment + 0);
        }
    }

    private void checkComments(Sheet sheet, int noOfRows, String comment) {
        for(int i = 0; i < noOfRows; i++) {
            assertNotNull(sheet.getRow(i));
            assertNotNull(sheet.getRow(i).getCell(0));
            assertNotNull("Did not get a Cell Comment for row " + i, sheet.getRow(i).getCell(0).getCellComment());
            assertNotNull(sheet.getRow(i).getCell(0).getCellComment().getString());
            assertEquals(comment + i, sheet.getRow(i).getCell(0).getCellComment().getString().getString());
        }
    }

    private Comment insertComment(Drawing<?> drawing, Cell cell, String message) {
        CreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();
        
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 1);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 1);
        anchor.setDx1(100); 
        anchor.setDx2(100);
        anchor.setDy1(100);
        anchor.setDy2(100);
            
        Comment comment = drawing.createCellComment(anchor);
        
        RichTextString str = factory.createRichTextString(message);
        comment.setString(str);
        comment.setAuthor("fanfy");
        cell.setCellComment(comment);
        
        return comment;
    }
    
    @Test
    public void resultEqualsToNonExistingAbstractShape() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        HSSFRow row = sh.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellComment(comment);

        assertEquals(comment.getEscherContainer().getChildRecords().size(), 5);

        //sp record
        byte[] expected = decompress("H4sIAAAAAAAAAFvEw/WBg4GBgZEFSHAxMAAA9gX7nhAAAAA=");
        byte[] actual = comment.getEscherContainer().getChild(0).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPggxIANAABK4+laGgAAAA==");
        actual = comment.getEscherContainer().getChild(2).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNgEPzAAAQACl6c5QgAAAA=");
        actual = comment.getEscherContainer().getChild(3).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        expected = decompress("H4sIAAAAAAAAAGNg4P3AAAQA6pyIkQgAAAA=");
        actual = comment.getEscherContainer().getChild(4).serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        ObjRecord obj = comment.getObjRecord();

        expected = decompress("H4sIAAAAAAAAAItlMGEQZRBikGRgZBF0YEACvAxiDLgBAJZsuoU4AAAA");
        actual = obj.serialize();

        assertEquals(expected.length, actual.length);
        //assertArrayEquals(expected, actual);

        TextObjectRecord tor = comment.getTextObjectRecord();

        expected = decompress("H4sIAAAAAAAAANvGKMQgxMSABgBGi8T+FgAAAA==");
        actual = tor.serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        NoteRecord note = comment.getNoteRecord();

        expected = decompress("H4sIAAAAAAAAAJNh4GGAAEYWEAkAS0KXuRAAAAA=");
        actual = note.serialize();

        assertEquals(expected.length, actual.length);
        assertArrayEquals(expected, actual);

        wb.close();
    }

    @Test
    public void addToExistingFile() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();
        int idx = wb.addPicture(new byte[]{1,2,3}, Workbook.PICTURE_TYPE_PNG);

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        comment.setColumn(5);
        comment.setString(new HSSFRichTextString("comment1"));
        comment = patriarch.createCellComment(new HSSFClientAnchor(0,0,100,100,(short)0,0,(short)10,10));
        comment.setRow(5);
        comment.setString(new HSSFRichTextString("comment2"));
        comment.setBackgroundImage(idx);
        assertEquals(comment.getBackgroundImageId(), idx);

        assertEquals(patriarch.getChildren().size(), 2);

        HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wbBack.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();

        comment = (HSSFComment) patriarch.getChildren().get(1);
        assertEquals(comment.getBackgroundImageId(), idx);
        comment.resetBackgroundImage();
        assertEquals(comment.getBackgroundImageId(), 0);

        assertEquals(patriarch.getChildren().size(), 2);
        comment = patriarch.createCellComment(new HSSFClientAnchor());
        comment.setString(new HSSFRichTextString("comment3"));

        assertEquals(patriarch.getChildren().size(), 3);
        HSSFWorkbook wbBack2 = HSSFTestDataSamples.writeOutAndReadBack(wbBack);
        sh = wbBack2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        comment = (HSSFComment) patriarch.getChildren().get(1);
        assertEquals(comment.getBackgroundImageId(), 0);
        assertEquals(patriarch.getChildren().size(), 3);
        assertEquals(((HSSFComment) patriarch.getChildren().get(0)).getString().getString(), "comment1");
        assertEquals(((HSSFComment) patriarch.getChildren().get(1)).getString().getString(), "comment2");
        assertEquals(((HSSFComment) patriarch.getChildren().get(2)).getString().getString(), "comment3");
        
        wb.close();
        wbBack.close();
        wbBack2.close();
    }

    @Test
    public void setGetProperties() throws IOException {
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

        HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wbBack.getSheetAt(0);
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

        HSSFWorkbook wbBack2 = HSSFTestDataSamples.writeOutAndReadBack(wbBack);
        sh = wbBack2.getSheetAt(0);
        patriarch = sh.getDrawingPatriarch();
        comment = (HSSFComment) patriarch.getChildren().get(0);

        assertEquals(comment.getString().getString(), "comment12");
        assertEquals("poi2", comment.getAuthor());
        assertEquals(comment.getColumn(), 32);
        assertEquals(comment.getRow(), 42);
        assertEquals(comment.isVisible(), true);
        
        wb.close();
        wbBack.close();
        wbBack2.close();
    }

    @Test
    public void existingFileWithComment() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        HSSFSheet sheet = wb.getSheet("comments");
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        assertEquals(1, drawing.getChildren().size());
        HSSFComment comment = (HSSFComment) drawing.getChildren().get(0);
        assertEquals(comment.getAuthor(), "evgeniy");
        assertEquals(comment.getString().getString(), "evgeniy:\npoi test");
        assertEquals(comment.getColumn(), 1);
        assertEquals(comment.getRow(), 2);
        wb.close();
    }

    @Test
    public void findComments() throws IOException{
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        HSSFRow row = sh.createRow(5);
        HSSFCell cell = row.createCell(4);
        cell.setCellComment(comment);

        assertNotNull(sh.findCellComment(5, 4));
        assertNull(sh.findCellComment(5, 5));

        HSSFWorkbook wbBack = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sh = wbBack.getSheetAt(0);

        assertNotNull(sh.findCellComment(5, 4));
        assertNull(sh.findCellComment(5, 5));
        
        wb.close();
        wbBack.close();
    }

    @Test
    public void initState() throws IOException{
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        EscherAggregate agg = HSSFTestHelper.getEscherAggregate(patriarch);
        assertEquals(agg.getTailRecords().size(), 0);

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());
        assertEquals(agg.getTailRecords().size(), 1);

        HSSFSimpleShape shape = patriarch.createSimpleShape(new HSSFClientAnchor());
        assertNotNull(shape);

        assertEquals(comment.getOptRecord().getEscherProperties().size(), 10);
        
        wb.close();
    }

    @Test
    public void shapeId() throws IOException{
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet();
        HSSFPatriarch patriarch = sh.createDrawingPatriarch();

        HSSFComment comment = patriarch.createCellComment(new HSSFClientAnchor());

        comment.setShapeId(2024);

        assertEquals(comment.getShapeId(), 2024);

        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) comment.getObjRecord().getSubRecords().get(0);
        assertEquals(2024, cod.getObjectId());
        EscherSpRecord spRecord = (EscherSpRecord) comment.getEscherContainer().getChild(0);
        assertEquals(2024, spRecord.getShapeId(), 2024);
        assertEquals(2024, comment.getShapeId(), 2024);
        assertEquals(2024, comment.getNoteRecord().getShapeId());
        
        wb.close();
    }
}
