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

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

/**
 * Represents a Chart in a .docx file
 */
@Beta
public class XWPFChart extends XDDFChart {
    /**
     * default width of chart in emu
     */
    public static final int DEFAULT_WIDTH = XDDFChart.DEFAULT_WIDTH;

    /**
     * default height of chart in emu
     */
    public static final int DEFAULT_HEIGHT = XDDFChart.DEFAULT_HEIGHT;

    // lazy initialization
    private Long checksum;

    /**
     * this object is used to modify drawing properties
     */
    private CTInline ctInline;

    /**
     * constructor to
     * Create a new chart in document
     *
     * @since POI 4.0.0
     */
    protected XWPFChart() {
        super();
    }

    /**
     * Construct a chart from a package part.
     *
     * @param part the package part holding the chart data,
     *             the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     * @since POI 4.0.0
     */
    protected XWPFChart(PackagePart part) throws IOException, XmlException {
        super(part);
    }

    @Override
    protected POIXMLRelation getChartRelation() {
        return XWPFRelation.CHART;
    }

    @Override
    protected POIXMLRelation getChartWorkbookRelation() {
        return XWPFRelation.WORKBOOK;
    }

    @Override
    protected POIXMLFactory getChartFactory() {
        return XWPFFactory.getInstance();
    }

    public Long getChecksum() {
        if (this.checksum == null) {
            byte[] data;
            try (InputStream is = getPackagePart().getInputStream()) {
                data = IOUtils.toByteArray(is);
            } catch (IOException e) {
                throw new POIXMLException(e);
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

    /**
     * Attach this chart known by its relation id to the given text run.
     *
     * @param chartRelId the relation id of this chart in its parent document.
     * @param run the text run to which this chart will be inlined.
     * @throws InvalidFormatException
     * @throws IOException
     * @since POI 4.0.0
     */
    protected void attach(String chartRelId, XWPFRun run)
        throws InvalidFormatException, IOException {
        ctInline = run.addChart(chartRelId);
        ctInline.addNewExtent();
        setChartBoundingBox(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * set chart height
     *
     * @param height height of chart
     * @since POI 4.0.0
     */
    public void setChartHeight(long height) {
        ctInline.getExtent().setCy(height);
    }

    /**
     * set chart width
     *
     * @param width width of chart
     * @since POI 4.0.0
     */
    public void setChartWidth(long width) {
        ctInline.getExtent().setCx(width);
    }

    /**
     * get chart height
     *
     * @since POI 4.0.0
     */
    public long getChartHeight() {
        return ctInline.getExtent().getCy();
    }

    /**
     * get chart width
     *
     * @since POI 4.0.0
     */
    public long getChartWidth() {
        return ctInline.getExtent().getCx();
    }

    /**
     * set chart height and width
     *
     * @param width  width of chart
     * @param height height of chart
     * @since POI 4.0.0
     */
    public void setChartBoundingBox(long width, long height) {
        this.setChartWidth(width);
        this.setChartHeight(height);
    }

    /**
     * set margin from top
     *
     * @param margin margin from top
     * @since POI 4.0.0
     */
    public void setChartTopMargin(long margin) {
        ctInline.setDistT(margin);
    }

    /**
     * get margin from Top
     *
     * @param margin
     * @since POI 4.0.0
     */
    public long getChartTopMargin(long margin) {
        return ctInline.getDistT();
    }

    /**
     * set margin from bottom
     *
     * @param margin margin from Bottom
     * @since POI 4.0.0
     */
    public void setChartBottomMargin(long margin) {
        ctInline.setDistB(margin);
    }

    /**
     * get margin from Bottom
     *
     * @param margin
     * @since POI 4.0.0
     */
    public long getChartBottomMargin(long margin) {
        return ctInline.getDistB();
    }

    /**
     * set margin from left
     *
     * @param margin margin from left
     * @since POI 4.0.0
     */
    public void setChartLeftMargin(long margin) {
        ctInline.setDistL(margin);
    }

    /**
     * get margin from left
     *
     * @param margin
     * @since POI 4.0.0
     */
    public long getChartLeftMargin(long margin) {
        return ctInline.getDistL();
    }

    /**
     * set margin from Right
     *
     * @param margin from right
     * @since POI 4.0.0
     */
    public void setChartRightMargin(long margin) {
        ctInline.setDistR(margin);
    }

    /**
     * get margin from Right
     *
     * @param margin
     * @since POI 4.0.0
     */
    public long getChartRightMargin(long margin) {
        return ctInline.getDistR();
    }

    /**
     * set chart margin
     *
     * @param top    margin from top
     * @param right  margin from right
     * @param bottom margin from bottom
     * @param left   margin from left
     * @since POI 4.0.0
     */
    public void setChartMargin(long top, long right, long bottom, long left) {
        this.setChartBottomMargin(bottom);
        this.setChartRightMargin(right);
        this.setChartLeftMargin(left);
        this.setChartRightMargin(right);
    }
}
