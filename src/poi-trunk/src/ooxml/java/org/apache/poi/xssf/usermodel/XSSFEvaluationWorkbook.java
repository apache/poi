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
    private XSSFEvaluationSheet[] _sheetCache;
    
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
        _sheetCache = null;
    }
    
    @Override
    public int getSheetIndex(EvaluationSheet evalSheet) {
        XSSFSheet sheet = ((XSSFEvaluationSheet)evalSheet).getXSSFSheet();
        return _uBook.getSheetIndex(sheet);
    }

    @Override
    public EvaluationSheet getSheet(int sheetIndex) {
        // Performance optimization: build sheet cache the first time this is called
        // to avoid re-creating the XSSFEvaluationSheet each time a new cell is evaluated
        // EvaluationWorkbooks make not guarantee to synchronize changes made to
        // the underlying workbook after the EvaluationWorkbook is created.
        if (_sheetCache == null) {
            final int numberOfSheets = _uBook.getNumberOfSheets();
            _sheetCache = new XSSFEvaluationSheet[numberOfSheets];
            for (int i=0; i < numberOfSheets; i++) {
                _sheetCache[i] = new XSSFEvaluationSheet(_uBook.getSheetAt(i));
            }
        }
        if (sheetIndex < 0 || sheetIndex >= _sheetCache.length) {
            // do this to reuse the out-of-bounds logic and message from XSSFWorkbook
            _uBook.getSheetAt(sheetIndex);
        }
        return _sheetCache[sheetIndex];
    }

    @Override    
    public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
        final XSSFCell cell = ((XSSFEvaluationCell)evalCell).getXSSFCell();
        final int sheetIndex = _uBook.getSheetIndex(cell.getSheet());
        final int rowIndex = cell.getRowIndex();
        return FormulaParser.parse(cell.getCellFormula(this), this, FormulaType.CELL, sheetIndex, rowIndex);
    }
}
