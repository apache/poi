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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.NameXPxg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.udf.IndexedUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Internal POI use only - parent of XSSF and SXSSF evaluation workbooks
 */
@Internal
public abstract class BaseXSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {
    protected final XSSFWorkbook _uBook;

    // lazily populated. This should only be accessed through getTableCache
    // keys are lower-case to make this a quasi-case-insensitive map
    private Map<String, XSSFTable> _tableCache;


    protected BaseXSSFEvaluationWorkbook(XSSFWorkbook book) {
        _uBook = book;
    }

    /* (non-JavaDoc), inherit JavaDoc from EvaluationWorkbook
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _tableCache = null;
    }

    private int convertFromExternalSheetIndex(int externSheetIndex) {
        return externSheetIndex;
    }
    /**
     * XSSF doesn't use external sheet indexes, so when asked treat
     * it just as a local index
     */
    @Override
    public int convertFromExternSheetIndex(int externSheetIndex) {
        return externSheetIndex;
    }
    /**
     * @return  the external sheet index of the sheet with the given internal
     * index. Used by some of the more obscure formula and named range things.
     * Fairly easy on XSSF (we think...) since the internal and external
     * indices are the same
     */
    private int convertToExternalSheetIndex(int sheetIndex) {
        return sheetIndex;
    }

    @Override
    public int getExternalSheetIndex(String sheetName) {
        int sheetIndex = _uBook.getSheetIndex(sheetName);
        return convertToExternalSheetIndex(sheetIndex);
    }

    private int resolveBookIndex(String bookName) {
        // Strip the [] wrapper, if still present
        if (bookName.startsWith("[") && bookName.endsWith("]")) {
            bookName = bookName.substring(1, bookName.length()-2);
        }

        // Is it already in numeric form?
        try {
            return Integer.parseInt(bookName);
        } catch (NumberFormatException e) {}

        // Look up an External Link Table for this name
        List<ExternalLinksTable> tables = _uBook.getExternalLinksTable();
        int index = findExternalLinkIndex(bookName, tables);
        if (index != -1) return index;

        // Is it an absolute file reference?
        if (bookName.startsWith("'file:///") && bookName.endsWith("'")) {
            String relBookName = bookName.substring(bookName.lastIndexOf('/')+1);
            relBookName = relBookName.substring(0, relBookName.length()-1); // Trailing '

            // Try with this name
            index = findExternalLinkIndex(relBookName, tables);
            if (index != -1) return index;

            // If we get here, it's got no associated proper links yet
            // So, add the missing reference and return
            // Note - this is really rather nasty...
            ExternalLinksTable fakeLinkTable = new FakeExternalLinksTable(relBookName);
            tables.add(fakeLinkTable);
            return tables.size(); // 1 based results, 0 = current workbook
        }

        // Not properly referenced
        throw new RuntimeException("Book not linked for filename " + bookName);
    }
    /* This is case-sensitive. Is that correct? */
    private int findExternalLinkIndex(String bookName, List<ExternalLinksTable> tables) {
        int i = 0;
        for (ExternalLinksTable table : tables) {
            if (table.getLinkedFileName().equals(bookName)) {
                return i+1; // 1 based results, 0 = current workbook
            }
            i++;
        }
        return -1;
    }
    private static class FakeExternalLinksTable extends ExternalLinksTable {
        private final String fileName;
        private FakeExternalLinksTable(String fileName) {
            this.fileName = fileName;
        }
        @Override
        public String getLinkedFileName() {
            return fileName;
        }
    }

    /**
     * Return EvaluationName wrapper around the matching XSSFName (named range)
     * @param name case-aware but case-insensitive named range in workbook
     * @param sheetIndex index of sheet if named range scope is limited to one sheet
     *         if named range scope is global to the workbook, sheetIndex is -1.
     * @return If name is a named range in the workbook, returns
     *  EvaluationName corresponding to that named range
     *  Returns null if there is no named range with the same name and scope in the workbook
     */
    @Override
    public EvaluationName getName(String name, int sheetIndex) {
        for (int i = 0; i < _uBook.getNumberOfNames(); i++) {
            XSSFName nm = _uBook.getNameAt(i);
            String nameText = nm.getNameName();
            int nameSheetindex = nm.getSheetIndex();
            if (name.equalsIgnoreCase(nameText) &&
                   (nameSheetindex == -1 || nameSheetindex == sheetIndex)) {
                return new Name(nm, i, this);
            }
        }
        return sheetIndex == -1 ? null : getName(name, -1);
    }

    @Override
    public String getSheetName(int sheetIndex) {
        return _uBook.getSheetName(sheetIndex);
    }

    @Override
    public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
        throw new IllegalStateException("HSSF-style external references are not supported for XSSF");
    }

    @Override
    public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
        if (externalWorkbookNumber > 0) {
            // External reference - reference is 1 based, link table is 0 based
            int linkNumber = externalWorkbookNumber - 1;
            ExternalLinksTable linkTable = _uBook.getExternalLinksTable().get(linkNumber);

            for (org.apache.poi.ss.usermodel.Name name : linkTable.getDefinedNames()) {
                if (name.getNameName().equals(nameName)) {
                    // HSSF returns one sheet higher than normal, and various bits
                    //  of the code assume that. So, make us match that behaviour!
                    int nameSheetIndex = name.getSheetIndex() + 1;

                    // TODO Return a more specialised form of this, see bug #56752
                    // Should include the cached values, for in case that book isn't available
                    // Should support XSSF stuff lookups
                    return new ExternalName(nameName, -1, nameSheetIndex);
                }
            }
            throw new IllegalArgumentException("Name '"+nameName+"' not found in " +
                                               "reference to " + linkTable.getLinkedFileName());
        } else {
            // Internal reference
            int nameIdx = _uBook.getNameIndex(nameName);
            return new ExternalName(nameName, nameIdx, 0);  // TODO Is this right?
        }

    }

    /**
     * Return an external name (named range, function, user-defined function) Pxg
     */
    @Override
    public NameXPxg getNameXPtg(String name, SheetIdentifier sheet) {
        // First, try to find it as a User Defined Function
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        FreeRefFunction func = udfFinder.findFunction(name);
        if (func != null) {
            return new NameXPxg(null, name);
        }

        // Otherwise, try it as a named range
        if (sheet == null) {
            if (!_uBook.getNames(name).isEmpty()) {
                return new NameXPxg(null, name);
            }
            return null;
        }
        if (sheet.getSheetIdentifier() == null) {
            // Workbook + Named Range only
            int bookIndex = resolveBookIndex(sheet.getBookName());
            return new NameXPxg(bookIndex, null, name);
        }

        // Use the sheetname and process
        String sheetName = sheet.getSheetIdentifier().getName();

        if (sheet.getBookName() != null) {
            int bookIndex = resolveBookIndex(sheet.getBookName());
            return new NameXPxg(bookIndex, sheetName, name);
        } else {
            return new NameXPxg(sheetName, name);
        }
    }
    @Override
    public Ptg get3DReferencePtg(CellReference cell, SheetIdentifier sheet) {
        if (sheet.getBookName() != null) {
            int bookIndex = resolveBookIndex(sheet.getBookName());
            return new Ref3DPxg(bookIndex, sheet, cell);
        } else {
            return new Ref3DPxg(sheet, cell);
        }
    }
    @Override
    public Ptg get3DReferencePtg(AreaReference area, SheetIdentifier sheet) {
        if (sheet.getBookName() != null) {
            int bookIndex = resolveBookIndex(sheet.getBookName());
            return new Area3DPxg(bookIndex, sheet, area);
        } else {
            return new Area3DPxg(sheet, area);
        }
    }

    @Override
    public String resolveNameXText(NameXPtg n) {
        int idx = n.getNameIndex();
        String name = null;

        // First, try to find it as a User Defined Function
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        name = udfFinder.getFunctionName(idx);
        if (name != null) return name;

        // Otherwise, try it as a named range
        XSSFName xname = _uBook.getNameAt(idx);
        if (xname != null) {
            name = xname.getNameName();
        }

        return name;
    }

    @Override
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        throw new IllegalStateException("HSSF-style external references are not supported for XSSF");
    }
    @Override
    public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        String workbookName;
        if (externalWorkbookNumber > 0) {
            // External reference - reference is 1 based, link table is 0 based
            int linkNumber = externalWorkbookNumber - 1;
            ExternalLinksTable linkTable = _uBook.getExternalLinksTable().get(linkNumber);
            workbookName = linkTable.getLinkedFileName();
        } else {
            // Internal reference
            workbookName = null;
        }

        if (lastSheetName == null || firstSheetName.equals(lastSheetName)) {
            return new ExternalSheet(workbookName, firstSheetName);
        } else {
            return new ExternalSheetRange(workbookName, firstSheetName, lastSheetName);
        }
    }

    @Override
    @NotImplemented
    public int getExternalSheetIndex(String workbookName, String sheetName) {
        throw new RuntimeException("not implemented yet");
    }
    @Override
    public int getSheetIndex(String sheetName) {
        return _uBook.getSheetIndex(sheetName);
    }

    @Override
    public String getSheetFirstNameByExternSheet(int externSheetIndex) {
        int sheetIndex = convertFromExternalSheetIndex(externSheetIndex);
        return _uBook.getSheetName(sheetIndex);
    }
    @Override
    public String getSheetLastNameByExternSheet(int externSheetIndex) {
        // XSSF does multi-sheet references differently, so this is the same as the first
        return getSheetFirstNameByExternSheet(externSheetIndex);
    }

    @Override
    public String getNameText(NamePtg namePtg) {
        return _uBook.getNameAt(namePtg.getIndex()).getNameName();
    }
    @Override
    public EvaluationName getName(NamePtg namePtg) {
        int ix = namePtg.getIndex();
        return new Name(_uBook.getNameAt(ix), ix, this);
    }
    @Override
    public XSSFName createName() {
        return _uBook.createName();
    }

    /*
     * TODO: data tables are stored at the workbook level in XSSF, but are bound to a single sheet.
     *       The current code structure has them hanging off XSSFSheet, but formulas reference them
     *       only by name (names are global, and case insensitive).
     *       This map stores names as lower case for case-insensitive lookups.
     *
     * FIXME: Caching tables by name here for fast formula lookup means the map is out of date if
     *       a table is renamed or added/removed to a sheet after the map is created.
     *
     *       Perhaps tables can be managed similar to PivotTable references above?
     */
    private Map<String, XSSFTable> getTableCache() {
        if ( _tableCache != null ) {
            return _tableCache;
        }
        _tableCache = new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Sheet sheet : _uBook) {
            for (XSSFTable tbl : ((XSSFSheet)sheet).getTables()) {
                _tableCache.put(tbl.getName(), tbl);
            }
        }
        return _tableCache;
    }

    /**
     * Returns the data table with the given name (case insensitive).
     * Tables are cached for performance (formula evaluation looks them up by name repeatedly).
     * After the first table lookup, adding or removing a table from the document structure will cause trouble.
     * This is meant to be used on documents whose structure is essentially static at the point formulas are evaluated.
     *
     * @param name the data table name (case-insensitive)
     * @return The Data table in the workbook named {@code name}, or {@code null} if no table is named {@code name}.
     * @since 3.15 beta 2
     */
    @Override
    public XSSFTable getTable(String name) {
        if (name == null) return null;
        return getTableCache().get(name);
    }

    @Override
    public UDFFinder getUDFFinder(){
        return _uBook.getUDFFinder();
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion(){
        return SpreadsheetVersion.EXCEL2007;
    }

    private static final class Name implements EvaluationName {

        private final XSSFName _nameRecord;
        private final int _index;
        private final FormulaParsingWorkbook _fpBook;

        public Name(XSSFName name, int index, FormulaParsingWorkbook fpBook) {
            _nameRecord = name;
            _index = index;
            _fpBook = fpBook;
        }

        @Override
        public Ptg[] getNameDefinition() {

            return FormulaParser.parse(_nameRecord.getRefersToFormula(), _fpBook, FormulaType.NAMEDRANGE, _nameRecord.getSheetIndex());
        }

        @Override
        public String getNameText() {
            return _nameRecord.getNameName();
        }

        @Override
        public boolean hasFormula() {
            // TODO - no idea if this is right
            CTDefinedName ctn = _nameRecord.getCTName();
            String strVal = ctn.getStringValue();
            return !ctn.getFunction() && strVal != null && strVal.length() > 0;
        }

        @Override
        public boolean isFunctionName() {
            return _nameRecord.isFunctionName();
        }

        @Override
        public boolean isRange() {
            return hasFormula(); // TODO - is this right?
        }
        @Override
        public NamePtg createPtg() {
            return new NamePtg(_index);
        }
    }
}
