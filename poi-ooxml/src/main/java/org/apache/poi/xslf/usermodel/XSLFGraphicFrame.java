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

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

import javax.xml.namespace.QName;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Yegor Kozlov
 */
@Beta
public class XSLFGraphicFrame extends XSLFShape {
    private final CTGraphicalObjectFrame _shape;
    private final XSLFSheet _sheet;

    /*package*/ XSLFGraphicFrame(CTGraphicalObjectFrame shape, XSLFSheet sheet){
        _shape = shape;
        _sheet = sheet;
    }

    public CTGraphicalObjectFrame getXmlObject(){
        return _shape;
    }

    public XSLFSheet getSheet(){
        return _sheet;
    }

    public int getShapeType(){
        throw new RuntimeException("NotImplemented");
    }

    public int getShapeId(){
        return (int)_shape.getNvGraphicFramePr().getCNvPr().getId();
    }

    public String getShapeName(){
        return _shape.getNvGraphicFramePr().getCNvPr().getName();
    }

    public Rectangle2D getAnchor(){
        CTTransform2D xfrm = _shape.getXfrm();
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
        CTTransform2D xfrm = _shape.getXfrm();
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
        String uri = shape.getGraphic().getGraphicData().getUri();
        if(XSLFTable.TABLE_URI.equals(uri)){
            return new XSLFTable(shape, sheet);
        } else {
            return new XSLFGraphicFrame(shape, sheet);
        }
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
    public double getRotation(){
    	return 0;
    }

    public void setFlipHorizontal(boolean flip){
    	throw new IllegalArgumentException("Operation not supported");
    }

    public void setFlipVertical(boolean flip){
    	throw new IllegalArgumentException("Operation not supported");
    }
    
    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public boolean getFlipHorizontal(){
    	return false;
    }

    public boolean getFlipVertical(){
    	return false;
    }

    public void draw(Graphics2D graphics){

    }

    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        CTGraphicalObjectData data = _shape.getGraphic().getGraphicData();
        String uri = data.getUri();
        if(uri.equals("http://schemas.openxmlformats.org/drawingml/2006/diagram")){
            copyDiagram(data, (XSLFGraphicFrame)sh);
        } else {
            // TODO  support other types of objects

        }
    }

    // TODO should be moved to a sub-class
    private void copyDiagram(CTGraphicalObjectData objData, XSLFGraphicFrame srcShape){
        String xpath = "declare namespace dgm='http://schemas.openxmlformats.org/drawingml/2006/diagram' $this//dgm:relIds";
        XmlObject[] obj = objData.selectPath(xpath);
        if(obj != null && obj.length == 1){
            XmlCursor c = obj[0].newCursor();

            XSLFSheet sheet = srcShape.getSheet();
            try {
                String dm = c.getAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "dm"));
                PackageRelationship dmRel = sheet.getPackagePart().getRelationship(dm);
                PackagePart dmPart = sheet.getPackagePart().getRelatedPart(dmRel);
                _sheet.importPart(dmRel, dmPart);

                String lo = c.getAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "lo"));
                PackageRelationship loRel = sheet.getPackagePart().getRelationship(lo);
                PackagePart loPart = sheet.getPackagePart().getRelatedPart(loRel);
                _sheet.importPart(loRel, loPart);

                String qs = c.getAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "qs"));
                PackageRelationship qsRel = sheet.getPackagePart().getRelationship(qs);
                PackagePart qsPart = sheet.getPackagePart().getRelatedPart(qsRel);
                _sheet.importPart(qsRel, qsPart);

                String cs = c.getAttributeText(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "cs"));
                PackageRelationship csRel = sheet.getPackagePart().getRelationship(cs);
                PackagePart csPart = sheet.getPackagePart().getRelatedPart(csRel);
                _sheet.importPart(csRel, csPart);

            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            }
            c.dispose();
        }
    }

}