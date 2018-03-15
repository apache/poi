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

import junit.framework.TestCase;

/**
 * Testcase for 'Analysis Toolpak' function IFERROR()
 * 
 * @author Johan Karlsteen
 */
public class TestIfError extends TestCase {

    /**
     * =IFERROR(210/35,\"Error in calculation\")"  Divides 210 by 35 and returns 6.0
     * =IFERROR(55/0,\"Error in calculation\")"    Divides 55 by 0 and returns the error text
     * =IFERROR(C1,\"Error in calculation\")"      References the result of dividing 55 by 0 and returns the error text
     */
    public static void testEvaluate(){
        Workbook wb = new HSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row row1 = sh.createRow(0);
        Row row2 = sh.createRow(1);

        // Create cells
        row1.createCell(0, CellType.NUMERIC);
        row1.createCell(1, CellType.NUMERIC);
        row1.createCell(2, CellType.NUMERIC);
        row2.createCell(0, CellType.NUMERIC);
        row2.createCell(1, CellType.NUMERIC);

        // Create references
        CellReference a1 = new CellReference("A1");
        CellReference a2 = new CellReference("A2");
        CellReference b1 = new CellReference("B1");
        CellReference b2 = new CellReference("B2");
        CellReference c1 = new CellReference("C1");
        
        // Set values
        sh.getRow(a1.getRow()).getCell(a1.getCol()).setCellValue(210);
        sh.getRow(a2.getRow()).getCell(a2.getCol()).setCellValue(55);
        sh.getRow(b1.getRow()).getCell(b1.getCol()).setCellValue(35);
        sh.getRow(b2.getRow()).getCell(b2.getCol()).setCellValue(0);
        sh.getRow(c1.getRow()).getCell(c1.getCol()).setCellFormula("A1/B2");
        
        Cell cell1 = sh.createRow(3).createCell(0);
        cell1.setCellFormula("IFERROR(A1/B1,\"Error in calculation\")");
        Cell cell2 = sh.createRow(3).createCell(0);
        cell2.setCellFormula("IFERROR(A2/B2,\"Error in calculation\")");
        Cell cell3 = sh.createRow(3).createCell(0);
        cell3.setCellFormula("IFERROR(C1,\"error\")");
        
        double accuracy = 1E-9;

        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        assertEquals("Checks that the cell is numeric",
        		CellType.NUMERIC, evaluator.evaluate(cell1).getCellType());
        assertEquals("Divides 210 by 35 and returns 6.0",
                6.0, evaluator.evaluate(cell1).getNumberValue(), accuracy);
        
        
        assertEquals("Checks that the cell is numeric",
        		CellType.STRING, evaluator.evaluate(cell2).getCellType());
        assertEquals("Rounds -10 to a nearest multiple of -3 (-9)",
                "Error in calculation", evaluator.evaluate(cell2).getStringValue());
        
        assertEquals("Check that C1 returns string", 
        		CellType.STRING, evaluator.evaluate(cell3).getCellType());
        assertEquals("Check that C1 returns string \"error\"", 
        		"error", evaluator.evaluate(cell3).getStringValue());
    }
}
