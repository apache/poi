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

package org.apache.poi.sl.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;


public class DrawShape implements Drawable {

    protected final Shape<?,?> shape;

    public DrawShape(Shape<?,?> shape) {
        this.shape = shape;
    }

    /**
     * Sometimes it's necessary to distinguish between XSLF/HSLF for the rendering.
     * Use this method on the shape to determine, if we work on the BIFF implementation
     *
     * @param shape the shape to render
     * @return {@code true} if HSLF implementation is used
     */
    protected static boolean isHSLF(Shape<?,?> shape) {
        return shape.getClass().getCanonicalName().toLowerCase(Locale.ROOT).contains("hslf");
    }
    
    /**
     * Apply 2-D transforms before drawing this shape. This includes rotation and flipping.
     *
     * @param graphics the graphics whos transform matrix will be modified
     */
    @Override
    public void applyTransform(Graphics2D graphics) {
        if (!(shape instanceof PlaceableShape)) {
            return;
        }

        PlaceableShape<?,?> ps = (PlaceableShape<?,?>)shape;
        final boolean isHSLF = isHSLF(shape);
        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(Drawable.GROUP_TRANSFORM);
        if (tx == null) {
            tx = new AffineTransform();
        }
        final Rectangle2D anchor = tx.createTransformedShape(ps.getAnchor()).getBounds2D();

        char cmds[] = isHSLF ? new char[]{ 'h','v','r' } : new char[]{ 'r','h','v' };
        for (char ch : cmds) {
            switch (ch) {
            case 'h':
                //flip horizontal
                if (ps.getFlipHorizontal()) {
                    graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
                    graphics.scale(-1, 1);
                    graphics.translate(-anchor.getX(), -anchor.getY());
                }
                break;
            case 'v':
                //flip vertical
                if (ps.getFlipVertical()) {
                    graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
                    graphics.scale(1, -1);
                    graphics.translate(-anchor.getX(), -anchor.getY());
                }
                break;
            case 'r':
                // rotation
                double rotation = ps.getRotation();
                if (rotation != 0.) {
                    // PowerPoint rotates shapes relative to the geometric center
                    double centerX = anchor.getCenterX();
                    double centerY = anchor.getCenterY();

                    // normalize rotation
                    rotation %= 360.;
                    if (rotation < 0) {
                        rotation += 360.;
                    }

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

                        AffineTransform txs;
                        if (isHSLF) {
                            txs = new AffineTransform(tx);
                        } else {
                            // this handling is only based on try and error ... not sure why xslf is handled differently.
                            txs = new AffineTransform();
                            txs.translate(centerX, centerY);
                            txs.rotate(Math.PI/2.); // actually doesn't matter if +/- 90 degrees
                            txs.translate(-centerX, -centerY);
                            txs.concatenate(tx);
                        }

                        txs.translate(centerX, centerY);
                        txs.rotate(Math.PI/2.);
                        txs.translate(-centerX, -centerY);

                        Rectangle2D anchor2 = txs.createTransformedShape(ps.getAnchor()).getBounds2D();

                        scaleX = safeScale(anchor.getWidth(), anchor2.getWidth());
                        scaleY = safeScale(anchor.getHeight(), anchor2.getHeight());
                    } else {
                        quadrant = 0;
                    }

                    // transformation is applied reversed ...
                    graphics.translate(centerX, centerY);
                    double rot = Math.toRadians(rotation-quadrant*90.);
                    if (rot != 0) {
                        graphics.rotate(rot);
                    }
                    graphics.scale(scaleX, scaleY);
                    rot = Math.toRadians(quadrant*90.);
                    if (rot != 0) {
                        graphics.rotate(rot);
                    }
                    graphics.translate(-centerX, -centerY);
                }
                break;
            default:
                throw new RuntimeException("unexpected transform code " + ch);
            }
        }
    }

    private static double safeScale(double dim1, double dim2) {
        if (dim1 == 0.) {
            return 1;
        }
        return (dim2 == 0.) ? 1 : dim1/dim2;
    }

    @Override
    public void draw(Graphics2D graphics) {
    }

    @Override
    public void drawContent(Graphics2D graphics) {
    }

    public static Rectangle2D getAnchor(Graphics2D graphics, PlaceableShape<?,?> shape) {
        return getAnchor(graphics, shape.getAnchor());
    }

    public static Rectangle2D getAnchor(Graphics2D graphics, Rectangle2D anchor) {
        if(graphics == null)  {
            return anchor;
        }

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(Drawable.GROUP_TRANSFORM);
        if(tx != null) {
            anchor = tx.createTransformedShape(anchor).getBounds2D();
        }
        return anchor;
    }
    
    protected Shape<?,?> getShape() {
        return shape;
    }
    
    protected static BasicStroke getStroke(StrokeStyle strokeStyle) {
        float lineWidth = (float) strokeStyle.getLineWidth();
        if (lineWidth == 0.0f) {
            // Both PowerPoint and OOo draw zero-length lines as 0.25pt
            lineWidth = 0.25f;
        }

        LineDash lineDash = strokeStyle.getLineDash();
        if (lineDash == null) {
            lineDash = LineDash.SOLID;
        }

        int dashPatI[] = lineDash.pattern;
        final float dash_phase = 0;
        float[] dashPatF = null;
        if (dashPatI != null) {
            dashPatF = new float[dashPatI.length];
            for (int i=0; i<dashPatI.length; i++) {
                dashPatF[i] = dashPatI[i]*Math.max(1, lineWidth);
            }
        }

        LineCap lineCapE = strokeStyle.getLineCap();
        if (lineCapE == null) {
            lineCapE = LineCap.FLAT;
        }
        int lineCap;
        switch (lineCapE) {
            case ROUND:
                lineCap = BasicStroke.CAP_ROUND;
                break;
            case SQUARE:
                lineCap = BasicStroke.CAP_SQUARE;
                break;
            default:
            case FLAT:
                lineCap = BasicStroke.CAP_BUTT;
                break;
        }

        int lineJoin = BasicStroke.JOIN_ROUND;

        return new BasicStroke(lineWidth, lineCap, lineJoin, lineWidth, dashPatF, dash_phase);
    }
}
