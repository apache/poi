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
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Removal;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot contain numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * <p>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * <p>
 */
public class HSSFCell extends CellBase {
    private static final String FILE_FORMAT_NAME  = "BIFF8";
    /**
     * The maximum  number of columns in BIFF8
     */
    public static final int LAST_COLUMN_NUMBER  = SpreadsheetVersion.EXCEL97.getLastColumnIndex(); // 2^8 - 1
    private static final String LAST_COLUMN_NAME  = SpreadsheetVersion.EXCEL97.getLastColumnName();

    public final static short        ENCODING_UNCHANGED          = -1;
    public final static short        ENCODING_COMPRESSED_UNICODE = 0;
    public final static short        ENCODING_UTF_16             = 1;

    private final HSSFWorkbook       _book;
    private final HSSFSheet          _sheet;
    private CellType                 _cellType;
    private HSSFRichTextString       _stringValue;
    private CellValueRecordInterface _record;
    private HSSFComment              _comment;

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     * <p>
     * When the cell is initially created it is set to {@link CellType#BLANK}. Cell types
     * can be changed/overwritten by calling setCellValue with the appropriate
     * type as a parameter although conversions from one type to another may be
     * prohibited.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     *
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(int)
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, int row, short col)
    {
        checkBounds(col);
        _stringValue  = null;
        _book    = book;
        _sheet   = sheet;

        // Relying on the fact that by default the cellType is set to 0 which
        // is different to {@link CellType#BLANK} hence the following method call correctly
        // creates a new blank cell.
        short xfindex = sheet.getSheet().getXFIndexForColAt(col);
        setCellType(CellType.BLANK, false, row, col,xfindex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL97;
    }

    /**
     * Returns the HSSFSheet this cell belongs to
     *
     * @return the HSSFSheet that owns this cell
     */
    public HSSFSheet getSheet() {
        return _sheet;
    }

    /**
     * Returns the HSSFRow this cell belongs to
     *
     * @return the HSSFRow that owns this cell
     */
    public HSSFRow getRow() {
        int rowIndex = getRowIndex();
        return _sheet.getRow(rowIndex);
    }

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     * @param type  - Type of cell
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(int,CellType)
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, int row, short col,
                       CellType type)
    {
        checkBounds(col);
        _cellType     = CellType._NONE; // Force 'setCellType' to create a first Record
        _stringValue  = null;
        _book    = book;
        _sheet   = sheet;

        short xfindex = sheet.getSheet().getXFIndexForColAt(col);
        setCellType(type,false,row,col,xfindex);
    }

    /**
     * Creates an HSSFCell from a CellValueRecordInterface.  HSSFSheet uses this when
     * reading in cells from an existing sheet.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param cval - the Cell Value Record we wish to represent
     */
    protected HSSFCell(HSSFWorkbook book, HSSFSheet sheet, CellValueRecordInterface cval) {
        _record      = cval;
        _cellType    = determineType(cval);
        _stringValue = null;
        _book   = book;
        _sheet  = sheet;
        switch (_cellType)
        {
            case STRING :
                _stringValue = new HSSFRichTextString(book.getWorkbook(), (LabelSSTRecord ) cval);
                break;

            case BLANK :
                break;

            case FORMULA :
                _stringValue=new HSSFRichTextString(((FormulaRecordAggregate) cval).getStringValue());
                break;

            default :
                break;
        }
    }


    /**
     * used internally -- given a cell value record, figure out its type
     */
    private static CellType determineType(CellValueRecordInterface cval) {
        if (cval instanceof FormulaRecordAggregate) {
            return CellType.FORMULA;
        }
        // all others are plain BIFF records
        Record record = ( Record ) cval;
        switch (record.getSid()) {

            case NumberRecord.sid :   return CellType.NUMERIC;
            case BlankRecord.sid :    return CellType.BLANK;
            case LabelSSTRecord.sid : return CellType.STRING;
            case BoolErrRecord.sid :
                BoolErrRecord boolErrRecord = ( BoolErrRecord ) record;

                return boolErrRecord.isBoolean()
                         ? CellType.BOOLEAN
                         : CellType.ERROR;
        }
        throw new RuntimeException("Bad cell value rec (" + cval.getClass().getName() + ")");
    }

    /**
     * Returns the Workbook that this Cell is bound to
     */
    protected InternalWorkbook getBoundWorkbook() {
        return _book.getWorkbook();
    }

    /**
     * @return the (zero based) index of the row containing this cell
     */
    @Override
    public int getRowIndex() {
        return _record.getRow();
    }

    /**
     * Updates the cell record's idea of what
     *  column it belongs in (0 based)
     * @param num the new cell number
     */
    protected void updateCellNum(short num)
    {
        _record.setColumn(num);
    }

    @Override
    public int getColumnIndex() {
        return _record.getColumn() & 0xFFFF;
    }

    @Override
    protected void setCellTypeImpl(CellType cellType) {
        notifyFormulaChanging();

        int row=_record.getRow();
        short col=_record.getColumn();
        short styleIndex=_record.getXFIndex();
        setCellType(cellType, true, row, col, styleIndex);
    }

    /**
     * sets the cell type. The setValue flag indicates whether to bother about
     *  trying to preserve the current value in the new record if one is created.
     *  <p>
     *  The @see #setCellValue method will call this method with false in setValue
     *  since it will overwrite the cell value later
     *
     */

    private void setCellType(CellType cellType, boolean setValue, int row,short col, short styleIndex)
    {
        switch (cellType)
        {

            case FORMULA :
                FormulaRecordAggregate frec;

                if (cellType != _cellType) {
                    frec = _sheet.getSheet().getRowsAggregate().createFormula(row, col);
                } else {
                    frec = (FormulaRecordAggregate) _record;
                    frec.setRow(row);
                    frec.setColumn(col);
                }
                if (getCellType() == CellType.BLANK) {
                    frec.getFormulaRecord().setValue(0);
                }
                frec.setXFIndex(styleIndex);
                _record = frec;
                break;

            case NUMERIC :
                NumberRecord nrec;

                if (cellType != _cellType)
                {
                    nrec = new NumberRecord();
                }
                else
                {
                    nrec = ( NumberRecord ) _record;
                }
                nrec.setColumn(col);
                if (setValue)
                {
                    nrec.setValue(getNumericCellValue());
                }
                nrec.setXFIndex(styleIndex);
                nrec.setRow(row);
                _record = nrec;
                break;

            case STRING :
                LabelSSTRecord lrec;

                if (cellType == _cellType) {
                    lrec = (LabelSSTRecord) _record;
                } else {
                    lrec = new LabelSSTRecord();
                    lrec.setColumn(col);
                    lrec.setRow(row);
                    lrec.setXFIndex(styleIndex);
                }
                if (setValue) {
                    String str = convertCellValueToString();
                    if(str == null) {
                        // bug 55668: don't try to store null-string when formula
                        // results in empty/null value
                        setCellType(CellType.BLANK, false, row, col, styleIndex);
                        return;
                    } else {
                        int sstIndex = _book.getWorkbook().addSSTString(new UnicodeString(str));
                        lrec.setSSTIndex(sstIndex);
                        UnicodeString us = _book.getWorkbook().getSSTString(sstIndex);
                        _stringValue = new HSSFRichTextString();
                        _stringValue.setUnicodeString(us);
                    }
                }
                _record = lrec;
                break;

            case BLANK :
                BlankRecord brec;

                if (cellType != _cellType)
                {
                    brec = new BlankRecord();
                }
                else
                {
                    brec = ( BlankRecord ) _record;
                }
                brec.setColumn(col);

                // During construction the cellStyle may be null for a Blank cell.
                brec.setXFIndex(styleIndex);
                brec.setRow(row);
                _record = brec;
                break;

            case BOOLEAN :
                BoolErrRecord boolRec;

                if (cellType != _cellType)
                {
                    boolRec = new BoolErrRecord();
                }
                else
                {
                    boolRec = ( BoolErrRecord ) _record;
                }
                boolRec.setColumn(col);
                if (setValue)
                {
                    boolRec.setValue(convertCellValueToBoolean());
                }
                boolRec.setXFIndex(styleIndex);
                boolRec.setRow(row);
                _record = boolRec;
                break;

            case ERROR :
                BoolErrRecord errRec;

                if (cellType != _cellType)
                {
                    errRec = new BoolErrRecord();
                }
                else
                {
                    errRec = ( BoolErrRecord ) _record;
                }
                errRec.setColumn(col);
                if (setValue)
                {
                    errRec.setValue(FormulaError.VALUE.getCode());
                }
                errRec.setXFIndex(styleIndex);
                errRec.setRow(row);
                _record = errRec;
                break;
            default :
                throw new IllegalStateException("Invalid cell type: " + cellType);
        }
        if (cellType != _cellType &&
            _cellType != CellType._NONE )  // Special Value to indicate an uninitialized Cell
        {
            _sheet.getSheet().replaceValueRecord(_record);
        }
        _cellType = cellType;
    }

    /**
     * get the cells type (numeric, formula or string)
     */
    @Override
    public CellType getCellType()
    {
        return _cellType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("fallthrough")
    protected void setCellValueImpl(double value) {
        switch (_cellType) {
            default:
                setCellType(CellType.NUMERIC,
                        false,
                        _record.getRow(),
                        _record.getColumn(),
                        _record.getXFIndex());
                // fall through
            case NUMERIC:
                ((NumberRecord)_record).setValue(value);
                break;
            case FORMULA:
                ((FormulaRecordAggregate)_record).setCachedDoubleResult(value);
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>In HSSF, only the number of days is stored. The fractional part is ignored.</p>
     * @see HSSFDateUtil
     * @see org.apache.poi.ss.usermodel.DateUtil
     */
    protected void setCellValueImpl(Date value) {
        setCellValue(HSSFDateUtil.getExcelDate(value, _book.getWorkbook().isUsing1904DateWindowing()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(Calendar value) {
        setCellValue( HSSFDateUtil.getExcelDate(value, _book.getWorkbook().isUsing1904DateWindowing()) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(String value) {
        setCellValueImpl(new HSSFRichTextString(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(RichTextString value) {
        if (_cellType == CellType.FORMULA) {
            // Set the 'pre-evaluated result' for the formula
            // note - formulas do not preserve text formatting.
            FormulaRecordAggregate fr = (FormulaRecordAggregate) _record;
            fr.setCachedStringResult(value.getString());
            // Update our local cache to the un-formatted version
            _stringValue = new HSSFRichTextString(value.getString());

            // All done
            return;
        }

        // If we get here, we're not dealing with a formula,
        //  so handle things as a normal rich text cell

        if (_cellType != CellType.STRING) {
            int row=_record.getRow();
            short col=_record.getColumn();
            short styleIndex=_record.getXFIndex();
            setCellType(CellType.STRING, false, row, col, styleIndex);
        }
        int index;

        HSSFRichTextString hvalue = (HSSFRichTextString) value;
        UnicodeString str = hvalue.getUnicodeString();
        index = _book.getWorkbook().addSSTString(str);
        (( LabelSSTRecord ) _record).setSSTIndex(index);
        _stringValue = hvalue;
        _stringValue.setWorkbookReferences(_book.getWorkbook(), (( LabelSSTRecord ) _record));
        _stringValue.setUnicodeString(_book.getWorkbook().getSSTString(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellFormulaImpl(String formula) {
        assert formula != null;

        int row=_record.getRow();
        short col=_record.getColumn();
        short styleIndex=_record.getXFIndex();

        final CellValue savedValue = readValue();
        int sheetIndex = _book.getSheetIndex(_sheet);
        Ptg[] ptgs = HSSFFormulaParser.parse(formula, _book, FormulaType.CELL, sheetIndex);
        setCellType(CellType.FORMULA, false, row, col, styleIndex);
        FormulaRecordAggregate agg = (FormulaRecordAggregate) _record;
        FormulaRecord frec = agg.getFormulaRecord();
        frec.setOptions((short) 2);

        //only set to default if there is no extended format index already set
        if (agg.getXFIndex() == (short)0) {
            agg.setXFIndex((short) 0x0f);
        }
        agg.setParsedExpression(ptgs);

        restoreValue(savedValue);
    }

    private CellValue readValue() {
        final CellType valueType = getCellType() == CellType.FORMULA ? getCachedFormulaResultType() : getCellType();
        switch (valueType) {
            case NUMERIC:
                return new CellValue(getNumericCellValue());
            case STRING:
                return new CellValue(getStringCellValue());
            case BOOLEAN:
                return CellValue.valueOf(getBooleanCellValue());
            case ERROR:
                return CellValue.getError(getErrorCellValue());
            default:
                throw new IllegalStateException();
        }
    }

    private void restoreValue(CellValue value) {
        switch (value.getCellType()) {
            case NUMERIC:
                setCellValue(value.getNumberValue());
                break;
            case STRING:
                setCellValue(value.getStringValue());
                break;
            case BOOLEAN:
                setCellValue(value.getBooleanValue());
                break;
            case ERROR:
                setCellErrorValue(FormulaError.forInt(value.getErrorValue()));
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void removeFormulaImpl() {
        assert getCellType() == CellType.FORMULA;

        notifyFormulaChanging();

        switch (getCachedFormulaResultType()) {
            case NUMERIC:
                double numericValue = ((FormulaRecordAggregate)_record).getFormulaRecord().getValue();
                _record = new NumberRecord();
                ((NumberRecord)_record).setValue(numericValue);
                _cellType = CellType.NUMERIC;
                break;
            case STRING:
                _record = new NumberRecord();
                ((NumberRecord)_record).setValue(0);
                _cellType = CellType.STRING;
                break;
            case BOOLEAN:
                boolean booleanValue = ((FormulaRecordAggregate)_record).getFormulaRecord().getCachedBooleanValue();
                _record = new BoolErrRecord();
                ((BoolErrRecord)_record).setValue(booleanValue);
                _cellType = CellType.BOOLEAN;
                break;
            case ERROR:
                byte errorValue = (byte) ((FormulaRecordAggregate)_record).getFormulaRecord().getCachedErrorValue();
                _record = new BoolErrRecord();
                ((BoolErrRecord)_record).setValue(errorValue);
                _cellType = CellType.ERROR;
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Should be called any time that a formula could potentially be deleted.
     * Does nothing if this cell currently does not hold a formula
     */
    private void notifyFormulaChanging() {
        if (_record instanceof FormulaRecordAggregate) {
            ((FormulaRecordAggregate)_record).notifyFormulaChanging();
        }
    }

    public String getCellFormula() {
        if (!(_record instanceof FormulaRecordAggregate)) {
            throw typeMismatch(CellType.FORMULA, _cellType, true);
        }
        return HSSFFormulaParser.toFormulaString(_book, ((FormulaRecordAggregate)_record).getFormulaTokens());
    }

    private static RuntimeException typeMismatch(CellType expectedTypeCode, CellType actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a " + expectedTypeCode + " value from a " + actualTypeCode
            + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }
    private static void checkFormulaCachedValueType(CellType expectedTypeCode, FormulaRecord fr) {
        CellType cachedValueType = CellType.forInt(fr.getCachedResultType());
        if (cachedValueType != expectedTypeCode) {
            throw typeMismatch(expectedTypeCode, cachedValueType, true);
        }
    }

    /**
     * Get the value of the cell as a number.
     * For strings we throw an exception.
     * For blank cells we return a 0.
     * See {@link HSSFDataFormatter} for turning this
     *  number into a string similar to that which
     *  Excel would render this number as.
     */
    public double getNumericCellValue() {

        switch(_cellType) {
            case BLANK:
                return 0.0;
            case NUMERIC:
                return ((NumberRecord)_record).getValue();
            default:
                throw typeMismatch(CellType.NUMERIC, _cellType, false);
            case FORMULA:
                break;
        }
        FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
        checkFormulaCachedValueType(CellType.NUMERIC, fr);
        return fr.getValue();
    }

    /**
     * Get the value of the cell as a date.
     * For strings we throw an exception.
     * For blank cells we return a null.
     * See {@link HSSFDataFormatter} for formatting
     *  this date into a string similar to how excel does.
     */
    public Date getDateCellValue() {

        if (_cellType == CellType.BLANK) {
            return null;
        }
        double value = getNumericCellValue();
        if (_book.getWorkbook().isUsing1904DateWindowing()) {
            return HSSFDateUtil.getJavaDate(value, true);
        }
        return HSSFDateUtil.getJavaDate(value, false);
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    public String getStringCellValue()
    {
      HSSFRichTextString str = getRichStringCellValue();
      return str.getString();
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     */
    public HSSFRichTextString getRichStringCellValue() {

        switch(_cellType) {
            case BLANK:
                return new HSSFRichTextString("");
            case STRING:
                return _stringValue;
            default:
                throw typeMismatch(CellType.STRING, _cellType, false);
            case FORMULA:
                break;
        }
        FormulaRecordAggregate fra = ((FormulaRecordAggregate)_record);
        checkFormulaCachedValueType(CellType.STRING, fra.getFormulaRecord());
        String strVal = fra.getStringValue();
        return new HSSFRichTextString(strVal == null ? "" : strVal);
    }

    /**
     * set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
    @SuppressWarnings("fallthrough")
    public void setCellValue(boolean value) {
        int row=_record.getRow();
        short col=_record.getColumn();
        short styleIndex=_record.getXFIndex();

        switch (_cellType) {
            default:
                setCellType(CellType.BOOLEAN, false, row, col, styleIndex);
                // fall through
            case BOOLEAN:
                (( BoolErrRecord ) _record).setValue(value);
                break;
            case FORMULA:
                ((FormulaRecordAggregate)_record).setCachedBooleanResult(value);
                break;
        }
    }

    /**
     * set a error value for the cell
     *
     * @param errorCode the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     *        For error code byte, see {@link FormulaError}.
     * @deprecated 3.15 beta 2. Use {@link #setCellErrorValue(FormulaError)} instead.
     */
    public void setCellErrorValue(byte errorCode) {
        FormulaError error = FormulaError.forInt(errorCode);
        setCellErrorValue(error);
    }
    /**
     * set a error value for the cell
     *
     * @param error the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     */
    @SuppressWarnings("fallthrough")
    public void setCellErrorValue(FormulaError error) {
        int row=_record.getRow();
        short col=_record.getColumn();
        short styleIndex=_record.getXFIndex();
        switch (_cellType) {
            default:
                setCellType(CellType.ERROR, false, row, col, styleIndex);
                // fall through
            case ERROR:
                (( BoolErrRecord ) _record).setValue(error);
                break;
            case FORMULA:
                ((FormulaRecordAggregate)_record).setCachedErrorResult(error.getCode());
                break;
        }
    }


    /**
     * Chooses a new boolean value for the cell when its type is changing.<p>
     *
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(int, Object) should be introduced to avoid this
     */
    private boolean convertCellValueToBoolean() {

        switch (_cellType) {
            case BOOLEAN:
                return (( BoolErrRecord ) _record).getBooleanValue();
            case STRING:
                int sstIndex = ((LabelSSTRecord)_record).getSSTIndex();
                String text = _book.getWorkbook().getSSTString(sstIndex).getString();
                return Boolean.valueOf(text).booleanValue();
            case NUMERIC:
                return ((NumberRecord)_record).getValue() != 0;

            case FORMULA:
                // use cached formula result if it's the right type:
                FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
                checkFormulaCachedValueType(CellType.BOOLEAN, fr);
                return fr.getCachedBooleanValue();
            // Other cases convert to false
            // These choices are not well justified.
            case ERROR:
            case BLANK:
                return false;
        }
        throw new RuntimeException("Unexpected cell type (" + _cellType + ")");
    }
    private String convertCellValueToString() {

        switch (_cellType) {
            case BLANK:
                return "";
            case BOOLEAN:
                return ((BoolErrRecord) _record).getBooleanValue() ? "TRUE" : "FALSE";
            case STRING:
                int sstIndex = ((LabelSSTRecord)_record).getSSTIndex();
                return _book.getWorkbook().getSSTString(sstIndex).getString();
            case NUMERIC:
                return NumberToTextConverter.toText(((NumberRecord)_record).getValue());
            case ERROR:
                   return FormulaError.forInt(((BoolErrRecord)_record).getErrorValue()).getString();
            case FORMULA:
                // should really evaluate, but HSSFCell can't call HSSFFormulaEvaluator
                // just use cached formula result instead
                break;
            default:
                throw new IllegalStateException("Unexpected cell type (" + _cellType + ")");
        }
        FormulaRecordAggregate fra = ((FormulaRecordAggregate)_record);
        FormulaRecord fr = fra.getFormulaRecord();
        switch (CellType.forInt(fr.getCachedResultType())) {
            case BOOLEAN:
                return fr.getCachedBooleanValue() ? "TRUE" : "FALSE";
            case STRING:
                return fra.getStringValue();
            case NUMERIC:
                return NumberToTextConverter.toText(fr.getValue());
            case ERROR:
                return FormulaError.forInt(fr.getCachedErrorValue()).getString();
            default:
                throw new IllegalStateException("Unexpected formula result type (" + _cellType + ")");
        }
        
    }

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */
    @Override
    public boolean getBooleanCellValue() {

        switch(_cellType) {
            case BLANK:
                return false;
            case BOOLEAN:
                return (( BoolErrRecord ) _record).getBooleanValue();
            case FORMULA:
                break;
            default:
                throw typeMismatch(CellType.BOOLEAN, _cellType, false);
        }
        FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
        checkFormulaCachedValueType(CellType.BOOLEAN, fr);
        return fr.getCachedBooleanValue();
    }

    /**
     * get the value of the cell as an error code.  For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     */
    @Override
    public byte getErrorCellValue() {
        switch(_cellType) {
            case ERROR:
                return (( BoolErrRecord ) _record).getErrorValue();
            case FORMULA:
                FormulaRecord fr = ((FormulaRecordAggregate)_record).getFormulaRecord();
                checkFormulaCachedValueType(CellType.ERROR, fr);
                return (byte) fr.getCachedErrorValue();
            default:
                throw typeMismatch(CellType.ERROR, _cellType, false);
        }
    }

    /**
     * <p>Set the style for the cell.  The style should be an HSSFCellStyle created/retreived from
     * the HSSFWorkbook.</p>
     * 
     * <p>To change the style of a cell without affecting other cells that use the same style,
     * use {@link org.apache.poi.ss.util.CellUtil#setCellStyleProperties(org.apache.poi.ss.usermodel.Cell, java.util.Map)}</p>
     *
     * @param style  reference contained in the workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(int)
     */
    public void setCellStyle(CellStyle style) {
        setCellStyle( (HSSFCellStyle)style );
    }
    public void setCellStyle(HSSFCellStyle style) {
        // A style of null means resetting back to the default style
        if (style == null) {
            _record.setXFIndex((short)0xf);
            return;
        }

        // Verify the style really does belong to our workbook
        style.verifyBelongsToWorkbook(_book);

        short styleIndex;
        if(style.getUserStyleName() != null) {
            styleIndex = applyUserCellStyle(style);
        } else {
            styleIndex = style.getIndex();
        }

        // Change our cell record to use this style
        _record.setXFIndex(styleIndex);
    }

    /**
     * get the style for the cell.  This is a reference to a cell style contained in the workbook
     * object.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(int)
     */
    public HSSFCellStyle getCellStyle()
    {
      short styleIndex=_record.getXFIndex();
      ExtendedFormatRecord xf = _book.getWorkbook().getExFormatAt(styleIndex);
      return new HSSFCellStyle(styleIndex, xf, _book);
    }

    /**
     * Should only be used by HSSFSheet and friends.  Returns the low level CellValueRecordInterface record
     *
     * @return CellValueRecordInterface representing the cell via the low level api.
     */

    protected CellValueRecordInterface getCellValueRecord()
    {
        return _record;
    }

    /**
     * @throws RuntimeException if the bounds are exceeded.
     */
    private static void checkBounds(int cellIndex) {
        if (cellIndex < 0 || cellIndex > LAST_COLUMN_NUMBER) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex
                    + ").  Allowable column range for " + FILE_FORMAT_NAME + " is (0.."
                    + LAST_COLUMN_NUMBER + ") or ('A'..'" + LAST_COLUMN_NAME + "')");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAsActiveCell()
    {
        int row=_record.getRow();
        short col=_record.getColumn();
        _sheet.getSheet().setActiveCellRow(row);
        _sheet.getSheet().setActiveCellCol(col);
    }

    /**
     * Returns a string representation of the cell
     *
     * This method returns a simple representation,
     * anything more complex should be in user code, with
     * knowledge of the semantics of the sheet being processed.
     *
     * Formula cells return the formula string,
     * rather than the formula result.
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     */
    public String toString() {
        switch (getCellTypeEnum()) {
            case BLANK:
                return "";
            case BOOLEAN:
                return getBooleanCellValue()?"TRUE":"FALSE";
            case ERROR:
                return ErrorEval.getText((( BoolErrRecord ) _record).getErrorValue());
            case FORMULA:
                return getCellFormula();
            case NUMERIC:
                //TODO apply the dataformat for this cell
                if (HSSFDateUtil.isCellDateFormatted(this)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(getDateCellValue());
                }
				return  String.valueOf(getNumericCellValue());
            case STRING:
                return getStringCellValue();
            default:
                return "Unknown Cell Type: " + getCellType();
        }
    }

    /**
     * Assign a comment to this cell. If the supplied
     *  comment is null, the comment for this cell
     *  will be removed.
     *
     * @param comment comment associated with this cell
     */
    public void setCellComment(Comment comment){
        if(comment == null) {
            removeCellComment();
            return;
        }

        comment.setRow(_record.getRow());
        comment.setColumn(_record.getColumn());
        _comment = (HSSFComment)comment;
    }

    /**
     * Returns comment associated with this cell
     *
     * @return comment associated with this cell
     */
     public HSSFComment getCellComment(){
        if (_comment == null) {
            _comment = _sheet.findCellComment(_record.getRow(), _record.getColumn());
        }
        return _comment;
    }

    /**
     * Removes the comment for this cell, if
     *  there is one.
     * WARNING - some versions of excel will loose
     *  all comments after performing this action!
     */
    public void removeCellComment() {
        HSSFComment comment = _sheet.findCellComment(_record.getRow(), _record.getColumn());
        _comment = null;
        if (null == comment){
            return;
        }
        _sheet.getDrawingPatriarch().removeShape(comment);
    }

    /**
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    @Override
    public HSSFHyperlink getHyperlink(){
        return _sheet.getHyperlink(_record.getRow(), _record.getColumn());
    }

    /**
     * Assign a hyperlink to this cell. If the supplied hyperlink is null, the
     * hyperlink for this cell will be removed.
     *
     * @param hyperlink hyperlink associated with this cell
     */
    @Override
    public void setHyperlink(Hyperlink hyperlink){
        if (hyperlink == null) {
            removeHyperlink();
            return;
        }

        HSSFHyperlink link = (HSSFHyperlink)hyperlink;

        link.setFirstRow(_record.getRow());
        link.setLastRow(_record.getRow());
        link.setFirstColumn(_record.getColumn());
        link.setLastColumn(_record.getColumn());

        switch(link.getTypeEnum()){
            case EMAIL:
            case URL:
                link.setLabel("url");
                break;
            case FILE:
                link.setLabel("file");
                break;
            case DOCUMENT:
                link.setLabel("place");
                break;
            default:
                break;
        }

        List<RecordBase> records = _sheet.getSheet().getRecords();
        int eofLoc = records.size() - 1;
        records.add( eofLoc, link.record );
    }

    /**
     * Removes the hyperlink for this cell, if there is one.
     */
    public void removeHyperlink() {
        for (Iterator<RecordBase> it = _sheet.getSheet().getRecords().iterator(); it.hasNext();) {
            RecordBase rec = it.next();
            if (rec instanceof HyperlinkRecord) {
                HyperlinkRecord link = (HyperlinkRecord) rec;
                if (link.getFirstColumn() == _record.getColumn() && link.getFirstRow() == _record.getRow()) {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     *
     * @since POI 4.0
     */
    @Override
    public CellType getCachedFormulaResultType() {
        if (_cellType != CellType.FORMULA) {
            throw new IllegalStateException("Only formula cells have cached results");
        }
        int code = ((FormulaRecordAggregate)_record).getFormulaRecord().getCachedResultType();
        return CellType.forInt(code);
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     * @since POI 3.15 beta 3
     * @deprecated use <code>getCachedFormulaResultType</code>
     * Will be deleted when we make the CellType enum transition. See bug 59791.
     */
    @Deprecated
    @Removal(version="4.2")
    @Override
    public CellType getCachedFormulaResultTypeEnum() {
        return getCachedFormulaResultType();
    }

    void setCellArrayFormula(CellRangeAddress range) {
        int row = _record.getRow();
        short col = _record.getColumn();
        short styleIndex = _record.getXFIndex();
        setCellType(CellType.FORMULA, false, row, col, styleIndex);

        // Billet for formula in rec
        Ptg[] ptgsForCell = {new ExpPtg(range.getFirstRow(), range.getFirstColumn())};
        FormulaRecordAggregate agg = (FormulaRecordAggregate) _record;
        agg.setParsedExpression(ptgsForCell);
    }

    public CellRangeAddress getArrayFormulaRange() {
        if (_cellType != CellType.FORMULA) {
            String ref = new CellReference(this).formatAsString();
            throw new IllegalStateException("Cell " + ref
                    + " is not part of an array formula.");
        }
        return ((FormulaRecordAggregate)_record).getArrayFormulaRange();
    }

    public boolean isPartOfArrayFormulaGroup() {
        return _cellType == CellType.FORMULA && ((FormulaRecordAggregate) _record).isPartOfArrayFormula();
    }

    /**
     * Applying a user-defined style (UDS) is special. Excel does not directly reference user-defined styles, but
     * instead create a 'proxy' ExtendedFormatRecord referencing the UDS as parent.
     *
     * The proceudre to apply a UDS is as follows:
     *
     * 1. search for a ExtendedFormatRecord with parentIndex == style.getIndex()
     *    and xfType ==  ExtendedFormatRecord.XF_CELL.
     * 2. if not found then create a new ExtendedFormatRecord and copy all attributes from the user-defined style
     *    and set the parentIndex to be style.getIndex()
     * 3. return the index of the ExtendedFormatRecord, this will be assigned to the parent cell record
     *
     * @param style  the user style to apply
     *
     * @return  the index of a ExtendedFormatRecord record that will be referenced by the cell
     */
    private short applyUserCellStyle(HSSFCellStyle style){
        if(style.getUserStyleName() == null) {
            throw new IllegalArgumentException("Expected user-defined style");
        }

        InternalWorkbook iwb = _book.getWorkbook();
        short userXf = -1;
        int numfmt = iwb.getNumExFormats();
        for(short i = 0; i < numfmt; i++){
            ExtendedFormatRecord xf = iwb.getExFormatAt(i);
            if(xf.getXFType() == ExtendedFormatRecord.XF_CELL && xf.getParentIndex() == style.getIndex() ){
                userXf = i;
                break;
            }
        }
        short styleIndex;
        if (userXf == -1){
            ExtendedFormatRecord xfr = iwb.createCellXF();
            xfr.cloneStyleFrom(iwb.getExFormatAt(style.getIndex()));
            xfr.setIndentionOptions((short)0);
            xfr.setXFType(ExtendedFormatRecord.XF_CELL);
            xfr.setParentIndex(style.getIndex());
            styleIndex = (short)numfmt;
        } else {
            styleIndex = userXf;
        }

        return styleIndex;
    }
}

