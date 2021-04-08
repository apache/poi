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

package org.apache.poi.xssf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcCell;

public final class TestCalculationChain {

    @Test
    void test46535() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("46535.xlsx")) {

            CalculationChain chain = wb.getCalculationChain();
            //the bean holding the reference to the formula to be deleted
            CTCalcCell c = chain.getCTCalcChain().getCArray(0);
            int cnt = chain.getCTCalcChain().sizeOfCArray();
            assertEquals(10, c.getI());
            assertEquals("E1", c.getR());

            XSSFSheet sheet = wb.getSheet("Test");
            XSSFCell cell = sheet.getRow(0).getCell(4);

            assertEquals(CellType.FORMULA, cell.getCellType());
            cell.setCellFormula(null);

            //the count of items is less by one
            c = chain.getCTCalcChain().getCArray(0);
            int cnt2 = chain.getCTCalcChain().sizeOfCArray();
            assertEquals(cnt - 1, cnt2);
            //the first item in the calculation chain is the former second one
            assertEquals(10, c.getI());
            assertEquals("C1", c.getR());

            assertEquals(CellType.STRING, cell.getCellType());
            cell.setCellValue("ABC");
            assertEquals(CellType.STRING, cell.getCellType());
        }
    }
}
