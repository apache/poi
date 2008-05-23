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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFRow.MissingCellPolicy;

/**
 * Test HSSFRow is okay.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestHSSFRow extends TestCase {

    public void testLastAndFirstColumns() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow(0);
        assertEquals(-1, row.getFirstCellNum());
        assertEquals(-1, row.getLastCellNum());

        row.createCell((short) 2);
        assertEquals(2, row.getFirstCellNum());
        assertEquals(3, row.getLastCellNum());

        row.createCell((short) 1);
        assertEquals(1, row.getFirstCellNum());
        assertEquals(3, row.getLastCellNum());

        // check the exact case reported in 'bug' 43901 - notice that the cellNum is '0' based
        row.createCell((short) 3);
        assertEquals(1, row.getFirstCellNum());
        assertEquals(4, row.getLastCellNum());
    }

    /**
     * Make sure that there is no cross-talk between rows especially with getFirstCellNum and getLastCellNum
     * This test was added in response to bug report 44987.
     */
    public void testBoundsInMultipleRows() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow rowA = sheet.createRow(0);

        rowA.createCell((short) 10);
        rowA.createCell((short) 5);
        assertEquals(5, rowA.getFirstCellNum());
        assertEquals(11, rowA.getLastCellNum());

        HSSFRow rowB = sheet.createRow(1);
        rowB.createCell((short) 15);
        rowB.createCell((short) 30);
        assertEquals(15, rowB.getFirstCellNum());
        assertEquals(31, rowB.getLastCellNum());

        assertEquals(5, rowA.getFirstCellNum());
        assertEquals(11, rowA.getLastCellNum());
        rowA.createCell((short) 50);
        assertEquals(51, rowA.getLastCellNum());

        assertEquals(31, rowB.getLastCellNum());
    }

    public void testRemoveCell() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow((short) 0);
        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());
        row.createCell((short) 1);
        assertEquals(2, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.createCell((short) 3);
        assertEquals(4, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.removeCell(row.getCell((short) 3));
        assertEquals(2, row.getLastCellNum());
        assertEquals(1, row.getFirstCellNum());
        row.removeCell(row.getCell((short) 1));
        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());

        // all cells on this row have been removed
        // so check the row record actually writes it out as 0's
        byte[] data = new byte[100];
        row.getRowRecord().serialize(0, data);
        assertEquals(0, data[6]);
        assertEquals(0, data[8]);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        sheet = workbook.getSheetAt(0);

        assertEquals(-1, sheet.getRow(0).getLastCellNum());
        assertEquals(-1, sheet.getRow(0).getFirstCellNum());
    }

    public void testMoveCell() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow((short) 0);
        HSSFRow rowB = sheet.createRow((short) 1);

        HSSFCell cellA2 = rowB.createCell((short)0);
        assertEquals(0, rowB.getFirstCellNum());
        assertEquals(0, rowB.getFirstCellNum());

        assertEquals(-1, row.getLastCellNum());
        assertEquals(-1, row.getFirstCellNum());
        HSSFCell cellB2 = row.createCell((short) 1);
        HSSFCell cellB3 = row.createCell((short) 2);
        HSSFCell cellB4 = row.createCell((short) 3);

        assertEquals(1, row.getFirstCellNum());
        assertEquals(4, row.getLastCellNum());

        // Try to move to somewhere else that's used
        try {
            row.moveCell(cellB2, (short)3);
            fail("IllegalArgumentException should have been thrown");
        } catch(IllegalArgumentException e) {
            // expected during successful test
        }

        // Try to move one off a different row
        try {
            row.moveCell(cellA2, (short)3);
            fail("IllegalArgumentException should have been thrown");
        } catch(IllegalArgumentException e) {
            // expected during successful test
        }

        // Move somewhere spare
        assertNotNull(row.getCell((short)1));
        row.moveCell(cellB2, (short)5);
        assertNull(row.getCell((short)1));
        assertNotNull(row.getCell((short)5));

        assertEquals(5, cellB2.getCellNum());
        assertEquals(2, row.getFirstCellNum());
        assertEquals(6, row.getLastCellNum());
    }

    public void testRowBounds() {
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet();
      //Test low row bound
      sheet.createRow( (short) 0);
      //Test low row bound exception
      try {
        sheet.createRow(-1);
        fail("IndexOutOfBoundsException should have been thrown");
      } catch (IllegalArgumentException e) {
        // expected during successful test
        assertEquals("Invalid row number (-1) outside allowable range (0..65535)", e.getMessage());
      }

      //Test high row bound
      sheet.createRow(65535);
      //Test high row bound exception
      try {
        sheet.createRow(65536);
        fail("IndexOutOfBoundsException should have been thrown");
      } catch (IllegalArgumentException e) {
        // expected during successful test
        assertEquals("Invalid row number (65536) outside allowable range (0..65535)", e.getMessage());
      }
    }

    /**
     * Prior to patch 43901, POI was producing files with the wrong last-column
     * number on the row
     */
    public void testLastCellNumIsCorrectAfterAddCell_bug43901(){
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFSheet sheet = book.createSheet("test");
        HSSFRow row = sheet.createRow(0);

        // New row has last col -1
        assertEquals(-1, row.getLastCellNum());
        if(row.getLastCellNum() == 0) {
            fail("Identified bug 43901");
        }

        // Create two cells, will return one higher
        //  than that for the last number
        row.createCell((short) 0);
        assertEquals(1, row.getLastCellNum());
        row.createCell((short) 255);
        assertEquals(256, row.getLastCellNum());
    }
    
    /**
     * Tests for the missing/blank cell policy stuff
     */
    public void testGetCellPolicy() throws Exception {
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFSheet sheet = book.createSheet("test");
        HSSFRow row = sheet.createRow(0);

        // 0 -> string
        // 1 -> num
        // 2 missing
        // 3 missing
        // 4 -> blank
        // 5 -> num
        row.createCell((short)0).setCellValue(new HSSFRichTextString("test"));
        row.createCell((short)1).setCellValue(3.2);
        row.createCell((short)4, HSSFCell.CELL_TYPE_BLANK);
        row.createCell((short)5).setCellValue(4);
        
        // First up, no policy given, uses default
        assertEquals(HSSFCell.CELL_TYPE_STRING,  row.getCell(0).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
        assertEquals(null, row.getCell(2));
        assertEquals(null, row.getCell(3));
        assertEquals(HSSFCell.CELL_TYPE_BLANK,   row.getCell(4).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(5).getCellType());
        
        // RETURN_NULL_AND_BLANK - same as default
        assertEquals(HSSFCell.CELL_TYPE_STRING,  row.getCell(0, HSSFRow.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(1, HSSFRow.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(null, row.getCell(2, HSSFRow.RETURN_NULL_AND_BLANK));
        assertEquals(null, row.getCell(3, HSSFRow.RETURN_NULL_AND_BLANK));
        assertEquals(HSSFCell.CELL_TYPE_BLANK,   row.getCell(4, HSSFRow.RETURN_NULL_AND_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(5, HSSFRow.RETURN_NULL_AND_BLANK).getCellType());
        
        // RETURN_BLANK_AS_NULL - nearly the same
        assertEquals(HSSFCell.CELL_TYPE_STRING,  row.getCell(0, HSSFRow.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(1, HSSFRow.RETURN_BLANK_AS_NULL).getCellType());
        assertEquals(null, row.getCell(2, HSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(3, HSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(null, row.getCell(4, HSSFRow.RETURN_BLANK_AS_NULL));
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(5, HSSFRow.RETURN_BLANK_AS_NULL).getCellType());
        
        // CREATE_NULL_AS_BLANK - creates as needed
        assertEquals(HSSFCell.CELL_TYPE_STRING,  row.getCell(0, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(1, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_BLANK,   row.getCell(2, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_BLANK,   row.getCell(3, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_BLANK,   row.getCell(4, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(5, HSSFRow.CREATE_NULL_AS_BLANK).getCellType());
        
        // Check created ones get the right column
        assertEquals((short)0, row.getCell(0, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)1, row.getCell(1, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)2, row.getCell(2, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)3, row.getCell(3, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)4, row.getCell(4, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        assertEquals((short)5, row.getCell(5, HSSFRow.CREATE_NULL_AS_BLANK).getCellNum());
        
        
        // Now change the cell policy on the workbook, check
        //  that that is now used if no policy given
        book.setMissingCellPolicy(HSSFRow.RETURN_BLANK_AS_NULL);
        
        assertEquals(HSSFCell.CELL_TYPE_STRING,  row.getCell(0).getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
        assertEquals(null, row.getCell(2));
        assertEquals(null, row.getCell(3));
        assertEquals(null, row.getCell(4));
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, row.getCell(5).getCellType());
    }
}
