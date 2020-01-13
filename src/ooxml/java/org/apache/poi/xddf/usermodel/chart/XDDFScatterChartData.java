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
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

@Beta
public class XDDFScatterChartData extends XDDFChartData {
    private CTScatterChart chart;

    @Internal
    protected XDDFScatterChartData(
            XDDFChart parent,
            CTScatterChart chart,
            Map<Long, XDDFChartAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        super(parent);
        this.chart = chart;
        for (CTScatterSer series : chart.getSerList()) {
            this.series.add(new Series(series, series.getXVal(), series.getYVal()));
        }
        defineAxes(categories, values);
    }

    private void defineAxes(Map<Long, XDDFChartAxis> categories, Map<Long, XDDFValueAxis> values) {
        if (chart.sizeOfAxIdArray() == 0) {
            for (Long id : categories.keySet()) {
                chart.addNewAxId().setVal(id);
            }
            for (Long id : values.keySet()) {
                chart.addNewAxId().setVal(id);
            }
        }
        defineAxes(chart.getAxIdArray(), categories, values);
    }

    @Internal
    @Override
    protected void removeCTSeries(int n) {
        chart.removeSer(n);
    }

    @Override
    public void setVaryColors(Boolean varyColors) {
        if (varyColors == null) {
            if (chart.isSetVaryColors()) {
                chart.unsetVaryColors();
            }
        } else {
            if (chart.isSetVaryColors()) {
                chart.getVaryColors().setVal(varyColors);
            } else {
                chart.addNewVaryColors().setVal(varyColors);
            }
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
        final long index = this.parent.incrementSeriesCount();
        final CTScatterSer ctSer = this.chart.addNewSer();
        ctSer.addNewXVal();
        ctSer.addNewYVal();
        ctSer.addNewIdx().setVal(index);
        ctSer.addNewOrder().setVal(index);
        final Series added = new Series(ctSer, category, values);
        added.setMarkerStyle(MarkerStyle.NONE);
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
            if (series.isSetTx()) {
                return series.getTx();
            } else {
                return series.addNewTx();
            }
        }

        /**
         * @since 4.0.1
         */
        public Boolean isSmooth() {
            if (series.isSetSmooth()) {
                return series.getSmooth().getVal();
            } else {
                return null;
            }
        }

        /**
         * @param smooth
         *        whether or not to smooth lines, if <code>null</code> then reverts to default.
         * @since 4.0.1
         */
        public void setSmooth(Boolean smooth) {
            if (smooth == null) {
                if (series.isSetSmooth()) {
                    series.unsetSmooth();
                }
            } else {
                if (series.isSetSmooth()) {
                    series.getSmooth().setVal(smooth);
                } else {
                    series.addNewSmooth().setVal(smooth);
                }
            }
        }

        /**
         * @param size
         * <dl><dt>Minimum inclusive:</dt><dd>2</dd><dt>Maximum inclusive:</dt><dd>72</dd></dl>
         * @since 4.0.1
         */
        public void setMarkerSize(short size) {
            if (size < 2 || 72 < size) {
                throw new IllegalArgumentException("Minimum inclusive: 2; Maximum inclusive: 72");
            }
            CTMarker marker = getMarker();
            if (marker.isSetSize()) {
                marker.getSize().setVal(size);
            } else {
                marker.addNewSize().setVal(size);
            }
        }

        /**
         * @since 4.0.1
         */
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

        /**
         * @since POI 4.1.2
         */
        public int getErrorBarsCount() {
            return series.sizeOfErrBarsArray();
        }

        /**
         * @since POI 4.1.2
         */
        public XDDFErrorBars getErrorBars(int index) {
            return new XDDFErrorBars(series.getErrBarsArray(index));
        }

        /**
         * @since POI 4.1.2
         */
        public XDDFErrorBars addNewErrorBars() {
            return new XDDFErrorBars(series.addNewErrBars());
        }

        /**
         * @since POI 4.1.2
         */
        public XDDFErrorBars insertNewErrorBars(int index) {
            return new XDDFErrorBars(series.insertNewErrBars(index));
        }

        /**
         * @since POI 4.1.2
         */
        public void removeErrorBars(int index) {
            series.removeErrBars(index);
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

        @Override
        protected void setIndex(long val) {
            series.getIdx().setVal(val);
        }

        @Override
        protected void setOrder(long val) {
            series.getOrder().setVal(val);
        }
    }
}
