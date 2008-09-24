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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.StylesSource;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;


public class XSSFCellStyle implements CellStyle {

    private int cellXfId;
    private int cellStyleXfId;
    private StylesSource stylesSource;
    private CTXf cellXf;
    private CTXf cellStyleXf;
    private XSSFCellBorder cellBorder;
    private XSSFCellFill cellFill;
    private XSSFFont font;
    private XSSFCellAlignment cellAlignment;

    /**
     * Creates a Cell Style from the supplied parts
     * @param cellXf The main XF for the cell
     * @param cellStyleXf Optional, style xf
     * @param stylesSource Styles Source to work off
     */
    public XSSFCellStyle(int cellXfId, int cellStyleXfId, StylesTable stylesSource) {
        this.cellXfId = cellXfId;
        this.cellStyleXfId = cellStyleXfId;
        this.stylesSource = stylesSource;
        this.cellXf = stylesSource.getCellXfAt(this.cellXfId);
        this.cellStyleXf = stylesSource.getCellStyleXfAt(this.cellStyleXfId);
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getCoreXf() {
        return cellXf;
    }
    /**
     * Used so that StylesSource can figure out our location
     */
    public CTXf getStyleXf() {
        return cellStyleXf;
    }

    /**
     * Creates an empty Cell Style
     */
    public XSSFCellStyle(StylesSource stylesSource) {
        this.stylesSource = stylesSource;
        // We need a new CTXf for the main styles
        // TODO decide on a style ctxf
        cellXf = CTXf.Factory.newInstance();
        cellStyleXf = null;
    }
    
    /**
     * Verifies that this style belongs to the supplied Workbook
     *  Styles Source.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToStylesSource(StylesSource src) {
        if(this.stylesSource != src) {
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook Stlyes Source. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
        }
    }

    /**
     * Clones all the style information from another
     *  XSSFCellStyle, onto this one. This
     *  XSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this XSSFCellStyle will be lost!
     *
     * The source XSSFCellStyle could be from another
     *  XSSFWorkbook if you like. This allows you to
     *  copy styles from one XSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
        if(source instanceof XSSFCellStyle) {
            this.cloneStyleFrom((XSSFCellStyle)source);
        }
        throw new IllegalArgumentException("Can only clone from one XSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
    }
    public void cloneStyleFrom(XSSFCellStyle source) {
        throw new IllegalStateException("TODO");
    }

    public short getAlignment() {
        return (short)(getAlignmentEnum().ordinal());
    }

    public HorizontalAlignment getAlignmentEnum() {
        return getCellAlignment().getHorizontal();
    }

    public short getBorderBottom() {
        return getBorderStyleAsShort(BorderSide.BOTTOM);
    }

    public String getBorderBottomAsString() {
        return getBorderStyleAsString(BorderSide.BOTTOM);
    }

    public short getBorderLeft() {
        return getBorderStyleAsShort(BorderSide.LEFT);
    }

    public String getBorderLeftAsString() {
        return getBorderStyleAsString(BorderSide.LEFT);
    }

    public short getBorderRight() {
        return getBorderStyleAsShort(BorderSide.RIGHT);
    }

    public String getBorderRightAsString() {
        return getBorderStyleAsString(BorderSide.RIGHT);
    }

    public short getBorderTop() {
        return getBorderStyleAsShort(BorderSide.TOP);
    }

    public String getBorderTopAsString() {
        return getBorderStyleAsString(BorderSide.TOP);
    }

    public short getBottomBorderColor() {
        return getBorderColorIndexed(BorderSide.BOTTOM);
    }

    public short getDataFormat() {
        return (short)cellXf.getNumFmtId();
    }
    public String getDataFormatString() {
        return stylesSource.getNumberFormatAt(getDataFormat());
    }

    public short getFillBackgroundColor() {
        return (short) getCellFill().getFillBackgroundColor().getIndexed();
    }

    public XSSFColor getFillBackgroundRgbColor() {
        return getCellFill().getFillBackgroundColor();
    }

    public short getFillForegroundColor() {
        return (short) getCellFill().getFillForegroundColor().getIndexed();
    }

    public XSSFColor getFillForegroundRgbColor() {
        return  getCellFill().getFillForegroundColor();
    }

    public short getFillPattern() {
	int fp= getCellFill().getPatternType().intValue();
	switch (fp) {
	case STPatternType.INT_NONE:
		return CellStyle.NO_FILL;
	case STPatternType.INT_SOLID: 
	    return CellStyle.SOLID_FOREGROUND;
	case STPatternType.INT_LIGHT_GRAY:
	    return CellStyle.FINE_DOTS;
	case STPatternType.INT_DARK_GRID:
		return CellStyle.ALT_BARS;
	case STPatternType.INT_DARK_GRAY:
	    return CellStyle.SPARSE_DOTS;
	case STPatternType.INT_DARK_HORIZONTAL: 
	    return CellStyle.THICK_HORZ_BANDS;
	case STPatternType.INT_DARK_VERTICAL:
	    return CellStyle.THICK_VERT_BANDS;
	case STPatternType.INT_DARK_UP:
		return CellStyle.THICK_BACKWARD_DIAG;
	case STPatternType.INT_DARK_DOWN:
		return CellStyle.THICK_FORWARD_DIAG;
	case STPatternType.INT_GRAY_0625:
		return CellStyle.BIG_SPOTS;
	case STPatternType.INT_DARK_TRELLIS:
		return CellStyle.BRICKS;
	case STPatternType.INT_LIGHT_HORIZONTAL:
		return CellStyle.THIN_HORZ_BANDS;
	case STPatternType.INT_LIGHT_VERTICAL:
		return CellStyle.THIN_VERT_BANDS;
	case STPatternType.INT_LIGHT_UP:
		return CellStyle.THIN_BACKWARD_DIAG;
	case STPatternType.INT_LIGHT_DOWN:
		return CellStyle.THIN_FORWARD_DIAG;
	case STPatternType.INT_LIGHT_GRID:
		return CellStyle.SQUARES;
	case STPatternType.INT_LIGHT_TRELLIS:
		return CellStyle.DIAMONDS;
	case STPatternType.INT_GRAY_125:
		return CellStyle.LESS_DOTS;
/*		
	case STPatternType.INT_GRAY_0625:
		return CellStyle.LEAST_DOTS;
*/		
	default:
	    	return CellStyle.NO_FILL;
	}
    //    return (short) getCellFill().getPatternType().intValue();
    }

    public Font getFont(Workbook parentWorkbook) {
        return getFont();
    }

    public Font getFont() {
        if (font == null) {
            font = (XSSFFont) ((StylesTable)stylesSource).getFontAt(getFontId());
        }
        return font;
    }

    public short getFontIndex() {
        return (short) getFontId();
    }

    public boolean getHidden() {
        return getCellProtection().getHidden();
    }

    public short getIndention() {
        return (short) getCellAlignment().getIndent();
    }

    public short getIndex() {
        return (short) this.cellXfId;
    }

    public short getLeftBorderColor() {
        return getBorderColorIndexed(BorderSide.LEFT);
    }

    public boolean getLocked() {
        return getCellProtection().getLocked();
    }

    public short getRightBorderColor() {
        return getBorderColorIndexed(BorderSide.RIGHT);
    }

    public short getRotation() {
        return (short) getCellAlignment().getTextRotation();
    }

    public short getTopBorderColor() {
        return getBorderColorIndexed(BorderSide.TOP);
    }

    public short getVerticalAlignment() {
        return (short) (getVerticalAlignmentEnum().ordinal());
    }

    public VerticalAlignment getVerticalAlignmentEnum() {
        return getCellAlignment().getVertical();
    }

    public boolean getWrapText() {
        return getCellAlignment().getWrapText();
    }

    public void setAlignment(short align) {
        getCellAlignment().setHorizontal(HorizontalAlignment.values()[align]);
    }

    public void setAlignment(HorizontalAlignment align) {
        setAlignment((short)align.ordinal());
    }

    public void setBorderBottom(short border) {
        setBorderBottomEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderBottomEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.BOTTOM, style);
    }

    public void setBorderLeft(short border) {
        setBorderLeftEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderLeftEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.LEFT, style);
    }

    public void setBorderRight(short border) {
        setBorderRightEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderRightEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.RIGHT, style);
    }

    public void setBorderTop(short border) {
        setBorderTopEnum(STBorderStyle.Enum.forInt(border));
    }

    public void setBorderTopEnum(STBorderStyle.Enum style) {
        getCellBorder().setBorderStyle(BorderSide.TOP, style);
    }

    public void setBottomBorderColor(short color) {
        setBorderColorIndexed(BorderSide.BOTTOM, color);
    }

    public void setDataFormat(short fmt) {
        cellXf.setNumFmtId((long)fmt);
    }

    public void setFillBackgroundRgbColor(XSSFColor color) {
        cellFill=getCellFill();
        cellFill.setFillBackgroundRgbColor(color);
    }

    public void setFillBackgroundColor(short bg) {
        getCellFill().setFillBackgroundColor(bg);
    }

    public void setFillForegroundRgbColor(XSSFColor color) {
        getCellFill().setFillForegroundRgbColor(color);
    }

    public void setFillForegroundColor(short bg) {
	getCellFill().setFillForegroundColor(bg);
    }

    public void setFillPattern(short fp) {
	cellFill=getCellFill();
	switch (fp) {
	case CellStyle.NO_FILL:
		cellFill.setPatternType(STPatternType.NONE);
	    break;
	case CellStyle.SOLID_FOREGROUND:
		cellFill.setPatternType(STPatternType.SOLID);
		    break;
	case CellStyle.FINE_DOTS:
		cellFill.setPatternType(STPatternType.LIGHT_GRAY);
		    break;
	case CellStyle.ALT_BARS:
		cellFill.setPatternType(STPatternType.DARK_GRID);
		    break;
	case CellStyle.SPARSE_DOTS:
		cellFill.setPatternType(STPatternType.DARK_GRAY);
		    break;
	case CellStyle.THICK_HORZ_BANDS:
		cellFill.setPatternType(STPatternType.DARK_HORIZONTAL);
		    break;
	case CellStyle.THICK_VERT_BANDS:
		cellFill.setPatternType(STPatternType.DARK_VERTICAL);
		    break;
	case CellStyle.THICK_BACKWARD_DIAG:
		cellFill.setPatternType(STPatternType.DARK_UP);
		    break;
	case CellStyle.THICK_FORWARD_DIAG:
		cellFill.setPatternType(STPatternType.DARK_DOWN);
		    break;
	case CellStyle.BIG_SPOTS:
		cellFill.setPatternType(STPatternType.GRAY_0625);
		    break;
	case CellStyle.BRICKS:
		cellFill.setPatternType(STPatternType.DARK_TRELLIS);
		    break;
	case CellStyle.THIN_HORZ_BANDS:
		cellFill.setPatternType(STPatternType.LIGHT_HORIZONTAL);
		    break;
	case CellStyle.THIN_VERT_BANDS:
		cellFill.setPatternType(STPatternType.LIGHT_VERTICAL);
		    break;
	case CellStyle.THIN_BACKWARD_DIAG:
		cellFill.setPatternType(STPatternType.LIGHT_UP);
		    break;
	case CellStyle.THIN_FORWARD_DIAG:
		cellFill.setPatternType(STPatternType.LIGHT_DOWN);
		    break;
	case CellStyle.SQUARES:
		cellFill.setPatternType(STPatternType.LIGHT_GRID);
		    break;
	case CellStyle.DIAMONDS:
		cellFill.setPatternType(STPatternType.LIGHT_TRELLIS);
		    break;
	case CellStyle.LESS_DOTS:
		cellFill.setPatternType(STPatternType.GRAY_125);
		    break;
	case CellStyle.LEAST_DOTS:
		cellFill.setPatternType(STPatternType.GRAY_0625);
		    break;
	default: throw new RuntimeException("Fill type ["+fp+"] not accepted");
	}
    }

    public void setFont(Font font) {
        if(font!=null){
            long index=this.stylesSource.putFont(font);
            this.cellXf.setFontId(index);
        }
        this.cellXf.setApplyFont(true);
    }

    public void setHidden(boolean hidden) {
        getCellProtection().setHidden(hidden);
    }

    public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
    }

    public void setLeftBorderColor(short color) {
        setBorderColorIndexed(BorderSide.LEFT, color);
    }

	
	private void setBorderColorIndexed(BorderSide side, XSSFColor color) {
		this.cellBorder.setBorderColor(side, color);
	}


    public void setLocked(boolean locked) {
        getCellProtection().setLocked(locked);
    }


    public void setRightBorderColor(short color) {
        setBorderColorIndexed(BorderSide.RIGHT, color);
    }

    public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
    }

    public void setTopBorderColor(short color) {
        setBorderColorIndexed(BorderSide.TOP, color);
    }

    public void setVerticalAlignment(short align) {
        getCellAlignment().setVertical(VerticalAlignment.values()[align]);
    }

    public void setVerticalAlignment(VerticalAlignment align) {
        getCellAlignment().setVertical(align);
    }

    public void setWrapText(boolean wrapped) {
        getCellAlignment().setWrapText(wrapped);
    }

    public XSSFColor getBorderColor(BorderSide side) {
        return getCellBorder().getBorderColor(side);
    }

    public void setBorderColor(BorderSide side, XSSFColor color) {
        getCellBorder().setBorderColor(side, color);
    }
    
    private XSSFCellBorder getCellBorder() {
        if (cellBorder == null) {
            // TODO make a common Cell Border object
            int borderId=getBorderId();
            if(borderId==-1){
        	cellBorder=new XSSFCellBorder();
		long index=((StylesTable)stylesSource).putBorder(cellBorder);
		this.cellXf.setBorderId(index);
		this.cellXf.setApplyBorder(true);
            }
            else{
        	cellBorder = ((StylesTable)stylesSource).getBorderAt(borderId);
            }
        }
        return cellBorder;
    }

    private int getBorderId() {
        if (cellXf.isSetBorderId() && cellXf.getBorderId()>0) {
            return (int) cellXf.getBorderId();
        }
        return -1;
      //  return (int) cellStyleXf.getBorderId();
    }
    
    private XSSFCellFill getCellFill() {
	if (cellFill == null) {
	    int fillId=getFillId();
	    if(fillId == -1) {
		cellFill=new XSSFCellFill();
		long index=((StylesTable)stylesSource).putFill(cellFill);
		this.cellXf.setFillId(index);
		this.cellXf.setApplyFill(true);
	    }
	    else{
		cellFill=((StylesTable)stylesSource).getFillAt(fillId);
	    }
	}
	return cellFill;
    }

    private int getFillId() {
        if (cellXf.isSetFillId() && cellXf.getFillId()>0) {
            return (int) cellXf.getFillId();
        }
        //return (int) cellStyleXf.getFillId();
        return -1; 
    }

    private int getFontId() {
        if (cellXf.isSetFontId()) {
            return (int) cellXf.getFontId();
        }
        return (int) cellStyleXf.getFontId();
    }

    private CTCellProtection getCellProtection() {
        if (cellXf.getProtection() == null) {
            cellXf.addNewProtection();
        }
        return cellXf.getProtection();
    }

    public XSSFCellAlignment getCellAlignment() {
        if (this.cellAlignment == null) {
            this.cellAlignment = new XSSFCellAlignment(getCTCellAlignment());
        }
        return this.cellAlignment;
    }

    private CTCellAlignment getCTCellAlignment() {
        if (cellXf.getAlignment() == null) {
            cellXf.setAlignment(CTCellAlignment.Factory.newInstance());
        }
        return cellXf.getAlignment();
    }

    private short getBorderColorIndexed(BorderSide side) {
        return (short) getBorderColor(side).getIndexed();
    }

    private void setBorderColorIndexed(BorderSide side, int color) {
        getBorderColor(side).setIndexed(color);
    }

    private short getBorderStyleAsShort(BorderSide side) {
        return (short) (getBorderStyle(side).intValue() - 1);
    }

    private String getBorderStyleAsString(BorderSide side) {
        return getBorderStyle(side).toString();
    }

    private STBorderStyle.Enum getBorderStyle(BorderSide side) {
        return getCellBorder().getBorderStyle(side);
    }

}
