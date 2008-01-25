
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

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.UnionPtg;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class TestAreaReference extends TestCase {
     public TestAreaReference(String s) {
        super(s);
    }
    public void testAreaRef1() {
        AreaReference ar = new AreaReference("$A$1:$B$2");
        assertTrue("Two cells expected",ar.getCells().length == 2);
        CellReference cf = ar.getCells()[0];
        assertTrue("row is 4",cf.getRow()==0);
        assertTrue("col is 1",cf.getCol()==0);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $A$1",cf.toString().equals("$A$1"));
        
        cf = ar.getCells()[1];
        assertTrue("row is 4",cf.getRow()==1);
        assertTrue("col is 1",cf.getCol()==1);
        assertTrue("row is abs",cf.isRowAbsolute());
        assertTrue("col is abs",cf.isColAbsolute());
        assertTrue("string is $B$2",cf.toString().equals("$B$2"));
        
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
    	String ref = "Tabelle1!B5";
		AreaReference myAreaReference = new AreaReference(ref);
		CellReference[] myCellReference = myAreaReference.getCells();

		assertEquals(1, myCellReference.length);
		assertNotNull("cell reference not null : "+myCellReference[0]);
    	assertEquals("Not Column B", (short)1,myCellReference[0].getCol());
		assertEquals("Not Row 5", 4,myCellReference[0].getRow());
		assertEquals("Shouldn't be absolute", false, myCellReference[0].isRowAbsolute());
		assertEquals("Shouldn't be absolute", false, myCellReference[0].isColAbsolute());
		
		assertEquals(1, myAreaReference.getAllReferencedCells().length);
		
		
		ref = "Tabelle1!$B$5:$B$7";
		myAreaReference = new AreaReference(ref);
		myCellReference = myAreaReference.getCells();
		assertEquals(2, myCellReference.length);
		
		assertEquals("Tabelle1", myCellReference[0].getSheetName());
		assertEquals(4, myCellReference[0].getRow());
		assertEquals(1, myCellReference[0].getCol());
		assertTrue(myCellReference[0].isRowAbsolute());
		assertTrue(myCellReference[0].isColAbsolute());
		
		assertEquals("Tabelle1", myCellReference[1].getSheetName());
		assertEquals(6, myCellReference[1].getRow());
		assertEquals(1, myCellReference[1].getCol());
		assertTrue(myCellReference[1].isRowAbsolute());
		assertTrue(myCellReference[1].isColAbsolute());
		
		// And all that make it up
		myCellReference = myAreaReference.getAllReferencedCells();
		assertEquals(3, myCellReference.length);
		
		assertEquals("Tabelle1", myCellReference[0].getSheetName());
		assertEquals(4, myCellReference[0].getRow());
		assertEquals(1, myCellReference[0].getCol());
		assertTrue(myCellReference[0].isRowAbsolute());
		assertTrue(myCellReference[0].isColAbsolute());
		
		assertEquals("Tabelle1", myCellReference[1].getSheetName());
		assertEquals(5, myCellReference[1].getRow());
		assertEquals(1, myCellReference[1].getCol());
		assertTrue(myCellReference[1].isRowAbsolute());
		assertTrue(myCellReference[1].isColAbsolute());
		
		assertEquals("Tabelle1", myCellReference[2].getSheetName());
		assertEquals(6, myCellReference[2].getRow());
		assertEquals(1, myCellReference[2].getCol());
		assertTrue(myCellReference[2].isRowAbsolute());
		assertTrue(myCellReference[2].isColAbsolute());
    }

    private static class HSSFWB extends HSSFWorkbook {
        private HSSFWB(InputStream in) throws Exception {
            super(in);
        }
        public Workbook getWorkbook() {
            return super.getWorkbook();
        }
    }

    public void testContiguousReferences() throws Exception {
        String refSimple = "$C$10";
        String ref2D = "$C$10:$D$11";
        String refDCSimple = "$C$10,$D$12,$E$14";
        String refDC2D = "$C$10:$C$11,$D$12,$E$14:$E$20";
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
        } catch(IllegalArgumentException e) {}
        try {
            new AreaReference(refDC2D);
            fail();
        } catch(IllegalArgumentException e) {}
        try {
            new AreaReference(refDC3D);
            fail();
        } catch(IllegalArgumentException e) {}

        // Test that we split as expected
        AreaReference[] refs;

        refs = AreaReference.generateContiguous(refSimple);
        assertEquals(1, refs.length);
        assertEquals(1, refs[0].getDim());
        assertEquals("$C$10", refs[0].toString());

        refs = AreaReference.generateContiguous(ref2D);
        assertEquals(1, refs.length);
        assertEquals(2, refs[0].getDim());
        assertEquals("$C$10:$D$11", refs[0].toString());

        refs = AreaReference.generateContiguous(refDCSimple);
        assertEquals(3, refs.length);
        assertEquals(1, refs[0].getDim());
        assertEquals(1, refs[1].getDim());
        assertEquals(1, refs[2].getDim());
        assertEquals("$C$10", refs[0].toString());
        assertEquals("$D$12", refs[1].toString());
        assertEquals("$E$14", refs[2].toString());

        refs = AreaReference.generateContiguous(refDC2D);
        assertEquals(3, refs.length);
        assertEquals(2, refs[0].getDim());
        assertEquals(1, refs[1].getDim());
        assertEquals(2, refs[2].getDim());
        assertEquals("$C$10:$C$11", refs[0].toString());
        assertEquals("$D$12", refs[1].toString());
        assertEquals("$E$14:$E$20", refs[2].toString());

        refs = AreaReference.generateContiguous(refDC3D);
        assertEquals(2, refs.length);
        assertEquals(2, refs[0].getDim());
        assertEquals(2, refs[1].getDim());
        assertEquals("$C$10:$C$14", refs[0].toString());
        assertEquals("$D$10:$D$12", refs[1].toString());
        assertEquals("Tabelle1", refs[0].getCells()[0].getSheetName());
        assertEquals("Tabelle1", refs[0].getCells()[1].getSheetName());
        assertEquals("Tabelle1", refs[1].getCells()[0].getSheetName());
        assertEquals("Tabelle1", refs[1].getCells()[1].getSheetName());
    }

    public void testDiscontinousReference() throws Exception {
        String filename = System.getProperty( "HSSF.testdata.path" );
        filename = filename + "/44167.xls";
        FileInputStream fin = new FileInputStream( filename );
        HSSFWB wb = new HSSFWB( fin );
        Workbook workbook = wb.getWorkbook();
        fin.close();

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

        List def =nr.getNameDefinition();
        assertEquals(4, def.size());

        MemFuncPtg ptgA = (MemFuncPtg)def.get(0);
        Area3DPtg ptgB = (Area3DPtg)def.get(1);
        Area3DPtg ptgC = (Area3DPtg)def.get(2);
        UnionPtg ptgD = (UnionPtg)def.get(3);
        assertEquals("", ptgA.toFormulaString(workbook));
        assertEquals(refA, ptgB.toFormulaString(workbook));
        assertEquals(refB, ptgC.toFormulaString(workbook));
        assertEquals(",", ptgD.toFormulaString(workbook));

        assertEquals(ref, nr.getAreaReference(workbook));

        // Check the high level definition
        int idx = wb.getNameIndex("test");
        assertEquals(0, idx);
        HSSFName aNamedCell = wb.getNameAt(idx);

        // Should have 2 references
        assertEquals(ref, aNamedCell.getReference());

        // Check the parsing of the reference into cells
        assertFalse(AreaReference.isContiguous(aNamedCell.getReference()));
        AreaReference[] arefs = AreaReference.generateContiguous(aNamedCell.getReference());
        assertEquals(2, arefs.length);
        assertEquals(rawRefA, arefs[0].toString());
        assertEquals(rawRefB, arefs[1].toString());

        for(int i=0; i<arefs.length; i++) {
            CellReference[] crefs = arefs[i].getCells();
            for (int j=0; j<crefs.length; j++) {
                // Check it turns into real stuff
                HSSFSheet s = wb.getSheet(crefs[j].getSheetName());
                HSSFRow r = s.getRow(crefs[j].getRow());
                HSSFCell c = r.getCell(crefs[j].getCol());
            }
        }
    }
    
    public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(TestAreaReference.class);
	}
        
}
