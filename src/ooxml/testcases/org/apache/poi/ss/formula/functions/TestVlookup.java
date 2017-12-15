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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Test the VLOOKUP function
 */
public class TestVlookup extends TestCase {

    @Test
    public void testFullColumnAreaRef61841() {
        final Workbook wb = XSSFTestDataSamples.openSampleWorkbook("VLookupFullColumn.xlsx");
        FormulaEvaluator feval = wb.getCreationHelper().createFormulaEvaluator();
        feval.evaluateAll();
        assertEquals("Wrong lookup value",  "Value1", feval.evaluate(wb.getSheetAt(0).getRow(3).getCell(1)).getStringValue());
        assertEquals("Lookup should return #N/A", CellType.ERROR, feval.evaluate(wb.getSheetAt(0).getRow(4).getCell(1)).getCellType());
    }

}
