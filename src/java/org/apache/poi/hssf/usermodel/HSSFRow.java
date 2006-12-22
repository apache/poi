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

/*
 * HSSFRow.java
 *
 * Created on September 30, 2001, 3:44 PM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.RowRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * High level representation of a row of a spreadsheet.
 *
 * Only rows that have cells should be added to a Sheet.
 * @version 1.0-pre
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class HSSFRow
        implements Comparable
{

    // used for collections
    public final static int INITIAL_CAPACITY = 5;
    //private short rowNum;
    private int rowNum;
    private HSSFCell[] cells=new HSSFCell[INITIAL_CAPACITY];
//    private short firstcell = -1;
//    private short lastcell = -1;

    /**
     * reference to low level representation
     */

    private RowRecord row;

    /**
     * reference to containing low level Workbook
     */

    private Workbook book;

    /**
     * reference to containing Sheet
     */

    private Sheet sheet;

    protected HSSFRow()
    {
    }

    /**
     * Creates new HSSFRow from scratch. Only HSSFSheet should do this.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param rowNum the row number of this row (0 based)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(int)
     */

    //protected HSSFRow(Workbook book, Sheet sheet, short rowNum)
    protected HSSFRow(Workbook book, Sheet sheet, int rowNum)
    {
        this.rowNum = rowNum;
        this.book = book;
        this.sheet = sheet;
        row = new RowRecord();
        row.setOptionFlags( (short)0x100 );   // seems necessary for outlining to work.  
        row.setHeight((short) 0xff);
        row.setLastCol((short) -1);
        row.setFirstCol((short) -1);

        setRowNum(rowNum);
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

    protected HSSFRow(Workbook book, Sheet sheet, RowRecord record)
    {
        this.book = book;
        this.sheet = sheet;
        row = record;

        setRowNum(record.getRowNumber());
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a CELL_TYPE_BLANK. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     *
     * @param column - the column number this cell represents
     *
     * @return HSSFCell a high level representation of the created cell.
     */

    public HSSFCell createCell(short column)
    {
      return this.createCell(column,HSSFCell.CELL_TYPE_BLANK);
    }

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

    public HSSFCell createCell(short column, int type)
    {
        HSSFCell cell = new HSSFCell(book, sheet, getRowNum(), column, type);

        addCell(cell);
        sheet.addValueRecord(getRowNum(), cell.getCellValueRecord());
        return cell;
    }

    /**
     * remove the HSSFCell from this row.
     * @param cell to remove
     */
    public void removeCell(HSSFCell cell)
    {
        CellValueRecordInterface cval = cell.getCellValueRecord();

        sheet.removeValueRecord(getRowNum(), cval);
        short column=cell.getCellNum();
        if(cell!=null && column<cells.length)
        {
          cells[column]=null;
        }

        if (cell.getCellNum() == row.getLastCol())
        {
            row.setLastCol(findLastCell(row.getLastCol()));
        }
        if (cell.getCellNum() == row.getFirstCol())
        {
            row.setFirstCol(findFirstCell(row.getFirstCol()));
        }
    }

    /**
     * create a high level HSSFCell object from an existing low level record.  Should
     * only be called from HSSFSheet or HSSFRow itself.
     * @param cell low level cell to create the high level representation from
     * @return HSSFCell representing the low level record passed in
     */

    protected HSSFCell createCellFromRecord(CellValueRecordInterface cell)
    {
        HSSFCell hcell = new HSSFCell(book, sheet, getRowNum(), cell);

        addCell(hcell);

        // sheet.addValueRecord(getRowNum(),cell.getCellValueRecord());
        return hcell;
    }

    /**
     * set the row number of this row.
     * @param rowNum  the row number (0-based)
     * @throws IndexOutOfBoundsException if the row number is not within the range 0-65535.
     */

    //public void setRowNum(short rowNum)
    public void setRowNum(int rowNum)
    {
        if ((rowNum < 0) || (rowNum > RowRecord.MAX_ROW_NUMBER))
          throw new IndexOutOfBoundsException("Row number must be between 0 and "+RowRecord.MAX_ROW_NUMBER+", was <"+rowNum+">");
        this.rowNum = rowNum;
        if (row != null)
        {
            row.setRowNumber(rowNum);   // used only for KEY comparison (HSSFRow)
        }
    }

    /**
     * get row number this row represents
     * @return the row number (0 based)
     */

    //public short getRowNum()
    public int getRowNum()
    {
        return rowNum;
    }

    /**
     * used internally to add a cell.
     */

    private void addCell(HSSFCell cell)
    {
        short column=cell.getCellNum();
        if (row.getFirstCol() == -1)
        {
            row.setFirstCol(column);
        }
        if (row.getLastCol() == -1)
        {
            row.setLastCol(column);
        }

        if(column>=cells.length)
        {
          HSSFCell[] oldCells=cells;
          int newSize=oldCells.length*2;
          if(newSize<column+1) newSize=column+1;
          cells=new HSSFCell[newSize];
          System.arraycopy(oldCells,0,cells,0,oldCells.length);
        }
        cells[column]=cell;

        if (column < row.getFirstCol())
        {
            row.setFirstCol(column);
        }
        if (column > row.getLastCol())
        {
            row.setLastCol(column);
        }
    }

    /**
     * get the hssfcell representing a given column (logical cell) 0-based.  If you
     * ask for a cell that is not defined....you get a null.
     *
     * @param cellnum  0 based column number
     * @return HSSFCell representing that column or null if undefined.
     */

    public HSSFCell getCell(short cellnum)
    {
      if(cellnum<0||cellnum>=cells.length) return null;
      return cells[cellnum];
    }

    /**
     * get the number of the first cell contained in this row.
     * @return short representing the first logical cell in the row, or -1 if the row does not contain any cells.
     */

    public short getFirstCellNum()
    {
        if (getPhysicalNumberOfCells() == 0)
            return -1;
        else
            return row.getFirstCol();
    }

    /**
     * gets the number of the last cell contained in this row <b>PLUS ONE</b>. 
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>, or -1 if the row does not contain any cells.
     */

    public short getLastCellNum()
    {
        if (getPhysicalNumberOfCells() == 0)
            return -1;
        else
            return row.getLastCol();
    }


    /**
     * gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     * @return int representing the number of defined cells in the row.
     */

    public int getPhysicalNumberOfCells()
    {
      int count=0;
      for(int i=0;i<cells.length;i++)
      {
        if(cells[i]!=null) count++;
      }
      return count;
    }

    /**
     * set the row's height or set to ff (-1) for undefined/default-height.  Set the height in "twips" or
     * 1/20th of a point.
     * @param height  rowheight or 0xff for undefined (use sheet default)
     */

    public void setHeight(short height)
    {

        // row.setOptionFlags(
        row.setBadFontHeight(true);
        row.setHeight(height);
    }

    /**
     * set whether or not to display this row with 0 height
     * @param zHeight  height is zero or not.
     */
    public void setZeroHeight(boolean zHeight) {
        row.setZeroHeight(zHeight);
    }
  
    /**
     * get whether or not to display this row with 0 height
     * @return - zHeight height is zero or not.
     */
    public boolean getZeroHeight() {
        return row.getZeroHeight();
    }

    /**
     * set the row's height in points.
     * @param height  row height in points
     */

    public void setHeightInPoints(float height)
    {

        // row.setOptionFlags(
        row.setBadFontHeight(true);
        row.setHeight((short) (height * 20));
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in twips (1/20th of a point)
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    public short getHeight()
    {
        return row.getHeight();
    }

    /**
     * get the row's height or ff (-1) for undefined/default-height in points (20*getHeight())
     * @return rowheight or 0xff for undefined (use sheet default)
     */

    public float getHeightInPoints()
    {
        return (row.getHeight() / 20);
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
     * used internally to refresh the "last cell" when the last cell is removed.
     */

    private short findLastCell(short lastcell)
    {
        short cellnum = (short) (lastcell - 1);
        HSSFCell r = getCell(cellnum);

        while (r == null && cellnum >= 0)
        {
            r = getCell(--cellnum);
        }
        return cellnum;
    }

    /**
     * used internally to refresh the "first cell" when the first cell is removed.
     */

    private short findFirstCell(short firstcell)
    {
        short cellnum = (short) (firstcell + 1);
        HSSFCell r = getCell(cellnum);

        while (r == null && cellnum <= getLastCellNum())
        {
            r = getCell(++cellnum);
        }
        if (cellnum > getLastCellNum())
            return -1;
        return cellnum;
    }

    /**
     * @return cell iterator of the physically defined cells.  Note element 4 may
     * actually be row cell depending on how many are defined!
     */

    public Iterator cellIterator()
    {
      return new CellIterator();
    }
    
    private class CellIterator implements Iterator
    {
      int thisId=-1;
      int nextId=-1;
      
      public CellIterator()
      {
        findNext();
      }

      public boolean hasNext() {
        return nextId<cells.length;
      }

      public Object next() {
    	  if (!hasNext())
    		  throw new NoSuchElementException("At last element");
        HSSFCell cell=cells[nextId];
        thisId=nextId;
        findNext();
        return cell;
      }

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

    public int compareTo(Object obj)
    {
        HSSFRow loc = (HSSFRow) obj;

        if (this.getRowNum() == loc.getRowNum())
        {
            return 0;
        }
        if (this.getRowNum() < loc.getRowNum())
        {
            return -1;
        }
        if (this.getRowNum() > loc.getRowNum())
        {
            return 1;
        }
        return -1;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof HSSFRow))
        {
            return false;
        }
        HSSFRow loc = (HSSFRow) obj;

        if (this.getRowNum() == loc.getRowNum())
        {
            return true;
        }
        return false;
    }
}
