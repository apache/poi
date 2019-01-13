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

package org.apache.poi.hssf.usermodel;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.util.Configurator;

/**
 * High level representation of a row of a spreadsheet.
 *
 * Only rows that have cells should be added to a Sheet.
 */
public final class HSSFRow implements Row, Comparable<HSSFRow> {

    // used for collections
    public final static int INITIAL_CAPACITY = Configurator.getIntValue("HSSFRow.ColInitialCapacity", 5);

    private int rowNum;
    private HSSFCell[] cells;

    /**
     * reference to low level representation
     */
    private final RowRecord row;

    /**
     * reference to containing low level Workbook
     */
    private final HSSFWorkbook book;

    /**
     * reference to containing Sheet
     */
    private final HSSFSheet sheet;

    /**
     * Creates new HSSFRow from scratch. Only HSSFSheet should do this.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param rowNum the row number of this row (0 based)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(int)
     */
    HSSFRow(HSSFWorkbook book, HSSFSheet sheet, int rowNum) {
        this(book, sheet, new RowRecord(rowNum));
    }

    /**
     * Creates an HSSFRow from a low level RowRecord object.  Only HSSFSheet should do
     * this.  HSSFSheet uses this when an existing file is read in.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param record the low level api object this row should represent
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(int)
     */
    HSSFRow(HSSFWorkbook book, HSSFSheet sheet, RowRecord record) {
        this.book = book;
        this.sheet = sheet;
        row = record;
        setRowNum(record.getRowNumber());
        
        // Size the initial cell list such that a read only case won't waste
        //  lots of memory, and a create/read followed by adding new cells can
        //  add a bit without needing a resize
        cells = new HSSFCell[record.getLastCol()+INITIAL_CAPACITY];
        
        // Don't trust colIx boundaries as read by other apps
        // set the RowRecord empty for the moment
        record.setEmpty();
        // subsequent calls to createCellFromRecord() will update the colIx boundaries properly
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link CellType#BLANK}. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     *   the maximum number of columns supported by the Excel binary format (.xls)
     */
    @Override
    public HSSFCell createCell(int column)
    {
        return this.createCell(column,CellType.BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned will be of the requested type.
     * The type can be changed either through calling setCellValue 
     *  or setCellType, but there is a small overhead to doing this,
     *  so it is best to create the required type up front.
     *
     * @param columnIndex - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 255,
     *   the maximum number of columns supported by the Excel binary format (.xls)
     */
    @Override
    public HSSFCell createCell(int columnIndex, CellType type)
    {
        short shortCellNum = (short)columnIndex;
        if(columnIndex > 0x7FFF) {
            shortCellNum = (short)(0xffff - columnIndex);
        }

        HSSFCell cell = new HSSFCell(book, sheet, getRowNum(), shortCellNum, type);
        addCell(cell);
        sheet.getSheet().addValueRecord(getRowNum(), cell.getCellValueRecord());
        return cell;
    }

    /**
     * remove the HSSFCell from this row.
     * @param cell to remove
     */
    @Override
    public void removeCell(Cell cell) {
        if(cell == null) {
            throw new IllegalArgumentException("cell must not be null");
        }
        removeCell((HSSFCell)cell, true);
    }
    private void removeCell(HSSFCell cell, boolean alsoRemoveRecords) {

        int column=cell.getColumnIndex();
        if(column < 0) {
            throw new RuntimeException("Negative cell indexes not allowed");
        }
        if(column >= cells.length || cell != cells[column]) {
            throw new RuntimeException("Specified cell is not from this row");
        }
        if(cell.isPartOfArrayFormulaGroup()){
            cell.tryToDeleteArrayFormula(null);
        }

        cells[column]=null;

        if(alsoRemoveRecords) {
            CellValueRecordInterface cval = cell.getCellValueRecord();
            sheet.getSheet().removeValueRecord(getRowNum(), cval);
        }
        if (cell.getColumnIndex()+1 == row.getLastCol()) {
            row.setLastCol(calculateNewLastCellPlusOne(row.getLastCol()));
        }
        if (cell.getColumnIndex() == row.getFirstCol()) {
            row.setFirstCol(calculateNewFirstCell(row.getFirstCol()));
        }
    }

    /**
     * Removes all the cells from the row, and their
     *  records too.
     */
    protected void removeAllCells() {
        for (HSSFCell cell : cells) {
            if (cell != null) {
                removeCell(cell, true);
            }
        }
        cells=new HSSFCell[INITIAL_CAPACITY];
    }

    /**
     * create a high level HSSFCell object from an existing low level record.  Should
     * only be called from HSSFSheet or HSSFRow itself.
     * @param cell low level cell to create the high level representation from
     * @return HSSFCell representing the low level record passed in
     */
    HSSFCell createCellFromRecord(CellValueRecordInterface cell) {
        HSSFCell hcell = new HSSFCell(book, sheet, cell);

        addCell(hcell);
        int colIx = cell.getColumn();
        if (row.isEmpty()) {
            row.setFirstCol(colIx);
            row.setLastCol(colIx + 1);
        } else {
            if (colIx < row.getFirstCol()) {
                row.setFirstCol(colIx);
            } else if (colIx > row.getLastCol()) {
                row.setLastCol(colIx + 1);
            } /*else {
                // added cell is within first and last cells
            }*/
        }
        // TODO - RowRecord column boundaries need to be updated for cell comments too
        return hcell;
    }

    /**
     * set the row number of this row.
     * @param rowIndex  the row number (0-based)
     * @throws IndexOutOfBoundsException if the row number is not within the range 0-65535.
     */
    @Override
    public void setRowNum(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if ((rowIndex < 0) || (rowIndex > maxrow)) {
          throw new IllegalArgumentException("Invalid row number (" + rowIndex
                  + ") outside allowable range (0.." + maxrow + ")");
        }
        rowNum = rowIndex;
        if (row != null) {
            row.setRowNumber(rowIndex);   // used only for KEY comparison (HSSFRow)
        }
    }

    /**
     * get row number this row represents
     * @return the row number (0 based)
     */
    @Override
    public int getRowNum()
    {
        return rowNum;
    }

    /**
     * Returns the HSSFSheet this row belongs to
     *
     * @return the HSSFSheet that owns this row
     */
    @Override
    public HSSFSheet getSheet()
    {
        return sheet;
    }

    /**
     * Returns the rows outline level. Increased as you
     *  put it into more groups (outlines), reduced as
     *  you take it out of them.
     */
    @Override
    public int getOutlineLevel() {
        return row.getOutlineLevel();
    }

    /**
     * Moves the supplied cell to a new column, which
     *  must not already have a cell there!
     * @param cell The cell to move
     * @param newColumn The new column number (0 based)
     */
    public void moveCell(HSSFCell cell, short newColumn) {
        // Ensure the destination is free
        if(cells.length > newColumn && cells[newColumn] != null) {
            throw new IllegalArgumentException("Asked to move cell to column " + newColumn + " but there's already a cell there");
        }

        // Check it's one of ours
        if(! cells[cell.getColumnIndex()].equals(cell)) {
            throw new IllegalArgumentException("Asked to move a cell, but it didn't belong to our row");
        }

        // Move the cell to the new position
        // (Don't remove the records though)
        removeCell(cell, false);
        cell.updateCellNum(newColumn);
        addCell(cell);
    }

    /**
     * used internally to add a cell.
     */
    private void addCell(HSSFCell cell) {

        int column=cell.getColumnIndex();
        // re-allocate cells array as required.
        if(column>=cells.length) {
            HSSFCell[] oldCells=cells;
            // New size based on the same logic as ArrayList
            int newSize=oldCells.length*3/2+1;
            if(newSize<column+1) {
                newSize=column+INITIAL_CAPACITY;
            }
            cells=new HSSFCell[newSize];
            System.arraycopy(oldCells,0,cells,0,oldCells.length);
        }
        cells[column]=cell;

        // fix up firstCol and lastCol indexes
        if (row.isEmpty() || column < row.getFirstCol()) {
            row.setFirstCol((short)column);
        }

        if (row.isEmpty() || column >= row.getLastCol()) {
            row.setLastCol((short) (column+1)); // +1 -> for one past the last index
        }
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based. If you ask for a cell that is not defined, then
     *  you get a null.
     * This is the basic call, with no policies applied
     *
     * @param cellIndex  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    private HSSFCell retrieveCell(int cellIndex) {
        if(cellIndex<0||cellIndex>=cells.length) {
            return null;
        }
        return cells[cellIndex];
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined then
     *  you get a null, unless you have set a different
     *  {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} on the base workbook.
     *
     * @param cellnum  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */
    @Override
    public HSSFCell getCell(int cellnum) {
        return getCell(cellnum, book.getMissingCellPolicy());
    }

    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined, then
     *  your supplied policy says what to do
     *
     * @param cellnum  0 based column number
     * @param policy Policy on blank / missing cells
     * @return representing that column or null if undefined + policy allows.
     */
    @Override
    public HSSFCell getCell(int cellnum, MissingCellPolicy policy) {
        HSSFCell cell = retrieveCell(cellnum);
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
     * get the number of the first cell contained in this row.
     * @return short representing the first logical cell in the row, or -1 if the row does not contain any cells.
     */
    @Override
    public short getFirstCellNum() {
        if (row.isEmpty()) {
            return -1;
        }
        return (short) row.getFirstCol();
    }

    /**
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     *   HSSFCell cell = row.getCell(colIx);
     *   if(cell == null) {
     *     continue;
     *   }
     *   //... do something with cell
     * }
     * </pre>
     *
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>, or -1 if the
     *  row does not contain any cells.
     */
    @Override
    public short getLastCellNum() {
        if (row.isEmpty()) {
            return -1;
        }
        return (short) row.getLastCol();
    }


    /**
     * gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     * @return int representing the number of defined cells in the row.
     */

    @Override
    public int getPhysicalNumberOfCells()
    {
        int count = 0;
        for (HSSFCell cell : cells) {
            if (cell != null) count++;
        }
        return count;
    }

    /**
     * set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     * @param height  rowheight or -1 for undefined (use sheet default)
     */

    @Override
    public void setHeight(short height)
    {
        if(height == -1){
            row.setHeight((short)(0xFF | 0x8000));
            row.setBadFontHeight(false);
        } else {
            row.setBadFontHeight(true);
            row.setHeight(height);
        }
    }

    /**
     * set whether or not to display this row with 0 height
     * @param zHeight  height is zero or not.
     */
    @Override
    public void setZeroHeight(boolean zHeight) {
        row.setZeroHeight(zHeight);
    }

    /**
     * get whether or not to display this row with 0 height
     * @return - zHeight height is zero or not.
     */
    @Override
    public boolean getZeroHeight() {
        return row.getZeroHeight();
    }

    /**
     * set the row's height in points.
     * @param height  row height in points, <code>-1</code> means to use the default height
     */

    @Override
    public void setHeightInPoints(float height)
    {
        if(height == -1){
            row.setHeight((short)(0xFF | 0x8000));
            row.setBadFontHeight(false);
        } else {
            row.setBadFontHeight(true);
            row.setHeight((short) (height * 20));
        }
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in twips (1/20th of a point)
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    @Override
    public short getHeight()
    {
        short height = row.getHeight();

        //The low-order 15 bits contain the row height.
        //The 0x8000 bit indicates that the row is standard height (optional)
        if ((height & 0x8000) != 0) height = sheet.getSheet().getDefaultRowHeight();
        else height &= 0x7FFF;

        return height;
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in points (20*getHeight())
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    @Override
    public float getHeightInPoints()
    {
        return ((float)getHeight() / 20);
    }

    /**
     * get the lowlevel RowRecord represented by this object - should only be called
     * by other parts of the high level API
     *
     * @return RowRecord this row represents
     */

    protected RowRecord getRowRecord()
    {
        return row;
    }

    /**
     * used internally to refresh the "last cell plus one" when the last cell is removed.
     * @return 0 when row contains no cells
     */
    private int calculateNewLastCellPlusOne(int lastcell) {
        int cellIx = lastcell - 1;
        HSSFCell r = retrieveCell(cellIx);

        while (r == null) {
            if (cellIx < 0) {
                return 0;
            }
            r = retrieveCell(--cellIx);
        }
        return cellIx+1;
    }

    /**
     * used internally to refresh the "first cell" when the first cell is removed.
     * @return 0 when row contains no cells (also when first cell is occupied)
     */
    private int calculateNewFirstCell(int firstcell) {
        int cellIx = firstcell + 1;
        HSSFCell r = retrieveCell(cellIx);

        while (r == null) {
            if (cellIx <= cells.length) {
                return 0;
            }
            r = retrieveCell(++cellIx);
        }
        return cellIx;
    }

    /**
     * Is this row formatted? Most aren't, but some rows
     *  do have whole-row styles. For those that do, you
     *  can get the formatting from {@link #getRowStyle()}
     */
    @Override
    public boolean isFormatted() {
        return row.getFormatted();
    }
    /**
     * Returns the whole-row cell styles. Most rows won't
     *  have one of these, so will return null. Call
     *  {@link #isFormatted()} to check first.
     */
    @Override
    public HSSFCellStyle getRowStyle() {
        if(!isFormatted()) { return null; }
        short styleIndex = row.getXFIndex();
        ExtendedFormatRecord xf = book.getWorkbook().getExFormatAt(styleIndex);
        return new HSSFCellStyle(styleIndex, xf, book);
    }
    /**
     * Applies a whole-row cell styling to the row.
     */
    public void setRowStyle(HSSFCellStyle style) {
        row.setFormatted(true);
        row.setXFIndex(style.getIndex());
    }
    /**
     * Applies a whole-row cell styling to the row.
     */
    @Override
    public void setRowStyle(CellStyle style) {
        setRowStyle((HSSFCellStyle)style);
    }

    /**
     * @return cell iterator of the physically defined cells.
     * Note that the 4th element might well not be cell 4, as the iterator
     *  will not return un-defined (null) cells.
     * Call getCellNum() on the returned cells to know which cell they are.
     * As this only ever works on physically defined cells,
     *  the {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} has no effect.
     */
    @Override
    public Iterator<Cell> cellIterator()
    {
      return new CellIterator();
    }
    /**
     * Alias for {@link #cellIterator} to allow
     *  foreach loops
     */
    @Override
    public Iterator<Cell> iterator() {
       return cellIterator();
    }

    /**
     * An iterator over the (physical) cells in the row.
     */
    private class CellIterator implements Iterator<Cell> {
      int thisId=-1;
      int nextId=-1;

      public CellIterator()
      {
        findNext();
      }

      @Override
    public boolean hasNext() {
        return nextId<cells.length;
      }

      @Override
    public Cell next() {
          if (!hasNext())
              throw new NoSuchElementException("At last element");
        HSSFCell cell=cells[nextId];
        thisId=nextId;
        findNext();
        return cell;
      }

      @Override
    public void remove() {
          if (thisId == -1)
              throw new IllegalStateException("remove() called before next()");
        cells[thisId]=null;
      }

      private void findNext()
      {
        int i=nextId+1;
        for(;i<cells.length;i++)
        {
          if(cells[i]!=null) break;
        }
        nextId=i;
      }

    }
    
    /**
     * Compares two <code>HSSFRow</code> objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   other   the <code>HSSFRow</code> to be compared.
     * @return  <ul>
     *      <li>
     *      the value <code>0</code> if the row number of this <code>HSSFRow</code> is
     *      equal to the row number of the argument <code>HSSFRow</code>
     *      </li>
     *      <li>
     *      a value less than <code>0</code> if the row number of this this <code>HSSFRow</code> is
     *      numerically less than the row number of the argument <code>HSSFRow</code>
     *      </li>
     *      <li>
     *      a value greater than <code>0</code> if the row number of this this <code>HSSFRow</code> is
     *      numerically greater than the row number of the argument <code>HSSFRow</code>
     *      </li>
     *      </ul>
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    @Override
    public int compareTo(HSSFRow other)
    {
        if (this.getSheet() != other.getSheet()) {
            throw new IllegalArgumentException("The compared rows must belong to the same sheet");
        }

        Integer thisRow = this.getRowNum();
        Integer otherRow = other.getRowNum();
        return thisRow.compareTo(otherRow);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof HSSFRow))
        {
            return false;
        }
        HSSFRow other = (HSSFRow) obj;

        return (this.getRowNum() == other.getRowNum()) &&
               (this.getSheet() == other.getSheet());
    }

    @Override
    public int hashCode() {
        return row.hashCode();
    }
    
    /**
     * Shifts column range [firstShiftColumnIndex-lastShiftColumnIndex] step places to the right.
     * @param firstShiftColumnIndex the column to start shifting
     * @param lastShiftColumnIndex the column to end shifting
     * @param step length of the shifting step
     */
    @Override
    public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        RowShifter.validateShiftParameters(firstShiftColumnIndex, lastShiftColumnIndex, step);

        if(lastShiftColumnIndex + step + 1> cells.length)
            extend(lastShiftColumnIndex + step + 1);
        for (int columnIndex = lastShiftColumnIndex; columnIndex >= firstShiftColumnIndex; columnIndex--){ // process cells backwards, because of shifting 
            HSSFCell cell = getCell(columnIndex);
            cells[columnIndex+step] = null;
            if(cell != null)
                moveCell(cell, (short)(columnIndex+step));
        }
        for (int columnIndex = firstShiftColumnIndex; columnIndex <= firstShiftColumnIndex+step-1; columnIndex++)
            cells[columnIndex] = null;
    }
    private void extend(int newLenght){
        HSSFCell[] temp = cells.clone();
        cells = new HSSFCell[newLenght];
        System.arraycopy(temp, 0, cells, 0, temp.length);
    }

    /**
     * Shifts column range [firstShiftColumnIndex-lastShiftColumnIndex] step places to the left.
     * @param firstShiftColumnIndex the column to start shifting
     * @param lastShiftColumnIndex the column to end shifting
     * @param step length of the shifting step
     */
    @Override
    public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        RowShifter.validateShiftLeftParameters(firstShiftColumnIndex, lastShiftColumnIndex, step);

        for (int columnIndex = firstShiftColumnIndex; columnIndex <= lastShiftColumnIndex; columnIndex++){ 
            HSSFCell cell = getCell(columnIndex);
            if(cell != null){
                cells[columnIndex-step] = null;
                moveCell(cell, (short)(columnIndex-step));
            }
            else cells[columnIndex-step] = null;
        }
        for (int columnIndex = lastShiftColumnIndex-step+1; columnIndex <= lastShiftColumnIndex; columnIndex++)
            cells[columnIndex] = null;
    }
}
