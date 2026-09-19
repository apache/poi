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

package org.apache.poi.ss.formula;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.util.Internal;

/**
 * The virtual single-sheet workbook backing the standalone formula engine. It serves
 * both parsing ({@link FormulaParsingWorkbook}) and evaluation ({@link EvaluationWorkbook})
 * of formulas against a {@link VirtualEvaluationSheet}.
 *
 * <p>Everything that does not exist in a standalone setup - defined names, tables,
 * sheet-qualified/3-D references, external workbooks - is rejected or reported as
 * absent so that formulas using them fail early and clearly.</p>
 */
@Internal
final class VirtualFormulaWorkbook implements EvaluationWorkbook, FormulaParsingWorkbook {

    static final String SHEET_NAME = "Sheet1";

    private final SpreadsheetVersion _version;
    private final UDFFinder _udfFinder;
    private final VirtualEvaluationSheet _sheet;
    private final VirtualEvaluationName[] _names;
    private final Map<String, VirtualEvaluationName> _namesByName;

    /**
     * @param sheet the sheet backing evaluation, or {@code null} for a parse-only workbook
     */
    VirtualFormulaWorkbook(SpreadsheetVersion version, UDFFinder udfFinder, VirtualEvaluationSheet sheet, String[] inputNames) {
        _version = version;
        _udfFinder = udfFinder;
        _sheet = sheet;
        _names = new VirtualEvaluationName[inputNames.length];
        _namesByName = new HashMap<>();
        for (int i = 0; i < inputNames.length; i++) {
            _names[i] = new VirtualEvaluationName(inputNames[i], i);
            _namesByName.put(inputNames[i].toLowerCase(Locale.ROOT), _names[i]);
        }
    }

    @Override
    public String getSheetName(int sheetIndex) {
        requireOwnSheet(sheetIndex);
        return SHEET_NAME;
    }

    @Override
    public int getSheetIndex(EvaluationSheet sheet) {
        return sheet == _sheet ? 0 : -1;
    }

    @Override
    public int getSheetIndex(String sheetName) {
        return SHEET_NAME.equalsIgnoreCase(sheetName) ? 0 : -1;
    }

    @Override
    public EvaluationSheet getSheet(int sheetIndex) {
        requireOwnSheet(sheetIndex);
        if (_sheet == null) {
            throw new IllegalStateException("This workbook is only configured for formula parsing");
        }
        return _sheet;
    }

    private void requireOwnSheet(int sheetIndex) {
        if (sheetIndex != 0) {
            throw new IllegalArgumentException("Invalid sheet index " + sheetIndex + ", standalone formula engines have a single sheet");
        }
    }

    @Override
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        throw unsupported("External sheet references");
    }

    @Override
    public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        if (externalWorkbookNumber >= 0) {
            throw unsupported("External workbook references");
        }
        // an external-style reference to our own workbook - reported as local so that
        // unknown function names resolve through the local name/UDF machinery
        return new ExternalSheet(null, firstSheetName);
    }

    @Override
    public int convertFromExternSheetIndex(int externSheetIndex) {
        return 0;
    }

    @Override
    public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
        throw unsupported("External name references");
    }

    @Override
    public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
        throw unsupported("External name references");
    }

    @Override
    public EvaluationName getName(NamePtg namePtg) {
        int index = namePtg.getIndex();
        if (index < 0 || index >= _names.length) {
            return null;
        }
        return _names[index];
    }

    @Override
    public EvaluationName getName(String name, int sheetIndex) {
        return _namesByName.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * A declared input exposed as a defined name whose definition is the plain
     * reference to the input's cell, so that formulas can reference inputs by name.
     */
    private static final class VirtualEvaluationName implements EvaluationName {

        private final String _name;
        private final int _index;
        private final Ptg[] _definition;

        VirtualEvaluationName(String name, int index) {
            _name = name;
            _index = index;
            _definition = new Ptg[]{new RefPtg(0, index, false, false)};
        }

        @Override
        public String getNameText() {
            return _name;
        }

        @Override
        public boolean isFunctionName() {
            return false;
        }

        @Override
        public boolean hasFormula() {
            return true;
        }

        @Override
        public Ptg[] getNameDefinition() {
            return _definition;
        }

        @Override
        public boolean isRange() {
            return true;
        }

        @Override
        public NamePtg createPtg() {
            return new NamePtg(_index);
        }
    }

    @Override
    public String resolveNameXText(NameXPtg ptg) {
        throw unsupported("External name references");
    }

    @Override
    public Ptg[] getFormulaTokens(EvaluationCell cell) {
        throw new IllegalStateException("Standalone formula engines do not hold formula cells");
    }

    @Override
    public UDFFinder getUDFFinder() {
        return _udfFinder;
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _version;
    }

    @Override
    public void clearAllCachedResultValues() {
        // nothing is cached
    }

    // FormulaParsingWorkbook

    @Override
    public Name createName() {
        throw unsupported("Defined names");
    }

    @Override
    public Table getTable(String tableName) {
        return null;
    }

    @Override
    public Ptg getNameXPtg(String name, SheetIdentifier sheet) {
        // unknown functions are tolerated at parse time and rejected at evaluation time
        return null;
    }

    @Override
    public Ptg get3DReferencePtg(CellReference cell, SheetIdentifier sheet) {
        throw unsupported("Sheet-qualified references");
    }

    @Override
    public Ptg get3DReferencePtg(AreaReference area, SheetIdentifier sheet) {
        throw unsupported("Sheet-qualified references");
    }

    @Override
    public int getExternalSheetIndex(String sheetName) {
        throw unsupported("Sheet-qualified references");
    }

    @Override
    public int getExternalSheetIndex(String workbookName, String sheetName) {
        throw unsupported("External workbook references");
    }

    private static UnsupportedOperationException unsupported(String what) {
        return new UnsupportedOperationException(what + " are not supported by standalone formula engines");
    }
}
