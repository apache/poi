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

package org.apache.poi.xssf.util;

import org.apache.poi.ss.util.CellReference;

import junit.framework.TestCase;


/**
 * Tests that the common CellReference works as we need it to
 */
public class TestCellReference extends TestCase {
	
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
