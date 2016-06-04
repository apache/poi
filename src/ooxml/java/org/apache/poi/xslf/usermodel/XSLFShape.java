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
import java.util.Comparator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.sl.usermodel.PlaceableShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrix;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathShadeType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * Base super-class class for all shapes in PresentationML
 */
@Beta
public abstract class XSLFShape implements Shape<XSLFShape,XSLFTextParagraph> {
    private final XmlObject _shape;
    private final XSLFSheet _sheet;
    private XSLFShapeContainer _parent;

    private CTShapeProperties _spPr;
    private CTShapeStyle _spStyle;
    private CTNonVisualDrawingProps _nvPr;
    private CTPlaceholder _ph;

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
    
    /**
     * @return human-readable name of this shape, e.g. "Rectange 3"
     */
    public String getShapeName(){
        return getCNvPr().getName();
    }

    /**
     * Returns a unique identifier for this shape within the current document.
     * This ID may be used to assist in uniquely identifying this object so that it can
     * be referred to by other parts of the document.
     * <p>
     * If multiple objects within the same document share the same id attribute value,
     * then the document shall be considered non-conformant.
     * </p>
     *
     * @return unique id of this shape
     */
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
            ps.setAnchor(((PlaceableShape<?,?>)sh).getAnchor());
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
        PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
            public boolean fetch(XSLFShape shape) {
                XmlObject pr = null;
                try {
                    pr = shape.getSpPr();
                    if (((CTShapeProperties)pr).isSetNoFill()) {
                        setValue(null);
                        return true;
                    }                    
                } catch (IllegalStateException e) {}
                // trying background properties now
                if (pr == null) {
                    pr = shape.getBgPr();
                }
                if (pr == null) {
                    pr = shape.getGrpSpPr();
                }
                if (pr == null) {
                    if (shape.getXmlObject() instanceof CTBackground) {
                        pr = shape.getXmlObject();
                    }
                }
                
                if (pr == null) return false;
                
                PaintStyle paint = null;
                PackagePart pp = getSheet().getPackagePart();
                for (XmlObject obj : pr.selectPath("*")) {
                    paint = selectPaint(obj, null, pp, theme);
                    if (paint != null) {
                        setValue(paint);
                        return true;
                    }
                }
                
                return false;
            }
        };
        fetchShapeProperty(fetcher);

        PaintStyle paint = fetcher.getValue();
        if (paint != null) return paint;
        
        // fill color was not found, check if it is defined in the theme
        // get a reference to a fill style within the style matrix.
        CTStyleMatrixReference fillRef = null;
        if (fillRef == null) {
            CTShapeStyle style = getSpStyle();
            if (style != null) fillRef = style.getFillRef();
        }
        if (fillRef == null) {
            fillRef = getBgRef();
        }
        paint = selectPaint(fillRef, theme);

        return paint;
    }

    protected CTBackgroundProperties getBgPr() {
        String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' p:bgPr";
        return selectProperty(CTBackgroundProperties.class, xquery);
    }
    
    protected CTStyleMatrixReference getBgRef() {
        String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' p:bgRef";
        return selectProperty(CTStyleMatrixReference.class, xquery);
    }
    
    protected CTGroupShapeProperties getGrpSpPr() {
        String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' p:grpSpPr";
        return selectProperty(CTGroupShapeProperties.class, xquery);
    }
    
    protected CTNonVisualDrawingProps getCNvPr() {
        if (_nvPr == null) {
            String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:cNvPr";
            _nvPr = selectProperty(CTNonVisualDrawingProps.class, xquery);
        }
        return _nvPr;
    }

    protected CTShapeProperties getSpPr() {
        if (_spPr == null) {
            String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' p:spPr";
            _spPr = selectProperty(CTShapeProperties.class, xquery);
        }
        if (_spPr == null) {
            throw new IllegalStateException("CTShapeProperties was not found.");
        }
        return _spPr;
    }

    protected CTShapeStyle getSpStyle() {
        if (_spStyle == null) {
            String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' p:style";
            _spStyle = selectProperty(CTShapeStyle.class, xquery);
        }
        return _spStyle;
    }

    protected CTPlaceholder getCTPlaceholder() {
        if (_ph == null) {
            String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:nvPr/p:ph";
            _ph = selectProperty(CTPlaceholder.class, xquery);
        }
        return _ph;
    }

    public Placeholder getPlaceholder() {
        CTPlaceholder ph = getCTPlaceholder();
        if (ph == null || !(ph.isSetType() || ph.isSetIdx())) {
            return null;
        }
        return Placeholder.lookupOoxml(ph.getType().intValue());
    }
    
    /**
     * Specifies that the corresponding shape should be represented by the generating application
     * as a placeholder. When a shape is considered a placeholder by the generating application
     * it can have special properties to alert the user that they may enter content into the shape.
     * Different types of placeholders are allowed and can be specified by using the placeholder
     * type attribute for this element
     *
     * @param placeholder
     */
    protected void setPlaceholder(Placeholder placeholder) {
        String xquery = "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:nvPr";
        CTApplicationNonVisualDrawingProps nv = selectProperty(CTApplicationNonVisualDrawingProps.class, xquery);
        if (nv == null) return;
        if(placeholder == null) {
            if (nv.isSetPh()) nv.unsetPh();
            _ph = null;
        } else {
            nv.addNewPh().setType(STPlaceholderType.Enum.forInt(placeholder.ooxmlId));
        }
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
    @SuppressWarnings("unchecked")
    protected <T extends XmlObject> T selectProperty(Class<T> resultClass, String xquery) {
        XmlObject[] rs = getXmlObject().selectPath(xquery);
        if (rs.length == 0) return null;
        return (resultClass.isInstance(rs[0])) ? (T)rs[0] : null;
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
    protected boolean fetchShapeProperty(PropertyFetcher<?> visitor) {
        boolean ok = visitor.fetch(this);

        XSLFSimpleShape masterShape;
        XSLFSheet masterSheet = (XSLFSheet)getSheet().getMasterSheet();
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
                XSLFSheet master = (XSLFSheet)masterSheet.getMasterSheet();
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

    protected PaintStyle getPaint(XmlObject spPr, CTSchemeColor phClr) {
        PaintStyle paint = null;
        XSLFSheet sheet = getSheet(); 
        PackagePart pp = sheet.getPackagePart();
        XSLFTheme theme = sheet.getTheme();
        for (XmlObject obj : spPr.selectPath("*")) {
            paint = selectPaint(obj, phClr, pp, theme);
            if(paint != null) break;
        }
        return paint;
    }
    
    /**
     * Convert shape fill into java.awt.Paint. The result is either Color or
     * TexturePaint or GradientPaint or null
     *
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
    protected static PaintStyle selectPaint(XmlObject obj, final CTSchemeColor phClr, final PackagePart parentPart, final XSLFTheme theme) {
        if (obj instanceof CTNoFillProperties) {
            return null;
        } else if (obj instanceof CTSolidColorFillProperties) {
            return selectPaint((CTSolidColorFillProperties)obj, phClr, theme);
        } else if (obj instanceof CTBlipFillProperties) {
            return selectPaint((CTBlipFillProperties)obj, parentPart);
        } else if (obj instanceof CTGradientFillProperties) {
            return selectPaint((CTGradientFillProperties) obj, phClr, theme);
        } else if (obj instanceof CTStyleMatrixReference) {
            return selectPaint((CTStyleMatrixReference)obj, theme);
        } else {
            return null;
        }
    }

    protected static PaintStyle selectPaint(CTSolidColorFillProperties solidFill, CTSchemeColor phClr, final XSLFTheme theme) {
        if (phClr == null && solidFill.isSetSchemeClr()) {
            phClr = solidFill.getSchemeClr();
        }
        final XSLFColor c = new XSLFColor(solidFill, theme, phClr);
        return DrawPaint.createSolidPaint(c.getColorStyle());
    }
    
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
    
    protected static PaintStyle selectPaint(final CTGradientFillProperties gradFill, CTSchemeColor phClr, final XSLFTheme theme) {

        final CTGradientStop[] gs = gradFill.getGsLst().getGsArray();

        Arrays.sort(gs, new Comparator<CTGradientStop>() {
            public int compare(CTGradientStop o1, CTGradientStop o2) {
                Integer pos1 = o1.getPos();
                Integer pos2 = o2.getPos();
                return pos1.compareTo(pos2);
            }
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
                // TODO: is this correct???
                return (gradFill.isSetRotWithShape() || !gradFill.getRotWithShape());
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
    
    protected static PaintStyle selectPaint(CTStyleMatrixReference fillRef, final XSLFTheme theme) {
        if (fillRef == null) return null;
        
        // The idx attribute refers to the index of a fill style or
        // background fill style within the presentation's style matrix, defined by the fmtScheme element.
        // value of 0 or 1000 indicates no background,
        // values 1-999 refer to the index of a fill style within the fillStyleLst element
        // values 1001 and above refer to the index of a background fill style within the bgFillStyleLst element.
        int idx = (int)fillRef.getIdx();
        CTSchemeColor phClr = fillRef.getSchemeClr();
        XmlObject fillProps = null;
        CTStyleMatrix matrix = theme.getXmlObject().getThemeElements().getFmtScheme();
        if (idx >= 1 && idx <= 999) {
            fillProps = matrix.getFillStyleLst().selectPath("*")[idx - 1];
        } else if (idx >= 1001 ){
            fillProps = matrix.getBgFillStyleLst().selectPath("*")[idx - 1001];
        }
        return (fillProps == null) ? null : selectPaint(fillProps, phClr, theme.getPackagePart(), theme);
    }
    
    @Override
    public void draw(Graphics2D graphics, Rectangle2D bounds) {
        DrawFactory.getInstance(graphics).drawShape(graphics, this, bounds);
    }
}