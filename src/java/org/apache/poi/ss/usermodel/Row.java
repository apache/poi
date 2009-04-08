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

package org.apache.poi.ss.usermodel;

import java.lang.Iterable;
import java.util.Iterator;

/**
 * High level representation of a row of a spreadsheet.
 */
public interface Row extends Iterable<Cell> {

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link Cell#CELL_TYPE_BLANK}. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than the maximum number of supported columns
     * (255 for *.xls, 1048576 for *.xlsx)
     */
    Cell createCell(int column);

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link Cell#CELL_TYPE_BLANK}. The type can be changed
     * either through calling setCellValue or setCellType.
     *
     * @param column - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greate than a maximum number of supported columns
     * (255 for *.xls, 1048576 for *.xlsx)
     */
    Cell createCell(int column, int type);

    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    void removeCell(Cell cell);

    /**
     * Set the row number of this row.
     *
     * @param rowNum  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0
     */
    void setRowNum(int rowNum);

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    int getRowNum();

    /**
     * Get the cell representing a given column (logical cell) 0-based.  If you
     * ask for a cell that is not defined....you get a null.
     *
     * @param cellnum  0 based column number
     * @return Cell representing that column or null if undefined.
     * @see #getCell(int, org.apache.poi.ss.usermodel.Row.MissingCellPolicy)
     */
    Cell getCell(int cellnum);
    
    /**
     * Returns the cell at the given (0 based) index, with the specified {@link org.apache.poi.ss.usermodel.Row.MissingCellPolicy}
     *
     * @return the cell at the given (0 based) index
     * @throws IllegalArgumentException if cellnum < 0 or the specified MissingCellPolicy is invalid
     * @see Row#RETURN_NULL_AND_BLANK
     * @see Row#RETURN_BLANK_AS_NULL
     * @see Row#CREATE_NULL_AS_BLANK
     */
    Cell getCell(int cellnum, MissingCellPolicy policy);

    /**
     * Get the number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    short getFirstCellNum();

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
    short getLastCellNum();

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    int getPhysicalNumberOfCells();

    /**
     * Set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     *
     * @param height  rowheight or 0xff for undefined (use sheet default)
     */
    void setHeight(short height);

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param zHeight  height is zero or not.
     */
    void setZeroHeight(boolean zHeight);

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - zHeight height is zero or not.
     */
    boolean getZeroHeight();

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    void setHeightInPoints(float height);

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link Sheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
    short getHeight();

    /**
     * Returns row height measured in point size. If the height is not set, the default worksheet value is returned,
     * See {@link Sheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in point size
     * @see Sheet#getDefaultRowHeightInPoints()
     */
    float getHeightInPoints();

    /**
     * @return Cell iterator of the physically defined cells.  Note element 4 may
     * actually be row cell depending on how many are defined!
     */
    Iterator<Cell> cellIterator();

    /**
     * Returns the Sheet this row belongs to
     *
     * @return the Sheet that owns this row
     */
    Sheet getSheet();

    /**
     * Used to specify the different possible policies
     *  if for the case of null and blank cells
     */
    public static final class MissingCellPolicy {
    	private static int NEXT_ID = 1;
    	public final int id;
    	private MissingCellPolicy() {
    		this.id = NEXT_ID++;
    	}
    }
    /** Missing cells are returned as null, Blank cells are returned as normal */
    public static final MissingCellPolicy RETURN_NULL_AND_BLANK = new MissingCellPolicy();
    /** Missing cells are returned as null, as are blank cells */
    public static final MissingCellPolicy RETURN_BLANK_AS_NULL = new MissingCellPolicy();
    /** A new, blank cell is created for missing cells. Blank cells are returned as normal */
    public static final MissingCellPolicy CREATE_NULL_AS_BLANK = new MissingCellPolicy();
}
