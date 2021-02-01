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

package org.apache.poi.ss.formula.ptg;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * Tests for proper calculation of named ranges from external workbooks.
 */
final class TestExternalNameReference {
	double MARKUP_COST_1 = 1.8d;
	double MARKUP_COST_2 = 1.5d;
	double PART_COST = 12.3d;
	double NEW_QUANT = 7.0d;
	double NEW_PART_COST = 15.3d;
	/**
	 * tests <tt>NameXPtg for external cell reference by name</tt> and logic in Workbook below that
	 */
	@Test
	void testReadCalcSheet() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalc.xls");
		HSSFName name = wb.getName("QUANT");
		assertNotNull(name);
		assertEquals("Sheet1!$A$2", name.getRefersToFormula());
		name = wb.getName("PART");
		assertNotNull(name);
		assertEquals("Sheet1!$B$2", name.getRefersToFormula());
		assertEquals("x123",wb.getSheet("Sheet1").getRow(1).getCell(1).getStringCellValue());
		name = wb.getName("UNITCOST");
		assertNotNull(name);
		assertEquals("Sheet1!$C$2", name.getRefersToFormula());
		name = wb.getName("UNITCOST");
		assertNotNull(name);
		CellReference cellRef = new CellReference(name.getRefersToFormula());
		HSSFCell cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		assertEquals("VLOOKUP(PART,COSTS,2,FALSE)",cell.getCellFormula());
		name = wb.getName("COST");
		assertNotNull(name);
		assertEquals("Sheet1!$D$2", name.getRefersToFormula());
		name = wb.getName("COST");
		assertNotNull(name);
		cellRef = new CellReference(name.getRefersToFormula());
		cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		assertEquals("UNITCOST*Quant",cell.getCellFormula());
		name = wb.getName("TOTALCOST");
		assertNotNull(name);
		assertEquals("Sheet1!$E$2", name.getRefersToFormula());
		name = wb.getName("TOTALCOST");
		assertNotNull(name);
		cellRef = new CellReference(name.getRefersToFormula());
		cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		assertEquals("Cost*Markup_Cost",cell.getCellFormula());
	}

	@Test
	void testReadReferencedSheet() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalcData.xls");
		HSSFName name = wb.getName("COSTS");
		assertNotNull(name);
		assertEquals("CostSheet!$A$2:$B$3", name.getRefersToFormula());
		assertEquals("x123",wb.getSheet("CostSheet").getRow(1).getCell(0).getStringCellValue());
		assertEquals(PART_COST,wb.getSheet("CostSheet").getRow(1).getCell(1).getNumericCellValue(), 0);
		name = wb.getName("Markup_Cost");
		assertNotNull(name);
		assertEquals("MarkupSheet!$B$1", name.getRefersToFormula());
		assertEquals(MARKUP_COST_1,wb.getSheet("MarkupSheet").getRow(0).getCell(1).getNumericCellValue(), 0);
	}

	@Test
	void testEvaluate() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalc.xls");
		HSSFWorkbook wb2 = HSSFTestDataSamples.openSampleWorkbook("XRefCalcData.xls");
		HSSFName name = wb.getName("QUANT");
		assertNotNull(name);
		CellReference cellRef = new CellReference(name.getRefersToFormula());
		HSSFCell cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		cell.setCellValue(NEW_QUANT);
		cell = wb2.getSheet("CostSheet").getRow(1).getCell(1);
		cell.setCellValue(NEW_PART_COST);
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
		HSSFFormulaEvaluator evaluatorCost = new HSSFFormulaEvaluator(wb2);
		String[] bookNames = { "XRefCalc.xls", "XRefCalcData.xls" };
		HSSFFormulaEvaluator[] evaluators = { evaluator, evaluatorCost, };
		HSSFFormulaEvaluator.setupEnvironment(bookNames, evaluators);
		name = wb.getName("UNITCOST");
		assertNotNull(name);
		cellRef = new CellReference(name.getRefersToFormula());
		HSSFCell uccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		name = wb.getName("COST");
		assertNotNull(name);
		cellRef = new CellReference(name.getRefersToFormula());
		HSSFCell ccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		name = wb.getName("TOTALCOST");
		assertNotNull(name);
		cellRef = new CellReference(name.getRefersToFormula());
		HSSFCell tccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell(cellRef.getCol());
		evaluator.evaluateFormulaCell(uccell);
		evaluator.evaluateFormulaCell(ccell);
		evaluator.evaluateFormulaCell(tccell);
		assertEquals(NEW_PART_COST, uccell.getNumericCellValue(), 0);
		assertEquals(NEW_PART_COST*NEW_QUANT, ccell.getNumericCellValue(), 0);
		assertEquals(NEW_PART_COST*NEW_QUANT*MARKUP_COST_2, tccell.getNumericCellValue(), 0);
	}
}
