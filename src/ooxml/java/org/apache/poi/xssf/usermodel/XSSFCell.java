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
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SharedFormula;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Removal;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.CalculationChain;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * <p>
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot contain numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells have the formula string, as well as
 * the formula result, which can be numeric or string.
 * </p>
 * <p>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * </p>
 */
public final class XSSFCell extends CellBase {

    private static final String FALSE_AS_STRING = "0";
    private static final String TRUE_AS_STRING  = "1";
    private static final String FALSE = "FALSE";
    private static final String TRUE = "TRUE";

    /**
     * the xml bean containing information about the cell's location, value,
     * data type, formatting, and formula
     */
    private CTCell _cell;

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
        } else {
            int prevNum = row.getLastCellNum();
            if(prevNum != -1){
                _cellNum = row.getCell(prevNum-1, MissingCellPolicy.RETURN_NULL_AND_BLANK).getColumnIndex() + 1;
            }
        }
        _sharedStringSource = row.getSheet().getWorkbook().getSharedStringSource();
        _stylesSource = row.getSheet().getWorkbook().getStylesSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    /**
     * Copy cell value, formula and style, from srcCell per cell copy policy
     * If srcCell is null, clears the cell value and cell style per cell copy policy
     * 
     * This does not shift references in formulas. Use {@link org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter} to shift references in formulas.
     * 
     * @param srcCell The cell to take value, formula and style from
     * @param policy The policy for copying the information, see {@link CellCopyPolicy}
     * @throws IllegalArgumentException if copy cell style and srcCell is from a different workbook
     */
    @Beta
    @Internal
    public void copyCellFrom(Cell srcCell, CellCopyPolicy policy) {
        // Copy cell value (cell type is updated implicitly)
        if (policy.isCopyCellValue()) {
            if (srcCell != null) {
                CellType copyCellType = srcCell.getCellType();
                if (copyCellType == CellType.FORMULA && !policy.isCopyCellFormula()) {
                    // Copy formula result as value
                    // FIXME: Cached value may be stale
                    copyCellType = srcCell.getCachedFormulaResultType();
                }
                switch (copyCellType) {
                    case NUMERIC:
                        // DataFormat is not copied unless policy.isCopyCellStyle is true
                        if (DateUtil.isCellDateFormatted(srcCell)) {
                            setCellValue(srcCell.getDateCellValue());
                        }
                        else {
                            setCellValue(srcCell.getNumericCellValue());
                        }
                        break;
                    case STRING:
                        setCellValue(srcCell.getStringCellValue());
                        break;
                    case FORMULA:
                        setCellFormula(srcCell.getCellFormula());
                        break;
                    case BLANK:
                        setBlank();
                        break;
                    case BOOLEAN:
                        setCellValue(srcCell.getBooleanCellValue());
                        break;
                    case ERROR:
                        setCellErrorValue(srcCell.getErrorCellValue());
                        break;

                    default:
                        throw new IllegalArgumentException("Invalid cell type " + srcCell.getCellType());
                }
            } else { //srcCell is null
                setBlank();
            }
        }
        
        // Copy CellStyle
        if (policy.isCopyCellStyle()) {
            setCellStyle(srcCell == null ? null : srcCell.getCellStyle());
        }
        
        final Hyperlink srcHyperlink = (srcCell == null) ? null : srcCell.getHyperlink();

        if (policy.isMergeHyperlink()) {
            // if srcCell doesn't have a hyperlink and destCell has a hyperlink, don't clear destCell's hyperlink
            if (srcHyperlink != null) {
                setHyperlink(new XSSFHyperlink(srcHyperlink));
            }
        } else if (policy.isCopyHyperlink()) {
            // overwrite the hyperlink at dest cell with srcCell's hyperlink
            // if srcCell doesn't have a hyperlink, clear the hyperlink (if one exists) at destCell
            setHyperlink(srcHyperlink == null ? null : new XSSFHyperlink(srcHyperlink));
        }
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
    @Override
    public XSSFSheet getSheet() {
        return getRow().getSheet();
    }

    /**
     * Returns the row this cell belongs to
     *
     * @return the row this cell belongs to
     */
    @Override
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
     *   is not {@link CellType#BOOLEAN}, {@link CellType#BLANK} or {@link CellType#FORMULA}
     */
    @Override
    public boolean getBooleanCellValue() {
        CellType cellType = getCellType();
        switch(cellType) {
            case BLANK:
                return false;
            case BOOLEAN:
                return _cell.isSetV() && TRUE_AS_STRING.equals(_cell.getV());
            case FORMULA:
                //YK: should throw an exception if requesting boolean value from a non-boolean formula
                return _cell.isSetV() && TRUE_AS_STRING.equals(_cell.getV());
            default:
                throw typeMismatch(CellType.BOOLEAN, cellType, false);
        }
    }

    /**
     * Set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
    @Override
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
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is {@link CellType#STRING}
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    @Override
    public double getNumericCellValue() {
        CellType valueType = isFormulaCell() ? getCachedFormulaResultType() : getCellType();
        switch(valueType) {
            case BLANK:
                return 0.0;
            case NUMERIC:
                if(_cell.isSetV()) {
                   String v = _cell.getV();
                   if (v.isEmpty()) {
                       return 0.0;
                   }
                   try {
                      return Double.parseDouble(v);
                   } catch(NumberFormatException e) {
                      throw typeMismatch(CellType.NUMERIC, CellType.STRING, false);
                   }
                } else {
                   return 0.0;
                }
            case FORMULA:
                throw new AssertionError();
            default:
                throw typeMismatch(CellType.NUMERIC, valueType, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValueImpl(double value) {
        _cell.setT(STCellType.N);
        _cell.setV(String.valueOf(value));
    }

    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception
     * </p>
     * @return the value of the cell as a string
     */
    @Override
    public String getStringCellValue() {
        return getRichStringCellValue().getString();
    }

    /**
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    @Override
    public XSSFRichTextString getRichStringCellValue() {
        CellType cellType = getCellType();
        XSSFRichTextString rt;
        switch (cellType) {
            case BLANK:
                rt = new XSSFRichTextString("");
                break;
            case STRING:
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
                        try {
                            int idx = Integer.parseInt(_cell.getV());
                            rt = (XSSFRichTextString)_sharedStringSource.getItemAt(idx);
                        } catch(Throwable t) {
                            rt = new XSSFRichTextString("");
                        }
                    } else {
                        rt = new XSSFRichTextString("");
                    }
                }
                break;
            case FORMULA:
                checkFormulaCachedValueType(CellType.STRING, getBaseCellType(false));
                rt = new XSSFRichTextString(_cell.isSetV() ? _cell.getV() : "");
                break;
            default:
                throw typeMismatch(CellType.STRING, cellType, false);
        }
        rt.setStylesTableReference(_stylesSource);
        return rt;
    }

    private static void checkFormulaCachedValueType(CellType expectedTypeCode, CellType cachedValueType) {
        if (cachedValueType != expectedTypeCode) {
            throw typeMismatch(expectedTypeCode, cachedValueType, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(String value) {
        setCellValueImpl(new XSSFRichTextString(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(RichTextString str) {
        CellType cellType = getCellType();
        if (cellType == CellType.FORMULA) {
            _cell.setV(str.getString());
            _cell.setT(STCellType.STR);
        } else {
            if(_cell.getT() == STCellType.INLINE_STR) {
                //set the 'pre-evaluated result
                _cell.setV(str.getString());
            } else {
                _cell.setT(STCellType.S);
                XSSFRichTextString rt = (XSSFRichTextString)str;
                rt.setStylesTableReference(_stylesSource);
                int sRef = _sharedStringSource.addSharedStringItem(rt);
                _cell.setV(Integer.toString(sRef));
            }
        }
    }

    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not {@link CellType#FORMULA}
     */
    @Override
    public String getCellFormula() {
        // existing behavior - create a new XSSFEvaluationWorkbook for every call
        return getCellFormula(null);
    }
    
    /**
     * package/hierarchy use only - reuse an existing evaluation workbook if available for caching
     *
     * @param fpb evaluation workbook for reuse, if available, or null to create a new one as needed
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not {@link CellType#FORMULA}
     */
    protected String getCellFormula(BaseXSSFEvaluationWorkbook fpb) {
        CellType cellType = getCellType();
        if(cellType != CellType.FORMULA) {
            throw typeMismatch(CellType.FORMULA, cellType, false);
        }

        CTCellFormula f = _cell.getF();
        if (isPartOfArrayFormulaGroup()) {
            /* In an excel generated array formula, the formula property might be set, but the string is empty in related cells */
            if (f == null || f.getStringValue().isEmpty()) {
                XSSFCell cell = getSheet().getFirstCellInArrayFormula(this);
                return cell.getCellFormula(fpb);
            }
        }
        if (f == null) {
            return null;
        } else if (f.getT() == STCellFormulaType.SHARED) {
            return convertSharedFormula(Math.toIntExact(f.getSi()),
                    fpb == null ? XSSFEvaluationWorkbook.create(getSheet().getWorkbook()) : fpb);
        } else {
            return f.getStringValue();
        }
    }

    /**
     * Creates a non shared formula from the shared formula counterpart
     *
     * @param si Shared Group Index
     * @return non shared formula created for the given shared formula and this cell
     */
    private String convertSharedFormula(int si, BaseXSSFEvaluationWorkbook fpb){
        XSSFSheet sheet = getSheet();

        CTCellFormula f = sheet.getSharedFormula(si);
        if(f == null) {
            throw new IllegalStateException(
                    "Master cell of a shared formula with sid="+si+" was not found");
        }

        String sharedFormula = f.getStringValue();
        //Range of cells which the shared formula applies to
        String sharedFormulaRange = f.getRef();

        CellRangeAddress ref = CellRangeAddress.valueOf(sharedFormulaRange);

        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        SharedFormula sf = new SharedFormula(SpreadsheetVersion.EXCEL2007);

        Ptg[] ptgs = FormulaParser.parse(sharedFormula, fpb, FormulaType.CELL, sheetIndex, getRowIndex());
        Ptg[] fmla = sf.convertSharedFormulas(ptgs,
                getRowIndex() - ref.getFirstRow(), getColumnIndex() - ref.getFirstColumn());
        return FormulaRenderer.toFormulaString(fpb, fmla);
    }

    /**
     * Sets formula for this cell.
     * <p>
     * Note, this method only sets the formula string and does not calculate the formula value.
     * To set the precalculated value use {@link #setCellValue(double)} or {@link #setCellValue(String)}
     * </p>
     * <p>
     * Note, if there are any shared formulas, his will invalidate any 
     * {@link FormulaEvaluator} instances based on this workbook.
     * </p>
     *
     * @param formula the formula to set, e.g. <code>"SUM(C4:E4)"</code>.
     *  If the argument is <code>null</code> then the current formula is removed.
     * @throws org.apache.poi.ss.formula.FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     * @throws IllegalStateException if the operation is not allowed, for example,
     *  when the cell is a part of a multi-cell array formula
     */
    @Override
    protected void setCellFormulaImpl(String formula) {
        assert formula != null;
        setFormula(formula, FormulaType.CELL);
    }

    /* package */ void setCellArrayFormula(String formula, CellRangeAddress range) {
        setFormula(formula, FormulaType.ARRAY);
        CTCellFormula cellFormula = _cell.getF();
        cellFormula.setT(STCellFormulaType.ARRAY);
        cellFormula.setRef(range.formatAsString());
    }

    private void setFormula(String formula, FormulaType formulaType) {
        XSSFWorkbook wb = _row.getSheet().getWorkbook();
        if (formulaType == FormulaType.ARRAY && formula == null) {
            wb.onDeleteFormula(this);
            if (_cell.isSetF()) {
                _row.getSheet().onDeleteFormula(this, null);
                _cell.unsetF();
            }
            return;
        }

        if (wb.getCellFormulaValidation()) {
            XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
            //validate through the FormulaParser
            FormulaParser.parse(formula, fpb, formulaType, wb.getSheetIndex(getSheet()), getRowIndex());
        }

        CTCellFormula f;
        if (_cell.isSetF()) {
            f = _cell.getF();
            f.setStringValue(formula);
            if(f.getT() == STCellFormulaType.SHARED){
                getRow().getSheet().onReadCell(this);
            }
        } else {
            f = CTCellFormula.Factory.newInstance();
            f.setStringValue(formula);
            _cell.setF(f);
        }
    }

    @Override
    protected void removeFormulaImpl() {
        _row.getSheet().getWorkbook().onDeleteFormula(this);
        if (_cell.isSetF()) {
            _row.getSheet().onDeleteFormula(this, null);
            _cell.unsetF();
        }
    }

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    @Override
    public int getColumnIndex() {
        return this._cellNum;
    }

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    @Override
    public int getRowIndex() {
        return _row.getRowNum();
    }

    /**
     * Returns an A1 style reference to the location of this cell
     *
     * @return A1 style reference to the location of this cell
     */
    public String getReference() {
        String ref = _cell.getR();
        if(ref == null) {
            return getAddress().formatAsString();
        }
        return ref;
    }

    /**
     * Return the cell's style.
     *
     * @return the cell's style.
     */
    @Override
    public XSSFCellStyle getCellStyle() {
        XSSFCellStyle style = null;
        if(_stylesSource.getNumCellStyles() > 0){
            long idx = _cell.isSetS() ? _cell.getS() : 0;
            style = _stylesSource.getStyleAt(Math.toIntExact(idx));
        }
        return style;
    }

    /**
     * <p>Set the style for the cell.  The style should be an XSSFCellStyle created/retreived from
     * the XSSFWorkbook.</p>
     *
     * <p>To change the style of a cell without affecting other cells that use the same style,
     * use {@link org.apache.poi.ss.util.CellUtil#setCellStyleProperties(Cell, java.util.Map)}</p>
     * 
     * @param style  reference contained in the workbook.
     * If the value is null then the style information is removed causing the cell to used the default workbook style.
     * @throws IllegalArgumentException if style belongs to a different styles source (most likely because style is from a different Workbook)
     */
    @Override
    public void setCellStyle(CellStyle style) {
        if(style == null) {
            if(_cell.isSetS()) {
                _cell.unsetS();
            }
        } else {
            XSSFCellStyle xStyle = (XSSFCellStyle)style;
            xStyle.verifyBelongsToStylesSource(_stylesSource);

            long idx = _stylesSource.putStyle(xStyle);
            _cell.setS(idx);
        }
    }
    
    /**
     * POI currently supports these formula types:
     * <ul>
     * <li> {@link STCellFormulaType#NORMAL}
     * <li> {@link STCellFormulaType#SHARED}
     * <li> {@link STCellFormulaType#ARRAY}
     * </ul>
     * POI does not support {@link STCellFormulaType#DATA_TABLE} formulas.
     * @return true if the cell is of a formula type POI can handle
     */
    private boolean isFormulaCell() {
        if ( (_cell.isSetF() && _cell.getF().getT() != STCellFormulaType.DATA_TABLE ) || getSheet().isCellInArrayFormulaContext(this)) {
            return true;
        }
        return false;
    }
    
    /**
     * Return the cell type.  Tables in an array formula return
     * {@link CellType#FORMULA} for all cells, even though the formula is only defined
     * in the OOXML file for the top left cell of the array.
     * <p>
     * NOTE: POI does not support data table formulas.
     * Cells in a data table appear to POI as plain cells typed from their cached value.
     *
     * @return the cell type
     */
    @Override
    public CellType getCellType() {
        if (isFormulaCell()) {
            return CellType.FORMULA;
        }

        return getBaseCellType(true);
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     */
    @Override
    public CellType getCachedFormulaResultType() {
        if (! isFormulaCell()) {
            throw new IllegalStateException("Only formula cells have cached results");
        }

        return getBaseCellType(false);
    }

    /**
     * Detect cell type based on the "t" attribute of the CTCell bean
     */
    private CellType getBaseCellType(boolean blankCells) {
        switch (_cell.getT().intValue()) {
            case STCellType.INT_B:
                return CellType.BOOLEAN;
            case STCellType.INT_N:
                if (!_cell.isSetV() && blankCells) {
                    // ooxml does have a separate cell type of 'blank'.  A blank cell gets encoded as
                    // (either not present or) a numeric cell with no value set.
                    // The formula evaluator (and perhaps other clients of this interface) needs to
                    // distinguish blank values which sometimes get translated into zero and sometimes
                    // empty string, depending on context
                    return CellType.BLANK;
                }
                return CellType.NUMERIC;
            case STCellType.INT_E:
                return CellType.ERROR;
            case STCellType.INT_S: // String is in shared strings
            case STCellType.INT_INLINE_STR: // String is inline in cell
            case STCellType.INT_STR:
                 return CellType.STRING;
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
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is {@link CellType#STRING}
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for formatting  this date into a string similar to how excel does.
     */
    @Override
    public Date getDateCellValue() {
        if (getCellType() == CellType.BLANK) {
            return null;
        }

        double value = getNumericCellValue();
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        return DateUtil.getJavaDate(value, date1904);
    }

    /**
     * Get the value of the cell as a LocalDateTime.
     * <p>
     * For strings we throw an exception. For blank cells we return a null.
     * </p>
     * @return the value of the cell as a LocalDateTime
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is {@link CellType#STRING}
     * @exception NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see DataFormatter for formatting  this date into a string similar to how excel does.
     */
    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        if (getCellType() == CellType.BLANK) {
            return null;
        }

        double value = getNumericCellValue();
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        return DateUtil.getLocalDateTime(value, date1904);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(Date value) {
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        setCellValue(DateUtil.getExcelDate(value, date1904));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(LocalDateTime value) {
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        setCellValue(DateUtil.getExcelDate(value, date1904));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(Calendar value) {
        boolean date1904 = getSheet().getWorkbook().isDate1904();
        setCellValue( DateUtil.getExcelDate(value, date1904 ));
    }

    /**
     * Returns the error message, such as #VALUE!
     *
     * @return the error message such as #VALUE!
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't {@link CellType#ERROR}
     * @see FormulaError
     */
    public String getErrorCellString() throws IllegalStateException {
        CellType cellType = getBaseCellType(true);
        if(cellType != CellType.ERROR) {
            throw typeMismatch(CellType.ERROR, cellType, false);
        }

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
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't {@link CellType #ERROR}
     * @see FormulaError
     */
    @Override
    public byte getErrorCellValue() throws IllegalStateException {
        String code = getErrorCellString();
        if (code == null) {
            return 0;
        }
        try {
            return FormulaError.forString(code).getCode();
        } catch (final IllegalArgumentException e) {
            throw new IllegalStateException("Unexpected error code", e);
        }
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
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public void setAsActiveCell() {
        getSheet().setActiveCell(getAddress());
    }

    /**
     * Blanks this cell. Blank cells have no formula or value but may have styling.
     * This method erases all the data previously associated with this cell.
     */
    private void setBlankPrivate(){
        CTCell blank = CTCell.Factory.newInstance();
        blank.setR(_cell.getR());
        if(_cell.isSetS()) {
            blank.setS(_cell.getS());
        }
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

    @Override
    protected void setCellTypeImpl(CellType cellType) {
        setCellType(cellType, null);
    }
    
    /**
     * Needed by bug #62834, which points out getCellFormula() expects an evaluation context or creates a new one,
     * so if there is one in use, it needs to be carried on through.
     * @param cellType
     * @param evalWb BaseXSSFEvaluationWorkbook already in use, or null if a new implicit one should be used
     */
    protected void setCellType(CellType cellType, BaseXSSFEvaluationWorkbook evalWb) {
        CellType prevType = getCellType();
        if(prevType == CellType.FORMULA && cellType != CellType.FORMULA) {
            if (_cell.isSetF()) {
                _row.getSheet().onDeleteFormula(this, evalWb);
            }
            getSheet().getWorkbook().onDeleteFormula(this);
        }

        switch (cellType) {
            case NUMERIC:
                _cell.setT(STCellType.N);
                break;
            case STRING:
                if(prevType != CellType.STRING){
                    String str = convertCellValueToString();
                    XSSFRichTextString rt = new XSSFRichTextString(str);
                    rt.setStylesTableReference(_stylesSource);
                    int sRef = _sharedStringSource.addSharedStringItem(rt);
                    _cell.setV(Integer.toString(sRef));
                }
                _cell.setT(STCellType.S);
                break;
            case FORMULA:
                if(!_cell.isSetF()){
                    CTCellFormula f =  CTCellFormula.Factory.newInstance();
                    f.setStringValue("0");
                    _cell.setF(f);
                    if(_cell.isSetT()) {
                        _cell.unsetT();
                    }
                }
                break;
            case BLANK:
                setBlankPrivate();
                break;
            case BOOLEAN:
                String newVal = convertCellValueToBoolean() ? TRUE_AS_STRING : FALSE_AS_STRING;
                _cell.setT(STCellType.B);
                _cell.setV(newVal);
                break;

            case ERROR:
                _cell.setT(STCellType.E);
                break;


            default:
                throw new IllegalArgumentException("Illegal cell type: " + cellType);
        }
        if (cellType != CellType.FORMULA && _cell.isSetF()) {
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
    @Override
    public String toString() {
        switch (getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(getDateCellValue());
                }
                return Double.toString(getNumericCellValue());
            case STRING:
                return getRichStringCellValue().toString();
            case FORMULA:
                return getCellFormula();
            case BLANK:
                return "";
            case BOOLEAN:
                return getBooleanCellValue() ? TRUE : FALSE;
            case ERROR:
                return ErrorEval.getText(getErrorCellValue());
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
    private static RuntimeException typeMismatch(CellType expectedType, CellType actualType, boolean isFormulaCell) {
        String msg = "Cannot get a " + expectedType + " value from a " + actualType+ " " + (isFormulaCell ? "formula " : "") + "cell";
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
    @Override
    public XSSFComment getCellComment() {
        return getSheet().getCellComment(new CellAddress(this));
    }

    /**
     * Assign a comment to this cell. If the supplied comment is null,
     * the comment for this cell will be removed.
     *
     * @param comment the XSSFComment associated with this cell
     */
    @Override
    public void setCellComment(Comment comment) {
        if(comment == null) {
            removeCellComment();
            return;
        }

        comment.setAddress(getRowIndex(), getColumnIndex());
    }

    /**
     * Removes the comment for this cell, if there is one.
    */
    @Override
    public void removeCellComment() {
        XSSFComment comment = getCellComment();
        if(comment != null){
            CellAddress ref = new CellAddress(getReference());
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
    @Override
    public XSSFHyperlink getHyperlink() {
        return getSheet().getHyperlink(_row.getRowNum(), _cellNum);
    }

    /**
     * Assign a hyperlink to this cell. If the supplied hyperlink is null, the
     * hyperlink for this cell will be removed.
     *
     * @param hyperlink the hyperlink to associate with this cell
     */
    @Override
    public void setHyperlink(Hyperlink hyperlink) {
        if (hyperlink == null) {
            removeHyperlink();
            return;
        }

        XSSFHyperlink link = (XSSFHyperlink)hyperlink;

        // Assign to us
        link.setCellReference( new CellReference(_row.getRowNum(), _cellNum).formatAsString() );

        // Add to the lists
        getSheet().addHyperlink(link);
    }

    /**
     * Removes the hyperlink for this cell, if there is one.
     */
    @Override
    public void removeHyperlink() {
        getSheet().removeHyperlink(_row.getRowNum(), _cellNum);
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
     * Set a new internal xml bean. This is only for internal use, do not call this from outside!
     * 
     * This is necessary in some rare cases to work around XMLBeans specialties.
     */
    @Internal
    public void setCTCell(CTCell cell) {
        _cell = cell;
    }

    /**
     * Chooses a new boolean value for the cell when its type is changing.<p>
     *
     * Usually the caller is calling setCellType() with the intention of calling
     * setCellValue(boolean) straight afterwards.  This method only exists to give
     * the cell a somewhat reasonable value until the setCellValue() call (if at all).
     * TODO - perhaps a method like setCellTypeAndValue(CellType, Object) should be introduced to avoid this
     *
     * @throws IllegalStateException if cell type cannot be converted to boolean
     */
    private boolean convertCellValueToBoolean() {
        CellType cellType = getCellType();

        if (cellType == CellType.FORMULA) {
            cellType = getBaseCellType(false);
        }

        switch (cellType) {
            case BOOLEAN:
                return TRUE_AS_STRING.equals(_cell.getV());
            case STRING:
                int sstIndex = Integer.parseInt(_cell.getV());
                RichTextString rt = _sharedStringSource.getItemAt(sstIndex);
                String text = rt.getString();
                return Boolean.parseBoolean(text);
            case NUMERIC:
                return Double.parseDouble(_cell.getV()) != 0;

            case ERROR:
                // fall-through
            case BLANK:
                return false;
                
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }
    }

    private String convertCellValueToString() {
        CellType cellType = getCellType();

        switch (cellType) {
            case BLANK:
                return "";
            case BOOLEAN:
                return TRUE_AS_STRING.equals(_cell.getV()) ? TRUE : FALSE;
            case STRING:
                try {
                    int sstIndex = Integer.parseInt(_cell.getV());
                    RichTextString rt = _sharedStringSource.getItemAt(sstIndex);
                    return rt.getString();
                } catch (Throwable t) {
                    return "";
                }
            case NUMERIC:
            case ERROR:
                return _cell.getV();
            case FORMULA:
                // should really evaluate, but HSSFCell can't call HSSFFormulaEvaluator
                // just use cached formula result instead
                break;
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }
        cellType = getBaseCellType(false);
        String textValue = _cell.getV();
        switch (cellType) {
            case BOOLEAN:
                if (TRUE_AS_STRING.equals(textValue)) {
                    return TRUE;
                }
                if (FALSE_AS_STRING.equals(textValue)) {
                    return FALSE;
                }
                throw new IllegalStateException("Unexpected boolean cached formula value '"
                    + textValue + "'.");
                
            case STRING:
                // fall-through
            case NUMERIC:
                // fall-through
            case ERROR:
                return textValue;
                
            default:
                throw new IllegalStateException("Unexpected formula result type (" + cellType + ")");
        }
        
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        XSSFCell cell = getSheet().getFirstCellInArrayFormula(this);
        if (cell == null) {
            throw new IllegalStateException("Cell " + new CellReference(this).formatAsString()
                    + " is not part of an array formula.");
        }
        String formulaRef = cell._cell.getF().getRef();
        return CellRangeAddress.valueOf(formulaRef);
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return getSheet().isCellInArrayFormulaContext(this);
    }

    //Moved from XSSFRow.shift(). Not sure what is purpose.
    public void updateCellReferencesForShifting(String msg){
        if(isPartOfArrayFormulaGroup()) {
            tryToDeleteArrayFormula(msg);
        }

        CalculationChain calcChain = getSheet().getWorkbook().getCalculationChain();
        int sheetId = Math.toIntExact(getSheet().sheet.getSheetId());
    
        //remove the reference in the calculation chain
        if(calcChain != null) calcChain.removeItem(sheetId, getReference());
    
        CTCell ctCell = getCTCell();
        String r = new CellReference(getRowIndex(), getColumnIndex()).formatAsString();
        ctCell.setR(r);
    }
        
}

