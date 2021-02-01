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

package org.apache.poi.ss;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Encapsulates a provider of test data for common HSSF / XSSF tests.
 */
public interface ITestDataProvider {
    /**
     * Provides HSSF / XSSF specific way for re-serialising a workbook
     *
     * @param wb the workbook to re-serialize
     * @return the re-serialized workbook
     */
    Workbook writeOutAndReadBack(Workbook wb);

    /**
     * Provides way of loading HSSF / XSSF sample workbooks
     *
     * @param sampleFileName the file name to load
     * @return an instance of Workbook loaded from the supplied file name
     */
    Workbook openSampleWorkbook(String sampleFileName);

    /**
     * Provides way of creating HSSF / XSSF workbooks
     * @return an instance of Workbook
     */
    Workbook createWorkbook();
    
    
    //************ SXSSF-specific methods ***************//
    /**
     * Provides way of creating a SXSSFWorkbook with a specific row access window size.
     * Equivalent to createWorkbook on others.
     * @return an instance of Workbook
     */
    Workbook createWorkbook(int rowAccessWindowSize);
    
    /**
     * Only matters for SXSSF - enables tracking of the column
     *  widths so that autosizing can work. No-op on others.
     */
    void trackAllColumnsForAutosizing(Sheet sheet);
    //************ End SXSSF-specific methods ***************//

    /**
     * Creates the corresponding {@link FormulaEvaluator} for the
     * type of Workbook handled by this Provider. 
     *
     * @param wb The workbook to base the formula evaluator on.
     * @return A new instance of a matching type of formula evaluator. 
     */
    FormulaEvaluator createFormulaEvaluator(Workbook wb);

    /**
     * Opens a sample file from the standard HSSF test data directory
     *
     * @return an open <tt>InputStream</tt> for the specified sample file
     */
    byte[] getTestDataFileContent(String fileName);

    SpreadsheetVersion getSpreadsheetVersion();

    /**
     * @return "xls" or "xlsx"
     */
    String getStandardFileNameExtension();
}
