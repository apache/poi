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

package org.apache.poi.hssf.record.formula;


import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
/**
 * Tests for proper calculation of named ranges from external workbooks.
 * 
 * 
 * @author Stephen Wolke (smwolke at geistig.com)
 */
public final class TestExternalNameReference extends TestCase {
	double MARKUP_COST = 1.9d;
	double MARKUP_COST_1 = 1.8d;
	double MARKUP_COST_2 = 1.5d;
	double PART_COST = 12.3d;
	double NEW_QUANT = 7.0d;
	double NEW_PART_COST = 15.3d;
	/**
	 * tests <tt>NameXPtg for external cell reference by name</tt> and logic in Workbook below that   
	 */
	public void testReadCalcSheet() {
	    try{
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalc.xls");
		assertEquals("Sheet1!$A$2", wb.getName("QUANT").getRefersToFormula());
		assertEquals("Sheet1!$B$2", wb.getName("PART").getRefersToFormula());
		assertEquals("x123",wb.getSheet("Sheet1").getRow(1).getCell(1).getStringCellValue());
		assertEquals("Sheet1!$C$2", wb.getName("UNITCOST").getRefersToFormula());
		CellReference cellRef = new CellReference(wb.getName("UNITCOST").getRefersToFormula());
		HSSFCell cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		assertEquals("VLOOKUP(PART,COSTS,2,FALSE)",cell.getCellFormula());
		assertEquals("Sheet1!$D$2", wb.getName("COST").getRefersToFormula());
		cellRef = new CellReference(wb.getName("COST").getRefersToFormula());
		cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		assertEquals("UNITCOST*Quant",cell.getCellFormula());
		assertEquals("Sheet1!$E$2", wb.getName("TOTALCOST").getRefersToFormula());
		cellRef = new CellReference(wb.getName("TOTALCOST").getRefersToFormula());
		cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		assertEquals("Cost*Markup_Cost",cell.getCellFormula());
	    }catch(Exception e){
		fail();
	    }
	}
	
	public void testReadReferencedSheet() {
	    try{
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalcData.xls");
		assertEquals("CostSheet!$A$2:$B$3", wb.getName("COSTS").getRefersToFormula());
		assertEquals("x123",wb.getSheet("CostSheet").getRow(1).getCell(0).getStringCellValue());
		assertEquals(PART_COST,wb.getSheet("CostSheet").getRow(1).getCell(1).getNumericCellValue());
		assertEquals("MarkupSheet!$B$1", wb.getName("Markup_Cost").getRefersToFormula());
		assertEquals(MARKUP_COST_1,wb.getSheet("MarkupSheet").getRow(0).getCell(1).getNumericCellValue());
	    }catch(Exception e){
		fail();
	    }
	}
	
	public void testEvaluate() throws Exception {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("XRefCalc.xls");
		HSSFWorkbook wb2 = HSSFTestDataSamples.openSampleWorkbook("XRefCalcData.xls");
		CellReference cellRef = new CellReference(wb.getName("QUANT").getRefersToFormula());
		HSSFCell cell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		cell.setCellValue(NEW_QUANT);
		cell = wb2.getSheet("CostSheet").getRow(1).getCell(1);
		cell.setCellValue(NEW_PART_COST);
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
		HSSFFormulaEvaluator evaluatorCost = new HSSFFormulaEvaluator(wb2);
		String[] bookNames = { "XRefCalc.xls", "XRefCalcData.xls" };
		HSSFFormulaEvaluator[] evaluators = { evaluator, evaluatorCost, };
		HSSFFormulaEvaluator.setupEnvironment(bookNames, evaluators);
		cellRef = new CellReference(wb.getName("UNITCOST").getRefersToFormula());
		HSSFCell uccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		cellRef = new CellReference(wb.getName("COST").getRefersToFormula());
		HSSFCell ccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		cellRef = new CellReference(wb.getName("TOTALCOST").getRefersToFormula());
		HSSFCell tccell = wb.getSheet(cellRef.getSheetName()).getRow(cellRef.getRow()).getCell((int)cellRef.getCol());
		evaluator.evaluateFormulaCell(uccell);
		evaluator.evaluateFormulaCell(ccell);
		evaluator.evaluateFormulaCell(tccell);
		assertEquals(NEW_PART_COST, uccell.getNumericCellValue());
		assertEquals(NEW_PART_COST*NEW_QUANT, ccell.getNumericCellValue());
		assertEquals(NEW_PART_COST*NEW_QUANT*MARKUP_COST_2, tccell.getNumericCellValue());
	}
}
