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
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

/**
 * Abstracts a workbook for the purpose of formula parsing.<br>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public interface FormulaParsingWorkbook {
    /**
     *  named range name matching is case insensitive
     */
    EvaluationName getName(String name, int sheetIndex);
    
    /**
     * Return the underlying workbook
     */
    Name createName();

    /**
     * XSSF Only - gets a table that exists in the worksheet
     */
    Table getTable(String name);
    
    /**
     * Return an external name (named range, function, user-defined function) Ptg
     */
    Ptg getNameXPtg(String name, SheetIdentifier sheet);
    
    /**
     * Produce the appropriate Ptg for a 3d cell reference
     */
    Ptg get3DReferencePtg(CellReference cell, SheetIdentifier sheet);

    /**
     * Produce the appropriate Ptg for a 3d area reference
     */
    Ptg get3DReferencePtg(AreaReference area, SheetIdentifier sheet);

    /**
     * gets the externSheet index for a sheet from this workbook
     */
    int getExternalSheetIndex(String sheetName);
    /**
     * gets the externSheet index for a sheet from an external workbook
     * @param workbookName e.g. "Budget.xls"
     * @param sheetName a name of a sheet in that workbook
     */
    int getExternalSheetIndex(String workbookName, String sheetName);

    /**
     * Returns an enum holding spreadhseet properties specific to an Excel version (
     * max column and row numbers, max arguments to a function, etc.)
     */
    SpreadsheetVersion getSpreadsheetVersion();

}
