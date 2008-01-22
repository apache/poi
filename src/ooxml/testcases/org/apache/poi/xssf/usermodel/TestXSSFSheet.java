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
	
	public void testRowIterator() throws Exception {
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
	
	public void testGetRow() throws Exception {
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
	
	public void testCreateRow() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet 1");
		
		// Test row creation with consecutive indexes
		Row row1 = sheet.createRow(0);
		Row row2 = sheet.createRow(1);
		assertEquals(0, row1.getRowNum());
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
	
	public void testGetSetDefaultRowHeight() throws Exception {
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
	
	public void testGetSetDefaultColumnWidth() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet 1");
		// Test that default column width set by the constructor
		assertEquals((short) 13, sheet.getDefaultColumnWidth());
		// Set a new default column width and get its value
		sheet.setDefaultColumnWidth((short) 14);
		assertEquals((short) 14, sheet.getDefaultColumnWidth());
	}
}
