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
import java.util.Map;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Removal;

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
 * Cells should have their number (0 based) before being added to a row.
 * </p>
 */
public interface Cell {

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    int getColumnIndex();

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    int getRowIndex();

    /**
     * Returns the sheet this cell belongs to
     *
     * @return the sheet this cell belongs to
     */
    Sheet getSheet();

    /**
     * Returns the Row this cell belongs to
     *
     * @return the Row that owns this cell
     */
     Row getRow();

    /**
     * Set the cells type (blank, numeric, boolean, error or string).
     * <p>If the cell currently contains a value, the value will
     *  be converted to match the new type, if possible. Formatting
     *  is generally lost in the process however.</p>
     * <p>Conversion rules:</p>
     * <p>to NUMERIC: numeric value is left as is. True converts to 1.0, false converts to 0. otherwise, the
     * value is set to 0. Formula is removed.</p>
     * <p>If what you want to do is get a String value for your
     *  numeric cell, <i>stop!</i> This is not the way to do it.
     *  Instead, for fetching the string value of a numeric or boolean
     *  or date cell, use {@link DataFormatter} instead.</p>
     * <p>If cell is a member of an array formula group containing more than 1 cell, an {@link IllegalStateException}
     * is thrown. If the array formula group contains only this cell, it is removed.</p>
     * <p>Passing {@link CellType#FORMULA} is illegal and will result in an {@link IllegalArgumentException}.</p>
     *
     * @deprecated This method is deprecated and will be removed in POI 5.0.
     * Use explicit {@link #setCellFormula(String)}, <code>setCellValue(...)</code> or {@link #setBlank()}
     * to get the desired result.
     * @throws IllegalArgumentException if the specified cell type is invalid (null, _NONE or FORMULA)
     * @throws IllegalStateException if the current value cannot be converted to the new type or
     * if the cell is a part of an array formula group containing other cells
     */
    @Deprecated
    @Removal(version = "5.0")
    void setCellType(CellType cellType);

    /**
     * Removes formula and value from the cell, and sets its type to {@link CellType#BLANK}.
     * Preserves comments and hyperlinks.
     * While {@link #setCellType(CellType)} exists, is an alias for {@code setCellType(CellType.BLANK)}.
     */
    void setBlank();

    /**
     * Return the cell type.
     *
     * @return the cell type
     */
    CellType getCellType();

    /**
     * Return the cell type.  Tables in an array formula return
     * {@link CellType#FORMULA} for all cells, even though the formula is only defined
     * in the OOXML file for the top left cell of the array.
     * <p>
     * NOTE: POI does not support data table formulas.
     * Cells in a data table appear to POI as plain cells typed from their cached value.
     *
     * @return the cell type
     * @since POI 3.15 beta 3
     * @deprecated will be removed in 4.2
     * Will be renamed to <code>getCellType()</code> when we make the CellType enum transition in POI 4.0. See bug 59791.
     */
    @Deprecated
    @Removal(version="4.2")
    CellType getCellTypeEnum();

    /**
     * Only valid for formula cells
     *
     * Will return {@link CellType} in a future version of POI.
     * For forwards compatibility, do not hard-code cell type literals in your code.
     *
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     */
    CellType getCachedFormulaResultType();

    /**
     * Only valid for formula cells
     * @return one of ({@link CellType#NUMERIC}, {@link CellType#STRING},
     *     {@link CellType#BOOLEAN}, {@link CellType#ERROR}) depending
     * on the cached value of the formula
     * @since POI 3.15 beta 3
     * @deprecated will be removed in 4.2
     * Will be renamed to <code>getCachedFormulaResultType()</code> when we make the CellType enum transition in POI 4.0. See bug 59791.
     */
    @Deprecated
    @Removal(version = "4.2")
    CellType getCachedFormulaResultTypeEnum();

    /**
     * Set a numeric value for the cell.
     *
     * @param value the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    void setCellValue(double value);

    /**
     * <p>Converts the supplied date to its equivalent Excel numeric value and sets
     * that into the cell.</p>
     *
     * <p><b>Note</b> - There is actually no 'DATE' cell type in Excel. In many
     * cases (when entering date values), Excel automatically adjusts the
     * <i>cell style</i> to some date format, creating the illusion that the cell
     * data type is now something besides {@link CellType#NUMERIC}.  POI
     * does not attempt to replicate this behaviour.  To make a numeric cell
     * display as a date, use {@link #setCellStyle(CellStyle)} etc.</p>
     *
     * @param value the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numerics cell and set its value.
     */
    void setCellValue(Date value);

    /**
     * <p>Set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.</p>
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
    void setCellValue(Calendar value);

    /**
     * Set a rich string value for the cell.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    void setCellValue(RichTextString value);

    /**
     * Set a string value for the cell.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */
    void setCellValue(String value);


    /**
     * Sets formula for this cell.
     * <p>If {@code formula} is not null, sets or updates the formula. If {@code formula} is null, removes the formula.
     * Or use {@link #removeFormula()} to remove the formula.</p>
     *
     * <p>Note, this method only sets the formula string and does not calculate the formula value.
     * To set the precalculated value use {@link #setCellValue}.</p>
     *
     * <p>If the cell was blank, sets value to 0. Otherwise, preserves the value as precalculated.</p>
     *
     * @param formula the formula to set, e.g. <code>"SUM(C4:E4)"</code>.
     * If the argument is <code>null</code> then the current formula is removed.
     *
     * @see Cell#removeFormula
     * @throws IllegalStateException if this cell is a part of an array formula group containing other cells
     * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
     */
    void setCellFormula(String formula) throws FormulaParseException, IllegalStateException;

    /**
     * Removes formula, if any.
     *
     * If cell was blank, leaves it as is.
     * If it is a part of an array formula group, blanks the cell.
     * If has a regular formula, removes the formula preserving the "cached" value.
     * @throws IllegalStateException if cell is a part of an array formula group containing other cells
     */
    void removeFormula() throws IllegalStateException;

    /**
     * Return a formula for the cell, for example, <code>SUM(C4:E4)</code>
     *
     * @return a formula for the cell
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} is not {@link CellType#FORMULA}
     */
    String getCellFormula();

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
    double getNumericCellValue();

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
    Date getDateCellValue();

    /**
     * Get the value of the cell as a XSSFRichTextString
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formula cells we return the pre-calculated value if a string, otherwise an exception.
     * </p>
     * @return the value of the cell as a XSSFRichTextString
     */
    RichTextString getRichStringCellValue();

    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we throw an exception.
     * </p>
     * @return the value of the cell as a string
     */
    String getStringCellValue();

    /**
     * Set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */
     void setCellValue(boolean value);

    /**
     * Set a error value for the cell
     *
     * @param value the error value to set this cell to.  For formulas we'll set the
     *        precalculated value , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     * @see FormulaError
     */
    void setCellErrorValue(byte value);

    /**
     * Get the value of the cell as a boolean.
     * <p>
     * For strings, numbers, and errors, we throw an exception. For blank cells we return a false.
     * </p>
     * @return the value of the cell as a boolean
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()}
     *   is not {@link CellType#BOOLEAN}, {@link CellType#BLANK} or {@link CellType#FORMULA}
     */
    boolean getBooleanCellValue();

    /**
     * Get the value of the cell as an error code.
     * <p>
     * For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     * </p>
     *
     * @return the value of the cell as an error code
     * @throws IllegalStateException if the cell type returned by {@link #getCellType()} isn't {@link CellType#ERROR}
     * @see FormulaError for error codes
     */
    byte getErrorCellValue();

    /**
     * <p>Set the style for the cell.  The style should be an CellStyle created/retrieved from
     * the Workbook.</p>
     *
     * <p>To change the style of a cell without affecting other cells that use the same style,
     * use {@link org.apache.poi.ss.util.CellUtil#setCellStyleProperties(Cell, Map)}</p>
     *
     * @param style  reference contained in the workbook.
     * If the value is null then the style information is removed causing the cell to used the default workbook style.
     * @see org.apache.poi.ss.usermodel.Workbook#createCellStyle()
     */
    void setCellStyle(CellStyle style);

    /**
     * Return the cell's style.
     *
     * @return the cell's style. Always not-null. Default cell style has zero index and can be obtained as
     * <code>workbook.getCellStyleAt(0)</code>
     * @see Workbook#getCellStyleAt(int)
     */
    CellStyle getCellStyle();

    /**
     * Sets this cell as the active cell for the worksheet
     */
    void setAsActiveCell();

   /**
     * Gets the address of this cell
     *
     * @return <code>A1</code> style address of this cell
     * @since 3.14beta1
     */
    CellAddress getAddress();

    /**
     * Assign a comment to this cell
     *
     * @param comment comment associated with this cell
     */
    void setCellComment(Comment comment);

    /**
     * Returns comment associated with this cell
     *
     * @return comment associated with this cell or <code>null</code> if not found
     */
    Comment getCellComment();

    /**
     * Removes the comment for this cell, if there is one.
     */
    void removeCellComment();

    /**
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    Hyperlink getHyperlink();

    /**
     * Assign a hyperlink to this cell
     *
     * @param link hyperlink associated with this cell
     */
    void setHyperlink(Hyperlink link);

    /**
     * Removes the hyperlink for this cell, if there is one.
     */
    void removeHyperlink();

    /**
     * Only valid for array formula cells
     *
     * @return range of the array formula group that the cell belongs to.
     */
    CellRangeAddress getArrayFormulaRange();

    /**
     * @return <code>true</code> if this cell is part of group of cells having a common array formula.
     */
    boolean isPartOfArrayFormulaGroup();
}
