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

package org.apache.poi.hssf.record.formula.function;

import java.lang.reflect.InvocationTargetException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * Tests reading from a sample spreadsheet some built-in functions that were not properly
 * registered in POI as of bug #44675, #44733 (March/April 2008).
 * 
 * @author Josh Micich
 */
public final class TestReadMissingBuiltInFuncs extends TestCase {

	/**
	 * This spreadsheet has examples of calls to the interesting built-in functions in cells A1:A7
	 */
	private static final String SAMPLE_SPREADSHEET_FILE_NAME = "missingFuncs44675.xls";
	private static HSSFSheet _sheet;

	private static HSSFSheet getSheet() {
		if (_sheet == null) {
			HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook(SAMPLE_SPREADSHEET_FILE_NAME);
			_sheet = wb.getSheetAt(0);
		}
		return _sheet;
	}

	public void testDatedif() {
		
		String formula;
		try {
			formula = getCellFormula(0);
		} catch (IllegalStateException e) {
			if(e.getMessage().startsWith("Too few arguments")) {
				if(e.getMessage().indexOf("AttrPtg") > 0) {
					throw afe("tAttrVolatile not supported in FormulaParser.toFormulaString");
				}
				throw afe("NOW() registered with 1 arg instead of 0");
			}
			if(e.getMessage().startsWith("too much stuff")) {
				throw afe("DATEDIF() not registered");
			}
			// some other unexpected error
			throw e;
		}
		assertEquals("DATEDIF(NOW(),NOW(),\"d\")", formula);
	}
	public void testDdb() {

		String formula = getCellFormula(1);
		if("externalflag(1,1,1,1,1)".equals(formula)) {
			throw afe("DDB() not registered");
		}
		assertEquals("DDB(1,1,1,1,1)", formula);
	}
	public void testAtan() {

		String formula = getCellFormula(2);
		if(formula.equals("ARCTAN(1)")) {
			throw afe("func ix 18 registered as ARCTAN() instead of ATAN()");
		}
		assertEquals("ATAN(1)", formula);
	}

	public void testUsdollar() {
	
		String formula = getCellFormula(3);
		if(formula.equals("YEN(1)")) {
			throw afe("func ix 204 registered as YEN() instead of USDOLLAR()");
		}
		assertEquals("USDOLLAR(1)", formula);
	}

	public void testDBCS() {
	
		String formula;
		try {
			formula = getCellFormula(4);
		} catch (IllegalStateException e) {
			if(e.getMessage().startsWith("too much stuff")) {
				throw afe("DBCS() not registered");
			}
			// some other unexpected error
			throw e;
		} catch (NegativeArraySizeException e) {
			throw afe("found err- DBCS() registered with -1 args");
		}
		if(formula.equals("JIS(\"abc\")")) {
			throw afe("func ix 215 registered as JIS() instead of DBCS()");
		}
		assertEquals("DBCS(\"abc\")", formula);
	}
	public void testIsnontext() {
		
		String formula;
		try {
			formula = getCellFormula(5);
		} catch (IllegalStateException e) {
			if(e.getMessage().startsWith("too much stuff")) {
				throw afe("ISNONTEXT() registered with wrong index");
			}
			// some other unexpected error
			throw e;
		}
		assertEquals("ISNONTEXT(\"abc\")", formula);
	}
	public void testDproduct() {
		
		String formula = getCellFormula(6);
		assertEquals("DPRODUCT(C1:E5,\"HarvestYield\",G1:H2)", formula);
	}

	private String getCellFormula(int rowIx) {
		HSSFSheet sheet;
		try {
			sheet = getSheet();
		} catch (RecordFormatException e) {
			if(e.getCause() instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e.getCause();
				if(ite.getTargetException() instanceof RuntimeException) {
					RuntimeException re = (RuntimeException) ite.getTargetException();
					if(re.getMessage().equals("Invalid built-in function index (189)")) {
						throw afe("DPRODUCT() registered with wrong index");
					}
				}
			}
			// some other unexpected error
			throw e;
		}
		String result = sheet.getRow(rowIx).getCell(0).getCellFormula();
		if (false) {
			System.err.println(result);
		}
		return result;
	}
	private static AssertionFailedError afe(String msg) {
		return new AssertionFailedError(msg);
	}
}
