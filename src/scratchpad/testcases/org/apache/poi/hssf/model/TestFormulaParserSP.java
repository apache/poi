
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
        
package org.apache.poi.hssf.model;

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.FuncVarPtg;
import org.apache.poi.hssf.record.formula.NamePtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Test the low level formula parser functionality,
 *  but using parts which need to use the
 *  HSSFFormulaEvaluator, which is in scratchpad 
 */
public class TestFormulaParserSP extends TestCase {

    public TestFormulaParserSP(String name) {
        super(name);
    }
	
	public void testWithNamedRange() throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook();
		FormulaParser fp;
		Ptg[] ptgs;

		HSSFSheet s = workbook.createSheet("Foo");
		s.createRow(0).createCell((short)0).setCellValue(1.1);
		s.createRow(1).createCell((short)0).setCellValue(2.3);
		s.createRow(2).createCell((short)2).setCellValue(3.1);

		HSSFName name = workbook.createName();
		name.setNameName("testName");
		name.setReference("A1:A2");

		fp = HSSFFormulaEvaluator.getUnderlyingParser(workbook, "SUM(testName)");
		fp.parse();
		ptgs = fp.getRPNPtg();
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());

		// Now make it a single cell
		name.setReference("C3");

		fp = HSSFFormulaEvaluator.getUnderlyingParser(workbook, "SUM(testName)");
		fp.parse();
		ptgs = fp.getRPNPtg();
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());
		
		// And make it non-contiguous
		name.setReference("A1:A2,C3");
		fp = HSSFFormulaEvaluator.getUnderlyingParser(workbook, "SUM(testName)");
		fp.parse();
		ptgs = fp.getRPNPtg();
		assertTrue("two tokens expected, got "+ptgs.length,ptgs.length == 2);
		assertEquals(NamePtg.class, ptgs[0].getClass());
		assertEquals(FuncVarPtg.class, ptgs[1].getClass());
	}

}
