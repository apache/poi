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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.RectAlign;


public class DrawPictureShape extends DrawSimpleShape {
    private static final Logger LOG = LogManager.getLogger(DrawPictureShape.class);

    public DrawPictureShape(PictureShape<?,?> shape) {
        super(shape);
    }

    @Override
    public void drawContent(Graphics2D graphics) {
        PictureShape<?,?> ps = getShape();

        Rectangle2D anchor = getAnchor(graphics, ps);
        Insets insets = ps.getClipping();

        PictureData[] pics = { ps.getAlternativePictureData(), ps.getPictureData() };
        for (PictureData data : pics) {
            if (data == null) {
                continue;
            }

            try {
                byte[] dataBytes = data.getData();

                PictureType type = PictureType.valueOf(FileMagic.valueOf(dataBytes));
                String ct = (type == PictureType.UNKNOWN) ? data.getContentType() : type.getContentType();

                ImageRenderer renderer = getImageRenderer(graphics, ct);
                if (renderer.canRender(ct)) {
                    renderer.loadImage(dataBytes, ct);
                    renderer.drawImage(graphics, anchor, insets);
                    return;
                }
            } catch (IOException e) {
                LOG.atError().withThrowable(e).log("image can't be loaded/rendered.");
            }
        }
    }

    /**
     * Returns an ImageRenderer for the PictureData
     *
     * @param graphics the graphics context
     * @return the image renderer
     */
    public static ImageRenderer getImageRenderer(Graphics2D graphics, String contentType) {
        final ImageRenderer renderer = (graphics != null) ? (ImageRenderer)graphics.getRenderingHint(Drawable.IMAGE_RENDERER) : null;
        if (renderer != null && renderer.canRender(contentType)) {
            return renderer;
        }

        final BitmapImageRenderer fallback = new BitmapImageRenderer();
        if (fallback.canRender(contentType)) {
            return fallback;
        }

        ClassLoader cl = DrawPictureShape.class.getClassLoader();
        Iterator<ImageRenderer> iter = ServiceLoader.load(ImageRenderer.class, cl).iterator();
        for (;;) {
            // Batik / SVGImageRenderer fails when executed on the module-path, hence we try/skip it in that case
            try {
                ImageRenderer ir = iter.next();
                if (ir.canRender(contentType)) {
                    return ir;
                }
            } catch (NoSuchElementException ignored) {
                break;
            } catch (Exception | ServiceConfigurationError ignored) {
            }
        }

        // the fallback is the BitmapImageRenderer, at least it gracefully handles invalid images
        LOG.atWarn().log("No suitable image renderer found for content-type '{}' - include " +
            "poi-scratchpad (for wmf/emf) or poi-ooxml (for svg) jars - " +
            "svgs/batik doesn't work on the module-path!", contentType);
        return fallback;
    }

    @Override
    protected Paint getFillPaint(Graphics2D graphics) {
        return null;
    }

    @Override
    protected PictureShape<?,?> getShape() {
        return (PictureShape<?,?>)shape;
    }

    /**
     * Resize this picture to the default size.
     *
     * For PNG and JPEG resizes the image to 100%,
     * for other types, if the size can't be determined it will be 200x200 pixels.
     */
    public void resize() {
        PictureShape<?,?> ps = getShape();
        Dimension dim = ps.getPictureData().getImageDimension();

        Rectangle2D origRect = ps.getAnchor();
        double x = origRect.getX();
        double y = origRect.getY();
        double w = dim.getWidth();
        double h = dim.getHeight();
        ps.setAnchor(new Rectangle2D.Double(x, y, w, h));
    }


    /**
     * Fit picture shape into the target rectangle, maintaining the aspect ratio
     * and repositioning within the target rectangle with a centered alignment.
     *
     * @param target    The target rectangle
     */
    public void resize(Rectangle2D target) {
        resize(target, RectAlign.CENTER);
    }


    /**
     * Fit picture shape into the target rectangle, maintaining the aspect ratio
     * and repositioning within the target rectangle based on the specified
     * alignment (gravity).
     *
     * @param target    The target rectangle
     * @param align
     *            The alignment within the target rectangle when resizing.
     *            A null value corresponds to RectAlign.CENTER
     */
    public void resize(Rectangle2D target, RectAlign align) {
        PictureShape<?,?> ps = getShape();
        Dimension dim = ps.getPictureData().getImageDimension();
        if (dim.width <= 0 || dim.height <= 0) {
            // nothing useful to be done for this case
            ps.setAnchor(target);
            return;
        }

        double w = target.getWidth();
        double h = target.getHeight();

        // scaling
        double sx = w / dim.width;
        double sy = h / dim.height;

        // position adjustments
        double dx = 0, dy = 0;

        if (sx > sy) {
            // use y-scaling for both, reposition x accordingly
            w  = sy * dim.width;
            dx = target.getWidth() - w;
        } else if (sy > sx) {
            // use x-scaling for both, reposition y accordingly
            h  = sx * dim.height;
            dy = target.getHeight() - h;
        } else {
            // uniform scaling, can use target values directly
            ps.setAnchor(target);
            return;
        }

        // the positioning
        double x = target.getX();
        double y = target.getY();
        switch (align) {
            case TOP:           // X=balance, Y=ok
                x += dx/2;
                break;
            case TOP_RIGHT:     // X=shift, Y=ok
                x += dx;
                break;
            case RIGHT:         // X=shift, Y=balance
                x += dx;
                y += dy/2;
                break;
            case BOTTOM_RIGHT:  // X=shift, Y=shift
                x += dx;
                y += dy;
                break;
            case BOTTOM:        // X=balance, Y=shift
                x += dx/2;
                y += dy;
                break;
            case BOTTOM_LEFT:   // X=ok, Y=shift
                y += dy;
                break;
            case LEFT:          // X=ok, Y=balance
                y += dy/2;
                break;
            case TOP_LEFT:      // X=ok, Y=ok
                /* no-op */
                break;
            default:            // CENTER: X=balance, Y=balance
                x += dx/2;
                y += dy/2;
                break;
        }

        ps.setAnchor(new Rectangle2D.Double(x, y, w, h));
    }
}
