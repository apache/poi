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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTOleObject;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

/**
 * Represents a group shape that consists of many shapes grouped together.
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFGroupShape extends XSLFShape
implements XSLFShapeContainer, GroupShape<XSLFShape,XSLFTextParagraph> {
    private final static POILogger _logger = POILogFactory.getLogger(XSLFGroupShape.class);

    private final List<XSLFShape> _shapes;
    private final CTGroupShapeProperties _grpSpPr;
    private XSLFDrawing _drawing;

    protected XSLFGroupShape(CTGroupShape shape, XSLFSheet sheet){
        super(shape,sheet);
        _shapes = XSLFSheet.buildShapes(shape, this);
        _grpSpPr = shape.getGrpSpPr();
    }

    @Override
    protected CTGroupShapeProperties getGrpSpPr() {
        return _grpSpPr;
    }

    private CTGroupTransform2D getSafeXfrm() {
        CTGroupTransform2D xfrm = getXfrm();
        return (xfrm == null ? getGrpSpPr().addNewXfrm() : xfrm);
    }

    protected CTGroupTransform2D getXfrm() {
        return getGrpSpPr().getXfrm();
    }

    @Override
    public Rectangle2D getAnchor(){
        CTGroupTransform2D xfrm = getXfrm();
        CTPoint2D off = xfrm.getOff();
        double x = Units.toPoints(POIXMLUnits.parseLength(off.xgetX()));
        double y = Units.toPoints(POIXMLUnits.parseLength(off.xgetY()));
        CTPositiveSize2D ext = xfrm.getExt();
        double cx = Units.toPoints(ext.getCx());
        double cy = Units.toPoints(ext.getCy());
        return new Rectangle2D.Double(x,y,cx,cy);
    }

    @Override
    public void setAnchor(Rectangle2D anchor){
        CTGroupTransform2D xfrm = getSafeXfrm();
        CTPoint2D off = xfrm.isSetOff() ? xfrm.getOff() : xfrm.addNewOff();
        long x = Units.toEMU(anchor.getX());
        long y = Units.toEMU(anchor.getY());
        off.setX(x);
        off.setY(y);
        CTPositiveSize2D ext = xfrm.isSetExt() ? xfrm.getExt() : xfrm.addNewExt();
        long cx = Units.toEMU(anchor.getWidth());
        long cy = Units.toEMU(anchor.getHeight());
        ext.setCx(cx);
        ext.setCy(cy);
    }

    /**
     *
     * @return the coordinates of the child extents rectangle
     * used for calculations of grouping, scaling, and rotation
     * behavior of shapes placed within a group.
     */
    @Override
    public Rectangle2D getInteriorAnchor(){
        CTGroupTransform2D xfrm = getXfrm();
        CTPoint2D off = xfrm.getChOff();
        double x = Units.toPoints(POIXMLUnits.parseLength(off.xgetX()));
        double y = Units.toPoints(POIXMLUnits.parseLength(off.xgetY()));
        CTPositiveSize2D ext = xfrm.getChExt();
        double cx = Units.toPoints(ext.getCx());
        double cy = Units.toPoints(ext.getCy());
        return new Rectangle2D.Double(x, y, cx, cy);
    }

    /**
     *
     * @param anchor the coordinates of the child extents rectangle
     * used for calculations of grouping, scaling, and rotation
     * behavior of shapes placed within a group.
     */
    @Override
    public void setInteriorAnchor(Rectangle2D anchor) {
        CTGroupTransform2D xfrm = getSafeXfrm();
        CTPoint2D off = xfrm.isSetChOff() ? xfrm.getChOff() : xfrm.addNewChOff();
        long x = Units.toEMU(anchor.getX());
        long y = Units.toEMU(anchor.getY());
        off.setX(x);
        off.setY(y);
        CTPositiveSize2D ext = xfrm.isSetChExt() ? xfrm.getChExt() : xfrm.addNewChExt();
        long cx = Units.toEMU(anchor.getWidth());
        long cy = Units.toEMU(anchor.getHeight());
        ext.setCx(cx);
        ext.setCy(cy);
    }

    /**
     * @return child shapes contained within this group
     */
    @Override
    public List<XSLFShape> getShapes(){
        return _shapes;
    }

    /**
     * Returns an iterator over the shapes in this sheet
     *
     * @return an iterator over the shapes in this sheet
     */
    @Override
    public Iterator<XSLFShape> iterator(){
        return _shapes.iterator();
    }

    /**
     * Remove the specified shape from this group
     */
    @Override
    public boolean removeShape(XSLFShape xShape) {
        XmlObject obj = xShape.getXmlObject();
        CTGroupShape grpSp = (CTGroupShape)getXmlObject();
        getSheet().deregisterShapeId(xShape.getShapeId());
        if(obj instanceof CTShape){
            grpSp.getSpList().remove(obj);
        } else if (obj instanceof CTGroupShape){
            XSLFGroupShape gs = (XSLFGroupShape)xShape;
            new ArrayList<>(gs.getShapes()).forEach(gs::removeShape);
            grpSp.getGrpSpList().remove(obj);
        } else if (obj instanceof CTConnector){
            grpSp.getCxnSpList().remove(obj);
        } else if (obj instanceof CTGraphicalObjectFrame) {
            grpSp.getGraphicFrameList().remove(obj);
        } else if (obj instanceof CTPicture) {
            XSLFPictureShape ps = (XSLFPictureShape)xShape;
            XSLFSheet sh = getSheet();
            if (sh != null) {
                sh.removePictureRelation(ps);
            }
            grpSp.getPicList().remove(obj);
        } else {
            throw new IllegalArgumentException("Unsupported shape: " + xShape);
        }
        return _shapes.remove(xShape);
    }

    /**
     * @param shapeId 1-based shapeId
     */
    static CTGroupShape prototype(int shapeId) {
        CTGroupShape ct = CTGroupShape.Factory.newInstance();
        CTGroupShapeNonVisual nvSpPr = ct.addNewNvGrpSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Group " + shapeId);
        cnv.setId(shapeId);

        nvSpPr.addNewCNvGrpSpPr();
        nvSpPr.addNewNvPr();
        ct.addNewGrpSpPr();
        return ct;
    }

    // shape factory methods
    private XSLFDrawing getDrawing(){
        if(_drawing == null) {
            _drawing = new XSLFDrawing(getSheet(), (CTGroupShape)getXmlObject());
        }
        return _drawing;
    }

    @Override
    public XSLFAutoShape createAutoShape(){
        XSLFAutoShape sh = getDrawing().createAutoShape();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFFreeformShape createFreeform(){
        XSLFFreeformShape sh = getDrawing().createFreeform();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFTextBox createTextBox(){
        XSLFTextBox sh = getDrawing().createTextBox();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFConnectorShape createConnector(){
        XSLFConnectorShape sh = getDrawing().createConnector();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFGroupShape createGroup(){
        XSLFGroupShape sh = getDrawing().createGroup();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFPictureShape createPicture(PictureData pictureData){
        if (!(pictureData instanceof XSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type XSLFPictureData");
        }
        RelationPart rp = getSheet().addRelation(null, XSLFRelation.IMAGES, (XSLFPictureData)pictureData);

        XSLFPictureShape sh = getDrawing().createPicture(rp.getRelationship().getId());
        new DrawPictureShape(sh).resize();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFObjectShape createOleShape(PictureData pictureData) {
        if (!(pictureData instanceof XSLFPictureData)) {
            throw new IllegalArgumentException("pictureData needs to be of type XSLFPictureData");
        }

        RelationPart rp = getSheet().addRelation(null, XSLFRelation.IMAGES, (XSLFPictureData)pictureData);

        XSLFObjectShape sh = getDrawing().createOleShape(rp.getRelationship().getId());
        CTOleObject oleObj = sh.getCTOleObject();
        Dimension dim = pictureData.getImageDimension();
        oleObj.setImgW(Units.toEMU(dim.getWidth()));
        oleObj.setImgH(Units.toEMU(dim.getHeight()));


        getShapes().add(sh);
        sh.setParent(this);
        return sh;
    }

    public XSLFTable createTable(){
        XSLFTable sh = getDrawing().createTable();
        _shapes.add(sh);
        sh.setParent(this);
        return sh;
    }

    @Override
    public XSLFTable createTable(int numRows, int numCols){
        if (numRows < 1 || numCols < 1) {
            throw new IllegalArgumentException("numRows and numCols must be greater than 0");
        }
        XSLFTable sh = getDrawing().createTable();
        _shapes.add(sh);
        sh.setParent(this);
        for (int r=0; r<numRows; r++) {
            XSLFTableRow row = sh.addRow();
            for (int c=0; c<numCols; c++) {
                row.addCell();
            }
        }
        return sh;
    }


    @Override
    public void setFlipHorizontal(boolean flip){
        getSafeXfrm().setFlipH(flip);
    }

    @Override
    public void setFlipVertical(boolean flip){
        getSafeXfrm().setFlipV(flip);
    }

    @Override
    public boolean getFlipHorizontal(){
        CTGroupTransform2D xfrm = getXfrm();
        return !(xfrm == null || !xfrm.isSetFlipH()) && xfrm.getFlipH();
    }

    @Override
    public boolean getFlipVertical(){
        CTGroupTransform2D xfrm = getXfrm();
        return !(xfrm == null || !xfrm.isSetFlipV()) && xfrm.getFlipV();
    }

    @Override
    public void setRotation(double theta){
        getSafeXfrm().setRot((int) (theta * 60000));
    }

    @Override
    public double getRotation(){
        CTGroupTransform2D xfrm = getXfrm();
        return (xfrm == null || !xfrm.isSetRot()) ? 0 : (xfrm.getRot() / 60000.d);
    }

    @Override
    void copy(XSLFShape src){
        XSLFGroupShape gr = (XSLFGroupShape)src;

        // recursively update each shape
        List<XSLFShape> tgtShapes = getShapes();
        List<XSLFShape> srcShapes = gr.getShapes();

        // workaround for a call by XSLFSheet.importContent:
        // if we have already the same amount of child shapes
        // then assume, that we've been called by import content and only need to update the children
        if (tgtShapes.size() == srcShapes.size()) {
            for(int i = 0; i < tgtShapes.size(); i++){
                XSLFShape s1 = srcShapes.get(i);
                XSLFShape s2 = tgtShapes.get(i);

                s2.copy(s1);
            }
        } else {
            // otherwise recreate the shapes from scratch
            clear();

            // recursively update each shape
            for(XSLFShape shape : srcShapes) {
                XSLFShape newShape;
                if (shape instanceof XSLFTextBox) {
                    newShape = createTextBox();
                } else if (shape instanceof XSLFFreeformShape) {
                    newShape = createFreeform();
                } else if (shape instanceof XSLFAutoShape) {
                    newShape = createAutoShape();
                } else if (shape instanceof XSLFConnectorShape) {
                    newShape = createConnector();
                } else if (shape instanceof XSLFPictureShape) {
                    XSLFPictureShape p = (XSLFPictureShape)shape;
                    XSLFPictureData pd = p.getPictureData();
                    XSLFPictureData pdNew = getSheet().getSlideShow().addPicture(pd.getData(), pd.getType());
                    newShape = createPicture(pdNew);
                } else if (shape instanceof XSLFGroupShape) {
                    newShape = createGroup();
                } else if (shape instanceof XSLFTable) {
                    newShape = createTable();
                } else {
                    _logger.log(POILogger.WARN, "copying of class "+shape.getClass()+" not supported.");
                    continue;
                }

                newShape.copy(shape);
            }
        }
    }

    /**
     * Removes all of the elements from this container (optional operation).
     * The container will be empty after this call returns.
     */
    @Override
    public void clear() {
        List<XSLFShape> shapes = new ArrayList<>(getShapes());
        for(XSLFShape shape : shapes){
            removeShape(shape);
        }
    }

    @Override
    public void addShape(XSLFShape shape) {
        throw new UnsupportedOperationException(
            "Adding a shape from a different container is not supported -"
            + " create it from scratch with XSLFGroupShape.create* methods");
    }
}