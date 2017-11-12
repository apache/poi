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

package org.apache.poi.xwpf.usermodel;

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;

/**
 * Represents a Chart in a .docx file
 */
@Beta
public class XWPFChart extends POIXMLDocumentPart {

    /**
     * Root element of the Chart part
     */
    private final CTChartSpace chartSpace;

    /**
     * The Chart within that
     */
    private final CTChart chart;

    // lazy initialization
    private Long checksum;

    /**
     * Construct a chart from a package part.
     *
     * @param part the package part holding the chart data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     * 
     * @since POI 4.0.0
     */
    protected XWPFChart(PackagePart part) throws IOException, XmlException {
        super(part);

        chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS).getChartSpace(); 
        chart = chartSpace.getChart();
    }
      
    @Override
    protected void onDocumentRead() throws IOException {
        super.onDocumentRead();
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

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

        try (OutputStream out = getPackagePart().getOutputStream()) {
            chartSpace.save(out, xmlOptions);
        }
    }
    
    public Long getChecksum() {
        if (this.checksum == null) {
            InputStream is = null;
            byte[] data;
            try {
                is = getPackagePart().getInputStream();
                data = IOUtils.toByteArray(is);
            } catch (IOException e) {
                throw new POIXMLException(e);
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException e) {
                    throw new POIXMLException(e);
                }
            }
            this.checksum = IOUtils.calculateChecksum(data);
        }
        return this.checksum;
    }

    @Override
    public boolean equals(Object obj) {
        /**
         * In case two objects ARE equal, but its not the same instance, this
         * implementation will always run through the whole
         * byte-array-comparison before returning true. If this will turn into a
         * performance issue, two possible approaches are available:<br>
         * a) Use the checksum only and take the risk that two images might have
         * the same CRC32 sum, although they are not the same.<br>
         * b) Use a second (or third) checksum algorithm to minimise the chance
         * that two images have the same checksums but are not equal (e.g.
         * CRC32, MD5 and SHA-1 checksums, additionally compare the
         * data-byte-array lengths).
         */
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof XWPFChart)) {
            return false;
        }
        return false;
    }
   
    @Override
    public int hashCode() {
        return getChecksum().hashCode();
    }
}
