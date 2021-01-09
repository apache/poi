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
package org.apache.poi.xddf.usermodel.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for bug 63153
 */
public class TestXDDFChartRemoveSeries {
    final File resultDir = new File("build/custom-reports-test");
    String procName = null;
    String fileName = null;
    XSSFWorkbook workbook = null;
    XSSFSheet sheet = null;
    XDDFScatterChartData chartData = null;
    XDDFChart chart = null;
    final int MAX_NUM_SERIES = 1;

    public TestXDDFChartRemoveSeries() {
        resultDir.mkdirs();
    }

    /**
     * This creates a workbook with one worksheet, which contains a single
     * scatter chart.
     */
    @BeforeEach
    public void setup() {
        final boolean bDebug = false;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();

        final XSSFDrawing xssfDrawing = sheet.createDrawingPatriarch();
        final XSSFClientAnchor anchor = xssfDrawing.createAnchor(0, 0, 0, 0, 1, 5, 20, 20);
        if (bDebug) {
            return;
        }
        chart = xssfDrawing.createChart(anchor);
        final XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        final XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);

        // Initialize data data sources

        final Double dX[] = new Double[5];
        final Double dY1[] = new Double[5];
        final Double dY2[] = new Double[5];

        for (int n = 0; n < 5; ++n) {
            dX[n] = (double) n;
            dY1[n] = 2.0 * n;
            dY2[n] = (double) (n * n);

        }

        final XDDFNumericalDataSource<Double> xData = XDDFDataSourcesFactory.fromArray(dX, null);
        final XDDFNumericalDataSource<Double> yData1 = XDDFDataSourcesFactory.fromArray(dY1, null);
        final XDDFNumericalDataSource<Double> yData2 = XDDFDataSourcesFactory.fromArray(dY2, null);

        // Create the chartdata

        chartData = (XDDFScatterChartData) chart.createData(ChartTypes.SCATTER, bottomAxis, leftAxis);

        // Add the series

        chartData.addSeries(xData, yData1);
        chartData.addSeries(xData, yData2);
    }

    /**
     * This method writes the workbook to resultDir/fileName.
     */
    @AfterEach
    public void cleanup() {
        if (workbook == null) {
            System.out.println(String.format(Locale.ROOT, "%s: workbook==null", procName));
            return;
        }

        if (fileName == null) {
            System.out.println(String.format(Locale.ROOT, "%s: fileName==null", procName));
            return;
        }

        // Finish up
        chart.plot(chartData);
        final int index = workbook.getSheetIndex(sheet);
        workbook.setSelectedTab(index);
        workbook.setActiveSheet(index);
        workbook.setFirstVisibleTab(index);

        final File file = new File(resultDir, fileName);
        try (OutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            System.out.println(
                    String.format(Locale.ROOT, "%s: test file written to %s", procName, file.getAbsolutePath()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the first series by calling chartData.getSeries().remove(0).
     * <p>
     * This used to corrupt the workbook but the returned <code>List</code> is unmodifiable.
     */
    @Test
    void testRemoveSeries0() {
        procName = "testRemoveSeries0";
        fileName = procName + ".xlsx";

        try {
            chartData.getSeries().remove(0);
        } catch (UnsupportedOperationException uoe) {
            assertEquals(2, chartData.getSeriesCount());
        }
    }

    /**
     * Remove the first series by calling chartData.removeSeries(0).
     * <p>
     * This will not corrupt the workbook.
     */
    @Test
    void testBugFixRemoveSeries0() {
        procName = "testBugFixRemoveSeries0";
        fileName = procName + ".xlsx";

        chartData.removeSeries(0);
        assertEquals(1, chartData.getSeriesCount());
    }

    /**
     * Remove the second series by calling chartData.removeSeries(1).
     * <p>
     * This will not corrupt the workbook.
     */
    @Test
    void testBugFixRemoveSeries1() {
        procName = "testBugFixRemoveSeries1";
        fileName = procName + ".xlsx";

        chartData.removeSeries(1);
        assertEquals(1, chartData.getSeriesCount());
    }

    /**
     * Do not remove any series from the chart.
     */
    @Test
    void testDontRemoveSeries() {
        procName = "testDontRemoveSeries";
        fileName = procName + ".xlsx";
    }

}