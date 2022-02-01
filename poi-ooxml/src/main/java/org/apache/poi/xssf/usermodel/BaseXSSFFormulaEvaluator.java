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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.ExternalLinksTable;

/**
 * Internal POI use only - parent of XSSF and SXSSF formula evaluators
 */
public abstract class BaseXSSFFormulaEvaluator extends BaseFormulaEvaluator {
    protected BaseXSSFFormulaEvaluator(WorkbookEvaluator bookEvaluator) {
        super(bookEvaluator);
    }
    @Override
    protected RichTextString createRichTextString(String str) {
        return new XSSFRichTextString(str);
    }

    /**
     * Turns a XSSFCell / SXSSFCell into a XSSFEvaluationCell
     */
    protected abstract EvaluationCell toEvaluationCell(Cell cell);

    /**
     * Returns a CellValue wrapper around the supplied ValueEval instance.
     */
    @Override
    protected CellValue evaluateFormulaCellValue(Cell cell) {
        EvaluationCell evalCell = toEvaluationCell(cell);
        ValueEval eval = _bookEvaluator.evaluate(evalCell);
        cacheExternalWorkbookCells(evalCell);
        if (eval instanceof NumberEval) {
            NumberEval ne = (NumberEval) eval;
            return new CellValue(ne.getNumberValue());
        }
        if (eval instanceof BoolEval) {
            BoolEval be = (BoolEval) eval;
            return CellValue.valueOf(be.getBooleanValue());
        }
        if (eval instanceof StringEval) {
            StringEval ne = (StringEval) eval;
            return new CellValue(ne.getStringValue());
        }
        if (eval instanceof ErrorEval) {
            return CellValue.getError(((ErrorEval)eval).getErrorCode());
        }
        throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
    }

    /**
     * cache cell value of external workbook
     *
     * @param evalCell sourceCell
     */
    private void cacheExternalWorkbookCells(EvaluationCell evalCell) {
        //
        Ptg[] formulaTokens = getEvaluationWorkbook().getFormulaTokens(evalCell);
        for (Ptg ptg : formulaTokens) {
            if (ptg instanceof Area3DPxg) {
                Area3DPxg area3DPxg = (Area3DPxg) ptg;
                if (area3DPxg.getExternalWorkbookNumber() > 0) {
                    EvaluationWorkbook.ExternalSheet externalSheet = getEvaluationWorkbook().getExternalSheet(area3DPxg.getSheetName(), area3DPxg.getLastSheetName(), area3DPxg.getExternalWorkbookNumber());

                    XSSFCell xssfCell = ((XSSFEvaluationCell) evalCell).getXSSFCell();
                    XSSFWorkbook externalWorkbook = (XSSFWorkbook) xssfCell.getSheet().getWorkbook().getCreationHelper().getReferencedWorkbooks().get(externalSheet.getWorkbookName());
                    ExternalLinksTable externalLinksTable = xssfCell.getSheet().getWorkbook().getExternalLinksTable().get(area3DPxg.getExternalWorkbookNumber() - 1);

                    int firstSheet = externalWorkbook.getSheetIndex(area3DPxg.getSheetName());
                    int lastSheet = firstSheet;
                    if (area3DPxg.getLastSheetName() != null) {
                        lastSheet = externalWorkbook.getSheetIndex(area3DPxg.getLastSheetName());
                    }

                    for (int sheetIndex = firstSheet; sheetIndex <= lastSheet; sheetIndex++) {
                        XSSFSheet sheet = externalWorkbook.getSheetAt(sheetIndex);
                        int firstRow = area3DPxg.getFirstRow();
                        int lastRow = area3DPxg.getLastRow();
                        for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                            XSSFRow row = sheet.getRow(rowIndex);
                            int firstColumn = area3DPxg.getFirstColumn();
                            int lastColumn = area3DPxg.getLastColumn();
                            for (int cellIndex = firstColumn; cellIndex <= lastColumn; cellIndex++) {
                                XSSFCell cell = row.getCell(cellIndex);
                                String cellValue = cell.getRawValue();
                                String cellR = new CellReference(cell).formatAsString(false);
                                externalLinksTable.cacheData(sheet.getSheetName(), (long)rowIndex + 1, cellR, cellValue);
                            }
                        }

                    }
                }

            }
        }
    }

    @Override
    protected void setCellType(Cell cell, CellType cellType) {
        if (cell instanceof XSSFCell) {
            EvaluationWorkbook evaluationWorkbook = getEvaluationWorkbook();
            BaseXSSFEvaluationWorkbook xewb = BaseXSSFEvaluationWorkbook.class.isAssignableFrom(evaluationWorkbook.getClass()) ? (BaseXSSFEvaluationWorkbook) evaluationWorkbook : null;

            ((XSSFCell) cell).setCellType(cellType, xewb);
        } else {
            // could be an SXSSFCell
            cell.setCellType(cellType);
        }
    }
}
