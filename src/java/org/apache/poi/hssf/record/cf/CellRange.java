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
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.util.Region;

/**
 * 
 * @author Dmitriy Kumshayev
 */
public final class CellRange
{
	/** max 65536 rows in BIFF8 */
	private static final int LAST_ROW_INDEX = 0x00FFFF; 
	/** max 256 columns in BIFF8 */
	private static final int LAST_COLUMN_INDEX = 0x00FF;

	private static final Region[] EMPTY_REGION_ARRAY = { };
	
	private int _firstRow;
	private int _lastRow;
	private int _firstColumn;
	private int _lastColumn;
	
	/**
	 * 
	 * @param firstRow
	 * @param lastRow pass <tt>-1</tt> for full column ranges
	 * @param firstColumn
	 * @param lastColumn  pass <tt>-1</tt> for full row ranges
	 */
	public CellRange(int firstRow, int lastRow, int firstColumn, int lastColumn)
	{
		if(!isValid(firstRow, lastRow, firstColumn, lastColumn)) {
			throw new IllegalArgumentException("invalid cell range (" + firstRow + ", " + lastRow 
					+ ", " + firstColumn + ", " + lastColumn + ")");
		}
		_firstRow = firstRow;
		_lastRow = convertM1ToMax(lastRow, LAST_ROW_INDEX);
		_firstColumn = firstColumn;
		_lastColumn = convertM1ToMax(lastColumn, LAST_COLUMN_INDEX);
	}
	
	/** 
	 * Range arithmetic is easier when using a large positive number for 'max row or column' 
	 * instead of <tt>-1</tt>. 
	 */
	private static int convertM1ToMax(int lastIx, int maxIndex) {
		if(lastIx < 0) {
			return maxIndex;
		}
		return lastIx;
	}

	public boolean isFullColumnRange() {
		return _firstRow == 0 && _lastRow == LAST_ROW_INDEX;
	}
	public boolean isFullRowRange() {
		return _firstColumn == 0 && _lastColumn == LAST_COLUMN_INDEX;
	}
	
	private static CellRange createFromRegion(org.apache.poi.ss.util.Region r) {
		return new CellRange(r.getRowFrom(), r.getRowTo(), r.getColumnFrom(), r.getColumnTo());
	}

	private static boolean isValid(int firstRow, int lastRow, int firstColumn, int lastColumn)
	{
		if(lastRow < 0 || lastRow > LAST_ROW_INDEX) {
			return false;
		}
		if(firstRow < 0 || firstRow > LAST_ROW_INDEX) {
			return false;
		}
		
		if(lastColumn < 0 || lastColumn > LAST_COLUMN_INDEX) {
			return false;
		}
		if(firstColumn < 0 || firstColumn > LAST_COLUMN_INDEX) {
			return false;
		}
		return true;
	}
	
	public int getFirstRow()
	{
		return _firstRow;
	}
	public int getLastRow()
	{
		return _lastRow;
	}
	public int getFirstColumn()
	{
		return _firstColumn;
	}
	public int getLastColumn()
	{
		return _lastColumn;
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
	 * @param another - the specified range
	 * @return code which reflects how the specified range is related to this range.<br/>
	 * Possible return codes are:	
	 * 		NO_INTERSECTION - the specified range is outside of this range;<br/> 
	 * 		OVERLAP - both ranges partially overlap;<br/>
	 * 		INSIDE - the specified range is inside of this one<br/>
	 * 		ENCLOSES - the specified range encloses (possibly exactly the same as) this range<br/>
	 */
	public int intersect(CellRange another )
	{
		
		int firstRow = another.getFirstRow();
		int lastRow  = another.getLastRow();
		int firstCol = another.getFirstColumn();
		int lastCol  = another.getLastColumn();
		
		if
		( 
				gt(getFirstRow(),lastRow) || 
				lt(getLastRow(),firstRow) ||
				gt(getFirstColumn(),lastCol) || 
				lt(getLastColumn(),firstCol) 
		)
		{
			return NO_INTERSECTION;
		}
		else if( contains(another) )
		{
			return INSIDE;
		}
		else if( another.contains(this))
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
	 * @param cellRangeList
	 * @return updated List of cell ranges
	 */
	public static CellRange[] mergeCellRanges(CellRange[] cellRanges) {
		if(cellRanges.length < 1) {
			return cellRanges;
		}
		List temp = mergeCellRanges(Arrays.asList(cellRanges));
		return toArray(temp);
	}
	private static List mergeCellRanges(List cellRangeList)
	{

		while(cellRangeList.size() > 1)
		{
			boolean somethingGotMerged = false;
			
			for( int i=0; i<cellRangeList.size(); i++)
			{
				CellRange range1 = (CellRange)cellRangeList.get(i);
				for( int j=i+1; j<cellRangeList.size(); j++)
				{
					CellRange range2 = (CellRange)cellRangeList.get(j);
					
					CellRange[] mergeResult = mergeRanges(range1, range2);
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
	private static CellRange[] mergeRanges(CellRange range1, CellRange range2) {
		
		int x = range1.intersect(range2);
		switch(x)
		{
			case CellRange.NO_INTERSECTION: 
				if( range1.hasExactSharedBorder(range2))
				{
					return new CellRange[] { range1.createEnclosingCellRange(range2), };
				}
				// else - No intersection and no shared border: do nothing 
				return null;
			case CellRange.OVERLAP:
				return resolveRangeOverlap(range1, range2);
			case CellRange.INSIDE:
				// Remove range2, since it is completely inside of range1
				return new CellRange[] { range1, };
			case CellRange.ENCLOSES:
				// range2 encloses range1, so replace it with the enclosing one
				return new CellRange[] { range2, };
		}
		throw new RuntimeException("unexpected intersection result (" + x + ")");
	}
	
	// TODO - write junit test for this
	static CellRange[] resolveRangeOverlap(CellRange rangeA, CellRange rangeB) {
		
		if(rangeA.isFullColumnRange()) {
			if(rangeB.isFullRowRange()) {
				// Excel seems to leave these unresolved
				return null;
			}
			return rangeA.sliceUp(rangeB);
		}
		if(rangeA.isFullRowRange()) {
			if(rangeB.isFullColumnRange()) {
				// Excel seems to leave these unresolved
				return null;
			}
			return rangeA.sliceUp(rangeB);
		}
		if(rangeB.isFullColumnRange()) {
			return rangeB.sliceUp(rangeA);
		}
		if(rangeB.isFullRowRange()) {
			return rangeB.sliceUp(rangeA);
		}
		return rangeA.sliceUp(rangeB);
	}

	/**
	 * @param range never a full row or full column range
	 * @return an array including <b>this</b> <tt>CellRange</tt> and all parts of <tt>range</tt> 
	 * outside of this range  
	 */
	private CellRange[] sliceUp(CellRange range) {
		
		List temp = new ArrayList();
		
		// Chop up range horizontally and vertically
		temp.add(range);
		if(!isFullColumnRange()) {
			temp = cutHorizontally(_firstRow, temp);
			temp = cutHorizontally(_lastRow+1, temp);
		}
		if(!isFullRowRange()) {
			temp = cutVertically(_firstColumn, temp);
			temp = cutVertically(_lastColumn+1, temp);
		}
		CellRange[] crParts = toArray(temp);

		// form result array
		temp.clear();
		temp.add(this);
		
		for (int i = 0; i < crParts.length; i++) {
			CellRange crPart = crParts[i];
			// only include parts that are not enclosed by this
			if(intersect(crPart) != ENCLOSES) {
				temp.add(crPart);
			}
		}
		return toArray(temp);
	}

	private static List cutHorizontally(int cutRow, List input) {
		
		List result = new ArrayList();
		CellRange[] crs = toArray(input);
		for (int i = 0; i < crs.length; i++) {
			CellRange cr = crs[i];
			if(cr._firstRow < cutRow && cutRow < cr._lastRow) {
				result.add(new CellRange(cr._firstRow, cutRow, cr._firstColumn, cr._lastColumn));
				result.add(new CellRange(cutRow+1, cr._lastRow, cr._firstColumn, cr._lastColumn));
			} else {
				result.add(cr);
			}
		}
		return result;
	}
	private static List cutVertically(int cutColumn, List input) {
		
		List result = new ArrayList();
		CellRange[] crs = toArray(input);
		for (int i = 0; i < crs.length; i++) {
			CellRange cr = crs[i];
			if(cr._firstColumn < cutColumn && cutColumn < cr._lastColumn) {
				result.add(new CellRange(cr._firstRow, cr._lastRow, cr._firstColumn, cutColumn));
				result.add(new CellRange(cr._firstRow, cr._lastRow, cutColumn+1, cr._lastColumn));
			} else {
				result.add(cr);
			}
		}
		return result;
	}


	private static CellRange[] toArray(List temp) {
		CellRange[] result = new CellRange[temp.size()];
		temp.toArray(result);
		return result;
	}

	/**
	 * Convert array of regions to a List of CellRange objects
	 *  
	 * @param regions
	 * @return List of CellRange objects
	 */
	public static CellRange[] convertRegionsToCellRanges(org.apache.poi.ss.util.Region[] regions)
	{
		CellRange[] result = new CellRange[regions.length];
		for( int i=0; i<regions.length; i++)
		{
			result[i] = createFromRegion(regions[i]);
		}
		return result;
	}
	
	/**
	 * Convert a List of CellRange objects to an array of regions 
	 *  
	 * @param List of CellRange objects
	 * @return regions
	 */
	public static Region[] convertCellRangesToRegions(CellRange[] cellRanges)
	{
		int size = cellRanges.length;
		if(size < 1) {
			return EMPTY_REGION_ARRAY;
		}
		
		Region[] result = new Region[size];

		for (int i = 0; i != size; i++)
		{
			result[i] = cellRanges[i].convertToRegion();
		}
		return result;
	}


		
	private Region convertToRegion() {
		
		return new Region(_firstRow, (short)_firstColumn, _lastRow, (short)_lastColumn);
	}


	/**
	 *  Check if the specified range is located inside of this cell range.
	 *  
	 * @param range
	 * @return true if this cell range contains the argument range inside if it's area
	 */
   public boolean contains(CellRange range)
   {
		int firstRow = range.getFirstRow();
		int lastRow = range.getLastRow();
		int firstCol = range.getFirstColumn();
		int lastCol = range.getLastColumn();
		return le(getFirstRow(), firstRow) && ge(getLastRow(), lastRow)
				&& le(getFirstColumn(), firstCol) && ge(getLastColumn(), lastCol);
	}
   
  	public boolean contains(int row, short column)
	{
		return le(getFirstRow(), row) && ge(getLastRow(), row)
				&& le(getFirstColumn(), column) && ge(getLastColumn(), column);
	}
   	
   /**
	* Check if the specified cell range has a shared border with the current range.
	* 
	* @return <code>true</code> if the ranges have a complete shared border (i.e.
	* the two ranges together make a simple rectangular region.
	*/
   	public boolean hasExactSharedBorder(CellRange range)
   	{
		int oFirstRow = range._firstRow;
		int oLastRow  = range._lastRow;
		int oFirstCol = range._firstColumn;
		int oLastCol  = range._lastColumn;
		
		if (_firstRow > 0 && _firstRow-1 == oLastRow || 
			oFirstRow > 0 && oFirstRow-1 == _lastRow) {
			// ranges have a horizontal border in common
			// make sure columns are identical:
			return _firstColumn == oFirstCol && _lastColumn == oLastCol;
		}

		if (_firstColumn>0 && _firstColumn - 1 == oLastCol ||
			oFirstCol>0 && _lastColumn == oFirstCol -1) {
			// ranges have a vertical border in common
			// make sure rows are identical:
			return _firstRow == oFirstRow && _lastRow == oLastRow;
		}
		return false;
   	}
   	
	/**
	 * Create an enclosing CellRange for the two cell ranges.
	 * 
	 * @return enclosing CellRange
	 */
	public CellRange createEnclosingCellRange(CellRange range)
	{
		if( range == null)
		{
			return cloneCellRange();
		}
		else
		{
			CellRange cellRange = 
				new CellRange(
					lt(range.getFirstRow(),getFirstRow())?range.getFirstRow():getFirstRow(),
					gt(range.getLastRow(),getLastRow())?range.getLastRow():getLastRow(),
					lt(range.getFirstColumn(),getFirstColumn())?range.getFirstColumn():getFirstColumn(),
					gt(range.getLastColumn(),getLastColumn())?range.getLastColumn():getLastColumn()
				);
			return cellRange;
		}
	}
	
	public CellRange cloneCellRange()
	{
		return new CellRange(getFirstRow(),getLastRow(),getFirstColumn(),getLastColumn());
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
	
	public String toString()
	{
		return "("+getFirstRow()+","+getLastRow()+","+getFirstColumn()+","+getLastColumn()+")";
	}
	
}
