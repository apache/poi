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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 * Tests reading from a sample spreadsheet some built-in functions that were not properly
 * registered in POI as bug #44675 (March 2008).
 * 
 * @author Josh Micich
 */
public final class TestReadMissingBuiltInFuncs extends TestCase {

	private HSSFSheet sht;

	protected void setUp() {
		String cwd = System.getProperty("HSSF.testdata.path");
		HSSFWorkbook wb;
		try {
			InputStream is = new FileInputStream(new File(cwd, "missingFuncs44675.xls"));
			wb = new HSSFWorkbook(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		sht = wb.getSheetAt(0);
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

	private String getCellFormula(int rowIx) {
		String result = sht.getRow(rowIx).getCell((short)0).getCellFormula();
		if (false) {
			System.err.println(result);
		}
		return result;
	}
	private static AssertionFailedError afe(String msg) {
		return new AssertionFailedError(msg);
	}
}
