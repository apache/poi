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

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.model.SharedStringSource;
import org.apache.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellType;

/**
 *
 */
public final class XSSFCell implements Cell {

    private static final String FALSE_AS_STRING = "0";
    private static final String TRUE_AS_STRING  = "1";
    private final CTCell cell;
    private final XSSFRow row;
    private int cellNum;
    private SharedStringSource sharedStringSource;
    private StylesTable stylesSource;

    private POILogger logger = POILogFactory.getLogger(XSSFCell.class);

    /**
     * Create a new XSSFCell. This method is protected to be used only by
     * tests.
     */
    protected XSSFCell(XSSFRow row) {
        this(row, CTCell.Factory.newInstance());
    }

    public XSSFCell(XSSFRow row, CTCell cell) {
        this.cell = cell;
        this.row = row;
        if (cell.getR() != null) {
            this.cellNum = parseCellNum(cell.getR());
        }
        this.sharedStringSource = row.getSheet().getWorkbook().getSharedStringSource();
        this.stylesSource = (StylesTable)row.getSheet().getWorkbook().getStylesSource();
    }

    protected SharedStringSource getSharedStringSource() {
        return this.sharedStringSource;
    }
    protected StylesSource getStylesSource() {
        return this.stylesSource;
    }

	public Sheet getSheet() {
		return this.row.getSheet();
	}

    public boolean getBooleanCellValue() {
        if (STCellType.B != cell.getT()) {
            throw new NumberFormatException("You cannot get a boolean value from a non-boolean cell");
        }
        if (cell.isSetV()) {
            return (TRUE_AS_STRING.equals(this.cell.getV()));
        }

        return false;
    }

    public Comment getCellComment() {
        return row.getSheet().getCellComment(row.getRowNum(), getCellNum());
    }

    public String getCellFormula() {
        if(this.cell.getF() == null) {
            throw new NumberFormatException("You cannot get a formula from a non-formula cell");
        }
        return this.cell.getF().getStringValue();
    }

    public short getCellNum() {
        return (short)this.cellNum;
    }
	public int getRowIndex() {
		return row.getRowNum();
	}

    public XSSFCellStyle getCellStyle() {
        // Zero is the empty default
        if(this.cell.getS() > 0) {
            return stylesSource.getStyleAt(this.cell.getS());
        }
        return null;
    }

    public int getCellType() {
        // Detecting formulas is quite pesky,
        //  as they don't get their type set
        if(this.cell.getF() != null) {
            return CELL_TYPE_FORMULA;
        }

        switch (this.cell.getT().intValue()) {
        case STCellType.INT_B:
            return CELL_TYPE_BOOLEAN;
        case STCellType.INT_N:
            if(!cell.isSetV()) {
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
            return CELL_TYPE_STRING;
        case STCellType.INT_STR:
            return CELL_TYPE_FORMULA;
        default:
            throw new IllegalStateException("Illegal cell type: " + this.cell.getT());
        }
    }

    public Date getDateCellValue() {
        if (STCellType.N == this.cell.getT() || STCellType.STR == this.cell.getT()) {
            double value = this.getNumericCellValue();
            if (false /* book.isUsing1904DateWindowing() */) {  // FIXME
                return HSSFDateUtil.getJavaDate(value,true);
            }
            else {
                return HSSFDateUtil.getJavaDate(value,false);
            }
        }
        throw new NumberFormatException("You cannot get a date value from a cell of type " + this.cell.getT());
    }

    /**
     * Returns the error message, such as #VALUE!
     */
    public String getErrorCellString() {
        if (STCellType.E != cell.getT()) {
            throw new NumberFormatException("You cannot get a error value from a non-error cell");
        }
        if (this.cell.isSetV()) {
            return this.cell.getV();
        }
        return null;
    }
    /**
     * Returns the error type, in the same way that
     *  HSSFCell does. See {@link Cell} for details
     */
    public byte getErrorCellValue() {
        if (STCellType.E != cell.getT()) {
            throw new NumberFormatException("You cannot get a error value from a non-error cell");
        }
        if (this.cell.isSetV()) {
            String errS = this.cell.getV();
            if(errS.equals(Cell.ERROR_NULL.getStringRepr())) {
                return Cell.ERROR_NULL.getType();
            }
            if(errS.equals(Cell.ERROR_DIV0.getStringRepr())) {
                return Cell.ERROR_DIV0.getType();
            }
            if(errS.equals(Cell.ERROR_VALUE.getStringRepr())) {
                return Cell.ERROR_VALUE.getType();
            }
            if(errS.equals(Cell.ERROR_REF.getStringRepr())) {
                return Cell.ERROR_REF.getType();
            }
            if(errS.equals(Cell.ERROR_NAME.getStringRepr())) {
                return Cell.ERROR_NAME.getType();
            }
            if(errS.equals(Cell.ERROR_NUM.getStringRepr())) {
                return Cell.ERROR_NUM.getType();
            }
            return Cell.ERROR_NA.getType();
        }
        return 0;
    }

    public double getNumericCellValue() {
        if (STCellType.N != cell.getT() && STCellType.STR != cell.getT()) {
            throw new NumberFormatException("You cannot get a numeric value from a non-numeric cell");
        }
        if (this.cell.isSetV()) {
            return Double.parseDouble(this.cell.getV());
        }
        // else - cell is blank.

        // TODO - behaviour in the case of blank cells.
        // Revise spec, choose best alternative below, and comment why.
        if (true) {
            // returning NaN from a blank cell seems wrong
            // there are a few junits which assert this behaviour, though.
            return Double.NaN;
        }
        if (true) {
            // zero is probably a more reasonable value.
            return 0.0;
        } else {
            // or perhaps disallow reading value from blank cell.
            throw new RuntimeException("You cannot get a numeric value from a blank cell");
        }
        // Note - it would be nice if the behaviour is consistent with getRichStringCellValue
        // (i.e. whether to return empty string or throw exception).
    }

    public XSSFRichTextString getRichStringCellValue() {
        if(this.cell.getT() == STCellType.INLINE_STR) {
            if(this.cell.isSetV()) {
                return new XSSFRichTextString(this.cell.getV());
            } else {
                return new XSSFRichTextString("");
            }
        }
        if(this.cell.getT() == STCellType.S) {
            XSSFRichTextString rt;
            if(this.cell.isSetV()) {
                int sRef = Integer.parseInt(this.cell.getV());
                rt = new XSSFRichTextString(sharedStringSource.getEntryAt(sRef));
            } else {
                rt = new XSSFRichTextString("");
            }
            rt.setStylesTableReference(stylesSource);
            return rt;
        }
        throw new NumberFormatException("You cannot get a string value from a non-string cell");
    }

    public void setAsActiveCell() {
        row.getSheet().setActiveCell(cell.getR());
    }

    public void setCellComment(Comment comment) {
        String cellRef =
            new CellReference(row.getRowNum(), getCellNum()).formatAsString();
        row.getSheet().setCellComment(cellRef, (XSSFComment)comment);
    }

    public void setCellErrorValue(byte value) {
        if(value == Cell.ERROR_DIV0.getType()) {
            setCellErrorValue(Cell.ERROR_DIV0);
        } else if(value == Cell.ERROR_NA.getType()) {
            setCellErrorValue(Cell.ERROR_NA);
        } else if(value == Cell.ERROR_NAME.getType()) {
            setCellErrorValue(Cell.ERROR_NAME);
        } else if(value == Cell.ERROR_NULL.getType()) {
            setCellErrorValue(Cell.ERROR_NULL);
        } else if(value == Cell.ERROR_NUM.getType()) {
            setCellErrorValue(Cell.ERROR_NUM);
        } else if(value == Cell.ERROR_REF.getType()) {
            setCellErrorValue(Cell.ERROR_REF);
        } else if(value == Cell.ERROR_VALUE.getType()) {
            setCellErrorValue(Cell.ERROR_VALUE);
        } else {
            logger.log(POILogger.WARN, "Unknown error type " + value + " specified, treating as #N/A");
            setCellErrorValue(Cell.ERROR_NA);
        }
    }
    public void setCellErrorValue(CELL_ERROR_TYPE errorType) {
        if ((this.cell.getT() != STCellType.E) && (this.cell.getT() != STCellType.STR))
        {
            this.cell.setT(STCellType.E);
        }
        this.cell.setV( errorType.getStringRepr() );
    }


    public void setCellFormula(String formula) {
        if (this.cell.getT() != STCellType.STR)
        {
            this.cell.setT(STCellType.STR);
        }
        CTCellFormula f =  CTCellFormula.Factory.newInstance();
        f.setStringValue(formula);
        this.cell.setF(f);
        // XXX: is this correct? Should we recompute the value when the formula changes?
        if (this.cell.isSetV()) {
            this.cell.unsetV();
        }

    }

    public void setCellNum(int num) {
        setCellNum((short)num);
    }
    public void setCellNum(short num) {
        checkBounds(num);
        this.cellNum = num;
        this.cell.setR(formatPosition());
    }

    protected static short parseCellNum(String r) {
        r = r.split("\\d+")[0];
        if (r.length() == 1) {
            return (short) (r.charAt(0) - 'A');
        } else {
            return (short) (r.charAt(1) - 'A' + 26 * (r.charAt(0) - '@'));

        }
    }

    protected String formatPosition() {
        int col = this.getCellNum();
        String result = Character.valueOf((char) (col % 26 + 'A')).toString();
        if (col >= 26){
            col = col / 26;
            result = Character.valueOf((char) (col + '@')) + result;
        }
        result = result + String.valueOf(row.getRowNum() + 1);
        return result;
    }

    public void setCellStyle(CellStyle style) {
        if(style == null) {
            this.cell.setS(0);
        } else {
			XSSFCellStyle xStyle = (XSSFCellStyle)style;
			xStyle.verifyBelongsToStylesSource(
				row.getSheet().getWorkbook().getStylesSource()
			);

            this.cell.setS(
                row.getSheet().getWorkbook().getStylesSource().putStyle(xStyle)
            );
        }
    }

    public void setCellType(int cellType) {
        switch (cellType) {
        case CELL_TYPE_BOOLEAN:
            this.cell.setT(STCellType.B);
            break;
        case CELL_TYPE_NUMERIC:
            this.cell.setT(STCellType.N);
            break;
        case CELL_TYPE_ERROR:
            this.cell.setT(STCellType.E);
            break;
        case CELL_TYPE_STRING:
            this.cell.setT(STCellType.S);
            break;
         default:
             throw new IllegalArgumentException("Illegal type: " + cellType);
        }
    }

    public void setCellValue(double value) {
        if ((this.cell.getT() != STCellType.N) && (this.cell.getT() != STCellType.STR))
        {
            this.cell.setT(STCellType.N);
        }
        this.cell.setV(String.valueOf(value));
    }

    public void setCellValue(Date value) {
        setCellValue(HSSFDateUtil.getExcelDate(value, false /*this.book.isUsing1904DateWindowing()*/)); // FIXME
    }

    public void setCellValue(Calendar value) {
        // TODO Auto-generated method stub

    }

    public void setCellValue(String str) {
        this.setCellValue(new XSSFRichTextString(str));
    }
    
    public void setCellValue(RichTextString value) {
        if(this.cell.getT() == STCellType.INLINE_STR) {
            this.cell.setV(value.getString());
            return;
        }
        if(this.cell.getT() != STCellType.S) {
            this.cell.setT(STCellType.S);
        }
        XSSFRichTextString rt = (XSSFRichTextString)value;
        rt.setStylesTableReference(stylesSource);
        int sRef = sharedStringSource.addEntry(rt.getCTRst());
        this.cell.setV(Integer.toString(sRef));
    }

    public void setCellValue(boolean value) {
        if ((this.cell.getT() != STCellType.B) && (this.cell.getT() != STCellType.STR))
        {
            this.cell.setT(STCellType.B);
        }
        this.cell.setV(value ? TRUE_AS_STRING : FALSE_AS_STRING);
    }

    public String toString() {
        return "[" + this.row.getRowNum() + "," + this.getCellNum() + "] " + this.cell.getV();
    }

    /**
     * Returns the raw, underlying ooxml value for the cell
     */
    public String getRawValue() {
        return this.cell.getV();
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

	public Hyperlink getHyperlink() {
		return row.getSheet().getHyperlink(row.getRowNum(), cellNum);
	}
	public void setHyperlink(Hyperlink hyperlink) {
		XSSFHyperlink link = (XSSFHyperlink)hyperlink;
		
		// Assign to us
		link.setCellReference( new CellReference(row.getRowNum(), cellNum).formatAsString() );
		
		// Add to the lists
		row.getSheet().setCellHyperlink(link);
	}
}
