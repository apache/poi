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

package org.apache.poi.xssf.streaming;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellBase;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.*;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * Streaming version of XSSFCell implementing the "BigGridDemo" strategy.
 */
public class SXSSFCell extends CellBase {
    private final SXSSFRow _row;
    private Value _value;
    private CellStyle _style;
    private Property _firstProperty;

    public SXSSFCell(SXSSFRow row, CellType cellType)
    {
        _row=row;
        _value = new BlankValue();
        setType(cellType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    @Override
    public int getColumnIndex()
    {
        return _row.getCellIndex(this);
    }

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    @Override
    public int getRowIndex()
    {
        return _row.getRowNum();
    }

    /**
     * Returns the sheet this cell belongs to
     *
     * @return the sheet this cell belongs to
     */
    @Override
    public SXSSFSheet getSheet()
    {
        return _row.getSheet();
    }

    /**
     * Returns the Row this cell belongs to
     *
     * @return the Row that owns this cell
     */
    @Override
    public Row getRow()
    {
        return _row;
    }

    @Override
    protected void setCellTypeImpl(CellType cellType) {
        ensureType(cellType);
    }

    private boolean isFormulaCell() {
        return _value instanceof FormulaValue;
    }

    /**
     * Return the cell type.
     *
     * @return the cell type
     */
    @Override
    public CellType getCellType() {
        if (isFormulaCell()) {
            return CellType.FORMULA;
        }

        return _value.getType();
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     */
    @Override
    public CellType getCachedFormulaResultType() {
        if (!isFormulaCell()) {
            throw new IllegalStateException("Only formula cells have cached results");
        }

        return ((FormulaValue)_value).getFormulaType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValueImpl(double value) {
        ensureTypeOrFormulaType(CellType.NUMERIC);
        if(_value.getType() == CellType.FORMULA) {
            ((NumericFormulaValue) _value).setPreEvaluatedValue(value);
        } else {
            ((NumericValue)_value).setValue(value);
        }
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
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(RichTextString value) {
        ensureRichTextStringType();

        if(_value instanceof RichTextStringFormulaValue) {
            ((RichTextStringFormulaValue) _value).setPreEvaluatedValue(value);
        } else {
            ((RichTextValue) _value).setValue(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCellValueImpl(String value) {
        ensureTypeOrFormulaType(CellType.STRING);
        if(_value.getType() == CellType.FORMULA) {
            ((StringFormulaValue) _value).setPreEvaluatedValue(value);
        } else {
            ((PlainStringValue) _value).setValue(value);
        }
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
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    @Override
    public void setCellFormulaImpl(String formula) throws FormulaParseException {
        assert formula != null;
        if (getCellType() == CellType.FORMULA) {
            ((FormulaValue)_value).setValue(formula);
        } else {
            switch (getCellType()) {
                case BLANK:
                case NUMERIC:
                    _value = new NumericFormulaValue(formula, getNumericCellValue());
                    break;
                case STRING:
                    if (_value instanceof PlainStringValue) {
                        _value = new StringFormulaValue(formula, getStringCellValue());
                    } else {
                        assert(_value instanceof RichTextValue);
                        _value = new RichTextStringFormulaValue(formula, ((RichTextValue) _value).getValue());
                    }
                    break;
                case BOOLEAN:
                    _value = new BooleanFormulaValue(formula, getBooleanCellValue());
                    break;
                case ERROR:
                    _value = new ErrorFormulaValue(formula, getErrorCellValue());
                    break;
                default:
                    throw new IllegalStateException("Cannot set a formula for a cell of type " + getCellType());
            }
        }
    }

    @Override
    protected void removeFormulaImpl() {
        assert getCellType() == CellType.FORMULA;
        switch (getCachedFormulaResultType()) {
            case NUMERIC:
                double numericValue = ((NumericFormulaValue)_value).getPreEvaluatedValue();
                _value = new NumericValue();
                ((NumericValue) _value).setValue(numericValue);
                break;
            case STRING:
                String stringValue = ((StringFormulaValue)_value).getPreEvaluatedValue();
                _value = new PlainStringValue();
                ((PlainStringValue) _value).setValue(stringValue);
                break;
            case BOOLEAN:
                boolean booleanValue = ((BooleanFormulaValue)_value).getPreEvaluatedValue();
                _value = new BooleanValue();
                ((BooleanValue) _value).setValue(booleanValue);
                break;
            case ERROR:
                byte errorValue = ((ErrorFormulaValue)_value).getPreEvaluatedValue();
                _value = new ErrorValue();
                ((ErrorValue) _value).setValue(errorValue);
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not CellType.FORMULA
     */
    @Override
    public String getCellFormula()
    {
       if(_value.getType()!=CellType.FORMULA)
           throw typeMismatch(CellType.FORMULA,_value.getType(),false);
        return ((FormulaValue)_value).getValue();
    }

    /**
     * Get the value of the cell as a number.
     * <p>
     * For strings we throw an exception. For blank cells we return a 0.
     * For formulas or error cells we return the precalculated value;
     * </p>
     * @return the value of the cell as a number
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CellType.STRING
     * @throws NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see org.apache.poi.ss.usermodel.DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    @Override
    public double getNumericCellValue()
    {
        CellType cellType = getCellType();
        switch(cellType)
        {
            case BLANK:
                return 0.0;
            case FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CellType.NUMERIC)
                      throw typeMismatch(CellType.NUMERIC, CellType.FORMULA, false);
                return ((NumericFormulaValue)_value).getPreEvaluatedValue();
            }
            case NUMERIC:
                return ((NumericValue)_value).getValue();
            default:
                throw typeMismatch(CellType.NUMERIC, cellType, false);
        }
    }

    /**
     * Get the value of the cell as a date.
     * <p>
     * For strings we throw an exception. For blank cells we return a null.
     * </p>
     * @return the value of the cell as a date
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CellType.STRING
     * @throws NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see org.apache.poi.ss.usermodel.DataFormatter for formatting  this date into a string similar to how excel does.
     */
    @Override
    public Date getDateCellValue()
    {
        CellType cellType = getCellType();
        if (cellType == CellType.BLANK)
        {
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
     * @return the value of the cell as a date
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is CellType.STRING
     * @throws NumberFormatException if the cell value isn't a parsable <code>double</code>.
     * @see org.apache.poi.ss.usermodel.DataFormatter for formatting  this date into a string similar to how excel does.
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
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception.
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    @Override
    public RichTextString getRichStringCellValue()
    {
        CellType cellType = getCellType();
        if(getCellType() != CellType.STRING)
            throw typeMismatch(CellType.STRING, cellType, false);

        StringValue sval = (StringValue)_value;
        if(sval.isRichText())
            return ((RichTextValue)_value).getValue();
        else {
            String plainText = getStringCellValue();
            // don't use the creation-helper here as it would spam the log with one line per row
            //return getSheet().getWorkbook().getCreationHelper().createRichTextString(plainText);
            return new XSSFRichTextString(plainText);
        }
    }


    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception.
     * </p>
     * @return the value of the cell as a string
     */
    @Override
    public String getStringCellValue()
    {
        CellType cellType = getCellType();
        switch(cellType)
        {
            case BLANK:
                return "";
            case FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CellType.STRING)
                      throw typeMismatch(CellType.STRING, CellType.FORMULA, false);
                if(_value instanceof RichTextStringFormulaValue) {
                    return ((RichTextStringFormulaValue) _value).getPreEvaluatedValue().getString();
                } else {
                    return ((StringFormulaValue) _value).getPreEvaluatedValue();
                }
            }
            case STRING:
            {
                if(((StringValue)_value).isRichText())
                    return ((RichTextValue)_value).getValue().getString();
                else
                    return ((PlainStringValue)_value).getValue();
            }
            default:
                throw typeMismatch(CellType.STRING, cellType, false);
        }
    }

    /**
     * Set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas, we'll set the
     *        precalculated value, for booleans we'll set its value. For other types, we
     *        will change the cell to a boolean cell and set its value.
     */
    @Override
    public void setCellValue(boolean value)
    {
        ensureTypeOrFormulaType(CellType.BOOLEAN);
        if(_value.getType()==CellType.FORMULA)
            ((BooleanFormulaValue)_value).setPreEvaluatedValue(value);
        else
            ((BooleanValue)_value).setValue(value);
    }

    /**
     * Set a error value for the cell
     *
     * @param value the error value to set this cell to.  For formulas, we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types, we will change the cell to an error
     *        cell and set its value.
     * @see org.apache.poi.ss.usermodel.FormulaError
     */
    @Override
    public void setCellErrorValue(byte value) {
        // for formulas, we want to keep the type and only have an ERROR as formula value
        if(_value.getType()==CellType.FORMULA) {
            _value = new ErrorFormulaValue(getCellFormula(), value);
        } else {
            _value = new ErrorValue(value);
        }
    }

    /**
     * Get the value of the cell as a boolean.
     * <p>
     * For strings, numbers, and errors, we throw an exception. For blank cells we return a false.
     * </p>
     * @return the value of the cell as a boolean
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()}
     *   is not CellType.BOOLEAN, CellType.BLANK or CellType.FORMULA
     */
    @Override
    public boolean getBooleanCellValue()
    {
        CellType cellType = getCellType();
        switch(cellType)
        {
            case BLANK:
                return false;
            case FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CellType.BOOLEAN)
                      throw typeMismatch(CellType.BOOLEAN, CellType.FORMULA, false);
                return ((BooleanFormulaValue)_value).getPreEvaluatedValue();
            }
            case BOOLEAN:
            {
                return ((BooleanValue)_value).getValue();
            }
            default:
                throw typeMismatch(CellType.BOOLEAN, cellType, false);
        }
    }

    /**
     * Get the value of the cell as an error code.
     * <p>
     * For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     * </p>
     *
     * @return the value of the cell as an error code
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't CellType.ERROR
     * @see org.apache.poi.ss.usermodel.FormulaError for error codes
     */
    @Override
    public byte getErrorCellValue()
    {
        CellType cellType = getCellType();
        switch(cellType)
        {
            case BLANK:
                return 0;
            case FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CellType.ERROR)
                      throw typeMismatch(CellType.ERROR, CellType.FORMULA, false);
                return ((ErrorFormulaValue)_value).getPreEvaluatedValue();
            }
            case ERROR:
            {
                return ((ErrorValue)_value).getValue();
            }
            default:
                throw typeMismatch(CellType.ERROR, cellType, false);
        }
    }

    /**
     * <p>Set the style for the cell.  The style should be an CellStyle created/retrieved from
     * the Workbook.</p>
     *
     * <p>To change the style of a cell without affecting other cells that use the same style,
     * use {@link org.apache.poi.ss.util.CellUtil#setCellStyleProperties(Cell, Map)}</p>
     *
     * @param style  reference contained in the workbook.
     * If the value is null then the style information is removed causing the cell to used the default workbook style.
     * @see org.apache.poi.ss.usermodel.Workbook#createCellStyle
     */
    @Override
    public void setCellStyle(CellStyle style)
    {
        _style=style;
    }

    /**
     * Return the cell's style.
     *
     * @return the cell's style. Always not-null. Default cell style has zero index and can be obtained as
     * <code>workbook.getCellStyleAt(0)</code>
     * @see org.apache.poi.ss.usermodel.Workbook#getCellStyleAt(int)
     */
    @Override
    public CellStyle getCellStyle()
    {
        if (_style == null) {
            CellStyle style = getDefaultCellStyleFromColumn();
            if (style == null) {
                SXSSFWorkbook wb = getSheet().getWorkbook();
                style = wb.getCellStyleAt(0);
            }
            return style;
        } else {
            return _style;
        }
    }

    private CellStyle getDefaultCellStyleFromColumn() {
        CellStyle style = null;
        SXSSFSheet sheet = getSheet();
        if (sheet != null) {
            style = sheet.getColumnStyle(getColumnIndex());
        }
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAsActiveCell()
    {
        getSheet().setActiveCell(getAddress());
    }

    /**
     * Assign a comment to this cell
     *
     * @param comment comment associated with this cell
     */
    @Override
    public void setCellComment(Comment comment)
    {
        setProperty(Property.COMMENT,comment);
    }

    /**
     * Returns comment associated with this cell
     *
     * @return comment associated with this cell or <code>null</code> if not found
     */
    @Override
    public Comment getCellComment()
    {
        return (Comment)getPropertyValue(Property.COMMENT);
    }

    /**
     * Removes the comment for this cell, if there is one.
     */
    @Override
    public void removeCellComment()
    {
        removeProperty(Property.COMMENT);
    }

    /**
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    @Override
    public Hyperlink getHyperlink()
    {
        return (Hyperlink)getPropertyValue(Property.HYPERLINK);
    }

    /**
     * Assign a hyperlink to this cell. If the supplied hyperlink is null, the
     * hyperlink for this cell will be removed.
     *
     * @param link hyperlink associated with this cell
     */
    @Override
    public void setHyperlink(Hyperlink link)
    {
        if (link == null) {
            removeHyperlink();
            return;
        }

        setProperty(Property.HYPERLINK,link);

        XSSFHyperlink xssfobj = (XSSFHyperlink)link;
        // Assign to us
        CellReference ref = new CellReference(getRowIndex(), getColumnIndex());
        xssfobj.setCellReference( ref );

        // Add to the lists
        getSheet()._sh.addHyperlink(xssfobj);
    }

    /**
     * Removes the hyperlink for this cell, if there is one.
     */
    @Override
    public void removeHyperlink()
    {
        removeProperty(Property.HYPERLINK);

        getSheet()._sh.removeHyperlink(getRowIndex(), getColumnIndex());
    }

    /**
     * Only valid for array formula cells
     *
     * @return range of the array formula group that the cell belongs to.
     */
// TODO: What is this?
    @NotImplemented
    public CellRangeAddress getArrayFormulaRange()
    {
        return null;
    }

    /**
     * @return <code>true</code> if this cell is part of group of cells having a common array formula.
     */
//TODO: What is this?
    @NotImplemented
    public boolean isPartOfArrayFormulaGroup()
    {
        return false;
    }
//end of interface implementation

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
            case BLANK:
                return "";
            case BOOLEAN:
                return getBooleanCellValue() ? "TRUE" : "FALSE";
            case ERROR:
                return ErrorEval.getText(getErrorCellValue());
            case FORMULA:
                return getCellFormula();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(this)) {
                    DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(getDateCellValue());
                }
                return getNumericCellValue() + "";
            case STRING:
                return getRichStringCellValue().toString();
            default:
                return "Unknown Cell Type: " + getCellType();
        }
    }

    /*package*/ void removeProperty(int type)
    {
        Property current=_firstProperty;
        Property previous=null;
        while(current!=null&&current.getType()!=type)
        {
            previous=current;
            current=current._next;
        }
        if(current!=null)
        {
            if(previous!=null)
            {
                previous._next=current._next;
            }
            else
            {
                _firstProperty=current._next;
            }
        }
    }
    /*package*/ void setProperty(int type,Object value)
    {
        Property current=_firstProperty;
        Property previous=null;
        while(current!=null&&current.getType()!=type)
        {
            previous=current;
            current=current._next;
        }
        if(current!=null)
        {
            current.setValue(value);
        }
        else
        {
            switch(type)
            {
                case Property.COMMENT:
                {
                    current=new CommentProperty(value);
                    break;
                }
                case Property.HYPERLINK:
                {
                    current=new HyperlinkProperty(value);
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException("Invalid type: " + type);
                }
            }
            if(previous!=null)
            {
                previous._next=current;
            }
            else
            {
                _firstProperty=current;
            }
        }
    }
    /*package*/ Object getPropertyValue(int type)
    {
        return getPropertyValue(type,null);
    }
    /*package*/ Object getPropertyValue(int type,String defaultValue)
    {
        Property current=_firstProperty;
        while(current!=null&&current.getType()!=type) current=current._next;
        return current==null?defaultValue:current.getValue();
    }
    /*package*/ void ensurePlainStringType()
    {
        if(_value.getType()!=CellType.STRING
           ||((StringValue)_value).isRichText())
            _value = new PlainStringValue();
    }
    /*package*/ void ensureRichTextStringType()
    {
        // don't change cell type for formulas
        if(_value.getType() == CellType.FORMULA) {
            String formula = ((FormulaValue)_value).getValue();
            _value = new RichTextStringFormulaValue(formula, new XSSFRichTextString(""));
        } else if(_value.getType()!=CellType.STRING ||
                !((StringValue)_value).isRichText()) {
            _value = new RichTextValue();
        }
    }
    /*package*/ void ensureType(CellType type)
    {
        if(_value.getType()!=type)
            setType(type);
    }

    /*
     * Sets the cell type to type if it is different
     */
    /*package*/ void ensureTypeOrFormulaType(CellType type)
    {
        if(_value.getType()==type)
        {
            if(type==CellType.STRING&&((StringValue)_value).isRichText())
                setType(CellType.STRING);
            return;
        }
        if(_value.getType()==CellType.FORMULA)
        {
            if(((FormulaValue)_value).getFormulaType()==type)
                return;
            switch (type) {
                case BOOLEAN:
                    _value = new BooleanFormulaValue(getCellFormula(), false);
                    break;
                case NUMERIC:
                    _value = new NumericFormulaValue(getCellFormula(), 0);
                    break;
                case STRING:
                    _value = new StringFormulaValue(getCellFormula(), "");
                    break;
                case ERROR:
                    _value = new ErrorFormulaValue(getCellFormula(), FormulaError._NO_ERROR.getCode());
                    break;
                default:
                    throw new AssertionError();
            }
            return;
        }
        setType(type);
    }
    /**
     * changes the cell type to the specified type, and resets the value to the default value for that type
     * If cell type is the same as specified type, this will reset the value to the default value for that type
     *
     * @param type the cell type to set
     * @throws IllegalArgumentException if type is not a recognized type
     */
    /*package*/ void setType(CellType type)
    {
        switch(type)
        {
            case NUMERIC:
            {
                _value = new NumericValue();
                break;
            }
            case STRING:
            {
                PlainStringValue sval = new PlainStringValue();
                if(_value != null){
                    // if a cell is not blank then convert the old value to string
                    String str = convertCellValueToString();
                    sval.setValue(str);
                }
                _value = sval;
                break;
            }
            case FORMULA:
            {
                if (getCellType() == CellType.BLANK) {
                    _value = new NumericFormulaValue("", 0);
                }
                break;
            }
            case BLANK:
            {
                _value = new BlankValue();
                break;
            }
            case BOOLEAN:
            {
                BooleanValue bval = new BooleanValue();
                if(_value != null){
                    // if a cell is not blank then convert the old value to string
                    boolean val = convertCellValueToBoolean();
                    bval.setValue(val);
                }
                _value = bval;
                break;
            }
            case ERROR:
            {
                _value = new ErrorValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Illegal type " + type);
            }
        }
    }

//COPIED FROM https://svn.apache.org/repos/asf/poi/trunk/poi-ooxml/src/main/java/org/apache/poi/xssf/usermodel/XSSFCell.java since the functions are declared private there
    /**
     * Used to help format error messages
     */
    private static IllegalStateException typeMismatch(CellType expectedTypeCode, CellType actualTypeCode, boolean isFormulaCell) {
        String msg = "Cannot get a " + expectedTypeCode + " value from a " + actualTypeCode
                + " " + (isFormulaCell ? "formula " : "") + "cell";
        return new IllegalStateException(msg);
    }

    private boolean convertCellValueToBoolean() {
        CellType cellType = getCellType();

        if (cellType == CellType.FORMULA) {
            cellType = getCachedFormulaResultType();
        }

        switch (cellType) {
            case BOOLEAN:
                return getBooleanCellValue();
            case STRING:

                String text = getStringCellValue();
                return Boolean.parseBoolean(text);
            case NUMERIC:
                return getNumericCellValue() != 0;
            case ERROR:
            case BLANK:
                return false;
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }

    }
    private String convertCellValueToString() {
        CellType cellType = getCellType();
        return convertCellValueToString(cellType);
    }
    private String convertCellValueToString(CellType cellType) {
        switch (cellType) {
            case BLANK:
                return "";
            case BOOLEAN:
                return getBooleanCellValue() ? "TRUE" : "FALSE";
            case STRING:
                return getStringCellValue();
            case NUMERIC:
                return Double.toString( getNumericCellValue() );
            case ERROR:
                byte errVal = getErrorCellValue();
                return FormulaError.forInt(errVal).getString();
            case FORMULA:
                if (_value != null) {
                    FormulaValue fv = (FormulaValue)_value;
                    if (fv.getFormulaType() != CellType.FORMULA) {
                        return convertCellValueToString(fv.getFormulaType());
                    }
                }
                return "";
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }
    }

//END OF COPIED CODE

    static abstract class Property
    {
        static final int COMMENT=1;
        static final int HYPERLINK=2;
        Object _value;
        Property _next;
        public Property(Object value)
        {
            _value = value;
        }
        abstract int getType();
        void setValue(Object value)
        {
            _value = value;
        }
        Object getValue()
        {
            return _value;
        }
    }
    static class CommentProperty extends Property
    {
        public CommentProperty(Object value)
        {
            super(value);
        }
        @Override
        public int getType()
        {
            return COMMENT;
        }
    }
    static class HyperlinkProperty extends Property
    {
        public HyperlinkProperty(Object value)
        {
            super(value);
        }
        @Override
        public int getType()
        {
            return HYPERLINK;
        }
    }
    interface Value
    {
        CellType getType();
    }
    static class NumericValue implements Value {
        double _value;

        public NumericValue() {
            _value = 0;
        }

        public NumericValue(double _value) {
            this._value = _value;
        }

        public CellType getType()
        {
            return CellType.NUMERIC;
        }
        void setValue(double value)
        {
            _value = value;
        }
        double getValue()
        {
            return _value;
        }
    }
    static abstract class StringValue implements Value
    {
        public CellType getType()
        {
            return CellType.STRING;
        }
//We cannot introduce a new type CellType.RICH_TEXT because the types are public so we have to make rich text as a type of string
        abstract boolean isRichText(); // using the POI style which seems to avoid "instanceof".
    }
    static class PlainStringValue extends StringValue
    {
        String _value;
        void setValue(String value)
        {
            _value = value;
        }
        String getValue()
        {
            return _value;
        }
        @Override
        boolean isRichText()
        {
            return false;
        }
    }
    static class RichTextValue extends StringValue
    {
        RichTextString _value;
        @Override
        public CellType getType()
        {
            return CellType.STRING;
        }
        void setValue(RichTextString value)
        {
            _value = value;
        }
        RichTextString getValue()
        {
            return _value;
        }
        @Override
        boolean isRichText()
        {
            return true;
        }
    }
    static abstract class FormulaValue implements Value
    {
        String _value;

        public FormulaValue(String _value) {
            this._value = _value;
        }

        public CellType getType()
        {
            return CellType.FORMULA;
        }
        void setValue(String value)
        {
            _value = value;
        }
        String getValue()
        {
            return _value;
        }
        abstract CellType getFormulaType();
    }
    static class NumericFormulaValue extends FormulaValue {
        double _preEvaluatedValue;

        public NumericFormulaValue(String formula, double _preEvaluatedValue) {
            super(formula);
            this._preEvaluatedValue = _preEvaluatedValue;
        }

        @Override
        CellType getFormulaType()
        {
            return CellType.NUMERIC;
        }
        void setPreEvaluatedValue(double value)
        {
            _preEvaluatedValue=value;
        }
        double getPreEvaluatedValue()
        {
            return _preEvaluatedValue;
        }
    }
    static class StringFormulaValue extends FormulaValue {
        String _preEvaluatedValue;

        public StringFormulaValue(String formula, String value) {
            super(formula);
            _preEvaluatedValue = value;
        }

        @Override
        CellType getFormulaType()
        {
            return CellType.STRING;
        }
        void setPreEvaluatedValue(String value)
        {
            _preEvaluatedValue=value;
        }
        String getPreEvaluatedValue()
        {
            return _preEvaluatedValue;
        }
    }

    static class RichTextStringFormulaValue extends FormulaValue {
        RichTextString _preEvaluatedValue;

        public RichTextStringFormulaValue(String formula, RichTextString value) {
            super(formula);
            _preEvaluatedValue = value;
        }

        @Override
        CellType getFormulaType()
        {
            return CellType.STRING;
        }
        void setPreEvaluatedValue(RichTextString value)
        {
            _preEvaluatedValue=value;
        }
        RichTextString getPreEvaluatedValue()
        {
            return _preEvaluatedValue;
        }
    }

    static class BooleanFormulaValue extends FormulaValue {
        boolean _preEvaluatedValue;

        public BooleanFormulaValue(String formula, boolean value) {
            super(formula);
            _preEvaluatedValue = value;
        }

        @Override
        CellType getFormulaType()
        {
            return CellType.BOOLEAN;
        }
        void setPreEvaluatedValue(boolean value)
        {
            _preEvaluatedValue=value;
        }
        boolean getPreEvaluatedValue()
        {
            return _preEvaluatedValue;
        }
    }

    static class ErrorFormulaValue extends FormulaValue {
        byte _preEvaluatedValue;

        public ErrorFormulaValue(String formula, byte value) {
            super(formula);
            _preEvaluatedValue = value;
        }

        @Override
        CellType getFormulaType()
        {
            return CellType.ERROR;
        }
        void setPreEvaluatedValue(byte value)
        {
            _preEvaluatedValue=value;
        }
        byte getPreEvaluatedValue()
        {
            return _preEvaluatedValue;
        }
    }

    static class BlankValue implements Value {
        public CellType getType()
        {
            return CellType.BLANK;
        }
    }

    static class BooleanValue implements Value {
        boolean _value;

        public BooleanValue() {
            _value = false;
        }

        public BooleanValue(boolean _value) {
            this._value = _value;
        }

        public CellType getType()
        {
            return CellType.BOOLEAN;
        }
        void setValue(boolean value)
        {
            _value = value;
        }
        boolean getValue()
        {
            return _value;
        }
    }
    static class ErrorValue implements Value
    {
        byte _value;

        public ErrorValue() {
            _value = FormulaError._NO_ERROR.getCode();
        }

        public ErrorValue(byte _value) {
            this._value = _value;
        }

        public CellType getType()
        {
            return CellType.ERROR;
        }
        void setValue(byte value)
        {
            _value = value;
        }
        byte getValue()
        {
            return _value;
        }
    }
}
