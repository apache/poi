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

import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;

/**
 * <p>This class is a container for POI usermodel row=0 column=0 cell references.
 * It is barely a container for these two coordinates. The implementation
 * of the Comparable interface sorts by "natural" order top left to bottom right.</p>
 * 
 * <p>Use <tt>CellAddress</tt> when you want to refer to the location of a cell in a sheet
 * when the concept of relative/absolute does not apply (such as the anchor location 
 * of a cell comment). Use {@link CellReference} when the concept of
 * relative/absolute does apply (such as a cell reference in a formula).
 * <tt>CellAddress</tt>es do not have a concept of "sheet", while <tt>CellReference</tt>s do.</p>
 */
public class CellAddress implements Comparable<CellAddress> {
    /** A constant for references to the first cell in a sheet. */
    public static final CellAddress A1 = new CellAddress(0, 0);
    
    private final int _row;
    private final int _col;
    
    /**
     * Create a new CellAddress object.
     *
     * @param row Row index (first row is 0)
     * @param column Column index (first column is 0)
     */
    public CellAddress(int row, int column) {
        super();
        this._row = row;
        this._col = column;
    }
    
    /**
     * Create a new CellAddress object.
     *
     * @param address a cell address in A1 format. Address may not contain sheet name or dollar signs.
     * (that is, address is not a cell reference. Use {@link #CellAddress(CellReference)} instead if
     * starting with a cell reference.)
     */
    public CellAddress(String address) {
        int length = address.length();

        int loc = 0;
        // step over column name chars until first digit for row number.
        for (; loc < length; loc++) {
            char ch = address.charAt(loc);
            if (Character.isDigit(ch)) {
                break;
            }
        }

        String sCol = address.substring(0,loc).toUpperCase(Locale.ROOT);
        String sRow = address.substring(loc);

        // FIXME: breaks if address contains a sheet name or dollar signs from an absolute CellReference
        this._row = Integer.parseInt(sRow)-1;
        this._col = CellReference.convertColStringToIndex(sCol);
    }
    
    /**
     * Create a new CellAddress object.
     *
     * @param reference a reference to a cell
     */
    public CellAddress(CellReference reference) {
        this(reference.getRow(), reference.getCol());
    }
    
    /**
     * Create a new CellAddress object
     * 
     * @param address a CellAddress
     */
    public CellAddress(CellAddress address) {
        this(address.getRow(), address.getColumn());
    }
    
    /**
     * Create a new CellAddress object.
     *
     * @param cell the Cell to get the location of
     */
    public CellAddress(Cell cell) {
        this(cell.getRowIndex(), cell.getColumnIndex());
    }
    
    /**
     * Get the cell address row
     *
     * @return row
     */
    public int getRow() {
        return _row;
    }

    /**
     * Get the cell address column
     *
     * @return column
     */
    public int getColumn() {
        return _col;
    }

    /**
     * Compare this CellAddress using the "natural" row-major, column-minor ordering.
     * That is, top-left to bottom-right ordering.
     * 
     * @param other
     * @return <ul>
     * <li>-1 if this CellAddress is before (above/left) of other</li>
     * <li>0 if addresses are the same</li>
     * <li>1 if this CellAddress is after (below/right) of other</li>
     * </ul>
     */
    @Override
    public int compareTo(CellAddress other) {
        int r = this._row-other._row;
        if (r!=0) return r;

        r = this._col-other._col;
        if (r!=0) return r;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if(!(o instanceof CellAddress)) {
            return false;
        }
        
        CellAddress other = (CellAddress) o;
        return _row == other._row &&
               _col == other._col;
    }

    @Override
    public int hashCode() {
        return this._row + this._col<<16;
    }

    @Override
    public String toString() {
        return formatAsString();
    }
    
    /**
     * Same as {@link #toString()}
     * @return A1-style cell address string representation
     */
    public String formatAsString() {
        return CellReference.convertNumToColString(this._col)+(this._row+1);
    }
}
