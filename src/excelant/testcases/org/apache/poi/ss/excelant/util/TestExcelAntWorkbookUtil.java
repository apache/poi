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
package org.apache.poi.ss.excelant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.examples.formula.CalculateMortgageFunction;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tools.ant.BuildException;

public class TestExcelAntWorkbookUtil extends TestCase {
	
	private final String mortgageCalculatorFileName =
		                                  "test-data/spreadsheet/excelant.xls" ;
	private ExcelAntWorkbookUtilTestHelper fixture ;
		
	
	@Override
	public void tearDown() {
		fixture = null ;
	}

	public void testStringConstructor() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
				                                  mortgageCalculatorFileName ) ;
		
		assertNotNull( fixture ) ;
	}

	public void testLoadNotExistingFile() {
		try {
			assertNotNull(new ExcelAntWorkbookUtilTestHelper( 
				                                  "notexistingFile" ));
			fail("Should catch exception here");
		} catch (BuildException e) {
			assertTrue(e.getMessage().contains("notexistingFile"));			
		}
	}
	
	public void testWorkbookConstructor() throws InvalidFormatException, IOException {
        File workbookFile = new File(mortgageCalculatorFileName);
        FileInputStream fis = new FileInputStream(workbookFile);
        Workbook workbook = WorkbookFactory.create(fis);

		fixture = new ExcelAntWorkbookUtilTestHelper( workbook ) ;
		
		assertNotNull( fixture ) ;
		
	}
	
	public void testAddFunction() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;

		assertNotNull( fixture ) ;
		
		fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction() ) ;
		
		UDFFinder functions = fixture.getFunctions() ;
		
		assertNotNull( functions ) ;
	}

	public void testGetWorkbook() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		assertNotNull( fixture ) ;
		
		Workbook workbook = fixture.getWorkbook() ;
		
		assertNotNull( workbook ) ;
	}
	
	public void testFileName() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		assertNotNull( fixture ) ;

		String fileName = fixture.getFileName() ;
		
		assertNotNull( fileName ) ;
		
		assertEquals( mortgageCalculatorFileName, fileName ) ;
		
	}
	
	public void testGetEvaluator() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		FormulaEvaluator evaluator = fixture.getEvaluator( 
				                                  mortgageCalculatorFileName ) ;
		
		assertNotNull( evaluator ) ;
		
		
 	}

	public void testGetEvaluatorXLSX() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                "test-data/spreadsheet/sample.xlsx") ;
		
		FormulaEvaluator evaluator = fixture.getEvaluator( 
				"test-data/spreadsheet/sample.xlsx" ) ;
		
		assertNotNull( evaluator ) ;
 	}

	public void testEvaluateCell() {
		String cell = "'MortgageCalculator'!B4" ;
		double expectedValue = 790.79 ;
		double precision = 0.1 ;

		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;

		ExcelAntEvaluationResult result = fixture.evaluateCell( cell, 
				                                                expectedValue, 
				                                                precision ) ;
		
		System.out.println(  result ) ;
		
		assertTrue( result.didTestPass() ) ;
	}
	
	public void testGetSheets() {
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		ArrayList<String> sheets = fixture.getSheets() ;
		
		assertNotNull( sheets ) ;
		assertEquals( sheets.size(), 3 ) ; 
	}
	
	public void testSetString() {
		String cell = "'MortgageCalculator'!C14" ;
		String cellValue = "testString" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		fixture.setStringValue( cell, cellValue ) ;
		
		String value = fixture.getCellAsString( cell ) ;
		
		assertNotNull( value ) ;
		
		assertEquals( cellValue, value ) ;
		
	}
	
	public void testSetDate() {
		String cell = "'MortgageCalculator'!C14" ;
		Date cellValue = new Date();
		
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		fixture.setDateValue( cell, cellValue ) ;
		
		double value = fixture.getCellAsDouble( cell ) ;
		
		assertNotNull( value ) ;
		
		assertEquals( DateUtil.getExcelDate(cellValue, false), value ) ;
		
	}

	public void testGetNonexistingString() {
		String cell = "'MortgageCalculator'!C33" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		String value = fixture.getCellAsString( cell ) ;
		
		assertEquals( "", value ) ;
	}

	public void testGetNonexistingDouble() {
		String cell = "'MortgageCalculator'!C33" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper( 
                mortgageCalculatorFileName ) ;
		
		double value = fixture.getCellAsDouble( cell ) ;
		
		assertEquals( 0.0, value ) ;
	}
}
