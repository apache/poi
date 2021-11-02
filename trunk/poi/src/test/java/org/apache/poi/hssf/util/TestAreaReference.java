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

package org.apache.poi.hssf.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.UnionPtg;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

final class TestAreaReference {

    @Test
    void testAreaRef1() {
        AreaReference ar = new AreaReference("$A$1:$B$2", SpreadsheetVersion.EXCEL97);
        assertFalse(ar.isSingleCell(), "Two cells expected");
        CellReference cf = ar.getFirstCell();
        assertEquals(0, cf.getRow(), "row is 4");
        assertEquals(0, cf.getCol(), "col is 1");
        assertTrue(cf.isRowAbsolute(), "row is abs");
        assertTrue(cf.isColAbsolute(), "col is abs");
        assertEquals("$A$1", cf.formatAsString(), "string is $A$1");

        cf = ar.getLastCell();
        assertEquals(1, cf.getRow(), "row is 4");
        assertEquals(1, cf.getCol(), "col is 1");
        assertTrue(cf.isRowAbsolute(), "row is abs");
        assertTrue(cf.isColAbsolute(), "col is abs");
        assertEquals("$B$2", cf.formatAsString(), "string is $B$2");

        CellReference[] refs = ar.getAllReferencedCells();
        assertEquals(4, refs.length);

        assertEquals(0, refs[0].getRow());
        assertEquals(0, refs[0].getCol());
        assertNull(refs[0].getSheetName());

        assertEquals(0, refs[1].getRow());
        assertEquals(1, refs[1].getCol());
        assertNull(refs[1].getSheetName());

        assertEquals(1, refs[2].getRow());
        assertEquals(0, refs[2].getCol());
        assertNull(refs[2].getSheetName());

        assertEquals(1, refs[3].getRow());
        assertEquals(1, refs[3].getCol());
        assertNull(refs[3].getSheetName());
    }

    /**
     * References failed when sheet names were being used Reported by
     * Arne.Clauss@gedas.de
     */
    @Test
    void testReferenceWithSheet() {
        AreaReference ar;

        ar = new AreaReference("Tabelle1!B5:B5", SpreadsheetVersion.EXCEL97);
        assertTrue(ar.isSingleCell());
        TestCellReference.confirmCell(ar.getFirstCell(), "Tabelle1", 4, 1, false, false, "Tabelle1!B5");

        assertEquals(1, ar.getAllReferencedCells().length);

        ar = new AreaReference("Tabelle1!$B$5:$B$7", SpreadsheetVersion.EXCEL97);
        assertFalse(ar.isSingleCell());

        TestCellReference.confirmCell(ar.getFirstCell(), "Tabelle1", 4, 1, true, true, "Tabelle1!$B$5");
        TestCellReference.confirmCell(ar.getLastCell(), "Tabelle1", 6, 1, true, true, "Tabelle1!$B$7");

        // And all that make it up
        CellReference[] allCells = ar.getAllReferencedCells();
        assertEquals(3, allCells.length);
        TestCellReference.confirmCell(allCells[0], "Tabelle1", 4, 1, true, true, "Tabelle1!$B$5");
        TestCellReference.confirmCell(allCells[1], "Tabelle1", 5, 1, true, true, "Tabelle1!$B$6");
        TestCellReference.confirmCell(allCells[2], "Tabelle1", 6, 1, true, true, "Tabelle1!$B$7");
    }

    @Test
    void testContiguousReferences() {
        String refSimple = "$C$10:$C$10";
        String ref2D = "$C$10:$D$11";
        String refDCSimple = "$C$10:$C$10,$D$12:$D$12,$E$14:$E$14";
        String refDC2D = "$C$10:$C$11,$D$12:$D$12,$E$14:$E$20";
        String refDC3D = "Tabelle1!$C$10:$C$14,Tabelle1!$D$10:$D$12";
        String refComma = "'A,Sheet'!$A$1:$A$1,'A,Sheet'!$A$4:$A$5";
        String refCommaExp = "'!Sheet,Comma!'!$A$1:$B$1";

        // Check that we detect as contiguous properly
        assertTrue(AreaReference.isContiguous(refSimple));
        assertTrue(AreaReference.isContiguous(ref2D));
        assertFalse(AreaReference.isContiguous(refDCSimple));
        assertFalse(AreaReference.isContiguous(refDC2D));
        assertFalse(AreaReference.isContiguous(refDC3D));
        assertFalse(AreaReference.isContiguous(refComma));
        assertTrue(AreaReference.isContiguous(refCommaExp));

        // Check we can only create contiguous entries
        new AreaReference(refSimple, SpreadsheetVersion.EXCEL97);
        new AreaReference(ref2D, SpreadsheetVersion.EXCEL97);
        assertThrows(IllegalArgumentException.class, () -> new AreaReference(refDCSimple, SpreadsheetVersion.EXCEL97));
        assertThrows(IllegalArgumentException.class, () -> new AreaReference(refDC2D, SpreadsheetVersion.EXCEL97));
        assertThrows(IllegalArgumentException.class, () -> new AreaReference(refDC3D, SpreadsheetVersion.EXCEL97));

        // Test that we split as expected
        AreaReference[] refs;

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refSimple);
        assertEquals(1, refs.length);
        assertTrue(refs[0].isSingleCell());
        assertEquals("$C$10", refs[0].formatAsString());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, ref2D);
        assertEquals(1, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertEquals("$C$10:$D$11", refs[0].formatAsString());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refDCSimple);
        assertEquals(3, refs.length);
        assertTrue(refs[0].isSingleCell());
        assertTrue(refs[1].isSingleCell());
        assertTrue(refs[2].isSingleCell());
        assertEquals("$C$10", refs[0].formatAsString());
        assertEquals("$D$12", refs[1].formatAsString());
        assertEquals("$E$14", refs[2].formatAsString());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refDC2D);
        assertEquals(3, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertTrue(refs[1].isSingleCell());
        assertFalse(refs[2].isSingleCell());
        assertEquals("$C$10:$C$11", refs[0].formatAsString());
        assertEquals("$D$12", refs[1].formatAsString());
        assertEquals("$E$14:$E$20", refs[2].formatAsString());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refDC3D);
        assertEquals(2, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertFalse(refs[0].isSingleCell());
        assertEquals("Tabelle1!$C$10:$C$14", refs[0].formatAsString());
        assertEquals("Tabelle1!$D$10:$D$12", refs[1].formatAsString());
        assertEquals("Tabelle1", refs[0].getFirstCell().getSheetName());
        assertEquals("Tabelle1", refs[0].getLastCell().getSheetName());
        assertEquals("Tabelle1", refs[1].getFirstCell().getSheetName());
        assertEquals("Tabelle1", refs[1].getLastCell().getSheetName());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refComma);
        assertEquals(2, refs.length);
        System.out.println(refs[0].formatAsString());
        assertTrue(refs[0].isSingleCell());
        assertEquals("'A,Sheet'!$A$1", refs[0].formatAsString());
        assertEquals("A,Sheet", refs[0].getLastCell().getSheetName());
        assertEquals("'A,Sheet'!$A$4:$A$5", refs[1].formatAsString());
        assertEquals("A,Sheet", refs[1].getLastCell().getSheetName());

        refs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, refCommaExp);
        assertEquals(1, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertEquals("'!Sheet,Comma!'!$A$1:$B$1", refs[0].formatAsString());
        assertEquals("!Sheet,Comma!", refs[0].getLastCell().getSheetName());
    }

    @Test
    void testDiscontinousReference() throws Exception {
        try (InputStream is = HSSFTestDataSamples.openSampleFileStream("44167.xls");
             HSSFWorkbook wb = new HSSFWorkbook(is)) {
            InternalWorkbook workbook = TestHSSFWorkbook.getInternalWorkbook(wb);
            HSSFEvaluationWorkbook eb = HSSFEvaluationWorkbook.create(wb);

            assertEquals(1, wb.getNumberOfNames());
            String sheetName = "Tabelle1";
            String rawRefA = "$C$10:$C$14";
            String rawRefB = "$C$16:$C$18";
            String refA = sheetName + "!" + rawRefA;
            String refB = sheetName + "!" + rawRefB;
            String ref = refA + "," + refB;

            // Check the low level record
            NameRecord nr = workbook.getNameRecord(0);
            assertNotNull(nr);
            assertEquals("test", nr.getNameText());

            Ptg[] def = nr.getNameDefinition();
            assertEquals(4, def.length);

            MemFuncPtg ptgA = (MemFuncPtg) def[0];
            Area3DPtg ptgB = (Area3DPtg) def[1];
            Area3DPtg ptgC = (Area3DPtg) def[2];
            UnionPtg ptgD = (UnionPtg) def[3];
            assertEquals("", ptgA.toFormulaString());
            assertEquals(refA, ptgB.toFormulaString(eb));
            assertEquals(refB, ptgC.toFormulaString(eb));
            assertEquals(",", ptgD.toFormulaString());

            assertEquals(ref, HSSFFormulaParser.toFormulaString(wb, nr.getNameDefinition()));

            // Check the high level definition
            HSSFName aNamedCell = wb.getName("test");

            // Should have 2 references
            assertNotNull(aNamedCell);
            String formulaRefs = aNamedCell.getRefersToFormula();
            assertNotNull(formulaRefs);
            assertEquals(ref, formulaRefs);

            // Check the parsing of the reference into cells
            assertFalse(AreaReference.isContiguous(formulaRefs));
            AreaReference[] arefs = AreaReference.generateContiguous(SpreadsheetVersion.EXCEL97, formulaRefs);
            assertEquals(2, arefs.length);
            assertEquals(refA, arefs[0].formatAsString());
            assertEquals(refB, arefs[1].formatAsString());

            for (AreaReference ar : arefs) {
                confirmResolveCellRef(wb, ar.getFirstCell());
                confirmResolveCellRef(wb, ar.getLastCell());
            }
        }
    }

    private static void confirmResolveCellRef(HSSFWorkbook wb, CellReference cref) {
        HSSFSheet s = wb.getSheet(cref.getSheetName());
        HSSFRow r = s.getRow(cref.getRow());
        HSSFCell c = r.getCell(cref.getCol());
        assertNotNull(c);
    }

    @Test
    void testSpecialSheetNames() {
        AreaReference ar;
        ar = new AreaReference("'Sheet A'!A1:A1", SpreadsheetVersion.EXCEL97);
        confirmAreaSheetName(ar, "Sheet A", "'Sheet A'!A1");

        ar = new AreaReference("'Hey! Look Here!'!A1:A1", SpreadsheetVersion.EXCEL97);
        confirmAreaSheetName(ar, "Hey! Look Here!", "'Hey! Look Here!'!A1");

        ar = new AreaReference("'O''Toole'!A1:B2", SpreadsheetVersion.EXCEL97);
        confirmAreaSheetName(ar, "O'Toole", "'O''Toole'!A1:B2");

        ar = new AreaReference("'one:many'!A1:B2", SpreadsheetVersion.EXCEL97);
        confirmAreaSheetName(ar, "one:many", "'one:many'!A1:B2");

        ar = new AreaReference("'O,Comma'!A1:B1", SpreadsheetVersion.EXCEL97);
        confirmAreaSheetName(ar, "O,Comma", "'O,Comma'!A1:B1");
    }

    private static void confirmAreaSheetName(AreaReference ar, String sheetName, String expectedFullText) {
        CellReference[] cells = ar.getAllReferencedCells();
        assertEquals(sheetName, cells[0].getSheetName());
        assertEquals(expectedFullText, ar.formatAsString());
    }

    @Test
    void testWholeColumnRefs() {
        confirmWholeColumnRef("A:A", 0, 0, false, false);
        confirmWholeColumnRef("$C:D", 2, 3, true, false);
        confirmWholeColumnRef("AD:$AE", 29, 30, false, true);
    }

    private static void confirmWholeColumnRef(String ref, int firstCol, int lastCol, boolean firstIsAbs, boolean lastIsAbs) {
        AreaReference ar = new AreaReference(ref, SpreadsheetVersion.EXCEL97);
        confirmCell(ar.getFirstCell(), 0, firstCol, true, firstIsAbs);
        confirmCell(ar.getLastCell(), 0xFFFF, lastCol, true, lastIsAbs);
    }

    private static void confirmCell(CellReference cell, int row, int col, boolean isRowAbs, boolean isColAbs) {
        assertEquals(row, cell.getRow());
        assertEquals(col, cell.getCol());
        assertEquals(isRowAbs, cell.isRowAbsolute());
        assertEquals(isColAbs, cell.isColAbsolute());
    }
}
