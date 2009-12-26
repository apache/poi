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

import junit.framework.TestCase;

import org.apache.poi.ss.ITestDataProvider;

/**
 * @author Yegor Kozlov
 */
public abstract class BaseTestFont extends TestCase {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestFont(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    protected final void baseTestDefaultFont(String defaultName, short defaultSize, short defaultColor){
        //get default font and check against default value
        Workbook workbook = _testDataProvider.createWorkbook();
        Font fontFind=workbook.findFont(Font.BOLDWEIGHT_NORMAL, defaultColor, defaultSize, defaultName, false, false, Font.SS_NONE, Font.U_NONE);
        assertNotNull(fontFind);

        //get default font, then change 2 values and check against different values (height changes)
        Font font=workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        assertEquals(Font.BOLDWEIGHT_BOLD, font.getBoldweight());
        font.setUnderline(Font.U_DOUBLE);
        assertEquals(Font.U_DOUBLE, font.getUnderline());
        font.setFontHeightInPoints((short)15);
        assertEquals(15*20, font.getFontHeight());
        assertEquals(15, font.getFontHeightInPoints());
        fontFind=workbook.findFont(Font.BOLDWEIGHT_BOLD, defaultColor, (short)(15*20), defaultName, false, false, Font.SS_NONE, Font.U_DOUBLE);
        assertNotNull(fontFind);
    }

    public final void testGetNumberOfFonts(){
        Workbook wb = _testDataProvider.createWorkbook();
        int num0 = wb.getNumberOfFonts();

        Font f1=wb.createFont();
        f1.setBoldweight(Font.BOLDWEIGHT_BOLD);
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
        assertEquals(Font.BOLDWEIGHT_BOLD,wb.getFontAt(idx1).getBoldweight());
        assertEquals(Font.U_DOUBLE,wb.getFontAt(idx2).getUnderline());
        assertEquals(23,wb.getFontAt(idx3).getFontHeightInPoints());
	}

    /**
     * Tests that we can define fonts to a new
     *  file, save, load, and still see them
     */
    public final void testCreateSave() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s1 = wb.createSheet();
        Row r1 = s1.createRow(0);
        Cell r1c1 = r1.createCell(0);
        r1c1.setCellValue(2.2);

        int num0 = wb.getNumberOfFonts();

        Font font=wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setStrikeout(true);
        font.setColor(IndexedColors.YELLOW.getIndex());
        font.setFontName("Courier");
        short font1Idx = font.getIndex();
        wb.createCellStyle().setFont(font);
        assertEquals(num0 + 1, wb.getNumberOfFonts());

        CellStyle cellStyleTitle=wb.createCellStyle();
        cellStyleTitle.setFont(font);
        r1c1.setCellStyle(cellStyleTitle);

        // Save and re-load
        wb = _testDataProvider.writeOutAndReadBack(wb);
        s1 = wb.getSheetAt(0);

        assertEquals(num0 + 1, wb.getNumberOfFonts());
        short idx = s1.getRow(0).getCell(0).getCellStyle().getFontIndex();
        Font fnt = wb.getFontAt(idx);
        assertNotNull(fnt);
        assertEquals(IndexedColors.YELLOW.getIndex(), fnt.getColor());
        assertEquals("Courier", fnt.getFontName());

        // Now add an orphaned one
        Font font2 = wb.createFont();
        font2.setItalic(true);
        font2.setFontHeightInPoints((short)15);
        short font2Idx = font2.getIndex();
        wb.createCellStyle().setFont(font2);
        assertEquals(num0 + 2, wb.getNumberOfFonts());

        // Save and re-load
        wb = _testDataProvider.writeOutAndReadBack(wb);
        s1 = wb.getSheetAt(0);

        assertEquals(num0 + 2, wb.getNumberOfFonts());
        assertNotNull(wb.getFontAt(font1Idx));
        assertNotNull(wb.getFontAt(font2Idx));

        assertEquals(15, wb.getFontAt(font2Idx).getFontHeightInPoints());
        assertEquals(true, wb.getFontAt(font2Idx).getItalic());
    }

    /**
     * Test that fonts get added properly
     *
     * @see org.apache.poi.hssf.usermodel.TestBugs#test45338()
     */
    public final void test45338() {
        Workbook wb = _testDataProvider.createWorkbook();
        int num0 = wb.getNumberOfFonts();

        Sheet s = wb.createSheet();
        s.createRow(0);
        s.createRow(1);
        s.getRow(0).createCell(0);
        s.getRow(1).createCell(0);

        //default font
        Font f1 = wb.getFontAt((short)0);
        assertEquals(Font.BOLDWEIGHT_NORMAL, f1.getBoldweight());

        // Check that asking for the same font
        //  multiple times gives you the same thing.
        // Otherwise, our tests wouldn't work!
        assertSame(wb.getFontAt((short)0), wb.getFontAt((short)0));

        // Look for a new font we have
        //  yet to add
        assertNull(
            wb.findFont(
                Font.BOLDWEIGHT_BOLD, (short)123, (short)(22*20),
                "Thingy", false, true, (short)2, (byte)2
            )
        );

        Font nf = wb.createFont();
        short nfIdx = nf.getIndex();
        assertEquals(num0 + 1, wb.getNumberOfFonts());

        assertSame(nf, wb.getFontAt(nfIdx));

        nf.setBoldweight(Font.BOLDWEIGHT_BOLD);
        nf.setColor((short)123);
        nf.setFontHeightInPoints((short)22);
        nf.setFontName("Thingy");
        nf.setItalic(false);
        nf.setStrikeout(true);
        nf.setTypeOffset((short)2);
        nf.setUnderline((byte)2);

        assertEquals(num0 + 1, wb.getNumberOfFonts());
        assertEquals(nf, wb.getFontAt(nfIdx));

        assertEquals(wb.getFontAt(nfIdx), wb.getFontAt(nfIdx));
        assertTrue(wb.getFontAt((short)0) != wb.getFontAt(nfIdx));

        // Find it now
        assertNotNull(
            wb.findFont(
                Font.BOLDWEIGHT_BOLD, (short)123, (short)(22*20),
                "Thingy", false, true, (short)2, (byte)2
            )
        );
        assertSame(nf,
               wb.findFont(
                   Font.BOLDWEIGHT_BOLD, (short)123, (short)(22*20),
                   "Thingy", false, true, (short)2, (byte)2
               )
        );
    }
}
