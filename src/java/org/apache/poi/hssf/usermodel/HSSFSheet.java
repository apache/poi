/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * HSSFSheet.java
 *
 * Created on September 30, 2001, 3:40 PM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.VCenterRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * High level representation of a worksheet.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Libin Roman (romal at vistaportal.com)
 */

public class HSSFSheet
{
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * Used for compile-time optimization.  This is the initial size for the collection of
     * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
     * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
     */

    public final static int INITIAL_CAPACITY = 20;

    /**
     * reference to the low level Sheet object
     */

    private Sheet sheet;
    private TreeMap rows;
    private Workbook book;
    private int firstrow;
    private int lastrow;
    private static POILogger log = POILogFactory.getLogger(HSSFSheet.class);

    /**
     * Creates new HSSFSheet   - called by HSSFWorkbook to create a sheet from
     * scratch.  You should not be calling this from application code (its protected anyhow).
     *
     * @param book - lowlevel Workbook object associated with the sheet.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(Workbook book)
    {
        sheet = Sheet.createSheet();
        rows = new TreeMap();   // new ArrayList(INITIAL_CAPACITY);
        this.book = book;
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param book - lowlevel Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(Workbook book, Sheet sheet)
    {
        this.sheet = sheet;
        rows = new TreeMap();
        this.book = book;
        setPropertiesFromSheet(sheet);
    }


    /**
     * used internally to set the properties given a Sheet object
     */

    private void setPropertiesFromSheet(Sheet sheet)
    {
        int sloc = sheet.getLoc();
        RowRecord row = sheet.getNextRow();

        while (row != null)
        {
            createRowFromRecord(row);

            row = sheet.getNextRow();
        }
        sheet.setLoc(sloc);
        CellValueRecordInterface cval = sheet.getNextValueRecord();
        long timestart = System.currentTimeMillis();

        log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ",
                new Long(timestart));
        HSSFRow lastrow = null;

        while (cval != null)
        {
            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if ((lastrow == null) || (lastrow.getRowNum() != cval.getRow()))
            {
                hrow = getRow(cval.getRow());
            }
            lastrow = hrow;
            hrow.createCellFromRecord(cval);
            cval = sheet.getNextValueRecord();
            log.log(DEBUG, "record took ",
                    new Long(System.currentTimeMillis() - cellstart));
        }
        log.log(DEBUG, "total sheet cell creation took ",
                new Long(System.currentTimeMillis() - timestart));
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level HSSFRow object representing a row in the sheet
     * @see org.apache.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(HSSFRow)
     */

    public HSSFRow createRow(short rownum)
    {
        HSSFRow row = new HSSFRow(book, sheet, rownum);

        addRow(row, true);
        return row;
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     * @param row  low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */

    private HSSFRow createRowFromRecord(RowRecord row)
    {
        HSSFRow hrow = new HSSFRow(book, sheet, row);

        addRow(hrow, false);
        return hrow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */

    public void removeRow(HSSFRow row)
    {
        sheet.setLoc(sheet.getDimsLoc());
        if (rows.size() > 0)
        {
            rows.remove(row);
            if (row.getRowNum() == getLastRowNum())
            {
                lastrow = findLastRow(lastrow);
            }
            if (row.getRowNum() == getFirstRowNum())
            {
                firstrow = findFirstRow(firstrow);
            }
            Iterator iter = row.cellIterator();

            while (iter.hasNext())
            {
                HSSFCell cell = (HSSFCell) iter.next();

                sheet.removeValueRecord(row.getRowNum(),
                        cell.getCellValueRecord());
            }
            sheet.removeRow(row.getRowRecord());
        }
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */

    private int findLastRow(int lastrow)
    {
        int rownum = lastrow - 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum >= 0)
        {
            r = getRow(--rownum);
        }
        return rownum;
    }

    /**
     * used internally to refresh the "first row" when the first row is removed.
     */

    private int findFirstRow(int firstrow)
    {
        int rownum = firstrow + 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum <= getLastRowNum())
        {
            r = getRow(++rownum);
        }

        if (rownum > getLastRowNum())
            return -1;

        return rownum;
    }

    /**
     * add a row to the sheet
     *
     * @param addLow whether to add the row to the low level model - false if its already there
     */

    private void addRow(HSSFRow row, boolean addLow)
    {
        rows.put(row, row);
        if (addLow)
        {
            sheet.addRow(row.getRowRecord());
        }
        if (row.getRowNum() > getLastRowNum())
        {
            lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum())
        {
            firstrow = row.getRowNum();
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     * @param rownum  row to get
     * @return HSSFRow representing the rownumber or null if its not defined on the sheet
     */

    public HSSFRow getRow(int rownum)
    {
        HSSFRow row = new HSSFRow();

        row.setRowNum((short) rownum);
        return (HSSFRow) rows.get(row);
    }

    /**
     * Returns the number of phsyically defined rows (NOT the number of rows in the sheet)
     */

    public int getPhysicalNumberOfRows()
    {
        return rows.size();
    }

    /**
     * gets the first row on the sheet
     * @return the number of the first logical row on the sheet
     */

    public int getFirstRowNum()
    {
        return firstrow;
    }

    /**
     * gets the last row on the sheet
     * @return last row contained n this sheet.
     */

    public int getLastRowNum()
    {
        return lastrow;
    }

    /**
     * set the width (in units of 1/256th of a character width)
     * @param column - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */

    public void setColumnWidth(short column, short width)
    {
        sheet.setColumnWidth(column, width);
    }

    /**
     * get the width (in units of 1/256th of a character width )
     * @param column - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */

    public short getColumnWidth(short column)
    {
        return sheet.getColumnWidth(column);
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @return default column width
     */

    public short getDefaultColumnWidth()
    {
        return sheet.getDefaultColumnWidth();
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @return  default row height
     */

    public short getDefaultRowHeight()
    {
        return sheet.getDefaultRowHeight();
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     * @return  default row height in points
     */

    public float getDefaultRowHeightInPoints()
    {
        return (sheet.getDefaultRowHeight() / 20);
    }

    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */

    public void setDefaultColumnWidth(short width)
    {
        sheet.setDefaultColumnWidth(width);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @param  height default row height
     */

    public void setDefaultRowHeight(short height)
    {
        sheet.setDefaultRowHeight(height);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */

    public void setDefaultRowHeightInPoints(float height)
    {
        sheet.setDefaultRowHeight((short) (height * 20));
    }

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    public boolean isGridsPrinted()
    {
        return sheet.isGridsPrinted();
    }

    /**
     * set whether gridlines printed.
     * @param value  false if not printed.
     */

    public void setGridsPrinted(boolean value)
    {
        sheet.setGridsPrinted(value);
    }

    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */

    public int addMergedRegion(Region region)
    {
        return sheet.addMergedRegion((short) region.getRowFrom(),
                region.getColumnFrom(),
                (short) region.getRowTo(),
                region.getColumnTo());
    }

    /**
     * determines whether the output is vertically centered on the page.
     * @param value true to vertically center, false otherwise.
     */

    public void setVerticallyCenter(boolean value)
    {
        VCenterRecord record =
                (VCenterRecord) sheet.findFirstRecordBySid(VCenterRecord.sid);

        record.setVCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */

    public boolean getVerticallyCenter(boolean value)
    {
        VCenterRecord record =
                (VCenterRecord) sheet.findFirstRecordBySid(VCenterRecord.sid);

        return record.getVCenter();
    }

    /**
     * removes a merged region of cells (hence letting them free)
     * @param index of the region to unmerge
     */

    public void removeMergedRegion(int index)
    {
        sheet.removeMergedRegion(index);
    }

    /**
     * returns the number of merged regions
     * @return number of merged regions
     */

    public int getNumMergedRegions()
    {
        return sheet.getNumMergedRegions();
    }

    /**
     * gets the region at a particular index
     * @param index of the region to fetch
     * @return the merged region (simple eh?)
     */

    public Region getMergedRegionAt(int index)
    {
        return new Region(sheet.getMergedRegionAt(index));
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */

    public Iterator rowIterator()
    {
        return rows.values().iterator();
    }

    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     * @return Sheet - low level representation of this HSSFSheet.
     */

    protected Sheet getSheet()
    {
        return sheet;
    }

    /**
     * whether alternate expression evaluation is on
     * @param b  alternative expression evaluation or not
     */

    public void setAlternativeExpression(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateExpression(b);
    }

    /**
     * whether alternative formula entry is on
     * @param b  alternative formulas or not
     */

    public void setAlternativeFormula(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateFormula(b);
    }

    /**
     * show automatic page breaks or not
     * @param b  whether to show auto page breaks
     */

    public void setAutobreaks(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAutobreaks(b);
    }

    /**
     * set whether sheet is a dialog sheet or not
     * @param b  isDialog or not
     */

    public void setDialog(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDialog(b);
    }

    /**
     * set whether to display the guts or not
     *
     * @param b  guts or no guts (or glory)
     */

    public void setDisplayGuts(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDisplayGuts(b);
    }

    /**
     * fit to page option is on
     * @param b  fit or not
     */

    public void setFitToPage(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setFitToPage(b);
    }

    /**
     * set if row summaries appear below detail in the outline
     * @param b  below or not
     */

    public void setRowSumsBelow(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsBelow(b);
    }

    /**
     * set if col summaries appear right of the detail in the outline
     * @param b  right or not
     */

    public void setRowSumsRight(boolean b)
    {
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsRight(b);
    }

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */

    public boolean getAlternateExpression()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateExpression();
    }

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */

    public boolean getAlternateFormula()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateFormula();
    }

    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */

    public boolean getAutobreaks()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAutobreaks();
    }

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */

    public boolean getDialog()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDialog();
    }

    /**
     * get whether to display the guts or not
     *
     * @return guts or no guts (or glory)
     */

    public boolean getDisplayGuts()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDisplayGuts();
    }

    /**
     * fit to page option is on
     * @return fit or not
     */

    public boolean getFitToPage()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getFitToPage();
    }

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */

    public boolean getRowSumsBelow()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getRowSumsBelow();
    }

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */

    public boolean getRowSumsRight()
    {
        return ((WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getRowSumsRight();
    }
}
