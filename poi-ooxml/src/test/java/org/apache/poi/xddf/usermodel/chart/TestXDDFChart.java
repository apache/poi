/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xddf.usermodel.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class TestXDDFChart {
    @Test
    void testConstruct() {
        // minimal test to cause ooxml-lite to include all the classes in poi-ooxml-lite
        XDDFChart xddfChart = newXDDFChart();

        assertNotNull(xddfChart.getCTChartSpace());
        assertNotNull(xddfChart.getCTPlotArea());
    }

    @Test
    void testSetMajorUnit() {
        // minimal test to cause ooxml-lite to include all the classes in poi-ooxml-lite
        XDDFChart xddfChart = newXDDFChart();

        XDDFValueAxis xAxis = xddfChart.createValueAxis(AxisPosition.BOTTOM);
        XDDFValueAxis yAxis = xddfChart.createValueAxis(AxisPosition.LEFT);
        assertNotNull(xAxis);
        assertNotNull(yAxis);

        xAxis.setTitle("Seconds Into Run");
        final double xAxisMajorUnits = 300.0;
        xAxis.setMajorUnit(xAxisMajorUnits);
        assertEquals(xAxisMajorUnits, xAxis.getMajorUnit());
        final double yAxisMajorUnits = 100.0;
        yAxis.setMinorUnit(yAxisMajorUnits);
        assertEquals(yAxisMajorUnits, yAxis.getMinorUnit());

        xAxis.setOrientation(AxisOrientation.MAX_MIN);
        assertEquals(AxisOrientation.MAX_MIN, xAxis.getOrientation());
        yAxis.setOrientation(AxisOrientation.MIN_MAX);
        assertEquals(AxisOrientation.MIN_MAX, yAxis.getOrientation());

        xAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        assertEquals(AxisCrosses.AUTO_ZERO, xAxis.getCrosses());

        yAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
        assertEquals(AxisCrossBetween.BETWEEN, yAxis.getCrossBetween());
    }

    @Test
    void testSetExternalId() {
        XDDFChart xddfChart = newXDDFChart();
        CTChartSpace ctChartSpace = xddfChart.getCTChartSpace();

        xddfChart.setExternalId("rid1");
        assertEquals("rid1", ctChartSpace.getExternalData().getId());

        xddfChart.setExternalId("rid2");
        assertEquals("rid2", ctChartSpace.getExternalData().getId());
    }

    @Test
    public void test65016() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("65016.xlsx")) {
            XSSFSheet splitSheet = wb.getSheet("Splits");

            XDDFChart chart = newXDDFChart();
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.BOTTOM);

            // Use a category axis for the bottom axis.
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

            // starting row 1 to include description
            XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(splitSheet,
                    new CellRangeAddress(2, 100, 0, 0));
            XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(splitSheet,
                    new CellRangeAddress(2, 100, 1, 1));

            XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(xs, ys1);
            assertEquals(series.categoryData.getPointCount(), xs.getPointCount());

            chart.plot(data);

            File file = TempFile.createTempFile("chart20201220", ".xlsx");
            try {
                try (OutputStream out = new FileOutputStream(file)) {
                    wb.write(out);
                }
            } finally {
                assertTrue(!file.exists() || file.delete());
            }
        }
    }

    private XDDFChart newXDDFChart() {
        return new XDDFChart() {
            @Override
            protected POIXMLRelation getChartRelation() {
                return null;
            }

            @Override
            protected POIXMLRelation getChartWorkbookRelation() {
                return null;
            }

            @Override
            protected POIXMLFactory getChartFactory() {
                return null;
            }
        };
    }
}
