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
package org.apache.poi.xssf;

import junit.framework.TestCase;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestSheetProtection extends TestCase {
	private XSSFSheet sheet;
	
	@Override
	protected void setUp() throws Exception {
		XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("sheetProtection_not_protected.xlsx");
		sheet = workbook.getSheetAt(0);
	}
	
	public void testShouldReadWorkbookProtection() throws Exception {
		assertFalse(sheet.isAutoFilterLocked());
		assertFalse(sheet.isDeleteColumnsLocked());
		assertFalse(sheet.isDeleteRowsLocked());
		assertFalse(sheet.isFormatCellsLocked());
		assertFalse(sheet.isFormatColumnsLocked());
		assertFalse(sheet.isFormatRowsLocked());
		assertFalse(sheet.isInsertColumnsLocked());
		assertFalse(sheet.isInsertHyperlinksLocked());
		assertFalse(sheet.isInsertRowsLocked());
		assertFalse(sheet.isPivotTablesLocked());
		assertFalse(sheet.isSortLocked());
		assertFalse(sheet.isObjectsLocked());
		assertFalse(sheet.isScenariosLocked());
		assertFalse(sheet.isSelectLockedCellsLocked());
		assertFalse(sheet.isSelectUnlockedCellsLocked());
		assertFalse(sheet.isSheetLocked());

		sheet = XSSFTestDataSamples.openSampleWorkbook("sheetProtection_allLocked.xlsx").getSheetAt(0);

		assertTrue(sheet.isAutoFilterLocked());
		assertTrue(sheet.isDeleteColumnsLocked());
		assertTrue(sheet.isDeleteRowsLocked());
		assertTrue(sheet.isFormatCellsLocked());
		assertTrue(sheet.isFormatColumnsLocked());
		assertTrue(sheet.isFormatRowsLocked());
		assertTrue(sheet.isInsertColumnsLocked());
		assertTrue(sheet.isInsertHyperlinksLocked());
		assertTrue(sheet.isInsertRowsLocked());
		assertTrue(sheet.isPivotTablesLocked());
		assertTrue(sheet.isSortLocked());
		assertTrue(sheet.isObjectsLocked());
		assertTrue(sheet.isScenariosLocked());
		assertTrue(sheet.isSelectLockedCellsLocked());
		assertTrue(sheet.isSelectUnlockedCellsLocked());
		assertTrue(sheet.isSheetLocked());
	}
	
	public void testWriteAutoFilter() throws Exception {
		assertFalse(sheet.isAutoFilterLocked());
		sheet.lockAutoFilter(true);
		assertFalse(sheet.isAutoFilterLocked());
		sheet.enableLocking();
		assertTrue(sheet.isAutoFilterLocked());
		sheet.lockAutoFilter(false);
		assertFalse(sheet.isAutoFilterLocked());
	}
	
	public void testWriteDeleteColumns() throws Exception {
		assertFalse(sheet.isDeleteColumnsLocked());
		sheet.lockDeleteColumns(true);
		assertFalse(sheet.isDeleteColumnsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isDeleteColumnsLocked());
		sheet.lockDeleteColumns(false);
		assertFalse(sheet.isDeleteColumnsLocked());
	}
	
	public void testWriteDeleteRows() throws Exception {
		assertFalse(sheet.isDeleteRowsLocked());
		sheet.lockDeleteRows(true);
		assertFalse(sheet.isDeleteRowsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isDeleteRowsLocked());
        sheet.lockDeleteRows(false);
        assertFalse(sheet.isDeleteRowsLocked());
	}
	
	public void testWriteFormatCells() throws Exception {
		assertFalse(sheet.isFormatCellsLocked());
		sheet.lockFormatCells(true);
		assertFalse(sheet.isFormatCellsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isFormatCellsLocked());
        sheet.lockFormatCells(false);
        assertFalse(sheet.isFormatCellsLocked());
	}
	
	public void testWriteFormatColumns() throws Exception {
		assertFalse(sheet.isFormatColumnsLocked());
		sheet.lockFormatColumns(true);
		assertFalse(sheet.isFormatColumnsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isFormatColumnsLocked());
        sheet.lockFormatColumns(false);
        assertFalse(sheet.isFormatColumnsLocked());
	}
	
	public void testWriteFormatRows() throws Exception {
		assertFalse(sheet.isFormatRowsLocked());
		sheet.lockFormatRows(true);
		assertFalse(sheet.isFormatRowsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isFormatRowsLocked());
        sheet.lockFormatRows(false);
        assertFalse(sheet.isFormatRowsLocked());
	}
	
	public void testWriteInsertColumns() throws Exception {
		assertFalse(sheet.isInsertColumnsLocked());
		sheet.lockInsertColumns(true);
		assertFalse(sheet.isInsertColumnsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isInsertColumnsLocked());
        sheet.lockInsertColumns(false);
        assertFalse(sheet.isInsertColumnsLocked());
	}
	
	public void testWriteInsertHyperlinks() throws Exception {
		assertFalse(sheet.isInsertHyperlinksLocked());
		sheet.lockInsertHyperlinks(true);
		assertFalse(sheet.isInsertHyperlinksLocked());
		sheet.enableLocking();
		assertTrue(sheet.isInsertHyperlinksLocked());
        sheet.lockInsertHyperlinks(false);
        assertFalse(sheet.isInsertHyperlinksLocked());
	}
	
	public void testWriteInsertRows() throws Exception {
		assertFalse(sheet.isInsertRowsLocked());
		sheet.lockInsertRows(true);
		assertFalse(sheet.isInsertRowsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isInsertRowsLocked());
        sheet.lockInsertRows(false);
        assertFalse(sheet.isInsertRowsLocked());
	}
	
	public void testWritePivotTables() throws Exception {
		assertFalse(sheet.isPivotTablesLocked());
		sheet.lockPivotTables(true);
		assertFalse(sheet.isPivotTablesLocked());
		sheet.enableLocking();
		assertTrue(sheet.isPivotTablesLocked());
        sheet.lockPivotTables(false);
        assertFalse(sheet.isPivotTablesLocked());
	}
	
	public void testWriteSort() throws Exception {
		assertFalse(sheet.isSortLocked());
		sheet.lockSort(true);
		assertFalse(sheet.isSortLocked());
		sheet.enableLocking();
		assertTrue(sheet.isSortLocked());
        sheet.lockSort(false);
        assertFalse(sheet.isSortLocked());
	}
	
	public void testWriteObjects() throws Exception {
		assertFalse(sheet.isObjectsLocked());
		sheet.lockObjects(true);
		assertFalse(sheet.isObjectsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isObjectsLocked());
        sheet.lockObjects(false);
        assertFalse(sheet.isObjectsLocked());
	}
	
	public void testWriteScenarios() throws Exception {
		assertFalse(sheet.isScenariosLocked());
		sheet.lockScenarios(true);
		assertFalse(sheet.isScenariosLocked());
		sheet.enableLocking();
		assertTrue(sheet.isScenariosLocked());
        sheet.lockScenarios(false);
        assertFalse(sheet.isScenariosLocked());
	}
	
	public void testWriteSelectLockedCells() throws Exception {
		assertFalse(sheet.isSelectLockedCellsLocked());
		sheet.lockSelectLockedCells(true);
		assertFalse(sheet.isSelectLockedCellsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isSelectLockedCellsLocked());
        sheet.lockSelectLockedCells(false);
        assertFalse(sheet.isSelectLockedCellsLocked());
	}
	
	public void testWriteSelectUnlockedCells() throws Exception {
		assertFalse(sheet.isSelectUnlockedCellsLocked());
		sheet.lockSelectUnlockedCells(true);
		assertFalse(sheet.isSelectUnlockedCellsLocked());
		sheet.enableLocking();
		assertTrue(sheet.isSelectUnlockedCellsLocked());
        sheet.lockSelectUnlockedCells(false);
        assertFalse(sheet.isSelectUnlockedCellsLocked());
	}

	public void testWriteSelectEnableLocking() throws Exception {
		sheet = XSSFTestDataSamples.openSampleWorkbook("sheetProtection_allLocked.xlsx").getSheetAt(0);
		
		assertTrue(sheet.isAutoFilterLocked());
		assertTrue(sheet.isDeleteColumnsLocked());
		assertTrue(sheet.isDeleteRowsLocked());
		assertTrue(sheet.isFormatCellsLocked());
		assertTrue(sheet.isFormatColumnsLocked());
		assertTrue(sheet.isFormatRowsLocked());
		assertTrue(sheet.isInsertColumnsLocked());
		assertTrue(sheet.isInsertHyperlinksLocked());
		assertTrue(sheet.isInsertRowsLocked());
		assertTrue(sheet.isPivotTablesLocked());
		assertTrue(sheet.isSortLocked());
		assertTrue(sheet.isObjectsLocked());
		assertTrue(sheet.isScenariosLocked());
		assertTrue(sheet.isSelectLockedCellsLocked());
		assertTrue(sheet.isSelectUnlockedCellsLocked());
		assertTrue(sheet.isSheetLocked());
		
		sheet.disableLocking();
		
		assertFalse(sheet.isAutoFilterLocked());
		assertFalse(sheet.isDeleteColumnsLocked());
		assertFalse(sheet.isDeleteRowsLocked());
		assertFalse(sheet.isFormatCellsLocked());
		assertFalse(sheet.isFormatColumnsLocked());
		assertFalse(sheet.isFormatRowsLocked());
		assertFalse(sheet.isInsertColumnsLocked());
		assertFalse(sheet.isInsertHyperlinksLocked());
		assertFalse(sheet.isInsertRowsLocked());
		assertFalse(sheet.isPivotTablesLocked());
		assertFalse(sheet.isSortLocked());
		assertFalse(sheet.isObjectsLocked());
		assertFalse(sheet.isScenariosLocked());
		assertFalse(sheet.isSelectLockedCellsLocked());
		assertFalse(sheet.isSelectUnlockedCellsLocked());
		assertFalse(sheet.isSheetLocked());
	}
}
