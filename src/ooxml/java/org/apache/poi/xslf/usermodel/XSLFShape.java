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
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Base super-class class for all shapes in PresentationML
 *
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFShape {

    /**
     * @return the position of this shape within the drawing canvas.
     *         The coordinates are expressed in points
     */
    public abstract Rectangle2D getAnchor();

    /**
     * @param anchor the position of this shape within the drawing canvas.
     *               The coordinates are expressed in points
     */
    public abstract void setAnchor(Rectangle2D anchor);

    /**
     * @return the xml bean holding this shape's data
     */
    public abstract XmlObject getXmlObject();

    /**
     * @return human-readable name of this shape, e.g. "Rectange 3"
     */
    public abstract String getShapeName();

    /**
     * Returns a unique identifier for this shape within the current document.
     * This ID may be used to assist in uniquely identifying this object so that it can
     * be referred to by other parts of the document.
     * <p>
     * If multiple objects within the same document share the same id attribute value,
     * then the document shall be considered non-conformant.
     * </p>
     *
     * @return unique id of this shape
     */
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

    /**
     * @param flip whether the shape is horizontally flipped
     */
    public abstract void setFlipHorizontal(boolean flip);

    /**
     * Whether the shape is vertically flipped
     *
     * @param flip whether the shape is vertically flipped
     */
    public abstract void setFlipVertical(boolean flip);

    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public abstract boolean getFlipHorizontal();

    /**
     * Whether the shape is vertically flipped
     *
     * @return whether the shape is vertically flipped
     */
    public abstract boolean getFlipVertical();

    /**
     * Draw this shape into the supplied canvas
     *
     * @param graphics the graphics to draw into
     */
    public abstract void draw(Graphics2D graphics);

    /**
     * Apply 2-D transforms before drawing this shape. This includes rotation and flipping.
     *
     * @param graphics the graphics whos transform matrix will be modified
     */
    protected void applyTransform(Graphics2D graphics) {
        Rectangle2D anchor = getAnchor();
        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(XSLFRenderingHint.GROUP_TRANSFORM);
        if(tx != null) {
            anchor = tx.createTransformedShape(anchor).getBounds2D();
        }

        // rotation
        double rotation = getRotation();
        if (rotation != 0.) {
            // PowerPoint rotates shapes relative to the geometric center
            double centerX = anchor.getX() + anchor.getWidth() / 2;
            double centerY = anchor.getY() + anchor.getHeight() / 2;

            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(rotation));
            graphics.translate(-centerX, -centerY);
        }

        //flip horizontal
        if (getFlipHorizontal()) {
            graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
            graphics.scale(-1, 1);
            graphics.translate(-anchor.getX(), -anchor.getY());
        }

        //flip vertical
        if (getFlipVertical()) {
            graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
            graphics.scale(1, -1);
            graphics.translate(-anchor.getX(), -anchor.getY());
        }
    }

    /**
     * Set the contents of this shape to be a copy of the source shape.
     * This method is called recursively for each shape when merging slides
     *
     * @param  sh the source shape
     * @see org.apache.poi.xslf.usermodel.XSLFSlide#importContent(XSLFSheet)
     */
    @Internal
    void copy(XSLFShape sh) {
        if (!getClass().isInstance(sh)) {
            throw new IllegalArgumentException(
                    "Can't copy " + sh.getClass().getSimpleName() + " into " + getClass().getSimpleName());
        }

        setAnchor(sh.getAnchor());
    }
}