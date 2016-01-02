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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.junit.Test;

/**
 * Tests Spreadsheet CellUtil
 *
 * @see org.apache.poi.ss.util.CellUtil
 */
public final class TestCellUtil {
    @Test
    public void testSetCellStyleProperty() throws Exception {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add a border should create a new style
        int styCnt1 = wb.getNumCellStyles();
        CellUtil.setCellStyleProperty(c, wb, CellUtil.BORDER_BOTTOM, CellStyle.BORDER_THIN);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals(styCnt2, styCnt1+1);

        // Add same border to another cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperty(c, wb, CellUtil.BORDER_BOTTOM, CellStyle.BORDER_THIN);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals(styCnt3, styCnt2);

        wb.close();
    }

    @Test
    public void testSetCellStyleProperties() throws Exception {
        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        Cell c = r.createCell(0);

        // Add multiple border properties to cell should create a single new style
        int styCnt1 = wb.getNumCellStyles();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(CellUtil.BORDER_TOP, CellStyle.BORDER_THIN);
        props.put(CellUtil.BORDER_BOTTOM, CellStyle.BORDER_THIN);
        props.put(CellUtil.BORDER_LEFT, CellStyle.BORDER_THIN);
        props.put(CellUtil.BORDER_RIGHT, CellStyle.BORDER_THIN);
        CellUtil.setCellStyleProperties(c, props);
        int styCnt2 = wb.getNumCellStyles();
        assertEquals(styCnt2, styCnt1+1);

        // Add same border another to same cell, should not create another style
        c = r.createCell(1);
        CellUtil.setCellStyleProperties(c, props);
        int styCnt3 = wb.getNumCellStyles();
        assertEquals(styCnt3, styCnt2);

        wb.close();
    }
}
