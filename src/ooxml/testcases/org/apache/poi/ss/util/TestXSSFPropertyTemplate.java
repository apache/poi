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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestXSSFPropertyTemplate {
    
    @Test
    public void applyBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        CellRangeAddress b2 = new CellRangeAddress(1, 1, 1, 1);
        PropertyTemplate pt = new PropertyTemplate();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        pt.drawBorders(a1c3, BorderStyle.THIN, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                assertEquals(BorderStyle.THIN, cs.getBorderTop());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderBottom());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderLeft());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
            }
        }
        
        pt.drawBorders(b2, BorderStyle.NONE, BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 0) {
                assertEquals(BorderStyle.THIN, cs.getBorderTop());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderTop());
                }
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 2) {
                assertEquals(BorderStyle.THIN, cs.getBorderBottom());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderBottom());
                }
                if (cell.getColumnIndex() == 0 || row.getRowNum() != 1) {
                assertEquals(BorderStyle.THIN, cs.getBorderLeft());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderLeft());
                }
                if (cell.getColumnIndex() == 2 || row.getRowNum() != 1) {
                assertEquals(BorderStyle.THIN, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderRight());
                }
            }
        }
        
        wb.close();
    }
    
    @Test
    public void clonePropertyTemplate() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        PropertyTemplate pt2 = new PropertyTemplate(pt);
        assertNotSame(pt2, pt);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt2.getNumBorderColors(i, j));
                assertEquals(4, pt2.getNumBorderColors(i, j));
            }
        }
        
        CellRangeAddress b2 = new CellRangeAddress(1,1,1,1);
        pt2.drawBorders(b2, BorderStyle.THIN, BorderExtent.ALL);
        
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        pt.applyBorders(sheet);
        
        for (Row row : sheet) {
            for (Cell cell : row) {
                CellStyle cs = cell.getCellStyle();
                assertEquals(BorderStyle.MEDIUM, cs.getBorderTop());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderBottom());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderLeft());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
            }
        }
        
        wb.close();
    }
}