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

package org.apache.poi.xssf.streaming;

import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationSheet;
import org.apache.poi.util.Internal;

/**
 * SXSSF wrapper for a sheet under evaluation
 */
@Internal
final class SXSSFEvaluationSheet implements EvaluationSheet {
    private final SXSSFSheet _xs;
    private int _lastDefinedRow = -1;

    public SXSSFEvaluationSheet(SXSSFSheet sheet) {
        _xs = sheet;
        _lastDefinedRow = _xs.getLastRowNum();
    }

    public SXSSFSheet getSXSSFSheet() {
        return _xs;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.formula.EvaluationSheet#getlastRowNum()
     * @since POI 4.0.0
     */
    @Override
    public int getLastRowNum() {
        return _lastDefinedRow;
    }
    
    @Override
    public EvaluationCell getCell(int rowIndex, int columnIndex) {
        SXSSFRow row = _xs.getRow(rowIndex);
        if (row == null) {
            if (rowIndex <= _xs.getLastFlushedRowNum()) {
                throw new SXSSFFormulaEvaluator.RowFlushedException(rowIndex);
            }
            return null;
        }
        SXSSFCell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        return new SXSSFEvaluationCell(cell, this);
    }
    
    /* (non-JavaDoc), inherit JavaDoc from EvaluationSheet
     * @since POI 3.15 beta 3
     */
    @Override
    public void clearAllCachedResultValues() {
        _lastDefinedRow = _xs.getLastRowNum();
    }
}
