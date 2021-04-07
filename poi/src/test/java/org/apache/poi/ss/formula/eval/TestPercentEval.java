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

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.EvalFactory;
import org.apache.poi.ss.formula.functions.NumericFunctionInvoker;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.jupiter.api.Test;

/**
 * Test for percent operator evaluator.
 */
final class TestPercentEval {

	private static void confirm(ValueEval arg, double expectedResult) {
		ValueEval[] args = {
			arg,
		};

		double result = NumericFunctionInvoker.invoke(PercentEval.instance, args, 0, 0);

		assertEquals(expectedResult, result, 0);
	}

	@Test
	void testBasic() {
		confirm(new NumberEval(5), 0.05);
		confirm(new NumberEval(3000), 30.0);
		confirm(new NumberEval(-150), -1.5);
		confirm(new StringEval("0.2"), 0.002);
		confirm(BoolEval.TRUE, 0.01);
	}

	@Test
	void test1x1Area() {
		AreaEval ae = EvalFactory.createAreaEval("B2:B2", new ValueEval[] { new NumberEval(50), });
		confirm(ae, 0.5);
	}

	@Test
	void testInSpreadSheet() throws IOException {
		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet sheet = wb.createSheet("Sheet1");
			HSSFRow row = sheet.createRow(0);
			HSSFCell cell = row.createCell(0);
			cell.setCellFormula("B1%");
			row.createCell(1).setCellValue(50.0);

			HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

			// bug 44608 - NullPointerException
			CellValue cv = fe.evaluate(cell);
			assertEquals(CellType.NUMERIC, cv.getCellType());
			assertEquals(0.5, cv.getNumberValue(), 0.0);
		}
	}
}
