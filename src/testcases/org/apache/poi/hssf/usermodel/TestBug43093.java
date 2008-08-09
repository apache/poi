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

import junit.framework.TestCase;

/**
 * 
 */
public final class TestBug43093 extends TestCase {

	private static void addNewSheetWithCellsA1toD4(HSSFWorkbook book, int sheet) {
		
		HSSFSheet sht = book .createSheet("s" + sheet);
		for (int r=0; r < 4; r++) {
			
			HSSFRow   row = sht.createRow (r);
			for (int c=0; c < 4; c++) {
			
				HSSFCell cel = row.createCell(c);
				cel.setCellValue(sheet*100 + r*10 + c);
			}
		}
	}

	public void testBug43093() {
		HSSFWorkbook     xlw    = new HSSFWorkbook();

		addNewSheetWithCellsA1toD4(xlw, 1);
		addNewSheetWithCellsA1toD4(xlw, 2);
		addNewSheetWithCellsA1toD4(xlw, 3);
		addNewSheetWithCellsA1toD4(xlw, 4);

		HSSFSheet s2   = xlw.getSheet("s2");
		HSSFRow   s2r3 = s2.getRow(3);
		HSSFCell  s2E4 = s2r3.createCell(4);
		s2E4.setCellFormula("SUM(s3!B2:C3)");

		HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(s2, xlw);
		double d = eva.evaluate(s2E4).getNumberValue();

		// internalEvaluate(...) Area3DEval.: 311+312+321+322 expected
		assertEquals(d, (311+312+321+322), 0.0000001);
		// System.out.println("Area3DEval ok.: 311+312+321+322=" + d);
	}
}
