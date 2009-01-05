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

package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.CellValue;
/**
 * 
 * @author Josh Micich
 */
public final class TestHSSFFormulaEvaluator extends TestCase {

	/**
	 * Test that the HSSFFormulaEvaluator can evaluate simple named ranges
	 *  (single cells and rectangular areas)
	 */
	public void testEvaluateSimple() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testNames.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFCell cell = sheet.getRow(8).getCell(0);
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		CellValue cv = fe.evaluate(cell);
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(3.72, cv.getNumberValue(), 0.0);
	}
	
	public void testFullColumnRefs() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell0 = row.createCell(0);
		cell0.setCellFormula("sum(D:D)");
		HSSFCell cell1 = row.createCell(1);
		cell1.setCellFormula("sum(D:E)");

		// some values in column D
		setValue(sheet, 1, 3, 5.0);
		setValue(sheet, 2, 3, 6.0);
		setValue(sheet, 5, 3, 7.0);
		setValue(sheet, 50, 3, 8.0);
		
		// some values in column E
		setValue(sheet, 1, 4, 9.0);
		setValue(sheet, 2, 4, 10.0);
		setValue(sheet, 30000, 4, 11.0);
		
		// some other values 
		setValue(sheet, 1, 2, 100.0);
		setValue(sheet, 2, 5, 100.0);
		setValue(sheet, 3, 6, 100.0);
		
		
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		assertEquals(26.0, fe.evaluate(cell0).getNumberValue(), 0.0);
		assertEquals(56.0, fe.evaluate(cell1).getNumberValue(), 0.0);
	}

	private static void setValue(HSSFSheet sheet, int rowIndex, int colIndex, double value) {
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		row.createCell(colIndex).setCellValue(value);
	}

	/**
	 * {@link HSSFFormulaEvaluator#evaluate(org.apache.poi.ss.usermodel.Cell)} should behave the same whether the cell
	 * is <code>null</code> or blank.
	 */
	public void testEvaluateBlank() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		assertNull(fe.evaluate(null));
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFCell cell = sheet.createRow(0).createCell(0);
		assertNull(fe.evaluate(cell));
	}

	/**
	 * Test for bug due to attempt to convert a cached formula error result to a boolean
	 */
	public void testUpdateCachedFormulaResultFromErrorToNumber_bug46479() {

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFRow row = sheet.createRow(0);
		HSSFCell cellA1 = row.createCell(0);
		HSSFCell cellB1 = row.createCell(1);
		cellB1.setCellFormula("A1+1");
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

		cellA1.setCellErrorValue((byte)HSSFErrorConstants.ERROR_NAME);
		fe.evaluateFormulaCell(cellB1);

		cellA1.setCellValue(2.5);
		fe.notifyUpdateCell(cellA1);
		try {
			fe.evaluateInCell(cellB1);
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Cannot get a numeric value from a error formula cell")) {
				throw new AssertionFailedError("Identified bug 46479a");
			}
		}
		assertEquals(3.5, cellB1.getNumericCellValue(), 0.0);
	}
}
