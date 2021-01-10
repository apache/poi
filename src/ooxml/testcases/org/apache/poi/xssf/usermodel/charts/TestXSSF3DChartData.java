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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetBuilder;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFArea3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFBar3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLine3DChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Tests for XSSF Area3d Charts
 */
class TestXSSF3DChartData {

    private static final Object[][] plotData = {
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"},
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
    };

    @Test
    void testArea3D() throws IOException {
        // This test currently doesn't produce a valid area 3d chart and is only used to test accessors
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = (XSSFSheet) new SheetBuilder(wb, plotData).build();
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 1, 1, 10, 30);

            Map<ChartTypes, Consumer<XDDFChartData>> map = new HashMap<>();
            map.put(ChartTypes.AREA3D, this::handleArea3D);
            map.put(ChartTypes.BAR3D, this::handleBar3D);
            map.put(ChartTypes.LINE3D, this::handleLine3D);

            for (Map.Entry<ChartTypes, Consumer<XDDFChartData>> me : map.entrySet()) {

                XSSFChart chart = drawing.createChart(anchor);

                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

                XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, CellRangeAddress.valueOf("A1:J1"));
                XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, CellRangeAddress.valueOf("A2:J2"));

                XDDFChartData data = chart.createData(me.getKey(), bottomAxis, leftAxis);
                XDDFChartData.Series series = data.addSeries(xs, ys);

                assertNotNull(series);
                assertEquals(1, data.getSeriesCount());
                assertEquals(series, data.getSeries(0));
                chart.plot(data);

                me.getValue().accept(data);
            }
        }
    }

    private void handleArea3D(XDDFChartData data) {
        XDDFArea3DChartData xArea3d = (XDDFArea3DChartData)data;
        xArea3d.setGapDepth(10);
        assertEquals(10, (int)xArea3d.getGapDepth());
    }

    private void handleBar3D(XDDFChartData data) {
        XDDFBar3DChartData xBar3d = (XDDFBar3DChartData) data;
        xBar3d.setGapDepth(10);
        assertEquals(10, (int)xBar3d.getGapDepth());
        xBar3d.setGapWidth(10);
        assertEquals(10, (int)xBar3d.getGapWidth());
    }

    private void handleLine3D(XDDFChartData data) {
        XDDFLine3DChartData xLine3d = (XDDFLine3DChartData) data;
        xLine3d.setGapDepth(10);
        assertEquals(10, (int)xLine3d.getGapDepth());
    }
}
