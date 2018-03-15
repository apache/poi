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

import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;

/**
 * Tests CellRange operations.
 */
public final class TestCellRange extends TestCase
{
	private static final CellRangeAddress biggest     = createCR( 0, -1, 0,-1);
	private static final CellRangeAddress tenthColumn = createCR( 0, -1,10,10);
	private static final CellRangeAddress tenthRow    = createCR(10, 10, 0,-1);
	private static final CellRangeAddress box10x10    = createCR( 0, 10, 0,10);
	private static final CellRangeAddress box9x9      = createCR( 0,  9, 0, 9);
	private static final CellRangeAddress box10to20c  = createCR( 0, 10,10,20);
	private static final CellRangeAddress oneCell     = createCR(10, 10,10,10);

	private static final CellRangeAddress[] sampleRanges = {
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
	private static CellRangeAddress createCR(int firstRow, int lastRow, int firstCol, int lastCol) {
		// max row & max col limit as per BIFF8
		return new CellRangeAddress(
				firstRow, 
				lastRow == -1 ? 0xFFFF : lastRow, 
				firstCol,
				lastCol == -1 ? 0x00FF : lastCol);
	}
	
	public void testContainsMethod()
	{
		CellRangeAddress [] ranges = sampleRanges;
		for(int i=0; i!=ranges.length;i++)
		{
			for(int j=0; j!=ranges.length;j++)
			{
				boolean expectedResult = containsExpectedResults[i][j];
				assertEquals("("+i+","+j+"): ", expectedResult, CellRangeUtil.contains(ranges[i], ranges[j]));
			}
		}
	}

	private static final CellRangeAddress col1     = createCR( 0, -1, 1,1);
	private static final CellRangeAddress col2     = createCR( 0, -1, 2,2);
	private static final CellRangeAddress row1     = createCR( 1,  1, 0,-1);
	private static final CellRangeAddress row2     = createCR( 2,  2, 0,-1);

	private static final CellRangeAddress box0     = createCR( 0, 2, 0,2);
	private static final CellRangeAddress box1     = createCR( 0, 1, 0,1);
	private static final CellRangeAddress box2     = createCR( 0, 1, 2,3);
	private static final CellRangeAddress box3     = createCR( 2, 3, 0,1);
	private static final CellRangeAddress box4     = createCR( 2, 3, 2,3);
	private static final CellRangeAddress box5     = createCR( 1, 3, 1,3);

	public void testHasSharedBorderMethod()
	{
		assertFalse(CellRangeUtil.hasExactSharedBorder(col1, col1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(col2, col2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(col1, col2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(col2, col1));

		assertFalse(CellRangeUtil.hasExactSharedBorder(row1, row1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(row2, row2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(row1, row2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(row2, row1));
		
		assertFalse(CellRangeUtil.hasExactSharedBorder(row1, col1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(row1, col2));
		assertFalse(CellRangeUtil.hasExactSharedBorder(col1, row1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(col2, row1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(row2, col1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(row2, col2));
		assertFalse(CellRangeUtil.hasExactSharedBorder(col1, row2));
		assertFalse(CellRangeUtil.hasExactSharedBorder(col2, row2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(col2, col1));
		
		assertFalse(CellRangeUtil.hasExactSharedBorder(box1, box1));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box1, box2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box1, box3));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box1, box4));
		
		assertTrue(CellRangeUtil.hasExactSharedBorder(box2, box1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box2, box2));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box2, box3));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box2, box4));
		
		assertTrue(CellRangeUtil.hasExactSharedBorder(box3, box1));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box3, box2));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box3, box3));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box3, box4));
		
		assertFalse(CellRangeUtil.hasExactSharedBorder(box4, box1));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box4, box2));
		assertTrue(CellRangeUtil.hasExactSharedBorder(box4, box3));
		assertFalse(CellRangeUtil.hasExactSharedBorder(box4, box4));
	}

	public void testIntersectMethod()
	{
		assertEquals(CellRangeUtil.OVERLAP, CellRangeUtil.intersect(box0, box5));
		assertEquals(CellRangeUtil.OVERLAP, CellRangeUtil.intersect(box5, box0));
		assertEquals(CellRangeUtil.NO_INTERSECTION, CellRangeUtil.intersect(box1, box4));
		assertEquals(CellRangeUtil.NO_INTERSECTION, CellRangeUtil.intersect(box4, box1));
		assertEquals(CellRangeUtil.NO_INTERSECTION, CellRangeUtil.intersect(box2, box3));
		assertEquals(CellRangeUtil.NO_INTERSECTION, CellRangeUtil.intersect(box3, box2));
		assertEquals(CellRangeUtil.INSIDE, CellRangeUtil.intersect(box0, box1));
		assertEquals(CellRangeUtil.INSIDE, CellRangeUtil.intersect(box0, box0));
		assertEquals(CellRangeUtil.ENCLOSES, CellRangeUtil.intersect(box1, box0));
		assertEquals(CellRangeUtil.INSIDE, CellRangeUtil.intersect(tenthColumn, oneCell));
		assertEquals(CellRangeUtil.ENCLOSES, CellRangeUtil.intersect(oneCell, tenthColumn));
		assertEquals(CellRangeUtil.OVERLAP, CellRangeUtil.intersect(tenthColumn, tenthRow));
		assertEquals(CellRangeUtil.OVERLAP, CellRangeUtil.intersect(tenthRow, tenthColumn));
		assertEquals(CellRangeUtil.INSIDE, CellRangeUtil.intersect(tenthColumn, tenthColumn));
		assertEquals(CellRangeUtil.INSIDE, CellRangeUtil.intersect(tenthRow, tenthRow));
		
		// Bug 55380
		assertEquals(CellRangeUtil.OVERLAP, CellRangeUtil.intersect(
		        CellRangeAddress.valueOf("C1:D2"), CellRangeAddress.valueOf("C2:C3")));
	}
	
	/**
	 * Cell ranges like the following are valid
	 * =$C:$IV,$B$1:$B$8,$B$10:$B$65536,$A:$A
	 */
	public void testCreate() {
		CellRangeAddress cr;
		
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

	private static void confirmRange(CellRangeAddress cr, boolean isFullRow, boolean isFullColumn) {
		assertEquals("isFullRowRange", isFullRow, cr.isFullRowRange());
		assertEquals("isFullColumnRange", isFullColumn, cr.isFullColumnRange());
	}
	
	public void testNumberOfCells() {
		assertEquals(1, oneCell.getNumberOfCells());
		assertEquals(100, box9x9.getNumberOfCells());
		assertEquals(121, box10to20c.getNumberOfCells());
	}

    public void testMergeCellRanges() {
        // no result on empty
        cellRangeTest(new String[]{ });

        // various cases with two ranges
        cellRangeTest(new String[]{"A1:B1", "A2:B2"}, "A1:B2");
        cellRangeTest(new String[]{"A1:B1" }, "A1:B1");
        cellRangeTest(new String[]{"A1:B2", "A2:B2"}, "A1:B2");
        cellRangeTest(new String[]{"A1:B3", "A2:B2"}, "A1:B3");
        cellRangeTest(new String[]{"A1:C1", "A2:B2"}, new String[] {"A1:C1", "A2:B2"});
        
        // cases with three ranges
        cellRangeTest(new String[]{"A1:A1", "A2:B2", "A1:C1"}, new String[] {"A1:C1", "A2:B2"});
        cellRangeTest(new String[]{"A1:C1", "A2:B2", "A1:A1"}, new String[] {"A1:C1", "A2:B2"});
        
        // "standard" cases
        // enclose
        cellRangeTest(new String[]{"A1:D4", "B2:C3"}, new String[] {"A1:D4"});
        // inside
        cellRangeTest(new String[]{"B2:C3", "A1:D4"}, new String[] {"A1:D4"});
        cellRangeTest(new String[]{"B2:C3", "A1:D4"}, new String[] {"A1:D4"});
        // disjunct
        cellRangeTest(new String[]{"A1:B2", "C3:D4"}, new String[] {"A1:B2", "C3:D4"});
        cellRangeTest(new String[]{"A1:B2", "A3:D4"}, new String[] {"A1:B2", "A3:D4"});
        // overlap that cannot be merged
        cellRangeTest(new String[]{"C1:D2", "C2:C3"}, new String[] {"C1:D2", "C2:C3"});
        // overlap which could theoretically be merged, but isn't because the implementation was buggy and therefore was removed
        cellRangeTest(new String[]{"A1:C3", "B1:D3"}, new String[] {"A1:C3", "B1:D3"}); // could be one region "A1:D3"
        cellRangeTest(new String[]{"A1:C3", "B1:D1"}, new String[] {"A1:C3", "B1:D1"}); // could be one region "A1:D3"
    }

    public void testMergeCellRanges55380() {
        cellRangeTest(new String[]{"C1:D2", "C2:C3"}, new String[] {"C1:D2", "C2:C3"});
        cellRangeTest(new String[]{"A1:C3", "B2:D2"}, new String[] {"A1:C3", "B2:D2"});
        cellRangeTest(new String[]{"C9:D30", "C7:C31"}, new String[] {"C9:D30",  "C7:C31"});
    }
    
//    public void testResolveRangeOverlap() {
//        resolveRangeOverlapTest("C1:D2", "C2:C3");
//    }
    
    private void cellRangeTest(String[] input, String... expectedOutput) {
        CellRangeAddress[] inputArr = new CellRangeAddress[input.length];
        for(int i = 0;i < input.length;i++) {
            inputArr[i] = CellRangeAddress.valueOf(input[i]);
        }
        CellRangeAddress[] result = CellRangeUtil.mergeCellRanges(inputArr);
        verifyExpectedResult(result, expectedOutput);
    }

//    private void resolveRangeOverlapTest(String a, String b, String...expectedOutput) {
//        CellRangeAddress rangeA = CellRangeAddress.valueOf(a);
//        CellRangeAddress rangeB = CellRangeAddress.valueOf(b);
//        CellRangeAddress[] result = CellRangeUtil.resolveRangeOverlap(rangeA, rangeB);
//        verifyExpectedResult(result, expectedOutput);
//    }
    
    private void verifyExpectedResult(CellRangeAddress[] result, String... expectedOutput) {
        assertEquals("\nExpected: " + Arrays.toString(expectedOutput) + "\nHad: " + Arrays.toString(result), 
                expectedOutput.length, result.length);
        for(int i = 0;i < expectedOutput.length;i++) {
            assertEquals("\nExpected: " + Arrays.toString(expectedOutput) + "\nHad: " + Arrays.toString(result),
                    expectedOutput[i], result[i].formatAsString());
        }
    }
    
    public void testValueOf() {
        CellRangeAddress cr1 = CellRangeAddress.valueOf("A1:B1");
        assertEquals(0, cr1.getFirstColumn());
        assertEquals(0, cr1.getFirstRow());
        assertEquals(1, cr1.getLastColumn());
        assertEquals(0, cr1.getLastRow());

        CellRangeAddress cr2 = CellRangeAddress.valueOf("B1");
        assertEquals(1, cr2.getFirstColumn());
        assertEquals(0, cr2.getFirstRow());
        assertEquals(1, cr2.getLastColumn());
        assertEquals(0, cr2.getLastRow());
    }
}
