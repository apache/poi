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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.geom.CustomGeometry;
import org.apache.poi.xslf.model.geom.Outline;
import org.apache.poi.xslf.model.geom.Path;
import org.apache.poi.xslf.model.geom.PresetGeometries;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single (non-group) shape in a .pptx slide show
 *
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFSimpleShape extends XSLFShape {
    private static CTOuterShadowEffect NO_SHADOW = CTOuterShadowEffect.Factory.newInstance();

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

    @Override
    public XmlObject getXmlObject() {
        return _shape;
    }

    /**
     *
     * @return the sheet this shape belongs to
     */
    public XSLFSheet getSheet() {
        return _sheet;
    }

    /**
     *
     * @param type
     */
    public void setShapeType(XSLFShapeType type){
        CTShape shape = (CTShape) getXmlObject();
        STShapeType.Enum geom = STShapeType.Enum.forInt(type.getIndex());
        shape.getSpPr().getPrstGeom().setPrst(geom);
    }

    public XSLFShapeType getShapeType(){
        CTShape shape = (CTShape) getXmlObject();
        STShapeType.Enum geom = shape.getSpPr().getPrstGeom().getPrst();
        return XSLFShapeType.forInt(geom.intValue());
    }

    @Override
    public String getShapeName() {
        return getNvPr().getName();
    }

    @Override
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

    protected CTPlaceholder getCTPlaceholder() {
        if (_ph == null) {
            XmlObject[] obj = _shape.selectPath(
                    "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:nvPr/p:ph");
            if (obj.length == 1) {
                _ph = (CTPlaceholder) obj[0];
            }
        }
        return _ph;
    }

    CTTransform2D getXfrm() {
        PropertyFetcher<CTTransform2D> fetcher = new PropertyFetcher<CTTransform2D>() {
            public boolean fetch(XSLFSimpleShape shape) {
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
        long x = off.getX();
        long y = off.getY();
        CTPositiveSize2D ext = xfrm.getExt();
        long cx = ext.getCx();
        long cy = ext.getCy();
        return new Rectangle2D.Double(
                Units.toPoints(x), Units.toPoints(y),
                Units.toPoints(cx), Units.toPoints(cy));
    }

    @Override
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

    @Override
    public void setRotation(double theta) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setRot((int) (theta * 60000));
    }

    @Override
    public double getRotation() {
        CTTransform2D xfrm = getXfrm();
        return (double) xfrm.getRot() / 60000;
    }

    @Override
    public void setFlipHorizontal(boolean flip) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setFlipH(flip);
    }

    @Override
    public void setFlipVertical(boolean flip) {
        CTShapeProperties spPr = getSpPr();
        CTTransform2D xfrm = spPr.isSetXfrm() ? spPr.getXfrm() : spPr.addNewXfrm();
        xfrm.setFlipV(flip);
    }

    @Override
    public boolean getFlipHorizontal() {
        return getXfrm().getFlipH();
    }

    @Override
    public boolean getFlipVertical() {
        return getXfrm().getFlipV();
    }

    /**
     * Get default line properties defined in the theme (if any).
     * Used internally to resolve shape properties.
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

    /**
    * @param color  the color to paint the shape outline.
     * A <code>null</code> value turns off the shape outline.
     */
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
            if(fill.isSetHslClr()) fill.unsetHslClr();
            if(fill.isSetPrstClr()) fill.unsetPrstClr();
            if(fill.isSetSchemeClr()) fill.unsetSchemeClr();
            if(fill.isSetScrgbClr()) fill.unsetScrgbClr();
            if(fill.isSetSysClr()) fill.unsetSysClr();
        }
    }

    /**
     *
     * @return the color of the shape outline or <code>null</code>
     * if outline is turned off
     */
    public Color getLineColor() {
        RenderableShape rShape = new RenderableShape(this);
        Paint paint = rShape.getLinePaint(null);
        if (paint instanceof Color) {
            return (Color) paint;
        }
        return null;
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
     *
     * @return line width in points. <code>0</code> means no line.
     */
    public double getLineWidth() {
        PropertyFetcher<Double> fetcher = new PropertyFetcher<Double>() {
            public boolean fetch(XSLFSimpleShape shape) {
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
     *
     * @param dash a preset line dashing scheme to stroke thr shape outline
     */
    public void setLineDash(LineDash dash) {
        CTShapeProperties spPr = getSpPr();
        if (dash == null) {
            if (spPr.isSetLn() &&  spPr.getLn().isSetPrstDash())
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

    /**
     * @return  a preset line dashing scheme to stroke thr shape outline
     */
    public LineDash getLineDash() {

        PropertyFetcher<LineDash> fetcher = new PropertyFetcher<LineDash>() {
            public boolean fetch(XSLFSimpleShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    CTPresetLineDashProperties ctDash = ln.getPrstDash();
                    if (ctDash != null) {
                        setValue(LineDash.values()[ctDash.getVal().intValue() - 1]);
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
                    dash = LineDash.values()[ctDash.getVal().intValue() - 1];
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
            ln.setCap(STLineCap.Enum.forInt(cap.ordinal() + 1));
        }
    }

    /**
     *
     * @return the line end cap style
     */
    public LineCap getLineCap() {
        PropertyFetcher<LineCap> fetcher = new PropertyFetcher<LineCap>() {
            public boolean fetch(XSLFSimpleShape shape) {
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    STLineCap.Enum stCap = ln.getCap();
                    if (stCap != null) {
                        setValue(LineCap.values()[stCap.intValue() - 1]);
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

            if (!spPr.isSetNoFill()) spPr.addNewNoFill();
        } else {
            if (spPr.isSetNoFill()) spPr.unsetNoFill();

            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr
                    .getSolidFill() : spPr.addNewSolidFill();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte) color.getRed(),
                    (byte) color.getGreen(), (byte) color.getBlue()});

            fill.setSrgbClr(rgb);
            if(fill.isSetHslClr()) fill.unsetHslClr();
            if(fill.isSetPrstClr()) fill.unsetPrstClr();
            if(fill.isSetSchemeClr()) fill.unsetSchemeClr();
            if(fill.isSetScrgbClr()) fill.unsetScrgbClr();
            if(fill.isSetSysClr()) fill.unsetSysClr();
        }
    }

    /**
     * @return solid fill color of null if not set or fill color
     * is not solid (pattern or gradient)
     */
    public Color getFillColor() {
        RenderableShape rShape = new RenderableShape(this);
        Paint paint = rShape.getFillPaint(null);
        if (paint instanceof Color) {
            return (Color) paint;
        }
        return null;
    }

    /**
     * @return shadow of this shape or null if shadow is disabled
     */
    public XSLFShadow getShadow() {
        PropertyFetcher<CTOuterShadowEffect> fetcher = new PropertyFetcher<CTOuterShadowEffect>() {
            public boolean fetch(XSLFSimpleShape shape) {
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
                    CTStyleMatrix styleMatrix = _sheet.getTheme().getXmlObject().getThemeElements().getFmtScheme();
                    CTEffectStyleItem ef = styleMatrix.getEffectStyleLst().getEffectStyleArray(idx - 1);
                    obj = ef.getEffectLst().getOuterShdw();
                }
            }
        }
        return (obj == null || obj == NO_SHADOW) ? null : new XSLFShadow(obj, this);
    }

    @Override
    public void draw(Graphics2D graphics) {
        RenderableShape rShape = new RenderableShape(this);
        rShape.render(graphics);

        // draw line decorations
        Color lineColor = getLineColor();
        if(lineColor != null) {
            graphics.setPaint(lineColor);
            for(Outline o : getDecorationOutlines(graphics)){
                if(o.getPath().isFilled()){
                    graphics.fill(o.getOutline());
                }
                if(o.getPath().isStroked()){
                    graphics.draw(o.getOutline());
                }
            }
        }
    }


    /**
     * Walk up the inheritance tree and fetch shape properties.
     *
     * The following order of inheritance is assumed:
     * <p>
     * slide <-- slideLayout <-- slideMaster
     * </p>
     *
     * @param visitor the object that collects the desired property
     * @return true if the property was fetched
     */
    boolean fetchShapeProperty(PropertyFetcher visitor) {
        boolean ok = visitor.fetch(this);

        XSLFSimpleShape masterShape;
        XSLFSheet masterSheet = getSheet().getMasterSheet();
        CTPlaceholder ph = getCTPlaceholder();

        if (masterSheet != null && ph != null) {
            if (!ok) {
                masterShape = masterSheet.getPlaceholder(ph);
                if (masterShape != null) {
                    ok = visitor.fetch(masterShape);
                }
            }

            // try slide master
            if (!ok ) {
                int textType;
                if ( !ph.isSetType()) textType = STPlaceholderType.INT_BODY;
                else {
                    switch (ph.getType().intValue()) {
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
                XSLFSheet master = masterSheet.getMasterSheet();
                if (master != null) {
                    masterShape = master.getPlaceholderByType(textType);
                    if (masterShape != null) {
                        ok = visitor.fetch(masterShape);
                    }
                }
            }
        }
        return ok;
    }

    /**
     *
     * @return definition of the shape geometry
     */
    CustomGeometry getGeometry(){
        CTShapeProperties spPr = getSpPr();
        CustomGeometry geom;
        PresetGeometries dict = PresetGeometries.getInstance();
        if(spPr.isSetPrstGeom()){
            String name = spPr.getPrstGeom().getPrst().toString();
            geom = dict.get(name);
            if(geom == null) {
                throw new IllegalStateException("Unknown shape geometry: " + name);
            }
        } else if (spPr.isSetCustGeom()){
            geom = new CustomGeometry(spPr.getCustGeom());
        } else {
            geom = dict.get("rect");
        }
        return geom;
    }


    /**
     * draw any content within this shape (image, text, etc.).
     *
     * @param graphics the graphics to draw into
     */
    public void drawContent(Graphics2D graphics){

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
    public void setLineHeadDecoration(LineDecoration style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineHeadDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getHeadEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the head of a line.
     */
    public void setLineHeadWidth(LineEndWidth style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineHeadWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineEndWidth.MEDIUM;

        STLineEndWidth.Enum w = ln.getHeadEnd().getW();
        return w == null ? LineEndWidth.MEDIUM : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineHeadLength(LineEndLength style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetHeadEnd() ? ln.getHeadEnd() : ln.addNewHeadEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineHeadLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetHeadEnd()) return LineEndLength.MEDIUM;

        STLineEndLength.Enum len = ln.getHeadEnd().getLen();
        return len == null ? LineEndLength.MEDIUM : LineEndLength.values()[len.intValue() - 1];
    }

    /**
     * Specifies the line end decoration, such as a triangle or arrowhead.
     */
    public void setLineTailDecoration(LineDecoration style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetType()) lnEnd.unsetType();
        } else {
            lnEnd.setType(STLineEndType.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineDecoration getLineTailDecoration() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineDecoration.NONE;

        STLineEndType.Enum end = ln.getTailEnd().getType();
        return end == null ? LineDecoration.NONE : LineDecoration.values()[end.intValue() - 1];
    }

    /**
     * specifies decorations which can be added to the tail of a line.
     */
    public void setLineTailWidth(LineEndWidth style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();
        if (style == null) {
            if (lnEnd.isSetW()) lnEnd.unsetW();
        } else {
            lnEnd.setW(STLineEndWidth.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndWidth getLineTailWidth() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineEndWidth.MEDIUM;

        STLineEndWidth.Enum w = ln.getTailEnd().getW();
        return w == null ? LineEndWidth.MEDIUM : LineEndWidth.values()[w.intValue() - 1];
    }

    /**
     * Specifies the line end width in relation to the line width.
     */
    public void setLineTailLength(LineEndLength style) {
        CTLineProperties ln = getSpPr().getLn();
        CTLineEndProperties lnEnd = ln.isSetTailEnd() ? ln.getTailEnd() : ln.addNewTailEnd();

        if (style == null) {
            if (lnEnd.isSetLen()) lnEnd.unsetLen();
        } else {
            lnEnd.setLen(STLineEndLength.Enum.forInt(style.ordinal() + 1));
        }
    }

    public LineEndLength getLineTailLength() {
        CTLineProperties ln = getSpPr().getLn();
        if (ln == null || !ln.isSetTailEnd()) return LineEndLength.MEDIUM;

        STLineEndLength.Enum len = ln.getTailEnd().getLen();
        return len == null ? LineEndLength.MEDIUM : LineEndLength.values()[len.intValue() - 1];
    }

    Outline getTailDecoration(Graphics2D graphics) {
        LineEndLength tailLength = getLineTailLength();
        LineEndWidth tailWidth = getLineTailWidth();

        double lineWidth = Math.max(2.5, getLineWidth());

        Rectangle2D anchor = new RenderableShape(this).getAnchor(graphics);
        double x2 = anchor.getX() + anchor.getWidth(),
                y2 = anchor.getY() + anchor.getHeight();

        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());

        AffineTransform at = new AffineTransform();
        Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = Math.pow(2, tailWidth.ordinal());
        double scaleX = Math.pow(2, tailLength.ordinal());
        switch (getLineTailDecoration()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x2 - bounds.getWidth() / 2, y2 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case ARROW:
                p = new Path();
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (-lineWidth * 3), (float) (-lineWidth * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (-lineWidth * 3), (float) (lineWidth * 2));
                shape = arrow;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = tailWidth.ordinal() + 1;
                scaleX = tailLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (-lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (-lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x2, y2);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }

    Outline getHeadDecoration(Graphics2D graphics) {
        LineEndLength headLength = getLineHeadLength();
        LineEndWidth headWidth = getLineHeadWidth();

        double lineWidth = Math.max(2.5, getLineWidth());

        Rectangle2D anchor = new RenderableShape(this).getAnchor(graphics);
        double x1 = anchor.getX(),
                y1 = anchor.getY();

        double alpha = Math.atan(anchor.getHeight() / anchor.getWidth());

        AffineTransform at = new AffineTransform();
        Shape shape = null;
        Path p = null;
        Rectangle2D bounds;
        double scaleY = 1;
        double scaleX = 1;
        switch (getLineHeadDecoration()) {
            case OVAL:
                p = new Path();
                shape = new Ellipse2D.Double(0, 0, lineWidth * scaleX, lineWidth * scaleY);
                bounds = shape.getBounds2D();
                at.translate(x1 - bounds.getWidth() / 2, y1 - bounds.getHeight() / 2);
                at.rotate(alpha, bounds.getX() + bounds.getWidth() / 2, bounds.getY() + bounds.getHeight() / 2);
                break;
            case STEALTH:
            case ARROW:
                p = new Path(false, true);
                GeneralPath arrow = new GeneralPath();
                arrow.moveTo((float) (lineWidth * 3 * scaleX), (float) (-lineWidth * scaleY * 2));
                arrow.lineTo(0, 0);
                arrow.lineTo((float) (lineWidth * 3 * scaleX), (float) (lineWidth * scaleY * 2));
                shape = arrow;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            case TRIANGLE:
                p = new Path();
                scaleY = headWidth.ordinal() + 1;
                scaleX = headLength.ordinal() + 1;
                GeneralPath triangle = new GeneralPath();
                triangle.moveTo((float) (lineWidth * scaleX), (float) (-lineWidth * scaleY / 2));
                triangle.lineTo(0, 0);
                triangle.lineTo((float) (lineWidth * scaleX), (float) (lineWidth * scaleY / 2));
                triangle.closePath();
                shape = triangle;
                at.translate(x1, y1);
                at.rotate(alpha);
                break;
            default:
                break;
        }

        if (shape != null) {
            shape = at.createTransformedShape(shape);
        }
        return shape == null ? null : new Outline(shape, p);
    }

    private List<Outline> getDecorationOutlines(Graphics2D graphics){
        List<Outline> lst = new ArrayList<Outline>();

        Outline head = getHeadDecoration(graphics);
        if(head != null) lst.add(head);

        Outline tail = getTailDecoration(graphics);
        if(tail != null) lst.add(tail);
        return lst;
    }

}
