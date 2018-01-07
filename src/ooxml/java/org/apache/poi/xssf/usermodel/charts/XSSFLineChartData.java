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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.LineChartSeries;
import org.apache.poi.util.Removal;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;

/**
 * Holds data for a XSSF Line Chart
 *
 * @deprecated use {@link XDDFLineChartData} instead
 */
@Deprecated
@Removal(version="4.2")
public class XSSFLineChartData implements LineChartData {

    /**
     * List of all data series.
     */
    private List<Series> series;

    public XSSFLineChartData() {
        series = new ArrayList<Series>();
    }

    static class Series extends AbstractXSSFChartSeries implements LineChartSeries {
        private int id;
        private int order;
        private ChartDataSource<?> categories;
        private ChartDataSource<? extends Number> values;

        protected Series(int id, int order,
                        ChartDataSource<?> categories,
                        ChartDataSource<? extends Number> values) {
            this.id = id;
            this.order = order;
            this.categories = categories;
            this.values = values;
        }

        @Override
        public ChartDataSource<?> getCategoryAxisData() {
            return categories;
        }

        @Override
        public ChartDataSource<? extends Number> getValues() {
            return values;
        }

        protected void addToChart(CTLineChart ctLineChart) {
            CTLineSer ctLineSer = ctLineChart.addNewSer();
            ctLineSer.addNewIdx().setVal(id);
            ctLineSer.addNewOrder().setVal(order);

            // No marker symbol on the chart line.
            ctLineSer.addNewMarker().addNewSymbol().setVal(STMarkerStyle.NONE);

            CTAxDataSource catDS = ctLineSer.addNewCat();
            XSSFChartUtil.buildAxDataSource(catDS, categories);
            CTNumDataSource valueDS = ctLineSer.addNewVal();
            XSSFChartUtil.buildNumDataSource(valueDS, values);

            if (isTitleSet()) {
                ctLineSer.setTx(getCTSerTx());
            }
        }
    }

    @Override
    public LineChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values) {
        if (!values.isNumeric()) {
            throw new IllegalArgumentException("Value data source must be numeric.");
        }
        int numOfSeries = series.size();
        Series newSeries = new Series(numOfSeries, numOfSeries, categoryAxisData, values);
        series.add(newSeries);
        return newSeries;
    }

    @Override
    public List<? extends LineChartSeries> getSeries() {
        return series;
    }

    @Override
    public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        XSSFChart xssfChart = (XSSFChart) chart;
        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
        CTLineChart lineChart = plotArea.addNewLineChart();
        lineChart.addNewVaryColors().setVal(false);

        for (Series s : series) {
            s.addToChart(lineChart);
        }

        for (ChartAxis ax : axis) {
            lineChart.addNewAxId().setVal(ax.getId());
        }
    }
}
