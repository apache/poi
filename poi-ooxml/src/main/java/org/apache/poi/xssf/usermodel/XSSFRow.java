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

package org.apache.poi.xssf.usermodel;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyContext;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

/**
 * High level representation of a row of a spreadsheet.
 */
public class XSSFRow implements Row, Comparable<XSSFRow> {
    /**
     * the xml bean containing all cell definitions for this row
     */
    private final CTRow _row;

    /**
     * Cells of this row keyed by their column indexes.
     * The TreeMap ensures that the cells are ordered by columnIndex in the ascending order.
     */
    private final TreeMap<Integer, XSSFCell> _cells;

    /**
     * the parent sheet
     */
    private final XSSFSheet _sheet;

    /**
     * Construct a XSSFRow.
     *
     * @param row the xml bean containing all cell definitions for this row.
     * @param sheet the parent sheet.
     */
    protected XSSFRow(CTRow row, XSSFSheet sheet) {
        _row = row;
        _sheet = sheet;
        _cells = new TreeMap<>();
        for (CTCell c : row.getCArray()) {
            XSSFCell cell = new XSSFCell(this, c);
            // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
            final Integer colI = Integer.valueOf(cell.getColumnIndex()); // NOSONAR
            _cells.put(colI, cell);
            sheet.onReadCell(cell);
        }

        if (! row.isSetR()) {
            // Certain file format writers skip the row number
            // Assume no gaps, and give this the next row number
            int nextRowNum = sheet.getLastRowNum()+2;
            if (nextRowNum == 2 && sheet.getPhysicalNumberOfRows() == 0) {
                nextRowNum = 1;
            }
            row.setR(nextRowNum);
        }
    }

    /**
     * Returns the XSSFSheet this row belongs to
     *
     * @return the XSSFSheet that owns this row
     */
    @Override
    public XSSFSheet getSheet() {
        return this._sheet;
    }

    /**
     * Cell iterator over the physically defined cells:
     * <pre>{@code
     * for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
     *     Cell cell = it.next();
     *     ...
     * }
     * }</pre>
     *
     * @return an iterator over cells in this row.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Cell> cellIterator() {
        return (Iterator<Cell>)(Iterator<? extends Cell>)_cells.values().iterator();
    }

    /**
     * Cell spliterator over the physically defined cells
     *
     * @return a spliterator over cells in this row.
     *
     * @since POI 5.2.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public Spliterator<Cell> spliterator() {
        return (Spliterator<Cell>)(Spliterator<? extends Cell>)_cells.values().spliterator();
    }

    /**
     * Compares two {@code XSSFRow} objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   other   the {@code XSSFRow} to be compared.
     * @return  <ul>
     *      <li>
     *      the value {@code 0} if the row number of this {@code XSSFRow} is
     *      equal to the row number of the argument {@code XSSFRow}
     *      </li>
     *      <li>
     *      a value less than {@code 0} if the row number of this this {@code XSSFRow} is
     *      numerically less than the row number of the argument {@code XSSFRow}
     *      </li>
     *      <li>
     *      a value greater than {@code 0} if the row number of this this {@code XSSFRow} is
     *      numerically greater than the row number of the argument {@code XSSFRow}
     *      </li>
     *      </ul>
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    @Override
    public int compareTo(XSSFRow other) {
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
        if (!(obj instanceof XSSFRow))
        {
            return false;
        }
        XSSFRow other = (XSSFRow) obj;

        return (this.getRowNum() == other.getRowNum()) &&
                (this.getSheet() == other.getSheet());
    }

    @Override
    public int hashCode() {
        return _row.hashCode();
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link CellType#BLANK}. The type can be changed
     * either through calling {@code setCellValue} or {@code setCellType}.
     * </p>
     * @param columnIndex - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex &lt; 0 or greater than 16384,
     *   the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     */
    @Override
    public XSSFCell createCell(int columnIndex) {
        return createCell(columnIndex, CellType.BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     *
     * @param columnIndex - the column number this cell represents
     * @param type - the cell's data type
     * @return XSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if the specified cell type is invalid, columnIndex &lt; 0
     *   or greater than 16384, the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     */
    @Override
    public XSSFCell createCell(int columnIndex, CellType type) {
        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer colI = Integer.valueOf(columnIndex); // NOSONAR
        CTCell ctCell;
        XSSFCell prev = _cells.get(colI);
        if(prev != null){
            ctCell = prev.getCTCell();
            ctCell.set(CTCell.Factory.newInstance());
        } else {
            ctCell = _row.addNewC();
        }
        XSSFCell xcell = new XSSFCell(this, ctCell);
        try {
            xcell.setCellNum(columnIndex);
        } catch (IllegalArgumentException e) {
            // we need to undo adding the CTCell in _row if something fails here, e.g.
            // cell-limits are exceeded
            _row.removeC(_row.getCList().size()-1);

            throw e;
        }
        if (type != CellType.BLANK && type != CellType.FORMULA) {
            setDefaultValue(xcell, type);
        }

        _cells.put(colI, xcell);
        return xcell;
    }

    private static void setDefaultValue(XSSFCell cell, CellType type) {
        switch (type) {
            case NUMERIC:
                cell.setCellValue(0);
                break;
            case STRING:
                cell.setCellValue("");
                break;
            case BOOLEAN:
                cell.setCellValue(false);
                break;
            case ERROR:
                cell.setCellErrorValue(FormulaError._NO_ERROR);
                break;
            default:
                throw new AssertionError("Unknown cell-type specified: " + type);
        }
    }

    /**
     * Returns the cell at the given (0 based) index,
     *  with the {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} from the parent Workbook.
     *
     * @return the cell at the given (0 based) index
     */
    @Override
    public XSSFCell getCell(int cellnum) {
        return getCell(cellnum, _sheet.getWorkbook().getMissingCellPolicy());
    }

    /**
     * Returns the cell at the given (0 based) index, with the specified {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy}
     *
     * @return the cell at the given (0 based) index
     * @throws IllegalArgumentException if cellnum &lt; 0 or the specified MissingCellPolicy is invalid
     */
    @Override
    public XSSFCell getCell(int cellnum, MissingCellPolicy policy) {
        if(cellnum < 0) {
            throw new IllegalArgumentException("Cell index must be >= 0");
        }

        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer colI = Integer.valueOf(cellnum); // NOSONAR
        XSSFCell cell = _cells.get(colI);
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
     * Get the 0-based number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    @Override
    public short getFirstCellNum() {
        return (short)(_cells.isEmpty() ? -1 : _cells.firstKey());
    }

    /**
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     *   XSSFCell cell = row.getCell(colIx);
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
    public short getLastCellNum() {
        return (short)(_cells.isEmpty() ? -1 : (_cells.lastKey() + 1));
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
    @Override
    public short getHeight() {
        return (short)(getHeightInPoints()*20);
    }

    /**
     * Returns row height measured in point size. If the height is not set, the default worksheet value is returned,
     * See {@link org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in point size
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()
     */
    @Override
    public float getHeightInPoints() {
        if (this._row.isSetHt()) {
            return (float) this._row.getHt();
        }
        return _sheet.getDefaultRowHeightInPoints();
    }

    /**
     *  Set the height in "twips" or  1/20th of a point.
     *
     * @param height the height in "twips" or  1/20th of a point. {@code -1}  resets to the default height
     */
    @Override
    public void setHeight(short height) {
        if (height == -1) {
            if (_row.isSetHt()) {
                _row.unsetHt();
            }
            if (_row.isSetCustomHeight()) {
                _row.unsetCustomHeight();
            }
        } else {
            _row.setHt((double) height / 20);
            _row.setCustomHeight(true);

        }
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. {@code -1}  resets to the default height
     */
    @Override
    public void setHeightInPoints(float height) {
        setHeight((short)(height == -1 ? -1 : (height*20)));
    }

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    @Override
    public int getPhysicalNumberOfCells() {
        return _cells.size();
    }

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    @Override
    public int getRowNum() {
        return Math.toIntExact(_row.getR() - 1);
    }

    /**
     * Set the row number of this row.
     *
     * @param rowIndex  the row number (0-based)
     * @throws IllegalArgumentException if rowNum &lt; 0 or greater than 1048575
     */
    @Override
    public void setRowNum(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        if (rowIndex < 0 || rowIndex > maxrow) {
            throw new IllegalArgumentException("Invalid row number (" + rowIndex
                    + ") outside allowable range (0.." + maxrow + ")");
        }
        _row.setR(rowIndex + 1L);
    }

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - height is zero or not.
     */
    @Override
    public boolean getZeroHeight() {
        return this._row.getHidden();
    }

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param height  height is zero or not.
     */
    @Override
    public void setZeroHeight(boolean height) {
        this._row.setHidden(height);

    }

    /**
     * Is this row formatted? Most aren't, but some rows
     *  do have whole-row styles. For those that do, you
     *  can get the formatting from {@link #getRowStyle()}
     */
    @Override
    public boolean isFormatted() {
        return _row.isSetS();
    }
    /**
     * Returns the whole-row cell style. Most rows won't
     *  have one of these, so will return null. Call
     *  {@link #isFormatted()} to check first.
     */
    @Override
    public XSSFCellStyle getRowStyle() {
        if(!isFormatted()) {
            return null;
        }

        StylesTable stylesSource = getSheet().getWorkbook().getStylesSource();
        if(stylesSource.getNumCellStyles() > 0) {
            return stylesSource.getStyleAt(Math.toIntExact(_row.getS()));
        } else {
            return null;
        }
    }

    /**
     * Applies a whole-row cell styling to the row.
     * If the value is null then the style information is removed,
     *  causing the cell to used the default workbook style.
     */
    @Override
    public void setRowStyle(CellStyle style) {
        if(style == null) {
            if(_row.isSetS()) {
                _row.unsetS();
                _row.unsetCustomFormat();
            }
        } else {
            StylesTable styleSource = getSheet().getWorkbook().getStylesSource();

            XSSFCellStyle xStyle = (XSSFCellStyle)style;
            xStyle.verifyBelongsToStylesSource(styleSource);

            long idx = styleSource.putStyle(xStyle);
            _row.setS(idx);
            _row.setCustomFormat(true);
        }
    }

    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    @Override
    public void removeCell(Cell cell) {
        if (cell.getRow() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this row");
        }
        //noinspection SuspiciousMethodCalls
        if(!_cells.containsValue(cell)) {
            throw new IllegalArgumentException("the row does not contain this cell");
        }

        XSSFCell xcell = (XSSFCell)cell;
        if(xcell.isPartOfArrayFormulaGroup()) {
            xcell.setCellFormula(null); // to remove the array formula
        }
        if(cell.getCellType() == CellType.FORMULA) {
            _sheet.getWorkbook().onDeleteFormula(xcell);
        }
        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer colI = Integer.valueOf(cell.getColumnIndex()); // NOSONAR
        XSSFCell removed = _cells.remove(colI);

        // also remove the corresponding CTCell from the _row.cArray,
        // it may not be at the same position right now
        // thus search for it
        int i = 0;
        for (CTCell ctCell : _row.getCArray()) {
            if(ctCell == removed.getCTCell()) {
                _row.removeC(i);
            }
            i++;
        }
    }

    /**
     * Returns the underlying CTRow xml bean containing all cell definitions in this row
     *
     * @return the underlying CTRow xml bean
     */
    @Internal
    public CTRow getCTRow(){
        return _row;
    }

    /**
     * Fired when the document is written to an output stream.
     *
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#write(java.io.OutputStream) ()
     */
    protected void onDocumentWrite() {
        // _row.cArray and _cells.getCTCell might be out of sync after adding/removing cells,
        // thus we need to re-order it here to make the resulting file correct

        // do a quick check if there is work to do to not incur the overhead if not necessary anyway
        CTCell[] cArrayOrig = _row.getCArray();
        if(cArrayOrig.length == _cells.size()) {
            boolean allEqual = true;
            Iterator<XSSFCell> it = _cells.values().iterator();
            for (CTCell ctCell : cArrayOrig) {
                XSSFCell cell = it.next();
                cell.applyDefaultCellStyleIfNecessary();

                // we want to compare on identity here on purpose
                // as we want to ensure that both lists contain the
                // same documents, not copies!
                if (ctCell != cell.getCTCell()) {
                    allEqual = false;
                    break;
                }
            }

            // we did not find any difference, so we can skip the work
            if(allEqual) {
                return;
            }
        }

        fixupCTCells(cArrayOrig);
    }

    private void fixupCTCells(CTCell[] cArrayOrig) {
        // copy all values to 2nd array and a map for lookup of index
        CTCell[] cArrayCopy = new CTCell[cArrayOrig.length];
        IdentityHashMap<CTCell, Integer> map = new IdentityHashMap<>(_cells.size());
        int i = 0;
        for (CTCell ctCell : cArrayOrig) {
            cArrayCopy[i] = (CTCell) ctCell.copy();
            map.put(ctCell, i);
            i++;
        }

        // populate _row.cArray correctly
        i = 0;
        for (XSSFCell cell : _cells.values()) {
            // no need to change anything if position is correct
            Integer correctPosition = map.get(cell.getCTCell());
            Objects.requireNonNull(correctPosition, "Should find CTCell in _row");
            if(correctPosition != i) {
                // we need to re-populate this CTCell
                _row.setCArray(i, cArrayCopy[correctPosition]);
                cell.setCTCell(_row.getCArray(i));
            }
            i++;
        }

        // remove any remaining illegal references in _rows.cArray
        while(cArrayOrig.length > _cells.size()) {
            _row.removeC(_cells.size());
        }
    }

    /**
     * @return formatted xml representation of this row
     */
    @Override
    public String toString(){
        return _row.toString();
    }

    /**
     * update cell references when shifting rows
     *
     * @param n the number of rows to move
     */
    protected void shift(int n) {
        final int rownum = getRowNum();
        final int newRownum = rownum + n;
        String msg = "Row[rownum=" + rownum + "] contains cell(s) included in a multi-cell array formula. " +
                "You cannot change part of an array.";
        setRowNum(newRownum);
        for(Cell c : this){
            ((XSSFCell)c).updateCellReferencesForShifting(msg);
        }

    }

    /**
     * Copy the cells from srcRow to this row.
     * If this row is not a blank row, this will merge the two rows, overwriting
     * the cells in this row with the cells in srcRow.
     * If srcRow is null, overwrite cells in destination row with blank values, styles, etc per cell copy policy.
     *
     * Note that if you are copying from a non-XSSF row then you will need to disable style copying
     * in the {@link CellCopyPolicy} (XSSF styles are not compatible with HSSF styles, for instance).
     *
     * @param srcRow the rows to copy from
     * @param policy the policy to determine what gets copied
     */
    @Beta
    public void copyRowFrom(Row srcRow, CellCopyPolicy policy) {
        copyRowFrom(srcRow, policy, null);
    }

    /**
     * Copy the cells from srcRow to this row
     * If this row is not a blank row, this will merge the two rows, overwriting
     * the cells in this row with the cells in srcRow
     * If srcRow is null, overwrite cells in destination row with blank values, styles, etc per cell copy policy
     * srcRow may be from a different sheet in the same workbook
     *
     * Note that if you are copying from a non-XSSF row then you will need to disable style copying
     * in the {@link CellCopyPolicy} (XSSF styles are not compatible with HSSF styles, for instance).
     *
     * @param srcRow the rows to copy from
     * @param policy the policy to determine what gets copied
     * @param context the context - see {@link CellCopyContext}
     */
    @Beta
    public void copyRowFrom(Row srcRow, CellCopyPolicy policy, CellCopyContext context) {
        if (srcRow == null) {
            // srcRow is blank. Overwrite cells with blank values, blank styles, etc per cell copy policy
            for (Cell destCell : this) {
                CellUtil.copyCell(null, destCell, policy, context);
            }

            if (policy.isCopyMergedRegions()) {
                // Remove MergedRegions in dest row
                final int destRowNum = getRowNum();
                int index = 0;
                final Set<Integer> indices = new HashSet<>();
                for (CellRangeAddress destRegion : getSheet().getMergedRegions()) {
                    if (destRowNum == destRegion.getFirstRow() && destRowNum == destRegion.getLastRow()) {
                        indices.add(index);
                    }
                    index++;
                }
                getSheet().removeMergedRegions(indices);
            }

            if (policy.isCopyRowHeight()) {
                // clear row height
                setHeight((short)-1);
            }

        } else {
            for (final Cell c : srcRow) {
                final XSSFCell destCell = createCell(c.getColumnIndex());
                CellUtil.copyCell(c, destCell, policy, context);
            }

            final int sheetIndex = _sheet.getWorkbook().getSheetIndex(_sheet);
            final String sheetName = _sheet.getWorkbook().getSheetName(sheetIndex);
            final int srcRowNum = srcRow.getRowNum();
            final int destRowNum = getRowNum();
            final int rowDifference = destRowNum - srcRowNum;

            final FormulaShifter formulaShifter = FormulaShifter.createForRowCopy(sheetIndex, sheetName, srcRowNum, srcRowNum, rowDifference, SpreadsheetVersion.EXCEL2007);
            final XSSFRowShifter rowShifter = new XSSFRowShifter(_sheet);
            rowShifter.updateRowFormulas(this, formulaShifter);

            // Copy merged regions that are fully contained on the row
            // FIXME: is this something that rowShifter could be doing?
            if (policy.isCopyMergedRegions()) {
                for (CellRangeAddress srcRegion : srcRow.getSheet().getMergedRegions()) {
                    if (srcRowNum == srcRegion.getFirstRow() && srcRowNum == srcRegion.getLastRow()) {
                        CellRangeAddress destRegion = srcRegion.copy();
                        destRegion.setFirstRow(destRowNum);
                        destRegion.setLastRow(destRowNum);
                        getSheet().addMergedRegion(destRegion);
                    }
                }
            }

            if (policy.isCopyRowHeight()) {
                setHeight(srcRow.getHeight());
            }
        }
    }

    @Override
    public int getOutlineLevel() {
        return _row.getOutlineLevel();
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

        for (int columnIndex = lastShiftColumnIndex; columnIndex >= firstShiftColumnIndex; columnIndex--){ // process cells backwards, because of shifting
            shiftCell(columnIndex, step);
        }
        for (int columnIndex = firstShiftColumnIndex; columnIndex <= firstShiftColumnIndex+step-1; columnIndex++)
        {
            _cells.remove(columnIndex);
            XSSFCell targetCell = getCell(columnIndex);
            if(targetCell != null) {
                targetCell.getCTCell().set(CTCell.Factory.newInstance());
            }
        }
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
            shiftCell(columnIndex, -step);
        }
        for (int columnIndex = lastShiftColumnIndex-step+1; columnIndex <= lastShiftColumnIndex; columnIndex++){
            _cells.remove(columnIndex);
            XSSFCell targetCell = getCell(columnIndex);
            if(targetCell != null) {
                targetCell.getCTCell().set(CTCell.Factory.newInstance());
            }
        }
    }

    private void shiftCell(int columnIndex, int step/*pass negative value for left shift*/){
        if(columnIndex + step < 0) {
            throw new IllegalStateException("Column index less than zero : " + (Integer.valueOf(columnIndex + step)));
        }

        XSSFCell currentCell = getCell(columnIndex);
        if(currentCell != null){
            currentCell.setCellNum(columnIndex+step);
            _cells.put(columnIndex+step, currentCell);
        }
        else {
            _cells.remove(columnIndex+step);
            XSSFCell targetCell = getCell(columnIndex+step);
            if(targetCell != null) {
                targetCell.getCTCell().set(CTCell.Factory.newInstance());
            }
        }
    }
}
