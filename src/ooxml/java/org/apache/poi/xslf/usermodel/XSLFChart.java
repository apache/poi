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
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.charts.XSLFBarChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFCategoryAxis;
import org.apache.poi.xslf.usermodel.charts.XSLFChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFPieChartSeries;
import org.apache.poi.xslf.usermodel.charts.XSLFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;

/**
 * Represents a Chart in a .pptx presentation
 *
 *
 */
@Beta
public final class XSLFChart extends POIXMLDocumentPart {
	protected static final POIXMLRelation WORKBOOK_RELATIONSHIP = new POIXMLRelation(
    		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    		POIXMLDocument.PACK_OBJECT_REL_TYPE,
    		"/ppt/embeddings/Microsoft_Excel_Worksheet#.xlsx",
    		XSSFWorkbook.class
    ){};


	/**
	 * Root element of the Chart part
	 */
	private final CTChartSpace chartSpace;

	/**
	 * Chart element in the chart space
	 */
	private final CTChart chart;

	/**
	 * Underlying workbook
	 */
	private XSSFWorkbook workbook;


    /**
     * Construct a chart.
     */
    protected XSLFChart() throws IOException, XmlException {
        super();

        chartSpace = CTChartSpace.Factory.newInstance();
        chart = chartSpace.addNewChart();
        chart.addNewPlotArea();
    }

    /**
     * Construct a chart from a package part.
     *
     * @param part the package part holding the chart data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     *
     * @since POI 3.14-Beta1
     */
    protected XSLFChart(PackagePart part) throws IOException, XmlException {
        super(part);

        chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS).getChartSpace();
        chart = chartSpace.getChart();
    }

	/**
	 * Return the underlying CTPlotArea bean, within the Chart Space
	 *
	 * @return the underlying CTPlotArea bean
	 */
	@Internal
	protected CTPlotArea getCTPlotArea() {
		return chart.getPlotArea();
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

	protected PackagePart getWorksheetPart() throws InvalidFormatException {
		for (RelationPart part : getRelationParts()) {
			if (WORKBOOK_RELATIONSHIP.getRelation().equals(part.getRelationship().getRelationshipType())) {
				return getTargetPart(part.getRelationship());
			}
		}
        Integer chartIdx = XSLFRelation.CHART.getFileNameIndex(this);
        POIXMLDocumentPart worksheet = getParent().createRelationship(XSLFChart.WORKBOOK_RELATIONSHIP, XSLFFactory.getInstance(), chartIdx);
        return getTargetPart(this.addRelation(null, XSLFChart.WORKBOOK_RELATIONSHIP, worksheet).getRelationship());
	}

	protected XSSFWorkbook getWorkbook() throws IOException, InvalidFormatException {
		if (workbook == null) {
			try {
				workbook = new XSSFWorkbook(getWorksheetPart().getInputStream());
			} catch (NotOfficeXmlFileException e) {
				workbook = new XSSFWorkbook();
				workbook.createSheet();
			}
		}
		return workbook;
	}

	protected void saveWorkbook(XSSFWorkbook workbook) throws IOException, InvalidFormatException {
        OutputStream xlsOut = getWorksheetPart().getOutputStream();
        try {
            workbook.write(xlsOut);
        } finally {
            xlsOut.close();
        }
	}

	public void setAutoTitleDeleted(boolean deleted) {
		if (!chart.isSetAutoTitleDeleted()) {
			chart.setAutoTitleDeleted(CTBoolean.Factory.newInstance());
		}
		chart.getAutoTitleDeleted().setVal(deleted);
	}

	public void setPlotVisualisationOnly(boolean only) {
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

	public List<XSLFChartSeries> getChartSeries() {
		List<XSLFChartSeries> series = new LinkedList<XSLFChartSeries>();
	    CTPlotArea plotArea = getCTPlotArea();
		Map<Long, XSLFCategoryAxis> categories = getCategoryAxes();
		Map<Long, XSLFValueAxis> values = getValueAxes();
		try {
	        XSSFSheet sheet = getWorkbook().getSheetAt(0);

	        for (int i = 0; i < plotArea.sizeOfBarChartArray(); i++) {
		        CTBarChart barChart = plotArea.getBarChartArray(i);
		        // TODO fill in the data sources from the sheet or from the cache
	            series.add(new XSLFBarChartSeries(sheet, barChart, categories, values));
	        }

	        for (int i = 0; i < plotArea.sizeOfPieChartArray(); i++) {
		        CTPieChart pieChart = plotArea.getPieChartArray(i);
		        // TODO fill in the data sources from the sheet or from the cache
	            series.add(new XSLFPieChartSeries(sheet, pieChart));
	        }

	        // TODO repeat above code for all kind of charts
		} catch(IOException e) {
		} catch(InvalidFormatException e) {
			System.err.println("No workbook available for chart.");
		} finally {
		}
		return series;
	}

	public void importContent(XSLFChart other) {
		this.chart.set(other.chart);
	}

	private Map<Long, XSLFCategoryAxis> getCategoryAxes() {
		CTPlotArea plotArea = getCTPlotArea();
		int sizeOfArray = plotArea.sizeOfCatAxArray();
		Map<Long, XSLFCategoryAxis> axes = new HashMap<Long, XSLFCategoryAxis>(sizeOfArray);
		for (int i = 0; i < sizeOfArray; i++) {
			CTCatAx category = plotArea.getCatAxArray(i);
			axes.put(category.getAxId().getVal(), new XSLFCategoryAxis(category));
		}
		return axes;
	}

	private Map<Long, XSLFValueAxis> getValueAxes() {
		CTPlotArea plotArea = getCTPlotArea();
		int sizeOfArray = plotArea.sizeOfValAxArray();
		Map<Long, XSLFValueAxis> axes = new HashMap<Long, XSLFValueAxis>(sizeOfArray);
		for (int i = 0; i < sizeOfArray; i++) {
			CTValAx values = plotArea.getValAxArray(i);
			axes.put(values.getAxId().getVal(), new XSLFValueAxis(values));
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
