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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.PlaceholderDetails;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFFillProperties;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.STSchemeColorVal;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_DRAWINGML;
import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_PRESENTATIONML;

/**
 * Base super-class class for all shapes in PresentationML
 */
@Beta
public abstract class XSLFShape implements Shape<XSLFShape,XSLFTextParagraph> {

    @Internal
    public interface ReparseFactory<T extends XmlObject> {
        T parse(XMLStreamReader reader) throws XmlException;
    }

    static final String DML_NS = NS_DRAWINGML;
    static final String PML_NS = NS_PRESENTATIONML;

    private static final QName[] NV_CONTAINER = {
        new QName(PML_NS, "nvSpPr"),
        new QName(PML_NS, "nvCxnSpPr"),
        new QName(PML_NS, "nvGrpSpPr"),
        new QName(PML_NS, "nvPicPr"),
        new QName(PML_NS, "nvGraphicFramePr")
    };

    private static final QName[] CNV_PROPS = {
        new QName(PML_NS, "cNvPr")
    };

    private final XmlObject _shape;
    private final XSLFSheet _sheet;
    private XSLFShapeContainer _parent;

    private CTShapeStyle _spStyle;
    private CTNonVisualDrawingProps _nvPr;

    protected XSLFShape(XmlObject shape, XSLFSheet sheet) {
        _shape = shape;
        _sheet = sheet;
    }

    /**
     * @return the xml bean holding this shape's data
     */
    public final XmlObject getXmlObject() {
        // it's final because the xslf inheritance hierarchy is not necessary the same as
        // the (not existing) xmlbeans hierarchy and subclasses shouldn't narrow it's return value
        return _shape;
    }

    @Override
    public XSLFSheet getSheet() {
        return _sheet;
    }

    @Override
    public String getShapeName() {
        CTNonVisualDrawingProps nonVisualDrawingProps = getCNvPr();
        return nonVisualDrawingProps == null ? null : nonVisualDrawingProps.getName();
    }

    @Override
    public int getShapeId() {
        CTNonVisualDrawingProps nonVisualDrawingProps = getCNvPr();
        if (nonVisualDrawingProps == null) {
            throw new IllegalStateException("no underlying shape exists");
        }
        return Math.toIntExact(nonVisualDrawingProps.getId());
    }

    /**
     * Set the contents of this shape to be a copy of the source shape.
     * This method is called recursively for each shape when merging slides
     *
     * @param  sh the source shape
     * @see org.apache.poi.xslf.usermodel.XSLFSlide#importContent(XSLFSheet)
     */
    @Internal
    void copy(XSLFShape sh) {
        if (!getClass().isInstance(sh)) {
            throw new IllegalArgumentException(
                    "Can't copy " + sh.getClass().getSimpleName() + " into " + getClass().getSimpleName());
        }

        if (this instanceof PlaceableShape) {
            PlaceableShape<?,?> ps = (PlaceableShape<?,?>)this;
            Rectangle2D anchor = sh.getAnchor();
            if (anchor != null) {
                ps.setAnchor(anchor);
            }
        }


    }

    public void setParent(XSLFShapeContainer parent) {
        this._parent = parent;
    }

    @Override
    public XSLFShapeContainer getParent() {
        return this._parent;
    }

    protected PaintStyle getFillPaint() {
        final XSLFTheme theme = getSheet().getTheme();
        final boolean hasPlaceholder = getPlaceholder() != null;

        PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
            @Override
            public boolean fetch(XSLFShape shape) {
                PackagePart pp = shape.getSheet().getPackagePart();
                if (shape instanceof XSLFPictureShape) {
                    CTPicture pic = (CTPicture)shape.getXmlObject();
                    if (pic.getBlipFill() != null) {
                        setValue(selectPaint(pic.getBlipFill(), pp, null, theme));
                        return true;
                    }
                }

                XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(shape.getShapeProperties());
                if (fp == null) {
                    return false;
                }

                if (fp.isSetNoFill()) {
                    setValue(null);
                    return true;
                }

                PaintStyle paint = selectPaint(fp, null, pp, theme, hasPlaceholder);
                if (paint != null) {
                    setValue(paint);
                    return true;
                }

                CTShapeStyle style = shape.getSpStyle();
                if (style != null) {
                    fp = XSLFPropertiesDelegate.getFillDelegate(style.getFillRef());
                    paint = selectPaint(fp, null, pp, theme, hasPlaceholder);
                }
                if (paint != null) {
                    setValue(paint);
                    return true;
                }


                return false;
            }
        };
        fetchShapeProperty(fetcher);

        return fetcher.getValue();
    }

    @SuppressWarnings("unused")
    protected CTBackgroundProperties getBgPr() {
        return getChild(CTBackgroundProperties.class, PML_NS, "bgPr");
    }

    @SuppressWarnings("unused")
    protected CTStyleMatrixReference getBgRef() {
        return getChild(CTStyleMatrixReference.class, PML_NS, "bgRef");
    }

    protected CTGroupShapeProperties getGrpSpPr() {
        return getChild(CTGroupShapeProperties.class, PML_NS, "grpSpPr");
    }

    protected CTNonVisualDrawingProps getCNvPr() {
        try {
            if (_nvPr == null) {
                _nvPr = XPathHelper.selectProperty(getXmlObject(), CTNonVisualDrawingProps.class, null, NV_CONTAINER, CNV_PROPS);
            }
            return _nvPr;
        } catch (XmlException e) {
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected CTShapeStyle getSpStyle() {
        if (_spStyle == null) {
            _spStyle = getChild(CTShapeStyle.class, PML_NS, "style");
        }
        return _spStyle;
    }

    /**
     * Return direct child objects of this shape
     *
     * @param childClass the class to cast the properties to
     * @param namespace the namespace - usually it is {@code "http://schemas.openxmlformats.org/presentationml/2006/main"}
     * @param nodename the node name, without prefix
     * @return the properties object or null if it can't be found
     */
    @SuppressWarnings({"unchecked", "WeakerAccess", "unused", "SameParameterValue"})
    protected <T extends XmlObject> T getChild(Class<T> childClass, String namespace, String nodename) {
        T child = null;
        try (XmlCursor cur = getXmlObject().newCursor()) {
            if (cur.toChild(namespace, nodename)) {
                child = (T)cur.getObject();
            }
            if (cur.toChild(XSLFRelation.NS_DRAWINGML, nodename)) {
                child = (T)cur.getObject();
            }
        }
        return child;
    }

    public boolean isPlaceholder() {
        return getPlaceholderDetails().getCTPlaceholder(false) != null;
    }

    /**
     * @see PlaceholderDetails#getPlaceholder()
     */
    public Placeholder getPlaceholder() {
        return getPlaceholderDetails().getPlaceholder();
    }

    /**
     * @see PlaceholderDetails#setPlaceholder(Placeholder)
     */
    public void setPlaceholder(final Placeholder placeholder) {
        getPlaceholderDetails().setPlaceholder(placeholder);
    }

    /**
     * @see SimpleShape#getPlaceholderDetails()
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFPlaceholderDetails getPlaceholderDetails() {
        return new XSLFPlaceholderDetails(this);
    }

    /**
     * As there's no xmlbeans hierarchy, but XSLF works with subclassing, not all
     * child classes work with a {@link CTShape} object, but often contain the same
     * properties. This method is the generalized form of selecting and casting those
     * properties.
     *
     * @param resultClass the requested result class
     * @param xquery the simple (xmlbean) xpath expression to the property
     * @return the xml object at the xpath location, or null if not found
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    protected <T extends XmlObject> T selectProperty(Class<T> resultClass, String xquery) {
        XmlObject[] rs = getXmlObject().selectPath(xquery);
        if (rs.length == 0) {
            return null;
        }
        return (resultClass.isInstance(rs[0])) ? (T)rs[0] : null;
    }

    /**
     * Walk up the inheritance tree and fetch shape properties.<p>
     *
     * The following order of inheritance is assumed:
     * <ol>
     * <li>slide
     * <li>slideLayout
     * <li>slideMaster
     * </ol>
     *
     * Currently themes and their defaults aren't correctly handled
     *
     * @param visitor the object that collects the desired property
     * @return true if the property was fetched
     */
    @SuppressWarnings("WeakerAccess")
    @Internal
    public boolean fetchShapeProperty(PropertyFetcher<?> visitor) {
        // try shape properties in slide
        if (visitor.fetch(this)) {
            return true;
        }

        final CTPlaceholder ph = getPlaceholderDetails().getCTPlaceholder(false);
        if (ph == null) {
            return false;
        }
        MasterSheet<XSLFShape,XSLFTextParagraph> sm = getSheet().getMasterSheet();

        // try slide layout
        if (sm instanceof XSLFSlideLayout) {
            XSLFSlideLayout slideLayout = (XSLFSlideLayout)sm;
            XSLFSimpleShape placeholderShape = slideLayout.getPlaceholder(ph);
            if (placeholderShape != null && visitor.fetch(placeholderShape)) {
                return true;
            }
            sm = slideLayout.getMasterSheet();
        }

        // try slide master
        if (sm instanceof XSLFSlideMaster) {
            XSLFSlideMaster master = (XSLFSlideMaster)sm;
            int textType = getPlaceholderType(ph);
            XSLFSimpleShape masterShape = master.getPlaceholderByType(textType);
            return masterShape != null && visitor.fetch(masterShape);
        }

        return false;
    }

    private static int getPlaceholderType(CTPlaceholder ph) {
        if ( !ph.isSetType()) {
            return STPlaceholderType.INT_BODY;
        }

        switch (ph.getType().intValue()) {
            case STPlaceholderType.INT_TITLE:
            case STPlaceholderType.INT_CTR_TITLE:
                return STPlaceholderType.INT_TITLE;
            case STPlaceholderType.INT_FTR:
            case STPlaceholderType.INT_SLD_NUM:
            case STPlaceholderType.INT_DT:
                return ph.getType().intValue();
            default:
                return STPlaceholderType.INT_BODY;
        }
    }

    /**
     * Convert shape fill into java.awt.Paint. The result is either Color or
     * TexturePaint or GradientPaint or null
     *
     * @param fp          a properties handler specific to the underlying shape properties
     * @param phClr       context color
     * @param parentPart  the parent package part. Any external references (images, etc.) are resolved relative to it.
     * @param theme       the theme for the shape/sheet
     *
     * @return  the applied Paint or null if none was applied
     */
    @SuppressWarnings("WeakerAccess")
    protected PaintStyle selectPaint(XSLFFillProperties fp, final CTSchemeColor phClr, final PackagePart parentPart, final XSLFTheme theme, boolean hasPlaceholder) {
        if (fp == null || fp.isSetNoFill()) {
            return null;
        } else if (fp.isSetSolidFill()) {
            return selectPaint(fp.getSolidFill(), phClr, theme);
        } else if (fp.isSetBlipFill()) {
            return selectPaint(fp.getBlipFill(), parentPart, phClr, theme);
        } else if (fp.isSetGradFill()) {
            return selectPaint(fp.getGradFill(), phClr, theme);
        } else if (fp.isSetMatrixStyle()) {
            return selectPaint(fp.getMatrixStyle(), theme, fp.isLineStyle(), hasPlaceholder);
        } else if (phClr != null) {
            return selectPaint(phClr, theme);
        } else {
            return null;
        }
    }

    protected PaintStyle selectPaint(CTSchemeColor phClr, final XSLFTheme theme) {
        final XSLFColor c = new XSLFColor(null, theme, phClr, _sheet);
        return DrawPaint.createSolidPaint(c.getColorStyle());
    }

    @SuppressWarnings("WeakerAccess")
    protected PaintStyle selectPaint(CTSolidColorFillProperties solidFill, CTSchemeColor phClr, final XSLFTheme theme) {
        CTSchemeColor nestedPhClr = solidFill.getSchemeClr();
        boolean useNested = nestedPhClr != null && nestedPhClr.getVal() != null && !STSchemeColorVal.PH_CLR.equals(nestedPhClr.getVal());
        final XSLFColor c = new XSLFColor(solidFill, theme, useNested ? nestedPhClr : phClr, _sheet);
        return DrawPaint.createSolidPaint(c.getColorStyle());
    }

    @SuppressWarnings("WeakerAccess")
    protected PaintStyle selectPaint(final CTBlipFillProperties blipFill, final PackagePart parentPart, CTSchemeColor phClr, final XSLFTheme theme) {
        return new XSLFTexturePaint(this, blipFill, parentPart, phClr, theme, _sheet);
    }

    @SuppressWarnings("WeakerAccess")
    protected PaintStyle selectPaint(final CTGradientFillProperties gradFill, CTSchemeColor phClr, final XSLFTheme theme) {
        return new XSLFGradientPaint(gradFill, phClr, theme, _sheet);
    }

    @SuppressWarnings("WeakerAccess")
    protected PaintStyle selectPaint(CTStyleMatrixReference fillRef, final XSLFTheme theme, boolean isLineStyle, boolean hasPlaceholder) {
        if (fillRef == null) {
            return null;
        }

        // The idx attribute refers to the index of a fill style or
        // background fill style within the presentation's style matrix, defined by the fmtScheme element.
        // value of 0 or 1000 indicates no background,
        // values 1-999 refer to the index of a fill style within the fillStyleLst element
        // values 1001 and above refer to the index of a background fill style within the bgFillStyleLst element.
        long idx = fillRef.getIdx();
        CTStyleMatrix matrix = theme.getXmlObject().getThemeElements().getFmtScheme();
        final XmlObject styleLst;
        long childIdx;
        if (idx >= 1 && idx <= 999) {
            childIdx = idx-1;
            styleLst = (isLineStyle) ? matrix.getLnStyleLst() : matrix.getFillStyleLst();
        } else if (idx >= 1001 ){
            childIdx = idx - 1001;
            styleLst = matrix.getBgFillStyleLst();
        } else {
            return null;
        }
        XSLFFillProperties fp = null;
        try (XmlCursor cur = styleLst.newCursor()) {
            if (cur.toChild(Math.toIntExact(childIdx))) {
                fp = XSLFPropertiesDelegate.getFillDelegate(cur.getObject());
            }
        }

        CTSchemeColor phClr = fillRef.getSchemeClr();
        PaintStyle res =  selectPaint(fp, phClr, theme.getPackagePart(), theme, hasPlaceholder);
        // check for empty placeholder value
        // see http://officeopenxml.com/prSlide-color.php - "Color Placeholders within Themes"
        if (res != null || hasPlaceholder) {
            return res;
        }
        XSLFColor col = new XSLFColor(fillRef, theme, phClr, _sheet);
        return DrawPaint.createSolidPaint(col.getColorStyle());
    }

    @Override
    public void draw(Graphics2D graphics, Rectangle2D bounds) {
        DrawFactory.getInstance(graphics).drawShape(graphics, this, bounds);
    }

    /**
     * Return the shape specific (visual) properties
     *
     * @return the shape specific properties
     */
    protected XmlObject getShapeProperties() {
        return getChild(CTShapeProperties.class, PML_NS, "spPr");
    }
}
