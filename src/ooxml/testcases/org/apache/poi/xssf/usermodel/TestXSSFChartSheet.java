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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.XSSFTestDataSamples;

public final class TestXSSFChartSheet extends TestCase {

    public void testXSSFFactory() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chart_sheet.xlsx");
        assertEquals(4, wb.getNumberOfSheets());

        //the third sheet is of type 'chartsheet'
        assertEquals("Chart1", wb.getSheetName(2));
        assertTrue(wb.getSheetAt(2) instanceof XSSFChartSheet);
        assertEquals("Chart1", wb.getSheetAt(2).getSheetName());

    }

    public void testGetAccessors() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chart_sheet.xlsx");
        XSSFChartSheet sheet = (XSSFChartSheet)wb.getSheetAt(2);

        for(Row row : sheet) {
            fail("Row iterator for chart sheets should return zero rows");
        }
        //access to a arbitrary row
        assertEquals(null, sheet.getRow(1));

        //some basic get* accessors
        assertEquals(0, sheet.getNumberOfComments());
        assertEquals(0, sheet.getNumHyperlinks());
        assertEquals(0, sheet.getNumMergedRegions());
        assertEquals(null, sheet.getActiveCell());
        assertEquals(true, sheet.getAutobreaks());
        assertEquals(null, sheet.getCellComment(0, 0));
        assertEquals(0, sheet.getColumnBreaks().length);
        assertEquals(true, sheet.getRowSumsBelow());
    }
}
