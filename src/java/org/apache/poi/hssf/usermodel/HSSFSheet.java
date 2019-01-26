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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.AutoFilterInfoRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.DimensionsRecord;
import org.apache.poi.hssf.record.DrawingRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.RowRecord;
import org.apache.poi.hssf.record.SCLRecord;
import org.apache.poi.hssf.record.WSBoolRecord;
import org.apache.poi.hssf.record.WindowTwoRecord;
import org.apache.poi.hssf.record.aggregates.DataValidityTable;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.RecordAggregate.RecordVisitor;
import org.apache.poi.hssf.record.aggregates.WorksheetProtectionBlock;
import org.apache.poi.hssf.usermodel.helpers.HSSFColumnShifter;

import org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.UnionPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.util.SSCellRange;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Configurator;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a worksheet.
 */
public final class HSSFSheet implements org.apache.poi.ss.usermodel.Sheet {
    private static final POILogger log = POILogFactory.getLogger(HSSFSheet.class);
    private static final int DEBUG = POILogger.DEBUG;

    /**
     * width of 1px in columns with default width in units of 1/256 of a character width
     */
    private static final float PX_DEFAULT = 32.00f;
    /**
     * width of 1px in columns with overridden width in units of 1/256 of a character width
     */
    private static final float PX_MODIFIED = 36.56f;

    
    /**
     * Used for compile-time optimization.  This is the initial size for the collection of
     * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
     * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
     */
    public final static int INITIAL_CAPACITY = Configurator.getIntValue("HSSFSheet.RowInitialCapacity", 20);

    /**
     * reference to the low level {@link InternalSheet} object
     */
    private final InternalSheet _sheet;
    /**
     * stores rows by zero-based row number
     */
    private final TreeMap<Integer, HSSFRow> _rows;
    protected final InternalWorkbook _book;
    protected final HSSFWorkbook _workbook;
    private HSSFPatriarch _patriarch;
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
        _sheet = InternalSheet.createSheet();
        _rows = new TreeMap<>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
    }

    /**
     * Creates an HSSFSheet representing the given Sheet object.  Should only be
     * called by HSSFWorkbook when reading in an exisiting file.
     *
     * @param workbook - The HSSF Workbook object associated with the sheet.
     * @param sheet    - lowlevel Sheet object this sheet will represent
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createSheet()
     */
    protected HSSFSheet(HSSFWorkbook workbook, InternalSheet sheet) {
        this._sheet = sheet;
        _rows = new TreeMap<>();
        this._workbook = workbook;
        this._book = workbook.getWorkbook();
        setPropertiesFromSheet(sheet);
    }

    HSSFSheet cloneSheet(HSSFWorkbook workbook) {
        // Aggregate drawing records
        this.getDrawingPatriarch();
        HSSFSheet sheet = new HSSFSheet(workbook, _sheet.cloneSheet());
        int pos = sheet._sheet.findFirstRecordLocBySid(DrawingRecord.sid);
        DrawingRecord dr = (DrawingRecord) sheet._sheet.findFirstRecordBySid(DrawingRecord.sid);
        if (null != dr) {
            sheet._sheet.getRecords().remove(dr);
        }
        if (getDrawingPatriarch() != null) {
            HSSFPatriarch patr = HSSFPatriarch.createPatriarch(this.getDrawingPatriarch(), sheet);
            sheet._sheet.getRecords().add(pos, patr.getBoundAggregate());
            sheet._patriarch = patr;
        }
        return sheet;
    }

    /**
     * check whether the data of sheet can be serialized
     */
    protected void preSerialize(){
        if (_patriarch != null){
            _patriarch.preSerialize();
        }
    }

    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    @Override
    public HSSFWorkbook getWorkbook() {
        return _workbook;
    }

    /**
     * used internally to set the properties given a Sheet object
     */
    private void setPropertiesFromSheet(InternalSheet sheet) {
        RowRecord row = sheet.getNextRow();

        while (row != null) {
            createRowFromRecord(row);

            row = sheet.getNextRow();
        }

        Iterator<CellValueRecordInterface> iter = sheet.getCellValueIterator();
        long timestart = System.currentTimeMillis();

        if (log.check( POILogger.DEBUG )) {
            log.log(DEBUG, "Time at start of cell creating in HSSF sheet = ",
                    Long.valueOf(timestart));
        }
        HSSFRow lastrow = null;

        // Add every cell to its row
        while (iter.hasNext()) {
            CellValueRecordInterface cval = iter.next();

            long cellstart = System.currentTimeMillis();
            HSSFRow hrow = lastrow;

            if (hrow == null || hrow.getRowNum() != cval.getRow()) {
                hrow = getRow(cval.getRow());
                lastrow = hrow;
                if (hrow == null) {
                    /* we removed this check, see bug 47245 for the discussion around this
                    // Some tools (like Perl module Spreadsheet::WriteExcel - bug 41187) skip the RowRecords
                    // Excel, OpenOffice.org and GoogleDocs are all OK with this, so POI should be too.
                    if (rowRecordsAlreadyPresent) {
                        // if at least one row record is present, all should be present.
                        throw new RuntimeException("Unexpected missing row when some rows already present");
                    }*/

                    // create the row record on the fly now.
                    RowRecord rowRec = new RowRecord(cval.getRow());
                    sheet.addRow(rowRec);
                    hrow = createRowFromRecord(rowRec);
                }
            }
            if (log.check( POILogger.DEBUG )) {
                if (cval instanceof Record) {
                log.log( DEBUG, "record id = " + Integer.toHexString( ( (Record) cval ).getSid() ) );
                } else {
                    log.log( DEBUG, "record = " + cval );
                }
            }
            hrow.createCellFromRecord( cval );
            if (log.check( POILogger.DEBUG )) {
                log.log( DEBUG, "record took ",
                    Long.valueOf( System.currentTimeMillis() - cellstart ) );
            }

        }
        if (log.check( POILogger.DEBUG )) {
            log.log(DEBUG, "total sheet cell creation took ",
                Long.valueOf(System.currentTimeMillis() - timestart));
    }
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum row number
     * @return High level HSSFRow object representing a row in the sheet
     * @see org.apache.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(org.apache.poi.ss.usermodel.Row)
     */
    @Override
    public HSSFRow createRow(int rownum) {
        HSSFRow row = new HSSFRow(_workbook, this, rownum);
        // new rows inherit default height from the sheet
        row.setHeight(getDefaultRowHeight());
        row.getRowRecord().setBadFontHeight(false);

        addRow(row, true);
        return row;
    }

    /**
     * Used internally to create a high level Row object from a low level row object.
     * USed when reading an existing file
     *
     * @param row low level record to represent as a high level Row and add to sheet
     * @return HSSFRow high level representation
     */

    private HSSFRow createRowFromRecord(RowRecord row) {
        HSSFRow hrow = new HSSFRow(_workbook, this, row);

        addRow(hrow, false);
        return hrow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row representing a row to remove.
     */
    @Override
    public void removeRow(Row row) {
        HSSFRow hrow = (HSSFRow) row;
        if (row.getSheet() != this) {
            throw new IllegalArgumentException("Specified row does not belong to this sheet");
        }
        for (Cell cell : row) {
            HSSFCell xcell = (HSSFCell) cell;
            if (xcell.isPartOfArrayFormulaGroup()) {
                String msg = "Row[rownum=" + row.getRowNum() + "] contains cell(s) included in a multi-cell array formula. You cannot change part of an array.";
                xcell.tryToDeleteArrayFormula(msg);
            }
        }

        if (_rows.size() > 0) {
            Integer key = Integer.valueOf(row.getRowNum());
            HSSFRow removedRow = _rows.remove(key);
            if (removedRow != row) {
                //should not happen if the input argument is valid
                throw new IllegalArgumentException("Specified row does not belong to this sheet");
            }
            if (hrow.getRowNum() == getLastRowNum()) {
                _lastrow = findLastRow(_lastrow);
            }
            if (hrow.getRowNum() == getFirstRowNum()) {
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

    private int findFirstRow(int firstrow) {
        int rownum = firstrow + 1;
        HSSFRow r = getRow(rownum);

        while (r == null && rownum <= getLastRowNum()) {
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

    private void addRow(HSSFRow row, boolean addLow) {
        _rows.put(Integer.valueOf(row.getRowNum()), row);
        if (addLow) {
            _sheet.addRow(row.getRowRecord());
        }
        boolean firstRow = _rows.size() == 1;
        if (row.getRowNum() > getLastRowNum() || firstRow) {
            _lastrow = row.getRowNum();
        }
        if (row.getRowNum() < getFirstRowNum() || firstRow) {
            _firstrow = row.getRowNum();
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rowIndex row to get
     * @return HSSFRow representing the row number or null if its not defined on the sheet
     */
    @Override
    public HSSFRow getRow(int rowIndex) {
        return _rows.get(Integer.valueOf(rowIndex));
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     */
    @Override
    public int getPhysicalNumberOfRows() {
        return _rows.size();
    }

    /**
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet, zero based
     */
    @Override
    public int getFirstRowNum() {
        return _firstrow;
    }

    /**
     * Gets the number last row on the sheet.
     * Owing to idiosyncrasies in the excel file
     * format, if the result of calling this method
     * is zero, you can't tell if that means there
     * are zero rows on the sheet, or one at
     * position zero. For that case, additionally
     * call {@link #getPhysicalNumberOfRows()} to
     * tell if there is a row at position zero
     * or not.
     *
     * @return the number of the last row contained in this sheet, zero based.
     */
    @Override
    public int getLastRowNum() {
        return _lastrow;
    }

    @Override
    public List<HSSFDataValidation> getDataValidations() {
        DataValidityTable dvt = _sheet.getOrCreateDataValidityTable();
        final List<HSSFDataValidation> hssfValidations = new ArrayList<>();
        RecordVisitor visitor = new RecordVisitor() {
            private HSSFEvaluationWorkbook book = HSSFEvaluationWorkbook.create(getWorkbook());

            @Override
            public void visitRecord(Record r) {
                if (!(r instanceof DVRecord)) {
                    return;
                }
                DVRecord dvRecord = (DVRecord) r;
                CellRangeAddressList regions = dvRecord.getCellRangeAddress().copy();
                DVConstraint constraint = DVConstraint.createDVConstraint(dvRecord, book);
                HSSFDataValidation hssfDataValidation = new HSSFDataValidation(regions, constraint);
                hssfDataValidation.setErrorStyle(dvRecord.getErrorStyle());
                hssfDataValidation.setEmptyCellAllowed(dvRecord.getEmptyCellAllowed());
                hssfDataValidation.setSuppressDropDownArrow(dvRecord.getSuppressDropdownArrow());
                hssfDataValidation.createPromptBox(dvRecord.getPromptTitle(), dvRecord.getPromptText());
                hssfDataValidation.setShowPromptBox(dvRecord.getShowPromptOnCellSelected());
                hssfDataValidation.createErrorBox(dvRecord.getErrorTitle(), dvRecord.getErrorText());
                hssfDataValidation.setShowErrorBox(dvRecord.getShowErrorOnInvalidValue());
                hssfValidations.add(hssfDataValidation);
            }
        };
        dvt.visitContainedRecords(visitor);
        return hssfValidations;
    }

    /**
     * Creates a data validation object
     *
     * @param dataValidation The Data validation object settings
     */
    @Override
    public void addValidationData(DataValidation dataValidation) {
        if (dataValidation == null) {
            throw new IllegalArgumentException("objValidation must not be null");
        }
        HSSFDataValidation hssfDataValidation = (HSSFDataValidation) dataValidation;
        DataValidityTable dvt = _sheet.getOrCreateDataValidityTable();

        DVRecord dvRecord = hssfDataValidation.createDVRecord(this);
        dvt.addDataValidation(dvRecord);
    }

    /**
     * Get the visibility state for a given column.
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden      - the visiblity state of the column
     */
    @Override
    public void setColumnHidden(int columnIndex, boolean hidden) {
        _sheet.setColumnHidden(columnIndex, hidden);
    }

    /**
     * Get the hidden state for a given column.
     *
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    @Override
    public boolean isColumnHidden(int columnIndex) {
        return _sheet.isColumnHidden(columnIndex);
    }

    /**
     * Set the width (in units of 1/256th of a character width)<p>
     * 
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font (first font in the workbook).<p>
     * 
     * Character width is defined as the maximum digit width
     * of the numbers <code>0, 1, 2, ... 9</code> as rendered
     * using the default font (first font in the workbook).<p>
     * 
     * Unless you are using a very special font, the default character is '0' (zero),
     * this is true for Arial (default font font in HSSF) and Calibri (default font in XSSF)<p>
     * 
     * Please note, that the width set by this method includes 4 pixels of margin padding (two on each side),
     * plus 1 pixel padding for the gridlines (Section 3.3.1.12 of the OOXML spec).
     * This results is a slightly less value of visible characters than passed to this method (approx. 1/2 of a character).<p>
     * 
     * To compute the actual number of visible characters,
     * Excel uses the following formula (Section 3.3.1.12 of the OOXML spec):<p>
     * 
     * <code>
     * width = Truncate([{Number of Visible Characters} *
     * {Maximum Digit Width} + {5 pixel padding}]/{Maximum Digit Width}*256)/256
     * </code>
     * <p>Using the Calibri font as an example, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
     * If you set a column width to be eight characters wide, e.g. <code>setColumnWidth(columnIndex, 8*256)</code>,
     * then the actual value of visible characters (the value shown in Excel) is derived from the following equation:
     * <code>
     * Truncate([numChars*7+5]/7*256)/256 = 8;
     * </code>
     * <p>
     * which gives <code>7.29</code>.
     *
     * @param columnIndex - the column to set (0-based)
     * @param width       - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width &gt; 255*256 (the maximum column width in Excel is 255 characters)
     */
    @Override
    public void setColumnWidth(int columnIndex, int width) {
        _sheet.setColumnWidth(columnIndex, width);
    }

    /**
     * get the width (in units of 1/256th of a character width )
     *
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    @Override
    public int getColumnWidth(int columnIndex) {
        return _sheet.getColumnWidth(columnIndex);
    }

    @Override
    public float getColumnWidthInPixels(int column){
        int cw = getColumnWidth(column);
        int def = getDefaultColumnWidth()*256;
        float px = (cw == def ? PX_DEFAULT : PX_MODIFIED);

        return cw/px;
    }
    
    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     *
     * @return default column width
     */
    @Override
    public int getDefaultColumnWidth() {
        return _sheet.getDefaultColumnWidth();
    }

    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     *
     * @param width default column width
     */
    @Override
    public void setDefaultColumnWidth(int width) {
        _sheet.setDefaultColumnWidth(width);
    }


    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @return default row height
     */
    @Override
    public short getDefaultRowHeight() {
        return _sheet.getDefaultRowHeight();
    }

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     *
     * @return default row height in points
     */
    @Override
    public float getDefaultRowHeightInPoints() {
        return ((float) _sheet.getDefaultRowHeight() / 20);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @param height default row height
     */
    @Override
    public void setDefaultRowHeight(short height) {
        _sheet.setDefaultRowHeight(height);
    }

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * points
     *
     * @param height default row height
     */
    @Override
    public void setDefaultRowHeightInPoints(float height) {
        _sheet.setDefaultRowHeight((short) (height * 20));
    }

    /**
     * Returns the HSSFCellStyle that applies to the given
     * (0 based) column, or null if no style has been
     * set for that column
     */
    @Override
    public HSSFCellStyle getColumnStyle(int column) {
        short styleIndex = _sheet.getXFIndexForColAt((short) column);

        if (styleIndex == 0xf) {
            // None set
            return null;
        }

        ExtendedFormatRecord xf = _book.getExFormatAt(styleIndex);
        return new HSSFCellStyle(styleIndex, xf, _book);
    }

    /**
     * get whether gridlines are printed.
     *
     * @return true if printed
     */
    public boolean isGridsPrinted() {
        return _sheet.isGridsPrinted();
    }

    /**
     * set whether gridlines printed.
     *
     * @param value false if not printed.
     */
    public void setGridsPrinted(boolean value) {
        _sheet.setGridsPrinted(value);
    }

    /**
     * Adds a merged region of cells on a sheet.
     *
     * @param region to merge
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if region intersects with an existing region on this sheet
     */
    @Override
    public int addMergedRegion(CellRangeAddress region) {
        return addMergedRegion(region, true);
    }

    /**
     * Adds a merged region of cells (hence those cells form one).
     * Skips validation. It is possible to create overlapping merged regions
     * or create a merged region that intersects a multi-cell array formula
     * with this formula, which may result in a corrupt workbook.
     *
     * To check for merged regions overlapping array formulas or other merged regions
     * after addMergedRegionUnsafe has been called, call {@link #validateMergedRegions()}, which runs in O(n^2) time.
     *
     * @param region to merge
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     */
    @Override
    public int addMergedRegionUnsafe(CellRangeAddress region) {
        return addMergedRegion(region, false);
    }

    /**
     * Verify that merged regions do not intersect multi-cell array formulas and
     * no merged regions intersect another merged region in this sheet.
     *
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if at least one region intersects with another merged region in this sheet
     */
    @Override
    public void validateMergedRegions() {
        checkForMergedRegionsIntersectingArrayFormulas();
        checkForIntersectingMergedRegions();
    }

    /**
     * adds a merged region of cells (hence those cells form one)
     *
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @param validate whether to validate merged region
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     * @throws IllegalStateException if region intersects with an existing merged region
     * or multi-cell array formula on this sheet
     */
    private int addMergedRegion(CellRangeAddress region, boolean validate) {
        if (region.getNumberOfCells() < 2) {
            throw new IllegalArgumentException("Merged region " + region.formatAsString() + " must contain 2 or more cells");
        }
        region.validate(SpreadsheetVersion.EXCEL97);

        if (validate) {
            // throw IllegalStateException if the argument CellRangeAddress intersects with
            // a multi-cell array formula defined in this sheet
            validateArrayFormulas(region);
        
            // Throw IllegalStateException if the argument CellRangeAddress intersects with
            // a merged region already in this sheet
            validateMergedRegions(region);
        }

        return _sheet.addMergedRegion(region.getFirstRow(),
                region.getFirstColumn(),
                region.getLastRow(),
                region.getLastColumn());
    }

    private void validateArrayFormulas(CellRangeAddress region) {
        // FIXME: this may be faster if it looped over array formulas directly rather than looping over each cell in
        // the region and searching if that cell belongs to an array formula
        int firstRow = region.getFirstRow();
        int firstColumn = region.getFirstColumn();
        int lastRow = region.getLastRow();
        int lastColumn = region.getLastColumn();
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            HSSFRow row = getRow(rowIn);
            if (row == null) continue;
            
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                HSSFCell cell = row.getCell(colIn);
                if (cell == null) continue;

                if (cell.isPartOfArrayFormulaGroup()) {
                    CellRangeAddress arrayRange = cell.getArrayFormulaRange();
                    if (arrayRange.getNumberOfCells() > 1 && region.intersects(arrayRange)) {
                        String msg = "The range " + region.formatAsString() + " intersects with a multi-cell array formula. " +
                                "You cannot merge cells of an array.";
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

    }

    /**
     * Verify that none of the merged regions intersect a multi-cell array formula in this sheet
     *
     * @throws IllegalStateException if candidate region intersects an existing array formula in this sheet
     */
    private void checkForMergedRegionsIntersectingArrayFormulas() {
        for (CellRangeAddress region : getMergedRegions()) {
            validateArrayFormulas(region);
        }
    }

    private void validateMergedRegions(CellRangeAddress candidateRegion) {
        for (final CellRangeAddress existingRegion : getMergedRegions()) {
            if (existingRegion.intersects(candidateRegion)) {
                throw new IllegalStateException("Cannot add merged region " + candidateRegion.formatAsString() +
                        " to sheet because it overlaps with an existing merged region (" + existingRegion.formatAsString() + ").");
            }
        }
    }

    /**
     * Verify that no merged regions intersect another merged region in this sheet.
     *
     * @throws IllegalStateException if at least one region intersects with another merged region in this sheet
     */
    private void checkForIntersectingMergedRegions() {
        final List<CellRangeAddress> regions = getMergedRegions();
        final int size = regions.size();
        for (int i=0; i < size; i++) {
            final CellRangeAddress region = regions.get(i);
            for (final CellRangeAddress other : regions.subList(i+1, regions.size())) {
                if (region.intersects(other)) {
                    String msg = "The range " + region.formatAsString() +
                                " intersects with another merged region " +
                                other.formatAsString() + " in this sheet";
                    throw new IllegalStateException(msg);
                }
            }
        }
    }

    /**
     * Control if Excel should be asked to recalculate all formulas on this sheet
     * when the workbook is opened.<p>
     * 
     * Calculating the formula values with {@link org.apache.poi.ss.usermodel.FormulaEvaluator} is the
     * recommended solution, but this may be used for certain cases where
     * evaluation in POI is not possible.<p>
     * 
     * It is recommended to force recalcuation of formulas on workbook level using
     * {@link org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)}
     * to ensure that all cross-worksheet formuals and external dependencies are updated.
     *
     * @param value true if the application will perform a full recalculation of
     *              this worksheet values when the workbook is opened
     * @see org.apache.poi.ss.usermodel.Workbook#setForceFormulaRecalculation(boolean)
     */
    @Override
    public void setForceFormulaRecalculation(boolean value) {
        _sheet.setUncalced(value);
    }

    /**
     * Whether a record must be inserted or not at generation to indicate that
     * formula must be recalculated when workbook is opened.
     *
     * @return true if an uncalced record must be inserted or not at generation
     */
    @Override
    public boolean getForceFormulaRecalculation() {
        return _sheet.getUncalced();
    }


    /**
     * determines whether the output is vertically centered on the page.
     *
     * @param value true to vertically center, false otherwise.
     */
    @Override
    public void setVerticallyCenter(boolean value) {
        _sheet.getPageSettings().getVCenter().setVCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */
    @Override
    public boolean getVerticallyCenter() {
        return _sheet.getPageSettings().getVCenter().getVCenter();
    }

    /**
     * determines whether the output is horizontally centered on the page.
     *
     * @param value true to horizontally center, false otherwise.
     */
    @Override
    public void setHorizontallyCenter(boolean value) {
        _sheet.getPageSettings().getHCenter().setHCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */
    @Override
    public boolean getHorizontallyCenter() {
        return _sheet.getPageSettings().getHCenter().getHCenter();
    }

    /**
     * Sets whether the worksheet is displayed from right to left instead of from left to right.
     *
     * @param value true for right to left, false otherwise.
     */
    @Override
    public void setRightToLeft(boolean value) {
        _sheet.getWindowTwo().setArabic(value);
    }

    /**
     * Whether the text is displayed in right-to-left mode in the window
     *
     * @return whether the text is displayed in right-to-left mode in the window
     */
    @Override
    public boolean isRightToLeft() {
        return _sheet.getWindowTwo().getArabic();
    }

    /**
     * removes a merged region of cells (hence letting them free)
     *
     * @param index of the region to unmerge
     */
    @Override
    public void removeMergedRegion(int index) {
        _sheet.removeMergedRegion(index);
    }
    
    /**
     * Removes a number of merged regions of cells (hence letting them free)
     *
     * @param indices A set of the regions to unmerge
     */
    @Override
    public void removeMergedRegions(Collection<Integer> indices) {
        for (int i : (new TreeSet<>(indices)).descendingSet()) {
            _sheet.removeMergedRegion(i);
        }
    }

    /**
     * returns the number of merged regions
     *
     * @return number of merged regions
     */
    @Override
    public int getNumMergedRegions() {
        return _sheet.getNumMergedRegions();
    }

    /**
     * @return the merged region at the specified index
     */
    @Override
    public CellRangeAddress getMergedRegion(int index) {
        return _sheet.getMergedRegionAt(index);
    }

    /**
     * @return the list of merged regions
     */
    @Override
    public List<CellRangeAddress> getMergedRegions() {
        List<CellRangeAddress> addresses = new ArrayList<>();
        int count = _sheet.getNumMergedRegions();
        for (int i=0; i < count; i++) {
            addresses.add(_sheet.getMergedRegionAt(i));
        }
        return addresses;
    }

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     *         be the third row if say for instance the second row is undefined.
     *         Call getRowNum() on each row if you care which one it is.
     */
    @Override
    public Iterator<Row> rowIterator() {
        @SuppressWarnings("unchecked") // can this clumsy generic syntax be improved?
                Iterator<Row> result = (Iterator<Row>) (Iterator<? extends Row>) _rows.values().iterator();
        return result;
    }

    /**
     * Alias for {@link #rowIterator()} to allow
     * foreach loops
     */
    @Override
    public Iterator<Row> iterator() {
        return rowIterator();
    }


    /**
     * used internally in the API to get the low level Sheet record represented by this
     * Object.
     *
     * @return Sheet - low level representation of this HSSFSheet.
     */
    /*package*/ InternalSheet getSheet() {
        return _sheet;
    }

    /**
     * whether alternate expression evaluation is on
     *
     * @param b alternative expression evaluation or not
     */
    public void setAlternativeExpression(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateExpression(b);
    }

    /**
     * whether alternative formula entry is on
     *
     * @param b alternative formulas or not
     */
    public void setAlternativeFormula(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAlternateFormula(b);
    }

    /**
     * show automatic page breaks or not
     *
     * @param b whether to show auto page breaks
     */
    @Override
    public void setAutobreaks(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setAutobreaks(b);
    }

    /**
     * set whether sheet is a dialog sheet or not
     *
     * @param b isDialog or not
     */
    public void setDialog(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDialog(b);
    }

    /**
     * set whether to display the guts or not
     *
     * @param b guts or no guts (or glory)
     */
    @Override
    public void setDisplayGuts(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setDisplayGuts(b);
    }

    /**
     * fit to page option is on
     *
     * @param b fit or not
     */
    @Override
    public void setFitToPage(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setFitToPage(b);
    }

    /**
     * set if row summaries appear below detail in the outline
     *
     * @param b below or not
     */
    @Override
    public void setRowSumsBelow(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsBelow(b);
        //setAlternateExpression must be set in conjuction with setRowSumsBelow
        record.setAlternateExpression(b);
    }

    /**
     * set if col summaries appear right of the detail in the outline
     *
     * @param b right or not
     */
    @Override
    public void setRowSumsRight(boolean b) {
        WSBoolRecord record =
                (WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid);

        record.setRowSumsRight(b);
    }

    /**
     * whether alternate expression evaluation is on
     *
     * @return alternative expression evaluation or not
     */
    public boolean getAlternateExpression() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateExpression();
    }

    /**
     * whether alternative formula entry is on
     *
     * @return alternative formulas or not
     */
    public boolean getAlternateFormula() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAlternateFormula();
    }

    /**
     * show automatic page breaks or not
     *
     * @return whether to show auto page breaks
     */
    @Override
    public boolean getAutobreaks() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getAutobreaks();
    }

    /**
     * get whether sheet is a dialog sheet or not
     *
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
    @Override
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
     *
     * @return whether all zero values on the worksheet are displayed
     */
    @Override
    public boolean isDisplayZeros() {
        return _sheet.getWindowTwo().getDisplayZeros();
    }

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     * <p>
     * In Excel 2003 this option can be set in the Options dialog on the View tab.
     * </p>
     *
     * @param value whether to display or hide all zero values on the worksheet
     */
    @Override
    public void setDisplayZeros(boolean value) {
        _sheet.getWindowTwo().setDisplayZeros(value);
    }

    /**
     * fit to page option is on
     *
     * @return fit or not
     */
    @Override
    public boolean getFitToPage() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getFitToPage();
    }

    /**
     * get if row summaries appear below detail in the outline
     *
     * @return below or not
     */
    @Override
    public boolean getRowSumsBelow() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getRowSumsBelow();
    }

    /**
     * get if col summaries appear right of the detail in the outline
     *
     * @return right or not
     */
    @Override
    public boolean getRowSumsRight() {
        return ((WSBoolRecord) _sheet.findFirstRecordBySid(WSBoolRecord.sid))
                .getRowSumsRight();
    }

    /**
     * Returns whether gridlines are printed.
     *
     * @return Gridlines are printed
     */
    @Override
    public boolean isPrintGridlines() {
        return getSheet().getPrintGridlines().getPrintGridlines();
    }

    /**
     * Turns on or off the printing of gridlines.
     *
     * @param show boolean to turn on or off the printing of
     *                          gridlines
     */
    @Override
    public void setPrintGridlines(boolean show) {
        getSheet().getPrintGridlines().setPrintGridlines(show);
    }
    
    /**
     * Returns whether row and column headings are printed.
     *
     * @return row and column headings are printed
     */
    @Override
    public boolean isPrintRowAndColumnHeadings() {
        return getSheet().getPrintHeaders().getPrintHeaders();
    }

    /**
     * Turns on or off the printing of row and column headings.
     *
     * @param show boolean to turn on or off the printing of
     *                          row and column headings
     */
    @Override
    public void setPrintRowAndColumnHeadings(boolean show) {
        getSheet().getPrintHeaders().setPrintHeaders(show);
    }

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    @Override
    public HSSFPrintSetup getPrintSetup() {
        return new HSSFPrintSetup(_sheet.getPageSettings().getPrintSetup());
    }

    @Override
    public HSSFHeader getHeader() {
        return new HSSFHeader(_sheet.getPageSettings());
    }

    @Override
    public HSSFFooter getFooter() {
        return new HSSFFooter(_sheet.getPageSettings());
    }

    /**
     * Note - this is not the same as whether the sheet is focused (isActive)
     *
     * @return <code>true</code> if this sheet is currently selected
     */
    @Override
    public boolean isSelected() {
        return getSheet().getWindowTwo().getSelected();
    }

    /**
     * Sets whether sheet is selected.
     *
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    @Override
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
     *
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    public void setActive(boolean sel) {
        getSheet().getWindowTwo().setActive(sel);
    }

    /**
     * Gets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @return the size of the margin
     */
    @Override
    public double getMargin(short margin) {
        switch (margin) {
            case FooterMargin:
                return _sheet.getPageSettings().getPrintSetup().getFooterMargin();
            case HeaderMargin:
                return _sheet.getPageSettings().getPrintSetup().getHeaderMargin();
            default:
                return _sheet.getPageSettings().getMargin(margin);
        }
    }

    /**
     * Sets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @param size   the size of the margin
     */
    @Override
    public void setMargin(short margin, double size) {
        switch (margin) {
            case FooterMargin:
                _sheet.getPageSettings().getPrintSetup().setFooterMargin(size);
                break;
            case HeaderMargin:
                _sheet.getPageSettings().getPrintSetup().setHeaderMargin(size);
                break;
            default:
                _sheet.getPageSettings().setMargin(margin, size);
        }
    }

    private WorksheetProtectionBlock getProtectionBlock() {
        return _sheet.getProtectionBlock();
    }

    /**
     * Answer whether protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    @Override
    public boolean getProtect() {
        return getProtectionBlock().isSheetProtected();
    }

    /**
     * @return hashed password
     */
    public short getPassword() {
        return (short) getProtectionBlock().getPasswordHash();
    }

    /**
     * Answer whether object protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    public boolean getObjectProtect() {
        return getProtectionBlock().isObjectProtected();
    }

    /**
     * Answer whether scenario protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    @Override
    public boolean getScenarioProtect() {
        return getProtectionBlock().isScenarioProtected();
    }

    /**
     * Sets the protection enabled as well as the password
     *
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    @Override
    public void protectSheet(String password) {
        getProtectionBlock().protectSheet(password, true, true); //protect objs&scenarios(normal)
    }

    /**
     * Sets the zoom magnification for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator   The numerator for the zoom magnification.
     * @param denominator The denominator for the zoom magnification.
     * @see #setZoom(int)
     */
    public void setZoom(int numerator, int denominator) {
        if (numerator < 1 || numerator > 65535)
            throw new IllegalArgumentException("Numerator must be greater than 0 and less than 65536");
        if (denominator < 1 || denominator > 65535)
            throw new IllegalArgumentException("Denominator must be greater than 0 and less than 65536");

        SCLRecord sclRecord = new SCLRecord();
        sclRecord.setNumerator((short) numerator);
        sclRecord.setDenominator((short) denominator);
        getSheet().setSCLRecord(sclRecord);
    }
    
    /**
     * Window zoom magnification for current view representing percent values.
     * Valid values range from 10 to 400. Horizontal &amp; Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * ...
     * 100 - 100%
     * ...
     * 400 - 400%
     * </pre>
     *
     * @param scale window zoom magnification
     * @throws IllegalArgumentException if scale is invalid
     */
    @Override
    public void setZoom(int scale) {
        setZoom(scale, 100);
    }

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    @Override
    public short getTopRow() {
        return _sheet.getTopRow();
    }

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    @Override
    public short getLeftCol() {
        return _sheet.getLeftCol();
    }

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     *
     * @param toprow  the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    @Override
    public void showInPane(int toprow, int leftcol) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if (toprow > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
        
        showInPane((short)toprow, (short)leftcol);
    }
    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     *
     * @param toprow  the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    private void showInPane(short toprow, short leftcol) {
        _sheet.setTopRow(toprow);
        _sheet.setLeftCol(leftcol);
    }
    
    /**
     * Shifts, grows, or shrinks the merged regions due to a row shift
     * 
     * @param startRow the start-index of the rows to shift, zero-based
     * @param endRow the end-index of the rows to shift, zero-based
     * @param n how far to shift, negative to shift up
     * @param isRow unused, kept for backwards compatibility
     * @deprecated POI 3.15 beta 2. Use {@link HSSFRowShifter#shiftMergedRegions(int, int, int)}.
     */
    protected void shiftMerged(int startRow, int endRow, int n, boolean isRow) {
        RowShifter rowShifter = new HSSFRowShifter(this);
        rowShifter.shiftMergedRegions(startRow, endRow, n);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.<p>
     * 
     * Calls {@code shiftRows(startRow, endRow, n, false, false);}<p>
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     *
     * @param startRow the row to start shifting
     * @param endRow   the row to end shifting
     * @param n        the number of rows to shift
     */
    @Override
    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around<p>
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted). All merged regions that are
     * completely overlaid by shifting will be deleted.<p>
     * 
     * TODO Might want to add bounds checking here
     *
     * @param startRow               the row to start shifting
     * @param endRow                 the row to end shifting
     * @param n                      the number of rows to shift
     * @param copyRowHeight          whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    @Override
    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight) {
        shiftRows(startRow, endRow, n, copyRowHeight, resetOriginalRowHeight, true);
    }
    
    /**
     * bound a row number between 0 and last row index (65535)
     *
     * @param row the row number
     */
    private static int clip(int row) {
        return Math.min(
                Math.max(0, row),
                SpreadsheetVersion.EXCEL97.getLastRowIndex());
    }

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around<p>
     * 
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).<p>
     * 
     * TODO Might want to add bounds checking here
     *
     * @param startRow               the row to start shifting
     * @param endRow                 the row to end shifting
     * @param n                      the number of rows to shift
     * @param copyRowHeight          whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     * @param moveComments           whether to move comments at the same time as the cells they are attached to
     */
    public void shiftRows(int startRow, int endRow, int n,
                          boolean copyRowHeight, boolean resetOriginalRowHeight, boolean moveComments) {
        int s, inc;
        if (endRow < startRow) {
            throw new IllegalArgumentException("startRow must be less than or equal to endRow. To shift rows up, use n<0.");
        }
        if (n < 0) {
            s = startRow;
            inc = 1;
        } else if (n > 0) {
            s = endRow;
            inc = -1;
        } else {
            // Nothing to do
            return;
        }
        
        final RowShifter rowShifter = new HSSFRowShifter(this);
        
        // Move comments from the source row to the
        //  destination row. Note that comments can
        //  exist for cells which are null
        // If the row shift would shift the comments off the sheet
        // (above the first row or below the last row), this code will shift the
        // comments to the first or last row, rather than moving them out of
        // bounds or deleting them
        if (moveComments) {
            moveCommentsForRowShift(startRow, endRow, n);
        }

        // Shift Merged Regions
        rowShifter.shiftMergedRegions(startRow, endRow, n);
        
        // Shift Row Breaks
        _sheet.getPageSettings().shiftRowBreaks(startRow, endRow, n);
        
        // Delete overwritten hyperlinks
        deleteOverwrittenHyperlinksForRowShift(startRow, endRow, n);

        for (int rowNum = s; rowNum >= startRow && rowNum <= endRow && rowNum >= 0 && rowNum < 65536; rowNum += inc) {
            HSSFRow row = getRow(rowNum);
            // notify all cells in this row that we are going to shift them,
            // it can throw IllegalStateException if the operation is not allowed, for example,
            // if the row contains cells included in a multi-cell array formula
            if (row != null) notifyRowShifting(row);

            HSSFRow row2Replace = getRow(rowNum + n);
            if (row2Replace == null)
                row2Replace = createRow(rowNum + n);


            // Remove all the old cells from the row we'll
            //  be writing to, before we start overwriting
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
                row.setHeight((short) 0xff);
            }

            // Copy each cell from the source row to
            //  the destination row
            for (Iterator<Cell> cells = row.cellIterator(); cells.hasNext(); ) {
                HSSFCell cell = (HSSFCell) cells.next();
                HSSFHyperlink link = cell.getHyperlink();
                row.removeCell(cell);
                CellValueRecordInterface cellRecord = cell.getCellValueRecord();
                cellRecord.setRow(rowNum + n);
                row2Replace.createCellFromRecord(cellRecord);
                _sheet.addValueRecord(rowNum + n, cellRecord);

                if (link != null) {
                    link.setFirstRow(link.getFirstRow() + n);
                    link.setLastRow(link.getLastRow() + n);
                }
            }
            // Now zap all the cells in the source row
            row.removeAllCells();
        }

        // Re-compute the first and last rows of the sheet as needed
        recomputeFirstAndLastRowsForRowShift(startRow, endRow, n);

        int sheetIndex = _workbook.getSheetIndex(this);
        short externSheetIndex = _book.checkExternSheet(sheetIndex);
        String sheetName = _workbook.getSheetName(sheetIndex);
        FormulaShifter formulaShifter = FormulaShifter.createForRowShift(
                externSheetIndex, sheetName, startRow, endRow, n, SpreadsheetVersion.EXCEL97);
        // Update formulas that refer to rows that have been moved
        updateFormulasForShift(formulaShifter);
    }

    private void updateFormulasForShift(FormulaShifter formulaShifter) {
        int sheetIndex = _workbook.getSheetIndex(this);
        short externSheetIndex = _book.checkExternSheet(sheetIndex);
        // update formulas on this sheet that point to rows which have been moved
        _sheet.updateFormulasAfterCellShift(formulaShifter, externSheetIndex);

        // update formulas on other sheets that point to rows that have been moved on this sheet
        int nSheets = _workbook.getNumberOfSheets();
        for (int i = 0; i < nSheets; i++) {
            InternalSheet otherSheet = _workbook.getSheetAt(i).getSheet();
            if (otherSheet == this._sheet) {
                continue;
            }
            short otherExtSheetIx = _book.checkExternSheet(i);
            otherSheet.updateFormulasAfterCellShift(formulaShifter, otherExtSheetIx);
        }
        _workbook.getWorkbook().updateNamesAfterCellShift(formulaShifter);
    }

    private void recomputeFirstAndLastRowsForRowShift(int startRow, int endRow, int n) {
        if (n > 0) {
            // Rows are moving down
            if (startRow == _firstrow) {
                // Need to walk forward to find the first non-blank row
                _firstrow = Math.max(startRow + n, 0);
                for (int i = startRow + 1; i < startRow + n; i++) {
                    if (getRow(i) != null) {
                        _firstrow = i;
                        break;
                    }
                }
            }
            if (endRow + n > _lastrow) {
                _lastrow = Math.min(endRow + n, SpreadsheetVersion.EXCEL97.getLastRowIndex());
            }
        } else {
            // Rows are moving up
            if (startRow + n < _firstrow) {
                _firstrow = Math.max(startRow + n, 0);
            }
            if (endRow == _lastrow) {
                // Need to walk backward to find the last non-blank row
                // NOTE: n is always negative here
                _lastrow = Math.min(endRow + n, SpreadsheetVersion.EXCEL97.getLastRowIndex());
                for (int i = endRow - 1; i > endRow + n; i--) {
                    if (getRow(i) != null) {
                        _lastrow = i;
                        break;
                    }
                }
            }
        }
    }

    private void deleteOverwrittenHyperlinksForRowShift(int startRow, int endRow, int n) {
        final int firstOverwrittenRow = startRow + n;
        final int lastOverwrittenRow = endRow + n;
        for (HSSFHyperlink link : getHyperlinkList()) {
            // If hyperlink is fully contained in the rows that will be overwritten, delete the hyperlink
            final int firstRow = link.getFirstRow();
            final int lastRow = link.getLastRow();
            if (firstOverwrittenRow <= firstRow
                    && firstRow <= lastOverwrittenRow
                    && lastOverwrittenRow <= lastRow
                    && lastRow <= lastOverwrittenRow) {
                removeHyperlink(link);
            }
        }
    }

    private void moveCommentsForRowShift(int startRow, int endRow, int n) {
        final HSSFPatriarch patriarch = createDrawingPatriarch();
        for (final HSSFShape shape : patriarch.getChildren()) {
            if (!(shape instanceof HSSFComment)) {
                continue;
            }
            final HSSFComment comment = (HSSFComment) shape;
            final int r = comment.getRow();
            if (startRow <= r && r <= endRow) {
                comment.setRow(clip(r + n));
            }
        }
    }
    
    /**
     * Shifts columns in range [startColumn, endColumn] for n places to the right.
     * For n < 0, it will shift columns left.
     * Additionally adjusts formulas.
     * Probably should also process other features (hyperlinks, comments...) in the way analog to shiftRows method 
     * @param startRow               the row to start shifting
     * @param endRow                 the row to end shifting
     * @param n                      the number of rows to shift
     */

    @Beta
    @Override
    public void shiftColumns(int startColumn, int endColumn, int n){ 
        HSSFColumnShifter columnShifter = new HSSFColumnShifter(this); 
        columnShifter.shiftColumns(startColumn, endColumn, n); 
        
        int sheetIndex = _workbook.getSheetIndex(this);
        short externSheetIndex = _book.checkExternSheet(sheetIndex);
        String sheetName = _workbook.getSheetName(sheetIndex);
        FormulaShifter formulaShifter = FormulaShifter.createForColumnShift(
                externSheetIndex, sheetName, startColumn, endColumn, n, SpreadsheetVersion.EXCEL97);
        updateFormulasForShift(formulaShifter); 
        // add logic for hyperlinks etc, like in shiftRows() 
    } 

    protected void insertChartRecords(List<Record> records) {
        int window2Loc = _sheet.findFirstRecordLocBySid(WindowTwoRecord.sid);
        _sheet.getRecords().addAll(window2Loc, records);
    }

    private void notifyRowShifting(HSSFRow row) {
        String msg = "Row[rownum=" + row.getRowNum() + "] contains cell(s) included in a multi-cell array formula. " +
                "You cannot change part of an array.";
        for (Cell cell : row) {
            HSSFCell hcell = (HSSFCell) cell;
            if (hcell.isPartOfArrayFormulaGroup()) {
                hcell.tryToDeleteArrayFormula(msg);
            }
        }
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.<p>
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is removed
     *
     * @param colSplit       Horizonatal position of split.
     * @param rowSplit       Vertical position of split.
     * @param leftmostColumn Left column visible in right pane.
     * @param topRow         Top row visible in bottom pane
     */
    @Override
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow) {
        validateColumn(colSplit);
        validateRow(rowSplit);
        if (leftmostColumn < colSplit)
            throw new IllegalArgumentException("leftmostColumn parameter must not be less than colSplit parameter");
        if (topRow < rowSplit)
            throw new IllegalArgumentException("topRow parameter must not be less than leftmostColumn parameter");
        getSheet().createFreezePane(colSplit, rowSplit, topRow, leftmostColumn);
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.<p>
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is removed
     *
     * @param colSplit Horizonatal position of split.
     * @param rowSplit Vertical position of split.
     */
    @Override
    public void createFreezePane(int colSplit, int rowSplit) {
        createFreezePane(colSplit, rowSplit, colSplit, rowSplit);
    }

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     *
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow         Top row visible in bottom pane
     * @param leftmostColumn Left column visible in right pane.
     * @param activePane     Active pane.  One of: PANE_LOWER_RIGHT,
     *                       PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
    @Override
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane) {
        getSheet().createSplitPane(xSplitPos, ySplitPos, topRow, leftmostColumn, activePane);
    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze).
     *
     * @return null if no pane configured, or the pane information.
     */
    @Override
    public PaneInformation getPaneInformation() {
        return getSheet().getPaneInformation();
    }

    /**
     * Sets whether the gridlines are shown in a viewer.
     *
     * @param show whether to show gridlines or not
     */
    @Override
    public void setDisplayGridlines(boolean show) {
        _sheet.setDisplayGridlines(show);
    }

    /**
     * Returns if gridlines are displayed.
     *
     * @return whether gridlines are displayed
     */
    @Override
    public boolean isDisplayGridlines() {
        return _sheet.isDisplayGridlines();
    }

    /**
     * Sets whether the formulas are shown in a viewer.
     *
     * @param show whether to show formulas or not
     */
    @Override
    public void setDisplayFormulas(boolean show) {
        _sheet.setDisplayFormulas(show);
    }

    /**
     * Returns if formulas are displayed.
     *
     * @return whether formulas are displayed
     */
    @Override
    public boolean isDisplayFormulas() {
        return _sheet.isDisplayFormulas();
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
     *
     * @param show whether to show RowColHeadings or not
     */
    @Override
    public void setDisplayRowColHeadings(boolean show) {
        _sheet.setDisplayRowColHeadings(show);
    }

    /**
     * Returns if RowColHeadings are displayed.
     *
     * @return whether RowColHeadings are displayed
     */
    @Override
    public boolean isDisplayRowColHeadings() {
        return _sheet.isDisplayRowColHeadings();
    }

    /**
     * Sets a page break at the indicated row
     * Breaks occur above the specified row and left of the specified column inclusive.<p>
     * 
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param row the row to break, inclusive
     */
    @Override
    public void setRowBreak(int row) {
        validateRow(row);
        _sheet.getPageSettings().setRowBreak(row, (short) 0, (short) 255);
    }

    /**
     * @return <code>true</code> if there is a page break at the indicated row
     */
    @Override
    public boolean isRowBroken(int row) {
        return _sheet.getPageSettings().isRowBroken(row);
    }

    /**
     * Removes the page break at the indicated row
     */
    @Override
    public void removeRowBreak(int row) {
        _sheet.getPageSettings().removeRowBreak(row);
    }

    /**
     * @return row indexes of all the horizontal page breaks, never <code>null</code>
     */
    @Override
    public int[] getRowBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getRowBreaks();
    }

    /**
     * @return column indexes of all the vertical page breaks, never <code>null</code>
     */
    @Override
    public int[] getColumnBreaks() {
        //we can probably cache this information, but this should be a sparsely used function
        return _sheet.getPageSettings().getColumnBreaks();
    }


    /**
     * Sets a page break at the indicated column.
     * Breaks occur above the specified row and left of the specified column inclusive.<p>
     * 
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Simuilar, {@code sheet.setRowBreak(2);}
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param column the column to break, inclusive
     */
    @Override
    public void setColumnBreak(int column) {
        validateColumn((short) column);
        _sheet.getPageSettings().setColumnBreak((short) column, (short) 0, (short) SpreadsheetVersion.EXCEL97.getLastRowIndex());
    }

    /**
     * Determines if there is a page break at the indicated column
     *
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    @Override
    public boolean isColumnBroken(int column) {
        return _sheet.getPageSettings().isColumnBroken(column);
    }

    /**
     * Removes a page break at the indicated column
     *
     * @param column The index of the column for which to remove a page-break, zero-based
     */
    @Override
    public void removeColumnBreak(int column) {
        _sheet.getPageSettings().removeColumnBreak(column);
    }

    /**
     * Runs a bounds check for row numbers
     *
     * @param row the index of the row to validate, zero-based
     */
    protected void validateRow(int row) {
        int maxrow = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        if (row > maxrow) throw new IllegalArgumentException("Maximum row number is " + maxrow);
        if (row < 0) throw new IllegalArgumentException("Minumum row number is 0");
    }

    /**
     * Runs a bounds check for column numbers
     *
     * @param column the index of the column to validate, zero-based
     */
    protected void validateColumn(int column) {
        int maxcol = SpreadsheetVersion.EXCEL97.getLastColumnIndex();
        if (column > maxcol) throw new IllegalArgumentException("Maximum column number is " + maxcol);
        if (column < 0) throw new IllegalArgumentException("Minimum column number is 0");
    }

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
    public void dumpDrawingRecords(boolean fat, PrintWriter pw) {
        _sheet.aggregateDrawingRecords(_book.getDrawingManager(), false);

        EscherAggregate r = (EscherAggregate) getSheet().findFirstRecordBySid(EscherAggregate.sid);
        List<EscherRecord> escherRecords = r.getEscherRecords();
        for (EscherRecord escherRecord : escherRecords) {
            if (fat) {
                pw.println(escherRecord);
            } else {
                escherRecord.display(pw, 0);
            }
        }
        pw.flush();
    }

    /**
     * Returns the agregate escher records for this sheet,
     * it there is one.
     */
    public EscherAggregate getDrawingEscherAggregate() {
        _book.findDrawingGroup();

        // If there's now no drawing manager, then there's
        //  no drawing escher records on the workbook
        if (_book.getDrawingManager() == null) {
            return null;
        }

        int found = _sheet.aggregateDrawingRecords(
                _book.getDrawingManager(), false
        );
        if (found == -1) {
            // Workbook has drawing stuff, but this sheet doesn't
            return null;
        }

        // Grab our aggregate record, and wire it up
        return (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
    }

    /**
     * This will hold any graphics or charts for the sheet.
     *
     * @return the top-level drawing patriarch, if there is one, else returns null
     */
    @Override
    public HSSFPatriarch getDrawingPatriarch() {
        _patriarch = getPatriarch(false);
        return _patriarch;
    }

    /**
     * Creates the top-level drawing patriarch. 
     * <p>This may then be used to add graphics or charts.</p>
     * <p>Note that this will normally have the effect of removing 
     *  any existing drawings on this sheet.</p>
     *
     * @return The new patriarch.
     */
    @Override
    public HSSFPatriarch createDrawingPatriarch() {
        _patriarch = getPatriarch(true);
        return _patriarch;
    }

    private HSSFPatriarch getPatriarch(boolean createIfMissing) {
        if (_patriarch != null) {
            return _patriarch;
        }
        DrawingManager2 dm = _book.findDrawingGroup();
        if (null == dm) {
            if (!createIfMissing) {
                return null;
            } else {
                _book.createDrawingGroup();
                dm = _book.getDrawingManager();
            }
        }
        EscherAggregate agg = (EscherAggregate) _sheet.findFirstRecordBySid(EscherAggregate.sid);
        if (null == agg) {
            int pos = _sheet.aggregateDrawingRecords(dm, false);
            if (-1 == pos) {
                if (createIfMissing) {
                    pos = _sheet.aggregateDrawingRecords(dm, true);
                    agg = (EscherAggregate) _sheet.getRecords().get(pos);
                    HSSFPatriarch patriarch = new HSSFPatriarch(this, agg);
                    patriarch.afterCreate();
                    return patriarch;
                } else {
                    return null;
                }
            }
            agg = (EscherAggregate) _sheet.getRecords().get(pos);
        }
        return new HSSFPatriarch(this, agg);
    }

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber One of the columns in the group.
     * @param collapsed    true = collapse group, false = expand group.
     */
    @Override
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed) {
        _sheet.setColumnGroupCollapsed(columnNumber, collapsed);
    }

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn beginning of the column range.
     * @param toColumn   end of the column range.
     */
    @Override
    public void groupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, true);
    }

    @Override
    public void ungroupColumn(int fromColumn, int toColumn) {
        _sheet.groupColumnRange(fromColumn, toColumn, false);
    }

    /**
     * Tie a range of cell together so that they can be collapsed or expanded
     *
     * @param fromRow start row (0-based)
     * @param toRow   end row (0-based)
     */
    @Override
    public void groupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, true);
    }

    @Override
    public void ungroupRow(int fromRow, int toRow) {
        _sheet.groupRowRange(fromRow, toRow, false);
    }

    @Override
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
     * @param style  the style to set
     */
    @Override
    public void setDefaultColumnStyle(int column, CellStyle style) {
        _sheet.setDefaultColumnStyle(column, style.getIndex());
    }

    /**
     * Adjusts the column width to fit the contents.<p>
     * 
     * This process can be relatively slow on large sheets, so this should
     * normally only be called once per column, at the end of your
     * processing.
     *
     * @param column the column index
     */
    @Override
    public void autoSizeColumn(int column) {
        autoSizeColumn(column, false);
    }

    /**
     * Adjusts the column width to fit the contents.<p>
     * 
     * This process can be relatively slow on large sheets, so this should
     * normally only be called once per column, at the end of your
     * processing.<p>
     * 
     * You can specify whether the content of merged cells should be considered or ignored.
     * Default is to ignore merged cells.
     *
     * @param column         the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    @Override
    public void autoSizeColumn(int column, boolean useMergedCells) {
        double width = SheetUtil.getColumnWidth(this, column, useMergedCells);

        if (width != -1) {
            width *= 256;
            int maxColumnWidth = 255 * 256; // The maximum column width for an individual cell is 255 characters
            if (width > maxColumnWidth) {
                width = maxColumnWidth;
            }
            setColumnWidth(column, (int) (width));
        }
    }
    
    /**
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
    @Override
    public HSSFComment getCellComment(CellAddress ref) {
        return findCellComment(ref.getRow(), ref.getColumn());
    }
    
    /**
     * Get a Hyperlink in this sheet anchored at row, column
     *
     * @param row The index of the row of the hyperlink, zero-based
     * @param column the index of the column of the hyperlink, zero-based
     * @return hyperlink if there is a hyperlink anchored at row, column; otherwise returns null
     */
    @Override
    public HSSFHyperlink getHyperlink(int row, int column) {
        for (RecordBase rec : _sheet.getRecords()) {
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord link = (HyperlinkRecord) rec;
                if (link.getFirstColumn() == column && link.getFirstRow() == row) {
                    return new HSSFHyperlink(link);
                }
            }
        }
        return null;
    }
    
    /**
     * Get a Hyperlink in this sheet located in a cell specified by {code addr}
     *
     * @param addr The address of the cell containing the hyperlink
     * @return hyperlink if there is a hyperlink anchored at {@code addr}; otherwise returns {@code null}
     * @since POI 3.15 beta 3
     */
    @Override
    public HSSFHyperlink getHyperlink(CellAddress addr) {
        return getHyperlink(addr.getRow(), addr.getColumn());
    }
    
    /**
     * Get a list of Hyperlinks in this sheet
     *
     * @return Hyperlinks for the sheet
     */
    @Override
    public List<HSSFHyperlink> getHyperlinkList() {
        final List<HSSFHyperlink> hyperlinkList = new ArrayList<>();
        for (RecordBase rec : _sheet.getRecords()) {
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord link = (HyperlinkRecord) rec;
                hyperlinkList.add(new HSSFHyperlink(link));
            }
        }
        return hyperlinkList;
    }
    
    /**
     * Remove the underlying HyperlinkRecord from this sheet.
     * If multiple HSSFHyperlinks refer to the same HyperlinkRecord, all HSSFHyperlinks will be removed.
     *
     * @param link the HSSFHyperlink wrapper around the HyperlinkRecord to remove
     */
    protected void removeHyperlink(HSSFHyperlink link) {
        removeHyperlink(link.record);
    }
    
    /**
     * Remove the underlying HyperlinkRecord from this sheet
     *
     * @param link the underlying HyperlinkRecord to remove from this sheet
     */
    protected void removeHyperlink(HyperlinkRecord link) {
        for (Iterator<RecordBase> it = _sheet.getRecords().iterator(); it.hasNext();) {
            RecordBase rec = it.next();
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord recLink = (HyperlinkRecord) rec;
                if (link == recLink) {
                    it.remove();
                    // if multiple HSSFHyperlinks refer to the same record
                    return;
                }
            }
        }
    }

    @Override
    public HSSFSheetConditionalFormatting getSheetConditionalFormatting() {
        return new HSSFSheetConditionalFormatting(this);
    }

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    @SuppressWarnings("resource")
    @Override
    public String getSheetName() {
        HSSFWorkbook wb = getWorkbook();
        int idx = wb.getSheetIndex(this);
        return wb.getSheetName(idx);
    }

    /**
     * Also creates cells if they don't exist
     */
    private CellRange<HSSFCell> getCellRange(CellRangeAddress range) {
        int firstRow = range.getFirstRow();
        int firstColumn = range.getFirstColumn();
        int lastRow = range.getLastRow();
        int lastColumn = range.getLastColumn();
        int height = lastRow - firstRow + 1;
        int width = lastColumn - firstColumn + 1;
        List<HSSFCell> temp = new ArrayList<>(height * width);
        for (int rowIn = firstRow; rowIn <= lastRow; rowIn++) {
            for (int colIn = firstColumn; colIn <= lastColumn; colIn++) {
                HSSFRow row = getRow(rowIn);
                if (row == null) {
                    row = createRow(rowIn);
                }
                HSSFCell cell = row.getCell(colIn);
                if (cell == null) {
                    cell = row.createCell(colIn);
                }
                temp.add(cell);
            }
        }
        return SSCellRange.create(firstRow, firstColumn, height, width, temp, HSSFCell.class);
    }

    @Override
    public CellRange<HSSFCell> setArrayFormula(String formula, CellRangeAddress range) {
        // make sure the formula parses OK first
        int sheetIndex = _workbook.getSheetIndex(this);
        Ptg[] ptgs = HSSFFormulaParser.parse(formula, _workbook, FormulaType.ARRAY, sheetIndex);
        CellRange<HSSFCell> cells = getCellRange(range);

        for (HSSFCell c : cells) {
            c.setCellArrayFormula(range);
        }
        HSSFCell mainArrayFormulaCell = cells.getTopLeftCell();
        FormulaRecordAggregate agg = (FormulaRecordAggregate) mainArrayFormulaCell.getCellValueRecord();
        agg.setArrayFormula(range, ptgs);
        return cells;
    }

    @Override
    public CellRange<HSSFCell> removeArrayFormula(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this sheet.");
        }
        CellValueRecordInterface rec = ((HSSFCell) cell).getCellValueRecord();
        if (!(rec instanceof FormulaRecordAggregate)) {
            String ref = new CellReference(cell).formatAsString();
            throw new IllegalArgumentException("Cell " + ref + " is not part of an array formula.");
        }
        FormulaRecordAggregate fra = (FormulaRecordAggregate) rec;
        CellRangeAddress range = fra.removeArrayFormula(cell.getRowIndex(), cell.getColumnIndex());

        CellRange<HSSFCell> result = getCellRange(range);
        // clear all cells in the range
        for (Cell c : result) {
            c.setBlank();
        }
        return result;
    }

    @Override
    public DataValidationHelper getDataValidationHelper() {
        return new HSSFDataValidationHelper(this);
    }

    @Override
    public HSSFAutoFilter setAutoFilter(CellRangeAddress range) {
        InternalWorkbook workbook = _workbook.getWorkbook();
        int sheetIndex = _workbook.getSheetIndex(this);

        NameRecord name = workbook.getSpecificBuiltinRecord(NameRecord.BUILTIN_FILTER_DB, sheetIndex + 1);

        if (name == null) {
            name = workbook.createBuiltInName(NameRecord.BUILTIN_FILTER_DB, sheetIndex + 1);
        }

        int firstRow = range.getFirstRow();
        
        // if row was not given when constructing the range...
        if(firstRow == -1) {
            firstRow = 0;
        }

        // The built-in name must consist of a single Area3d Ptg.
        Area3DPtg ptg = new Area3DPtg(firstRow, range.getLastRow(),
                range.getFirstColumn(), range.getLastColumn(),
                false, false, false, false, sheetIndex);
        name.setNameDefinition(new Ptg[]{ptg});

        AutoFilterInfoRecord r = new AutoFilterInfoRecord();
        // the number of columns that have AutoFilter enabled.
        int numcols = 1 + range.getLastColumn() - range.getFirstColumn();
        r.setNumEntries((short) numcols);
        int idx = _sheet.findFirstRecordLocBySid(DimensionsRecord.sid);
        _sheet.getRecords().add(idx, r);

        //create a combobox control for each column
        HSSFPatriarch p = createDrawingPatriarch();
        final int firstColumn = range.getFirstColumn();
        final int lastColumn = range.getLastColumn();
        for (int col = firstColumn; col <= lastColumn; col++) {
            p.createComboBox(new HSSFClientAnchor(0, 0, 0, 0,
                    (short) col, firstRow, (short) (col + 1), firstRow + 1));
        }

        return new HSSFAutoFilter(this);
    }

    protected HSSFComment findCellComment(int row, int column) {
        HSSFPatriarch patriarch = getDrawingPatriarch();
        if (null == patriarch) {
            patriarch = createDrawingPatriarch();
        }
        return lookForComment(patriarch, row, column);
    }

    private HSSFComment lookForComment(HSSFShapeContainer container, int row, int column) {
        for (Object object : container.getChildren()) {
            HSSFShape shape = (HSSFShape) object;
            if (shape instanceof HSSFShapeGroup) {
                HSSFShape res = lookForComment((HSSFShapeContainer) shape, row, column);
                if (null != res) {
                    return (HSSFComment) res;
                }
                continue;
            }
            if (shape instanceof HSSFComment) {
                HSSFComment comment = (HSSFComment) shape;
                if (comment.hasPosition() && comment.getColumn() == column && comment.getRow() == row) {
                    return comment;
                }
            }
        }
        return null;
    }

    /**
     * Returns all cell comments on this sheet.
     * @return A map of each Comment in the sheet, keyed on the cell address where
     * the comment is located.
     */
    @Override
    public Map<CellAddress, HSSFComment> getCellComments() {
        HSSFPatriarch patriarch = getDrawingPatriarch();
        if (null == patriarch) {
            patriarch = createDrawingPatriarch();
        }
        
        Map<CellAddress, HSSFComment> locations = new TreeMap<>();
        findCellCommentLocations(patriarch, locations);
        return locations;
    }
    /**
     * Finds all cell comments in this sheet and adds them to the specified locations map
     *
     * @param container a container that may contain HSSFComments
     * @param locations the map to store the HSSFComments in
     */
    private void findCellCommentLocations(HSSFShapeContainer container, Map<CellAddress, HSSFComment> locations) {
        for (Object object : container.getChildren()) {
            HSSFShape shape = (HSSFShape) object;
            if (shape instanceof HSSFShapeGroup) {
                findCellCommentLocations((HSSFShapeGroup) shape, locations);
                continue;
            }
            if (shape instanceof HSSFComment) {
                HSSFComment comment = (HSSFComment) shape;
                if (comment.hasPosition()) {
                    locations.put(new CellAddress(comment.getRow(), comment.getColumn()), comment);
                }
            }
        }
    }

    @Override
    public CellRangeAddress getRepeatingRows() {
        return getRepeatingRowsOrColums(true);
    }

    @Override
    public CellRangeAddress getRepeatingColumns() {
        return getRepeatingRowsOrColums(false);
    }

    @Override
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
        CellRangeAddress columnRangeRef = getRepeatingColumns();
        setRepeatingRowsAndColumns(rowRangeRef, columnRangeRef);
    }

    @Override
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
        CellRangeAddress rowRangeRef = getRepeatingRows();
        setRepeatingRowsAndColumns(rowRangeRef, columnRangeRef);
    }


    private void setRepeatingRowsAndColumns(
            CellRangeAddress rowDef, CellRangeAddress colDef) {
        int sheetIndex = _workbook.getSheetIndex(this);
        int maxRowIndex = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        int maxColIndex = SpreadsheetVersion.EXCEL97.getLastColumnIndex();

        int col1 = -1;
        int col2 = -1;
        int row1 = -1;
        int row2 = -1;

        if (rowDef != null) {
            row1 = rowDef.getFirstRow();
            row2 = rowDef.getLastRow();
            if ((row1 == -1 && row2 != -1) || (row1 > row2)
                    || (row1 < 0 || row1 > maxRowIndex)
                    || (row2 < 0 || row2 > maxRowIndex)) {
                throw new IllegalArgumentException("Invalid row range specification");
            }
        }
        if (colDef != null) {
            col1 = colDef.getFirstColumn();
            col2 = colDef.getLastColumn();
            if ((col1 == -1 && col2 != -1) || (col1 > col2)
                    || (col1 < 0 || col1 > maxColIndex)
                    || (col2 < 0 || col2 > maxColIndex)) {
                throw new IllegalArgumentException("Invalid column range specification");
            }
        }

        short externSheetIndex =
                _workbook.getWorkbook().checkExternSheet(sheetIndex);

        boolean setBoth = rowDef != null && colDef != null;
        boolean removeAll = rowDef == null && colDef == null;

        HSSFName name = _workbook.getBuiltInName(
                NameRecord.BUILTIN_PRINT_TITLE, sheetIndex);
        if (removeAll) {
            if (name != null) {
                _workbook.removeName(name);
            }
            return;
        }
        if (name == null) {
            name = _workbook.createBuiltInName(
                    NameRecord.BUILTIN_PRINT_TITLE, sheetIndex);
        }

        List<Ptg> ptgList = new ArrayList<>();
        if (setBoth) {
            final int exprsSize = 2 * 11 + 1; // 2 * Area3DPtg.SIZE + UnionPtg.SIZE
            ptgList.add(new MemFuncPtg(exprsSize));
        }
        if (colDef != null) {
            Area3DPtg colArea = new Area3DPtg(0, maxRowIndex, col1, col2,
                    false, false, false, false, externSheetIndex);
            ptgList.add(colArea);
        }
        if (rowDef != null) {
            Area3DPtg rowArea = new Area3DPtg(row1, row2, 0, maxColIndex,
                    false, false, false, false, externSheetIndex);
            ptgList.add(rowArea);
        }
        if (setBoth) {
            ptgList.add(UnionPtg.instance);
        }

        Ptg[] ptgs = new Ptg[ptgList.size()];
        ptgList.toArray(ptgs);
        name.setNameDefinition(ptgs);

        HSSFPrintSetup printSetup = getPrintSetup();
        printSetup.setValidSettings(false);
        setActive(true);
    }


    private CellRangeAddress getRepeatingRowsOrColums(boolean rows) {
        NameRecord rec = getBuiltinNameRecord(NameRecord.BUILTIN_PRINT_TITLE);
        if (rec == null) {
            return null;
        }

        Ptg[] nameDefinition = rec.getNameDefinition();
        if (nameDefinition == null) {
            return null;
        }

        int maxRowIndex = SpreadsheetVersion.EXCEL97.getLastRowIndex();
        int maxColIndex = SpreadsheetVersion.EXCEL97.getLastColumnIndex();

        for (Ptg ptg : nameDefinition) {

            if (ptg instanceof Area3DPtg) {
                Area3DPtg areaPtg = (Area3DPtg) ptg;

                if (areaPtg.getFirstColumn() == 0
                        && areaPtg.getLastColumn() == maxColIndex) {
                    if (rows) {
                        return new CellRangeAddress(
                                areaPtg.getFirstRow(), areaPtg.getLastRow(), -1, -1);
                    }
                } else if (areaPtg.getFirstRow() == 0
                        && areaPtg.getLastRow() == maxRowIndex) {
                    if (!rows) {
                        return new CellRangeAddress(-1, -1,
                                areaPtg.getFirstColumn(), areaPtg.getLastColumn());
                    }
                }

            }

        }

        return null;
    }


    private NameRecord getBuiltinNameRecord(byte builtinCode) {
        int sheetIndex = _workbook.getSheetIndex(this);
        int recIndex =
                _workbook.findExistingBuiltinNameRecordIdx(sheetIndex, builtinCode);
        if (recIndex == -1) {
            return null;
        }
        return _workbook.getNameRecord(recIndex);
    }
    
    /**
     * Returns the column outline level. Increased as you
     *  put it into more groups (outlines), reduced as
     *  you take it out of them.
     */
    @Override
    public int getColumnOutlineLevel(int columnIndex) {
        return _sheet.getColumnOutlineLevel(columnIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellAddress getActiveCell() {
        int row = _sheet.getActiveCellRow();
        int col = _sheet.getActiveCellCol();
        return new CellAddress(row, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveCell(CellAddress address) {
        int row = address.getRow();
        short col = (short) address.getColumn();
        _sheet.setActiveCellRow(row);
        _sheet.setActiveCellCol(col);
    }
}
