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
package org.apache.poi.xssf.streaming.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.NotImplemented;

/**
 * Represents an excel row. Supports only minimal functionality in order to
 * reduce memory consumption.
 *
 */
public class StreamedRow implements Row {
    private List<StreamedCell> cells;
    private int rowNumber;

    public StreamedRow(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * <pre>
     * Used to get cells of a Row
     * </pre>
     * 
     * @return Iterator<Cell>
     */
    public Iterator<StreamedCell> getCellIterator() {
        if (cells == null) {
            cells = new ArrayList<StreamedCell>();
        }
        return cells.iterator();
    }

    /**
     * <pre>
     *  Returns the row number
     * </pre>
     * 
     * @return int
     */
    @Override
    public int getRowNum() {
        return rowNumber;
    }

    @Override
    public void setRowNum(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * <pre>
     * Returns String representation of row.
     * o/p format[Row Number:<row num> --> <col_1>|<col_2>|.....|<col_n>]
     * </pre>
     * 
     * @return String
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(250);
        sb.append("Row Number:").append(rowNumber);
        sb.append(" --> ");
        if (cells != null) {
            for (StreamedCell cell : cells) {
                sb.append(cell.toString());
                sb.append(" | ");
            }
        }

        return sb.toString();
    }

    /*
     * @Override protected void finalize() throws Throwable { super.finalize();
     * if (cells != null) { cells.clear(); cells = null; } }
     */

    /**
     * <pre>
     * Returns the List of Cells contained in the row.
     * </pre>
     * 
     * @return List<StreamedCell>
     */
    public List<StreamedCell> getCells() {
        if (cells == null) {
            cells = new ArrayList<StreamedCell>();
        }
        return cells;
    }

    /**
     * <pre>
     * Will not be supported. Use getCellIterator() instead
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Cell> iterator() {
        throw new UnsupportedOperationException("Operation not supported. Use getCellIterator() instead.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     * 
     */
    @Override
    @NotImplemented
    public Cell createCell(int column) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Cell createCell(int column, int type) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Cell createCell(int column, CellType type) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void removeCell(Cell cell) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     *  Returns the cell on specified cell number
     * </pre>
     * 
     * @return Cell
     */
    @Override
    public Cell getCell(int cellnum) {
        StreamedCell cell = null;

        if (cells != null) {
            cell = cells.get(cellnum);
        }

        return cell;
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    public Cell getCell(int cellnum, MissingCellPolicy policy) {
        /*
         * StreamedCell cell = null;
         * 
         * if(cells != null){ cell = cells.get(cellnum); }
         * 
         * switch(policy){ case RETURN_NULL_AND_BLANK: return cell; case
         * RETURN_BLANK_AS_NULL: boolean isBlank = (cell != null &&
         * cell.getCellTypeEnum() == CellType.BLANK); return (isBlank) ? null :
         * cell; case CREATE_NULL_AS_BLANK: return (cell == null) ?
         * createCell(cellnum, CellType.BLANK) : cell; default: throw new
         * IllegalArgumentException("Illegal policy " policy " (" policy.id
         * ")"); }
         */
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Get the number of the first cell contained in this row.
     * </pre>
     *
     * @return short representing the first logical cell in the row, or -1 if
     *         the row does not contain any cells.
     */
    @Override
    public short getFirstCellNum() {
        short firstCellNumber = -1;

        if (cells != null && cells.size() > 0) {
            firstCellNumber = (short) cells.get(0).getColumnIndex();
        }

        return firstCellNumber;
    }

    /**
     * <pre>
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>.
     * </pre>
     *
     * @return short representing the last logical cell in the row, or -1 if the
     *         row does not contain any cells.
     */
    @Override
    public short getLastCellNum() {
        short lastCellNumber = -1;

        if (cells != null && cells.size() > 0) {
            lastCellNumber = (short) (cells.get((cells.size() - 1)).getColumnIndex() + 1);
        }

        return lastCellNumber;
    }

    /**
     * <pre>
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     * </pre>
     * 
     * @return int representing the number of defined cells in the row.
     */
    @Override
    public int getPhysicalNumberOfCells() {
        if (cells != null) {
            return cells.size();
        } else {
            return 0;
        }
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setHeight(short height) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setZeroHeight(boolean zHeight) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean getZeroHeight() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setHeightInPoints(float height) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public short getHeight() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public float getHeightInPoints() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public boolean isFormatted() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public CellStyle getRowStyle() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

    /**
     * <pre>
     * Not supported right now, as StreamedWorkbook
     * supports only reading.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public void setRowStyle(CellStyle style) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Not supported right now. Use getCellIterator instead
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Iterator<Cell> cellIterator() {
        throw new UnsupportedOperationException("Operation not supported.Use getCellIterator instead.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public Sheet getSheet() {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    /**
     * <pre>
     * Will be supported in future.
     * </pre>
     * 
     * @exception UnsupportedOperationException
     */
    @Override
    @NotImplemented
    public int getOutlineLevel() {
        throw new UnsupportedOperationException("Not implememted yet.");
    }

}