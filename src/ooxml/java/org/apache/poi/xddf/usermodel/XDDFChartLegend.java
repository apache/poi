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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;

/**
 * Represents a DrawingML chart legend
 */
@Beta
public final class XDDFChartLegend {

    /**
     * Underlaying CTLagend bean
     */
    private CTLegend legend;

    /**
     * Create a new DrawingML chart legend
     */
    public XDDFChartLegend(CTChart ctChart) {
        this.legend = (ctChart.isSetLegend()) ? ctChart.getLegend() : ctChart.addNewLegend();

        setDefaults();
    }

    /**
     * Set sensible default styling.
     */
    private void setDefaults() {
        if (!legend.isSetOverlay()) {
            legend.addNewOverlay();
        }
        legend.getOverlay().setVal(false);
    }

    /**
     * Return the underlying CTLegend bean.
     *
     * @return the underlying CTLegend bean
     */
    @Internal
    public CTLegend getCTLegend() {
        return legend;
    }

    public void setPosition(LegendPosition position) {
        if (!legend.isSetLegendPos()) {
            legend.addNewLegendPos();
        }
        legend.getLegendPos().setVal(position.underlying);
    }

    /*
     * According to ECMA-376 default position is RIGHT.
     */
    public LegendPosition getPosition() {
        if (legend.isSetLegendPos()) {
            return LegendPosition.valueOf(legend.getLegendPos().getVal());
        } else {
            return LegendPosition.RIGHT;
        }
    }

    public XDDFManualLayout getManualLayout() {
        if (!legend.isSetLayout()) {
            legend.addNewLayout();
        }
        return new XDDFManualLayout(legend.getLayout());
    }

    public boolean isOverlay() {
        return legend.getOverlay().getVal();
    }

    public void setOverlay(boolean value) {
        legend.getOverlay().setVal(value);
    }
}
