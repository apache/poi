package org.apache.poi.ss.usermodel;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.NotImplemented;

/**
 * A read-only Row view of a sheet column.
 * 
 * Intended to iterate over cells of a sheet.
 *
 * Get this object with Sheet.getColumn(colNum).
 *
 * @author luca vercelli 2018
 */
public class Column implements Row {

    private int colNum;
    private Sheet sheet;

    /**
     * Constructor. Return a read-only Row view of the given sheet column
     * @param sheet
     * @param colNum 0-based
     */
    public Column(Sheet sheet, int colNum) {
        this.sheet = sheet;
        this.colNum = colNum;
    }

    protected static class ColumnIterator implements Iterator<Cell> {

        private int rowNum = 0;
        private int colNum;
        private Sheet sheet;
        private int lastRowNum;

        public ColumnIterator(Sheet sheet, int colNum) {
            this.sheet = sheet;
            this.colNum = colNum;
            this.lastRowNum = sheet.getLastRowNum();
        }

        @Override
        public boolean hasNext() {
            return rowNum < lastRowNum;
        }

        @Override
        public Cell next() {
            try {
                return sheet.getRow(rowNum++).getCell(colNum);
            } catch (NullPointerException e) {
                return null;
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("A column is a read-only object");
        }

    }

    /**
     * Cell iterator
     */
    @Override
    public Iterator<Cell> iterator() {
        return new ColumnIterator(sheet, colNum);
    }

    /**
     * Cell iterator
     */
    @Override
    public Iterator<Cell> cellIterator() {
        return iterator();
    }

    @Override
    public Cell createCell(int colnum) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public Cell createCell(int colnum, CellType cellType) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    /**
     * Get the cell representing a given column (logical cell) 0-based.
     * If you ask for a cell that is not defined... you get a null.
     *
     * @param rowNum 0-based row number
     * @return Cell representing that row or null if undefined.
     */
    @Override
    public Cell getCell(int rowNum) {
        try {
            return sheet.getRow(rowNum).getCell(colNum);
        } catch (NullPointerException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the cell at the given (0 based) index, with the specified Row.MissingCellPolicy.
     *
     * @param rowNum 0-based row number
     * @return Cell representing that row or null if undefined.
     */
    @Override
    public Cell getCell(int rowNum, MissingCellPolicy policy) {
        try {
            return sheet.getRow(rowNum).getCell(colNum, policy);
        } catch (NullPointerException e) {
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Return the index of first non-null cell in this column, or -1 if none exists.
     *
     * Please notiche that the result is a <code>short</code>, so at most 32767 rows are supported.
     * If the first cell is outside this boundary, the function returns -1.
     */
    @Override
    public short getFirstCellNum() {
        int rownum = 0;
        for (Cell cell : this) {
            ++rownum;
            if (rownum == 32768) {
                break;
            }
            if (cell != null) {
                return (short)rownum;
            }
        }
        return -1;
    }
    
    /**
     * Return the index of first non-null cell in this column, or -1 if none exists.
     */
    public int getFirstCellNumInt() {
        int rownum = 0;
        for (Cell cell : this) {
            ++rownum;
            if (cell != null) {
                return rownum;
            }
        }
        return -1;
    }

    @Override
    @NotImplemented
    public short getHeight() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    @NotImplemented
    public float getHeightInPoints() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Return the index of last non-null cell in this column, or -1 if none exists.
     *
     * Please notiche that the result is a <code>short</code>, so at most 32767 rows are supported.
     * If the last cell is outside this boundary, the function returns -1.
     */
    @Override
    public short getLastCellNum() {
        int ret = -1;
        int rownum = 0;
        for (Cell cell : this) {
            ++rownum;
            if (rownum == 32768) {
                break;
            }
            if (cell != null) {
                ret = rownum;
            }
        }
        return (short)ret;
    }

    /**
     * Return the index of last non-null cell in this column, or -1 if none exists.
     */
    public int getLastCellNumInt() {
        int ret = -1;
        int rownum = 0;
        for (Cell cell : this) {
            ++rownum;
            if (cell != null) {
                ret = rownum;
            }
        }
        return ret;
    }
    
    @Override
    @NotImplemented
    public int getOutlineLevel() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    @NotImplemented
    public int getPhysicalNumberOfCells() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public int getRowNum() {
        return colNum;
    }

    /**
     * Returns the whole-column cell styles. Most columns won't have one of these, so will return null.
     * Call isFormatted() to check first.
     */
    @Override
    @NotImplemented
    public CellStyle getRowStyle() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    @Override
    @NotImplemented
    public boolean getZeroHeight() {
        throw new IllegalStateException("Not implemented");
    }

    /**
     * Is this column formatted? Most aren't, but some columns do have whole-column styles.
     * For those that do, you can get the formatting from getRowStyle()
     */
    @Override
    @NotImplemented
    public boolean isFormatted() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void removeCell(Cell cellnum) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void setHeight(short height) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void setHeightInPoints(float heightInPoints) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void setRowNum(int colNum) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void setRowStyle(CellStyle rowStyle) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void setZeroHeight(boolean v) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void shiftCellsLeft(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    @Override
    public void shiftCellsRight(int firstShiftColumnIndex, int lastShiftColumnIndex, int step) {
        throw new UnsupportedOperationException("A column is a read-only object");
    }

    /**
     * True if the given column does really exist in
     * the worksheet. We actually only look at row 0, so the result can be
     * wrong.
     * 
     * @return
     */
    public boolean exists() {
        for (Cell cell : this) {
            if (cell != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the number of columns in the sheet.
     *
     * This implementation is slow.
     */
    public static int numOfColumns(Sheet sheet) {
        int cols = -1;
        for (Row curRow : sheet) {
            cols = Math.max(cols, curRow.getLastCellNum());
        }
        return cols;

    }
}
