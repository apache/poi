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

package org.apache.poi.hssf.model;

import junit.framework.TestCase;

import org.apache.poi.hssf.model.FormulaParser.FormulaParseException;
import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator.CellValue;

/**
 * Test the low level formula parser functionality,
 *  but using parts which need to use 
 *  HSSFFormulaEvaluator.
 */
public final class TestFormulaParserEval extends TestCase {

	public void testWithNamedRange() {
		HSSFWorkbook workbook = new HSSFWorkbook();
		Ptg[] ptgs;

		HSSFSheet s = workbook.createSheet("Foo");
		s.createRow(0).createCell(0).setCellValue(1.1);
		s.createRow(1).createCell(0).setCellValue(2.3);
		s.createRow(2).createCell(2).setCellValue(3.1);

		HSSFName name = workbook.createName();
		name.setNameName("testName");
		name.setReference("A1:A2");

		ptgs = FormulaParser.parse("SUM(testName)", workbook);
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());

		// Now make it a single cell
		name.setReference("C3");
		ptgs = FormulaParser.parse("SUM(testName)", workbook);
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());
		
		// And make it non-contiguous
		name.setReference("A1:A2,C3");
		ptgs = FormulaParser.parse("SUM(testName)", workbook);
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());
	}

	public void testEvaluateFormulaWithRowBeyond32768_Bug44539() {
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		wb.setSheetName(0, "Sheet1");
		
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellFormula("SUM(A32769:A32770)");

		// put some values in the cells to make the evaluation more interesting
		sheet.createRow(32768).createCell(0).setCellValue(31);
		sheet.createRow(32769).createCell(0).setCellValue(11);
		
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(sheet, wb);
		CellValue result;
		try {
			result = fe.evaluate(cell);
		} catch (FormulaParseException e) {
			if(e.getMessage().equals("Found reference to named range \"A\", but that named range wasn't defined!")) {
				fail("Identifed bug 44539");
			}
			throw new RuntimeException(e);
		}
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, result.getCellType());
		assertEquals(42.0, result.getNumberValue(), 0.0);
	}
}
