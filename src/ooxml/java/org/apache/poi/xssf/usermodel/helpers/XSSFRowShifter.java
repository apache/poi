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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaErrPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTConditionalFormatting;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

/**
 * Helper for shifting rows up or down
 * 
 * When possible, code should be implemented in the RowShifter abstract class to avoid duplication with {@link org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter}
 */
public final class XSSFRowShifter extends RowShifter {
    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class);

    public XSSFRowShifter(XSSFSheet sh) {
        super(sh);
    }
    
    /**
     * Shift merged regions
     * 
     * @param startRow the row to start shifting
     * @param endRow   the row to end shifting
     * @param n        the number of rows to shift
     * @return an array of merged cell regions
     * @deprecated POI 3.15 beta 2. Use {@link #shiftMergedRegions(int, int, int)} instead.
     */
    public List<CellRangeAddress> shiftMerged(int startRow, int endRow, int n) {
        return shiftMergedRegions(startRow, endRow, n);
    }

    /**
     * Updated named ranges
     */
    public void updateNamedRanges(FormulaShifter shifter) {
        Workbook wb = sheet.getWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
        for (Name name : wb.getAllNames()) {
            String formula = name.getRefersToFormula();
            int sheetIndex = name.getSheetIndex();
            final int rowIndex = -1; //don't care, named ranges are not allowed to include structured references

            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.NAMEDRANGE, sheetIndex, rowIndex);
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
        Workbook wb = sheet.getWorkbook();
        for (Sheet sh : wb) {
            if (sheet == sh) continue;
            updateSheetFormulas(sh, shifter);
        }
    }

    private void updateSheetFormulas(Sheet sh, FormulaShifter shifter) {
        for (Row r : sh) {
            XSSFRow row = (XSSFRow) r;
            updateRowFormulas(row, shifter);
        }
    }

    /**
     * Update the formulas in specified row using the formula shifting policy specified by shifter
     *
     * @param row the row to update the formulas on
     * @param shifter the formula shifting policy
     */
    @Internal
    public void updateRowFormulas(Row row, FormulaShifter shifter) {
        XSSFSheet sheet = (XSSFSheet) row.getSheet();
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
                        if(f.getT() == STCellFormulaType.SHARED){
                            int si = (int)f.getSi();
                            CTCellFormula sf = sheet.getSharedFormula(si);
                            sf.setStringValue(shiftedFormula);
                            updateRefInCTCellFormula(row, shifter, sf);
                        }
                    }

                }

                //Range of cells which the formula applies to.
                updateRefInCTCellFormula(row, shifter, f);
            }

        }
    }

    private void updateRefInCTCellFormula(Row row, FormulaShifter shifter, CTCellFormula f) {
        if (f.isSetRef()) { //Range of cells which the formula applies to.
            String ref = f.getRef();
            String shiftedRef = shiftFormula(row, ref, shifter);
            if (shiftedRef != null) f.setRef(shiftedRef);
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
    private static String shiftFormula(Row row, String formula, FormulaShifter shifter) {
        Sheet sheet = row.getSheet();
        Workbook wb = sheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);
        final int rowIndex = row.getRowNum();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
        
        try {
            Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, rowIndex);
            String shiftedFmla = null;
            if (shifter.adjustFormula(ptgs, sheetIndex)) {
                shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
            }
            return shiftedFmla;
        } catch (FormulaParseException fpe) {
            // Log, but don't change, rather than breaking
            logger.log(POILogger.WARN, "Error shifting formula on row ", row.getRowNum(), fpe);
            return formula;
        }
    }

    public void updateConditionalFormatting(FormulaShifter shifter) {
        XSSFSheet xsheet = (XSSFSheet) sheet;
        XSSFWorkbook wb = xsheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);
        final int rowIndex = -1; //don't care, structured references not allowed in conditional formatting

        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        CTWorksheet ctWorksheet = xsheet.getCTWorksheet();
        CTConditionalFormatting[] conditionalFormattingArray = ctWorksheet.getConditionalFormattingArray();
        // iterate backwards due to possible calls to ctWorksheet.removeConditionalFormatting(j)
        for (int j = conditionalFormattingArray.length - 1; j >= 0; j--) {
            CTConditionalFormatting cf = conditionalFormattingArray[j];

            ArrayList<CellRangeAddress> cellRanges = new ArrayList<CellRangeAddress>();
            for (Object stRef : cf.getSqref()) {
                String[] regions = stRef.toString().split(" ");
                for (String region : regions) {
                    cellRanges.add(CellRangeAddress.valueOf(region));
                }
            }

            boolean changed = false;
            List<CellRangeAddress> temp = new ArrayList<CellRangeAddress>();
            for (CellRangeAddress craOld : cellRanges) {
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
                    ctWorksheet.removeConditionalFormatting(j);
                    continue;
                }
                List<String> refs = new ArrayList<String>();
                for(CellRangeAddress a : temp) refs.add(a.formatAsString());
                cf.setSqref(refs);
            }

            for(CTCfRule cfRule : cf.getCfRuleArray()){
                String[] formulaArray = cfRule.getFormulaArray();
                for (int i = 0; i < formulaArray.length; i++) {
                    String formula = formulaArray[i];
                    Ptg[] ptgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, rowIndex);
                    if (shifter.adjustFormula(ptgs, sheetIndex)) {
                        String shiftedFmla = FormulaRenderer.toFormulaString(fpb, ptgs);
                        cfRule.setFormulaArray(i, shiftedFmla);
                    }
                }
            }
        }
    }
    
    /**
     * Shift the Hyperlink anchors (not the hyperlink text, even if the hyperlink
     * is of type LINK_DOCUMENT and refers to a cell that was shifted). Hyperlinks
     * do not track the content they point to.
     *
     * @param shifter
     */
    public void updateHyperlinks(FormulaShifter shifter) {
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet);
        List<? extends Hyperlink> hyperlinkList = sheet.getHyperlinkList();
        
        for (Hyperlink hyperlink : hyperlinkList) {
            XSSFHyperlink xhyperlink = (XSSFHyperlink) hyperlink;
            String cellRef = xhyperlink.getCellRef();
            CellRangeAddress cra = CellRangeAddress.valueOf(cellRef);
            CellRangeAddress shiftedRange = shiftRange(shifter, cra, sheetIndex);
            if (shiftedRange != null && shiftedRange != cra) {
                // shiftedRange should not be null. If shiftedRange is null, that means
                // that a hyperlink wasn't deleted at the beginning of shiftRows when
                // identifying rows that should be removed because they will be overwritten
                xhyperlink.setCellReference(shiftedRange.formatAsString());
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
