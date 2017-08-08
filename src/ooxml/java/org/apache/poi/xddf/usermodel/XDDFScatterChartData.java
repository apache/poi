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

package org.apache.poi.xddf.usermodel;

import java.util.Map;

import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;

@Beta
public class XDDFScatterChartData extends XDDFChartData {
    private CTScatterChart chart;

    public XDDFScatterChartData(XSSFSheet sheet, CTScatterChart chart, Map<Long, XDDFCategoryAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        super(sheet);
        this.chart = chart;
        for (CTScatterSer series : chart.getSerList()) {
            this.series.add(new Series(series, series.getXVal(), series.getYVal()));
        }
        defineAxes(chart.getAxIdArray(), categories, values);
    }

    @Override
    public void setVaryColors(boolean varyColors) {
        if (chart.isSetVaryColors()) {
            chart.getVaryColors().setVal(varyColors);
        } else {
            chart.addNewVaryColors().setVal(varyColors);
        }
    }

    public ScatterStyle getStyle() {
        CTScatterStyle scatterStyle = chart.getScatterStyle();
        if (scatterStyle == null) {
            scatterStyle = chart.addNewScatterStyle();
        }
        return ScatterStyle.valueOf(scatterStyle.getVal());
    }

    public void setStyle(ScatterStyle style) {
        CTScatterStyle scatterStyle = chart.getScatterStyle();
        if (scatterStyle == null) {
            scatterStyle = chart.addNewScatterStyle();
        }
        scatterStyle.setVal(style.underlying);
    }

    @Override
    public void addSeries(XDDFCategoryDataSource category, XDDFNumericalDataSource<? extends Number> values) {
        this.series.add(new Series(this.chart.addNewSer(), category, values));
    }

    public class Series extends XDDFChartData.Series {
        private CTScatterSer series;

        protected Series(CTScatterSer series, XDDFCategoryDataSource category,
                XDDFNumericalDataSource<? extends Number> values) {
            super(category, values);
            series.addNewXVal();
            series.addNewYVal();
            this.series = series;
        }

        protected Series(CTScatterSer series, CTAxDataSource category, CTNumDataSource values) {
            super(XDDFDataSourcesFactory.fromAxDataSource(category), XDDFDataSourcesFactory.fromNumDataSource(values));
            this.series = series;
        }

        @Override
        protected CTStrRef getSeriesTxStrRef() {
            return series.getTx().getStrRef();
        }

        @Override
        public void setShowLeaderLines(boolean showLeaderLines) {
            if (!series.isSetDLbls()) {
                series.addNewDLbls();
            }
            if (series.getDLbls().isSetShowLeaderLines()) {
                series.getDLbls().getShowLeaderLines().setVal(showLeaderLines);
            } else {
                series.getDLbls().addNewShowLeaderLines().setVal(showLeaderLines);
            }
        }

        @Override
        protected CTAxDataSource getAxDS() {
            return series.getXVal();
        }

        @Override
        protected CTNumDataSource getNumDS() {
            return series.getYVal();
        }
    }
}
