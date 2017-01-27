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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ScatterChartSeries;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * Tests for XSSFScatterChartData.
 */
public final class TestXSSFScatterChartData {

    private static final Object[][] plotData = {
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"},
            {  1,    2,   3,    4,    5,   6,    7,   8,    9,  10}
    };

    @Test
    public void testOneSeriePlot() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = new SheetBuilder(wb, plotData).build();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
        Chart chart = drawing.createChart(anchor);

        ChartAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        ChartAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

        ScatterChartData scatterChartData =
                chart.getChartDataFactory().createScatterChartData();

        ChartDataSource<String> xs = DataSources.fromStringCellRange(sheet, CellRangeAddress.valueOf("A1:J1"));
        ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sheet, CellRangeAddress.valueOf("A2:J2"));
        ScatterChartSeries series = scatterChartData.addSerie(xs, ys);

        assertNotNull(series);
        assertEquals(1, scatterChartData.getSeries().size());
        assertTrue(scatterChartData.getSeries().contains(series));

        chart.plot(scatterChartData, bottomAxis, leftAxis);
        wb.close();
    }
}
