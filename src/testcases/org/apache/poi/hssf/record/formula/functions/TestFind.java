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

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Tests for {@link Financed}
 * 
 * @author Torstein Svendsen (torstei@officenet.no)
 */
public final class TestFind extends TestCase {

	public void testFind() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCell cell = wb.createSheet().createRow(0).createCell(0);

		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

		confirmResult(fe, cell, "find(\"h\", \"haystack\")", 1);
		confirmResult(fe, cell, "find(\"a\", \"haystack\",2)", 2);
		confirmResult(fe, cell, "find(\"a\", \"haystack\",3)", 6);

		// number args converted to text
		confirmResult(fe, cell, "find(7, 32768)", 3);
		confirmResult(fe, cell, "find(\"34\", 1341235233412, 3)", 10);
		confirmResult(fe, cell, "find(5, 87654)", 4);

		// Errors
		confirmError(fe, cell, "find(\"n\", \"haystack\")", HSSFErrorConstants.ERROR_VALUE);
		confirmError(fe, cell, "find(\"k\", \"haystack\",9)", HSSFErrorConstants.ERROR_VALUE);
		confirmError(fe, cell, "find(\"k\", \"haystack\",#REF!)", HSSFErrorConstants.ERROR_REF);
		confirmError(fe, cell, "find(\"k\", \"haystack\",0)", HSSFErrorConstants.ERROR_VALUE);
		confirmError(fe, cell, "find(#DIV/0!, #N/A, #REF!)", HSSFErrorConstants.ERROR_DIV_0);
		confirmError(fe, cell, "find(2, #N/A, #REF!)", HSSFErrorConstants.ERROR_NA);
	}

	private static void confirmResult(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,
			int expectedResult) {
		cell.setCellFormula(formulaText);
		fe.notifyUpdateCell(cell);
		CellValue result = fe.evaluate(cell);
		assertEquals(result.getCellType(), HSSFCell.CELL_TYPE_NUMERIC);
		assertEquals(expectedResult, result.getNumberValue(), 0.0);
	}

	private static void confirmError(HSSFFormulaEvaluator fe, HSSFCell cell, String formulaText,
			int expectedErrorCode) {
		cell.setCellFormula(formulaText);
		fe.notifyUpdateCell(cell);
		CellValue result = fe.evaluate(cell);
		assertEquals(result.getCellType(), HSSFCell.CELL_TYPE_ERROR);
		assertEquals(expectedErrorCode, result.getErrorValue());
	}
}
