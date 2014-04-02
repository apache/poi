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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlObject;

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
            double centerX = anchor.getCenterX();
            double centerY = anchor.getCenterY();

            // normalize rotation
            rotation = (360.+(rotation%360.))%360.;
            int quadrant = (((int)rotation+45)/90)%4;
            double scaleX = 1.0, scaleY = 1.0;

            // scale to bounding box (bug #53176)
            if (quadrant == 1 || quadrant == 3) {
                // In quadrant 1 and 3, which is basically a shape in a more or less portrait orientation 
                // (45-135 degrees and 225-315 degrees), we need to first rotate the shape by a multiple 
                // of 90 degrees and then resize the bounding box to its original bbox. After that we can 
                // rotate the shape to the exact rotation amount.
                // It's strange that you'll need to rotate the shape back and forth again, but you can
                // think of it, as if you paint the shape on a canvas. First you rotate the canvas, which might
                // be already (differently) scaled, so you can paint the shape in its default orientation
                // and later on, turn it around again to compare it with its original size ...
                AffineTransform txg = new AffineTransform(); // graphics coordinate space
                AffineTransform txs = new AffineTransform(tx); // shape coordinate space
                txg.translate(centerX, centerY);
                txg.rotate(Math.toRadians(quadrant*90));
                txg.translate(-centerX, -centerY);
                txs.translate(centerX, centerY);
                txs.rotate(Math.toRadians(-quadrant*90));
                txs.translate(-centerX, -centerY);
                txg.concatenate(txs);
                Rectangle2D anchor2 = txg.createTransformedShape(getAnchor()).getBounds2D();
                scaleX = anchor.getWidth() == 0. ? 1.0 : anchor.getWidth() / anchor2.getWidth();
                scaleY = anchor.getHeight() == 0. ? 1.0 : anchor.getHeight() / anchor2.getHeight();
            }

            // transformation is applied reversed ...
            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(rotation-(double)(quadrant*90)));
            graphics.scale(scaleX, scaleY);
            graphics.rotate(Math.toRadians(quadrant*90));
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