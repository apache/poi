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

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Tests row shifting capabilities.
 *
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */
public final class TestSheetShiftRows extends TestCase {

    /**
     * Tests the shiftRows function.  Does three different shifts.
     * After each shift, writes the workbook to file and reads back to
     * check.  This ensures that if some changes code that breaks
     * writing or what not, they realize it.
     *
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testShiftRows() throws Exception
    {
        // Read initial file in
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("SimpleMultiCell.xls");
        HSSFSheet s = wb.getSheetAt( 0 );

        // Shift the second row down 1 and write to temp file
        s.shiftRows( 1, 1, 1 );

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        // Read from temp file and check the number of cells in each
        // row (in original file each row was unique)
        s = wb.getSheetAt( 0 );

        assertEquals( s.getRow( 0 ).getPhysicalNumberOfCells(), 1 );
        assertTrue( s.getRow( 1 ) == null || s.getRow( 1 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 2 ).getPhysicalNumberOfCells(), 2 );
        assertEquals( s.getRow( 3 ).getPhysicalNumberOfCells(), 4 );
        assertEquals( s.getRow( 4 ).getPhysicalNumberOfCells(), 5 );

        // Shift rows 1-3 down 3 in the current one.  This tests when
        // 1 row is blank.  Write to a another temp file
        s.shiftRows( 0, 2, 3 );
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        // Read and ensure things are where they should be
        s = wb.getSheetAt( 0 );
        assertTrue( s.getRow( 0 ) == null || s.getRow( 0 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 1 ) == null || s.getRow( 1 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 2 ) == null || s.getRow( 2 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 3 ).getPhysicalNumberOfCells(), 1 );
        assertTrue( s.getRow( 4 ) == null || s.getRow( 4 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 5 ).getPhysicalNumberOfCells(), 2 );

        // Read the first file again
        wb = HSSFTestDataSamples.openSampleWorkbook("SimpleMultiCell.xls");
        s = wb.getSheetAt( 0 );

        // Shift rows 3 and 4 up and write to temp file
        s.shiftRows( 2, 3, -2 );
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        s = wb.getSheetAt( 0 );
        assertEquals( s.getRow( 0 ).getPhysicalNumberOfCells(), 3 );
        assertEquals( s.getRow( 1 ).getPhysicalNumberOfCells(), 4 );
        assertTrue( s.getRow( 2 ) == null || s.getRow( 2 ).getPhysicalNumberOfCells() == 0 );
        assertTrue( s.getRow( 3 ) == null || s.getRow( 3 ).getPhysicalNumberOfCells() == 0 );
        assertEquals( s.getRow( 4 ).getPhysicalNumberOfCells(), 5 );
    }

    /**
     * Tests when rows are null.
     *
     * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
     */
    public void testShiftRow(){
       HSSFWorkbook b = new HSSFWorkbook();
       HSSFSheet s    = b.createSheet();
       s.createRow(0).createCell(0).setCellValue("TEST1");
       s.createRow(3).createCell(0).setCellValue("TEST2");
       s.shiftRows(0,4,1);
    }

    /**
     * Tests when shifting the first row.
     *
     * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
     */
    public void testShiftRow0(){
       HSSFWorkbook b = new HSSFWorkbook();
       HSSFSheet s    = b.createSheet();
       s.createRow(0).createCell(0).setCellValue("TEST1");
       s.createRow(3).createCell(0).setCellValue("TEST2");
       s.shiftRows(0,4,1);
    }

    /**
     * When shifting rows, the page breaks should go with it
     *
     */
    public void testShiftRowBreaks(){
      HSSFWorkbook b = new HSSFWorkbook();
      HSSFSheet s    = b.createSheet();
      HSSFRow row = s.createRow(4);
      row.createCell(0).setCellValue("test");
      s.setRowBreak(4);

      s.shiftRows(4, 4, 2);
      assertTrue("Row number 6 should have a pagebreak", s.isRowBroken(6));

    }


    public void testShiftWithComments() throws Exception {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("comments.xls");

        HSSFSheet sheet = wb.getSheet("Sheet1");
        assertEquals(3, sheet.getLastRowNum());

        // Verify comments are in the position expected
        assertNotNull(sheet.getCellComment(0,0));
        assertNull(sheet.getCellComment(1,0));
        assertNotNull(sheet.getCellComment(2,0));
        assertNotNull(sheet.getCellComment(3,0));

        String comment1 = sheet.getCellComment(0,0).getString().getString();
        assertEquals(comment1,"comment top row1 (index0)\n");
        String comment3 = sheet.getCellComment(2,0).getString().getString();
        assertEquals(comment3,"comment top row3 (index2)\n");
        String comment4 = sheet.getCellComment(3,0).getString().getString();
        assertEquals(comment4,"comment top row4 (index3)\n");

        // Shifting all but first line down to test comments shifting
        sheet.shiftRows(1, sheet.getLastRowNum(), 1, true, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        wb.write(outputStream);

        // Test that comments were shifted as expected
        assertEquals(4, sheet.getLastRowNum());
        assertNotNull(sheet.getCellComment(0,0));
        assertNull(sheet.getCellComment(1,0));
        assertNull(sheet.getCellComment(2,0));
        assertNotNull(sheet.getCellComment(3,0));
        assertNotNull(sheet.getCellComment(4,0));

        String comment1_shifted = sheet.getCellComment(0,0).getString().getString();
        assertEquals(comment1,comment1_shifted);
        String comment3_shifted = sheet.getCellComment(3,0).getString().getString();
        assertEquals(comment3,comment3_shifted);
        String comment4_shifted = sheet.getCellComment(4,0).getString().getString();
        assertEquals(comment4,comment4_shifted);

        // Write out and read back in again
        // Ensure that the changes were persisted
        wb = new HSSFWorkbook( new ByteArrayInputStream(outputStream.toByteArray()) );
        sheet = wb.getSheet("Sheet1");
        assertEquals(4, sheet.getLastRowNum());

        // Verify comments are in the position expected after the shift
        assertNotNull(sheet.getCellComment(0,0));
        assertNull(sheet.getCellComment(1,0));
        assertNull(sheet.getCellComment(2,0));
        assertNotNull(sheet.getCellComment(3,0));
        assertNotNull(sheet.getCellComment(4,0));

        comment1_shifted = sheet.getCellComment(0,0).getString().getString();
        assertEquals(comment1,comment1_shifted);
        comment3_shifted = sheet.getCellComment(3,0).getString().getString();
        assertEquals(comment3,comment3_shifted);
        comment4_shifted = sheet.getCellComment(4,0).getString().getString();
        assertEquals(comment4,comment4_shifted);
    }

    /**
     * See bug #34023
     */
    public void testShiftWithFormulas() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ForShifting.xls");

        HSSFSheet sheet = wb.getSheet("Sheet1");
        assertEquals(19, sheet.getLastRowNum());

        assertEquals("cell B1 (ref)", sheet.getRow(0).getCell(3).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B1,\" (ref)\")", sheet.getRow(0).getCell(3).getCellFormula());
        assertEquals("cell B2 (ref)", sheet.getRow(1).getCell(3).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B2,\" (ref)\")", sheet.getRow(1).getCell(3).getCellFormula());
        assertEquals("cell B3 (ref)", sheet.getRow(2).getCell(3).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B3,\" (ref)\")", sheet.getRow(2).getCell(3).getCellFormula());
        assertEquals("cell B2 (ref)", sheet.getRow(6).getCell(1).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B2,\" (ref)\")", sheet.getRow(6).getCell(1).getCellFormula());

        sheet.shiftRows(1, 1, 10);

        // Row 1 => Row 11
        // So strings on row 11 unchanged, but reference in formula is
        assertEquals("cell B1 (ref)", sheet.getRow(0).getCell(3).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B1,\" (ref)\")", sheet.getRow(0).getCell(3).getCellFormula());
        assertEquals(0, sheet.getRow(1).getPhysicalNumberOfCells());

        // still save b2
        assertEquals("cell B2 (ref)", sheet.getRow(11).getCell(3).getRichStringCellValue().toString());
        // but points to b12
        assertEquals("CONCATENATE(B12,\" (ref)\")", sheet.getRow(11).getCell(3).getCellFormula());

        assertEquals("cell B3 (ref)", sheet.getRow(2).getCell(3).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B3,\" (ref)\")", sheet.getRow(2).getCell(3).getCellFormula());

        // one on a non-shifted row also updated
        assertEquals("cell B2 (ref)", sheet.getRow(6).getCell(1).getRichStringCellValue().toString());
        assertEquals("CONCATENATE(B12,\" (ref)\")", sheet.getRow(6).getCell(1).getCellFormula());
    }
}

