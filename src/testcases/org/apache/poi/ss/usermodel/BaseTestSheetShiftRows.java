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
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Tests row shifting capabilities.
 *
 * @author Shawn Laubach (slaubach at apache dot com)
 * @author Toshiaki Kamoshida (kamoshida.toshiaki at future dot co dot jp)
 */
public abstract class BaseTestSheetShiftRows  extends TestCase {

    /**
     * @return an object that provides test data in HSSF / XSSF specific way
     */
    protected abstract ITestDataProvider getTestDataProvider();

    /**
     * Tests the shiftRows function.  Does three different shifts.
     * After each shift, writes the workbook to file and reads back to
     * check.  This ensures that if some changes code that breaks
     * writing or what not, they realize it.
     *
     * @param sampleName the sample file to test against
     */
    public final void baseTestShiftRows(String sampleName){
        // Read initial file in
        Workbook wb = getTestDataProvider().openSampleWorkbook(sampleName);
        Sheet s = wb.getSheetAt( 0 );

        // Shift the second row down 1 and write to temp file
        s.shiftRows( 1, 1, 1 );

        wb = getTestDataProvider().writeOutAndReadBack(wb);

        // Read from temp file and check the number of cells in each
        // row (in original file each row was unique)
        s = wb.getSheetAt( 0 );

        assertEquals(s.getRow(0).getPhysicalNumberOfCells(), 1);
        confirmEmptyRow(s, 1);
        assertEquals(s.getRow(2).getPhysicalNumberOfCells(), 2);
        assertEquals(s.getRow(3).getPhysicalNumberOfCells(), 4);
        assertEquals(s.getRow(4).getPhysicalNumberOfCells(), 5);

        // Shift rows 1-3 down 3 in the current one.  This tests when
        // 1 row is blank.  Write to a another temp file
        s.shiftRows( 0, 2, 3 );
        wb = getTestDataProvider().writeOutAndReadBack(wb);

        // Read and ensure things are where they should be
        s = wb.getSheetAt(0);
        confirmEmptyRow(s, 0);
        confirmEmptyRow(s, 1);
        confirmEmptyRow(s, 2);
        assertEquals(s.getRow(3).getPhysicalNumberOfCells(), 1);
        confirmEmptyRow(s, 4);
        assertEquals(s.getRow(5).getPhysicalNumberOfCells(), 2);

        // Read the first file again
        wb = getTestDataProvider().openSampleWorkbook(sampleName);
        s = wb.getSheetAt( 0 );

        // Shift rows 3 and 4 up and write to temp file
        s.shiftRows( 2, 3, -2 );
        wb = getTestDataProvider().writeOutAndReadBack(wb);
        s = wb.getSheetAt( 0 );
        assertEquals(s.getRow(0).getPhysicalNumberOfCells(), 3);
        assertEquals(s.getRow(1).getPhysicalNumberOfCells(), 4);
        confirmEmptyRow(s, 2);
        confirmEmptyRow(s, 3);
        assertEquals(s.getRow(4).getPhysicalNumberOfCells(), 5);
    }
    private static void confirmEmptyRow(Sheet s, int rowIx) {
        Row row = s.getRow(rowIx);
        assertTrue(row == null || row.getPhysicalNumberOfCells() == 0);
    }

    /**
     * Tests when rows are null.
     */
    public final void baseTestShiftRow() {
        Workbook b = getTestDataProvider().createWorkbook();
        Sheet s	= b.createSheet();
        s.createRow(0).createCell(0).setCellValue("TEST1");
        s.createRow(3).createCell(0).setCellValue("TEST2");
        s.shiftRows(0,4,1);
    }

    /**
     * Tests when shifting the first row.
     */
    public final void baseTestActiveCell() {
        Workbook b = getTestDataProvider().createWorkbook();
        Sheet s	= b.createSheet();

        s.createRow(0).createCell(0).setCellValue("TEST1");
        s.createRow(3).createCell(0).setCellValue("TEST2");
        s.shiftRows(0,4,1);
    }

    /**
     * When shifting rows, the page breaks should go with it
     *
     */
    public final void baseTestShiftRowBreaks() {
        Workbook b = getTestDataProvider().createWorkbook();
        Sheet s	= b.createSheet();
        Row row = s.createRow(4);
        row.createCell(0).setCellValue("test");
        s.setRowBreak(4);

        s.shiftRows(4, 4, 2);
        assertTrue("Row number 6 should have a pagebreak", s.isRowBroken(6));
    }


    public final void baseTestShiftWithComments(String sampleName) {
        Workbook wb = getTestDataProvider().openSampleWorkbook(sampleName);

        Sheet sheet = wb.getSheet("Sheet1");
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
        wb = getTestDataProvider().writeOutAndReadBack(wb);
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

    public final void baseTestShiftWithNames() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");
        wb.createSheet("Sheet2");
        Row row = sheet1.createRow(0);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(2.2);

        Name name1 = wb.createName();
        name1.setNameName("name1");
        name1.setRefersToFormula("Sheet1!$A$1+Sheet1!$B$1");

        Name name2 = wb.createName();
        name2.setNameName("name2");
        name2.setRefersToFormula("Sheet1!$A$1");

        //refers to A1 but on Sheet2. Should stay unaffected.
        Name name3 = wb.createName();
        name3.setNameName("name3");
        name3.setRefersToFormula("Sheet2!$A$1");

        //The scope of this one is Sheet2. Should stay unaffected.
        Name name4 = wb.createName();
        name4.setNameName("name4");
        name4.setRefersToFormula("A1");
        name4.setSheetIndex(1);

        sheet1.shiftRows(0, 1, 2);  //shift down the top row on Sheet1.
        name1 = wb.getNameAt(0);
        assertEquals("Sheet1!$A$3+Sheet1!$B$3", name1.getRefersToFormula());

        name2 = wb.getNameAt(1);
        assertEquals("Sheet1!$A$3", name2.getRefersToFormula());

        //name3 and name4 refer to Sheet2 and should not be affected
        name3 = wb.getNameAt(2);
        assertEquals("Sheet2!$A$1", name3.getRefersToFormula());

        name4 = wb.getNameAt(3);
        assertEquals("A1", name4.getRefersToFormula());
    }

    public final void baseTestShiftWithMergedRegions() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sheet	= wb.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1.1);
        row.createCell(1).setCellValue(2.2);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, 2);
        assertEquals("A1:C1", region.formatAsString());

        sheet.addMergedRegion(region);

        sheet.shiftRows(0, 1, 2);
        region = sheet.getMergedRegion(0);
        assertEquals("A3:C3", region.formatAsString());
   }

    /**
     * See bug #34023
     *
     * @param sampleName the sample file to test against
     */
    public void baseTestShiftWithFormulas(String sampleName) {
        Workbook wb = getTestDataProvider().openSampleWorkbook(sampleName);

        Sheet sheet = wb.getSheet("Sheet1");
        assertEquals(20, sheet.getLastRowNum());

        confirmRow(sheet, 0, 1, 171, 1, "ROW(D1)", "100+B1", "COUNT(D1:E1)");
        confirmRow(sheet, 1, 2, 172, 1, "ROW(D2)", "100+B2", "COUNT(D2:E2)");
        confirmRow(sheet, 2, 3, 173, 1, "ROW(D3)", "100+B3", "COUNT(D3:E3)");

        confirmCell(sheet, 6, 1, 271, "200+B1");
        confirmCell(sheet, 7, 1, 272, "200+B2");
        confirmCell(sheet, 8, 1, 273, "200+B3");

        confirmCell(sheet, 14, 0, 0.0, "A12"); // the cell referred to by this formula will be replaced

        // -----------
        // Row index 1 -> 11 (row "2" -> row "12")
        sheet.shiftRows(1, 1, 10);

        // Now check what sheet looks like after move

        // no changes on row "1"
        confirmRow(sheet, 0, 1, 171, 1, "ROW(D1)", "100+B1", "COUNT(D1:E1)");

        // row "2" is now empty
        confirmEmptyRow(sheet, 1);

        // Row "2" moved to row "12", and the formula has been updated.
        // note however that the cached formula result (2) has not been updated. (POI differs from Excel here)
        confirmRow(sheet, 11, 2, 172, 1, "ROW(D12)", "100+B12", "COUNT(D12:E12)");

        // no changes on row "3"
        confirmRow(sheet, 2, 3, 173, 1, "ROW(D3)", "100+B3", "COUNT(D3:E3)");


        confirmCell(sheet, 14, 0, 0.0, "#REF!");


        // Formulas on rows that weren't shifted:
        confirmCell(sheet, 6, 1, 271, "200+B1");
        confirmCell(sheet, 7, 1, 272, "200+B12"); // this one moved
        confirmCell(sheet, 8, 1, 273, "200+B3");

        // check formulas on other sheets
        Sheet sheet2 = wb.getSheet("Sheet2");
        confirmCell(sheet2,  0, 0, 371, "300+Sheet1!B1");
        confirmCell(sheet2,  1, 0, 372, "300+Sheet1!B12");
        confirmCell(sheet2,  2, 0, 373, "300+Sheet1!B3");

        confirmCell(sheet2, 11, 0, 300, "300+Sheet1!#REF!");


        // Note - named ranges formulas have not been updated
    }

    private static void confirmRow(Sheet sheet, int rowIx, double valA, double valB, double valC,
                String formulaA, String formulaB, String formulaC) {
        confirmCell(sheet, rowIx, 4, valA, formulaA);
        confirmCell(sheet, rowIx, 5, valB, formulaB);
        confirmCell(sheet, rowIx, 6, valC, formulaC);
    }

    private static void confirmCell(Sheet sheet, int rowIx, int colIx,
            double expectedValue, String expectedFormula) {
        Cell cell = sheet.getRow(rowIx).getCell(colIx);
        assertEquals(expectedValue, cell.getNumericCellValue(), 0.0);
        assertEquals(expectedFormula, cell.getCellFormula());
    }
}
