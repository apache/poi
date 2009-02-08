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

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

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
    private static POILogger logger = POILogFactory.getLogger(XSSFCell.class);

    private static final String FILE_FORMAT_NAME  = "BIFF12";
    /**
     * The maximum  number of columns in SpreadsheetML
     */
    public static final int MAX_COLUMN_NUMBER  = 16384; // 2^14
    private static final int LAST_COLUMN_NUMBER  = MAX_COLUMN_NUMBER-1;
    private static final String LAST_COLUMN_NAME  = "XFD";

    private static final String FALSE_AS_STRING = "0";
    private static final String TRUE_AS_STRING  = "1";

    /**
     * the xml bean containing information about the cell's location, value,
     * data type, formatting, and formula
     */
    private final CTCell cell;

    /**
     * the XSSFRow this cell belongs to
     */
    private final XSSFRow row;

    /**
     * 0-based column index
     */
    private int cellNum;

    /**
     * Table of strings shared across this workbook.
     * If two cells contain the same string, then the cell value is the same index into SharedStringsTable
     */
    private SharedStringsTable sharedStringSource;

    /**
     * Table of cell styles shared across all cells in a workbook.
     */
    private StylesTable stylesSource;

    /**
     * Construct a XSSFCell.
     *
     * @param row the parent row.
     * @param cell the xml bean containing information about the cell.
     */
    protected XSSFCell(XSSFRow row, CTCell cell) {
        this.cell = cell;
        this.row = row;
        if (cell.getR() != null) {
            this.cellNum = parseCellNum(cell.getR());
        }
        this.sharedStringSource = row.getSheet().getWorkbook().getSharedStringSource();
        this.stylesSource = row.getSheet().getWorkbook().getStylesSource();
    }

    /**
     * @return table of strings shared across this workbook
     */
    protected SharedStringsTable getSharedStringSource() {
        return sharedStringSource;
    }

    /**
     * @return table of cell styles shared across this workbook
     */
    protected StylesTable getStylesSource() {
        return stylesSource;
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
        return row;
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
                return cell.isSetV() && TRUE_AS_STRING.equals(cell.getV());
            case CELL_TYPE_FORMULA:
                //YK: should throw an exception if requesting boolean value from a non-boolean formula
                return cell.isSetV() && TRUE_AS_STRING.equals(cell.getV());
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
        cell.setT(STCellType.B);
        cell.setV(value ? TRUE_AS_STRING : FALSE_AS_STRING);
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
            case CELL_TYPE_ERROR:
            case CELL_TYPE_FORMULA:
            case CELL_TYPE_NUMERIC:
                return cell.isSetV() ? Double.parseDouble(cell.getV()) : 0.0;
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
        int cellType = getCellType();
        switch (cellType) {
            case CELL_TYPE_ERROR:
            case CELL_TYPE_FORMULA:
                cell.setV(String.valueOf(value));
                break;
            default:
                cell.setT(STCellType.N);
                cell.setV(String.valueOf(value));
                break;
        }
    }

    /**
     * Get the value of the cell as a string
     * <p>
     * For numeric cells we throw an exception. For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we return empty String.
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
     * For formula cells we return the pre-calculated value.
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
                if (!cell.isSetV()) rt = new XSSFRichTextString("");
                else {
                    if (cell.getT() == STCellType.INLINE_STR) {
                        return new XSSFRichTextString(cell.getV());
                    } else {
                        int idx = Integer.parseInt(cell.getV());
                        rt = new XSSFRichTextString(sharedStringSource.getEntryAt(idx));
                    }
                }
                break;
            case CELL_TYPE_FORMULA:
                rt = new XSSFRichTextString(cell.isSetV() ? cell.getV() : "");
                break;
            default:
                throw typeMismatch(CELL_TYPE_STRING, cellType, false);
        }
        rt.setStylesTableReference(stylesSource);
        return rt;
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
        if(str == null){
            setBlank();
            return;
        }
        int cellType = getCellType();
        switch(cellType){
            case Cell.CELL_TYPE_FORMULA:
                cell.setV(str.getString());
                break;
            default:
                if(cell.getT() == STCellType.INLINE_STR) {
                    //set the 'pre-evaluated result
                    cell.setV(str.getString());
                } else {
                    cell.setT(STCellType.S);
                    XSSFRichTextString rt = (XSSFRichTextString)str;
                    rt.setStylesTableReference(stylesSource);
                    int sRef = sharedStringSource.addEntry(rt.getCTRst());
                    cell.setV(Integer.toString(sRef));
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

        CTCellFormula f = cell.getF();
        if(f.getT() == STCellFormulaType.SHARED){
            return convertSharedFormula((int)f.getSi());
        } else {
            return f.getStringValue();
        }
    }

    /**
     * Creates a non shared formula from the shared formula counterpart
     *
     * @return non shared formula created for the given shared formula and this cell
     */
    private String convertSharedFormula(int idx){
        XSSFSheet sheet = getSheet();
        XSSFCell sfCell = sheet.getSharedFormulaCell(idx);
        if(sfCell == null){
            throw new IllegalStateException("Shared Formula not found for group index " + idx);
        }
        String sharedFormula = sfCell.getCTCell().getF().getStringValue();
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(sheet.getWorkbook());
        Ptg[] ptgs = FormulaParser.parse(sharedFormula, fpb, FormulaType.CELL, sheetIndex);
        Ptg[] fmla = SharedFormulaRecord.convertSharedFormulas(ptgs,
                getRowIndex() - sfCell.getRowIndex(), getColumnIndex() - sfCell.getColumnIndex());
        return FormulaRenderer.toFormulaString(fpb, fmla);
    }

    /**
     * Sets formula for this cell.
     * <p>
     * Note, this method only sets the formula string and does not calculate the formula value.
     * To set the precalculated value use {@link #setCellValue(double)} or {@link #setCellValue(String)}
     * </p>
     *
     * @param formula the formula to set, e.g. <code>SUM(C4:E4)</code>.
     *  If the argument is <code>null</code> then the current formula is removed.
     * @throws IllegalArgumentException if the formula is invalid
     */
    public void setCellFormula(String formula) {
        XSSFWorkbook wb = row.getSheet().getWorkbook();
        if (formula == null && cell.isSetF()) {
            wb.onDeleteFormula(this);
            cell.unsetF();
            return;
        }

        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        try {
            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, wb.getSheetIndex(getSheet()));
        } catch (RuntimeException e) {
            if (e.getClass().getName().startsWith(FormulaParser.class.getName())) {
                throw new IllegalArgumentException("Unparsable formula '" + formula + "'", e);
            }
            throw e;
        }

        CTCellFormula f =  CTCellFormula.Factory.newInstance();
        f.setStringValue(formula);
        cell.setF(f);
        if(cell.isSetV()) cell.unsetV();
    }

    /**
     * Returns column index of this cell
     *
     * @return zero-based column index of a column in a sheet.
     */
    public int getColumnIndex() {
        return this.cellNum;
    }

    /**
     * Returns row index of a row in the sheet that contains this cell
     *
     * @return zero-based row index of a row in the sheet that contains this cell
     */
    public int getRowIndex() {
        return row.getRowNum();
    }

    /**
     * Returns an A1 style reference to the location of this cell
     *
     * @return A1 style reference to the location of this cell
     */
    public String getReference() {
        return cell.getR();
    }

    /**
     * Return the cell's style.
     *
     * @return the cell's style. Always not-null. Default cell style has zero index and can be obtained as
     * <code>workbook.getCellStyleAt(0)</code>
     */
    public XSSFCellStyle getCellStyle() {
        long idx = cell.isSetS() ? cell.getS() : 0;
        return stylesSource.getStyleAt((int)idx);
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
            if(cell.isSetS()) cell.unsetS();
        } else {
            XSSFCellStyle xStyle = (XSSFCellStyle)style;
            xStyle.verifyBelongsToStylesSource(stylesSource);

            long idx = stylesSource.putStyle(xStyle);
            cell.setS(idx);
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

        if (cell.getF() != null) {
            return CELL_TYPE_FORMULA;
        }

        switch (this.cell.getT().intValue()) {
            case STCellType.INT_B:
                return CELL_TYPE_BOOLEAN;
            case STCellType.INT_N:
                if (!cell.isSetV()) {
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
                throw new IllegalStateException("Illegal cell type: " + this.cell.getT());
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
        int cellType = getCellType();
        if(cellType != CELL_TYPE_ERROR) throw typeMismatch(CELL_TYPE_ERROR, cellType, false);

        return cell.getV();
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
        if(code == null) return 0;

        return (byte)FormulaError.forString(code).getCode();
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
        cell.setT(STCellType.E);
        cell.setV(error.getString());
    }

    /**
     * Sets this cell as the active cell for the worksheet.
     */
    public void setAsActiveCell() {
        getSheet().setActiveCell(cell.getR());
    }

    /**
     * Blanks this cell. Blank cells have no formula or value but may have styling.
     * This method erases all the data previously associated with this cell.
     */
    private void setBlank(){
        CTCell blank = CTCell.Factory.newInstance();
        blank.setR(cell.getR());
        cell.set(blank);
    }

    /**
     * Sets column index of this cell
     *
     * @param num column index of this cell
     */
    protected void setCellNum(int num) {
        checkBounds(num);
        cellNum = num;
        String ref = new CellReference(getRowIndex(), getColumnIndex()).formatAsString();
        cell.setR(ref);
    }

    /**
     * Converts A1 style reference into 0-based column index
     *
     * @param r an A1 style reference to the location of this cell
     * @return 0-based column index
     */
    protected static short parseCellNum(String r) {
        r = r.split("\\d+")[0];
        if (r.length() == 1) {
            return (short) (r.charAt(0) - 'A');
        } else {
            return (short) (r.charAt(1) - 'A' + 26 * (r.charAt(0) - '@'));
        }
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
        switch (cellType) {
            case CELL_TYPE_BLANK:
                setBlank();
                break;
            case CELL_TYPE_BOOLEAN:
                cell.setT(STCellType.B);
                break;
            case CELL_TYPE_NUMERIC:
                cell.setT(STCellType.N);
                break;
            case CELL_TYPE_ERROR:
                cell.setT(STCellType.E);
                break;
            case CELL_TYPE_STRING:
                cell.setT(STCellType.S);
                break;
            case CELL_TYPE_FORMULA:
                if(!cell.isSetF()){
                    CTCellFormula f =  CTCellFormula.Factory.newInstance();
                    f.setStringValue("0");
                    cell.setF(f);
                    cell.unsetT();
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal cell type: " + cellType);
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
                } else {
                    return getNumericCellValue() + "";
                }
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
        return cell.getV();
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
        if (cellIndex < 0 || cellIndex > LAST_COLUMN_NUMBER) {
            throw new IllegalArgumentException("Invalid column index (" + cellIndex 
                    + ").  Allowable column range for " + FILE_FORMAT_NAME + " is (0.." 
                    + LAST_COLUMN_NUMBER + ") or ('A'..'" + LAST_COLUMN_NAME + "')");
        }
    }

    /**
     * Returns cell comment associated with this cell
     *
     * @return the cell comment associated with this cell or <code>null</code>
     */
    public XSSFComment getCellComment() {
        return getSheet().getCellComment(row.getRowNum(), getColumnIndex());
    }

    /**
     * Assign a comment to this cell. If the supplied comment is null,
     * the comment for this cell will be removed.
     *
     * @param comment comment associated with this cell
     */
    public void setCellComment(Comment comment) {
        String cellRef = new CellReference(row.getRowNum(), getColumnIndex()).formatAsString();
        getSheet().setCellComment(cellRef, (XSSFComment)comment);
    }

    /**
     * Returns hyperlink associated with this cell
     *
     * @return hyperlink associated with this cell or <code>null</code> if not found
     */
    public XSSFHyperlink getHyperlink() {
        return getSheet().getHyperlink(row.getRowNum(), cellNum);
    }

    /**
     * Assign a hypelrink to this cell
     *
     * @param hyperlink the hypelrink to associate with this cell
     */
    public void setHyperlink(Hyperlink hyperlink) {
        XSSFHyperlink link = (XSSFHyperlink)hyperlink;

        // Assign to us
        link.setCellReference( new CellReference(row.getRowNum(), cellNum).formatAsString() );

        // Add to the lists
        getSheet().setCellHyperlink(link);
    }

    /**
     * Returns the xml bean containing information about the cell's location (reference), value,
     * data type, formatting, and formula
     *
     * @return the xml bean containing information about this cell
     */
    public CTCell getCTCell(){
        return cell;
    }

    /**
     * update cell reference when shifting rows
     *
     * @param row
     */
    protected void modifyCellReference(XSSFRow row) {
        this.cell.setR(new CellReference(row.getRowNum(), cellNum).formatAsString());

        CTCell[] ctCells = row.getCTRow().getCArray();
        for (CTCell ctCell : ctCells) {
            ctCell.setR(new CellReference(row.getRowNum(), cellNum).formatAsString());
        }
    }
}
