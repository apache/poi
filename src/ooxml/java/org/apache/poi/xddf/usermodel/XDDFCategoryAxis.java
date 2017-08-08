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

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

public class XDDFCategoryAxis extends XDDFChartAxis {

    private CTCatAx ctCatAx;

    public XDDFCategoryAxis(CTPlotArea plotArea, AxisPosition position) {
        initializeAxis(plotArea, position);
    }

    public XDDFCategoryAxis(CTCatAx ctCatAx) {
        this.ctCatAx = ctCatAx;
    }

    @Override
    @Internal
    public CTChartLines getMajorGridLines() {
        return ctCatAx.getMajorGridlines();
    }

    @Override
    @Internal
    public CTShapeProperties getLine() {
        return ctCatAx.getSpPr();
    }

    @Override
    public void crossAxis(XDDFChartAxis axis) {
        ctCatAx.getCrossAx().setVal(axis.getId());
    }

    @Override
    protected CTUnsignedInt getCTAxId() {
        return ctCatAx.getAxId();
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctCatAx.getAxPos();
    }

    @Override
    public boolean hasNumberFormat() {
        return ctCatAx.isSetNumFmt();
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctCatAx.isSetNumFmt()) {
            return ctCatAx.getNumFmt();
        }
        return ctCatAx.addNewNumFmt();
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctCatAx.getScaling();
    }

    @Override
    protected CTCrosses getCTCrosses() {
        CTCrosses crosses = ctCatAx.getCrosses();
        if (crosses == null) {
            return ctCatAx.addNewCrosses();
        } else {
            return crosses;
        }
    }

    @Override
    protected CTBoolean getDelete() {
        return ctCatAx.getDelete();
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctCatAx.getMajorTickMark();
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctCatAx.getMinorTickMark();
    }

    public AxisLabelAlignment getLabelAlignment() {
        return AxisLabelAlignment.valueOf(ctCatAx.getLblAlgn().getVal());
    }

    public void setLabelAlignment(AxisLabelAlignment labelAlignment) {
        ctCatAx.getLblAlgn().setVal(labelAlignment.underlying);
    }

    private void initializeAxis(CTPlotArea plotArea, AxisPosition position) {
        final long id = getNextAxId(plotArea);
        ctCatAx = plotArea.addNewCatAx();
        ctCatAx.addNewAxId().setVal(id);
        ctCatAx.addNewAxPos();
        ctCatAx.addNewScaling();
        ctCatAx.addNewCrosses();
        ctCatAx.addNewCrossAx();
        ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        ctCatAx.addNewDelete();
        ctCatAx.addNewMajorTickMark();
        ctCatAx.addNewMinorTickMark();

        setPosition(position);
        setOrientation(AxisOrientation.MIN_MAX);
        setCrosses(AxisCrosses.AUTO_ZERO);
        setVisible(true);
        setMajorTickMark(AxisTickMark.CROSS);
        setMinorTickMark(AxisTickMark.NONE);
    }
}
