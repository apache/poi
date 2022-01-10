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

package org.apache.poi.xslf.draw;

import static java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
import static org.apache.batik.svggen.SVGSyntax.ID_PREFIX_IMAGE;
import static org.apache.batik.svggen.SVGSyntax.ID_PREFIX_PATTERN;
import static org.apache.batik.svggen.SVGSyntax.SIGN_POUND;
import static org.apache.batik.svggen.SVGSyntax.URL_PREFIX;
import static org.apache.batik.svggen.SVGSyntax.URL_SUFFIX;
import static org.apache.batik.util.SVGConstants.*;
import static org.apache.poi.sl.usermodel.PictureData.PictureType.GIF;
import static org.apache.poi.sl.usermodel.PictureData.PictureType.JPEG;
import static org.apache.poi.sl.usermodel.PictureData.PictureType.PNG;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGTexturePaint;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.poi.sl.draw.BitmapImageRenderer;
import org.apache.poi.sl.draw.DrawTexturePaint;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.ImageRenderer;
import org.apache.poi.sl.draw.PathGradientPaint;
import org.apache.poi.sl.draw.PathGradientPaint.PathGradientContext;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.util.Dimension2DDouble;
import org.apache.poi.util.Internal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Extension of Batik's DefaultExtensionHandler which handles different kinds of Paint objects
 * <p>
 * Taken (with permission) from https://gist.github.com/msteiger/4509119,
 * including the fixes that are discussed in the comments
 *
 * @see <a href="https://issues.apache.org/jira/browse/BATIK-1032">BATIK-1032</a>
 */
@Internal
public class SVGRenderExtension extends DefaultExtensionHandler {
    private static final int LINE_LENGTH = 65;
    private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
    private final Map<Long, String> imageMap = new HashMap<>();
    private WeakReference<SVGGraphics2D> svgGraphics2D = null;


    public SVGGraphics2D getSvgGraphics2D() {
        return (svgGraphics2D != null) ? svgGraphics2D.get() : null;
    }

    public void setSvgGraphics2D(SVGGraphics2D svgGraphics2D) {
        this.svgGraphics2D = new WeakReference<>(svgGraphics2D);
    }


    @Override
    public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext generatorContext) {
        if (paint instanceof LinearGradientPaint) {
            return getLgpDescriptor((LinearGradientPaint)paint, generatorContext);
        }

        if (paint instanceof RadialGradientPaint) {
            return getRgpDescriptor((RadialGradientPaint)paint, generatorContext);
        }

        if (paint instanceof PathGradientPaint) {
            return getPathDescriptor((PathGradientPaint)paint, generatorContext);
        }

        if (paint instanceof DrawTexturePaint) {
            return getDtpDescriptor((DrawTexturePaint)paint, generatorContext);
        }

        return super.handlePaint(paint, generatorContext);
    }

    private SVGPaintDescriptor getPathDescriptor(PathGradientPaint gradient, SVGGeneratorContext genCtx) {
        RenderingHints hints = genCtx.getGraphicContextDefaults().getRenderingHints();
        Shape shape = (Shape)hints.get(Drawable.GRADIENT_SHAPE);
        if (shape == null) {
            return null;
        }

        PathGradientContext context = gradient.createContext(ColorModel.getRGBdefault(), shape.getBounds(), shape.getBounds2D(), new AffineTransform(), hints);
        WritableRaster raster = context.createRaster();
        BufferedImage img = new BufferedImage(context.getColorModel(), raster, false, null);

        SVGTexturePaint texturePaint = new SVGTexturePaint(genCtx);
        TexturePaint tp = new TexturePaint(img, shape.getBounds2D());
        return texturePaint.toSVG(tp);
    }


    private SVGPaintDescriptor getRgpDescriptor(RadialGradientPaint gradient, SVGGeneratorContext genCtx) {
        Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_RADIAL_GRADIENT_TAG);

        // Create and set unique XML id
        String id = genCtx.getIDGenerator().generateID("gradient");
        gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

        // Set x,y pairs
        setPoint(gradElem, gradient.getCenterPoint(), "cx", "cy");
        setPoint(gradElem, gradient.getFocusPoint(), "fx", "fy");

        gradElem.setAttribute("r", String.valueOf(gradient.getRadius()));

        addMgpAttributes(gradElem, genCtx, gradient);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
    }

    private SVGPaintDescriptor getLgpDescriptor(LinearGradientPaint gradient, SVGGeneratorContext genCtx) {
        Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);

        // Create and set unique XML id
        String id = genCtx.getIDGenerator().generateID("gradient");
        gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

        // Set x,y pairs
        setPoint(gradElem, gradient.getStartPoint(), "x1", "y1");
        setPoint(gradElem, gradient.getEndPoint(), "x2", "y2");

        addMgpAttributes(gradElem, genCtx, gradient);

        return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
    }

    private void addMgpAttributes(Element gradElem, SVGGeneratorContext genCtx, MultipleGradientPaint gradient) {
        gradElem.setAttribute(SVG_GRADIENT_UNITS_ATTRIBUTE, SVG_USER_SPACE_ON_USE_VALUE);

        // Set cycle method
        final String cycleVal;
        switch (gradient.getCycleMethod()) {
            case REFLECT:
                cycleVal = SVG_REFLECT_VALUE;
                break;
            case REPEAT:
                cycleVal = SVG_REPEAT_VALUE;
                break;
            case NO_CYCLE:
            default:
                cycleVal = SVG_PAD_VALUE;
                break;
        }
        gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, cycleVal);

        // Set color space
        final String colorSpace = (gradient.getColorSpace() == LINEAR_RGB) ? SVG_LINEAR_RGB_VALUE : SVG_SRGB_VALUE;
        gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, colorSpace);

        // Set transform matrix if not identity
        AffineTransform tf = gradient.getTransform();
        if (!tf.isIdentity()) {
            String matrix = "matrix(" + tf.getScaleX() + " " + tf.getShearY()
                    + " " + tf.getShearX() + " " + tf.getScaleY() + " "
                    + tf.getTranslateX() + " " + tf.getTranslateY() + ")";
            gradElem.setAttribute(SVG_GRADIENT_TRANSFORM_ATTRIBUTE, matrix);
        }

        // Convert gradient stops
        final Color[] colors = gradient.getColors();
        final float[] fracs = gradient.getFractions();

        for (int i = 0; i < colors.length; i++) {
            Element stop = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
            SVGPaintDescriptor pd = SVGColor.toSVG(colors[i], genCtx);

            stop.setAttribute(SVG_OFFSET_ATTRIBUTE, (int) (fracs[i] * 100.0f) + "%");
            stop.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, pd.getPaintValue());

            if (colors[i].getAlpha() != 255) {
                stop.setAttribute(SVG_STOP_OPACITY_ATTRIBUTE, pd.getOpacityValue());
            }

            gradElem.appendChild(stop);
        }
    }

    private static void setPoint(Element gradElem, Point2D point, String x, String y) {
        gradElem.setAttribute(x, Double.toString(point.getX()));
        gradElem.setAttribute(y, Double.toString(point.getY()));
    }

    private SVGPaintDescriptor getDtpDescriptor(DrawTexturePaint tdp, SVGGeneratorContext genCtx) {
        String imgID = getImageID(tdp, genCtx);
        Document domFactory = genCtx.getDOMFactory();

        Element patternDef = domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_PATTERN_TAG);
        String patID = genCtx.getIDGenerator().generateID(ID_PREFIX_PATTERN);

        PaintStyle.TexturePaint fill = tdp.getFill();

        Insets2D stretch = fill.getStretch();
        if (stretch == null) {
            stretch = new Insets2D(0,0,0,0);
        }

        Rectangle2D anchorRect = tdp.getAnchorRect();
        String x = genCtx.doubleString(-stretch.left/100_000 * anchorRect.getWidth());
        String y = genCtx.doubleString(-stretch.top/100_000 * anchorRect.getHeight());
        String w = genCtx.doubleString((100_000+stretch.left+stretch.right)/100_000 * anchorRect.getWidth());
        String h = genCtx.doubleString((100_000+stretch.top+stretch.bottom)/100_000 * anchorRect.getHeight());

        Dimension2D scale = fill.getScale();
        if (scale == null) {
            scale = new Dimension2DDouble(1,1);
        }
        Point2D offset = fill.getOffset();
        if (offset == null) {
            offset = new Point2D.Double(0,0);
        }

        PaintStyle.FlipMode flipMode = fill.getFlipMode();
        if (flipMode == null) {
            flipMode = PaintStyle.FlipMode.NONE;
        }

        setAttribute(genCtx, patternDef,
            null, SVG_PATTERN_UNITS_ATTRIBUTE, SVG_OBJECT_BOUNDING_BOX_VALUE,
            null, SVG_ID_ATTRIBUTE, patID,
            null, SVG_X_ATTRIBUTE, offset.getX(),
            null, SVG_Y_ATTRIBUTE, offset.getY(),
            null, SVG_WIDTH_ATTRIBUTE, genCtx.doubleString(scale.getWidth()*100)+"%",
            null, SVG_HEIGHT_ATTRIBUTE, genCtx.doubleString(scale.getHeight()*100)+"%",
            null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, SVG_NONE_VALUE,
            null, SVG_VIEW_BOX_ATTRIBUTE, x+" "+ y+" "+ w+" "+h
        );

        org.apache.poi.sl.usermodel.Shape<?,?> slShape = fill.getShape();

        // TODO: the rotation handling is incomplete and the scale handling is missing
        //  see DrawTexturePaint on how to do it for AWT
        if (!fill.isRotatedWithShape() && slShape instanceof SimpleShape) {
            double rot = ((SimpleShape<?,?>)slShape).getRotation();
            if (rot != 0) {
                setAttribute(genCtx, patternDef,
                    null, SVG_PATTERN_TRANSFORM_ATTRIBUTE, "rotate(" + genCtx.doubleString(-rot) + ")");
            }
        }

        Element useImageEl = domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_USE_TAG);
        useImageEl.setAttributeNS(null, "href", "#"+imgID);
        patternDef.appendChild(useImageEl);

        String patternAttrBuf = URL_PREFIX + SIGN_POUND + patID + URL_SUFFIX;
        return new SVGPaintDescriptor(patternAttrBuf, SVG_OPAQUE_VALUE, patternDef);
    }

    private String getImageID(DrawTexturePaint tdp, SVGGeneratorContext genCtx) {
        final ImageRenderer imgRdr = tdp.getImageRenderer();

        byte[] imgData = null;
        String contentType = null;
        if (imgRdr instanceof BitmapImageRenderer) {
            BitmapImageRenderer bir = (BitmapImageRenderer)imgRdr;
            String ct = bir.getCachedContentType();
            if (PNG.contentType.equals(ct) ||
                JPEG.contentType.equals(ct) ||
                GIF.contentType.equals(ct)) {
                contentType = ct;
                imgData = bir.getCachedImage();
            }
        }
        if (imgData == null) {
            BufferedImage bi = imgRdr.getImage();
            UnsynchronizedByteArrayOutputStream bos = new UnsynchronizedByteArrayOutputStream();
            try {
                ImageIO.write(bi, "PNG", bos);
            } catch (IOException e) {
                return null;
            }
            imgData = bos.toByteArray();
            contentType = PNG.contentType;
        }

        CRC32 crc = new CRC32();
        crc.update(imgData);
        Long imageCrc = crc.getValue();

        String imgID = imageMap.get(imageCrc);
        if (imgID != null) {
            return imgID;
        }

        Document domFactory = genCtx.getDOMFactory();
        Rectangle2D anchorRect = tdp.getAnchorRect();

        imgID = genCtx.getIDGenerator().generateID(ID_PREFIX_IMAGE);
        imageMap.put(imageCrc, imgID);

        // length of a base64 string
        int sbLen = ((4 * imgData.length / 3) + 3) & ~3;
        // add line breaks every 65 chars and a few more padding chars
        sbLen += sbLen / LINE_LENGTH + 30;
        StringBuilder sb = new StringBuilder(sbLen);
        sb.append("data:");
        sb.append(contentType);
        sb.append(";base64,\n");
        sb.append(Base64.getMimeEncoder(LINE_LENGTH, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(imgData));

        Element imageEl = domFactory.createElementNS(SVG_NAMESPACE_URI, SVG_IMAGE_TAG);
        setAttribute(genCtx, imageEl,
            null, SVG_ID_ATTRIBUTE, imgID,
            null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, SVG_NONE_VALUE,
            null, SVG_X_ATTRIBUTE, anchorRect.getX(),
            null, SVG_Y_ATTRIBUTE, anchorRect.getY(),
            null, SVG_WIDTH_ATTRIBUTE, anchorRect.getWidth(),
            null, SVG_HEIGHT_ATTRIBUTE, anchorRect.getHeight(),
            XLINK_NS, "xlink:href", sb.toString()
        );

        getSvgGraphics2D().getDOMTreeManager().addOtherDef(imageEl);

        return imgID;
    }

    private static void setAttribute(SVGGeneratorContext genCtx, Element el, Object... params) {
        for (int i=0; i<params.length; i+=3) {
            String ns = (String)params[i];
            String name = (String)params[i+1];
            Object oval = params[i+2];
            String val;
            if (oval instanceof String) {
                val = (String)oval;
            } else if (oval instanceof Number) {
                val = genCtx.doubleString(((Number) oval).doubleValue());
            } else if (oval == null) {
                val = "";
            } else {
                val = oval.toString();
            }
            el.setAttributeNS(ns, name, val);
        }
    }
}
