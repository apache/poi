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

public class XSSFShiftingManager {

    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class);

    protected final Sheet shiftingSheet;
    protected FormulaShifter shifter;

    public XSSFShiftingManager(Sheet shiftingSheet, FormulaShifter shifter){
    	this.shiftingSheet = shiftingSheet;
        this.shifter = shifter;
	}
    
    public void updateFormulas() {
        //update formulas on the parent sheet
        updateSheetFormulas(shiftingSheet);

        //update formulas on other sheets
        Workbook wb = shiftingSheet.getWorkbook();
        for (Sheet sh : wb) {
            if (shiftingSheet == sh) continue;
            updateSheetFormulas(sh);
        }
    }

    private void updateSheetFormulas(Sheet sh) {
        for (Row r : sh) {
            XSSFRow row = (XSSFRow) r;
            updateRowFormulas(row);
        }
    }

    /**
     * Update the formulas in specified row using the formula shifting policy specified by shifter
     *
     * @param row the row to update the formulas on
     * @param shifter the formula shifting policy
     */
    @Internal
    public void updateRowFormulas(Row row) {
        for (Cell c : row) {
           	updateCellFormula(row, (XSSFCell) c);
        }
    }
    
    public void updateCellFormula(Row row, XSSFCell cell){
        CTCell ctCell = cell.getCTCell();
        if (ctCell.isSetF()) {
            CTCellFormula f = ctCell.getF();
            String formula = f.getStringValue();
            if (formula.length() > 0) {
                String shiftedFormula = shiftFormula(row, formula);
                if (shiftedFormula != null) {
                    f.setStringValue(shiftedFormula);
                    if(f.getT() == STCellFormulaType.SHARED){
                        int si = (int)f.getSi();
                        XSSFSheet sheet = (XSSFSheet) row.getSheet();
                        CTCellFormula sf = sheet.getSharedFormula(si);
                        sf.setStringValue(shiftedFormula);
                        updateRefInCTCellFormula(row, sf);
                    }
                }
            }
            //Range of cells which the formula applies to.
            updateRefInCTCellFormula(row, f);
        }
    }
    private void updateRefInCTCellFormula(Row row, CTCellFormula f) {
        if (f.isSetRef()) { //Range of cells which the formula applies to.
            String ref = f.getRef();
            String shiftedRef = shiftFormula(row, ref);
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
    private String shiftFormula(Row row, String formula) {
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

    
    public void updateConditionalFormatting() {
        XSSFSheet xsheet = (XSSFSheet) shiftingSheet;
        XSSFWorkbook wb = xsheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(shiftingSheet);
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
    
    /**
     * Shift the Hyperlink anchors (not the hyperlink text, even if the hyperlink
     * is of type LINK_DOCUMENT and refers to a cell that was shifted). Hyperlinks
     * do not track the content they point to.
     *
     * @param shifter
     */
    public void updateHyperlinks() {
        int sheetIndex = shiftingSheet.getWorkbook().getSheetIndex(shiftingSheet);
        List<? extends Hyperlink> hyperlinkList = shiftingSheet.getHyperlinkList();
        
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

    public void updateNamedRanges() {
        Workbook wb = shiftingSheet.getWorkbook();
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

    public static int shiftedItemIndex(int startShiftingIndex, int endShiftingIndex, int shiftingStep, int originalItemIndex) {
        // no change if before any affected item
        if(originalItemIndex < startShiftingIndex && (shiftingStep > 0 || (startShiftingIndex - originalItemIndex) > shiftingStep)) {
            return originalItemIndex;
        }
        // no change if after any affected item
        if(originalItemIndex > endShiftingIndex && (shiftingStep < 0 || (originalItemIndex - endShiftingIndex) > shiftingStep)) {
            return originalItemIndex;
        }
        // item before and things are moved up
        if(originalItemIndex < startShiftingIndex) {
            // item is moved down by the shifting
            return originalItemIndex + (endShiftingIndex - startShiftingIndex);
        }
        // item is after and things are moved down
        if(originalItemIndex > endShiftingIndex) {
            // item is moved up by the shifting
            return originalItemIndex - (endShiftingIndex - startShiftingIndex);
        }
        // item is part of the shifted block
        return originalItemIndex + shiftingStep;
    }


}
