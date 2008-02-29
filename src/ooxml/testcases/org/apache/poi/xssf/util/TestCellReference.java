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

import org.apache.poi.xssf.util.CellReference;

import junit.framework.TestCase;


public class TestCellReference extends TestCase {
	
	public void testGetCellRefParts() {
		String cellRef = "A1";
		CellReference cellReference = new CellReference(cellRef);
		String[] parts = cellReference.getCellRefParts(cellRef);
		assertNotNull(parts);
		assertEquals("A", parts[0]);
		assertEquals("1", parts[1]);

		cellRef = "AA1";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		assertNotNull(parts);
		assertEquals("AA", parts[0]);
		assertEquals("1", parts[1]);

		cellRef = "AA100";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		assertNotNull(parts);
		assertEquals("AA", parts[0]);
		assertEquals("100", parts[1]);


		cellRef = "AABC10065";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		assertNotNull(parts);
		assertEquals("AABC", parts[0]);
		assertEquals("10065", parts[1]);
	}
	
	public void testGetColNumFromRef() {
		String cellRef = "A1";
		CellReference cellReference = new CellReference(cellRef);
		String[] parts = cellReference.getCellRefParts(cellRef);
		short col = cellReference.getColNumFromRef(parts[0]);
		assertEquals(0, col);

		cellRef = "AB1";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		col = cellReference.getColNumFromRef(parts[0]);
		assertEquals(27, col);

		cellRef = "A1100";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		col = cellReference.getColNumFromRef(parts[0]);
		assertEquals(0, col);

		cellRef = "BC15";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		col = cellReference.getColNumFromRef(parts[0]);
		assertEquals(54, col);
	}
	
	public void testGetRowNumFromRef() {
		String cellRef = "A1";
		CellReference cellReference = new CellReference(cellRef);
		String[] parts = cellReference.getCellRefParts(cellRef);
		int row = cellReference.getRowNumFromRef(parts[1]);
		assertEquals(0, row);

		cellRef = "A12";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		row = cellReference.getRowNumFromRef(parts[1]);
		assertEquals(11, row);

		cellRef = "AS121";
		cellReference = new CellReference(cellRef);
		parts = cellReference.getCellRefParts(cellRef);
		row = cellReference.getRowNumFromRef(parts[1]);
		assertEquals(120, row);
	}
	
	public void testConvertNumToColString() {
		short col = 702;
		String collRef = new CellReference().convertNumToColString(col);
		assertEquals("AAA", collRef);
		System.err.println("***");
		short col2 = 0;
		String collRef2 = new CellReference().convertNumToColString(col2);
		assertEquals("A", collRef2);
		short col3 = 27;
		String collRef3 = new CellReference().convertNumToColString(col3);
		assertEquals("AB", collRef3);
	}
	
}
