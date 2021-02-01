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

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLine3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;

@Beta
public class XDDFLine3DChartData extends XDDFChartData {
    private CTLine3DChart chart;

    @Internal
    protected XDDFLine3DChartData(
            XDDFChart parent,
            CTLine3DChart chart,
            Map<Long, XDDFChartAxis> categories,
            Map<Long, XDDFValueAxis> values) {
        super(parent);
        this.chart = chart;
        for (CTLineSer series : chart.getSerList()) {
            this.series.add(new Series(series, series.getCat(), series.getVal()));
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

    public Grouping getGrouping() {
        return Grouping.valueOf(chart.getGrouping().getVal());
    }

   public void setGrouping(Grouping grouping) {
      if (chart.getGrouping() != null) {
         chart.getGrouping().setVal(grouping.underlying);
      } else {
         chart.addNewGrouping().setVal(grouping.underlying);
      }
   }

   public Integer getGapDepth() {
       return (chart.isSetGapDepth()) ? POIXMLUnits.parsePercent(chart.getGapDepth().xgetVal()) / 1000 : null;
   }

   public void setGapDepth(Integer depth) {
       if (depth == null) {
           if (chart.isSetGapDepth()) {
               chart.unsetGapDepth();
           }
       } else {
           if (chart.isSetGapDepth()) {
               chart.getGapDepth().setVal(depth);
           } else {
               chart.addNewGapDepth().setVal(depth);
           }
       }
   }

    @Override
    public XDDFChartData.Series addSeries(XDDFDataSource<?> category,
            XDDFNumericalDataSource<? extends Number> values) {
        final long index = this.parent.incrementSeriesCount();
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
            if (series.isSetTx()) {
                return series.getTx();
            } else {
                return series.addNewTx();
            }
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
         *
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
         * @since 4.1.2
         */
        public boolean hasErrorBars() {
            return series.isSetErrBars();
        }

        /**
         * @since 4.1.2
         */
        public XDDFErrorBars getErrorBars() {
                if (series.isSetErrBars()) {
                    return new XDDFErrorBars(series.getErrBars());
                } else {
                    return null;
                }
        }

        /**
         * @since 4.1.2
         */
        public void setErrorBars(XDDFErrorBars bars) {
            if (bars == null) {
                if (series.isSetErrBars()) {
                    series.unsetErrBars();
                }
            } else {
                if (series.isSetErrBars()) {
                    series.getErrBars().set(bars.getXmlObject());
                } else {
                    series.addNewErrBars().set(bars.getXmlObject());
                }
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
