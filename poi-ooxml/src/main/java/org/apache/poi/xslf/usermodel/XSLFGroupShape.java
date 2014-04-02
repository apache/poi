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

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTConnector;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a group shape that consists of many shapes grouped together.
 * 
 * @author Yegor Kozlov
 */
@Beta
public class XSLFGroupShape extends XSLFShape implements XSLFShapeContainer {
    private final CTGroupShape _shape;
    private final XSLFSheet _sheet;
    private final List<XSLFShape> _shapes;
    private final CTGroupShapeProperties _spPr;
    private XSLFDrawing _drawing;

    /*package*/ XSLFGroupShape(CTGroupShape shape, XSLFSheet sheet){
        _shape = shape;
        _sheet = sheet;

        _shapes = _sheet.buildShapes(_shape);
        _spPr = shape.getGrpSpPr();
    }

    @Override
    public CTGroupShape getXmlObject(){
        return _shape;
    }

    @Override
    public Rectangle2D getAnchor(){
        CTGroupTransform2D xfrm = _spPr.getXfrm();
        CTPoint2D off = xfrm.getOff();
        long x = off.getX();
        long y = off.getY();
        CTPositiveSize2D ext = xfrm.getExt();
        long cx = ext.getCx();
        long cy = ext.getCy();
        return new Rectangle2D.Double(
                Units.toPoints(x), Units.toPoints(y),
                Units.toPoints(cx), Units.toPoints(cy));
    }

    @Override
    public void setAnchor(Rectangle2D anchor){
        CTGroupTransform2D xfrm = _spPr.isSetXfrm() ? _spPr.getXfrm() : _spPr.addNewXfrm();
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
    public Rectangle2D getInteriorAnchor(){
        CTGroupTransform2D xfrm = _spPr.getXfrm();
        CTPoint2D off = xfrm.getChOff();
        long x = off.getX();
        long y = off.getY();
        CTPositiveSize2D ext = xfrm.getChExt();
        long cx = ext.getCx();
        long cy = ext.getCy();
        return new Rectangle2D.Double(
                Units.toPoints(x), Units.toPoints(y),
                Units.toPoints(cx), Units.toPoints(cy));
    }

    /**
     *
     * @param anchor the coordinates of the child extents rectangle
     * used for calculations of grouping, scaling, and rotation
     * behavior of shapes placed within a group.
     */
    public void setInteriorAnchor(Rectangle2D anchor){
        CTGroupTransform2D xfrm = _spPr.isSetXfrm() ? _spPr.getXfrm() : _spPr.addNewXfrm();
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
     *
     * @return child shapes contained witin this group
     */
    public XSLFShape[] getShapes(){
        return _shapes.toArray(new XSLFShape[_shapes.size()]);
    }

    /**
     * Returns an iterator over the shapes in this sheet
     *
     * @return an iterator over the shapes in this sheet
     */
    public Iterator<XSLFShape> iterator(){
        return _shapes.iterator();
    }

    /**
     * Remove the specified shape from this group
     */
    public boolean removeShape(XSLFShape xShape) {
        XmlObject obj = xShape.getXmlObject();
        if(obj instanceof CTShape){
            _shape.getSpList().remove(obj);
        } else if (obj instanceof CTGroupShape){
            _shape.getGrpSpList().remove(obj);
        } else if (obj instanceof CTConnector){
            _shape.getCxnSpList().remove(obj);
        } else {
            throw new IllegalArgumentException("Unsupported shape: " + xShape);
        }
        return _shapes.remove(xShape);
    }

    @Override
    public String getShapeName(){
        return _shape.getNvGrpSpPr().getCNvPr().getName();
    }

    @Override
    public int getShapeId(){
        return (int)_shape.getNvGrpSpPr().getCNvPr().getId();
    }

    /**
     * @param shapeId 1-based shapeId
     */
    static CTGroupShape prototype(int shapeId) {
        CTGroupShape ct = CTGroupShape.Factory.newInstance();
        CTGroupShapeNonVisual nvSpPr = ct.addNewNvGrpSpPr();
        CTNonVisualDrawingProps cnv = nvSpPr.addNewCNvPr();
        cnv.setName("Group " + shapeId);
        cnv.setId(shapeId + 1);

        nvSpPr.addNewCNvGrpSpPr();
        nvSpPr.addNewNvPr();
        ct.addNewGrpSpPr();
        return ct;
    }

    // shape factory methods
    private XSLFDrawing getDrawing(){
        if(_drawing == null) {
            _drawing = new XSLFDrawing(_sheet, _shape);
        }
        return _drawing;
    }

    public XSLFAutoShape createAutoShape(){
        XSLFAutoShape sh = getDrawing().createAutoShape();
        _shapes.add(sh);
        return sh;
    }

    public XSLFFreeformShape createFreeform(){
        XSLFFreeformShape sh = getDrawing().createFreeform();
        _shapes.add(sh);
        return sh;
    }

    public XSLFTextBox createTextBox(){
        XSLFTextBox sh = getDrawing().createTextBox();
        _shapes.add(sh);
        return sh;
    }

    public XSLFConnectorShape createConnector(){
        XSLFConnectorShape sh = getDrawing().createConnector();
        _shapes.add(sh);
        return sh;
    }

    public XSLFGroupShape createGroup(){
        XSLFGroupShape sh = getDrawing().createGroup();
        _shapes.add(sh);
        return sh;
    }

    public XSLFPictureShape createPicture(int pictureIndex){

        List<PackagePart>  pics = _sheet.getPackagePart().getPackage()
                .getPartsByName(Pattern.compile("/ppt/media/image" + (pictureIndex + 1) + ".*?"));

        if(pics.size() == 0) {
            throw new IllegalArgumentException("Picture with index=" + pictureIndex + " was not found");
        }

        PackagePart pic = pics.get(0);

        PackageRelationship rel = _sheet.getPackagePart().addRelationship(
                pic.getPartName(), TargetMode.INTERNAL, XSLFRelation.IMAGES.getRelation());

        XSLFPictureShape sh = getDrawing().createPicture(rel.getId());
        sh.resize();
        _shapes.add(sh);
        return sh;
    }

    @Override
    public void setFlipHorizontal(boolean flip){
        _spPr.getXfrm().setFlipH(flip);
    }

    @Override
    public void setFlipVertical(boolean flip){
        _spPr.getXfrm().setFlipV(flip);
    }

    @Override
    public boolean getFlipHorizontal(){
         return _spPr.getXfrm().getFlipH();
    }

    @Override
    public boolean getFlipVertical(){
         return _spPr.getXfrm().getFlipV();
    }

    @Override
    public void setRotation(double theta){
        _spPr.getXfrm().setRot((int)(theta*60000));
    }

    @Override
    public double getRotation(){
        return (double)_spPr.getXfrm().getRot()/60000;
    }

    @Override
    public void draw(Graphics2D graphics){

    	// the coordinate system of this group of shape
        Rectangle2D interior = getInteriorAnchor();
        // anchor of this group relative to the parent shape
        Rectangle2D exterior = getAnchor();

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(XSLFRenderingHint.GROUP_TRANSFORM);
        AffineTransform tx0 = new AffineTransform(tx);

        double scaleX = interior.getWidth() == 0. ? 1.0 : exterior.getWidth() / interior.getWidth();
        double scaleY = interior.getHeight() == 0. ? 1.0 : exterior.getHeight() / interior.getHeight();

        tx.translate(exterior.getX(), exterior.getY());
        tx.scale(scaleX, scaleY);
        tx.translate(-interior.getX(), -interior.getY());

        for (XSLFShape shape : getShapes()) {
        	// remember the initial transform and restore it after we are done with the drawing
        	AffineTransform at = graphics.getTransform();
            graphics.setRenderingHint(XSLFRenderingHint.GSAVE, true);

            shape.applyTransform(graphics);
        	shape.draw(graphics);

            // restore the coordinate system
            graphics.setTransform(at);
            graphics.setRenderingHint(XSLFRenderingHint.GRESTORE, true);
        }

        graphics.setRenderingHint(XSLFRenderingHint.GROUP_TRANSFORM, tx0);
        
    }

    @Override
    void copy(XSLFShape src){
        XSLFGroupShape gr = (XSLFGroupShape)src;
        // recursively update each shape
        XSLFShape[] tgtShapes = getShapes();
        XSLFShape[] srcShapes = gr.getShapes();
        for(int i = 0; i < tgtShapes.length; i++){
            XSLFShape s1 = srcShapes[i];
            XSLFShape s2 = tgtShapes[i];

            s2.copy(s1);
        }
    }

    /**
     * Removes all of the elements from this container (optional operation).
     * The container will be empty after this call returns.
     */
    public void clear() {
        for(XSLFShape shape : getShapes()){
            removeShape(shape);
        }
    }

}