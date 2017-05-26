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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.charts.XSLFCategoryAxis;
import org.apache.poi.xslf.usermodel.charts.XSLFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;

/**
 * Represents a Chart in a .pptx presentation
 *
 *
 */
@Beta
public final class XSLFChart extends POIXMLDocumentPart {

	/**
	 * Root element of the Chart part
	 */
	private CTChartSpace chartSpace;


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
    }

	/**
	 * Return the underlying CTPlotArea bean, within the Chart Space
	 *
	 * @return the underlying CTPlotArea bean
	 */
	@Internal
	public CTPlotArea getCTPlotArea(){
		return chartSpace.getChart().getPlotArea();
	}

	public XSLFTextShape getTitle() {
		final CTTitle title = chartSpace.getChart().getTitle();
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

	private POIXMLDocumentPart getXslxPart() {
		// cannot cache this while in the constructor
		return getRelationById(chartSpace.getExternalData().getId());
	}
	
	public XSSFWorkbook getWorkbook() throws IOException {
		return new XSSFWorkbook(getXslxPart().getPackagePart().getInputStream());
	}

	public void saveWorkbook(XSSFWorkbook workbook) throws IOException {
        OutputStream xlsOut = getXslxPart().getPackagePart().getOutputStream();
        try {
            workbook.write(xlsOut);
        } finally {
            xlsOut.close();
        }
	}

	public List<XSLFCategoryAxis> getCategoryAxes() {
		CTPlotArea plotArea = getCTPlotArea();
		int sizeOfArray = plotArea.sizeOfCatAxArray();
		List<XSLFCategoryAxis> axes = new ArrayList<XSLFCategoryAxis>(sizeOfArray);
		for (int i = 0; i < sizeOfArray; i++) {
			axes.add(new XSLFCategoryAxis(plotArea.getCatAxArray(i)));
		}
		return axes;
	}
	
	public List<XSLFValueAxis> getValueAxes() {
		CTPlotArea plotArea = getCTPlotArea();
		int sizeOfArray = plotArea.sizeOfValAxArray();
		List<XSLFValueAxis> axes = new ArrayList<XSLFValueAxis>(sizeOfArray);
		for (int i = 0; i < sizeOfArray; i++) {
			axes.add(new XSLFValueAxis(plotArea.getValAxArray(i)));
		}
		return axes;
	}

	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		chartSpace.save(out, xmlOptions);
		out.close();
	}
}
