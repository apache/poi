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

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestNamedRange;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFName extends BaseTestNamedRange {

    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
    }

    //TODO combine testRepeatingRowsAndColums() for HSSF and XSSF
    public void testRepeatingRowsAndColums() {
        // First test that setting RR&C for same sheet more than once only creates a
        // single  Print_Titles built-in record
        XSSFWorkbook wb = getTestDataProvider().createWorkbook();
        wb.createSheet("First Sheet");

        wb.setRepeatingRowsAndColumns(0, -1, -1, -1, -1);

        // set repeating rows and columns twice for the first sheet
        for (int i = 0; i < 2; i++) {
            wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 3);
            //sheet.createFreezePane(0, 3);
        }
        assertEquals(1, wb.getNumberOfNames());
        XSSFName nr1 = wb.getNameAt(0);

        assertEquals(XSSFName.BUILTIN_PRINT_TITLE, nr1.getNameName());
        assertEquals("'First Sheet'!$A:$A,'First Sheet'!$1:$4", nr1.getRefersToFormula());

        //remove the columns part
        wb.setRepeatingRowsAndColumns(0, -1, -1, 0, 3);
        assertEquals("'First Sheet'!$1:$4", nr1.getRefersToFormula());

        //revert
        wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 3);

        //remove the rows part
        wb.setRepeatingRowsAndColumns(0, 0, 0, -1, -1);
        assertEquals("'First Sheet'!$A:$A", nr1.getRefersToFormula());

        //revert
        wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 3);

        // Save and re-open
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);

        assertEquals(1, nwb.getNumberOfNames());
        nr1 = nwb.getNameAt(0);

        assertEquals(XSSFName.BUILTIN_PRINT_TITLE, nr1.getNameName());
        assertEquals("'First Sheet'!$A:$A,'First Sheet'!$1:$4", nr1.getRefersToFormula());

        // check that setting RR&C on a second sheet causes a new Print_Titles built-in
        // name to be created
        nwb.createSheet("SecondSheet");
        nwb.setRepeatingRowsAndColumns(1, 1, 2, 0, 0);

        assertEquals(2, nwb.getNumberOfNames());
        XSSFName nr2 = nwb.getNameAt(1);

        assertEquals(XSSFName.BUILTIN_PRINT_TITLE, nr2.getNameName());
        assertEquals("SecondSheet!$B:$C,SecondSheet!$1:$1", nr2.getRefersToFormula());

        nwb.setRepeatingRowsAndColumns(1, -1, -1, -1, -1);
    }
}
