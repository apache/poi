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

package org.apache.poi.hssf.usermodel.helpers;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Internal;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Class for code common to {@link HSSFRowShifter} and {@link HSSFColumnShifter}
 *
 * @since 5.1.0
 */
@Internal
/*private*/ final class HSSFRowColShifter {
    private static final Logger LOG = PoiLogManager.getLogger(HSSFRowColShifter.class);

    private HSSFRowColShifter() { /*no instances for static classes*/}

    /**
     * Update formulas.
     */
    /*package*/ static void updateFormulas(Sheet sheet, FormulaShifter formulaShifter) {
        //update formulas on the parent sheet
        updateSheetFormulas(sheet,formulaShifter);

        //update formulas on other sheets
        Workbook wb = sheet.getWorkbook();
        for(Sheet sh : wb)
        {
            if (sheet == sh) continue;
            updateSheetFormulas(sh, formulaShifter);
        }
    }

    /*package*/ static void updateSheetFormulas(Sheet sh, FormulaShifter formulashifter) {
        for (Row r : sh) {
            HSSFRow row = (HSSFRow) r;
            updateRowFormulas(row, formulashifter);
        }
    }

    /**
     * Update the formulas in the specified row using the formula shifting policy specified by shifter
     *
     * @param row the row to update the formulas on
     * @param formulaShifter the formula shifting policy
     */
    /*package*/ static void updateRowFormulas(HSSFRow row, FormulaShifter formulaShifter) {
        for (Cell cell : row) {
            if (cell.getCellType() == CellType.FORMULA) {
                String formula = cell.getCellFormula();
                if (formula != null && !formula.isEmpty()) {
                    String shiftedFormula = shiftFormula(row, formula, formulaShifter);
                    cell.setCellFormula(shiftedFormula);
                }
            }
        }
    }

    /**
     * Shift a formula using the supplied FormulaShifter
     *
     * @param row            the row of the cell this formula belongs to. Used to get a reference to the parent workbook.
     * @param formula        the formula to shift
     * @param formulaShifter the FormulaShifter object that operates on the parsed formula tokens
     * @return the shifted formula if the formula was changed,
     * <code>null</code> if the formula wasn't modified
     */
    /*package*/
    static String shiftFormula(Row row, String formula, FormulaShifter formulaShifter) {
        Sheet sheet = row.getSheet();
        Workbook wb = sheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);
        final int rowIndex = row.getRowNum();
        HSSFEvaluationWorkbook fpb = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);

        try {
            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, rowIndex);
            String shiftedFmla;
            if (formulaShifter.adjustFormula(ptgs, sheetIndex)) {
                shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
            } else {
                shiftedFmla = formula;
            }
            return shiftedFmla;
        } catch (FormulaParseException fpe) {
            // Log, but don't change, rather than breaking
            LOG.atWarn().withThrowable(fpe).log("Error shifting formula on row {}", box(row.getRowNum()));
            return formula;
        }
    }

    /**
     * Shift the anchors of the shapes (pictures, charts, ...) in the sheet's drawing along with the rows
     * they are anchored to. A shape is moved when its top-left anchor row is within {@code [startRow, endRow]};
     * the whole shape is moved so it keeps its size (Excel's "move but don't size with cells").
     * Comments are not touched here, {@link HSSFSheet#shiftRows} moves them along with their cells.
     */
    /*package*/ static void shiftDrawingAnchorRows(HSSFSheet sheet, int startRow, int endRow, int n) {
        shiftDrawingAnchors(sheet, startRow, endRow, n, true);
    }

    /**
     * Shift the anchors of the shapes (pictures, charts, ...) in the sheet's drawing along with the columns
     * they are anchored to. A shape is moved when its top-left anchor column is within
     * {@code [startColumn, endColumn]}; the whole shape is moved so it keeps its size.
     */
    /*package*/ static void shiftDrawingAnchorColumns(HSSFSheet sheet, int startColumn, int endColumn, int n) {
        shiftDrawingAnchors(sheet, startColumn, endColumn, n, false);
    }

    private static void shiftDrawingAnchors(HSSFSheet sheet, int start, int end, int n, boolean rows) {
        if (n == 0) {
            return;
        }
        HSSFPatriarch patriarch = sheet.getDrawingPatriarch();
        if (patriarch == null) {
            return;
        }
        for (HSSFShape shape : patriarch.getChildren()) {
            // comments are anchored to their cell and moved with it by HSSFSheet.shiftRows
            if (shape instanceof HSSFComment || !(shape.getAnchor() instanceof HSSFClientAnchor anchor)) {
                continue;
            }
            if (rows) {
                if (anchor.getRow1() >= start && anchor.getRow1() <= end) {
                    anchor.setRow1(clip(anchor.getRow1() + n, HSSFClientAnchor.MAX_ROW));
                    anchor.setRow2(clip(anchor.getRow2() + n, HSSFClientAnchor.MAX_ROW));
                }
            } else {
                if (anchor.getCol1() >= start && anchor.getCol1() <= end) {
                    anchor.setCol1(clip(anchor.getCol1() + n, HSSFClientAnchor.MAX_COL));
                    anchor.setCol2(clip(anchor.getCol2() + n, HSSFClientAnchor.MAX_COL));
                }
            }
        }
    }

    private static int clip(int idx, int max) {
        return Math.min(Math.max(0, idx), max);
    }
}
