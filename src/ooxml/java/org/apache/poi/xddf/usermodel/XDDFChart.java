/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xddf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;

public abstract class XDDFChart extends POIXMLDocumentPart {

    /**
     * Root element of the Chart part
     */
    protected final CTChartSpace chartSpace;

    /**
     * Chart element in the chart space
     */
    protected final CTChart chart;

    /**
     * Construct a chart.
     */
    protected XDDFChart() {
        super();

        chartSpace = CTChartSpace.Factory.newInstance();
        chart = chartSpace.addNewChart();
        chart.addNewPlotArea();
    }

    /**
     * Construct a DrawingML chart from a package part.
     *
     * @param part the package part holding the chart data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     *
     * @since POI 3.14-Beta1
     */
    protected XDDFChart(PackagePart part) throws IOException, XmlException {
        super(part);

        chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS).getChartSpace();
        chart = chartSpace.getChart();
    }

    /**
     * Return the underlying CTChartSpace bean, the root element of the Chart part.
     *
     * @return the underlying CTChartSpace bean
     */
    @Internal
    public CTChartSpace getCTChartSpace(){
        return chartSpace;
    }

    /**
     * Return the underlying CTChart bean, within the Chart Space
     *
     * @return the underlying CTChart bean
     */
    @Internal
    public CTChart getCTChart(){
        return chart;

    }

    /**
     * Return the underlying CTPlotArea bean, within the Chart
     *
     * @return the underlying CTPlotArea bean
     */
    @Internal
    protected CTPlotArea getCTPlotArea() {
        return chart.getPlotArea();
    }

    /**
     * @return true if only visible cells will be present on the chart,
     *         false otherwise
     */
    public boolean isPlotOnlyVisibleCells() {
        if (chart.isSetPlotVisOnly()) {
            return chart.getPlotVisOnly().getVal();
        } else {
            return false;
        }
    }

    /**
     * @param only a flag specifying if only visible cells should be
     *        present on the chart
     */
    public void setPlotOnlyVisibleCells(boolean only) {
        if (!chart.isSetPlotVisOnly()) {
            chart.setPlotVisOnly(CTBoolean.Factory.newInstance());
        }
        chart.getPlotVisOnly().setVal(only);
    }

    public void setFloor(int thickness) {
        if (!chart.isSetFloor()) {
            chart.setFloor(CTSurface.Factory.newInstance());
        }
        chart.getFloor().getThickness().setVal(thickness);
    }

    public void setBackWall(int thickness) {
        if (!chart.isSetBackWall()) {
            chart.setBackWall(CTSurface.Factory.newInstance());
        }
        chart.getBackWall().getThickness().setVal(thickness);
    }

    public void setSideWall(int thickness) {
        if (!chart.isSetSideWall()) {
            chart.setSideWall(CTSurface.Factory.newInstance());
        }
        chart.getSideWall().getThickness().setVal(thickness);
    }

    public void setAutoTitleDeleted(boolean deleted) {
        if (!chart.isSetAutoTitleDeleted()) {
            chart.setAutoTitleDeleted(CTBoolean.Factory.newInstance());
        }
        chart.getAutoTitleDeleted().setVal(deleted);
    }


    public XDDFChartLegend getOrAddLegend() {
        return new XDDFChartLegend(chart);
    }

    public void deleteLegend() {
        if (chart.isSetLegend()) {
            chart.unsetLegend();
        }
    }

    public XDDFManualLayout getOrAddManualLayout() {
        return new XDDFManualLayout(chart.getPlotArea());
    }


    public void plot(XDDFChartData data) {
        for (XDDFChartData.Series series : data.getSeries()) {
            series.plot();
        }
    }

}
