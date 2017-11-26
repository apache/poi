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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;


/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p>
 *
 * Common superclass of 8-bit and 16-bit versions
 */
public abstract class CellRangeAddressBase implements Iterable<CellAddress> {

    /**
     * Indicates a cell or range is in the given relative position in a range.
     * More than one of these may apply at once.
     */
    public static enum CellPosition {
        /** range starting rows are equal */
        TOP,
        /** range ending rows are equal */
        BOTTOM,
        /** range starting columns are equal */
        LEFT,
        /** range ending columns are equal */
        RIGHT,
        /** a cell or range is completely inside another range, without touching any edges (a cell in this position can't be in any others) */
        INSIDE,
        ;
    }
	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;

	protected CellRangeAddressBase(int firstRow, int lastRow, int firstCol, int lastCol) {
		_firstRow = firstRow;
		_lastRow = lastRow;
		_firstCol = firstCol;
		_lastCol = lastCol;
	}

	/**
	 * Validate the range limits against the supplied version of Excel
	 *
	 * @param ssVersion the version of Excel to validate against
	 * @throws IllegalArgumentException if the range limits are outside of the allowed range
	 */
	public void validate(SpreadsheetVersion ssVersion) {
		validateRow(_firstRow, ssVersion);
		validateRow(_lastRow, ssVersion);
		validateColumn(_firstCol, ssVersion);
		validateColumn(_lastCol, ssVersion);
	}
	/**
	 * Runs a bounds check for row numbers
	 * @param row
	 */
	private static void validateRow(int row, SpreadsheetVersion ssVersion) {
		int maxrow = ssVersion.getLastRowIndex();
		if (row > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
		if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
	}

	/**
	 * Runs a bounds check for column numbers
	 * @param column
	 */
	private static void validateColumn(int column, SpreadsheetVersion ssVersion) {
		int maxcol = ssVersion.getLastColumnIndex();
		if (column > maxcol) throw new IllegalArgumentException("Maximum column number is " + maxcol);
		if (column < 0)	throw new IllegalArgumentException("Minimum column number is 0");
	}


	//TODO use the correct SpreadsheetVersion
	public final boolean isFullColumnRange() {
		return (_firstRow == 0 && _lastRow == SpreadsheetVersion.EXCEL97.getLastRowIndex())
		  || (_firstRow == -1 && _lastRow == -1);
	}
	//TODO use the correct SpreadsheetVersion
	public final boolean isFullRowRange() {
		return (_firstCol == 0 && _lastCol == SpreadsheetVersion.EXCEL97.getLastColumnIndex())
		  || (_firstCol == -1 && _lastCol == -1);
	}

	/**
	 * @return column number for the upper left hand corner
	 */
	public final int getFirstColumn() {
		return _firstCol;
	}

	/**
	 * @return row number for the upper left hand corner
	 */
	public final int getFirstRow() {
		return _firstRow;
	}

	/**
	 * @return column number for the lower right hand corner
	 */
	public final int getLastColumn() {
		return _lastCol;
	}

	/**
	 * @return row number for the lower right hand corner
	 */
	public final int getLastRow() {
		return _lastRow;
	}

	/**
	 * Determines if the given coordinates lie within the bounds 
	 * of this range.
	 *
	 * @param rowInd The row, 0-based.
	 * @param colInd The column, 0-based.
	 * @return True if the coordinates lie within the bounds, false otherwise.
	 * @see #intersects(CellRangeAddressBase) for checking if two ranges overlap
	 */
	public boolean isInRange(int rowInd, int colInd) {
		return _firstRow <= rowInd && rowInd <= _lastRow && //containsRow
				_firstCol <= colInd && colInd <= _lastCol; //containsColumn
	}
	
    /**
     * Determines if the given {@link CellReference} lies within the bounds 
     * of this range.  
     * <p>NOTE: It is up to the caller to ensure the reference is 
     * for the correct sheet, since this instance doesn't have a sheet reference.
     *
     * @param ref the CellReference to check
     * @return True if the reference lies within the bounds, false otherwise.
     * @see #intersects(CellRangeAddressBase) for checking if two ranges overlap
     */
	public boolean isInRange(CellReference ref) {
	    return isInRange(ref.getRow(), ref.getCol());
	}
	
    /**
     * Determines if the given {@link CellAddress} lies within the bounds 
     * of this range.  
     * <p>NOTE: It is up to the caller to ensure the reference is 
     * for the correct sheet, since this instance doesn't have a sheet reference.
     *
     * @param ref the CellAddress to check
     * @return True if the reference lies within the bounds, false otherwise.
     * @see #intersects(CellRangeAddressBase) for checking if two ranges overlap
     */
    public boolean isInRange(CellAddress ref) {
        return isInRange(ref.getRow(), ref.getColumn());
    }
	
	/**
	 * Determines if the given {@link Cell} lies within the bounds 
	 * of this range.  
	 * <p>NOTE: It is up to the caller to ensure the reference is 
	 * for the correct sheet, since this instance doesn't have a sheet reference.
	 *
	 * @param cell the Cell to check
	 * @return True if the cell lies within the bounds, false otherwise.
	 * @see #intersects(CellRangeAddressBase) for checking if two ranges overlap
	 */
	public boolean isInRange(Cell cell) {
	    return isInRange(cell.getRowIndex(), cell.getColumnIndex());
	}
	
	/**
	 * Check if the row is in the specified cell range
	 *
	 * @param rowInd the row to check
	 * @return true if the range contains the row [rowInd]
	 */
	public boolean containsRow(int rowInd) {
		return _firstRow <= rowInd && rowInd <= _lastRow;
	}
	
	/**
	 * Check if the column is in the specified cell range
	 *
	 * @param colInd the column to check
	 * @return true if the range contains the column [colInd]
	 */
	public boolean containsColumn(int colInd) {
		return _firstCol <= colInd && colInd <= _lastCol;
	}
	
	/**
	 * Determines whether or not this CellRangeAddress and the specified CellRangeAddress intersect.
	 *
	 * @param other a candidate cell range address to check for intersection with this range
	 * @return returns true if this range and other range have at least 1 cell in common
	 * @see #isInRange(int, int) for checking if a single cell intersects
	 */
	public boolean intersects(CellRangeAddressBase other) {
		return this._firstRow <= other._lastRow &&
				this._firstCol <= other._lastCol &&
				other._firstRow <= this._lastRow &&
				other._firstCol <= this._lastCol;
	}
	
	/**
	 * Useful for logic like table/range styling, where some elements apply based on relative position in a range.
	 * @param rowInd
	 * @param colInd
	 * @return set of {@link CellPosition}s occupied by the given coordinates.  Empty if the coordinates are not in the range, never null.
	 * @since 3.17 beta 1
	 */
	public Set<CellPosition> getPosition(int rowInd, int colInd) {
	    Set<CellPosition> positions = EnumSet.noneOf(CellPosition.class);
	    if (rowInd > getFirstRow() && rowInd < getLastRow() && colInd > getFirstColumn() && colInd < getLastColumn()) {
	        positions.add(CellPosition.INSIDE);
	        return positions; // entirely inside, matches no boundaries
	    }
	    // check edges
	    if (rowInd == getFirstRow()) positions.add(CellPosition.TOP);
	    if (rowInd == getLastRow()) positions.add(CellPosition.BOTTOM);
	    if (colInd == getFirstColumn()) positions.add(CellPosition.LEFT);
	    if (colInd == getLastColumn()) positions.add(CellPosition.RIGHT);
	    
	    return positions;
	}
	
	/**
	 * @param firstCol column number for the upper left hand corner
	 */
	public final void setFirstColumn(int firstCol) {
		_firstCol = firstCol;
	}

	/**
	 * @param firstRow row number for the upper left hand corner
	 */
	public final void setFirstRow(int firstRow) {
		_firstRow = firstRow;
	}

	/**
	 * @param lastCol column number for the lower right hand corner
	 */
	public final void setLastColumn(int lastCol) {
		_lastCol = lastCol;
	}

	/**
	 * @param lastRow row number for the lower right hand corner
	 */
	public final void setLastRow(int lastRow) {
		_lastRow = lastRow;
	}
	/**
	 * @return the size of the range (number of cells in the area).
	 */
	public int getNumberOfCells() {
		return (_lastRow - _firstRow + 1) * (_lastCol - _firstCol + 1);
	}

	/**
	 * Returns an iterator over the CellAddresses in this cell range in row-major order.
	 * @since POI 4.0.0
	 */
	@Override
	public Iterator<CellAddress> iterator() {
		return new RowMajorCellAddressIterator(this);
	}
	
	/**
	 *  Iterates over the cell addresses in a cell range in row major order
	 *  
	 *  The iterator is unaffected by changes to the CellRangeAddressBase instance
	 *  after the iterator is created.
	 */
	private static class RowMajorCellAddressIterator implements Iterator<CellAddress> {
		private final int firstRow, firstCol, lastRow, lastCol;
		private int r, c;
		
		public RowMajorCellAddressIterator(CellRangeAddressBase ref) {
			r = firstRow = ref.getFirstRow();
			c = firstCol = ref.getFirstColumn();
			lastRow = ref.getLastRow();
			lastCol = ref.getLastColumn();
			
			// whole row and whole column ranges currently not supported
			if (firstRow < 0) throw new IllegalStateException("First row cannot be negative.");
			if (firstCol < 0) throw new IllegalStateException("First column cannot be negative.");
			
			// avoid infinite iteration
			if (firstRow > lastRow) throw new IllegalStateException("First row cannot be greater than last row.");
			if (firstCol > lastCol) throw new IllegalStateException("First column cannot be greater than last column.");
		}
		
		@Override
		public boolean hasNext() {
			return r <= lastRow && c <= lastCol;
		}
		
		@Override
		public CellAddress next() {
			if (hasNext()) {
				final CellAddress addr = new CellAddress(r, c);
				// row major order
				if (c < lastCol) {
					c++;
				}
				else { //c >= lastCol, end of row reached
					c = firstCol; //CR
					r++;		  //LF
				}
				return addr;
			}
			throw new NoSuchElementException();
		}
	}

	@Override
	public final String toString() {
		CellAddress crA = new CellAddress(_firstRow, _firstCol);
		CellAddress crB = new CellAddress(_lastRow, _lastCol);
		return getClass().getName() + " [" + crA.formatAsString() + ":" + crB.formatAsString() +"]";
	}
	
	// In case _firstRow > _lastRow or _firstCol > _lastCol
	protected int getMinRow() {
		return Math.min(_firstRow, _lastRow);
	}
	protected int getMaxRow() {
		return Math.max(_firstRow, _lastRow);
	}
	protected int getMinColumn() {
		return Math.min(_firstCol, _lastCol);
	}
	protected int getMaxColumn() {
		return Math.max(_firstCol, _lastCol);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof CellRangeAddressBase) {
			CellRangeAddressBase o = (CellRangeAddressBase) other;
			return ((getMinRow() == o.getMinRow()) &&
					(getMaxRow() == o.getMaxRow()) &&
					(getMinColumn() == o.getMinColumn()) &&
					(getMaxColumn() == o.getMaxColumn()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
        return (getMinColumn() +
        (getMaxColumn() << 8) +
        (getMinRow() << 16) +
        (getMaxRow() << 24));
	}
}
