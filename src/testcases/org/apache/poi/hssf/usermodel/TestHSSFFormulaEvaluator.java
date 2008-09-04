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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator.CellValue;

import junit.framework.TestCase;
/**
 * 
 * @author Josh Micich
 */
public final class TestHSSFFormulaEvaluator extends TestCase {

	/**
	 * Test that the HSSFFormulaEvaluator can evaluate simple named ranges
	 *  (single cells and rectangular areas)
	 */
	public void testEvaluateSimple() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testNames.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFCell cell = sheet.getRow(8).getCell(0);
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(sheet, wb);
		CellValue cv = fe.evaluate(cell);
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cv.getCellType());
		assertEquals(3.72, cv.getNumberValue(), 0.0);
	}
}
