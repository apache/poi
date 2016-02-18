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

import static org.junit.Assert.assertEquals;

import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Ignore;
import org.junit.Test;

public final class TestSXSSFBugs extends BaseTestBugzillaIssues {
    public TestSXSSFBugs() {
        super(SXSSFITestDataProvider.instance);
    }

    // override some tests which do not work for SXSSF
    @Override @Ignore("cloneSheet() not implemented") @Test public void bug18800() { /* cloneSheet() not implemented */ }
    @Override @Ignore("cloneSheet() not implemented") @Test public void bug22720() { /* cloneSheet() not implemented */ }
    @Override @Ignore("Evaluation is not fully supported") @Test public void bug47815() { /* Evaluation is not supported */ }
    @Override @Ignore("Evaluation is not fully supported") @Test public void test58113() { /* Evaluation is not supported */ }
    @Override @Ignore("Evaluation is not fully supported") @Test public void bug46729_testMaxFunctionArguments() { /* Evaluation is not supported */ }
    
    /**
     * Setting repeating rows and columns shouldn't break
     *  any print settings that were there before
     */
    @SuppressWarnings("deprecation")
    @Test
    public void bug49253() throws Exception {
        Workbook wb1 = new SXSSFWorkbook();
        Workbook wb2 = new SXSSFWorkbook();

        // No print settings before repeating
        Sheet s1 = wb1.createSheet(); 

        wb1.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);

        PrintSetup ps1 = s1.getPrintSetup();
        assertEquals(false, ps1.getValidSettings());
        assertEquals(false, ps1.getLandscape());


        // Had valid print settings before repeating
        Sheet s2 = wb2.createSheet();
        PrintSetup ps2 = s2.getPrintSetup();

        ps2.setLandscape(false);
        assertEquals(true, ps2.getValidSettings());
        assertEquals(false, ps2.getLandscape());

        wb2.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);

        ps2 = s2.getPrintSetup();
        assertEquals(true, ps2.getValidSettings());
        assertEquals(false, ps2.getLandscape());

        wb1.close();
        wb2.close();
    }
}
