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

public interface Row extends Iterable<Cell> {

    // used for collections
    public final static int INITIAL_CAPACITY = 5;

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     *
     * @return Cell a high level representation of the created cell.
     */
    Cell createCell(int column);

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     *
     * @return Cell a high level representation of the created cell.
     */
    Cell createCell(short column);

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling setCellValue or setCellType.
     *
     * @param column - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     */
    Cell createCell(int column, int type);

    /**
     * remove the HSSFCell from this row.
     * @param cell to remove
     */
    void removeCell(Cell cell);

    /**
     * set the row number of this row.
     * @param rowNum  the row number (0-based)
     * @throws IndexOutOfBoundsException if the row number is not within the range 0-65535.
     */

    void setRowNum(int rowNum);

    /**
     * get row number this row represents
     * @return the row number (0 based)
     */

    int getRowNum();

    /**
     * get the hssfcell representing a given column (logical cell) 0-based.  If you
     * ask for a cell that is not defined....you get a null.
     *
     * @param cellnum  0 based column number
     * @return Cell representing that column or null if undefined.
     */
    Cell getCell(int cellnum);
    
    /**
     * Get the hssfcell representing a given column (logical cell)
     *  0-based.  If you ask for a cell that is not defined, then
     *  your supplied policy says what to do
     *
     * @param cellnum  0 based column number
     * @param policy Policy on blank / missing cells
     * @return representing that column or null if undefined + policy allows.
     */
    public Cell getCell(int cellnum, MissingCellPolicy policy);

    /**
     * get the number of the first cell contained in this row.
     * @return short representing the first logical cell in the row, or -1 if the row does not contain any cells.
     */
    short getFirstCellNum();

    /**
     * gets the number of the last cell contained in this row <b>PLUS ONE</b>. 
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>, or -1 if the row does not contain any cells.
     */

    short getLastCellNum();

    /**
     * gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     * @return int representing the number of defined cells in the row.
     */

    int getPhysicalNumberOfCells();

    /**
     * set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     * @param height  rowheight or 0xff for undefined (use sheet default)
     */

    void setHeight(short height);

    /**
     * set whether or not to display this row with 0 height
     * @param zHeight  height is zero or not.
     */
    void setZeroHeight(boolean zHeight);

    /**
     * get whether or not to display this row with 0 height
     * @return - zHeight height is zero or not.
     */
    boolean getZeroHeight();

    /**
     * set the row's height in points.
     * @param height  row height in points
     */

    void setHeightInPoints(float height);

    /**
     * get the row's height or ff (-1) for undefined/default-height in twips (1/20th of a point)
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    short getHeight();

    /**
     * get the row's height or ff (-1) for undefined/default-height in points (20*getHeight())
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    float getHeightInPoints();

    /**
     * @return Cell iterator of the physically defined cells.  Note element 4 may
     * actually be row cell depending on how many are defined!
     */
    Iterator<Cell> cellIterator();

	/**
	 * Alias for {@link #cellIterator()} to allow
	 * foreach loops
	 */
	Iterator<Cell> iterator();

    int compareTo(Object obj);

    boolean equals(Object obj);

    
    /**
     * Used to specify the different possible policies
     *  if for the case of null and blank cells
     */
    public static class MissingCellPolicy {
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
