/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.tests.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 *
 */
class TestSumifsXSSF {

    /**
     * handle null cell predicate
     */
    @Test
    void testBug60858() throws IOException {
        try (Workbook wb = XSSFTestDataSamples.openSampleWorkbook("bug60858.xlsx")) {
            FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = wb.getSheetAt(0);
            Cell cell = sheet.getRow(1).getCell(5);
            fe.evaluate(cell);
            assertEquals(0.0, cell.getNumericCellValue(), 0.0000000000000001);
        }
    }

}
