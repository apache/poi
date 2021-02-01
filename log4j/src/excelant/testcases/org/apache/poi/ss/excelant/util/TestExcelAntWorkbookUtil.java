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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.excelant.CalculateMortgageFunction;
import org.apache.poi.ss.excelant.TestBuildFile;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TestExcelAntWorkbookUtil {

    private static final String mortgageCalculatorFileName =
        TestBuildFile.getDataDir() + "/spreadsheet/excelant.xls" ;

	private ExcelAntWorkbookUtilTestHelper fixture ;


	@AfterEach
	void tearDown() {
		fixture = null ;
	}

	@Test
	void testStringConstructor() {
		fixture = new ExcelAntWorkbookUtilTestHelper(mortgageCalculatorFileName);

		assertNotNull(fixture);
	}

	@Test
	void testLoadNotExistingFile() {
		BuildException e = assertThrows(BuildException.class, () -> new ExcelAntWorkbookUtilTestHelper("notexistingFile"));
		assertTrue(e.getMessage().contains("notexistingFile"));
	}

	@Test
	void testWorkbookConstructor() throws IOException {
        File workbookFile = new File(mortgageCalculatorFileName);
        FileInputStream fis = new FileInputStream(workbookFile);
        Workbook workbook = WorkbookFactory.create(fis);

		fixture = new ExcelAntWorkbookUtilTestHelper(workbook);

		assertNotNull(fixture);
	}

	@Test
	void testAddFunction() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		assertNotNull(fixture);

		fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());

		UDFFinder functions = fixture.getFunctions();

		assertNotNull(functions);
		assertNotNull(functions.findFunction("h2_ZFactor"));
	}

	@Test
    void testAddFunctionClassName() throws Exception {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        assertNotNull(fixture);

        fixture.addFunction("h2_ZFactor", CalculateMortgageFunction.class.getName());

        UDFFinder functions = fixture.getFunctions();

        assertNotNull(functions);
        assertNotNull(functions.findFunction("h2_ZFactor"));
    }

	@Test
    void testAddFunctionInvalidClassName() throws Exception {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        assertNotNull(fixture);

        fixture.addFunction("h2_ZFactor", String.class.getName());

        UDFFinder functions = fixture.getFunctions();

        assertNotNull(functions);
        assertNull(functions.findFunction("h2_ZFactor"));
    }

	@Test
	void testGetWorkbook() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		assertNotNull(fixture);

		Workbook workbook = fixture.getWorkbook();

		assertNotNull(workbook);
	}

	@Test
	void testFileName() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		assertNotNull(fixture);

		String fileName = fixture.getFileName();

		assertNotNull(fileName);

		assertEquals(mortgageCalculatorFileName, fileName);

	}

	@Test
	void testGetEvaluator() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		FormulaEvaluator evaluator = fixture.getEvaluator(
				                                  mortgageCalculatorFileName);

		assertNotNull(evaluator);
 	}

	@Test
    void testGetEvaluatorWithUDF() {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());

        FormulaEvaluator evaluator = fixture.getEvaluator(
                                                  mortgageCalculatorFileName);

        assertNotNull(evaluator);
    }

	@Test
	void testGetEvaluatorXLSX() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                TestBuildFile.getDataDir() + "/spreadsheet/sample.xlsx");

		FormulaEvaluator evaluator = fixture.getEvaluator(
                TestBuildFile.getDataDir() + "/spreadsheet/sample.xlsx");

		assertNotNull(evaluator);
 	}

	@Test
    void testGetEvaluatorXLSXWithFunction() {
        fixture = new ExcelAntWorkbookUtilTestHelper(
                TestBuildFile.getDataDir() + "/spreadsheet/sample.xlsx");

        fixture.addFunction("h2_ZFactor", new CalculateMortgageFunction());

        FormulaEvaluator evaluator = fixture.getEvaluator(
                TestBuildFile.getDataDir() + "/spreadsheet/sample.xlsx");

        assertNotNull(evaluator);
    }

	@Test
	void testEvaluateCell() {
		String cell = "'MortgageCalculator'!B4" ;
		double expectedValue = 790.79 ;
		double precision = 0.1 ;

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		ExcelAntEvaluationResult result = fixture.evaluateCell(cell,
				                                                expectedValue,
				                                                precision);

		//System.out.println(result);
		assertTrue( result.toString().contains("evaluationCompletedWithError=false"), "Had:" + result );
		assertTrue( result.toString().contains("returnValue=790.79"), "Had:" + result );
		assertTrue( result.toString().contains("cellName='MortgageCalculator'!B4"), "Had:" + result );
        assertFalse(result.toString().contains("#N/A"));

		assertFalse(result.evaluationCompleteWithError());
		assertTrue(result.didTestPass());
	}

	@Test
    void testEvaluateCellFailedPrecision() {
        String cell = "'MortgageCalculator'!B4" ;
        double expectedValue = 790.79 ;
        double precision = 0.0000000000001 ;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        ExcelAntEvaluationResult result = fixture.evaluateCell(cell,
                                                                expectedValue,
                                                                precision);

        //System.out.println(result);
        assertTrue( result.toString().contains("evaluationCompletedWithError=false"), "Had:" + result );
        assertTrue( result.toString().contains("returnValue=790.79"), "Had:" + result );
        assertTrue( result.toString().contains("cellName='MortgageCalculator'!B4"), "Had:" + result );
        assertFalse( result.toString().contains("#"), "Should not see an error, but had:" + result );

        assertFalse(result.evaluationCompleteWithError());
        assertFalse(result.didTestPass());
    }

	@Test
    void testEvaluateCellWithError() {
        String cell = "'ErrorCell'!A1" ;
        double expectedValue = 790.79 ;
        double precision = 0.1 ;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        ExcelAntEvaluationResult result = fixture.evaluateCell(cell,
                                                                expectedValue,
                                                                precision);

        System.out.println(result);
        assertTrue( result.toString().contains("evaluationCompletedWithError=true"), "Had:" + result );
        assertTrue( result.toString().contains("returnValue=0.0"), "Had:" + result );
        assertTrue( result.toString().contains("cellName='ErrorCell'!A1"), "Had:" + result );
        assertTrue( result.toString().contains("#N/A"), "Had:" + result );

        assertTrue(result.evaluationCompleteWithError());
        assertFalse(result.didTestPass());
    }

	@Test
	void testGetSheets() {
		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		List<String> sheets = fixture.getSheets();

		assertNotNull(sheets);
		assertEquals(sheets.size(), 3);
	}

	@Test
	void testSetString() {
		String cell = "'MortgageCalculator'!C14" ;
		String cellValue = "testString" ;

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		fixture.setStringValue(cell, cellValue);

		String value = fixture.getCellAsString(cell);

		assertNotNull(value);
		assertEquals(cellValue, value);
	}

	@Test
    void testSetNotExistingSheet() {
        String cell = "'NotexistingSheet'!C14" ;

        fixture = new ExcelAntWorkbookUtilTestHelper(mortgageCalculatorFileName);
		BuildException e = assertThrows(BuildException.class, () -> fixture.setStringValue(cell, "some"));
		assertTrue(e.getMessage().contains("NotexistingSheet"));
    }

	@Test
    void testSetFormula() {
        String cell = "'MortgageCalculator'!C14" ;
        String cellValue = "SUM(B14:B18)" ;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        fixture.setFormulaValue(cell, cellValue);

        double value = fixture.getCellAsDouble(cell);

        assertEquals(0.0, value, 0);
    }

	@Test
    void testSetDoubleValue() {
        String cell = "'MortgageCalculator'!C14" ;
        double cellValue = 1.2;

        fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

        fixture.setDoubleValue(cell, cellValue);

        double value = fixture.getCellAsDouble(cell);

        assertEquals(cellValue, value, 0);
    }

	@Test
	void testSetDate() {
		String cell = "'MortgageCalculator'!C14" ;
		Date cellValue = new Date();

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		fixture.setDateValue(cell, cellValue);

		double value = fixture.getCellAsDouble(cell);

		assertEquals(DateUtil.getExcelDate(cellValue, false), value, 0);
	}

	@Test
	void testGetNonexistingString() {
		String cell = "'MortgageCalculator'!C33" ;

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		String value = fixture.getCellAsString(cell);

		assertEquals("", value);
	}

	@Test
	void testGetNonexistingDouble() {
		String cell = "'MortgageCalculator'!C33" ;

		fixture = new ExcelAntWorkbookUtilTestHelper(
                mortgageCalculatorFileName);

		double value = fixture.getCellAsDouble(cell);

		assertEquals(0.0, value, 0);
	}
}
