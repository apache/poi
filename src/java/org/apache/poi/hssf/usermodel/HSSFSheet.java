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
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.aggregates.WorksheetProtectionBlock;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.SpreadsheetVersion;
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
public final class HSSFSheet implements org.apache.poi.ss.usermodel.Sheet {
    private static final POILogger log = POILogFactory.getLogger(HSSFSheet.class);
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * Used for compile-time optimization.  This is the initial size for the collection of
     * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
     * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
     */
    public final static int INITIAL_CAPACITY = 20;

    /**
     * reference to the low level {@link Sheet} object
     */
    private final Sheet _sheet;
    /** stores rows by zero-based row number */
    private final TreeMap<Integer, HSSFRow> _rows;
    protected final Workbook _book;
    protected final HSSFWorkbook _workbook;
    private int _firstrow;
    private int _lastrow;

    /**
     * Creates new HSSFSheet   - called by HSSFWorkbook to create a sheet from
     * scratch.  You should not be calling this from application code (its protected anyhow).
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook) {
        _sheet = Sheet.createSheet();
        _rows = new TreeMap<Integer, HSSFRow>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet - lowlevel Sheet object this sheet will represent
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook, Sheet sheet) {
        this._sheet = sheet;
        _rows = new TreeMap<Integer, HSSFRow>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
        setPropertiesFromSheet(sheet);
    }

    HSSFSheet cloneSheet(HSSFWorkbook workbook) {
      return new HSSFSheet(workbook, _sheet.cloneSheet());
    }

    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    public HSSFWorkbook getWorkbook(){
        return _workbook;
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
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    public HSSFRow createRow(int rownum)
    {
        HSSFRow row = new HSSFRow(_workbook, this, rownum);

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
        HSSFRow hrow = new HSSFRow(_workbook, this, row);

        addRow(hrow, false);
        return hrow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */
    public void removeRow(Row row) {
        HSSFRow hrow = (HSSFRow) row;
        if (row.getSheet() != this) {
            throw new IllegalArgumentException("Specified row does not belong to this sheet");
        }

        if (_rows.size() > 0) {
            Integer key = new Integer(row.getRowNum());
            HSSFRow removedRow = _rows.remove(key);
            if (removedRow != row) {
                //should not happen if the input argument is valid
                throw new IllegalArgumentException("Specified row does not belong to this sheet");
            }
            if (hrow.getRowNum() == getLastRowNum())
            {
                _lastrow = findLastRow(_lastrow);
            }
            if (hrow.getRowNum() == getFirstRowNum())
            {
                _firstrow = findFirstRow(_firstrow);
            }
            _sheet.removeRow(hrow.getRowRecord());
        }
    }

    /**
     * used internally to refresh the "last row" when the last row is removed.
     */
    private int findLastRow(int lastrow) {
        if (lastrow < 1) {
            return 0;
        }
        int rownum = lastrow - 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum > 0) {
            r = getRow(--rownum);
        }
        if (r == null) {
            return 0;
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
            return 0;

        return rownum;
    }

    /**
     * add a row to the sheet
     *
     * @param addLow whether to add the row to the low level model - false if its already there
     */

    private void addRow(HSSFRow row, boolean addLow)
    {
        _rows.put(new Integer(row.getRowNum()), row);
        if (addLow)
        {
            _sheet.addRow(row.getRowRecord());
        }
        boolean firstRow = _rows.size() == 1;
        if (row.getRowNum() > getLastRowNum() || firstRow)
        {
            _lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum() || firstRow)
        {
            _firstrow = row.getRowNum();
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     * @param rowIndex  row to get
     * @return HSSFRow representing the row number or null if its not defined on the sheet
     */
    public HSSFRow getRow(int rowIndex) {
        return _rows.get(new Integer(rowIndex));
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     */
    public int getPhysicalNumberOfRows() {
        return _rows.size();
    }

    /**
     * Gets the first row on the sheet
     * @return the number of the first logical row on the sheet, zero based
     */
    public int getFirstRowNum() {
        return _firstrow;
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
    public int getLastRowNum() {
        return _lastrow;
    }

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    public void addValidationData(HSSFDataValidation dataValidation) {
       if (dataValidation == null) {
           throw new IllegalArgumentException("objValidation must not be null");
       }
       DataValidityTable dvt = _sheet.getOrCreateDataValidityTable();

       DVRecord dvRecord = dataValidation.createDVRecord(this);
       dvt.addDataValidation(dvRecord);
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
        _sheet.setColumnHidden(columnIndex, hidden);
    }

    /**
     * Get the hidden state for a given column.
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    public boolean isColumnHidden(int columnIndex) {
        return _sheet.isColumnHidden(columnIndex);
    }

    /**
     * Set the width (in units of 1/256th of a character width)
     * <p>
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font.
     * </p>
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width > 65536 (the maximum column width in Excel)
     */
    public void setColumnWidth(int columnIndex, int width) {
        _sheet.setColumnWidth(columnIndex, width);
    }

    /**
     * get the width (in units of 1/256th of a character width )
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    public int getColumnWidth(int columnIndex) {
        return _sheet.getColumnWidth(columnIndex);
    }

    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @return default column width
     */
    public int getDefaultColumnWidth() {
        return _sheet.getDefaultColumnWidth();
    }
    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */
    public void setDefaultColumnWidth(int width) {
        _sheet.setDefaultColumnWidth(width);
    }


    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @return  default row height
     */
    public short getDefaultRowHeight() {
        return _sheet.getDefaultRowHeight();
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     * @return  default row height in points
     */

    public float getDefaultRowHeightInPoints()
    {
        return ((float)_sheet.getDefaultRowHeight() / 20);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @param  height default row height
     */

    public void setDefaultRowHeight(short height)
    {
        _sheet.setDefaultRowHeight(height);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */

    public void setDefaultRowHeightInPoints(float height)
    {
        _sheet.setDefaultRowHeight((short) (height * 20));
    }

    /**
     * Returns the HSSFCellStyle that applies to the given
     *  (0 based) column, or null if no style has been
     *  set for that column
     */
    public HSSFCellStyle getColumnStyle(int column) {
        short styleIndex = _sheet.getXFIndexForColAt((short)column);

        if(styleIndex == 0xf) {
            // None set
            return null;
        }

        ExtendedFormatRecord xf = _book.getExFormatAt(styleIndex);
        return new HSSFCellStyle(styleIndex, xf, _book);
    }

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    public boolean isGridsPrinted()
    {
        return _sheet.isGridsPrinted();
    }

    /**
     * set whether gridlines printed.
     * @param value  false if not printed.
     */

    public void setGridsPrinted(boolean value)
    {
        _sheet.setGridsPrinted(value);
    }

    /**
     * @deprecated (Aug-2008) use <tt>CellRangeAddress</tt> instead of <tt>Region</tt>
     */
    public int addMergedRegion(org.apache.poi.ss.util.Region region)
    {
        return _sheet.addMergedRegion( region.getRowFrom(),
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
        region.validate(SpreadsheetVersion.EXCEL97);
        return _sheet.addMergedRegion( region.getFirstRow(),
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
        _sheet.setUncalced(value);
    }
    /**
     * Whether a record must be inserted or not at generation to indicate that
     * formula must be recalculated when workbook is opened.
     * @return true if an uncalced record must be inserted or not at generation
     */
    public boolean getForceFormulaRecalculation()
    {
        return _sheet.getUncalced();
    }


    /**
     * determines whether the output is vertically centered on the page.
     * @param value true to vertically center, false otherwise.
     */

    public void setVerticallyCenter(boolean value)
    {
        _sheet.getPageSettings().getVCenter().setVCenter(value);
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
        return _sheet.getPageSettings().getVCenter().getVCenter();
    }

    /**
     * determines whether the output is horizontally centered on the page.
     * @param value true to horizontally center, false otherwise.
     */

    public void setHorizontallyCenter(boolean value)
    {
        _sheet.getPageSettings().getHCenter().setHCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    public boolean getHorizontallyCenter()
    {

        return _sheet.getPageSettings().getHCenter().getHCenter();
    }



    /**
     * removes a merged region of cells (hence letting them free)
     * @param index of the region to unmerge
     */

    public void removeMergedRegion(int index)
    {
        _sheet.removeMergedRegion(index);
    }

    /**
     * returns the number of merged regions
     * @return number of merged regions
     */

    public int getNumMergedRegions()
    {
        return _sheet.getNumMergedRegions();
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
    public CellRangeAddress getMergedRegion(int index) {
        return _sheet.getMergedRegionAt(index);
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     * Call getRowNum() on each row if you care which one it is.
     */
    public Iterator<Row> rowIterator() {
        @SuppressWarnings("unchecked") // can this clumsy generic syntax be improved?
        Iterator<Row> result = (Iterator<Row>)(Iterator<? extends Row>)_rows.values().iterator();
        return result;
    }
    /**
     * Alias for {@link #rowIterator()} to allow
     *  foreach loops
     */
    public Iterator<Row> iterator() {
        return rowIterator();
    }


    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     * @return Sheet - low level representation of this HSSFSheet.
     */
    Sheet getSheet() {
        return _sheet;
    }

    /**
     * whether alternate expression evaluation is on
     * @param b  alternative expression evaluation or not
     */
    public void setAlternativeExpression(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateExpression(b);
    }

    /**
     * whether alternative formula entry is on
     * @param b  alternative formulas or not
     */
    public void setAlternativeFormula(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateFormula(b);
    }

    /**
     * show automatic page breaks or not
     * @param b  whether to show auto page breaks
     */
    public void setAutobreaks(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAutobreaks(b);
    }

    /**
     * set whether sheet is a dialog sheet or not
     * @param b  isDialog or not
     */
    public void setDialog(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDialog(b);
    }

    /**
     * set whether to display the guts or not
     *
     * @param b  guts or no guts (or glory)
     */
    public void setDisplayGuts(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDisplayGuts(b);
    }

    /**
     * fit to page option is on
     * @param b  fit or not
     */
    public void setFitToPage(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setFitToPage(b);
    }

    /**
     * set if row summaries appear below detail in the outline
     * @param b  below or not
     */
    public void setRowSumsBelow(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsBelow(b);
        //setAlternateExpression must be set in conjuction with setRowSumsBelow
        record.setAlternateExpression(b);
    }

    /**
     * set if col summaries appear right of the detail in the outline
     * @param b  right or not
     */
    public void setRowSumsRight(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsRight(b);
    }

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */
    public boolean getAlternateExpression() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateExpression();
    }

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */
    public boolean getAlternateFormula() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateFormula();
    }

    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */
    public boolean getAutobreaks() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAutobreaks();
    }

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */
    public boolean getDialog() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDialog();
    }

    /**
     * get whether to display the guts or not
     *
     * @return guts or no guts (or glory)
     */
    public boolean getDisplayGuts() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getDisplayGuts();
    }


    /**
     * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     * <p>
     * In Excel 2003 this option can be changed in the Options dialog on the View tab.
     * </p>
     * @return whether all zero values on the worksheet are displayed
     */
    public boolean isDisplayZeros(){
        return _sheet.getWindowTwo().getDisplayZeros();
    }

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     * <p>
     * In Excel 2003 this option can be set in the Options dialog on the View tab.
     * </p>
     * @param value whether to display or hide all zero values on the worksheet
     */
    public void setDisplayZeros(boolean value){
        _sheet.getWindowTwo().setDisplayZeros(value);
    }

    /**
     * fit to page option is on
     * @return fit or not
     */
    public boolean getFitToPage() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getFitToPage();
    }

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */
    public boolean getRowSumsBelow() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getRowSumsBelow();
    }

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */
    public boolean getRowSumsRight() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
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
    public void setPrintGridlines(boolean newPrintGridlines) {
        getSheet().getPrintGridlines().setPrintGridlines(newPrintGridlines);
    }

    /**
     * Gets the print setup object.
     * @return The user model for the print setup object.
     */
    public HSSFPrintSetup getPrintSetup() {
        return new HSSFPrintSetup(_sheet.getPageSettings().getPrintSetup());
    }

    public HSSFHeader getHeader() {
        return new HSSFHeader(_sheet.getPageSettings());
    }

    public HSSFFooter getFooter() {
        return new HSSFFooter(_sheet.getPageSettings());
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
    public void setSelected(boolean sel) {
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
    public void setActive(boolean sel) {
        getSheet().getWindowTwo().setActive(sel);
    }

    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
    public double getMargin(short margin) {
        return _sheet.getPageSettings().getMargin(margin);
    }

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    public void setMargin(short margin, double size) {
        _sheet.getPageSettings().setMargin(margin, size);
    }

    private WorksheetProtectionBlock getProtectionBlock() {
        return _sheet.getProtectionBlock();
    }
    /**
     * Answer whether protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getProtect() {
        return getProtectionBlock().isSheetProtected();
    }

    /**
     * @return hashed password
     */
    public short getPassword() {
        return (short)getProtectionBlock().getPasswordHash();
    }

    /**
     * Answer whether object protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getObjectProtect() {
        return getProtectionBlock().isObjectProtected();
    }

    /**
     * Answer whether scenario protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect() {
        return getProtectionBlock().isScenarioProtected();
    }
    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    public void protectSheet(String password) {
        getProtectionBlock().protectSheet(password, true, true); //protect objs&scenarios(normal)
    }

    /**
     * Sets the zoom magnification for the sheet.  The zoom is expressed as a
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
    public short getTopRow() {
        return _sheet.getTopRow();
    }

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getLeftCol() {
        return _sheet.getLeftCol();
    }

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    public void showInPane(short toprow, short leftcol){
        _sheet.setTopRow(toprow);
        _sheet.setLeftCol(leftcol);
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
        List<CellRangeAddress> shiftedRegions = new ArrayList<CellRangeAddress>();
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
        Iterator<CellRangeAddress> iterator = shiftedRegions.iterator();
        while (iterator.hasNext()) {
            CellRangeAddress region = iterator.next();

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
    public void shiftRows( int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
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
        NoteRecord[] noteRecs;
        if (moveComments) {
            noteRecs = _sheet.getNoteRecords();
        } else {
            noteRecs = NoteRecord.EMPTY_ARRAY;
        }

        shiftMerged(startRow, endRow, n, true);
        _sheet.getPageSettings().shiftRowBreaks(startRow, endRow, n);

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

            // Fix up row heights if required
            if (copyRowHeight) {
                row2Replace.setHeight(row.getHeight());
            }
            if (resetOriginalRowHeight) {
                row.setHeight((short)0xff);
            }

            // Copy each cell from the source row to
            //  the destination row
            for(Iterator<Cell> cells = row.cellIterator(); cells.hasNext(); ) {
                HSSFCell cell = (HSSFCell)cells.next();
                row.removeCell( cell );
                CellValueRecordInterface cellRecord = cell.getCellValueRecord();
                cellRecord.setRow( rowNum + n );
                row2Replace.createCellFromRecord( cellRecord );
                _sheet.addValueRecord( rowNum + n, cellRecord );

                HSSFHyperlink link = cell.getHyperlink();
                if(link != null){
                    link.setFirstRow(link.getFirstRow() + n);
                    link.setLastRow(link.getLastRow() + n);
                }
            }
            // Now zap all the cells in the source row
            row.removeAllCells();

            // Move comments from the source row to the
            //  destination row. Note that comments can
            //  exist for cells which are null
            if(moveComments) {
                // This code would get simpler if NoteRecords could be organised by HSSFRow.
                for(int i=noteRecs.length-1; i>=0; i--) {
                    NoteRecord nr = noteRecs[i];
                    if (nr.getRow() != rowNum) {
                        continue;
                    }
                    HSSFComment comment = getCellComment(rowNum, nr.getColumn());
                    if (comment != null) {
                       comment.setRow(rowNum + n);
                    }
                }
            }
        }
        if ( endRow == _lastrow || endRow + n > _lastrow ) _lastrow = Math.min( endRow + n, SpreadsheetVersion.EXCEL97.getLastRowIndex() );
        if ( startRow == _firstrow || startRow + n < _firstrow ) _firstrow = Math.max( startRow + n, 0 );

        // Update any formulas on this sheet that point to
        //  rows which have been moved
        int sheetIndex = _workbook.getSheetIndex(this);
        short externSheetIndex = _book.checkExternSheet(sheetIndex);
        FormulaShifter shifter = FormulaShifter.createForRowShift(externSheetIndex, startRow, endRow, n);
        _sheet.updateFormulasAfterCellShift(shifter, externSheetIndex);

        int nSheets = _workbook.getNumberOfSheets();
        for(int i=0; i<nSheets; i++) {
            Sheet otherSheet = _workbook.getSheetAt(i).getSheet();
            if (otherSheet == this._sheet) {
                continue;
            }
            short otherExtSheetIx = _book.checkExternSheet(i);
            otherSheet.updateFormulasAfterCellShift(shifter, otherExtSheetIx);
        }
        _workbook.getWorkbook().updateNamesAfterCellShift(shifter);
    }

    protected void insertChartRecords(List<Record> records) {
        int window2Loc = _sheet.findFirstRecordLocBySid(WindowTwoRecord.sid);
        _sheet.getRecords().addAll(window2Loc, records);
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        validateColumn(colSplit);
        validateRow(rowSplit);
        if (leftmostColumn < colSplit) throw new IllegalArgumentException("leftmostColumn parameter must not be less than colSplit parameter");
        if (topRow < rowSplit) throw new IllegalArgumentException("topRow parameter must not be less than leftmostColumn parameter");
        getSheet().createFreezePane( colSplit, rowSplit, topRow, leftmostColumn );
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    public void createFreezePane(int colSplit, int rowSplit) {
        createFreezePane(colSplit, rowSplit, colSplit, rowSplit);
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
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
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
        _sheet.setDisplayGridlines(show);
    }

    /**
     * Returns if gridlines are displayed.
     * @return whether gridlines are displayed
     */
    public boolean isDisplayGridlines() {
    return _sheet.isDisplayGridlines();
    }

    /**
     * Sets whether the formulas are shown in a viewer.
     * @param show whether to show formulas or not
     */
    public void setDisplayFormulas(boolean show) {
        _sheet.setDisplayFormulas(show);
    }

    /**
     * Returns if formulas are displayed.
     * @return whether formulas are displayed
     */
    public boolean isDisplayFormulas() {
        return _sheet.isDisplayFormulas();
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
     * @param show whether to show RowColHeadings or not
     */
    public void setDisplayRowColHeadings(boolean show) {
        _sheet.setDisplayRowColHeadings(show);
    }

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    public boolean isDisplayRowColHeadings() {
        return _sheet.isDisplayRowColHeadings();
    }

    /**
     * Sets a page break at the indicated row
     * @param row FIXME: Document this!
     */
    public void setRowBreak(int row) {
        validateRow(row);
        _sheet.getPageSettings().setRowBreak(row, (short)0, (short)255);
    }

    /**
     * @return <code>true</code> if there is a page break at the indicated row
     */
    public boolean isRowBroken(int row) {
        return _sheet.getPageSettings().isRowBroken(row);
    }

    /**
     * Removes the page break at the indicated row
     */
    public void removeRowBreak(int row) {
        _sheet.getPageSettings().removeRowBreak(row);
    }

    /**
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    public int[] getRowBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getRowBreaks();
    }

    /**
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    public int[] getColumnBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getColumnBreaks();
    }


    /**
     * Sets a page break at the indicated column
     * @param column
     */
    public void setColumnBreak(int column) {
        validateColumn((short)column);
        _sheet.getPageSettings().setColumnBreak((short)column, (short)0, (short) SpreadsheetVersion.EXCEL97.getLastRowIndex());
    }

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    public boolean isColumnBroken(int column) {
        return _sheet.getPageSettings().isColumnBroken(column);
    }

    /**
     * Removes a page break at the indicated column
     * @param column
     */
    public void removeColumnBreak(int column) {
        _sheet.getPageSettings().removeColumnBreak(column);
    }

    /**
     * Runs a bounds check for row numbers
     * @param row
     */
    protected void validateRow(int row) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if (row > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
        if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }

    /**
     * Runs a bounds check for column numbers
     * @param column
     */
    protected void validateColumn(int column) {
        int maxcol = SpreadsheetVersion.EXCEL97.getLastColumnIndex();
        if (column > maxcol) throw new IllegalArgumentException("Maximum column number is " + maxcol);
        if (column < 0)    throw new IllegalArgumentException("Minimum column number is 0");
    }

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
    public void dumpDrawingRecords(boolean fat) {
        _sheet.aggregateDrawingRecords(_book.getDrawingManager(), false);

        EscherAggregate r = (EscherAggregate) getSheet().findFirstRecordBySid(EscherAggregate.sid);
        List<EscherRecord> escherRecords = r.getEscherRecords();
        PrintWriter w = new PrintWriter(System.out);
        for (Iterator<EscherRecord> iterator = escherRecords.iterator(); iterator.hasNext();) {
            EscherRecord escherRecord = iterator.next();
            if (fat) {
                System.out.println(escherRecord.toString());
            } else {
                escherRecord.display(w, 0);
            }
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
    public HSSFPatriarch createDrawingPatriarch() {
        // Create the drawing group if it doesn't already exist.
        _book.createDrawingGroup();

        _sheet.aggregateDrawingRecords(_book.getDrawingManager(), true);
        EscherAggregate agg = (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
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
        _book.findDrawingGroup();

        // If there's now no drawing manager, then there's
        //  no drawing escher records on the workbook
        if(_book.getDrawingManager() == null) {
            return null;
        }

        int found = _sheet.aggregateDrawingRecords(
                _book.getDrawingManager(), false
        );
        if(found == -1) {
            // Workbook has drawing stuff, but this sheet doesn't
            return null;
        }

        // Grab our aggregate record, and wire it up
        EscherAggregate agg = (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
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
        _sheet.setColumnGroupCollapsed(columnNumber, collapsed);
    }

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    public void groupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, true);
    }

    public void ungroupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, false);
    }

    /**
     * Tie a range of cell together so that they can be collapsed or expanded
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    public void groupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, true);
    }

    public void ungroupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, false);
    }

    public void setRowGroupCollapsed(int rowIndex, boolean collapse) {
        if (collapse) {
            _sheet.getRowsAggregate().collapseRow(rowIndex);
        } else {
            _sheet.getRowsAggregate().expandRow(rowIndex);
        }
    }

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
    public void setDefaultColumnStyle(int column, CellStyle style) {
        _sheet.setDefaultColumnStyle(column, ((HSSFCellStyle)style).getIndex());
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
    public void autoSizeColumn(int column) {
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
    public void autoSizeColumn(int column, boolean useMergedCells) {
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

        HSSFWorkbook wb = new HSSFWorkbook(_book);
        HSSFFont defaultFont = wb.getFontAt((short) 0);

        str = new AttributedString("" + defaultChar);
        copyAttributes(defaultFont, str, 0, 1);
        layout = new TextLayout(str.getIterator(), frc);
        int defaultCharWidth = (int)layout.getAdvance();

        double width = -1;
        rows:
        for (Iterator<Row> it = rowIterator(); it.hasNext();) {
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
            int cellType = cell.getCellType();
            if(cellType == HSSFCell.CELL_TYPE_FORMULA) cellType = cell.getCachedFormulaResultType();

            HSSFFont font = wb.getFontAt(style.getFontIndex());

            if (cellType == HSSFCell.CELL_TYPE_STRING) {
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
                if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
                    String dfmt = style.getDataFormatString();
                    String format = dfmt == null ? null : dfmt.replaceAll("\"", "");
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
                } else if (cellType == HSSFCell.CELL_TYPE_BOOLEAN) {
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
            _sheet.setColumnWidth(column, (short) (width));
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
            }
            // No cell, so you will get new
            //  objects every time, sorry...
            return HSSFCell.findCellComment(_sheet, row, column);
        }
        return null;
    }

    public HSSFSheetConditionalFormatting getSheetConditionalFormatting() {
        return new HSSFSheetConditionalFormatting(this);
    }

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    public String getSheetName() {
        HSSFWorkbook wb = getWorkbook();
        int idx = wb.getSheetIndex(this);
        return wb.getSheetName(idx);
    }

}
