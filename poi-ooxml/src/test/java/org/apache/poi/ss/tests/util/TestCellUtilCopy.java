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

package org.apache.poi.ss.tests.util;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCellUtilCopy {

    private XSSFCell srcCell, destCell; //used for testCopyCellFrom_CellCopyPolicy

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
    public final void testCopyCellFrom_CellCopyPolicy_formulaWithUnregisteredUDF() {
        setUp_testCopyCellFrom_CellCopyPolicy();

        srcCell.setCellFormula("MYFUNC2(123, $A5, Sheet1!$B7)");

        // Copy formula verbatim (no shifting). This is okay because copyCellFrom is Internal.
        // Users should use higher-level copying functions to row- or column-shift formulas.
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().cellFormula(true).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertEquals("MYFUNC2(123, $A5, Sheet1!$B7)", destCell.getCellFormula());
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
        link.setAddress("https://poi.apache.org/");
        srcCell.setHyperlink(link);

        // Set link cell style (optional)
        setLinkCellStyle(wb, srcCell);

        // Copy hyperlink
        final CellCopyPolicy policy = new CellCopyPolicy.Builder().copyHyperlink(true).mergeHyperlink(false).build();
        CellUtil.copyCell(srcCell, destCell, policy, new CellCopyContext());
        assertNotNull(destCell.getHyperlink());

        assertSame(srcCell.getSheet(), destCell.getSheet(),
                "unit test assumes srcCell and destCell are on the same sheet");

        final List<XSSFHyperlink> links = srcCell.getSheet().getHyperlinkList();
        assertEquals(2, links.size(), "number of hyperlinks on sheet");
        assertEquals(new CellAddress(srcCell).formatAsString(), links.get(0).getCellRef(), "source hyperlink");
        assertEquals(new CellAddress(destCell).formatAsString(), links.get(1).getCellRef(), "destination hyperlink");

        wb.close();
    }

    private void setUp_testCopyCellFrom_CellCopyPolicy() {
        @SuppressWarnings("resource")
        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFRow row = wb.createSheet("Sheet1").createRow(0);
        srcCell = row.createCell(0);
        destCell = row.createCell(1);

        srcCell.setCellFormula("2+3");

        final CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THICK);
        style.setFillBackgroundColor((short) 5);
        srcCell.setCellStyle(style);

        destCell.setCellValue(true);
    }

    private void setLinkCellStyle(Workbook wb, XSSFCell srcCell) {
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        srcCell.setCellStyle(hlinkStyle);
    }

}
