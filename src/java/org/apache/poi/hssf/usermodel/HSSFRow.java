/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
    private HashMap cells;
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
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(short)
     */

    //protected HSSFRow(Workbook book, Sheet sheet, short rowNum)
    protected HSSFRow(Workbook book, Sheet sheet, int rowNum)
    {
        this.rowNum = rowNum;
        cells = new HashMap(10);   // new ArrayList(INITIAL_CAPACITY);
        this.book = book;
        this.sheet = sheet;
        row = new RowRecord();
        row.setOptionFlags( (short)0x100 );   // seems necessary for outlining to work.  
        row.setHeight((short) 0xff);
        row.setLastCol((short) -1);
        row.setFirstCol((short) -1);

        // row.setRowNumber(rowNum);
        setRowNum(rowNum);
    }

    /**
     * Creates an HSSFRow from a low level RowRecord object.  Only HSSFSheet should do
     * this.  HSSFSheet uses this when an existing file is read in.
     *
     * @param book low-level Workbook object containing the sheet that contains this row
     * @param sheet low-level Sheet object that contains this Row
     * @param record the low level api object this row should represent
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#createRow(short)
     */

    protected HSSFRow(Workbook book, Sheet sheet, RowRecord record)
    {
        //this.rowNum = rowNum;
        cells = new HashMap();   // ArrayList(INITIAL_CAPACITY);
        this.book = book;
        this.sheet = sheet;
        row = record;

        // row.setHeight(record.getHeight());
        // row.setRowNumber(rowNum);
        setRowNum(record.getRowNumber());

//        addColumns(book, sheet, record);
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
        HSSFCell cell = new HSSFCell(book, sheet, getRowNum(), column);

        addCell(cell);
        sheet.addValueRecord(getRowNum(), cell.getCellValueRecord());
        return cell;
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
     * @deprecated As of 22-Jan-2002 use createCell(short) and use setCellValue to
     * specify the type lazily.
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
        cells.remove(new Integer(cell.getCellNum()));

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
     */

    //public void setRowNum(short rowNum)
    public void setRowNum(int rowNum)
    {
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
        if (row.getFirstCol() == -1)
        {
            row.setFirstCol(cell.getCellNum());
        }
        if (row.getLastCol() == -1)
        {
            row.setLastCol(cell.getCellNum());
        }
        cells.put(new Integer(cell.getCellNum()), cell);

        if (cell.getCellNum() < row.getFirstCol())
        {
            row.setFirstCol(cell.getCellNum());
        }
        if (cell.getCellNum() > row.getLastCol())
        {
            row.setLastCol(cell.getCellNum());
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

/*        for (int k = 0; k < cells.size(); k++)
        {
            HSSFCell cell = ( HSSFCell ) cells.get(k);

            if (cell.getCellNum() == cellnum)
            {
                return cell;
            }
        }*/
        return (HSSFCell) cells.get(new Integer(cellnum));
    }

    /**
     * get the number of the first cell contained in this row.
     * @return short representing the first logical cell in the row
     */

    public short getFirstCellNum()
    {
        if (getPhysicalNumberOfCells() == 0)
            return -1;
        else
            return row.getFirstCol();
    }

    /**
     * get the number of the last cell contained in this row.
     * @return short representing the last logical cell in the row
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
        if (cells == null)
        {
            return 0;   // shouldn't be possible but it is due to missing API support for BLANK/MULBLANK
        }
        return cells.size();
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
        return cells.values().iterator();
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
