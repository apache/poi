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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Tests TestHSSFCellComment.
 *
 * @author  Yegor Kozlov
 */
public final class TestHSSFComment extends TestCase {

    /**
     * Test that we can create cells and add comments to it.
     */
    public static void testWriteComments() throws Exception {
        String cellText = "Hello, World";
        String commentText = "We can set comments in POI";
        String commentAuthor = "Apache Software Foundation";
        int cellRow = 3;
        int cellColumn = 1;

        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFSheet sheet = wb.createSheet();

        HSSFCell cell = sheet.createRow(cellRow).createCell(cellColumn);
        cell.setCellValue(new HSSFRichTextString(cellText));
        assertNull(cell.getCellComment());

        HSSFPatriarch patr = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor = new HSSFClientAnchor();
        anchor.setAnchor( (short)4, 2, 0, 0, (short) 6, 5, 0, 0);
        HSSFComment comment = patr.createComment(anchor);
        HSSFRichTextString string1 = new HSSFRichTextString(commentText);
        comment.setString(string1);
        comment.setAuthor(commentAuthor);
        cell.setCellComment(comment);
        if (false) {
            // TODO - the following line should break this test, but it doesn't
            cell.removeCellComment();
        }

        //verify our settings
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());

        //serialize the workbook and read it again
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = wb.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());


        // Change slightly, and re-test
        comment.setString(new HSSFRichTextString("New Comment Text"));

        out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = wb.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals("New Comment Text", comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());
    }

    /**
     * test that we can read cell comments from an existing workbook.
     */
    public static void testReadComments() {

         HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithComments.xls");

         HSSFSheet sheet = wb.getSheetAt(0);

         HSSFCell cell;
         HSSFRow row;
         HSSFComment comment;

         for (int rownum = 0; rownum < 3; rownum++) {
             row = sheet.getRow(rownum);
             cell = row.getCell(0);
             comment = cell.getCellComment();
             assertNull("Cells in the first column are not commented", comment);
             assertNull(sheet.getCellComment(rownum, 0));
         }

         for (int rownum = 0; rownum < 3; rownum++) {
             row = sheet.getRow(rownum);
             cell = row.getCell(1);
             comment = cell.getCellComment();
             assertNotNull("Cells in the second column have comments", comment);
             assertNotNull("Cells in the second column have comments", sheet.getCellComment(rownum, 1));

             assertEquals(HSSFSimpleShape.OBJECT_TYPE_COMMENT, comment.getShapeType());
             assertEquals("Yegor Kozlov", comment.getAuthor());
             assertFalse("cells in the second column have not empyy notes",
                     "".equals(comment.getString().getString()));
             assertEquals(rownum, comment.getRow());
             assertEquals(cell.getColumnIndex(), comment.getColumn());
         }
     }

    /**
     * test that we can modify existing cell comments
     */
    public static void testModifyComments() throws IOException {

         HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithComments.xls");

         HSSFSheet sheet = wb.getSheetAt(0);

         HSSFCell cell;
         HSSFRow row;
         HSSFComment comment;

         for (int rownum = 0; rownum < 3; rownum++) {
             row = sheet.getRow(rownum);
             cell = row.getCell(1);
             comment = cell.getCellComment();
             comment.setAuthor("Mofified["+rownum+"] by Yegor");
             comment.setString(new HSSFRichTextString("Modified comment at row " + rownum));
         }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();

        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        sheet = wb.getSheetAt(0);

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();

            assertEquals("Mofified["+rownum+"] by Yegor", comment.getAuthor());
            assertEquals("Modified comment at row " + rownum, comment.getString().getString());
        }

     }

    public void testDeleteComments() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleWithComments.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        // Zap from rows 1 and 3
        assertNotNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(2).getCell(1).getCellComment());

        sheet.getRow(0).getCell(1).removeCellComment();
        sheet.getRow(2).getCell(1).setCellComment(null);

        // Check gone so far
        assertNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNull(sheet.getRow(2).getCell(1).getCellComment());

        // Save and re-load
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));

        // Check
        assertNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNull(sheet.getRow(2).getCell(1).getCellComment());

//        FileOutputStream fout = new FileOutputStream("/tmp/c.xls");
//        wb.write(fout);
//        fout.close();
    }
}
