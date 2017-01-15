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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * Tests ChartLegend
 */
public final class TestXSSFChartLegend {
    @Test
	public void testLegendPositionAccessMethods() throws IOException {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();

		legend.setPosition(LegendPosition.TOP_RIGHT);
		assertEquals(LegendPosition.TOP_RIGHT, legend.getPosition());
		
		wb.close();
	}

    @Test
    public void test_setOverlay_defaultChartLegend_expectOverlayInitialValueSetToFalse() throws IOException {
        // Arrange
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
        Chart chart = drawing.createChart(anchor);
        ChartLegend legend = chart.getOrCreateLegend();

        // Act

        // Assert
        assertFalse(legend.isOverlay());

        wb.close();
    }

    @Test
    public void test_setOverlay_chartLegendSetToTrue_expectOverlayInitialValueSetToTrue() throws IOException {
        // Arrange
        Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
		Chart chart = drawing.createChart(anchor);
		ChartLegend legend = chart.getOrCreateLegend();

        // Act
		legend.setOverlay(true);

        // Assert
        assertTrue(legend.isOverlay());

        wb.close();
	}
}
