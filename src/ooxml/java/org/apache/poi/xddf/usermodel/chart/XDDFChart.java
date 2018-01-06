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

package org.apache.poi.xddf.usermodel.chart;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDateAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;

@Beta
public abstract class XDDFChart extends POIXMLDocumentPart {

    protected List<XDDFChartAxis> axes = new ArrayList<>();

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
    public CTChartSpace getCTChartSpace() {
        return chartSpace;
    }

    /**
     * Return the underlying CTChart bean, within the Chart Space
     *
     * @return the underlying CTChart bean
     */
    @Internal
    public CTChart getCTChart() {
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

    public XDDFShapeProperties getOrAddShapeProperties() {
        CTPlotArea plotArea = getCTPlotArea();
        CTShapeProperties properties;
        if (plotArea.isSetSpPr()) {
            properties = plotArea.getSpPr();
        } else {
            properties = plotArea.addNewSpPr();
        }
        return new XDDFShapeProperties(properties);
    }

    public void deleteShapeProperties() {
        if (getCTPlotArea().isSetSpPr()) {
            getCTPlotArea().unsetSpPr();
        }
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

    public List<XDDFChartData> getChartSeries() {
        List<XDDFChartData> series = new LinkedList<>();
        CTPlotArea plotArea = getCTPlotArea();
        Map<Long, XDDFChartAxis> categories = getCategoryAxes();
        Map<Long, XDDFValueAxis> values = getValueAxes();

        for (int i = 0; i < plotArea.sizeOfBarChartArray(); i++) {
            CTBarChart barChart = plotArea.getBarChartArray(i);
            series.add(new XDDFBarChartData(barChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfLineChartArray(); i++) {
            CTLineChart lineChart = plotArea.getLineChartArray(i);
            series.add(new XDDFLineChartData(lineChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfPieChartArray(); i++) {
            CTPieChart pieChart = plotArea.getPieChartArray(i);
            series.add(new XDDFPieChartData(pieChart));
        }

        for (int i = 0; i < plotArea.sizeOfRadarChartArray(); i++) {
            CTRadarChart radarChart = plotArea.getRadarChartArray(i);
            series.add(new XDDFRadarChartData(radarChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfScatterChartArray(); i++) {
            CTScatterChart scatterChart = plotArea.getScatterChartArray(i);
            series.add(new XDDFScatterChartData(scatterChart, categories, values));
        }

        // TODO repeat above code for all kind of charts
        return series;
    }

    private Map<Long, XDDFChartAxis> getCategoryAxes() {
        CTPlotArea plotArea = getCTPlotArea();
        int sizeOfArray = plotArea.sizeOfCatAxArray();
        Map<Long, XDDFChartAxis> axes = new HashMap<Long, XDDFChartAxis>(sizeOfArray);
        for (int i = 0; i < sizeOfArray; i++) {
            CTCatAx category = plotArea.getCatAxArray(i);
            axes.put(category.getAxId().getVal(), new XDDFCategoryAxis(category));
        }
        return axes;
    }

    private Map<Long, XDDFValueAxis> getValueAxes() {
        CTPlotArea plotArea = getCTPlotArea();
        int sizeOfArray = plotArea.sizeOfValAxArray();
        Map<Long, XDDFValueAxis> axes = new HashMap<>(sizeOfArray);
        for (int i = 0; i < sizeOfArray; i++) {
            CTValAx values = plotArea.getValAxArray(i);
            axes.put(values.getAxId().getVal(), new XDDFValueAxis(values));
        }
        return axes;
    }

    public XDDFValueAxis createValueAxis(AxisPosition pos) {
        XDDFValueAxis valueAxis = new XDDFValueAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(valueAxis);
            valueAxis.crossAxis(axis);
        }
        axes.add(valueAxis);
        return valueAxis;
    }

    public XDDFCategoryAxis createCategoryAxis(AxisPosition pos) {
        XDDFCategoryAxis categoryAxis = new XDDFCategoryAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(categoryAxis);
            categoryAxis.crossAxis(axis);
        }
        axes.add(categoryAxis);
        return categoryAxis;
    }

    public XDDFDateAxis createDateAxis(AxisPosition pos) {
        XDDFDateAxis dateAxis = new XDDFDateAxis(chart.getPlotArea(), pos);
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(dateAxis);
            dateAxis.crossAxis(axis);
        }
        axes.add(dateAxis);
        return dateAxis;
    }

    public XDDFChartData createData(ChartTypes type, XDDFChartAxis category, XDDFValueAxis values) {
        Map<Long, XDDFChartAxis> categories = Collections.singletonMap(category.getId(), category);
        Map<Long, XDDFValueAxis> mapValues = Collections.singletonMap(values.getId(), values);
        final CTPlotArea plotArea = getCTPlotArea();
        switch (type) {
        case BAR:
            return new XDDFBarChartData(plotArea.addNewBarChart(), categories, mapValues);
        case LINE:
            return new XDDFLineChartData(plotArea.addNewLineChart(), categories, mapValues);
        case PIE:
            return new XDDFPieChartData(plotArea.addNewPieChart());
        case RADAR:
            return new XDDFRadarChartData(plotArea.addNewRadarChart(), categories, mapValues);
        case SCATTER:
            return new XDDFScatterChartData(plotArea.addNewScatterChart(), categories, mapValues);
        default:
            return null;
        }
    }

    public List<? extends XDDFChartAxis> getAxes() {
        if (axes.isEmpty() && hasAxes()) {
            parseAxes();
        }
        return axes;
    }

    private boolean hasAxes() {
        CTPlotArea ctPlotArea = chart.getPlotArea();
        int totalAxisCount = ctPlotArea.sizeOfValAxArray() + ctPlotArea.sizeOfCatAxArray() + ctPlotArea.sizeOfDateAxArray() + ctPlotArea.sizeOfSerAxArray();
        return totalAxisCount > 0;
    }

    private void parseAxes() {
        // TODO: add other axis types
        for (CTCatAx catAx : chart.getPlotArea().getCatAxArray()) {
            axes.add(new XDDFCategoryAxis(catAx));
        }
        for (CTDateAx dateAx : chart.getPlotArea().getDateAxArray()) {
            axes.add(new XDDFDateAxis(dateAx));
        }
        for (CTValAx valAx : chart.getPlotArea().getValAxArray()) {
            axes.add(new XDDFValueAxis(valAx));
        }
    }

}
