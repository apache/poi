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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.text.TextContainer;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

/**
 * Represents a DrawingML chart legend
 */
@Beta
public final class XDDFChartLegend implements TextContainer {

    /**
     * Underlying CTLegend bean
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
    protected CTLegend getXmlObject() {
        return legend;
    }

    @Internal  // will later replace with XDDFShapeProperties
    public CTShapeProperties getShapeProperties() {
        if (legend.isSetSpPr()) {
            return legend.getSpPr();
        } else {
            return null;
        }
    }

    @Internal  // will later replace with XDDFShapeProperties
    public void setShapeProperties(CTShapeProperties properties) {
        if (properties == null) {
            if (legend.isSetSpPr()) {
                legend.unsetSpPr();
            }
        } else {
            legend.setSpPr(properties);
        }
    }

    public XDDFTextBody getTextBody() {
        if (legend.isSetTxPr()) {
            return new XDDFTextBody(this, legend.getTxPr());
        } else {
            return null;
        }
    }

    public void setTextBody(XDDFTextBody body) {
        if (body == null) {
            if (legend.isSetTxPr()) {
                legend.unsetTxPr();
            }
        } else {
            legend.setTxPr(body.getXmlObject());
        }
    }

    public XDDFLegendEntry addEntry() {
        return new XDDFLegendEntry(legend.addNewLegendEntry());
    }

    public XDDFLegendEntry getEntry(int index) {
        return new XDDFLegendEntry(legend.getLegendEntryArray(index));
    }

    public List<XDDFLegendEntry> getEntries() {
        return legend
            .getLegendEntryList()
            .stream()
            .map(entry -> new XDDFLegendEntry(entry))
            .collect(Collectors.toList());
    }

    public void setExtensionList(XDDFChartExtensionList list) {
        if (list == null) {
            if (legend.isSetExtLst()) {
                legend.unsetExtLst();
            }
        } else {
            legend.setExtLst(list.getXmlObject());
        }
    }

    public XDDFChartExtensionList getExtensionList() {
        if (legend.isSetExtLst()) {
            return new XDDFChartExtensionList(legend.getExtLst());
        } else {
            return null;
        }
    }

    public void setLayout(XDDFLayout layout) {
        if (layout == null) {
            if (legend.isSetLayout()) {
                legend.unsetLayout();
            }
        } else {
            legend.setLayout(layout.getXmlObject());
        }
    }

    public XDDFLayout getLayout() {
        if (legend.isSetLayout()) {
            return new XDDFLayout(legend.getLayout());
        } else {
            return null;
        }
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

    public XDDFManualLayout getOrAddManualLayout() {
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

    public <R> Optional<R> findDefinedParagraphProperty(
            Function<CTTextParagraphProperties, Boolean> isSet,
            Function<CTTextParagraphProperties, R> getter) {
        return Optional.empty(); // chart legend has no (indirect) paragraph properties
    }

    public <R> Optional<R> findDefinedRunProperty(
            Function<CTTextCharacterProperties, Boolean> isSet,
            Function<CTTextCharacterProperties, R> getter) {
        return Optional.empty(); // chart legend has no (indirect) paragraph properties
    }
}
