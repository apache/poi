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

package org.apache.poi.ss.formula.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

/**
 * Test cases for RANK()
 */
final class TestRank {

    @Test
    void testFromFile() {

        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("rank.xls");
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        HSSFSheet example1 = wb.getSheet("Example 1");
        HSSFCell ex1cell1 = example1.getRow(7).getCell(0);
        assertEquals(3.0, fe.evaluate(ex1cell1).getNumberValue(), 0);
        HSSFCell ex1cell2 = example1.getRow(8).getCell(0);
        assertEquals(5.0, fe.evaluate(ex1cell2).getNumberValue(), 0);

        HSSFSheet example2 = wb.getSheet("Example 2");
        for(int rownum = 1; rownum<= 10; rownum ++){
            HSSFCell cell = example2.getRow(rownum).getCell(2);
            double cachedResult = cell.getNumericCellValue(); //cached formula result
            assertEquals(cachedResult, fe.evaluate(cell).getNumberValue(), 0);
        }

        HSSFSheet example3 = wb.getSheet("Example 3");
        for(int rownum = 1; rownum<= 10; rownum ++){
            HSSFCell cellD = example3.getRow(rownum).getCell(3);
            double cachedResultD = cellD.getNumericCellValue(); //cached formula result
            assertEquals(cachedResultD, fe.evaluate(cellD).getNumberValue(), 0, new CellReference(cellD).formatAsString());

            HSSFCell cellE = example3.getRow(rownum).getCell(4);
            double cachedResultE = cellE.getNumericCellValue(); //cached formula result
            assertEquals(cachedResultE, fe.evaluate(cellE).getNumberValue(), 0, new CellReference(cellE).formatAsString());

            HSSFCell cellF = example3.getRow(rownum).getCell(5);
            double cachedResultF = cellF.getNumericCellValue(); //cached formula result
            assertEquals(cachedResultF, fe.evaluate(cellF).getNumberValue(), 0, new CellReference(cellF).formatAsString());
        }
    }
}
