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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SheetIdentifier;
import org.apache.poi.ss.formula.SheetRangeIdentifier;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Internal POI use only
 */
public final class HSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {

    private static POILogger logger = POILogFactory.getLogger(HSSFEvaluationWorkbook.class);
    private final HSSFWorkbook uBook;
    private final InternalWorkbook iBook;

    public static HSSFEvaluationWorkbook create(HSSFWorkbook book) {
        if (book == null) {
            return null;
        }
        return new HSSFEvaluationWorkbook(book);
    }

    private HSSFEvaluationWorkbook(HSSFWorkbook book) {
        uBook = book;
        iBook = book.getWorkbook();
    }

    public int getExternalSheetIndex(String sheetName) {
        int sheetIndex = uBook.getSheetIndex(sheetName);
        return iBook.checkExternSheet(sheetIndex);
    }

    public int getExternalSheetIndex(String workbookName, String sheetName) {
        return iBook.getExternalSheetIndex(workbookName, sheetName);
    }

    public Ptg get3DReferencePtg(CellReference cr, SheetIdentifier sheet) {
        int extIx = getSheetExtIx(sheet);
        return new Ref3DPtg(cr, extIx);
    }

    public Ptg get3DReferencePtg(AreaReference areaRef, SheetIdentifier sheet) {
        int extIx = getSheetExtIx(sheet);
        return new Area3DPtg(areaRef, extIx);
    }

    public NameXPtg getNameXPtg(String name, SheetIdentifier sheet) {
        int sheetRefIndex = getSheetExtIx(sheet);
        return iBook.getNameXPtg(name, sheetRefIndex, uBook.getUDFFinder());
    }

    /**
     * Looks up a named range by its name.
     *
     * @param name       the name to search
     * @param sheetIndex the 0-based index of the sheet this formula belongs to.
     *                   The sheet index is required to resolve sheet-level names.
     *                   <code>-1</code> means workbook-global names
     */
    public EvaluationName getName(String name, int sheetIndex) {
        for (int i = 0; i < iBook.getNumNames(); i++) {
            NameRecord nr = iBook.getNameRecord(i);
            if (nr.getSheetNumber() == sheetIndex + 1 && name.equalsIgnoreCase(nr.getNameText())) {
                return new Name(nr, i);
            }
        }
        return sheetIndex == -1 ? null : getName(name, -1);
    }

    public int getSheetIndex(EvaluationSheet evalSheet) {
        HSSFSheet sheet = ((HSSFEvaluationSheet) evalSheet).getHSSFSheet();
        return uBook.getSheetIndex(sheet);
    }

    public int getSheetIndex(String sheetName) {
        return uBook.getSheetIndex(sheetName);
    }

    public String getSheetName(int sheetIndex) {
        return uBook.getSheetName(sheetIndex);
    }

    public EvaluationSheet getSheet(int sheetIndex) {
        return new HSSFEvaluationSheet(uBook.getSheetAt(sheetIndex));
    }

    public int convertFromExternSheetIndex(int externSheetIndex) {
        // TODO Update this to expose first and last sheet indexes
        return iBook.getFirstSheetIndexFromExternSheetIndex(externSheetIndex);
    }

    public ExternalSheet getExternalSheet(int externSheetIndex) {
        ExternalSheet sheet = iBook.getExternalSheet(externSheetIndex);
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
            int lastLocalSheetIndex = iBook.getLastSheetIndexFromExternSheetIndex(externSheetIndex);
            if (lastLocalSheetIndex == localSheetIndex) {
                sheet = new ExternalSheet(null, sheetName);
            } else {
                String lastSheetName = getSheetName(lastLocalSheetIndex);
                sheet = new ExternalSheetRange(null, sheetName, lastSheetName);
            }
        }
        return sheet;
    }

    public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        throw new IllegalStateException("XSSF-style external references are not supported for HSSF");
    }

    public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
        return iBook.getExternalName(externSheetIndex, externNameIndex);
    }

    public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
        throw new IllegalStateException("XSSF-style external names are not supported for HSSF");
    }

    public String resolveNameXText(NameXPtg n) {
        return iBook.resolveNameXText(n.getSheetRefIndex(), n.getNameIndex());
    }

    public String getSheetFirstNameByExternSheet(int externSheetIndex) {
        return iBook.findSheetFirstNameFromExternSheet(externSheetIndex);
    }

    public String getSheetLastNameByExternSheet(int externSheetIndex) {
        return iBook.findSheetLastNameFromExternSheet(externSheetIndex);
    }

    public String getNameText(NamePtg namePtg) {
        return iBook.getNameRecord(namePtg.getIndex()).getNameText();
    }

    public EvaluationName getName(NamePtg namePtg) {
        int ix = namePtg.getIndex();
        return new Name(iBook.getNameRecord(ix), ix);
    }

    @SuppressWarnings("unused")
    public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
        HSSFCell cell = ((HSSFEvaluationCell) evalCell).getHSSFCell();
        if (false) {
            // re-parsing the formula text also works, but is a waste of time
            // It is useful from time to time to run all unit tests with this code
            // to make sure that all formulas POI can evaluate can also be parsed.
            try {
                return HSSFFormulaParser.parse(cell.getCellFormula(), uBook, FormulaType.CELL, uBook.getSheetIndex(cell.getSheet()));
            } catch (FormulaParseException e) {
                // Note - as of Bugzilla 48036 (svn r828244, r828247) POI is capable of evaluating
                // IntesectionPtg.  However it is still not capable of parsing it.
                // So FormulaEvalTestData.xls now contains a few formulas that produce errors here.
                logger.log(POILogger.ERROR, e.getMessage());
            }
        }
        FormulaRecordAggregate fra = (FormulaRecordAggregate) cell.getCellValueRecord();
        return fra.getFormulaTokens();
    }

    public UDFFinder getUDFFinder() {
        return uBook.getUDFFinder();
    }

    private static final class Name implements EvaluationName {

        private final NameRecord nameRecord;
        private final int index;

        public Name(NameRecord nameRecord, int index) {
            this.nameRecord = nameRecord;
            this.index = index;
        }

        public Ptg[] getNameDefinition() {
            return nameRecord.getNameDefinition();
        }

        public String getNameText() {
            return nameRecord.getNameText();
        }

        public boolean hasFormula() {
            return nameRecord.hasFormula();
        }

        public boolean isFunctionName() {
            return nameRecord.isFunctionName();
        }

        public boolean isRange() {
            return nameRecord.hasFormula(); // TODO - is this right?
        }

        public NamePtg createPtg() {
            return new NamePtg(index);
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
                lastSheetName = ((SheetRangeIdentifier) sheetIden).getLastSheetIdentifier().getName();
            }

            if (workbookName == null) {
                int firstSheetIndex = uBook.getSheetIndex(firstSheetName);
                int lastSheetIndex = uBook.getSheetIndex(lastSheetName);
                extIx = iBook.checkExternSheet(firstSheetIndex, lastSheetIndex);
            } else {
                extIx = iBook.getExternalSheetIndex(workbookName, firstSheetName, lastSheetName);
            }
        }
        return extIx;
    }

    public SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL97;
    }
}
