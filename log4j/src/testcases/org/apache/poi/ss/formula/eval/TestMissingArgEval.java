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

package org.apache.poi.ss.formula.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MissingArgEval}
 */
final class TestMissingArgEval {

	@Test
	void testEvaluateMissingArgs() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
			HSSFSheet sheet = wb.createSheet("Sheet1");
			HSSFCell cell = sheet.createRow(0).createCell(0);

			cell.setCellFormula("if(true,)");
			fe.clearAllCachedResultValues();
			// EmptyStackException -> Missing args evaluation not implemented (bug 43354)
			CellValue cv = fe.evaluate(cell);
			// MissingArg -> BlankEval -> zero (as formula result)
			assertEquals(0.0, cv.getNumberValue(), 0.0);

			// MissingArg -> BlankEval -> empty string (in concatenation)
			cell.setCellFormula("\"abc\"&if(true,)");
			fe.clearAllCachedResultValues();
			assertEquals("abc", fe.evaluate(cell).getStringValue());
		}
	}

	@Test
	void testCompareMissingArgs() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
			HSSFSheet sheet = wb.createSheet("Sheet1");
			HSSFCell cell = sheet.createRow(0).createCell(0);

			cell.setCellFormula("iferror(0/0,)<0");
			fe.clearAllCachedResultValues();
			CellValue cv = fe.evaluate(cell);
			assertFalse(cv.getBooleanValue());

			cell.setCellFormula("iferror(0/0,)<=0");
			fe.clearAllCachedResultValues();
			cv = fe.evaluate(cell);
			assertTrue(cv.getBooleanValue());

			cell.setCellFormula("iferror(0/0,)=0");
			fe.clearAllCachedResultValues();
			cv = fe.evaluate(cell);
			assertTrue(cv.getBooleanValue());

			cell.setCellFormula("iferror(0/0,)>=0");
			fe.clearAllCachedResultValues();
			cv = fe.evaluate(cell);
			assertTrue(cv.getBooleanValue());

			cell.setCellFormula("iferror(0/0,)>0");
			fe.clearAllCachedResultValues();
			cv = fe.evaluate(cell);
			assertFalse(cv.getBooleanValue());

			// invert above for code coverage
			cell.setCellFormula("0<iferror(0/0,)");
            fe.clearAllCachedResultValues();
            cv = fe.evaluate(cell);
            assertFalse(cv.getBooleanValue());

            cell.setCellFormula("0<=iferror(0/0,)");
            fe.clearAllCachedResultValues();
            cv = fe.evaluate(cell);
            assertTrue(cv.getBooleanValue());

            cell.setCellFormula("0=iferror(0/0,)");
            fe.clearAllCachedResultValues();
            cv = fe.evaluate(cell);
            assertTrue(cv.getBooleanValue());

            cell.setCellFormula("0>=iferror(0/0,)");
            fe.clearAllCachedResultValues();
            cv = fe.evaluate(cell);
            assertTrue(cv.getBooleanValue());

            cell.setCellFormula("0>iferror(0/0,)");
            fe.clearAllCachedResultValues();
            cv = fe.evaluate(cell);
            assertFalse(cv.getBooleanValue());
		}
	}

	@Test
	void testCountFuncs() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
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
}
