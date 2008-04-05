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
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSides;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment.Enum;


public class XSSFCellStyle implements CellStyle {
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
	public XSSFCellStyle(CTXf cellXf, CTXf cellStyleXf, StylesSource stylesSource) {
		this.stylesSource = stylesSource;
		this.cellXf = cellXf;
		this.cellStyleXf = cellStyleXf;
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
	
	public short getAlignment() {
		return (short)getAlignmentEnum().intValue();
	}
	
	public STHorizontalAlignment.Enum getAlignmentEnum() {
		return getCellAlignment().getHorizontal();
	}

	public short getBorderBottom() {
		return (short) (getBorderStyle(BorderSides.BOTTOM).intValue() - 1);
	}
	
	public String getBorderBottomAsString() {
		return getBorderStyle(BorderSides.BOTTOM).toString();
	}

	public short getBorderLeft() {
		return (short) (getBorderStyle(BorderSides.LEFT).intValue() - 1);
	}
	
	public String getBorderLeftAsString() {
		return getBorderStyle(BorderSides.LEFT).toString();
	}

	public short getBorderRight() {
		return (short) (getBorderStyle(BorderSides.RIGHT).intValue() - 1);
	}
	
	public String getBorderRightAsString() {
		return getBorderStyle(BorderSides.RIGHT).toString();
	}

	public short getBorderTop() {
		return (short) (getBorderStyle(BorderSides.TOP).intValue() - 1);
	}
	
	public String getBorderTopAsString() {
		return getBorderStyle(BorderSides.TOP).toString();
	}

	public short getBottomBorderColor() {
		return getBorderColorBySide(BorderSides.BOTTOM);
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

	public short getFillForegroundColor() {
		return (short) getCellFill().getFillForegroundColor().getIndexed();
	}

	public short getFillPattern() {
		return (short) getCellFill().getPatternType().intValue();
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
		// TODO Auto-generated method stub
		return 0;
	}

	public short getLeftBorderColor() {
		return getBorderColorBySide(BorderSides.LEFT);
	}

	public boolean getLocked() {
		return getCellProtection().getLocked();
	}

	public short getRightBorderColor() {
		return getBorderColorBySide(BorderSides.RIGHT);
	}

	public short getRotation() {
		return (short) getCellAlignment().getTextRotation();
	}

	public short getTopBorderColor() {
		return getBorderColorBySide(BorderSides.TOP);
	}

	public short getVerticalAlignment() {
		return (short) getVerticalAlignmentEnum().intValue();
	}
	
	public STVerticalAlignment.Enum getVerticalAlignmentEnum() {
		return getCellAlignment().getVertical();
	}

	public boolean getWrapText() {
		return getCellAlignment().getWrapText();
	}

	public void setAlignment(short align) {
		getCellAlignment().setHorizontal(STHorizontalAlignment.Enum.forInt(align));
	}
	
	public void setAlignementEnum(STHorizontalAlignment.Enum align) {
		getCellAlignment().setHorizontal(align);
	}

	public void setBorderBottom(short border) {
		// TODO Auto-generated method stub
		
	}

	public void setBorderLeft(short border) {
		// TODO Auto-generated method stub
		
	}

	public void setBorderRight(short border) {
		// TODO Auto-generated method stub
		
	}

	public void setBorderTop(short border) {
		// TODO Auto-generated method stub
		
	}

	public void setBottomBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setDataFormat(short fmt) {
		cellXf.setNumFmtId((long)fmt);
	}

	public void setFillBackgroundColor(short bg) {
		// TODO Auto-generated method stub
		
	}

	public void setFillForegroundColor(short bg) {
		// TODO Auto-generated method stub
		
	}

	public void setFillPattern(short fp) {
		// TODO Auto-generated method stub
		
	}

	public void setFont(Font font) {
		// TODO Auto-generated method stub
		
	}

	public void setHidden(boolean hidden) {
		getCellProtection().setHidden(hidden);
	}

	public void setIndention(short indent) {
		getCellAlignment().setIndent(indent);
	}

	public void setLeftBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setLocked(boolean locked) {
		getCellProtection().setLocked(locked);
	}

	public void setRightBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setRotation(short rotation) {
		getCellAlignment().setTextRotation(rotation);
	}

	public void setTopBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setVerticalAlignment(short align) {
		setVerticalAlignmentEnum(STVerticalAlignment.Enum.forInt(align));
	}

	public void setVerticalAlignmentEnum(STVerticalAlignment.Enum align) {
		getCellAlignment().setVertical(align);
	}

	public void setWrapText(boolean wrapped) {
		getCellAlignment().setWrapText(wrapped);
	}

	private XSSFCellBorder getCellBorder() {
		if (cellBorder == null) {
			// TODO make a common Cell Border object
			cellBorder = ((StylesTable)stylesSource).getBorderAt(getBorderId());
		}
		return cellBorder;
	}

	private int getBorderId() {
		if (cellXf.isSetBorderId()) {
			return (int) cellXf.getBorderId();
		}
		return (int) cellStyleXf.getBorderId();
	}
	
	private XSSFCellFill getCellFill() {
		if (cellFill == null) {
			cellFill = ((StylesTable)stylesSource).getFillAt(getFillId());
		}
		return cellFill;
	}
	
	private int getFillId() {
		if (cellXf.isSetFillId()) {
			return (int) cellXf.getFillId();
		}
		return (int) cellStyleXf.getFillId();
	}

	private STBorderStyle.Enum getBorderStyle(BorderSides side) {
		return getCellBorder().getBorderStyle(side);
	}

	private short getBorderColorBySide(BorderSides side) {
		return (short) getCellBorder().getBorderColor(side).getIndexed();
	}
	
	private int getFontId() {
		if (cellXf.isSetFontId()) {
			return (int) cellXf.getFontId();
		}
		return (int) cellStyleXf.getFontId();
	}

	private CTCellProtection getCellProtection() {
		if (cellXf.getProtection() == null) {
			CTCellProtection protection = cellXf.addNewProtection();
		}
		return cellXf.getProtection();
	}

	private XSSFCellAlignment getCellAlignment() {
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
	
}
