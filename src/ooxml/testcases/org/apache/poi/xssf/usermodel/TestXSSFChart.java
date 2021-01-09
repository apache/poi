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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;

public final class TestXSSFChart {
    @Test
    void testGetAccessors() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithThreeCharts.xlsx")) {
            XSSFSheet s1 = wb.getSheetAt(0);
            XSSFSheet s2 = wb.getSheetAt(1);
            XSSFSheet s3 = wb.getSheetAt(2);

            assertEquals(0, s1.getRelations().size());
            assertEquals(1, s2.getRelations().size());
            assertEquals(1, s3.getRelations().size());

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
        }
    }

    @Test
    void testGetCharts() throws Exception {
       try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("WithThreeCharts.xlsx")) {
           XSSFSheet s1 = wb.getSheetAt(0);
           XSSFSheet s2 = wb.getSheetAt(1);
           XSSFSheet s3 = wb.getSheetAt(2);

           assertEquals(0, s1.createDrawingPatriarch().getCharts().size());
           assertEquals(2, s2.createDrawingPatriarch().getCharts().size());
           assertEquals(1, s3.createDrawingPatriarch().getCharts().size());

           // Check the titles
           XSSFChart chart = s2.createDrawingPatriarch().getCharts().get(0);
           assertNull(chart.getTitleText());

           chart = s2.createDrawingPatriarch().getCharts().get(1);
           XSSFRichTextString title = chart.getTitleText();
           assertNotNull(title);
           assertEquals("Pie Chart Title Thingy", title.getString());

           chart = s3.createDrawingPatriarch().getCharts().get(0);
           title = chart.getTitleText();
           assertNotNull(title);
           assertEquals("Sheet 3 Chart with Title", title.getString());

           assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
       }
    }

    @Test
	void testAddChartsToNewWorkbook() throws Exception {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet s1 = wb.createSheet();
            XSSFDrawing d1 = s1.createDrawingPatriarch();
            XSSFClientAnchor a1 = new XSSFClientAnchor(0, 0, 0, 0, 1, 1, 10, 30);
            XSSFChart c1 = d1.createChart(a1);

            assertEquals(1, d1.getCharts().size());

            assertNotNull(c1.getGraphicFrame());
            assertNotNull(c1.getOrAddLegend());

            XSSFClientAnchor a2 = new XSSFClientAnchor(0, 0, 0, 0, 1, 11, 10, 60);
            XSSFChart c2 = d1.createChart(a2);
            assertNotNull(c2);
            assertEquals(2, d1.getCharts().size());

            assertNotNull(XSSFTestDataSamples.writeOutAndReadBack(wb));
        }
	}
}
