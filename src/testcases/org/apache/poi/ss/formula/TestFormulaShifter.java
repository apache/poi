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

package org.apache.poi.ss.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.AreaErrPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.util.CellReference;

import org.junit.Test;

/**
 * Tests for {@link FormulaShifter}.
 */
public final class TestFormulaShifter {
    // Note - the expected result row coordinates here were determined/verified
    // in Excel 2007 by manually testing.

    /**
     * Tests what happens to area refs when a range of rows from inside, or overlapping are
     * moved
     */
    @Test
    public void testShiftAreasSourceRows() {

        // all these operations are on an area ref spanning rows 10 to 20
        AreaPtg aptg  = createAreaPtgRow(10, 20);

        confirmAreaRowShift(aptg,  9, 21, 20, 30, 40);
        confirmAreaRowShift(aptg, 10, 21, 20, 30, 40);
        confirmAreaRowShift(aptg,  9, 20, 20, 30, 40);

        confirmAreaRowShift(aptg, 8, 11,  -3, 7, 20); // simple expansion of top
        // rows containing area top being shifted down:
        confirmAreaRowShift(aptg, 8, 11,  3, 13, 20);
        confirmAreaRowShift(aptg, 8, 11,  7, 17, 20);
        confirmAreaRowShift(aptg, 8, 11,  8, 18, 20);
        confirmAreaRowShift(aptg, 8, 11,  9, 12, 20); // note behaviour changes here
        confirmAreaRowShift(aptg, 8, 11, 10, 12, 21);
        confirmAreaRowShift(aptg, 8, 11, 12, 12, 23);
        confirmAreaRowShift(aptg, 8, 11, 13, 10, 20);  // ignored

        // rows from within being moved:
        confirmAreaRowShift(aptg, 12, 16,  3, 10, 20);  // stay within - no change
        confirmAreaRowShift(aptg, 11, 19, 20, 10, 20);  // move completely out - no change
        confirmAreaRowShift(aptg, 16, 17, -6, 10, 20);  // moved exactly to top - no change
        confirmAreaRowShift(aptg, 16, 17, -7, 11, 20);  // truncation at top
        confirmAreaRowShift(aptg, 12, 16,  4, 10, 20);  // moved exactly to bottom - no change
        confirmAreaRowShift(aptg, 12, 16,  6, 10, 17);  // truncation at bottom

        // rows containing area bottom being shifted up:
        confirmAreaRowShift(aptg, 18, 22, -1, 10, 19); // simple contraction at bottom
        confirmAreaRowShift(aptg, 18, 22, -7, 10, 13); // simple contraction at bottom
        confirmAreaRowShift(aptg, 18, 22, -8, 10, 17); // top calculated differently here
        confirmAreaRowShift(aptg, 18, 22, -9,  9, 17);
        confirmAreaRowShift(aptg, 18, 22,-15, 10, 20); // no change because range would be turned inside out
        confirmAreaRowShift(aptg, 15, 19, -7, 13, 20); // dest truncates top (even though src is from inside range)
        confirmAreaRowShift(aptg, 19, 23,-12,  7, 18); // complex: src encloses bottom, dest encloses top

        confirmAreaRowShift(aptg, 18, 22,  5, 10, 25); // simple expansion at bottom
    }

    @Test
    public void testShiftAreasSourceColumns() {

        // all these operations are on an area ref spanning columns 10 to 20
        AreaPtg aptg  = createAreaPtgColumn(10, 20);

        confirmAreaColumnShift(aptg,  9, 21, 20, 30, 40);
        confirmAreaColumnShift(aptg, 10, 21, 20, 30, 40);
        confirmAreaColumnShift(aptg,  9, 20, 20, 30, 40);

        confirmAreaColumnShift(aptg, 8, 11,  -3, 7, 20); // simple expansion of top
        // columns containing area top being shifted down:
        confirmAreaColumnShift(aptg, 8, 11,  3, 13, 20);
        confirmAreaColumnShift(aptg, 8, 11,  7, 17, 20);
        confirmAreaColumnShift(aptg, 8, 11,  8, 18, 20);
        confirmAreaColumnShift(aptg, 8, 11,  9, 12, 20); // note behaviour changes here
        confirmAreaColumnShift(aptg, 8, 11, 10, 12, 21);
        confirmAreaColumnShift(aptg, 8, 11, 12, 12, 23);
        confirmAreaColumnShift(aptg, 8, 11, 13, 10, 20);  // ignored

        // columns from within being moved:
        confirmAreaColumnShift(aptg, 12, 16,  3, 10, 20);  // stay within - no change
        confirmAreaColumnShift(aptg, 11, 19, 20, 10, 20);  // move completely out - no change
        confirmAreaColumnShift(aptg, 16, 17, -6, 10, 20);  // moved exactly to top - no change
        confirmAreaColumnShift(aptg, 16, 17, -7, 11, 20);  // truncation at top
        confirmAreaColumnShift(aptg, 12, 16,  4, 10, 20);  // moved exactly to bottom - no change
        confirmAreaColumnShift(aptg, 12, 16,  6, 10, 17);  // truncation at bottom

        // columns containing area bottom being shifted up:
        confirmAreaColumnShift(aptg, 18, 22, -1, 10, 19); // simple contraction at bottom
        confirmAreaColumnShift(aptg, 18, 22, -7, 10, 13); // simple contraction at bottom
        confirmAreaColumnShift(aptg, 18, 22, -8, 10, 17); // top calculated differently here
        confirmAreaColumnShift(aptg, 18, 22, -9,  9, 17);
        confirmAreaColumnShift(aptg, 18, 22,-15, 10, 20); // no change because range would be turned inside out
        confirmAreaColumnShift(aptg, 15, 19, -7, 13, 20); // dest truncates top (even though src is from inside range)
        confirmAreaColumnShift(aptg, 19, 23,-12,  7, 18); // complex: src encloses bottom, dest encloses top

        confirmAreaColumnShift(aptg, 18, 22,  5, 10, 25); // simple expansion at bottom
    }
    
    @Test
    public void testCopyAreasSourceRowsRelRel() {

        // all these operations are on an area ref spanning rows 10 to 20
        final AreaPtg aptg  = createAreaPtgRow(10, 20, true, true);

        confirmAreaRowCopy(aptg,  0, 30, 20, 30, 40, true);
        confirmAreaRowCopy(aptg,  15, 25, -15, -1, -1, true); //DeletedRef
    }
    
    @Test
    public void testCopyAreasSourceRowsRelAbs() {

        // all these operations are on an area ref spanning rows 10 to 20
        final AreaPtg aptg  = createAreaPtgRow(10, 20, true, false);

        // Only first row should move
        confirmAreaRowCopy(aptg,  0, 30, 20, 20, 30, true);
        confirmAreaRowCopy(aptg,  15, 25, -15, -1, -1, true); //DeletedRef
    }
    
    @Test
    public void testCopyAreasSourceRowsAbsRel() {
        // aptg is part of a formula in a cell that was just copied to another row
        // aptg row references should be updated by the difference in rows that the cell was copied
        // No other references besides the cells that were involved in the copy need to be updated
        // this makes the row copy significantly different from the row shift, where all references
        // in the workbook need to track the row shift

        // all these operations are on an area ref spanning rows 10 to 20
        final AreaPtg aptg  = createAreaPtgRow(10, 20, false, true);

        // Only last row should move
        confirmAreaRowCopy(aptg,  0, 30, 20, 10, 40, true);
        confirmAreaRowCopy(aptg,  15, 25, -15, 5, 10, true); //sortTopLeftToBottomRight swapped firstRow and lastRow because firstRow is absolute
    }
    
    @Test
    public void testCopyAreasSourceRowsAbsAbs() {
        // aptg is part of a formula in a cell that was just copied to another row
        // aptg row references should be updated by the difference in rows that the cell was copied
        // No other references besides the cells that were involved in the copy need to be updated
        // this makes the row copy significantly different from the row shift, where all references
        // in the workbook need to track the row shift
        
        // all these operations are on an area ref spanning rows 10 to 20
        final AreaPtg aptg  = createAreaPtgRow(10, 20, false, false);

        //AbsFirstRow AbsLastRow references should't change when copied to a different row
        confirmAreaRowCopy(aptg,  0, 30, 20, 10, 20, false);
        confirmAreaRowCopy(aptg,  15, 25, -15, 10, 20, false);
    }

    @Test
    public void testCopyAreasSourceColumnsRelRel() {

        // all these operations are on an area ref spanning columns 10 to 20
        final AreaPtg aptg  = createAreaPtgColumn(10, 20, true, true);

        confirmAreaColumnCopy(aptg,  0, 30, 20, 30, 40, true);
        confirmAreaColumnCopy(aptg,  15, 25, -15, -1, -1, true); //DeletedRef
    }

    @Test
    public void testCopyAreasSourceColumnsRelAbs() {

        // all these operations are on an area ref spanning columns 10 to 20
        final AreaPtg aptg  = createAreaPtgColumn(10, 20, true, false);

        // Only first column should move
        confirmAreaColumnCopy(aptg,  0, 30, 20, 20, 30, true);
        confirmAreaColumnCopy(aptg,  15, 25, -15, -1, -1, true); //DeletedRef
    }

    @Test
    public void testCopyAreasSourceColumnsAbsRel() {
        // aptg is part of a formula in a cell that was just copied to another column
        // aptg column references should be updated by the difference in columns that the cell was copied
        // No other references besides the cells that were involved in the copy need to be updated
        // this makes the column copy significantly different from the column shift, where all references
        // in the workbook need to track the column shift

        // all these operations are on an area ref spanning columns 10 to 20
        final AreaPtg aptg  = createAreaPtgColumn(10, 20, false, true);

        // Only last column should move
        confirmAreaColumnCopy(aptg,  0, 30, 20, 10, 40, true);
        confirmAreaColumnCopy(aptg,  15, 25, -15, 5, 10, true); //sortTopLeftToBottomRight swapped firstColumn and lastColumn because firstColumn is absolute
    }

    @Test
    public void testCopyAreasSourceColumnsAbsAbs() {
        // aptg is part of a formula in a cell that was just copied to another column
        // aptg column references should be updated by the difference in columns that the cell was copied
        // No other references besides the cells that were involved in the copy need to be updated
        // this makes the column copy significantly different from the column shift, where all references
        // in the workbook need to track the column shift

        // all these operations are on an area ref spanning columns 10 to 20
        final AreaPtg aptg  = createAreaPtgColumn(10, 20, false, false);

        //AbsFirstColumn AbsLastColumn references should't change when copied to a different column
        confirmAreaColumnCopy(aptg,  0, 30, 20, 10, 20, false);
        confirmAreaColumnCopy(aptg,  15, 25, -15, 10, 20, false);
    }
    
    
    
    /**
     * Tests what happens to an area ref when some outside rows are moved to overlap
     * that area ref
     */
    @Test
    public void testShiftAreasDestRows() {
        // all these operations are on an area ref spanning rows 20 to 25
        AreaPtg aptg  = createAreaPtgRow(20, 25);

        // no change because no overlap:
        confirmAreaRowShift(aptg,  5, 10,  9, 20, 25);
        confirmAreaRowShift(aptg,  5, 10, 21, 20, 25);

        confirmAreaRowShift(aptg, 11, 14, 10, 20, 25);

        confirmAreaRowShift(aptg,   7, 17, 10, -1, -1); // converted to DeletedAreaRef
        confirmAreaRowShift(aptg,   5, 15,  7, 23, 25); // truncation at top
        confirmAreaRowShift(aptg,  13, 16, 10, 20, 22); // truncation at bottom
    }

    /**
     * Tests what happens to an area ref when some outside columns are moved to overlap
     * that area ref
     */
    @Test
    public void testShiftAreasDestColumns() {
        // all these operations are on an area ref spanning columns 20 to 25
        AreaPtg aptg  = createAreaPtgColumn(20, 25);

        // no change because no overlap:
        confirmAreaColumnShift(aptg,  5, 10,  9, 20, 25);
        confirmAreaColumnShift(aptg,  5, 10, 21, 20, 25);

        confirmAreaColumnShift(aptg, 11, 14, 10, 20, 25);

        confirmAreaColumnShift(aptg,   7, 17, 10, -1, -1); // converted to DeletedAreaRef
        confirmAreaColumnShift(aptg,   5, 15,  7, 23, 25); // truncation at top
        confirmAreaColumnShift(aptg,  13, 16, 10, 20, 22); // truncation at bottom
    }

    private static void confirmAreaRowShift(
            AreaPtg aptg,
            int firstRowMoved, int lastRowMoved, int numberRowsMoved,
            int expectedAreaFirstRow, int expectedAreaLastRow) {

        FormulaShifter fs = FormulaShifter.createForRowShift(0, "", firstRowMoved, lastRowMoved, numberRowsMoved, SpreadsheetVersion.EXCEL2007);
        boolean expectedChanged = aptg.getFirstRow() != expectedAreaFirstRow || aptg.getLastRow() != expectedAreaLastRow;

        AreaPtg copyPtg = (AreaPtg) aptg.copy(); // clone so we can re-use aptg in calling method
        Ptg[] ptgs = { copyPtg, };
        boolean actualChanged = fs.adjustFormula(ptgs, 0);
        if (expectedAreaFirstRow < 0) {
            assertEquals(AreaErrPtg.class, ptgs[0].getClass());
            return;
        }
        assertEquals(expectedChanged, actualChanged);
        assertEquals(copyPtg, ptgs[0]);  // expected to change in place (although this is not a strict requirement)
        assertEquals(expectedAreaFirstRow, copyPtg.getFirstRow());
        assertEquals(expectedAreaLastRow, copyPtg.getLastRow());

    }

    private static void confirmAreaColumnShift(
            AreaPtg aptg,
            int firstColumnMoved, int lastColumnMoved, int numberColumnsMoved,
            int expectedAreaFirstColumn, int expectedAreaLastColumn) {

        FormulaShifter fs = FormulaShifter.createForColumnShift(0, "", firstColumnMoved, lastColumnMoved, numberColumnsMoved, SpreadsheetVersion.EXCEL2007);
        boolean expectedChanged = aptg.getFirstColumn() != expectedAreaFirstColumn || aptg.getLastColumn() != expectedAreaLastColumn;

        AreaPtg copyPtg = (AreaPtg) aptg.copy(); // clone so we can re-use aptg in calling method
        Ptg[] ptgs = { copyPtg, };
        boolean actualChanged = fs.adjustFormula(ptgs, 0);
        if (expectedAreaFirstColumn < 0) {
            assertEquals(AreaErrPtg.class, ptgs[0].getClass());
            return;
        }
        assertEquals(expectedChanged, actualChanged);
        assertEquals(copyPtg, ptgs[0]);  // expected to change in place (although this is not a strict requirement)
        assertEquals(expectedAreaFirstColumn, copyPtg.getFirstColumn());
        assertEquals(expectedAreaLastColumn, copyPtg.getLastColumn());

    }
    
    
    private static void confirmAreaRowCopy(AreaPtg aptg,
                                           int firstRowCopied, int lastRowCopied, int rowOffset,
                                           int expectedFirstRow, int expectedLastRow, boolean expectedChanged) {

        final AreaPtg copyPtg = (AreaPtg) aptg.copy(); // clone so we can re-use aptg in calling method
        final Ptg[] ptgs = { copyPtg, };
        final FormulaShifter fs = FormulaShifter.createForRowCopy(0, null, firstRowCopied, lastRowCopied, rowOffset, SpreadsheetVersion.EXCEL2007);
        final boolean actualChanged = fs.adjustFormula(ptgs, 0);
        
        // DeletedAreaRef
        if (expectedFirstRow < 0 || expectedLastRow < 0) {
            assertEquals("Reference should have shifted off worksheet, producing #REF! error: " + ptgs[0],
                    AreaErrPtg.class, ptgs[0].getClass());
            return;
        }
        
        assertEquals("Should this AreaPtg change due to row copy?", expectedChanged, actualChanged);
        assertEquals("AreaPtgs should be modified in-place when a row containing the AreaPtg is copied", copyPtg, ptgs[0]);  // expected to change in place (although this is not a strict requirement)
        assertEquals("AreaPtg first row", expectedFirstRow, copyPtg.getFirstRow());
        assertEquals("AreaPtg last row", expectedLastRow, copyPtg.getLastRow());

    }

    private static void confirmAreaColumnCopy(AreaPtg aptg,
                                           int firstColumnCopied, int lastColumnCopied, int columnOffset,
                                           int expectedFirstColumn, int expectedLastColumn, boolean expectedChanged) {

        final AreaPtg copyPtg = (AreaPtg) aptg.copy(); // clone so we can re-use aptg in calling method
        final Ptg[] ptgs = { copyPtg, };
        final FormulaShifter fs = FormulaShifter.createForColumnCopy(0, null, firstColumnCopied, lastColumnCopied, columnOffset, SpreadsheetVersion.EXCEL2007);
        final boolean actualChanged = fs.adjustFormula(ptgs, 0);

        // DeletedAreaRef
        if (expectedFirstColumn < 0 || expectedLastColumn < 0) {
            assertEquals("Reference should have shifted off worksheet, producing #REF! error: " + ptgs[0],
                    AreaErrPtg.class, ptgs[0].getClass());
            return;
        }

        assertEquals("Should this AreaPtg change due to column copy?", expectedChanged, actualChanged);
        assertEquals("AreaPtgs should be modified in-place when a column containing the AreaPtg is copied", copyPtg, ptgs[0]);  // expected to change in place (although this is not a strict requirement)
        assertEquals("AreaPtg first column", expectedFirstColumn, copyPtg.getFirstColumn());
        assertEquals("AreaPtg last column", expectedLastColumn, copyPtg.getLastColumn());

    }
    
    private static AreaPtg createAreaPtgRow(int initialAreaFirstRow, int initialAreaLastRow) {
        return createAreaPtgRow(initialAreaFirstRow, initialAreaLastRow, false, false);
    }

    private static AreaPtg createAreaPtgColumn(int initialAreaFirstColumn, int initialAreaLastColumn) {
        return createAreaPtgColumn(initialAreaFirstColumn, initialAreaLastColumn, false, false);
    }
    
    private static AreaPtg createAreaPtgRow(int initialAreaFirstRow, int initialAreaLastRow, boolean firstRowRelative, boolean lastRowRelative) {
        return new AreaPtg(initialAreaFirstRow, initialAreaLastRow, 2, 5, firstRowRelative, lastRowRelative, false, false);
    }

    private static AreaPtg createAreaPtgColumn(int initialAreaFirstColumn, int initialAreaLastColumn, boolean firstColumnRelative, boolean lastColumnRelative) {
        return new AreaPtg(2, 5, initialAreaFirstColumn, initialAreaLastColumn, false, false, firstColumnRelative, lastColumnRelative);
    }

    @Test
    public void testShiftSheet() {
        // 4 sheets, move a sheet from pos 2 to pos 0, i.e. current 0 becomes 1, current 1 becomes pos 2 
        FormulaShifter shifter = FormulaShifter.createForSheetShift(2, 0);
        
        Ptg[] ptgs = new Ptg[] {
          new Ref3DPtg(new CellReference("first", 0, 0, true, true), 0),
          new Ref3DPtg(new CellReference("second", 0, 0, true, true), 1),
          new Ref3DPtg(new CellReference("third", 0, 0, true, true), 2),
          new Ref3DPtg(new CellReference("fourth", 0, 0, true, true), 3),
        };
        
        shifter.adjustFormula(ptgs, -1);
        
        assertEquals("formula previously pointing to sheet 0 should now point to sheet 1", 
                1, ((Ref3DPtg)ptgs[0]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 1 should now point to sheet 2", 
                2, ((Ref3DPtg)ptgs[1]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 2 should now point to sheet 0", 
                0, ((Ref3DPtg)ptgs[2]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 3 should be unchanged", 
                3, ((Ref3DPtg)ptgs[3]).getExternSheetIndex());
    }

    @Test
    public void testShiftSheet2() {
        // 4 sheets, move a sheet from pos 1 to pos 2, i.e. current 2 becomes 1, current 1 becomes pos 2 
        FormulaShifter shifter = FormulaShifter.createForSheetShift(1, 2);
        
        Ptg[] ptgs = new Ptg[] {
          new Ref3DPtg(new CellReference("first", 0, 0, true, true), 0),
          new Ref3DPtg(new CellReference("second", 0, 0, true, true), 1),
          new Ref3DPtg(new CellReference("third", 0, 0, true, true), 2),
          new Ref3DPtg(new CellReference("fourth", 0, 0, true, true), 3),
        };

        shifter.adjustFormula(ptgs, -1);
        
        assertEquals("formula previously pointing to sheet 0 should be unchanged", 
                0, ((Ref3DPtg)ptgs[0]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 1 should now point to sheet 2", 
                2, ((Ref3DPtg)ptgs[1]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 2 should now point to sheet 1", 
                1, ((Ref3DPtg)ptgs[2]).getExternSheetIndex());
        assertEquals("formula previously pointing to sheet 3 should be unchanged", 
                3, ((Ref3DPtg)ptgs[3]).getExternSheetIndex());
    }
    
    @Test
    public void testInvalidArgument() {
        try {
            FormulaShifter.createForRowShift(1, "name", 1, 2, 0, SpreadsheetVersion.EXCEL97);
            fail("Should catch exception here");
        } catch (IllegalArgumentException e) {
            // expected here
        }

        try {
            FormulaShifter.createForRowShift(1, "name", 2, 1, 2, SpreadsheetVersion.EXCEL97);
            fail("Should catch exception here");
        } catch (IllegalArgumentException e) {
            // expected here
        }
    }
    
    @Test
    public void testConstructor() {
        assertNotNull(FormulaShifter.createForRowShift(1, "name", 1, 2, 2, SpreadsheetVersion.EXCEL97));
    }

    @Test
    public void testToString() {
        FormulaShifter shifter = FormulaShifter.createForRowShift(0, "sheet", 123, 456, 789,
                SpreadsheetVersion.EXCEL2007);
        assertNotNull(shifter);
        assertNotNull(shifter.toString());
        assertTrue(shifter.toString().contains("123"));
        assertTrue(shifter.toString().contains("456"));
        assertTrue(shifter.toString().contains("789"));
    }
}
