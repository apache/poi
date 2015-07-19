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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestCellComment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
        HSSFComment comment = new HSSFComment((HSSFShape)null, new HSSFClientAnchor());
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
    }

    /**
     *  HSSFCell#findCellComment should NOT rely on the order of records
     * when matching cells and their cell comments. The correct algorithm is to map
     */
    @Test
    public void bug47924() {
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
    
    @Test
    public void testBug56380InsertComments() throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Drawing drawing = sheet.createDrawingPatriarch();
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
        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet();
            Drawing drawing = sheet.createDrawingPatriarch();
            String comment = "c";
    
            for(int rowNum = 0;rowNum < 258;rowNum++) {
            	sheet.createRow(rowNum);
            }
            
            // should still work, for some reason DrawingManager2.allocateShapeId() skips the first 1024...
            for(int count = 1025;count < 65535;count++) {
            	int rowNum = count / 255;
            	int cellNum = count % 255;
                Cell cell = sheet.getRow(rowNum).createCell(cellNum);
                try {
                	Comment commentObj = insertComment(drawing, cell, comment + cellNum);
                	
                	assertEquals(count, ((HSSFComment)commentObj).getNoteRecord().getShapeId());
                } catch (IllegalArgumentException e) {
                	throw new IllegalArgumentException("While adding shape number " + count, e);
                }
            }        	
            
            // this should now fail to insert
            Row row = sheet.createRow(257);
            Cell cell = row.createCell(0);
            insertComment(drawing, cell, comment + 0);
        } finally {
        	workbook.close();
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

    private Comment insertComment(Drawing drawing, Cell cell, String message) {
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
}
