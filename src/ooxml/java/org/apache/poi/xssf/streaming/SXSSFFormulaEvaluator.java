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

package org.apache.poi.xssf.streaming;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

/**
 * Streaming-specific Formula Evaluator, which is able to 
 *  lookup cells within the current Window.
 */
public class SXSSFFormulaEvaluator extends XSSFFormulaEvaluator {
    private static POILogger logger = POILogFactory.getLogger(SXSSFFormulaEvaluator.class);
    
    private SXSSFWorkbook wb;
    
    public SXSSFFormulaEvaluator(SXSSFWorkbook workbook) {
        super(workbook.getXSSFWorkbook());
        this.wb = workbook;
    }
    
    /**
     * Turns a SXSSFCell into a SXSSFEvaluationCell
     */
    @Override
    protected EvaluationCell toEvaluationCell(Cell cell) {
        if (!(cell instanceof SXSSFCell)){
            throw new IllegalArgumentException("Unexpected type of cell: " + cell.getClass() + "." +
                    " Only SXSSFCells can be evaluated.");
        }

        return new SXSSFEvaluationCell((SXSSFCell)cell);
    }
    
    /**
     * For active worksheets only, will loop over rows and
     *  cells, evaluating formula cells there.
     * If formula cells are outside the window for that sheet,
     *  it can either skip them silently, or give an exception
     */
    public static void evaluateAllFormulaCells(SXSSFWorkbook wb, boolean skipOutOfWindow) {
        SXSSFFormulaEvaluator eval = new SXSSFFormulaEvaluator(wb);
        
        // Check they're all available
        for (int i=0; i<wb.getNumberOfSheets(); i++) {
            SXSSFSheet s = wb.getSheetAt(i);
            if (s.isFlushed()) {
                throw new SheetsFlushedException();
            }
        }
        
        // Process the sheets as best we can
        for (int i=0; i<wb.getNumberOfSheets(); i++) {
            SXSSFSheet s = wb.getSheetAt(i);
            
            // Check if any rows have already been flushed out
            int firstRowNum = s.getFirstRowNum();
            int firstAvailableRowNum = s.iterator().next().getRowNum();
            if (firstRowNum != firstAvailableRowNum) {
                if (skipOutOfWindow) throw new RowsFlushedException();
                logger.log(POILogger.INFO, "Rows from " + firstRowNum + " to" +
                           (firstAvailableRowNum-1) + " have already been flushed, skipping");
            }
            
            // Evaluate what we have
            for (Row r : s) {
                for (Cell c : r) {
                    if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        eval.evaluateFormulaCell(c);
                    }
                }
            }
        }
    }
    
    /**
     * Loops over rows and cells, evaluating formula cells there.
     * If any sheets are inactive, or any cells outside of the window,
     *  will give an Exception.
     * For SXSSF, you generally don't want to use this method, instead
     *  evaluate your formulas as you go before they leave the window.
     */
    public void evaluateAll() {
        // Have the evaluation done, with exceptions
        evaluateAllFormulaCells(wb, false);
    }
    
    public static class SheetsFlushedException extends IllegalStateException {
        protected SheetsFlushedException() {
            super("One or more sheets have been flushed, cannot evaluate all cells");
        }
    }
    public static class RowsFlushedException extends IllegalStateException {
        protected RowsFlushedException() {
            super("One or more rows have been flushed, cannot evaluate all cells");
        }
    }
}
