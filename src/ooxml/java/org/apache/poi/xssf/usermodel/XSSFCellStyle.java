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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSides;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle.Enum;


public class XSSFCellStyle implements CellStyle {
	private StylesTable stylesTable;
	private CTXf cellXf;
	private CTXf cellStyleXf;
	private XSSFCellBorder cellBorder;
	
	/**
	 * @param cellXf The main XF for the cell
	 * @param cellStyleXf Optional, style xf
	 * @param stylesTable Styles Table to work off
	 */
	public XSSFCellStyle(CTXf cellXf, CTXf cellStyleXf, StylesTable stylesTable) {
		this.stylesTable = stylesTable;
		this.cellXf = cellXf;
		this.cellStyleXf = cellStyleXf;
	}

	public short getAlignment() {
		// TODO Auto-generated method stub
		return 0;
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
		return stylesTable.getNumberFormatAt(getDataFormat());
	}

	public short getFillBackgroundColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getFillForegroundColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getFillPattern() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Font getFont(Workbook parentWorkbook) {
		// TODO Auto-generated method stub
		return null;
	}

	public short getFontIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	public short getIndention() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getLeftBorderColor() {
		return getBorderColorBySide(BorderSides.LEFT);
	}

	public boolean getLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	public short getRightBorderColor() {
		return getBorderColorBySide(BorderSides.RIGHT);
	}

	public short getRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getTopBorderColor() {
		return getBorderColorBySide(BorderSides.TOP);
	}

	public short getVerticalAlignment() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getWrapText() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setAlignment(short align) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	public void setIndention(short indent) {
		// TODO Auto-generated method stub
		
	}

	public void setLeftBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setLocked(boolean locked) {
		// TODO Auto-generated method stub
		
	}

	public void setRightBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setRotation(short rotation) {
		// TODO Auto-generated method stub
		
	}

	public void setTopBorderColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setVerticalAlignment(short align) {
		// TODO Auto-generated method stub
		
	}

	public void setWrapText(boolean wrapped) {
		// TODO Auto-generated method stub
		
	}

	private XSSFCellBorder getCellBorder() {
		if (cellBorder == null) {
			cellBorder = stylesTable.getBorderAt(getBorderId());
		}
		return cellBorder;
	}

	private int getBorderId() {
		if (cellXf.isSetBorderId()) {
			return (int) cellXf.getBorderId();
		}
		return (int) cellStyleXf.getBorderId();
	}

	private Enum getBorderStyle(BorderSides side) {
		return getCellBorder().getBorderStyle(side);
	}

	private short getBorderColorBySide(BorderSides side) {
		return (short) getCellBorder().getBorderColor(side).getIndexed();
	}
	
}
