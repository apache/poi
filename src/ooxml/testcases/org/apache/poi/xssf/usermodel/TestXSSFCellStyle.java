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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;


public class TestXSSFCellStyle extends TestCase {

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

		ctStylesheet = stylesTable.getCTStylesheet();

		ctBorderA = CTBorder.Factory.newInstance();
		XSSFCellBorder borderA = new XSSFCellBorder(ctBorderA);
		long borderId = stylesTable.putBorder(borderA);
		assertEquals(1, borderId);

		XSSFCellBorder borderB = new XSSFCellBorder();
		assertEquals(1, stylesTable.putBorder(borderB));

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
        //default values
        assertEquals(CellStyle.BORDER_NONE, cellStyle.getBorderBottom());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
        assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderBottom());
        //a new border has been added
        assertEquals(num + 1, stylesTable.getBorders().size());
        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(STBorderStyle.MEDIUM, ctBorder.getBottom().getStyle());

        num = stylesTable.getBorders().size();
        //setting the same border multiple times should not change borderId
        for (int i = 0; i < 3; i++) {
            cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderBottom());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <bottom> element
        cellStyle.setBorderBottom(CellStyle.BORDER_NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertFalse(ctBorder.isSetBottom());
    }

    public void testGetSetBorderRight() {
        //default values
        assertEquals(CellStyle.BORDER_NONE, cellStyle.getBorderRight());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
        assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderRight());
        //a new border has been added
        assertEquals(num + 1, stylesTable.getBorders().size());
        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(STBorderStyle.MEDIUM, ctBorder.getRight().getStyle());

        num = stylesTable.getBorders().size();
        //setting the same border multiple times should not change borderId
        for (int i = 0; i < 3; i++) {
            cellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
            assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderRight());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <right> element
        cellStyle.setBorderRight(CellStyle.BORDER_NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertFalse(ctBorder.isSetRight());
    }

	public void testGetSetBorderLeft() {
        //default values
        assertEquals(CellStyle.BORDER_NONE, cellStyle.getBorderLeft());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
        assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderLeft());
        //a new border has been added
        assertEquals(num + 1, stylesTable.getBorders().size());
        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(STBorderStyle.MEDIUM, ctBorder.getLeft().getStyle());

        num = stylesTable.getBorders().size();
        //setting the same border multiple times should not change borderId
        for (int i = 0; i < 3; i++) {
            cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderLeft());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <left> element
        cellStyle.setBorderLeft(CellStyle.BORDER_NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertFalse(ctBorder.isSetLeft());
	}

	public void testGetSetBorderTop() {
        //default values
        assertEquals(CellStyle.BORDER_NONE, cellStyle.getBorderTop());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderTop());
        //a new border has been added
        assertEquals(num + 1, stylesTable.getBorders().size());
        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(STBorderStyle.MEDIUM, ctBorder.getTop().getStyle());

        num = stylesTable.getBorders().size();
        //setting the same border multiple times should not change borderId
        for (int i = 0; i < 3; i++) {
            cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            assertEquals(CellStyle.BORDER_MEDIUM, cellStyle.getBorderTop());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <top> element
        cellStyle.setBorderTop(CellStyle.BORDER_NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertFalse(ctBorder.isSetTop());
	}

	public void testGetSetBottomBorderColor() {
        //defaults
        assertEquals(IndexedColors.BLACK.getIndex(), cellStyle.getBottomBorderColor());
        assertNull(cellStyle.getBottomBorderXSSFColor());

        int num = stylesTable.getBorders().size();

        XSSFColor clr;

        //setting indexed color
        cellStyle.setBottomBorderColor(IndexedColors.BLUE_GREY.getIndex());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), cellStyle.getBottomBorderColor());
        clr = cellStyle.getBottomBorderXSSFColor();
        assertTrue(clr.getCTColor().isSetIndexed());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), clr.getIndexed());
        //a new border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), ctBorder.getBottom().getColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getBorders().size();
        clr = new XSSFColor(java.awt.Color.CYAN);
        cellStyle.setBottomBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getBottomBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getBottomBorderXSSFColor().getRgb();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setBottomBorderColor(null);
        assertNull(cellStyle.getBottomBorderXSSFColor());
    }

	public void testGetSetTopBorderColor() {
        //defaults
        assertEquals(IndexedColors.BLACK.getIndex(), cellStyle.getTopBorderColor());
        assertNull(cellStyle.getTopBorderXSSFColor());

        int num = stylesTable.getBorders().size();

        XSSFColor clr;

        //setting indexed color
        cellStyle.setTopBorderColor(IndexedColors.BLUE_GREY.getIndex());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), cellStyle.getTopBorderColor());
        clr = cellStyle.getTopBorderXSSFColor();
        assertTrue(clr.getCTColor().isSetIndexed());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), clr.getIndexed());
        //a new border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), ctBorder.getTop().getColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getBorders().size();
        clr = new XSSFColor(java.awt.Color.CYAN);
        cellStyle.setTopBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getTopBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getTopBorderXSSFColor().getRgb();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setTopBorderColor(null);
        assertNull(cellStyle.getTopBorderXSSFColor());
	}

	public void testGetSetLeftBorderColor() {
        //defaults
        assertEquals(IndexedColors.BLACK.getIndex(), cellStyle.getLeftBorderColor());
        assertNull(cellStyle.getLeftBorderXSSFColor());

        int num = stylesTable.getBorders().size();

        XSSFColor clr;

        //setting indexed color
        cellStyle.setLeftBorderColor(IndexedColors.BLUE_GREY.getIndex());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), cellStyle.getLeftBorderColor());
        clr = cellStyle.getLeftBorderXSSFColor();
        assertTrue(clr.getCTColor().isSetIndexed());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), clr.getIndexed());
        //a new border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), ctBorder.getLeft().getColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getBorders().size();
        clr = new XSSFColor(java.awt.Color.CYAN);
        cellStyle.setLeftBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getLeftBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getLeftBorderXSSFColor().getRgb();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setLeftBorderColor(null);
        assertNull(cellStyle.getLeftBorderXSSFColor());
	}

	public void testGetSetRightBorderColor() {
        //defaults
        assertEquals(IndexedColors.BLACK.getIndex(), cellStyle.getRightBorderColor());
        assertNull(cellStyle.getRightBorderXSSFColor());

        int num = stylesTable.getBorders().size();

        XSSFColor clr;

        //setting indexed color
        cellStyle.setRightBorderColor(IndexedColors.BLUE_GREY.getIndex());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), cellStyle.getRightBorderColor());
        clr = cellStyle.getRightBorderXSSFColor();
        assertTrue(clr.getCTColor().isSetIndexed());
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), clr.getIndexed());
        //a new border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //id of the created border
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(IndexedColors.BLUE_GREY.getIndex(), ctBorder.getRight().getColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getBorders().size();
        clr = new XSSFColor(java.awt.Color.CYAN);
        cellStyle.setRightBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getRightBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getRightBorderXSSFColor().getRgb();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setRightBorderColor(null);
        assertNull(cellStyle.getRightBorderXSSFColor());
	}

	public void testGetSetFillBackgroundColor() {

        assertEquals(IndexedColors.AUTOMATIC.getIndex(), cellStyle.getFillBackgroundColor());
        assertNull(cellStyle.getFillBackgroundXSSFColor());

        XSSFColor clr;

        int num = stylesTable.getFills().size();

        //setting indexed color
        cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
        assertEquals(IndexedColors.RED.getIndex(), cellStyle.getFillBackgroundColor());
        clr = cellStyle.getFillBackgroundXSSFColor();
        assertTrue(clr.getCTColor().isSetIndexed());
        assertEquals(IndexedColors.RED.getIndex(), clr.getIndexed());
        //a new fill was added to the styles table
        assertEquals(num + 1, stylesTable.getFills().size());

        //id of the created border
        int fillId = (int)cellStyle.getCoreXf().getFillId();
        assertTrue(fillId > 0);
        //check changes in the underlying xml bean
        CTFill ctFill = stylesTable.getFillAt(fillId).getCTFill();
        assertEquals(IndexedColors.RED.getIndex(), ctFill.getPatternFill().getBgColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getFills().size();
        clr = new XSSFColor(java.awt.Color.CYAN);
        cellStyle.setFillBackgroundColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getFillBackgroundXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getFillBackgroundXSSFColor().getRgb();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getFills().size());

        //passing null unsets the color
        cellStyle.setFillBackgroundColor(null);
        assertNull(cellStyle.getFillBackgroundXSSFColor());
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), cellStyle.getFillBackgroundColor());
	}

	public void testDefaultStyles() {

		XSSFWorkbook wb1 = new XSSFWorkbook();

		XSSFCellStyle style1 = wb1.createCellStyle();
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), style1.getFillBackgroundColor());
        assertNull(style1.getFillBackgroundXSSFColor());

        //compatibility with HSSF
        HSSFWorkbook wb2 = new HSSFWorkbook();
        HSSFCellStyle style2 = wb2.createCellStyle();
        assertEquals(style2.getFillBackgroundColor(), style1.getFillBackgroundColor());
        assertEquals(style2.getFillForegroundColor(), style1.getFillForegroundColor());
        assertEquals(style2.getFillPattern(), style1.getFillPattern());

        assertEquals(style2.getLeftBorderColor(), style1.getLeftBorderColor());
        assertEquals(style2.getTopBorderColor(), style1.getTopBorderColor());
        assertEquals(style2.getRightBorderColor(), style1.getRightBorderColor());
        assertEquals(style2.getBottomBorderColor(), style1.getBottomBorderColor());

        assertEquals(style2.getBorderBottom(), style1.getBorderBottom());
        assertEquals(style2.getBorderLeft(), style1.getBorderLeft());
        assertEquals(style2.getBorderRight(), style1.getBorderRight());
        assertEquals(style2.getBorderTop(), style1.getBorderTop());
	}


	public void testGetFillForegroundColor() {

        XSSFWorkbook wb = new XSSFWorkbook();
        StylesTable styles = wb.getStylesSource();
        assertEquals(1, wb.getNumCellStyles());
        assertEquals(2, styles.getFills().size());

        XSSFCellStyle defaultStyle = wb.getCellStyleAt((short)0);
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), defaultStyle.getFillForegroundColor());
        assertEquals(null, defaultStyle.getFillForegroundXSSFColor());
        assertEquals(CellStyle.NO_FILL, defaultStyle.getFillPattern());

        XSSFCellStyle customStyle = wb.createCellStyle();

        customStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        assertEquals(CellStyle.SOLID_FOREGROUND, customStyle.getFillPattern());
        assertEquals(3, styles.getFills().size());

        customStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), customStyle.getFillForegroundColor());
        assertEquals(4, styles.getFills().size());

        for (int i = 0; i < 3; i++) {
            XSSFCellStyle style = wb.createCellStyle();

            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            assertEquals(CellStyle.SOLID_FOREGROUND, style.getFillPattern());
            assertEquals(4, styles.getFills().size());

            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), style.getFillForegroundColor());
            assertEquals(4, styles.getFills().size());
        }
	}

	public void testGetFillPattern() {

        assertEquals(CellStyle.NO_FILL, cellStyle.getFillPattern());

        int num = stylesTable.getFills().size();
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        assertEquals(CellStyle.SOLID_FOREGROUND, cellStyle.getFillPattern());
        assertEquals(num + 1, stylesTable.getFills().size());
        int fillId = (int)cellStyle.getCoreXf().getFillId();
        assertTrue(fillId > 0);
        //check changes in the underlying xml bean
        CTFill ctFill = stylesTable.getFillAt(fillId).getCTFill();
        assertEquals(STPatternType.SOLID, ctFill.getPatternFill().getPatternType());

        //setting the same fill multiple time does not update the styles table
        for (int i = 0; i < 3; i++) {
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        }
        assertEquals(num + 1, stylesTable.getFills().size());

        cellStyle.setFillPattern(CellStyle.NO_FILL);
        assertEquals(CellStyle.NO_FILL, cellStyle.getFillPattern());
        fillId = (int)cellStyle.getCoreXf().getFillId();
        ctFill = stylesTable.getFillAt(fillId).getCTFill();
        assertFalse(ctFill.getPatternFill().isSetPatternType());

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
		cellStyle.setIndention((short)3);
		assertEquals((short)3, cellStyle.getIndention());
		cellStyle.setIndention((short) 13);
		assertEquals((short)13, cellStyle.getIndention());
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
		cellStyle.setWrapText(true);
		assertTrue(cellStyle.getWrapText());
		cellStyle.setWrapText(false);
        assertFalse(cellStyle.getWrapText());
	}

	/**
	 * Cloning one XSSFCellStyle onto Another, same XSSFWorkbook
	 */
	public void testCloneStyleSameWB() {
		// TODO
	}
	/**
	 * Cloning one XSSFCellStyle onto Another, different XSSFWorkbooks
	 */
	public void testCloneStyleDiffWB() {
		// TODO
	}
}
