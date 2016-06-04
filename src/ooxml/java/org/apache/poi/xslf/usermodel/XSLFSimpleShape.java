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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.draw.geom.Guide;
import org.apache.poi.sl.draw.geom.PresetGeometries;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.LineDecoration;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationShape;
import org.apache.poi.sl.usermodel.LineDecoration.DecorationSize;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectStyleItem;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineStyleList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetLineDashProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STCompoundLine;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * Represents a single (non-group) shape in a .pptx slide show
 */
@Beta
public abstract class XSLFSimpleShape extends XSLFShape
    implements SimpleShape<XSLFShape,XSLFTextParagraph> {
    private static CTOuterShadowEffect NO_SHADOW = CTOuterShadowEffect.Factory.newInstance();

    /* package */XSLFSimpleShape(XmlObject shape, XSLFSheet sheet) {
        super(shape,sheet);
    }

    @Override
    public void setShapeType(ShapeType type) {
        STShapeType.Enum geom = STShapeType.Enum.forInt(type.ooxmlId);
        getSpPr().getPrstGeom().setPrst(geom);
    }

    @Override
    public ShapeType getShapeType(){
        STShapeType.Enum geom = getSpPr().getPrstGeom().getPrst();
        return ShapeType.forId(geom.intValue(), true);
    }

    protected CTTransform2D getSafeXfrm() {
        CTTransform2D xfrm = getXfrm();
        return (xfrm == null ? getSpPr().addNewXfrm() : xfrm);
    }

    protected CTTransform2D getXfrm() {
        PropertyFetcher<CTTransform2D> fetcher = new PropertyFetcher<CTTransform2D>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties pr = shape.getSpPr();
                if (pr.isSetXfrm()) {
                    setValue(pr.getXfrm());
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue();
    }

    @Override
    public Rectangle2D getAnchor() {

        CTTransform2D xfrm = getXfrm();

        CTPoint2D off = xfrm.getOff();
        double x = Units.toPoints(off.getX());
        double y = Units.toPoints(off.getY());
        CTPositiveSize2D ext = xfrm.getExt();
        double cx = Units.toPoints(ext.getCx());
        double cy = Units.toPoints(ext.getCy());
        return new Rectangle2D.Double(x, y, cx, cy);
    }

    @Override
    public void setAnchor(Rectangle2D anchor) {
        CTTransform2D xfrm = getSafeXfrm();
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
        getSafeXfrm().setRot((int) (theta * 60000));
    }

    @Override
    public double getRotation() {
        CTTransform2D xfrm = getXfrm();
        return (xfrm == null || !xfrm.isSetRot()) ? 0 : (xfrm.getRot() / 60000.d);
    }

    @Override
    public void setFlipHorizontal(boolean flip) {
        getSafeXfrm().setFlipH(flip);
    }

    @Override
    public void setFlipVertical(boolean flip) {
        getSafeXfrm().setFlipV(flip);
    }

    @Override
    public boolean getFlipHorizontal() {
        CTTransform2D xfrm = getXfrm();
        return (xfrm == null || !xfrm.isSetFlipH()) ? false : getXfrm().getFlipH();
    }

    @Override
    public boolean getFlipVertical() {
        CTTransform2D xfrm = getXfrm();
        return (xfrm == null || !xfrm.isSetFlipV()) ? false : getXfrm().getFlipV();
    }


    /**
     * Get default line properties defined in the theme (if any).
     * Used internally to resolve shape properties.
     *
     * @return line propeties from the theme of null
     */
    CTLineProperties getDefaultLineProperties() {
        CTShapeStyle style = getSpStyle();
        if (style == null) return null;
        CTStyleMatrixReference lnRef = style.getLnRef();
        if (lnRef == null) return null;
        // 1-based index of a line style within the style matrix
        int idx = (int)lnRef.getIdx();

        XSLFTheme theme = getSheet().getTheme();
        if (theme == null) return null;
        CTBaseStyles styles = theme.getXmlObject().getThemeElements();
        if (styles == null) return null;
        CTStyleMatrix styleMatrix = styles.getFmtScheme();
        if (styleMatrix == null) return null;
        CTLineStyleList lineStyles = styleMatrix.getLnStyleLst();
        if (lineStyles == null || lineStyles.sizeOfLnArray() < idx) return null;

        return lineStyles.getLnArray(idx - 1);
    }

    /**
    * @param color  the color to paint the shape outline.
     * A <code>null</code> value turns off the shape outline.
     */
    public void setLineColor(Color color) {
        CTShapeProperties spPr = getSpPr();
        CTLineProperties ln = spPr.getLn();
        if (color == null) {
            if (ln == null) {
                return;
            }
            
            if (ln.isSetSolidFill()) {
                ln.unsetSolidFill();
            }
            
            if (!ln.isSetNoFill()) {
                ln.addNewNoFill();
            }
        } else {
            if (ln == null) {
                ln = spPr.addNewLn();
            }
            if (ln.isSetNoFill()) {
                ln.unsetNoFill();
            }
            
            CTSolidColorFillProperties fill = ln.isSetSolidFill() ? ln.getSolidFill() : ln.addNewSolidFill();

            XSLFColor col = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr());
            col.setColor(color);
        }
    }

    /**
     *
     * @return the color of the shape outline or <code>null</code>
     * if outline is turned off
     */
    public Color getLineColor() {
        PaintStyle ps = getLinePaint();
        if (ps instanceof SolidPaint) {
            return ((SolidPaint)ps).getSolidColor().getColor();
        }
        return null;
    }

    protected PaintStyle getLinePaint() {
        final XSLFTheme theme = getSheet().getTheme();
        PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
            public boolean fetch(XSLFShape shape) {
                CTLineProperties spPr = shape.getSpPr().getLn();
                if (spPr != null) {
                    if (spPr.isSetNoFill()) {
                        setValue(null); // use it as 'nofill' value
                        return true;
                    }

                    PaintStyle paint = null;
                    PackagePart pp = getSheet().getPackagePart();
                    for (XmlObject obj : spPr.selectPath("*")) {
                        paint = selectPaint(obj, null, pp, theme);
                        if (paint != null) {
                            setValue(paint);
                            return true;
                        }
                    }

                    CTShapeStyle style = shape.getSpStyle();
                    if (style != null) {
                        paint = selectPaint(style.getLnRef(), theme);
                        if (paint != null) {
                            setValue(paint);
                            return true;
                        }
                    }
                }
                return false;

            }
        };
        fetchShapeProperty(fetcher);

        PaintStyle paint = fetcher.getValue();
        if (paint != null) return paint;

        // line color was not found, check if it is defined in the theme
        CTShapeStyle style = getSpStyle();
        if (style == null) return null;

        // get a reference to a line style within the style matrix.
        CTStyleMatrixReference lnRef = style.getLnRef();
        int idx = (int)lnRef.getIdx();
        CTSchemeColor phClr = lnRef.getSchemeClr();
        if(idx > 0){
            XmlObject lnProps = theme.getXmlObject().
                    getThemeElements().getFmtScheme().getLnStyleLst().selectPath("*")[idx - 1];
            paint = getPaint(lnProps, phClr);
        }

        return paint;
    }

    /**
     *
     * @param width line width in points. <code>0</code> means no line
     */
    public void setLineWidth(double width) {
        CTShapeProperties spPr = getSpPr();
        if (width == 0.) {
            if (spPr.isSetLn() && spPr.getLn().isSetW())
                spPr.getLn().unsetW();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();
            ln.setW(Units.toEMU(width));
        }
    }

    /**
     * @return line width in points. <code>0</code> means no line.
     */
    public double getLineWidth() {
        PropertyFetcher<Double> fetcher = new PropertyFetcher<Double>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
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
                if (defaultLn.isSetW()) lineWidth = Units.toPoints(defaultLn.getW());
            }
        } else {
            lineWidth = fetcher.getValue();
        }

        return lineWidth;
    }


    /**
     * @param compound set the line compound style
     */
    public void setLineCompound(LineCompound compound) {
        CTShapeProperties spPr = getSpPr();
        if (compound == null) {
            if (spPr.isSetLn() && spPr.getLn().isSetCmpd())
                spPr.getLn().unsetCmpd();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
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
    public LineCompound getLineCompound() {
        PropertyFetcher<Integer> fetcher = new PropertyFetcher<Integer>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
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
            if (defaultLn != null) {
                STCompoundLine.Enum stCmpd = defaultLn.getCmpd();
                if (stCmpd != null) {
                    cmpd = stCmpd.intValue();
                }
            }
        }

        if (cmpd == null) return null;

        switch (cmpd) {
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

    /**
     *
     * @param dash a preset line dashing scheme to stroke thr shape outline
     */
    public void setLineDash(LineDash dash) {
        CTShapeProperties spPr = getSpPr();
        if (dash == null) {
            if (spPr.isSetLn() && spPr.getLn().isSetPrstDash())
                spPr.getLn().unsetPrstDash();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
            CTPresetLineDashProperties ldp = ln.isSetPrstDash() ? ln.getPrstDash() : ln.addNewPrstDash();
            ldp.setVal(STPresetLineDashVal.Enum.forInt(dash.ooxmlId));
        }
    }

    /**
     * @return  a preset line dashing scheme to stroke thr shape outline
     */
    public LineDash getLineDash() {

        PropertyFetcher<LineDash> fetcher = new PropertyFetcher<LineDash>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    CTPresetLineDashProperties ctDash = ln.getPrstDash();
                    if (ctDash != null) {
                        setValue(LineDash.fromOoxmlId(ctDash.getVal().intValue()));
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        LineDash dash = fetcher.getValue();
        if (dash == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                CTPresetLineDashProperties ctDash = defaultLn.getPrstDash();
                if (ctDash != null) {
                    dash = LineDash.fromOoxmlId(ctDash.getVal().intValue());
                }
            }
        }
        return dash;
    }

    /**
     *
     * @param cap the line end cap style
     */
    public void setLineCap(LineCap cap) {
        CTShapeProperties spPr = getSpPr();
        if (cap == null) {
            if (spPr.isSetLn() && spPr.getLn().isSetCap())
                spPr.getLn().unsetCap();
        } else {
            CTLineProperties ln = spPr.isSetLn() ? spPr.getLn() : spPr
                    .addNewLn();
            ln.setCap(STLineCap.Enum.forInt(cap.ooxmlId));
        }
    }

    /**
     *
     * @return the line end cap style
     */
    public LineCap getLineCap() {
        PropertyFetcher<LineCap> fetcher = new PropertyFetcher<LineCap>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    STLineCap.Enum stCap = ln.getCap();
                    if (stCap != null) {
                        setValue(LineCap.fromOoxmlId(stCap.intValue()));
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        LineCap cap = fetcher.getValue();
        if (cap == null) {
            CTLineProperties defaultLn = getDefaultLineProperties();
            if (defaultLn != null) {
                STLineCap.Enum stCap = defaultLn.getCap();
                if (stCap != null) {
                    cap = LineCap.fromOoxmlId(stCap.intValue());
                }
            }
        }
        return cap;
    }

    @Override
    public void setFillColor(Color color) {
        CTShapeProperties spPr = getSpPr();
        if (color == null) {
            if (spPr.isSetSolidFill()) {
                spPr.unsetSolidFill();
            }

            if (!spPr.isSetNoFill()) {
                spPr.addNewNoFill();
            }
        } else {
            if (spPr.isSetNoFill()) {
                spPr.unsetNoFill();
            }

            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
                    
            XSLFColor col = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr());
            col.setColor(color);
        }
    }

    @Override
    public Color getFillColor() {
        PaintStyle ps = getFillPaint();
        if (ps instanceof SolidPaint) {
            return ((SolidPaint)ps).getSolidColor().getColor();
        }
        return null;
    }

    /**
     * @return shadow of this shape or null if shadow is disabled
     */
    public XSLFShadow getShadow() {
        PropertyFetcher<CTOuterShadowEffect> fetcher = new PropertyFetcher<CTOuterShadowEffect>() {
            public boolean fetch(XSLFShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                if (spPr.isSetEffectLst()) {
                    CTOuterShadowEffect obj = spPr.getEffectLst().getOuterShdw();
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
            if (style != null) {
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
     *
     * @return definition of the shape geometry
     */
    public CustomGeometry getGeometry(){
        CTShapeProperties spPr = getSpPr();
        CustomGeometry geom;
        PresetGeometries dict = PresetGeometries.getInstance();
        if(spPr.isSetPrstGeom()){
            String name = spPr.getPrstGeom().getPrst().toString();
            geom = dict.get(name);
            if(geom == null) {
                throw new IllegalStateException("Unknown shape geometry: " + name + ", available geometries are: " + dict.keySet());
            }
        } else if (spPr.isSetCustGeom()){
            XMLStreamReader staxReader = spPr.getCustGeom().newXMLStreamReader();
            geom = PresetGeometries.convertCustomGeometry(staxReader);
            try { staxReader.close(); }
            catch (XMLStreamException e) {}
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

        if(getSpPr().isSetBlipFill()){
            CTBlip blip = getSpPr().getBlipFill().getBlip();
            String blipId = blip.getEmbed();

            String relId = getSheet().importBlip(blipId, s.getSheet().getPackagePart());
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
     */
    public void setLineHeadDecoration(DecorationShape style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationShape getLineHeadDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return DecorationShape.NONE;

        STLineEndType.Enum end = ln.getHeadEnd().getType();
        return end == null ? DecorationShape.NONE : DecorationShape.fromOoxmlId(end.intValue());
    }

    /**
     * specifies decorations which can be added to the head of a line.
     */
    public void setLineHeadWidth(DecorationSize style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationSize getLineHeadWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return DecorationSize.MEDIUM;

        STLineEndWidth.Enum w = ln.getHeadEnd().getW();
        return w == null ? DecorationSize.MEDIUM : DecorationSize.fromOoxmlId(w.intValue());
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineHeadLength(DecorationSize style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationSize getLineHeadLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return DecorationSize.MEDIUM;

        STLineEndLength.Enum len = ln.getHeadEnd().getLen();
        return len == null ? DecorationSize.MEDIUM : DecorationSize.fromOoxmlId(len.intValue());
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineTailDecoration(DecorationShape style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationShape getLineTailDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return DecorationShape.NONE;

        STLineEndType.Enum end = ln.getTailEnd().getType();
        return end == null ? DecorationShape.NONE : DecorationShape.fromOoxmlId(end.intValue());
    }

    /**
     * specifies decorations which can be added to the tail of a line.
     */
    public void setLineTailWidth(DecorationSize style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationSize getLineTailWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return DecorationSize.MEDIUM;

        STLineEndWidth.Enum w = ln.getTailEnd().getW();
        return w == null ? DecorationSize.MEDIUM : DecorationSize.fromOoxmlId(w.intValue());
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineTailLength(DecorationSize style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ooxmlId));
        }
    }

    public DecorationSize getLineTailLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return DecorationSize.MEDIUM;

        STLineEndLength.Enum len = ln.getTailEnd().getLen();
        return len == null ? DecorationSize.MEDIUM : DecorationSize.fromOoxmlId(len.intValue());
    }

    public boolean isPlaceholder() {
        CTPlaceholder ph = getCTPlaceholder();
        return ph != null;
    }

    public Guide getAdjustValue(String name) {
        CTPresetGeometry2D prst = getSpPr().getPrstGeom();
        if (prst.isSetAvLst()) {
            for (CTGeomGuide g : prst.getAvLst().getGdArray()) {
                if (g.getName().equals(name)) {
                    return new Guide(g.getName(), g.getFmla());
                }
            }
        }

        return null;
    }

    public LineDecoration getLineDecoration() {
        return new LineDecoration() {
            public DecorationShape getHeadShape() {
                return getLineHeadDecoration();
            }

            public DecorationSize getHeadWidth() {
                return getLineHeadWidth();
            }

            public DecorationSize getHeadLength() {
                return getLineHeadLength();
            }

            public DecorationShape getTailShape() {
                return getLineTailDecoration();
            }

            public DecorationSize getTailWidth() {
                return getLineTailWidth();
            }

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
    public FillStyle getFillStyle() {
        return new FillStyle() {
            public PaintStyle getPaint() {
                return XSLFSimpleShape.this.getFillPaint();
            }
        };
    }

    public StrokeStyle getStrokeStyle() {
        return new StrokeStyle() {
            public PaintStyle getPaint() {
                return XSLFSimpleShape.this.getLinePaint();
            }

            public LineCap getLineCap() {
                return XSLFSimpleShape.this.getLineCap();
            }

            public LineDash getLineDash() {
                return XSLFSimpleShape.this.getLineDash();
            }

            public double getLineWidth() {
                return XSLFSimpleShape.this.getLineWidth();
            }

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
    public void setPlaceholder(Placeholder placeholder) {
        super.setPlaceholder(placeholder);
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
}
