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

import org.apache.poi.xslf.usermodel.LineCap;
import org.apache.poi.xslf.usermodel.LineDash;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetLineDashProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectStyleItem;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.*;
import java.awt.geom.Rectangle2D;

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
        final XSLFTheme theme = _sheet.getTheme();
        final Color noline = new Color(0,0,0,0);
        PropertyFetcher<Color> fetcher = new PropertyFetcher<Color>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                CTLineProperties ln = spPr.getLn();
                if (ln != null) {
                    if (ln.isSetNoFill()) {
                        setValue(noline);
                        return true;
                    }
                    CTSolidColorFillProperties solidLine = ln.getSolidFill();
                    if (solidLine != null) {
                        setValue( theme.getSolidFillColor(ln.getSolidFill()) );
                        return true;
                    }
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        Color color = fetcher.getValue();
        if(color == null){
            // line color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null) {
                CTSchemeColor schemeColor = style.getLnRef().getSchemeClr();
                if (schemeColor != null) {
                    color = theme.getSchemeColor(schemeColor);
                }
            }
        }
        return color == noline ? null : color;
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
        final XSLFTheme theme = _sheet.getTheme();
        final Color nofill = new Color(0,0,0,0);
        PropertyFetcher<Color> fetcher = new PropertyFetcher<Color>(){
            public boolean fetch(XSLFSimpleShape shape){
                CTShapeProperties spPr = shape.getSpPr();
                if (spPr.isSetNoFill()) {
                    setValue(nofill); // use it as 'nofill' value
                    return true;
                }
                if (spPr.isSetSolidFill()) {
                    setValue( theme.getSolidFillColor(spPr.getSolidFill()) );
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        Color color = fetcher.getValue();
        if(color == null){
            // fill color was not found, check if it is defined in the theme
            CTShapeStyle style = getSpStyle();
            if (style != null) {
                CTSchemeColor schemeColor = style.getFillRef().getSchemeClr();
                if (schemeColor != null) {
                    color = theme.getSchemeColor(schemeColor);
                }
            }
        }
        return color == nofill ? null : color;
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

    protected void applyFill(Graphics2D graphics) {

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

}
