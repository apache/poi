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

package org.apache.poi.xssf.usermodel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * <p>
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot conatin numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * </p>
 * <p>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * </p>
 */
public final class XSSFCell implements Cell {

    private static final String FALSE_AS_STRING = "0";
    private static final String TRUE_AS_STRING  = "1";

    /**
     * the xml bean containing information about the cell's location, value,
     * data type, formatting, and formula
     */
    private final CTCell _cell;

    /**
     * the XSSFRow this cell belongs to
     */
    private final XSSFRow _row;

    /**
     * 0-based column index
     */
    private int _cellNum;

    /**
     * Table of strings shared across this workbook.
     * If two cells contain the same string, then the cell value is the same index into SharedStringsTable
     */
    private SharedStringsTable _sharedStringSource;

    /**
     * Table of cell styles shared across all cells in a workbook.
     */
    private StylesTable _stylesSource;

    /**
     * Construct a XSSFCell.
     *
     * @param row the parent row.
     * @param cell the xml bean containing information about the cell.
     */
    protected XSSFCell(XSSFRow row, CTCell cell) {
        _cell = cell;
        _row = row;
        if (cell.getR() != null) {
            _cellNum = new CellReference(cell.getR()).getCol();
        }
        _sharedStringSource = row.getSheet().getWorkbook().getSharedStringSource();
        _stylesSource = row.getSheet().getWorkbook().getStylesSource();
    }

    /**
     * @return table of strings shared across this workbook
     */
    protected SharedStringsTable getSharedStringSource() {
        return _sharedStringSource;
    }

    /**
     * @return table of cell styles shared across this workbook
     */
    protected StylesTable getStylesSource() {
        return _stylesSource;
    }

    /**
     * Returns the sheet this cell belongs to
     *
     * @return the sheet this cell belongs to
     */
    public XSSFSheet getSheet() {
        return getRow().getSheet();
    }

    /**
     * Returns the row this cell belongs to
     *
     * @return the row this cell belongs to
     */
    public XSSFRow getRow() {
        return _row;
    }

    /**
     * Get the value of the cell as a boolean.
     * <p>
     * For strings, numbers, and errors, we throw an exception. For blank cells we return a false.
     * </p>
     * @return the value of the cell as a boolean
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()}
     *   is not CELL_TYPE_BOOLEAN, CELL_TYPE_BLANK or CELL_TYPE_FORMULA
     */
    public boolean getBooleanCellValue() {
        int cellType = getCellType();
        switch(cellType) {
            case CELL_TYPE_BLANK:
                return false;
            case CELL_TYPE_BOOLEAN:
                return _cell.isSetV() && TRUE_AS_STRING.equals(_cell.getV());
            case CELL_TYPE_FORMULA:
                //YK: should throw an exception if requesting boolean value from a non-boolean formula
                return _cell.isSetV() && TRUE_AS_STRING.equals(_cell.getV());
            default:
                throw typeMismatch(CELL_TYPE_BOOLEAN, cellType, false);
        }
    }

    /**
     * Set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
    public void setCellValue(boolean value) {
        _cell.setT(STCellType.B);
        _cell.setV(value ? TRUE_AS_STRING : FALSE_AS_STRING);
    }

    /**
     * Get the value of the cell as a number.
     * <p>
     * For strings we throw an exception. For blank cells we return a 0.
     * For formulas or error cells we return the precalculated value;
     * </p>
     * @return the value of the cell as a number
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    public double getNumericCellValue() {
        int cellType = getCellType();
        switch(cellType) {
            case CELL_TYPE_BLANK:
                return 0.0;
            case CELL_TYPE_FORMULA:
            case CELL_TYPE_NUMERIC:
                if(_cell.isSetV()) {
                   try {
                      return Double.parseDouble(_cell.getV());
                   } catch(NumberFormatException e) {
                      throw typeMismatch(CELL_TYPE_NUMERIC, CELL_TYPE_STRING, false);
                   }
                } else {
                   return 0.0;
                }
            default:
                throw typeMismatch(CELL_TYPE_NUMERIC, cellType, false);
        }
    }


    /**
     * Set a numeric value for the cell
     *
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(double value) {
        if(Double.isInfinite(value)) {
            // Excel does not support positive/negative infinities,
            // rather, it gives a #DIV/0! error in these cases.
            _cell.setT(STCellType.E);
            _cell.setV(FormulaError.DIV0.getString());
        } else if (Double.isNaN(value)){
            // Excel does not support Not-a-Number (NaN),
            // instead it immediately generates an #NUM! error.
            _cell.setT(STCellType.E);
            _cell.setV(FormulaError.NUM.getString());
        } else {
            _cell.setT(STCellType.N);
            _cell.setV(String.valueOf(value));
        }
    }

    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     * </p>
     * @return the value of the cell as a string
     */
    public String getStringCellValue() {
        XSSFRichTextString str = getRichStringCellValue();
        return str == null ? null : str.getString();
    }

    /**
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    public XSSFRichTextString getRichStringCellValue() {
        int cellType = getCellType();
        XSSFRichTextString rt;
        switch (cellType) {
            case CELL_TYPE_BLANK:
                rt = new XSSFRichTextString("");
                break;
            case CELL_TYPE_STRING:
                if (_cell.getT() == STCellType.INLINE_STR) {
                    if(_cell.isSetIs()) {
                        //string is expressed directly in the cell definition instead of implementing the shared string table.
                        rt = new XSSFRichTextString(_cell.getIs());
                    } else if (_cell.isSetV()) {
                        //cached result of a formula
                        rt = new XSSFRichTextString(_cell.getV());
                    } else {
                        rt = new XSSFRichTextString("");
                    }
                } else if (_cell.getT() == STCellType.STR) {
                    //cached formula value
                    rt = new XSSFRichTextString(_cell.isSetV() ? _cell.getV() : "");
                } else {
                    if (_cell.isSetV()) {
                        int idx = Integer.parseInt(_cell.getV());
                        rt = new XSSFRichTextString(_sharedStringSource.getEntryAt(idx));
                    }
                    else {
                        rt = new XSSFRichTextString("");
                    }
                }
                break;
            case CELL_TYPE_FORMULA:
                checkFormulaCachedValueType(CELL_TYPE_STRING, getBaseCellType(false));
                rt = new XSSFRichTextString(_cell.isSetV() ? _cell.getV() : "");
                break;
            default:
                throw typeMismatch(CELL_TYPE_STRING, cellType, false);
        }
        rt.setStylesTableReference(_stylesSource);
        return rt;
    }

    private static void checkFormulaCachedValueType(int expectedTypeCode, int cachedValueType) {
        if (cachedValueType != expectedTypeCode) {
            throw typeMismatch(expectedTypeCode, cachedValueType, true);
        }
	}

	/**
     * Set a string value for the cell.
     *
     * @param str value to set the cell to.  For formulas we'll set the formula
     * cached string result, for String cells we'll set its value. For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    public void setCellValue(String str) {
        setCellValue(str == null ? null : new XSSFRichTextString(str));
    }

    /**
     * Set a string value for the cell.
     *
     * @param str  value to set the cell to.  For formulas we'll set the 'pre-evaluated result string,
     * for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    public void setCellValue(RichTextString str) {
        if(str == null || str.getString() == null){
            setBlank();
            return;
        }
        int cellType = getCellType();
        switch(cellType){
            case Cell.CELL_TYPE_FORMULA:
                _cell.setV(str.getString());
                _cell.setT(STCellType.STR);
                break;
            default:
                if(_cell.getT() == STCellType.INLINE_STR) {
                    //set the 'pre-evaluated result
                    _cell.setV(str.getString());
                } else {
                    _cell.setT(STCellType.S);
                    XSSFRichTextString rt = (XSSFRichTextString)str;
                    rt.setStylesTableReference(_stylesSource);
                    int sRef = _sharedStringSource.addEntry(rt.getCTRst());
                    _cell.setV(Integer.toString(sRef));
                }
                break;
        }
    }

    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not CELL_TYPE_FORMULA
     */
    public String getCellFormula() {
        int cellType = getCellType();
        if(cellType != CELL_TYPE_FORMULA) throw typeMismatch(CELL_TYPE_FORMULA, cellType, false);

        CTCellFormula f = _cell.getF();
        if (isPartOfArrayFormulaGroup() && f == null) {
            XSSFCell cell = getSheet().getFirstCellInArrayFormula(this);
            return cell.getCellFormula();
        }
        if (f.getT() == STCellFormulaType.SHARED) {
            return convertSharedFormula((int)f.getSi());
        }
        return f.getStringValue();
    }

    /**
     * Creates a non shared formula from the shared formula counterpart
     *
     * @param si Shared Group Index
     * @return non shared formula created for the given shared formula and this cell
     */
    private String convertSharedFormula(int si){
        XSSFSheet sheet = getSheet();

        CTCellFormula f = sheet.getSharedFormula(si);
        if(f == null) throw new IllegalStateException(
                "Master cell of a shared formula with sid="+si+" was not found");

        String sharedFormula = f.getStringValue();
        //Range of cells which the shared formula applies to
        String sharedFormulaRange = f.getRef();

        CellRangeAddress ref = CellRangeAddress.valueOf(sharedFormulaRange);

        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(sheet.getWorkbook());
        Ptg[] ptgs = FormulaParser.parse(sharedFormula, fpb, FormulaType.CELL, sheetIndex);
        Ptg[] fmla = SharedFormulaRecord.convertSharedFormulas(ptgs,
                getRowIndex() - ref.getFirstRow(), getColumnIndex() - ref.getFirstColumn());
        return FormulaRenderer.toFormulaString(fpb, fmla);
    }

    /**
     * Sets formula for this cell.
     * <p>
     * Note, this method only sets the formula string and does not calculate the formula value.
     * To set the precalculated value use {@link #setCellValue(double)} or {@link #setCellValue(String)}
     * </p>
     *
     * @param formula the formula to set, e.g. <code>"SUM(C4:E4)"</code>.
     *  If the argument is <code>null</code> then the current formula is removed.
     * @throws org.apache.poi.ss.formula.FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     * @throws IllegalStateException if the operation is not allowed, for example,
     *  when the cell is a part of a multi-cell array formula
     */
    public void setCellFormula(String formula) {
        if(isPartOfArrayFormulaGroup()){
            notifyArrayFormulaChanging();
        }
        setFormula(formula, FormulaType.CELL);
    }

    /* package */ void setCellArrayFormula(String formula, CellRangeAddress range) {
        setFormula(formula, FormulaType.ARRAY);
        CTCellFormula cellFormula = _cell.getF();
        cellFormula.setT(STCellFormulaType.ARRAY);
        cellFormula.setRef(range.formatAsString());
    }

    private void setFormula(String formula, int formulaType) {
        XSSFWorkbook wb = _row.getSheet().getWorkbook();
        if (formula == null) {
            wb.onDeleteFormula(this);
            if(_cell.isSetF()) _cell.unsetF();
            return;
        }

        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        //validate through the FormulaParser
        FormulaParser.parse(formula, fpb, formulaType, wb.getSheetIndex(getSheet()));

        CTCellFormula f = CTCellFormula.Factory.newInstance();
        f.setStringValue(formula);
        _cell.setF(f);
        if(_cell.isSetV()) _cell.unsetV();
    }

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    public int getColumnIndex() {
        return this._cellNum;
    }

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    public int getRowIndex() {
        return _row.getRowNum();
    }

    /**
     * Returns an A1 style reference to the location of this cell
     *
     * @return A1 style reference to the location of this cell
     */
    public String getReference() {
        return _cell.getR();
    }

    /**
     * Return the cell's style.
     *
     * @return the cell's style.</code>
     */
    public XSSFCellStyle getCellStyle() {
        XSSFCellStyle style = null;
        if(_stylesSource.getNumCellStyles() > 0){
            long idx = _cell.isSetS() ? _cell.getS() : 0;
            style = _stylesSource.getStyleAt((int)idx);
        }
        return style;
    }

    /**
     * Set the style for the cell.  The style should be an XSSFCellStyle created/retreived from
     * the XSSFWorkbook.
     *
     * @param style  reference contained in the workbook.
     * If the value is null then the style information is removed causing the cell to used the default workbook style.
     */
    public void setCellStyle(CellStyle style) {
        if(style == null) {
            if(_cell.isSetS()) _cell.unsetS();
        } else {
            XSSFCellStyle xStyle = (XSSFCellStyle)style;
            xStyle.verifyBelongsToStylesSource(_stylesSource);

            long idx = _stylesSource.putStyle(xStyle);
            _cell.setS(idx);
        }
    }

    /**
     * Return the cell type.
     *
     * @return the cell type
     * @see Cell#CELL_TYPE_BLANK
     * @see Cell#CELL_TYPE_NUMERIC
     * @see Cell#CELL_TYPE_STRING
     * @see Cell#CELL_TYPE_FORMULA
     * @see Cell#CELL_TYPE_BOOLEAN
     * @see Cell#CELL_TYPE_ERROR
     */
    public int getCellType() {

        if (_cell.getF() != null || getSheet().isCellInArrayFormulaContext(this)) {
            return CELL_TYPE_FORMULA;
        }

        return getBaseCellType(true);
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link #CELL_TYPE_NUMERIC}, {@link #CELL_TYPE_STRING},
     *     {@link #CELL_TYPE_BOOLEAN}, {@link #CELL_TYPE_ERROR}) depending
     * on the cached value of the formula
     */
    public int getCachedFormulaResultType() {
        if (_cell.getF() == null) {
            throw new IllegalStateException("Only formula cells have cached results");
        }

        return getBaseCellType(false);
    }

    /**
     * Detect cell type based on the "t" attribute of the CTCell bean
     */
    private int getBaseCellType(boolean blankCells) {
        switch (_cell.getT().intValue()) {
            case STCellType.INT_B:
                return CELL_TYPE_BOOLEAN;
            case STCellType.INT_N:
                if (!_cell.isSetV() && blankCells) {
                    // ooxml does have a separate cell type of 'blank'.  A blank cell gets encoded as
                    // (either not present or) a numeric cell with no value set.
                    // The formula evaluator (and perhaps other clients of this interface) needs to
                    // distinguish blank values which sometimes get translated into zero and sometimes
                    // empty string, depending on context
                    return CELL_TYPE_BLANK;
                }
                return CELL_TYPE_NUMERIC;
            case STCellType.INT_E:
                return CELL_TYPE_ERROR;
            case STCellType.INT_S: // String is in shared strings
            case STCellType.INT_INLINE_STR: // String is inline in cell
            case STCellType.INT_STR:
                 return CELL_TYPE_STRING;
            default:
                throw new IllegalStateException("Illegal cell type: " + this._cell.getT());
        }
    }

    /**
     * Get the value of the cell as a date.
     * <p>
     * For strings we throw an exception. For blank cells we return a null.
     * </p>
     * @return the value of the cell as a date
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CELL_TYPE_STRING
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for formatting  this date into a string similar to how excel does.
     */
    public Date getDateCellValue() {
        int cellType = getCellType();
        if (cellType == CELL_TYPE_BLANK) {
            return null;
        }

        double value = getNumericCellValue();
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        return DateUtil.getJavaDate(value, date1904);
    }

    /**
     * Set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Date value) {
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        setCellValue(DateUtil.getExcelDate(value, date1904));
    }

    /**
     * Set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     * <p>
     * This will set the cell value based on the Calendar's timezone. As Excel
     * does not support timezones this means that both 20:00+03:00 and
     * 20:00-03:00 will be reported as the same value (20:00) even that there
     * are 6 hours difference between the two times. This difference can be
     * preserved by using <code>setCellValue(value.getTime())</code> which will
     * automatically shift the times to the default timezone.
     * </p>
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For othertypes we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Calendar value) {
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        setCellValue( DateUtil.getExcelDate(value, date1904 ));
    }

    /**
     * Returns the error message, such as #VALUE!
     *
     * @return the error message such as #VALUE!
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't CELL_TYPE_ERROR
     * @see FormulaError
     */
    public String getErrorCellString() {
        int cellType = getBaseCellType(true);
        if(cellType != CELL_TYPE_ERROR) throw typeMismatch(CELL_TYPE_ERROR, cellType, false);

        return _cell.getV();
    }
    /**
     * Get the value of the cell as an error code.
     * <p>
     * For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     * </p>
     *
     * @return the value of the cell as an error code
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't CELL_TYPE_ERROR
     * @see FormulaError
     */
    public byte getErrorCellValue() {
        String code = getErrorCellString();
        if (code == null) {
            return 0;
        }

        return FormulaError.forString(code).getCode();
    }

    /**
     * Set a error value for the cell
     *
     * @param errorCode the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     * @see FormulaError
     */
    public void setCellErrorValue(byte errorCode) {
        FormulaError error = FormulaError.forInt(errorCode);
        setCellErrorValue(error);
    }

    /**
     * Set a error value for the cell
     *
     * @param error the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     */
    public void setCellErrorValue(FormulaError error) {
        _cell.setT(STCellType.E);
        _cell.setV(error.getString());
    }

    /**
     * Sets this cell as the active cell for the worksheet.
     */
    public void setAsActiveCell() {
        getSheet().setActiveCell(_cell.getR());
    }

    /**
     * Blanks this cell. Blank cells have no formula or value but may have styling.
     * This method erases all the data previously associated with this cell.
     */
    private void setBlank(){
        CTCell blank = CTCell.Factory.newInstance();
        blank.setR(_cell.getR());
        if(_cell.isSetS()) blank.setS(_cell.getS());
        _cell.set(blank);
    }

    /**
     * Sets column index of this cell
     *
     * @param num column index of this cell
     */
    protected void setCellNum(int num) {
        checkBounds(num);
        _cellNum = num;
        String ref = new CellReference(getRowIndex(), getColumnIndex()).formatAsString();
        _cell.setR(ref);
    }

    /**
     * Set the cells type (numeric, formula or string)
     *
     * @throws IllegalArgumentException if the specified cell type is invalid
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */
    public void setCellType(int cellType) {
        int prevType = getCellType();
       
        if(isPartOfArrayFormulaGroup()){
            notifyArrayFormulaChanging();
        }
        if(prevType == CELL_TYPE_FORMULA && cellType != CELL_TYPE_FORMULA) {
            getSheet().getWorkbook().onDeleteFormula(this);
        }
        
        switch (cellType) {
            case CELL_TYPE_BLANK:
                setBlank();
                break;
            case CELL_TYPE_BOOLEAN:
                String newVal = convertCellValueToBoolean() ? TRUE_AS_STRING : FALSE_AS_STRING;
                _cell.setT(STCellType.B);
                _cell.setV(newVal);
                break;
            case CELL_TYPE_NUMERIC:
                _cell.setT(STCellType.N);
                break;
            case CELL_TYPE_ERROR:
                _cell.setT(STCellType.E);
                break;
            case CELL_TYPE_STRING:
                if(prevType != CELL_TYPE_STRING){
                    String str = convertCellValueToString();
                    XSSFRichTextString rt = new XSSFRichTextString(str);
                    rt.setStylesTableReference(_stylesSource);
                    int sRef = _sharedStringSource.addEntry(rt.getCTRst());
                    _cell.setV(Integer.toString(sRef));
                }
                _cell.setT(STCellType.S);
                break;
            case CELL_TYPE_FORMULA:
                if(!_cell.isSetF()){
                    CTCellFormula f =  CTCellFormula.Factory.newInstance();
                    f.setStringValue("0");
                    _cell.setF(f);
                    if(_cell.isSetT()) _cell.unsetT();
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal cell type: " + cellType);
        }
        if (cellType != CELL_TYPE_FORMULA && _cell.isSetF()) {
			_cell.unsetF();
		}
    }

    /**
     * Returns a string representation of the cell
     * <p>
     * Formula cells return the formula string, rather than the formula result.
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     * </p>
     */
    public String toString() {
        switch (getCellType()) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return getBooleanCellValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_ERROR:
                return ErrorEval.getText(getErrorCellValue());
            case CELL_TYPE_FORMULA:
                return getCellFormula();
            case CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    return sdf.format(getDateCellValue());
                }
                return getNumericCellValue() + "";
            case CELL_TYPE_STRING:
                return getRichStringCellValue().toString();
            default:
                return "Unknown Cell Type: " + getCellType();
        }
    }

    /**
     * Returns the raw, underlying ooxml value for the cell
     * <p>
     * If the cell contains a string, then this value is an index into
     * the shared string table, pointing to the actual string value. Otherwise,
     * the value of the cell is expressed directly in this element. Cells containing formulas express
     * the last calculated result of the formula in this element.
     * </p>
     *
     * @return the raw cell value as contained in the underlying CTCell bean,
     *     <code>null</code> for blank cells.
     */
    public String getRawValue() {
        return _cell.getV();
    }

    /**
     * Used to help format error messages
     */
    private static String getCellTypeName(int cellTypeCode) {
        switch (cellTypeCode) {
            case CELL_TYPE_BLANK:   return "blank";
            case CELL_TYPE_STRING:  return "text";
            case CELL_TYPE_BOOLEAN: return "boolean";
            case CELL_TYPE_ERROR:   return "error";
            case CELL_TYPE_NUMERIC: return "numeric";
            case CELL_TYPE_FORMULA: return "formula";
        }
        return "#unknown cell type (" + cellTypeCode + ")#";
    }

    /**
     * Used to help format error messages
     */
    private static RuntimeException typeMismatch(int expectedTypeCode, int actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a "
            + getCellTypeName(expectedTypeCode) + " value from a "
            + getCellTypeName(actualTypeCode) + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }

    /**
     * @throws RuntimeException if the bounds are exceeded.
     */
    private static void checkBounds(int cellIndex) {
        SpreadsheetVersion v = SpreadsheetVersion.EXCEL2007;
        int maxcol = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();
        if (cellIndex < 0 || cellIndex > maxcol) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex
                    + ").  Allowable column range for " + v.name() + " is (0.."
                    + maxcol + ") or ('A'..'" + v.getLastColumnName() + "')");
        }
    }

    /**
     * Returns cell comment associated with this cell
     *
     * @return the cell comment associated with this cell or <code>null</code>
     */
    public XSSFComment getCellComment() {
        return getSheet().getCellComment(_row.getRowNum(), getColumnIndex());
    }

    /**
     * Assign a comment to this cell. If the supplied comment is null,
     * the comment for this cell will be removed.
     *
     * @param comment the XSSFComment associated with this cell
     */
    public void setCellComment(Comment comment) {
        if(comment == null) {
            removeCellComment();
            return;
        }

        comment.setRow(getRowIndex());
        comment.setColumn(getColumnIndex());
    }

    /**
     * Removes the comment for this cell, if there is one.
    */
    public void removeCellComment() {
        XSSFComment comment = getCellComment();
        if(comment != null){
            String ref = _cell.getR();
            XSSFSheet sh = getSheet();
            sh.getCommentsTable(false).removeComment(ref);
            sh.getVMLDrawing(false).removeCommentShape(getRowIndex(), getColumnIndex());
        }
    }

    /**
     * Returns hyperlink associated with this cell
     *
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    public XSSFHyperlink getHyperlink() {
        return getSheet().getHyperlink(_row.getRowNum(), _cellNum);
    }

    /**
     * Assign a hypelrink to this cell
     *
     * @param hyperlink the hypelrink to associate with this cell
     */
    public void setHyperlink(Hyperlink hyperlink) {
        XSSFHyperlink link = (XSSFHyperlink)hyperlink;

        // Assign to us
        link.setCellReference( new CellReference(_row.getRowNum(), _cellNum).formatAsString() );

        // Add to the lists
        getSheet().setCellHyperlink(link);
    }

    /**
     * Returns the xml bean containing information about the cell's location (reference), value,
     * data type, formatting, and formula
     *
     * @return the xml bean containing information about this cell
     */
    @Internal
    public CTCell getCTCell(){
        return _cell;
    }

    /**
     * Chooses a new boolean value for the cell when its type is changing.<p/>
     *
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(int, Object) should be introduced to avoid this
     */
    private boolean convertCellValueToBoolean() {
        int cellType = getCellType();

        if (cellType == CELL_TYPE_FORMULA) {
            cellType = getBaseCellType(false);
        }

        switch (cellType) {
            case CELL_TYPE_BOOLEAN:
                return TRUE_AS_STRING.equals(_cell.getV());
            case CELL_TYPE_STRING:
                int sstIndex = Integer.parseInt(_cell.getV());
                XSSFRichTextString rt = new XSSFRichTextString(_sharedStringSource.getEntryAt(sstIndex));
                String text = rt.getString();
                return Boolean.parseBoolean(text);
            case CELL_TYPE_NUMERIC:
                return Double.parseDouble(_cell.getV()) != 0;

            case CELL_TYPE_ERROR:
            case CELL_TYPE_BLANK:
                return false;
        }
        throw new RuntimeException("Unexpected cell type (" + cellType + ")");
    }

    private String convertCellValueToString() {
        int cellType = getCellType();

        switch (cellType) {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_BOOLEAN:
                return TRUE_AS_STRING.equals(_cell.getV()) ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                int sstIndex = Integer.parseInt(_cell.getV());
                XSSFRichTextString rt = new XSSFRichTextString(_sharedStringSource.getEntryAt(sstIndex));
                return rt.getString();
            case CELL_TYPE_NUMERIC:
            case CELL_TYPE_ERROR:
                return _cell.getV();
            case CELL_TYPE_FORMULA:
                // should really evaluate, but HSSFCell can't call HSSFFormulaEvaluator
                // just use cached formula result instead
                break;
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }
        cellType = getBaseCellType(false);
        String textValue = _cell.getV();
        switch (cellType) {
            case CELL_TYPE_BOOLEAN:
                if (TRUE_AS_STRING.equals(textValue)) {
                    return "TRUE";
                }
                if (FALSE_AS_STRING.equals(textValue)) {
                    return "FALSE";
                }
                throw new IllegalStateException("Unexpected boolean cached formula value '"
                    + textValue + "'.");
            case CELL_TYPE_STRING:
            case CELL_TYPE_NUMERIC:
            case CELL_TYPE_ERROR:
                return textValue;
        }
        throw new IllegalStateException("Unexpected formula result type (" + cellType + ")");
    }

    public CellRangeAddress getArrayFormulaRange() {
        XSSFCell cell = getSheet().getFirstCellInArrayFormula(this);
        if (cell == null) {
            throw new IllegalStateException("Cell " + _cell.getR()
                    + " is not part of an array formula.");
        }
        String formulaRef = cell._cell.getF().getRef();
        return CellRangeAddress.valueOf(formulaRef);
    }

    public boolean isPartOfArrayFormulaGroup() {
        return getSheet().isCellInArrayFormulaContext(this);
    }

    /**
     * The purpose of this method is to validate the cell state prior to modification
     *
     * @see #notifyArrayFormulaChanging()
     */
    void notifyArrayFormulaChanging(String msg){
        if(isPartOfArrayFormulaGroup()){
            CellRangeAddress cra = getArrayFormulaRange();
            if(cra.getNumberOfCells() > 1) {
                throw new IllegalStateException(msg);
            }
            //un-register the single-cell array formula from the parent XSSFSheet
            getRow().getSheet().removeArrayFormula(this);
        }
    }

    /**
     * Called when this cell is modified.
     * <p>
     * The purpose of this method is to validate the cell state prior to modification.
     * </p>
     *
     * @see #setCellType(int)
     * @see #setCellFormula(String)
     * @see XSSFRow#removeCell(org.apache.poi.ss.usermodel.Cell)
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#removeRow(org.apache.poi.ss.usermodel.Row)
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#shiftRows(int, int, int)
     * @see org.apache.poi.xssf.usermodel.XSSFSheet#addMergedRegion(org.apache.poi.ss.util.CellRangeAddress)
     * @throws IllegalStateException if modification is not allowed
     */
    void notifyArrayFormulaChanging(){
        CellReference ref = new CellReference(this);
        String msg = "Cell "+ref.formatAsString()+" is part of a multi-cell array formula. " +
                "You cannot change part of an array.";
        notifyArrayFormulaChanging(msg);
    }
}
