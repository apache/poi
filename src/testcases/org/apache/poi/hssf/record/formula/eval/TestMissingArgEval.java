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

import java.util.EmptyStackException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;

/**
 * Tests for {@link MissingArgEval}
 *
 * @author Josh Micich
 */
public final class TestMissingArgEval extends TestCase {
	
	public void testEvaluateMissingArgs() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFCell cell = sheet.createRow(0).createCell(0);
		
		cell.setCellFormula("if(true,)"); 
		fe.clearAllCachedResultValues();
		CellValue cv;
		try {
			cv = fe.evaluate(cell);
		} catch (EmptyStackException e) {
			throw new AssertionFailedError("Missing args evaluation not implemented (bug 43354");
		}
		// MissingArg -> BlankEval -> zero (as formula result)
		assertEquals(0.0, cv.getNumberValue(), 0.0);
		
		// MissingArg -> BlankEval -> empty string (in concatenation)
		cell.setCellFormula("\"abc\"&if(true,)"); 
		fe.clearAllCachedResultValues();
		assertEquals("abc", fe.evaluate(cell).getStringValue());
	}
	
	public void testCountFuncs() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		HSSFSheet sheet = wb.createSheet("Sheet1");
		HSSFCell cell = sheet.createRow(0).createCell(0);
		
		cell.setCellFormula("COUNT(C5,,,,)"); // 4 missing args, C5 is blank 
		assertEquals(4.0, fe.evaluate(cell).getNumberValue(), 0.0);

		cell.setCellFormula("COUNTA(C5,,)"); // 2 missing args, C5 is blank 
		fe.clearAllCachedResultValues();
		assertEquals(2.0, fe.evaluate(cell).getNumberValue(), 0.0);
	}
}
