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

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.apache.poi.sl.usermodel.PaintStyle;

/* package */ class DrawTexturePaint extends java.awt.TexturePaint {
    private final PaintStyle.TexturePaint fill;
    private final Shape shape;
    private final double flipX, flipY;
    private final boolean isBitmapSrc;

    DrawTexturePaint(BufferedImage txtr, Shape shape, PaintStyle.TexturePaint fill, double flipX, double flipY, boolean isBitmapSrc) {
        // deactivate scaling/translation in super class, by specifying the dimension of the texture
        super(txtr, new Rectangle2D.Double(0,0,txtr.getWidth(),txtr.getHeight()));
        this.fill = fill;
        this.shape = shape;
        this.flipX = flipX;
        this.flipY = flipY;
        this.isBitmapSrc = isBitmapSrc;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {

        final double usr_w, usr_h;

        if (fill.isRotatedWithShape() || shape == null) {
            usr_w = userBounds.getWidth();
            usr_h = userBounds.getHeight();

            xform.translate(userBounds.getX(), userBounds.getY());
        } else {
            AffineTransform	transform = new AffineTransform(xform);

            // Eliminate any post-translation
            transform.preConcatenate(AffineTransform.getTranslateInstance(
                    -transform.getTranslateX(), -transform.getTranslateY()));
            Point2D p1 = new Point2D.Double(1, 0);
            p1 = transform.transform(p1,p1);

            final double rad = Math.atan2(p1.getY(),p1.getX());

            if (rad != 0) {
                xform.rotate(-rad, userBounds.getCenterX(), userBounds.getCenterY());
            }

            // TODO: check if approximation via rotating only the bounds (instead of the shape) is sufficient
            transform = AffineTransform.getRotateInstance(rad, userBounds.getCenterX(), userBounds.getCenterY());
            Rectangle2D newBounds = transform.createTransformedShape(shape).getBounds2D();
            usr_w = newBounds.getWidth();
            usr_h = newBounds.getHeight();

            xform.translate(newBounds.getX(), newBounds.getY());
        }

        final Dimension2D scale = fill.getScale();

        final BufferedImage bi = getImage();
        final double img_w = bi.getWidth() * (scale == null ? 1 : scale.getWidth())/flipX;
        final double img_h = bi.getHeight() * (scale == null ? 1 : scale.getHeight())/flipY;

        // Alignment happens after the scaling but before any offset.
        PaintStyle.TextureAlignment ta = fill.getAlignment();
        final double alg_x, alg_y;
        switch (ta == null ? PaintStyle.TextureAlignment.TOP_LEFT : ta) {
            case BOTTOM:
                alg_x = (usr_w-img_w)/2;
                alg_y = usr_h-img_h;
                break;
            case BOTTOM_LEFT:
                alg_x = 0;
                alg_y = usr_h-img_h;
                break;
            case BOTTOM_RIGHT:
                alg_x = usr_w-img_w;
                alg_y = usr_h-img_h;
                break;
            case CENTER:
                alg_x = (usr_w-img_w)/2;
                alg_y = (usr_h-img_h)/2;
                break;
            case LEFT:
                alg_x = 0;
                alg_y = (usr_h-img_h)/2;
                break;
            case RIGHT:
                alg_x = usr_w-img_w;
                alg_y = (usr_h-img_h)/2;
                break;
            case TOP:
                alg_x = (usr_w-img_w)/2;
                alg_y = 0;
                break;
            default:
            case TOP_LEFT:
                alg_x = 0;
                alg_y = 0;
                break;
            case TOP_RIGHT:
                alg_x = usr_w-img_w;
                alg_y = 0;
                break;
        }
        xform.translate(alg_x, alg_y);

        // Apply additional horizontal/vertical offset after alignment.
        // Values are as percentages.

        // TODO: apply scaling of drawing context to offset
        final Point2D offset = fill.getOffset();

        if (offset != null) {
            xform.translate(offset.getX(),offset.getY());
        }

        if (scale != null) {
            xform.scale(scale.getWidth()/(isBitmapSrc ? flipX : 1.),scale.getHeight()/(isBitmapSrc ? flipY : 1.));
        }

        return super.createContext(cm, deviceBounds, userBounds, xform, hints);
    }
}
