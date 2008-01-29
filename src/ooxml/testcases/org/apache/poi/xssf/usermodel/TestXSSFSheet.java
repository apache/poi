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

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import junit.framework.TestCase;


public class TestXSSFSheet extends TestCase {
    
    public void testRowIterator() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        Iterator<Row> it = sheet.rowIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals(row1, it.next());
        assertTrue(it.hasNext());
        assertEquals(row2, it.next());
        assertFalse(it.hasNext());
    }
    
    public void testGetRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(0);
        Cell cell = row1.createCell((short) 0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue((double) 1000);
        
        // Test getting a row and check its cell's value
        Row row_got = sheet.getRow(0);
        Cell cell_got = row_got.getCell((short) 0);
        assertEquals((double) 1000, cell_got.getNumericCellValue());
    }
    
    public void testCreateRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        
        // Test row creation with consecutive indexes
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(0, row1.getRowNum());
        assertEquals(1, row2.getRowNum());
        Iterator<Row> it = sheet.rowIterator();
        assertTrue(it.hasNext());
        assertEquals(row1, it.next());
        assertTrue(it.hasNext());
        assertEquals(row2, it.next());
        
        // Test row creation with non consecutive index
        Row row101 = sheet.createRow(100);
        assertNotNull(row101);
        
        // Test overwriting an existing row
        Row row2_ovrewritten = sheet.createRow(1);
        Cell cell = row2_ovrewritten.createCell((short) 0);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue((double) 100);
        Iterator<Row> it2 = sheet.rowIterator();
        assertTrue(it2.hasNext());
        assertEquals(row1, it2.next());
        assertTrue(it2.hasNext());
        Row row2_overwritten_copy = it2.next();
        assertEquals(row2_ovrewritten, row2_overwritten_copy);
        assertEquals(row2_overwritten_copy.getCell((short) 0).getNumericCellValue(), (double) 100);
    }
    
    public void testRemoveRow() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        assertNotNull(sheet.getRow(1));
        sheet.removeRow(row2);
        assertNull(sheet.getRow(0));
        assertNull(sheet.getRow(2));
        assertNotNull(sheet.getRow(1));
    }
    
    public void testGetSetDefaultRowHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Test that default height set by the constructor
        assertEquals((short) 300, sheet.getDefaultRowHeight());
        assertEquals((float) 15, sheet.getDefaultRowHeightInPoints());
        // Set a new default row height in twips and test getting the value in points
        sheet.setDefaultRowHeight((short) 360);
        assertEquals((float) 18, sheet.getDefaultRowHeightInPoints());
        // Set a new default row height in points and test getting the value in twips
        sheet.setDefaultRowHeightInPoints((short) 17);
        assertEquals((short) 340, sheet.getDefaultRowHeight());
    }
    
    public void testGetSetDefaultColumnWidth() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Test that default column width set by the constructor
        assertEquals((short) 13, sheet.getDefaultColumnWidth());
        // Set a new default column width and get its value
        sheet.setDefaultColumnWidth((short) 14);
        assertEquals((short) 14, sheet.getDefaultColumnWidth());
    }
    
    public void testGetSetColumnWidth() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Test setting a column width and getting that value
        sheet.setColumnWidth((short) 0, (short) 16);
        assertEquals(16, sheet.getColumnWidth((short) 0));
    }
    
    public void testGetFirstLastRowNum() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row10 = sheet.createRow(9);
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(9, sheet.getLastRowNum());    
    }
    
    public void testGetPhysicalNumberOfRows() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        Row row10 = sheet.createRow(9);
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        assertEquals(3, sheet.getPhysicalNumberOfRows());
    }
    
    public void testGetSetRowBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertNull(sheet.getRowBreaks());
        sheet.setRowBreak(1);
        sheet.setRowBreak(15);
        assertNotNull(sheet.getRowBreaks());
        assertEquals(1, sheet.getRowBreaks()[0]);
        assertEquals(15, sheet.getRowBreaks()[1]);
        sheet.setRowBreak(1);
        assertEquals(2, sheet.getRowBreaks().length);
    }
    
    public void testRemoveRowBreak() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
        sheet.setRowBreak(2);
        assertEquals(2, sheet.getRowBreaks().length);
        sheet.removeRowBreak(1);
        assertEquals(1, sheet.getRowBreaks().length);
    }
    
    public void testGetSetColumnBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertNull(sheet.getColumnBreaks());
        sheet.setColumnBreak((short) 11);
        assertNotNull(sheet.getColumnBreaks());
        assertEquals(11, sheet.getColumnBreaks()[0]);
        sheet.setColumnBreak((short) 11223);
        assertEquals(2, sheet.getColumnBreaks().length);
    }
    
    public void testRemoveColumnBreak() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertNull(sheet.getColumnBreaks());
        sheet.setColumnBreak((short) 11);
        assertNotNull(sheet.getColumnBreaks());
        sheet.setColumnBreak((short) 12);
        assertEquals(2, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 11);
        assertEquals(1, sheet.getColumnBreaks().length);
        sheet.removeColumnBreak((short) 15);
        assertEquals(1, sheet.getColumnBreaks().length);
    }
    
    public void testIsRowColumnBroken() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertFalse(sheet.isRowBroken(0));
        sheet.setRowBreak(3);
        assertTrue(sheet.isRowBroken(3));
        assertFalse(sheet.isColumnBroken((short) 0));
        sheet.setColumnBreak((short) 3);
        assertTrue(sheet.isColumnBroken((short) 3));
    }
    
    public void testGetSetAutoBreaks() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertTrue(sheet.getAutobreaks());
        sheet.setAutobreaks(false);
        assertFalse(sheet.getAutobreaks());
    }
    
    public void testIsSetColumnHidden() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        sheet.setColumnWidth((short) 0, (short) 13);
        sheet.setColumnHidden((short) 1, false);
        sheet.setColumnHidden((short) 2, true);
        assertFalse(sheet.isColumnHidden((short) 0));
        assertFalse(sheet.isColumnHidden((short) 1));
        assertTrue(sheet.isColumnHidden((short) 2));
    }
    
    public void testIsSetFitToPage() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertFalse(sheet.getFitToPage());
        sheet.setFitToPage(true);
        assertTrue(sheet.getFitToPage());
        sheet.setFitToPage(false);
        assertFalse(sheet.getFitToPage());
    }
    
    public void testGetSetMargin() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet 1");
        assertEquals((double) 0, sheet.getMargin((short) 0));
        sheet.setMargin((short) 0, 10);
        sheet.setMargin((short) 1, 11);
        sheet.setMargin((short) 2, 12);
        sheet.setMargin((short) 3, 13);
        sheet.setMargin((short) 4, 14);
        sheet.setMargin((short) 5, 15);
        assertEquals((double) 10, sheet.getMargin((short) 0));
        assertEquals((double) 11, sheet.getMargin((short) 1));
        assertEquals((double) 12, sheet.getMargin((short) 2));
        assertEquals((double) 13, sheet.getMargin((short) 3));
        assertEquals((double) 14, sheet.getMargin((short) 4));
        assertEquals((double) 15, sheet.getMargin((short) 5));
        
        // Test that nothing happens if another margin constant is given (E.G. 65)
        sheet.setMargin((short) 65, 15);
        assertEquals((double) 10, sheet.getMargin((short) 0));
        assertEquals((double) 11, sheet.getMargin((short) 1));
        assertEquals((double) 12, sheet.getMargin((short) 2));
        assertEquals((double) 13, sheet.getMargin((short) 3));
        assertEquals((double) 14, sheet.getMargin((short) 4));
        assertEquals((double) 15, sheet.getMargin((short) 5));
    }
}
