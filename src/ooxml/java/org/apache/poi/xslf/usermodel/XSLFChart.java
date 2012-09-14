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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
	 * The Chart within that
	 */
	private CTChart chart;

	/**
	 * Construct a chart from a package part.
	 *
	 * @param part the package part holding the chart data,
	 * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
	 * @param rel  the package relationship holding this chart,
	 * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart
	 */
	protected XSLFChart(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
		super(part, rel);

		chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream()).getChartSpace(); 
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

	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));
		Map<String, String> map = new HashMap<String, String>();
		map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
		map.put("http://schemas.openxmlformats.org/drawingml/2006/chart", "c");
		map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
		xmlOptions.setSaveSuggestedPrefixes(map);

		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		chartSpace.save(out, xmlOptions);
		out.close();
	}


}
