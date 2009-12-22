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

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.UnionPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.TestHSSFWorkbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
/**
 *
 */
public final class TestAreaReference extends TestCase {

    public void testAreaRef1() {
        AreaReference ar = new AreaReference("$A$1:$B$2");
        assertFalse("Two cells expected", ar.isSingleCell());
        CellReference cf = ar.getFirstCell();
        assertTrue("row is 4",cf.getRow()==0);
        assertTrue("col is 1",cf.getCol()==0);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $A$1",cf.formatAsString().equals("$A$1"));

        cf = ar.getLastCell();
        assertTrue("row is 4",cf.getRow()==1);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$2",cf.formatAsString().equals("$B$2"));

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
     * References failed when sheet names were being used
     * Reported by Arne.Clauss@gedas.de
     */
    public void testReferenceWithSheet() {
        AreaReference ar;

        ar = new AreaReference("Tabelle1!B5:B5");
        assertTrue(ar.isSingleCell());
        TestCellReference.confirmCell(ar.getFirstCell(), "Tabelle1", 4, 1, false, false, "Tabelle1!B5");

        assertEquals(1, ar.getAllReferencedCells().length);


        ar = new AreaReference("Tabelle1!$B$5:$B$7");
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

    public void testContiguousReferences() {
        String refSimple = "$C$10:$C$10";
        String ref2D = "$C$10:$D$11";
        String refDCSimple = "$C$10:$C$10,$D$12:$D$12,$E$14:$E$14";
        String refDC2D = "$C$10:$C$11,$D$12:$D$12,$E$14:$E$20";
        String refDC3D = "Tabelle1!$C$10:$C$14,Tabelle1!$D$10:$D$12";

        // Check that we detect as contiguous properly
        assertTrue(AreaReference.isContiguous(refSimple));
        assertTrue(AreaReference.isContiguous(ref2D));
        assertFalse(AreaReference.isContiguous(refDCSimple));
        assertFalse(AreaReference.isContiguous(refDC2D));
        assertFalse(AreaReference.isContiguous(refDC3D));

        // Check we can only create contiguous entries
        new AreaReference(refSimple);
        new AreaReference(ref2D);
        try {
            new AreaReference(refDCSimple);
            fail();
        } catch(IllegalArgumentException e) {
        	// expected during successful test
        }
        try {
            new AreaReference(refDC2D);
            fail();
        } catch(IllegalArgumentException e) {
        	// expected during successful test
        }
        try {
            new AreaReference(refDC3D);
            fail();
        } catch(IllegalArgumentException e) {
        	// expected during successful test
        }

        // Test that we split as expected
        AreaReference[] refs;

        refs = AreaReference.generateContiguous(refSimple);
        assertEquals(1, refs.length);
        assertTrue(refs[0].isSingleCell());
        assertEquals("$C$10", refs[0].formatAsString());

        refs = AreaReference.generateContiguous(ref2D);
        assertEquals(1, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertEquals("$C$10:$D$11", refs[0].formatAsString());

        refs = AreaReference.generateContiguous(refDCSimple);
        assertEquals(3, refs.length);
        assertTrue(refs[0].isSingleCell());
        assertTrue(refs[1].isSingleCell());
        assertTrue(refs[2].isSingleCell());
        assertEquals("$C$10", refs[0].formatAsString());
        assertEquals("$D$12", refs[1].formatAsString());
        assertEquals("$E$14", refs[2].formatAsString());

        refs = AreaReference.generateContiguous(refDC2D);
        assertEquals(3, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertTrue(refs[1].isSingleCell());
        assertFalse(refs[2].isSingleCell());
        assertEquals("$C$10:$C$11", refs[0].formatAsString());
        assertEquals("$D$12", refs[1].formatAsString());
        assertEquals("$E$14:$E$20", refs[2].formatAsString());

        refs = AreaReference.generateContiguous(refDC3D);
        assertEquals(2, refs.length);
        assertFalse(refs[0].isSingleCell());
        assertFalse(refs[0].isSingleCell());
        assertEquals("Tabelle1!$C$10:$C$14", refs[0].formatAsString());
        assertEquals("Tabelle1!$D$10:$D$12", refs[1].formatAsString());
        assertEquals("Tabelle1", refs[0].getFirstCell().getSheetName());
        assertEquals("Tabelle1", refs[0].getLastCell().getSheetName());
        assertEquals("Tabelle1", refs[1].getFirstCell().getSheetName());
        assertEquals("Tabelle1", refs[1].getLastCell().getSheetName());
    }

    public void testDiscontinousReference() throws Exception {
        InputStream is = HSSFTestDataSamples.openSampleFileStream("44167.xls");
        HSSFWorkbook wb = new HSSFWorkbook(is);
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

        Ptg[] def =nr.getNameDefinition();
        assertEquals(4, def.length);

        MemFuncPtg ptgA = (MemFuncPtg)def[0];
        Area3DPtg ptgB = (Area3DPtg)def[1];
        Area3DPtg ptgC = (Area3DPtg)def[2];
        UnionPtg ptgD = (UnionPtg)def[3];
        assertEquals("", ptgA.toFormulaString());
        assertEquals(refA, ptgB.toFormulaString(eb));
        assertEquals(refB, ptgC.toFormulaString(eb));
        assertEquals(",", ptgD.toFormulaString());

        assertEquals(ref, HSSFFormulaParser.toFormulaString(wb, nr.getNameDefinition()));

        // Check the high level definition
        int idx = wb.getNameIndex("test");
        assertEquals(0, idx);
        HSSFName aNamedCell = wb.getNameAt(idx);

        // Should have 2 references
        assertEquals(ref, aNamedCell.getRefersToFormula());

        // Check the parsing of the reference into cells
        assertFalse(AreaReference.isContiguous(aNamedCell.getRefersToFormula()));
        AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getRefersToFormula());
        assertEquals(2, arefs.length);
        assertEquals(refA, arefs[0].formatAsString());
        assertEquals(refB, arefs[1].formatAsString());

        for(int i=0; i<arefs.length; i++) {
            AreaReference ar = arefs[i];
            confirmResolveCellRef(wb, ar.getFirstCell());
            confirmResolveCellRef(wb, ar.getLastCell());
        }
    }

    private static void confirmResolveCellRef(HSSFWorkbook wb, CellReference cref) {
        HSSFSheet s = wb.getSheet(cref.getSheetName());
        HSSFRow r = s.getRow(cref.getRow());
        HSSFCell c = r.getCell((int)cref.getCol());
        assertNotNull(c);
    }

    public void testSpecialSheetNames() {
        AreaReference ar;
        ar = new AreaReference("'Sheet A'!A1:A1");
        confirmAreaSheetName(ar, "Sheet A", "'Sheet A'!A1");

        ar = new AreaReference("'Hey! Look Here!'!A1:A1");
        confirmAreaSheetName(ar, "Hey! Look Here!", "'Hey! Look Here!'!A1");

        ar = new AreaReference("'O''Toole'!A1:B2");
        confirmAreaSheetName(ar, "O'Toole", "'O''Toole'!A1:B2");

        ar = new AreaReference("'one:many'!A1:B2");
        confirmAreaSheetName(ar, "one:many", "'one:many'!A1:B2");
    }

    private static void confirmAreaSheetName(AreaReference ar, String sheetName, String expectedFullText) {
        CellReference[] cells = ar.getAllReferencedCells();
        assertEquals(sheetName, cells[0].getSheetName());
        assertEquals(expectedFullText, ar.formatAsString());
    }

    public void testWholeColumnRefs() {
        confirmWholeColumnRef("A:A", 0, 0, false, false);
        confirmWholeColumnRef("$C:D", 2, 3, true, false);
        confirmWholeColumnRef("AD:$AE", 29, 30, false, true);
    }

    private static void confirmWholeColumnRef(String ref, int firstCol, int lastCol, boolean firstIsAbs, boolean lastIsAbs) {
        AreaReference ar = new AreaReference(ref);
        confirmCell(ar.getFirstCell(), 0, firstCol, true, firstIsAbs);
        confirmCell(ar.getLastCell(), 0xFFFF, lastCol, true, lastIsAbs);
    }

    private static void confirmCell(CellReference cell, int row, int col, boolean isRowAbs,
            boolean isColAbs) {
        assertEquals(row, cell.getRow());
        assertEquals(col, cell.getCol());
        assertEquals(isRowAbs, cell.isRowAbsolute());
        assertEquals(isColAbs, cell.isColAbsolute());
    }
}
