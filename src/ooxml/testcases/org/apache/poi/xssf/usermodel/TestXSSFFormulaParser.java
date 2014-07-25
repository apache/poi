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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

public final class TestXSSFFormulaParser {
	private static Ptg[] parse(FormulaParsingWorkbook fpb, String fmla) {
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
        
        // Formula referencing one cell
        ptgs = parse(fpb, "ISEVEN(A1)");
        assertEquals(3, ptgs.length);
        assertEquals(NameXPxg.class,   ptgs[0].getClass());
        assertEquals(RefPtg.class,     ptgs[1].getClass());
        assertEquals(FuncVarPtg.class, ptgs[2].getClass());
        assertEquals("ISEVEN", ptgs[0].toFormulaString());
        assertEquals("A1",     ptgs[1].toFormulaString());
        assertEquals("#external#", ptgs[2].toFormulaString());
        
        // Formula referencing an area
        ptgs = parse(fpb, "SUM(A1:B3)");
        assertEquals(2, ptgs.length);
        assertEquals(AreaPtg.class, ptgs[0].getClass());
        assertEquals(AttrPtg.class, ptgs[1].getClass());
        assertEquals("A1:B3", ptgs[0].toFormulaString());
        assertEquals("SUM",   ptgs[1].toFormulaString());
        
        // Formula referencing one cell in a different sheet
        ptgs = parse(fpb, "SUM(Sheet1!A1)");
        assertEquals(2, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals(AttrPtg.class,  ptgs[1].getClass());
        assertEquals("Sheet1!A1", ptgs[0].toFormulaString());
        assertEquals("SUM",       ptgs[1].toFormulaString());
        
        // Formula referencing an area in a different sheet
        ptgs = parse(fpb, "SUM(Sheet1!A1:B3)");
        assertEquals(2, ptgs.length);
        assertEquals(Area3DPxg.class,ptgs[0].getClass());
        assertEquals(AttrPtg.class,  ptgs[1].getClass());
        assertEquals("Sheet1!A1:B3", ptgs[0].toFormulaString());
        assertEquals("SUM",          ptgs[1].toFormulaString());
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
    public void formaulReferncesSameWorkbook() {
        // Use a test file with "other workbook" style references
        //  to itself
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // Reference to a named range in our own workbook, as if it
        // were defined in a different workbook
        ptgs = parse(fpb, "[0]!NR_Global_B2");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(0,    ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals(null, ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_Global_B2",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[0]!NR_Global_B2",((NameXPxg)ptgs[0]).toFormulaString());
    }
   
	@Test
    public void formulaReferencesOtherSheets() {
        // Use a test file with the named ranges in place
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // Reference to a single cell in a different sheet
        ptgs = parse(fpb, "Uses!A1");
        assertEquals(1, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals(-1,   ((Ref3DPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("A1", ((Ref3DPxg)ptgs[0]).format2DRefAsString());
        assertEquals("Uses!A1", ((Ref3DPxg)ptgs[0]).toFormulaString());
        
        // Reference to a single cell in a different sheet, which needs quoting
        ptgs = parse(fpb, "'Testing 47100'!A1");
        assertEquals(1, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals(-1,   ((Ref3DPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Testing 47100", ((Ref3DPxg)ptgs[0]).getSheetName());
        assertEquals("A1", ((Ref3DPxg)ptgs[0]).format2DRefAsString());
        assertEquals("'Testing 47100'!A1", ((Ref3DPxg)ptgs[0]).toFormulaString());
        
        // Reference to a sheet scoped named range from another sheet
        ptgs = parse(fpb, "Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(-1,        ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Defines", ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_To_A1",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("Defines!NR_To_A1",((NameXPxg)ptgs[0]).toFormulaString());
        
        // Reference to a workbook scoped named range
        ptgs = parse(fpb, "NR_Global_B2");
        assertEquals(1, ptgs.length);
        assertEquals(NamePtg.class, ptgs[0].getClass());
        assertEquals("NR_Global_B2",((NamePtg)ptgs[0]).toFormulaString(fpb));
    }
    
    @Test
    public void formulaReferencesOtherWorkbook() {
        // Use a test file with the external linked table in place
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("ref-56737.xlsx");
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // Reference to a single cell in a different workbook
        ptgs = parse(fpb, "[1]Uses!$A$1");
        assertEquals(1, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals(1,     ((Ref3DPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Uses",((Ref3DPxg)ptgs[0]).getSheetName());
        assertEquals("$A$1",((Ref3DPxg)ptgs[0]).format2DRefAsString());
        assertEquals("[1]Uses!$A$1",((Ref3DPxg)ptgs[0]).toFormulaString());
        
        // Reference to a sheet-scoped named range in a different workbook
        ptgs = parse(fpb, "[1]Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(1,         ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Defines", ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_To_A1",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[1]Defines!NR_To_A1",((NameXPxg)ptgs[0]).toFormulaString());
        
        // Reference to a global named range in a different workbook
        ptgs = parse(fpb, "[1]!NR_Global_B2");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(1,    ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals(null, ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_Global_B2",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[1]!NR_Global_B2",((NameXPxg)ptgs[0]).toFormulaString());
    }
    
    /**
     * A handful of functions (such as SUM, COUNTA, MIN) support
     *  multi-sheet references (eg Sheet1:Sheet3!A1 = Cell A1 from
     *  Sheets 1 through Sheet 3) and multi-sheet area references 
     *  (eg Sheet1:Sheet3!A1:B2 = Cells A1 through B2 from Sheets
     *   1 through Sheet 3).
     * This test, based on common test files for HSSF and XSSF, checks
     *  that we can read and parse these kinds of references 
     * (but not evaluate - that's elsewhere in the test suite)
     */
    @Test
    public void multiSheetReferencesHSSFandXSSF() throws Exception {
        Workbook[] wbs = new Workbook[] {
                HSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xls"),
                XSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xlsx")
        };
        for (Workbook wb : wbs) {
            Sheet s1 = wb.getSheetAt(0);
            Ptg[] ptgs;
            
            // Check the contents
            Cell sumF = s1.getRow(2).getCell(0);
            assertNotNull(sumF);
            assertEquals("SUM(Sheet1:Sheet3!A1)", sumF.getCellFormula());
            
            Cell avgF = s1.getRow(2).getCell(1);
            assertNotNull(avgF);
            assertEquals("AVERAGE(Sheet1:Sheet3!A1)", avgF.getCellFormula());
            
            Cell countAF = s1.getRow(2).getCell(2);
            assertNotNull(countAF);
            assertEquals("COUNTA(Sheet1:Sheet3!C1)", countAF.getCellFormula());
            
            Cell maxF = s1.getRow(4).getCell(1);
            assertNotNull(maxF);
            assertEquals("MAX(Sheet1:Sheet3!A$1)", maxF.getCellFormula());
            
            
            Cell sumFA = s1.getRow(2).getCell(7);
            assertNotNull(sumFA);
            assertEquals("SUM(Sheet1:Sheet3!A1:B2)", sumFA.getCellFormula());
            
            Cell avgFA = s1.getRow(2).getCell(8);
            assertNotNull(avgFA);
            assertEquals("AVERAGE(Sheet1:Sheet3!A1:B2)", avgFA.getCellFormula());
            
            Cell maxFA = s1.getRow(4).getCell(8);
            assertNotNull(maxFA);
            assertEquals("MAX(Sheet1:Sheet3!A$1:B$2)", maxFA.getCellFormula());
            
            Cell countFA = s1.getRow(5).getCell(8);
            assertNotNull(countFA);
            assertEquals("COUNT(Sheet1:Sheet3!$A$1:$B$2)", countFA.getCellFormula());
            
            
            // Create a formula parser
            FormulaParsingWorkbook fpb = null;
            if (wb instanceof HSSFWorkbook)
                fpb = HSSFEvaluationWorkbook.create((HSSFWorkbook)wb);
            else
                fpb = XSSFEvaluationWorkbook.create((XSSFWorkbook)wb);
            
            
            // Check things parse as expected:
            
            
            // SUM to one cell over 3 workbooks, relative reference
            ptgs = parse(fpb, "SUM(Sheet1:Sheet3!A1)");
            assertEquals(2, ptgs.length);
            if (wb instanceof HSSFWorkbook) {
                assertEquals(Ref3DPtg.class, ptgs[0].getClass());
            } else {
                assertEquals(Ref3DPxg.class, ptgs[0].getClass());
            }
            assertEquals("Sheet1:Sheet3!A1", toFormulaString(ptgs[0], fpb));
            assertEquals(AttrPtg.class, ptgs[1].getClass());
            assertEquals("SUM",         toFormulaString(ptgs[1], fpb));
            
            
            // MAX to one cell over 3 workbooks, absolute row reference
            ptgs = parse(fpb, "MAX(Sheet1:Sheet3!A$1)");
            assertEquals(2, ptgs.length);
            if (wb instanceof HSSFWorkbook) {
                assertEquals(Ref3DPtg.class, ptgs[0].getClass());
            } else {
                assertEquals(Ref3DPxg.class, ptgs[0].getClass());
            }
            assertEquals("Sheet1:Sheet3!A$1", toFormulaString(ptgs[0], fpb));
            assertEquals(FuncVarPtg.class, ptgs[1].getClass());
            assertEquals("MAX",            toFormulaString(ptgs[1], fpb));
            
            
            // MIN to one cell over 3 workbooks, absolute reference
            ptgs = parse(fpb, "MIN(Sheet1:Sheet3!$A$1)");
            assertEquals(2, ptgs.length);
            if (wb instanceof HSSFWorkbook) {
                assertEquals(Ref3DPtg.class, ptgs[0].getClass());
            } else {
                assertEquals(Ref3DPxg.class, ptgs[0].getClass());
            }
            assertEquals("Sheet1:Sheet3!$A$1", toFormulaString(ptgs[0], fpb));
            assertEquals(FuncVarPtg.class, ptgs[1].getClass());
            assertEquals("MIN",            toFormulaString(ptgs[1], fpb));
            
            
            // SUM to a range of cells over 3 workbooks
            ptgs = parse(fpb, "SUM(Sheet1:Sheet3!A1:B2)");
            assertEquals(2, ptgs.length);
            if (wb instanceof HSSFWorkbook) {
                assertEquals(Area3DPtg.class, ptgs[0].getClass());
            } else {
                assertEquals(Area3DPxg.class, ptgs[0].getClass());
            }
            assertEquals("Sheet1:Sheet3!A1:B2", toFormulaString(ptgs[0], fpb));
            assertEquals(AttrPtg.class, ptgs[1].getClass());
            assertEquals("SUM",         toFormulaString(ptgs[1], fpb));
            
            
            // MIN to a range of cells over 3 workbooks, absolute reference
            ptgs = parse(fpb, "MIN(Sheet1:Sheet3!$A$1:$B$2)");
            assertEquals(2, ptgs.length);
            if (wb instanceof HSSFWorkbook) {
                assertEquals(Area3DPtg.class, ptgs[0].getClass());
            } else {
                assertEquals(Area3DPxg.class, ptgs[0].getClass());
            }
            assertEquals("Sheet1:Sheet3!$A$1:$B$2", toFormulaString(ptgs[0], fpb));
            assertEquals(FuncVarPtg.class, ptgs[1].getClass());
            assertEquals("MIN",            toFormulaString(ptgs[1], fpb));
            
            
            // Check we can round-trip - try to set a new one to a new single cell
            Cell newF = s1.getRow(0).createCell(10, Cell.CELL_TYPE_FORMULA);
            newF.setCellFormula("SUM(Sheet2:Sheet3!A1)");
            assertEquals("SUM(Sheet2:Sheet3!A1)", newF.getCellFormula());
            
            // Check we can round-trip - try to set a new one to a cell range
            newF = s1.getRow(0).createCell(11, Cell.CELL_TYPE_FORMULA);
            newF.setCellFormula("MIN(Sheet1:Sheet2!A1:B2)");
            assertEquals("MIN(Sheet1:Sheet2!A1:B2)", newF.getCellFormula());
        }
    }
    private static String toFormulaString(Ptg ptg, FormulaParsingWorkbook wb) {
        if (ptg instanceof WorkbookDependentFormula) {
            return ((WorkbookDependentFormula)ptg).toFormulaString((FormulaRenderingWorkbook)wb);
        }
        return ptg.toFormulaString();
    }
}
