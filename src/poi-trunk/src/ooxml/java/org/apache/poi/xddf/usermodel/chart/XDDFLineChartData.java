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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

@Beta
public class XDDFLineChartData extends XDDFChartData {
    private CTLineChart chart;

    public XDDFLineChartData(CTLineChart chart, Map<Long, XDDFChartAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        this.chart = chart;
        for (CTLineSer series : chart.getSerList()) {
            this.series.add(new Series(series, series.getCat(), series.getVal()));
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

    public Grouping getGrouping() {
        return Grouping.valueOf(chart.getGrouping().getVal());
    }

    public void setGrouping(Grouping grouping) {
        chart.getGrouping().setVal(grouping.underlying);
    }

    @Override
    public XDDFChartData.Series addSeries(XDDFDataSource<?> category,
            XDDFNumericalDataSource<? extends Number> values) {
        final int index = this.series.size();
        final CTLineSer ctSer = this.chart.addNewSer();
        ctSer.addNewCat();
        ctSer.addNewVal();
        ctSer.addNewIdx().setVal(index);
        ctSer.addNewOrder().setVal(index);
        final Series added = new Series(ctSer, category, values);
        this.series.add(added);
        return added;
    }

    public class Series extends XDDFChartData.Series {
        private CTLineSer series;

        protected Series(CTLineSer series, XDDFDataSource<?> category,
                XDDFNumericalDataSource<? extends Number> values) {
            super(category, values);
            this.series = series;
        }

        protected Series(CTLineSer series, CTAxDataSource category, CTNumDataSource values) {
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

        public void setMarkerSize(short size) {
            CTMarker marker = getMarker();
            if (marker.isSetSize()) {
                marker.getSize().setVal(size);
            } else {
                marker.addNewSize().setVal(size);
            }
        }

        public void setMarkerStyle(MarkerStyle style) {
            CTMarker marker = getMarker();
            if (marker.isSetSymbol()) {
                marker.getSymbol().setVal(style.underlying);
            } else {
                marker.addNewSymbol().setVal(style.underlying);
            }
        }

        private CTMarker getMarker() {
            if (series.isSetMarker()) {
                return series.getMarker();
            } else {
                return series.addNewMarker();
            }
        }

        @Override
        protected CTAxDataSource getAxDS() {
            return series.getCat();
        }

        @Override
        protected CTNumDataSource getNumDS() {
            return series.getVal();
        }
    }
}
