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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ss.usermodel.BaseTestArrayFormulas;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;

/**
 * Test array formulas in HSSF
 *
 * @author Yegor Kozlov
 */
public final class TestHSSFArrayFormulas extends BaseTestArrayFormulas {

    @Override
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }

    public void testHSSFSetArrayFormula_singleCell() {
        HSSFWorkbook workbook = getTestDataProvider().createWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        CellRangeAddress range = new CellRangeAddress(2, 2, 2, 2);
        HSSFCell[] cells = sheet.setArrayFormula("SUM(C11:C12*D11:D12)", range);
        assertEquals(1, cells.length);

        // sheet.setArrayFormula creates rows and cells for the designated range
        assertNotNull(sheet.getRow(2));
        HSSFCell cell = sheet.getRow(2).getCell(2);
        assertNotNull(cell);

        assertTrue(cell.isPartOfArrayFormulaGroup());
        //retrieve the range and check it is the same
        assertEquals(range.formatAsString(), cell.getArrayFormulaRange().formatAsString());

        FormulaRecordAggregate agg = (FormulaRecordAggregate)cell.getCellValueRecord();
        assertEquals(range.formatAsString(), agg.getArrayFormulaRange().formatAsString());
        assertTrue(agg.isPartOfArrayFormula());

    }

}