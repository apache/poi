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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.Test;

/**
 * Tests for the INDIRECT() function.</p>
 */
public final class TestIndirect {
    // convenient access to namespace
    // private static final ErrorEval EE = null;

    private static void createDataRow(HSSFSheet sheet, int rowIndex, double... vals) {
        HSSFRow row = sheet.createRow(rowIndex);
        for (int i = 0; i < vals.length; i++) {
            row.createCell(i).setCellValue(vals[i]);
        }
    }

    private static HSSFWorkbook createWBA() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("John's sales");

        createDataRow(sheet1, 0, 11, 12, 13, 14);
        createDataRow(sheet1, 1, 21, 22, 23, 24);
        createDataRow(sheet1, 2, 31, 32, 33, 34);

        createDataRow(sheet2, 0, 50, 55, 60, 65);
        createDataRow(sheet2, 1, 51, 56, 61, 66);
        createDataRow(sheet2, 2, 52, 57, 62, 67);

        createDataRow(sheet3, 0, 30, 31, 32);
        createDataRow(sheet3, 1, 33, 34, 35);

        HSSFName name1 = wb.createName();
        name1.setNameName("sales1");
        name1.setRefersToFormula("Sheet1!A1:D1");

        HSSFName name2 = wb.createName();
        name2.setNameName("sales2");
        name2.setRefersToFormula("Sheet2!B1:C3");

        HSSFRow row = sheet1.createRow(3);
        row.createCell(0).setCellValue("sales1");  //A4
        row.createCell(1).setCellValue("sales2");  //B4

        return wb;
    }

    private static HSSFWorkbook createWBB() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("Sheet1");
        HSSFSheet sheet2 = wb.createSheet("Sheet2");
        HSSFSheet sheet3 = wb.createSheet("## Look here!");

        createDataRow(sheet1, 0, 400, 440, 480, 520);
        createDataRow(sheet1, 1, 420, 460, 500, 540);

        createDataRow(sheet2, 0, 50, 55, 60, 65);
        createDataRow(sheet2, 1, 51, 56, 61, 66);

        createDataRow(sheet3, 0, 42);

        return wb;
    }

    @Test
    public void testBasic() throws Exception {

        HSSFWorkbook wbA = createWBA();
        HSSFCell c = wbA.getSheetAt(0).createRow(5).createCell(2);
        HSSFFormulaEvaluator feA = new HSSFFormulaEvaluator(wbA);

        // non-error cases
        confirm(feA, c, "INDIRECT(\"C2\")", 23);
        confirm(feA, c, "INDIRECT(\"C2\", TRUE)", 23);
        confirm(feA, c, "INDIRECT(\"$C2\")", 23);
        confirm(feA, c, "INDIRECT(\"C$2\")", 23);
        confirm(feA, c, "SUM(INDIRECT(\"Sheet2!B1:C3\"))", 351); // area ref
        confirm(feA, c, "SUM(INDIRECT(\"Sheet2! B1 : C3 \"))", 351); // spaces in area ref
        confirm(feA, c, "SUM(INDIRECT(\"'John''s sales'!A1:C1\"))", 93); // special chars in sheet name
        confirm(feA, c, "INDIRECT(\"'Sheet1'!B3\")", 32); // redundant sheet name quotes
        confirm(feA, c, "INDIRECT(\"sHeet1!B3\")", 32); // case-insensitive sheet name
        confirm(feA, c, "INDIRECT(\" D3 \")", 34); // spaces around cell ref
        confirm(feA, c, "INDIRECT(\"Sheet1! D3 \")", 34); // spaces around cell ref
        confirm(feA, c, "INDIRECT(\"A1\", TRUE)", 11); // explicit arg1. only TRUE supported so far

        confirm(feA, c, "INDIRECT(\"A1:G1\")", 13); // de-reference area ref (note formula is in C4)

        confirm(feA, c, "SUM(INDIRECT(A4))", 50); // indirect defined name
        confirm(feA, c, "SUM(INDIRECT(B4))", 351); // indirect defined name pointinh to other sheet

        // simple error propagation:

        // arg0 is evaluated to text first
        confirm(feA, c, "INDIRECT(#DIV/0!)", ErrorEval.DIV_ZERO);
        confirm(feA, c, "INDIRECT(#DIV/0!)", ErrorEval.DIV_ZERO);
        confirm(feA, c, "INDIRECT(#NAME?, \"x\")", ErrorEval.NAME_INVALID);
        confirm(feA, c, "INDIRECT(#NUM!, #N/A)", ErrorEval.NUM_ERROR);

        // arg1 is evaluated to boolean before arg0 is decoded
        confirm(feA, c, "INDIRECT(\"garbage\", #N/A)", ErrorEval.NA);
        confirm(feA, c, "INDIRECT(\"garbage\", \"\")", ErrorEval.VALUE_INVALID); // empty string is not valid boolean
        confirm(feA, c, "INDIRECT(\"garbage\", \"flase\")", ErrorEval.VALUE_INVALID); // must be "TRUE" or "FALSE"


        // spaces around sheet name (with or without quotes makes no difference)
        confirm(feA, c, "INDIRECT(\"'Sheet1 '!D3\")", ErrorEval.REF_INVALID);
        confirm(feA, c, "INDIRECT(\" Sheet1!D3\")", ErrorEval.REF_INVALID);
        confirm(feA, c, "INDIRECT(\"'Sheet1' !D3\")", ErrorEval.REF_INVALID);


        confirm(feA, c, "SUM(INDIRECT(\"'John's sales'!A1:C1\"))", ErrorEval.REF_INVALID); // bad quote escaping
        confirm(feA, c, "INDIRECT(\"[Book1]Sheet1!A1\")", ErrorEval.REF_INVALID); // unknown external workbook
        confirm(feA, c, "INDIRECT(\"Sheet3!A1\")", ErrorEval.REF_INVALID); // unknown sheet
//        if (false) { // TODO - support evaluation of defined names
//            confirm(feA, c, "INDIRECT(\"Sheet1!IW1\")", ErrorEval.REF_INVALID); // bad column
//            confirm(feA, c, "INDIRECT(\"Sheet1!A65537\")", ErrorEval.REF_INVALID); // bad row
//        }
        confirm(feA, c, "INDIRECT(\"Sheet1!A 1\")", ErrorEval.REF_INVALID); // space in cell ref

        wbA.close();
    }

    @Test
    public void testMultipleWorkbooks() throws Exception {
        HSSFWorkbook wbA = createWBA();
        HSSFCell cellA = wbA.getSheetAt(0).createRow(10).createCell(0);
        HSSFFormulaEvaluator feA = new HSSFFormulaEvaluator(wbA);

        HSSFWorkbook wbB = createWBB();
        HSSFCell cellB = wbB.getSheetAt(0).createRow(10).createCell(0);
        HSSFFormulaEvaluator feB = new HSSFFormulaEvaluator(wbB);

        String[] workbookNames = { "MyBook", "Figures for January", };
        HSSFFormulaEvaluator[] evaluators = { feA, feB, };
        HSSFFormulaEvaluator.setupEnvironment(workbookNames, evaluators);

        confirm(feB, cellB, "INDIRECT(\"'[Figures for January]## Look here!'!A1\")", 42); // same wb
        confirm(feA, cellA, "INDIRECT(\"'[Figures for January]## Look here!'!A1\")", 42); // across workbooks

        // 2 level recursion
        confirm(feB, cellB, "INDIRECT(\"[MyBook]Sheet2!A1\")", 50); // set up (and check) first level
        confirm(feA, cellA, "INDIRECT(\"'[Figures for January]Sheet1'!A11\")", 50); // points to cellB
        
        wbB.close();
        wbA.close();
    }

    private static void confirm(FormulaEvaluator fe, Cell cell, String formula,
            double expectedResult) {
        fe.clearAllCachedResultValues();
        cell.setCellFormula(formula);
        CellValue cv = fe.evaluate(cell);
        if (cv.getCellType() != CellType.NUMERIC) {
            fail("expected numeric cell type but got " + cv.formatAsString());
        }
        assertEquals(expectedResult, cv.getNumberValue(), 0.0);
    }
    
    private static void confirm(FormulaEvaluator fe, Cell cell, String formula,
            ErrorEval expectedResult) {
        fe.clearAllCachedResultValues();
        cell.setCellFormula(formula);
        CellValue cv = fe.evaluate(cell);
        if (cv.getCellType() != CellType.ERROR) {
            fail("expected error cell type but got " + cv.formatAsString());
        }
        int expCode = expectedResult.getErrorCode();
        if (cv.getErrorValue() != expCode) {
            fail("Expected error '" + ErrorEval.getText(expCode)
                    + "' but got '" + cv.formatAsString() + "'.");
        }
    }

    @Test
    public void testInvalidInput() {
        assertEquals(ErrorEval.VALUE_INVALID, Indirect.instance.evaluate(new ValueEval[] {}, null));
    }
}
