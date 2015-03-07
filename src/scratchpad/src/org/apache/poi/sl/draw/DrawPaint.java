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
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.sl.usermodel.*;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;


public class DrawPaint {
    public final static Color NO_PAINT = new Color(0xFF, 0xFF, 0xFF, 0);
    private final static POILogger LOG = POILogFactory.getLogger(DrawPaint.class);

    protected PlaceableShape shape;
    
    public DrawPaint(PlaceableShape shape) {
        this.shape = shape;
    }
    
    public Paint getPaint(Graphics2D graphics, PaintStyle paint) {
        if (paint instanceof SolidPaint) {
            return getSolidPaint((SolidPaint)paint, graphics);
        } else if (paint instanceof GradientPaint) {
            return getGradientPaint((GradientPaint)paint, graphics);
        } else if (paint instanceof TexturePaint) {
            return getTexturePaint((TexturePaint)paint, graphics);
        }
        return null;
    }
    
    protected Paint getSolidPaint(SolidPaint fill, Graphics2D graphics) {
        return applyColorTransform(fill.getSolidColor());
    }

    protected Paint getGradientPaint(GradientPaint fill, Graphics2D graphics) {
        switch (fill.getGradientType()) {
        case linear:
            return createLinearGradientPaint(fill, graphics);
        case circular:
            return createRadialGradientPaint(fill, graphics);
        case shape:
            return createPathGradientPaint(fill, graphics);
        default:
            throw new UnsupportedOperationException("gradient fill of type "+fill+" not supported.");
        }
    }

    protected Paint getTexturePaint(TexturePaint fill, Graphics2D graphics) {
        InputStream is = fill.getImageData();
        if (is == null) return NO_PAINT;
        assert(graphics != null);
        
        ImageRenderer renderer = (ImageRenderer)graphics.getRenderingHint(Drawable.IMAGE_RENDERER);
        if (renderer == null) renderer = new ImageRenderer();

        try {
            renderer.loadImage(fill.getImageData(), fill.getContentType());
        } catch (IOException e) {
            LOG.log(POILogger.ERROR, "Can't load image data - using transparent color", e);
            return NO_PAINT;
        }

        int alpha = fill.getAlpha();
        if (alpha != -1) {
            renderer.setAlpha(fill.getAlpha()/100000.f);
        }
        
        Dimension dim = renderer.getDimension();
        Rectangle2D textAnchor = new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight());
        Paint paint = new java.awt.TexturePaint(renderer.getImage(), textAnchor);

        return paint;
    }
    
    /**
     * Convert color transformations in {@link ColorStyle} to a {@link Color} instance
     */
    public static Color applyColorTransform(ColorStyle color){
        Color result = color.getColor();

        if (result == null || color.getAlpha() == 100) return NO_PAINT;
        
        result = applyAlpha(result, color);
        result = applyLuminanace(result, color);
        result = applyShade(result, color);
        result = applyTint(result, color);

        return result;
    }

    protected static Color applyAlpha(Color c, ColorStyle fc) {
        int alpha = c.getAlpha();
        return (alpha == 0 || alpha == -1) ? c : new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha); 
    }
    
    /**
     * Apply lumMod / lumOff adjustments
     *
     * @param c the color to modify
     * @param lumMod luminance modulation in the range [0..100000]
     * @param lumOff luminance offset in the range [0..100000]
     * @return  modified color
     */
    protected static Color applyLuminanace(Color c, ColorStyle fc) {
        int lumMod = fc.getLumMod();
        if (lumMod == -1) lumMod = 100000;

        int lumOff = fc.getLumOff();
        if (lumOff == -1) lumOff = 0;
        
        if (lumMod == 100000 && lumOff == 0) return c;

        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        
        float red,green,blue;
        
        if (lumOff > 0) {
            float flumOff = lumOff / 100000.f;
            red = (255.f - r) * (1.f - flumOff) + r;
            green = (255.f - g) * flumOff + g;
            blue = (255.f - b) * flumOff + b;
        } else {
            float flumMod = lumMod / 100000.f;
            red = r * flumMod;
            green = g * flumMod;
            blue = b * flumMod;
        }
        return new Color(Math.round(red), Math.round(green), Math.round(blue), c.getAlpha());
    }
    
    /**
     * This algorithm returns result different from PowerPoint.
     * TODO: revisit and improve
     */
    protected static Color applyShade(Color c, ColorStyle fc) {
        int shade = fc.getShade();
        if (shade == -1) return c;
        
        float fshade = shade / 100000.f;

        float red = c.getRed() * fshade;
        float green = c.getGreen() * fshade;
        float blue = c.getGreen() * fshade;
        
        return new Color(Math.round(red), Math.round(green), Math.round(blue), c.getAlpha());
    }

    /**
     * This algorithm returns result different from PowerPoint.
     * TODO: revisit and improve
     */
    protected static Color applyTint(Color c, ColorStyle fc) {
        int tint = fc.getTint();
        if (tint == -1) return c;
        
        float ftint = tint / 100000.f;

        float red = ftint * c.getRed() + (1.f - ftint) * 255.f;
        float green = ftint * c.getGreen() + (1.f - ftint) * 255.f;
        float blue = ftint * c.getBlue() + (1.f - ftint) * 255.f;

        return new Color(Math.round(red), Math.round(green), Math.round(blue), c.getAlpha());
    }
    

    protected Paint createLinearGradientPaint(GradientPaint fill, Graphics2D graphics) {
        double angle = fill.getGradientAngle();
        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);

        AffineTransform at = AffineTransform.getRotateInstance(
            Math.toRadians(angle),
            anchor.getX() + anchor.getWidth() / 2,
            anchor.getY() + anchor.getHeight() / 2);

        double diagonal = Math.sqrt(anchor.getHeight() * anchor.getHeight() + anchor.getWidth() * anchor.getWidth());
        Point2D p1 = new Point2D.Double(anchor.getX() + anchor.getWidth() / 2 - diagonal / 2,
                anchor.getY() + anchor.getHeight() / 2);
        p1 = at.transform(p1, null);

        Point2D p2 = new Point2D.Double(anchor.getX() + anchor.getWidth(), anchor.getY() + anchor.getHeight() / 2);
        p2 = at.transform(p2, null);

        snapToAnchor(p1, anchor);
        snapToAnchor(p2, anchor);

        float[] fractions = fill.getGradientFractions();
        Color[] colors = new Color[fractions.length];
        
        int i = 0;
        for (ColorStyle fc : fill.getGradientColors()) {
            colors[i++] = applyColorTransform(fc);
        }

        AffineTransform grAt  = new AffineTransform();
        if(fill.isRotatedWithShape()) {
            double rotation = shape.getRotation();
            if (rotation != 0.) {
                double centerX = anchor.getX() + anchor.getWidth() / 2;
                double centerY = anchor.getY() + anchor.getHeight() / 2;

                grAt.translate(centerX, centerY);
                grAt.rotate(Math.toRadians(-rotation));
                grAt.translate(-centerX, -centerY);
            }
        }

        return new LinearGradientPaint
            (p1, p2, fractions, colors, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, grAt);
    }

    protected Paint createRadialGradientPaint(GradientPaint fill, Graphics2D graphics) {
        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);

        Point2D pCenter = new Point2D.Double(anchor.getX() + anchor.getWidth()/2,
                anchor.getY() + anchor.getHeight()/2);

        float radius = (float)Math.max(anchor.getWidth(), anchor.getHeight());

        float[] fractions = fill.getGradientFractions();
        Color[] colors = new Color[fractions.length];

        int i=0;
        for (ColorStyle fc : fill.getGradientColors()) {
            colors[i++] = applyColorTransform(fc);
        }

        return new RadialGradientPaint(pCenter, radius, fractions, colors);
    }

    protected Paint createPathGradientPaint(GradientPaint fill, Graphics2D graphics) {
        // currently we ignore an eventually center setting
        
        float[] fractions = fill.getGradientFractions();
        Color[] colors = new Color[fractions.length];

        int i=0;
        for (ColorStyle fc : fill.getGradientColors()) {
            colors[i++] = applyColorTransform(fc);
        }

        return new PathGradientPaint(colors, fractions);
    }
    
    protected void snapToAnchor(Point2D p, Rectangle2D anchor) {
        if (p.getX() < anchor.getX()) {
            p.setLocation(anchor.getX(), p.getY());
        } else if (p.getX() > (anchor.getX() + anchor.getWidth())) {
            p.setLocation(anchor.getX() + anchor.getWidth(), p.getY());
        }

        if (p.getY() < anchor.getY()) {
            p.setLocation(p.getX(), anchor.getY());
        } else if (p.getY() > (anchor.getY() + anchor.getHeight())) {
            p.setLocation(p.getX(), anchor.getY() + anchor.getHeight());
        }
    }

    public static class PathGradientPaint implements Paint {

        // http://asserttrue.blogspot.de/2010/01/how-to-iimplement-custom-paint-in-50.html
        protected final Color colors[];
        protected final float fractions[];
        protected final int capStyle;
        protected final int joinStyle;
        protected final int transparency;

        
        public PathGradientPaint(Color colors[], float fractions[]) {
            this(colors,fractions,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        }
        
        public PathGradientPaint(Color colors[], float fractions[], int capStyle, int joinStyle) {
            this.colors = colors;
            this.fractions = fractions;
            this.capStyle = capStyle;
            this.joinStyle = joinStyle;

            // determine transparency
            boolean opaque = true;
            for (int i = 0; i < colors.length; i++){
                opaque = opaque && (colors[i].getAlpha() == 0xff);
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
            protected final Rectangle deviceBounds;
            protected final Rectangle2D userBounds;
            protected final AffineTransform xform;
            protected final RenderingHints hints;

            /**
             * for POI: the shape will be only known when the subclasses determines the concrete implementation 
             * in the draw/-content method, so we need to postpone the setting/creation as long as possible
             **/
            protected final Shape shape;
            protected final PaintContext pCtx;
            protected final int gradientSteps;
            WritableRaster raster;

            public PathGradientContext(
                  ColorModel cm
                , Rectangle deviceBounds
                , Rectangle2D userBounds
                , AffineTransform xform
                , RenderingHints hints
            ) {
                shape = (Shape)hints.get(Drawable.GRADIENT_SHAPE);
                if (shape == null) {
                    throw new IllegalPathStateException("PathGradientPaint needs a shape to be set via the rendering hint PathGradientPaint.GRADIANT_SHAPE.");
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

            protected int getGradientSteps(Shape shape) {
                Rectangle rect = shape.getBounds();
                int lower = 1;
                int upper = (int)(Math.max(rect.getWidth(),rect.getHeight())/2.0);
                while (lower < upper-1) {
                    int mid = lower + (upper - lower) / 2;
                    BasicStroke bs = new BasicStroke(mid, capStyle, joinStyle);
                    Area area = new Area(bs.createStrokedShape(shape));
                    if (area.isSingular()) {
                        upper = mid;
                    } else {
                        lower = mid;
                    }
                }
                return upper;
            }
            
            
            
            protected void createRaster() {
                ColorModel cm = getColorModel();
                raster = cm.createCompatibleWritableRaster((int)deviceBounds.getWidth(), (int)deviceBounds.getHeight());
                BufferedImage img = new BufferedImage(cm, raster, false, null);
                Graphics2D graphics = img.createGraphics();
                graphics.setRenderingHints(hints);
                graphics.translate(-deviceBounds.getX(), -deviceBounds.getY());
                graphics.transform(xform);

                Raster img2 = pCtx.getRaster(0, 0, gradientSteps, 1);
                int rgb[] = new int[cm.getNumComponents()];

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
}
