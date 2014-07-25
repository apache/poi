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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestFormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

public final class TestXSSFFormulaEvaluation extends BaseTestFormulaEvaluator {

    public TestXSSFFormulaEvaluation() {
        super(XSSFITestDataProvider.instance);
    }

    public void testSharedFormulas(){
        baseTestSharedFormulas("shared_formulas.xlsx");
    }

    public void testSharedFormulas_evaluateInCell(){
        XSSFWorkbook wb = (XSSFWorkbook)_testDataProvider.openSampleWorkbook("49872.xlsx");
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        XSSFSheet sheet = wb.getSheetAt(0);

        double result = 3.0;

        // B3 is a master shared formula, C3 and D3 don't have the formula written in their f element.
        // Instead, the attribute si for a particular cell is used to figure what the formula expression
        // should be based on the cell's relative location to the master formula, e.g.
        // B3:        <f t="shared" ref="B3:D3" si="0">B1+B2</f>
        // C3 and D3: <f t="shared" si="0"/>

        // get B3 and evaluate it in the cell
        XSSFCell b3 = sheet.getRow(2).getCell(1);
        assertEquals(result, evaluator.evaluateInCell(b3).getNumericCellValue());

        //at this point the master formula is gone, but we are still able to evaluate dependent cells
        XSSFCell c3 = sheet.getRow(2).getCell(2);
        assertEquals(result, evaluator.evaluateInCell(c3).getNumericCellValue());

        XSSFCell d3 = sheet.getRow(2).getCell(3);
        assertEquals(result, evaluator.evaluateInCell(d3).getNumericCellValue());
    }

    /**
     * Evaluation of cell references with column indexes greater than 255. See bugzilla 50096
     */
    public void testEvaluateColumnGreaterThan255() {
        XSSFWorkbook wb = (XSSFWorkbook) _testDataProvider.openSampleWorkbook("50096.xlsx");
        XSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        /**
         *  The first row simply contains the numbers 1 - 300.
         *  The second row simply refers to the cell value above in the first row by a simple formula.
         */
        for (int i = 245; i < 265; i++) {
            XSSFCell cell_noformula = wb.getSheetAt(0).getRow(0).getCell(i);
            XSSFCell cell_formula = wb.getSheetAt(0).getRow(1).getCell(i);

            CellReference ref_noformula = new CellReference(cell_noformula.getRowIndex(), cell_noformula.getColumnIndex());
            CellReference ref_formula = new CellReference(cell_noformula.getRowIndex(), cell_noformula.getColumnIndex());
            String fmla = cell_formula.getCellFormula();
            // assure that the formula refers to the cell above.
            // the check below is 'deep' and involves conversion of the shared formula:
            // in the sample file a shared formula in GN1 is spanned in the range GN2:IY2,
            assertEquals(ref_noformula.formatAsString(), fmla);

            CellValue cv_noformula = evaluator.evaluate(cell_noformula);
            CellValue cv_formula = evaluator.evaluate(cell_formula);
            assertEquals("Wrong evaluation result in " + ref_formula.formatAsString(),
                    cv_noformula.getNumberValue(), cv_formula.getNumberValue());
        }
    }
    
    /**
     * Related to bugs #56737 and #56752 - XSSF workbooks which have
     *  formulas that refer to cells and named ranges in multiple other
     *  workbooks, both HSSF and XSSF ones
     */
    public void testReferencesToOtherWorkbooks() throws Exception {
        XSSFWorkbook wb = (XSSFWorkbook) _testDataProvider.openSampleWorkbook("ref2-56737.xlsx");
        XSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        XSSFSheet s = wb.getSheetAt(0);
        
        // References to a .xlsx file
        Row rXSLX = s.getRow(2);
        Cell cXSLX_cell = rXSLX.getCell(4);
        Cell cXSLX_sNR  = rXSLX.getCell(6);
        Cell cXSLX_gNR  = rXSLX.getCell(8);
        assertEquals("[1]Uses!$A$1",        cXSLX_cell.getCellFormula());
        assertEquals("[1]Defines!NR_To_A1", cXSLX_sNR.getCellFormula());
        assertEquals("[1]!NR_Global_B2",    cXSLX_gNR.getCellFormula());
        
        assertEquals("Hello!", cXSLX_cell.getStringCellValue());
        assertEquals("Test A1", cXSLX_sNR.getStringCellValue());
        assertEquals(142.0, cXSLX_gNR.getNumericCellValue());
        
        // References to a .xls file
        Row rXSL = s.getRow(4);
        Cell cXSL_cell = rXSL.getCell(4);
        Cell cXSL_sNR  = rXSL.getCell(6);
        Cell cXSL_gNR  = rXSL.getCell(8);
        assertEquals("[2]Uses!$C$1",        cXSL_cell.getCellFormula());
        assertEquals("[2]Defines!NR_To_A1", cXSL_sNR.getCellFormula());
        assertEquals("[2]!NR_Global_B2",    cXSL_gNR.getCellFormula());
        
        assertEquals("Hello!", cXSL_cell.getStringCellValue());
        assertEquals("Test A1", cXSL_sNR.getStringCellValue());
        assertEquals(142.0, cXSL_gNR.getNumericCellValue());
        
        // Try to evaluate without references, won't work
        // (At least, not unit we fix bug #56752 that is)
        try {
            evaluator.evaluate(cXSL_cell);
            fail("Without a fix for #56752, shouldn't be able to evaluate a " +
                 "reference to a non-provided linked workbook");
        } catch(Exception e) {}
        
        // Setup the environment
        Map<String,FormulaEvaluator> evaluators = new HashMap<String, FormulaEvaluator>();
        evaluators.put("ref2-56737.xlsx", evaluator);
        evaluators.put("56737.xlsx", 
                _testDataProvider.openSampleWorkbook("56737.xlsx").getCreationHelper().createFormulaEvaluator());
        evaluators.put("56737.xls", 
                HSSFTestDataSamples.openSampleWorkbook("56737.xls").getCreationHelper().createFormulaEvaluator());
        evaluator.setupReferencedWorkbooks(evaluators);
        
        // Try evaluating all of them, ensure we don't blow up
        for(Row r : s) {
            for (Cell c : r) {
                evaluator.evaluate(c);
            }
        }
        
        // Evaluate and check results
        assertEquals("\"Hello!\"",  evaluator.evaluate(cXSLX_cell).formatAsString());
        assertEquals("\"Test A1\"", evaluator.evaluate(cXSLX_sNR).formatAsString());
        assertEquals("142.0",   evaluator.evaluate(cXSLX_gNR).formatAsString());

        assertEquals("\"Hello!\"",  evaluator.evaluate(cXSL_cell).formatAsString());
        assertEquals("\"Test A1\"", evaluator.evaluate(cXSL_sNR).formatAsString());
        assertEquals("142.0",   evaluator.evaluate(cXSL_gNR).formatAsString());
    }
    
    /**
     * If a formula references cells or named ranges in another workbook,
     *  but that isn't available at evaluation time, the cached values
     *  should be used instead
     * TODO Add the support then add a unit test
     * See bug #56752
     */
    public void TODOtestCachedReferencesToOtherWorkbooks() throws Exception {
        // TODO
    }
    
    /**
     * A handful of functions (such as SUM, COUNTA, MIN) support
     *  multi-sheet references (eg Sheet1:Sheet3!A1 = Cell A1 from
     *  Sheets 1 through Sheet 3).
     * This test, based on common test files for HSSF and XSSF, checks
     *  that we can correctly evaluate these
     */
    public void testMultiSheetReferencesHSSFandXSSF() throws Exception {
        Workbook[] wbs = new Workbook[] {
                HSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xls"),
                XSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xlsx")
        };
        for (Workbook wb : wbs) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet s1 = wb.getSheetAt(0);

            
            // Simple SUM over numbers
            Cell sumF = s1.getRow(2).getCell(0);
            assertNotNull(sumF);
            assertEquals("SUM(Sheet1:Sheet3!A1)", sumF.getCellFormula());
            assertEquals("Failed for " + wb.getClass(), "66.0", evaluator.evaluate(sumF).formatAsString());
            
            
            // Various Stats formulas on numbers
            Cell avgF = s1.getRow(2).getCell(1);
            assertNotNull(avgF);
            assertEquals("AVERAGE(Sheet1:Sheet3!A1)", avgF.getCellFormula());
            assertEquals("22.0", evaluator.evaluate(avgF).formatAsString());
            
            Cell minF = s1.getRow(3).getCell(1);
            assertNotNull(minF);
            assertEquals("MIN(Sheet1:Sheet3!A$1)", minF.getCellFormula());
            assertEquals("11.0", evaluator.evaluate(minF).formatAsString());
            
            Cell maxF = s1.getRow(4).getCell(1);
            assertNotNull(maxF);
            assertEquals("MAX(Sheet1:Sheet3!A$1)", maxF.getCellFormula());
            assertEquals("33.0", evaluator.evaluate(maxF).formatAsString());
            
            Cell countF = s1.getRow(5).getCell(1);
            assertNotNull(countF);
            assertEquals("COUNT(Sheet1:Sheet3!A$1)", countF.getCellFormula());
            assertEquals("3.0", evaluator.evaluate(countF).formatAsString());
            
            
            // Various CountAs on Strings
            Cell countA_1F = s1.getRow(2).getCell(2);
            assertNotNull(countA_1F);
            assertEquals("COUNTA(Sheet1:Sheet3!C1)", countA_1F.getCellFormula());
            assertEquals("3.0", evaluator.evaluate(countA_1F).formatAsString());
            
            Cell countA_2F = s1.getRow(2).getCell(3);
            assertNotNull(countA_2F);
            assertEquals("COUNTA(Sheet1:Sheet3!D1)", countA_2F.getCellFormula());
            assertEquals("0.0", evaluator.evaluate(countA_2F).formatAsString());
            
            Cell countA_3F = s1.getRow(2).getCell(4);
            assertNotNull(countA_3F);
            assertEquals("COUNTA(Sheet1:Sheet3!E1)", countA_3F.getCellFormula());
            assertEquals("3.0", evaluator.evaluate(countA_3F).formatAsString());
        }
    }
    /**
     * A handful of functions (such as SUM, COUNTA, MIN) support
     *  multi-sheet areas (eg Sheet1:Sheet3!A1:B2 = Cell A1 to Cell B2,
     *  from Sheets 1 through Sheet 3).
     * This test, based on common test files for HSSF and XSSF, checks
     *  that we can correctly evaluate these
     * 
     * DISABLED pending support, see bug #55906
     */
    public void DISABLEDtestMultiSheetAreasHSSFandXSSF() throws Exception {
        Workbook[] wbs = new Workbook[] {
                HSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xls"),
                XSSFTestDataSamples.openSampleWorkbook("55906-MultiSheetRefs.xlsx")
        };
        for (Workbook wb : wbs) {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet s1 = wb.getSheetAt(0);

            
            // SUM over a range
            Cell sumFA = s1.getRow(2).getCell(7);
            assertNotNull(sumFA);
            assertEquals("SUM(Sheet1:Sheet3!A1:B2)", sumFA.getCellFormula());
            assertEquals("110.0", evaluator.evaluate(sumFA).formatAsString());

            
            // Various Stats formulas on ranges of numbers
            Cell avgFA = s1.getRow(2).getCell(7);
            assertNotNull(avgFA);
            assertEquals("AVERAGE(Sheet1:Sheet3!A1:B2)", avgFA.getCellFormula());
            assertEquals("27.5", evaluator.evaluate(avgFA).formatAsString());
            
            Cell minFA = s1.getRow(3).getCell(8);
            assertNotNull(minFA);
            assertEquals("MIN(Sheet1:Sheet3!A$1:B$2)", minFA.getCellFormula());
            assertEquals("11.0", evaluator.evaluate(minFA).formatAsString());
            
            Cell maxFA = s1.getRow(4).getCell(8);
            assertNotNull(maxFA);
            assertEquals("MAX(Sheet1:Sheet3!A$1:B$2)", maxFA.getCellFormula());
            assertEquals("44.0", evaluator.evaluate(maxFA).formatAsString());
            
            Cell countFA = s1.getRow(5).getCell(8);
            assertNotNull(countFA);
            assertEquals("COUNT(Sheet1:Sheet3!$A$1:$B$2)", countFA.getCellFormula());
            assertEquals("4.0", evaluator.evaluate(countFA).formatAsString());
        }
    }
}
