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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPageMargins;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPrintSettings;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTx;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents a SpreadsheetML Chart
 */
public final class XSSFChart extends XDDFChart {

    /**
     * Parent graphic frame.
     */
    private XSSFGraphicFrame frame;

    /**
     * Create a new SpreadsheetML chart
     */
    protected XSSFChart() {
        super();
        createChart();
    }

    /**
     * Construct a SpreadsheetML chart from a package part.
     *
     * @param part
     *            the package part holding the chart data, the content type must
     *            be
     *            {@code application/vnd.openxmlformats-officedocument.drawingml.chart+xml}
     *
     * @since POI 3.14-Beta1
     */
    protected XSSFChart(PackagePart part) throws IOException, XmlException {
        super(part);
    }

    @Override
    protected POIXMLRelation getChartRelation() {
        return null;
    }

    @Override
    protected POIXMLRelation getChartWorkbookRelation() {
        return null;
    }

    @Override
    protected POIXMLFactory getChartFactory() {
        return null;
    }

    /**
     * Construct a new CTChartSpace bean. By default, it's just an empty
     * placeholder for chart objects.
     */
    private void createChart() {
        CTPlotArea plotArea = getCTPlotArea();

        plotArea.addNewLayout();
        getCTChart().addNewPlotVisOnly().setVal(true);

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

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        /*
         * Saved chart space must have the following namespaces set:
         * <c:chartSpace
         * xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
         * xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
         * xmlns:r=
         * "http://schemas.openxmlformats.org/officeDocument/2006/relationships">
         */
        xmlOptions.setSaveSyntheticDocumentElement(
            new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));

        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            chartSpace.save(out, xmlOptions);
        }
    }

    /**
     * Returns the parent graphic frame.
     *
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

    /**
     * Returns the title static text, or null if none is set. Note that a title
     * formula may be set instead. Empty text result is for backward
     * compatibility, and could mean the title text is empty or there is a
     * formula instead. Check for a formula first, falling back on text for
     * cleaner logic.
     *
     * @return static title text if set, null if there is no title, empty string
     *         if the title text is empty or the title uses a formula instead
     */
    public XSSFRichTextString getTitleText() {
        if (!getCTChart().isSetTitle()) {
            return null;
        }

        // TODO Do properly
        CTTitle title = getCTChart().getTitle();

        StringBuilder text = new StringBuilder(64);
        XmlObject[] t = title.selectPath("declare namespace a='" + XSSFDrawing.NAMESPACE_A + "' .//a:t");
        for (XmlObject element : t) {
            NodeList kids = element.getDomNode().getChildNodes();
            final int count = kids.getLength();
            for (int n = 0; n < count; n++) {
                Node kid = kids.item(n);
                if (kid instanceof Text) {
                    text.append(kid.getNodeValue());
                }
            }
        }

        return new XSSFRichTextString(text.toString());
    }

    /**
     * Get the chart title formula expression if there is one
     *
     * @return formula expression or null
     */
    public String getTitleFormula() {
        if (!getCTChart().isSetTitle()) {
            return null;
        }

        CTTitle title = getCTChart().getTitle();

        if (!title.isSetTx()) {
            return null;
        }

        CTTx tx = title.getTx();

        if (!tx.isSetStrRef()) {
            return null;
        }

        return tx.getStrRef().getF();
    }

    /**
     * Set the formula expression to use for the chart title
     */
    public void setTitleFormula(String formula) {
        CTTitle ctTitle;
        if (getCTChart().isSetTitle()) {
            ctTitle = getCTChart().getTitle();
        } else {
            ctTitle = getCTChart().addNewTitle();
        }

        CTTx tx;
        if (ctTitle.isSetTx()) {
            tx = ctTitle.getTx();
        } else {
            tx = ctTitle.addNewTx();
        }

        if (tx.isSetRich()) {
            tx.unsetRich();
        }

        CTStrRef strRef;
        if (tx.isSetStrRef()) {
            strRef = tx.getStrRef();
        } else {
            strRef = tx.addNewStrRef();
        }

        strRef.setF(formula);
    }
}
