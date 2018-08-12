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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
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
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathShadeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * Base super-class class for all shapes in PresentationML
 */
@Beta
public abstract class XSLFShape implements Shape<XSLFShape,XSLFTextParagraph> {
    static final String PML_NS = "http://schemas.openxmlformats.org/presentationml/2006/main";
    
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
    
    public XSLFSheet getSheet() {
        return _sheet;
    }
    
    @Override
    public String getShapeName(){
        return getCNvPr().getName();
    }

    @Override
    public int getShapeId() {
        return (int)getCNvPr().getId();
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
            ps.setAnchor(sh.getAnchor());
        }
        
        
    }
    
    public void setParent(XSLFShapeContainer parent) {
        this._parent = parent;
    }
    
    public XSLFShapeContainer getParent() {
        return this._parent;
    }
    
    protected PaintStyle getFillPaint() {
        final XSLFTheme theme = getSheet().getTheme();
        final boolean hasPlaceholder = getPlaceholder() != null;
        PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
            public boolean fetch(XSLFShape shape) {
                XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(shape.getShapeProperties());
                if (fp == null) {
                    return false;
                }

                if (fp.isSetNoFill()) {
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
        if (_nvPr == null) {
            String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr";
            _nvPr = selectProperty(CTNonVisualDrawingProps.class, xquery);
        }
        return _nvPr;
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
        XmlCursor cur = getXmlObject().newCursor();
        T child = null;
        if (cur.toChild(namespace, nodename)) {
            child = (T)cur.getObject();
        }
        if (cur.toChild(XSLFRelation.NS_DRAWINGML, nodename)) {
            child = (T)cur.getObject();
        }
        cur.dispose();
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
        if (rs.length == 0) return null;
        return (resultClass.isInstance(rs[0])) ? (T)rs[0] : null;
    }

    /**
     * Walk up the inheritance tree and fetch shape properties.<p>
     *
     * The following order of inheritance is assumed:<p>
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
    protected boolean fetchShapeProperty(PropertyFetcher<?> visitor) {
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
    protected static PaintStyle selectPaint(XSLFFillProperties fp, final CTSchemeColor phClr, final PackagePart parentPart, final XSLFTheme theme, boolean hasPlaceholder) {
        if (fp == null || fp.isSetNoFill()) {
            return null;
        } else if (fp.isSetSolidFill()) {
            return selectPaint(fp.getSolidFill(), phClr, theme);
        } else if (fp.isSetBlipFill()) {
            return selectPaint(fp.getBlipFill(), parentPart);
        } else if (fp.isSetGradFill()) {
            return selectPaint(fp.getGradFill(), phClr, theme);
        } else if (fp.isSetMatrixStyle()) {
            return selectPaint(fp.getMatrixStyle(), theme, fp.isLineStyle(), hasPlaceholder);
        } else {
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected static PaintStyle selectPaint(CTSolidColorFillProperties solidFill, CTSchemeColor phClr, final XSLFTheme theme) {
        if (solidFill.isSetSchemeClr()) {
        	// if there's a reference to the placeholder color,
        	// stop evaluating further and let the caller select
        	// the next style inheritance level
//            if (STSchemeColorVal.PH_CLR.equals(solidFill.getSchemeClr().getVal())) {
//                return null;
//            }
            if (phClr == null) {
                phClr = solidFill.getSchemeClr();
            }
        }
        final XSLFColor c = new XSLFColor(solidFill, theme, phClr);
        return DrawPaint.createSolidPaint(c.getColorStyle());
    }

    @SuppressWarnings("WeakerAccess")
    protected static PaintStyle selectPaint(final CTBlipFillProperties blipFill, final PackagePart parentPart) {
        final CTBlip blip = blipFill.getBlip();
        return new TexturePaint() {
            private PackagePart getPart() {
                try {
                    String blipId = blip.getEmbed();
                    PackageRelationship rel = parentPart.getRelationship(blipId);
                    return parentPart.getRelatedPart(rel);
                } catch (InvalidFormatException e) {
                    throw new RuntimeException(e);
                }
            }
            
            public InputStream getImageData() {
                try {
                    return getPart().getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String getContentType() {
                /* TOOD: map content-type */
                return getPart().getContentType();
            }

            public int getAlpha() {
                return (blip.sizeOfAlphaModFixArray() > 0)
                    ? blip.getAlphaModFixArray(0).getAmt()
                    : 100000;
            }
        };        
    }

    @SuppressWarnings("WeakerAccess")
    protected static PaintStyle selectPaint(final CTGradientFillProperties gradFill, CTSchemeColor phClr, final XSLFTheme theme) {

        @SuppressWarnings("deprecation")
        final CTGradientStop[] gs = gradFill.getGsLst().getGsArray();

        Arrays.sort(gs, (o1, o2) -> {
            Integer pos1 = o1.getPos();
            Integer pos2 = o2.getPos();
            return pos1.compareTo(pos2);
        });

        final ColorStyle cs[] = new ColorStyle[gs.length];
        final float fractions[] = new float[gs.length];
        
        int i=0;
        for (CTGradientStop cgs : gs) {
            CTSchemeColor phClrCgs = phClr;
            if (phClrCgs == null && cgs.isSetSchemeClr()) {
                phClrCgs = cgs.getSchemeClr();
            }
            cs[i] = new XSLFColor(cgs, theme, phClrCgs).getColorStyle();
            fractions[i] = cgs.getPos() / 100000.f;
            i++;
        }
        
        return new GradientPaint() {

            public double getGradientAngle() {
                return (gradFill.isSetLin())
                    ? gradFill.getLin().getAng() / 60000.d
                    : 0;
            }

            public ColorStyle[] getGradientColors() {
                return cs;
            }

            public float[] getGradientFractions() {
                return fractions;
            }

            public boolean isRotatedWithShape() {
                return gradFill.getRotWithShape();
            }

            public GradientType getGradientType() {
                if (gradFill.isSetLin()) {
                    return GradientType.linear;
                }
                
                if (gradFill.isSetPath()) {
                    /* TODO: handle rect path */
                    STPathShadeType.Enum ps = gradFill.getPath().getPath();
                    if (ps == STPathShadeType.CIRCLE) {
                        return GradientType.circular;
                    } else if (ps == STPathShadeType.SHAPE) {
                        return GradientType.shape;
                    }
                }
                
                return GradientType.linear;
            }
        };        
    }
    
    @SuppressWarnings("WeakerAccess")
    protected static PaintStyle selectPaint(CTStyleMatrixReference fillRef, final XSLFTheme theme, boolean isLineStyle, boolean hasPlaceholder) {
        if (fillRef == null) return null;
        
        // The idx attribute refers to the index of a fill style or
        // background fill style within the presentation's style matrix, defined by the fmtScheme element.
        // value of 0 or 1000 indicates no background,
        // values 1-999 refer to the index of a fill style within the fillStyleLst element
        // values 1001 and above refer to the index of a background fill style within the bgFillStyleLst element.
        int idx = (int)fillRef.getIdx();
        CTStyleMatrix matrix = theme.getXmlObject().getThemeElements().getFmtScheme();
        final XmlObject styleLst;
        int childIdx;
        if (idx >= 1 && idx <= 999) {
            childIdx = idx-1;
            styleLst = (isLineStyle) ? matrix.getLnStyleLst() : matrix.getFillStyleLst();
        } else if (idx >= 1001 ){
            childIdx = idx - 1001;
            styleLst = matrix.getBgFillStyleLst();
        } else {
            return null;
        }
        XmlCursor cur = styleLst.newCursor();
        XSLFFillProperties fp = null;
        if (cur.toChild(childIdx)) {
            fp = XSLFPropertiesDelegate.getFillDelegate(cur.getObject());
        }
        cur.dispose();
            
        CTSchemeColor phClr = fillRef.getSchemeClr();
        PaintStyle res =  selectPaint(fp, phClr, theme.getPackagePart(), theme, hasPlaceholder);
        // check for empty placeholder value
        // see http://officeopenxml.com/prSlide-color.php - "Color Placeholders within Themes"
        if (res != null || hasPlaceholder) {
            return res;
        }
        XSLFColor col = new XSLFColor(fillRef, theme, phClr);
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