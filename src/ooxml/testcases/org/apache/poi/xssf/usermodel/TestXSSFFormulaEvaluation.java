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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import junit.framework.TestCase;

public class TestXSSFFormulaEvaluation extends TestCase {
	public TestXSSFFormulaEvaluation(String name) {
		super(name);
		
		// Use system out logger
		System.setProperty(
				"org.apache.poi.util.POILogger",
				"org.apache.poi.util.SystemOutLogger"
		);
	}

	public void testSimpleArithmatic() {
		Workbook wb = new XSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = s.createRow(0);
		
		Cell c1 = r.createCell(0);
		c1.setCellFormula("1+5");
		assertTrue( Double.isNaN(c1.getNumericCellValue()) );
		
		Cell c2 = r.createCell(1);
		c2.setCellFormula("10/2");
		assertTrue( Double.isNaN(c2.getNumericCellValue()) );
		
		FormulaEvaluator fe = new FormulaEvaluator(s, wb);
		
		fe.evaluateFormulaCell(c1);
		fe.evaluateFormulaCell(c2);
		
		assertEquals(6.0, c1.getNumericCellValue(), 0.0001);
		assertEquals(5.0, c2.getNumericCellValue(), 0.0001);
	}
	
	public void testSumCount() {
		Workbook wb = new XSSFWorkbook();
		Sheet s = wb.createSheet();
		Row r = s.createRow(0);
		r.createCell(0).setCellValue(2.5);
		r.createCell(1).setCellValue(1.1);
		r.createCell(2).setCellValue(3.2);
		r.createCell(4).setCellValue(10.7);
		
		r = s.createRow(1);
		
		Cell c1 = r.createCell(0);
		c1.setCellFormula("SUM(A1:B1)");
		assertTrue( Double.isNaN(c1.getNumericCellValue()) );
		
		Cell c2 = r.createCell(1);
		c2.setCellFormula("SUM(A1:E1)");
		assertTrue( Double.isNaN(c2.getNumericCellValue()) );
		
		Cell c3 = r.createCell(2);
		c3.setCellFormula("COUNT(A1:A1)");
		assertTrue( Double.isNaN(c3.getNumericCellValue()) );

		Cell c4 = r.createCell(3);
		c4.setCellFormula("COUNTA(A1:E1)");
		assertTrue( Double.isNaN(c4.getNumericCellValue()) );


		// Evaluate and test
		FormulaEvaluator fe = new FormulaEvaluator(s, wb);
		
		fe.evaluateFormulaCell(c1);
		fe.evaluateFormulaCell(c2);
		fe.evaluateFormulaCell(c3);
		fe.evaluateFormulaCell(c4);
		
		assertEquals(3.6, c1.getNumericCellValue(), 0.0001);
		assertEquals(17.5, c2.getNumericCellValue(), 0.0001);
		assertEquals(1, c3.getNumericCellValue(), 0.0001);
		assertEquals(4, c4.getNumericCellValue(), 0.0001);
	}
}
