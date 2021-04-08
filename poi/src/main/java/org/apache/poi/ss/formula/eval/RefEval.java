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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.SheetRange;
import org.apache.poi.ss.usermodel.CellType;

/**
 * RefEval is the super interface for Ref2D and Ref3DEval. Basically a RefEval
 * impl should contain reference to the original ReferencePtg or Ref3DPtg as
 * well as the final "value" resulting from the evaluation of the cell
 * reference. Thus if the Cell has type {@link CellType#NUMERIC}, the contained
 * value object should be of type NumberEval; if cell type is {@link CellType#STRING},
 * contained value object should be of type StringEval
 */
public interface RefEval extends ValueEval, SheetRange {
    /**
     * @return the evaluated value of the cell referred to by this RefEval on the given sheet
     */
    ValueEval getInnerValueEval(int sheetIndex);

    /**
     * returns the zero based column index.
     */
    int getColumn();

    /**
     * returns the zero based row index.
     */
    int getRow();
    
    /**
     * returns the first sheet index this applies to
     */
    int getFirstSheetIndex();

    /**
     * returns the last sheet index this applies to, which
     *  will be the same as the first for a 2D and many 3D references
     */
    int getLastSheetIndex();
    
    /**
     * returns the number of sheets this applies to
     */
    int getNumberOfSheets();

    /**
     * Creates an {@link AreaEval} offset by a relative amount from this RefEval
     */
    AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx);
}
