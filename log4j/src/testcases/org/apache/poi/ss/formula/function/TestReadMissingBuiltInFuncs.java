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

package org.apache.poi.ss.formula.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests reading from a sample spreadsheet some built-in functions that were not properly
 * registered in POI as of bug #44675, #44733 (March/April 2008).
 */
final class TestReadMissingBuiltInFuncs {

	/**
	 * This spreadsheet has examples of calls to the interesting built-in functions in cells A1:A7
	 */
	private static final String SAMPLE_SPREADSHEET_FILE_NAME = "missingFuncs44675.xls";

	private static HSSFWorkbook wb;
	private static HSSFSheet _sheet;

	@BeforeAll
	public static void initSheet() {
        wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_SPREADSHEET_FILE_NAME);
        try {
            _sheet = wb.getSheetAt(0);
	    } catch (RecordFormatException e) {
            if(e.getCause() instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e.getCause();
                if(ite.getTargetException() instanceof RuntimeException) {
                    RuntimeException re = (RuntimeException) ite.getTargetException();
					assertNotEquals("Invalid built-in function index (189)", re.getMessage(),
						"DPRODUCT() registered with wrong index");
                }
            }
            // some other unexpected error
            throw e;
        }
	}

	@AfterAll
	public static void closeResources() throws Exception {
	    wb.close();
	}

	@Test
	void testDatedif() {
		String formula;
		try {
			formula = getCellFormula(0);
		} catch (IllegalStateException e) {
		    if(e.getMessage().startsWith("Too few arguments")) {
		    	assertFalse(e.getMessage().contains("AttrPtg"),
					"tAttrVolatile not supported in FormulaParser.toFormulaString");
				fail("NOW() registered with 1 arg instead of 0");
			}
			if(e.getMessage().startsWith("too much stuff")) {
				fail("DATEDIF() not registered");
			}
			// some other unexpected error
			throw e;
		}
		assertEquals("DATEDIF(NOW(),NOW(),\"d\")", formula);
	}

	@Test
	void testDdb() {
		String formula = getCellFormula(1);
		assertNotEquals("externalflag(1,1,1,1,1)", formula, "DDB() not registered");
		assertEquals("DDB(1,1,1,1,1)", formula);
	}

	@Test
	void testAtan() {
		String formula = getCellFormula(2);
		assertNotEquals("ARCTAN(1)", formula, "func ix 18 registered as ARCTAN() instead of ATAN()");
		assertEquals("ATAN(1)", formula);
	}

	@Test
	void testUsdollar() {
		String formula = getCellFormula(3);
		assertNotEquals("YEN(1)", formula, "func ix 204 registered as YEN() instead of USDOLLAR()");
		assertEquals("USDOLLAR(1)", formula);
	}

	@Test
	void testDBCS() {
		String formula = "";
		try {
			formula = getCellFormula(4);
		} catch (IllegalStateException e) {
			assertFalse(e.getMessage().startsWith("too much stuff"), "DBCS() not registered");
			// some other unexpected error
			throw e;
		} catch (NegativeArraySizeException e) {
			fail("found err- DBCS() registered with -1 args");
		}
		assertNotEquals("JIS(\"abc\")", formula, "func ix 215 registered as JIS() instead of DBCS()");
		assertEquals("DBCS(\"abc\")", formula);
	}

	@Test
	void testIsnontext() {
		String formula;
		try {
			formula = getCellFormula(5);
		} catch (IllegalStateException e) {
			assertFalse(e.getMessage().startsWith("too much stuff"), "ISNONTEXT() registered with wrong index");
			// some other unexpected error
			throw e;
		}
		assertEquals("ISNONTEXT(\"abc\")", formula);
	}

	@Test
	void testDproduct() {
		String formula = getCellFormula(6);
		assertEquals("DPRODUCT(C1:E5,\"HarvestYield\",G1:H2)", formula);
	}

	private String getCellFormula(int rowIx) {
        //		if (false) {
//			System.err.println(result);
//		}
		return _sheet.getRow(rowIx).getCell(0).getCellFormula();
	}
}
