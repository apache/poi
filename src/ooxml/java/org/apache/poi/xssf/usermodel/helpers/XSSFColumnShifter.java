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

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.helpers.ColumnShifter;
import org.apache.poi.util.Beta;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.*;

/**
 * Helper for shifting columns up or down
 */
// non-Javadoc: When possible, code should be implemented in the ColumnShifter abstract class to avoid duplication with
// {@link org.apache.poi.hssf.usermodel.helpers.HSSFColumnShifter}
@Beta
public final class XSSFColumnShifter extends ColumnShifter {
    private static final POILogger logger = POILogFactory.getLogger(XSSFColumnShifter.class);

    public XSSFColumnShifter(XSSFSheet sh) {
        super(sh);
    }

    /**
     * Updated named ranges
     */
    @Override
    public void updateNamedRanges(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateNamedRanges(sheet, formulaShifter);
    }

    /**
     * Update formulas.
     */
    @NotImplemented
    @Override
    public void updateFormulas(FormulaShifter formulaShifter) {
        throw new NotImplementedException("updateFormulas");
    }

    private void updateSheetFormulas(Sheet sh, FormulaShifter formulaShifter) {
        throw new NotImplementedException("updateSheetFormulas");
    }

    @Override
    public void updateConditionalFormatting(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateConditionalFormatting(sheet, formulaShifter);
    }
    
    /**
     * Shift the Hyperlink anchors (not the hyperlink text, even if the hyperlink
     * is of type LINK_DOCUMENT and refers to a cell that was shifted). Hyperlinks
     * do not track the content they point to.
     *
     * @param formulaShifter
     */
    @Override
    public void updateHyperlinks(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateHyperlinks(sheet, formulaShifter);
    }

}
