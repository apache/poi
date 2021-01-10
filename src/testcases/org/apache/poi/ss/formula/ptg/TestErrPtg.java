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

package org.apache.poi.ss.formula.ptg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ErrPtg}.
 */
final class TestErrPtg extends BaseTestPtg {
    /**
     * Tests reading a file containing this ptg.
     */
    @Test
    void testReading() throws IOException {
        try (HSSFWorkbook workbook = loadWorkbook("ErrPtg.xls")) {
            HSSFCell cell = workbook.getSheetAt(0).getRow(3).getCell(0);
            assertEquals(4.0, cell.getNumericCellValue(), 0.0, "Wrong cell value");
            assertEquals("ERROR.TYPE(#REF!)", cell.getCellFormula(), "Wrong cell formula");
        }
    }
}
