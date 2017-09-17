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

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.ScatterStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
    	XSSFWorkbook wb = new XSSFWorkbook();
    	XSSFSheet sheet = (XSSFSheet) new SheetBuilder(wb, plotData).build();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);
        XSSFChart chart = drawing.createChart(anchor);

        XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, CellRangeAddress.valueOf("A1:J1"));
        XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, CellRangeAddress.valueOf("A2:J2"));

        XDDFScatterChartData scatterChartData = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);
        XDDFChartData.Series series = scatterChartData.addSeries(xs, ys);

        assertEquals(ScatterStyle.LINE_MARKER, scatterChartData.getStyle());
        assertNotNull(series);
        assertEquals(1, scatterChartData.getSeries().size());
        assertTrue(scatterChartData.getSeries().contains(series));

        chart.plot(scatterChartData);
        wb.close();
    }
}
