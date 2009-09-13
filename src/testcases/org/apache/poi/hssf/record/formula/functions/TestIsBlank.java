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

package org.apache.poi.hssf.record.formula.functions;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
/**
 * Tests for Excel function ISBLANK()
 * 
 * @author Josh Micich
 */
public final class TestIsBlank extends TestCase {

	public void test3DArea() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet();
        wb.setSheetName(0, "Sheet1");
        wb.createSheet();
        wb.setSheetName(1, "Sheet2");
        HSSFRow row = sheet1.createRow(0);
        HSSFCell cell = row.createCell(0);

         
        cell.setCellFormula("isblank(Sheet2!A1:A1)");
        
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        CellValue result = fe.evaluate(cell);
        assertEquals(HSSFCell.CELL_TYPE_BOOLEAN, result.getCellType());
        assertEquals(true, result.getBooleanValue());
        
        cell.setCellFormula("isblank(D7:D7)");
        
        result = fe.evaluate(cell);
        assertEquals(HSSFCell.CELL_TYPE_BOOLEAN, result.getCellType());
        assertEquals(true, result.getBooleanValue());
   }
}
