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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.util.Internal;

/**
 * Abstracts a workbook for the purpose of formula evaluation.<br>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
@Internal
public interface EvaluationWorkbook {
    String getSheetName(int sheetIndex);
    /**
     * @return -1 if the specified sheet is from a different book
     */
    int getSheetIndex(EvaluationSheet sheet);
    /**
     * Finds a sheet index by case insensitive name.
     * @return the index of the sheet matching the specified name.  -1 if not found
     */
    int getSheetIndex(String sheetName);

    EvaluationSheet getSheet(int sheetIndex);

    /**
     * HSSF Only - fetch the external-style sheet details
     * <p>Return will have no workbook set if it's actually in our own workbook</p>
     */
    ExternalSheet getExternalSheet(int externSheetIndex);
    /**
     * XSSF Only - fetch the external-style sheet details
     * <p>Return will have no workbook set if it's actually in our own workbook</p>
     */
    ExternalSheet getExternalSheet(String firstSheetName, String lastSheetName, int externalWorkbookNumber);
    /**
     * HSSF Only - convert an external sheet index to an internal sheet index,
     *  for an external-style reference to one of this workbook's own sheets 
     */
    int convertFromExternSheetIndex(int externSheetIndex);

    /**
     * HSSF Only - fetch the external-style name details
     */
    ExternalName getExternalName(int externSheetIndex, int externNameIndex);
    /**
     * XSSF Only - fetch the external-style name details
     */
    ExternalName getExternalName(String nameName, String sheetName, int externalWorkbookNumber);
    
    EvaluationName getName(NamePtg namePtg);
    EvaluationName getName(String name, int sheetIndex);
    String resolveNameXText(NameXPtg ptg);
    Ptg[] getFormulaTokens(EvaluationCell cell);
    UDFFinder getUDFFinder();
    SpreadsheetVersion getSpreadsheetVersion();
    
    /**
     * Propagated from {@link WorkbookEvaluator#clearAllCachedResultValues()} to clear locally cached data.
     * Implementations must call the same method on all referenced {@link EvaluationSheet} instances, as well as clearing local caches.
     * @see WorkbookEvaluator#clearAllCachedResultValues()
     * 
     * @since POI 3.15 beta 3
     */
    public void clearAllCachedResultValues();

    class ExternalSheet {
        private final String _workbookName;
        private final String _sheetName;

        public ExternalSheet(String workbookName, String sheetName) {
            _workbookName = workbookName;
            _sheetName = sheetName;
        }
        public String getWorkbookName() {
            return _workbookName;
        }
        public String getSheetName() {
            return _sheetName;
        }
    }
    class ExternalSheetRange extends ExternalSheet {
        private final String _lastSheetName;
        public ExternalSheetRange(String workbookName, String firstSheetName, String lastSheetName) {
            super(workbookName, firstSheetName);
            this._lastSheetName = lastSheetName;
        }
        
        public String getFirstSheetName() {
            return getSheetName();
        }
        public String getLastSheetName() {
            return _lastSheetName;
        }
    }
    class ExternalName {
        private final String _nameName;
        private final int _nameNumber;
        private final int _ix;

        public ExternalName(String nameName, int nameNumber, int ix) {
            _nameName = nameName;
            _nameNumber = nameNumber;
            _ix = ix;
        }
        public String getName() {
            return _nameName;
        }
        public int getNumber() {
            return _nameNumber;
        }
        public int getIx() {
            return _ix;
        }
    }
}
