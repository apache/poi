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

package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Internal;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartAxisFactory;
import org.apache.poi.xssf.usermodel.charts.XSSFChartDataFactory;
import org.apache.poi.xssf.usermodel.charts.XSSFChartAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.apache.poi.xssf.usermodel.charts.XSSFManualLayout;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.apache.poi.ss.usermodel.charts.ChartData;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPrintSettings;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPageMargins;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents a SpreadsheetML Chart
 * @author Nick Burch
 * @author Roman Kashitsyn
 */
public final class XSSFChart extends POIXMLDocumentPart implements Chart, ChartAxisFactory {

	/**
	 * Parent graphic frame.
	 */
	private XSSFGraphicFrame frame;

	/**
	 * Root element of the SpreadsheetML Chart part
	 */
	private CTChartSpace chartSpace;
	/**
	 * The Chart within that
	 */
	private CTChart chart;

	List<XSSFChartAxis> axis;

	/**
	 * Create a new SpreadsheetML chart
	 */
	protected XSSFChart() {
		super();
		axis = new ArrayList<XSSFChartAxis>();
		createChart();
	}

	/**
	 * Construct a SpreadsheetML chart from a package part.
	 *
	 * @param part the package part holding the chart data,
	 * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
	 * @param rel  the package relationship holding this chart,
	 * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart
	 */
	protected XSSFChart(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
		super(part, rel);

		chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream()).getChartSpace(); 
		chart = chartSpace.getChart();
	}

	/**
	 * Construct a new CTChartSpace bean.
	 * By default, it's just an empty placeholder for chart objects.
	 *
	 * @return a new CTChartSpace bean
	 */
	private void createChart() {
		chartSpace = CTChartSpace.Factory.newInstance();
		chart = chartSpace.addNewChart();
		CTPlotArea plotArea = chart.addNewPlotArea();

		plotArea.addNewLayout();
		chart.addNewPlotVisOnly().setVal(true);

		CTPrintSettings printSettings = chartSpace.addNewPrintSettings();
		printSettings.addNewHeaderFooter();

		CTPageMargins pageMargins = printSettings.addNewPageMargins();
		pageMargins.setB(0.75);
		pageMargins.setL(0.70);
		pageMargins.setR(0.70);
		pageMargins.setT(0.75);
		pageMargins.setHeader(0.30);
		pageMargins.setFooter(0.30);
		printSettings.addNewPageSetup();
	}

	/**
	 * Return the underlying CTChartSpace bean, the root element of the SpreadsheetML Chart part.
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

	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

		/*
		   Saved chart space must have the following namespaces set:
		   <c:chartSpace
		      xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
		      xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
		      xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
		 */
		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));
		Map<String, String> map = new HashMap<String, String>();
		map.put(XSSFDrawing.NAMESPACE_A, "a");
		map.put(XSSFDrawing.NAMESPACE_C, "c");
		map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
		xmlOptions.setSaveSuggestedPrefixes(map);

		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		chartSpace.save(out, xmlOptions);
		out.close();
	}

	/**
	 * Returns the parent graphic frame.
	 * @return the graphic frame this chart belongs to
	 */
	public XSSFGraphicFrame getGraphicFrame() {
		return frame;
	}

	/**
	 * Sets the parent graphic frame.
	 */
	protected void setGraphicFrame(XSSFGraphicFrame frame) {
		this.frame = frame;
	}

	public XSSFChartDataFactory getChartDataFactory() {
		return XSSFChartDataFactory.getInstance();
	}

	public XSSFChart getChartAxisFactory() {
		return this;
	}

	public void plot(ChartData data, ChartAxis... axis) {
		data.fillChart(this, axis);
	}

	public XSSFValueAxis createValueAxis(AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFValueAxis valueAxis = new XSSFValueAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(valueAxis);
			valueAxis.crossAxis(ax);
		}
		axis.add(valueAxis);
		return valueAxis;
	}

	public List<? extends XSSFChartAxis> getAxis() {
		if (axis.isEmpty() && hasAxis()) {
			parseAxis();
		}
		return axis;
	}

	public XSSFManualLayout getManualLayout() {
		return new XSSFManualLayout(this);
	}

	/**
	 * @return true if only visible cells will be present on the chart,
	 *         false otherwise
	 */
	public boolean isPlotOnlyVisibleCells() {
		return chart.getPlotVisOnly().getVal();
	}

	/**
	 * @param plotVisOnly a flag specifying if only visible cells should be
	 *        present on the chart
	 */
	public void setPlotOnlyVisibleCells(boolean plotVisOnly) {
		chart.getPlotVisOnly().setVal(plotVisOnly);
	}

	/**
	 * Returns the title, or null if none is set
	 */
	public XSSFRichTextString getTitle() {
		if(! chart.isSetTitle()) {
			return null;
		}

		// TODO Do properly
		CTTitle title = chart.getTitle();

		StringBuffer text = new StringBuffer();
		XmlObject[] t = title
			.selectPath("declare namespace a='"+XSSFDrawing.NAMESPACE_A+"' .//a:t");
		for (int m = 0; m < t.length; m++) {
			NodeList kids = t[m].getDomNode().getChildNodes();
			for (int n = 0; n < kids.getLength(); n++) {
				if (kids.item(n) instanceof Text) {
					text.append(kids.item(n).getNodeValue());
				}
			}
		}

		return new XSSFRichTextString(text.toString());
	}

	public XSSFChartLegend getOrCreateLegend() {
		return new XSSFChartLegend(this);
	}

	public void deleteLegend() {
		if (chart.isSetLegend()) {
			chart.unsetLegend();
		}
	}

	private boolean hasAxis() {
		CTPlotArea ctPlotArea = chart.getPlotArea();
		int totalAxisCount =
			ctPlotArea.sizeOfValAxArray()  +
			ctPlotArea.sizeOfCatAxArray()  +
			ctPlotArea.sizeOfDateAxArray() +
			ctPlotArea.sizeOfSerAxArray();
		return totalAxisCount > 0;
	}

	private void parseAxis() {
		// TODO: add other axis types
		parseValueAxis();
	}

	private void parseValueAxis() {
		for (CTValAx valAx : chart.getPlotArea().getValAxList()) {
			axis.add(new XSSFValueAxis(this, valAx));
		}
	}

}
