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

package org.apache.poi.ss.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.functions.Offset.LinearOffsetRange;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for OFFSET function implementation
 *
 * @author Josh Micich
 */
final class TestOffset {

	/**
	 * Excel's double to int conversion (for function 'OFFSET()') behaves more like Math.floor().
	 * Note - negative values are not symmetrical
	 * Fractional values are silently truncated.
	 * Truncation is toward negative infinity.
	 */
	@ParameterizedTest
	@CsvSource({
		"100.09, 100", "100.01, 100", "100.00, 100", "99.99, 99", "+2.01, +2",
		"+2.00, +2", "+1.99, +1", "+1.01, +1", "+1.00, +1", "+0.99,  0",
		"+0.01,  0", "0.00,  0","-0.01, -1", "-0.99, -1", "-1.00, -1",
		"-1.01, -2", "-1.99, -2", "-2.00, -2", "-2.01, -3"
	})
	void testDoubleConversion(double doubleVal, int expected) throws EvaluationException {
		assertEquals(expected, Offset.evaluateIntArg(new NumberEval(doubleVal), -1, -1));
	}

	@Test
	void testLinearOffsetRange() {
		LinearOffsetRange lor;

		lor = new LinearOffsetRange(3, 2);
		assertEquals(3, lor.getFirstIndex());
		assertEquals(4, lor.getLastIndex());
		lor = lor.normaliseAndTranslate(0); // expected no change
		assertEquals(3, lor.getFirstIndex());
		assertEquals(4, lor.getLastIndex());

		lor = lor.normaliseAndTranslate(5);
		assertEquals(8, lor.getFirstIndex());
		assertEquals(9, lor.getLastIndex());

		// negative length

		lor = new LinearOffsetRange(6, -4).normaliseAndTranslate(0);
		assertEquals(3, lor.getFirstIndex());
		assertEquals(6, lor.getLastIndex());


		// bounds checking
		lor = new LinearOffsetRange(0, 100);
		assertFalse(lor.isOutOfBounds(0, 16383));
		lor = lor.normaliseAndTranslate(16300);
		assertTrue(lor.isOutOfBounds(0, 16383));
		assertFalse(lor.isOutOfBounds(0, 65535));
	}

	@Test
	void testOffsetWithEmpty23Arguments() throws IOException {
		try (Workbook workbook = new HSSFWorkbook()) {
			Cell cell = workbook.createSheet().createRow(0).createCell(0);
			cell.setCellFormula("OFFSET(B1,,)");

			String value = "EXPECTED_VALUE";
			Cell valueCell = cell.getRow().createCell(1);
			valueCell.setCellValue(value);

			workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

			assertEquals(CellType.STRING, cell.getCachedFormulaResultType());
			assertEquals(value, cell.getStringCellValue());
		}
	}
}
