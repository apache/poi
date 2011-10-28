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

import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFShape {


    public abstract Rectangle2D getAnchor();

    public abstract void setAnchor(Rectangle2D anchor);

    public abstract XmlObject getXmlObject();

    public abstract String getShapeName();

    public abstract int getShapeId();

    /**
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    public abstract void setRotation(double theta);
   
    /**
     * Rotation angle in degrees
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @return rotation angle in degrees
     */
    public abstract double getRotation();

    public abstract void setFlipHorizontal(boolean flip);

    public abstract void setFlipVertical(boolean flip);
    
    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public abstract boolean getFlipHorizontal();

    public abstract boolean getFlipVertical();

    public abstract void draw(Graphics2D graphics);

    protected java.awt.Shape getOutline(){
        return getAnchor();
    }
    
    protected void applyTransform(Graphics2D graphics){
        Rectangle2D anchor = getAnchor();

        // rotation
        double rotation = getRotation();
        if(rotation != 0.) {
        	// PowerPoint rotates shapes relative to the geometric center
            double centerX = anchor.getX() + anchor.getWidth()/2;
            double centerY = anchor.getY() + anchor.getHeight()/2;

            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(rotation));
            graphics.translate(-centerX, -centerY);
        }

        //flip horizontal
        if(getFlipHorizontal()){
            graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
            graphics.scale(-1, 1);
            graphics.translate(-anchor.getX() , -anchor.getY());
        }

        //flip vertical
        if(getFlipVertical()){
            graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
            graphics.scale(1, -1);
            graphics.translate(-anchor.getX(), -anchor.getY());
        }
    }
    
}