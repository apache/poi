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

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.AreaErrPtg;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTConditionalFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Yegor Kozlov
 */
public final class XSSFRowShifter {
    private final XSSFSheet sheet;

    public XSSFRowShifter(XSSFSheet sh) {
        sheet = sh;
    }

    /**
     * Shift merged regions
     *
     * @param startRow the row to start shifting
     * @param endRow   the row to end shifting
     * @param n        the number of rows to shift
     * @return an array of affected cell regions
     */
    public List<CellRangeAddress> shiftMerged(int startRow, int endRow, int n) {
        List<CellRangeAddress> shiftedRegions = new ArrayList<CellRangeAddress>();
        //move merged regions completely if they fall within the new region boundaries when they are shifted
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);

            boolean inStart = (merged.getFirstRow() >= startRow || merged.getLastRow() >= startRow);
            boolean inEnd = (merged.getFirstRow() <= endRow || merged.getLastRow() <= endRow);

            //don't check if it's not within the shifted area
            if (!inStart || !inEnd) {
                continue;
            }

            //only shift if the region outside the shifted rows is not merged too
            if (!containsCell(merged, startRow - 1, 0) && !containsCell(merged, endRow + 1, 0)) {
                merged.setFirstRow(merged.getFirstRow() + n);
                merged.setLastRow(merged.getLastRow() + n);
                //have to remove/add it back
                shiftedRegions.add(merged);
                sheet.removeMergedRegion(i);
                i = i - 1; // we have to back up now since we removed one
            }
        }

        //read so it doesn't get shifted again
        for (CellRangeAddress region : shiftedRegions) {
            sheet.addMergedRegion(region);
        }
        return shiftedRegions;
    }

    /**
     * Check if the  row and column are in the specified cell range
     *
     * @param cr    the cell range to check in
     * @param rowIx the row to check
     * @param colIx the column to check
     * @return true if the range contains the cell [rowIx,colIx]
     */
    private static boolean containsCell(CellRangeAddress cr, int rowIx, int colIx) {
        if (cr.getFirstRow() <= rowIx && cr.getLastRow() >= rowIx
                && cr.getFirstColumn() <= colIx && cr.getLastColumn() >= colIx) {
            return true;
        }
        return false;
    }

    /**
     * Updated named ranges
     */
    public void updateNamedRanges(FormulaShifter shifter) {
        XSSFWorkbook wb = sheet.getWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        for (int i = 0; i < wb.getNumberOfNames(); i++) {
            XSSFName name = wb.getNameAt(i);
            String formula = name.getRefersToFormula();
            int sheetIndex = name.getSheetIndex();

            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.NAMEDRANGE, sheetIndex);
            if (shifter.adjustFormula(ptgs, sheetIndex)) {
                String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
                name.setRefersToFormula(shiftedFmla);
            }

        }
    }

    /**
     * Update formulas.
     */
    public void updateFormulas(FormulaShifter shifter) {
        //update formulas on the parent sheet
        updateSheetFormulas(sheet, shifter);

        //update formulas on other sheets
        XSSFWorkbook wb = sheet.getWorkbook();
        for (XSSFSheet sh : wb) {
            if (sheet == sh) continue;
            updateSheetFormulas(sh, shifter);
        }
    }

    private void updateSheetFormulas(XSSFSheet sh, FormulaShifter shifter) {
        for (Row r : sh) {
            XSSFRow row = (XSSFRow) r;
            updateRowFormulas(row, shifter);
        }
    }

    private void updateRowFormulas(XSSFRow row, FormulaShifter shifter) {
        for (Cell c : row) {
            XSSFCell cell = (XSSFCell) c;

            CTCell ctCell = cell.getCTCell();
            if (ctCell.isSetF()) {
                CTCellFormula f = ctCell.getF();
                String formula = f.getStringValue();
                if (formula.length() > 0) {
                    String shiftedFormula = shiftFormula(row, formula, shifter);
                    if (shiftedFormula != null) {
                        f.setStringValue(shiftedFormula);
                    }
                }

                if (f.isSetRef()) { //Range of cells which the formula applies to.
                    String ref = f.getRef();
                    String shiftedRef = shiftFormula(row, ref, shifter);
                    if (shiftedRef != null) f.setRef(shiftedRef);
                }
            }

        }
    }

    /**
     * Shift a formula using the supplied FormulaShifter
     *
     * @param row     the row of the cell this formula belongs to. Used to get a reference to the parent workbook.
     * @param formula the formula to shift
     * @param shifter the FormulaShifter object that operates on the parsed formula tokens
     * @return the shifted formula if the formula was changed,
     *         <code>null</code> if the formula wasn't modified
     */
    private static String shiftFormula(XSSFRow row, String formula, FormulaShifter shifter) {
        XSSFSheet sheet = row.getSheet();
        XSSFWorkbook wb = sheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex);
        String shiftedFmla = null;
        if (shifter.adjustFormula(ptgs, sheetIndex)) {
            shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
        }
        return shiftedFmla;
    }

    public void updateConditionalFormatting(FormulaShifter shifter) {
        XSSFWorkbook wb = sheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);


        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        List<CTConditionalFormatting> cfList = sheet.getCTWorksheet().getConditionalFormattingList();
        for(int j = 0; j< cfList.size(); j++){
            CTConditionalFormatting cf = cfList.get(j);

            ArrayList<CellRangeAddress> cellRanges = new ArrayList<CellRangeAddress>();
            for (Object stRef : cf.getSqref()) {
                String[] regions = stRef.toString().split(" ");
                for (int i = 0; i < regions.length; i++) {
                    cellRanges.add(CellRangeAddress.valueOf(regions[i]));
                }
            }

            boolean changed = false;
            List<CellRangeAddress> temp = new ArrayList<CellRangeAddress>();
            for (int i = 0; i < cellRanges.size(); i++) {
                CellRangeAddress craOld = cellRanges.get(i);
                CellRangeAddress craNew = shiftRange(shifter, craOld, sheetIndex);
                if (craNew == null) {
                    changed = true;
                    continue;
                }
                temp.add(craNew);
                if (craNew != craOld) {
                    changed = true;
                }
            }

            if (changed) {
                int nRanges = temp.size();
                if (nRanges == 0) {
                    cfList.remove(j);
                    continue;
                }
                List<String> refs = new ArrayList<String>();
                for(CellRangeAddress a : temp) refs.add(a.formatAsString());
                cf.setSqref(refs);
            }

            for(CTCfRule cfRule : cf.getCfRuleList()){
                List<String> formulas = cfRule.getFormulaList();
                for (int i = 0; i < formulas.size(); i++) {
                    String formula = formulas.get(i);
                    Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex);
                    if (shifter.adjustFormula(ptgs, sheetIndex)) {
                        String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
                        formulas.set(i, shiftedFmla);
                    }
                }
            }
        }
    }

    private static CellRangeAddress shiftRange(FormulaShifter shifter, CellRangeAddress cra, int currentExternSheetIx) {
        // FormulaShifter works well in terms of Ptgs - so convert CellRangeAddress to AreaPtg (and back) here
        AreaPtg aptg = new AreaPtg(cra.getFirstRow(), cra.getLastRow(), cra.getFirstColumn(), cra.getLastColumn(), false, false, false, false);
        Ptg[] ptgs = { aptg, };

        if (!shifter.adjustFormula(ptgs, currentExternSheetIx)) {
            return cra;
        }
        Ptg ptg0 = ptgs[0];
        if (ptg0 instanceof AreaPtg) {
            AreaPtg bptg = (AreaPtg) ptg0;
            return new CellRangeAddress(bptg.getFirstRow(), bptg.getLastRow(), bptg.getFirstColumn(), bptg.getLastColumn());
        }
        if (ptg0 instanceof AreaErrPtg) {
            return null;
        }
        throw new IllegalStateException("Unexpected shifted ptg class (" + ptg0.getClass().getName() + ")");
    }

}
