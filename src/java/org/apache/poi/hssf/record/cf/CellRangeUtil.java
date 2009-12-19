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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 
 * @author Dmitriy Kumshayev
 */
public final class CellRangeUtil
{
	
	private CellRangeUtil() {
		// no instance of this class
	}
	
	public static final int NO_INTERSECTION = 1;
	public static final int OVERLAP = 2;
	/** first range is within the second range */
	public static final int INSIDE = 3;
	/** first range encloses or is equal to the second */
	public static final int ENCLOSES = 4;
	
	/**
	 * Intersect this range with the specified range.
	 * 
	 * @param crB - the specified range
	 * @return code which reflects how the specified range is related to this range.<br/>
	 * Possible return codes are:	
	 * 		NO_INTERSECTION - the specified range is outside of this range;<br/> 
	 * 		OVERLAP - both ranges partially overlap;<br/>
	 * 		INSIDE - the specified range is inside of this one<br/>
	 * 		ENCLOSES - the specified range encloses (possibly exactly the same as) this range<br/>
	 */
	public static int intersect(CellRangeAddress crA, CellRangeAddress crB )
	{
		
		int firstRow = crB.getFirstRow();
		int lastRow  = crB.getLastRow();
		int firstCol = crB.getFirstColumn();
		int lastCol  = crB.getLastColumn();
		
		if
		( 
				gt(crA.getFirstRow(),lastRow) || 
				lt(crA.getLastRow(),firstRow) ||
				gt(crA.getFirstColumn(),lastCol) || 
				lt(crA.getLastColumn(),firstCol) 
		)
		{
			return NO_INTERSECTION;
		}
		else if( contains(crA, crB) )
		{
			return INSIDE;
		}
		else if( contains(crB, crA))
		{
			return ENCLOSES;
		}
		else
		{
			return OVERLAP;
		}
			
	}
	
	/**
	 * Do all possible cell merges between cells of the list so that:<br>
	 * 	<li>if a cell range is completely inside of another cell range, it gets removed from the list 
	 * 	<li>if two cells have a shared border, merge them into one bigger cell range
	 * @param cellRanges
	 * @return updated List of cell ranges
	 */
	public static CellRangeAddress[] mergeCellRanges(CellRangeAddress[] cellRanges) {
		if(cellRanges.length < 1) {
			return cellRanges;
		}

        List<CellRangeAddress> lst = new ArrayList<CellRangeAddress>();
        for(CellRangeAddress cr : cellRanges) lst.add(cr);
        List temp = mergeCellRanges(lst);
		return toArray(temp);
	}
	private static List mergeCellRanges(List cellRangeList)
	{

		while(cellRangeList.size() > 1)
		{
			boolean somethingGotMerged = false;
			
			for( int i=0; i<cellRangeList.size(); i++)
			{
				CellRangeAddress range1 = (CellRangeAddress)cellRangeList.get(i);
				for( int j=i+1; j<cellRangeList.size(); j++)
				{
					CellRangeAddress range2 = (CellRangeAddress)cellRangeList.get(j);
					
					CellRangeAddress[] mergeResult = mergeRanges(range1, range2);
					if(mergeResult == null) {
						continue;
					}
					somethingGotMerged = true;
					// overwrite range1 with first result 
					cellRangeList.set(i, mergeResult[0]);
					// remove range2
					cellRangeList.remove(j--);
					// add any extra results beyond the first
					for(int k=1; k<mergeResult.length; k++) {
						j++;
						cellRangeList.add(j, mergeResult[k]);
					}
				}
			}
			if(!somethingGotMerged) {
				break;
			}
		}
		

		return cellRangeList;
	}
	
	/**
	 * @return the new range(s) to replace the supplied ones.  <code>null</code> if no merge is possible
	 */
	private static CellRangeAddress[] mergeRanges(CellRangeAddress range1, CellRangeAddress range2) {
		
		int x = intersect(range1, range2);
		switch(x)
		{
			case CellRangeUtil.NO_INTERSECTION: 
				if(hasExactSharedBorder(range1, range2)) {
					return new CellRangeAddress[] { createEnclosingCellRange(range1, range2), };
				}
				// else - No intersection and no shared border: do nothing 
				return null;
			case CellRangeUtil.OVERLAP:
				return resolveRangeOverlap(range1, range2);
			case CellRangeUtil.INSIDE:
				// Remove range2, since it is completely inside of range1
				return new CellRangeAddress[] { range1, };
			case CellRangeUtil.ENCLOSES:
				// range2 encloses range1, so replace it with the enclosing one
				return new CellRangeAddress[] { range2, };
		}
		throw new RuntimeException("unexpected intersection result (" + x + ")");
	}
	
	// TODO - write junit test for this
	static CellRangeAddress[] resolveRangeOverlap(CellRangeAddress rangeA, CellRangeAddress rangeB) {
		
		if(rangeA.isFullColumnRange()) {
			if(rangeA.isFullRowRange()) {
				// Excel seems to leave these unresolved
				return null;
			}
			return sliceUp(rangeA, rangeB);
		}
		if(rangeA.isFullRowRange()) {
			if(rangeB.isFullColumnRange()) {
				// Excel seems to leave these unresolved
				return null;
			}
			return sliceUp(rangeA, rangeB);
		}
		if(rangeB.isFullColumnRange()) {
			return sliceUp(rangeB, rangeA);
		}
		if(rangeB.isFullRowRange()) {
			return sliceUp(rangeB, rangeA);
		}
		return sliceUp(rangeA, rangeB);
	}

	/**
	 * @param crB never a full row or full column range
	 * @return an array including <b>this</b> <tt>CellRange</tt> and all parts of <tt>range</tt> 
	 * outside of this range  
	 */
	private static CellRangeAddress[] sliceUp(CellRangeAddress crA, CellRangeAddress crB) {
		
		List temp = new ArrayList();
		
		// Chop up range horizontally and vertically
		temp.add(crB);
		if(!crA.isFullColumnRange()) {
			temp = cutHorizontally(crA.getFirstRow(), temp);
			temp = cutHorizontally(crA.getLastRow()+1, temp);
		}
		if(!crA.isFullRowRange()) {
			temp = cutVertically(crA.getFirstColumn(), temp);
			temp = cutVertically(crA.getLastColumn()+1, temp);
		}
		CellRangeAddress[] crParts = toArray(temp);

		// form result array
		temp.clear();
		temp.add(crA);
		
		for (int i = 0; i < crParts.length; i++) {
			CellRangeAddress crPart = crParts[i];
			// only include parts that are not enclosed by this
			if(intersect(crA, crPart) != ENCLOSES) {
				temp.add(crPart);
			}
		}
		return toArray(temp);
	}

	private static List cutHorizontally(int cutRow, List input) {
		
		List result = new ArrayList();
		CellRangeAddress[] crs = toArray(input);
		for (int i = 0; i < crs.length; i++) {
			CellRangeAddress cr = crs[i];
			if(cr.getFirstRow() < cutRow && cutRow < cr.getLastRow()) {
				result.add(new CellRangeAddress(cr.getFirstRow(), cutRow, cr.getFirstColumn(), cr.getLastColumn()));
				result.add(new CellRangeAddress(cutRow+1, cr.getLastRow(), cr.getFirstColumn(), cr.getLastColumn()));
			} else {
				result.add(cr);
			}
		}
		return result;
	}
	private static List cutVertically(int cutColumn, List input) {
		
		List result = new ArrayList();
		CellRangeAddress[] crs = toArray(input);
		for (int i = 0; i < crs.length; i++) {
			CellRangeAddress cr = crs[i];
			if(cr.getFirstColumn() < cutColumn && cutColumn < cr.getLastColumn()) {
				result.add(new CellRangeAddress(cr.getFirstRow(), cr.getLastRow(), cr.getFirstColumn(), cutColumn));
				result.add(new CellRangeAddress(cr.getFirstRow(), cr.getLastRow(), cutColumn+1, cr.getLastColumn()));
			} else {
				result.add(cr);
			}
		}
		return result;
	}


	private static CellRangeAddress[] toArray(List temp) {
		CellRangeAddress[] result = new CellRangeAddress[temp.size()];
		temp.toArray(result);
		return result;
	}



	/**
	 *  Check if the specified range is located inside of this cell range.
	 *  
	 * @param crB
	 * @return true if this cell range contains the argument range inside if it's area
	 */
   public static boolean contains(CellRangeAddress crA, CellRangeAddress crB)
   {
		int firstRow = crB.getFirstRow();
		int lastRow = crB.getLastRow();
		int firstCol = crB.getFirstColumn();
		int lastCol = crB.getLastColumn();
		return le(crA.getFirstRow(), firstRow) && ge(crA.getLastRow(), lastRow)
				&& le(crA.getFirstColumn(), firstCol) && ge(crA.getLastColumn(), lastCol);
	}
   	
   /**
	* Check if the specified cell range has a shared border with the current range.
	* 
	* @return <code>true</code> if the ranges have a complete shared border (i.e.
	* the two ranges together make a simple rectangular region.
	*/
   	public static boolean hasExactSharedBorder(CellRangeAddress crA, CellRangeAddress crB) {
		int oFirstRow = crB.getFirstRow();
		int oLastRow  = crB.getLastRow();
		int oFirstCol = crB.getFirstColumn();
		int oLastCol  = crB.getLastColumn();
		
		if (crA.getFirstRow() > 0 && crA.getFirstRow()-1 == oLastRow || 
			oFirstRow > 0 && oFirstRow-1 == crA.getLastRow()) {
			// ranges have a horizontal border in common
			// make sure columns are identical:
			return crA.getFirstColumn() == oFirstCol && crA.getLastColumn() == oLastCol;
		}

		if (crA.getFirstColumn()>0 && crA.getFirstColumn() - 1 == oLastCol ||
			oFirstCol>0 && crA.getLastColumn() == oFirstCol -1) {
			// ranges have a vertical border in common
			// make sure rows are identical:
			return crA.getFirstRow() == oFirstRow && crA.getLastRow() == oLastRow;
		}
		return false;
   	}
   	
	/**
	 * Create an enclosing CellRange for the two cell ranges.
	 * 
	 * @return enclosing CellRange
	 */
	public static CellRangeAddress createEnclosingCellRange(CellRangeAddress crA, CellRangeAddress crB) {
		if( crB == null) {
			return crA.copy();
		}
		
		return
			new CellRangeAddress(
				lt(crB.getFirstRow(),   crA.getFirstRow())   ?crB.getFirstRow()   :crA.getFirstRow(),
				gt(crB.getLastRow(),    crA.getLastRow())    ?crB.getLastRow()    :crA.getLastRow(),
				lt(crB.getFirstColumn(),crA.getFirstColumn())?crB.getFirstColumn():crA.getFirstColumn(),
				gt(crB.getLastColumn(), crA.getLastColumn()) ?crB.getLastColumn() :crA.getLastColumn()
			);
		
	}
	
	/**
	 * @return true if a < b
	 */
	private static boolean lt(int a, int b)
	{
		return a == -1 ? false : (b == -1 ? true : a < b);
	}
	
	/**
	 * @return true if a <= b
	 */
	private static boolean le(int a, int b)
	{
		return a == b || lt(a,b);
	}
	
	/**
	 * @return true if a > b
	 */
	private static boolean gt(int a, int b)
	{
		return lt(b,a);
	}

	/**
	 * @return true if a >= b
	 */
	private static boolean ge(int a, int b)
	{
		return !lt(a,b);
	}
}
