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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for 'Analysis Toolpak' function SWITCH()
 *
 * @author Pieter Degraeuwe
 */
public class TestSwitch {

    /**
     * =SWITCH(A1, "A", "Value for A", "B", "Value for B" )
     * =SWITCH(A1, "A", "Value for A", "B", "Value for B", "Something else" )
     */
    @Test
    public void testEvaluate() {
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row1 = sh.createRow(0);

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        // Create cells
        row1.createCell(0, CellType.STRING);

        // Create references
        CellReference a1Ref = new CellReference("A1");

        // Set values
        final Cell cellA1 = sh.getRow(a1Ref.getRow()).getCell(a1Ref.getCol());


        Cell cell1 = row1.createCell(1);
        cell1.setCellFormula("SWITCH(A1, \"A\",\"Value for A\", \"B\",\"Value for B\", \"Something else\")");


        cellA1.setCellValue("A");
        assertEquals(CellType.STRING, evaluator.evaluate(cell1).getCellType());
        assertEquals("Value for A", evaluator.evaluate(cell1).getStringValue(),
                "SWITCH should return 'Value for A'");

        cellA1.setCellValue("B");
        evaluator.clearAllCachedResultValues();
        assertEquals(CellType.STRING, evaluator.evaluate(cell1).getCellType());
        assertEquals("Value for B", evaluator.evaluate(cell1).getStringValue(),
                "SWITCH should return 'Value for B'");

        cellA1.setCellValue("");
        evaluator.clearAllCachedResultValues();
        assertEquals(CellType.STRING, evaluator.evaluate(cell1).getCellType());
        assertEquals("Something else", evaluator.evaluate(cell1).getStringValue(),
                "SWITCH should return 'Something else'");

    }

}
