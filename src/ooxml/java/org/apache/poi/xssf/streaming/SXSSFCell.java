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
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 * Streaming version of XSSFRow implementing the "BigGridDemo" strategy.
 *
 * @author Alex Geller, Four J's Development Tools
*/
public class SXSSFCell implements Cell 
{

    SXSSFRow _row;
    Value _value;
    CellStyle _style;
    Property _firstProperty;

    public SXSSFCell(SXSSFRow row,int cellType)
    {
        _row=row;
        setType(cellType);
    }

//start of interface implementation

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    public int getColumnIndex()
    {
        return _row.getCellIndex(this);
    }

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    public int getRowIndex()
    {
        return _row.getRowNum();
    }

    /**
     * Returns the sheet this cell belongs to
     *
     * @return the sheet this cell belongs to
     */
    public Sheet getSheet()
    {
        return _row.getSheet();
    }

    /**
     * Returns the Row this cell belongs to
     *
     * @return the Row that owns this cell
     */
     public Row getRow()
     {
         return _row;
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
    public void setCellType(int cellType)
    {
        ensureType(cellType);
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
    public int getCellType()
    {
        return _value.getType();
    }

    /**
     * Only valid for formula cells
     * @return one of ({@link #CELL_TYPE_NUMERIC}, {@link #CELL_TYPE_STRING},
     *     {@link #CELL_TYPE_BOOLEAN}, {@link #CELL_TYPE_ERROR}) depending
     * on the cached value of the formula
     */
    public int getCachedFormulaResultType()
    {
        if (_value.getType() != CELL_TYPE_FORMULA) {
            throw new IllegalStateException("Only formula cells have cached results");
        }

        return ((FormulaValue)_value).getFormulaType();
    }

    /**
     * Set a numeric value for the cell
     *
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(double value)
    {
        if(Double.isInfinite(value)) {
            // Excel does not support positive/negative infinities,
            // rather, it gives a #DIV/0! error in these cases.
            setCellErrorValue(FormulaError.DIV0.getCode());
        } else if (Double.isNaN(value)){
            setCellErrorValue(FormulaError.NUM.getCode());
        } else {
            ensureTypeOrFormulaType(CELL_TYPE_NUMERIC);
            if(_value.getType()==CELL_TYPE_FORMULA)
                ((NumericFormulaValue)_value).setPreEvaluatedValue(value);
            else
                ((NumericValue)_value).setValue(value);
        }
    }

    /**
     * Converts the supplied date to its equivalent Excel numeric value and sets
     * that into the cell.
     * <p/>
     * <b>Note</b> - There is actually no 'DATE' cell type in Excel. In many
     * cases (when entering date values), Excel automatically adjusts the
     * <i>cell style</i> to some date format, creating the illusion that the cell
     * data type is now something besides {@link Cell#CELL_TYPE_NUMERIC}.  POI
     * does not attempt to replicate this behaviour.  To make a numeric cell
     * display as a date, use {@link #setCellStyle(CellStyle)} etc.
     *
     * @param value the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numerics cell and set its value.
     */
    public void setCellValue(Date value)
    {
//TODO: activate this when compiling against 3.7.
        //boolean date1904 = getSheet().getXSSFWorkbook().isDate1904();
        boolean date1904 = false;
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
    public void setCellValue(Calendar value)
    {
//TODO: activate this when compiling against 3.7.
        //boolean date1904 = getSheet().getXSSFWorkbook().isDate1904();
        boolean date1904 = false;
        setCellValue( DateUtil.getExcelDate(value, date1904 ));
    }

    /**
     * Set a rich string value for the cell.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    public void setCellValue(RichTextString value)
    {
        ensureRichTextStringType();
        ((RichTextValue)_value).setValue(value);
    }

    /**
     * Set a string value for the cell.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    public void setCellValue(String value)
    {
        ensureTypeOrFormulaType(CELL_TYPE_STRING);
        if(_value.getType()==CELL_TYPE_FORMULA)
            ((StringFormulaValue)_value).setPreEvaluatedValue(value);
        else
            ((PlainStringValue)_value).setValue(value);
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
    public void setCellFormula(String formula) throws FormulaParseException
    {
        if(formula == null) {
            setType(Cell.CELL_TYPE_BLANK);
            return;
        }

        ensureFormulaType(computeTypeFromFormula(formula));
        ((FormulaValue)_value).setValue(formula);
    }
    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not CELL_TYPE_FORMULA
     */
    public String getCellFormula()
    {
       if(_value.getType()!=CELL_TYPE_FORMULA)
           throw typeMismatch(CELL_TYPE_FORMULA,_value.getType(),false);
        return ((FormulaValue)_value).getValue();
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
     * @see org.apache.poi.ss.usermodel.DataFormatter for turning this number into a string similar to that which Excel would render this number as.
     */
    public double getNumericCellValue()
    {
        int cellType = getCellType();
        switch(cellType) 
        {
            case CELL_TYPE_BLANK:
                return 0.0;
            case CELL_TYPE_FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CELL_TYPE_NUMERIC)
                      throw typeMismatch(CELL_TYPE_NUMERIC, CELL_TYPE_FORMULA, false);
                return ((NumericFormulaValue)_value).getPreEvaluatedValue();
            }
            case CELL_TYPE_NUMERIC:
                return ((NumericValue)_value).getValue();
            default:
                throw typeMismatch(CELL_TYPE_NUMERIC, cellType, false);
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
     * @see org.apache.poi.ss.usermodel.DataFormatter for formatting  this date into a string similar to how excel does.
     */
    public Date getDateCellValue()
    {
        int cellType = getCellType();
        if (cellType == CELL_TYPE_BLANK) 
        {
            return null;
        }

        double value = getNumericCellValue();
//TODO: activate this when compiling against 3.7.
        //boolean date1904 = getSheet().getXSSFWorkbook().isDate1904();
        boolean date1904 = false;
        return DateUtil.getJavaDate(value, date1904);
    }

    /**
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception.
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    public RichTextString getRichStringCellValue()
    {
        int cellType = getCellType();
        if(getCellType() != CELL_TYPE_STRING)
            throw typeMismatch(CELL_TYPE_STRING, cellType, false);

        StringValue sval = (StringValue)_value;
        if(sval.isRichText())
            return ((RichTextValue)_value).getValue();
        else {
            String plainText = getStringCellValue();
            return getSheet().getWorkbook().getCreationHelper().createRichTextString(plainText);
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
    public String getStringCellValue()
    {
        int cellType = getCellType();
        switch(cellType) 
        {
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CELL_TYPE_STRING)
                      throw typeMismatch(CELL_TYPE_STRING, CELL_TYPE_FORMULA, false);
                return ((StringFormulaValue)_value).getPreEvaluatedValue();
            }
            case CELL_TYPE_STRING:
            {
                if(((StringValue)_value).isRichText())
                    return ((RichTextValue)_value).getValue().getString();
                else
                    return ((PlainStringValue)_value).getValue();
            }
            default:
                throw typeMismatch(CELL_TYPE_STRING, cellType, false);
        }
    }

    /**
     * Set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
    public void setCellValue(boolean value)
    {
        ensureTypeOrFormulaType(CELL_TYPE_BOOLEAN);
        if(_value.getType()==CELL_TYPE_FORMULA)
            ((BooleanFormulaValue)_value).setPreEvaluatedValue(value);
        else
            ((BooleanValue)_value).setValue(value);
    }

    /**
     * Set a error value for the cell
     *
     * @param value the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     * @see org.apache.poi.ss.usermodel.FormulaError
     */
    public void setCellErrorValue(byte value)
    {
        ensureType(CELL_TYPE_ERROR);
        if(_value.getType()==CELL_TYPE_FORMULA)
            ((ErrorFormulaValue)_value).setPreEvaluatedValue(value);
        else
            ((ErrorValue)_value).setValue(value);
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
    public boolean getBooleanCellValue()
    {
        int cellType = getCellType();
        switch(cellType) 
        {
            case CELL_TYPE_BLANK:
                return false;
            case CELL_TYPE_FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CELL_TYPE_BOOLEAN)
                      throw typeMismatch(CELL_TYPE_BOOLEAN, CELL_TYPE_FORMULA, false);
                return ((BooleanFormulaValue)_value).getPreEvaluatedValue();
            }
            case CELL_TYPE_BOOLEAN:
            {
                return ((BooleanValue)_value).getValue();
            }
            default:
                throw typeMismatch(CELL_TYPE_BOOLEAN, cellType, false);
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
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't CELL_TYPE_ERROR
     * @see org.apache.poi.ss.usermodel.FormulaError for error codes
     */
    public byte getErrorCellValue()
    {
        int cellType = getCellType();
        switch(cellType) 
        {
            case CELL_TYPE_BLANK:
                return 0;
            case CELL_TYPE_FORMULA:
            {
                FormulaValue fv=(FormulaValue)_value;
                if(fv.getFormulaType()!=CELL_TYPE_ERROR)
                      throw typeMismatch(CELL_TYPE_ERROR, CELL_TYPE_FORMULA, false);
                return ((ErrorFormulaValue)_value).getPreEvaluatedValue();
            }
            case CELL_TYPE_ERROR:
            {
                return ((ErrorValue)_value).getValue();
            }
            default:
                throw typeMismatch(CELL_TYPE_ERROR, cellType, false);
        }
    }

    /**
     * Set the style for the cell.  The style should be an CellStyle created/retreived from
     * the Workbook.
     *
     * @param style  reference contained in the workbook.
     * If the value is null then the style information is removed causing the cell to used the default workbook style.
     * @see org.apache.poi.ss.usermodel.Workbook#createCellStyle()
     */
    public void setCellStyle(CellStyle style)
    {
        _style=style;
    }

    /**
     * Return the cell's style.
     *
     * @return the cell's style. Always not-null. Default cell style has zero index and can be obtained as
     * <code>workbook.getCellStyleAt(0)</code>
     * @see org.apache.poi.ss.usermodel.Workbook#getCellStyleAt(short)
     */
    public CellStyle getCellStyle()
    {
        if(_style == null){
            SXSSFWorkbook wb = (SXSSFWorkbook)getRow().getSheet().getWorkbook();
            return wb.getCellStyleAt((short)0);
        } else {
            return _style;
        }
    }

    /**
     * Sets this cell as the active cell for the worksheet
     */
    public void setAsActiveCell()
    {
//TODO: What needs to be done here? Is there a "the active cell" at the sheet or even the workbook level?
        //getRow().setAsActiveCell(this);
    }

    /**
     * Assign a comment to this cell
     *
     * @param comment comment associated with this cell
     */
    public void setCellComment(Comment comment)
    {
        setProperty(Property.COMMENT,comment);
    }

    /**
     * Returns comment associated with this cell
     *
     * @return comment associated with this cell or <code>null</code> if not found
     */
    public Comment getCellComment()
    {
        return (Comment)getPropertyValue(Property.COMMENT);
    }

    /**
     * Removes the comment for this cell, if there is one.
     */
    public void removeCellComment()
    {
        removeProperty(Property.COMMENT);
    }

    /**
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    public Hyperlink getHyperlink()
    {
        return (Hyperlink)getPropertyValue(Property.HYPERLINK);
    }

    /**
     * Assign a hyperlink to this cell
     *
     * @param link hyperlink associated with this cell
     */
    public void setHyperlink(Hyperlink link)
    {
        setProperty(Property.HYPERLINK,link);
    }

    /**
     * Only valid for array formula cells
     *
     * @return range of the array formula group that the cell belongs to.
     */
//TODO: What is this?
    public CellRangeAddress getArrayFormulaRange()
    {
        return null;
    }

    /**
     * @return <code>true</code> if this cell is part of group of cells having a common array formula.
     */
//TODO: What is this?
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

    void removeProperty(int type)
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
    void setProperty(int type,Object value)
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
    Object getPropertyValue(int type)
    {
        return getPropertyValue(type,null);
    }
    Object getPropertyValue(int type,String defaultValue)
    {
        Property current=_firstProperty;
        while(current!=null&&current.getType()!=type) current=current._next;
        return current==null?defaultValue:current.getValue();
    }
    void ensurePlainStringType()
    {
        if(_value.getType()!=CELL_TYPE_STRING
           ||((StringValue)_value).isRichText())
            _value=new PlainStringValue();
    }
    void ensureRichTextStringType()
    {
        if(_value.getType()!=CELL_TYPE_STRING
           ||!((StringValue)_value).isRichText())
            _value=new RichTextValue();
    }
    void ensureType(int type)
    {
        if(_value.getType()!=type)
            setType(type);
    }
    void ensureFormulaType(int type)
    {
        if(_value.getType()!=CELL_TYPE_FORMULA
           ||((FormulaValue)_value).getFormulaType()!=type)
            setFormulaType(type);
    }
    void ensureTypeOrFormulaType(int type)
    {
        assert type==CELL_TYPE_NUMERIC||
               type==CELL_TYPE_STRING||
               type==CELL_TYPE_BOOLEAN||
               type==CELL_TYPE_ERROR;
        if(_value.getType()==type)
        {
            if(type==CELL_TYPE_STRING&&((StringValue)_value).isRichText())
                setType(CELL_TYPE_STRING);
            return;
        }
        if(_value.getType()==CELL_TYPE_FORMULA)
        {
            if(((FormulaValue)_value).getFormulaType()==type)
                return;
            setFormulaType(type); // once a formula, always a formula
            return;
        }
        setType(type);
    }
    void setType(int type)
    {
        switch(type)
        {
            case CELL_TYPE_NUMERIC:
            {
                _value=new NumericValue();
                break;
            }
            case CELL_TYPE_STRING:
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
            case CELL_TYPE_FORMULA:
            {
                _value=new NumericFormulaValue();
                break;
            }
            case CELL_TYPE_BLANK:
            {
                _value=new BlankValue();
                break;
            }
            case CELL_TYPE_BOOLEAN:
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
            case CELL_TYPE_ERROR:
            {
                _value=new ErrorValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Illegal type " + type);
            }
        }
    }
    void setFormulaType(int type)
    {
        switch(type)
        {
            case CELL_TYPE_NUMERIC:
            {
                _value=new NumericFormulaValue();
                break;
            }
            case CELL_TYPE_STRING:
            {
                _value=new StringFormulaValue();
                break;
            }
            case CELL_TYPE_BOOLEAN:
            {
                _value=new BooleanFormulaValue();
                break;
            }
            case CELL_TYPE_ERROR:
            {
                _value=new ErrorFormulaValue();
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Illegal type " + type);
            }
        }
    }
//TODO: implement this correctly
    int computeTypeFromFormula(String formula)
    {
        return CELL_TYPE_NUMERIC;
    }
//COPIED FROM https://svn.apache.org/repos/asf/poi/trunk/src/ooxml/java/org/apache/poi/xssf/usermodel/XSSFCell.java since the functions are declared private there
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
    private boolean convertCellValueToBoolean() {
        int cellType = getCellType();

        if (cellType == CELL_TYPE_FORMULA) {
            cellType = getCachedFormulaResultType();
        }

        switch (cellType) {
            case CELL_TYPE_BOOLEAN:
                return getBooleanCellValue();
            case CELL_TYPE_STRING:

                String text = getStringCellValue();
                return Boolean.parseBoolean(text);
            case CELL_TYPE_NUMERIC:
                return getNumericCellValue() != 0;
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
                return getBooleanCellValue() ? "TRUE" : "FALSE";
            case CELL_TYPE_STRING:
                return getStringCellValue();
            case CELL_TYPE_NUMERIC:
            case CELL_TYPE_ERROR:
                byte errVal = getErrorCellValue();
                return FormulaError.forInt(errVal).getString();
            case CELL_TYPE_FORMULA:
                return "";
            default:
                throw new IllegalStateException("Unexpected cell type (" + cellType + ")");
        }
    }

//END OF COPIED CODE

    static abstract class Property
    {
        final static int COMMENT=1;
        final static int HYPERLINK=2;
        Object _value;
        Property _next;
        public Property(Object value)
        {
            _value=value;
        }
        abstract int getType();
        void setValue(Object value)
        {
            _value=value;
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
        public int getType()
        {
            return HYPERLINK;
        }
    }
    interface Value
    {
        int getType();
    }
    static class NumericValue implements Value
    {
        double _value;
        public int getType()
        {
            return CELL_TYPE_NUMERIC;
        }
        void setValue(double value)
        {
            _value=value;
        }
        double getValue()
        {
            return _value;
        }
    }
    static abstract class StringValue implements Value
    {
        public int getType()
        {
            return CELL_TYPE_STRING;
        }
//We cannot introduce a new type CELL_TYPE_RICH_TEXT because the types are public so we have to make rich text as a type of string
        abstract boolean isRichText(); // using the POI style which seems to avoid "instanceof".
    }
    static class PlainStringValue extends StringValue
    {
        String _value;
        void setValue(String value)
        {
            _value=value;
        }
        String getValue()
        {
            return _value;
        }
        boolean isRichText()
        {
            return false;
        }
    }
    static class RichTextValue extends StringValue
    {
        RichTextString _value;
        public int getType()
        {
            return CELL_TYPE_STRING;
        }
        void setValue(RichTextString value)
        {
            _value=value;
        }
        RichTextString getValue()
        {
            return _value;
        }
        boolean isRichText()
        {
            return true;
        }
    }
    static abstract class FormulaValue implements Value
    {
        String _value;
        public int getType()
        {
            return CELL_TYPE_FORMULA;
        }
        void setValue(String value)
        {
            _value=value;
        }
        String getValue()
        {
            return _value;
        }
        abstract int getFormulaType();
    }
    static class NumericFormulaValue extends FormulaValue
    {
        double _preEvaluatedValue;
        int getFormulaType()
        {
            return CELL_TYPE_NUMERIC;
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
    static class StringFormulaValue extends FormulaValue
    {
        String _preEvaluatedValue;
        int getFormulaType()
        {
            return CELL_TYPE_STRING;
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
    static class BooleanFormulaValue extends FormulaValue
    {
        boolean _preEvaluatedValue;
        int getFormulaType()
        {
            return CELL_TYPE_BOOLEAN;
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
    static class ErrorFormulaValue extends FormulaValue
    {
        byte _preEvaluatedValue;
        int getFormulaType()
        {
            return CELL_TYPE_ERROR;
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
    static class BlankValue implements Value
    {
        public int getType()
        {
            return CELL_TYPE_BLANK;
        }
    }
    static class BooleanValue implements Value
    {
        boolean _value;
        public int getType()
        {
            return CELL_TYPE_BOOLEAN;
        }
        void setValue(boolean value)
        {
            _value=value;
        }
        boolean getValue()
        {
            return _value;
        }
    }
    static class ErrorValue implements Value
    {
        byte _value;
        public int getType()
        {
            return CELL_TYPE_ERROR;
        }
        void setValue(byte value)
        {
            _value=value;
        }
        byte getValue()
        {
            return _value;
        }
    }
}
