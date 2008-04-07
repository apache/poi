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

package org.apache.poi.hssf.record.formula;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
/**
 * Tests for functions from external workbooks (e.g. YEARFRAC).
 * 
 * 
 * @author Josh Micich
 */
public final class TestExternalFunctionFormulas extends TestCase {

    
    /**
     * tests <tt>NameXPtg.toFormulaString(Workbook)</tt> and logic in Workbook below that   
     */
    public void testReadFormulaContainingExternalFunction() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("externalFunctionExample.xls");
        
        String expectedFormula = "YEARFRAC(B1,C1)";
        HSSFSheet sht = wb.getSheetAt(0);
        String cellFormula = sht.getRow(0).getCell((short)0).getCellFormula();
        assertEquals(expectedFormula, cellFormula);
    }
    
}
