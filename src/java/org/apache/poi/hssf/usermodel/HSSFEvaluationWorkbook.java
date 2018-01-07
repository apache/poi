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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.SheetRangeIdentifier;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Internal;

/**
 * Internal POI use only
 */
@Internal
public final class HSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {
    private final HSSFWorkbook _uBook;
    private final InternalWorkbook _iBook;

    public static HSSFEvaluationWorkbook create(HSSFWorkbook book) {
        if (book == null) {
            return null;
        }
        return new HSSFEvaluationWorkbook(book);
    }

    private HSSFEvaluationWorkbook(HSSFWorkbook book) {
        _uBook = book;
        _iBook = book.getWorkbook();
    }

    /* (non-JavaDoc), inherit JavaDoc from EvaluationWorkbook
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        // nothing to do
    }
    
    @Override
    public HSSFName createName() {
        return _uBook.createName();
    }

    @Override
    public int getExternalSheetIndex(String sheetName) {
        int sheetIndex = _uBook.getSheetIndex(sheetName);
        return _iBook.checkExternSheet(sheetIndex);
    }
    @Override
    public int getExternalSheetIndex(String workbookName, String sheetName) {
        return _iBook.getExternalSheetIndex(workbookName, sheetName);
    }
    
    @Override
    public Ptg get3DReferencePtg(CellReference cr, SheetIdentifier sheet) {
        int extIx = getSheetExtIx(sheet);
        return new Ref3DPtg(cr, extIx);
    }
    @Override
    public Ptg get3DReferencePtg(AreaReference areaRef, SheetIdentifier sheet) {
        int extIx = getSheetExtIx(sheet);
        return new Area3DPtg(areaRef, extIx);
    }
    /**
     * Return an external name (named range, function, user-defined function) Ptg
     */
    @Override
    public NameXPtg getNameXPtg(String name, SheetIdentifier sheet) {
        int sheetRefIndex = getSheetExtIx(sheet);
        return _iBook.getNameXPtg(name, sheetRefIndex, _uBook.getUDFFinder());
    }

    /**
     * Lookup a named range by its name.
     *
     * @param name the name to search
     * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
     * The sheet index is required to resolve sheet-level names. <code>-1</code> means workbook-global names
     */
    @Override
    public EvaluationName getName(String name, int sheetIndex) {
        for(int i=0; i < _iBook.getNumNames(); i++) {
            NameRecord nr = _iBook.getNameRecord(i);
            if (nr.getSheetNumber() == sheetIndex+1 && name.equalsIgnoreCase(nr.getNameText())) {
                return new Name(nr, i);
            }
        }
        return sheetIndex == -1 ? null : getName(name, -1);
    }

    @Override
    public int getSheetIndex(EvaluationSheet evalSheet) {
        HSSFSheet sheet = ((HSSFEvaluationSheet)evalSheet).getHSSFSheet();
        return _uBook.getSheetIndex(sheet);
    }
    @Override
    public int getSheetIndex(String sheetName) {
        return _uBook.getSheetIndex(sheetName);
    }

    @Override
    public String getSheetName(int sheetIndex) {
        return _uBook.getSheetName(sheetIndex);
    }

    @Override
    public EvaluationSheet getSheet(int sheetIndex) {
        // TODO Cache these evaluation sheets so they aren't re-generated on every getSheet call
        return new HSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex));
    }
    @Override
    public int convertFromExternSheetIndex(int externSheetIndex) {
        // TODO Update this to expose first and last sheet indexes
        return _iBook.getFirstSheetIndexFromExternSheetIndex(externSheetIndex);
    }

    @Override
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        ExternalSheet sheet = _iBook.getExternalSheet(externSheetIndex);
        if (sheet == null) {
            // Try to treat it as a local sheet
            int localSheetIndex = convertFromExternSheetIndex(externSheetIndex);
            if (localSheetIndex == -1) {
                // The sheet referenced can't be found, sorry
                return null;
            }
            if (localSheetIndex == -2) {
                // Not actually sheet based at all - is workbook scoped
                return null;
            }
            
            // Look up the local sheet
            String sheetName = getSheetName(localSheetIndex);
            
            // Is it a single local sheet, or a range?
            int lastLocalSheetIndex = _iBook.getLastSheetIndexFromExternSheetIndex(externSheetIndex);
            if (lastLocalSheetIndex == localSheetIndex) {
                sheet = new ExternalSheet(null, sheetName);
            } else {
                String lastSheetName = getSheetName(lastLocalSheetIndex);
                sheet = new ExternalSheetRange(null, sheetName, lastSheetName);
            }
        }
        return sheet;
    }

    /**
     * @throws IllegalStateException: XSSF-style external references are not supported for HSSF
     */
    @Override
    public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        throw new IllegalStateException("XSSF-style external references are not supported for HSSF");
    }

    @Override
    public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
        return _iBook.getExternalName(externSheetIndex, externNameIndex);
    }

    /**
     * @throws IllegalStateException: XSSF-style external names are not supported for HSSF
     */
    @Override
    public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
        throw new IllegalStateException("XSSF-style external names are not supported for HSSF");
    }

    @Override
    public String resolveNameXText(NameXPtg n) {
        return _iBook.resolveNameXText(n.getSheetRefIndex(), n.getNameIndex());
    }

    @Override
    public String getSheetFirstNameByExternSheet(int externSheetIndex) {
        return _iBook.findSheetFirstNameFromExternSheet(externSheetIndex);
    }
    @Override
    public String getSheetLastNameByExternSheet(int externSheetIndex) {
        return _iBook.findSheetLastNameFromExternSheet(externSheetIndex);
    }
    @Override
    public String getNameText(NamePtg namePtg) {
        return _iBook.getNameRecord(namePtg.getIndex()).getNameText();
    }
    @Override
    public EvaluationName getName(NamePtg namePtg) {
        int ix = namePtg.getIndex();
        return new Name(_iBook.getNameRecord(ix), ix);
    }

    @Override
    public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
        HSSFCell cell = ((HSSFEvaluationCell)evalCell).getHSSFCell();
        // re-parsing the formula text also works, but is a waste of time
        // return HSSFFormulaParser.parse(cell.getCellFormula(), _uBook, FormulaType.CELL, _uBook.getSheetIndex(cell.getSheet()));
        // It is useful within the tests to make sure that all formulas POI can evaluate can also be parsed.
        // see HSSFFileHandler.handleFile instead
        FormulaRecordAggregate fra = (FormulaRecordAggregate) cell.getCellValueRecord();
        return fra.getFormulaTokens();
    }

    @Override
    public UDFFinder getUDFFinder(){
        return _uBook.getUDFFinder();
    }

    private static final class Name implements EvaluationName {
        private final NameRecord _nameRecord;
        private final int _index;

        public Name(NameRecord nameRecord, int index) {
            _nameRecord = nameRecord;
            _index = index;
        }
        @Override
        public Ptg[] getNameDefinition() {
            return _nameRecord.getNameDefinition();
        }
        @Override
        public String getNameText() {
            return _nameRecord.getNameText();
        }
        @Override
        public boolean hasFormula() {
            return _nameRecord.hasFormula();
        }
        @Override
        public boolean isFunctionName() {
            return _nameRecord.isFunctionName();
        }
        @Override
        public boolean isRange() {
            return _nameRecord.hasFormula(); // TODO - is this right?
        }
        @Override
        public NamePtg createPtg() {
            return new NamePtg(_index);
        }
    }

    private int getSheetExtIx(SheetIdentifier sheetIden) {
        int extIx;
        if (sheetIden == null) {
            extIx = -1;
        } else {
            String workbookName = sheetIden.getBookName(); 
            String firstSheetName = sheetIden.getSheetIdentifier().getName();
            String lastSheetName = firstSheetName;
            
            if (sheetIden instanceof SheetRangeIdentifier) {
                lastSheetName = ((SheetRangeIdentifier)sheetIden).getLastSheetIdentifier().getName();
            }
            
            if (workbookName == null) {
                int firstSheetIndex = _uBook.getSheetIndex(firstSheetName);
                int lastSheetIndex = _uBook.getSheetIndex(lastSheetName);
                extIx = _iBook.checkExternSheet(firstSheetIndex, lastSheetIndex);
            } else {
                extIx = _iBook.getExternalSheetIndex(workbookName, firstSheetName, lastSheetName);
            }
        }
        return extIx;
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion(){
        return SpreadsheetVersion.EXCEL97;
    }

    /**
      * @throws IllegalStateException: data tables are not supported in Excel 97-2003 format
      */
    @Override
    public Table getTable(String name) {
        throw new IllegalStateException("XSSF-style tables are not supported for HSSF");
    }
}
