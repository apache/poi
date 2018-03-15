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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.poi.ss.ITestDataProvider;
import org.junit.Test;

/**
 * @author Yegor Kozlov
 */
public abstract class BaseTestFont {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestFont(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    protected final void baseTestDefaultFont(String defaultName, short defaultSize, short defaultColor) throws IOException {
        //get default font and check against default value
        Workbook workbook = _testDataProvider.createWorkbook();
        Font fontFind=workbook.findFont(false, defaultColor, defaultSize, defaultName, false, false, Font.SS_NONE, Font.U_NONE);
        assertNotNull(fontFind);

        //get default font, then change 2 values and check against different values (height changes)
        Font font=workbook.createFont();
        font.setBold(true);
        assertTrue(font.getBold());
        font.setUnderline(Font.U_DOUBLE);
        assertEquals(Font.U_DOUBLE, font.getUnderline());
        font.setFontHeightInPoints((short)15);
        assertEquals(15*20, font.getFontHeight());
        assertEquals(15, font.getFontHeightInPoints());
        fontFind=workbook.findFont(true, defaultColor, (short)(15*20), defaultName, false, false, Font.SS_NONE, Font.U_DOUBLE);
        assertNotNull(fontFind);
        workbook.close();
    }

    @Test
    public final void testGetNumberOfFonts() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        int num0 = wb.getNumberOfFontsAsInt();

        Font f1=wb.createFont();
        f1.setBold(true);
        short idx1 = f1.getIndex();
        wb.createCellStyle().setFont(f1);

        Font f2=wb.createFont();
        f2.setUnderline(Font.U_DOUBLE);
        short idx2 = f2.getIndex();
        wb.createCellStyle().setFont(f2);

        Font f3=wb.createFont();
        f3.setFontHeightInPoints((short)23);
        short idx3 = f3.getIndex();
        wb.createCellStyle().setFont(f3);

        assertEquals(num0 + 3,wb.getNumberOfFonts());
        assertTrue(wb.getFontAt(idx1).getBold());
        assertEquals(Font.U_DOUBLE,wb.getFontAt(idx2).getUnderline());
        assertEquals(23,wb.getFontAt(idx3).getFontHeightInPoints());
        wb.close();
	}

    /**
     * Tests that we can define fonts to a new
     *  file, save, load, and still see them
     */
    @Test
    public final void testCreateSave() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s1 = wb1.createSheet();
        Row r1 = s1.createRow(0);
        Cell r1c1 = r1.createCell(0);
        r1c1.setCellValue(2.2);

        int num0 = wb1.getNumberOfFonts();

        Font font=wb1.createFont();
        font.setBold(true);
        font.setStrikeout(true);
        font.setColor(IndexedColors.YELLOW.getIndex());
        font.setFontName("Courier");
        int font1Idx = font.getIndexAsInt();
        wb1.createCellStyle().setFont(font);
        assertEquals(num0 + 1, wb1.getNumberOfFontsAsInt());

        CellStyle cellStyleTitle=wb1.createCellStyle();
        cellStyleTitle.setFont(font);
        r1c1.setCellStyle(cellStyleTitle);

        // Save and re-load
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        s1 = wb2.getSheetAt(0);

        assertEquals(num0 + 1, wb2.getNumberOfFontsAsInt());
        int idx = s1.getRow(0).getCell(0).getCellStyle().getFontIndexAsInt();
        Font fnt = wb2.getFontAt(idx);
        assertNotNull(fnt);
        assertEquals(IndexedColors.YELLOW.getIndex(), fnt.getColor());
        assertEquals("Courier", fnt.getFontName());

        // Now add an orphaned one
        Font font2 = wb2.createFont();
        font2.setItalic(true);
        font2.setFontHeightInPoints((short)15);
        int font2Idx = font2.getIndexAsInt();
        wb2.createCellStyle().setFont(font2);
        assertEquals(num0 + 2, wb2.getNumberOfFontsAsInt());

        // Save and re-load
        Workbook wb3 = _testDataProvider.writeOutAndReadBack(wb2);
        wb2.close();
        s1 = wb3.getSheetAt(0);
        assertNotNull(s1);

        assertEquals(num0 + 2, wb3.getNumberOfFontsAsInt());
        assertNotNull(wb3.getFontAt(font1Idx));
        assertNotNull(wb3.getFontAt(font2Idx));

        assertEquals(15, wb3.getFontAt(font2Idx).getFontHeightInPoints());
        assertEquals(true, wb3.getFontAt(font2Idx).getItalic());
        wb3.close();
    }

    /**
     * Test that fonts get added properly
     */
    @Test
    public final void test45338() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        int num0 = wb.getNumberOfFontsAsInt();

        Sheet s = wb.createSheet();
        s.createRow(0);
        s.createRow(1);
        s.getRow(0).createCell(0);
        s.getRow(1).createCell(0);

        //default font
        Font f1 = wb.getFontAt(0);
        assertFalse(f1.getBold());

        // Check that asking for the same font
        //  multiple times gives you the same thing.
        // Otherwise, our tests wouldn't work!
        assertSame(wb.getFontAt(0), wb.getFontAt(0));

        // Look for a new font we have
        //  yet to add
        assertNull(
            wb.findFont(
                true, (short)123, (short)(22*20),
                "Thingy", false, true, (short)2, (byte)2
            )
        );

        Font nf = wb.createFont();
        int nfIdx = nf.getIndexAsInt();
        assertEquals(num0 + 1, wb.getNumberOfFontsAsInt());

        assertSame(nf, wb.getFontAt(nfIdx));

        nf.setBold(true);
        nf.setColor((short)123);
        nf.setFontHeightInPoints((short)22);
        nf.setFontName("Thingy");
        nf.setItalic(false);
        nf.setStrikeout(true);
        nf.setTypeOffset((short)2);
        nf.setUnderline((byte)2);

        assertEquals(num0 + 1, wb.getNumberOfFontsAsInt());
        assertEquals(nf, wb.getFontAt(nfIdx));

        assertEquals(wb.getFontAt(nfIdx), wb.getFontAt(nfIdx));
        assertTrue(wb.getFontAt(0) != wb.getFontAt(nfIdx));

        // Find it now
        assertNotNull(
            wb.findFont(
                true, (short)123, (short)(22*20),
                "Thingy", false, true, (short)2, (byte)2
            )
        );
        assertSame(nf,
               wb.findFont(
                   true, (short)123, (short)(22*20),
                   "Thingy", false, true, (short)2, (byte)2
               )
        );
        wb.close();
    }
}
