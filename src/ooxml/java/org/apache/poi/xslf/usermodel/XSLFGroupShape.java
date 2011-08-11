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
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeGroup;
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

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a group shape that consists of many shapes grouped together.
 * 
 * @author Yegor Kozlov
 */
@Beta
public class XSLFGroupShape extends XSLFShape {
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

    public CTGroupShape getXmlObject(){
        return _shape;
    }

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

    public XSLFShape[] getShapes(){
        return _shapes.toArray(new XSLFShape[_shapes.size()]);
    }

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

    public String getShapeName(){
        return _shape.getNvGrpSpPr().getCNvPr().getName();
    }

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
                .getPartsByName(Pattern.compile("/ppt/media/.*?"));

        PackagePart pic = pics.get(pictureIndex);

        PackageRelationship rel = _sheet.getPackagePart().addRelationship(
                pic.getPartName(), TargetMode.INTERNAL, XSLFRelation.IMAGES.getRelation());

        XSLFPictureShape sh = getDrawing().createPicture(rel.getId());
        sh.resize();
        _shapes.add(sh);
        return sh;
    }

}