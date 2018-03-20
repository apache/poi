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

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTChartsheet;

import static org.junit.Assert.*;

public final class TestXSSFChartSheet {

    @Test
    public void testXSSFFactory() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chart_sheet.xlsx");
        assertEquals(4, wb.getNumberOfSheets());

        //the third sheet is of type 'chartsheet'
        assertEquals("Chart1", wb.getSheetName(2));
        assertTrue(wb.getSheetAt(2) instanceof XSSFChartSheet);
        assertEquals("Chart1", wb.getSheetAt(2).getSheetName());

        final CTChartsheet ctChartsheet = ((XSSFChartSheet) wb.getSheetAt(2)).getCTChartsheet();
        assertNotNull(ctChartsheet);
    }

    @Test
    public void testGetAccessors() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chart_sheet.xlsx");
        XSSFChartSheet sheet = (XSSFChartSheet)wb.getSheetAt(2);

        assertFalse("Row iterator for charts sheets should return zero rows",
                sheet.iterator().hasNext());

        //access to a arbitrary row
        assertNull(sheet.getRow(1));

        //some basic get* accessors
        assertEquals(0, sheet.getNumberOfComments());
        assertEquals(0, sheet.getNumHyperlinks());
        assertEquals(0, sheet.getNumMergedRegions());
        assertNull(sheet.getActiveCell());
        assertTrue(sheet.getAutobreaks());
        assertNull(sheet.getCellComment(new CellAddress(0, 0)));
        assertEquals(0, sheet.getColumnBreaks().length);
        assertTrue(sheet.getRowSumsBelow());
        assertNotNull(sheet.createDrawingPatriarch());
        assertNotNull(sheet.getDrawingPatriarch());
        assertNotNull(sheet.getCTChartsheet());
    }
    
    @Test
    public void testGetCharts() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("chart_sheet.xlsx");
       
       XSSFSheet ns = wb.getSheetAt(0);
       XSSFChartSheet cs = (XSSFChartSheet)wb.getSheetAt(2);
       
       assertEquals(0, ns.createDrawingPatriarch().getCharts().size());
       assertEquals(1, cs.createDrawingPatriarch().getCharts().size());
       
       XSSFChart chart = cs.createDrawingPatriarch().getCharts().get(0);
       assertNull(chart.getTitleText());
    }
}
