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

package org.apache.poi.xssf.usermodel.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests ChartLegend
 */
public final class TestXDDFChartLegend {
    @Test
	void testLegendPositionAccessMethods() throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet();
		XSSFDrawing drawing = sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		XSSFChart chart = drawing.createChart(anchor);
		XDDFChartLegend legend = chart.getOrAddLegend();

		legend.setPosition(LegendPosition.TOP_RIGHT);
		assertEquals(LegendPosition.TOP_RIGHT, legend.getPosition());

		wb.close();
	}

    @Test
    void test_setOverlay_defaultChartLegend_expectOverlayInitialValueSetToFalse() throws IOException {
        // Arrange
    	XSSFWorkbook wb = new XSSFWorkbook();
    	XSSFSheet sheet = wb.createSheet();
    	XSSFDrawing drawing = sheet.createDrawingPatriarch();
    	XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
    	XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();

        // Act

        // Assert
        assertFalse(legend.isOverlay());

        wb.close();
    }

    @Test
    void test_setOverlay_chartLegendSetToTrue_expectOverlayInitialValueSetToTrue() throws IOException {
        // Arrange
    	XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
		XSSFDrawing drawing = sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		XSSFChart chart = drawing.createChart(anchor);
		XDDFChartLegend legend = chart.getOrAddLegend();

        // Act
		legend.setOverlay(true);

        // Assert
        assertTrue(legend.isOverlay());

        wb.close();
	}
}
