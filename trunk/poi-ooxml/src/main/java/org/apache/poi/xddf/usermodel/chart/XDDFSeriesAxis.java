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

import org.apache.poi.util.Beta;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.text.XDDFRunProperties;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;

@Beta
public class XDDFSeriesAxis extends XDDFChartAxis {

    private CTSerAx ctSerAx;

    public XDDFSeriesAxis(CTPlotArea plotArea, AxisPosition position) {
        initializeAxis(plotArea, position);
    }

    public XDDFSeriesAxis(CTSerAx ctSerAx) {
        this.ctSerAx = ctSerAx;
    }

    @Override
    public XDDFShapeProperties getOrAddMajorGridProperties() {
        CTChartLines majorGridlines;
        if (ctSerAx.isSetMajorGridlines()) {
            majorGridlines = ctSerAx.getMajorGridlines();
        } else {
            majorGridlines = ctSerAx.addNewMajorGridlines();
        }
        return new XDDFShapeProperties(getOrAddLinesProperties(majorGridlines));
    }

    @Override
    public XDDFShapeProperties getOrAddMinorGridProperties() {
        CTChartLines minorGridlines;
        if (ctSerAx.isSetMinorGridlines()) {
            minorGridlines = ctSerAx.getMinorGridlines();
        } else {
            minorGridlines = ctSerAx.addNewMinorGridlines();
        }
        return new XDDFShapeProperties(getOrAddLinesProperties(minorGridlines));
    }

    @Override
    public XDDFShapeProperties getOrAddShapeProperties() {
        CTShapeProperties properties;
        if (ctSerAx.isSetSpPr()) {
            properties = ctSerAx.getSpPr();
        } else {
            properties = ctSerAx.addNewSpPr();
        }
        return new XDDFShapeProperties(properties);
    }

    /**
     * @since POI 4.0.2
     */
    @Override
    public XDDFRunProperties getOrAddTextProperties() {
        CTTextBody text;
        if (ctSerAx.isSetTxPr()) {
            text = ctSerAx.getTxPr();
        } else {
            text = ctSerAx.addNewTxPr();
        }
        return new XDDFRunProperties(getOrAddTextProperties(text));
    }

    /**
     * @since 4.0.1
     */
    @Override
    public void setTitle(String text) {
        if (!ctSerAx.isSetTitle()) {
            ctSerAx.addNewTitle();
        }
        XDDFTitle title = new XDDFTitle(null, ctSerAx.getTitle());
        title.setOverlay(false);
        title.setText(text);
    }

    @Override
    public boolean isSetMinorUnit() {
        return false;
    }

    @Override
    public void setMinorUnit(double minor) {
        // nothing
    }

    @Override
    public double getMinorUnit() {
        return Double.NaN;
    }

    @Override
    public boolean isSetMajorUnit() {
        return false;
    }

    @Override
    public void setMajorUnit(double major) {
        // nothing
    }

    @Override
    public double getMajorUnit() {
        return Double.NaN;
    }

    @Override
    public void crossAxis(XDDFChartAxis axis) {
        ctSerAx.getCrossAx().setVal(axis.getId());
    }

    @Override
    protected CTUnsignedInt getCTAxId() {
        return ctSerAx.getAxId();
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctSerAx.getAxPos();
    }

    @Override
    public boolean hasNumberFormat() {
        return ctSerAx.isSetNumFmt();
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctSerAx.isSetNumFmt()) {
            return ctSerAx.getNumFmt();
        }
        return ctSerAx.addNewNumFmt();
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctSerAx.getScaling();
    }

    @Override
    protected CTCrosses getCTCrosses() {
        CTCrosses crosses = ctSerAx.getCrosses();
        if (crosses == null) {
            return ctSerAx.addNewCrosses();
        } else {
            return crosses;
        }
    }

    @Override
    protected CTBoolean getDelete() {
        return ctSerAx.getDelete();
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctSerAx.getMajorTickMark();
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctSerAx.getMinorTickMark();
    }

    @Override
    protected CTTickLblPos getCTTickLblPos() {
        return ctSerAx.getTickLblPos();
    }

    private void initializeAxis(CTPlotArea plotArea, AxisPosition position) {
        final long id = getNextAxId(plotArea);
        ctSerAx = plotArea.addNewSerAx();
        ctSerAx.addNewAxId().setVal(id);
        ctSerAx.addNewAxPos();
        ctSerAx.addNewScaling();
        ctSerAx.addNewCrosses();
        ctSerAx.addNewCrossAx();
        ctSerAx.addNewTickLblPos();
        ctSerAx.addNewDelete();
        ctSerAx.addNewMajorTickMark();
        ctSerAx.addNewMinorTickMark();

        setPosition(position);
        setOrientation(AxisOrientation.MIN_MAX);
        setCrosses(AxisCrosses.AUTO_ZERO);
        setVisible(true);
        setMajorTickMark(AxisTickMark.CROSS);
        setMinorTickMark(AxisTickMark.NONE);
        setTickLabelPosition(AxisTickLabelPosition.NEXT_TO);
    }
}
