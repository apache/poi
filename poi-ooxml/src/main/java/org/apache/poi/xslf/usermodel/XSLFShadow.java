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

package org.apache.poi.xslf.usermodel;

import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Represents a shadow of a shape. For now supports only outer shadows.
 *
 * @author Yegor Kozlov
 */
public class XSLFShadow extends XSLFSimpleShape {

    private XSLFSimpleShape _parent;

    /* package */XSLFShadow(CTOuterShadowEffect shape, XSLFSimpleShape parentShape) {
        super(shape, parentShape.getSheet());

        _parent = parentShape;
    }


    public void fill(Graphics2D graphics, Shape outline) {

        double shapeRotation = _parent.getRotation();
        if(_parent.getFlipVertical()){
            shapeRotation += 180;
        }
        double angle = getAngle() - shapeRotation;
        double dist = getDistance();
        double dx = dist * Math.cos(Math.toRadians(angle));
        double dy = dist * Math.sin(Math.toRadians(angle));

        graphics.translate(dx, dy);

        Color fillColor = getFillColor();
        if (fillColor != null) {
            graphics.setColor(fillColor);
            graphics.fill(outline);
        }

        graphics.translate(-dx, -dy);
    }

    public void draw(Graphics2D graphics, Shape outline) {

        double angle = getAngle();
        double dist = getDistance();
        double dx = dist * Math.cos(Math.toRadians(angle));
        double dy = dist * Math.sin(Math.toRadians(angle));

        graphics.translate(dx, dy);

        Color fillColor = getFillColor();
        if (fillColor != null) {
            graphics.setColor(fillColor);
            graphics.draw(outline);
        }

        graphics.translate(-dx, -dy);
    }


    @Override
    public Rectangle2D getAnchor(){
        return _parent.getAnchor();
    }

    @Override
    public void setAnchor(Rectangle2D anchor){
        throw new IllegalStateException("You can't set anchor of a shadow");
    }

    /**
     * @return the offset of this shadow in points
     */
    public double getDistance(){
        CTOuterShadowEffect ct = (CTOuterShadowEffect)getXmlObject();
        return ct.isSetDist() ? Units.toPoints(ct.getDist()) : 0;        
    }

    /**
     * 
     * @return the direction to offset the shadow in angles
     */
    public double getAngle(){
        CTOuterShadowEffect ct = (CTOuterShadowEffect)getXmlObject();
        return ct.isSetDir() ? (double)ct.getDir() / 60000 : 0;
    }

    /**
     * 
     * @return the blur radius of the shadow
     * TODO: figure out how to make sense of this property when rendering shadows 
     */
    public double getBlur(){
        CTOuterShadowEffect ct = (CTOuterShadowEffect)getXmlObject();
        return ct.isSetBlurRad() ? Units.toPoints(ct.getBlurRad()) : 0;
    }

    /**
     * @return the color of this shadow. 
     * Depending whether the parent shape is filled or stroked, this color is used to fill or stroke this shadow
     */
    @Override
    public Color getFillColor() {
        XSLFTheme theme = getSheet().getTheme();
        CTOuterShadowEffect ct = (CTOuterShadowEffect)getXmlObject();
        if(ct == null) {
            return null;
        } else {
            CTSchemeColor phClr = ct.getSchemeClr();
            return new XSLFColor(ct, theme, phClr).getColor();
        }
    }
}