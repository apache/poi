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

package org.apache.poi.hssf.usermodel.helpers;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.helpers.RowShifter;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Helper for shifting rows up or down
 */
// non-Javadoc: When possible, code should be implemented in the RowShifter abstract class to avoid duplication with
// {@link org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter}
public final class HSSFRowShifter extends RowShifter {
    private static final POILogger logger = POILogFactory.getLogger(HSSFRowShifter.class);

    public HSSFRowShifter(HSSFSheet sh) {
        super(sh);
    }

    @Override
    @NotImplemented
    public void updateNamedRanges(FormulaShifter formulaShifter) {
        throw new NotImplementedException("HSSFRowShifter.updateNamedRanges");
    }

    @Override
    @NotImplemented
    public void updateFormulas(FormulaShifter formulaShifter) {
        throw new NotImplementedException("updateFormulas");
    }

    @Override
    @NotImplemented
    public void updateConditionalFormatting(FormulaShifter formulaShifter) {
        throw new NotImplementedException("updateConditionalFormatting");
    }

    @Override
    @NotImplemented
    public void updateHyperlinks(FormulaShifter formulaShifter) {
        throw new NotImplementedException("updateHyperlinks");
    }

}
