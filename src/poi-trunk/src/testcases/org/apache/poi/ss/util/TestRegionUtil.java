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

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests that the common RegionUtil works as we need it to.
 */
public final class TestRegionUtil {
    private static final CellRangeAddress A1C3 = new CellRangeAddress(0, 2, 0, 2);
    private static final BorderStyle NONE = BorderStyle.NONE;
    private static final BorderStyle THIN = BorderStyle.THIN;
    private static final int RED = IndexedColors.RED.getIndex();
    private static final int DEFAULT_COLOR = 0;
    private Workbook wb;
    private Sheet sheet;
    
    @Before
    public void setUp() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet();
    }
    
    @After
    public void tearDown() throws IOException {
        wb.close();
    }
    
    private CellStyle getCellStyle(int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);
        Cell cell = row.getCell(columnIndex);
        if (cell == null) cell = row.createCell(columnIndex);
        return cell.getCellStyle();
    }
    
    @Test
    public void setBorderTop() {
        assertEquals(NONE, getCellStyle(0, 0).getBorderTop());
        assertEquals(NONE, getCellStyle(0, 1).getBorderTop());
        assertEquals(NONE, getCellStyle(0, 2).getBorderTop());
        RegionUtil.setBorderTop(THIN, A1C3, sheet);
        assertEquals(THIN, getCellStyle(0, 0).getBorderTop());
        assertEquals(THIN, getCellStyle(0, 1).getBorderTop());
        assertEquals(THIN, getCellStyle(0, 2).getBorderTop());
    }
    @Test
    public void setBorderBottom() {
        assertEquals(NONE, getCellStyle(2, 0).getBorderBottom());
        assertEquals(NONE, getCellStyle(2, 1).getBorderBottom());
        assertEquals(NONE, getCellStyle(2, 2).getBorderBottom());
        RegionUtil.setBorderBottom(THIN, A1C3, sheet);
        assertEquals(THIN, getCellStyle(2, 0).getBorderBottom());
        assertEquals(THIN, getCellStyle(2, 1).getBorderBottom());
        assertEquals(THIN, getCellStyle(2, 2).getBorderBottom());
    }
    @Test
    public void setBorderRight() {
        assertEquals(NONE, getCellStyle(0, 2).getBorderRight());
        assertEquals(NONE, getCellStyle(1, 2).getBorderRight());
        assertEquals(NONE, getCellStyle(2, 2).getBorderRight());
        RegionUtil.setBorderRight(THIN, A1C3, sheet);
        assertEquals(THIN, getCellStyle(0, 2).getBorderRight());
        assertEquals(THIN, getCellStyle(1, 2).getBorderRight());
        assertEquals(THIN, getCellStyle(2, 2).getBorderRight());
    }
    @Test
    public void setBorderLeft() {
        assertEquals(NONE, getCellStyle(0, 0).getBorderLeft());
        assertEquals(NONE, getCellStyle(1, 0).getBorderLeft());
        assertEquals(NONE, getCellStyle(2, 0).getBorderLeft());
        RegionUtil.setBorderLeft(THIN, A1C3, sheet);
        assertEquals(THIN, getCellStyle(0, 0).getBorderLeft());
        assertEquals(THIN, getCellStyle(1, 0).getBorderLeft());
        assertEquals(THIN, getCellStyle(2, 0).getBorderLeft());
    }
    
    @Test
    public void setTopBorderColor() {
        assertEquals(DEFAULT_COLOR, getCellStyle(0, 0).getTopBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(0, 1).getTopBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(0, 2).getTopBorderColor());
        RegionUtil.setTopBorderColor(RED, A1C3, sheet);
        assertEquals(RED, getCellStyle(0, 0).getTopBorderColor());
        assertEquals(RED, getCellStyle(0, 1).getTopBorderColor());
        assertEquals(RED, getCellStyle(0, 2).getTopBorderColor());
    }
    @Test
    public void setBottomBorderColor() {
        assertEquals(DEFAULT_COLOR, getCellStyle(2, 0).getBottomBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(2, 1).getBottomBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(2, 2).getBottomBorderColor());
        RegionUtil.setBottomBorderColor(RED, A1C3, sheet);
        assertEquals(RED, getCellStyle(2, 0).getBottomBorderColor());
        assertEquals(RED, getCellStyle(2, 1).getBottomBorderColor());
        assertEquals(RED, getCellStyle(2, 2).getBottomBorderColor());
    }
    @Test
    public void setRightBorderColor() {
        assertEquals(DEFAULT_COLOR, getCellStyle(0, 2).getRightBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(1, 2).getRightBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(2, 2).getRightBorderColor());
        RegionUtil.setRightBorderColor(RED, A1C3, sheet);
        assertEquals(RED, getCellStyle(0, 2).getRightBorderColor());
        assertEquals(RED, getCellStyle(1, 2).getRightBorderColor());
        assertEquals(RED, getCellStyle(2, 2).getRightBorderColor());
    }
    @Test
    public void setLeftBorderColor() {
        assertEquals(DEFAULT_COLOR, getCellStyle(0, 0).getLeftBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(1, 0).getLeftBorderColor());
        assertEquals(DEFAULT_COLOR, getCellStyle(2, 0).getLeftBorderColor());
        RegionUtil.setLeftBorderColor(RED, A1C3, sheet);
        assertEquals(RED, getCellStyle(0, 0).getLeftBorderColor());
        assertEquals(RED, getCellStyle(1, 0).getLeftBorderColor());
        assertEquals(RED, getCellStyle(2, 0).getLeftBorderColor());
    }
    
    @Test
    public void bordersCanBeAddedToNonExistantCells() {
        RegionUtil.setBorderTop(THIN, A1C3, sheet);
        assertEquals(THIN, getCellStyle(0, 0).getBorderTop());
        assertEquals(THIN, getCellStyle(0, 1).getBorderTop());
        assertEquals(THIN, getCellStyle(0, 2).getBorderTop());
    }
    @Test
    public void borderColorsCanBeAddedToNonExistantCells() {
        RegionUtil.setTopBorderColor(RED, A1C3, sheet);
        assertEquals(RED, getCellStyle(0, 0).getTopBorderColor());
        assertEquals(RED, getCellStyle(0, 1).getTopBorderColor());
        assertEquals(RED, getCellStyle(0, 2).getTopBorderColor());
    }
}
