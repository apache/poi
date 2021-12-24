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

package org.apache.poi.ss.util;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseTestCellUtilCopy {

    protected Cell srcCell, destCell; //used for testCopyCellFrom_CellCopyPolicy

    @AfterEach
    public void tearDown() throws IOException {
        if (srcCell != null) {
            srcCell.getRow().getSheet().getWorkbook().close();
        }
        if (destCell != null) {
            destCell.getRow().getSheet().getWorkbook().close();
        }
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_default() {
        setUp_testCopyCellFrom_CellCopyPolicy();

        // default copy policy
        final CellCopyPolicy policy = new CellCopyPolicy();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());

        assertEquals(CellType.FORMULA, destCell.getCellType());
        assertEquals("2+3", destCell.getCellFormula());
        assertEquals(srcCell.getCellStyle(), destCell.getCellStyle());
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_value() {
        setUp_testCopyCellFrom_CellCopyPolicy();

        // Paste values only
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(false).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertEquals(CellType.NUMERIC, destCell.getCellType());
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_richTextValue() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        Workbook wb = srcCell.getSheet().getWorkbook();
        CreationHelper creationHelper = wb.getCreationHelper();
        RichTextString rts = creationHelper.createRichTextString("text 123");
        Font font = wb.createFont();
        font.setFontHeight((short) 999);
        font.setFontName("Muriel");
        rts.applyFont(0, 3, font);
        srcCell.setCellFormula(null);
        srcCell.setCellValue(rts);

        // Paste values only
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(false).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertEquals(CellType.STRING, destCell.getCellType());
        assertTrue(compareRichText(rts, destCell.getRichStringCellValue()));
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_formulaWithUnregisteredUDF() {
        setUp_testCopyCellFrom_CellCopyPolicy();

        srcCell.setCellFormula("MYFUNC2(123, $A5, Sheet1!$B7)");

        // Copy formula verbatim (no shifting). This is okay because copyCellFrom is Internal.
        // Users should use higher-level copying functions to row- or column-shift formulas.
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(true).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertEquals("MYFUNC2(123,$A5,Sheet1!$B7)", stringWithoutSpaces(destCell.getCellFormula()));
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_style() {
        setUp_testCopyCellFrom_CellCopyPolicy();
        srcCell.setCellValue((String) null);

        // Paste styles only
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellValue(false).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertEquals(srcCell.getCellStyle(), destCell.getCellStyle());

        // Old cell value should not have been overwritten
        assertNotEquals(CellType.BLANK, destCell.getCellType());
        assertEquals(CellType.BOOLEAN, destCell.getCellType());
        assertTrue(destCell.getBooleanCellValue());
    }

    @Test
    public final void testCopyCellFrom_CellCopyPolicy_copyHyperlink() throws IOException {
        setUp_testCopyCellFrom_CellCopyPolicy();
        final Workbook wb = srcCell.getSheet().getWorkbook();
        final CreationHelper createHelper = wb.getCreationHelper();

        srcCell.setCellValue("URL LINK");
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        final String address = "https://poi.apache.org/";
        link.setAddress(address);
        srcCell.setHyperlink(link);

        // Set link cell style (optional)
        setLinkCellStyle(wb, srcCell);

        // Copy hyperlink
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().copyHyperlink(true).mergeHyperlink(false).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertNotNull(destCell.getHyperlink());

        assertSame(srcCell.getSheet(), destCell.getSheet(),
                "unit test assumes srcCell and destCell are on the same sheet");

        final List<? extends Hyperlink> links = srcCell.getSheet().getHyperlinkList();
        assertEquals(2, links.size(), "number of hyperlinks on sheet");
        assertEquals(address, links.get(0).getAddress());
        assertEquals(address, links.get(1).getAddress());
        checkHyperlinkCellRef(links.get(0), srcCell.getAddress());
        checkHyperlinkCellRef(links.get(1), destCell.getAddress());

        wb.close();
    }

    private void setUp_testCopyCellFrom_CellCopyPolicy() {
        @SuppressWarnings("resource")
        final Workbook wb = createNewWorkbook();
        final Row row = wb.createSheet("Sheet1").createRow(0);
        srcCell = row.createCell(0);
        destCell = row.createCell(1);

        srcCell.setCellFormula("2+3");

        final CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THICK);
        style.setFillBackgroundColor((short) 5);
        srcCell.setCellStyle(style);

        destCell.setCellValue(true);
    }

    protected void setLinkCellStyle(Workbook wb, Cell srcCell) {
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        srcCell.setCellStyle(hlinkStyle);
    }

    protected String stringWithoutSpaces(String input) {
        return input.replace(" ", "");
    }

    protected void checkHyperlinkCellRef(Hyperlink hyperlink, CellAddress cellRef) {
        assertEquals(cellRef.getRow(), hyperlink.getFirstRow(), "first row");
        assertEquals(cellRef.getRow(), hyperlink.getLastRow(), "last row");
        assertEquals(cellRef.getColumn(), hyperlink.getFirstColumn(), "first column");
        assertEquals(cellRef.getColumn(), hyperlink.getLastColumn(), "last column");
    }

    protected abstract Workbook createNewWorkbook();

    protected boolean compareRichText(RichTextString rts1, RichTextString rts2) {
        return rts1.getString().equals(rts2.getString());
    }
}
