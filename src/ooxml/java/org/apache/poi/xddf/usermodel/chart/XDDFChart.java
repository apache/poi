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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLFactory;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
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

    /**
     * Underlying workbook
     */
    private XSSFWorkbook workbook;

    XSSFSheet sheet = null;
    
    private int chartIndex = 0;

    POIXMLDocumentPart documentPart = null;
    
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
     *             the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
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
     * false otherwise
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
     *             present on the chart
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
        XSSFSheet sheet = getSheet();
        for (XDDFChartData.Series series : data.getSeries()) {
            series.plot();
            fillSheet(sheet, series.getCategoryData(), series.getValuesData());
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

    /**
     * method to create relationship with embedded part
     * for example writing xlsx file stream into output stream
     *
     * @param chartRelation relationship object
     * @param chartFactory  ChartFactory object
     * @param chartIndex    index used to suffix on file
     * @return return relation part which used to write relation in .rels file and get relation id
     * @since POI 4.0.0
     */
    public PackageRelationship createRelationshipInChart(POIXMLRelation chartRelation, POIXMLFactory chartFactory, int chartIndex) {
        documentPart = createRelationship(chartRelation, chartFactory, chartIndex, true).getDocumentPart();
        return this.addRelation(null, chartRelation, documentPart).getRelationship();
    }

    /**
     * if embedded part was null then create new part
     *
     * @param chartRelation         chart relation object
     * @param chartWorkbookRelation chart workbook relation object
     * @param chartFactory          factory object of POIXMLFactory (XWPFFactory/XSLFFactory)
     * @return return the new package part
     * @throws InvalidFormatException
     * @since POI 4.0.0
     */
    private PackagePart createWorksheetPart(POIXMLRelation chartRelation, POIXMLRelation chartWorkbookRelation, POIXMLFactory chartFactory) throws InvalidFormatException {
        PackageRelationship xlsx = createRelationshipInChart(chartWorkbookRelation, chartFactory, chartIndex);
        this.setExternalId(xlsx.getId());
        return getTargetPart(xlsx);
    }

    /**
     * this method write the XSSFWorkbook object data into embedded excel file
     *
     * @param workbook XSSFworkbook object
     * @throws IOException
     * @throws InvalidFormatException
     * @since POI 4.0.0
     */
    public void saveWorkbook(XSSFWorkbook workbook) throws IOException, InvalidFormatException {
        PackagePart worksheetPart = getWorksheetPart();
        if (worksheetPart == null) {
            POIXMLRelation chartRelation = getChartRelation();
            POIXMLRelation chartWorkbookRelation = getChartWorkbookRelation();
            POIXMLFactory chartFactory = getChartFactory();
            if (chartRelation != null
                && chartWorkbookRelation != null
                && chartFactory != null) {
                worksheetPart = createWorksheetPart(chartRelation, chartWorkbookRelation, chartFactory);
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
     * @param sheet        sheet of embedded excel
     * @param categoryData category values
     * @param valuesData   data values
     * @since POI 4.0.0
     */
    protected void fillSheet(XSSFSheet sheet, XDDFDataSource<?> categoryData, XDDFNumericalDataSource<?> valuesData) {
        int numOfPoints = categoryData.getPointCount();
        for (int i = 0; i < numOfPoints; i++) {
            XSSFRow row = this.getRow(sheet,i + 1); // first row is for title
            this.getCell(row,categoryData.getColIndex()).setCellValue(categoryData.getPointAt(i).toString());
            this.getCell(row,valuesData.getColIndex()).setCellValue(valuesData.getPointAt(i).doubleValue());
        }
    }

    /**
     * this method return row on given index
     * if row is null then create new row
     *
     * @param sheet current sheet object
     * @param index index of current row
     * @return this method return sheet row on given index
     * @since POI 4.0.0
     */
    private XSSFRow getRow(XSSFSheet sheet,int index)
    {
        if(sheet.getRow(index)!=null)
            return sheet.getRow(index);
        else
            return sheet.createRow(index);
    }
    
    /**
     * this method return cell on given index
     * if cell is null then create new cell
     *
     * @param row current row object
     * @param index index of current cell
     * @return this method return sheet cell on given index
     * @since POI 4.0.0
     */
    private XSSFCell getCell(XSSFRow row,int index)
    {
        if(row.getCell(index)!=null)
            return row.getCell(index);
        else
            return row.createCell(index);
    }
    
    /**
     * import content from other chart to created chart
     *
     * @param other chart object
     * @since POI 4.0.0
     */
    public void importContent(XDDFChart other) {
        this.chart.set(other.chart);
    }

    /**
     * save chart xml
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

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
     * set sheet time in excel file
     *
     * @param title title of sheet
     * @return return cell reference
     * @since POI 4.0.0
     */
    public CellReference setSheetTitle(String title) {
        XSSFSheet sheet = getSheet();
        sheet.createRow(0).createCell(1).setCellValue(title);
        return new CellReference(sheet.getSheetName(), 0, 1, true, true);
    }

    /**
     * @param range
     * @return
     * @since POI 4.0.0
     */
    public String formatRange(CellRangeAddress range) {
        return range.formatAsString(getSheet().getSheetName(), true);
    }

    /**
     * get sheet object of embedded excel file
     *
     * @return excel sheet object
     * @since POI 4.0.0
     */
    private XSSFSheet getSheet() {
        if(sheet==null)
        {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet();
        }
        return sheet;
    }

    /**
     * this method is used to get worksheet part
     * if call is from saveworkbook method then check isCommitted
     * isCommitted variable shows that we are writing xssfworkbook object into output stream of embedded part
     *
     * @param isCommitted if it's true then it shows that we are writing xssfworkbook object into output stream of embedded part
     * @return returns the packagepart of embedded file
     * @throws InvalidFormatException
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

    private void setWorksheetPartCommitted() throws InvalidFormatException {
        for (RelationPart part : getRelationParts()) {
            if (POIXMLDocument.PACK_OBJECT_REL_TYPE.equals(part.getRelationship().getRelationshipType())) {
                part.getDocumentPart().setCommited(true);
                break;
            }
        }
    }
    
    /**
     * @return returns the workbook object of embedded excel file
     * @throws IOException
     * @throws InvalidFormatException
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
                    workbook = new XSSFWorkbook(worksheetPart.getInputStream());
                }
            } catch (NotOfficeXmlFileException e) {
                workbook = new XSSFWorkbook();
                workbook.createSheet();
            }
        }
        return workbook;
    }

    /**
     * while reading chart from template file then we need to parse and store embedded excel
     * file in chart object show that we can modify value according to use
     *
     * @param workbook workbook object which we read from chart embedded part
     * @since POI 4.0.0
     */
    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    /**
     * set the relation id of embedded excel relation id into external data relation tag
     *
     * @param id relation id of embedded excel relation id into external data relation tag
     * @since POI 4.0.0
     */
    public void setExternalId(String id) {
        getCTChartSpace().addNewExternalData().setId(id);
    }

    /**
     * @return method return chart index
     * @since POI 4.0.0
     */
    protected int getChartIndex() {
        return chartIndex;
    }

    /**
     * set chart index which can be use for relation part
     *
     * @param chartIndex chart index which can be use for relation part
     */
    public void setChartIndex(int chartIndex) {
        this.chartIndex = chartIndex;
    }
}
