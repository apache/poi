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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordBase;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.ptg.ExpPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot contain numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * <p/>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 */
public class HSSFCell implements Cell {

    private static POILogger log = POILogFactory.getLogger(HSSFCell.class);

    private static final String FILE_FORMAT_NAME = "BIFF8";
    /**
     * The maximum  number of columns in BIFF8
     */
    public static final int LAST_COLUMN_NUMBER = SpreadsheetVersion.EXCEL97.getLastColumnIndex(); // 2^8 - 1
    private static final String LAST_COLUMN_NAME = SpreadsheetVersion.EXCEL97.getLastColumnName();

    public static final short ENCODING_UNCHANGED = -1;
    public static final short ENCODING_COMPRESSED_UNICODE = 0;
    public static final short ENCODING_UTF_16 = 1;

    private final HSSFWorkbook book;
    private final HSSFSheet sheet;
    private int cellType;
    private HSSFRichTextString stringValue;
    private CellValueRecordInterface record;
    private HSSFComment comment;

    /**
     * Creates a new Cell from scratch.
     * Should only be called by HSSFRow.
     * <p/>
     * When the cell is initially created it is set to CELL_TYPE_BLANK. Cell types
     * can be changed/overwritten by calling setCellValue with the appropriate
     * type as a parameter although conversions from one type to another may be
     * prohibited.
     *
     * @param book  Workbook record of the workbook containing this cell
     * @param sheet Sheet record of the sheet containing this cell
     * @param row   the row of this cell
     * @param col   the column for this cell
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short)
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, int row, short col) {
        checkBounds(col);
        this.stringValue = null;
        this.book = book;
        this.sheet = sheet;

        // Relying on the fact that by default the cellType is set to 0 which
        // is different to CELL_TYPE_BLANK hence the following method call correctly
        // creates a new blank cell.
        short xfindex = sheet.getSheet().getXFIndexForColAt(col);
        setCellType(CELL_TYPE_BLANK, false, row, col, xfindex);
    }

    /**
     * Returns the HSSFSheet this cell belongs to.
     *
     * @return the HSSFSheet that owns this cell
     */
    public HSSFSheet getSheet() {
        return sheet;
    }

    /**
     * Returns the HSSFRow this cell belongs to.
     *
     * @return the HSSFRow that owns this cell
     */
    public HSSFRow getRow() {
        int rowIndex = getRowIndex();
        return sheet.getRow(rowIndex);
    }

    /**
     * Creates a new Cell from scratch.
     * Should only be called by HSSFRow.
     *
     * @param book  Workbook record of the workbook containing this cell
     * @param sheet Sheet record of the sheet containing this cell
     * @param row   the row of this cell
     * @param col   the column for this cell
     * @param type  the type of cell
     *              CELL_TYPE_NUMERIC, CELL_TYPE_STRING, CELL_TYPE_FORMULA,
     *              CELL_TYPE_BLANK, CELL_TYPE_BOOLEAN, CELL_TYPE_ERROR
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short, int)
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, int row, short col, int type) {
        checkBounds(col);
        this.cellType = -1; // Force 'setCellType' to create a first Record
        this.stringValue = null;
        this.book = book;
        this.sheet = sheet;

        short xfindex = sheet.getSheet().getXFIndexForColAt(col);
        setCellType(type, false, row, col, xfindex);
    }

    /**
     * Creates an HSSFCell from a CellValueRecordInterface.
     * HSSFSheet uses this when reading in cells from an existing sheet.
     *
     * @param book  Workbook record of the workbook containing this cell
     * @param sheet Sheet record of the sheet containing this cell
     * @param cval  the Cell Value Record we wish to represent
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, CellValueRecordInterface cval) {
        this.record = cval;
        this.cellType = determineType(cval);
        this.stringValue = null;
        this.book = book;
        this.sheet = sheet;

        switch (cellType) {
            case CELL_TYPE_STRING:
                stringValue = new HSSFRichTextString(book.getWorkbook(), (LabelSSTRecord) cval);
                break;

            case CELL_TYPE_BLANK:
                break;

            case CELL_TYPE_FORMULA:
                stringValue = new HSSFRichTextString(((FormulaRecordAggregate) cval).getStringValue());
                break;
        }
    }

    /**
     * Figures out the type of a cell value record.
     */
    private static int determineType(CellValueRecordInterface cval) {
        if (cval instanceof FormulaRecordAggregate) {
            return HSSFCell.CELL_TYPE_FORMULA;
        }
        // all others are plain BIFF records
        Record record = (Record) cval;
        switch (record.getSid()) {

            case NumberRecord.sid:
                return HSSFCell.CELL_TYPE_NUMERIC;
            case BlankRecord.sid:
                return HSSFCell.CELL_TYPE_BLANK;
            case LabelSSTRecord.sid:
                return HSSFCell.CELL_TYPE_STRING;
            case BoolErrRecord.sid:
                BoolErrRecord boolErrRecord = (BoolErrRecord) record;

                return boolErrRecord.isBoolean()
                        ? HSSFCell.CELL_TYPE_BOOLEAN
                        : HSSFCell.CELL_TYPE_ERROR;
        }
        throw new RuntimeException("Bad cell value rec (" + cval.getClass().getName() + ")");
    }

    /**
     * Returns the Workbook that this Cell is bound to.
     */
    protected InternalWorkbook getBoundWorkbook() {
        return book.getWorkbook();
    }

    /**
     * @return the (zero based) index of the row containing this cell
     */
    public int getRowIndex() {
        return record.getRow();
    }

    /**
     * Sets the cell's number within the row (0 based).
     *
     * @param num short the cell number
     * @deprecated (Jan 2008) Doesn't update the row's idea of what cell this is, use {@link HSSFRow#moveCell(HSSFCell, short)} instead
     */
    public void setCellNum(short num) {
        record.setColumn(num);
    }

    /**
     * Updates the cell's idea of what column it belongs in (0 based).
     *
     * @param num the new cell number
     */
    protected void updateCellNum(short num) {
        record.setColumn(num);
    }

    /**
     * @deprecated (Oct 2008) use {@link #getColumnIndex()}
     */
    public short getCellNum() {
        return (short) getColumnIndex();
    }

    public int getColumnIndex() {
        return record.getColumn() & 0xFFFF;
    }

    /**
     * Sets the cell type (numeric, formula or string).
     * If the cell currently contains a value, the value will
     * be converted to match the new type, if possible.
     *
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */
    public void setCellType(int cellType) {
        notifyFormulaChanging();
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging();
        }
        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();
        setCellType(cellType, true, row, col, styleIndex);
    }

    /**
     * Sets the cell type. The setValue flag indicates whether to bother about
     * trying to preserve the current value in the new record if one is created.
     * <p/>
     * The @see #setCellValue method will call this method with false in setValue
     * since it will overwrite the cell value later
     */
    private void setCellType(int cellType, boolean setValue, int row, short col, short styleIndex) {
        if (cellType > CELL_TYPE_ERROR) {
            throw new RuntimeException("I have no idea what type that is!");
        }
        switch (cellType) {
            case CELL_TYPE_FORMULA:
                FormulaRecordAggregate frec;

                if (cellType != this.cellType) {
                    frec = sheet.getSheet().getRowsAggregate().createFormula(row, col);
                } else {
                    frec = (FormulaRecordAggregate) record;
                    frec.setRow(row);
                    frec.setColumn(col);
                }
                if (setValue) {
                    frec.getFormulaRecord().setValue(getNumericCellValue());
                }
                frec.setXFIndex(styleIndex);
                record = frec;
                break;

            case CELL_TYPE_NUMERIC:
                NumberRecord nrec = null;

                if (cellType != this.cellType) {
                    nrec = new NumberRecord();
                } else {
                    nrec = (NumberRecord) record;
                }
                nrec.setColumn(col);
                if (setValue) {
                    nrec.setValue(getNumericCellValue());
                }
                nrec.setXFIndex(styleIndex);
                nrec.setRow(row);
                record = nrec;
                break;

            case CELL_TYPE_STRING:
                LabelSSTRecord lrec;

                if (cellType == this.cellType) {
                    lrec = (LabelSSTRecord) record;
                } else {
                    lrec = new LabelSSTRecord();
                    lrec.setColumn(col);
                    lrec.setRow(row);
                    lrec.setXFIndex(styleIndex);
                }
                if (setValue) {
                    String str = convertCellValueToString();
                    int sstIndex = book.getWorkbook().addSSTString(new UnicodeString(str));
                    lrec.setSSTIndex(sstIndex);
                    UnicodeString us = book.getWorkbook().getSSTString(sstIndex);
                    stringValue = new HSSFRichTextString();
                    stringValue.setUnicodeString(us);
                }
                record = lrec;
                break;

            case CELL_TYPE_BLANK:
                BlankRecord brec = null;

                if (cellType != this.cellType) {
                    brec = new BlankRecord();
                } else {
                    brec = (BlankRecord) record;
                }
                brec.setColumn(col);

                // During construction the cellStyle may be null for a Blank cell.
                brec.setXFIndex(styleIndex);
                brec.setRow(row);
                record = brec;
                break;

            case CELL_TYPE_BOOLEAN:
                BoolErrRecord boolRec = null;

                if (cellType != this.cellType) {
                    boolRec = new BoolErrRecord();
                } else {
                    boolRec = (BoolErrRecord) record;
                }
                boolRec.setColumn(col);
                if (setValue) {
                    boolRec.setValue(convertCellValueToBoolean());
                }
                boolRec.setXFIndex(styleIndex);
                boolRec.setRow(row);
                record = boolRec;
                break;

            case CELL_TYPE_ERROR:
                BoolErrRecord errRec = null;

                if (cellType != this.cellType) {
                    errRec = new BoolErrRecord();
                } else {
                    errRec = (BoolErrRecord) record;
                }
                errRec.setColumn(col);
                if (setValue) {
                    errRec.setValue((byte) HSSFErrorConstants.ERROR_VALUE);
                }
                errRec.setXFIndex(styleIndex);
                errRec.setRow(row);
                record = errRec;
                break;
        }
        if (cellType != this.cellType && this.cellType != -1) { // Special Value to indicate an uninitialized Cell
            sheet.getSheet().replaceValueRecord(record);
        }
        this.cellType = cellType;
    }

    /**
     * Get the cell type (numeric, formula or string).
     *
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */
    public int getCellType() {
        return cellType;
    }

    /**
     * Sets a numeric value for the cell.
     *
     * @param value the numeric value to set this cell to.  For formulas we'll set the
     *              precalculated value, for numerics we'll set its value. For other types we
     *              will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(double value) {
        if (Double.isInfinite(value)) {
            // Excel does not support positive/negative infinities,
            // rather, it gives a #DIV/0! error in these cases.
            setCellErrorValue(FormulaError.DIV0.getCode());
        } else if (Double.isNaN(value)) {
            // Excel does not support Not-a-Number (NaN),
            // instead it immediately generates a #NUM! error.
            setCellErrorValue(FormulaError.NUM.getCode());
        } else {
            int row = record.getRow();
            short col = record.getColumn();
            short styleIndex = record.getXFIndex();

            switch (cellType) {
                default:
                    setCellType(CELL_TYPE_NUMERIC, false, row, col, styleIndex);
                case CELL_TYPE_NUMERIC:
                    ((NumberRecord) record).setValue(value);
                    break;
                case CELL_TYPE_FORMULA:
                    ((FormulaRecordAggregate) record).setCachedDoubleResult(value);
                    break;
            }
        }
    }

    /**
     * Sets a date value for the cell.
     * Excel treats dates as numeric so you will need to format the cell as a date.
     *
     * @param value the date value to set this cell to.  For formulas we'll set the
     *              precalculated value, for numerics we'll set its value. For other types we
     *              will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Date value) {
        setCellValue(HSSFDateUtil.getExcelDate(value, book.getWorkbook().isUsing1904DateWindowing()));
    }

    /**
     * Sets a date value for the cell.
     * Excel treats dates as numeric so you will need to format the cell as a date.
     * <p/>
     * This will set the cell value based on the Calendar's timezone. As Excel
     * does not support timezones this means that both 20:00+03:00 and
     * 20:00-03:00 will be reported as the same value (20:00) even that there
     * are 6 hours difference between the two times. This difference can be
     * preserved by using <code>setCellValue(value.getTime())</code> which will
     * automatically shift the times to the default timezone.
     *
     * @param value the date value to set this cell to.  For formulas we'll set the
     *              precalculated value, for numerics we'll set its value. For othertypes we
     *              will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Calendar value) {
        setCellValue(HSSFDateUtil.getExcelDate(value, book.getWorkbook().isUsing1904DateWindowing()));
    }

    /**
     * Sets a string value for the cell.
     *
     * @param value value to set the cell to.  For formulas we'll set the formula
     *              cached string result, for String cells we'll set its value. For other types we will
     *              change the cell to a string cell and set its value.
     *              If value is null then we will change the cell to a Blank cell.
     */
    public void setCellValue(String value) {
        HSSFRichTextString str = value == null ? null : new HSSFRichTextString(value);
        setCellValue(str);
    }

    /**
     * Sets a string value for the cell.
     *
     * @param value value to set the cell to.  For formulas we'll set the formula
     *              string, for String cells we'll set its value.  For other types we will
     *              change the cell to a string cell and set its value.
     *              If value is <code>null</code> then we will change the cell to a Blank cell.
     */
    public void setCellValue(RichTextString value) {
        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();
        if (value == null) {
            notifyFormulaChanging();
            setCellType(CELL_TYPE_BLANK, false, row, col, styleIndex);
            return;
        }

        if (value.length() > SpreadsheetVersion.EXCEL97.getMaxTextLength()) {
            throw new IllegalArgumentException("The maximum length of cell contents (text) is 32,767 characters");
        }

        if (cellType == CELL_TYPE_FORMULA) {
            // Set the 'pre-evaluated result' for the formula
            // note - formulas do not preserve text formatting.
            FormulaRecordAggregate fr = (FormulaRecordAggregate) record;
            fr.setCachedStringResult(value.getString());
            // Update our local cache to the un-formatted version
            stringValue = new HSSFRichTextString(value.getString());

            // All done
            return;
        }

        // If we get here, we're not dealing with a formula,
        // so handle things as a normal rich text cell

        if (cellType != CELL_TYPE_STRING) {
            setCellType(CELL_TYPE_STRING, false, row, col, styleIndex);
        }
        int index = 0;

        HSSFRichTextString hvalue = (HSSFRichTextString) value;
        UnicodeString str = hvalue.getUnicodeString();
        index = book.getWorkbook().addSSTString(str);
        ((LabelSSTRecord) record).setSSTIndex(index);
        stringValue = hvalue;
        stringValue.setWorkbookReferences(book.getWorkbook(), ((LabelSSTRecord) record));
        stringValue.setUnicodeString(book.getWorkbook().getSSTString(index));
    }

    public void setCellFormula(String formula) {
        if (isPartOfArrayFormulaGroup()) {
            notifyArrayFormulaChanging();
        }

        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();

        if (formula == null) {
            notifyFormulaChanging();
            setCellType(CELL_TYPE_BLANK, false, row, col, styleIndex);
            return;
        }
        int sheetIndex = book.getSheetIndex(sheet);
        Ptg[] ptgs = HSSFFormulaParser.parse(formula, book, FormulaType.CELL, sheetIndex);
        setCellType(CELL_TYPE_FORMULA, false, row, col, styleIndex);
        FormulaRecordAggregate agg = (FormulaRecordAggregate) record;
        FormulaRecord frec = agg.getFormulaRecord();
        frec.setOptions((short) 2);
        frec.setValue(0);

        // only set to default if there is no extended format index already set
        if (agg.getXFIndex() == (short) 0) {
            agg.setXFIndex((short) 0x0f);
        }
        agg.setParsedExpression(ptgs);
    }

    /**
     * Should be called any time that a formula could potentially be deleted.
     * Does nothing if this cell currently does not hold a formula
     */
    private void notifyFormulaChanging() {
        if (record instanceof FormulaRecordAggregate) {
            ((FormulaRecordAggregate) record).notifyFormulaChanging();
        }
    }

    public String getCellFormula() {
        if (!(record instanceof FormulaRecordAggregate)) {
            throw typeMismatch(CELL_TYPE_FORMULA, cellType, true);
        }
        return HSSFFormulaParser.toFormulaString(book, ((FormulaRecordAggregate) record).getFormulaTokens());
    }

    /**
     * Used to help format error messages
     */
    private static String getCellTypeName(int cellTypeCode) {
        switch (cellTypeCode) {
            case CELL_TYPE_BLANK:
                return "blank";
            case CELL_TYPE_STRING:
                return "text";
            case CELL_TYPE_BOOLEAN:
                return "boolean";
            case CELL_TYPE_ERROR:
                return "error";
            case CELL_TYPE_NUMERIC:
                return "numeric";
            case CELL_TYPE_FORMULA:
                return "formula";
        }
        return "#unknown cell type (" + cellTypeCode + ")#";
    }

    private static RuntimeException typeMismatch(int expectedTypeCode, int actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a "
                + getCellTypeName(expectedTypeCode) + " value from a "
                + getCellTypeName(actualTypeCode) + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }

    private static void checkFormulaCachedValueType(int expectedTypeCode, FormulaRecord fr) {
        int cachedValueType = fr.getCachedResultType();
        if (cachedValueType != expectedTypeCode) {
            throw typeMismatch(expectedTypeCode, cachedValueType, true);
        }
    }

    /**
     * Gets the value of the cell as a number.
     * For strings we throw an exception.
     * For blank cells we return 0.
     * See {@link HSSFDataFormatter} for turning this
     * number into a string similar to that which
     * Excel would render this number as.
     */
    public double getNumericCellValue() {
        switch (cellType) {
            case CELL_TYPE_BLANK:
                return 0.0;
            case CELL_TYPE_NUMERIC:
                return ((NumberRecord) record).getValue();
            default:
                throw typeMismatch(CELL_TYPE_NUMERIC, cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate) record).getFormulaRecord();
        checkFormulaCachedValueType(CELL_TYPE_NUMERIC, fr);
        return fr.getValue();
    }

    /**
     * Gets the value of the cell as a date.
     * For strings we throw an exception.
     * For blank cells we return null.
     * See {@link HSSFDataFormatter} for formatting
     * this date into a string similar to how excel does.
     */
    public Date getDateCellValue() {
        if (cellType == CELL_TYPE_BLANK) {
            return null;
        }
        double value = getNumericCellValue();
        if (book.getWorkbook().isUsing1904DateWindowing()) {
            return HSSFDateUtil.getJavaDate(value, true);
        }
        return HSSFDateUtil.getJavaDate(value, false);
    }

    /**
     * Gets the value of the cell as a string.
     * For numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception.
     */
    public String getStringCellValue() {
        HSSFRichTextString str = getRichStringCellValue();
        return str.getString();
    }

    /**
     * Gets the value of the cell as a string.
     * For numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception.
     */
    public HSSFRichTextString getRichStringCellValue() {
        switch (cellType) {
            case CELL_TYPE_BLANK:
                return new HSSFRichTextString("");
            case CELL_TYPE_STRING:
                return stringValue;
            default:
                throw typeMismatch(CELL_TYPE_STRING, cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecordAggregate fra = ((FormulaRecordAggregate) record);
        checkFormulaCachedValueType(CELL_TYPE_STRING, fra.getFormulaRecord());
        String strVal = fra.getStringValue();
        return new HSSFRichTextString(strVal == null ? "" : strVal);
    }

    /**
     * Sets a boolean value for the cell.
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *              precalculated value, for booleans we'll set its value. For other types we
     *              will change the cell to a boolean cell and set its value.
     */
    public void setCellValue(boolean value) {
        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();

        switch (cellType) {
            default:
                setCellType(CELL_TYPE_BOOLEAN, false, row, col, styleIndex);
            case CELL_TYPE_BOOLEAN:
                ((BoolErrRecord) record).setValue(value);
                break;
            case CELL_TYPE_FORMULA:
                ((FormulaRecordAggregate) record).setCachedBooleanResult(value);
                break;
        }
    }

    /**
     * Sets an error value for the cell.
     *
     * @param errorCode the error value to set this cell to.  For formulas we'll set the
     *                  precalculated value , for errors we'll set
     *                  its value. For other types we will change the cell to an error
     *                  cell and set its value.
     */
    public void setCellErrorValue(byte errorCode) {
        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();
        switch (cellType) {
            default:
                setCellType(CELL_TYPE_ERROR, false, row, col, styleIndex);
            case CELL_TYPE_ERROR:
                ((BoolErrRecord) record).setValue(errorCode);
                break;
            case CELL_TYPE_FORMULA:
                ((FormulaRecordAggregate) record).setCachedErrorResult(errorCode);
                break;
        }
    }

    /**
     * Chooses a new boolean value for the cell when its type is changing.
     * <p/>
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(int, Object) should be introduced to avoid this
     */
    private boolean convertCellValueToBoolean() {
        switch (cellType) {
            case CELL_TYPE_BOOLEAN:
                return ((BoolErrRecord) record).getBooleanValue();
            case CELL_TYPE_STRING:
                int sstIndex = ((LabelSSTRecord) record).getSSTIndex();
                String text = book.getWorkbook().getSSTString(sstIndex).getString();
                return Boolean.valueOf(text).booleanValue();
            case CELL_TYPE_NUMERIC:
                return ((NumberRecord) record).getValue() != 0;
            case CELL_TYPE_FORMULA:
                // use cached formula result if it's the right type:
                FormulaRecord fr = ((FormulaRecordAggregate) record).getFormulaRecord();
                checkFormulaCachedValueType(CELL_TYPE_BOOLEAN, fr);
                return fr.getCachedBooleanValue();
            // Other cases convert to false
            // These choices are not well justified.
            case CELL_TYPE_ERROR:
            case CELL_TYPE_BLANK:
                return false;
        }
        throw new RuntimeException("Unexpected cell type (" + cellType + ")");
    }

    private String convertCellValueToString() {
        switch (cellType) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return ((BoolErrRecord) record).getBooleanValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                int sstIndex = ((LabelSSTRecord) record).getSSTIndex();
                return book.getWorkbook().getSSTString(sstIndex).getString();
            case CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(((NumberRecord) record).getValue());
            case CELL_TYPE_ERROR:
                return HSSFErrorConstants.getText(((BoolErrRecord) record).getErrorValue());
            case CELL_TYPE_FORMULA:
                // should really evaluate, but HSSFCell can't call HSSFFormulaEvaluator
                // just use cached formula result instead
                break;
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }

        FormulaRecordAggregate fra = ((FormulaRecordAggregate) record);
        FormulaRecord fr = fra.getFormulaRecord();

        switch (fr.getCachedResultType()) {
            case CELL_TYPE_BOOLEAN:
                return fr.getCachedBooleanValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                return fra.getStringValue();
            case CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(fr.getValue());
            case CELL_TYPE_ERROR:
                return HSSFErrorConstants.getText(fr.getCachedErrorValue());
        }
        throw new IllegalStateException("Unexpected formula result type (" + cellType + ")");
    }

    /**
     * Gets the value of the cell as a boolean.
     * For strings, numbers, and errors, we throw an exception.
     * For blank cells we return false.
     */
    public boolean getBooleanCellValue() {
        switch (cellType) {
            case CELL_TYPE_BLANK:
                return false;
            case CELL_TYPE_BOOLEAN:
                return ((BoolErrRecord) record).getBooleanValue();
            default:
                throw typeMismatch(CELL_TYPE_BOOLEAN, cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate) record).getFormulaRecord();
        checkFormulaCachedValueType(CELL_TYPE_BOOLEAN, fr);
        return fr.getCachedBooleanValue();
    }

    /**
     * Gets the value of the cell as an error code.
     * For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return 0.
     */
    public byte getErrorCellValue() {
        switch (cellType) {
            case CELL_TYPE_ERROR:
                return ((BoolErrRecord) record).getErrorValue();
            default:
                throw typeMismatch(CELL_TYPE_ERROR, cellType, false);
            case CELL_TYPE_FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate) record).getFormulaRecord();
        checkFormulaCachedValueType(CELL_TYPE_ERROR, fr);
        return (byte) fr.getCachedErrorValue();
    }

    /**
     * Sets the style for the cell.
     * The style should be an HSSFCellStyle created/retreived from
     * the HSSFWorkbook.
     *
     * @param style reference contained in the workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */
    public void setCellStyle(CellStyle style) {
        setCellStyle((HSSFCellStyle) style);
    }

    public void setCellStyle(HSSFCellStyle style) {
        // A style of null means resetting back to the default style
        if (style == null) {
            record.setXFIndex((short) 0xf);
            return;
        }

        // Verify the style really does belong to our workbook
        style.verifyBelongsToWorkbook(book);

        short styleIndex;
        if (style.getUserStyleName() != null) {
            styleIndex = applyUserCellStyle(style);
        } else {
            styleIndex = style.getIndex();
        }

        // Change our cell record to use this style
        record.setXFIndex(styleIndex);
    }

    /**
     * Gets the style for the cell.
     * This is a reference to a cell style contained in the workbook object.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */
    public HSSFCellStyle getCellStyle() {
        short styleIndex = record.getXFIndex();
        ExtendedFormatRecord xf = book.getWorkbook().getExFormatAt(styleIndex);
        return new HSSFCellStyle(styleIndex, xf, book);
    }

    /**
     * Returns the low level CellValueRecordInterface record.
     * Should only be used by HSSFSheet and friends.
     *
     * @return CellValueRecordInterface representing the cell via the low level api
     */
    protected CellValueRecordInterface getCellValueRecord() {
        return record;
    }

    /**
     * @throws RuntimeException if the bounds are exceeded
     */
    private static void checkBounds(int cellIndex) {
        if (cellIndex < 0 || cellIndex > LAST_COLUMN_NUMBER) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex
                    + ").  Allowable column range for " + FILE_FORMAT_NAME + " is (0.."
                    + LAST_COLUMN_NUMBER + ") or ('A'..'" + LAST_COLUMN_NAME + "')");
        }
    }

    /**
     * Sets this cell as the active cell for the worksheet.
     */
    public void setAsActiveCell() {
        int row = record.getRow();
        short col = record.getColumn();
        sheet.getSheet().setActiveCellRow(row);
        sheet.getSheet().setActiveCellCol(col);
    }

    /**
     * Returns a string representation of the cell.
     * <p/>
     * This method returns a simple representation,
     * anthing more complex should be in user code, with
     * knowledge of the semantics of the sheet being processed.
     * <p/>
     * Formula cells return the formula string,
     * rather than the formula result.
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     */
    public String toString() {
        switch (getCellType()) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return getBooleanCellValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_ERROR:
                return ErrorEval.getText(((BoolErrRecord) record).getErrorValue());
            case CELL_TYPE_FORMULA:
                return getCellFormula();
            case CELL_TYPE_NUMERIC:
                //TODO apply the dataformat for this cell
                if (HSSFDateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    return sdf.format(getDateCellValue());
                }
                return String.valueOf(getNumericCellValue());
            case CELL_TYPE_STRING:
                return getStringCellValue();
            default:
                return "Unknown Cell Type: " + getCellType();
        }
    }

    /**
     * Assign a comment to this cell.
     * If the supplied comment is null,
     * the comment for this cell will be removed.
     *
     * @param comment comment associated with this cell
     */
    public void setCellComment(Comment comment) {
        if (comment == null) {
            removeCellComment();
            return;
        }

        comment.setRow(record.getRow());
        comment.setColumn(record.getColumn());
        this.comment = (HSSFComment) comment;
    }

    /**
     * Returns the comment associated with this cell.
     *
     * @return comment associated with this cell
     */
    public HSSFComment getCellComment() {
        if (comment == null) {
            comment = sheet.findCellComment(record.getRow(), record.getColumn());
        }
        return comment;
    }

    /**
     * Removes the comment for this cell, if there is one.
     * WARNING - some versions of excel will loose
     * all comments after performing this action!
     */
    public void removeCellComment() {
        HSSFComment comment = sheet.findCellComment(record.getRow(), record.getColumn());
        this.comment = null;
        if (null == comment) {
            return;
        }
        sheet.getDrawingPatriarch().removeShape(comment);
    }

    /**
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    public HSSFHyperlink getHyperlink() {
        for (Iterator<RecordBase> it = sheet.getSheet().getRecords().iterator(); it.hasNext(); ) {
            RecordBase rec = it.next();
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord link = (HyperlinkRecord) rec;
                if (link.getFirstColumn() == record.getColumn() && link.getFirstRow() == record.getRow()) {
                    return new HSSFHyperlink(link);
                }
            }
        }
        return null;
    }

    /**
     * Assigns a hyperlink to this cell.
     * If the supplied hyperlink is null,
     * the hyperlink for this cell will be removed.
     *
     * @param hyperlink hyperlink associated with this cell
     */
    public void setHyperlink(Hyperlink hyperlink) {
        if (hyperlink == null) {
            removeHyperlink();
            return;
        }

        HSSFHyperlink link = (HSSFHyperlink) hyperlink;

        link.setFirstRow(record.getRow());
        link.setLastRow(record.getRow());
        link.setFirstColumn(record.getColumn());
        link.setLastColumn(record.getColumn());

        switch (link.getType()) {
            case HSSFHyperlink.LINK_EMAIL:
            case HSSFHyperlink.LINK_URL:
                link.setLabel("url");
                break;
            case HSSFHyperlink.LINK_FILE:
                link.setLabel("file");
                break;
            case HSSFHyperlink.LINK_DOCUMENT:
                link.setLabel("place");
                break;
        }

        List<RecordBase> records = sheet.getSheet().getRecords();
        int eofLoc = records.size() - 1;
        records.add(eofLoc, link.record);
    }

    /**
     * Removes the hyperlink for this cell, if there is one.
     */
    public void removeHyperlink() {
        for (Iterator<RecordBase> it = sheet.getSheet().getRecords().iterator(); it.hasNext(); ) {
            RecordBase rec = it.next();
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord link = (HyperlinkRecord) rec;
                if (link.getFirstColumn() == record.getColumn() && link.getFirstRow() == record.getRow()) {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * Gets the cached formula result type.
     * Only valid for formula cells.
     *
     * @return one of ({@link #CELL_TYPE_NUMERIC}, {@link #CELL_TYPE_STRING},
     * {@link #CELL_TYPE_BOOLEAN}, {@link #CELL_TYPE_ERROR}) depending
     * on the cached value of the formula
     */
    public int getCachedFormulaResultType() {
        if (cellType != CELL_TYPE_FORMULA) {
            throw new IllegalStateException("Only formula cells have cached results");
        }
        return ((FormulaRecordAggregate) record).getFormulaRecord().getCachedResultType();
    }

    void setCellArrayFormula(CellRangeAddress range) {
        int row = record.getRow();
        short col = record.getColumn();
        short styleIndex = record.getXFIndex();
        setCellType(CELL_TYPE_FORMULA, false, row, col, styleIndex);

        // Billet for formula in rec
        Ptg[] ptgsForCell = {new ExpPtg(range.getFirstRow(), range.getFirstColumn())};
        FormulaRecordAggregate agg = (FormulaRecordAggregate) record;
        agg.setParsedExpression(ptgsForCell);
    }

    public CellRangeAddress getArrayFormulaRange() {
        if (cellType != CELL_TYPE_FORMULA) {
            String ref = new CellReference(this).formatAsString();
            throw new IllegalStateException("Cell " + ref
                    + " is not part of an array formula.");
        }
        return ((FormulaRecordAggregate) record).getArrayFormulaRange();
    }

    public boolean isPartOfArrayFormulaGroup() {
        if (cellType != CELL_TYPE_FORMULA) {
            return false;
        }
        return ((FormulaRecordAggregate) record).isPartOfArrayFormula();
    }

    /**
     * The purpose of this method is to validate the cell state prior to modification.
     *
     * @see #notifyArrayFormulaChanging()
     */
    void notifyArrayFormulaChanging(String msg) {
        CellRangeAddress cra = getArrayFormulaRange();
        if (cra.getNumberOfCells() > 1) {
            throw new IllegalStateException(msg);
        }
        //un-register the single-cell array formula from the parent XSSFSheet
        getRow().getSheet().removeArrayFormula(this);
    }

    /**
     * Called when this cell is modified.
     * <p/>
     * The purpose of this method is to validate the cell state prior to modification.
     *
     * @throws IllegalStateException if modification is not allowed
     * @see #setCellType(int)
     * @see #setCellFormula(String)
     * @see HSSFRow#removeCell(org.apache.poi.ss.usermodel.Cell)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#removeRow(org.apache.poi.ss.usermodel.Row)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#shiftRows(int, int, int)
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#addMergedRegion(org.apache.poi.ss.util.CellRangeAddress)
     */
    void notifyArrayFormulaChanging() {
        CellReference ref = new CellReference(this);
        String msg = "Cell " + ref.formatAsString() + " is part of a multi-cell array formula. " +
                "You cannot change part of an array.";
        notifyArrayFormulaChanging(msg);
    }

    /**
     * Applying a user-defined style (UDS) is special. Excel does not directly reference user-defined styles, but
     * instead create a 'proxy' ExtendedFormatRecord referencing the UDS as parent.
     * <p/>
     * The proceudre to apply a UDS is as follows:
     * <p/>
     * 1. search for a ExtendedFormatRecord with parentIndex == style.getIndex()
     * and xfType ==  ExtendedFormatRecord.XF_CELL.<br>
     * 2. if not found then create a new ExtendedFormatRecord and copy all attributes from the user-defined style
     * and set the parentIndex to be style.getIndex()<br>
     * 3. return the index of the ExtendedFormatRecord, this will be assigned to the parent cell record
     *
     * @param style the user style to apply
     * @return the index of a ExtendedFormatRecord record that will be referenced by the cell
     */
    private short applyUserCellStyle(HSSFCellStyle style) {
        if (style.getUserStyleName() == null) {
            throw new IllegalArgumentException("Expected user-defined style");
        }

        InternalWorkbook iwb = book.getWorkbook();
        short userXf = -1;
        int numfmt = iwb.getNumExFormats();
        for (short i = 0; i < numfmt; i++) {
            ExtendedFormatRecord xf = iwb.getExFormatAt(i);
            if (xf.getXFType() == ExtendedFormatRecord.XF_CELL && xf.getParentIndex() == style.getIndex()) {
                userXf = i;
                break;
            }
        }
        short styleIndex;
        if (userXf == -1) {
            ExtendedFormatRecord xfr = iwb.createCellXF();
            xfr.cloneStyleFrom(iwb.getExFormatAt(style.getIndex()));
            xfr.setIndentionOptions((short) 0);
            xfr.setXFType(ExtendedFormatRecord.XF_CELL);
            xfr.setParentIndex(style.getIndex());
            styleIndex = (short) numfmt;
        } else {
            styleIndex = userXf;
        }

        return styleIndex;
    }
}
