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

package org.apache.poi.ss.util;


/**
 * Represents a from/to row/col square.  This is a object primitive
 * that can be used to represent row,col - row,col just as one would use String
 * to represent a string of characters.  Its really only useful for HSSF though.
 *
 * @author  Andrew C. Oliver acoliver at apache dot org
 * @deprecated (Aug-2008) use {@link CellRangeAddress}
 */
public class Region implements Comparable<Region> {
	private int   _rowFrom;
	private short _colFrom;
	private int   _rowTo;
	private short _colTo;

	/**
	 * Creates a new instance of Region (0,0 - 0,0)
	 */
	public Region()
	{
	}

	public Region(int rowFrom, short colFrom, int rowTo, short colTo)
	{
		this._rowFrom = rowFrom;
		this._rowTo   = rowTo;
		this._colFrom = colFrom;
		this._colTo   = colTo;
	}

	public Region(String ref) {
		CellReference cellReferenceFrom = new CellReference(ref.substring(0, ref.indexOf(":")));
		CellReference cellReferenceTo = new CellReference(ref.substring(ref.indexOf(":") + 1));
		this._rowFrom = cellReferenceFrom.getRow();
		this._colFrom = cellReferenceFrom.getCol();
		this._rowTo = cellReferenceTo.getRow();
		this._colTo = cellReferenceTo.getCol();
	}


	/**
	 * get the upper left hand corner column number
	 *
	 * @return column number for the upper left hand corner
	 */
	public short getColumnFrom()
	{
		return _colFrom;
	}

	/**
	 * get the upper left hand corner row number
	 *
	 * @return row number for the upper left hand corner
	 */
	public int getRowFrom()
	{
		return _rowFrom;
	}

	/**
	 * get the lower right hand corner column number
	 *
	 * @return column number for the lower right hand corner
	 */

	public short getColumnTo()
	{
		return _colTo;
	}

	/**
	 * get the lower right hand corner row number
	 *
	 * @return row number for the lower right hand corner
	 */
	public int getRowTo()
	{
		return _rowTo;
	}

	/**
	 * set the upper left hand corner column number
	 *
	 * @param colFrom  column number for the upper left hand corner
	 */
	public void setColumnFrom(short colFrom)
	{
		this._colFrom = colFrom;
	}

	/**
	 * set the upper left hand corner row number
	 *
	 * @param rowFrom  row number for the upper left hand corner
	 */
	public void setRowFrom(int rowFrom)
	{
		this._rowFrom = rowFrom;
	}

	/**
	 * set the lower right hand corner column number
	 *
	 * @param colTo  column number for the lower right hand corner
	 */

	public void setColumnTo(short colTo)
	{
		this._colTo = colTo;
	}

	/**
	 * get the lower right hand corner row number
	 *
	 * @param rowTo  row number for the lower right hand corner
	 */
	public void setRowTo(int rowTo)
	{
		this._rowTo = rowTo;
	}


	/**
	 * Answers: "is the row/column inside this range?"
	 *
	 * @return <code>true</code> if the cell is in the range and
	 * <code>false</code> if it is not
	 */
	public boolean contains(int row, short col)
	{
		if ((this._rowFrom <= row) && (this._rowTo >= row)
				&& (this._colFrom <= col) && (this._colTo >= col))
		{

//				System.out.println("Region ("+rowFrom+","+colFrom+","+rowTo+","+
//								   colTo+") does contain "+row+","+col);
			return true;
		}
		return false;
	}

	public boolean equals(Region r)
	{
		return (compareTo(r) == 0);
	}

	/**
	 * Compares that the given region is the same less than or greater than this
	 * region.  If any regional coordiant passed in is less than this regions
	 * coordinants then a positive integer is returned.  Otherwise a negative
	 * integer is returned.
	 *
	 * @param r  region
	 * @see #compareTo(Object)
	 */
	public int compareTo(Region r)
	{
		if ((this.getRowFrom() == r.getRowFrom())
				&& (this.getColumnFrom() == r.getColumnFrom())
				&& (this.getRowTo() == r.getRowTo())
				&& (this.getColumnTo() == r.getColumnTo()))
		{
			return 0;
		}
		if ((this.getRowFrom() < r.getRowFrom())
				|| (this.getColumnFrom() < r.getColumnFrom())
				|| (this.getRowTo() < r.getRowTo())
				|| (this.getColumnTo() < r.getColumnTo()))
		{
			return 1;
		}
		return -1;
	}

	/**
	 * @return the size of the region (number of cells in the area).
	 */
	public int getArea() {
		return (_rowTo - _rowFrom + 1) * (_colTo - _colFrom + 1);
	}


	/**
	 * Convert a List of CellRange objects to an array of regions
	 *
	 * @param cellRanges list of CellRange objects
	 * @return regions
	 */
	public static Region[] convertCellRangesToRegions(CellRangeAddress[] cellRanges) {
		int size = cellRanges.length;
		if(size < 1) {
			return new Region[0];
		}

		Region[] result = new Region[size];

		for (int i = 0; i != size; i++) {
			result[i] = convertToRegion(cellRanges[i]);
		}
		return result;
	}



	private static Region convertToRegion(CellRangeAddress cr) {

		return new Region(cr.getFirstRow(), (short)cr.getFirstColumn(), cr.getLastRow(), (short)cr.getLastColumn());
	}

	public static CellRangeAddress[] convertRegionsToCellRanges(Region[] regions) {
		int size = regions.length;
		if(size < 1) {
			return new CellRangeAddress[0];
		}

		CellRangeAddress[] result = new CellRangeAddress[size];

		for (int i = 0; i != size; i++) {
			result[i] = convertToCellRangeAddress(regions[i]);
		}
		return result;
	}

	public static CellRangeAddress convertToCellRangeAddress(Region r) {
		return new CellRangeAddress(r.getRowFrom(), r.getRowTo(), r.getColumnFrom(), r.getColumnTo());
	}

	/**
	 * @return the string reference for this region
	 */
	public String getRegionRef() {
		CellReference cellRefFrom = new CellReference(_rowFrom, _colFrom);
		CellReference cellRefTo = new CellReference(_rowTo, _colTo);
		String ref = cellRefFrom.formatAsString() + ":" + cellRefTo.formatAsString();
		return ref;
	}
}
