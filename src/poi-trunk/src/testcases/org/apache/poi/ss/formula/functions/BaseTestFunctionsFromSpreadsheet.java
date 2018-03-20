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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public abstract class BaseTestFunctionsFromSpreadsheet {

    /**
     * This class defines constants for navigating around the test data spreadsheet used for these tests.
     */
    interface SS {
        /** Name of the first sheet in the spreadsheet (contains comments) */
        String README_SHEET_NAME = "Read Me";

        /** Row (zero-based) in each sheet where the evaluation cases start.   */
        int START_TEST_CASES_ROW_INDEX = 4; // Row '5'
        /**  Index of the column that contains the function names */
        int COLUMN_INDEX_MARKER = 0; // Column 'A'
        int COLUMN_INDEX_EVALUATION = 1; // Column 'B'
        int COLUMN_INDEX_EXPECTED_RESULT = 2; // Column 'C'
        int COLUMN_ROW_COMMENT = 3; // Column 'D'

        /** Used to indicate when there are no more test cases on the current sheet   */
        String TEST_CASES_END_MARKER = "<end>";
        /** Used to indicate that the test on the current row should be ignored */
        String SKIP_CURRENT_TEST_CASE_MARKER = "<skip>";

    }

    @Parameter()
    public String testName;
    @Parameter(value = 1)
    public String filename;
    @Parameter(value = 2)
    public HSSFSheet sheet;
    @Parameter(value = 3)
    public int formulasRowIdx;
    @Parameter(value = 4)
    public HSSFFormulaEvaluator evaluator;


    
    protected static Collection<Object[]> data(Class<? extends BaseTestFunctionsFromSpreadsheet> clazz, String filename) throws Exception {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook(filename);
        confirmReadMeSheet(workbook, clazz);

        List<Object[]> data = new ArrayList<>();

        int nSheets = workbook.getNumberOfSheets();
        for(int sheetIdx=1; sheetIdx< nSheets; sheetIdx++) {
            HSSFSheet sheet = workbook.getSheetAt(sheetIdx);
            processFunctionGroup(data, sheet, SS.START_TEST_CASES_ROW_INDEX, filename);
        }
        
        workbook.close();
        
        return data;
    }

    private static void processFunctionGroup(List<Object[]> data, HSSFSheet sheet, final int startRowIndex, String filename) {
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet.getWorkbook());

        String currentGroupComment = "";
        final int maxRows = sheet.getLastRowNum()+1;
        for(int rowIndex=startRowIndex; rowIndex<maxRows; rowIndex++) {
            HSSFRow r = sheet.getRow(rowIndex);
            if(r == null) {
                continue;
            }
            String newMarkerValue = getCellTextValue(r, SS.COLUMN_INDEX_MARKER, "marker");                    
            if(SS.TEST_CASES_END_MARKER.equalsIgnoreCase(newMarkerValue)) {
                // normal exit point
                return;
            }
            if(SS.SKIP_CURRENT_TEST_CASE_MARKER.equalsIgnoreCase(newMarkerValue)) {
                // currently disabled test case row
                continue;
            }
            if(newMarkerValue != null) {
                currentGroupComment = newMarkerValue;
            }
            HSSFCell evalCell = r.getCell(SS.COLUMN_INDEX_EVALUATION);
            if (evalCell == null || evalCell.getCellType() != CellType.FORMULA) {
                continue;
            }
            String rowComment = getCellTextValue(r, SS.COLUMN_ROW_COMMENT, "row comment");

            String testName = (currentGroupComment+'\n'+rowComment).replace("null", "").trim().replace("\n", " - ");
            if (testName.isEmpty()) {
                testName = evalCell.getCellFormula();
            }
            
            data.add(new Object[]{testName, filename, sheet, rowIndex, evaluator});
        }
        fail("Missing end marker '" + SS.TEST_CASES_END_MARKER + "' on sheet '" + sheet.getSheetName() + "'");
    }

    @Test
    public void processFunctionRow() throws Exception {
        HSSFRow r = sheet.getRow(formulasRowIdx);
        HSSFCell evalCell = r.getCell(SS.COLUMN_INDEX_EVALUATION);
        HSSFCell expectedCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_RESULT);
        
        CellReference cr = new CellReference(sheet.getSheetName(), formulasRowIdx, evalCell.getColumnIndex(), false, false);
        String msg = String.format(Locale.ROOT, "In %s %s {=%s} '%s'"
            , filename, cr.formatAsString(), evalCell.getCellFormula(), testName);

        CellValue actualValue = evaluator.evaluate(evalCell);

        assertNotNull(msg + " - Bad setup data expected value is null", expectedCell);
        assertNotNull(msg + " - actual value was null", actualValue);

        if (expectedCell.getCellType() == CellType.ERROR) {
            int expectedErrorCode = expectedCell.getErrorCellValue();
            assertEquals(msg, CellType.ERROR, actualValue.getCellType());
            assertEquals(msg, ErrorEval.getText(expectedErrorCode), actualValue.formatAsString());
            assertEquals(msg, expectedErrorCode, actualValue.getErrorValue());
            assertEquals(msg, ErrorEval.getText(expectedErrorCode), ErrorEval.getText(actualValue.getErrorValue()));
            return;
        }

        // unexpected error
        assertNotEquals(msg, CellType.ERROR, actualValue.getCellType());
        assertNotEquals(msg, formatValue(expectedCell), ErrorEval.getText(actualValue.getErrorValue()));

        // wrong type error
        assertEquals(msg, expectedCell.getCellType(), actualValue.getCellType());

        final CellType expectedCellType = expectedCell.getCellType();
        switch (expectedCellType) {
            case BOOLEAN:
                assertEquals(msg, expectedCell.getBooleanCellValue(), actualValue.getBooleanValue());
                break;
            case FORMULA: // will never be used, since we will call method after formula evaluation
                fail("Cannot expect formula as result of formula evaluation: " + msg);
            case NUMERIC:
                assertEquals(expectedCell.getNumericCellValue(), actualValue.getNumberValue(), 0.0);
                break;
            case STRING:
                assertEquals(msg, expectedCell.getRichStringCellValue().getString(), actualValue.getStringValue());
                break;
            default:
                fail("Unexpected cell type: " + expectedCellType);
        }
    }

    /**
     * Asserts that the 'read me' comment page exists, and has this class' name in one of the
     * cells.  This back-link is to make it easy to find this class if a reader encounters the
     * spreadsheet first.
     */
    private static void confirmReadMeSheet(HSSFWorkbook workbook, Class<? extends BaseTestFunctionsFromSpreadsheet> clazz) {
        String firstSheetName = workbook.getSheetName(0);
        assertTrue("First sheet's name was '" + firstSheetName + "' but expected '" + SS.README_SHEET_NAME + "'",
            firstSheetName.equalsIgnoreCase(SS.README_SHEET_NAME));
        HSSFSheet sheet = workbook.getSheetAt(0);
        String specifiedClassName = sheet.getRow(2).getCell(0).getRichStringCellValue().getString();
        assertEquals("Test class name in spreadsheet comment", clazz.getName(), specifiedClassName);
    }

    /**
     * @return <code>null</code> if cell is missing, empty or blank
     */
    private static String getCellTextValue(HSSFRow r, int colIndex, String columnName) {
        if(r == null) {
            return null;
        }
        HSSFCell cell = r.getCell(colIndex);
        if(cell == null) {
            return null;
        }
        if(cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if(cell.getCellType() == CellType.STRING) {
            return cell.getRichStringCellValue().getString();
        }

        fail("Bad cell type for '" + columnName + "' column: ("
                + cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
        return "";
    }

    private static String formatValue(HSSFCell expectedCell) {
        switch (expectedCell.getCellType()) {
            case BLANK: return "<blank>";
            case BOOLEAN: return Boolean.toString(expectedCell.getBooleanCellValue());
            case NUMERIC: return Double.toString(expectedCell.getNumericCellValue());
            case STRING: return expectedCell.getRichStringCellValue().getString();
            default: fail("Unexpected cell type of expected value (" + expectedCell.getCellType() + ")");
        }
        return "";
    }


}
