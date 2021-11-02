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

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.draw.geom.Guide;
import org.apache.poi.sl.draw.geom.PresetGeometries;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.LineDecoration;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.draw.geom.XSLFCustomGeometry;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFEffectProperties;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFFillProperties;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFGeometryProperties;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;

/**
 * Represents a single (non-group) shape in a .pptx slide show
 */
@Beta
public abstract class XSLFSimpleShape extends XSLFShape
    implements SimpleShape<XSLFShape,XSLFTextParagraph> {
    private static final CTOuterShadowEffect NO_SHADOW = CTOuterShadowEffect.Factory.newInstance();
    private static final Logger LOG = LogManager.getLogger(XSLFSimpleShape.class);

    /* package */XSLFSimpleShape(XmlObject shape, XSLFSheet sheet) {
        super(shape,sheet);
    }

    @Override
    public void setShapeType(ShapeType type) {
        XSLFGeometryProperties gp = XSLFPropertiesDelegate.getGeometryDelegate(getShapeProperties());
        if (gp == null) {
            return;
        }
        if (gp.isSetCustGeom()) {
            gp.unsetCustGeom();
        }
        CTPresetGeometry2D prst = (gp.isSetPrstGeom()) ? gp.getPrstGeom() : gp.addNewPrstGeom();
        prst.setPrst(STShapeType.Enum.forInt(type.ooxmlId));
    }

    @Override
    public ShapeType getShapeType(){
        XSLFGeometryProperties gp = XSLFPropertiesDelegate.getGeometryDelegate(getShapeProperties());
        if (gp != null && gp.isSetPrstGeom()) {
            STShapeType.Enum geom = gp.getPrstGeom().getPrst();
            if (geom != null) {
                return ShapeType.forId(geom.intValue(), true);
            }
        }
        return null;
    }

    protected CTTransform2D getXfrm(boolean create) {
        PropertyFetcher<CTTransform2D> fetcher = new PropertyFetcher<CTTransform2D>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                XmlObject xo = shape.getShapeProperties();
                if (xo instanceof CTShapeProperties && ((CTShapeProperties)xo).isSetXfrm()) {
                    setValue(((CTShapeProperties)xo).getXfrm());
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        CTTransform2D xfrm = fetcher.getValue();
        if (!create || xfrm != null) {
            return xfrm;
        } else {
            XmlObject xo = getShapeProperties();
            if (xo instanceof CTShapeProperties) {
                return ((CTShapeProperties)xo).addNewXfrm();
            } else {
                // ... group shapes have their own getXfrm()
                LOG.atWarn().log("{} doesn't have xfrm element.", getClass());
                return null;
            }
        }
    }

    @Override
    public Rectangle2D getAnchor() {

        CTTransform2D xfrm = getXfrm(false);
        if (xfrm == null || !xfrm.isSetOff()) {
            return null;
        }

        CTPoint2D off = xfrm.getOff();
        double x = Units.toPoints(POIXMLUnits.parseLength(off.xgetX()));
        double y = Units.toPoints(POIXMLUnits.parseLength(off.xgetY()));
        CTPositiveSize2D ext = xfrm.getExt();
        double cx = Units.toPoints(ext.getCx());
        double cy = Units.toPoints(ext.getCy());
        return new Rectangle2D.Double(x, y, cx, cy);
    }

    @Override
    public void setAnchor(Rectangle2D anchor) {
        CTTransform2D xfrm = getXfrm(true);
        if (xfrm == null) {
            return;
        }
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

    @Override
    public void setRotation(double theta) {
        CTTransform2D xfrm = getXfrm(true);
        if (xfrm != null) {
            xfrm.setRot((int) (theta * 60000));
        }
    }

    @Override
    public double getRotation() {
        CTTransform2D xfrm = getXfrm(false);
        return (xfrm == null || !xfrm.isSetRot()) ? 0 : (xfrm.getRot() / 60000.d);
    }

    @Override
    public void setFlipHorizontal(boolean flip) {
        CTTransform2D xfrm = getXfrm(true);
        if (xfrm != null) {
            xfrm.setFlipH(flip);
        }
    }

    @Override
    public void setFlipVertical(boolean flip) {
        CTTransform2D xfrm = getXfrm(true);
        if (xfrm != null) {
            xfrm.setFlipV(flip);
        }
    }

    @Override
    public boolean getFlipHorizontal() {
        CTTransform2D xfrm = getXfrm(false);
        return (xfrm != null && xfrm.isSetFlipH()) && xfrm.getFlipH();
    }

    @Override
    public boolean getFlipVertical() {
        CTTransform2D xfrm = getXfrm(false);
        return (xfrm != null && xfrm.isSetFlipV()) && xfrm.getFlipV();
    }


    /**
     * Get default line properties defined in the theme (if any).
     * Used internally to resolve shape properties.
     *
     * @return line properties from the theme of null
     */
    private CTLineProperties getDefaultLineProperties() {
        CTShapeStyle style = getSpStyle();
        if (style == null) {
            return null;
        }
        CTStyleMatrixReference lnRef = style.getLnRef();
        if (lnRef == null) {
            return null;
        }
        // 1-based index of a line style within the style matrix
        int idx = Math.toIntExact(lnRef.getIdx());

        XSLFTheme theme = getSheet().getTheme();
        if (theme == null) {
            return null;
        }
        CTBaseStyles styles = theme.getXmlObject().getThemeElements();
        if (styles == null) {
            return null;
        }
        CTStyleMatrix styleMatrix = styles.getFmtScheme();
        if (styleMatrix == null) {
            return null;
        }
        CTLineStyleList lineStyles = styleMatrix.getLnStyleLst();
        if (lineStyles == null || lineStyles.sizeOfLnArray() < idx) {
            return null;
        }

        return lineStyles.getLnArray(idx - 1);
    }

    /**
    * @param color  the color to paint the shape outline.
     * A {@code null} value turns off the shape outline.
     */
    public void setLineColor(Color color) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        if (ln.isSetSolidFill()) {
            ln.unsetSolidFill();
        }
        if (ln.isSetGradFill()) {
            ln.unsetGradFill();
        }
        if (ln.isSetPattFill()) {
            ln.unsetPattFill();
        }
        if (ln.isSetNoFill()) {
            ln.unsetNoFill();
        }


        if (color == null) {
            ln.addNewNoFill();
        } else {
            CTSolidColorFillProperties fill = ln.addNewSolidFill();
            XSLFColor col = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr(), getSheet());
            col.setColor(color);
        }
    }

    /**
     *
     * @return the color of the shape outline or {@code null}
     * if outline is turned off
     */
    @SuppressWarnings("WeakerAccess")
    public Color getLineColor() {
        PaintStyle ps = getLinePaint();
        if (ps instanceof SolidPaint) {
            return ((SolidPaint)ps).getSolidColor().getColor();
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    protected PaintStyle getLinePaint() {
        XSLFSheet sheet = getSheet();
        final XSLFTheme theme = sheet.getTheme();
        final boolean hasPlaceholder = getPlaceholder() != null;
        PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                CTLineProperties spPr = getLn(shape, false);
                XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(spPr);

                if (fp != null && fp.isSetNoFill()) {
                    setValue(null);
                    return true;
                }

                PackagePart pp = shape.getSheet().getPackagePart();
                PaintStyle paint = selectPaint(fp, null, pp, theme, hasPlaceholder);
                if (paint != null) {
                    setValue(paint);
                    return true;
                }

                CTShapeStyle style = shape.getSpStyle();
                if (style != null) {
                    fp = XSLFPropertiesDelegate.getFillDelegate(style.getLnRef());
                    paint = selectPaint(fp, null, pp, theme, hasPlaceholder);

                    // line color was not found, check if it is defined in the theme
                    if (paint == null) {
                        paint = getThemePaint(style, pp);
                    }
                }

                if (paint != null) {
                    setValue(paint);
                    return true;
                }

                return false;
            }

            PaintStyle getThemePaint(CTShapeStyle style, PackagePart pp) {
                // get a reference to a line style within the style matrix.
                CTStyleMatrixReference lnRef = style.getLnRef();
                if (lnRef == null) {
                    return null;
                }
                int idx = Math.toIntExact(lnRef.getIdx());
                CTSchemeColor phClr = lnRef.getSchemeClr();
                if(idx <= 0){
                    return null;
                }

                CTLineProperties props = theme.getXmlObject().getThemeElements().getFmtScheme().getLnStyleLst().getLnArray(idx - 1);
                XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(props);
                return selectPaint(fp, phClr, pp, theme, hasPlaceholder);
            }
        };
        fetchShapeProperty(fetcher);

        return fetcher.getValue();
    }

    /**
     *
     * @param width line width in points. {@code 0} means no line
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineWidth(double width) {
        CTLineProperties lnPr = getLn(this, true);
        if (lnPr == null) {
            return;
        }

        if (width == 0.) {
            if (lnPr.isSetW()) {
                lnPr.unsetW();
            }
            if (!lnPr.isSetNoFill()) {
                lnPr.addNewNoFill();
            }
            if (lnPr.isSetSolidFill()) {
                lnPr.unsetSolidFill();
            }
            if (lnPr.isSetGradFill()) {
                lnPr.unsetGradFill();
            }
            if (lnPr.isSetPattFill()) {
                lnPr.unsetPattFill();
            }
        } else {
            if (lnPr.isSetNoFill()) {
                lnPr.unsetNoFill();
            }

            lnPr.setW(Units.toEMU(width));
        }
    }

    /**
     * @return line width in points. {@code 0} means no line.
     */
    @SuppressWarnings("WeakerAccess")
    public double getLineWidth() {
        PropertyFetcher<Double> fetcher = new PropertyFetcher<Double>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                CTLineProperties ln = getLn(shape, false);
                if (ln != null) {
                    if (ln.isSetNoFill()) {
                        setValue(0.);
                        return true;
                    }

                    if (ln.isSetW()) {
                        setValue(Units.toPoints(ln.getW()));
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        double lineWidth = 0;
        if (fetcher.getValue() == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                if (defaultLn.isSetW()) {
                    lineWidth = Units.toPoints(defaultLn.getW());
                }
            }
        } else {
            lineWidth = fetcher.getValue();
        }

        return lineWidth;
    }


    /**
     * @param compound set the line compound style
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineCompound(LineCompound compound) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }
        if (compound == null) {
            if (ln.isSetCmpd()) {
                ln.unsetCmpd();
            }
        } else {
            STCompoundLine.Enum xCmpd;
            switch (compound) {
                default:
                case SINGLE:
                    xCmpd = STCompoundLine.SNG;
                    break;
                case DOUBLE:
                    xCmpd = STCompoundLine.DBL;
                    break;
                case THICK_THIN:
                    xCmpd = STCompoundLine.THICK_THIN;
                    break;
                case THIN_THICK:
                    xCmpd = STCompoundLine.THIN_THICK;
                    break;
                case TRIPLE:
                    xCmpd = STCompoundLine.TRI;
                    break;
            }
            ln.setCmpd(xCmpd);
        }
    }

    /**
     * @return the line compound
     */
    @SuppressWarnings("WeakerAccess")
    public LineCompound getLineCompound() {
        PropertyFetcher<Integer> fetcher = new PropertyFetcher<Integer>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                CTLineProperties ln = getLn(shape, false);
                if (ln != null) {
                    STCompoundLine.Enum stCmpd = ln.getCmpd();
                    if (stCmpd != null) {
                        setValue(stCmpd.intValue());
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        Integer cmpd = fetcher.getValue();
        if (cmpd == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null && defaultLn.isSetCmpd()) {
                switch (defaultLn.getCmpd().intValue()) {
                default:
                case STCompoundLine.INT_SNG:
                    return LineCompound.SINGLE;
                case STCompoundLine.INT_DBL:
                    return LineCompound.DOUBLE;
                case STCompoundLine.INT_THICK_THIN:
                    return LineCompound.THICK_THIN;
                case STCompoundLine.INT_THIN_THICK:
                    return LineCompound.THIN_THICK;
                case STCompoundLine.INT_TRI:
                    return LineCompound.TRIPLE;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param dash a preset line dashing scheme to stroke thr shape outline
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineDash(LineDash dash) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }
        if (dash == null) {
            if (ln.isSetPrstDash()) {
                ln.unsetPrstDash();
            }
        } else {
            CTPresetLineDashProperties ldp = ln.isSetPrstDash() ? ln.getPrstDash() : ln.addNewPrstDash();
            ldp.setVal(STPresetLineDashVal.Enum.forInt(dash.ooxmlId));
        }
    }

    /**
     * @return  a preset line dashing scheme to stroke the shape outline
     */
    @SuppressWarnings("WeakerAccess")
    public LineDash getLineDash() {

        PropertyFetcher<LineDash> fetcher = new PropertyFetcher<LineDash>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                CTLineProperties ln = getLn(shape, false);
                if (ln == null || !ln.isSetPrstDash()) {
                    return false;
                }

                setValue(LineDash.fromOoxmlId(ln.getPrstDash().getVal().intValue()));
                return true;
            }
        };
        fetchShapeProperty(fetcher);

        LineDash dash = fetcher.getValue();
        if (dash == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null && defaultLn.isSetPrstDash()) {
                dash = LineDash.fromOoxmlId(defaultLn.getPrstDash().getVal().intValue());
            }
        }
        return dash;
    }

    /**
     *
     * @param cap the line end cap style
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineCap(LineCap cap) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        if (cap == null) {
            if (ln.isSetCap()) {
                ln.unsetCap();
            }
        } else {
            ln.setCap(STLineCap.Enum.forInt(cap.ooxmlId));
        }
    }

    /**
     *
     * @return the line end cap style
     */
    @SuppressWarnings("WeakerAccess")
    public LineCap getLineCap() {
        PropertyFetcher<LineCap> fetcher = new PropertyFetcher<LineCap>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                CTLineProperties ln = getLn(shape, false);
                if (ln != null && ln.isSetCap()) {
                    setValue(LineCap.fromOoxmlId(ln.getCap().intValue()));
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        LineCap cap = fetcher.getValue();
        if (cap == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null && defaultLn.isSetCap()) {
                cap = LineCap.fromOoxmlId(defaultLn.getCap().intValue());
            }
        }
        return cap;
    }

    @Override
    public void setFillColor(Color color) {
        XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(getShapeProperties());
        if (fp == null) {
            return;
        }
        if (color == null) {
            if (fp.isSetSolidFill()) {
                fp.unsetSolidFill();
            }

            if (fp.isSetGradFill()) {
                fp.unsetGradFill();
            }

            if (fp.isSetPattFill()) {
                fp.unsetGradFill();
            }

            if (fp.isSetBlipFill()) {
                fp.unsetBlipFill();
            }

            if (!fp.isSetNoFill()) {
                fp.addNewNoFill();
            }
        } else {
            if (fp.isSetNoFill()) {
                fp.unsetNoFill();
            }

            CTSolidColorFillProperties fill = fp.isSetSolidFill() ? fp.getSolidFill() : fp.addNewSolidFill();

            XSLFColor col = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr(), getSheet());
            col.setColor(color);
        }
    }

    @Override
    public Color getFillColor() {
        PaintStyle ps = getFillPaint();
        if (ps instanceof SolidPaint) {
            return DrawPaint.applyColorTransform(((SolidPaint)ps).getSolidColor());
        }
        return null;
    }

    /**
     * @return shadow of this shape or null if shadow is disabled
     */
    @Override
    public XSLFShadow getShadow() {
        PropertyFetcher<CTOuterShadowEffect> fetcher = new PropertyFetcher<CTOuterShadowEffect>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                XSLFEffectProperties ep = XSLFPropertiesDelegate.getEffectDelegate(shape.getShapeProperties());
                if (ep != null && ep.isSetEffectLst()) {
                    CTOuterShadowEffect obj = ep.getEffectLst().getOuterShdw();
                    setValue(obj == null ? NO_SHADOW : obj);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        CTOuterShadowEffect obj = fetcher.getValue();
        if (obj == null) {
            // fill color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null && style.getEffectRef() != null) {
                // 1-based index of a shadow style within the style matrix
                int idx = (int) style.getEffectRef().getIdx();
                if(idx != 0) {
                    CTStyleMatrix styleMatrix = getSheet().getTheme().getXmlObject().getThemeElements().getFmtScheme();
                    CTEffectStyleItem ef = styleMatrix.getEffectStyleLst().getEffectStyleArray(idx - 1);
                    obj = ef.getEffectLst().getOuterShdw();
                }
            }
        }
        return (obj == null || obj == NO_SHADOW) ? null : new XSLFShadow(obj, this);
    }

    /**
     * @return definition of the shape geometry
     */
    @Override
    public CustomGeometry getGeometry() {
        XSLFGeometryProperties gp = XSLFPropertiesDelegate.getGeometryDelegate(getShapeProperties());

        if (gp == null) {
            return null;
        }

        CustomGeometry geom;
        PresetGeometries dict = PresetGeometries.getInstance();
        if(gp.isSetPrstGeom()){
            String name = gp.getPrstGeom().getPrst().toString();
            geom = dict.get(name);
            if(geom == null) {
                throw new IllegalStateException("Unknown shape geometry: " + name + ", available geometries are: " + dict.keySet());
            }
        } else if (gp.isSetCustGeom()){
            geom = XSLFCustomGeometry.convertCustomGeometry(gp.getCustGeom());
        } else {
            geom = dict.get("rect");
        }
        return geom;
    }

    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        XSLFSimpleShape s = (XSLFSimpleShape)sh;

        Color srsSolidFill = s.getFillColor();
        Color tgtSoliFill = getFillColor();
        if(srsSolidFill != null && !srsSolidFill.equals(tgtSoliFill)){
            setFillColor(srsSolidFill);
        }

        XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(getShapeProperties());
        if(fp != null && fp.isSetBlipFill()){
            CTBlip blip = fp.getBlipFill().getBlip();
            String blipId = blip.getEmbed();

            String relId = getSheet().importBlip(blipId, s.getSheet());
            blip.setEmbed(relId);
        }

        Color srcLineColor = s.getLineColor();
        Color tgtLineColor = getLineColor();
        if(srcLineColor != null && !srcLineColor.equals(tgtLineColor)) {
            setLineColor(srcLineColor);
        }

        double srcLineWidth = s.getLineWidth();
        double tgtLineWidth = getLineWidth();
        if(srcLineWidth != tgtLineWidth) {
            setLineWidth(srcLineWidth);
        }

        LineDash srcLineDash = s.getLineDash();
        LineDash tgtLineDash = getLineDash();
        if(srcLineDash != null && srcLineDash != tgtLineDash) {
            setLineDash(srcLineDash);
        }

        LineCap srcLineCap = s.getLineCap();
        LineCap tgtLineCap = getLineCap();
        if(srcLineCap != null && srcLineCap != tgtLineCap) {
            setLineCap(srcLineCap);
        }

    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     *
     * @param style the line end docoration style
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineHeadDecoration(DecorationShape style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetType()) {
                lnEnd.unsetType();
            }
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration shape
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationShape getLineHeadDecoration() {
        CTLineProperties ln = getLn(this, false);
        DecorationShape ds = DecorationShape.NONE;
        if (ln != null && ln.isSetHeadEnd() && ln.getHeadEnd().isSetType()) {
            ds = DecorationShape.fromOoxmlId(ln.getHeadEnd().getType().intValue());
        }
        return ds;
    }

    /**
     * specifies decoration width of the head of a line.
     *
     * @param style the decoration width
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineHeadWidth(DecorationSize style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetW()) {
                lnEnd.unsetW();
            }
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration width
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationSize getLineHeadWidth() {
        CTLineProperties ln = getLn(this, false);
        DecorationSize ds = DecorationSize.MEDIUM;
        if (ln != null && ln.isSetHeadEnd() && ln.getHeadEnd().isSetW()) {
            ds = DecorationSize.fromOoxmlId(ln.getHeadEnd().getW().intValue());
        }
        return ds;
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineHeadLength(DecorationSize style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetLen()) {
                lnEnd.unsetLen();
            }
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration length
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationSize getLineHeadLength() {
        CTLineProperties ln = getLn(this, false);

        DecorationSize ds = DecorationSize.MEDIUM;
        if (ln != null && ln.isSetHeadEnd() && ln.getHeadEnd().isSetLen()) {
            ds = DecorationSize.fromOoxmlId(ln.getHeadEnd().getLen().intValue());
        }
        return ds;
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineTailDecoration(DecorationShape style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetType()) {
                lnEnd.unsetType();
            }
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration shape
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationShape getLineTailDecoration() {
        CTLineProperties ln = getLn(this, false);

        DecorationShape ds = DecorationShape.NONE;
        if (ln != null && ln.isSetTailEnd() && ln.getTailEnd().isSetType()) {
            ds = DecorationShape.fromOoxmlId(ln.getTailEnd().getType().intValue());
        }
        return ds;
    }

    /**
     * specifies decorations which can be added to the tail of a line.
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineTailWidth(DecorationSize style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetW()) {
                lnEnd.unsetW();
            }
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration width
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationSize getLineTailWidth() {
        CTLineProperties ln = getLn(this, false);
        DecorationSize ds = DecorationSize.MEDIUM;
        if (ln != null && ln.isSetTailEnd() && ln.getTailEnd().isSetW()) {
            ds = DecorationSize.fromOoxmlId(ln.getTailEnd().getW().intValue());
        }
        return ds;
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    @SuppressWarnings("WeakerAccess")
    public void setLineTailLength(DecorationSize style) {
        CTLineProperties ln = getLn(this, true);
        if (ln == null) {
            return;
        }

        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetLen()) {
                lnEnd.unsetLen();
            }
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ooxmlId));
        }
    }

    /**
     * @return the line end decoration length
     */
    @SuppressWarnings("WeakerAccess")
    public DecorationSize getLineTailLength() {
        CTLineProperties ln = getLn(this, false);

        DecorationSize ds = DecorationSize.MEDIUM;
        if (ln != null && ln.isSetTailEnd() && ln.getTailEnd().isSetLen()) {
            ds = DecorationSize.fromOoxmlId(ln.getTailEnd().getLen().intValue());
        }
        return ds;
    }

    @Override
    public Guide getAdjustValue(String name) {
        XSLFGeometryProperties gp = XSLFPropertiesDelegate.getGeometryDelegate(getShapeProperties());

        if (gp != null && gp.isSetPrstGeom() && gp.getPrstGeom().isSetAvLst()) {
            for (CTGeomGuide g : gp.getPrstGeom().getAvLst().getGdArray()) {
                if (g.getName().equals(name)) {
                    Guide gd = new Guide();
                    gd.setName(g.getName());
                    gd.setFmla(g.getFmla());
                    return gd;
                }
            }
        }

        return null;
    }

    @Override
    public LineDecoration getLineDecoration() {
        return new LineDecoration() {
            @Override
            public DecorationShape getHeadShape() {
                return getLineHeadDecoration();
            }

            @Override
            public DecorationSize getHeadWidth() {
                return getLineHeadWidth();
            }

            @Override
            public DecorationSize getHeadLength() {
                return getLineHeadLength();
            }

            @Override
            public DecorationShape getTailShape() {
                return getLineTailDecoration();
            }

            @Override
            public DecorationSize getTailWidth() {
                return getLineTailWidth();
            }

            @Override
            public DecorationSize getTailLength() {
                return getLineTailLength();
            }
        };
    }

    /**
     * fetch shape fill as a java.awt.Paint
     *
     * @return either Color or GradientPaint or TexturePaint or null
     */
    @Override
    public FillStyle getFillStyle() {
        return XSLFSimpleShape.this::getFillPaint;
    }

    @Override
    public StrokeStyle getStrokeStyle() {
        return new StrokeStyle() {
            @Override
            public PaintStyle getPaint() {
                return XSLFSimpleShape.this.getLinePaint();
            }

            @Override
            public LineCap getLineCap() {
                return XSLFSimpleShape.this.getLineCap();
            }

            @Override
            public LineDash getLineDash() {
                return XSLFSimpleShape.this.getLineDash();
            }

            @Override
            public double getLineWidth() {
                return XSLFSimpleShape.this.getLineWidth();
            }

            @Override
            public LineCompound getLineCompound() {
                return XSLFSimpleShape.this.getLineCompound();
            }

        };
    }

    @Override
    public void setStrokeStyle(Object... styles) {
        if (styles.length == 0) {
            // remove stroke
            setLineColor(null);
            return;
        }

        // TODO: handle PaintStyle
        for (Object st : styles) {
            if (st instanceof Number) {
                setLineWidth(((Number)st).doubleValue());
            } else if (st instanceof LineCap) {
                setLineCap((LineCap)st);
            } else if (st instanceof LineDash) {
                setLineDash((LineDash)st);
            } else if (st instanceof LineCompound) {
                setLineCompound((LineCompound)st);
            } else if (st instanceof Color) {
                setLineColor((Color)st);
            }
        }
    }

    @Override
    public XSLFHyperlink getHyperlink() {
        CTNonVisualDrawingProps cNvPr = getCNvPr();
        if (!cNvPr.isSetHlinkClick()) {
            return null;
        }
        return new XSLFHyperlink(cNvPr.getHlinkClick(), getSheet());
    }

    @Override
    public XSLFHyperlink createHyperlink() {
        XSLFHyperlink hl = getHyperlink();
        if (hl == null) {
            CTNonVisualDrawingProps cNvPr = getCNvPr();
            hl = new XSLFHyperlink(cNvPr.addNewHlinkClick(), getSheet());
        }
        return hl;
    }

    private static CTLineProperties getLn(XSLFShape shape, boolean create) {
        XmlObject pr = shape.getShapeProperties();
        if (!(pr instanceof CTShapeProperties)) {
            LOG.atWarn().log("{} doesn't have line properties", shape.getClass());
            return null;
        }

        CTShapeProperties spr = (CTShapeProperties)pr;
        return (spr.isSetLn() || !create) ? spr.getLn() : spr.addNewLn();
    }
}
