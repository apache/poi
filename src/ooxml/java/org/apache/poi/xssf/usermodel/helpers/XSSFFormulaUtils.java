/*
 *  ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;

/**
 * Utility to update formulas and named ranges when a sheet name was changed
 *
 * @author Yegor Kozlov
 */
public final class XSSFFormulaUtils {
    private final XSSFWorkbook _wb;
    private final XSSFEvaluationWorkbook _fpwb;

    public XSSFFormulaUtils(XSSFWorkbook wb) {
        _wb = wb;
        _fpwb = XSSFEvaluationWorkbook.create(_wb);
    }

    /**
     * Update sheet name in all formulas and named ranges.
     * Called from {@link XSSFWorkbook#setSheetName(int, String)}
     * <p/>
     * <p>
     * The idea is to parse every formula and render it back to string
     * with the updated sheet name. The FormulaParsingWorkbook passed to the formula parser
     * is constructed from the old workbook (sheet name is not yet updated) and
     * the FormulaRenderingWorkbook passed to FormulaRenderer#toFormulaString is a custom implementation that
     * returns the new sheet name.
     * </p>
     *
     * @param sheetIndex the 0-based index of the sheet being changed
     * @param name       the new sheet name
     */
    public void updateSheetName(final int sheetIndex, final String name) {

        /**
         * An instance of FormulaRenderingWorkbook that returns
         */
        FormulaRenderingWorkbook frwb = new FormulaRenderingWorkbook() {

            public ExternalSheet getExternalSheet(int externSheetIndex) {
                return _fpwb.getExternalSheet(externSheetIndex);
            }

            public String getSheetNameByExternSheet(int externSheetIndex) {
                if (externSheetIndex == sheetIndex) return name;
                else return _fpwb.getSheetNameByExternSheet(externSheetIndex);
            }

            public String resolveNameXText(NameXPtg nameXPtg) {
                return _fpwb.resolveNameXText(nameXPtg);
            }

            public String getNameText(NamePtg namePtg) {
                return _fpwb.getNameText(namePtg);
            }
        };

        // update named ranges
        for (int i = 0; i < _wb.getNumberOfNames(); i++) {
            XSSFName nm = _wb.getNameAt(i);
            if (nm.getSheetIndex() == -1 || nm.getSheetIndex() == sheetIndex) {
                updateName(nm, frwb);
            }
        }

        // update formulas
        for (Sheet sh : _wb) {
            for (Row row : sh) {
                for (Cell cell : row) {
                    if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        updateFormula((XSSFCell) cell, frwb);
                    }
                }
            }
        }
    }

    /**
     * Parse cell formula and re-assemble it back using the specified FormulaRenderingWorkbook.
     *
     * @param cell the cell to update
     * @param frwb the formula rendering workbbok that returns new sheet name
     */
    private void updateFormula(XSSFCell cell, FormulaRenderingWorkbook frwb) {
        CTCellFormula f = cell.getCTCell().getF();
        if (f != null) {
            String formula = f.getStringValue();
            if (formula != null) {
                int sheetIndex = _wb.getSheetIndex(cell.getSheet());
                Ptg[] ptgs = FormulaParser.parse(formula, _fpwb, FormulaType.CELL, sheetIndex);
                String updatedFormula = FormulaRenderer.toFormulaString(frwb, ptgs);
                if (!formula.equals(updatedFormula)) f.setStringValue(updatedFormula);
            }
        }
    }

    /**
     * Parse formula in the named range and re-assemble it  back using the specified FormulaRenderingWorkbook.
     *
     * @param name the name to update
     * @param frwb the formula rendering workbbok that returns new sheet name
     */
    private void updateName(XSSFName name, FormulaRenderingWorkbook frwb) {
        String formula = name.getRefersToFormula();
        if (formula != null) {
            int sheetIndex = name.getSheetIndex();
            Ptg[] ptgs = FormulaParser.parse(formula, _fpwb, FormulaType.NAMEDRANGE, sheetIndex);
            String updatedFormula = FormulaRenderer.toFormulaString(frwb, ptgs);
            if (!formula.equals(updatedFormula)) name.setRefersToFormula(updatedFormula);
        }
    }
}
