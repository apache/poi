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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.util.Internal;

/**
 * Internal POI use only
 */
@Internal
public final class XSSFEvaluationWorkbook extends BaseXSSFEvaluationWorkbook {
    private final Map<XSSFSheet, XSSFEvaluationSheet> _sheetCache = new HashMap<>();

    public static XSSFEvaluationWorkbook create(XSSFWorkbook book) {
        if (book == null) {
            return null;
        }
        return new XSSFEvaluationWorkbook(book);
    }

    private XSSFEvaluationWorkbook(XSSFWorkbook book) {
        super(book);
    }

    /* (non-JavaDoc), inherit JavaDoc from EvaluationSheet
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        super.clearAllCachedResultValues();
        _sheetCache.clear();
    }

    @Override
    public int getSheetIndex(EvaluationSheet evalSheet) {
        XSSFSheet sheet = ((XSSFEvaluationSheet)evalSheet).getXSSFSheet();
        return _uBook.getSheetIndex(sheet);
    }

    @Override
    public EvaluationSheet getSheet(int sheetIndex) {
        // verify index and let the method in _uBook throw the exception so we report
        // it the same way as in other places
        if (sheetIndex < 0 || sheetIndex >= _uBook.getNumberOfSheets()) {
            // this will throw an exception now as the index is out of bounds
            _uBook.getSheetAt(sheetIndex);
        }

        // Performance optimization: build sheet cache for each sheet to avoid re-creating
        // the XSSFEvaluationSheet each time a new cell is evaluated
        // EvaluationWorkbooks make not guarantee to synchronize changes made to
        // the underlying workbook after the EvaluationWorkbook is created.
        final XSSFSheet sheet = _uBook.getSheetAt(sheetIndex);
        return _sheetCache.computeIfAbsent(sheet, rows -> new XSSFEvaluationSheet(sheet));
    }

    @Override
    public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
        final XSSFCell cell = ((XSSFEvaluationCell)evalCell).getXSSFCell();
        final int sheetIndex = _uBook.getSheetIndex(cell.getSheet());
        final int rowIndex = cell.getRowIndex();
        return FormulaParser.parse(cell.getCellFormula(this), this, FormulaType.CELL, sheetIndex, rowIndex);
    }
}
