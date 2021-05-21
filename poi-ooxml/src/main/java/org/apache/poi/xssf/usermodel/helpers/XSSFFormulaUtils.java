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

import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Pxg;
import org.apache.poi.ss.formula.ptg.Pxg3D;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility to update formulas and named ranges when a sheet name was changed
 */
public final class XSSFFormulaUtils {
    private final XSSFWorkbook _wb;
    private final XSSFEvaluationWorkbook _fpwb;

    public XSSFFormulaUtils(XSSFWorkbook wb) {
        _wb = wb;
        _fpwb = XSSFEvaluationWorkbook.create(_wb);
    }

    /**
     * Update sheet name in all charts, formulas and named ranges.
     * Called from {@link XSSFWorkbook#setSheetName(int, String)}
     * <p>
     * The idea is to parse every formula and render it back to string
     * with the updated sheet name. This is done by parsing into Ptgs,
     * looking for ones with sheet references in them, and changing those
     *
     * @param sheetIndex the 0-based index of the sheet being changed
     * @param oldName    the old sheet name
     * @param newName    the new sheet name
     */
    public void updateSheetName(final int sheetIndex, final String oldName, final String newName) {
        // update named ranges
        for (XSSFName nm : _wb.getAllNames()) {
            if (nm.getSheetIndex() == -1 || nm.getSheetIndex() == sheetIndex) {
                updateName(nm, oldName, newName);
            }
        }

        // update formulas
        for (Sheet sh : _wb) {
            for (Row row : sh) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.FORMULA) {
                        updateFormula((XSSFCell) cell, oldName, newName);
                    }
                }
            }
        }

        // update charts
        List<POIXMLDocumentPart> rels = _wb.getSheetAt(sheetIndex).getRelations();
        for (POIXMLDocumentPart r : rels) {
            if (r instanceof XSSFDrawing) {
                XSSFDrawing dg = (XSSFDrawing) r;
                for (XSSFChart chart : dg.getCharts()) {
                    Node dom = chart.getCTChartSpace().getDomNode();
                    updateDomSheetReference(dom, oldName, newName);
                }
            }
        }
    }

    /**
     * Parse cell formula and re-assemble it back using the new sheet name
     *
     * @param cell the cell to update
     */
    private void updateFormula(XSSFCell cell, String oldName, String newName) {
        CTCellFormula f = cell.getCTCell().getF();
        if (f != null) {
            String formula = f.getStringValue();
            if (formula != null && formula.length() > 0) {
                int sheetIndex = _wb.getSheetIndex(cell.getSheet());
                Ptg[] ptgs = FormulaParser.parse(formula, _fpwb, FormulaType.CELL, sheetIndex, cell.getRowIndex());
                for (Ptg ptg : ptgs) {
                    updatePtg(ptg, oldName, newName);
                }
                String updatedFormula = FormulaRenderer.toFormulaString(_fpwb, ptgs);
                if (!formula.equals(updatedFormula)) {
                    f.setStringValue(updatedFormula);
                }
            }
        }
    }

    /**
     * Parse formula in the named range and re-assemble it back using the new sheet name.
     *
     * @param name the name to update
     */
    private void updateName(XSSFName name, String oldName, String newName) {
        String formula = name.getRefersToFormula();
        if (formula != null) {
            int sheetIndex = name.getSheetIndex();
            int rowIndex = -1; //don't care
            Ptg[] ptgs = FormulaParser.parse(formula, _fpwb, FormulaType.NAMEDRANGE, sheetIndex, rowIndex);
            for (Ptg ptg : ptgs) {
                updatePtg(ptg, oldName, newName);
            }
            String updatedFormula = FormulaRenderer.toFormulaString(_fpwb, ptgs);
            if (!formula.equals(updatedFormula)) {
                name.setRefersToFormula(updatedFormula);
            }
        }
    }

    private void updatePtg(Ptg ptg, String oldName, String newName) {
        if (ptg instanceof Pxg) {
            Pxg pxg = (Pxg)ptg;
            if (pxg.getExternalWorkbookNumber() < 1) {
                if (pxg.getSheetName() != null &&
                        pxg.getSheetName().equals(oldName)) {
                    pxg.setSheetName(newName);
                }
                if (pxg instanceof Pxg3D) {
                    Pxg3D pxg3D = (Pxg3D)pxg;
                    if (pxg3D.getLastSheetName() != null &&
                            pxg3D.getLastSheetName().equals(oldName)) {
                        pxg3D.setLastSheetName(newName);
                    }
                }
            }
        }
    }


    /**
     * Parse the DOM tree recursively searching for text containing reference to the old sheet name and replacing it.
     *
     * @param dom the XML node in which to perform the replacement.
     *
     * Code extracted from: <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=54470">Bug 54470</a>
     */
    private void updateDomSheetReference(Node dom, final String oldName, final String newName) {
        String value = dom.getNodeValue();
        if (value != null) {
            // make sure the value contains the old sheet and not a similar sheet
            // (ex: Valid: 'Sheet1'! or Sheet1! ; NotValid: 'Sheet1Test'! or Sheet1Test!)
            if (value.contains(oldName+"!") || value.contains(oldName+"'!")) {
                XSSFName temporary = _wb.createName();
                temporary.setRefersToFormula(value);
                updateName(temporary, oldName, newName);
                dom.setNodeValue(temporary.getRefersToFormula());
                _wb.removeName(temporary);
            }
        }
        NodeList nl = dom.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            updateDomSheetReference(nl.item(i), oldName, newName);
        }
    }

}
