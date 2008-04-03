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

package org.apache.poi.hssf.record.cf;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests CellRange operations.
 */
public final class TestCellRange extends TestCase
{
	private static final CellRange biggest     = createCR( 0, -1, 0,-1);
	private static final CellRange tenthColumn = createCR( 0, -1,10,10);
	private static final CellRange tenthRow    = createCR(10, 10, 0,-1);
	private static final CellRange box10x10    = createCR( 0, 10, 0,10);
	private static final CellRange box9x9      = createCR( 0,  9, 0, 9);
	private static final CellRange box10to20c  = createCR( 0, 10,10,20);
	private static final CellRange oneCell     = createCR(10, 10,10,10);

	private static final CellRange[] sampleRanges = {
		biggest, tenthColumn, tenthRow, box10x10, box9x9, box10to20c, oneCell,
	};
	
	/** cross-reference of <tt>contains()</tt> operations for sampleRanges against itself */
	private static final boolean [][] containsExpectedResults = 
    {
	//               biggest, tenthColumn, tenthRow, box10x10, box9x9, box10to20c, oneCell
	/*biggest    */ {true,       true ,    true ,    true ,    true ,      true ,  true},	
	/*tenthColumn*/ {false,      true ,    false,    false,    false,      false,  true},	
	/*tenthRow   */ {false,      false,    true ,    false,    false,      false,  true},	
	/*box10x10   */ {false,      false,    false,    true ,    true ,      false,  true},	
	/*box9x9     */ {false,      false,    false,    false,    true ,      false, false},	
	/*box10to20c */ {false,      false,    false,    false,    false,      true ,  true},	
	/*oneCell    */ {false,      false,    false,    false,    false,      false,  true},	
     } ;

	/**
	 * @param lastRow pass -1 for max row index 
	 * @param lastCol pass -1 for max col index
	 */
	private static CellRange createCR(int firstRow, int lastRow, int firstCol, int lastCol) {
		// max row & max col limit as per BIFF8
		return new CellRange(
				firstRow, 
				lastRow == -1 ? 0xFFFF : lastRow, 
				firstCol,
				lastCol == -1 ? 0x00FF : lastCol);
	}
	
	public void testContainsMethod()
	{
		CellRange [] ranges = sampleRanges;
		for(int i=0; i!=ranges.length;i++)
		{
			for(int j=0; j!=ranges.length;j++)
			{
				boolean expectedResult = containsExpectedResults[i][j];
				assertEquals("("+i+","+j+"): ", expectedResult, ranges[i].contains(ranges[j]));
			}
		}
	}

	private static final CellRange col1     = createCR( 0, -1, 1,1);
	private static final CellRange col2     = createCR( 0, -1, 2,2);
	private static final CellRange row1     = createCR( 1,  1, 0,-1);
	private static final CellRange row2     = createCR( 2,  2, 0,-1);

	private static final CellRange box0     = createCR( 0, 2, 0,2);
	private static final CellRange box1     = createCR( 0, 1, 0,1);
	private static final CellRange box2     = createCR( 0, 1, 2,3);
	private static final CellRange box3     = createCR( 2, 3, 0,1);
	private static final CellRange box4     = createCR( 2, 3, 2,3);
	private static final CellRange box5     = createCR( 1, 3, 1,3);

	public void testHasSharedBorderMethod()
	{
		assertFalse(col1.hasExactSharedBorder(col1));
		assertFalse(col2.hasExactSharedBorder(col2));
		assertTrue(col1.hasExactSharedBorder(col2));
		assertTrue(col2.hasExactSharedBorder(col1));

		assertFalse(row1.hasExactSharedBorder(row1));
		assertFalse(row2.hasExactSharedBorder(row2));
		assertTrue(row1.hasExactSharedBorder(row2));
		assertTrue(row2.hasExactSharedBorder(row1));
		
		assertFalse(row1.hasExactSharedBorder(col1));
		assertFalse(row1.hasExactSharedBorder(col2));
		assertFalse(col1.hasExactSharedBorder(row1));
		assertFalse(col2.hasExactSharedBorder(row1));
		assertFalse(row2.hasExactSharedBorder(col1));
		assertFalse(row2.hasExactSharedBorder(col2));
		assertFalse(col1.hasExactSharedBorder(row2));
		assertFalse(col2.hasExactSharedBorder(row2));
		assertTrue(col2.hasExactSharedBorder(col1));
		
		assertFalse(box1.hasExactSharedBorder(box1));
		assertTrue(box1.hasExactSharedBorder(box2));
		assertTrue(box1.hasExactSharedBorder(box3));
		assertFalse(box1.hasExactSharedBorder(box4));
		
		assertTrue(box2.hasExactSharedBorder(box1));
		assertFalse(box2.hasExactSharedBorder(box2));
		assertFalse(box2.hasExactSharedBorder(box3));
		assertTrue(box2.hasExactSharedBorder(box4));
		
		assertTrue(box3.hasExactSharedBorder(box1));
		assertFalse(box3.hasExactSharedBorder(box2));
		assertFalse(box3.hasExactSharedBorder(box3));
		assertTrue(box3.hasExactSharedBorder(box4));
		
		assertFalse(box4.hasExactSharedBorder(box1));
		assertTrue(box4.hasExactSharedBorder(box2));
		assertTrue(box4.hasExactSharedBorder(box3));
		assertFalse(box4.hasExactSharedBorder(box4));
	}

	public void testIntersectMethod()
	{
		assertEquals(CellRange.OVERLAP,box0.intersect(box5));
		assertEquals(CellRange.OVERLAP,box5.intersect(box0));
		assertEquals(CellRange.NO_INTERSECTION,box1.intersect(box4));
		assertEquals(CellRange.NO_INTERSECTION,box4.intersect(box1));
		assertEquals(CellRange.NO_INTERSECTION,box2.intersect(box3));
		assertEquals(CellRange.NO_INTERSECTION,box3.intersect(box2));
		assertEquals(CellRange.INSIDE,box0.intersect(box1));
		assertEquals(CellRange.INSIDE,box0.intersect(box0));
		assertEquals(CellRange.ENCLOSES,box1.intersect(box0));
		assertEquals(CellRange.INSIDE,tenthColumn.intersect(oneCell));
		assertEquals(CellRange.ENCLOSES,oneCell.intersect(tenthColumn));
		assertEquals(CellRange.OVERLAP,tenthColumn.intersect(tenthRow));
		assertEquals(CellRange.OVERLAP,tenthRow.intersect(tenthColumn));
		assertEquals(CellRange.INSIDE,tenthColumn.intersect(tenthColumn));
		assertEquals(CellRange.INSIDE,tenthRow.intersect(tenthRow));
	}
	
	/**
	 * Cell ranges like the following are valid
	 * =$C:$IV,$B$1:$B$8,$B$10:$B$65536,$A:$A
	 */
	public void testCreate() {
		CellRange cr;
		
		cr = createCR(0, -1, 2, 255); // $C:$IV
		confirmRange(cr, false, true);
		cr = createCR(0, 7, 1, 1); // $B$1:$B$8
		
		try {
			cr = createCR(9, -1, 1, 1); // $B$65536
		} catch (IllegalArgumentException e) {
			if(e.getMessage().startsWith("invalid cell range")) {
				throw new AssertionFailedError("Identified bug 44739");
			}
			throw e;
		}
		cr = createCR(0, -1, 0, 0); // $A:$A
	}

	private static void confirmRange(CellRange cr, boolean isFullRow, boolean isFullColumn) {
		assertEquals("isFullRowRange", isFullRow, cr.isFullRowRange());
		assertEquals("isFullColumnRange", isFullColumn, cr.isFullColumnRange());
	}
}
