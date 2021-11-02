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
package org.apache.poi.xssf.usermodel.extensions;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

import static org.junit.jupiter.api.Assertions.*;

public class XSSFCellBorderTest {
    private final XSSFWorkbook wb = new XSSFWorkbook();
    private final StylesTable stylesSource = wb.getStylesSource();
    private final XSSFCellBorder empty = new XSSFCellBorder();

    @BeforeEach
    void setUp() {
        assertNotNull(stylesSource);
        assertEquals(1, stylesSource.getBorders().size());
    }

    @Test
    void testEquals() {
        for (XSSFCellBorder.BorderSide side : XSSFCellBorder.BorderSide.values()) {
            XSSFCellBorder border = new XSSFCellBorder();
            assertEquals(empty, border);
            assertEquals(empty.hashCode(), border.hashCode());

            border.setBorderStyle(side, BorderStyle.THICK);
            assertNotEquals(empty, border);

            border = new XSSFCellBorder();
            assertEquals(empty, border);
            assertEquals(empty.hashCode(), border.hashCode());

            border.setBorderColor(side, new XSSFColor(stylesSource.getIndexedColors()));
            assertNotEquals(empty, border);
        }

        // also verify diagonal_up, diagonal_down and outline
        XSSFCellBorder border = new XSSFCellBorder();
        border.getCTBorder().setDiagonalUp(true);
        assertNotEquals(empty, border);

        border = new XSSFCellBorder();
        border.getCTBorder().setDiagonalDown(true);
        assertNotEquals(empty, border);

        border = new XSSFCellBorder();
        border.getCTBorder().setOutline(true);
        assertNotEquals(empty, border);
    }

    @Test
    void testConstruct() {
        XSSFCellBorder border = new XSSFCellBorder((CTBorder) empty.getCTBorder().copy());
        assertEquals(empty, border);
        border.getCTBorder().setOutline(true);
        assertNotEquals(empty, border);

        border = new XSSFCellBorder((CTBorder) empty.getCTBorder().copy(), stylesSource.getIndexedColors());
        assertEquals(empty, border);
        border.getCTBorder().setOutline(true);
        assertNotEquals(empty, border);

        border = new XSSFCellBorder((CTBorder) empty.getCTBorder().copy(), stylesSource.getTheme(),
                stylesSource.getIndexedColors());
        assertEquals(empty, border);
        border.getCTBorder().setOutline(true);
        assertNotEquals(empty, border);
    }

    @Test
    void testGettersSetters() {
        assertNotNull(empty.getCTBorder());

        XSSFCellBorder border = new XSSFCellBorder((CTBorder) empty.getCTBorder().copy());
        border.setThemesTable(stylesSource.getTheme());
        assertNotNull(border.getCTBorder());
    }

    @Test
    void testSetBorderStyle() {
        XSSFCellBorder border = new XSSFCellBorder();
        for (XSSFCellBorder.BorderSide side : XSSFCellBorder.BorderSide.values()) {
            assertEquals(BorderStyle.NONE, border.getBorderStyle(side));

            border.setBorderStyle(side, BorderStyle.THIN);
            assertEquals(BorderStyle.THIN, border.getBorderStyle(side));
        }
    }

    @Test
    void testSetBorderColor() {
        XSSFCellBorder border = new XSSFCellBorder();
        XSSFColor color = new XSSFColor(stylesSource.getIndexedColors());

        for (XSSFCellBorder.BorderSide side : XSSFCellBorder.BorderSide.values()) {
            assertNull(border.getBorderColor(side));

            border.setBorderColor(side, color);
            assertEquals(color, border.getBorderColor(side));
        }
    }

    @Test
    void testRegression() throws Exception {
        XSSFCellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);

        assertEquals(3, stylesSource.getBorders().size());

        ThemesTable _theme = stylesSource.getTheme();
        CTXf _cellXf = style.getCoreXf();
        assertTrue(_cellXf.getApplyBorder());

        int idx = (int) _cellXf.getBorderId();
        XSSFCellBorder cf = stylesSource.getBorderAt(idx);
        CTBorder ct = (CTBorder) cf.getCTBorder().copy();

        assertFalse(ct.isSetDiagonal());
        CTBorderPr pr = ct.addNewDiagonal();
        ct.setDiagonalUp(true);
        pr.setStyle(STBorderStyle.Enum.forInt(BorderStyle.THICK.getCode() + 1));
        idx = stylesSource.putBorder(new XSSFCellBorder(ct, _theme,
                stylesSource.getIndexedColors()));
        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);

        assertEquals(4, stylesSource.getBorders().size());

        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);

        Sheet sheet = wb.createSheet("Sheet1");

        assertEquals(6, stylesSource.getBorders().size());

        Cell cell = sheet.createRow(1).createCell(2);
        cell.setCellStyle(style);

        assertEquals(6, stylesSource.getBorders().size());

        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);

        assertEquals(6, wbBack.getStylesSource().getBorders().size());

        wb.close();
    }
}