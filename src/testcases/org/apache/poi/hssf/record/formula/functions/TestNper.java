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

import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests for {@link FinanceFunction#NPER}
 *
 * @author Josh Micich
 */
public final class TestNper extends TestCase {
	public void testSimpleEvaluate() {

		ValueEval[] args = {
			new NumberEval(0.05),
			new NumberEval(250),
			new NumberEval(-1000),
		};
		ValueEval result = FinanceFunction.NPER.evaluate(args, 0, (short)0);

		assertEquals(NumberEval.class, result.getClass());
		assertEquals(4.57353557, ((NumberEval)result).getNumberValue(), 0.00000001);
	}

	public void testEvaluate_bug_45732() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFCell cell = sheet.createRow(0).createCell(0);

		cell.setCellFormula("NPER(12,4500,100000,100000)");
		cell.setCellValue(15.0);
		assertEquals("NPER(12,4500,100000,100000)", cell.getCellFormula());
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cell.getCachedFormulaResultType());
		assertEquals(15.0, cell.getNumericCellValue(), 0.0);

		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		fe.evaluateFormulaCell(cell);
		assertEquals(HSSFCell.CELL_TYPE_ERROR, cell.getCachedFormulaResultType());
		assertEquals(HSSFErrorConstants.ERROR_NUM, cell.getErrorCellValue());
	}
}
