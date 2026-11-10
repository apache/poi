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

package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.usermodel.BaseTestFormulaEvaluatorCacheNotification;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.junit.jupiter.api.Test;

/**
 * The cache notification tests, run against a workbook whose fill-down formulas are shared
 * formulas.
 *
 * @see XSSFSharedFormulaFixture
 */
final class TestXSSFFormulaEvaluatorCacheNotificationSharedFormulas extends BaseTestFormulaEvaluatorCacheNotification {

    TestXSSFFormulaEvaluatorCacheNotificationSharedFormulas() {
        super(XSSFITestDataProvider.instance);
    }

    @Override
    protected Workbook finishWorkbook(Workbook built) {
        return XSSFSharedFormulaFixture.shareFillDownFormulasAndReload(built);
    }

    @Test
    void fixtureReallyUsesSharedFormulas() {
        XSSFSharedFormulaFixture.assertSharedFormulas(wb);
        // dependents reconstruct their formula text from the master
        assertEquals("VLOOKUP(B3,Prices!$A$2:$B$4,2,FALSE)", cell("D3").getCellFormula());
        assertEquals("VLOOKUP(B4,Prices!$A$2:$B$4,2,FALSE)", cell("D4").getCellFormula());
        assertEquals("SUM(C2:C4)", cell("C6").getCellFormula());
    }
}
