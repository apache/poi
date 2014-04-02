/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.geom.Context;
import org.apache.poi.xslf.model.geom.CustomGeometry;
import org.apache.poi.xslf.model.geom.Guide;
import org.apache.poi.xslf.model.geom.IAdjustableShape;
import org.apache.poi.xslf.model.geom.Outline;
import org.apache.poi.xslf.model.geom.Path;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPathShadeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathShadeType;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Encapsulates logic to translate DrawingML objects to Java2D
 */
@Internal
class RenderableShape {
    public final static Color NO_PAINT = new Color(0xFF, 0xFF, 0xFF, 0);

    private XSLFSimpleShape _shape;

    public RenderableShape(XSLFSimpleShape shape){
        _shape = shape;
    }

    /**
     * Convert shape fill into java.awt.Paint. The result is either Color or
     * TexturePaint or GradientPaint or null
     *
     * @param graphics  the target graphics
     * @param obj       the xml to read. Must contain elements from the EG_ColorChoice group:
     * <code>
     *     a:scrgbClr    RGB Color Model - Percentage Variant
     *     a:srgbClr    RGB Color Model - Hex Variant
     *     a:hslClr    Hue, Saturation, Luminance Color Model
     *     a:sysClr    System Color
     *     a:schemeClr    Scheme Color
     *     a:prstClr    Preset Color
     *  </code>
     *
     * @param phClr     context color
     * @param parentPart    the parent package part. Any external references (images, etc.) are resolved relative to it.
     *
     * @return  the applied Paint or null if none was applied
     */
    public Paint selectPaint(Graphics2D graphics, XmlObject obj, CTSchemeColor phClr, PackagePart parentPart) {
        XSLFTheme theme = _shape.getSheet().getTheme();

        Paint paint = null;
        if (obj instanceof CTNoFillProperties) {
            paint = NO_PAINT;

        }
        else if (obj instanceof CTSolidColorFillProperties) {
            CTSolidColorFillProperties solidFill = (CTSolidColorFillProperties) obj;
            XSLFColor c = new XSLFColor(solidFill, theme, phClr);
            paint = c.getColor();
        }
        else if (obj instanceof CTBlipFillProperties) {
            CTBlipFillProperties blipFill = (CTBlipFillProperties)obj;
            paint = createTexturePaint(blipFill, graphics, parentPart);
        }
        else if (obj instanceof CTGradientFillProperties) {
            Rectangle2D anchor = getAnchor(graphics);
            CTGradientFillProperties gradFill = (CTGradientFillProperties) obj;
            if (gradFill.isSetLin()) {
                 paint = createLinearGradientPaint(graphics, gradFill, anchor, theme, phClr);
            } else if (gradFill.isSetPath()){
                CTPathShadeProperties ps = gradFill.getPath();
                if(ps.getPath() ==  STPathShadeType.CIRCLE){
                    paint = createRadialGradientPaint(gradFill, anchor, theme, phClr);
                } else if (ps.getPath() ==  STPathShadeType.SHAPE){
                    paint = toRadialGradientPaint(gradFill, anchor, theme, phClr);
                }
            }
        }

        return paint;
    }

    private Paint createTexturePaint(CTBlipFillProperties blipFill, Graphics2D graphics,
            PackagePart parentPart){
        Paint paint = null;
        CTBlip blip = blipFill.getBlip();
        String blipId = blip.getEmbed();
        PackageRelationship rel = parentPart.getRelationship(blipId);
        if (rel != null) {
            XSLFImageRenderer renderer = null;
            if (graphics != null)
                renderer = (XSLFImageRenderer) graphics.getRenderingHint(XSLFRenderingHint.IMAGE_RENDERER);
            if (renderer == null) renderer = new XSLFImageRenderer();

            try {
                BufferedImage img = renderer.readImage(parentPart.getRelatedPart(rel));
                if (blip.sizeOfAlphaModFixArray() > 0) {
                    float alpha = blip.getAlphaModFixArray(0).getAmt() / 100000.f;
                    AlphaComposite ac = AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, alpha);
                    if (graphics != null) graphics.setComposite(ac);
                }

                if(img != null) {
                    paint = new TexturePaint(
                            img, new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight()));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return paint;
    }

    private Paint createLinearGradientPaint(
            Graphics2D graphics,
            CTGradientFillProperties gradFill, Rectangle2D anchor,
            XSLFTheme theme, CTSchemeColor phClr) {
        double angle = gradFill.getLin().getAng() / 60000;
        @SuppressWarnings("deprecation")
        CTGradientStop[] gs = gradFill.getGsLst().getGsArray();

        Arrays.sort(gs, new Comparator<CTGradientStop>() {
            public int compare(CTGradientStop o1, CTGradientStop o2) {
                Integer pos1 = o1.getPos();
                Integer pos2 = o2.getPos();
                return pos1.compareTo(pos2);
            }
        });

        Color[] colors = new Color[gs.length];
        float[] fractions = new float[gs.length];

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

        for (int i = 0; i < gs.length; i++) {
            CTGradientStop stop = gs[i];
            colors[i] = new XSLFColor(stop, theme, phClr).getColor();
            fractions[i] = stop.getPos() / 100000.f;
        }

        AffineTransform grAt  = new AffineTransform();
        if(gradFill.isSetRotWithShape() || !gradFill.getRotWithShape()) {
            double rotation = _shape.getRotation();
            if (rotation != 0.) {
                double centerX = anchor.getX() + anchor.getWidth() / 2;
                double centerY = anchor.getY() + anchor.getHeight() / 2;

                grAt.translate(centerX, centerY);
                grAt.rotate(Math.toRadians(-rotation));
                grAt.translate(-centerX, -centerY);
            }
        }

        // Trick to return GradientPaint on JDK 1.5 and LinearGradientPaint on JDK 1.6+
        Paint paint;
        try {
            Class clz = Class.forName("java.awt.LinearGradientPaint");
            Class clzCycleMethod = Class.forName("java.awt.MultipleGradientPaint$CycleMethod");
            Class clzColorSpaceType = Class.forName("java.awt.MultipleGradientPaint$ColorSpaceType");
            Constructor c =
                    clz.getConstructor(Point2D.class, Point2D.class, float[].class, Color[].class,
                            clzCycleMethod, clzColorSpaceType, AffineTransform.class);
            paint = (Paint) c.newInstance(p1, p2, fractions, colors,
                    Enum.valueOf(clzCycleMethod, "NO_CYCLE"),
                    Enum.valueOf(clzColorSpaceType, "SRGB"), grAt);
        } catch (ClassNotFoundException e) {
            paint = new GradientPaint(p1, colors[0], p2, colors[colors.length - 1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return paint;
    }

    /**
     * gradients with type=shape are enot supported by Java graphics.
     * We approximate it with a radial gradient.
     */
    private static Paint toRadialGradientPaint(
            CTGradientFillProperties gradFill, Rectangle2D anchor,
            XSLFTheme theme, CTSchemeColor phClr) {

        @SuppressWarnings("deprecation")
        CTGradientStop[] gs = gradFill.getGsLst().getGsArray();
        Arrays.sort(gs, new Comparator<CTGradientStop>() {
            public int compare(CTGradientStop o1, CTGradientStop o2) {
                Integer pos1 = o1.getPos();
                Integer pos2 = o2.getPos();
                return pos1.compareTo(pos2);
            }
        });
        gs[1].setPos(50000);

        CTGradientFillProperties g = CTGradientFillProperties.Factory.newInstance();
        g.set(gradFill);
        g.getGsLst().setGsArray(new CTGradientStop[]{gs[0], gs[1]});
        return createRadialGradientPaint(g, anchor, theme, phClr);
    }

    private static Paint createRadialGradientPaint(
            CTGradientFillProperties gradFill, Rectangle2D anchor,
            XSLFTheme theme, CTSchemeColor phClr) {
        @SuppressWarnings("deprecation")
        CTGradientStop[] gs = gradFill.getGsLst().getGsArray();

        Point2D pCenter = new Point2D.Double(anchor.getX() + anchor.getWidth()/2,
                anchor.getY() + anchor.getHeight()/2);

        float radius = (float)Math.max(anchor.getWidth(), anchor.getHeight());

        Arrays.sort(gs, new Comparator<CTGradientStop>() {
            public int compare(CTGradientStop o1, CTGradientStop o2) {
                Integer pos1 = o1.getPos();
                Integer pos2 = o2.getPos();
                return pos1.compareTo(pos2);
            }
        });

        Color[] colors = new Color[gs.length];
        float[] fractions = new float[gs.length];


        for (int i = 0; i < gs.length; i++) {
            CTGradientStop stop = gs[i];
            colors[i] = new XSLFColor(stop, theme, phClr).getColor();
            fractions[i] = stop.getPos() / 100000.f;
        }

        // Trick to return GradientPaint on JDK 1.5 and RadialGradientPaint on JDK 1.6+
        Paint paint;
        try {
            Class clz = Class.forName("java.awt.RadialGradientPaint");
            Constructor c =
                    clz.getConstructor(Point2D.class, float.class,
                            float[].class, Color[].class);
            paint = (Paint) c.newInstance(pCenter, radius, fractions, colors);
        } catch (ClassNotFoundException e) {
            // the result on JDK 1.5 is incorrect, but it is better than nothing
            paint = new GradientPaint(
                    new Point2D.Double(anchor.getX(), anchor.getY()),
                    colors[0], pCenter, colors[colors.length - 1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return paint;
    }

    private static void snapToAnchor(Point2D p, Rectangle2D anchor) {
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


    Paint getPaint(Graphics2D graphics, XmlObject spPr, CTSchemeColor phClr) {

        Paint paint = null;
        for (XmlObject obj : spPr.selectPath("*")) {
            paint = selectPaint(graphics, obj, phClr, _shape.getSheet().getPackagePart());
            if(paint != null) break;
        }
        return paint == NO_PAINT ? null : paint;
    }


    /**
     * fetch shape fill as a java.awt.Paint
     *
     * @return either Color or GradientPaint or TexturePaint or null
     */
    Paint getFillPaint(final Graphics2D graphics) {
        PropertyFetcher<Paint> fetcher = new PropertyFetcher<Paint>() {
            public boolean fetch(XSLFSimpleShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                if (spPr.isSetNoFill()) {
                    setValue(RenderableShape.NO_PAINT); // use it as 'nofill' value
                    return true;
                }
                Paint paint = getPaint(graphics, spPr, null);
                if (paint != null) {
                    setValue(paint);
                    return true;
                }
                return false;
            }
        };
        _shape.fetchShapeProperty(fetcher);

        Paint paint = fetcher.getValue();
        if (paint == null) {
            // fill color was not found, check if it is defined in the theme
            CTShapeStyle style = _shape.getSpStyle();
            if (style != null) {
                // get a reference to a fill style within the style matrix.
                CTStyleMatrixReference fillRef = style.getFillRef();
                // The idx attribute refers to the index of a fill style or
                // background fill style within the presentation's style matrix, defined by the fmtScheme element.
                // value of 0 or 1000 indicates no background,
                // values 1-999 refer to the index of a fill style within the fillStyleLst element
                // values 1001 and above refer to the index of a background fill style within the bgFillStyleLst element.
                int idx = (int)fillRef.getIdx();
                CTSchemeColor phClr = fillRef.getSchemeClr();
                XSLFSheet sheet = _shape.getSheet();
                XSLFTheme theme = sheet.getTheme();
                XmlObject fillProps = null;
                if(idx >= 1 && idx <= 999){
                    fillProps = theme.getXmlObject().
                            getThemeElements().getFmtScheme().getFillStyleLst().selectPath("*")[idx - 1];
                } else if (idx >= 1001 ){
                    fillProps = theme.getXmlObject().
                            getThemeElements().getFmtScheme().getBgFillStyleLst().selectPath("*")[idx - 1001];
                }
                if(fillProps != null) {
                    paint = selectPaint(graphics, fillProps, phClr, sheet.getPackagePart());
                }
            }
        }
        return paint == RenderableShape.NO_PAINT ? null : paint;
    }

    public Paint getLinePaint(final Graphics2D graphics) {
        PropertyFetcher<Paint> fetcher = new PropertyFetcher<Paint>() {
            public boolean fetch(XSLFSimpleShape shape) {
                CTLineProperties spPr = shape.getSpPr().getLn();
                if (spPr != null) {
                    if (spPr.isSetNoFill()) {
                        setValue(NO_PAINT); // use it as 'nofill' value
                        return true;
                    }
                    Paint paint = getPaint(graphics, spPr, null);
                    if (paint != null) {
                        setValue(paint);
                        return true;
                    }
                }
                return false;

            }
        };
        _shape.fetchShapeProperty(fetcher);

        Paint paint = fetcher.getValue();
        if (paint == null) {
            // line color was not found, check if it is defined in the theme
            CTShapeStyle style = _shape.getSpStyle();
            if (style != null) {
                // get a reference to a line style within the style matrix.
                CTStyleMatrixReference lnRef = style.getLnRef();
                int idx = (int)lnRef.getIdx();
                CTSchemeColor phClr = lnRef.getSchemeClr();
                if(idx > 0){
                    XSLFTheme theme = _shape.getSheet().getTheme();
                    XmlObject lnProps = theme.getXmlObject().
                            getThemeElements().getFmtScheme().getLnStyleLst().selectPath("*")[idx - 1];
                    paint = getPaint(graphics, lnProps, phClr);
                }
            }
        }

        return paint == NO_PAINT ? null : paint;
    }

    /**
     * convert PPT dash into java.awt.BasicStroke
     *
     * The mapping is derived empirically on PowerPoint 2010
     */
    private static float[] getDashPattern(LineDash lineDash, float lineWidth) {
        float[] dash = null;
        switch (lineDash) {
            case SYS_DOT:
                dash = new float[]{lineWidth, lineWidth};
                break;
            case SYS_DASH:
                dash = new float[]{2 * lineWidth, 2 * lineWidth};
                break;
            case DASH:
                dash = new float[]{3 * lineWidth, 4 * lineWidth};
                break;
            case DASH_DOT:
                dash = new float[]{4 * lineWidth, 3 * lineWidth, lineWidth,
                        3 * lineWidth};
                break;
            case LG_DASH:
                dash = new float[]{8 * lineWidth, 3 * lineWidth};
                break;
            case LG_DASH_DOT:
                dash = new float[]{8 * lineWidth, 3 * lineWidth, lineWidth,
                        3 * lineWidth};
                break;
            case LG_DASH_DOT_DOT:
                dash = new float[]{8 * lineWidth, 3 * lineWidth, lineWidth,
                        3 * lineWidth, lineWidth, 3 * lineWidth};
                break;
        }
        return dash;
    }


    public Stroke applyStroke(Graphics2D graphics) {

        float lineWidth = (float) _shape.getLineWidth();
        if(lineWidth == 0.0f) lineWidth = 0.25f; // Both PowerPoint and OOo draw zero-length lines as 0.25pt

        LineDash lineDash = _shape.getLineDash();
        float[] dash = null;
        float dash_phase = 0;
        if (lineDash != null) {
            dash = getDashPattern(lineDash, lineWidth);
        }

        int cap = BasicStroke.CAP_BUTT;
        LineCap lineCap = _shape.getLineCap();
        if (lineCap != null) {
            switch (lineCap) {
                case ROUND:
                    cap = BasicStroke.CAP_ROUND;
                    break;
                case SQUARE:
                    cap = BasicStroke.CAP_SQUARE;
                    break;
                default:
                    cap = BasicStroke.CAP_BUTT;
                    break;
            }
        }

        int meter = BasicStroke.JOIN_ROUND;

        Stroke stroke = new BasicStroke(lineWidth, cap, meter, Math.max(1, lineWidth), dash,
                dash_phase);
        graphics.setStroke(stroke);
        return stroke;
    }

    public void render(Graphics2D graphics){
        Collection<Outline> elems = computeOutlines(graphics);

        // shadow
        XSLFShadow shadow = _shape.getShadow();

        // first fill
        Paint fill = getFillPaint(graphics);
        Paint line = getLinePaint(graphics);
        applyStroke(graphics); // the stroke applies both to the shadow and the shape

        // first paint the shadow
        if(shadow != null) for(Outline o : elems){
            if(o.getPath().isFilled()){
                if(fill != null) shadow.fill(graphics, o.getOutline());
                else if(line != null) shadow.draw(graphics, o.getOutline());
            }
        }
        // then fill the shape interior
        if(fill != null) for(Outline o : elems){
            if(o.getPath().isFilled()){
                graphics.setPaint(fill);
                graphics.fill(o.getOutline());
            }
        }

        // then draw any content within this shape (text, image, etc.)
        _shape.drawContent(graphics);

        // then stroke the shape outline
        if(line != null) for(Outline o : elems){
            if(o.getPath().isStroked()){
                graphics.setPaint(line);
                graphics.draw(o.getOutline());
            }
        }
    }

    private Collection<Outline> computeOutlines(Graphics2D graphics) {

        Collection<Outline> lst = new ArrayList<Outline>();
        CustomGeometry geom = _shape.getGeometry();
        if(geom == null) {
            return lst;
        }

        Rectangle2D anchor = getAnchor(graphics);
        for (Path p : geom) {

            double w = p.getW() == -1 ? anchor.getWidth() * Units.EMU_PER_POINT : p.getW();
            double h = p.getH() == -1 ? anchor.getHeight() * Units.EMU_PER_POINT : p.getH();

            // the guides in the shape definitions are all defined relative to each other,
            // so we build the path starting from (0,0).
            final Rectangle2D pathAnchor = new Rectangle2D.Double(
                    0,
                    0,
                    w,
                    h
            );

            Context ctx = new Context(geom, pathAnchor, new IAdjustableShape() {

                public Guide getAdjustValue(String name) {
                    CTPresetGeometry2D prst = _shape.getSpPr().getPrstGeom();
                    if (prst.isSetAvLst()) {
                        for (CTGeomGuide g : prst.getAvLst().getGdList()) {
                            if (g.getName().equals(name)) {
                                return new Guide(g);
                            }
                        }
                    }
                    return null;
                }
            }) ;

            Shape gp = p.getPath(ctx);

            // translate the result to the canvas coordinates in points
            AffineTransform at = new AffineTransform();
            at.translate(anchor.getX(), anchor.getY());

            double scaleX, scaleY;
            if (p.getW() != -1) {
                scaleX = anchor.getWidth() / p.getW();
            } else {
                scaleX = 1.0 / Units.EMU_PER_POINT;
            }
            if (p.getH() != -1) {
                scaleY = anchor.getHeight() / p.getH();
            } else {
                scaleY = 1.0 / Units.EMU_PER_POINT;
            }

            at.scale(scaleX, scaleY);

            Shape canvasShape = at.createTransformedShape(gp);

            lst.add(new Outline(canvasShape, p));
        }

        return lst;
    }

    public Rectangle2D getAnchor(Graphics2D graphics) {
        Rectangle2D anchor = _shape.getAnchor();
        if(graphics == null)  {
            return anchor;
        }

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(XSLFRenderingHint.GROUP_TRANSFORM);
        if(tx != null) {
            anchor = tx.createTransformedShape(anchor).getBounds2D();
        }
        return anchor;
    }    
}
