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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;

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
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.IntersectionPtg;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
import org.apache.poi.ss.formula.ptg.MemFuncPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

public final class TestXSSFFormulaParser {
    private static Ptg[] parse(FormulaParsingWorkbook fpb, String fmla) {
        return FormulaParser.parse(fmla, fpb, FormulaType.CELL, -1);
    }
    private static Ptg[] parse(FormulaParsingWorkbook fpb, String fmla, int rowIndex) {
        return FormulaParser.parse(fmla, fpb, FormulaType.CELL, -1, rowIndex);
    }

    @Test
    void basicParsing() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        ptgs = parse(fpb, "ABC10");
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg, "Had " + Arrays.toString(ptgs));

        ptgs = parse(fpb, "A500000");
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg, "Had " + Arrays.toString(ptgs));

        ptgs = parse(fpb, "ABC500000");
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg, "Had " + Arrays.toString(ptgs));

        //highest allowed rows and column (XFD and 0x100000)
        ptgs = parse(fpb, "XFD1048576");
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg, "Had " + Arrays.toString(ptgs));


        //column greater than XFD
        FormulaParseException e;
        e = assertThrows(FormulaParseException.class, () -> parse(fpb, "XFE10"));
        assertEquals("Specified named range 'XFE10' does not exist in the current workbook.", e.getMessage());

        //row greater than 0x100000
        e = assertThrows(FormulaParseException.class, () -> parse(fpb, "XFD1048577"));
        assertEquals("Specified named range 'XFD1048577' does not exist in the current workbook.", e.getMessage());

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

        wb.close();
    }

    @Test
    void builtInFormulas() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        ptgs = parse(fpb, "LOG10");
        assertEquals(1, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);

        ptgs = parse(fpb, "LOG10(100)");
        assertEquals(2, ptgs.length);
        assertTrue(ptgs[0] instanceof IntPtg);
        assertTrue(ptgs[1] instanceof FuncPtg);

        wb.close();
    }

    @Test
    void formulaReferencesSameWorkbook() throws IOException {
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
        assertNull(((NameXPxg) ptgs[0]).getSheetName());
        assertEquals("NR_Global_B2",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[0]!NR_Global_B2", ptgs[0].toFormulaString());

        wb.close();
    }

    @Test
    void formulaReferencesOtherSheets() throws IOException {
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
        assertEquals("Uses!A1", ptgs[0].toFormulaString());

        // Reference to a single cell in a different sheet, which needs quoting
        ptgs = parse(fpb, "'Testing 47100'!A1");
        assertEquals(1, ptgs.length);
        assertEquals(Ref3DPxg.class, ptgs[0].getClass());
        assertEquals(-1,   ((Ref3DPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Testing 47100", ((Ref3DPxg)ptgs[0]).getSheetName());
        assertEquals("A1", ((Ref3DPxg)ptgs[0]).format2DRefAsString());
        assertEquals("'Testing 47100'!A1", ptgs[0].toFormulaString());

        // Reference to a sheet scoped named range from another sheet
        ptgs = parse(fpb, "Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(-1,        ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Defines", ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_To_A1",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("Defines!NR_To_A1", ptgs[0].toFormulaString());

        // Reference to a workbook scoped named range
        ptgs = parse(fpb, "NR_Global_B2");
        assertEquals(1, ptgs.length);
        assertEquals(NamePtg.class, ptgs[0].getClass());
        assertEquals("NR_Global_B2",((NamePtg)ptgs[0]).toFormulaString(fpb));

        wb.close();
    }

    @Test
    void formulaReferencesOtherWorkbook() throws IOException {
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
        assertEquals("[1]Uses!$A$1", ptgs[0].toFormulaString());

        // Reference to a sheet-scoped named range in a different workbook
        ptgs = parse(fpb, "[1]Defines!NR_To_A1");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(1,         ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertEquals("Defines", ((NameXPxg)ptgs[0]).getSheetName());
        assertEquals("NR_To_A1",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[1]Defines!NR_To_A1", ptgs[0].toFormulaString());

        // Reference to a global named range in a different workbook
        ptgs = parse(fpb, "[1]!NR_Global_B2");
        assertEquals(1, ptgs.length);
        assertEquals(NameXPxg.class, ptgs[0].getClass());
        assertEquals(1,    ((NameXPxg)ptgs[0]).getExternalWorkbookNumber());
        assertNull(((NameXPxg) ptgs[0]).getSheetName());
        assertEquals("NR_Global_B2",((NameXPxg)ptgs[0]).getNameName());
        assertEquals("[1]!NR_Global_B2", ptgs[0].toFormulaString());

        wb.close();
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
    void multiSheetReferencesHSSFandXSSF() throws IOException {
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
            final FormulaParsingWorkbook fpb;
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
            Cell newF = s1.getRow(0).createCell(10, CellType.FORMULA);
            newF.setCellFormula("SUM(Sheet2:Sheet3!A1)");
            assertEquals("SUM(Sheet2:Sheet3!A1)", newF.getCellFormula());

            // Check we can round-trip - try to set a new one to a cell range
            newF = s1.getRow(0).createCell(11, CellType.FORMULA);
            newF.setCellFormula("MIN(Sheet1:Sheet2!A1:B2)");
            assertEquals("MIN(Sheet1:Sheet2!A1:B2)", newF.getCellFormula());

            wb.close();
        }
    }

    private static String toFormulaString(Ptg ptg, FormulaParsingWorkbook wb) {
        if (ptg instanceof WorkbookDependentFormula) {
            return ((WorkbookDependentFormula)ptg).toFormulaString((FormulaRenderingWorkbook)wb);
        }
        return ptg.toFormulaString();
    }

    @Test
    void test58648Single() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
            Ptg[] ptgs;

            ptgs = parse(fpb, "(ABC10 )");
            assertEquals(2, ptgs.length, "Had: " + Arrays.toString(ptgs));
            assertTrue(ptgs[0] instanceof RefPtg, "Had " + Arrays.toString(ptgs));
            assertTrue(ptgs[1] instanceof ParenthesisPtg, "Had " + Arrays.toString(ptgs));
        }
    }

    @Test
    void test58648Basic() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // verify whitespaces in different places
        ptgs = parse(fpb, "(ABC10)");
        assertEquals(2, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);

        ptgs = parse(fpb, "( ABC10)");
        assertEquals(2, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);

        ptgs = parse(fpb, "(ABC10 )");
        assertEquals(2, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);

        ptgs = parse(fpb, "((ABC10))");
        assertEquals(3, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);
        assertTrue(ptgs[2] instanceof ParenthesisPtg);

        ptgs = parse(fpb, "((ABC10) )");
        assertEquals(3, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);
        assertTrue(ptgs[2] instanceof ParenthesisPtg);

        ptgs = parse(fpb, "( (ABC10))");
        assertEquals(3, ptgs.length);
        assertTrue(ptgs[0] instanceof RefPtg);
        assertTrue(ptgs[1] instanceof ParenthesisPtg);
        assertTrue(ptgs[2] instanceof ParenthesisPtg);

        wb.close();
    }

    @Test
    void test58648FormulaParsing() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("58648.xlsx");

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet xsheet = wb.getSheetAt(i);

            for (Row row : xsheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        try {
                            evaluator.evaluateFormulaCell(cell);
                        } catch (Exception e) {
                            CellReference cellRef = new CellReference(cell.getRowIndex(), cell.getColumnIndex());
                            throw new RuntimeException("error at: " + cellRef, e);
                        }
                    }
                }
            }
        }

        Sheet sheet = wb.getSheet("my-sheet");
        Cell cell = sheet.getRow(1).getCell(4);

        assertEquals(5d, cell.getNumericCellValue(), 0d);

        wb.close();
    }

    @Test
    void testWhitespaceInFormula() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // verify whitespaces in different places
        ptgs = parse(fpb, "INTERCEPT(A2:A5, B2:B5)");
        assertEquals(3, ptgs.length);
        assertTrue(ptgs[0] instanceof AreaPtg);
        assertTrue(ptgs[1] instanceof AreaPtg);
        assertTrue(ptgs[2] instanceof FuncPtg);

        ptgs = parse(fpb, " INTERCEPT ( \t \r A2 : \nA5 , B2 : B5 ) \t");
        assertEquals(3, ptgs.length);
        assertTrue(ptgs[0] instanceof AreaPtg);
        assertTrue(ptgs[1] instanceof AreaPtg);
        assertTrue(ptgs[2] instanceof FuncPtg);

        ptgs = parse(fpb, "(VLOOKUP(\"item1\", A2:B3, 2, FALSE) - VLOOKUP(\"item2\", A2:B3, 2, FALSE) )");
        assertEquals(12, ptgs.length);
        assertTrue(ptgs[0] instanceof StringPtg);
        assertTrue(ptgs[1] instanceof AreaPtg);
        assertTrue(ptgs[2] instanceof IntPtg);

        ptgs = parse(fpb, "A1:B1 B1:B2");
        assertEquals(4, ptgs.length);
        assertTrue(ptgs[0] instanceof MemAreaPtg);
        assertTrue(ptgs[1] instanceof AreaPtg);
        assertTrue(ptgs[2] instanceof AreaPtg);
        assertTrue(ptgs[3] instanceof IntersectionPtg);

        ptgs = parse(fpb, "A1:B1    B1:B2");
        assertEquals(4, ptgs.length);
        assertTrue(ptgs[0] instanceof MemAreaPtg);
        assertTrue(ptgs[1] instanceof AreaPtg);
        assertTrue(ptgs[2] instanceof AreaPtg);
        assertTrue(ptgs[3] instanceof IntersectionPtg);

        wb.close();
    }

    @Test
    void testWhitespaceInComplexFormula() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        // verify whitespaces in different places
        ptgs = parse(fpb, "SUM(A1:INDEX(1:1048576,MAX(IFERROR(MATCH(99^99,B:B,1),0),IFERROR(MATCH(\"zzzz\",B:B,1),0)),MAX(IFERROR(MATCH(99^99,1:1,1),0),IFERROR(MATCH(\"zzzz\",1:1,1),0))))");
        assertEquals(40, ptgs.length);
        assertTrue(ptgs[0] instanceof MemFuncPtg);
        assertTrue(ptgs[1] instanceof RefPtg);
        assertTrue(ptgs[2] instanceof AreaPtg);
        assertTrue(ptgs[3] instanceof NameXPxg);

        ptgs = parse(fpb, "SUM ( A1 : INDEX( 1 : 1048576 , MAX( IFERROR ( MATCH ( 99 ^ 99 , B : B , 1 ) , 0 ) , IFERROR ( MATCH ( \"zzzz\" , B:B , 1 ) , 0 ) ) , MAX ( IFERROR ( MATCH ( 99 ^ 99 , 1 : 1 , 1 ) , 0 ) , IFERROR ( MATCH ( \"zzzz\" , 1 : 1 , 1 )   , 0 )   )   )   )");
        assertEquals(40, ptgs.length);
        assertTrue(ptgs[0] instanceof MemFuncPtg);
        assertTrue(ptgs[1] instanceof RefPtg);
        assertTrue(ptgs[2] instanceof AreaPtg);
        assertTrue(ptgs[3] instanceof NameXPxg);

        wb.close();
    }

    @Test
    void parseStructuredReferences() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("StructuredReferences.xlsx");

        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs;

        /*
        The following cases are tested (copied from FormulaParser.parseStructuredReference)
           1 Table1[col]
           2 Table1[[#Totals],[col]]
           3 Table1[#Totals]
           4 Table1[#All]
           5 Table1[#Data]
           6 Table1[#Headers]
           7 Table1[#Totals]
           8 Table1[#This Row]
           9 Table1[[#All],[col]]
          10 Table1[[#Headers],[col]]
          11 Table1[[#Totals],[col]]
          12 Table1[[#All],[col1]:[col2]]
          13 Table1[[#Data],[col1]:[col2]]
          14 Table1[[#Headers],[col1]:[col2]]
          15 Table1[[#Totals],[col1]:[col2]]
          16 Table1[[#Headers],[#Data],[col2]]
          17 Table1[[#This Row], [col1]]
          18 Table1[ [col1]:[col2] ]
        */

        final String tbl = "\\_Prime.1";
        final String noTotalsRowReason = ": Tables without a Totals row should return #REF! on [#Totals]";

        ////// Case 1: Evaluate Table1[col] with apostrophe-escaped #-signs ////////
        ptgs = parse(fpb, "SUM("+tbl+"[calc='#*'#])");
        assertEquals(2, ptgs.length);

        // Area3DPxg [sheet=Table ! A2:A7]
        assertTrue(ptgs[0] instanceof Area3DPxg);
        Area3DPxg ptg0 = (Area3DPxg) ptgs[0];
        assertEquals("Table", ptg0.getSheetName());
        assertEquals("A2:A7", ptg0.format2DRefAsString());
        // Note: structured references are evaluated and resolved to regular 3D area references.
        assertEquals("Table!A2:A7", ptg0.toFormulaString());

        // AttrPtg [sum ]
        assertTrue(ptgs[1] instanceof AttrPtg);
        AttrPtg ptg1 = (AttrPtg) ptgs[1];
        assertTrue(ptg1.isSum());

        ////// Case 1: Evaluate "Table1[col]" ////////
        ptgs = parse(fpb, tbl+"[Name]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!B2:B7", ptgs[0].toFormulaString(), "Table1[col]");

        ////// Case 2: Evaluate "Table1[[#Totals],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#Totals],[col]]");
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.REF_INVALID, ptgs[0], "Table1[[#Totals],[col]]" + noTotalsRowReason);

        ////// Case 3: Evaluate "Table1[#Totals]" ////////
        ptgs = parse(fpb, tbl+"[#Totals]");
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.REF_INVALID, ptgs[0], "Table1[#Totals]" + noTotalsRowReason);

        ////// Case 4: Evaluate "Table1[#All]" ////////
        ptgs = parse(fpb, tbl+"[#All]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!A1:C7", ptgs[0].toFormulaString(), "Table1[#All]");

        ////// Case 5: Evaluate "Table1[#Data]" (excludes Header and Data rows) ////////
        ptgs = parse(fpb, tbl+"[#Data]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!A2:C7", ptgs[0].toFormulaString(), "Table1[#Data]");

        ////// Case 6: Evaluate "Table1[#Headers]" ////////
        ptgs = parse(fpb, tbl+"[#Headers]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!A1:C1", ptgs[0].toFormulaString(), "Table1[#Headers]");

        ////// Case 7: Evaluate "Table1[#Totals]" ////////
        ptgs = parse(fpb, tbl+"[#Totals]");
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.REF_INVALID, ptgs[0], "Table1[#Totals]" + noTotalsRowReason);

        ////// Case 8: Evaluate "Table1[#This Row]" ////////
        ptgs = parse(fpb, tbl+"[#This Row]", 2);
        assertEquals(1, ptgs.length);
        assertEquals("Table!A3:C3", ptgs[0].toFormulaString(), "Table1[#This Row]");

        ////// Evaluate "Table1[@]" (equivalent to "Table1[#This Row]") ////////
        ptgs = parse(fpb, tbl+"[@]", 2);
        assertEquals(1, ptgs.length);
        assertEquals("Table!A3:C3", ptgs[0].toFormulaString());

        ////// Evaluate "Table1[#This Row]" when rowIndex is outside Table ////////
        ptgs = parse(fpb, tbl+"[#This Row]", 10);
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.VALUE_INVALID, ptgs[0], "Table1[#This Row]");

        ////// Evaluate "Table1[@]" when rowIndex is outside Table ////////
        ptgs = parse(fpb, tbl+"[@]", 10);
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.VALUE_INVALID, ptgs[0], "Table1[@]");

        ////// Evaluate "Table1[[#Data],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#Data], [Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!C2:C7", ptgs[0].toFormulaString(), "Table1[[#Data],[col]]");


        ////// Case 9: Evaluate "Table1[[#All],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#All], [Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!C1:C7", ptgs[0].toFormulaString(), "Table1[[#All],[col]]");

        ////// Case 10: Evaluate "Table1[[#Headers],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#Headers], [Number]]");
        assertEquals(1, ptgs.length);
        // also acceptable: Table1!B1
        assertEquals("Table!C1:C1", ptgs[0].toFormulaString(), "Table1[[#Headers],[col]]");

        ////// Case 11: Evaluate "Table1[[#Totals],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#Totals],[Name]]");
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.REF_INVALID, ptgs[0], "Table1[[#Totals],[col]]" + noTotalsRowReason);

        ////// Case 12: Evaluate "Table1[[#All],[col1]:[col2]]" ////////
        ptgs = parse(fpb, tbl+"[[#All], [Name]:[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!B1:C7", ptgs[0].toFormulaString(), "Table1[[#All],[col1]:[col2]]");

        ////// Case 13: Evaluate "Table1[[#Data],[col]:[col2]]" ////////
        ptgs = parse(fpb, tbl+"[[#Data], [Name]:[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!B2:C7", ptgs[0].toFormulaString(), "Table1[[#Data],[col]:[col2]]");

        ////// Case 14: Evaluate "Table1[[#Headers],[col1]:[col2]]" ////////
        ptgs = parse(fpb, tbl+"[[#Headers], [Name]:[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!B1:C1", ptgs[0].toFormulaString(), "Table1[[#Headers],[col1]:[col2]]");

        ////// Case 15: Evaluate "Table1[[#Totals],[col]:[col2]]" ////////
        ptgs = parse(fpb, tbl+"[[#Totals], [Name]:[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals(ErrPtg.REF_INVALID, ptgs[0], "Table1[[#Totals],[col]:[col2]]" + noTotalsRowReason);

        ////// Case 16: Evaluate "Table1[[#Headers],[#Data],[col]]" ////////
        ptgs = parse(fpb, tbl+"[[#Headers],[#Data],[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!C1:C7", ptgs[0].toFormulaString(), "Table1[[#Headers],[#Data],[col]]");

        ////// Case 17: Evaluate "Table1[[#This Row], [col1]]" ////////
        ptgs = parse(fpb, tbl+"[[#This Row], [Number]]", 2);
        assertEquals(1, ptgs.length);
        // also acceptable: Table!C3
        assertEquals("Table!C3:C3", ptgs[0].toFormulaString(), "Table1[[#This Row], [col1]]");

        ////// Case 18: Evaluate "Table1[[col]:[col2]]" ////////
        ptgs = parse(fpb, tbl+"[[Name]:[Number]]");
        assertEquals(1, ptgs.length);
        assertEquals("Table!B2:C7", ptgs[0].toFormulaString(), "Table1[[col]:[col2]]");

        wb.close();
    }
}
