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

package org.apache.poi.xslf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Beta;
import org.apache.poi.xddf.usermodel.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.XDDFChart;
import org.apache.poi.xddf.usermodel.XDDFChartAxis;
import org.apache.poi.xddf.usermodel.XDDFChartData;
import org.apache.poi.xddf.usermodel.XDDFDataSource;
import org.apache.poi.xddf.usermodel.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.XDDFPieChartData;
import org.apache.poi.xddf.usermodel.XDDFRadarChartData;
import org.apache.poi.xddf.usermodel.XDDFScatterChartData;
import org.apache.poi.xddf.usermodel.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;

/**
 * Represents a Chart in a .pptx presentation
 *
 *
 */
@Beta
public final class XSLFChart extends XDDFChart {
    protected static final POIXMLRelation WORKBOOK_RELATIONSHIP = new POIXMLRelation(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            POIXMLDocument.PACK_OBJECT_REL_TYPE,
            "/ppt/embeddings/Microsoft_Excel_Worksheet#.xlsx",
            XSSFWorkbook.class
    ){};


    /**
     * Underlying workbook
     */
    private XSSFWorkbook workbook;


    /**
     * Construct a PresentationML chart.
     */
    protected XSLFChart() {
        super();
    }

    /**
     * Construct a PresentationML chart from a package part.
     *
     * @param part the package part holding the chart data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     *
     * @since POI 3.14-Beta1
     */
    protected XSLFChart(PackagePart part) throws IOException, XmlException {
        super(part);
    }

    public XSLFTextShape getTitle() {
        if (!chart.isSetTitle()) {
            chart.addNewTitle();
        }
        final CTTitle title = chart.getTitle();
        if (title.getTx() != null && title.getTx().isSetRich()) {
            return new XSLFTextShape(title, null) {
                @Override
                protected CTTextBody getTextBody(boolean create) {
                    return title.getTx().getRich();
                }
            };
        } else {
            return new XSLFTextShape(title, null) {
                @Override
                protected CTTextBody getTextBody(boolean create) {
                    return title.getTxPr();
                }
            };
        }
    }

    public CellReference setSheetTitle(String title) {
        XSSFSheet sheet = getSheet();
        sheet.createRow(0).createCell(1).setCellValue(title);
        return new CellReference(sheet.getSheetName(), 0, 1, true, true);
    }

    public String formatRange(CellRangeAddress range) {
        return range.formatAsString(getSheet().getSheetName(), true);
    }

    private XSSFSheet getSheet() {
        XSSFSheet sheet = null;
        try {
            sheet = getWorkbook().getSheetAt(0);
        } catch (InvalidFormatException ife) {
        } catch (IOException ioe) {
        }
        return sheet;
    }

    private PackagePart getWorksheetPart() throws InvalidFormatException {
        for (RelationPart part : getRelationParts()) {
            if (WORKBOOK_RELATIONSHIP.getRelation().equals(part.getRelationship().getRelationshipType())) {
                return getTargetPart(part.getRelationship());
            }
        }
        return null;
    }

    protected XSSFWorkbook getWorkbook() throws IOException, InvalidFormatException {
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

    private XMLSlideShow getSlideShow() {
        POIXMLDocumentPart p = getParent();
        while(p != null) {
            if(p instanceof XMLSlideShow){
                return (XMLSlideShow)p;
            }
            p = p.getParent();
        }
        throw new IllegalStateException("SlideShow was not found");
    }

    private PackagePart createWorksheetPart() throws InvalidFormatException {
        Integer chartIdx = XSLFRelation.CHART.getFileNameIndex(this);
        return getTargetPart(getSlideShow().createWorkbookRelationship(this, chartIdx));
    }

    protected void saveWorkbook(XSSFWorkbook workbook) throws IOException, InvalidFormatException {
        PackagePart worksheetPart = getWorksheetPart();
        if (worksheetPart == null) {
            worksheetPart = createWorksheetPart();
        }
        OutputStream xlsOut = worksheetPart.getOutputStream();
        try {
            workbook.write(xlsOut);
        } finally {
            xlsOut.close();
        }
    }

    private void fillSheet(XSSFSheet sheet, XDDFDataSource<?> categoryData, XDDFNumericalDataSource<?> valuesData) {
        int numOfPoints = categoryData.getPointCount();
        for (int i = 0; i < numOfPoints; i++) {
            XSSFRow row = sheet.createRow(i + 1); // first row is for title
            row.createCell(0).setCellValue(categoryData.getPointAt(i).toString());
            row.createCell(1).setCellValue(valuesData.getPointAt(i).doubleValue());
        }
    }

    @Override
    public void plot(XDDFChartData data) {
        super.plot(data);
        XSSFSheet sheet = getSheet();
        for(XDDFChartData.Series series : data.getSeries()) {
            fillSheet(sheet, series.getCategoryData(), series.getValuesData());
        }
    }

    public List<XDDFChartData> getChartSeries() {
        List<XDDFChartData> series = new LinkedList<XDDFChartData>();
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

    public void importContent(XSLFChart other) {
        this.chart.set(other.chart);
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
        Map<Long, XDDFValueAxis> axes = new HashMap<Long, XDDFValueAxis>(sizeOfArray);
        for (int i = 0; i < sizeOfArray; i++) {
            CTValAx values = plotArea.getValAxArray(i);
            axes.put(values.getAxId().getVal(), new XDDFValueAxis(values));
        }
        return axes;
    }

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
        OutputStream out = part.getOutputStream();
        chartSpace.save(out, xmlOptions);
        out.close();
    }
}
