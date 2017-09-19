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

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.functions.TestMathX;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.AssertionFailedError;

@RunWith(Parameterized.class)
public final class TestMatrixFormulasFromBinarySpreadsheet {

    private static final POILogger LOG = POILogFactory.getLogger(TestMatrixFormulasFromBinarySpreadsheet.class);


    private static HSSFWorkbook workbook;
    private static Sheet sheet;
    private static FormulaEvaluator evaluator;
    private static Locale userLocale;
    
    /*
     * Unlike TestFormulaFromSpreadsheet which this class is modified from, there is no
     * differentiation between operators and functions, if more functionality is implemented with
     * array formulas then it might be worth it to separate operators from functions
     * 
     * Also, output matrices are statically 3x3, if larger matrices wanted to be tested
     * then adding matrix size parameter would be useful and parsing would be based off that.
     */
    
    private static interface Navigator {
        /**
         * Name of the test spreadsheet (found in the standard test data folder)
         */
        String FILENAME = "MatrixFormulaEvalTestData.xls";
        /**
         * Row (zero-based) in the spreadsheet where operations start
         */
        int START_OPERATORS_ROW_INDEX = 1;
        /**
         * Column (zero-based) in the spreadsheet where operations start
         */
        int START_OPERATORS_COL_INDEX = 0;
        /**
         * Column (zero-based) in the spreadsheet where evaluations start
         */
        int START_RESULT_COL_INDEX = 7;
        /**
         * Column separation in the spreadsheet between evaluations and expected results
         */
        int COL_OFF_EXPECTED_RESULT = 3;
        /**
         * Row separation in the spreadsheet between operations
         */
        int ROW_OFF_NEXT_OP = 4;
        /**
         * Used to indicate when there are no more operations left
         */
        String END_OF_TESTS = "<END>";
        
    }
    
    /* Parameters for test case */
    @Parameter(0)
    public String targetFunctionName;
    @Parameter(1)
    public int formulasRowIdx;
    
    @AfterClass
    public static void closeResource() throws Exception {
        LocaleUtil.setUserLocale(userLocale);
        workbook.close();
    }
    
    /* generating parameter instances */
    @Parameters(name="{0}")
    public static Collection<Object[]> data() throws Exception {
        // Function "Text" uses custom-formats which are locale specific
        // can't set the locale on a per-testrun execution, as some settings have been
        // already set, when we would try to change the locale by then
        userLocale = LocaleUtil.getUserLocale();
        LocaleUtil.setUserLocale(Locale.ROOT);
        
        workbook = HSSFTestDataSamples.openSampleWorkbook(Navigator.FILENAME);
        sheet = workbook.getSheetAt(0);
        evaluator = new HSSFFormulaEvaluator(workbook);
        
        List<Object[]> data = new ArrayList<Object[]>();
        
        processFunctionGroup(data, Navigator.START_OPERATORS_ROW_INDEX, null);
        
        return data;
    }
    
    /**
     * @param startRowIndex row index in the spreadsheet where the first function/operator is found
     * @param testFocusFunctionName name of a single function/operator to test alone.
     * Typically pass <code>null</code> to test all functions
     */
    private static void processFunctionGroup(List<Object[]> data, int startRowIndex, String testFocusFunctionName) {
        for (int rowIndex = startRowIndex; true; rowIndex += Navigator.ROW_OFF_NEXT_OP) {
            Row r = sheet.getRow(rowIndex);
            String targetFunctionName = getTargetFunctionName(r);
            assertNotNull("Test spreadsheet cell empty on row ("
                    + (rowIndex) + "). Expected function name or '"
                    + Navigator.END_OF_TESTS + "'", targetFunctionName);
            if(targetFunctionName.equals(Navigator.END_OF_TESTS)) {
                // found end of functions list
                break;
            }
            if(testFocusFunctionName == null || targetFunctionName.equalsIgnoreCase(testFocusFunctionName)) {
                data.add(new Object[]{targetFunctionName, rowIndex});
            }
        }
    }
    
    @Test
    public void processFunctionRow() {

       int endColNum = Navigator.START_RESULT_COL_INDEX + Navigator.COL_OFF_EXPECTED_RESULT;

       for (int rowNum = formulasRowIdx; rowNum < formulasRowIdx + Navigator.ROW_OFF_NEXT_OP - 1; rowNum++) {
           for (int colNum = Navigator.START_RESULT_COL_INDEX; colNum < endColNum; colNum++) {
               Row r = sheet.getRow(rowNum);
               
               /* mainly to escape row failures on MDETERM which only returns a scalar */
               if (r == null) {
                   continue;
               }
               
               Cell c = sheet.getRow(rowNum).getCell(colNum);
               
               if (c == null || c.getCellType() != CellType.FORMULA) {
                   continue;
               }
    
               CellValue actValue = evaluator.evaluate(c);
               Cell expValue = sheet.getRow(rowNum).getCell(colNum + Navigator.COL_OFF_EXPECTED_RESULT);
    
               String msg = String.format(Locale.ROOT, "Function '%s': Formula: %s @ %d:%d"
                       , targetFunctionName, c.getCellFormula(), rowNum, colNum);
    
               assertNotNull(msg + " - Bad setup data expected value is null", expValue);
               assertNotNull(msg + " - actual value was null", actValue);
    
               final CellType cellType = expValue.getCellType();
               switch (cellType) {
                   case BLANK:
                       assertEquals(msg, CellType.BLANK, actValue.getCellType());
                       break;
                   case BOOLEAN:
                       assertEquals(msg, CellType.BOOLEAN, actValue.getCellType());
                       assertEquals(msg, expValue.getBooleanCellValue(), actValue.getBooleanValue());
                       break;
                   case ERROR:
                       assertEquals(msg, CellType.ERROR, actValue.getCellType());
                       assertEquals(msg, ErrorEval.getText(expValue.getErrorCellValue()), ErrorEval.getText(actValue.getErrorValue()));
                       break;
                   case FORMULA: // will never be used, since we will call method after formula evaluation
                       fail("Cannot expect formula as result of formula evaluation: " + msg);
                   case NUMERIC:
                       assertEquals(msg, CellType.NUMERIC, actValue.getCellType());
                       TestMathX.assertEquals(msg, expValue.getNumericCellValue(), actValue.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
                       break;
                   case STRING:
                       assertEquals(msg, CellType.STRING, actValue.getCellType());
                       assertEquals(msg, expValue.getRichStringCellValue().getString(), actValue.getStringValue());
                       break;
                   default:
                       fail("Unexpected cell type: " + cellType);
               }
           }
       }
   }
    
    /**
     * @return <code>null</code> if cell is missing, empty or blank
     */
    private static String getTargetFunctionName(Row r) {
        if(r == null) {

            LOG.log(POILogger.WARN,"Warning - given null row, can't figure out function name");
            return null;
        }
        Cell cell = r.getCell(Navigator.START_OPERATORS_COL_INDEX);
        LOG.log(POILogger.INFO, String.valueOf(Navigator.START_OPERATORS_COL_INDEX));
        if(cell == null) {
            LOG.log(POILogger.WARN,
                    "Warning - Row " + r.getRowNum() + " has no cell " + Navigator.START_OPERATORS_COL_INDEX + ", can't figure out function name");
            return null;
        }
        if(cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if(cell.getCellType() == CellType.STRING) {
            return cell.getRichStringCellValue().getString();
        }

        throw new AssertionFailedError("Bad cell type for 'function name' column: ("
                + cell.getCellType() + ") row (" + (r.getRowNum() +1) + ")");
    }
    
    
    
    
    

}
