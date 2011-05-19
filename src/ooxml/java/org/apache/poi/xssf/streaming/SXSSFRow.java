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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Streaming version of XSSFRow implementing the "BigGridDemo" strategy.
 *
 * @author Alex Geller, Four J's Development Tools
*/
public class SXSSFRow implements Row
{
    SXSSFSheet _sheet;
    SXSSFCell[] _cells;
    int _maxColumn=-1;
    short _height=-1;
    boolean _zHeight = false;

    public SXSSFRow(SXSSFSheet sheet, int initialSize)
    {
        _sheet=sheet;
        _cells=new SXSSFCell[initialSize];
    }
    public Iterator<Cell> allCellsIterator()
    {
        return new CellIterator();
    }
    public boolean hasCustomHeight()
    {
        return _height!=-1;
    }
//begin of interface implementation
    public Iterator<Cell> iterator()
    {
        return new FilledCellIterator();
    }

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
    public Cell createCell(int column)
    {
        return createCell(column,Cell.CELL_TYPE_BLANK);
    }

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
    public Cell createCell(int column, int type)
    {
        if(column>=_cells.length)
        {
            SXSSFCell[] newCells=new SXSSFCell[Math.max(column+1,_cells.length*2)];
            System.arraycopy(_cells,0,newCells,0,_cells.length);
            _cells=newCells;
        }
        _cells[column]=new SXSSFCell(this,type);
        if(column>_maxColumn) _maxColumn=column;
        return _cells[column];
    }

    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    public void removeCell(Cell cell)
    {
        int index=getCellIndex(cell);
        if(index>=0)
        {
            _cells[index]=null;
            while(_maxColumn>=0&&_cells[_maxColumn]==null) _maxColumn--;
        }
    }

    int getCellIndex(Cell cell)
    {
        for(int i=0;i<=_maxColumn;i++)
        {
            if(_cells[i]==cell) return i;
        }
        return -1;
    }

    /**
     * Set the row number of this row.
     *
     * @param rowNum  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0
     */
    public void setRowNum(int rowNum)
    {
        _sheet.changeRowNum(this,rowNum);
    }

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    public int getRowNum()
    {
        return _sheet.getRowNum(this);
    }

    /**
     * Get the cell representing a given column (logical cell) 0-based.  If you
     * ask for a cell that is not defined....you get a null.
     *
     * @param cellnum  0 based column number
     * @return Cell representing that column or null if undefined.
     * @see #getCell(int, org.apache.poi.ss.usermodel.Row.MissingCellPolicy)
     */
    public Cell getCell(int cellnum) {
        if(cellnum < 0) throw new IllegalArgumentException("Cell index must be >= 0");

        Cell cell = cellnum > _maxColumn ? null : _cells[cellnum];

        MissingCellPolicy policy = _sheet.getWorkbook().getMissingCellPolicy();
        if(policy == RETURN_NULL_AND_BLANK) {
            return cell;
        }
        if (policy == RETURN_BLANK_AS_NULL) {
            if (cell == null) return cell;
            if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                return null;
            }
            return cell;
        }
        if (policy == CREATE_NULL_AS_BLANK) {
            if (cell == null) {
                return createCell((short) cellnum, Cell.CELL_TYPE_BLANK);
            }
            return cell;
        }
        throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
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
    public Cell getCell(int cellnum, MissingCellPolicy policy)
    {
        assert false;
        Cell cell = getCell(cellnum);
        if(policy == RETURN_NULL_AND_BLANK)
        {
            return cell;
        }
        if(policy == RETURN_BLANK_AS_NULL)
        {
            if(cell == null) return cell;
            if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
            {
                return null;
            }
            return cell;
        }
        if(policy == CREATE_NULL_AS_BLANK)
        {
            if(cell == null)
            {
                return createCell(cellnum, Cell.CELL_TYPE_BLANK);
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
    public short getFirstCellNum()
    {
        for(int i=0;i<=_maxColumn;i++)
            if(_cells[i]!=null) return (short)i;
        return -1;
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
    public short getLastCellNum()
    {
        return _maxColumn == -1 ? -1 : (short)(_maxColumn+1);
    }

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    public int getPhysicalNumberOfCells()
    {
        int count=0;
        for(int i=0;i<=_maxColumn;i++)
        {
            if(_cells[i]!=null) count++;
        }
        return count;
    }

    /**
     * Set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     *
     * @param height  rowheight or 0xff for undefined (use sheet default)
     */
    public void setHeight(short height)
    {
        _height=height;
    }

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param zHeight  height is zero or not.
     */
    public void setZeroHeight(boolean zHeight)
    {
        _zHeight=zHeight;
    }

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - zHeight height is zero or not.
     */
    public boolean getZeroHeight()
    {
        return _zHeight;
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    public void setHeightInPoints(float height)
    {
        if(height==-1)
            _height=-1;
        else
            _height=(short)(height*20);
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link Sheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
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
    public float getHeightInPoints()
    {
        return (float)(_height==-1?getSheet().getDefaultRowHeightInPoints():(float)_height/20.0);
    }

    /**
     * @return Cell iterator of the physically defined cells.  Note element 4 may
     * actually be row cell depending on how many are defined!
     */
    public Iterator<Cell> cellIterator()
    {
        return iterator();
    }

    /**
     * Returns the Sheet this row belongs to
     *
     * @return the Sheet that owns this row
     */
    public Sheet getSheet()
    {
        return _sheet;
    }
//end of interface implementation


/** returns all filled cells (created via Row.createCell())*/
    public class FilledCellIterator implements Iterator<Cell>
    {
        int pos=0;

        FilledCellIterator(){
            for (int i = 0; i <= _maxColumn; i++) {
                if (_cells[i] != null) {
                    pos = i;
                    break;
                }
            }
        }

        public boolean hasNext()
        {
            return pos <= _maxColumn;
        }
        void advanceToNext()
        {
            pos++;
            while(pos<=_maxColumn&&_cells[pos]==null) pos++;
        }
        public Cell next() throws NoSuchElementException
        {
            if (hasNext())
            {
                Cell retval=_cells[pos];
                advanceToNext();
                return retval;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
/** returns all cells including empty cells in which case "null" is returned*/
    public class CellIterator implements Iterator<Cell>
    {
        int pos=0;
        public boolean hasNext()
        {
            return pos <= _maxColumn;
        }
        public Cell next() throws NoSuchElementException
        {
            if (hasNext())
                return _cells[pos++];
            else
                throw new NoSuchElementException();
        }
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}

