/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */



/*
 * Cell.java
 *
 * Created on September 30, 2001, 3:46 PM
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.util.SheetReferences;

//import org.apache.poi.hssf.record.formula.FormulaParser;

import java.util.Date;
import java.util.Calendar;

/**
 * High level representation of a cell in a row of a spreadsheet.
 * Cells can be numeric, formula-based or string-based (text).  The cell type
 * specifies this.  String cells cannot conatin numbers and numeric cells cannot
 * contain strings (at least according to our model).  Client apps should do the
 * conversions themselves.  Formula cells are treated like string cells, simply
 * containing a formula string.  They'll be rendered differently.
 * <p>
 * Cells should have their number (0 based) before being added to a row.  Only
 * cells that have values should be added.
 * <p>
 * NOTE: the alpha won't be implementing formulas
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Dan Sherman (dsherman at isisph.com)
 * @author  Brian Sanders (kestrel at burdell dot org) Active Cell support
 * @version 1.0-pre
 */

public class HSSFCell
{

    /**
     * Numeric Cell type (0)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_NUMERIC           = 0;

    /**
     * String Cell type (1)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_STRING            = 1;

    /**
     * Formula Cell type (2)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_FORMULA           = 2;

    /**
     * Blank Cell type (3)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_BLANK             = 3;

    /**
     * Boolean Cell type (4)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_BOOLEAN           = 4;

    /**
     * Error Cell type (5)
     * @see #setCellType(int)
     * @see #getCellType()
     */

    public final static int          CELL_TYPE_ERROR             = 5;
    public final static short        ENCODING_COMPRESSED_UNICODE = 0;
    public final static short        ENCODING_UTF_16             = 1;
    private short                    cellNum;
    private int                      cellType;
    private HSSFCellStyle            cellStyle;
    private double                   cellValue;
    private String                   stringValue;
    private boolean                  booleanValue;
    private byte                     errorValue;
    private short                    encoding = ENCODING_COMPRESSED_UNICODE;
    private Workbook                 book;
    private Sheet                    sheet;
    //private short                    row;
    private int                    row;
    private CellValueRecordInterface record;

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     * <p>
     * When the cell is initially created it is set to CELL_TYPE_BLANK. Cell types
     * can be changed/overwritten by calling setCellValue with the appropriate
     * type as a parameter although conversions from one type to another may be
     * prohibited.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     *
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short)
     */

    //protected HSSFCell(Workbook book, Sheet sheet, short row, short col)
    protected HSSFCell(Workbook book, Sheet sheet, int row, short col)
    {
        checkBounds(col);
        cellNum      = col;
        this.row     = row;
        cellStyle    = null;
        cellValue    = 0;
        stringValue  = null;
        booleanValue = false;
        errorValue   = ( byte ) 0;
        this.book    = book;
        this.sheet   = sheet;

        // Relying on the fact that by default the cellType is set to 0 which
        // is different to CELL_TYPE_BLANK hence the following method call correctly
        // creates a new blank cell.
        setCellType(CELL_TYPE_BLANK, false);
        ExtendedFormatRecord xf = book.getExFormatAt(0xf);

        setCellStyle(new HSSFCellStyle(( short ) 0xf, xf));
    }

    /**
     * Creates new Cell - Should only be called by HSSFRow.  This creates a cell
     * from scratch.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param row   - the row of this cell
     * @param col   - the column for this cell
     * @param type  - CELL_TYPE_NUMERIC, CELL_TYPE_STRING, CELL_TYPE_FORMULA, CELL_TYPE_BLANK,
     *                CELL_TYPE_BOOLEAN, CELL_TYPE_ERROR
     *                Type of cell
     * @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short,int)
     * @deprecated As of 22-Jan-2002 use @see org.apache.poi.hssf.usermodel.HSSFRow#createCell(short)
     * and use setCellValue to specify the type lazily.
     */

    //protected HSSFCell(Workbook book, Sheet sheet, short row, short col,
    protected HSSFCell(Workbook book, Sheet sheet, int row, short col,
                       int type)
    {
        checkBounds(col);
        cellNum      = col;
        this.row     = row;
        cellType     = type;
        cellStyle    = null;
        cellValue    = 0;
        stringValue  = null;
        booleanValue = false;
        errorValue   = ( byte ) 0;
        this.book    = book;
        this.sheet   = sheet;
        switch (type)
        {

            case CELL_TYPE_NUMERIC :
                record = new NumberRecord();
                (( NumberRecord ) record).setColumn(col);
                (( NumberRecord ) record).setRow(row);
                (( NumberRecord ) record).setValue(( short ) 0);
                (( NumberRecord ) record).setXFIndex(( short ) 0);
                break;

            case CELL_TYPE_STRING :
                record = new LabelSSTRecord();
                (( LabelSSTRecord ) record).setColumn(col);
                (( LabelSSTRecord ) record).setRow(row);
                (( LabelSSTRecord ) record).setXFIndex(( short ) 0);
                break;

            case CELL_TYPE_BLANK :
                record = new BlankRecord();
                (( BlankRecord ) record).setColumn(col);
                (( BlankRecord ) record).setRow(row);
                (( BlankRecord ) record).setXFIndex(( short ) 0);
                break;

            case CELL_TYPE_FORMULA :
                FormulaRecord formulaRecord = new FormulaRecord();
                record = new FormulaRecordAggregate(formulaRecord,null);
                formulaRecord.setColumn(col);
                formulaRecord.setRow(row);
                formulaRecord.setXFIndex(( short ) 0);
            case CELL_TYPE_BOOLEAN :
                record = new BoolErrRecord();
                (( BoolErrRecord ) record).setColumn(col);
                (( BoolErrRecord ) record).setRow(row);
                (( BoolErrRecord ) record).setXFIndex(( short ) 0);
                (( BoolErrRecord ) record).setValue(false);
                break;

            case CELL_TYPE_ERROR :
                record = new BoolErrRecord();
                (( BoolErrRecord ) record).setColumn(col);
                (( BoolErrRecord ) record).setRow(row);
                (( BoolErrRecord ) record).setXFIndex(( short ) 0);
                (( BoolErrRecord ) record).setValue(( byte ) 0);
                break;
        }
        ExtendedFormatRecord xf = book.getExFormatAt(0xf);

        setCellStyle(new HSSFCellStyle(( short ) 0xf, xf));
    }

    /**
     * Creates an HSSFCell from a CellValueRecordInterface.  HSSFSheet uses this when
     * reading in cells from an existing sheet.
     *
     * @param book - Workbook record of the workbook containing this cell
     * @param sheet - Sheet record of the sheet containing this cell
     * @param cval - the Cell Value Record we wish to represent
     */

    //protected HSSFCell(Workbook book, Sheet sheet, short row,
    protected HSSFCell(Workbook book, Sheet sheet, int row,
                       CellValueRecordInterface cval)
    {
        cellNum     = cval.getColumn();
        record      = cval;
        this.row    = row;
        cellType    = determineType(cval);
        cellStyle   = null;
        stringValue = null;
        this.book   = book;
        this.sheet  = sheet;
        switch (cellType)
        {

            case CELL_TYPE_NUMERIC :
                cellValue = (( NumberRecord ) cval).getValue();
                break;

            case CELL_TYPE_STRING :
                stringValue =
                    book.getSSTString( ( (LabelSSTRecord ) cval).getSSTIndex());
                break;

            case CELL_TYPE_BLANK :
                break;

            case CELL_TYPE_FORMULA :
                cellValue = (( FormulaRecordAggregate ) cval).getFormulaRecord().getValue();
                stringValue=((FormulaRecordAggregate) cval).getStringValue();
                break;

            case CELL_TYPE_BOOLEAN :
                booleanValue = (( BoolErrRecord ) cval).getBooleanValue();
                break;

            case CELL_TYPE_ERROR :
                errorValue = (( BoolErrRecord ) cval).getErrorValue();
                break;
        }
        ExtendedFormatRecord xf = book.getExFormatAt(cval.getXFIndex());

        setCellStyle(new HSSFCellStyle(( short ) cval.getXFIndex(), xf));
    }

    /**
     * private constructor to prevent blank construction
     */
    private HSSFCell()
    {
    }

    /**
     * used internally -- given a cell value record, figure out its type
     */
    private int determineType(CellValueRecordInterface cval)
    {
        Record record = ( Record ) cval;
        int    sid    = record.getSid();
        int    retval = 0;

        switch (sid)
        {

            case NumberRecord.sid :
                retval = HSSFCell.CELL_TYPE_NUMERIC;
                break;

            case BlankRecord.sid :
                retval = HSSFCell.CELL_TYPE_BLANK;
                break;

            case LabelSSTRecord.sid :
                retval = HSSFCell.CELL_TYPE_STRING;
                break;

            case FormulaRecordAggregate.sid :
                retval = HSSFCell.CELL_TYPE_FORMULA;
                break;

            case BoolErrRecord.sid :
                BoolErrRecord boolErrRecord = ( BoolErrRecord ) record;

                retval = (boolErrRecord.isBoolean())
                         ? HSSFCell.CELL_TYPE_BOOLEAN
                         : HSSFCell.CELL_TYPE_ERROR;
                break;
        }
        return retval;
    }

    /**
     * set the cell's number within the row (0 based)
     * @param num  short the cell number
     */

    public void setCellNum(short num)
    {
        cellNum = num;
        record.setColumn(num);
    }

    /**
     *  get the cell's number within the row
     * @return short reperesenting the column number (logical!)
     */

    public short getCellNum()
    {
        return cellNum;
    }

    /**
     * set the cells type (numeric, formula or string) -- DONT USE FORMULAS IN THIS RELEASE
     * WE'LL THROW YOU A RUNTIME EXCEPTION IF YOU DO
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BLANK
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */

    public void setCellType(int cellType)
    {
        setCellType(cellType, true);
    }

    /**
     * sets the cell type. The setValue flag indicates whether to bother about
     *  trying to preserve the current value in the new record if one is created.
     *  <p>
     *  The @see #setCellValue method will call this method with false in setValue
     *  since it will overwrite the cell value later
     *
     */

    private void setCellType(int cellType, boolean setValue)
    {

        // if (cellType == CELL_TYPE_FORMULA)
        // {
        // throw new RuntimeException(
        // "Formulas have not been implemented in this release");
        // }
        if (cellType > CELL_TYPE_ERROR)
        {
            throw new RuntimeException("I have no idea what type that is!");
        }
        switch (cellType)
        {

            case CELL_TYPE_FORMULA :
                FormulaRecordAggregate frec = null;

                if (cellType != this.cellType)
                {
                    frec = new FormulaRecordAggregate(new FormulaRecord(),null);
                }
                else
                {
                    frec = ( FormulaRecordAggregate ) record;
                }
                frec.setColumn(getCellNum());
                if (setValue)
                {
                    frec.getFormulaRecord().setValue(getNumericCellValue());
                }
                frec.setXFIndex(( short ) cellStyle.getIndex());
                frec.setRow(row);
                record = frec;
                break;

            case CELL_TYPE_NUMERIC :
                NumberRecord nrec = null;

                if (cellType != this.cellType)
                {
                    nrec = new NumberRecord();
                }
                else
                {
                    nrec = ( NumberRecord ) record;
                }
                nrec.setColumn(getCellNum());
                if (setValue)
                {
                    nrec.setValue(getNumericCellValue());
                }
                nrec.setXFIndex(( short ) cellStyle.getIndex());
                nrec.setRow(row);
                record = nrec;
                break;

            case CELL_TYPE_STRING :
                LabelSSTRecord lrec = null;

                if (cellType != this.cellType)
                {
                    lrec = new LabelSSTRecord();
                }
                else
                {
                    lrec = ( LabelSSTRecord ) record;
                }
                lrec.setColumn(getCellNum());
                lrec.setRow(row);
                lrec.setXFIndex(( short ) cellStyle.getIndex());
                if (setValue)
                {
                    if ((getStringCellValue() != null)
                            && (!getStringCellValue().equals("")))
                    {
                        int sst = 0;

                        if (encoding == ENCODING_COMPRESSED_UNICODE)
                        {
                            sst = book.addSSTString(getStringCellValue());
                        }
                        if (encoding == ENCODING_UTF_16)
                        {
                            sst = book.addSSTString(getStringCellValue(),
                                                    true);
                        }
                        lrec.setSSTIndex(sst);
                    }
                }
                record = lrec;
                break;

            case CELL_TYPE_BLANK :
                BlankRecord brec = null;

                if (cellType != this.cellType)
                {
                    brec = new BlankRecord();
                }
                else
                {
                    brec = ( BlankRecord ) record;
                }
                brec.setColumn(getCellNum());

                // During construction the cellStyle may be null for a Blank cell.
                if (cellStyle != null)
                {
                    brec.setXFIndex(( short ) cellStyle.getIndex());
                }
                else
                {
                    brec.setXFIndex(( short ) 0);
                }
                brec.setRow(row);
                record = brec;
                break;

            case CELL_TYPE_BOOLEAN :
                BoolErrRecord boolRec = null;

                if (cellType != this.cellType)
                {
                    boolRec = new BoolErrRecord();
                }
                else
                {
                    boolRec = ( BoolErrRecord ) record;
                }
                boolRec.setColumn(getCellNum());
                if (setValue)
                {
                    boolRec.setValue(getBooleanCellValue());
                }
                boolRec.setXFIndex(( short ) cellStyle.getIndex());
                boolRec.setRow(row);
                record = boolRec;
                break;

            case CELL_TYPE_ERROR :
                BoolErrRecord errRec = null;

                if (cellType != this.cellType)
                {
                    errRec = new BoolErrRecord();
                }
                else
                {
                    errRec = ( BoolErrRecord ) record;
                }
                errRec.setColumn(getCellNum());
                if (setValue)
                {
                    errRec.setValue(getErrorCellValue());
                }
                errRec.setXFIndex(( short ) cellStyle.getIndex());
                errRec.setRow(row);
                record = errRec;
                break;
        }
        if (cellType != this.cellType)
        {
            int loc = sheet.getLoc();

            sheet.replaceValueRecord(record);
            sheet.setLoc(loc);
        }
        this.cellType = cellType;
    }

    /**
     * get the cells type (numeric, formula or string)
     * @see #CELL_TYPE_STRING
     * @see #CELL_TYPE_NUMERIC
     * @see #CELL_TYPE_FORMULA
     * @see #CELL_TYPE_BOOLEAN
     * @see #CELL_TYPE_ERROR
     */

    public int getCellType()
    {
        return cellType;
    }

    /**
     * set a numeric value for the cell
     *
     * @param value  the numeric value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(double value)
    {
        if ((cellType != CELL_TYPE_NUMERIC) && (cellType != CELL_TYPE_FORMULA))
        {
            setCellType(CELL_TYPE_NUMERIC, false);
        }
        (( NumberRecord ) record).setValue(value);
        cellValue = value;
    }

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For other types we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Date value)
    {
        setCellValue(HSSFDateUtil.getExcelDate(value));
    }

    /**
     * set a date value for the cell. Excel treats dates as numeric so you will need to format the cell as
     * a date.
     *
     * @param value  the date value to set this cell to.  For formulas we'll set the
     *        precalculated value, for numerics we'll set its value. For othertypes we
     *        will change the cell to a numeric cell and set its value.
     */
    public void setCellValue(Calendar value)
    {
        setCellValue(value.getTime());
    }

    /**
     * set a string value for the cell. Please note that if you are using
     * full 16 bit unicode you should call <code>setEncoding()</code> first.
     *
     * @param value  value to set the cell to.  For formulas we'll set the formula
     * string, for String cells we'll set its value.  For other types we will
     * change the cell to a string cell and set its value.
     * If value is null then we will change the cell to a Blank cell.
     */

    public void setCellValue(String value)
    {
        if (value == null)
        {
            setCellType(CELL_TYPE_BLANK, false);
        }
        else
        {
            if ((cellType != CELL_TYPE_STRING ) && ( cellType != CELL_TYPE_FORMULA))
            {
                setCellType(CELL_TYPE_STRING, false);
            }
            int index = 0;

            if (encoding == ENCODING_COMPRESSED_UNICODE)
            {
                index = book.addSSTString(value);
            }
            if (encoding == ENCODING_UTF_16)
            {
                index = book.addSSTString(value, true);
            }
            (( LabelSSTRecord ) record).setSSTIndex(index);
            stringValue = value;
        }
    }

    public void setCellFormula(String formula) {
        //Workbook.currentBook=book;
        if (formula==null) {
            setCellType(CELL_TYPE_BLANK,false);
        } else {
            setCellType(CELL_TYPE_FORMULA,false);
            FormulaRecordAggregate rec = (FormulaRecordAggregate) record;
            rec.getFormulaRecord().setOptions(( short ) 2);
            rec.getFormulaRecord().setValue(0);
            
            //only set to default if there is no extended format index already set
            if (rec.getXFIndex() == (short)0) rec.setXFIndex(( short ) 0x0f);
            FormulaParser fp = new FormulaParser(formula+";",book);
            fp.parse();
            Ptg[] ptg  = fp.getRPNPtg();
            int   size = 0;
            //System.out.println("got Ptgs " + ptg.length);
            for (int k = 0; k < ptg.length; k++) {
                size += ptg[ k ].getSize();
                rec.getFormulaRecord().pushExpressionToken(ptg[ k ]);
            }
            rec.getFormulaRecord().setExpressionLength(( short ) size);
            //Workbook.currentBook = null;
        }
    }

    public String getCellFormula() {
        //Workbook.currentBook=book;
        String retval = FormulaParser.toFormulaString(book, ((FormulaRecordAggregate)record).getFormulaRecord().getParsedExpression());
        //Workbook.currentBook=null;
        return retval;
    }


    /**
     * get the value of the cell as a number.  For strings we throw an exception.
     * For blank cells we return a 0.
     */

    public double getNumericCellValue()
    {
        if (cellType == CELL_TYPE_BLANK)
        {
            return 0;
        }
        if (cellType == CELL_TYPE_STRING)
        {
            throw new NumberFormatException(
                "You cannot get a numeric value from a String based cell");
        }
        if (cellType == CELL_TYPE_BOOLEAN)
        {
            throw new NumberFormatException(
                "You cannot get a numeric value from a boolean cell");
        }
        if (cellType == CELL_TYPE_ERROR)
        {
            throw new NumberFormatException(
                "You cannot get a numeric value from an error cell");
        }
        return cellValue;
    }

    /**
     * get the value of the cell as a date.  For strings we throw an exception.
     * For blank cells we return a null.
     */
    public Date getDateCellValue()
    {
        if (cellType == CELL_TYPE_BLANK)
        {
            return null;
        }
        if (cellType == CELL_TYPE_STRING)
        {
            throw new NumberFormatException(
                "You cannot get a date value from a String based cell");
        }
        if (cellType == CELL_TYPE_BOOLEAN)
        {
            throw new NumberFormatException(
                "You cannot get a date value from a boolean cell");
        }
        if (cellType == CELL_TYPE_ERROR)
        {
            throw new NumberFormatException(
                "You cannot get a date value from an error cell");
        }
        if (book.isUsing1904DateWindowing()) {
            return HSSFDateUtil.getJavaDate(cellValue,true);
        }
        else {
            return HSSFDateUtil.getJavaDate(cellValue,false);
        }
    }

    /**
     * get the value of the cell as a string - for numeric cells we throw an exception.
     * For blank cells we return an empty string.
     * For formulaCells that are not string Formulas, we return empty String
     */

    public String getStringCellValue()
    {
        if (cellType == CELL_TYPE_BLANK)
        {
            return "";
        }
        if (cellType == CELL_TYPE_NUMERIC)
        {
            throw new NumberFormatException(
                "You cannot get a string value from a numeric cell");
        }
        if (cellType == CELL_TYPE_BOOLEAN)
        {
            throw new NumberFormatException(
                "You cannot get a string value from a boolean cell");
        }
        if (cellType == CELL_TYPE_ERROR)
        {
            throw new NumberFormatException(
                "You cannot get a string value from an error cell");
        }
        if (cellType == CELL_TYPE_FORMULA) 
        {
            if (stringValue==null) return "";
        }
        return stringValue;
    }

    /**
     * set a boolean value for the cell
     *
     * @param value the boolean value to set this cell to.  For formulas we'll set the
     *        precalculated value, for booleans we'll set its value. For other types we
     *        will change the cell to a boolean cell and set its value.
     */

    public void setCellValue(boolean value)
    {
        if ((cellType != CELL_TYPE_BOOLEAN ) && ( cellType != CELL_TYPE_FORMULA))
        {
            setCellType(CELL_TYPE_BOOLEAN, false);
        }
        (( BoolErrRecord ) record).setValue(value);
        booleanValue = value;
    }

    /**
     * set a error value for the cell
     *
     * @param value the error value to set this cell to.  For formulas we'll set the
     *        precalculated value ??? IS THIS RIGHT??? , for errors we'll set
     *        its value. For other types we will change the cell to an error
     *        cell and set its value.
     */

    public void setCellErrorValue(byte value)
    {
        if ((cellType != CELL_TYPE_ERROR) && (cellType != CELL_TYPE_FORMULA))
        {
            setCellType(CELL_TYPE_ERROR, false);
        }
        (( BoolErrRecord ) record).setValue(value);
        errorValue = value;
    }

    /**
     * get the value of the cell as a boolean.  For strings, numbers, and errors, we throw an exception.
     * For blank cells we return a false.
     */

    public boolean getBooleanCellValue()
    {
        if (cellType == CELL_TYPE_BOOLEAN)
        {
            return booleanValue;
        }
        if (cellType == CELL_TYPE_BLANK)
        {
            return false;
        }
        throw new NumberFormatException(
            "You cannot get a boolean value from a non-boolean cell");
    }

    /**
     * get the value of the cell as an error code.  For strings, numbers, and booleans, we throw an exception.
     * For blank cells we return a 0.
     */

    public byte getErrorCellValue()
    {
        if (cellType == CELL_TYPE_ERROR)
        {
            return errorValue;
        }
        if (cellType == CELL_TYPE_BLANK)
        {
            return ( byte ) 0;
        }
        throw new NumberFormatException(
            "You cannot get an error value from a non-error cell");
    }

    /**
     * set the style for the cell.  The style should be an HSSFCellStyle created/retreived from
     * the HSSFWorkbook.
     *
     * @param style  reference contained in the workbook
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */

    public void setCellStyle(HSSFCellStyle style)
    {
        cellStyle = style;
        record.setXFIndex(style.getIndex());
    }

    /**
     * get the style for the cell.  This is a reference to a cell style contained in the workbook
     * object.
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
     */

    public HSSFCellStyle getCellStyle()
    {
        return cellStyle;
    }

    /**
     * used for internationalization, currently 0 for compressed unicode or 1 for 16-bit
     *
     * @see #ENCODING_COMPRESSED_UNICODE
     * @see #ENCODING_UTF_16
     *
     * @return 1 or 0 for compressed or uncompressed (used only with String type)
     */

    public short getEncoding()
    {
        return encoding;
    }

    /**
     * set the encoding to either 8 or 16 bit. (US/UK use 8-bit, rest of the western world use 16bit)
     *
     * @see #ENCODING_COMPRESSED_UNICODE
     * @see #ENCODING_UTF_16
     *
     * @param encoding either ENCODING_COMPRESSED_UNICODE (0) or ENCODING_UTF_16 (1)
     */

    public void setEncoding(short encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Should only be used by HSSFSheet and friends.  Returns the low level CellValueRecordInterface record
     *
     * @return CellValueRecordInterface representing the cell via the low level api.
     */

    protected CellValueRecordInterface getCellValueRecord()
    {
        return record;
    }

    /**
     * @throws RuntimeException if the bounds are exceeded.
     */
    private void checkBounds(int cellNum) {
      if (cellNum > 255) {
          throw new RuntimeException("You cannot have more than 255 columns "+
                    "in a given row (IV).  Because Excel can't handle it");
      }
      else if (cellNum < 0) {
          throw new RuntimeException("You cannot reference columns with an index of less then 0.");
      }
    }
    
    /**
     * Sets this cell as the active cell for the worksheet
     */
    public void setAsActiveCell()
    {
        this.sheet.setActiveCellRow(this.row);
        this.sheet.setActiveCellCol(this.cellNum);
    }
}
