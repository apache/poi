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
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFITestDataProvider;

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
        // TODO Fix XSSF reference evaluations to work
//        assertEquals("\"Test A1\"", evaluator.evaluate(cXSLX_sNR).formatAsString());
//        assertEquals("142.0",   evaluator.evaluate(cXSLX_gNR).formatAsString());

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
}
