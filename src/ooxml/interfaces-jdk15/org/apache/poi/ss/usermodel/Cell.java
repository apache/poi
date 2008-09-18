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

package org.apache.poi.ss.usermodel;

import java.util.Calendar;
import java.util.Date;

public interface Cell {

    /**
     * Numeric Cell type (0)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_NUMERIC = 0;

    /**
     * String Cell type (1)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_STRING = 1;

    /**
     * Formula Cell type (2)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_FORMULA = 2;

    /**
     * Blank Cell type (3)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_BLANK = 3;

    /**
     * Boolean Cell type (4)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_BOOLEAN = 4;

    /**
     * Error Cell type (5)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int CELL_TYPE_ERROR = 5;
    
    public final static class CELL_ERROR_TYPE {
    	private final byte type;
    	private final String repr;
    	private CELL_ERROR_TYPE(int type, String repr) {
    		this.type = (byte)type;
    		this.repr = repr;
    	}
    	
    	public byte getType() { return type; }
    	public String getStringRepr() { return repr; }
    }
    public static final CELL_ERROR_TYPE ERROR_NULL  = new CELL_ERROR_TYPE(0, "#NULL!");
    public static final CELL_ERROR_TYPE ERROR_DIV0  = new CELL_ERROR_TYPE(7, "#DIV/0!");
    public static final CELL_ERROR_TYPE ERROR_VALUE = new CELL_ERROR_TYPE(15, "#VALUE!");
    public static final CELL_ERROR_TYPE ERROR_REF   = new CELL_ERROR_TYPE(23, "#REF!");
    public static final CELL_ERROR_TYPE ERROR_NAME  = new CELL_ERROR_TYPE(29, "#NAME?");
    public static final CELL_ERROR_TYPE ERROR_NUM   = new CELL_ERROR_TYPE(36, "#NUM!");
    public static final CELL_ERROR_TYPE ERROR_NA    = new CELL_ERROR_TYPE(42, "#N/A");


    /**
     * set the cell's number within the row (0 based)
     * @param num  short the cell number
     */

    void setCellNum(short num);

    /**
     *  get the cell's number within the row
     * @return short reperesenting the column number (logical!)
     */

    short getCellNum();

    int getRowIndex();

    Sheet getSheet();

    /**
     * set the cells type (numeric, formula or string)
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */

    void setCellType(int cellType);

    /**
     * @return the cell's type (e.g. numeric, formula or string)
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_ERROR
     */

    int getCellType();

    /**
     * set a numeric value for the cell
     *
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    void setCellValue(double value);

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    void setCellValue(Date value);

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For othertypes we
     *        will change the cell to a numeric cell and set its value.
     */
    void setCellValue(Calendar value);

    /**
     * set a string value for the cell. Please note that if you are using
     * full 16 bit unicode you should call <code>setEncoding()</code> first.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */

    void setCellValue(RichTextString value);

    void setCellValue(String value);

    void setCellFormula(String formula);

    String getCellFormula();

    /**
     * get the value of the cell as a number.  For strings we throw an exception.
     * For blank cells we return a 0.
     */

    double getNumericCellValue();

    /**
     * get the value of the cell as a date.  For strings we throw an exception.
     * For blank cells we return a null.
     */
    Date getDateCellValue();

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we return empty String
     */

    RichTextString getRichStringCellValue();

    /**
     * set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */

    void setCellValue(boolean value);

    /**
     * set a error value for the cell
     *
     * @param value the error value to set this cell to.  For formulas we'll set the
     *        precalculated value ??? IS THIS RIGHT??? , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     */

    void setCellErrorValue(byte value);

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */

    boolean getBooleanCellValue();

    /**
     * get the value of the cell as an error code.  For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     */

    byte getErrorCellValue();

    /**
     * set the style for the cell.  The style should be an HSSFCellStyle created/retreived from
     * the HSSFWorkbook.
     *
     * @param style  reference contained in the workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */

    void setCellStyle(CellStyle style);

    /**
     * get the style for the cell.  This is a reference to a cell style contained in the workbook
     * object.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */

    CellStyle getCellStyle();

    /**
     * Sets this cell as the active cell for the worksheet
     */
    void setAsActiveCell();

    /**
     * Returns a string representation of the cell
     * 
     * This method returns a simple representation, 
     * anthing more complex should be in user code, with
     * knowledge of the semantics of the sheet being processed. 
     * 
     * Formula cells return the formula string, 
     * rather than the formula result. 
     * Dates are displayed in dd-MMM-yyyy format
     * Errors are displayed as #ERR&lt;errIdx&gt;
     */
    String toString();

    /**
     * Assign a comment to this cell
     *
     * @param comment comment associated with this cell
     */
    void setCellComment(Comment comment);

    /**
     * Returns comment associated with this cell
     *
     * @return comment associated with this cell
     */
    Comment getCellComment();

    /**
     * Returns hyperlink associated with this cell
     *
     * @return hyperlink associated with this cell or null if not found
     */
    public Hyperlink getHyperlink();

    /**
     * Assign a hypelrink to this cell
     *
     * @param link hypelrink associated with this cell
     */
    public void setHyperlink(Hyperlink link);
}
