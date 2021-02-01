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

import static org.apache.poi.sl.draw.geom.ArcToCommand.convertOoxml2AwtAngle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.poi.sl.usermodel.AbstractColorStyle;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.FlipMode;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.util.Dimension2DDouble;
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
                if (value == -1) {
                    return -1;
                }
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
        case rectangular:
            // TODO: implement rectangular gradient fill
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
        assert(graphics != null);

        final String contentType = fill.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            return TRANSPARENT;
        }

        ImageRenderer renderer = DrawPictureShape.getImageRenderer(graphics, contentType);

        // TODO: handle tile settings, currently the pattern is always streched 100% in height/width
        Rectangle2D textAnchor = shape.getAnchor();

        try (InputStream is = fill.getImageData()) {
            if (is == null) {
                return TRANSPARENT;
            }

            renderer.loadImage(is, contentType);

            int alpha = fill.getAlpha();
            if (0 <= alpha && alpha < 100000) {
                renderer.setAlpha(alpha/100000.f);
            }

            Dimension2D imgDim = renderer.getDimension();
            if ("image/x-wmf".contains(contentType)) {
                // don't rely on wmf dimensions, use dimension of anchor
                // TODO: check pixels vs. points for image dimension
                imgDim = new Dimension2DDouble(textAnchor.getWidth(), textAnchor.getHeight());
            }

            BufferedImage image = renderer.getImage(imgDim);
            if(image == null) {
                LOG.log(POILogger.ERROR, "Can't load image data");
                return TRANSPARENT;
            }

            double flipX = 1, flipY = 1;
            final FlipMode flip = fill.getFlipMode();
            if (flip != null && flip != FlipMode.NONE) {
                final int width = image.getWidth(), height = image.getHeight();
                switch (flip) {
                    case X:
                        flipX = 2;
                        break;
                    case Y:
                        flipY = 2;
                        break;
                    case XY:
                        flipX = 2;
                        flipY = 2;
                        break;
                }

                final BufferedImage img = new BufferedImage((int)(width*flipX), (int)(height*flipY), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(image, 0, 0, null);

                switch (flip) {
                    case X:
                        g.drawImage(image, 2*width, 0, -width, height, null);
                        break;
                    case Y:
                        g.drawImage(image, 0, 2*height, width, -height, null);
                        break;
                    case XY:
                        g.drawImage(image, 2*width, 0, -width, height, null);
                        g.drawImage(image, 0, 2*height, width, -height, null);
                        g.drawImage(image, 2*width, 2*height, -width, -height, null);
                        break;
                }

                g.dispose();
                image = img;
            }

            image = colorizePattern(fill, image);

            Shape s = (Shape)graphics.getRenderingHint(Drawable.GRADIENT_SHAPE);

            // TODO: check why original bitmaps scale/behave differently to vector based images
            return new DrawTexturePaint(image, s, fill, flipX, flipY, renderer instanceof BitmapImageRenderer);
        } catch (IOException e) {
            LOG.log(POILogger.ERROR, "Can't load image data - using transparent color", e);
            return TRANSPARENT;
        }
    }

    /**
     * In case a duotone element is specified, handle image as pattern and replace its color values
     * with the corresponding percentile / linear value between fore- and background color
     *
     * @return the original image if no duotone was found, otherwise the colorized pattern
     */
    private static BufferedImage colorizePattern(TexturePaint fill, BufferedImage pattern) {
        final List<ColorStyle> duoTone = fill.getDuoTone();
        if (duoTone == null || duoTone.size() != 2) {
            return pattern;
        }

        // the pattern image is actually a gray scale image, so we simply take the first color component
        // as an index into our gradient samples
        final int redBits = pattern.getSampleModel().getSampleSize(0);
        final int blendBits = Math.max(Math.min(redBits, 8), 1);
        final int blendShades = 1 << blendBits;
        // Currently ImageIO converts 16-bit images to 8-bit internally, so it's unlikely to get a blendRatio != 1
        final double blendRatio = blendShades / (double)(1 << Math.max(redBits,1));
        final int[] gradSample = linearBlendedColors(duoTone, blendShades);

        final IndexColorModel icm = new IndexColorModel(blendBits, blendShades, gradSample, 0, true, -1, DataBuffer.TYPE_BYTE);
        final BufferedImage patIdx = new BufferedImage(pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, icm);

        final WritableRaster rasterRGBA = pattern.getRaster();
        final WritableRaster rasterIdx = patIdx.getRaster();

        final int[] redSample = new int[pattern.getWidth()];
        for (int y=0; y<pattern.getHeight(); y++) {
            rasterRGBA.getSamples(0, y, redSample.length, 1, 0, redSample);
            scaleShades(redSample, blendRatio);
            rasterIdx.setSamples(0, y, redSample.length, 1, 0, redSample);
        }

        return patIdx;
    }

    private static void scaleShades(int[] samples, double ratio) {
        if (ratio != 1) {
            for (int x=0; x<samples.length; x++) {
                samples[x] = (int)Math.rint(samples[x] * ratio);
            }
        }
    }

    private static int[] linearBlendedColors(List<ColorStyle> duoTone, final int blendShades) {
        Color[] colors = duoTone.stream().map(DrawPaint::applyColorTransform).toArray(Color[]::new);
        float[] fractions = { 0, 1 };

        // create lookup list of blended colors of back- and foreground
        BufferedImage gradBI = new BufferedImage(blendShades, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gradG = gradBI.createGraphics();
        gradG.setPaint(new LinearGradientPaint(0,0, blendShades,0, fractions, colors));
        gradG.fillRect(0,0, blendShades,1);
        gradG.dispose();

        return gradBI.getRGB(0, 0, blendShades, 1, null, 0, blendShades);
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

        final double alpha = getAlpha(result, color);

        final double[] scRGB = RGB2SCRGB(result);
        applyShade(scRGB, color);
        applyTint(scRGB, color);
        result = SCRGB2RGB(scRGB);

        // values are in the range [0..100] (usually ...)
        double[] hsl = RGB2HSL(result);
        applyHslModOff(hsl, 0, color.getHueMod(), color.getHueOff());
        applyHslModOff(hsl, 1, color.getSatMod(), color.getSatOff());
        applyHslModOff(hsl, 2, color.getLumMod(), color.getLumOff());

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
        if (mod != -1) {
            hsl[hslPart] *= mod / 100_000d;
        }
        if (off != -1) {
            hsl[hslPart] += off / 1000d;
        }
    }

    /**
     * Apply the shade
     *
     * For a shade, the equation is luminance * %tint.
     */
    private static void applyShade(double[] scRGB, ColorStyle fc) {
        int shade = fc.getShade();
        if (shade == -1) {
            return;
        }

        final double shadePct = shade / 100_000.;
        for (int i=0; i<3; i++) {
            scRGB[i] = Math.max(0, Math.min(1, scRGB[i]*shadePct));
        }
    }

    /**
     * Apply the tint
     */
    private static void applyTint(double[] scRGB, ColorStyle fc) {
        int tint = fc.getTint();
        if (tint == -1 || tint == 0) {
            return;
        }

        // see 18.8.19 fgColor (Foreground Color)
        double tintPct = tint / 100_000.;

        for (int i=0; i<3; i++) {
            scRGB[i] =  1 - (1 - scRGB[i]) * tintPct;
        }
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
        if (anchor == null) {
            return TRANSPARENT;
        }

        angle = convertOoxml2AwtAngle(-angle, anchor.getWidth(), anchor.getHeight());

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
        if (anchor == null) {
            return TRANSPARENT;
        }

        Insets2D insets = fill.getFillToInsets();
        if (insets == null) {
            insets = new Insets2D(0,0,0,0);
        }

        // TODO: handle negative width/height
        final Point2D pCenter = new Point2D.Double(
            anchor.getCenterX(), anchor.getCenterY()
        );

        final Point2D pFocus = new Point2D.Double(
            getCenterVal(anchor.getMinX(), anchor.getMaxX(), insets.left, insets.right),
            getCenterVal(anchor.getMinY(), anchor.getMaxY(), insets.top, insets.bottom)
        );

        final float radius = (float)Math.max(anchor.getWidth(), anchor.getHeight());

        final AffineTransform at = new AffineTransform();
        at.translate(pFocus.getX(), pFocus.getY());
        at.scale(
            getScale(anchor.getMinX(), anchor.getMaxX(), insets.left, insets.right),
            getScale(anchor.getMinY(), anchor.getMaxY(), insets.top, insets.bottom)
        );
        at.translate(-pFocus.getX(), -pFocus.getY());

        return safeFractions((f,c)->new RadialGradientPaint(pCenter, radius, pFocus, f, c, CycleMethod.NO_CYCLE, ColorSpaceType.SRGB, at), fill);
    }

    private static double getScale(double absMin, double absMax, double relMin, double relMax) {
        double absDelta = absMax-absMin;
        double absStart = absMin+absDelta*relMin;
        double absStop = (relMin+relMax <= 1) ? absMax-absDelta*relMax : absMax+absDelta*relMax;
        return (absDelta == 0) ? 1 : (absStop-absStart)/absDelta;
    }

    private static double getCenterVal(double absMin, double absMax, double relMin, double relMax) {
        double absDelta = absMax-absMin;
        double absStart = absMin+absDelta*relMin;
        double absStop = (relMin+relMax <= 1) ? absMax-absDelta*relMax : absMax+absDelta*relMax;
        return absStart+(absStop-absStart)/2.;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected Paint createPathGradientPaint(GradientPaint fill, Graphics2D graphics) {
        // currently we ignore an eventually center setting

        return safeFractions(PathGradientPaint::new, fill);
    }

    private Paint safeFractions(BiFunction<float[],Color[],Paint> init, GradientPaint fill) {
        // if style is null, use transparent color to get color of background
        final Iterator<Color> styles = Stream.of(fill.getGradientColors())
            .map(s -> s == null ? TRANSPARENT : applyColorTransform(s))
            .iterator();

        // need to remap the fractions, because Java doesn't like repeating fraction values
        Map<Float,Color> m = new TreeMap<>();
        for (float fraction : fill.getGradientFractions()) {
            m.put(fraction, styles.next());
        }

        return init.apply(toArray(m.keySet()), m.values().toArray(new Color[0]));
    }

    private static float[] toArray(Collection<Float> floatList) {
        int[] idx = { 0 };
        float[] ret = new float[floatList.size()];
        floatList.forEach(f -> ret[idx[0]++] = f);
        return ret;
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
    public static double[] RGB2HSL(Color color) {
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
     * Convert sRGB Color to scRGB [0..1] (0:red,1:green,2:blue).
     * Alpha needs to be handled separately.
     *
     * @see <a href="https://referencesource.microsoft.com/#PresentationCore/Core/CSharp/System/Windows/Media/Color.cs,1048">.Net implementation sRgbToScRgb</a>
     */
    public static double[] RGB2SCRGB(Color color) {
        float[] rgb = color.getColorComponents(null);
        double[] scRGB = new double[3];
        for (int i=0; i<3; i++) {
            if (rgb[i] < 0) {
                scRGB[i] = 0;
            } else if (rgb[i] <= 0.04045) {
                scRGB[i] = rgb[i] / 12.92;
            } else if (rgb[i] <= 1) {
                scRGB[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
            } else {
                scRGB[i] = 1;
            }
        }
        return scRGB;
    }

    /**
     * Convert scRGB [0..1] components (0:red,1:green,2:blue) to sRGB Color.
     * Alpha needs to be handled separately.
     *
     * @see <a href="https://referencesource.microsoft.com/#PresentationCore/Core/CSharp/System/Windows/Media/Color.cs,1075">.Net implementation ScRgbTosRgb</a>
     */
    public static Color SCRGB2RGB(double... scRGB) {
        final double[] rgb = new double[3];
        for (int i=0; i<3; i++) {
            if (scRGB[i] < 0) {
                rgb[i] = 0;
            } else if (scRGB[i] <= 0.0031308) {
                rgb[i] = scRGB[i] * 12.92;
            } else if (scRGB[i] < 1) {
                rgb[i] = 1.055 * Math.pow(scRGB[i], 1.0 / 2.4) - 0.055;
            } else {
                rgb[i] = 1;
            }
        }
        return new Color((float)rgb[0],(float)rgb[1],(float)rgb[2]);
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
