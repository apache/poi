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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

/**
 * Tests to show that our documentation at
 *  https://poi.apache.org/hssf/eval.html
 * all actually works as we'd expect them to
 */
final class TestFormulaEvaluatorDocs {

	/**
	 * https://poi.apache.org/hssf/eval.html#EvaluateAll
	 */
	@Test
	void testEvaluateAll() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet s1 = wb.createSheet();
			HSSFSheet s2 = wb.createSheet();
			wb.setSheetName(0, "S1");
			wb.setSheetName(1, "S2");

			HSSFRow s1r1 = s1.createRow(0);
			HSSFRow s1r2 = s1.createRow(1);
			HSSFRow s2r1 = s2.createRow(0);

			HSSFCell s1r1c1 = s1r1.createCell(0);
			HSSFCell s1r1c2 = s1r1.createCell(1);
			HSSFCell s1r1c3 = s1r1.createCell(2);
			s1r1c1.setCellValue(22.3);
			s1r1c2.setCellValue(33.4);
			s1r1c3.setCellFormula("SUM(A1:B1)");

			HSSFCell s1r2c1 = s1r2.createCell(0);
			HSSFCell s1r2c2 = s1r2.createCell(1);
			HSSFCell s1r2c3 = s1r2.createCell(2);
			s1r2c1.setCellValue(-1.2);
			s1r2c2.setCellValue(-3.4);
			s1r2c3.setCellFormula("SUM(A2:B2)");

			HSSFCell s2r1c1 = s2r1.createCell(0);
			s2r1c1.setCellFormula("S1!A1");

			// Not evaluated yet
			assertEquals(0.0, s1r1c3.getNumericCellValue(), 0);
			assertEquals(0.0, s1r2c3.getNumericCellValue(), 0);
			assertEquals(0.0, s2r1c1.getNumericCellValue(), 0);

			// Do a full evaluate, as per our docs
			// uses evaluateFormulaCell()
			for (Sheet sheet : wb) {
				HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
				for (Row r : sheet) {
					for (Cell c : r) {
						if (c.getCellType() == CellType.FORMULA) {
							evaluator.evaluateFormulaCell(c);

							// For testing - all should be numeric
							assertEquals(CellType.NUMERIC, evaluator.evaluateFormulaCell(c));
						}
					}
				}
			}

			// Check now as expected
			assertEquals(55.7, wb.getSheetAt(0).getRow(0).getCell(2).getNumericCellValue(), 0);
			assertEquals("SUM(A1:B1)", wb.getSheetAt(0).getRow(0).getCell(2).getCellFormula());
			assertEquals(CellType.FORMULA, wb.getSheetAt(0).getRow(0).getCell(2).getCellType());

			assertEquals(-4.6, wb.getSheetAt(0).getRow(1).getCell(2).getNumericCellValue(), 0);
			assertEquals("SUM(A2:B2)", wb.getSheetAt(0).getRow(1).getCell(2).getCellFormula());
			assertEquals(CellType.FORMULA, wb.getSheetAt(0).getRow(1).getCell(2).getCellType());

			assertEquals(22.3, wb.getSheetAt(1).getRow(0).getCell(0).getNumericCellValue(), 0);
			assertEquals("'S1'!A1", wb.getSheetAt(1).getRow(0).getCell(0).getCellFormula());
			assertEquals(CellType.FORMULA, wb.getSheetAt(1).getRow(0).getCell(0).getCellType());


			// Now do the alternate call, which zaps the formulas
			// uses evaluateInCell()
			for (Sheet sheet : wb) {
				HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);

				for (Row r : sheet) {
					for (Cell c : r) {
						if (c.getCellType() == CellType.FORMULA) {
							evaluator.evaluateInCell(c);
						}
					}
				}
			}

			assertEquals(55.7, wb.getSheetAt(0).getRow(0).getCell(2).getNumericCellValue(), 0);
			assertEquals(CellType.NUMERIC, wb.getSheetAt(0).getRow(0).getCell(2).getCellType());

			assertEquals(-4.6, wb.getSheetAt(0).getRow(1).getCell(2).getNumericCellValue(), 0);
			assertEquals(CellType.NUMERIC, wb.getSheetAt(0).getRow(1).getCell(2).getCellType());

			assertEquals(22.3, wb.getSheetAt(1).getRow(0).getCell(0).getNumericCellValue(), 0);
			assertEquals(CellType.NUMERIC, wb.getSheetAt(1).getRow(0).getCell(0).getCellType());
		}
	}
}
