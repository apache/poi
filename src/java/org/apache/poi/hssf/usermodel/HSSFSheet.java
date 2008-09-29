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

package org.apache.poi.hssf.usermodel;

import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.PrintWriter;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a worksheet.
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Glen Stampoultzis (glens at apache.org)
 * @author  Libin Roman (romal at vistaportal.com)
 * @author  Shawn Laubach (slaubach at apache dot org) (Just a little)
 * @author  Jean-Pierre Paris (jean-pierre.paris at m4x dot org) (Just a little, too)
 * @author  Yegor Kozlov (yegor at apache.org) (Autosizing columns)
 */

public class HSSFSheet implements org.apache.poi.ss.usermodel.Sheet
{
    private static final int DEBUG = POILogger.DEBUG;

    /* Constants for margins */
    public static final short LeftMargin = Sheet.LeftMargin;
    public static final short RightMargin = Sheet.RightMargin;
    public static final short TopMargin = Sheet.TopMargin;
    public static final short BottomMargin = Sheet.BottomMargin;

    public static final byte PANE_LOWER_RIGHT = (byte)0;
    public static final byte PANE_UPPER_RIGHT = (byte)1;
    public static final byte PANE_LOWER_LEFT = (byte)2;
    public static final byte PANE_UPPER_LEFT = (byte)3;


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
    /** stores <tt>HSSFRow</tt>s by <tt>Integer</tt> (zero-based row number) key */
    private TreeMap rows;
    protected Workbook book;
    protected HSSFWorkbook workbook;
    private int firstrow;
    private int lastrow;
    private static POILogger log = POILogFactory.getLogger(HSSFSheet.class);

    /**
     * Creates new HSSFSheet   - called by HSSFWorkbook to create a sheet from
     * scratch.  You should not be calling this from application code (its protected anyhow).
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(HSSFWorkbook workbook)
    {
        sheet = Sheet.createSheet();
        rows = new TreeMap();
        this.workbook = workbook;
        this.book = workbook.getWorkbook();
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */

    protected HSSFSheet(HSSFWorkbook workbook, Sheet sheet)
    {
        this.sheet = sheet;
        rows = new TreeMap();
        this.workbook = workbook;
        this.book = workbook.getWorkbook();
        setPropertiesFromSheet(sheet);
    }

    HSSFSheet cloneSheet(HSSFWorkbook workbook) {
      return new HSSFSheet(workbook, sheet.cloneSheet());
    }


    /**
     * used internally to set the properties given a Sheet object
     */
    private void setPropertiesFromSheet(Sheet sheet) {

        RowRecord row = sheet.getNextRow();
        boolean rowRecordsAlreadyPresent = row!=null;

        while (row != null) {
            createRowFromRecord(row);

            row = sheet.getNextRow();
        }

        CellValueRecordInterface[] cvals = sheet.getValueRecords();
        long timestart = System.currentTimeMillis();

        if (log.check( POILogger.DEBUG ))
            log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ",
                new Long(timestart));
        HSSFRow lastrow = null;

        // Add every cell to its row
        for (int i = 0; i < cvals.length; i++) {
            CellValueRecordInterface cval = cvals[i];

            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if (hrow == null || hrow.getRowNum() != cval.getRow()) {
                hrow = getRow( cval.getRow() );
                lastrow = hrow;
                if (hrow == null) {
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords 
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
                    if (rowRecordsAlreadyPresent) {
                        // if at least one row record is present, all should be present.
                        throw new RuntimeException("Unexpected missing row when some rows already present");
                    }
                    // create the row record on the fly now.
                    RowRecord rowRec = new RowRecord(cval.getRow());
                    sheet.addRow(rowRec);
                    hrow = createRowFromRecord(rowRec);
                }
            }
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record id = " + Integer.toHexString( ( (Record) cval ).getSid() ) );
            hrow.createCellFromRecord( cval );
            if (log.check( POILogger.DEBUG ))
                log.log( DEBUG, "record took ",
                    new Long( System.currentTimeMillis() - cellstart ) );
            
        }
        if (log.check( POILogger.DEBUG ))
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
    public HSSFRow createRow(int rownum)
    {
        HSSFRow row = new HSSFRow(workbook, this, rownum);

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
        HSSFRow hrow = new HSSFRow(workbook, this, row);

        addRow(hrow, false);
        return hrow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */
    public void removeRow(Row row)
    {
        HSSFRow hrow = (HSSFRow) row;
        if (rows.size() > 0)
        {
            Integer key = new Integer(row.getRowNum());
            HSSFRow removedRow = (HSSFRow) rows.remove(key);
            if (removedRow != row) {
                if (removedRow != null) {
                    rows.put(key, removedRow);
                }
                throw new RuntimeException("Specified row does not belong to this sheet");
            }
            if (hrow.getRowNum() == getLastRowNum())
            {
                lastrow = findLastRow(lastrow);
            }
            if (hrow.getRowNum() == getFirstRowNum())
            {
                firstrow = findFirstRow(firstrow);
            }
            sheet.removeRow(hrow.getRowRecord());
        }
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */
    private int findLastRow(int lastrow) {
        if (lastrow < 1) {
            return -1;
        }
        int rownum = lastrow - 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum > 0) {
            r = getRow(--rownum);
        }
        if (r == null) {
            return -1;
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
        rows.put(new Integer(row.getRowNum()), row);
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
     * @param rowIndex  row to get
     * @return HSSFRow representing the rownumber or null if its not defined on the sheet
     */
    public HSSFRow getRow(int rowIndex) {
        return (HSSFRow) rows.get(new Integer(rowIndex));
    }

    /**
     * Returns the number of phsyically defined rows (NOT the number of rows in the sheet)
     */

    public int getPhysicalNumberOfRows()
    {
        return rows.size();
    }

    /**
     * Gets the first row on the sheet
     * @return the number of the first logical row on the sheet, zero based
     */
    public int getFirstRowNum()
    {
        return firstrow;
    }

    /**
     * Gets the number last row on the sheet.
     * Owing to idiosyncrasies in the excel file
     *  format, if the result of calling this method
     *  is zero, you can't tell if that means there 
     *  are zero rows on the sheet, or one at
     *  position zero. For that case, additionally
     *  call {@link #getPhysicalNumberOfRows()} to
     *  tell if there is a row at position zero
     *  or not. 
     * @return the number of the last row contained in this sheet, zero based.
     */

    public int getLastRowNum()
    {
        return lastrow;
    }

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    public void addValidationData(HSSFDataValidation dataValidation) {
       if (dataValidation == null) {
           throw new IllegalArgumentException("objValidation must not be null");
       }
       DataValidityTable dvt = sheet.getOrCreateDataValidityTable();

       DVRecord dvRecord = dataValidation.createDVRecord(workbook);
       dvt.addDataValidation(dvRecord);
    }
    
    /**
     * Get the DVRecords objects that are associated to this sheet
     * @return a list of DVRecord instances
     */
    public List getDVRecords() {
        List dvRecords = new ArrayList();
        List records = sheet.getRecords();
        
        for(int index=0; index<records.size(); index++) {
           if(records.get(index) instanceof DVRecord) {
               dvRecords.add(records.get(index));
           }
        }
        return dvRecords;
    }


    /**
     * @deprecated (Sep 2008) use {@link #setColumnHidden(int, boolean)}
     */
    public void setColumnHidden(short columnIndex, boolean hidden) {
        setColumnHidden(columnIndex & 0xFFFF, hidden);
    }

    /**
     * @deprecated (Sep 2008) use {@link #isColumnHidden(int)}
     */
    public boolean isColumnHidden(short columnIndex) {
        return isColumnHidden(columnIndex & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #setColumnWidth(int, int)}
     */
    public void setColumnWidth(short columnIndex, short width) {
        setColumnWidth(columnIndex & 0xFFFF, width & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #getColumnWidth(int)}
     */
    public short getColumnWidth(short columnIndex) {
        return (short)getColumnWidth(columnIndex & 0xFFFF);
    }

    /**
     * @deprecated (Sep 2008) use {@link #setDefaultColumnWidth(int)}
     */
    public void setDefaultColumnWidth(short width) {
        setDefaultColumnWidth(width & 0xFFFF);
    }

    /**
     * Get the visibility state for a given column.
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
    public void setColumnHidden(int columnIndex, boolean hidden) {
        sheet.setColumnHidden(columnIndex, hidden);
    }

    /**
     * Get the hidden state for a given column.
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    public boolean isColumnHidden(int columnIndex) {
        return sheet.isColumnHidden(columnIndex);
    }

    /**
     * set the width (in units of 1/256th of a character width)
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */
    public void setColumnWidth(int columnIndex, int width) {
        sheet.setColumnWidth(columnIndex, width);
    }

    /**
     * get the width (in units of 1/256th of a character width )
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    public int getColumnWidth(int columnIndex) {
        return sheet.getColumnWidth(columnIndex);
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @return default column width
     */
    public int getDefaultColumnWidth() {
        return sheet.getDefaultColumnWidth();
    }
    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */
    public void setDefaultColumnWidth(int width) {
        sheet.setDefaultColumnWidth(width);
    }


    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @return  default row height
     */
    public short getDefaultRowHeight() {
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
     * @deprecated (Aug-2008) use <tt>CellRangeAddress</tt> instead of <tt>Region</tt>
     */
    public int addMergedRegion(org.apache.poi.ss.util.Region region)
    {
        return sheet.addMergedRegion( region.getRowFrom(),
                region.getColumnFrom(),
                //(short) region.getRowTo(),
                region.getRowTo(),
                region.getColumnTo());
    }
    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */
    public int addMergedRegion(CellRangeAddress region)
    {
        return sheet.addMergedRegion( region.getFirstRow(),
                region.getFirstColumn(),
                region.getLastRow(),
                region.getLastColumn());
    }

    /**
     * Whether a record must be inserted or not at generation to indicate that
     * formula must be recalculated when workbook is opened.
     * @param value true if an uncalced record must be inserted or not at generation
     */
    public void setForceFormulaRecalculation(boolean value)
    {
        sheet.setUncalced(value);
    }
    /**
     * Whether a record must be inserted or not at generation to indicate that
     * formula must be recalculated when workbook is opened.
     * @return true if an uncalced record must be inserted or not at generation
     */
    public boolean getForceFormulaRecalculation()
    {
        return sheet.getUncalced();
    }


    /**
     * determines whether the output is vertically centered on the page.
     * @param value true to vertically center, false otherwise.
     */

    public void setVerticallyCenter(boolean value)
    {
        sheet.getPageSettings().getVCenter().setVCenter(value);
    }

    /**
     * TODO: Boolean not needed, remove after next release
     * @deprecated (Mar-2008) use getVerticallyCenter() instead
     */
    public boolean getVerticallyCenter(boolean value) {
        return getVerticallyCenter();
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */
    public boolean getVerticallyCenter()
    {
        return sheet.getPageSettings().getVCenter().getVCenter();
    }

    /**
     * determines whether the output is horizontally centered on the page.
     * @param value true to horizontally center, false otherwise.
     */

    public void setHorizontallyCenter(boolean value)
    {
        sheet.getPageSettings().getHCenter().setHCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    public boolean getHorizontallyCenter()
    {

        return sheet.getPageSettings().getHCenter().getHCenter();
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
     * @deprecated (Aug-2008) use {@link HSSFSheet#getMergedRegion(int)}
     */
    public Region getMergedRegionAt(int index) {
        CellRangeAddress cra = getMergedRegion(index);
        
        return new Region(cra.getFirstRow(), (short)cra.getFirstColumn(), 
                cra.getLastRow(), (short)cra.getLastColumn());
    }
    /**
     * @return the merged region at the specified index
     */
    public org.apache.poi.hssf.util.CellRangeAddress getMergedRegion(int index) {
        return sheet.getMergedRegionAt(index);
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    public Iterator rowIterator()
    {
        return rows.values().iterator();
    }
    /**
     * Alias for {@link #rowIterator()} to allow
     *  foreach loops
     */
    public Iterator iterator() {
        return rowIterator();
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

    /**
     * Returns whether gridlines are printed.
     * @return Gridlines are printed
     */
    public boolean isPrintGridlines() {
        return getSheet().getPrintGridlines().getPrintGridlines();
    }

    /**
     * Turns on or off the printing of gridlines.
     * @param newPrintGridlines boolean to turn on or off the printing of
     * gridlines
     */
    public void setPrintGridlines( boolean newPrintGridlines )
    {
        getSheet().getPrintGridlines().setPrintGridlines( newPrintGridlines );
    }

    /**
     * Gets the print setup object.
     * @return The user model for the print setup object.
     */
    public HSSFPrintSetup getPrintSetup()
    {
        return new HSSFPrintSetup( sheet.getPageSettings().getPrintSetup() );
    }

    /**
     * Gets the user model for the document header.
     * @return The Document header.
     */
    public HSSFHeader getHeader()
    {
        return new HSSFHeader( sheet.getPageSettings().getHeader() );
    }

    /**
     * Gets the user model for the document footer.
     * @return The Document footer.
     */
    public HSSFFooter getFooter()
    {
        return new HSSFFooter( sheet.getPageSettings().getFooter() );
    }

    /**
     * Note - this is not the same as whether the sheet is focused (isActive)
     * @return <code>true</code> if this sheet is currently selected
     */
    public boolean isSelected() {
        return getSheet().getWindowTwo().getSelected();
    }
    /**
     * Sets whether sheet is selected.
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    public void setSelected( boolean sel )
    {
        getSheet().getWindowTwo().setSelected(sel);
    }
    /**
     * @return <code>true</code> if this sheet is currently focused
     */
    public boolean isActive() {
        return getSheet().getWindowTwo().isActive();
    }
    /**
     * Sets whether sheet is selected.
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    public void setActive(boolean sel )
    {
        getSheet().getWindowTwo().setActive(sel);
    }

    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
    public double getMargin( short margin )
    {
        return sheet.getPageSettings().getMargin( margin );
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    public void setMargin( short margin, double size )
    {
        sheet.getPageSettings().setMargin( margin, size );
    }

    /**
     * Answer whether protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getProtect() {
        return getSheet().isProtected()[0];
    }

    /**
     * @return hashed password
     */
    public short getPassword() {
        return getSheet().getPassword().getPassword();
    }

    /**
     * Answer whether object protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getObjectProtect() {
        return getSheet().isProtected()[1];
    }

    /**
     * Answer whether scenario protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect() {
        return getSheet().isProtected()[2];
    }

    /**
     * Sets the protection on enabled or disabled
     * @param protect true => protection enabled; false => protection disabled
     * @deprecated (Jul 2007) use {@link #protectSheet(String)}
     */
    public void setProtect(boolean protect) {
        getSheet().getProtect().setProtect(protect);
    }

    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection
     */
    public void protectSheet(String password) {
            getSheet().protectSheet(password, true, true); //protect objs&scenarios(normal)
    }

    /**
     * Sets the zoom magnication for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator     The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     */
    public void setZoom( int numerator, int denominator)
    {
        if (numerator < 1 || numerator > 65535)
            throw new IllegalArgumentException("Numerator must be greater than 1 and less than 65536");
        if (denominator < 1 || denominator > 65535)
            throw new IllegalArgumentException("Denominator must be greater than 1 and less than 65536");

        SCLRecord sclRecord = new SCLRecord();
        sclRecord.setNumerator((short)numerator);
        sclRecord.setDenominator((short)denominator);
        getSheet().setSCLRecord(sclRecord);
    }

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getTopRow()
    {
        return sheet.getTopRow();
    }

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getLeftCol()
    {
        return sheet.getLeftCol();
    }

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    public void showInPane(short toprow, short leftcol){
        this.sheet.setTopRow(toprow);
        this.sheet.setLeftCol(leftcol);
        }

    /**
     * Shifts the merged regions left or right depending on mode
     * <p>
     * TODO: MODE , this is only row specific
     * @param startRow
     * @param endRow
     * @param n
     * @param isRow
     */
    protected void shiftMerged(int startRow, int endRow, int n, boolean isRow) {
        List shiftedRegions = new ArrayList();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        for (int i = 0; i < getNumMergedRegions(); i++) {
             CellRangeAddress merged = getMergedRegion(i);

             boolean inStart= (merged.getFirstRow() >= startRow || merged.getLastRow() >= startRow);
             boolean inEnd  = (merged.getFirstRow() <= endRow   || merged.getLastRow() <= endRow);

             //don't check if it's not within the shifted area
             if (!inStart || !inEnd) {
                continue;
             }

             //only shift if the region outside the shifted rows is not merged too
             if (!containsCell(merged, startRow-1, 0) && !containsCell(merged, endRow+1, 0)){
                 merged.setFirstRow(merged.getFirstRow()+n);
                 merged.setLastRow(merged.getLastRow()+n);
                 //have to remove/add it back
                 shiftedRegions.add(merged);
                 removeMergedRegion(i);
                 i = i -1; // we have to back up now since we removed one
             }
        }

        //read so it doesn't get shifted again
        Iterator iterator = shiftedRegions.iterator();
        while (iterator.hasNext()) {
            CellRangeAddress region = (CellRangeAddress)iterator.next();

            this.addMergedRegion(region);
        }
    }
    private static boolean containsCell(CellRangeAddress cr, int rowIx, int colIx) {
        if (cr.getFirstRow() <= rowIx && cr.getLastRow() >= rowIx
                && cr.getFirstColumn() <= colIx && cr.getLastColumn() >= colIx)
        {
            return true;
        }
        return false;
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.
     *
     * Calls shiftRows(startRow, endRow, n, false, false);
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     */
    public void shiftRows( int startRow, int endRow, int n ) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    public void shiftRows( int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight)
    {
        shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight, true);
    }
    
    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * TODO Might want to add bounds checking here
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     * @param moveComments whether to move comments at the same time as the cells they are attached to
     */
    public void shiftRows(int startRow, int endRow, int n, 
            boolean copyRowHeight, boolean resetOriginalRowHeight, boolean moveComments) {
        int s, inc;
        if (n < 0) {
            s = startRow;
            inc = 1;
        } else {
            s = endRow;
            inc = -1;
        }

        shiftMerged(startRow, endRow, n, true);
        sheet.getPageSettings().shiftRowBreaks(startRow, endRow, n);

        for ( int rowNum = s; rowNum >= startRow && rowNum <= endRow && rowNum >= 0 && rowNum < 65536; rowNum += inc ) {
            HSSFRow row = getRow( rowNum );
            HSSFRow row2Replace = getRow( rowNum + n );
            if ( row2Replace == null )
                row2Replace = createRow( rowNum + n );

            
            // Remove all the old cells from the row we'll
            //  be writing too, before we start overwriting 
            //  any cells. This avoids issues with cells 
            //  changing type, and records not being correctly
            //  overwritten
            row2Replace.removeAllCells();

            // If this row doesn't exist, nothing needs to
            //  be done for the now empty destination row
            if (row == null) continue; // Nothing to do for this row

            // Fetch the first and last columns of the
            //  row now, so we still have them to hand
            //  once we start removing cells
            short firstCol = row.getFirstCellNum();
            short lastCol = row.getLastCellNum();

            // Fix up row heights if required
            if (copyRowHeight) {
                row2Replace.setHeight(row.getHeight());
            }
            if (resetOriginalRowHeight) {
                row.setHeight((short)0xff);
            }

            // Copy each cell from the source row to
            //  the destination row
            for(Iterator cells = row.cellIterator(); cells.hasNext(); ) {
                HSSFCell cell = (HSSFCell)cells.next();
                row.removeCell( cell );
                CellValueRecordInterface cellRecord = cell.getCellValueRecord();
                cellRecord.setRow( rowNum + n );
                row2Replace.createCellFromRecord( cellRecord );
                sheet.addValueRecord( rowNum + n, cellRecord );
            }
            // Now zap all the cells in the source row
            row.removeAllCells();
            
            // Move comments from the source row to the
            //  destination row. Note that comments can
            //  exist for cells which are null
            if(moveComments) {
                for( short col = firstCol; col <= lastCol; col++ ) {
                    HSSFComment comment = getCellComment(rowNum, col);
                    if (comment != null) {
                       comment.setRow(rowNum + n);
                    }
                }
            }
        }
        if ( endRow == lastrow || endRow + n > lastrow ) lastrow = Math.min( endRow + n, 65535 );
        if ( startRow == firstrow || startRow + n < firstrow ) firstrow = Math.max( startRow + n, 0 );

        // Update any formulas on this sheet that point to
        //  rows which have been moved
        int sheetIndex = workbook.getSheetIndex(this);
        short externSheetIndex = book.checkExternSheet(sheetIndex);
        FormulaShifter shifter = FormulaShifter.createForRowShift(externSheetIndex, startRow, endRow, n);
        sheet.updateFormulasAfterCellShift(shifter, externSheetIndex);

        int nSheets = workbook.getNumberOfSheets();
        for(int i=0; i<nSheets; i++) {
            Sheet otherSheet = workbook.getSheetAt(i).getSheet();
            if (otherSheet == this.sheet) {
                continue;
            }
            short otherExtSheetIx = book.checkExternSheet(i);
            otherSheet.updateFormulasAfterCellShift(shifter, otherExtSheetIx);
        }
        // TODO - adjust formulas in named ranges
    }

    protected void insertChartRecords( List records )
    {
        int window2Loc = sheet.findFirstRecordLocBySid( WindowTwoRecord.sid );
        sheet.getRecords().addAll( window2Loc, records );
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow )
    {
        if (colSplit < 0 || colSplit > 255) throw new IllegalArgumentException("Column must be between 0 and 255");
        if (rowSplit < 0 || rowSplit > 65535) throw new IllegalArgumentException("Row must be between 0 and 65535");
        if (leftmostColumn < colSplit) throw new IllegalArgumentException("leftmostColumn parameter must not be less than colSplit parameter");
        if (topRow < rowSplit) throw new IllegalArgumentException("topRow parameter must not be less than leftmostColumn parameter");
        getSheet().createFreezePane( colSplit, rowSplit, topRow, leftmostColumn );
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    public void createFreezePane( int colSplit, int rowSplit )
    {
        createFreezePane( colSplit, rowSplit, colSplit, rowSplit );
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     *                      PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane )
    {
        getSheet().createSplitPane( xSplitPos, ySplitPos, topRow, leftmostColumn, activePane );
    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     * @return null if no pane configured, or the pane information.
     */
    public PaneInformation getPaneInformation() {
      return getSheet().getPaneInformation();
    }

    /**
     * Sets whether the gridlines are shown in a viewer.
     * @param show whether to show gridlines or not
     */
    public void setDisplayGridlines(boolean show) {
        sheet.setDisplayGridlines(show);
    }

    /**
     * Returns if gridlines are displayed.
     * @return whether gridlines are displayed
     */
    public boolean isDisplayGridlines() {
    return sheet.isDisplayGridlines();
    }

    /**
     * Sets whether the formulas are shown in a viewer.
     * @param show whether to show formulas or not
     */
    public void setDisplayFormulas(boolean show) {
        sheet.setDisplayFormulas(show);
    }

    /**
     * Returns if formulas are displayed.
     * @return whether formulas are displayed
     */
    public boolean isDisplayFormulas() {
        return sheet.isDisplayFormulas();
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
     * @param show whether to show RowColHeadings or not
     */
    public void setDisplayRowColHeadings(boolean show) {
        sheet.setDisplayRowColHeadings(show);
    }

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    public boolean isDisplayRowColHeadings() {
        return sheet.isDisplayRowColHeadings();
    }

    /**
     * Sets a page break at the indicated row
     * @param row FIXME: Document this!
     */
    public void setRowBreak(int row) {
        validateRow(row);
        sheet.getPageSettings().setRowBreak(row, (short)0, (short)255);
    }

    /**
     * @return <code>true</code> if there is a page break at the indicated row
     */
    public boolean isRowBroken(int row) {
        return sheet.getPageSettings().isRowBroken(row);
    }

    /**
     * Removes the page break at the indicated row
     */
    public void removeRowBreak(int row) {
        sheet.getPageSettings().removeRowBreak(row);
    }

    /**
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    public int[] getRowBreaks(){
        //we can probably cache this information, but this should be a sparsely used function
        return sheet.getPageSettings().getRowBreaks();
    }

    /**
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    public int[] getColumnBreaks(){
        //we can probably cache this information, but this should be a sparsely used function
        return sheet.getPageSettings().getColumnBreaks();
    }


    /**
     * Sets a page break at the indicated column
     * @param column
     */
    public void setColumnBreak(short column) {
        validateColumn(column);
        sheet.getPageSettings().setColumnBreak(column, (short)0, (short)65535);
    }

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    public boolean isColumnBroken(short column) {
        return sheet.getPageSettings().isColumnBroken(column);
    }

    /**
     * Removes a page break at the indicated column
     * @param column
     */
    public void removeColumnBreak(short column) {
        sheet.getPageSettings().removeColumnBreak(column);
    }

    /**
     * Runs a bounds check for row numbers
     * @param row
     */
    protected void validateRow(int row) {
        if (row > 65535) throw new IllegalArgumentException("Maximum row number is 65535");
        if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }

    /**
     * Runs a bounds check for column numbers
     * @param column
     */
    protected void validateColumn(short column) {
        if (column > 255) throw new IllegalArgumentException("Maximum column number is 255");
        if (column < 0)    throw new IllegalArgumentException("Minimum column number is 0");
    }

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
    public void dumpDrawingRecords(boolean fat)
    {
        sheet.aggregateDrawingRecords(book.getDrawingManager(), false);

        EscherAggregate r = (EscherAggregate) getSheet().findFirstRecordBySid(EscherAggregate.sid);
        List escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(System.out);
        for ( Iterator iterator = escherRecords.iterator(); iterator.hasNext(); )
        {
            EscherRecord escherRecord = (EscherRecord) iterator.next();
            if (fat)
                System.out.println(escherRecord.toString());
            else
                escherRecord.display(w, 0);
        }
        w.flush();
    }

    /**
     * Creates the top-level drawing patriarch.  This will have
     *  the effect of removing any existing drawings on this
     *  sheet.
     * This may then be used to add graphics or charts
     * @return  The new patriarch.
     */
    public HSSFPatriarch createDrawingPatriarch()
    {
        // Create the drawing group if it doesn't already exist.
        book.createDrawingGroup();

        sheet.aggregateDrawingRecords(book.getDrawingManager(), true);
        EscherAggregate agg = (EscherAggregate) sheet.findFirstRecordBySid(EscherAggregate.sid);
        HSSFPatriarch patriarch = new HSSFPatriarch(this, agg);
        agg.clear();     // Initially the behaviour will be to clear out any existing shapes in the sheet when
                         // creating a new patriarch.
        agg.setPatriarch(patriarch);
        return patriarch;
    }

    /**
     * Returns the agregate escher records for this sheet,
     *  it there is one.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out.
     */
    public EscherAggregate getDrawingEscherAggregate() {
        book.findDrawingGroup();

        // If there's now no drawing manager, then there's
        //  no drawing escher records on the workbook
        if(book.getDrawingManager() == null) {
            return null;
        }

        int found = sheet.aggregateDrawingRecords(
                book.getDrawingManager(), false
        );
        if(found == -1) {
            // Workbook has drawing stuff, but this sheet doesn't
            return null;
        }

        // Grab our aggregate record, and wire it up
        EscherAggregate agg = (EscherAggregate) sheet.findFirstRecordBySid(EscherAggregate.sid);
        return agg;
    }
    
    /**
     * Returns the top-level drawing patriach, if there is
     *  one.
     * This will hold any graphics or charts for the sheet.
     * WARNING - calling this will trigger a parsing of the
     *  associated escher records. Any that aren't supported
     *  (such as charts and complex drawing types) will almost
     *  certainly be lost or corrupted when written out. Only
     *  use this with simple drawings, otherwise call
     *  {@link HSSFSheet#createDrawingPatriarch()} and
     *  start from scratch!
     */
    public HSSFPatriarch getDrawingPatriarch() {
        EscherAggregate agg = getDrawingEscherAggregate();
        if(agg == null) return null;
        
        HSSFPatriarch patriarch = new HSSFPatriarch(this, agg);
        agg.setPatriarch(patriarch);

        // Have it process the records into high level objects
        //  as best it can do (this step may eat anything
        //  that isn't supported, you were warned...)
        agg.convertRecordsToUserModel();

        // Return what we could cope with
        return patriarch;
    }

    /**
     * @deprecated (Sep 2008) use {@link #setColumnGroupCollapsed(int, boolean)}
     */
    public void setColumnGroupCollapsed(short columnNumber, boolean collapsed) {
        setColumnGroupCollapsed(columnNumber & 0xFFFF, collapsed);
    }
    /**
     * @deprecated (Sep 2008) use {@link #groupColumn(int, int)}
     */
    public void groupColumn(short fromColumn, short toColumn) {
        groupColumn(fromColumn & 0xFFFF, toColumn & 0xFFFF);
    }
    /**
     * @deprecated (Sep 2008) use {@link #ungroupColumn(int, int)}
     */
    public void ungroupColumn(short fromColumn, short toColumn) {
        ungroupColumn(fromColumn & 0xFFFF, toColumn & 0xFFFF);
    }

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        sheet.setColumnGroupCollapsed(columnNumber, collapsed);
    }

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    public void groupColumn(int fromColumn, int toColumn) {
        sheet.groupColumnRange(fromColumn, toColumn, true);
    }

    public void ungroupColumn(int fromColumn, int toColumn) {
        sheet.groupColumnRange(fromColumn, toColumn, false);
    }

    public void groupRow(int fromRow, int toRow)
    {
        sheet.groupRowRange( fromRow, toRow, true );
    }

    public void ungroupRow(int fromRow, int toRow)
    {
        sheet.groupRowRange( fromRow, toRow, false );
    }

    public void setRowGroupCollapsed(int rowIndex, boolean collapse) {
        if (collapse) {
            sheet.getRowsAggregate().collapseRow(rowIndex);
        } else {
            sheet.getRowsAggregate().expandRow(rowIndex);
        }
    }

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
    public void setDefaultColumnStyle(short column, CellStyle style) {
        sheet.setDefaultColumnStyle(column, ((HSSFCellStyle)style).getIndex());
    }

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * @param column the column index
     */
    public void autoSizeColumn(short column) {
        autoSizeColumn(column, false);
    }
    
    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * You can specify whether the content of merged cells should be considered or ignored.  
     *  Default is to ignore merged cells.
     *   
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    public void autoSizeColumn(short column, boolean useMergedCells) {
        AttributedString str;
        TextLayout layout;
        /**
         * Excel measures columns in units of 1/256th of a character width
         * but the docs say nothing about what particular character is used.
         * '0' looks to be a good choice.
         */
        char defaultChar = '0';
       
        /**
         * This is the multiple that the font height is scaled by when determining the
         * boundary of rotated text.
         */
        double fontHeightMultiple = 2.0;
       
        FontRenderContext frc = new FontRenderContext(null, true, true);

        HSSFWorkbook wb = new HSSFWorkbook(book);
        HSSFFont defaultFont = wb.getFontAt((short) 0);

        str = new AttributedString("" + defaultChar);
        copyAttributes(defaultFont, str, 0, 1);
        layout = new TextLayout(str.getIterator(), frc);
        int defaultCharWidth = (int)layout.getAdvance();

        double width = -1;
        rows:
        for (Iterator it = rowIterator(); it.hasNext();) {
            HSSFRow row = (HSSFRow) it.next();
            HSSFCell cell = row.getCell(column);

            if (cell == null) {
                continue;
            }

            int colspan = 1;
            for (int i = 0 ; i < getNumMergedRegions(); i++) {
                CellRangeAddress region = getMergedRegion(i);
                if (containsCell(region, row.getRowNum(), column)) {
                    if (!useMergedCells) {
                        // If we're not using merged cells, skip this one and move on to the next. 
                        continue rows;
                    }
                    cell = row.getCell(region.getFirstColumn());
                    colspan = 1 + region.getLastColumn() - region.getFirstColumn();
                }
            }

            HSSFCellStyle style = cell.getCellStyle();
            HSSFFont font = wb.getFontAt(style.getFontIndex());

            if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                HSSFRichTextString rt = cell.getRichStringCellValue();
                String[] lines = rt.getString().split("\\n");
                for (int i = 0; i < lines.length; i++) {
                    String txt = lines[i] + defaultChar;
                    str = new AttributedString(txt);
                    copyAttributes(font, str, 0, txt.length());

                    if (rt.numFormattingRuns() > 0) {
                        for (int j = 0; j < lines[i].length(); j++) {
                            int idx = rt.getFontAtIndex(j);
                            if (idx != 0) {
                                HSSFFont fnt = wb.getFontAt((short) idx);
                                copyAttributes(fnt, str, j, j + 1);
                            }
                        }
                    }

                    layout = new TextLayout(str.getIterator(), frc);
                    if(style.getRotation() != 0){
                        /*
                         * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                         * and then rotate the text before computing the bounds. The scale results in some whitespace around
                         * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                         * is added by the standard Excel autosize.
                         */
                        AffineTransform trans = new AffineTransform();
                        trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                        trans.concatenate(
                        AffineTransform.getScaleInstance(1, fontHeightMultiple)
                        );
                        width = Math.max(width, ((layout.getOutline(trans).getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    } else {
                        width = Math.max(width, ((layout.getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    }
                }
            } else {
                String sval = null;
                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                    String format = style.getDataFormatString().replaceAll("\"", "");
                    double value = cell.getNumericCellValue();
                    try {
                        NumberFormat fmt;
                        if ("General".equals(format))
                            sval = "" + value;
                        else
                        {
                            fmt = new DecimalFormat(format);
                            sval = fmt.format(value);
                        }
                    } catch (Exception e) {
                        sval = "" + value;
                    }
                } else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
                    sval = String.valueOf(cell.getBooleanCellValue());
                }
                if(sval != null) {
                    String txt = sval + defaultChar;
                    str = new AttributedString(txt);
                    copyAttributes(font, str, 0, txt.length());

                    layout = new TextLayout(str.getIterator(), frc);
                    if(style.getRotation() != 0){
                        /*
                         * Transform the text using a scale so that it's height is increased by a multiple of the leading,
                         * and then rotate the text before computing the bounds. The scale results in some whitespace around
                         * the unrotated top and bottom of the text that normally wouldn't be present if unscaled, but
                         * is added by the standard Excel autosize.
                         */
                        AffineTransform trans = new AffineTransform();
                        trans.concatenate(AffineTransform.getRotateInstance(style.getRotation()*2.0*Math.PI/360.0));
                        trans.concatenate(
                        AffineTransform.getScaleInstance(1, fontHeightMultiple)
                        );
                        width = Math.max(width, ((layout.getOutline(trans).getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    } else {
                        width = Math.max(width, ((layout.getBounds().getWidth() / colspan) / defaultCharWidth) + cell.getCellStyle().getIndention());
                    }
                }
            }

        }
        if (width != -1) {
            width *= 256;
            if (width > Short.MAX_VALUE) { //width can be bigger that Short.MAX_VALUE!
                width = Short.MAX_VALUE;
            }
            sheet.setColumnWidth(column, (short) (width));
        }
    }

    /**
     * Copy text attributes from the supplied HSSFFont to Java2D AttributedString
     */
    private void copyAttributes(HSSFFont font, AttributedString str, int startIdx, int endIdx) {
        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
        str.addAttribute(TextAttribute.SIZE, new Float(font.getFontHeightInPoints()));
        if (font.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
        if (font.getItalic() ) str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
        if (font.getUnderline() == HSSFFont.U_SINGLE ) str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, startIdx, endIdx);
    }

    /**
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
     public HSSFComment getCellComment(int row, int column) {
        // Don't call findCellComment directly, otherwise
        //  two calls to this method will result in two
        //  new HSSFComment instances, which is bad
        HSSFRow r = getRow(row);
        if(r != null) {
            HSSFCell c = r.getCell(column);
            if(c != null) {
                return c.getCellComment();
            } else {
                // No cell, so you will get new
                //  objects every time, sorry...
                return HSSFCell.findCellComment(sheet, row, column);
            }
        }
        return null;
    }

    public HSSFSheetConditionalFormatting getSheetConditionalFormatting() {
        return new HSSFSheetConditionalFormatting(workbook, sheet);
    }
}
