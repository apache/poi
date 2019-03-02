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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.apache.poi.sl.usermodel.AbstractColorStyle;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;


/**
 * This class handles color transformations.
 *
 * @see <a href="https://tips4java.wordpress.com/2009/07/05/hsl-color/">HSL code taken from Java Tips Weblog</a>
 */
public class DrawPaint {
    // HSL code is public domain - see https://tips4java.wordpress.com/contact-us/

    private static final POILogger LOG = POILogFactory.getLogger(DrawPaint.class);

    private static final Color TRANSPARENT = new Color(1f,1f,1f,0f);

    protected PlaceableShape<?,?> shape;

    public DrawPaint(PlaceableShape<?,?> shape) {
        this.shape = shape;
    }

    private static class SimpleSolidPaint implements SolidPaint {
        private final ColorStyle solidColor;

        SimpleSolidPaint(final Color color) {
            if (color == null) {
                throw new NullPointerException("Color needs to be specified");
            }
            this.solidColor = new AbstractColorStyle(){
                    @Override
                    public Color getColor() {
                        return new Color(color.getRed(), color.getGreen(), color.getBlue());
                    }
                    @Override
                    public int getAlpha() { return (int)Math.round(color.getAlpha()*100000./255.); }
                    @Override
                    public int getHueOff() { return -1; }
                    @Override
                    public int getHueMod() { return -1; }
                    @Override
                    public int getSatOff() { return -1; }
                    @Override
                    public int getSatMod() { return -1; }
                    @Override
                    public int getLumOff() { return -1; }
                    @Override
                    public int getLumMod() { return -1; }
                    @Override
                    public int getShade() { return -1; }
                    @Override
                    public int getTint() { return -1; }


                };
        }

        SimpleSolidPaint(ColorStyle color) {
            if (color == null) {
                throw new NullPointerException("Color needs to be specified");
            }
            this.solidColor = color;
        }

        @Override
        public ColorStyle getSolidColor() {
            return solidColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SolidPaint)) {
                return false;
            }
            return Objects.equals(getSolidColor(), ((SolidPaint) o).getSolidColor());
        }

        @Override
        public int hashCode() {
            return Objects.hash(solidColor);
        }
    }

    public static SolidPaint createSolidPaint(final Color color) {
        return (color == null) ? null : new SimpleSolidPaint(color);
    }

    public static SolidPaint createSolidPaint(final ColorStyle color) {
        return (color == null) ? null : new SimpleSolidPaint(color);
    }

    public Paint getPaint(Graphics2D graphics, PaintStyle paint) {
        return getPaint(graphics, paint, PaintModifier.NORM);
    }

    public Paint getPaint(Graphics2D graphics, PaintStyle paint, PaintModifier modifier) {
        if (modifier == PaintModifier.NONE) {
            return TRANSPARENT;
        }
        if (paint instanceof SolidPaint) {
            return getSolidPaint((SolidPaint)paint, graphics, modifier);
        } else if (paint instanceof GradientPaint) {
            return getGradientPaint((GradientPaint)paint, graphics);
        } else if (paint instanceof TexturePaint) {
            return getTexturePaint((TexturePaint)paint, graphics);
        }
        return TRANSPARENT;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected Paint getSolidPaint(SolidPaint fill, Graphics2D graphics, final PaintModifier modifier) {
        final ColorStyle orig = fill.getSolidColor();
        ColorStyle cs = new AbstractColorStyle() {
            @Override
            public Color getColor() {
                return orig.getColor();
            }

            @Override
            public int getAlpha() {
                return orig.getAlpha();
            }

            @Override
            public int getHueOff() {
                return orig.getHueOff();
            }

            @Override
            public int getHueMod() {
                return orig.getHueMod();
            }

            @Override
            public int getSatOff() {
                return orig.getSatOff();
            }

            @Override
            public int getSatMod() {
                return orig.getSatMod();
            }

            @Override
            public int getLumOff() {
                return orig.getLumOff();
            }

            @Override
            public int getLumMod() {
                return orig.getLumMod();
            }

            @Override
            public int getShade() {
                return scale(orig.getShade(), PaintModifier.DARKEN_LESS, PaintModifier.DARKEN);
            }

            @Override
            public int getTint() {
                return scale(orig.getTint(), PaintModifier.LIGHTEN_LESS, PaintModifier.LIGHTEN);
            }

            private int scale(int value, PaintModifier lessModifier, PaintModifier moreModifier) {
                int delta = (modifier == lessModifier ? 20000 : (modifier == moreModifier ? 40000 : 0));
                return Math.min(100000, Math.max(0,value)+delta);
            }
        };

        return applyColorTransform(cs);
    }

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
    protected Paint getTexturePaint(TexturePaint fill, Graphics2D graphics) {
        InputStream is = fill.getImageData();
        if (is == null) {
            return TRANSPARENT;
        }
        assert(graphics != null);

        ImageRenderer renderer = DrawPictureShape.getImageRenderer(graphics, fill.getContentType());

        try {
            try {
                renderer.loadImage(is, fill.getContentType());
            } finally {
                is.close();
            }
        } catch (IOException e) {
            LOG.log(POILogger.ERROR, "Can't load image data - using transparent color", e);
            return TRANSPARENT;
        }

        int alpha = fill.getAlpha();
        if (0 <= alpha && alpha < 100000) {
            renderer.setAlpha(alpha/100000.f);
        }

        Rectangle2D textAnchor = shape.getAnchor();
        BufferedImage image;
        if ("image/x-wmf".equals(fill.getContentType())) {
            // don't rely on wmf dimensions, use dimension of anchor
            // TODO: check pixels vs. points for image dimension
            image = renderer.getImage(new Dimension((int)textAnchor.getWidth(), (int)textAnchor.getHeight()));
        } else {
            image = renderer.getImage();
        }

        if(image == null) {
            LOG.log(POILogger.ERROR, "Can't load image data");
            return TRANSPARENT;
        }

        return new java.awt.TexturePaint(image, textAnchor);
    }

    /**
     * Convert color transformations in {@link ColorStyle} to a {@link Color} instance
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/dd560821%28v=office.12%29.aspx">Using Office Open XML to Customize Document Formatting in the 2007 Office System</a>
     * @see <a href="https://social.msdn.microsoft.com/Forums/office/en-US/040e0a1f-dbfe-4ce5-826b-38b4b6f6d3f7/saturation-modulation-satmod">saturation modulation (satMod)</a>
     * @see <a href="http://stackoverflow.com/questions/6754127/office-open-xml-satmod-results-in-more-than-100-saturation">Office Open XML satMod results in more than 100% saturation</a>
     */
    public static Color applyColorTransform(ColorStyle color){
        // TODO: The colors don't match 100% the results of Powerpoint, maybe because we still
        // operate in sRGB and not scRGB ... work in progress ...
        if (color == null || color.getColor() == null) {
            return TRANSPARENT;
        }

        Color result = color.getColor();

        double alpha = getAlpha(result, color);
        double[] hsl = RGB2HSL(result); // values are in the range [0..100] (usually ...)
        applyHslModOff(hsl, 0, color.getHueMod(), color.getHueOff());
        applyHslModOff(hsl, 1, color.getSatMod(), color.getSatOff());
        applyHslModOff(hsl, 2, color.getLumMod(), color.getLumOff());
        applyShade(hsl, color);
        applyTint(hsl, color);

        result = HSL2RGB(hsl[0], hsl[1], hsl[2], alpha);

        return result;
    }

    private static double getAlpha(Color c, ColorStyle fc) {
        double alpha = c.getAlpha()/255d;
        int fcAlpha = fc.getAlpha();
        if (fcAlpha != -1) {
            alpha *= fcAlpha/100000d;
        }
        return Math.min(1, Math.max(0, alpha));
    }

    /**
     * Apply the modulation and offset adjustments to the given HSL part
     *
     * Example for lumMod/lumOff:
     * The lumMod value is the percent luminance. A lumMod value of "60000",
     * is 60% of the luminance of the original color.
     * When the color is a shade of the original theme color, the lumMod
     * attribute is the only one of the tags shown here that appears.
     * The <a:lumOff> tag appears after the <a:lumMod> tag when the color is a
     * tint of the original. The lumOff value always equals 1-lumMod, which is used in the tint calculation
     *
     * Despite having different ways to display the tint and shade percentages,
     * all of the programs use the same method to calculate the resulting color.
     * Convert the original RGB value to HSL ... and then adjust the luminance (L)
     * with one of the following equations before converting the HSL value back to RGB.
     * (The % tint in the following equations refers to the tint, themetint, themeshade,
     * or lumMod values, as applicable.)
     *
     * @param hsl the hsl values
     * @param hslPart the hsl part to modify [0..2]
     * @param mod the modulation adjustment
     * @param off the offset adjustment
     */
    private static void applyHslModOff(double[] hsl, int hslPart, int mod, int off) {
        if (mod == -1) {
            mod = 100000;
        }
        if (off == -1) {
            off = 0;
        }
        if (!(mod == 100000 && off == 0)) {
            double fOff = off / 1000d;
            double fMod = mod / 100000d;
            hsl[hslPart] = hsl[hslPart]*fMod+fOff;
        }
    }

    /**
     * Apply the shade
     *
     * For a shade, the equation is luminance * %tint.
     */
    private static void applyShade(double[] hsl, ColorStyle fc) {
        int shade = fc.getShade();
        if (shade == -1) {
            return;
        }

        double shadePct = shade / 100000.;

        hsl[2] *= 1. - shadePct;
    }

    /**
     * Apply the tint
     *
     * For a tint, the equation is luminance * %tint + (1-%tint).
     * (Note that 1-%tint is equal to the lumOff value in DrawingML.)
     */
    private static void applyTint(double[] hsl, ColorStyle fc) {
        int tint = fc.getTint();
        if (tint == -1) {
            return;
        }

        // see 18.8.19 fgColor (Foreground Color)
        double tintPct = tint / 100000.;
        hsl[2] = hsl[2]*(1.-tintPct) + (100.-100.*(1.-tintPct));
    }

    @SuppressWarnings("WeakerAccess")
    protected Paint createLinearGradientPaint(GradientPaint fill, Graphics2D graphics) {
        // TODO: we need to find the two points for gradient - the problem is, which point at the outline
        // do you take? My solution would be to apply the gradient rotation to the shape in reverse
        // and then scan the shape for the largest possible horizontal distance

        double angle = fill.getGradientAngle();
        if (!fill.isRotatedWithShape()) {
            angle -= shape.getRotation();
        }

        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);

        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), anchor.getCenterX(), anchor.getCenterY());

        double diagonal = Math.sqrt(Math.pow(anchor.getWidth(),2) + Math.pow(anchor.getHeight(),2));
        final Point2D p1 = at.transform(new Point2D.Double(anchor.getCenterX() - diagonal / 2, anchor.getCenterY()), null);
        final Point2D p2 = at.transform(new Point2D.Double(anchor.getMaxX(), anchor.getCenterY()), null);

//        snapToAnchor(p1, anchor);
//        snapToAnchor(p2, anchor);

        // gradient paint on the same point throws an exception ... and doesn't make sense
        // also having less than two fractions will not work
        return (p1.equals(p2) || fill.getGradientFractions().length < 2) ?
                null :
                safeFractions((f,c)->new LinearGradientPaint(p1,p2,f,c), fill);
    }


    @SuppressWarnings("WeakerAccess")
    protected Paint createRadialGradientPaint(GradientPaint fill, Graphics2D graphics) {
        Rectangle2D anchor = DrawShape.getAnchor(graphics, shape);

        final Point2D pCenter = new Point2D.Double(anchor.getCenterX(), anchor.getCenterY());

        final float radius = (float)Math.max(anchor.getWidth(), anchor.getHeight());

        return safeFractions((f,c)->new RadialGradientPaint(pCenter,radius,f,c), fill);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected Paint createPathGradientPaint(GradientPaint fill, Graphics2D graphics) {
        // currently we ignore an eventually center setting

        return safeFractions(PathGradientPaint::new, fill);
    }

    private Paint safeFractions(BiFunction<float[],Color[],Paint> init, GradientPaint fill) {
        float[] fractions = fill.getGradientFractions();
        final ColorStyle[] styles = fill.getGradientColors();

        // need to remap the fractions, because Java doesn't like repeating fraction values
        Map<Float,Color> m = new TreeMap<>();
        for (int i = 0; i<fractions.length; i++) {
            // if fc is null, use transparent color to get color of background
            m.put(fractions[i], (styles[i] == null ? TRANSPARENT : applyColorTransform(styles[i])));
        }

        final Color[] colors = new Color[m.size()];
        if (fractions.length != m.size()) {
            fractions = new float[m.size()];
        }

        int i=0;
        for (Map.Entry<Float,Color> me : m.entrySet()) {
            fractions[i] = me.getKey();
            colors[i] = me.getValue();
            i++;
        }

        return init.apply(fractions, colors);
    }

    /**
     *  Convert HSL values to a RGB Color.
     *
     *  @param h Hue is specified as degrees in the range 0 - 360.
     *  @param s Saturation is specified as a percentage in the range 1 - 100.
     *  @param l Luminance is specified as a percentage in the range 1 - 100.
     *  @param alpha  the alpha value between 0 - 1
     *
     *  @return the RGB Color object
     */
    public static Color HSL2RGB(double h, double s, double l, double alpha) {
        // we clamp the values, as it possible to come up with more than 100% sat/lum
        // (see links in applyColorTransform() for more info)
        s = Math.max(0, Math.min(100, s));
        l = Math.max(0, Math.min(100, l));

        if (alpha <0.0f || alpha > 1.0f) {
            String message = "Color parameter outside of expected range - Alpha: " + alpha;
            throw new IllegalArgumentException( message );
        }

        //  Formula needs all values between 0 - 1.

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        double q = (l < 0.5d)
            ? l * (1d + s)
            : (l + s) - (s * l);

        double p = 2d * l - q;

        double r = Math.max(0, HUE2RGB(p, q, h + (1.0d / 3.0d)));
        double g = Math.max(0, HUE2RGB(p, q, h));
        double b = Math.max(0, HUE2RGB(p, q, h - (1.0d / 3.0d)));

        r = Math.min(r, 1.0d);
        g = Math.min(g, 1.0d);
        b = Math.min(b, 1.0d);

        return new Color((float)r, (float)g, (float)b, (float)alpha);
    }

    private static double HUE2RGB(double p, double q, double h) {
        if (h < 0d) {
            h += 1d;
        }

        if (h > 1d) {
            h -= 1d;
        }

        if (6d * h < 1d) {
            return p + ((q - p) * 6d * h);
        }

        if (2d * h < 1d) {
            return q;
        }

        if (3d * h < 2d) {
            return p + ( (q - p) * 6d * ((2.0d / 3.0d) - h) );
        }

        return p;
    }


    /**
     *  Convert a RGB Color to it corresponding HSL values.
     *
     *  @return an array containing the 3 HSL values.
     */
    private static double[] RGB2HSL(Color color)
    {
        //  Get RGB values in the range 0 - 1

        float[] rgb = color.getRGBColorComponents( null );
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];

        //  Minimum and Maximum RGB values are used in the HSL calculations

        double min = Math.min(r, Math.min(g, b));
        double max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        double h = 0;

        if (max == min) {
            h = 0;
        } else if (max == r) {
            h = ((60d * (g - b) / (max - min)) + 360d) % 360d;
        } else if (max == g) {
            h = (60d * (b - r) / (max - min)) + 120d;
        } else if (max == b) {
            h = (60d * (r - g) / (max - min)) + 240d;
        }

        //  Calculate the Luminance

        double l = (max + min) / 2d;

        //  Calculate the Saturation

        final double s;

        if (max == min) {
            s = 0;
        } else if (l <= .5d) {
            s = (max - min) / (max + min);
        } else {
            s = (max - min) / (2d - max - min);
        }

        return new double[] {h, s * 100, l * 100};
    }

    /**
     * Convert sRGB float component [0..1] from sRGB to linear RGB [0..100000]
     *
     * @see Color#getRGBColorComponents(float[])
     */
    public static int srgb2lin(float sRGB) {
        // scRGB has a linear gamma of 1.0, scale the AWT-Color which is in sRGB to linear RGB
        // see https://en.wikipedia.org/wiki/SRGB (the reverse transformation)
        if (sRGB <= 0.04045d) {
            return (int)Math.rint(100000d * sRGB / 12.92d);
        } else {
            return (int)Math.rint(100000d * Math.pow((sRGB + 0.055d) / 1.055d, 2.4d));
        }
    }

    /**
     * Convert linear RGB [0..100000] to sRGB float component [0..1]
     *
     * @see Color#getRGBColorComponents(float[])
     */
    public static float lin2srgb(int linRGB) {
        // color in percentage is in linear RGB color space, i.e. needs to be gamma corrected for AWT color
        // see https://en.wikipedia.org/wiki/SRGB (The forward transformation)
        if (linRGB <= 0.0031308d) {
            return (float)(linRGB / 100000d * 12.92d);
        } else {
            return (float)(1.055d * Math.pow(linRGB / 100000d, 1.0d/2.4d) - 0.055d);
        }
    }


    static void fillPaintWorkaround(Graphics2D graphics, Shape shape) {
        // the ibm jdk has a rendering/JIT bug, which throws an AIOOBE in
        // TexturePaintContext$Int.setRaster(TexturePaintContext.java:476)
        // this usually doesn't happen while debugging, because JIT doesn't jump in then.
        try {
            graphics.fill(shape);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.log(POILogger.WARN, "IBM JDK failed with TexturePaintContext AIOOBE - try adding the following to the VM parameter:\n" +
                "-Xjit:exclude={sun/java2d/pipe/AlphaPaintPipe.renderPathTile(Ljava/lang/Object;[BIIIIII)V} and " +
                "search for 'JIT Problem Determination for IBM SDK using -Xjit' (http://www-01.ibm.com/support/docview.wss?uid=swg21294023) " +
                "for how to add/determine further excludes", e);
        }
    }
}
