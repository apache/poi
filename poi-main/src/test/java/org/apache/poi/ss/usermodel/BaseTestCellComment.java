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

package org.apache.poi.ss.usermodel;

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * Common superclass for testing implementations of
 * {@link Comment}
 */
public abstract class BaseTestCellComment extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestCellComment(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    public final void testFind() {
        Workbook book = _testDataProvider.createWorkbook();
        Sheet sheet = book.createSheet();
        assertNull(sheet.getCellComment(0, 0));

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        assertNull(sheet.getCellComment(0, 0));
        assertNull(cell.getCellComment());
    }

    public final void testCreate() {
        String cellText = "Hello, World";
        String commentText = "We can set comments in POI";
        String commentAuthor = "Apache Software Foundation";
        int cellRow = 3;
        int cellColumn = 1;

        Workbook wb = _testDataProvider.createWorkbook();
        CreationHelper factory = wb.getCreationHelper();

        Sheet sheet = wb.createSheet();
        assertNull(sheet.getCellComment(cellRow, cellColumn));

        Cell cell = sheet.createRow(cellRow).createCell(cellColumn);
        cell.setCellValue(factory.createRichTextString(cellText));
        assertNull(cell.getCellComment());
        assertNull(sheet.getCellComment(cellRow, cellColumn));

        Drawing patr = sheet.createDrawingPatriarch();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(2);
        anchor.setCol2(5);
        anchor.setRow1(1);
        anchor.setRow2(2);
        Comment comment = patr.createCellComment(anchor);
        assertFalse(comment.isVisible());
        comment.setVisible(true);
        assertTrue(comment.isVisible());
        RichTextString string1 = factory.createRichTextString(commentText);
        comment.setString(string1);
        comment.setAuthor(commentAuthor);
        cell.setCellComment(comment);
        assertNotNull(cell.getCellComment());
        assertNotNull(sheet.getCellComment(cellRow, cellColumn));

        //verify our settings
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals(commentText, comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());
        assertTrue(comment.isVisible());

        // Change slightly, and re-test
        comment.setString(factory.createRichTextString("New Comment Text"));
        comment.setVisible(false);

        wb = _testDataProvider.writeOutAndReadBack(wb);

        sheet = wb.getSheetAt(0);
        cell = sheet.getRow(cellRow).getCell(cellColumn);
        comment = cell.getCellComment();

        assertNotNull(comment);
        assertEquals(commentAuthor, comment.getAuthor());
        assertEquals("New Comment Text", comment.getString().getString());
        assertEquals(cellRow, comment.getRow());
        assertEquals(cellColumn, comment.getColumn());
        assertFalse(comment.isVisible());
    }

    /**
     * test that we can read cell comments from an existing workbook.
     */
    public final void testReadComments() {

        Workbook wb = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());

        Sheet sheet = wb.getSheetAt(0);

        Cell cell;
        Row row;
        Comment comment;

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
    public final void testModifyComments() {

        Workbook wb = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());
        CreationHelper factory = wb.getCreationHelper();

        Sheet sheet = wb.getSheetAt(0);

        Cell cell;
        Row row;
        Comment comment;

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();
            comment.setAuthor("Mofified[" + rownum + "] by Yegor");
            comment.setString(factory.createRichTextString("Modified comment at row " + rownum));
        }

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        for (int rownum = 0; rownum < 3; rownum++) {
            row = sheet.getRow(rownum);
            cell = row.getCell(1);
            comment = cell.getCellComment();

            assertEquals("Mofified[" + rownum + "] by Yegor", comment.getAuthor());
            assertEquals("Modified comment at row " + rownum, comment.getString().getString());
        }
    }

    public final void testDeleteComments() {
        Workbook wb = _testDataProvider.openSampleWorkbook("SimpleWithComments." + _testDataProvider.getStandardFileNameExtension());
        Sheet sheet = wb.getSheetAt(0);

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
        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        // Check
        assertNull(sheet.getRow(0).getCell(1).getCellComment());
        assertNotNull(sheet.getRow(1).getCell(1).getCellComment());
        assertNull(sheet.getRow(2).getCell(1).getCellComment());
    }

    /**
     * code from the quick guide
     */
    public void testQuickGuide(){
        Workbook wb = _testDataProvider.createWorkbook();

        CreationHelper factory = wb.getCreationHelper();

        Sheet sheet = wb.createSheet();

        Cell cell = sheet.createRow(3).createCell(5);
        cell.setCellValue("F4");

        Drawing drawing = sheet.createDrawingPatriarch();

        ClientAnchor anchor = factory.createClientAnchor();
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString("Hello, World!");
        comment.setString(str);
        comment.setAuthor("Apache POI");
        //assign the comment to the cell
        cell.setCellComment(comment);

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        cell = sheet.getRow(3).getCell(5);
        comment = cell.getCellComment();
        assertNotNull(comment);
        assertEquals("Hello, World!", comment.getString().getString());
        assertEquals("Apache POI", comment.getAuthor());
        assertEquals(3, comment.getRow());
        assertEquals(5, comment.getColumn());
    }
}
