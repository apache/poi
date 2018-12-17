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

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.Beta;
import org.apache.poi.xddf.usermodel.chart.XDDFChart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrameNonVisual;

/**
 * Represents a Chart in a .pptx presentation
 */
@Beta
public final class XSLFChart extends XDDFChart {

    private static String CHART_URI = "http://schemas.openxmlformats.org/drawingml/2006/chart";

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
     *             the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
     * @since POI 3.14-Beta1
     */
    protected XSLFChart(PackagePart part) throws IOException, XmlException {
        super(part);
    }

    @Override
    protected POIXMLRelation getChartRelation() {
        return XSLFRelation.CHART;
    }

    @Override
    protected POIXMLRelation getChartWorkbookRelation() {
        return XSLFRelation.WORKBOOK;
    }

    @Override
    protected POIXMLFactory getChartFactory() {
        return XSLFFactory.getInstance();
    }

    public XSLFTextShape getTitleShape() {
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

    /**
     * method to add graphic frame for XSLF chart
     *
     * @param shapeId shape id
     * @param rID relation id
     * @param anchor size and location of chart
     * @return graphic frame object
     * @since POI 4.1.0
     */
    static CTGraphicalObjectFrame prototype(int shapeId, String rID, Rectangle2D anchor) {
        CTGraphicalObjectFrame frame = CTGraphicalObjectFrame.Factory.newInstance();
        CTGraphicalObjectFrameNonVisual nvGr = frame.addNewNvGraphicFramePr();

        CTNonVisualDrawingProps cnv = nvGr.addNewCNvPr();
        cnv.setName("Chart " + shapeId);
        cnv.setId(shapeId);
        nvGr.addNewCNvGraphicFramePr().addNewGraphicFrameLocks().setNoGrp(true);
        nvGr.addNewNvPr();

        CTTransform2D xfrm = frame.addNewXfrm();

        CTPoint2D off = xfrm.addNewOff();
        off.setX((int)anchor.getX());
        off.setY((int)anchor.getY());

        CTPositiveSize2D ext = xfrm.addNewExt();
        ext.setCx((int)anchor.getWidth());
        ext.setCy((int)anchor.getHeight());

        xfrm.setExt(ext);
        xfrm.setOff(off);

        CTGraphicalObjectData gr = frame.addNewGraphic().addNewGraphicData();
        XmlCursor grCur = gr.newCursor();
        grCur.toNextToken();
        grCur.beginElement(new QName(CHART_URI, "chart"));
        grCur.insertAttributeWithValue("id", PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS, rID);
        grCur.dispose();

        gr.setUri(CHART_URI);
        return frame;
    }
}
