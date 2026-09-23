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
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SharedFormula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

/**
 * XSSF wrapper for a sheet under evaluation
 */
@Internal
final class XSSFEvaluationSheet implements EvaluationSheet {

    private final XSSFSheet _xs;
    private Map<CellKey, EvaluationCell> _cellCache;
    /** parsed formula tokens by cell; a cell is parsed once per evaluator unless it is notified */
    private Map<XSSFCell, Ptg[]> _formulaTokens;
    /**
     * parsed tokens of shared formula masters, keyed by the master {@link CTCellFormula} the sheet
     * currently holds for the group (a new master object is installed when the group changes)
     */
    private Map<CTCellFormula, Ptg[]> _sharedFormulaTokens;

    public XSSFEvaluationSheet(XSSFSheet sheet) {
        _xs = sheet;
    }

    public XSSFSheet getXSSFSheet() {
        return _xs;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#getlastRowNum()
     * @since 4.0.0
     */
    @Override
    public int getLastRowNum() {
        return _xs.getLastRowNum();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#isRowHidden(int)
     * @since 4.1.0
     */
    @Override
    public boolean isRowHidden(int rowIndex) {
        final XSSFRow row = _xs.getRow(rowIndex);
        if (row == null) return false;
        return row.getZeroHeight();
    }

    /* (non-JavaDoc), inherit JavaDoc from EvaluationWorkbook
     * @since 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _cellCache = null;
        _formulaTokens = null;
        _sharedFormulaTokens = null;
    }

    @Override
    public void notifyDeleteCell(int rowIndex, int columnIndex) {
        forgetFormulaTokens(rowIndex, columnIndex);
        if (_cellCache != null) {
            _cellCache.remove(new CellKey(rowIndex, columnIndex));
        }
    }

    /**
     * @since 6.0.0
     */
    @Override
    public void notifyUpdateCell(int rowIndex, int columnIndex) {
        forgetFormulaTokens(rowIndex, columnIndex);
    }

    private void forgetFormulaTokens(int rowIndex, int columnIndex) {
        if (_formulaTokens == null) {
            return;
        }
        // prefer the wrapper: it still knows the cell after it was removed from the sheet
        EvaluationCell wrapper = _cellCache == null ? null : _cellCache.get(new CellKey(rowIndex, columnIndex));
        XSSFCell cell;
        if (wrapper != null) {
            cell = ((XSSFEvaluationCell) wrapper).getXSSFCell();
        } else {
            XSSFRow row = _xs.getRow(rowIndex);
            cell = row == null ? null : row.getCell(columnIndex);
        }
        if (cell != null) {
            _formulaTokens.remove(cell);
        }
    }

    /**
     * @return the parsed formula of the cell, from the cache when it was parsed before
     * @since 6.0.0
     */
    /* package */ Ptg[] getFormulaTokens(XSSFCell cell, BaseXSSFEvaluationWorkbook fpb, int sheetIndex) {
        if (_formulaTokens == null) {
            _formulaTokens = new IdentityHashMap<>();
        }
        Ptg[] ptgs = _formulaTokens.get(cell);
        if (ptgs == null) {
            ptgs = parseFormulaTokens(cell, fpb, sheetIndex);
            _formulaTokens.put(cell, ptgs);
        }
        return ptgs;
    }

    private Ptg[] parseFormulaTokens(XSSFCell cell, BaseXSSFEvaluationWorkbook fpb, int sheetIndex) {
        CTCellFormula f = cell.getCTCell().getF();
        if (f != null && f.getT() == STCellFormulaType.SHARED && !cell.isPartOfArrayFormulaGroup()) {
            Ptg[] shared = sharedFormulaTokens(cell, Math.toIntExact(f.getSi()), fpb, sheetIndex);
            if (shared != null) {
                return shared;
            }
        }
        return FormulaParser.parse(cell.getCellFormula(fpb), fpb, FormulaType.CELL, sheetIndex, cell.getRowIndex());
    }

    /**
     * A cell of a shared formula group takes the master's tokens, shifted to its own position.
     * Parsing the master once per group replaces the per-cell parse, shift, render and re-parse
     * that {@link XSSFCell#getCellFormula()} has to do to produce the formula text.
     *
     * @return {@code null} when the group cannot be handled this way (no master, or a formula
     * whose parse depends on the row, i.e. one with a structured table reference)
     */
    private Ptg[] sharedFormulaTokens(XSSFCell cell, int si, BaseXSSFEvaluationWorkbook fpb, int sheetIndex) {
        CTCellFormula master = _xs.getSharedFormula(si);
        if (master == null) {
            return null;
        }
        String formula = master.getStringValue();
        if (formula == null || formula.indexOf('[') >= 0) {
            return null;
        }
        if (_sharedFormulaTokens == null) {
            _sharedFormulaTokens = new IdentityHashMap<>();
        }
        Ptg[] masterPtgs = _sharedFormulaTokens.get(master);
        if (masterPtgs == null) {
            masterPtgs = FormulaParser.parse(formula, fpb, FormulaType.CELL, sheetIndex, cell.getRowIndex());
            _sharedFormulaTokens.put(master, masterPtgs);
        }
        CellRangeAddress ref = CellRangeAddress.valueOf(master.getRef());
        return new SharedFormula(SpreadsheetVersion.EXCEL2007).convertSharedFormulas(masterPtgs,
                cell.getRowIndex() - ref.getFirstRow(), cell.getColumnIndex() - ref.getFirstColumn());
    }

    @Override
    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        // shortcut evaluation if reference is outside the bounds of existing data
        // see issue #61841 for impact on VLOOKUP in particular
        if (rowIndex > getLastRowNum()) {
            return null;
        }

        // cache for performance: ~30% speedup due to caching
        if (_cellCache == null) {
            _cellCache = new HashMap<>(_xs.getLastRowNum() * 3);
            for (final Row row : _xs) {
                final int rowNum = row.getRowNum();
                for (final Cell cell : row) {
                    // cast is safe, the iterator is just defined using the interface
                    final CellKey key = new CellKey(rowNum, cell.getColumnIndex());
                    final EvaluationCell evalcell = new XSSFEvaluationCell((XSSFCell) cell, this);
                    _cellCache.put(key, evalcell);
                }
            }
        }

        final CellKey key = new CellKey(rowIndex, columnIndex);
        EvaluationCell evalcell = _cellCache.get(key);

        // If cache is stale, update cache with this one cell
        // This is a compromise between rebuilding the entire cache
        // (which would quickly defeat the benefit of the cache)
        // and not caching at all.
        // See bug 59958: Add cells on the fly to the evaluation sheet cache on cache miss
        if (evalcell == null) {
            XSSFRow row = _xs.getRow(rowIndex);
            if (row == null) {
                return null;
            }
            XSSFCell cell = row.getCell(columnIndex);
            if (cell == null) {
                return null;
            }
            evalcell = new XSSFEvaluationCell(cell, this);
            _cellCache.put(key, evalcell);
        }

        return evalcell;
    }

    private static class CellKey {
        private final int _row;
        private final int _col;
        private int _hash = -1; //lazily computed

        protected CellKey(int row, int col) {
            _row = row;
            _col = col;
        }

        @Override
        public int hashCode() {
            if ( _hash == -1 ) {
                 _hash = (17 * 37 + _row) * 37 + _col;
            }
            return _hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CellKey)) {
                return false;
            }
            // assumes other object is one of us, otherwise ClassCastException is thrown
            final CellKey oKey = (CellKey) obj;
            return _row == oKey._row && _col == oKey._col;
        }
    }
}
