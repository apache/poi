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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.util.CellReference;

/**
 * The PMT formula seems to be giving some grief
 */
public final class TestBug44691 extends TestCase {
	String dirname;

	protected void setUp() throws Exception {
		super.setUp();
		dirname = System.getProperty("HSSF.testdata.path");
	}

	public void DISABLEDtestBug44691() throws Exception {
		HSSFWorkbook outWorkbook = new HSSFWorkbook();
		HSSFSheet outPMTSheet = outWorkbook.createSheet("PMT Sheet");
		HSSFRow row = outPMTSheet.createRow((short) 0);
		HSSFCell cell = row.createCell((short) 0);
		cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		cell.setCellFormula("PMT(0.09/12,48,-10000)");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		outWorkbook.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		HSSFWorkbook inWorkbook = new HSSFWorkbook(bais);

		HSSFSheet inPMTSheet = inWorkbook.getSheet("PMT Sheet");
		HSSFFormulaEvaluator evaluator = new
		HSSFFormulaEvaluator(inPMTSheet, inWorkbook);
		CellReference cellReference = new CellReference("A1");
		HSSFRow inRow = inPMTSheet.getRow(cellReference.getRow());
		HSSFCell inCell = inRow.getCell(cellReference.getCol());
		
		assertEquals("PMT(0.09/12,48,-10000)", 
				inCell.getCellFormula());
		assertEquals(HSSFCell.CELL_TYPE_FORMULA, inCell.getCellType());
		
		evaluator.setCurrentRow(inRow);
		HSSFFormulaEvaluator.CellValue inCellValue =
			evaluator.evaluate(inCell);
		
		assertEquals(0, inCellValue.getErrorValue());
		assertEquals(HSSFCell.CELL_TYPE_NUMERIC, inCellValue.getCellType());
		assertEquals(248.85, inCellValue.getNumberValue(), 0.0001);
	}
}
