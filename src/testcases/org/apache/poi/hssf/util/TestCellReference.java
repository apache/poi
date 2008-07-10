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

import org.apache.poi.hssf.util.CellReference.NameType;


public final class TestCellReference extends TestCase {
    
    public void testAbsRef1(){
        CellReference cf = new CellReference("$B$5");
        confirmCell(cf, null, 4, 1, true, true, "$B$5");
    }
    
    public void  testAbsRef2(){
        CellReference cf = new CellReference(4,1,true,true);
        confirmCell(cf, null, 4, 1, true, true, "$B$5");
    }

    public void  testAbsRef3(){
        CellReference cf = new CellReference("B$5");
        confirmCell(cf, null, 4, 1, true, false, "B$5");
    }
    
    public void  testAbsRef4(){
        CellReference cf = new CellReference(4,1,true,false);
        confirmCell(cf, null, 4, 1, true, false, "B$5");
    }
    
    public void  testAbsRef5(){
        CellReference cf = new CellReference("$B5");
        confirmCell(cf, null, 4, 1, false, true, "$B5");
    }
    
    public void  testAbsRef6(){
        CellReference cf = new CellReference(4,1,false,true);
        confirmCell(cf, null, 4, 1, false, true, "$B5");
    }

    public void  testAbsRef7(){
        CellReference cf = new CellReference("B5");
        confirmCell(cf, null, 4, 1, false, false, "B5");
    }
    
    public void  testAbsRef8(){
        CellReference cf = new CellReference(4,1,false,false);
        confirmCell(cf, null, 4, 1, false, false, "B5");
    }
    
    public void testSpecialSheetNames() {
        CellReference cf;
        cf = new CellReference("'profit + loss'!A1");
        confirmCell(cf, "profit + loss", 0, 0, false, false, "'profit + loss'!A1");
        
        cf = new CellReference("'O''Brien''s Sales'!A1");
        confirmCell(cf, "O'Brien's Sales", 0, 0, false, false, "'O''Brien''s Sales'!A1");
        
        cf = new CellReference("'Amazing!'!A1");
        confirmCell(cf, "Amazing!", 0, 0, false, false, "'Amazing!'!A1");
    }

    /* package */ static void confirmCell(CellReference cf, String expSheetName, int expRow, 
            int expCol, boolean expIsRowAbs, boolean expIsColAbs, String expText) {
        
        assertEquals(expSheetName, cf.getSheetName());
        assertEquals("row index is wrong", expRow, cf.getRow());
        assertEquals("col index is wrong", expCol, cf.getCol());
        assertEquals("isRowAbsolute is wrong", expIsRowAbs, cf.isRowAbsolute());
        assertEquals("isColAbsolute is wrong", expIsColAbs, cf.isColAbsolute());
        assertEquals("text is wrong", expText, cf.formatAsString());
    }

    public void testClassifyCellReference() {
        confirmNameType("a1", NameType.CELL);
        confirmNameType("pfy1", NameType.NAMED_RANGE);
        confirmNameType("pf1", NameType.NAMED_RANGE); // (col) out of cell range
        confirmNameType("fp1", NameType.CELL);
        confirmNameType("pf$1", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("_A1", NameType.NAMED_RANGE);
        confirmNameType("A_1", NameType.NAMED_RANGE);
        confirmNameType("A1_", NameType.NAMED_RANGE);
        confirmNameType(".A1", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("A.1", NameType.NAMED_RANGE);
        confirmNameType("A1.", NameType.NAMED_RANGE);
    }
    
    private void confirmNameType(String ref, int expectedResult) {
        int actualResult = CellReference.classifyCellReference(ref);
        assertEquals(expectedResult, actualResult);
    }


	public void testGetCellRefParts() {
		CellReference cellReference;
		String[] parts;
		
		String cellRef = "A1";
		cellReference = new CellReference(cellRef);
		assertEquals(0, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("1", parts[1]);
		assertEquals("A", parts[2]);

		cellRef = "AA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("1", parts[1]);
		assertEquals("AA", parts[2]);

		cellRef = "AA100";
		cellReference = new CellReference(cellRef);
		assertEquals(26, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("100", parts[1]);
		assertEquals("AA", parts[2]);

		cellRef = "AAA300";
		cellReference = new CellReference(cellRef);
		assertEquals(702, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("300", parts[1]);
		assertEquals("AAA", parts[2]);

		cellRef = "ZZ100521";
		cellReference = new CellReference(cellRef);
		assertEquals(26*26+25, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("100521", parts[1]);
		assertEquals("ZZ", parts[2]);

		cellRef = "ZYX987";
		cellReference = new CellReference(cellRef);
		assertEquals(26*26*26 + 25*26 + 24 - 1, cellReference.getCol());
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("987", parts[1]);
		assertEquals("ZYX", parts[2]);

		cellRef = "AABC10065";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts();
		assertNotNull(parts);
		assertEquals(null, parts[0]);
		assertEquals("10065", parts[1]);
		assertEquals("AABC", parts[2]);
	}
	
	public void testGetColNumFromRef() {
		String cellRef = "A1";
		CellReference cellReference = new CellReference(cellRef);
		assertEquals(0, cellReference.getCol());

		cellRef = "AA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26, cellReference.getCol());

		cellRef = "AB1";
		cellReference = new CellReference(cellRef);
		assertEquals(27, cellReference.getCol());

		cellRef = "BA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26+26, cellReference.getCol());
		
		cellRef = "CA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26+26+26, cellReference.getCol());
		
		cellRef = "ZA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26*26, cellReference.getCol());
		
		cellRef = "ZZ1";
		cellReference = new CellReference(cellRef);
		assertEquals(26*26+25, cellReference.getCol());
		
		cellRef = "AAA1";
		cellReference = new CellReference(cellRef);
		assertEquals(26*26+26, cellReference.getCol());
		
		
		cellRef = "A1100";
		cellReference = new CellReference(cellRef);
		assertEquals(0, cellReference.getCol());

		cellRef = "BC15";
		cellReference = new CellReference(cellRef);
		assertEquals(54, cellReference.getCol());
	}
	
	public void testGetRowNumFromRef() {
		String cellRef = "A1";
		CellReference cellReference = new CellReference(cellRef);
		assertEquals(0, cellReference.getRow());

		cellRef = "A12";
		cellReference = new CellReference(cellRef);
		assertEquals(11, cellReference.getRow());

		cellRef = "AS121";
		cellReference = new CellReference(cellRef);
		assertEquals(120, cellReference.getRow());
	}
	
	public void testConvertNumToColString() {
		short col = 702;
		String collRef = new CellReference(0, col).formatAsString();
		assertEquals("AAA1", collRef);

		short col2 = 0;
		String collRef2 = new CellReference(0, col2).formatAsString();
		assertEquals("A1", collRef2);
		
		short col3 = 27;
		String collRef3 = new CellReference(0, col3).formatAsString();
		assertEquals("AB1", collRef3);
		
		short col4 = 2080;
		String collRef4 = new CellReference(0, col4).formatAsString();
		assertEquals("CBA1", collRef4);
	}
}