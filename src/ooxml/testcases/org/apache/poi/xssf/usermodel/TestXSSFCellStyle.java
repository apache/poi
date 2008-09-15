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

import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
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

    public void setUp() {
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
        assertEquals(1, fillId);

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
        XSSFColor color = new XSSFColor(ctColor);
        assertEquals((short)2, cellStyle.getBottomBorderColor());
        CTColor anotherCtColor = CTColor.Factory.newInstance();
        anotherCtColor.setIndexed(4);
        anotherCtColor.setTheme(3);
        anotherCtColor.setRgb("1234".getBytes());
        XSSFColor anotherColor = new XSSFColor(anotherCtColor);
        cellStyle.setBorderColor(BorderSide.BOTTOM, anotherColor);
        assertEquals((short)4, cellStyle.getBottomBorderColor());
        assertEquals(new String("1234".getBytes()), new String(cellStyle.getBorderColor(BorderSide.BOTTOM).getRgb()));
    }

    public void testGetSetTopBorderColor() {
        CTColor ctColor = ctBorderA.addNewTop().addNewColor();
        ctColor.setIndexed(5);
        XSSFColor color = new XSSFColor(ctColor);
        assertEquals((short)5, cellStyle.getTopBorderColor());
        CTColor anotherCtColor = CTColor.Factory.newInstance();
        anotherCtColor.setIndexed(7);
        anotherCtColor.setTheme(3);
        anotherCtColor.setRgb("abcd".getBytes());
        XSSFColor anotherColor = new XSSFColor(anotherCtColor);
        cellStyle.setBorderColor(BorderSide.TOP, anotherColor);
        assertEquals((short)7, cellStyle.getTopBorderColor());
        assertEquals(new String("abcd".getBytes()), new String(cellStyle.getBorderColor(BorderSide.TOP).getRgb()));
    }

    public void testGetSetLeftBorderColor() {
        CTColor ctColor = ctBorderA.addNewLeft().addNewColor();
        ctColor.setIndexed(2);
        XSSFColor color = new XSSFColor(ctColor);
        assertEquals((short)2, cellStyle.getLeftBorderColor());
        CTColor anotherCtColor = CTColor.Factory.newInstance();
        anotherCtColor.setIndexed(4);
        anotherCtColor.setTheme(3);
        anotherCtColor.setRgb("1234".getBytes());
        XSSFColor anotherColor = new XSSFColor(anotherCtColor);
        cellStyle.setBorderColor(BorderSide.LEFT, anotherColor);
        assertEquals((short)4, cellStyle.getLeftBorderColor());
        assertEquals(new String("1234".getBytes()), new String(cellStyle.getBorderColor(BorderSide.LEFT).getRgb()));
    }

    public void testGetSetRightBorderColor() {
        CTColor ctColor = ctBorderA.addNewRight().addNewColor();
        ctColor.setIndexed(8);
        XSSFColor color = new XSSFColor(ctColor);
        assertEquals((short)8, cellStyle.getRightBorderColor());
        CTColor anotherCtColor = CTColor.Factory.newInstance();
        anotherCtColor.setIndexed(14);
        anotherCtColor.setTheme(3);
        anotherCtColor.setRgb("af67".getBytes());
        XSSFColor anotherColor = new XSSFColor(anotherCtColor);
        cellStyle.setBorderColor(BorderSide.RIGHT, anotherColor);
        assertEquals((short)14, cellStyle.getRightBorderColor());
        assertEquals(new String("af67".getBytes()), new String(cellStyle.getBorderColor(BorderSide.RIGHT).getRgb()));
    }

    public void testGetFillBackgroundColor() {
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        CTColor ctBgColor = ctPatternFill.addNewBgColor();
        ctBgColor.setIndexed(4);
        assertEquals(4, cellStyle.getFillBackgroundColor());
    }

    public void testGetFillForegroundColor() {
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        CTColor ctFgColor = ctPatternFill.addNewFgColor();
        ctFgColor.setIndexed(5);
        assertEquals(5, cellStyle.getFillForegroundColor());
    }

    public void testGetFillPattern() {
        CTPatternFill ctPatternFill = ctFill.addNewPatternFill();
        ctPatternFill.setPatternType(STPatternType.DARK_DOWN);
        assertEquals(8, cellStyle.getFillPattern());
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
