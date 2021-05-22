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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.Internal;

@Internal
public class DrawTexturePaint extends java.awt.TexturePaint {
    private final ImageRenderer imgRdr;
    private final PaintStyle.TexturePaint fill;
    private final Shape shape;
    private final double flipX, flipY;
    private final boolean isBitmapSrc;

    private static final Insets2D INSETS_EMPTY = new Insets2D(0,0,0,0);


    DrawTexturePaint(ImageRenderer imgRdr, BufferedImage txtr, Shape shape, PaintStyle.TexturePaint fill, double flipX, double flipY, boolean isBitmapSrc) {
        // deactivate scaling/translation in super class, by specifying the dimension of the texture
        super(txtr, new Rectangle2D.Double(0,0,txtr.getWidth(),txtr.getHeight()));
        this.imgRdr = imgRdr;
        this.fill = fill;
        this.shape = shape;
        this.flipX = flipX;
        this.flipY = flipY;
        this.isBitmapSrc = isBitmapSrc;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {

        final Dimension2D userDim = new Dimension2DDouble();
        final Rectangle2D usedBounds;
        if (fill.isRotatedWithShape() || shape == null) {
            usedBounds = userBounds;
        } else {
            AffineTransform transform = new AffineTransform(xform);

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
            usedBounds = transform.createTransformedShape(shape).getBounds2D();
        }
        userDim.setSize(usedBounds.getWidth(), usedBounds.getHeight());
        xform.translate(usedBounds.getX(), usedBounds.getY());

        BufferedImage bi = getImage(usedBounds);

        if (fill.getStretch() != null) {
            TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, bi.getWidth(), bi.getHeight()));
            return tp.createContext(cm, deviceBounds, usedBounds, xform, hints);
        } else if (fill.getScale() != null) {
            AffineTransform newXform = getTiledInstance(usedBounds, (AffineTransform) xform.clone());
            TexturePaint tp = new TexturePaint(bi, new Rectangle2D.Double(0, 0, bi.getWidth(), bi.getHeight()));
            return tp.createContext(cm, deviceBounds, userBounds, newXform, hints);
        } else {
            return super.createContext(cm, deviceBounds, userBounds, xform, hints);
        }
    }

    public BufferedImage getImage(Rectangle2D userBounds) {
        BufferedImage bi = super.getImage();
        final Insets2D insets = fill.getInsets();
        final Insets2D stretch = fill.getStretch();

        if ((insets == null || INSETS_EMPTY.equals(insets)) && (stretch == null) || userBounds == null || userBounds.isEmpty()) {
            return bi;
        }

        if (insets != null && !INSETS_EMPTY.equals(insets)) {
            final int width = bi.getWidth();
            final int height = bi.getHeight();

            bi = bi.getSubimage(
                (int)(Math.max(insets.left,0)/100_000 * width),
                (int)(Math.max(insets.top,0)/100_000 * height),
                (int)((100_000-Math.max(insets.left,0)-Math.max(insets.right,0))/100_000 * width),
                (int)((100_000-Math.max(insets.top,0)-Math.max(insets.bottom,0))/100_000 * height)
            );

            int addTop = (int)(Math.max(-insets.top, 0)/100_000 * height);
            int addLeft = (int)(Math.max(-insets.left, 0)/100_000 * width);
            int addBottom = (int)(Math.max(-insets.bottom, 0)/100_000 * height);
            int addRight = (int)(Math.max(-insets.right, 0)/100_000 * width);

            // handle outsets
            if (addTop > 0 || addLeft > 0 || addBottom > 0 || addRight > 0) {
                int[] buf = new int[bi.getWidth()*bi.getHeight()];
                bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), buf, 0, bi.getWidth());
                BufferedImage borderBi = new BufferedImage(bi.getWidth()+addLeft+addRight, bi.getHeight()+addTop+addBottom, bi.getType());
                borderBi.setRGB(addLeft, addTop, bi.getWidth(), bi.getHeight(), buf, 0, bi.getWidth());
                bi = borderBi;
            }
        }

        if (stretch != null) {
            Rectangle2D srcBounds = new Rectangle2D.Double(
                0, 0, bi.getWidth(), bi.getHeight()
            );

            Rectangle2D dstBounds = new Rectangle2D.Double(
                stretch.left/100_000 * userBounds.getWidth(),
                stretch.top/100_000 * userBounds.getHeight(),
                (100_000-stretch.left-stretch.right)/100_000 * userBounds.getWidth(),
                (100_000-stretch.top-stretch.bottom)/100_000 * userBounds.getHeight()
            );

            BufferedImage stretchBi = new BufferedImage((int)userBounds.getWidth(), (int)userBounds.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = stretchBi.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, stretchBi.getWidth(), stretchBi.getHeight());
            g.setComposite(AlphaComposite.SrcOver);

            AffineTransform at = new AffineTransform();
            at.translate(dstBounds.getCenterX(), dstBounds.getCenterY());
            at.scale(dstBounds.getWidth()/srcBounds.getWidth(), dstBounds.getHeight()/srcBounds.getHeight());
            at.translate(-srcBounds.getCenterX(), -srcBounds.getCenterY());

            g.drawRenderedImage(bi, at);

            g.dispose();

            bi = stretchBi;
        }

        return bi;
    }

    private AffineTransform getTiledInstance(final Rectangle2D usedBounds, final AffineTransform xform) {
        final BufferedImage bi = getImage();
        final Dimension2D scale = fill.getScale();
        assert(scale != null);
        final double img_w = bi.getWidth() * (scale.getWidth() == 0 ? 1 : scale.getWidth())/flipX;
        final double img_h = bi.getHeight() * (scale.getHeight() == 0 ? 1 : scale.getHeight())/flipY;

        // Alignment happens after the scaling but before any offset.
        PaintStyle.TextureAlignment ta = fill.getAlignment();
        final double alg_x, alg_y;
        final double usr_w = usedBounds.getWidth(), usr_h = usedBounds.getHeight();
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

        xform.scale(scale.getWidth()/(isBitmapSrc ? flipX : 1.),scale.getHeight()/(isBitmapSrc ? flipY : 1.));

        return xform;
    }

    public ImageRenderer getImageRenderer() {
        return imgRdr;
    }

    public PaintStyle.TexturePaint getFill() {
        return fill;
    }

    public Shape getAwtShape() {
        return shape;
    }
}
