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
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.examples.formula.CalculateMortgageFunction;
import org.apache.poi.ss.excelant.BuildFileTest;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

public class TestExcelAntWorkbookUtil extends TestCase {
	
    private static final String mortgageCalculatorFileName =
        BuildFileTest.getDataDir() + "/spreadsheet/excelant.xls" ;

	private ExcelAntWorkbookUtilTestHelper fixture ;
		
	
	@Override
	public void tearDown() {
		fixture = null ;
	}

	public void testStringConstructor() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
				                                  mortgageCalculatorFileName);
		
		assertNotNull(fixture);
	}

	public void testLoadNotExistingFile() {
		try {
			new ExcelAntWorkbookUtilTestHelper("notexistingFile");
			fail("Should catch exception here");
		} catch (BuildException e) {
			assertTrue(e.getMessage().contains("notexistingFile"));			
		}
	}
	
	public void testWorkbookConstructor() throws IOException {
        File workbookFile = new File(mortgageCalculatorFileName);
        FileInputStream fis = new FileInputStream(workbookFile);
        Workbook workbook = WorkbookFactory.create(fis);

		fixture = new ExcelAntWorkbookUtilTestHelper(workbook);
		
		assertNotNull(fixture);
	}
	
	public void testAddFunction() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		assertNotNull(fixture);
		
		fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());
		
		UDFFinder functions = fixture.getFunctions();
		
		assertNotNull(functions);
		assertNotNull(functions.findFunction("h2_ZFactor"));
	}

    public void testAddFunctionClassName() throws Exception {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        assertNotNull(fixture);
        
        fixture.addFunction("h2_ZFactor", CalculateMortgageFunction.class.getName());
        
        UDFFinder functions = fixture.getFunctions();
        
        assertNotNull(functions);
        assertNotNull(functions.findFunction("h2_ZFactor"));
    }

    public void testAddFunctionInvalidClassName() throws Exception {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        assertNotNull(fixture);
        
        fixture.addFunction("h2_ZFactor", String.class.getName());
        
        UDFFinder functions = fixture.getFunctions();
        
        assertNotNull(functions);
        assertNull(functions.findFunction("h2_ZFactor"));
    }

	public void testGetWorkbook() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		assertNotNull(fixture);
		
		Workbook workbook = fixture.getWorkbook();
		
		assertNotNull(workbook);
	}
	
	public void testFileName() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		assertNotNull(fixture);

		String fileName = fixture.getFileName();
		
		assertNotNull(fileName);
		
		assertEquals(mortgageCalculatorFileName, fileName);
		
	}
	
	public void testGetEvaluator() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		FormulaEvaluator evaluator = fixture.getEvaluator(
				                                  mortgageCalculatorFileName);
		
		assertNotNull(evaluator);
 	}

    public void testGetEvaluatorWithUDF() {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());
        
        FormulaEvaluator evaluator = fixture.getEvaluator(
                                                  mortgageCalculatorFileName);
        
        assertNotNull(evaluator);
    }
	
	public void testGetEvaluatorXLSX() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                BuildFileTest.getDataDir() + "/spreadsheet/sample.xlsx");
		
		FormulaEvaluator evaluator = fixture.getEvaluator(
				BuildFileTest.getDataDir() + "/spreadsheet/sample.xlsx");
		
		assertNotNull(evaluator);
 	}

    public void testGetEvaluatorXLSXWithFunction() {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                BuildFileTest.getDataDir() + "/spreadsheet/sample.xlsx");
        
        fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());
        
        FormulaEvaluator evaluator = fixture.getEvaluator(
                BuildFileTest.getDataDir() + "/spreadsheet/sample.xlsx");
        
        assertNotNull(evaluator);
    }

	public void testEvaluateCell() {
		String cell = "'MortgageCalculator'!B4" ;
		double expectedValue = 790.79 ;
		double precision = 0.1 ;

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		ExcelAntEvaluationResult result = fixture.evaluateCell(cell, 
				                                                expectedValue, 
				                                                precision);

		//System.out.println(result);
		assertTrue("Had:" + result, result.toString().contains("evaluationCompletedWithError=false"));
		assertTrue("Had:" + result, result.toString().contains("returnValue=790.79"));
		assertTrue("Had:" + result, result.toString().contains("cellName='MortgageCalculator'!B4"));
        assertFalse(result.toString().contains("#N/A"));

		assertFalse(result.evaluationCompleteWithError());
		assertTrue(result.didTestPass());
	}
	
    public void testEvaluateCellFailedPrecision() {
        String cell = "'MortgageCalculator'!B4" ;
        double expectedValue = 790.79 ;
        double precision = 0.0000000000001 ;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        ExcelAntEvaluationResult result = fixture.evaluateCell(cell, 
                                                                expectedValue, 
                                                                precision);

        //System.out.println(result);
        assertTrue("Had:" + result, result.toString().contains("evaluationCompletedWithError=false"));
        assertTrue("Had:" + result, result.toString().contains("returnValue=790.79"));
        assertTrue("Had:" + result, result.toString().contains("cellName='MortgageCalculator'!B4"));
        assertFalse("Should not see an error, but had:" + result, result.toString().contains("#"));

        assertFalse(result.evaluationCompleteWithError());
        assertFalse(result.didTestPass());
    }
    
    public void testEvaluateCellWithError() {
        String cell = "'ErrorCell'!A1" ;
        double expectedValue = 790.79 ;
        double precision = 0.1 ;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        ExcelAntEvaluationResult result = fixture.evaluateCell(cell, 
                                                                expectedValue, 
                                                                precision);

        System.out.println(result);
        assertTrue("Had:" + result, result.toString().contains("evaluationCompletedWithError=true"));
        assertTrue("Had:" + result, result.toString().contains("returnValue=0.0"));
        assertTrue("Had:" + result, result.toString().contains("cellName='ErrorCell'!A1"));
        assertTrue("Had:" + result, result.toString().contains("#N/A"));

        assertTrue(result.evaluationCompleteWithError());
        assertFalse(result.didTestPass());
    }
    
	public void testGetSheets() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		List<String> sheets = fixture.getSheets();
		
		assertNotNull(sheets);
		assertEquals(sheets.size(), 3); 
	}
	
	public void testSetString() {
		String cell = "'MortgageCalculator'!C14" ;
		String cellValue = "testString" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		fixture.setStringValue(cell, cellValue);
		
		String value = fixture.getCellAsString(cell);
		
		assertNotNull(value);
		assertEquals(cellValue, value);
	}
	
    public void testSetNotExistingSheet() {
        String cell = "'NotexistingSheet'!C14" ;
        
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
        try {
            fixture.setStringValue(cell, "some");
            fail("Should catch exception here");
        } catch (BuildException e) {
            assertTrue(e.getMessage().contains("NotexistingSheet"));
        }
    }

    public void testSetFormula() {
        String cell = "'MortgageCalculator'!C14" ;
        String cellValue = "SUM(B14:B18)" ;
        
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
        
        fixture.setFormulaValue(cell, cellValue);
        
        double value = fixture.getCellAsDouble(cell);
        
        assertEquals(0.0, value);
    }
    
    public void testSetDoubleValue() {
        String cell = "'MortgageCalculator'!C14" ;
        double cellValue = 1.2;
        
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
        
        fixture.setDoubleValue(cell, cellValue);
        
        Double value = fixture.getCellAsDouble(cell);
        
        assertNotNull(value);
        assertEquals(cellValue, value);
    }
    
	public void testSetDate() {
		String cell = "'MortgageCalculator'!C14" ;
		Date cellValue = new Date();
		
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		fixture.setDateValue(cell, cellValue);
		
		double value = fixture.getCellAsDouble(cell);
		
		assertEquals(DateUtil.getExcelDate(cellValue, false), value);
	}

	public void testGetNonexistingString() {
		String cell = "'MortgageCalculator'!C33" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		String value = fixture.getCellAsString(cell);
		
		assertEquals("", value);
	}

	public void testGetNonexistingDouble() {
		String cell = "'MortgageCalculator'!C33" ;
		
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);
		
		double value = fixture.getCellAsDouble(cell);
		
		assertEquals(0.0, value);
	}
}
