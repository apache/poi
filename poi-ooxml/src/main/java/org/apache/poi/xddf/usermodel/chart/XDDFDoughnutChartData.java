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

import java.util.List;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

@Beta
public class XDDFDoughnutChartData extends XDDFChartData {
    private CTDoughnutChart chart;

    @Internal
    protected XDDFDoughnutChartData(XDDFChart parent, CTDoughnutChart chart) {
        super(parent);
        this.chart = chart;
        for (CTPieSer series : chart.getSerList()) {
            this.series.add(new Series(series, series.getCat(), series.getVal()));
        }
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

    public Integer getFirstSliceAngle() {
        if (chart.isSetFirstSliceAng()) {
            return chart.getFirstSliceAng().getVal();
        } else {
            return null;
        }
    }

    public void setFirstSliceAngle(Integer angle) {
        if (angle == null) {
            if (chart.isSetFirstSliceAng()) {
                chart.unsetFirstSliceAng();
            }
        } else {
            if (angle < 0 || 360 < angle) {
                throw new IllegalArgumentException("Value of angle must be between 0 and 360, both inclusive.");
            }
            if (chart.isSetFirstSliceAng()) {
                chart.getFirstSliceAng().setVal(angle);
            } else {
                chart.addNewFirstSliceAng().setVal(angle);
            }
        }
    }

    public Integer getHoleSize() {
        if (chart.isSetHoleSize()) {
            return POIXMLUnits.parsePercent(chart.getHoleSize().xgetVal());
        } else {
            return null;
        }
    }

    public void setHoleSize(Integer holeSize) {
        if (holeSize == null) {
            if (chart.isSetHoleSize()) {
                chart.unsetHoleSize();
            }
        } else {
            if (holeSize < 10 || holeSize > 90) {
                throw new IllegalArgumentException("Value of holeSize must be between 10 and 90, both inclusive.");
            }
            if (chart.isSetHoleSize()) {
                chart.getHoleSize().setVal(holeSize);
            } else {
                chart.addNewHoleSize().setVal(holeSize);
            }
        }
    }

    @Override
    public XDDFChartData.Series addSeries(XDDFDataSource<?> category,
            XDDFNumericalDataSource<? extends Number> values) {
        final long index = this.parent.incrementSeriesCount();
        final CTPieSer ctSer = this.chart.addNewSer();
        ctSer.addNewCat();
        ctSer.addNewVal();
        ctSer.addNewIdx().setVal(index);
        ctSer.addNewOrder().setVal(index);
        final Series added = new Series(ctSer, category, values);
        this.series.add(added);
        return added;
    }

    public class Series extends XDDFChartData.Series {
        private CTPieSer series;

        protected Series(CTPieSer series, XDDFDataSource<?> category,
                XDDFNumericalDataSource<? extends Number> values) {
            super(category, values);
            this.series = series;
        }

        protected Series(CTPieSer series, CTAxDataSource category, CTNumDataSource values) {
            super(XDDFDataSourcesFactory.fromDataSource(category), XDDFDataSourcesFactory.fromDataSource(values));
            this.series = series;
        }

        /**
         * @since POI 5.2.3
         */
        public CTPieSer getCTPieSer() {
            return series;
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

        public Long getExplosion() {
            if (series.isSetExplosion()) {
                return series.getExplosion().getVal();
            } else {
                return null;
            }
        }

        public void setExplosion(Long explosion) {
            if (explosion == null) {
                if (series.isSetExplosion()) {
                    series.unsetExplosion();
                }
            } else {
                if (series.isSetExplosion()) {
                    series.getExplosion().setVal(explosion);
                } else {
                    series.addNewExplosion().setVal(explosion);
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

        @Override
        protected List<CTDPt> getDPtList() {
            return series.getDPtList();
        }
    }
}
