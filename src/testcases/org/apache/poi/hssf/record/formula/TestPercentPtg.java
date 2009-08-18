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

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests for {@link PercentPtg}.
 *
 * @author Daniel Noll (daniel at nuix dot com dot au)
 */
public final class TestPercentPtg extends AbstractPtgTestCase {
    /**
     * Tests reading a file containing this ptg.
     */
    public void testReading() {
        HSSFWorkbook workbook = loadWorkbook("PercentPtg.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);

        assertEquals("Wrong numeric value for original number", 53000.0,
                     sheet.getRow(0).getCell(0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for percent formula result", 5300.0,
                     sheet.getRow(1).getCell(0).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for percent formula", "A1*10%",
                     sheet.getRow(1).getCell(0).getCellFormula());
    }
}
