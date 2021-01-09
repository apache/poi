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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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



    protected static Stream<Arguments> data(Class<? extends BaseTestFunctionsFromSpreadsheet> clazz, String filename) throws Exception {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook(filename);
        confirmReadMeSheet(workbook, clazz);

        List<Arguments> data = new ArrayList<>();

        int nSheets = workbook.getNumberOfSheets();
        for(int sheetIdx=1; sheetIdx< nSheets; sheetIdx++) {
            HSSFSheet sheet = workbook.getSheetAt(sheetIdx);
            processFunctionGroup(data, sheet, SS.START_TEST_CASES_ROW_INDEX, filename);
        }

        workbook.close();

        return data.stream();
    }

    private static void processFunctionGroup(List<Arguments> data, HSSFSheet sheet, final int startRowIndex, String filename) {
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet.getWorkbook());

        int precisionColumnIndex = -1;
        HSSFRow precisionRow = sheet.getWorkbook().getSheetAt(0).getRow(11);
        HSSFCell precisionCell = precisionRow == null ? null : precisionRow.getCell(0);
        if(precisionCell != null && precisionCell.getCellType() == CellType.NUMERIC){
            precisionColumnIndex = (int)precisionCell.getNumericCellValue();
        }


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

            data.add(Arguments.of(testName, filename, sheet, rowIndex, evaluator, precisionColumnIndex));
        }
        fail("Missing end marker '" + SS.TEST_CASES_END_MARKER + "' on sheet '" + sheet.getSheetName() + "'");
    }

    @ParameterizedTest
    @MethodSource("data")
    void processFunctionRow(
        String testName, String filename, HSSFSheet sheet, int formulasRowIdx, HSSFFormulaEvaluator evaluator, int precisionColumnIndex
    ) throws Exception {
        HSSFRow r = sheet.getRow(formulasRowIdx);
        HSSFCell evalCell = r.getCell(SS.COLUMN_INDEX_EVALUATION);
        HSSFCell expectedCell = r.getCell(SS.COLUMN_INDEX_EXPECTED_RESULT);
        HSSFCell precisionCell = r.getCell(precisionColumnIndex);

        CellReference cr = new CellReference(sheet.getSheetName(), formulasRowIdx, evalCell.getColumnIndex(), false, false);
        String msg = String.format(Locale.ROOT, "In %s %s {=%s} '%s'"
            , filename, cr.formatAsString(), evalCell.getCellFormula(), testName);

        CellValue actualValue = evaluator.evaluate(evalCell);

        assertNotNull(expectedCell, msg + " - Bad setup data expected value is null");
        assertNotNull(actualValue, msg + " - actual value was null");

        if (expectedCell.getCellType() == CellType.ERROR) {
            int expectedErrorCode = expectedCell.getErrorCellValue();
            assertEquals(CellType.ERROR, actualValue.getCellType(), msg);
            assertEquals(ErrorEval.getText(expectedErrorCode), actualValue.formatAsString(), msg);
            assertEquals(expectedErrorCode, actualValue.getErrorValue(), msg);
            assertEquals(ErrorEval.getText(expectedErrorCode), ErrorEval.getText(actualValue.getErrorValue()), msg);
            return;
        }

        // unexpected error
        assertNotEquals(CellType.ERROR, actualValue.getCellType(), msg);
        assertNotEquals(msg, formatValue(expectedCell), ErrorEval.getText(actualValue.getErrorValue()));

        // wrong type error
        assertEquals(expectedCell.getCellType(), actualValue.getCellType(), msg);

        final CellType expectedCellType = expectedCell.getCellType();
        switch (expectedCellType) {
            case BOOLEAN:
                assertEquals(expectedCell.getBooleanCellValue(), actualValue.getBooleanValue(), msg);
                break;
            case FORMULA: // will never be used, since we will call method after formula evaluation
                fail("Cannot expect formula as result of formula evaluation: " + msg);
            case NUMERIC:
                double precision = precisionCell != null && precisionCell.getCellType() == CellType.NUMERIC
                        ? precisionCell.getNumericCellValue() : 0.0;
                assertEquals(expectedCell.getNumericCellValue(), actualValue.getNumberValue(), precision);
                break;
            case STRING:
                assertEquals(expectedCell.getRichStringCellValue().getString(), actualValue.getStringValue(), msg);
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
        assertTrue(firstSheetName.equalsIgnoreCase(SS.README_SHEET_NAME),
                   "First sheet's name was '" + firstSheetName + "' but expected '" + SS.README_SHEET_NAME + "'");
        HSSFSheet sheet = workbook.getSheetAt(0);
        String specifiedClassName = sheet.getRow(2).getCell(0).getRichStringCellValue().getString();
        assertEquals(clazz.getName(), specifiedClassName, "Test class name in spreadsheet comment");

        HSSFRow precisionRow = sheet.getRow(11);
        HSSFCell precisionCell = precisionRow == null ? null : precisionRow.getCell(0);
        if(precisionCell != null && precisionCell.getCellType() == CellType.NUMERIC){

        }
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
