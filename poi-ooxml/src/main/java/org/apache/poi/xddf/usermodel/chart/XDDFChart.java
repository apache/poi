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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.text.TextContainer;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTArea3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDateAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTExternalData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLine3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurfaceChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTView3D;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

@Beta
public abstract class XDDFChart extends POIXMLDocumentPart implements TextContainer {

    /**
     * default width of chart in emu
     */
    public static final int DEFAULT_WIDTH = 500000;

    /**
     * default height of chart in emu
     */
    public static final int DEFAULT_HEIGHT = 500000;

    /**
     * default x-coordinate  of chart in emu
     */
    public static final int DEFAULT_X = 10;

    /**
     * default y-coordinate value of chart in emu
     */
    public static final int DEFAULT_Y = 10;

    /**
     * Underlying workbook
     */
    private XSSFWorkbook workbook;

    private int chartIndex = 0;

    protected List<XDDFChartAxis> axes = new ArrayList<>();

    /**
     * Root element of the Chart part
     */
    protected final CTChartSpace chartSpace;


    /**
     * Construct a chart.
     */
    protected XDDFChart() {
        super();

        chartSpace = CTChartSpace.Factory.newInstance();
        chartSpace.addNewChart().addNewPlotArea();
    }

    /**
     * Construct a DrawingML chart from a package part.
     *
     * @param part
     *            the package part holding the chart data, the content type must
     *            be
     *            {@code application/vnd.openxmlformats-officedocument.drawingml.chart+xml}
     * @since POI 3.14-Beta1
     */
    protected XDDFChart(PackagePart part) throws IOException, XmlException {
        super(part);

        try (InputStream stream = part.getInputStream()) {
            chartSpace = ChartSpaceDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS).getChartSpace();
        }
    }

    /**
     * Return the underlying CTChartSpace bean, the root element of the Chart
     * part.
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
        return chartSpace.getChart();
    }

    /**
     * Return the underlying CTPlotArea bean, within the Chart
     *
     * @return the underlying CTPlotArea bean
     */
    @Internal
    protected CTPlotArea getCTPlotArea() {
        return getCTChart().getPlotArea();
    }

    /**
     * Clear all properties, as if a new instance had just been created.
     * @since POI 4.1.2
     */
    public void clear() {
        axes.clear();
        seriesCount = 0;
        if (workbook != null) {
            workbook.removeSheetAt(0);
            workbook.createSheet();
        }
        getCTChart().set(CTChart.Factory.newInstance());
        getCTChart().addNewPlotArea();
    }

    /**
     * @return true if only visible cells will be present on the chart, false
     *         otherwise
     */
    public boolean isPlotOnlyVisibleCells() {
        if (getCTChart().isSetPlotVisOnly()) {
            return getCTChart().getPlotVisOnly().getVal();
        } else {
            return false;
        }
    }

    /**
     * @param only
     *            a flag specifying if only visible cells should be present on
     *            the chart
     */
    public void setPlotOnlyVisibleCells(boolean only) {
        if (!getCTChart().isSetPlotVisOnly()) {
            getCTChart().setPlotVisOnly(CTBoolean.Factory.newInstance());
        }
        getCTChart().getPlotVisOnly().setVal(only);
    }

    public void setFloor(int thickness) {
        if (!getCTChart().isSetFloor()) {
            getCTChart().setFloor(CTSurface.Factory.newInstance());
        }
        getCTChart().getFloor().getThickness().setVal(thickness);
    }

    public void setBackWall(int thickness) {
        if (!getCTChart().isSetBackWall()) {
            getCTChart().setBackWall(CTSurface.Factory.newInstance());
        }
        getCTChart().getBackWall().getThickness().setVal(thickness);
    }

    public void setSideWall(int thickness) {
        if (!getCTChart().isSetSideWall()) {
            getCTChart().setSideWall(CTSurface.Factory.newInstance());
        }
        getCTChart().getSideWall().getThickness().setVal(thickness);
    }

    public void setAutoTitleDeleted(boolean deleted) {
        if (!getCTChart().isSetAutoTitleDeleted()) {
            getCTChart().setAutoTitleDeleted(CTBoolean.Factory.newInstance());
        }
        getCTChart().getAutoTitleDeleted().setVal(deleted);
        if (deleted && getCTChart().isSetTitle()) {
            getCTChart().unsetTitle();
        }
    }

    /**
     * @since 4.0.1
     *
     */
    public void displayBlanksAs(DisplayBlanks as) {
        if (as == null){
            if (getCTChart().isSetDispBlanksAs()) {
                getCTChart().unsetDispBlanksAs();
            }
        } else {
            if (getCTChart().isSetDispBlanksAs()) {
              getCTChart().getDispBlanksAs().setVal(as.underlying);
            } else {
                getCTChart().addNewDispBlanksAs().setVal(as.underlying);
            }
        }
    }

    /**
     * @since 4.0.1
     */
    public Boolean getTitleOverlay() {
        if (getCTChart().isSetTitle()) {
            CTTitle title = getCTChart().getTitle();
            if (title.isSetOverlay()) {
                return title.getOverlay().getVal();
            }
        }
        return null;
    }

    /**
     * @since 4.0.1
     */
    public void setTitleOverlay(boolean overlay) {
        if (!getCTChart().isSetTitle()) {
            getCTChart().addNewTitle();
        }
        new XDDFTitle(this, getCTChart().getTitle()).setOverlay(overlay);
    }

    /**
     * Sets the title text as a static string.
     *
     * @param text
     *            to use as new title
     * @since 4.0.1
     */
    public void setTitleText(String text) {
        if (!getCTChart().isSetTitle()) {
            getCTChart().addNewTitle();
        }
        new XDDFTitle(this, getCTChart().getTitle()).setText(text);
    }

    /**
     * @since 4.0.1
     */
    public XDDFTitle getTitle() {
        if (getCTChart().isSetTitle()) {
            return new XDDFTitle(this, getCTChart().getTitle());
        } else {
            return null;
        }
    }

    /**
     * Remove the chart title.
     * @since POI 5.0.0
     */
    public void removeTitle() {
        setAutoTitleDeleted(true);
    }

    /**
    * Get or Add chart 3D view into chart
    *
    * @return this method will add 3D view
    */
   public XDDFView3D getOrAddView3D() {
      CTView3D view3D;
      if (getCTChart().isSetView3D()) {
         view3D = getCTChart().getView3D();
      } else {
         view3D = getCTChart().addNewView3D();
      }
      return new XDDFView3D(view3D);
   }

    /**
     * Get the chart title body if there is one, i.e. title is set and is not a
     * formula.
     *
     * @return text body or null, if title is a formula or no title is set.
     */
    @Beta
    public XDDFTextBody getFormattedTitle() {
        if (!getCTChart().isSetTitle()) {
            return null;
        }
        return new XDDFTitle(this, getCTChart().getTitle()).getBody();
    }

    @Override
    public <R> Optional<R> findDefinedParagraphProperty(Predicate<CTTextParagraphProperties> isSet,
        Function<CTTextParagraphProperties, R> getter) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public <R> Optional<R> findDefinedRunProperty(Predicate<CTTextCharacterProperties> isSet,
        Function<CTTextCharacterProperties, R> getter) {
        // TODO Auto-generated method stub
        return Optional.empty();
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
        return new XDDFChartLegend(getCTChart());
    }

    public void deleteLegend() {
        if (getCTChart().isSetLegend()) {
            getCTChart().unsetLegend();
        }
    }

    public XDDFManualLayout getOrAddManualLayout() {
        return new XDDFManualLayout(getCTPlotArea());
    }

    private long seriesCount = 0;
    protected long incrementSeriesCount() {
        return seriesCount++;
    }

    public void plot(XDDFChartData data) {
        XSSFSheet sheet = getSheet();
        for (int idx = 0; idx < data.getSeriesCount(); idx++) {
            XDDFChartData.Series series = data.getSeries(idx);
            series.plot();
            XDDFDataSource<?> categoryDS = series.getCategoryData();
            XDDFNumericalDataSource<? extends Number> valuesDS = series.getValuesData();
            if (categoryDS != null && !categoryDS.isCellRange() && !categoryDS.isLiteral() &&
                valuesDS != null && !valuesDS.isCellRange() && !valuesDS.isLiteral()) {
                fillSheet(sheet, categoryDS, valuesDS);
            }
            // otherwise let's assume the data is already in the sheet
        }
    }

    public List<XDDFChartData> getChartSeries() {
        List<XDDFChartData> series = new LinkedList<>();
        CTPlotArea plotArea = getCTPlotArea();
        Map<Long, XDDFChartAxis> categories = getCategoryAxes();
        Map<Long, XDDFValueAxis> values = getValueAxes();

        for (int i = 0; i < plotArea.sizeOfAreaChartArray(); i++) {
            CTAreaChart areaChart = plotArea.getAreaChartArray(i);
            series.add(new XDDFAreaChartData(this, areaChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfArea3DChartArray(); i++) {
            CTArea3DChart areaChart = plotArea.getArea3DChartArray(i);
            series.add(new XDDFArea3DChartData(this, areaChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfBarChartArray(); i++) {
            CTBarChart barChart = plotArea.getBarChartArray(i);
            series.add(new XDDFBarChartData(this, barChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfBar3DChartArray(); i++) {
            CTBar3DChart barChart = plotArea.getBar3DChartArray(i);
            series.add(new XDDFBar3DChartData(this, barChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfDoughnutChartArray(); i++) {
            CTDoughnutChart doughnutChart = plotArea.getDoughnutChartArray(i);
            series.add(new XDDFDoughnutChartData(this, doughnutChart));
        }

        for (int i = 0; i < plotArea.sizeOfLineChartArray(); i++) {
            CTLineChart lineChart = plotArea.getLineChartArray(i);
            series.add(new XDDFLineChartData(this, lineChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfLine3DChartArray(); i++) {
            CTLine3DChart lineChart = plotArea.getLine3DChartArray(i);
            series.add(new XDDFLine3DChartData(this, lineChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfPieChartArray(); i++) {
            CTPieChart pieChart = plotArea.getPieChartArray(i);
            series.add(new XDDFPieChartData(this, pieChart));
        }

        for (int i = 0; i < plotArea.sizeOfPie3DChartArray(); i++) {
            CTPie3DChart pieChart = plotArea.getPie3DChartArray(i);
            series.add(new XDDFPie3DChartData(this, pieChart));
        }

        for (int i = 0; i < plotArea.sizeOfRadarChartArray(); i++) {
            CTRadarChart radarChart = plotArea.getRadarChartArray(i);
            series.add(new XDDFRadarChartData(this, radarChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfScatterChartArray(); i++) {
            CTScatterChart scatterChart = plotArea.getScatterChartArray(i);
            series.add(new XDDFScatterChartData(this, scatterChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfSurfaceChartArray(); i++) {
            CTSurfaceChart surfaceChart = plotArea.getSurfaceChartArray(i);
            series.add(new XDDFSurfaceChartData(this, surfaceChart, categories, values));
        }

        for (int i = 0; i < plotArea.sizeOfSurface3DChartArray(); i++) {
            CTSurface3DChart surfaceChart = plotArea.getSurface3DChartArray(i);
            series.add(new XDDFSurface3DChartData(this, surfaceChart, categories, values));
        }
        // TODO repeat above code for missing charts: Bubble, OfPie and Stock

        seriesCount = series.size();
        return series;
    }

    /**
     * Clear all chart series, as if a new instance had just been created.
     * @since POI 4.1.2
     */
    public void clearChartSeries() {
        CTPlotArea plotArea = getCTPlotArea();

        for (int i = plotArea.sizeOfAreaChartArray(); i > 0; i--) {
            plotArea.removeAreaChart(i - 1);
        }

        for (int i = plotArea.sizeOfArea3DChartArray(); i > 0; i--) {
            plotArea.removeArea3DChart(i - 1);
        }

        for (int i = plotArea.sizeOfBarChartArray(); i > 0; i--) {
            plotArea.removeBarChart(i - 1);
        }

        for (int i = plotArea.sizeOfBar3DChartArray(); i > 0; i--) {
            plotArea.removeBar3DChart(i - 1);
        }

        for (int i = plotArea.sizeOfBubbleChartArray(); i > 0; i--) {
            plotArea.removeBubbleChart(i - 1);
        }

        for (int i = plotArea.sizeOfDoughnutChartArray(); i > 0; i--) {
            plotArea.removeDoughnutChart(i - 1);
        }

        for (int i = plotArea.sizeOfLineChartArray(); i > 0; i--) {
            plotArea.removeLineChart(i - 1);
        }

        for (int i = plotArea.sizeOfLine3DChartArray(); i > 0; i--) {
            plotArea.removeLine3DChart(i - 1);
        }

        for (int i = plotArea.sizeOfOfPieChartArray(); i > 0; i--) {
            plotArea.removeOfPieChart(i - 1);
        }

        for (int i = plotArea.sizeOfPieChartArray(); i > 0; i--) {
            plotArea.removePieChart(i - 1);
        }

        for (int i = plotArea.sizeOfPie3DChartArray(); i > 0; i--) {
            plotArea.removePie3DChart(i - 1);
        }

        for (int i = plotArea.sizeOfRadarChartArray(); i > 0; i--) {
            plotArea.removeRadarChart(i - 1);
        }

        for (int i = plotArea.sizeOfScatterChartArray(); i > 0; i--) {
            plotArea.removeScatterChart(i - 1);
        }

        for (int i = plotArea.sizeOfStockChartArray(); i > 0; i--) {
            plotArea.removeStockChart(i - 1);
        }

        for (int i = plotArea.sizeOfSurfaceChartArray(); i > 0; i--) {
            plotArea.removeSurfaceChart(i - 1);
        }

        for (int i = plotArea.sizeOfSurface3DChartArray(); i > 0; i--) {
            plotArea.removeSurface3DChart(i - 1);
        }
    }

    private Map<Long, XDDFChartAxis> getCategoryAxes() {
        CTPlotArea plotArea = getCTPlotArea();
        int sizeOfArray = plotArea.sizeOfCatAxArray();
        Map<Long, XDDFChartAxis> axesMap = new HashMap<>(sizeOfArray);
        for (int i = 0; i < sizeOfArray; i++) {
            CTCatAx category = plotArea.getCatAxArray(i);
            axesMap.put(category.getAxId().getVal(), new XDDFCategoryAxis(category));
        }
        return axesMap;
    }

    private Map<Long, XDDFValueAxis> getValueAxes() {
        CTPlotArea plotArea = getCTPlotArea();
        int sizeOfArray = plotArea.sizeOfValAxArray();
        Map<Long, XDDFValueAxis> axesMap = new HashMap<>(sizeOfArray);
        for (int i = 0; i < sizeOfArray; i++) {
            CTValAx values = plotArea.getValAxArray(i);
            axesMap.put(values.getAxId().getVal(), new XDDFValueAxis(values));
        }
        return axesMap;
    }

    public XDDFValueAxis createValueAxis(AxisPosition pos) {
        XDDFValueAxis valueAxis = new XDDFValueAxis(getCTPlotArea(), pos);
        addAxis(valueAxis);
        return valueAxis;
    }

    /**
     * this method will return series axis with specified position
     *
     * @param pos axis position Left, Right, Top, Bottom
     * @return series axis with specified position
     */
    public XDDFSeriesAxis createSeriesAxis(AxisPosition pos) {
        XDDFSeriesAxis seriesAxis = new XDDFSeriesAxis(getCTPlotArea(), pos);
        addAxis(seriesAxis);
        return seriesAxis;
    }

    public XDDFCategoryAxis createCategoryAxis(AxisPosition pos) {
        XDDFCategoryAxis categoryAxis = new XDDFCategoryAxis(getCTPlotArea(), pos);
        addAxis(categoryAxis);
        return categoryAxis;
    }

    public XDDFDateAxis createDateAxis(AxisPosition pos) {
        XDDFDateAxis dateAxis = new XDDFDateAxis(getCTPlotArea(), pos);
        addAxis(dateAxis);
        return dateAxis;
    }

    private void addAxis(XDDFChartAxis newAxis) {
        if (axes.size() == 1) {
            XDDFChartAxis axis = axes.get(0);
            axis.crossAxis(newAxis);
            newAxis.crossAxis(axis);
            axis.setCrosses(AxisCrosses.AUTO_ZERO);
            newAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        }
        axes.add(newAxis);
    }

    /**
     * this method will return specified chart data with category and series values
     *
     * @param type chart type
     * @param category category values of chart
     * @param values series values of chart
     * @return specified chart data.
     */
    public XDDFChartData createData(ChartTypes type, XDDFChartAxis category, XDDFValueAxis values) {
        Map<Long, XDDFChartAxis> categories = null;
        Map<Long, XDDFValueAxis> mapValues = null;

        if (ChartTypes.PIE != type && ChartTypes.PIE3D != type && ChartTypes.DOUGHNUT != type) {
            categories = Collections.singletonMap(category.getId(), category);
            mapValues = Collections.singletonMap(values.getId(), values);
        }

        final CTPlotArea plotArea = getCTPlotArea();
        switch (type) {
        case AREA:
            return new XDDFAreaChartData(this, plotArea.addNewAreaChart(), categories, mapValues);
        case AREA3D:
            return new XDDFArea3DChartData(this, plotArea.addNewArea3DChart(), categories, mapValues);
        case BAR:
            return new XDDFBarChartData(this, plotArea.addNewBarChart(), categories, mapValues);
        case BAR3D:
            return new XDDFBar3DChartData(this, plotArea.addNewBar3DChart(), categories, mapValues);
        case DOUGHNUT:
            return new XDDFDoughnutChartData(this, plotArea.addNewDoughnutChart());
        case LINE:
            return new XDDFLineChartData(this, plotArea.addNewLineChart(), categories, mapValues);
        case LINE3D:
            return new XDDFLine3DChartData(this, plotArea.addNewLine3DChart(), categories, mapValues);
        case PIE:
            return new XDDFPieChartData(this, plotArea.addNewPieChart());
        case PIE3D:
            return new XDDFPie3DChartData(this, plotArea.addNewPie3DChart());
        case RADAR:
            return new XDDFRadarChartData(this, plotArea.addNewRadarChart(), categories, mapValues);
        case SCATTER:
            return new XDDFScatterChartData(this, plotArea.addNewScatterChart(), categories, mapValues);
        case SURFACE:
            return new XDDFSurfaceChartData(this, plotArea.addNewSurfaceChart(), categories, mapValues);
        case SURFACE3D:
            return new XDDFSurface3DChartData(this, plotArea.addNewSurface3DChart(), categories, mapValues);
        // TODO repeat above code for missing charts: Bubble, OfPie and Stock
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
        CTPlotArea ctPlotArea = getCTPlotArea();
        int totalAxisCount = ctPlotArea.sizeOfValAxArray() + ctPlotArea.sizeOfCatAxArray() + ctPlotArea
            .sizeOfDateAxArray() + ctPlotArea.sizeOfSerAxArray();
        return totalAxisCount > 0;
    }

    private void parseAxes() {
        for (CTCatAx catAx : getCTPlotArea().getCatAxArray()) {
            axes.add(new XDDFCategoryAxis(catAx));
        }
        for (CTDateAx dateAx : getCTPlotArea().getDateAxArray()) {
            axes.add(new XDDFDateAxis(dateAx));
        }
        for (CTSerAx serAx : getCTPlotArea().getSerAxArray()) {
            axes.add(new XDDFSeriesAxis(serAx));
        }
        for (CTValAx valAx : getCTPlotArea().getValAxArray()) {
            axes.add(new XDDFValueAxis(valAx));
        }
    }

    /**
     * Set value range (basic Axis Options)
     *
     * @param axisIndex
     *            0 - primary axis, 1 - secondary axis
     * @param minimum
     *            minimum value; Double.NaN - automatic; null - no change
     * @param maximum
     *            maximum value; Double.NaN - automatic; null - no change
     * @param majorUnit
     *            major unit value; Double.NaN - automatic; null - no change
     * @param minorUnit
     *            minor unit value; Double.NaN - automatic; null - no change
     */
    public void setValueRange(int axisIndex, Double minimum, Double maximum, Double majorUnit, Double minorUnit) {
        XDDFChartAxis axis = getAxes().get(axisIndex);
        if (axis == null) {
            return;
        }
        if (minimum != null) {
            axis.setMinimum(minimum);
        }
        if (maximum != null) {
            axis.setMaximum(maximum);
        }
        if (majorUnit != null) {
            axis.setMajorUnit(majorUnit);
        }
        if (minorUnit != null) {
            axis.setMinorUnit(minorUnit);
        }
    }

    /**
     * method to create relationship with embedded part for example writing xlsx
     * file stream into output stream
     *
     * @param chartRelation
     *            relationship object
     * @param chartFactory
     *            ChartFactory object
     * @param chartIndex
     *            index used to suffix on file
     * @return return relation part which used to write relation in .rels file
     *         and get relation id
     * @since POI 4.0.0
     */
    public PackageRelationship createRelationshipInChart(POIXMLRelation chartRelation, POIXMLFactory chartFactory,
        int chartIndex) {
        POIXMLDocumentPart documentPart =
                createRelationship(chartRelation, chartFactory, chartIndex, true).getDocumentPart();
        return addRelation(null, chartRelation, documentPart).getRelationship();
    }

    /**
     * if embedded part was null then create new part
     *
     * @param chartWorkbookRelation
     *            chart workbook relation object
     * @param chartFactory
     *            factory object of POIXMLFactory (XWPFFactory/XSLFFactory)
     * @return return the new package part
     * @since POI 4.0.0
     */
    private PackagePart createWorksheetPart(POIXMLRelation chartWorkbookRelation, POIXMLFactory chartFactory)
            throws InvalidFormatException {
        PackageRelationship xlsx = createRelationshipInChart(chartWorkbookRelation, chartFactory, chartIndex);
        setExternalId(xlsx.getId());
        return getTargetPart(xlsx);
    }

    /**
     * this method write the XSSFWorkbook object data into embedded excel file
     *
     * @param workbook XSSFworkbook object
     * @since POI 4.0.0
     */
    public void saveWorkbook(XSSFWorkbook workbook) throws IOException, InvalidFormatException {
        PackagePart worksheetPart = getWorksheetPart();
        if (worksheetPart == null) {
            POIXMLRelation chartWorkbookRelation = getChartWorkbookRelation();
            POIXMLFactory chartFactory = getChartFactory();
            if (chartWorkbookRelation != null && chartFactory != null) {
                worksheetPart = createWorksheetPart(chartWorkbookRelation, chartFactory);
            } else {
                throw new InvalidFormatException("unable to determine chart relations");
            }
        }
        try (OutputStream xlsOut = worksheetPart.getOutputStream()) {
            setWorksheetPartCommitted();
            workbook.write(xlsOut);
        }
    }

    /**
     *
     * @return the chart relation in the implementing subclass.
     * @since POI 4.0.0
     */
    protected abstract POIXMLRelation getChartRelation();

    /**
     *
     * @return the chart workbook relation in the implementing subclass.
     * @since POI 4.0.0
     */
    protected abstract POIXMLRelation getChartWorkbookRelation();

    /**
     *
     * @return the chart factory in the implementing subclass.
     * @since POI 4.0.0
     */
    protected abstract POIXMLFactory getChartFactory();

    /**
     * this method writes the data into sheet
     *
     * @param sheet
     *            sheet of embedded excel
     * @param categoryData
     *            category values
     * @param valuesData
     *            data values
     * @since POI 4.0.0
     */
    protected void fillSheet(XSSFSheet sheet, XDDFDataSource<?> categoryData, XDDFNumericalDataSource<?> valuesData) {
        int numOfPoints = categoryData.getPointCount();
        for (int i = 0; i < numOfPoints; i++) {
            XSSFRow row = getRow(sheet, i + 1); // first row is for title
            Object category = categoryData.getPointAt(i);
            if (category != null) {
                getCell(row, categoryData.getColIndex()).setCellValue(category.toString());
            }
            Number value = valuesData.getPointAt(i);
            if (value != null) {
                getCell(row, valuesData.getColIndex()).setCellValue(value.doubleValue());
            }
        }
    }

    /**
     * this method return row on given index if row is null then create new row
     *
     * @param sheet
     *            current sheet object
     * @param index
     *            index of current row
     * @return this method return sheet row on given index
     * @since POI 4.0.0
     */
    private XSSFRow getRow(XSSFSheet sheet, int index) {
        XSSFRow row = sheet.getRow(index);
        if (row == null) {
            return sheet.createRow(index);
        } else {
            return row;
        }
    }

    /**
     * this method return cell on given index if cell is null then create new
     * cell
     *
     * @param row
     *            current row object
     * @param index
     *            index of current cell
     * @return this method return sheet cell on given index
     * @since POI 4.0.0
     */
    private XSSFCell getCell(XSSFRow row, int index) {
        XSSFCell cell = row.getCell(index);
        if (cell == null) {
            return row.createCell(index);
        } else {
            return cell;
        }
    }

    /**
     * import content from other chart to created chart
     *
     * @param other
     *            chart object
     * @since POI 4.0.0
     */
    public void importContent(XDDFChart other) {
        getCTChartSpace().set(other.getCTChartSpace());
    }

    /**
     * save chart xml
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(
            new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

        if (workbook != null) {
            try {
                saveWorkbook(workbook);
            } catch (InvalidFormatException e) {
                throw new POIXMLException(e);
            }
        }

        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            chartSpace.save(out, xmlOptions);
        }
    }

    /**
     * set sheet title in excel file
     *
     * @param title
     *            title of sheet
     * @param column
     *            column index
     * @return return cell reference
     * @since POI 4.0.0
     */
    public CellReference setSheetTitle(String title, int column) {
        XSSFSheet sheet = getSheet();
        if (sheet == null) {
            return null;
        }
        XSSFRow row = getRow(sheet, 0);
        XSSFCell cell = getCell(row, column);
        cell.setCellValue(title);

        return new CellReference(sheet.getSheetName(), 0, column, true, true);
    }

    /**
     * @since POI 4.0.0
     */
    public String formatRange(CellRangeAddress range) {
        final XSSFSheet sheet = getSheet();
        return (sheet == null) ? null : range.formatAsString(sheet.getSheetName(), true);
    }

    /**
     * get sheet object of embedded excel file
     *
     * @return excel sheet object
     * @since POI 4.0.0
     */
    private XSSFSheet getSheet() {
        try {
            return getWorkbook().getSheetAt(0);
        } catch (InvalidFormatException | IOException ignored) {
            return null;
        }
    }

    /**
     * this method is used to get worksheet part if call is from saveworkbook
     * method then check isCommitted isCommitted variable shows that we are
     * writing xssfworkbook object into output stream of embedded part
     *
     * @return returns the packagepart of embedded file
     * @since POI 4.0.0
     */
    private PackagePart getWorksheetPart() throws InvalidFormatException {
        for (RelationPart part : getRelationParts()) {
            if (POIXMLDocument.PACK_OBJECT_REL_TYPE.equals(part.getRelationship().getRelationshipType())) {
                return getTargetPart(part.getRelationship());
            }
        }
        return null;
    }

    private void setWorksheetPartCommitted() {
        for (RelationPart part : getRelationParts()) {
            if (POIXMLDocument.PACK_OBJECT_REL_TYPE.equals(part.getRelationship().getRelationshipType())) {
                part.getDocumentPart().setCommitted(true);
                break;
            }
        }
    }

    /**
     * @return returns the workbook object of embedded excel file
     * @since POI 4.0.0
     */
    public XSSFWorkbook getWorkbook() throws IOException, InvalidFormatException {
        if (workbook == null) {
            try {
                PackagePart worksheetPart = getWorksheetPart();
                if (worksheetPart == null) {
                    workbook = new XSSFWorkbook();
                    workbook.createSheet();
                } else {
                    try (InputStream stream = worksheetPart.getInputStream()){
                        workbook = new XSSFWorkbook(stream);
                    }
                }
            } catch (NotOfficeXmlFileException e) {
                workbook = new XSSFWorkbook();
                workbook.createSheet();
            }
        }
        return workbook;
    }

    /**
     * while reading chart from template file then we need to parse and store
     * embedded excel file in chart object show that we can modify value
     * according to use
     *
     * @param workbook
     *            workbook object which we read from chart embedded part
     * @since POI 4.0.0
     */
    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    /**
     * set the relation id of embedded excel relation id into external data
     * relation tag
     *
     * @param id
     *            relation id of embedded excel relation id into external data
     *            relation tag
     * @since POI 4.0.0
     */
    public void setExternalId(String id) {
        CTChartSpace ctChartSpace = getCTChartSpace();
        CTExternalData externalData = ctChartSpace.isSetExternalData()
                ? ctChartSpace.getExternalData()
                : ctChartSpace.addNewExternalData();
        externalData.setId(id);
    }

    /**
     * @return method return chart index
     * @since POI 4.0.0
     */
    protected int getChartIndex() {
        return chartIndex;
    }

    /**
     * Set chart index which can be used for relation part.
     *
     * @param chartIndex
     *            chart index which can be used for relation part.
     */
    public void setChartIndex(int chartIndex) {
        this.chartIndex = chartIndex;
    }

    /**
     * Replace references to the sheet name in the data supporting the chart.
     *
     * @param newSheet
     *          sheet to be used in the data references.
     *
     * @since POI 5.1.0
     */
    public void replaceReferences(XSSFSheet newSheet) {
        for (XDDFChartData data : getChartSeries()) {
            for (XDDFChartData.Series series : data.series) {
                XDDFDataSource<?> newCategory = series.categoryData;
                XDDFNumericalDataSource<? extends Number> newValues = series.valuesData;
                try {
                    if (series.categoryData != null && series.categoryData.isReference()) {
                        String ref = series.categoryData.getDataRangeReference();
                        CellRangeAddress rangeAddress = CellRangeAddress.valueOf(ref.substring(ref.indexOf('!') + 1));
                        newCategory = series.categoryData.isNumeric()
                                ? XDDFDataSourcesFactory.fromNumericCellRange(newSheet, rangeAddress)
                                : XDDFDataSourcesFactory.fromStringCellRange(newSheet, rangeAddress);
                        if (newCategory.isNumeric()) {
                            ((XDDFNumericalDataSource<? extends Number>) newCategory).setFormatCode(series.categoryData.getFormatCode());
                        }
                    }
                    if (series.valuesData!= null && series.valuesData.isReference()) {
                        String ref = series.valuesData.getDataRangeReference();
                        CellRangeAddress rangeAddress = CellRangeAddress.valueOf(ref.substring(ref.indexOf('!') + 1));
                        newValues = XDDFDataSourcesFactory.fromNumericCellRange(newSheet, rangeAddress);
                        newValues.setFormatCode(series.valuesData.getFormatCode());
                    }
                } catch (IllegalArgumentException iae) {
                    // keep old values when cell range cannot be parsed
                }
                series.replaceData(newCategory, newValues);
                series.plot();
            }
        }
    }
}
