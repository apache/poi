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
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.geom.Context;
import org.apache.poi.xslf.model.geom.CustomGeometry;
import org.apache.poi.xslf.model.geom.Guide;
import org.apache.poi.xslf.model.geom.IAdjustableShape;
import org.apache.poi.xslf.model.geom.Path;
import org.apache.poi.xslf.model.geom.PresetGeometries;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.TexturePaint;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/**
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFSimpleShape extends XSLFShape {
    private final XmlObject _shape;
    private final XSLFSheet _sheet;
    private CTShapeProperties _spPr;
    private CTShapeStyle _spStyle;
    private CTNonVisualDrawingProps _nvPr;
    private CTPlaceholder _ph;

    /* package */XSLFSimpleShape(XmlObject shape, XSLFSheet sheet) {
        _shape = shape;
        _sheet = sheet;
    }

    public XmlObject getXmlObject() {
        return _shape;
    }

    public XSLFSheet getSheet() {
        return _sheet;
    }

    /**
     * TODO match STShapeType with
     * {@link org.apache.poi.sl.usermodel.ShapeTypes}
     */
    public int getShapeType() {
        CTPresetGeometry2D prst = getSpPr().getPrstGeom();
        STShapeType.Enum stEnum = prst == null ? null : prst.getPrst();
        return stEnum == null ? 0 : stEnum.intValue();
    }

    public String getShapeName() {
        return getNvPr().getName();
    }

    public int getShapeId() {
        return (int) getNvPr().getId();
    }

    protected CTNonVisualDrawingProps getNvPr() {
        if (_nvPr == null) {
            XmlObject[] rs = _shape
                    .selectPath("declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr");
            if (rs.length != 0) {
                _nvPr = (CTNonVisualDrawingProps) rs[0];
            }
        }
        return _nvPr;
    }

    protected CTShapeProperties getSpPr() {
        if (_spPr == null) {
            for (XmlObject obj : _shape.selectPath("*")) {
                if (obj instanceof CTShapeProperties) {
                    _spPr = (CTShapeProperties) obj;
                }
            }
        }
        if (_spPr == null) {
            throw new IllegalStateException("CTShapeProperties was not found.");
        }
        return _spPr;
    }

    protected CTShapeStyle getSpStyle() {
        if (_spStyle == null) {
            for (XmlObject obj : _shape.selectPath("*")) {
                if (obj instanceof CTShapeStyle) {
                    _spStyle = (CTShapeStyle) obj;
                }
            }
        }
        return _spStyle;
    }

    protected CTPlaceholder getCTPlaceholder(){
        if(_ph == null){
            XmlObject[] obj = _shape.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:nvPr/p:ph");
            if(obj.length == 1){
                _ph = (CTPlaceholder)obj[0];
            }
        }
        return _ph;
    }

    private CTTransform2D getXfrm(){
        PropertyFetcher<CTTransform2D> fetcher = new PropertyFetcher<CTTransform2D>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties pr = shape.getSpPr();
                if(pr.isSetXfrm()){
                    setValue(pr.getXfrm());
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue();
    }

    public Rectangle2D getAnchor() {

        CTTransform2D xfrm = getXfrm();
        
        CTPoint2D off = xfrm.getOff();
        long x = off.getX();
        long y = off.getY();
        CTPositiveSize2D ext = xfrm.getExt();
        long cx = ext.getCx();
        long cy = ext.getCy();
        return new Rectangle2D.Double(
                Units.toPoints(x), Units.toPoints(y),
                Units.toPoints(cx), Units.toPoints(cy));
    }

    public void setAnchor(Rectangle2D anchor) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        CTPoint2D off = xfrm.isSetOff() ? xfrm.getOff() : xfrm.addNewOff();
        long x = Units.toEMU(anchor.getX());
        long y = Units.toEMU(anchor.getY());
        off.setX(x);
        off.setY(y);
        CTPositiveSize2D ext = xfrm.isSetExt() ? xfrm.getExt() : xfrm
                .addNewExt();
        long cx = Units.toEMU(anchor.getWidth());
        long cy = Units.toEMU(anchor.getHeight());
        ext.setCx(cx);
        ext.setCy(cy);
    }

    /**
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y
     * axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    public void setRotation(double theta) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setRot((int) (theta * 60000));
    }

    /**
     * Rotation angle in degrees
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y
     * axis).
     * </p>
     *
     * @return rotation angle in degrees
     */
    public double getRotation() {
        CTTransform2D xfrm = getXfrm();
        return (double) xfrm.getRot() / 60000;
    }

    public void setFlipHorizontal(boolean flip) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setFlipH(flip);
    }

    public void setFlipVertical(boolean flip) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setFlipV(flip);
    }

    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public boolean getFlipHorizontal() {
        return getXfrm().getFlipH();
    }

    public boolean getFlipVertical() {
        return getXfrm().getFlipV();
    }

    /**
     * Get line properties defined in the theme (if any)
     *
     * @return line propeties from the theme of null
     */
    CTLineProperties getDefaultLineProperties() {
        CTLineProperties ln = null;
        CTShapeStyle style = getSpStyle();
        if (style != null) {
            // 1-based index of a line style within the style matrix
            int idx = (int) style.getLnRef().getIdx();
            CTStyleMatrix styleMatrix = _sheet.getTheme().getXmlObject().getThemeElements().getFmtScheme();
            ln = styleMatrix.getLnStyleLst().getLnArray(idx - 1);
        }
        return ln;
    }

    public void setLineColor(Color color) {
        CTShapeProperties spPr = getSpPr();
        if (color == null) {
            if (spPr.isSetLn() && spPr.getLn().isSetSolidFill())
                spPr.getLn().unsetSolidFill();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte) color.getRed(),
                    (byte) color.getGreen(), (byte) color.getBlue()});

            CTSolidColorFillProperties fill = ln.isSetSolidFill() ? ln
                    .getSolidFill() : ln.addNewSolidFill();
            fill.setSrgbClr(rgb);
        }
    }

    public Color getLineColor() {
        Paint paint = getLinePaint(null);
        if(paint instanceof Color){
            return (Color)paint;
        }
        return null;
    }

    public Paint getLinePaint(final Graphics2D graphics) {
        final XSLFTheme theme = _sheet.getTheme();
        final Color nofill = new Color(0,0,0,0);
        PropertyFetcher<Paint> fetcher = new PropertyFetcher<Paint>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTLineProperties spPr = shape.getSpPr().getLn();
                if (spPr != null) {
                    if (spPr.isSetNoFill()) {
                        setValue(nofill); // use it as 'nofill' value
                        return true;
                    }
                    Paint paint = getPaint(graphics, spPr);
                    if (paint != null) {
                        setValue( paint );
                        return true;
                    }
                }
                return false;

            }
        };
        fetchShapeProperty(fetcher);

        Paint color = fetcher.getValue();
        if(color == null){
            // line color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null) {
                color = new XSLFColor(style.getLnRef(), theme).getColor();
            }
        }
        return color == nofill ? null : color;
    }

    public void setLineWidth(double width) {
        CTShapeProperties spPr = getSpPr();
        if (width == 0.) {
            if (spPr.isSetLn())
                spPr.getLn().unsetW();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();
            ln.setW(Units.toEMU(width));
        }
    }

    public double getLineWidth() {
        PropertyFetcher<Double> fetcher = new PropertyFetcher<Double>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    if (ln.isSetNoFill()) {
                        setValue(0.);
                        return true;
                    }

                    if (ln.isSetW()) {
                        setValue( Units.toPoints(ln.getW()) );
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        double lineWidth = 0;
        if(fetcher.getValue() == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                if (defaultLn.isSetW()) lineWidth = Units.toPoints(defaultLn.getW());
            }
        } else {
            lineWidth = fetcher.getValue();
        }

        return lineWidth;
    }

    public void setLineDash(LineDash dash) {
        CTShapeProperties spPr = getSpPr();
        if (dash == null) {
            if (spPr.isSetLn())
                spPr.getLn().unsetPrstDash();
        } else {
            CTPresetLineDashProperties val = CTPresetLineDashProperties.Factory
                    .newInstance();
            val.setVal(STPresetLineDashVal.Enum.forInt(dash.ordinal() + 1));
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();
            ln.setPrstDash(val);
        }
    }

    public LineDash getLineDash() {

        PropertyFetcher<LineDash> fetcher = new PropertyFetcher<LineDash>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    CTPresetLineDashProperties ctDash = ln.getPrstDash();
                    if (ctDash != null) {
                        setValue( LineDash.values()[ctDash.getVal().intValue() - 1] );
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        LineDash dash = fetcher.getValue();
        if(dash == null){
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                CTPresetLineDashProperties ctDash = defaultLn.getPrstDash();
                if (ctDash != null) {
                    dash = LineDash.values()[ctDash.getVal().intValue() - 1];
                }
            }
        }
        return dash;
    }

    public void setLineCap(LineCap cap) {
        CTShapeProperties spPr = getSpPr();
        if (cap == null) {
            if (spPr.isSetLn())
                spPr.getLn().unsetCap();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();
            ln.setCap(STLineCap.Enum.forInt(cap.ordinal() + 1));
        }
    }

    public LineCap getLineCap() {
        PropertyFetcher<LineCap> fetcher = new PropertyFetcher<LineCap>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    STLineCap.Enum stCap = ln.getCap();
                    if (stCap != null) {
                        setValue( LineCap.values()[stCap.intValue() - 1] );
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        LineCap cap = fetcher.getValue();
        if(cap == null){
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                STLineCap.Enum stCap = defaultLn.getCap();
                if (stCap != null) {
                    cap = LineCap.values()[stCap.intValue() - 1];
                }
            }
        }
        return cap;
    }

    /**
     * Specifies a solid color fill. The shape is filled entirely with the
     * specified color.
     *
     * @param color the solid color fill. The value of <code>null</code> unsets
     *              the solidFIll attribute from the underlying xml
     */
    public void setFillColor(Color color) {
        CTShapeProperties spPr = getSpPr();
        if (color == null) {
            if (spPr.isSetSolidFill()) spPr.unsetSolidFill();

            if(!spPr.isSetNoFill()) spPr.addNewNoFill();
        } else {
            if(spPr.isSetNoFill()) spPr.unsetNoFill();
            
            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr
                    .getSolidFill() : spPr.addNewSolidFill();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte) color.getRed(),
                    (byte) color.getGreen(), (byte) color.getBlue()});

            fill.setSrgbClr(rgb);
        }
    }

    /**
     * @return solid fill color of null if not set
     */
    public Color getFillColor() {
        Paint paint = getFill(null);
        if(paint instanceof Color){
            return (Color)paint;
        }
        return null;
    }

    /**
     * fetch shape fill as a java.awt.Paint
     *
     * @return either Color or GradientPaint or TexturePaint or null
     */
    Paint getFill(final Graphics2D graphics) {
        final XSLFTheme theme = _sheet.getTheme();
        final Color nofill = new Color(0xFF,0xFF,0xFF, 0);
        PropertyFetcher<Paint> fetcher = new PropertyFetcher<Paint>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                if (spPr.isSetNoFill()) {
                    setValue(nofill); // use it as 'nofill' value
                    return true;
                }
                Paint paint = getPaint(graphics, spPr);
                if (paint != null) {
                    setValue( paint );
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        Paint paint = fetcher.getValue();
        if(paint == null){
            // fill color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null) {
                paint = new XSLFColor(style.getFillRef(), theme).getColor();
            }
        }
        return paint == nofill ? null : paint;
    }

    public XSLFShadow getShadow(){
        PropertyFetcher<CTOuterShadowEffect> fetcher = new PropertyFetcher<CTOuterShadowEffect>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                if (spPr.isSetEffectLst()) {
                    CTOuterShadowEffect obj = spPr.getEffectLst().getOuterShdw();
                    setValue(obj);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        CTOuterShadowEffect obj = fetcher.getValue();
        if(obj == null){
            // fill color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null) {
                // 1-based index of a shadow style within the style matrix
                int idx = (int) style.getEffectRef().getIdx();
                
                CTStyleMatrix styleMatrix = _sheet.getTheme().getXmlObject().getThemeElements().getFmtScheme();
                CTEffectStyleItem ef = styleMatrix.getEffectStyleLst().getEffectStyleArray(idx - 1);
                obj = ef.getEffectLst().getOuterShdw();
            }
        }
        return obj == null ? null : new XSLFShadow(obj, this);
    }

    public void draw(Graphics2D graphics) {

    }

    @SuppressWarnings("deprecation") //  getXYZArray() array accessors are deprecated
    protected Paint getPaint(Graphics2D graphics, XmlObject spPr) {
        XSLFTheme theme = getSheet().getTheme();
        Rectangle2D anchor = getAnchor();

        Paint paint = null;
        for(XmlObject obj : spPr.selectPath("*")){
            if(obj instanceof CTNoFillProperties){
                paint = null;
                break;
            }
            if(obj instanceof CTSolidColorFillProperties){
                CTSolidColorFillProperties solidFill = (CTSolidColorFillProperties)obj;
                XSLFColor c = new XSLFColor(solidFill, theme);
                paint = c.getColor();
            }
            if(obj instanceof CTBlipFillProperties){
                CTBlipFillProperties blipFill = (CTBlipFillProperties)obj;
                CTBlip blip = blipFill.getBlip();
                String blipId = blip.getEmbed();
                PackagePart p = getSheet().getPackagePart();
                PackageRelationship rel = p.getRelationship(blipId);
                if (rel != null) {
                    XSLFImageRendener renderer = null;
                    if(graphics != null) renderer = (XSLFImageRendener)graphics.getRenderingHint(XSLFRenderingHint.IMAGE_RENDERER);
                    if(renderer == null) renderer = new XSLFImageRendener();

                    try {
                        BufferedImage img = renderer.readImage(p.getRelatedPart(rel).getInputStream());
                        if(blip.sizeOfAlphaModFixArray() > 0){
                            float alpha = blip.getAlphaModFixArray(0).getAmt()/100000.f;
                            AlphaComposite ac = AlphaComposite.getInstance(
                                                   AlphaComposite.SRC_OVER, alpha);
                            if(graphics != null) graphics.setComposite(ac);
                        }

                        paint = new TexturePaint(
                                img, new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight()));
                    }
                    catch (Exception e) {
                        return null;
                    }
                }
            }
            if(obj instanceof CTGradientFillProperties){
                CTGradientFillProperties gradFill = (CTGradientFillProperties)obj;
                double angle;
                if(gradFill.isSetLin()) {
                    angle = gradFill.getLin().getAng() / 60000;
                } else {
                    // XSLF only supports linear gradient fills. Other types are filled as liner with angle=90 degrees
                    angle = 90;
                }
                CTGradientStop[] gs =  gradFill.getGsLst().getGsArray();

                Arrays.sort(gs, new Comparator<CTGradientStop>(){
                    public int compare(CTGradientStop o1, CTGradientStop o2){
                        Integer pos1 = o1.getPos();
                        Integer pos2 = o2.getPos();
                        return pos1.compareTo(pos2);
                    }
                });

                Color[] colors = new Color[gs.length];
                float[] fractions = new float[gs.length];

                AffineTransform at = AffineTransform.getRotateInstance(
                        Math.toRadians(angle),
                        anchor.getX() + anchor.getWidth()/2,
                        anchor.getY() + anchor.getHeight()/2);

                double diagonal = Math.sqrt(anchor.getHeight()*anchor.getHeight() + anchor.getWidth()*anchor.getWidth());
                Point2D p1 = new Point2D.Double(anchor.getX() + anchor.getWidth()/2 - diagonal/2,
                        anchor.getY() + anchor.getHeight()/2);
                p1 = at.transform(p1, null);

                Point2D p2 = new Point2D.Double(anchor.getX() + anchor.getWidth(), anchor.getY() + anchor.getHeight()/2);
                p2 = at.transform(p2, null);

                norm(p1, anchor);
                norm(p2, anchor);

                for(int i = 0; i < gs.length; i++){
                    CTGradientStop stop = gs[i];
                    colors[i] = new XSLFColor(stop, theme).getColor();
                    fractions[i] = stop.getPos() / 100000.f;
                }

                paint = createGradientPaint(p1, p2, fractions, colors);
            }
        }
        return paint;
    }

    /**
     * Trick to return GradientPaint on JDK 1.5 and LinearGradientPaint on JDK 1.6+
     */
    private Paint createGradientPaint(Point2D p1, Point2D p2, float[] fractions, Color[] colors){
        Paint paint;
        try {
            Class clz = Class.forName("java.awt.LinearGradientPaint");
            Constructor c =
                    clz.getConstructor(Point2D.class, Point2D.class, float[].class, Color[].class);
            paint = (Paint)c.newInstance(p1, p2, fractions, colors);
        } catch (ClassNotFoundException e){
            paint = new GradientPaint(p1, colors[0], p2, colors[colors.length - 1]);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return paint;
    }

    void norm(Point2D p, Rectangle2D anchor){
        if(p.getX() < anchor.getX()){
            p.setLocation(anchor.getX(), p.getY());
        } else if(p.getX() > (anchor.getX() + anchor.getWidth())){
            p.setLocation(anchor.getX() + anchor.getWidth(), p.getY());
        }

        if(p.getY() < anchor.getY()){
            p.setLocation(p.getX(), anchor.getY());
        } else if (p.getY() > (anchor.getY() + anchor.getHeight())){
            p.setLocation(p.getX(), anchor.getY() + anchor.getHeight());
        }
    }

    protected float[] getDashPattern(LineDash lineDash, float lineWidth) {
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

    protected void applyStroke(Graphics2D graphics) {

        float lineWidth = (float) getLineWidth();
        LineDash lineDash = getLineDash();
        float[] dash = null;
        float dash_phase = 0;
        if (lineDash != null) {
            dash = getDashPattern(lineDash, lineWidth);
        }

        int cap = BasicStroke.CAP_BUTT;
        LineCap lineCap = getLineCap();
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
    }

    /**
     * Walk up the inheritance tree and fetch properties.
     *
     * slide <-- slideLayout <-- slideMaster
     *
     *
     * @param visitor   the object that collects the desired property
     * @return true if the property was fetched
     */
    boolean fetchShapeProperty(PropertyFetcher visitor){
        boolean ok = visitor.fetch(this);

        XSLFSimpleShape masterShape;
        if(!ok){

            // first try to fetch from the slide layout
            XSLFSlideLayout layout = getSheet().getSlideLayout();
            if(layout != null) {
                CTPlaceholder ph = getCTPlaceholder();
                if (ph != null) {
                    masterShape = layout.getPlaceholder(ph);
                    if (masterShape != null) {
                        ok = visitor.fetch(masterShape);
                    }
                }
            }
        }

        // try slide master
        if (!ok) {
            int textType;
            CTPlaceholder ph = getCTPlaceholder();
            if(ph == null || !ph.isSetType()) textType = STPlaceholderType.INT_BODY;
            else {
                switch(ph.getType().intValue()){
                    case STPlaceholderType.INT_TITLE:
                    case STPlaceholderType.INT_CTR_TITLE:
                        textType = STPlaceholderType.INT_TITLE;
                        break;
                    case STPlaceholderType.INT_FTR:
                    case STPlaceholderType.INT_SLD_NUM:
                    case STPlaceholderType.INT_DT:
                        textType = ph.getType().intValue();
                        break;
                    default:
                        textType = STPlaceholderType.INT_BODY;
                        break;
                }
            }
            XSLFSlideMaster master = getSheet().getSlideMaster();
            if(master != null) {
                masterShape = master.getPlaceholderByType(textType);
                if (masterShape != null) {
                    ok = visitor.fetch(masterShape);
                }
            }
        }

        return ok;
    }


    @Override
    protected java.awt.Shape getOutline(){
        PresetGeometries dict = PresetGeometries.getInstance();
        CTShapeProperties spPr = getSpPr();
        String name;
        if(spPr.isSetPrstGeom()) {
            name = spPr.getPrstGeom().getPrst().toString();
        } else {
            name = "rect";
        }
        CustomGeometry geom = dict.get(name);
        Rectangle2D anchor = getAnchor();
        if(geom != null) {
            // the guides in the shape definitions are all defined relative to each other,
            // so we build the path starting from (0,0).
            final Rectangle2D anchorEmu = new Rectangle2D.Double(
                    0,
                    0,
                    Units.toEMU(anchor.getWidth()),
                    Units.toEMU(anchor.getHeight())
            );

            GeneralPath path = new GeneralPath();
            Context ctx = new Context(geom, new IAdjustableShape() {
                public Rectangle2D getAnchor() {
                    return anchorEmu;
                }

                public Guide getAdjustValue(String name) {
                    CTPresetGeometry2D prst = getSpPr().getPrstGeom();
                    if(prst.isSetAvLst()) {
                        for(CTGeomGuide g : prst.getAvLst().getGdList()){
                            if(g.getName().equals(name)) {
                                return new Guide(g);
                            }
                        }
                    }
                    return null;
                }
            });

            for(Path p : geom){
                path.append( p.getPath(ctx) , false);
            }

            // translate the result to the canvas coordinates in points
            AffineTransform at = new AffineTransform();
            at.scale(
                    1.0/Units.EMU_PER_POINT, 1.0/Units.EMU_PER_POINT);
            at.translate(Units.toEMU(anchor.getX()), Units.toEMU(anchor.getY()));
            return at.createTransformedShape(path);
        } else {
            return anchor;
        }
     }

}
