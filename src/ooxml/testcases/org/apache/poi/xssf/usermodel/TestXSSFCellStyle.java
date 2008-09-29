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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;


public class TestXSSFCellStyle extends TestCase {

	private static final int AUTO_COLOR_INDEX = 64;
	private StylesTable stylesTable;
	private CTBorder ctBorderA;
	private CTFill ctFill;
	private CTFont ctFont;
	private CTXf cellStyleXf;
	private CTXf cellXf;
	private CTCellXfs cellXfs;
	private XSSFCellStyle cellStyle;
	private CTStylesheet ctStylesheet;

	@Override
	protected void setUp() {
		stylesTable = new StylesTable();
		
		ctStylesheet = stylesTable._getRawStylesheet();
		
		// Until we do XSSFBorder properly, cheat
		ctBorderA = CTBorder.Factory.newInstance();
		XSSFCellBorder borderA = new XSSFCellBorder(ctBorderA);
		long borderId = stylesTable.putBorder(borderA);
		assertEquals(1, borderId);

		XSSFCellBorder borderB = new XSSFCellBorder();
		assertEquals(2, stylesTable.putBorder(borderB));

		ctFill = CTFill.Factory.newInstance();
		XSSFCellFill fill = new XSSFCellFill(ctFill);
		long fillId = stylesTable.putFill(fill);
		assertEquals(2, fillId);

		ctFont = CTFont.Factory.newInstance();
		XSSFFont font = new XSSFFont(ctFont);
		long fontId = stylesTable.putFont(font);
		assertEquals(1, fontId);

		cellStyleXf = ctStylesheet.addNewCellStyleXfs().addNewXf();
		cellStyleXf.setBorderId(1);
		cellStyleXf.setFillId(1);
		cellStyleXf.setFontId(1);
		
		cellXfs = ctStylesheet.addNewCellXfs();
		cellXf = cellXfs.addNewXf();
		cellXf.setXfId(1);
		cellXf.setBorderId(1);
		cellXf.setFillId(1);
		cellXf.setFontId(1);
		stylesTable.putCellStyleXf(cellStyleXf);
		stylesTable.putCellXf(cellXf);
		cellStyle = new XSSFCellStyle(1, 1, stylesTable);
	}

	public void testGetSetBorderBottom() {
		ctBorderA.addNewBottom().setStyle(STBorderStyle.THIN);
		assertEquals((short)1, cellStyle.getBorderBottom());
		cellStyle.setBorderBottom((short) 2);
		assertEquals(STBorderStyle.THIN, ctBorderA.getBottom().getStyle());
		cellStyle.setBorderBottomEnum(STBorderStyle.THICK);
		assertEquals(6, ctBorderA.getBottom().getStyle().intValue());
	}

	public void testGetBorderBottomAsString() {
		ctBorderA.addNewBottom().setStyle(STBorderStyle.THIN);
		assertEquals("thin", cellStyle.getBorderBottomAsString());
	}

	public void testGetSetBorderRight() {
		ctBorderA.addNewRight().setStyle(STBorderStyle.MEDIUM);
		assertEquals((short)2, cellStyle.getBorderRight());
		cellStyle.setBorderRight((short) 2);
		assertEquals(STBorderStyle.THIN, ctBorderA.getRight().getStyle());
		cellStyle.setBorderRightEnum(STBorderStyle.THICK);
		assertEquals(6, ctBorderA.getRight().getStyle().intValue());
	}

	public void testGetBorderRightAsString() {
		ctBorderA.addNewRight().setStyle(STBorderStyle.MEDIUM);
		assertEquals("medium", cellStyle.getBorderRightAsString());
	}

	public void testGetSetBorderLeft() {
		ctBorderA.addNewLeft().setStyle(STBorderStyle.DASHED);
		assertEquals((short)3, cellStyle.getBorderLeft());
		cellStyle.setBorderLeft((short) 2);
		assertEquals(STBorderStyle.THIN, ctBorderA.getLeft().getStyle());
		cellStyle.setBorderLeftEnum(STBorderStyle.THICK);
		assertEquals(6, ctBorderA.getLeft().getStyle().intValue());
	}

	public void testGetBorderLeftAsString() {
		ctBorderA.addNewLeft().setStyle(STBorderStyle.DASHED);
		assertEquals("dashed", cellStyle.getBorderLeftAsString());
	}

	public void testGetSetBorderTop() {
		ctBorderA.addNewTop().setStyle(STBorderStyle.HAIR);
		assertEquals((short)7, cellStyle.getBorderTop());
		cellStyle.setBorderTop((short) 2);
		assertEquals(STBorderStyle.THIN, ctBorderA.getTop().getStyle());
		cellStyle.setBorderTopEnum(STBorderStyle.THICK);
		assertEquals(6, ctBorderA.getTop().getStyle().intValue());
	}

	public void testGetBorderTopAsString() {
		ctBorderA.addNewTop().setStyle(STBorderStyle.HAIR);
		assertEquals("hair", cellStyle.getBorderTopAsString());
	}

	public void testGetSetBottomBorderColor() {
		CTColor ctColor = ctBorderA.addNewBottom().addNewColor();
		ctColor.setIndexed(2);
		assertEquals((short)2, cellStyle.getBottomBorderColor());
		CTColor anotherCtColor = CTColor.Factory.newInstance();
		anotherCtColor.setIndexed(4);
		anotherCtColor.setTheme(3);
		anotherCtColor.setRgb("1234".getBytes());
		XSSFColor anotherColor = new XSSFColor(anotherCtColor);
		cellStyle.setBorderColor(BorderSide.BOTTOM, anotherColor);
		assertEquals((short)4, cellStyle.getBottomBorderColor());
		assertEquals("1234", new String(cellStyle.getBorderColor(BorderSide.BOTTOM).getRgb()));
	}

	public void testGetSetTopBorderColor() {
		CTColor ctColor = ctBorderA.addNewTop().addNewColor();
		ctColor.setIndexed(5);
		assertEquals((short)5, cellStyle.getTopBorderColor());
		CTColor anotherCtColor = CTColor.Factory.newInstance();
		anotherCtColor.setIndexed(7);
		anotherCtColor.setTheme(3);
		anotherCtColor.setRgb("abcd".getBytes());
		XSSFColor anotherColor = new XSSFColor(anotherCtColor);
		cellStyle.setBorderColor(BorderSide.TOP, anotherColor);
		assertEquals((short)7, cellStyle.getTopBorderColor());
		assertEquals("abcd", new String(cellStyle.getBorderColor(BorderSide.TOP).getRgb()));
	}

	public void testGetSetLeftBorderColor() {
		CTColor ctColor = ctBorderA.addNewLeft().addNewColor();
		ctColor.setIndexed(2);
		assertEquals((short)2, cellStyle.getLeftBorderColor());
		CTColor anotherCtColor = CTColor.Factory.newInstance();
		anotherCtColor.setIndexed(4);
		anotherCtColor.setTheme(3);
		anotherCtColor.setRgb("1234".getBytes());
		XSSFColor anotherColor = new XSSFColor(anotherCtColor);
		cellStyle.setBorderColor(BorderSide.LEFT, anotherColor);
		assertEquals((short)4, cellStyle.getLeftBorderColor());
		assertEquals("1234", new String(cellStyle.getBorderColor(BorderSide.LEFT).getRgb()));
	}

	public void testGetSetRightBorderColor() {
		CTColor ctColor = ctBorderA.addNewRight().addNewColor();
		ctColor.setIndexed(8);
		assertEquals((short)8, cellStyle.getRightBorderColor());
		CTColor anotherCtColor = CTColor.Factory.newInstance();
		anotherCtColor.setIndexed(14);
		anotherCtColor.setTheme(3);
		anotherCtColor.setRgb("af67".getBytes());
		XSSFColor anotherColor = new XSSFColor(anotherCtColor);
		cellStyle.setBorderColor(BorderSide.RIGHT, anotherColor);
		assertEquals((short)14, cellStyle.getRightBorderColor());
		assertEquals("af67", new String(cellStyle.getBorderColor(BorderSide.RIGHT).getRgb()));
	}

	public void testGetFillBackgroundColor() {

		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		CTColor ctBgColor = ctPatternFill.addNewBgColor();
		ctBgColor.setIndexed(IndexedColors.BRIGHT_GREEN.getIndex());
		ctPatternFill.setBgColor(ctBgColor);

		XSSFCellFill cellFill=new XSSFCellFill(ctFill);
		long index=stylesTable.putFill(cellFill);
		cellStyle.getCoreXf().setFillId(index);

		assertEquals(2,cellStyle.getCoreXf().getFillId());
		assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), cellStyle.getFillBackgroundColor());
		
		cellStyle.setFillBackgroundColor(IndexedColors.BLUE.getIndex());
		assertEquals(IndexedColors.BLUE.getIndex(), ctFill.getPatternFill().getBgColor().getIndexed());
		
		//test rgb color - XSSFColor
		CTColor ctColor=CTColor.Factory.newInstance();
		ctColor.setRgb("FFFFFF".getBytes());
		ctPatternFill.setBgColor(ctColor);
		assertEquals(ctColor.toString(), cellStyle.getFillBackgroundRgbColor().getCTColor().toString());
		
		cellStyle.setFillBackgroundRgbColor(new XSSFColor(ctColor));
		assertEquals(ctColor.getRgb()[0], ctPatternFill.getBgColor().getRgb()[0]);
		assertEquals(ctColor.getRgb()[1], ctPatternFill.getBgColor().getRgb()[1]);
		assertEquals(ctColor.getRgb()[2], ctPatternFill.getBgColor().getRgb()[2]);
		assertEquals(ctColor.getRgb()[3], ctPatternFill.getBgColor().getRgb()[3]);
	}
	
	public void testGetFillBackgroundColor_default() {

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFCellStyle style = wb.createCellStyle();

		short color;
		try {
			color = style.getFillBackgroundColor();
		} catch (NullPointerException e) {
			throw new AssertionFailedError("Identified bug 45898");
		}
		assertEquals(AUTO_COLOR_INDEX, color);
		XSSFColor xcolor=style.getFillBackgroundRgbColor();
		assertEquals(xcolor.getIndexed(), AUTO_COLOR_INDEX);
	}
	

	public void testGetFillForegroundColor() {

		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		CTColor ctFgColor = ctPatternFill.addNewFgColor();
		ctFgColor.setIndexed(IndexedColors.BRIGHT_GREEN.getIndex());
		ctPatternFill.setFgColor(ctFgColor);

		XSSFCellFill cellFill=new XSSFCellFill(ctFill);
		long index=stylesTable.putFill(cellFill);
		cellStyle.getCoreXf().setFillId(index);

		assertEquals(2,cellStyle.getCoreXf().getFillId());
		assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), cellStyle.getFillForegroundColor());
		
		cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		assertEquals(IndexedColors.BLUE.getIndex(), ctFill.getPatternFill().getFgColor().getIndexed());
		
		//test rgb color - XSSFColor
		CTColor ctColor=CTColor.Factory.newInstance();
		ctColor.setRgb("FFFFFF".getBytes());
		ctPatternFill.setFgColor(ctColor);
		assertEquals(ctColor.toString(), cellStyle.getFillForegroundRgbColor().getCTColor().toString());
		
		cellStyle.setFillForegroundRgbColor(new XSSFColor(ctColor));
		assertEquals(ctColor.getRgb()[0], ctPatternFill.getFgColor().getRgb()[0]);
		assertEquals(ctColor.getRgb()[1], ctPatternFill.getFgColor().getRgb()[1]);
		assertEquals(ctColor.getRgb()[2], ctPatternFill.getFgColor().getRgb()[2]);
		assertEquals(ctColor.getRgb()[3], ctPatternFill.getFgColor().getRgb()[3]);
	}
	
	public void testGetFillForegroundColor_default() {

		XSSFWorkbook wb = new XSSFWorkbook();

		XSSFCellStyle style = wb.createCellStyle();

		short color;
		try {
			color = style.getFillForegroundColor();
		} catch (NullPointerException e) {
			throw new AssertionFailedError("Identified bug 45898");
		}
		assertEquals(AUTO_COLOR_INDEX, color);
		XSSFColor xcolor=style.getFillForegroundRgbColor();
		assertEquals(xcolor.getIndexed(), AUTO_COLOR_INDEX);
	}
	

	public void testGetFillPattern() {

		CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
		ctPatternFill.setPatternType(STPatternType.DARK_DOWN);
		XSSFCellFill cellFill=new XSSFCellFill(ctFill);
		long index=stylesTable.putFill(cellFill);
		cellStyle.getCoreXf().setFillId(index);
	
		assertEquals(CellStyle.THICK_FORWARD_DIAG, cellStyle.getFillPattern());
		
		cellStyle.setFillPattern(CellStyle.BRICKS);
		assertEquals(STPatternType.INT_DARK_TRELLIS,ctPatternFill.getPatternType().intValue());
	}

	public void testGetFont() {
		assertNotNull(cellStyle.getFont());
	}

	public void testGetSetHidden() {
		assertFalse(cellStyle.getHidden());
		cellXf.getProtection().setHidden(true);
		assertTrue(cellStyle.getHidden());
		cellStyle.setHidden(false);
		assertFalse(cellStyle.getHidden());
	}

	public void testGetSetLocked() {
		assertFalse(cellStyle.getLocked());
		cellXf.getProtection().setLocked(true);
		assertTrue(cellStyle.getLocked());
		cellStyle.setLocked(false);
		assertFalse(cellStyle.getLocked());
	}

	public void testGetSetIndent() {
		assertEquals((short)0, cellStyle.getIndention());
		cellXf.getAlignment().setIndent(3);
		assertEquals((short)3, cellStyle.getIndention());
		cellStyle.setIndention((short) 13);
		assertEquals((short)13, cellXf.getAlignment().getIndent());
	}

	public void testGetSetAlignement() {
		assertNull(cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());
		assertEquals(HorizontalAlignment.GENERAL, cellStyle.getAlignmentEnum());

		cellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		assertEquals(XSSFCellStyle.ALIGN_LEFT, cellStyle.getAlignment());
		assertEquals(HorizontalAlignment.LEFT, cellStyle.getAlignmentEnum());
		assertEquals(STHorizontalAlignment.LEFT, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());

		cellStyle.setAlignment(HorizontalAlignment.JUSTIFY);
		assertEquals(XSSFCellStyle.ALIGN_JUSTIFY, cellStyle.getAlignment());
		assertEquals(HorizontalAlignment.JUSTIFY, cellStyle.getAlignmentEnum());
		assertEquals(STHorizontalAlignment.JUSTIFY, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());

		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		assertEquals(XSSFCellStyle.ALIGN_CENTER, cellStyle.getAlignment());
		assertEquals(HorizontalAlignment.CENTER, cellStyle.getAlignmentEnum());
		assertEquals(STHorizontalAlignment.CENTER, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());
	}

	public void testGetSetVerticalAlignment() {
		assertEquals(VerticalAlignment.BOTTOM, cellStyle.getVerticalAlignmentEnum());
		assertEquals(XSSFCellStyle.VERTICAL_BOTTOM, cellStyle.getVerticalAlignment());
		assertNull(cellStyle.getCellAlignment().getCTCellAlignment().getVertical());

		cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		assertEquals(XSSFCellStyle.VERTICAL_CENTER, cellStyle.getVerticalAlignment());
		assertEquals(VerticalAlignment.CENTER, cellStyle.getVerticalAlignmentEnum());
		assertEquals(STVerticalAlignment.CENTER, cellStyle.getCellAlignment().getCTCellAlignment().getVertical());

		cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_JUSTIFY);
		assertEquals(XSSFCellStyle.VERTICAL_JUSTIFY, cellStyle.getVerticalAlignment());
		assertEquals(VerticalAlignment.JUSTIFY, cellStyle.getVerticalAlignmentEnum());
		assertEquals(STVerticalAlignment.JUSTIFY, cellStyle.getCellAlignment().getCTCellAlignment().getVertical());
	}

	public void testGetSetWrapText() {
		assertFalse(cellStyle.getWrapText());
		cellXf.getAlignment().setWrapText(true);
		assertTrue(cellStyle.getWrapText());
		cellStyle.setWrapText(false);
		assertFalse(cellXf.getAlignment().getWrapText());
	}

	/**
	 * Cloning one XSSFCellStyle onto Another, same XSSFWorkbook
	 */
	public void testCloneStyleSameWB() throws Exception {
		// TODO
	}
	/**
	 * Cloning one XSSFCellStyle onto Another, different XSSFWorkbooks
	 */
	public void testCloneStyleDiffWB() throws Exception {
		// TODO
	}
}
