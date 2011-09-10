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

import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ScatterChartSerie;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents DrawingML scatter charts.
 *
 * @author Roman Kashitsyn
 */
@Beta
public class XSSFScatterChartData implements ScatterChartData {

    /**
     * List of all data series.
     */
    private List<Serie> series;

    public XSSFScatterChartData() {
        series = new ArrayList<Serie>();
    }

    /**
     * Package private ScatterChartSerie implementation.
     */
    static class Serie implements ScatterChartSerie {
        private int id;
        private int order;
        private ChartDataSource<?> xs;
        private ChartDataSource<? extends Number> ys;

        protected Serie(int id, int order,
                        ChartDataSource<?> xs,
                        ChartDataSource<? extends Number> ys) {
            super();
            this.id = id;
            this.order = order;
            this.xs = xs;
            this.ys = ys;
        }

        /**
         * Returns data source used for X axis values.
         * @return data source used for X axis values
         */
        public ChartDataSource<?> getXValues() {
            return xs;
        }

        /**
         * Returns data source used for Y axis values.
         * @return data source used for Y axis values
         */
        public ChartDataSource<? extends Number> getYValues() {
            return ys;
        }

        protected void addToChart(CTScatterChart ctScatterChart) {
            CTScatterSer scatterSer = ctScatterChart.addNewSer();
            scatterSer.addNewIdx().setVal(this.id);
            scatterSer.addNewOrder().setVal(this.order);

            CTAxDataSource xVal = scatterSer.addNewXVal();
            XSSFChartUtil.buildAxDataSource(xVal, xs);

            CTNumDataSource yVal = scatterSer.addNewYVal();
            XSSFChartUtil.buildNumDataSource(yVal, ys);
        }
    }

    public ScatterChartSerie addSerie(ChartDataSource<?> xs,
                                      ChartDataSource<? extends Number> ys) {
        if (!ys.isNumeric()) {
            throw new IllegalArgumentException("Y axis data source must be numeric.");
        }
        int numOfSeries = series.size();
        Serie newSerie = new Serie(numOfSeries, numOfSeries, xs, ys);
        series.add(newSerie);
        return newSerie;
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        XSSFChart xssfChart = (XSSFChart) chart;
        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
        CTScatterChart scatterChart = plotArea.addNewScatterChart();
        addStyle(scatterChart);

        for (Serie s : series) {
            s.addToChart(scatterChart);
        }

        for (ChartAxis ax : axis) {
            scatterChart.addNewAxId().setVal(ax.getId());
        }
    }

    public List<? extends Serie> getSeries() {
        return series;
    }

    private void addStyle(CTScatterChart ctScatterChart) {
        CTScatterStyle scatterStyle = ctScatterChart.addNewScatterStyle();
        scatterStyle.setVal(STScatterStyle.LINE_MARKER);
    }
}
