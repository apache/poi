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

import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.POIXMLException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;

/**
 * High level representation of a row of a spreadsheet.
 */
public class XSSFRow implements Row, Comparable<XSSFRow> {

    /**
     * The maximum  number of rows in SpreadsheetML
     */
    public static final int MAX_ROW_NUMBER  = 1048576; //2 ^ 20

    /**
     * the xml bean containing all cell definitions for this row
     */
    private final CTRow row;

    /**
     * Cells of this row keyed by their column indexes.
     * The TreeMap ensures that the cells are ordered by columnIndex in the ascending order.
     */
    private final TreeMap<Integer, Cell> cells;

    /**
     * the parent sheet
     */
    private final XSSFSheet sheet;

    /**
     * Construct a XSSFRow.
     *
     * @param row the xml bean containing all cell definitions for this row.
     * @param sheet the parent sheet.
     */
    protected XSSFRow(CTRow row, XSSFSheet sheet) {
        this.row = row;
        this.sheet = sheet;
        this.cells = new TreeMap<Integer, Cell>();
        for (CTCell c : row.getCArray()) {
            XSSFCell cell = new XSSFCell(this, c);
            this.cells.put(cell.getColumnIndex(), cell);
        }
    }

    /**
     * Returns the XSSFSheet this row belongs to
     *
     * @return the XSSFSheet that owns this row
     */
    public XSSFSheet getSheet() {
        return this.sheet;
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
    public Iterator<Cell> cellIterator() {
        return cells.values().iterator();
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
    public Iterator<Cell> iterator() {
    	return cellIterator();
    }

    /**
     * Compares two <code>XSSFRow</code> objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   row   the <code>XSSFRow</code> to be compared.
     * @return	the value <code>0</code> if the row number of this <code>XSSFRow</code> is
     * 		equal to the row number of the argument <code>XSSFRow</code>; a value less than
     * 		<code>0</code> if the row number of this this <code>XSSFRow</code> is numerically less
     * 		than the row number of the argument <code>XSSFRow</code>; and a value greater
     * 		than <code>0</code> if the row number of this this <code>XSSFRow</code> is numerically
     * 		 greater than the row number of the argument <code>XSSFRow</code>.
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    public int compareTo(XSSFRow row) {
        int thisVal = this.getRowNum();
        if(row.getSheet() != getSheet()) throw new IllegalArgumentException("The compared rows must belong to the same XSSFSheet");

        int anotherVal = row.getRowNum();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link Cell#CELL_TYPE_BLANK}. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     * </p>
     * @param columnIndex - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 16384,
     *   the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     */
    public XSSFCell createCell(int columnIndex) {
    	return createCell(columnIndex, Cell.CELL_TYPE_BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     *
     * @param columnIndex - the column number this cell represents
     * @param type - the cell's data type
     * @return XSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if the specified cell type is invalid, columnIndex < 0
     *   or greater than 16384, the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     * @see Cell#CELL_TYPE_BLANK
     * @see Cell#CELL_TYPE_BOOLEAN
     * @see Cell#CELL_TYPE_ERROR
     * @see Cell#CELL_TYPE_FORMULA
     * @see Cell#CELL_TYPE_NUMERIC
     * @see Cell#CELL_TYPE_STRING
     */
    public XSSFCell createCell(int columnIndex, int type) {
        if(columnIndex < 0) throw new IllegalArgumentException("columnIndex must be >= 0, was " + columnIndex);

        CTCell ctcell = CTCell.Factory.newInstance();
        XSSFCell xcell = new XSSFCell(this, ctcell);
        xcell.setCellNum(columnIndex);
        if (type != Cell.CELL_TYPE_BLANK) {
        	xcell.setCellType(type);
        }
        cells.put(columnIndex, xcell);
        return xcell;
    }

    /**
     * Returns the cell at the given (0 based) index,
     *  with the {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy} from the parent Workbook.
     *
     * @return the cell at the given (0 based) index
     */
    public XSSFCell getCell(int cellnum) {
    	return getCell(cellnum, sheet.getWorkbook().getMissingCellPolicy());
    }

    /**
     * Returns the cell at the given (0 based) index, with the specified {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy}
     *
     * @return the cell at the given (0 based) index
     * @throws IllegalArgumentException if cellnum < 0 or the specified MissingCellPolicy is invalid
     * @see Row#RETURN_NULL_AND_BLANK
     * @see Row#RETURN_BLANK_AS_NULL
     * @see Row#CREATE_NULL_AS_BLANK
     */
    public XSSFCell getCell(int cellnum, MissingCellPolicy policy) {
    	if(cellnum < 0) throw new IllegalArgumentException("Cell index must be >= 0");

        XSSFCell cell = (XSSFCell)cells.get(cellnum);
    	if(policy == RETURN_NULL_AND_BLANK) {
    		return cell;
    	}
    	if(policy == RETURN_BLANK_AS_NULL) {
    		if(cell == null) return cell;
    		if(cell.getCellType() == Cell.CELL_TYPE_BLANK) {
    			return null;
    		}
    		return cell;
    	}
    	if(policy == CREATE_NULL_AS_BLANK) {
    		if(cell == null) {
    			return createCell((short)cellnum, Cell.CELL_TYPE_BLANK);
    		}
    		return cell;
    	}
    	throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
    }

    /**
     * Get the number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    public short getFirstCellNum() {
    	return (short)(cells.size() == 0 ? -1 : cells.firstKey());
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
    public short getLastCellNum() {
    	return (short)(cells.size() == 0 ? -1 : (cells.lastKey() + 1));
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link org.apache.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
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
    public float getHeightInPoints() {
    	if (this.row.isSetHt()) {
    		return (float) this.row.getHt();
    	} else {
            return sheet.getDefaultRowHeightInPoints();
        }
    }

    /**
     *  Set the height in "twips" or  1/20th of a point.
     *
     * @param height the height in "twips" or  1/20th of a point. <code>-1</code>  resets to the default height
     */
    public void setHeight(short height) {
        if (height == -1) {
            if (row.isSetHt()) row.unsetHt();
            if (row.isSetCustomHeight()) row.unsetCustomHeight();
        } else {
            row.setHt((double) height / 20);
            row.setCustomHeight(true);

        }
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    public void setHeightInPoints(float height) {
	    setHeight((short)(height == -1 ? -1 : (height*20)));
    }

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    public int getPhysicalNumberOfCells() {
    	return cells.size();
    }

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    public int getRowNum() {
        return (int) (row.getR() - 1);
    }

    /**
     * Set the row number of this row.
     *
     * @param rowNum  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0 or greater than {@link #MAX_ROW_NUMBER}
     */
    public void setRowNum(int rowNum) {
        if(rowNum < 0) throw new IllegalArgumentException("Row number must be >= 0");
        if (rowNum > MAX_ROW_NUMBER)
            throw new IllegalArgumentException("You cannot have more than "+MAX_ROW_NUMBER+" rows ");

        this.row.setR(rowNum + 1);
    }

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - height is zero or not.
     */
    public boolean getZeroHeight() {
    	return this.row.getHidden();
    }

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param height  height is zero or not.
     */
    public void setZeroHeight(boolean height) {
    	this.row.setHidden(height);

    }

    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    public void removeCell(Cell cell) {
    	cells.remove(cell.getColumnIndex());
    }

    /**
     * Returns the underlying CTRow xml bean containing all cell definitions in this row
     *
     * @return the underlying CTRow xml bean
     */
    public CTRow getCTRow(){
    	return row;
    }

    /**
     * Fired when the document is written to an output stream.
     * <p>
     * Attaches CTCell beans to the underlying CTRow bean
     * </p>
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#commit()
     */
    protected void onDocumentWrite(){
        ArrayList<CTCell> cArray = new ArrayList<CTCell>(cells.size());
        //create array of CTCell objects.
        //TreeMap's value iterator ensures that the cells are ordered by columnIndex in the ascending order
        for (Cell cell : cells.values()) {
            XSSFCell c = (XSSFCell)cell;
            cArray.add(c.getCTCell());
        }
        row.setCArray(cArray.toArray(new CTCell[cArray.size()]));
    }

    /**
     * @return formatted xml representation of this row
     */
    @Override
    public String toString(){
        return row.toString();
    }
}
