/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestFormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assume;
import org.junit.Test;

/**
 * Formula Evaluation with SXSSF.
 * 
 * Note that SXSSF can only evaluate formulas where the
 *  cell is in the current window, and all references
 *  from the cell are in the current window
 */
public final class TestSXSSFFormulaEvaluation  extends BaseTestFormulaEvaluator {

    public TestSXSSFFormulaEvaluation() {
        super(SXSSFITestDataProvider.instance);
    }

    /**
     * EvaluateAll will normally fail, as any reference or
     *  formula outside of the window will fail, and any
     *  non-active sheets will fail
     */
    @Test
    public void testEvaluateAllFails() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(5);
        SXSSFSheet s = wb.createSheet();
        
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        
        s.createRow(0).createCell(0).setCellFormula("1+2");
        s.createRow(1).createCell(0).setCellFormula("A21");
        for (int i=2; i<19; i++) { s.createRow(i); }
        
        // Cells outside window will fail, whether referenced or not
        s.createRow(19).createCell(0).setCellFormula("A1+A2");
        s.createRow(20).createCell(0).setCellFormula("A1+A11+100");
        try {
            eval.evaluateAll();
            fail("Evaluate All shouldn't work, as some cells outside the window");
        } catch(SXSSFFormulaEvaluator.RowFlushedException e) {
            // Expected
        }
        
        
        // Inactive sheets will fail
        XSSFWorkbook xwb = new XSSFWorkbook();
        xwb.createSheet("Open");
        xwb.createSheet("Closed");

        wb.close();
        wb = new SXSSFWorkbook(xwb, 5);
        s = wb.getSheet("Closed");
        s.flushRows();
        s = wb.getSheet("Open");
        s.createRow(0).createCell(0).setCellFormula("1+2");
        
        eval = wb.getCreationHelper().createFormulaEvaluator();
        try {
            eval.evaluateAll();
            fail("Evaluate All shouldn't work, as sheets flushed");
        } catch (SXSSFFormulaEvaluator.SheetsFlushedException e) {
            // expected here
        }
        
        wb.close();
    }
    
    @Test
    public void testEvaluateRefOutsideWindowFails() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(5);
        SXSSFSheet s = wb.createSheet();
        
        s.createRow(0).createCell(0).setCellFormula("1+2");
        assertEquals(false, s.areAllRowsFlushed());
        assertEquals(-1, s.getLastFlushedRowNum());
        
        for (int i=1; i<=19; i++) { s.createRow(i); }
        Cell c = s.createRow(20).createCell(0);
        c.setCellFormula("A1+100");
        
        assertEquals(false, s.areAllRowsFlushed());
        assertEquals(15, s.getLastFlushedRowNum());
        
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        try {
            eval.evaluateFormulaCell(c);
            fail("Evaluate shouldn't work, as reference outside the window");
        } catch(SXSSFFormulaEvaluator.RowFlushedException e) {
            // Expected
        }
        
        wb.close();
    }
    
    /**
     * If all formula cells + their references are inside the window,
     *  then evaluation works
     */
    @Test
    public void testEvaluateAllInWindow() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(5);
        SXSSFSheet s = wb.createSheet();
        s.createRow(0).createCell(0).setCellFormula("1+2");
        s.createRow(1).createCell(1).setCellFormula("A1+10");
        s.createRow(2).createCell(2).setCellFormula("B2+100");
        
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        eval.evaluateAll();
        
        assertEquals(3, (int)s.getRow(0).getCell(0).getNumericCellValue());
        assertEquals(13, (int)s.getRow(1).getCell(1).getNumericCellValue());
        assertEquals(113, (int)s.getRow(2).getCell(2).getNumericCellValue());
        
        wb.close();
    }
    
    @Test
    public void testEvaluateRefInsideWindow() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(5);
        SXSSFSheet s = wb.createSheet();
        
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        
        SXSSFCell c = s.createRow(0).createCell(0);
        c.setCellValue(1.5);
        
        c = s.createRow(1).createCell(0);
        c.setCellFormula("A1*2");
        
        assertEquals(0, (int)c.getNumericCellValue());
        eval.evaluateFormulaCell(c);
        assertEquals(3, (int)c.getNumericCellValue());
        
        wb.close();
    }
    
    @Test
    public void testEvaluateSimple() throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(5);
        SXSSFSheet s = wb.createSheet();
        
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        
        SXSSFCell c = s.createRow(0).createCell(0);
        c.setCellFormula("1+2");
        assertEquals(0, (int)c.getNumericCellValue());
        eval.evaluateFormulaCell(c);
        assertEquals(3, (int)c.getNumericCellValue());
        
        c = s.createRow(1).createCell(0);
        c.setCellFormula("CONCATENATE(\"hello\",\" \",\"world\")");
        eval.evaluateFormulaCell(c);
        assertEquals("hello world", c.getStringCellValue());
        
        wb.close();
    }
    @Test
    public void testUpdateCachedFormulaResultFromErrorToNumber_bug46479() throws IOException {
        Assume.assumeTrue("This test is disabled because it fails for SXSSF because " +
                        "handling of errors in formulas is slightly different than in XSSF, " +
                        "but this proved to be non-trivial to solve...",
                false);
    }
}
