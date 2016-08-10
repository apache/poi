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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

/**
 * High level representation of a row of a spreadsheet.
 */
public class XSSFRow implements Row, Comparable<XSSFRow> {
    //private static final POILogger _logger = POILogFactory.getLogger(XSSFRow.class);

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
        _cells = new TreeMap<Integer, XSSFCell>();
        for (CTCell c : row.getCArray()) {
            XSSFCell cell = new XSSFCell(this, c);
            // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
            final Integer colI = new Integer(cell.getColumnIndex()); // NOSONAR
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
     * <blockquote><pre>
     * for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
     *     Cell cell = it.next();
     *     ...
     * }
     * </pre></blockquote>
     *
     * @return an iterator over cells in this row.
     */
    @Override
    @SuppressWarnings("unchecked")
	public Iterator<Cell> cellIterator() {
        return (Iterator<Cell>)(Iterator<? extends Cell>)_cells.values().iterator();
    }

    /**
     * Alias for {@link #cellIterator()} to allow  foreach loops:
     * <blockquote><pre>
     * for(Cell cell : row){
     *     ...
     * }
     * </pre></blockquote>
     *
     * @return an iterator over cells in this row.
     */
    @Override
    public Iterator<Cell> iterator() {
    	return cellIterator();
    }

    /**
     * Compares two <code>XSSFRow</code> objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   other   the <code>XSSFRow</code> to be compared.
     * @return  <ul>
     *      <li>
     *      the value <code>0</code> if the row number of this <code>XSSFRow</code> is
     *      equal to the row number of the argument <code>XSSFRow</code>
     *      </li>
     *      <li>
     *      a value less than <code>0</code> if the row number of this this <code>XSSFRow</code> is
     *      numerically less than the row number of the argument <code>XSSFRow</code>
     *      </li>
     *      <li>
     *      a value greater than <code>0</code> if the row number of this this <code>XSSFRow</code> is
     *      numerically greater than the row number of the argument <code>XSSFRow</code>
     *      </li>
     *      </ul>
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    @Override
    public int compareTo(XSSFRow other) {
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
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     * </p>
     * @param columnIndex - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 16384,
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
     * @throws IllegalArgumentException if the specified cell type is invalid, columnIndex < 0
     *   or greater than 16384, the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     * @see CellType#BLANK
     * @see CellType#BOOLEAN
     * @see CellType#ERROR
     * @see CellType#FORMULA
     * @see CellType#NUMERIC
     * @see CellType#STRING
     * @deprecated POI 3.15 beta 3. Use {@link #createCell(int, CellType)} instead.
     */
    @Override
    public XSSFCell createCell(int columnIndex, int type) {
        return createCell(columnIndex, CellType.forInt(type));
    }
    /**
     * Use this to create new cells within the row and return it.
     *
     * @param columnIndex - the column number this cell represents
     * @param type - the cell's data type
     * @return XSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if the specified cell type is invalid, columnIndex < 0
     *   or greater than 16384, the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     */
    @Override
    public XSSFCell createCell(int columnIndex, CellType type) {
        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer colI = new Integer(columnIndex); // NOSONAR
        CTCell ctCell;
        XSSFCell prev = _cells.get(colI);
        if(prev != null){
            ctCell = prev.getCTCell();
            ctCell.set(CTCell.Factory.newInstance());
        } else {
            ctCell = _row.addNewC();
        }
        XSSFCell xcell = new XSSFCell(this, ctCell);
        xcell.setCellNum(columnIndex);
        if (type != CellType.BLANK) {
            xcell.setCellType(type);
        }
        _cells.put(colI, xcell);
        return xcell;
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
    	if(cellnum < 0) throw new IllegalArgumentException("Cell index must be >= 0");

        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
    	final Integer colI = new Integer(cellnum); // NOSONAR
        XSSFCell cell = _cells.get(colI);
        switch (policy) {
            case RETURN_NULL_AND_BLANK:
                return cell;
            case RETURN_BLANK_AS_NULL:
                boolean isBlank = (cell != null && cell.getCellTypeEnum() == CellType.BLANK);
                return (isBlank) ? null : cell;
            case CREATE_NULL_AS_BLANK:
                return (cell == null) ? createCell(cellnum, CellType.BLANK) : cell;
            default:
                throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
        }
    }

    /**
     * Get the number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    @Override
    public short getFirstCellNum() {
    	return (short)(_cells.size() == 0 ? -1 : _cells.firstKey());
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
    	return (short)(_cells.size() == 0 ? -1 : (_cells.lastKey() + 1));
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
     * @param height the height in "twips" or  1/20th of a point. <code>-1</code>  resets to the default height
     */
    @Override
    public void setHeight(short height) {
        if (height == -1) {
            if (_row.isSetHt()) _row.unsetHt();
            if (_row.isSetCustomHeight()) _row.unsetCustomHeight();
        } else {
            _row.setHt((double) height / 20);
            _row.setCustomHeight(true);

        }
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
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
        return (int) (_row.getR() - 1);
    }

    /**
     * Set the row number of this row.
     *
     * @param rowIndex  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0 or greater than 1048575
     */
    @Override
    public void setRowNum(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        if (rowIndex < 0 || rowIndex > maxrow) {
            throw new IllegalArgumentException("Invalid row number (" + rowIndex
                    + ") outside allowable range (0.." + maxrow + ")");
        }
        _row.setR(rowIndex + 1);
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
       if(!isFormatted()) return null;
       
       StylesTable stylesSource = getSheet().getWorkbook().getStylesSource();
       if(stylesSource.getNumCellStyles() > 0) {
           return stylesSource.getStyleAt((int)_row.getS());
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

        XSSFCell xcell = (XSSFCell)cell;
        if(xcell.isPartOfArrayFormulaGroup()) {
            xcell.notifyArrayFormulaChanging();
        }
        if(cell.getCellTypeEnum() == CellType.FORMULA) {
           _sheet.getWorkbook().onDeleteFormula(xcell);
        }
        // Performance optimization for bug 57840: explicit boxing is slightly faster than auto-unboxing, though may use more memory
        final Integer colI = new Integer(cell.getColumnIndex()); // NOSONAR
        _cells.remove(colI);
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
    protected void onDocumentWrite(){
        // check if cells in the CTRow are ordered
        boolean isOrdered = true;
        CTCell[] cArray = _row.getCArray();
        if (cArray.length != _cells.size()) {
            isOrdered = false;
        } else {
            int i = 0;
            for (XSSFCell cell : _cells.values()) {
                CTCell c1 = cell.getCTCell();
                CTCell c2 = cArray[i++];

                String r1 = c1.getR();
                String r2 = c2.getR();
                if (!(r1==null ? r2==null : r1.equals(r2))){
                    isOrdered = false;
                    break;
                }
            }
        }

        if(!isOrdered){
            cArray = new CTCell[_cells.size()];
            int i = 0;
            for (XSSFCell xssfCell : _cells.values()) {
                cArray[i] = (CTCell) xssfCell.getCTCell().copy();
                
                // we have to copy and re-create the XSSFCell here because the 
                // elements as otherwise setCArray below invalidates all the columns!
                // see Bug 56170, XMLBeans seems to always release previous objects
                // in the CArray, so we need to provide completely new ones here!
                //_cells.put(entry.getKey(), new XSSFCell(this, cArray[i]));
                xssfCell.setCTCell(cArray[i]);
                i++;
            }

            _row.setCArray(cArray);
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
        int rownum = getRowNum() + n;
        CalculationChain calcChain = _sheet.getWorkbook().getCalculationChain();
        int sheetId = (int)_sheet.sheet.getSheetId();
        String msg = "Row[rownum="+getRowNum()+"] contains cell(s) included in a multi-cell array formula. " +
                "You cannot change part of an array.";
        for(Cell c : this){
            XSSFCell cell = (XSSFCell)c;
            if(cell.isPartOfArrayFormulaGroup()){
                cell.notifyArrayFormulaChanging(msg);
            }

            //remove the reference in the calculation chain
            if(calcChain != null) calcChain.removeItem(sheetId, cell.getReference());

            CTCell ctCell = cell.getCTCell();
            String r = new CellReference(rownum, cell.getColumnIndex()).formatAsString();
            ctCell.setR(r);
        }
        setRowNum(rownum);
    }
    
    /**
     * Copy the cells from srcRow to this row
     * If this row is not a blank row, this will merge the two rows, overwriting
     * the cells in this row with the cells in srcRow
     * If srcRow is null, overwrite cells in destination row with blank values, styles, etc per cell copy policy
     * srcRow may be from a different sheet in the same workbook
     * @param srcRow the rows to copy from
     * @param policy the policy to determine what gets copied
     */
    @Beta
    public void copyRowFrom(Row srcRow, CellCopyPolicy policy) {
        if (srcRow == null) {
            // srcRow is blank. Overwrite cells with blank values, blank styles, etc per cell copy policy
            for (Cell destCell : this) {
                final XSSFCell srcCell = null;
                // FIXME: remove type casting when copyCellFrom(Cell, CellCopyPolicy) is added to Cell interface
                ((XSSFCell)destCell).copyCellFrom(srcCell, policy);
            }

            if (policy.isCopyMergedRegions()) {
                // Remove MergedRegions in dest row
                final int destRowNum = getRowNum();
                int index = 0;
                final Set<Integer> indices = new HashSet<Integer>();
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

        }
        else {
            for (final Cell c : srcRow){
                final XSSFCell srcCell = (XSSFCell)c;
                final XSSFCell destCell = createCell(srcCell.getColumnIndex(), srcCell.getCellTypeEnum());
                destCell.copyCellFrom(srcCell, policy);
            }

            final XSSFRowShifter rowShifter = new XSSFRowShifter(_sheet);
            final int sheetIndex = _sheet.getWorkbook().getSheetIndex(_sheet);
            final String sheetName = _sheet.getWorkbook().getSheetName(sheetIndex);
            final int srcRowNum = srcRow.getRowNum();
            final int destRowNum = getRowNum();
            final int rowDifference = destRowNum - srcRowNum;
            final FormulaShifter shifter = FormulaShifter.createForRowCopy(sheetIndex, sheetName, srcRowNum, srcRowNum, rowDifference, SpreadsheetVersion.EXCEL2007);
            rowShifter.updateRowFormulas(this, shifter);

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
}
