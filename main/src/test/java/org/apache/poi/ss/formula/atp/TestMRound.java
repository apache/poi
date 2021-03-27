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
package org.apache.poi.ss.formula.atp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

/**
 * Testcase for 'Analysis Toolpak' function MROUND()
 */
class TestMRound {

    /**
        =MROUND(10, 3) 	Rounds 10 to a nearest multiple of 3 (9)
        =MROUND(-10, -3) 	Rounds -10 to a nearest multiple of -3 (-9)
        =MROUND(1.3, 0.2) 	Rounds 1.3 to a nearest multiple of 0.2 (1.4)
        =MROUND(5, -2) 	Returns an error, because -2 and 5 have different signs (#NUM!)     *
     */
    @Test
    void testEvaluate(){
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Cell cell1 = sh.createRow(0).createCell(0);
        cell1.setCellFormula("MROUND(10, 3)");
        Cell cell2 = sh.createRow(0).createCell(0);
        cell2.setCellFormula("MROUND(-10, -3)");
        Cell cell3 = sh.createRow(0).createCell(0);
        cell3.setCellFormula("MROUND(1.3, 0.2)");
        Cell cell4 = sh.createRow(0).createCell(0);
        cell4.setCellFormula("MROUND(5, -2)");
        Cell cell5 = sh.createRow(0).createCell(0);
        cell5.setCellFormula("MROUND(5, 0)");

        double accuracy = 1E-9;

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        assertEquals(9.0, evaluator.evaluate(cell1).getNumberValue(), accuracy,
                     "Rounds 10 to a nearest multiple of 3 (9)");

        assertEquals(-9.0, evaluator.evaluate(cell2).getNumberValue(), accuracy,
                     "Rounds -10 to a nearest multiple of -3 (-9)");

        assertEquals(1.4, evaluator.evaluate(cell3).getNumberValue(), accuracy,
                     "Rounds 1.3 to a nearest multiple of 0.2 (1.4)");

        assertEquals(ErrorEval.NUM_ERROR.getErrorCode(), evaluator.evaluate(cell4).getErrorValue(),
                     "Returns an error, because -2 and 5 have different signs (#NUM!)");

        assertEquals(0.0, evaluator.evaluate(cell5).getNumberValue(), 0,
                     "Returns 0 because the multiple is 0");
    }
}
