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

package org.apache.poi.hssf.record.formula.eval;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator.CellValue;
/**
 * 
 * @author Josh Micich
 */
public final class TestExternalFunction extends TestCase {

	/**
	 * Checks that an external function can get invoked from the formula evaluator. 
	 */
	public void testInvoke() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);

        HSSFName hssfName = wb.createName();
        hssfName.setNameName("myFunc");
        
        cell.setCellFormula("myFunc()");
        String actualFormula=cell.getCellFormula();
        assertEquals("myFunc()", actualFormula);
		
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(sheet, wb);
		CellValue evalResult = fe.evaluate(cell);
		
		// Check the return value from ExternalFunction.evaluate()
		// TODO - make this test assert something more interesting as soon as ExternalFunction works a bit better
		assertEquals(HSSFCell.CELL_TYPE_ERROR, evalResult.getCellType());
		assertEquals(ErrorEval.FUNCTION_NOT_IMPLEMENTED.getErrorCode(), evalResult.getErrorValue());
	}
}
