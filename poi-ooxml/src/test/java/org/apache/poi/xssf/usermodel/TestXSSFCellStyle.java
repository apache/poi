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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.ReadingOrder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;

class TestXSSFCellStyle {
	private StylesTable stylesTable;
    private XSSFCellStyle cellStyle;

    @BeforeEach
	void setUp() {
		stylesTable = new StylesTable();

        CTStylesheet ctStylesheet = stylesTable.getCTStylesheet();

        CTBorder ctBorderA = CTBorder.Factory.newInstance();
		XSSFCellBorder borderA = new XSSFCellBorder(ctBorderA);
		long borderId = stylesTable.putBorder(borderA);
		assertEquals(0, borderId);

		XSSFCellBorder borderB = new XSSFCellBorder();
		assertEquals(0, stylesTable.putBorder(borderB));

        CTFill ctFill = CTFill.Factory.newInstance();
		XSSFCellFill fill = new XSSFCellFill(ctFill, null);
		long fillId = stylesTable.putFill(fill);
		assertEquals(2, fillId);

        CTFont ctFont = CTFont.Factory.newInstance();
		XSSFFont font = new XSSFFont(ctFont);
		long fontId = stylesTable.putFont(font);
		assertEquals(1, fontId);

        CTXf cellStyleXf = ctStylesheet.addNewCellStyleXfs().addNewXf();
		cellStyleXf.setBorderId(1);
		cellStyleXf.setFillId(1);
		cellStyleXf.setFontId(1);

        CTCellXfs cellXfs = ctStylesheet.addNewCellXfs();
        CTXf cellXf = cellXfs.addNewXf();
		cellXf.setXfId(1);
		cellXf.setBorderId(1);
		cellXf.setFillId(1);
		cellXf.setFontId(1);
		assertEquals(2, stylesTable.putCellStyleXf(cellStyleXf));
		assertEquals(2, stylesTable.putCellXf(cellXf));
		cellStyle = new XSSFCellStyle(1, 1, stylesTable, null);

		assertNotNull(stylesTable.getFillAt(1).getCTFill().getPatternFill());
		assertEquals(STPatternType.INT_DARK_GRAY, stylesTable.getFillAt(1).getCTFill().getPatternFill().getPatternType().intValue());
	}

	@Test
	void testGetSetBorderBottom() {
        //default values
        assertEquals(BorderStyle.NONE, cellStyle.getBorderBottom());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderBottom());
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
            cellStyle.setBorderBottom(BorderStyle.MEDIUM);
            assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderBottom());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <bottom> element
        cellStyle.setBorderBottom(BorderStyle.NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        //none is not the same as "not set", therefore the following doesn't work any more
        //assertFalse(ctBorder.isSetBottom());
        //replacement:
        assertEquals(ctBorder.getBottom().getStyle(), STBorderStyle.NONE);
    }

	@Test
    void testGetSetBorderRight() {
        //default values
        assertEquals(BorderStyle.NONE, cellStyle.getBorderRight());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderRight());
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
            cellStyle.setBorderRight(BorderStyle.MEDIUM);
            assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderRight());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <right> element
        cellStyle.setBorderRight(BorderStyle.NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        //none is not the same as "not set", therefore the following doesn't work any more
        //assertFalse(ctBorder.isSetRight());
        //replacement:
        assertEquals(ctBorder.getRight().getStyle(), STBorderStyle.NONE);
    }

	@Test
    void testGetSetBorderLeft() {
        //default values
        assertEquals(BorderStyle.NONE, cellStyle.getBorderLeft());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderLeft());
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
            cellStyle.setBorderLeft(BorderStyle.MEDIUM);
            assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderLeft());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <left> element
        cellStyle.setBorderLeft(BorderStyle.NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        //none is not the same as "not set", therefore the following doesn't work any more
        //assertFalse(ctBorder.isSetLeft());
        //replacement:
        assertEquals(ctBorder.getLeft().getStyle(), STBorderStyle.NONE);
	}

	@Test
    void testGetSetBorderTop() {
        //default values
        assertEquals(BorderStyle.NONE, cellStyle.getBorderTop());

        int num = stylesTable.getBorders().size();
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderTop());
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
            cellStyle.setBorderTop(BorderStyle.MEDIUM);
            assertEquals(BorderStyle.MEDIUM, cellStyle.getBorderTop());
        }
        assertEquals(borderId, cellStyle.getCoreXf().getBorderId());
        assertEquals(num, stylesTable.getBorders().size());
        assertSame(ctBorder, stylesTable.getBorderAt(borderId).getCTBorder());

        //setting border to none removes the <top> element
        cellStyle.setBorderTop(BorderStyle.NONE);
        assertEquals(num, stylesTable.getBorders().size());
        borderId = (int)cellStyle.getCoreXf().getBorderId();
        ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        //none is not the same as "not set", therefore the following doesn't work any more
        //assertFalse(ctBorder.isSetTop());
        //replacement:
        assertEquals(ctBorder.getTop().getStyle(), STBorderStyle.NONE);
    }

    private void testGetSetBorderXMLBean(BorderStyle border, STBorderStyle.Enum expected) {
        cellStyle.setBorderTop(border);
        assertEquals(border, cellStyle.getBorderTop());
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        assertTrue(borderId > 0);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertEquals(expected, ctBorder.getTop().getStyle());
    }


    // Border Styles, in BorderStyle/STBorderStyle enum order
    @Test
    void testGetSetBorderNone() {
        cellStyle.setBorderTop(BorderStyle.NONE);
        assertEquals(BorderStyle.NONE, cellStyle.getBorderTop());
        int borderId = (int)cellStyle.getCoreXf().getBorderId();
        // The default Style is already "none"
        // Therefore the new style already exists as Id=0
        //assertTrue(borderId > 0);
        // replacement:
        assertEquals(0, borderId);
        //check changes in the underlying xml bean
        CTBorder ctBorder = stylesTable.getBorderAt(borderId).getCTBorder();
        assertNotNull(ctBorder.getTop());
        // no border style and STBorderStyle.NONE are equivalent
        // POI prefers to unset the border style than explicitly set it STBorderStyle.NONE
    }

    @Test
    void testGetSetBorderThin() {
        testGetSetBorderXMLBean(BorderStyle.THIN, STBorderStyle.THIN);
    }

    @Test
    void testGetSetBorderMedium() {
        testGetSetBorderXMLBean(BorderStyle.MEDIUM, STBorderStyle.MEDIUM);
    }

    @Test
    void testGetSetBorderDashed() {
        testGetSetBorderXMLBean(BorderStyle.DASHED, STBorderStyle.DASHED);
    }

    @Test
    void testGetSetBorderDotted() {
        testGetSetBorderXMLBean(BorderStyle.DOTTED, STBorderStyle.DOTTED);
    }

    @Test
    void testGetSetBorderThick() {
        testGetSetBorderXMLBean(BorderStyle.THICK, STBorderStyle.THICK);
    }

    @Test
    void testGetSetBorderDouble() {
        testGetSetBorderXMLBean(BorderStyle.DOUBLE, STBorderStyle.DOUBLE);
    }

    @Test
    void testGetSetBorderHair() {
        testGetSetBorderXMLBean(BorderStyle.HAIR, STBorderStyle.HAIR);
    }

    @Test
    void testGetSetBorderMediumDashed() {
        testGetSetBorderXMLBean(BorderStyle.MEDIUM_DASHED, STBorderStyle.MEDIUM_DASHED);
    }

    @Test
    void testGetSetBorderDashDot() {
        testGetSetBorderXMLBean(BorderStyle.DASH_DOT, STBorderStyle.DASH_DOT);
    }

    @Test
    void testGetSetBorderMediumDashDot() {
        testGetSetBorderXMLBean(BorderStyle.MEDIUM_DASH_DOT, STBorderStyle.MEDIUM_DASH_DOT);
    }

    @Test
    void testGetSetBorderDashDotDot() {
        testGetSetBorderXMLBean(BorderStyle.DASH_DOT_DOT, STBorderStyle.DASH_DOT_DOT);
    }

    @Test
    void testGetSetBorderMediumDashDotDot() {
        testGetSetBorderXMLBean(BorderStyle.MEDIUM_DASH_DOT_DOT, STBorderStyle.MEDIUM_DASH_DOT_DOT);
    }

    @Test
    void testGetSetBorderSlantDashDot() {
        testGetSetBorderXMLBean(BorderStyle.SLANTED_DASH_DOT, STBorderStyle.SLANT_DASH_DOT);
    }

    @Test
    void testGetSetBottomBorderColor() {
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
        clr = new XSSFColor(java.awt.Color.CYAN, stylesTable.getIndexedColors());
        cellStyle.setBottomBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getBottomBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getBottomBorderXSSFColor().getRGB();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setBottomBorderColor(null);
        assertNull(cellStyle.getBottomBorderXSSFColor());
    }

	@Test
    void testGetSetTopBorderColor() {
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
        clr = new XSSFColor(java.awt.Color.CYAN, stylesTable.getIndexedColors());
        cellStyle.setTopBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getTopBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getTopBorderXSSFColor().getRGB();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setTopBorderColor(null);
        assertNull(cellStyle.getTopBorderXSSFColor());
	}

	@Test
    void testGetSetLeftBorderColor() {
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
        clr = new XSSFColor(java.awt.Color.CYAN, stylesTable.getIndexedColors());
        cellStyle.setLeftBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getLeftBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getLeftBorderXSSFColor().getRGB();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setLeftBorderColor(null);
        assertNull(cellStyle.getLeftBorderXSSFColor());
	}

	@Test
    void testGetSetRightBorderColor() {
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
        clr = new XSSFColor(java.awt.Color.CYAN, stylesTable.getIndexedColors());
        cellStyle.setRightBorderColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getRightBorderXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getRightBorderXSSFColor().getRGB();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getBorders().size());

        //passing null unsets the color
        cellStyle.setRightBorderColor(null);
        assertNull(cellStyle.getRightBorderXSSFColor());
	}

	@Test
    void testGetSetFillBackgroundColor() {

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
        CTFill ctFill2 = stylesTable.getFillAt(fillId).getCTFill();
        assertEquals(IndexedColors.RED.getIndex(), ctFill2.getPatternFill().getBgColor().getIndexed());

        //setting XSSFColor
        num = stylesTable.getFills().size();
        clr = new XSSFColor(java.awt.Color.CYAN, stylesTable.getIndexedColors());
        cellStyle.setFillBackgroundColor(clr);
        assertEquals(clr.getCTColor().toString(), cellStyle.getFillBackgroundXSSFColor().getCTColor().toString());
        byte[] rgb = cellStyle.getFillBackgroundXSSFColor().getRGB();
        assertEquals(java.awt.Color.CYAN, new java.awt.Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
        //another border was added to the styles table
        assertEquals(num + 1, stylesTable.getFills().size());

        //passing null unsets the color
        cellStyle.setFillBackgroundColor(null);
        assertNull(cellStyle.getFillBackgroundXSSFColor());
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), cellStyle.getFillBackgroundColor());
	}

    @Test
    void testDefaultStyles() throws IOException {

		XSSFWorkbook wb1 = new XSSFWorkbook();

		XSSFCellStyle style1 = wb1.createCellStyle();
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), style1.getFillBackgroundColor());
        assertNull(style1.getFillBackgroundXSSFColor());

        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb1));
        wb1.close();

        //compatibility with HSSF
        HSSFWorkbook wb2 = new HSSFWorkbook();
        HSSFCellStyle style2 = wb2.createCellStyle();
        assertEquals(style2.getFillBackgroundColor(), style1.getFillBackgroundColor());
        assertEquals(style2.getFillForegroundColor(), style1.getFillForegroundColor());
        assertEquals(style2.getFillPattern(), style1.getFillPattern());
        assertEquals(style2.getFillPattern(), style1.getFillPattern());

        assertEquals(style2.getLeftBorderColor(), style1.getLeftBorderColor());
        assertEquals(style2.getTopBorderColor(), style1.getTopBorderColor());
        assertEquals(style2.getRightBorderColor(), style1.getRightBorderColor());
        assertEquals(style2.getBottomBorderColor(), style1.getBottomBorderColor());

        assertEquals(style2.getBorderBottom(), style1.getBorderBottom());
        assertEquals(style2.getBorderLeft(), style1.getBorderLeft());
        assertEquals(style2.getBorderRight(), style1.getBorderRight());
        assertEquals(style2.getBorderTop(), style1.getBorderTop());
        wb2.close();
	}

    @Test
    void testGetFillForegroundColor() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        StylesTable styles = wb.getStylesSource();
        assertEquals(1, wb.getNumCellStyles());
        assertEquals(2, styles.getFills().size());

        XSSFCellStyle defaultStyle = wb.getCellStyleAt((short)0);
        assertEquals(IndexedColors.AUTOMATIC.getIndex(), defaultStyle.getFillForegroundColor());
        assertNull(defaultStyle.getFillForegroundXSSFColor());
        assertEquals(FillPatternType.NO_FILL, defaultStyle.getFillPattern());

        XSSFCellStyle customStyle = wb.createCellStyle();

        customStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        assertEquals(FillPatternType.SOLID_FOREGROUND, customStyle.getFillPattern());
        assertEquals(3, styles.getFills().size());

        customStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), customStyle.getFillForegroundColor());
        assertEquals(4, styles.getFills().size());

        for (int i = 0; i < 3; i++) {
            XSSFCellStyle style = wb.createCellStyle();

            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            assertEquals(FillPatternType.SOLID_FOREGROUND, style.getFillPattern());
            assertEquals(4, styles.getFills().size());

            style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            assertEquals(IndexedColors.BRIGHT_GREEN.getIndex(), style.getFillForegroundColor());
            assertEquals(4, styles.getFills().size());
        }

        assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
        wb.close();
    }

    @Test
    void testGetFillPattern() {

        assertEquals(STPatternType.INT_DARK_GRAY-1, cellStyle.getFillPattern().getCode());

        int num = stylesTable.getFills().size();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        assertEquals(FillPatternType.SOLID_FOREGROUND, cellStyle.getFillPattern());
        assertEquals(num + 1, stylesTable.getFills().size());
        int fillId = (int)cellStyle.getCoreXf().getFillId();
        assertTrue(fillId > 0);
        //check changes in the underlying xml bean
        CTFill ctFill2 = stylesTable.getFillAt(fillId).getCTFill();
        assertEquals(STPatternType.SOLID, ctFill2.getPatternFill().getPatternType());

        //setting the same fill multiple time does not update the styles table
        for (int i = 0; i < 3; i++) {
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        assertEquals(num + 1, stylesTable.getFills().size());

        cellStyle.setFillPattern(FillPatternType.NO_FILL);
        assertEquals(FillPatternType.NO_FILL, cellStyle.getFillPattern());
        fillId = (int)cellStyle.getCoreXf().getFillId();
        ctFill2 = stylesTable.getFillAt(fillId).getCTFill();
        assertNull(ctFill2.getPatternFill());
	}

	@Test
    void testGetFont() {
		assertNotNull(cellStyle.getFont());
	}

	@Test
    void testGetSetHidden() {
		assertFalse(cellStyle.getHidden());
		cellStyle.setHidden(true);
		assertTrue(cellStyle.getHidden());
		cellStyle.setHidden(false);
		assertFalse(cellStyle.getHidden());
	}

	@Test
    void testGetSetLocked() {
		assertTrue(cellStyle.getLocked());
		cellStyle.setLocked(true);
		assertTrue(cellStyle.getLocked());
		cellStyle.setLocked(false);
		assertFalse(cellStyle.getLocked());
	}

	@Test
    void testGetSetIndent() {
		assertEquals((short)0, cellStyle.getIndention());
		cellStyle.setIndention((short)3);
		assertEquals((short)3, cellStyle.getIndention());
		cellStyle.setIndention((short) 13);
		assertEquals((short)13, cellStyle.getIndention());
	}

    @Test
    void testGetSetAlignment() {
		assertNull(cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());
		assertEquals(HorizontalAlignment.GENERAL, cellStyle.getAlignment());

		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		assertEquals(HorizontalAlignment.LEFT, cellStyle.getAlignment());
		assertEquals(STHorizontalAlignment.LEFT, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());

		cellStyle.setAlignment(HorizontalAlignment.JUSTIFY);
		assertEquals(HorizontalAlignment.JUSTIFY, cellStyle.getAlignment());
		assertEquals(STHorizontalAlignment.JUSTIFY, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());

		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		assertEquals(HorizontalAlignment.CENTER, cellStyle.getAlignment());
		assertEquals(STHorizontalAlignment.CENTER, cellStyle.getCellAlignment().getCTCellAlignment().getHorizontal());
	}

	@Test
    void testGetSetReadingOrder() {
	    assertEquals(ReadingOrder.CONTEXT, cellStyle.getReadingOrder());
	    assertEquals(ReadingOrder.CONTEXT.getCode(), cellStyle.getCellAlignment().getCTCellAlignment().getReadingOrder());

        cellStyle.setReadingOrder(ReadingOrder.LEFT_TO_RIGHT);
        assertEquals(ReadingOrder.LEFT_TO_RIGHT, cellStyle.getReadingOrder());
        assertEquals(ReadingOrder.LEFT_TO_RIGHT.getCode(), cellStyle.getCellAlignment().getCTCellAlignment().getReadingOrder());

        cellStyle.setReadingOrder(ReadingOrder.RIGHT_TO_LEFT);
        assertEquals(ReadingOrder.RIGHT_TO_LEFT, cellStyle.getReadingOrder());
        assertEquals(ReadingOrder.RIGHT_TO_LEFT.getCode(), cellStyle.getCellAlignment().getCTCellAlignment().getReadingOrder());

        cellStyle.setReadingOrder(ReadingOrder.CONTEXT);
        assertEquals(ReadingOrder.CONTEXT, cellStyle.getReadingOrder());
        assertEquals(ReadingOrder.CONTEXT.getCode(), cellStyle.getCellAlignment().getCTCellAlignment().getReadingOrder());
    }

    @Test
    void testGetSetVerticalAlignment() {
		assertEquals(VerticalAlignment.BOTTOM, cellStyle.getVerticalAlignment());
		assertFalse(cellStyle.getCellAlignment().getCTCellAlignment().isSetVertical());
		assertEquals(STVerticalAlignment.BOTTOM, cellStyle.getCellAlignment().getCTCellAlignment().getVertical());

		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		assertEquals(VerticalAlignment.CENTER, cellStyle.getVerticalAlignment());
		assertEquals(STVerticalAlignment.CENTER, cellStyle.getCellAlignment().getCTCellAlignment().getVertical());

		cellStyle.setVerticalAlignment(VerticalAlignment.JUSTIFY);
		assertEquals(VerticalAlignment.JUSTIFY, cellStyle.getVerticalAlignment());
		assertEquals(STVerticalAlignment.JUSTIFY, cellStyle.getCellAlignment().getCTCellAlignment().getVertical());
	}

	@Test
    void testGetSetWrapText() {
		assertFalse(cellStyle.getWrapText());
		cellStyle.setWrapText(true);
		assertTrue(cellStyle.getWrapText());
		cellStyle.setWrapText(false);
        assertFalse(cellStyle.getWrapText());
	}

	/**
	 * Cloning one XSSFCellStyle onto Another, same XSSFWorkbook
	 */
	@Test
    void testCloneStyleSameWB() throws IOException {
      XSSFWorkbook wb = new XSSFWorkbook();
      assertEquals(1, wb.getNumberOfFonts());

      XSSFFont fnt = wb.createFont();
      fnt.setFontName("TestingFont");
      assertEquals(2, wb.getNumberOfFonts());

      XSSFCellStyle orig = wb.createCellStyle();
      orig.setAlignment(HorizontalAlignment.RIGHT);
      orig.setFont(fnt);
      orig.setDataFormat((short)18);

      assertEquals(HorizontalAlignment.RIGHT, orig.getAlignment());
      assertEquals(fnt, orig.getFont());
      assertEquals(18, orig.getDataFormat());

      XSSFCellStyle clone = wb.createCellStyle();
        assertNotSame(HorizontalAlignment.RIGHT, clone.getAlignment());
        assertNotSame(fnt, clone.getFont());
        assertNotEquals(18, clone.getDataFormat());

      clone.cloneStyleFrom(orig);
      assertEquals(HorizontalAlignment.RIGHT, clone.getAlignment());
      assertEquals(fnt, clone.getFont());
      assertEquals(18, clone.getDataFormat());
      assertEquals(2, wb.getNumberOfFonts());

      XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(wb);
      assertNotNull(wb2);
      wb2.close();

      wb.close();
	}

    /**
     * Cloning one XSSFCellStyle onto Another, different XSSFWorkbooks
     */
    @Test
    void testCloneStyleDiffWB() throws IOException {
        XSSFWorkbook wbOrig = new XSSFWorkbook();
        assertEquals(1, wbOrig.getNumberOfFonts());
        assertEquals(0, wbOrig.getStylesSource().getNumberFormats().size());

        XSSFFont fnt = wbOrig.createFont();
        fnt.setFontName("TestingFont");
        assertEquals(2, wbOrig.getNumberOfFonts());
        assertEquals(0, wbOrig.getStylesSource().getNumberFormats().size());

        XSSFDataFormat fmt = wbOrig.createDataFormat();
        fmt.getFormat("MadeUpOne");
        fmt.getFormat("MadeUpTwo");

        XSSFCellStyle orig = wbOrig.createCellStyle();
        orig.setAlignment(HorizontalAlignment.RIGHT);
        orig.setFont(fnt);
        orig.setDataFormat(fmt.getFormat("Test##"));
        orig.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        orig.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());

        XSSFCellStyle origEmpty = wbOrig.createCellStyle();
        assertNotNull(origEmpty);

        assertSame(HorizontalAlignment.RIGHT, orig.getAlignment());
        assertSame(fnt, orig.getFont());
        assertEquals(fmt.getFormat("Test##"), orig.getDataFormat());

        assertEquals(2, wbOrig.getNumberOfFonts());
        assertEquals(3, wbOrig.getStylesSource().getNumberFormats().size());


        // Now a style on another workbook
        XSSFWorkbook wbClone = new XSSFWorkbook();
        assertEquals(1, wbClone.getNumberOfFonts());
        assertEquals(0, wbClone.getStylesSource().getNumberFormats().size());
        assertEquals(1, wbClone.getNumCellStyles());

        XSSFDataFormat fmtClone = wbClone.createDataFormat();
        XSSFCellStyle clone = wbClone.createCellStyle();

        assertEquals(1, wbClone.getNumberOfFonts());
        assertEquals(0, wbClone.getStylesSource().getNumberFormats().size());

        assertNotSame(HorizontalAlignment.RIGHT, clone.getAlignment());
        assertNotEquals("TestingFont", clone.getFont().getFontName());

        clone.cloneStyleFrom(orig);

        assertEquals(2, wbClone.getNumberOfFonts());
        assertEquals(2, wbClone.getNumCellStyles());
        assertEquals(1, wbClone.getStylesSource().getNumberFormats().size());

        assertEquals(HorizontalAlignment.RIGHT, clone.getAlignment());
        assertEquals("TestingFont", clone.getFont().getFontName());
        assertEquals(fmtClone.getFormat("Test##"), clone.getDataFormat());
        assertNotEquals(fmtClone.getFormat("Test##"), fmt.getFormat("Test##"));
        assertEquals(clone.getFillPattern(), FillPatternType.SOLID_FOREGROUND);
        assertEquals(clone.getFillForegroundColor(), IndexedColors.BRIGHT_GREEN.getIndex());

        // Save it and re-check
        XSSFWorkbook wbReload = XSSFTestDataSamples.writeOutAndReadBack(wbClone);
        assertEquals(2, wbReload.getNumberOfFonts());
        assertEquals(2, wbReload.getNumCellStyles());
        assertEquals(1, wbReload.getStylesSource().getNumberFormats().size());

        XSSFCellStyle reload = wbReload.getCellStyleAt((short)1);
        assertEquals(HorizontalAlignment.RIGHT, reload.getAlignment());
        assertEquals("TestingFont", reload.getFont().getFontName());
        assertEquals(fmtClone.getFormat("Test##"), reload.getDataFormat());
        assertNotEquals(fmtClone.getFormat("Test##"), fmt.getFormat("Test##"));
        assertEquals(clone.getFillPattern(), FillPatternType.SOLID_FOREGROUND);
        assertEquals(clone.getFillForegroundColor(), IndexedColors.BRIGHT_GREEN.getIndex());

        XSSFWorkbook wbOrig2 = XSSFTestDataSamples.writeOutAndReadBack(wbOrig);
        assertNotNull(wbOrig2);
        wbOrig2.close();

        XSSFWorkbook wbClone2 = XSSFTestDataSamples.writeOutAndReadBack(wbClone);
        assertNotNull(wbClone2);
        wbClone2.close();

        wbReload.close();
        wbClone.close();
        wbOrig.close();
    }

    /**
     * Avoid ArrayIndexOutOfBoundsException  when creating cell style
     * in a workbook that has an empty xf table.
     */
	@Test
    void testBug52348() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("52348.xlsx");
        StylesTable st = workbook.getStylesSource();
        assertEquals(0, st._getStyleXfsSize());

        XSSFCellStyle style = workbook.createCellStyle(); // no exception at this point
        assertNull(style.getStyleXf());

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertNotNull(wb2);
        wb2.close();
        workbook.close();
    }

    /**
     * Avoid ArrayIndexOutOfBoundsException  when getting cell style
     * in a workbook that has an empty xf table.
     */
	@Test
    void testBug55650() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("52348.xlsx");
        StylesTable st = workbook.getStylesSource();
        assertEquals(0, st._getStyleXfsSize());

        // no exception at this point
        XSSFCellStyle style = workbook.getSheetAt(0).getRow(0).getCell(0).getCellStyle();
        assertNull(style.getStyleXf());

        XSSFWorkbook wb2 = XSSFTestDataSamples.writeOutAndReadBack(workbook);
        assertNotNull(wb2);
        wb2.close();

        workbook.close();
    }

	@Test
    void testShrinkToFit() throws IOException {
    	// Existing file
    	XSSFWorkbook wb1 = XSSFTestDataSamples.openSampleWorkbook("ShrinkToFit.xlsx");
    	Sheet s = wb1.getSheetAt(0);
    	Row r = s.getRow(0);
    	CellStyle cs = r.getCell(0).getCellStyle();

        assertTrue(cs.getShrinkToFit());

    	// New file
    	XSSFWorkbook wb2 = new XSSFWorkbook();
    	s = wb2.createSheet();
    	r = s.createRow(0);

    	cs = wb2.createCellStyle();
    	cs.setShrinkToFit(false);
    	r.createCell(0).setCellStyle(cs);

    	cs = wb2.createCellStyle();
    	cs.setShrinkToFit(true);
    	r.createCell(1).setCellStyle(cs);

    	// Write out, read, and check
    	XSSFWorkbook wb3 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
    	s = wb3.getSheetAt(0);
    	r = s.getRow(0);
        assertFalse(r.getCell(0).getCellStyle().getShrinkToFit());
        assertTrue(r.getCell(1).getCellStyle().getShrinkToFit());

    	XSSFWorkbook wb4 = XSSFTestDataSamples.writeOutAndReadBack(wb2);
    	assertNotNull(wb4);
    	wb4.close();

    	XSSFWorkbook wb5 = XSSFTestDataSamples.writeOutAndReadBack(wb3);
    	assertNotNull(wb5);
    	wb5.close();

    	wb3.close();
    	wb2.close();
    	wb1.close();

    }

    @Test
    void testSetColor() throws IOException {
        try(Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            Row row = sheet.createRow(0);

            DataFormat format = wb.createDataFormat();
            Cell cell = row.createCell(1);
            cell.setCellValue("somevalue");
            CellStyle cellStyle2 = wb.createCellStyle();


            cellStyle2.setDataFormat(format.getFormat("###0"));

            cellStyle2.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
            cellStyle2.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            cellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            cellStyle2.setAlignment(HorizontalAlignment.RIGHT);
            cellStyle2.setVerticalAlignment(VerticalAlignment.TOP);

            cell.setCellStyle(cellStyle2);

            try (Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb)) {
                Cell cellBack = wbBack.getSheetAt(0).getRow(0).getCell(1);
                assertNotNull(cellBack);
                CellStyle styleBack = cellBack.getCellStyle();
                assertEquals(IndexedColors.DARK_BLUE.getIndex(), styleBack.getFillBackgroundColor());
                assertEquals(IndexedColors.DARK_BLUE.getIndex(), styleBack.getFillForegroundColor());
                assertEquals(HorizontalAlignment.RIGHT, styleBack.getAlignment());
                assertEquals(VerticalAlignment.TOP, styleBack.getVerticalAlignment());
                assertEquals(FillPatternType.SOLID_FOREGROUND, styleBack.getFillPattern());
            }
        }
    }

    public static void copyStyles(Workbook reference, Workbook target) {
        final int numberOfStyles = reference.getNumCellStyles();
        // don't copy default style (style index 0)
        for (int i = 1; i < numberOfStyles; i++) {
            final CellStyle referenceStyle = reference.getCellStyleAt(i);
            final CellStyle targetStyle = target.createCellStyle();
            targetStyle.cloneStyleFrom(referenceStyle);
        }
        /*System.out.println("Reference : "+reference.getNumCellStyles());
        System.out.println("Target    : "+target.getNumCellStyles());*/
    }

    @Test
    void test58084() throws IOException {
        Workbook reference = XSSFTestDataSamples.openSampleWorkbook("template.xlsx");
        Workbook target = new XSSFWorkbook();
        copyStyles(reference, target);

        assertEquals(reference.getNumCellStyles(), target.getNumCellStyles());
        final Sheet sheet = target.createSheet();
        final Row row = sheet.createRow(0);
        int col = 0;
        for (short i = 1; i < target.getNumCellStyles(); i++) {
            final Cell cell = row.createCell(col++);
            cell.setCellValue("Coucou"+i);
            cell.setCellStyle(target.getCellStyleAt(i));
        }
        /*OutputStream out = new FileOutputStream("C:\\temp\\58084.xlsx");
        target.write(out);
        out.close();*/

        Workbook copy = XSSFTestDataSamples.writeOutAndReadBack(target);

        // previously this failed because the border-element was not copied over
        copy.getCellStyleAt((short)1).getBorderBottom();

        copy.close();

        target.close();
        reference.close();
    }

    @Test
    void test58043() {
        assertEquals(0, cellStyle.getRotation());

        cellStyle.setRotation((short)89);
        assertEquals(89, cellStyle.getRotation());

        cellStyle.setRotation((short)90);
        assertEquals(90, cellStyle.getRotation());

        cellStyle.setRotation((short)179);
        assertEquals(179, cellStyle.getRotation());

        cellStyle.setRotation((short)180);
        assertEquals(180, cellStyle.getRotation());

        // negative values are mapped to the correct values for compatibility between HSSF and XSSF
        cellStyle.setRotation((short)-1);
        assertEquals(91, cellStyle.getRotation());

        cellStyle.setRotation((short)-89);
        assertEquals(179, cellStyle.getRotation());

        cellStyle.setRotation((short)-90);
        assertEquals(180, cellStyle.getRotation());
    }

    @Test
    void bug58996_UsedToWorkIn3_11_ButNotIn3_13() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(null);
        assertNull(cellStyle.getFillForegroundColorColor());

        cellStyle.setFillBackgroundColor(null);
        assertNull(cellStyle.getFillBackgroundColorColor());

        cellStyle.setFillPattern(FillPatternType.NO_FILL);
        assertEquals(FillPatternType.NO_FILL, cellStyle.getFillPattern());

        cellStyle.setBottomBorderColor(null);
        assertNull(cellStyle.getBottomBorderXSSFColor());

        cellStyle.setTopBorderColor(null);
        assertNull(cellStyle.getTopBorderXSSFColor());

        cellStyle.setLeftBorderColor(null);
        assertNull(cellStyle.getLeftBorderXSSFColor());

        cellStyle.setRightBorderColor(null);
        assertNull(cellStyle.getRightBorderXSSFColor());

        workbook.close();
    }
}
