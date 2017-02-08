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

package org.apache.poi.ss.formula.functions;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import junit.framework.TestCase;

/**
 * LogicalFunction unit tests.
 */
public class TestLogicalFunction extends TestCase {

    private FormulaEvaluator evaluator;
    private Row row3;
    private Cell cell1;
    private Cell cell2;

    @Override
    public void setUp() throws IOException {
        Workbook wb = new HSSFWorkbook();
        try {
            buildWorkbook(wb);
        } finally {
            wb.close();
        }
    }

    private void buildWorkbook(Workbook wb) {
        Sheet sh = wb.createSheet();
        Row row1 = sh.createRow(0);
        Row row2 = sh.createRow(1);
        row3 = sh.createRow(2);

        row1.createCell(0, CellType.NUMERIC);
        row1.createCell(1, CellType.NUMERIC);

        row2.createCell(0, CellType.NUMERIC);
        row2.createCell(1, CellType.NUMERIC);

        row3.createCell(0);
        row3.createCell(1);

        CellReference a1 = new CellReference("A1");
        CellReference a2 = new CellReference("A2");
        CellReference b1 = new CellReference("B1");
        CellReference b2 = new CellReference("B2");

        sh.getRow(a1.getRow()).getCell(a1.getCol()).setCellValue(35);
        sh.getRow(a2.getRow()).getCell(a2.getCol()).setCellValue(0);
        sh.getRow(b1.getRow()).getCell(b1.getCol()).setCellFormula("A1/A2");
        sh.getRow(b2.getRow()).getCell(b2.getCol()).setCellFormula("NA()");

        evaluator = wb.getCreationHelper().createFormulaEvaluator();
    }

    public void testIsErr() {
        cell1 = row3.createCell(0);
        cell1.setCellFormula("ISERR(B1)"); // produces #DIV/0!
        cell2 = row3.createCell(1);
        cell2.setCellFormula("ISERR(B2)"); // produces #N/A

        CellValue cell1Value = evaluator.evaluate(cell1);
        CellValue cell2Value = evaluator.evaluate(cell2);

        assertEquals(true, cell1Value.getBooleanValue());
        assertEquals(false, cell2Value.getBooleanValue());
    }

    public void testIsError() {
        cell1 = row3.createCell(0);
        cell1.setCellFormula("ISERROR(B1)"); // produces #DIV/0!
        cell2 = row3.createCell(1);
        cell2.setCellFormula("ISERROR(B2)"); // produces #N/A

        CellValue cell1Value = evaluator.evaluate(cell1);
        CellValue cell2Value = evaluator.evaluate(cell2);

        assertEquals(true, cell1Value.getBooleanValue());
        assertEquals(true, cell2Value.getBooleanValue());
    }
}
