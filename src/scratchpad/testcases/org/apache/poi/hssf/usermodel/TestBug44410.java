package org.apache.poi.hssf.usermodel;
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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.List;

import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.FuncVarPtg;

/**
 * Bug 44410: SUM(C:C) is valid in excel, and means a sum
 *  of all the rows in Column C
 *
 * @author Nick Burch
 */

public class TestBug44410 extends TestCase {
    protected String cwd = System.getProperty("HSSF.testdata.path");

    public void test44410() throws IOException {
        FileInputStream in = new FileInputStream(new File(cwd, "SingleLetterRanges.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(in);
        in.close();

        HSSFSheet sheet   = wb.getSheetAt(0);

        HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(sheet, wb);

        // =index(C:C,2,1)   -> 2
        HSSFRow rowIDX = (HSSFRow)sheet.getRow(3);
        // =sum(C:C)         -> 6
        HSSFRow rowSUM = (HSSFRow)sheet.getRow(4);
        // =sum(C:D)         -> 66
        HSSFRow rowSUM2D = (HSSFRow)sheet.getRow(5);
        
        // Test the sum
        HSSFCell cellSUM = rowSUM.getCell((short)0);
        
        FormulaRecordAggregate frec = 
        	(FormulaRecordAggregate)cellSUM.getCellValueRecord();
        List ops = frec.getFormulaRecord().getParsedExpression();
        assertEquals(2, ops.size());
        assertEquals(AreaPtg.class, ops.get(0).getClass());
        assertEquals(FuncVarPtg.class, ops.get(1).getClass());

        // Actually stored as C1 to C65536 
        //  (last row is -1 === 65535)
        AreaPtg ptg = (AreaPtg)ops.get(0);
        assertEquals(2, ptg.getFirstColumn());
        assertEquals(2, ptg.getLastColumn());
        assertEquals(0, ptg.getFirstRow());
        assertEquals(65535, ptg.getLastRow());
        assertEquals("C:C", ptg.toFormulaString(wb.getWorkbook()));
        
        // Will show as C:C, but won't know how many
        //  rows it covers as we don't have the sheet
        //  to hand when turning the Ptgs into a string
        assertEquals("SUM(C:C)", cellSUM.getCellFormula());
        eva.setCurrentRow(rowSUM);
        
        // But the evaluator knows the sheet, so it
        //  can do it properly
        assertEquals(6, eva.evaluate(cellSUM).getNumberValue(), 0);
        
        
        // Test the index
        // Again, the formula string will be right but
        //  lacking row count, evaluated will be right
        HSSFCell cellIDX = rowIDX.getCell((short)0);
        assertEquals("INDEX(C:C,2,1)", cellIDX.getCellFormula());
        eva.setCurrentRow(rowIDX);
        assertEquals(2, eva.evaluate(cellIDX).getNumberValue(), 0);
        
        // Across two colums
        HSSFCell cellSUM2D = rowSUM2D.getCell((short)0);
        assertEquals("SUM(C:D)", cellSUM2D.getCellFormula());
        eva.setCurrentRow(rowSUM2D);
        assertEquals(66, eva.evaluate(cellSUM2D).getNumberValue(), 0);
    }
}
