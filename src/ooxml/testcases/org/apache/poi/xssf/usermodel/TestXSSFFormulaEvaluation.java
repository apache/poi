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

import org.apache.poi.ss.usermodel.*;
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
}
