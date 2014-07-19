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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Ignore;
import org.junit.Test;

public final class TestXSSFFormulaParser {
	private static Ptg[] parse(XSSFEvaluationWorkbook fpb, String fmla) {
		return FormulaParser.parse(fmla, fpb, FormulaType.CELL, -1);
	}

	@Test
    public void basicParsing() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        ptgs = parse(fpb, "ABC10");
        assertEquals(1, ptgs.length);
        assertTrue("", ptgs[0] instanceof RefPtg);

        ptgs = parse(fpb, "A500000");
        assertEquals(1, ptgs.length);
        assertTrue("", ptgs[0] instanceof RefPtg);

        ptgs = parse(fpb, "ABC500000");
        assertEquals(1, ptgs.length);
        assertTrue("", ptgs[0] instanceof RefPtg);

        //highest allowed rows and column (XFD and 0x100000)
        ptgs = parse(fpb, "XFD1048576");
        assertEquals(1, ptgs.length);
        assertTrue("", ptgs[0] instanceof RefPtg);


        //column greater than XFD
        try {
            ptgs = parse(fpb, "XFE10");
            fail("expected exception");
        } catch (FormulaParseException e){
            assertEquals("Specified named range 'XFE10' does not exist in the current workbook.", e.getMessage());
        }

        //row greater than 0x100000
        try {
            ptgs = parse(fpb, "XFD1048577");
            fail("expected exception");
        } catch (FormulaParseException e){
            assertEquals("Specified named range 'XFD1048577' does not exist in the current workbook.", e.getMessage());
        }
    }

	@Test
    public void builtInFormulas() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        ptgs = parse(fpb, "LOG10");
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));

        ptgs = parse(fpb, "LOG10(100)");
        assertEquals(2, ptgs.length);
        assertTrue("", ptgs[0] instanceof IntPtg);
        assertTrue("", ptgs[1] instanceof FuncPtg);
    }
    
	@Test
	@Ignore("Work in progress, see bug #56737")
    public void formulaReferencesOtherSheets() {
        // Use a test file with the named ranges in place
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // Reference to a single cell in a different sheet
        ptgs = parse(fpb, "Uses!A1");
        assertEquals(1, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals("A1", ((Ref3DPxg)ptgs[0]).format2DRefAsString());
        assertEquals("Uses!A1", ((Ref3DPxg)ptgs[0]).toFormulaString());
        
        // Reference to a sheet scoped named range from another sheet
        ptgs = parse(fpb, "Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        // TODO assert
        
        // Reference to a workbook scoped named range
        ptgs = parse(fpb, "NR_Global_B2");
        assertEquals(1, ptgs.length);
        // TODO assert
    }
    
    @Test
    @Ignore("Work in progress, see bug #56737")
    public void fFormaulReferncesSameWorkbook() {
        // Use a test file with "other workbook" style references
        //  to itself
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;
        
        // Reference to a named range in our own workbook, as if it
        // were defined in a different workbook
        ptgs = parse(fpb, "[0]!NR_Global_B2");
        assertEquals(1, ptgs.length);
        // TODO assert
    }
    
    @Test
    @Ignore("Work in progress, see bug #56737")
    public void formulaReferencesOtherWorkbook() {
        // Use a test file with the external linked table in place
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // Reference to a single cell in a different workbook
        ptgs = parse(fpb, "[1]Uses!$A$1");
        assertEquals(1, ptgs.length);
        // TODO assert
        
        // Reference to a sheet-scoped named range in a different workbook
        ptgs = parse(fpb, "[1]Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        // TODO assert
        
        // Reference to a global named range in a different workbook
        ptgs = parse(fpb, "[1]!NR_Global_B2");
        assertEquals(1, ptgs.length);
        // TODO assert
    }
}
