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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.apache.poi.sl.usermodel.*;
import org.apache.poi.util.*;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * Base super-class class for all shapes in PresentationML
 *
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFShape implements Shape {
    protected final XmlObject _shape;
    protected final XSLFSheet _sheet;
    protected XSLFShapeContainer _parent;

    private CTShapeProperties _spPr;
    private CTShapeStyle _spStyle;
    private CTNonVisualDrawingProps _nvPr;
    private CTPlaceholder _ph;

    private static final PaintStyle TRANSPARENT_PAINT = new SolidPaint() {
        public ColorStyle getSolidColor() {
            return new ColorStyle(){
                public Color getColor() { return DrawPaint.NO_PAINT; }
                public int getAlpha() { return -1; }
                public int getLumOff() { return -1; }
                public int getLumMod() { return -1; }
                public int getShade() { return -1; }
                public int getTint() { return -1; }
            };
        }
    };
    
    
    protected XSLFShape(XmlObject shape, XSLFSheet sheet) {
        _shape = shape;
        _sheet = sheet;
    }

    
    /**
     * @return the xml bean holding this shape's data
     */
    public XmlObject getXmlObject() {
        return _shape;
    }
    
    /**
     * @return human-readable name of this shape, e.g. "Rectange 3"
     */
    public abstract String getShapeName();

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
    public abstract int getShapeId();

    /**
     * Rotate this shape.
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @param theta the rotation angle in degrees.
     */
    public abstract void setRotation(double theta);

    /**
     * Rotation angle in degrees
     * <p>
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     * </p>
     *
     * @return rotation angle in degrees
     */
    public abstract double getRotation();

    /**
     * @param flip whether the shape is horizontally flipped
     */
    public abstract void setFlipHorizontal(boolean flip);

    /**
     * Whether the shape is vertically flipped
     *
     * @param flip whether the shape is vertically flipped
     */
    public abstract void setFlipVertical(boolean flip);

    /**
     * Whether the shape is horizontally flipped
     *
     * @return whether the shape is horizontally flipped
     */
    public abstract boolean getFlipHorizontal();

    /**
     * Whether the shape is vertically flipped
     *
     * @return whether the shape is vertically flipped
     */
    public abstract boolean getFlipVertical();

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

        setAnchor(sh.getAnchor());
    }
    
    public void setParent(XSLFShapeContainer parent) {
        this._parent = parent;
    }
    
    public XSLFShapeContainer getParent() {
        return this._parent;
    }
    
    public boolean isPlaceholder() {
        return false;
    }

    public StrokeStyle getStrokeStyle() {
        // TODO Auto-generated method stub
        return null;
    }

    public CustomGeometry getGeometry() {
        // TODO Auto-generated method stub
        return null;
    }

    public ShapeType getShapeType() {
        // TODO Auto-generated method stub
        return null;
    }

    public XSLFSheet getSheet() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * fetch shape fill as a java.awt.Paint
     *
     * @return either Color or GradientPaint or TexturePaint or null
     */
    @Override
    public FillStyle getFillStyle() {
        return new FillStyle() {
            public PaintStyle getPaint() {
                PropertyFetcher<PaintStyle> fetcher = new PropertyFetcher<PaintStyle>() {
                    public boolean fetch(XSLFShape shape) {
                        CTShapeProperties spPr = shape.getSpPr();
                        if (spPr.isSetNoFill()) {
                            setValue(TRANSPARENT_PAINT);
                            return true;
                        }
                        
                        PaintStyle paint = null;
                        for (XmlObject obj : spPr.selectPath("*")) {
                            paint = selectPaint(obj, null, getSheet().getPackagePart());
                            if (paint != null) break;
                        }
                        
                        if (paint == null) return false;
                        
                        setValue(paint);
                        return true;
                    }
                };
                fetchShapeProperty(fetcher);

                PaintStyle paint = fetcher.getValue();

                if (paint != null) return paint;
                
                // fill color was not found, check if it is defined in the theme
                CTShapeStyle style = getSpStyle();
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
                    XSLFSheet sheet = _sheet;
                    XSLFTheme theme = sheet.getTheme();
                    XmlObject fillProps = null;
                    CTStyleMatrix matrix = theme.getXmlObject().getThemeElements().getFmtScheme();
                    if(idx >= 1 && idx <= 999){
                        fillProps = matrix.getFillStyleLst().selectPath("*")[idx - 1];
                    } else if (idx >= 1001 ){
                        fillProps = matrix.getBgFillStyleLst().selectPath("*")[idx - 1001];
                    }
                    if(fillProps != null) {
                        paint = selectPaint(fillProps, phClr, sheet.getPackagePart());
                    }
                }
                return paint == RenderableShape.NO_PAINT ? null : paint;
            }
        };
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

    CTTransform2D getXfrm() {
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
    
    /**
     * @return the position of this shape within the drawing canvas.
     *         The coordinates are expressed in points
     */
    public Rectangle2D getAnchor() {
        CTTransform2D xfrm = getXfrm();
        if (xfrm == null) return null;

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

    /**
     * @param anchor the position of this shape within the drawing canvas.
     *               The coordinates are expressed in points
     */
    public void setAnchor(Rectangle2D anchor) {
        CTShapeProperties spPr = getSpPr();
        if (spPr == null) return;
        
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
    protected PaintStyle selectPaint(XmlObject obj, final CTSchemeColor phClr, final PackagePart parentPart) {
        final XSLFTheme theme = getSheet().getTheme();

        if (obj instanceof CTNoFillProperties) {
            return TRANSPARENT_PAINT;
        }
        
        if (obj instanceof CTSolidColorFillProperties) {
            CTSolidColorFillProperties solidFill = (CTSolidColorFillProperties) obj;
            final XSLFColor c = new XSLFColor(solidFill, theme, phClr);
            return new SolidPaint() {
                public ColorStyle getSolidColor() {
                    return c.getColorStyle();
                }
            };
        }
        
        if (obj instanceof CTBlipFillProperties) {
            CTBlipFillProperties blipFill = (CTBlipFillProperties)obj;
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
                        : 0;
                }
            };
        }
        
        if (obj instanceof CTGradientFillProperties) {
            final CTGradientFillProperties gradFill = (CTGradientFillProperties) obj;

            @SuppressWarnings("deprecation")
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
                cs[i] = new XSLFColor(cgs, theme, phClr).getColorStyle();
                fractions[i] = cgs.getPos() / 100000.f;
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
        
        return TRANSPARENT_PAINT;
    }

}