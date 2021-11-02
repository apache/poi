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
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLayout;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTManualLayout;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;

/**
 * Represents a DrawingML manual layout.
 */
@Beta
public final class XDDFManualLayout {

    /**
     * Underlaying CTManualLayout bean.
     */
    private CTManualLayout layout;

    private static final LayoutMode defaultLayoutMode = LayoutMode.EDGE;
    private static final LayoutTarget defaultLayoutTarget = LayoutTarget.INNER;

    /**
     * Create a new DrawingML manual layout.
     *
     * @param ctLayout
     *            a DrawingML layout that should be used as base.
     */
    public XDDFManualLayout(CTLayout ctLayout) {
        initializeLayout(ctLayout);
    }

    /**
     * Create a new DrawingML manual layout for chart.
     *
     * @param ctPlotArea
     *            a chart's plot area to create layout for.
     */
    public XDDFManualLayout(CTPlotArea ctPlotArea) {
        CTLayout ctLayout = ctPlotArea.isSetLayout() ? ctPlotArea.getLayout() : ctPlotArea.addNewLayout();

        initializeLayout(ctLayout);
    }

    /**
     * Return the underlying CTManualLayout bean.
     *
     * @return the underlying CTManualLayout bean.
     */
    @Internal
    protected CTManualLayout getXmlObject() {
        return layout;
    }

    public void setExtensionList(XDDFChartExtensionList list) {
        if (list == null) {
            if (layout.isSetExtLst()) {
                layout.unsetExtLst();
            }
        } else {
            layout.setExtLst(list.getXmlObject());
        }
    }

    public XDDFChartExtensionList getExtensionList() {
        if (layout.isSetExtLst()) {
            return new XDDFChartExtensionList(layout.getExtLst());
        } else {
            return null;
        }
    }

    public void setWidthRatio(double ratio) {
        if (!layout.isSetW()) {
            layout.addNewW();
        }
        layout.getW().setVal(ratio);
    }

    public double getWidthRatio() {
        if (!layout.isSetW()) {
            return 0.0;
        }
        return layout.getW().getVal();
    }

    public void setHeightRatio(double ratio) {
        if (!layout.isSetH()) {
            layout.addNewH();
        }
        layout.getH().setVal(ratio);
    }

    public double getHeightRatio() {
        if (!layout.isSetH()) {
            return 0.0;
        }
        return layout.getH().getVal();
    }

    public LayoutTarget getTarget() {
        if (!layout.isSetLayoutTarget()) {
            return defaultLayoutTarget;
        }
        return LayoutTarget.valueOf(layout.getLayoutTarget().getVal());
    }

    public void setTarget(LayoutTarget target) {
        if (!layout.isSetLayoutTarget()) {
            layout.addNewLayoutTarget();
        }
        layout.getLayoutTarget().setVal(target.underlying);
    }

    public LayoutMode getXMode() {
        if (!layout.isSetXMode()) {
            return defaultLayoutMode;
        }
        return LayoutMode.valueOf(layout.getXMode().getVal());
    }

    public void setXMode(LayoutMode mode) {
        if (!layout.isSetXMode()) {
            layout.addNewXMode();
        }
        layout.getXMode().setVal(mode.underlying);
    }

    public LayoutMode getYMode() {
        if (!layout.isSetYMode()) {
            return defaultLayoutMode;
        }
        return LayoutMode.valueOf(layout.getYMode().getVal());
    }

    public void setYMode(LayoutMode mode) {
        if (!layout.isSetYMode()) {
            layout.addNewYMode();
        }
        layout.getYMode().setVal(mode.underlying);
    }

    public double getX() {
        if (!layout.isSetX()) {
            return 0.0;
        }
        return layout.getX().getVal();
    }

    public void setX(double x) {
        if (!layout.isSetX()) {
            layout.addNewX();
        }
        layout.getX().setVal(x);
    }

    public double getY() {
        if (!layout.isSetY()) {
            return 0.0;
        }
        return layout.getY().getVal();
    }

    public void setY(double y) {
        if (!layout.isSetY()) {
            layout.addNewY();
        }
        layout.getY().setVal(y);
    }

    public LayoutMode getWidthMode() {
        if (!layout.isSetWMode()) {
            return defaultLayoutMode;
        }
        return LayoutMode.valueOf(layout.getWMode().getVal());
    }

    public void setWidthMode(LayoutMode mode) {
        if (!layout.isSetWMode()) {
            layout.addNewWMode();
        }
        layout.getWMode().setVal(mode.underlying);
    }

    public LayoutMode getHeightMode() {
        if (!layout.isSetHMode()) {
            return defaultLayoutMode;
        }
        return LayoutMode.valueOf(layout.getHMode().getVal());
    }

    public void setHeightMode(LayoutMode mode) {
        if (!layout.isSetHMode()) {
            layout.addNewHMode();
        }
        layout.getHMode().setVal(mode.underlying);
    }

    private void initializeLayout(CTLayout ctLayout) {
        if (ctLayout.isSetManualLayout()) {
            this.layout = ctLayout.getManualLayout();
        } else {
            this.layout = ctLayout.addNewManualLayout();
        }
    }
}
