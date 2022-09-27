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

import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_PRESENTATIONML;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.usermodel.GraphicalFrame;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;

@Beta
public class XSLFGraphicFrame extends XSLFShape implements GraphicalFrame<XSLFShape, XSLFTextParagraph> {
    private static final String DRAWINGML_CHART_URI = "http://schemas.openxmlformats.org/drawingml/2006/chart";
    private static final Logger LOG = LogManager.getLogger(XSLFGraphicFrame.class);

    /*package*/ XSLFGraphicFrame(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        super(shape,sheet);
    }

    public ShapeType getShapeType(){
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle2D getAnchor(){
        CTTransform2D xfrm = ((CTGraphicalObjectFrame)getXmlObject()).getXfrm();
        CTPoint2D off = xfrm.getOff();
        double x = Units.toPoints(POIXMLUnits.parseLength(off.xgetX()));
        double y = Units.toPoints(POIXMLUnits.parseLength(off.xgetY()));
        CTPositiveSize2D ext = xfrm.getExt();
        double cx = Units.toPoints(ext.getCx());
        double cy = Units.toPoints(ext.getCy());
        return new Rectangle2D.Double(x, y, cx, cy);
    }

    @Override
    public void setAnchor(Rectangle2D anchor){
        CTTransform2D xfrm = ((CTGraphicalObjectFrame)getXmlObject()).getXfrm();
        CTPoint2D off = xfrm.isSetOff() ? xfrm.getOff() : xfrm.addNewOff();
        long x = Units.toEMU(anchor.getX());
        long y = Units.toEMU(anchor.getY());
        off.setX(x);
        off.setY(y);
        CTPositiveSize2D ext = xfrm.isSetExt() ? xfrm.getExt() : xfrm
                .addNewExt();
        long cx = Units.toEMU(anchor.getWidth());
        long cy = Units.toEMU(anchor.getHeight());
        ext.setCx(cx);
        ext.setCy(cy);
    }


    static XSLFGraphicFrame create(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        final String uri = getUri(shape);
        switch (uri == null ? "" : uri) {
        case XSLFTable.TABLE_URI:
            return new XSLFTable(shape, sheet);
        case XSLFObjectShape.OLE_URI:
            return new XSLFObjectShape(shape, sheet);
        case XSLFDiagram.DRAWINGML_DIAGRAM_URI:
            return new XSLFDiagram(shape, sheet);
        default:
            return new XSLFGraphicFrame(shape, sheet);
        }
    }

    private static String getUri(CTGraphicalObjectFrame shape) {
        final CTGraphicalObject g = shape.getGraphic();
        if (g == null) {
            return null;
        }
        CTGraphicalObjectData gd = g.getGraphicData();
        return (gd == null) ? null : gd.getUri();
    }

    /**
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    @Override
    public void setRotation(double theta){
        throw new IllegalArgumentException("Operation not supported");
    }

    /**
     * Rotation angle in degrees
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @return rotation angle in degrees
     */
    @Override
    public double getRotation(){
        return 0;
    }

    @Override
    public void setFlipHorizontal(boolean flip){
        throw new IllegalArgumentException("Operation not supported");
    }

    @Override
    public void setFlipVertical(boolean flip){
        throw new IllegalArgumentException("Operation not supported");
    }

    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    @Override
    public boolean getFlipHorizontal(){
        return false;
    }

    @Override
    public boolean getFlipVertical(){
        return false;
    }

    public boolean hasChart() {
        String uri = getGraphicalData().getUri();
        return uri.equals(DRAWINGML_CHART_URI);
    }

    /**
     * @since POI 5.2.0
     */
    public boolean hasDiagram() {
        String uri = getGraphicalData().getUri();
        return uri.equals(XSLFDiagram.DRAWINGML_DIAGRAM_URI);
    }

    private CTGraphicalObjectData getGraphicalData() {
        return ((CTGraphicalObjectFrame)getXmlObject()).getGraphic().getGraphicData();
    }

    public XSLFChart getChart() {
        if (hasChart()) {
            String id = null;
            String xpath = "declare namespace c='" + DRAWINGML_CHART_URI + "' c:chart";
            XmlObject[] obj = getGraphicalData().selectPath(xpath);
            if (obj != null && obj.length == 1) {
                try (XmlCursor c = obj[0].newCursor()) {
                    QName idQualifiedName = new QName(CORE_PROPERTIES_ECMA376_NS, "id");
                    id = c.getAttributeText(idQualifiedName);
                }
            }
            if (id == null) {
                return null;
            } else {
                return (XSLFChart) getSheet().getRelationById(id);
            }
        } else {
            return null;
        }
    }

    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        CTGraphicalObjectData data = getGraphicalData();
        String uri = data.getUri();
        if(uri.equals(XSLFDiagram.DRAWINGML_DIAGRAM_URI)){
            copyDiagram(data, (XSLFGraphicFrame)sh);
        } if(uri.equals(DRAWINGML_CHART_URI)){
            copyChart(data, (XSLFGraphicFrame)sh);
        } else {
            // TODO  support other types of objects

        }
    }

    private void copyChart(CTGraphicalObjectData objData, XSLFGraphicFrame srcShape) {
        XSLFSlide slide = (XSLFSlide) getSheet();
        XSLFSheet src = srcShape.getSheet();
        String xpath = "declare namespace c='" + DRAWINGML_CHART_URI + "' c:chart";
        XmlObject[] obj = objData.selectPath(xpath);
        if (obj != null && obj.length == 1) {
            try (XmlCursor c = obj[0].newCursor()) {
                // duplicate chart with embedded workbook
                QName idQualifiedName = new QName(CORE_PROPERTIES_ECMA376_NS, "id");
                String id = c.getAttributeText(idQualifiedName);
                XSLFChart srcChart = (XSLFChart) src.getRelationById(id);
                XSLFChart chartCopy = slide.getSlideShow().createChart(slide);
                chartCopy.importContent(srcChart);
                chartCopy.setWorkbook(srcChart.getWorkbook());
                c.setAttributeText(idQualifiedName, slide.getRelationId(chartCopy));

                // duplicate the blip fill if set
                CTChartSpace chartSpaceCopy = chartCopy.getCTChartSpace();
                if (chartSpaceCopy != null) {
                    XSLFPropertiesDelegate.XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(chartSpaceCopy.getSpPr());
                    if (fp != null && fp.isSetBlipFill()) {
                        CTBlip blip = fp.getBlipFill().getBlip();
                        String blipId = blip.getEmbed();
                        String relId = slide.getSlideShow().importBlip(blipId, srcChart, chartCopy);
                        blip.setEmbed(relId);
                    }
                }
            } catch (InvalidFormatException | IOException e) {
                throw new POIXMLException(e);
            }
        }
    }

    // TODO should be moved to a sub-class
    private void copyDiagram(CTGraphicalObjectData objData, XSLFGraphicFrame srcShape){
        String xpath = "declare namespace dgm='" + XSLFDiagram.DRAWINGML_DIAGRAM_URI + "' $this//dgm:relIds";
        XmlObject[] obj = objData.selectPath(xpath);
        if(obj != null && obj.length == 1) {
            XSLFSheet sheet = srcShape.getSheet();
            try (XmlCursor c = obj[0].newCursor()) {
                String dm = c.getAttributeText(new QName(CORE_PROPERTIES_ECMA376_NS, "dm"));
                PackageRelationship dmRel = sheet.getPackagePart().getRelationship(dm);
                PackagePart dmPart = sheet.getPackagePart().getRelatedPart(dmRel);
                getSheet().importPart(dmRel, dmPart);

                String lo = c.getAttributeText(new QName(CORE_PROPERTIES_ECMA376_NS, "lo"));
                PackageRelationship loRel = sheet.getPackagePart().getRelationship(lo);
                PackagePart loPart = sheet.getPackagePart().getRelatedPart(loRel);
                getSheet().importPart(loRel, loPart);

                String qs = c.getAttributeText(new QName(CORE_PROPERTIES_ECMA376_NS, "qs"));
                PackageRelationship qsRel = sheet.getPackagePart().getRelationship(qs);
                PackagePart qsPart = sheet.getPackagePart().getRelatedPart(qsRel);
                getSheet().importPart(qsRel, qsPart);

                String cs = c.getAttributeText(new QName(CORE_PROPERTIES_ECMA376_NS, "cs"));
                PackageRelationship csRel = sheet.getPackagePart().getRelationship(cs);
                PackagePart csPart = sheet.getPackagePart().getRelatedPart(csRel);
                getSheet().importPart(csRel, csPart);

            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            }
        }
    }

    @Override
    public XSLFPictureShape getFallbackPicture() {
        String xquery =
                  "declare namespace p='" + NS_PRESENTATIONML + "'; "
                + "declare namespace mc='http://schemas.openxmlformats.org/markup-compatibility/2006' "
                + ".//mc:Fallback/*/p:pic"
                ;
        XmlObject xo = selectProperty(XmlObject.class, xquery);
        if (xo == null) {
            return null;
        }

        CTGroupShape gs;
        try {
            gs = CTGroupShape.Factory.parse(xo.newDomNode());
        } catch (XmlException e) {
            LOG.atWarn().withThrowable(e).log("Can't parse fallback picture stream of graphical frame");
            return null;
        }

        if (gs.sizeOfPicArray() == 0) {
            return null;
        }

        return new XSLFPictureShape(gs.getPicArray(0), getSheet());
    }
}
