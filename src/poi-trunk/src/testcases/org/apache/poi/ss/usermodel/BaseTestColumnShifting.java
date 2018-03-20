/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.ss.usermodel;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.poi.ss.usermodel.helpers.ColumnShifter;

public abstract class BaseTestColumnShifting {
    protected Workbook wb;
    protected Sheet sheet1;
    protected ColumnShifter columnShifter;

    @Before
    public void init() {
        int rowIndex = 0;
        sheet1 = wb.createSheet("sheet1");
        Row row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.NUMERIC).setCellValue(0);
        row.createCell(3, CellType.NUMERIC).setCellValue(3);
        row.createCell(4, CellType.NUMERIC).setCellValue(4);

        row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.NUMERIC).setCellValue(0.1);
        row.createCell(1, CellType.NUMERIC).setCellValue(1.1);
        row.createCell(2, CellType.NUMERIC).setCellValue(2.1);
        row.createCell(3, CellType.NUMERIC).setCellValue(3.1);
        row.createCell(4, CellType.NUMERIC).setCellValue(4.1);
        row.createCell(5, CellType.NUMERIC).setCellValue(5.1);
        row.createCell(6, CellType.NUMERIC).setCellValue(6.1);
        row.createCell(7, CellType.NUMERIC).setCellValue(7.1);
        row = sheet1.createRow(rowIndex++);
        row.createCell(3, CellType.NUMERIC).setCellValue(3.2);
        row.createCell(5, CellType.NUMERIC).setCellValue(5.2);
        row.createCell(7, CellType.NUMERIC).setCellValue(7.2);

        initColumnShifter();
    }

    protected void initColumnShifter() {}

    @Test
    public void testShift3ColumnsRight() {
        columnShifter.shiftColumns(1, 2, 3);
        
        Cell cell = sheet1.getRow(0).getCell(4);
        assertNull(cell);
        cell = sheet1.getRow(1).getCell(4);
        assertEquals(1.1, cell.getNumericCellValue(), 0.01);
        cell = sheet1.getRow(1).getCell(5);
        assertEquals(2.1, cell.getNumericCellValue(), 0.01);
        cell = sheet1.getRow(2).getCell(4);
        assertNull(cell);
    }

    @Test
    public void testShiftLeft() {
        try {
            columnShifter.shiftColumns(1, 2, -3);
            assertTrue("Shift to negative indices should throw exception", false);
        }
        catch(IllegalStateException e){
            assertTrue(true);
        }
    }
}
