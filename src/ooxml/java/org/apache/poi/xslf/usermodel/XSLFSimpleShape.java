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

import org.apache.poi.xslf.usermodel.LineCap;
import org.apache.poi.xslf.usermodel.LineDash;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetLineDashProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFSimpleShape extends XSLFShape {
    private final XmlObject _shape;
    private final XSLFSheet _sheet;
    private CTShapeProperties _spPr;
    private CTNonVisualDrawingProps _nvPr;

    /*package*/ XSLFSimpleShape(XmlObject shape, XSLFSheet sheet){
        _shape = shape;
        _sheet = sheet;
    }

    public XmlObject getXmlObject(){
        return _shape;
    }
    
    public XSLFSheet getSheet(){
        return _sheet;
    }
    /**
     * TODO match STShapeType with {@link org.apache.poi.sl.usermodel.ShapeTypes}
     */
    public int getShapeType() {
        STShapeType.Enum stEnum = getSpPr().getPrstGeom().getPrst();
        return stEnum.intValue();
    }

    public String getShapeName() {
        return getNvPr().getName();
    }

    public int getShapeId() {
        return (int)getNvPr().getId();
    }

    protected CTNonVisualDrawingProps getNvPr(){
        if(_nvPr == null){
            XmlObject[] rs = _shape.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr");
            if(rs.length != 0) {
                _nvPr = (CTNonVisualDrawingProps)rs[0];
            }
        }
        return _nvPr;
    }

    protected CTShapeProperties getSpPr(){
        if(_spPr == null) {
            for(XmlObject obj : _shape.selectPath("*")){
                if(obj instanceof CTShapeProperties){
                    _spPr = (CTShapeProperties)obj;
                }
            }
        }
        if(_spPr == null) {
            throw new IllegalStateException("CTShapeProperties was not found.");
        }
        return _spPr;
    }

    public Rectangle2D getAnchor(){
        CTTransform2D xfrm = getSpPr().getXfrm();
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
        CTTransform2D xfrm = getSpPr().isSetXfrm() ? getSpPr().getXfrm() : getSpPr().addNewXfrm();
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
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    public void setRotation(double theta){
        CTTransform2D xfrm = getSpPr().getXfrm();
        xfrm.setRot((int)(theta*60000));
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
        CTTransform2D xfrm = getSpPr().getXfrm();
        return (double)xfrm.getRot()/60000;
    }

    public void setFlipHorizontal(boolean flip){
        CTTransform2D xfrm = getSpPr().getXfrm();
        xfrm.setFlipH(flip);
    }

    public void setFlipVertical(boolean flip){
        CTTransform2D xfrm = getSpPr().getXfrm();
        xfrm.setFlipV(flip);
    }
    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public boolean getFlipHorizontal(){
         return getSpPr().getXfrm().getFlipH();
    }

    public boolean getFlipVertical(){
         return getSpPr().getXfrm().getFlipV();
    }

    public void setLineColor(Color color){
        CTShapeProperties spPr = getSpPr();
        if(color == null) {
            if(spPr.isSetLn() && spPr.getLn().isSetSolidFill()) spPr.getLn().unsetSolidFill();
        }
        else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue()});

            CTSolidColorFillProperties fill = ln.isSetSolidFill() ? ln.getSolidFill() : ln.addNewSolidFill();
            fill.setSrgbClr(rgb);
        }
    }

    public Color getLineColor(){
        CTShapeProperties spPr = getSpPr();
        if(!spPr.isSetLn() || !spPr.getLn().isSetSolidFill()) return null;

        CTSRgbColor rgb = spPr.getLn().getSolidFill().getSrgbClr();
        byte[] val = rgb.getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

    public void setLineWidth(double width){
        CTShapeProperties spPr = getSpPr();
        if(width == 0.) {
            if(spPr.isSetLn()) spPr.getLn().unsetW();
        }
        else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
            ln.setW(Units.toEMU(width));
        }
    }

    public double getLineWidth(){
        CTShapeProperties spPr = getSpPr();
        CTLineProperties ln = spPr.getLn();
        if(ln == null || !ln.isSetW()) return 0;

        return Units.toPoints(ln.getW());
    }

    public void setLineDash(LineDash dash){
        CTShapeProperties spPr = getSpPr();
        if(dash == null) {
            if(spPr.isSetLn()) spPr.getLn().unsetPrstDash();
        }
        else {
            CTPresetLineDashProperties val = CTPresetLineDashProperties.Factory.newInstance();
            val.setVal(STPresetLineDashVal.Enum.forInt(dash.ordinal() + 1));
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
            ln.setPrstDash(val);
        }
    }

    public LineDash getLineDash(){
        CTShapeProperties spPr = getSpPr();
        CTLineProperties ln = spPr.getLn();
        if(ln == null || !ln.isSetPrstDash()) return null;

        CTPresetLineDashProperties dash = ln.getPrstDash();
        return LineDash.values()[dash.getVal().intValue() - 1];
    }

    public void setLineCap(LineCap cap){
        CTShapeProperties spPr = getSpPr();
        if(cap == null) {
            if(spPr.isSetLn()) spPr.getLn().unsetCap();
        }
        else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
            ln.setCap(STLineCap.Enum.forInt(cap.ordinal() + 1));
        }
    }

    public LineCap getLineCap(){
        CTShapeProperties spPr = getSpPr();
        CTLineProperties ln = spPr.getLn();
        if(ln == null || !ln.isSetCap()) return null;

        return LineCap.values()[ln.getCap().intValue() - 1];
    }

}
