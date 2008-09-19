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

package org.apache.poi.hssf.record.formula;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
/**
 * Tests for functions from external workbooks (e.g. YEARFRAC).
 * 
 * 
 * @author Josh Micich
 */
public final class TestExternalFunctionFormulas extends TestCase {

	/**
	 * tests <tt>NameXPtg.toFormulaString(Workbook)</tt> and logic in Workbook below that   
	 */
	public void testReadFormulaContainingExternalFunction() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("externalFunctionExample.xls");
		
		String expectedFormula = "YEARFRAC(B1,C1)";
		HSSFSheet sht = wb.getSheetAt(0);
		String cellFormula = sht.getRow(0).getCell(0).getCellFormula();
		assertEquals(expectedFormula, cellFormula);
	}
	
	public void testParse() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("externalFunctionExample.xls");
		Ptg[] ptgs = HSSFFormulaParser.parse("YEARFRAC(B1,C1)", wb);
		assertEquals(4, ptgs.length);
		assertEquals(NameXPtg.class, ptgs[0].getClass());
		
		wb.getSheetAt(0).getRow(0).createCell(6).setCellFormula("YEARFRAC(C1,B1)");
		if (false) {
			// In case you fancy checking in excel
			try {
				File tempFile = File.createTempFile("testExtFunc", ".xls");
				FileOutputStream fout = new FileOutputStream(tempFile);
				wb.write(fout);
				fout.close();
				System.out.println("check out " + tempFile.getAbsolutePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void testEvaluate() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("externalFunctionExample.xls");
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
		confirmCellEval(sheet, 0, 0, fe, "YEARFRAC(B1,C1)", 29.0/90.0);
		confirmCellEval(sheet, 1, 0, fe, "YEARFRAC(B2,C2)", 0.0);
		confirmCellEval(sheet, 2, 0, fe, "YEARFRAC(B3,C3,D3)", 0.0);
		confirmCellEval(sheet, 3, 0, fe, "IF(ISEVEN(3),1.2,1.6)", 1.6);
		confirmCellEval(sheet, 4, 0, fe, "IF(ISODD(3),1.2,1.6)", 1.2);
	}

	private static void confirmCellEval(HSSFSheet sheet, int rowIx, int colIx, 
			HSSFFormulaEvaluator fe, String expectedFormula, double expectedResult) {
		HSSFCell cell = sheet.getRow(rowIx).getCell(colIx);
		assertEquals(expectedFormula, cell.getCellFormula());
		CellValue cv = fe.evaluate(cell);
		assertEquals(expectedResult, cv.getNumberValue(), 0.0);
	}
}
