/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hslf.usermodel;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint.GradientType;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * Represents functionality provided by the 'Fill Effects' dialog in PowerPoint.
 */
@SuppressWarnings("WeakerAccess")
public final class HSLFFill {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFFill.class);

    /**
     *  Fill with a solid color
     */
    static final int FILL_SOLID = 0;

    /**
     *  Fill with a pattern (bitmap)
     */
    static final int FILL_PATTERN = 1;

    /**
     *  A texture (pattern with its own color map)
     */
    static final int FILL_TEXTURE = 2;

    /**
     *  Center a picture in the shape
     */
    static final int FILL_PICTURE = 3;

    /**
     *  Shade from start to end points
     */
    static final int FILL_SHADE = 4;

    /**
     *  Shade from bounding rectangle to end point
     */
    static final int FILL_SHADE_CENTER = 5;

    /**
     *  Shade from shape outline to end point
     */
    static final int FILL_SHADE_SHAPE = 6;

    /**
     *  Similar to FILL_SHADE, but the fill angle
     *  is additionally scaled by the aspect ratio of
     *  the shape. If shape is square, it is the same as FILL_SHADE
     */
    static final int FILL_SHADE_SCALE = 7;

    /**
     *  shade to title
     */
    static final int FILL_SHADE_TITLE = 8;

    /**
     *  Use the background fill color/pattern
     */
    static final int FILL_BACKGROUND = 9;

    /**
     * A bit that specifies whether the RecolorFillAsPicture bit is set.
     * A value of 0x0 specifies that the fRecolorFillAsPicture MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_RECOLOR_FILL_AS_PICTURE = BitFieldFactory.getInstance(0x00400000);

    /**
     * A bit that specifies whether the UseShapeAnchor bit is set.
     * A value of 0x0 specifies that the fUseShapeAnchor MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_USE_SHAPE_ANCHOR = BitFieldFactory.getInstance(0x00200000);

    /**
     * A bit that specifies whether the Filled bit is set.
     * A value of 0x0 specifies that the Filled MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_FILLED = BitFieldFactory.getInstance(0x00100000);

    /**
     * A bit that specifies whether the HitTestFill bit is set.
     * A value of 0x0 specifies that the HitTestFill MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_HIT_TEST_FILL = BitFieldFactory.getInstance(0x00080000);
    
    /**
     * A bit that specifies whether the fillShape bit is set.
     * A value of 0x0 specifies that the fillShape MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_FILL_SHAPE = BitFieldFactory.getInstance(0x00040000);
    
    /**
     * A bit that specifies whether the fillUseRect bit is set.
     * A value of 0x0 specifies that the fillUseRect MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_FILL_USE_RECT = BitFieldFactory.getInstance(0x00020000);
    
    /**
     * A bit that specifies whether the fNoFillHitTest bit is set.
     * A value of 0x0 specifies that the fNoFillHitTest MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_NO_FILL_HIT_TEST = BitFieldFactory.getInstance(0x00010000);
    
    /**
     * A bit that specifies how to recolor a picture fill. If this bit is set to 0x1, the pictureFillCrMod
     * property of the picture fill is used for recoloring. If this bit is set to 0x0, the fillCrMod property,
     * as defined in section 2.3.7.6, is used for recoloring.
     * If UsefRecolorFillAsPicture equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_RECOLOR_FILL_AS_PICTURE = BitFieldFactory.getInstance(0x00000040);
    
    /**
     * A bit that specifies whether the fill is rotated with the shape.
     * If UseUseShapeAnchor equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_USE_SHAPE_ANCHOR = BitFieldFactory.getInstance(0x00000020);
    
    /**
     * A bit that specifies whether the fill is rendered if the shape is a 2-D shape.
     * If this bit is set to 0x1, the fill of this shape is rendered based on the properties of the Fill Style
     * property set. If this bit is set to 0x0, the fill of this shape is not rendered.
     * If UseFilled is 0x0, this value MUST be ignored. The default value for this property is 0x1.
     */
    private static final BitField FILL_FILLED = BitFieldFactory.getInstance(0x00000010);
    
    /**
     * A bit that specifies whether this fill will be hit tested.
     * If UsefHitTestFill equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x1.
     */
    private static final BitField FILL_HIT_TEST_FILL = BitFieldFactory.getInstance(0x00000008);
    
    /**
     * A bit that specifies how the fill is aligned. If this bit is set to 0x1, the fill is
     * aligned relative to the shape so that it moves with the shape. If this bit is set to 0x0,
     * the fill is aligned with the origin of the view. If fUsefillShape equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x1.
     */
    private static final BitField FILL_FILL_SHAPE = BitFieldFactory.getInstance(0x00000004);
    
    /**
     * A bit that specifies whether to use the rectangle specified by the fillRectLeft, fillRectRight,
     * fillRectTop, and fillRectBottom properties, rather than the bounding rectangle of the shape,
     * as the filled area. If fUsefillUseRect equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_FILL_USE_RECT = BitFieldFactory.getInstance(0x00000002);
    
    /**
     * A bit that specifies whether this shape will be hit tested as though it were filled.
     * If UsefNoFillHitTest equals 0x0, this value MUST be ignored.
     * The default value for this property is 0x0.
     */
    private static final BitField FILL_NO_FILL_HIT_TEST = BitFieldFactory.getInstance(0x00000001);


    /**
     * The shape this background applies to
     */
    private HSLFShape shape;

    /**
     * Construct a {@code Fill} object for a shape.
     * Fill information will be read from shape's escher properties.
     *
     * @param shape the shape this background applies to
     */
    public HSLFFill(HSLFShape shape){
        this.shape = shape;
    }


    public FillStyle getFillStyle() {
        return this::getPaintStyle;
    }

    private PaintStyle getPaintStyle() {
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();

        EscherSimpleProperty hitProp = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (hitProp == null) ? 0 : hitProp.getPropertyValue();

        EscherSimpleProperty masterProp = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.SHAPE__MASTER);

        if (!FILL_USE_FILLED.isSet(propVal) && masterProp != null) {
            int masterId = masterProp.getPropertyValue();
            HSLFShape o = shape.getSheet().getMasterSheet().getShapes().stream().filter(s -> s.getShapeId() == masterId).findFirst().orElse(null);
            return o != null ? o.getFillStyle().getPaint() : null;
        }

        final int fillType = getFillType();
        // TODO: fix gradient types, this mismatches with the MS-ODRAW definition ...
        // need to handle (not only) the type (radial,rectangular,linear),
        // the direction, e.g. top right, and bounds (e.g. for rectangular boxes)
        switch (fillType) {
            case FILL_SOLID:
                return DrawPaint.createSolidPaint(getForegroundColor());
            case FILL_SHADE_SHAPE:
                return getGradientPaint(GradientType.shape);
            case FILL_SHADE_CENTER:
            case FILL_SHADE_TITLE:
                return getGradientPaint(GradientType.circular);
            case FILL_SHADE:
            case FILL_SHADE_SCALE:
                return getGradientPaint(GradientType.linear);
            case FILL_PICTURE:
                return getTexturePaint();
            default:
                LOG.log(POILogger.WARN, "unsuported fill type: ", fillType);
                return null;
        }
    }
    
    private boolean isRotatedWithShape() {
        // NOFILLHITTEST can be in the normal escher opt record but also in the tertiary record
        // the extended bit fields seem to be in the second
        AbstractEscherOptRecord opt = shape.getEscherChild(EscherRecordTypes.USER_DEFINED);
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (p == null) ? 0 : p.getPropertyValue();
        return FILL_USE_USE_SHAPE_ANCHOR.isSet(propVal) && FILL_USE_SHAPE_ANCHOR.isSet(propVal);
    }

    private GradientPaint getGradientPaint(final GradientType gradientType) {
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();

        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (p == null) ? 0 : p.getPropertyValue();

        if (FILL_USE_FILLED.isSet(propVal) && !FILL_FILLED.isSet(propVal)) {
            return null;
        }


        final EscherArrayProperty ep = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__SHADECOLORS);
        final int colorCnt = (ep == null) ? 0 : ep.getNumberOfElementsInArray();

        final List<Color> colors = new ArrayList<>();
        final List<Float> fractions = new ArrayList<>();

        // TODO: handle palette colors and alpha(?) value
        if (colorCnt == 0) {
            colors.add(getBackgroundColor());
            colors.add(getForegroundColor());
            fractions.add(0f);
            fractions.add(1f);
        } else {
            ep.forEach(data -> {
                EscherColorRef ecr = new EscherColorRef(data, 0, 4);
                colors.add(shape.getColor(ecr));
                double pos = Units.fixedPointToDouble(LittleEndian.getInt(data, 4));
                fractions.add((float)pos);
            });
        }

        int focus = getFillFocus();
        if (focus == 100 || focus == -100) {
            Collections.reverse(colors);
        } else if (focus != 0) {
            if (focus < 0) {
                focus = 100+focus;
            }
            // TODO: depending on fill focus, rotation with shape and other escher properties
            // there are still a lot of cases where we get the gradients wrong
            List<Color> reflectedColors = new ArrayList<>(colors.subList(1,colors.size()));
            Collections.reverse(reflectedColors);
            colors.addAll(0, reflectedColors);

            final List<Float> fractRev = new ArrayList<>();
            for (int i=fractions.size()-2; i >= 0; i--) {
                float val = (float)(1 - fractions.get(i) * focus / 100.);
                fractRev.add(val);
            }
            for (int i=0; i<fractions.size(); i++) {
                float val = (float)(fractions.get(i) * focus / 100.);
                fractions.set(i, val);
            }
            fractions.addAll(fractRev);
        }

        return new GradientPaint() {
            @Override
            public double getGradientAngle() {
                // A value of type FixedPoint, as specified in [MS-OSHARED] section 2.2.1.6,
                // that specifies the angle of the gradient fill. Zero degrees represents a vertical vector from
                // bottom to top. The default value for this property is 0x00000000.
                int rot = shape.getEscherProperty(EscherPropertyTypes.FILL__ANGLE);
                return 90-Units.fixedPointToDouble(rot);
            }

            @Override
            public ColorStyle[] getGradientColors() {
                return colors.stream().map(this::wrapColor).toArray(ColorStyle[]::new);
            }
            
            private ColorStyle wrapColor(Color col) {
                return (col == null) ? null : DrawPaint.createSolidPaint(col).getSolidColor();
            }
            
            @Override
            public float[] getGradientFractions() {
                float[] frc = new float[fractions.size()];
                for (int i = 0; i<fractions.size(); i++) {
                    frc[i] = fractions.get(i);
                }
                return frc;
            }
            
            @Override
            public boolean isRotatedWithShape() {
                return HSLFFill.this.isRotatedWithShape();
            }
            
            @Override
            public GradientType getGradientType() {
                return gradientType;
            }
        };
    }
    
    private TexturePaint getTexturePaint() {
        final HSLFPictureData pd = getPictureData();
        if (pd == null) {
            return null;
        }

        return new TexturePaint() {
            @Override
            public InputStream getImageData() {
                return new ByteArrayInputStream(pd.getData());
            }

            @Override
            public String getContentType() {
                return pd.getContentType();
            }

            @Override
            public int getAlpha() {
                return (int)(shape.getAlpha(EscherPropertyTypes.FILL__FILLOPACITY)*100000.0);
            }

            @Override
            public boolean isRotatedWithShape() {
                return HSLFFill.this.isRotatedWithShape();
            }
        };
    }

    /**
     * Returns fill type.
     * Must be one of the {@code FILL_*} constants defined in this class.
     *
     * @return type of fill
     */
    public int getFillType(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty prop = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__FILLTYPE);
        return prop == null ? FILL_SOLID : prop.getPropertyValue();
    }

    /**
     * The fillFocus property specifies the relative position of the last color in the shaded fill.
     * Its used to specify the center of an reflected fill. 0 = no reflection, 50 = reflected in the middle.
     * If fillFocus is less than 0, the relative position of the last color is outside the shape,
     * and the relative position of the first color is within the shape.
     *
     * @return a percentage in the range of -100 .. 100; defaults to 0
     */
    public int getFillFocus() {
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty prop = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__FOCUS);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    void afterInsert(HSLFSheet sh){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__PATTERNTEXTURE);
        if(p != null) {
            int idx = p.getPropertyValue();
            EscherBSERecord bse = getEscherBSERecord(idx);
            if (bse != null) {
                bse.setRef(bse.getRef() + 1);
            }
        }
    }

    @SuppressWarnings("resource")
    EscherBSERecord getEscherBSERecord(int idx){
        HSLFSheet sheet = shape.getSheet();
        if(sheet == null) {
            LOG.log(POILogger.DEBUG, "Fill has not yet been assigned to a sheet");
            return null;
        }
        HSLFSlideShow ppt = sheet.getSlideShow();
        Document doc = ppt.getDocumentRecord();
        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if(bstore == null) {
            LOG.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
            return null;
        }
        List<EscherRecord> lst = bstore.getChildRecords();
        return (EscherBSERecord)lst.get(idx-1);
    }

    /**
     * Sets fill type.
     * Must be one of the {@code FILL_*} constants defined in this class.
     *
     * @param type type of the fill
     */
    public void setFillType(int type){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__FILLTYPE, type);
    }

    /**
     * Foreground color
     */
    public Color getForegroundColor(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (p == null) ? 0 : p.getPropertyValue();

        return (!FILL_USE_FILLED.isSet(propVal) || (FILL_USE_FILLED.isSet(propVal) && FILL_FILLED.isSet(propVal)))
            ? shape.getColor(EscherPropertyTypes.FILL__FILLCOLOR, EscherPropertyTypes.FILL__FILLOPACITY)
            : null;
    }

    /**
     * Foreground color
     */
    public void setForegroundColor(Color color){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        opt.removeEscherProperty(EscherPropertyTypes.FILL__FILLOPACITY);
        opt.removeEscherProperty(EscherPropertyTypes.FILL__FILLCOLOR);

        if (color != null) {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__FILLCOLOR, rgb);
            int alpha = color.getAlpha();
            if (alpha < 255) {
                int alphaFP = Units.doubleToFixedPoint(alpha/255d);
                HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__FILLOPACITY, alphaFP);
            }
        }
        
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (p == null) ? 0 : p.getPropertyValue();
        propVal = FILL_FILLED.setBoolean(propVal, color != null);
        propVal = FILL_NO_FILL_HIT_TEST.setBoolean(propVal, color != null);
        propVal = FILL_USE_FILLED.set(propVal);
        propVal = FILL_USE_FILL_SHAPE.set(propVal);
        propVal = FILL_USE_NO_FILL_HIT_TEST.set(propVal);
        // TODO: check why we always clear this ...
        propVal = FILL_FILL_SHAPE.clear(propVal);

        HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST, propVal);
    }

    /**
     * Background color
     */
    public Color getBackgroundColor(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__NOFILLHITTEST);
        int propVal = (p == null) ? 0 : p.getPropertyValue();

        return (!FILL_USE_FILLED.isSet(propVal) || (FILL_USE_FILLED.isSet(propVal) && FILL_FILLED.isSet(propVal)))
            ? shape.getColor(EscherPropertyTypes.FILL__FILLBACKCOLOR, EscherPropertyTypes.FILL__FILLOPACITY)
            : null;
    }

    /**
     * Background color
     */
    public void setBackgroundColor(Color color){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        if (color == null) {
            HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__FILLBACKCOLOR, -1);
        }
        else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__FILLBACKCOLOR, rgb);
        }
    }

    /**
     * {@code PictureData} object used in a texture, pattern of picture fill.
     */
    @SuppressWarnings("resource")
    public HSLFPictureData getPictureData(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherPropertyTypes.FILL__PATTERNTEXTURE);
        if (p == null) {
            return null;
        }

        HSLFSlideShow ppt = shape.getSheet().getSlideShow();
        List<HSLFPictureData> pict = ppt.getPictureData();
        Document doc = ppt.getDocumentRecord();

        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);

        java.util.List<EscherRecord> lst = bstore.getChildRecords();
        int idx = p.getPropertyValue();
        if (idx == 0){
            LOG.log(POILogger.WARN, "no reference to picture data found ");
        } else {
            EscherBSERecord bse = (EscherBSERecord)lst.get(idx - 1);
            for (HSLFPictureData pd : pict) {
                if (pd.getOffset() ==  bse.getOffset()){
                    return pd;
                }
            }
        }

        return null;
    }

    /**
     * Assign picture used to fill the underlying shape.
     *
     * @param data the picture data added to this ppt by {@link HSLFSlideShow#addPicture(byte[], org.apache.poi.sl.usermodel.PictureData.PictureType)} method.
     */
    public void setPictureData(HSLFPictureData data){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        HSLFShape.setEscherProperty(opt, EscherPropertyTypes.FILL__PATTERNTEXTURE, true, (data == null ? 0 : data.getIndex()));
        if(data != null && shape.getSheet() != null) {
            EscherBSERecord bse = getEscherBSERecord(data.getIndex());
            if (bse != null) {
                bse.setRef(bse.getRef() + 1);
            }
        }
    }

}
