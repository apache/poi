package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RangeCopier;
import org.apache.poi.ss.usermodel.Sheet;

public class XSSFRangeCopier extends RangeCopier {

    public XSSFRangeCopier(Sheet sourceSheet, Sheet destSheet){
        super(sourceSheet, destSheet);
    }
    protected void adjustCellReferencesInsideFormula(Cell cell, Sheet destSheet, int deltaX, int deltaY){
        XSSFWorkbook hostWorkbook = (XSSFWorkbook) destSheet.getWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(hostWorkbook); 
        Ptg[] ptgs = FormulaParser.parse(cell.getCellFormula(), fpb, FormulaType.CELL, 0);
        int destSheetIndex = hostWorkbook.getSheetIndex(destSheet);
        if(adjustInBothDirections(ptgs, destSheetIndex, deltaX, deltaY))
            cell.setCellFormula(FormulaRenderer.toFormulaString(fpb, ptgs));
    }
}
