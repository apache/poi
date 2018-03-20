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

import java.util.Map;

import org.apache.poi.util.Beta;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

@Beta
public class XDDFScatterChartData extends XDDFChartData {
    private CTScatterChart chart;

    public XDDFScatterChartData(CTScatterChart chart, Map<Long, XDDFChartAxis> categories,
            Map<Long, XDDFValueAxis> values) {
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
            scatterStyle.setVal(ScatterStyle.LINE_MARKER.underlying);
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
    public XDDFChartData.Series addSeries(XDDFDataSource<?> category,
            XDDFNumericalDataSource<? extends Number> values) {
        final int index = this.series.size();
        final CTScatterSer ctSer = this.chart.addNewSer();
        ctSer.addNewXVal();
        ctSer.addNewYVal();
        ctSer.addNewIdx().setVal(index);
        ctSer.addNewOrder().setVal(index);
        final Series added = new Series(ctSer, category, values);
        this.series.add(added);
        return added;
    }

    public class Series extends XDDFChartData.Series {
        private CTScatterSer series;

        protected Series(CTScatterSer series, XDDFDataSource<?> category, XDDFNumericalDataSource<?> values) {
            super(category, values);
            this.series = series;
        }

        protected Series(CTScatterSer series, CTAxDataSource category, CTNumDataSource values) {
            super(XDDFDataSourcesFactory.fromDataSource(category), XDDFDataSourcesFactory.fromDataSource(values));
            this.series = series;
        }

        @Override
        protected CTSerTx getSeriesText() {
            return series.getTx();
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
        public XDDFShapeProperties getShapeProperties() {
            if (series.isSetSpPr()) {
                return new XDDFShapeProperties(series.getSpPr());
            } else {
                return null;
            }
        }

        @Override
        public void setShapeProperties(XDDFShapeProperties properties) {
            if (properties == null) {
                if (series.isSetSpPr()) {
                    series.unsetSpPr();
                }
            } else {
                if (series.isSetSpPr()) {
                    series.setSpPr(properties.getXmlObject());
                } else {
                    series.addNewSpPr().set(properties.getXmlObject());
                }
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
