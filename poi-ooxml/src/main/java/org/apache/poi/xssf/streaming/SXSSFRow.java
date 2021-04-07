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

package org.apache.poi.xssf.streaming;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Internal;
import org.apache.poi.util.NotImplemented;

/**
 * Streaming version of XSSFRow implementing the "BigGridDemo" strategy.
 */
public class SXSSFRow implements Row, Comparable<SXSSFRow>
{
    private static final Boolean UNDEFINED = null;

    private final SXSSFSheet _sheet; // parent sheet
    private final SortedMap<Integer, SXSSFCell> _cells = new TreeMap<>();
    private short _style = -1; // index of cell style in style table
    private short _height = -1; // row height in twips (1/20 point)
    private boolean _zHeight; // row zero-height (this is somehow different than being hidden)
    private int _outlineLevel;   // Outlining level of the row, when outlining is on
    // use Boolean to have a tri-state for on/off/undefined
    private Boolean _hidden = UNDEFINED;
    private Boolean _collapsed = UNDEFINED;
    private int _rowNum;

    public SXSSFRow(SXSSFSheet sheet)
    {
        _sheet=sheet;
    }

    public Iterator<Cell> allCellsIterator()
    {
        return new CellIterator();
    }

    public boolean hasCustomHeight()
    {
        return _height!=-1;
    }

    @Override
    public int getOutlineLevel(){
        return _outlineLevel;
    }
    void setOutlineLevel(int level){
        _outlineLevel = level;
    }

    /**
     * get row hidden state: Hidden (true), Unhidden (false), Undefined (null)
     *
     * @return row hidden state
     */
    public Boolean getHidden() {
        return _hidden;
    }

    /**
     * set row hidden state: Hidden (true), Unhidden (false), Undefined (null)
     *
     * @param hidden row hidden state
     */
    public void setHidden(Boolean hidden) {
        this._hidden = hidden;
    }

    public Boolean getCollapsed() {
        return _collapsed;
    }

    public void setCollapsed(Boolean collapsed) {
        this._collapsed = collapsed;
    }
//begin of interface implementation
    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Cell> iterator()
    {
        return new FilledCellIterator();
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link CellType#BLANK}. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than the maximum number of supported columns
     * (255 for *.xls, 1048576 for *.xlsx)
     */
    @Override
    public SXSSFCell createCell(int column)
    {
        return createCell(column, CellType.BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link CellType#BLANK}. The type can be changed
     * either through calling setCellValue or setCellType.
     *
     * @param column - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than a maximum number of supported columns
     * (255 for *.xls, 1048576 for *.xlsx)
     */
    @Override
    public SXSSFCell createCell(int column, CellType type)
    {
        checkBounds(column);
        SXSSFCell cell = new SXSSFCell(this, type);
        _cells.put(column, cell);
        return cell;
    }

    /**
     * @throws RuntimeException if the bounds are exceeded.
     */
    private static void checkBounds(int cellIndex) {
        SpreadsheetVersion v = SpreadsheetVersion.EXCEL2007;
        int maxcol = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();
        if (cellIndex < 0 || cellIndex > maxcol) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex
                    + ").  Allowable column range for " + v.name() + " is (0.."
                    + maxcol + ") or ('A'..'" + v.getLastColumnName() + "')");
        }
    }

    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    @Override
    public void removeCell(Cell cell)
    {
        int index = getCellIndex((SXSSFCell) cell);
        _cells.remove(index);
    }

    /**
     * Return the column number of a cell if it is in this row
     * Otherwise return -1
     *
     * @param cell the cell to get the index of
     * @return cell column index if it is in this row, -1 otherwise
     */
    /*package*/ int getCellIndex(SXSSFCell cell)
    {
        for (Entry<Integer, SXSSFCell> entry : _cells.entrySet()) {
            if (entry.getValue()==cell) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Set the row number of this row.
     *
     * @param rowNum  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0
     */
    @Override
    public void setRowNum(int rowNum)
    {
        this._rowNum = rowNum;
        _sheet.changeRowNum(this, rowNum);
    }

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    @Override
    public int getRowNum()
    {
        return _rowNum;
    }

    /**
     * Get the cell representing a given column (logical cell) 0-based.
     * If cell is missing or blank, uses the workbook's MissingCellPolicy
     * to determine the return value.
     *
     * @param cellnum  0 based column number
     * @return Cell representing that column or null if undefined.
     * @see #getCell(int, org.apache.poi.ss.usermodel.Row.MissingCellPolicy)
     * @throws RuntimeException if cellnum is out of bounds
     */
    @Override
    public SXSSFCell getCell(int cellnum) {
        MissingCellPolicy policy = _sheet.getWorkbook().getMissingCellPolicy();
        return getCell(cellnum, policy);
    }

    /**
     * Returns the cell at the given (0 based) index, with the specified {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy}
     *
     * @return the cell at the given (0 based) index
     * @throws IllegalArgumentException if cellnum < 0 or the specified MissingCellPolicy is invalid
     */
    @Override
    public SXSSFCell getCell(int cellnum, MissingCellPolicy policy)
    {
        checkBounds(cellnum);

        final SXSSFCell cell = _cells.get(cellnum);
        switch (policy) {
            case RETURN_NULL_AND_BLANK:
                return cell;
            case RETURN_BLANK_AS_NULL:
                boolean isBlank = (cell != null && cell.getCellType() == CellType.BLANK);
                return (isBlank) ? null : cell;
            case CREATE_NULL_AS_BLANK:
                return (cell == null) ? createCell(cellnum, CellType.BLANK) : cell;
            default:
                throw new IllegalArgumentException("Illegal policy " + policy);
        }
    }

    /**
     * Get the number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    @Override
    public short getFirstCellNum()
    {
        try {
            return _cells.firstKey().shortValue();
        } catch (final NoSuchElementException e) {
            return -1;
        }
    }

    /**
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     *   Cell cell = row.getCell(colIx);
     *   if(cell == null) {
     *     continue;
     *   }
     *   //... do something with cell
     * }
     * </pre>
     *
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>,
     *   or -1 if the row does not contain any cells.
     */
    @Override
    public short getLastCellNum()
    {
        return _cells.isEmpty() ? -1 : (short)(_cells.lastKey() + 1);
    }

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    @Override
    public int getPhysicalNumberOfCells()
    {
        return _cells.size();
    }

    /**
     * Set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     *
     * @param height  rowheight or 0xff for undefined (use sheet default)
     */
    @Override
    public void setHeight(short height)
    {
        _height=height;
    }

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param zHeight  height is zero or not.
     */
    @Override
    public void setZeroHeight(boolean zHeight)
    {
        _zHeight=zHeight;
    }

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - zHeight height is zero or not.
     */
    @Override
    public boolean getZeroHeight()
    {
        return _zHeight;
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    @Override
    public void setHeightInPoints(float height)
    {
        if(height==-1) {
            _height=-1;
        } else {
            _height=(short)(height*20);
        }
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link Sheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
    @Override
    public short getHeight()
    {
        return (short)(_height==-1?getSheet().getDefaultRowHeightInPoints()*20:_height);
    }

    /**
     * Returns row height measured in point size. If the height is not set, the default worksheet value is returned,
     * See {@link Sheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in point size
     * @see Sheet#getDefaultRowHeightInPoints()
     */
    @Override
    public float getHeightInPoints()
    {
        return (float)(_height==-1?getSheet().getDefaultRowHeightInPoints():_height/20.0);
    }

    /**
     * Is this row formatted? Most aren't, but some rows
     *  do have whole-row styles. For those that do, you
     *  can get the formatting from {@link #getRowStyle()}
     */
    @Override
    public boolean isFormatted() {
        return _style > -1;
    }
    /**
     * Returns the whole-row cell style. Most rows won't
     *  have one of these, so will return null. Call
     *  {@link #isFormatted()} to check first.
     */
    @Override
    public CellStyle getRowStyle() {
        if(!isFormatted()) {
            return null;
        }

        return getSheet().getWorkbook().getCellStyleAt(_style);
    }

    @Internal
        /*package*/ int getRowStyleIndex() {
        return _style;
    }

    /**
     * Applies a whole-row cell styling to the row.
     * The row style can be cleared by passing in <code>null</code>.
     */
    @Override
    public void setRowStyle(CellStyle style) {
        if(style == null) {
            _style = -1;
        } else {
            _style = style.getIndex();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Cell> cellIterator()
    {
        return iterator();
    }

    /**
     * Returns the Sheet this row belongs to
     *
     * @return the Sheet that owns this row
     */
    @Override
    public SXSSFSheet getSheet()
    {
        return _sheet;
    }

//end of interface implementation

    void setRowNumWithoutUpdatingSheet(int rowNum)
    {
        this._rowNum = rowNum;
    }

    /**
     * Create an iterator over the cells from [0, getLastCellNum()).
     * Includes blank cells, excludes empty cells
     *
     * Returns an iterator over all filled cells (created via Row.createCell())
     * Throws ConcurrentModificationException if cells are added, moved, or
     * removed after the iterator is created.
     */
    public class FilledCellIterator implements Iterator<Cell>
    {
        private final Iterator<SXSSFCell> iter = _cells.values().iterator();

        @Override
        public boolean hasNext()
        {
            return iter.hasNext();
        }
        @Override
        public Cell next() throws NoSuchElementException
        {
            return iter.next();
        }
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * returns all cells including empty cells (<code>null</code> values are returned
     * for empty cells).
     * This method is not synchronized. This iterator should not be used after
     * cells are added, moved, or removed, though a ConcurrentModificationException
     * is NOT thrown.
     */
    public class CellIterator implements Iterator<Cell>
    {
        final int maxColumn = getLastCellNum(); //last column PLUS ONE
        int pos;

        @Override
        public boolean hasNext()
        {
            return pos < maxColumn;
        }
        @Override
        public Cell next() throws NoSuchElementException
        {
            if (hasNext()) {
                return _cells.get(pos++);
            } else {
                throw new NoSuchElementException();
            }
        }
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Compares two <code>SXSSFRow</code> objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   other   the <code>SXSSFRow</code> to be compared.
     * @return  <ul>
     *      <li>
     *      the value <code>0</code> if the row number of this <code>SXSSFRow</code> is
     *      equal to the row number of the argument <code>SXSSFRow</code>
     *      </li>
     *      <li>
     *      a value less than <code>0</code> if the row number of this this <code>SXSSFRow</code> is
     *      numerically less than the row number of the argument <code>SXSSFRow</code>
     *      </li>
     *      <li>
     *      a value greater than <code>0</code> if the row number of this this <code>SXSSFRow</code> is
     *      numerically greater than the row number of the argument <code>SXSSFRow</code>
     *      </li>
     *      </ul>
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    @Override
    public int compareTo(SXSSFRow other) {
        if (this.getSheet() != other.getSheet()) {
            throw new IllegalArgumentException("The compared rows must belong to the same sheet");
        }

        int thisRow = this.getRowNum();
        int otherRow = other.getRowNum();
        return Integer.compare(thisRow, otherRow);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof SXSSFRow))
        {
            return false;
        }
        SXSSFRow other = (SXSSFRow) obj;

        return (this.getRowNum() == other.getRowNum()) &&
                (this.getSheet() == other.getSheet());
    }

    @Override
    public int hashCode() {
        return _cells.hashCode();
    }

    @Override
    @NotImplemented
    public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step){
        throw new NotImplementedException("shiftCellsRight");
    }
    @Override
    @NotImplemented
    public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step){
        throw new NotImplementedException("shiftCellsLeft");
    }

}
