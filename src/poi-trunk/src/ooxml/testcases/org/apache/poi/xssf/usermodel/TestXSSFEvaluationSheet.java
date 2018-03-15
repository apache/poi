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

import org.junit.Test;

import static org.junit.Assert.*;

public class TestXSSFEvaluationSheet {

    @Test
    public void test() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("test");
        XSSFRow row = sheet.createRow(0);
        row.createCell(0);
        XSSFEvaluationSheet evalsheet = new XSSFEvaluationSheet(sheet);

        assertNotNull("Cell 0,0 is found", evalsheet.getCell(0, 0));
        assertNull("Cell 0,1 is not found", evalsheet.getCell(0, 1));
        assertNull("Cell 1,0 is not found", evalsheet.getCell(1, 0));

        // now add Cell 0,1
        row.createCell(1);

        assertNotNull("Cell 0,0 is found", evalsheet.getCell(0, 0));
        assertNotNull("Cell 0,1 is now also found", evalsheet.getCell(0, 1));
        assertNull("Cell 1,0 is not found", evalsheet.getCell(1, 0));

        // after clearing all values it also works
        row.createCell(2);
        evalsheet.clearAllCachedResultValues();

        assertNotNull("Cell 0,0 is found", evalsheet.getCell(0, 0));
        assertNotNull("Cell 0,2 is now also found", evalsheet.getCell(0, 2));
        assertNull("Cell 1,0 is not found", evalsheet.getCell(1, 0));

        // other things
        assertEquals(sheet, evalsheet.getXSSFSheet());
    }
}