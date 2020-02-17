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
import static org.apache.batik.util.SVGConstants.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGTexturePaint;
import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.draw.PathGradientPaint;
import org.apache.poi.sl.draw.PathGradientPaint.PathGradientContext;
import org.apache.poi.util.Internal;
import org.w3c.dom.Element;


/**
 * Extension of Batik's DefaultExtensionHandler which handles different kinds of Paint objects
 * <p>
 * Taken (with permission) from https://gist.github.com/msteiger/4509119,
 * including the fixes that are discussed in the comments
 *
 * @author Martin Steiger
 *
 * @see <a href="https://stackoverflow.com/questions/14258206/">Gradient paints not working in Apache Batik's svggen</a>
 * @see <a href="https://issues.apache.org/jira/browse/BATIK-1032">BATIK-1032</a>
 */
@Internal
public class SVGRenderExtension extends DefaultExtensionHandler {
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
        Color[] colors = gradient.getColors();
        float[] fracs = gradient.getFractions();

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
}
