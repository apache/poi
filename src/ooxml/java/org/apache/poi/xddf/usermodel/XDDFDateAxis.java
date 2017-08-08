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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartLines;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDateAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTUnsignedInt;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

/**
 * Date axis type. Currently only implements the same values as
 * {@link XDDFCategoryAxis}, since the two are nearly identical.
 */
@Beta
public class XDDFDateAxis extends XDDFChartAxis {

    private CTDateAx ctDateAx;

    public XDDFDateAxis(CTPlotArea plotArea, AxisPosition position) {
        initializeAxis(plotArea, position);
    }

    public XDDFDateAxis(CTDateAx ctDateAx) {
        this.ctDateAx = ctDateAx;
    }

    @Override
    @Internal
    public CTChartLines getMajorGridLines() {
        return ctDateAx.getMajorGridlines();
    }

    @Override
    @Internal
    public CTShapeProperties getLine() {
        return ctDateAx.getSpPr();
    }

    @Override
    public void crossAxis(XDDFChartAxis axis) {
        ctDateAx.getCrossAx().setVal(axis.getId());
    }

    @Override
    protected CTUnsignedInt getCTAxId() {
        return ctDateAx.getAxId();
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctDateAx.getAxPos();
    }

    @Override
    public boolean hasNumberFormat() {
        return ctDateAx.isSetNumFmt();
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctDateAx.isSetNumFmt()) {
            return ctDateAx.getNumFmt();
        }
        return ctDateAx.addNewNumFmt();
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctDateAx.getScaling();
    }

    @Override
    protected CTCrosses getCTCrosses() {
        CTCrosses crosses = ctDateAx.getCrosses();
        if (crosses == null) {
            return ctDateAx.addNewCrosses();
        } else {
            return crosses;
        }
    }

    @Override
    protected CTBoolean getDelete() {
        return ctDateAx.getDelete();
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctDateAx.getMajorTickMark();
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctDateAx.getMinorTickMark();
    }

    private void initializeAxis(CTPlotArea plotArea, AxisPosition position) {
        final long id = getNextAxId(plotArea);
        ctDateAx = plotArea.addNewDateAx();
        ctDateAx.addNewAxId().setVal(id);
        ctDateAx.addNewAxPos();
        ctDateAx.addNewScaling();
        ctDateAx.addNewCrosses();
        ctDateAx.addNewCrossAx();
        ctDateAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
        ctDateAx.addNewDelete();
        ctDateAx.addNewMajorTickMark();
        ctDateAx.addNewMinorTickMark();

        setPosition(position);
        setOrientation(AxisOrientation.MIN_MAX);
        setCrosses(AxisCrosses.AUTO_ZERO);
        setVisible(true);
        setMajorTickMark(AxisTickMark.CROSS);
        setMinorTickMark(AxisTickMark.NONE);
    }
}
