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

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * Helper for shifting rows up or down
 */
// non-Javadoc: When possible, code should be implemented in the RowShifter abstract class to avoid duplication with
// {@link org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter}
public final class XSSFRowShifter extends RowShifter {
    private static final POILogger logger = POILogFactory.getLogger(XSSFRowShifter.class);

    public XSSFRowShifter(XSSFSheet sh) {
        super(sh);
    }
    @Override
    public void updateNamedRanges(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateNamedRanges(sheet, formulaShifter);
    }

    @Override
    public void updateFormulas(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateFormulas(sheet, formulaShifter);
    }
    
    /**
     * Update the formulas in specified row using the formula shifting policy specified by shifter
     *
     * @param row the row to update the formulas on
     * @param formulaShifter the formula shifting policy
     */
    @Internal(since="3.15 beta 2")
    public void updateRowFormulas(XSSFRow row, FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateRowFormulas(row, formulaShifter);
    }

    @Override
    public void updateConditionalFormatting(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateConditionalFormatting(sheet, formulaShifter);
    }

    @Override
    public void updateHyperlinks(FormulaShifter formulaShifter) {
        XSSFRowColShifter.updateHyperlinks(sheet, formulaShifter);
    }
}
