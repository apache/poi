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

import org.apache.poi.util.Internal;

/**
 * Abstracts a sheet for the purpose of formula evaluation.<br>
 * 
 * For POI internal use only
 * 
 * @author Josh Micich
 */
@Internal
public interface EvaluationSheet {

    /**
     * @return <code>null</code> if there is no cell at the specified coordinates
     */
    EvaluationCell getCell(int rowIndex, int columnIndex);
    
    /**
     * Propagated from {@link EvaluationWorkbook#clearAllCachedResultValues()} to clear locally cached data.
     * 
     * @see WorkbookEvaluator#clearAllCachedResultValues()
     * @see EvaluationWorkbook#clearAllCachedResultValues()
     * @since POI 3.15 beta 3
     */
    public void clearAllCachedResultValues();
    
    /**
     * @return last row index referenced on this sheet, for evaluation optimization
     * @since POI 4.0.0
     */
    public int getLastRowNum();
    
    /**
     * Used by SUBTOTAL and similar functions that have options to ignore hidden rows
     * @param rowIndex
     * @return true if the row is hidden, false if not
     * @since POI 4.1.0
     */
    public boolean isRowHidden(int rowIndex);
}
