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

import junit.framework.TestCase;

/**
 * Tests CellRange operations.
 */
public class TestCellRange extends TestCase
{
	private static final CellRange biggest     = new CellRange( 0, -1, 0,-1);
	private static final CellRange tenthColumn = new CellRange( 0, -1,10,10);
	private static final CellRange tenthRow    = new CellRange(10, 10, 0,-1);
	private static final CellRange box10x10    = new CellRange( 0, 10, 0,10);
	private static final CellRange box9x9      = new CellRange( 0,  9, 0, 9);
	private static final CellRange box10to20c  = new CellRange( 0, 10,10,20);
	private static final CellRange oneCell     = new CellRange(10, 10,10,10);

	boolean [][] contanis = new boolean[][]
    {
        		//           biggest, tenthColumn, tenthRow, box10x10, box9x9, box10to20c, oneCell
  /*biggest    */ new boolean[]{true,       true ,    true ,    true ,  true ,      true ,  true},	
  /*tenthColumn*/ new boolean[]{false,      true ,    false,    false,  false,      false,  true},	
  /*tenthRow   */ new boolean[]{false,      false,    true ,    false,  false,      false,  true},	
  /*box10x10   */ new boolean[]{false,      false,    false,    true ,  true ,      false,  true},	
  /*box9x9     */ new boolean[]{false,      false,    false,    false,  true ,      false, false},	
  /*box10to20c */ new boolean[]{false,      false,    false,    false,  false,      true ,  true},	
  /*oneCell    */ new boolean[]{false,      false,    false,    false,  false,      false,  true},	
     } ;
	
	
	public void testContainsMethod()
	{
		CellRange [] ranges = new CellRange[]{biggest,tenthColumn,tenthRow,box10x10,box9x9,box10to20c,oneCell};
		testContainsMethod(contanis,ranges);
	}
	
	private void testContainsMethod(boolean[][]contains,CellRange[] ranges)
	{
		for(int i=0; i!=ranges.length;i++)
		{
			for(int j=0; j!=ranges.length;j++)
			{
				assertEquals("("+i+","+j+"): ",contains[i][j],ranges[i].contains(ranges[j]));
			}
		}
	}

	private static final CellRange col1     = new CellRange( 0, -1, 1,1);
	private static final CellRange col2     = new CellRange( 0, -1, 2,2);
	private static final CellRange row1     = new CellRange( 1,  1, 0,-1);
	private static final CellRange row2     = new CellRange( 2,  2, 0,-1);

	private static final CellRange box0     = new CellRange( 0, 2, 0,2);
	private static final CellRange box1     = new CellRange( 0, 1, 0,1);
	private static final CellRange box2     = new CellRange( 0, 1, 2,3);
	private static final CellRange box3     = new CellRange( 2, 3, 0,1);
	private static final CellRange box4     = new CellRange( 2, 3, 2,3);
	private static final CellRange box5     = new CellRange( 1, 3, 1,3);

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
		assertEquals( CellRange.OVERLAP,box0.intersect(box5));
		assertEquals( CellRange.OVERLAP,box5.intersect(box0));
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
}
