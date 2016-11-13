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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.formula.TestMissingWorkbook;
import org.apache.poi.xssf.XSSFTestDataSamples;

/**
 * XSSF Specific version of the Missing Workbooks test
 */
public final class TestMissingWorkbookOnXSSF extends TestMissingWorkbook {
    public TestMissingWorkbookOnXSSF() {
        super("52575_main.xlsx", "source_dummy.xlsx", "52575_source.xls");
    }
    
    @Override
    protected void setUp() throws Exception {
        mainWorkbook = XSSFTestDataSamples.openSampleWorkbook(MAIN_WORKBOOK_FILENAME);
        sourceWorkbook = HSSFTestDataSamples.openSampleWorkbook(SOURCE_WORKBOOK_FILENAME);
        
        assertNotNull(mainWorkbook);
        assertNotNull(sourceWorkbook);
    }
}
