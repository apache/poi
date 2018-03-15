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

package org.apache.poi.ss.formula.eval.forked;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationName;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Internal;

/**
 * Represents a workbook being used for forked evaluation. Most operations are delegated to the
 * shared master workbook, except those that potentially involve cell values that may have been
 * updated after a call to {@link #getOrCreateUpdatableCell(String, int, int)}.<br>
 * 
 * For POI internal use only
 */
@Internal
final class ForkedEvaluationWorkbook implements EvaluationWorkbook {

    private final EvaluationWorkbook _masterBook;
    private final Map<String, ForkedEvaluationSheet> _sharedSheetsByName;

    public ForkedEvaluationWorkbook(EvaluationWorkbook master) {
        _masterBook = master;
        _sharedSheetsByName = new HashMap<>();
    }

    public ForkedEvaluationCell getOrCreateUpdatableCell(String sheetName, int rowIndex,
            int columnIndex) {
        ForkedEvaluationSheet sheet = getSharedSheet(sheetName);
        return sheet.getOrCreateUpdatableCell(rowIndex, columnIndex);
    }

    public EvaluationCell getEvaluationCell(String sheetName, int rowIndex, int columnIndex) {
        ForkedEvaluationSheet sheet = getSharedSheet(sheetName);
        return sheet.getCell(rowIndex, columnIndex);
    }

    private ForkedEvaluationSheet getSharedSheet(String sheetName) {
        ForkedEvaluationSheet result = _sharedSheetsByName.get(sheetName);
        if (result == null) {
            result = new ForkedEvaluationSheet(_masterBook.getSheet(_masterBook
                    .getSheetIndex(sheetName)));
            _sharedSheetsByName.put(sheetName, result);
        }
        return result;
    }

    public void copyUpdatedCells(Workbook workbook) {
        String[] sheetNames = new String[_sharedSheetsByName.size()];
        _sharedSheetsByName.keySet().toArray(sheetNames);
        for (String sheetName : sheetNames) {
            ForkedEvaluationSheet sheet = _sharedSheetsByName.get(sheetName);
            sheet.copyUpdatedCells(workbook.getSheet(sheetName));
        }
    }

    @Override
    public int convertFromExternSheetIndex(int externSheetIndex) {
        return _masterBook.convertFromExternSheetIndex(externSheetIndex);
    }

    @Override
    public ExternalSheet getExternalSheet(int externSheetIndex) {
        return _masterBook.getExternalSheet(externSheetIndex);
    }
    @Override
    public ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber) {
        return _masterBook.getExternalSheet(firstSheetName, lastSheetName, externalWorkbookNumber);
    }

    @Override
    public Ptg[] getFormulaTokens(EvaluationCell cell) {
        if (cell instanceof ForkedEvaluationCell) {
            // doesn't happen yet because formulas cannot be modified from the master workbook
            throw new RuntimeException("Updated formulas not supported yet");
        }
        return _masterBook.getFormulaTokens(cell);
    }

    @Override
    public EvaluationName getName(NamePtg namePtg) {
        return _masterBook.getName(namePtg);
    }

    @Override
    public EvaluationName getName(String name, int sheetIndex){
        return _masterBook.getName(name, sheetIndex);
    }

    @Override
    public EvaluationSheet getSheet(int sheetIndex) {
        return getSharedSheet(getSheetName(sheetIndex));
    }
    
    @Override
    public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
       return _masterBook.getExternalName(externSheetIndex, externNameIndex);
    }
    @Override
    public ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber) {
           return _masterBook.getExternalName(nameName, sheetName, externalWorkbookNumber);
    }

    @Override
    public int getSheetIndex(EvaluationSheet sheet) {
        if (sheet instanceof ForkedEvaluationSheet) {
            ForkedEvaluationSheet mes = (ForkedEvaluationSheet) sheet;
            return mes.getSheetIndex(_masterBook);
        }
        return _masterBook.getSheetIndex(sheet);
    }

    @Override
    public int getSheetIndex(String sheetName) {
        return _masterBook.getSheetIndex(sheetName);
    }

    @Override
    public String getSheetName(int sheetIndex) {
        return _masterBook.getSheetName(sheetIndex);
    }

    @Override
    public String resolveNameXText(NameXPtg ptg) {
        return _masterBook.resolveNameXText(ptg);
    }

    @Override
    public UDFFinder getUDFFinder() {
        return _masterBook.getUDFFinder();
    }
    
    public SpreadsheetVersion getSpreadsheetVersion() {
        return _masterBook.getSpreadsheetVersion();
    }
    
    /* (non-Javadoc)
     * leave the map alone, if it needs resetting, reusing this class is probably a bad idea.
     * @see org.apache.poi.ss.formula.EvaluationSheet#clearAllCachedResultValues()
     * 
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _masterBook.clearAllCachedResultValues();
    }
}
