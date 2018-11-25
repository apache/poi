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

import java.awt.*;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.*;
import java.awt.image.*;

import org.apache.poi.util.Internal;

@Internal
class PathGradientPaint implements Paint {

    // http://asserttrue.blogspot.de/2010/01/how-to-iimplement-custom-paint-in-50.html
    private final Color[] colors;
    private final float[] fractions;
    private final int capStyle;
    private final int joinStyle;
    private final int transparency;

    
    PathGradientPaint(float[] fractions, Color[] colors) {
        this(fractions,colors,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
    }
    
    private PathGradientPaint(float[] fractions, Color[] colors, int capStyle, int joinStyle) {
        this.colors = colors.clone();
        this.fractions = fractions.clone();
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;

        // determine transparency
        boolean opaque = true;
        for (Color c : colors) {
            if (c != null) {
                opaque = opaque && (c.getAlpha() == 0xff);
            }
        }
        this.transparency = opaque ? OPAQUE : TRANSLUCENT;
    }
    
    public PaintContext createContext(ColorModel cm,
        Rectangle deviceBounds,
        Rectangle2D userBounds,
        AffineTransform transform,
        RenderingHints hints) {
        return new PathGradientContext(cm, deviceBounds, userBounds, transform, hints);
    }
    
    public int getTransparency() {
        return transparency;
    }

    class PathGradientContext implements PaintContext {
        final Rectangle deviceBounds;
        final Rectangle2D userBounds;
        protected final AffineTransform xform;
        final RenderingHints hints;

        /**
         * for POI: the shape will be only known when the subclasses determines the concrete implementation 
         * in the draw/-content method, so we need to postpone the setting/creation as long as possible
         **/
        protected final Shape shape;
        final PaintContext pCtx;
        final int gradientSteps;
        WritableRaster raster;

        PathGradientContext(
                ColorModel cm
                , Rectangle deviceBounds
                , Rectangle2D userBounds
                , AffineTransform xform
                , RenderingHints hints
        ) {
            shape = (Shape)hints.get(Drawable.GRADIENT_SHAPE);
            if (shape == null) {
                throw new IllegalPathStateException("PathGradientPaint needs a shape to be set via the rendering hint Drawable.GRADIANT_SHAPE.");
            }

            this.deviceBounds = deviceBounds;
            this.userBounds = userBounds;
            this.xform = xform;
            this.hints = hints;

            gradientSteps = getGradientSteps(shape);

            Point2D start = new Point2D.Double(0, 0);
            Point2D end = new Point2D.Double(gradientSteps, 0);
            LinearGradientPaint gradientPaint = new LinearGradientPaint(start, end, fractions, colors, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, new AffineTransform());
            
            Rectangle bounds = new Rectangle(0, 0, gradientSteps, 1);
            pCtx = gradientPaint.createContext(cm, bounds, bounds, new AffineTransform(), hints);
        }

        public void dispose() {}

        public ColorModel getColorModel() {
            return pCtx.getColorModel();
        }

        public Raster getRaster(int xOffset, int yOffset, int w, int h) {
            ColorModel cm = getColorModel();
            if (raster == null) createRaster();

            // TODO: eventually use caching here
            WritableRaster childRaster = cm.createCompatibleWritableRaster(w, h);
            Rectangle2D childRect = new Rectangle2D.Double(xOffset, yOffset, w, h);
            if (!childRect.intersects(deviceBounds)) {
                // usually doesn't happen ...
                return childRaster;
            }
            
            Rectangle2D destRect = new Rectangle2D.Double();
            Rectangle2D.intersect(childRect, deviceBounds, destRect);
            int dx = (int)(destRect.getX()-deviceBounds.getX());
            int dy = (int)(destRect.getY()-deviceBounds.getY());
            int dw = (int)destRect.getWidth();
            int dh = (int)destRect.getHeight();
            Object data = raster.getDataElements(dx, dy, dw, dh, null);
            dx = (int)(destRect.getX()-childRect.getX());
            dy = (int)(destRect.getY()-childRect.getY());
            childRaster.setDataElements(dx, dy, dw, dh, data);
            
            return childRaster;
        }

        int getGradientSteps(Shape gradientShape) {
            Rectangle rect = gradientShape.getBounds();
            int lower = 1;
            int upper = (int)(Math.max(rect.getWidth(),rect.getHeight())/2.0);
            while (lower < upper-1) {
                int mid = lower + (upper - lower) / 2;
                BasicStroke bs = new BasicStroke(mid, capStyle, joinStyle);
                Area area = new Area(bs.createStrokedShape(gradientShape));
                if (area.isSingular()) {
                    upper = mid;
                } else {
                    lower = mid;
                }
            }
            return upper;
        }
        
        
        
        void createRaster() {
            ColorModel cm = getColorModel();
            raster = cm.createCompatibleWritableRaster((int)deviceBounds.getWidth(), (int)deviceBounds.getHeight());
            BufferedImage img = new BufferedImage(cm, raster, false, null);
            Graphics2D graphics = img.createGraphics();
            graphics.setRenderingHints(hints);
            graphics.translate(-deviceBounds.getX(), -deviceBounds.getY());
            graphics.transform(xform);

            Raster img2 = pCtx.getRaster(0, 0, gradientSteps, 1);
            int[] rgb = new int[cm.getNumComponents()];

            for (int i = gradientSteps-1; i>=0; i--) {
                img2.getPixel(i, 0, rgb);
                Color c = new Color(rgb[0],rgb[1],rgb[2]);
                if (rgb.length == 4) {
                    // it doesn't work to use just a color with transparency ...
                    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, rgb[3]/255.0f));                           
                }
                graphics.setStroke(new BasicStroke(i+1, capStyle, joinStyle));
                graphics.setColor(c);
                graphics.draw(shape);
            }
            
            graphics.dispose();
        }
    }
}
