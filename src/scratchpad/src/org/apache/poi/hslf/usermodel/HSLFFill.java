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
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint;
import org.apache.poi.sl.usermodel.PaintStyle.GradientPaint.GradientType;
import org.apache.poi.sl.usermodel.PaintStyle.TexturePaint;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Units;

/**
 * Represents functionality provided by the 'Fill Effects' dialog in PowerPoint.
 */
public final class HSLFFill {
    // For logging
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    /**
     *  Fill with a solid color
     */
    public static final int FILL_SOLID = 0;

    /**
     *  Fill with a pattern (bitmap)
     */
    public static final int FILL_PATTERN = 1;

    /**
     *  A texture (pattern with its own color map)
     */
    public static final int FILL_TEXTURE = 2;

    /**
     *  Center a picture in the shape
     */
    public static final int FILL_PICTURE = 3;

    /**
     *  Shade from start to end points
     */
    public static final int FILL_SHADE = 4;

    /**
     *  Shade from bounding rectangle to end point
     */
    public static final int FILL_SHADE_CENTER = 5;

    /**
     *  Shade from shape outline to end point
     */
    public static final int FILL_SHADE_SHAPE = 6;

    /**
     *  Similar to FILL_SHADE, but the fill angle
     *  is additionally scaled by the aspect ratio of
     *  the shape. If shape is square, it is the same as FILL_SHADE
     */
    public static final int FILL_SHADE_SCALE = 7;

    /**
     *  shade to title
     */
    public static final int FILL_SHADE_TITLE = 8;

    /**
     *  Use the background fill color/pattern
     */
    public static final int FILL_BACKGROUND = 9;



    /**
     * The shape this background applies to
     */
    protected HSLFShape shape;

    /**
     * Construct a <code>Fill</code> object for a shape.
     * Fill information will be read from shape's escher properties.
     *
     * @param shape the shape this background applies to
     */
    public HSLFFill(HSLFShape shape){
        this.shape = shape;
    }


    public FillStyle getFillStyle() {
        return new FillStyle() {
            public PaintStyle getPaint() {
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
                        logger.log(POILogger.WARN, "unsuported fill type: " + fillType);
                        return null;
                }
            }
        };
    }
    
    

    private GradientPaint getGradientPaint(final GradientType gradientType) {
        final AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        final EscherArrayProperty ep = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__SHADECOLORS);
        final int colorCnt = (ep == null) ? 0 : ep.getNumberOfElementsInArray();

        return new GradientPaint() {
            public double getGradientAngle() {
                // A value of type FixedPoint, as specified in [MS-OSHARED] section 2.2.1.6,
                // that specifies the angle of the gradient fill. Zero degrees represents a vertical vector from
                // bottom to top. The default value for this property is 0x00000000.
                int rot = shape.getEscherProperty(EscherProperties.FILL__ANGLE);
                return 90-Units.fixedPointToDouble(rot);
            }
            public ColorStyle[] getGradientColors() {
                ColorStyle cs[];
                if (colorCnt == 0) {
                    cs = new ColorStyle[2];
                    cs[0] = wrapColor(getBackgroundColor());
                    cs[1] = wrapColor(getForegroundColor());
                } else {
                    cs = new ColorStyle[colorCnt];
                    int idx = 0;
                    // TODO: handle palette colors and alpha(?) value 
                    for (byte data[] : ep) {
                        EscherColorRef ecr = new EscherColorRef(data, 0, 4);
                        cs[idx++] = wrapColor(shape.getColor(ecr));
                    }
                }
                return cs;
            }
            private ColorStyle wrapColor(Color col) {
                return (col == null) ? null : DrawPaint.createSolidPaint(col).getSolidColor();
            }
            public float[] getGradientFractions() {
                float frc[];
                if (colorCnt == 0) {
                    frc = new float[]{0, 1};
                } else {
                    frc = new float[colorCnt];
                    int idx = 0;
                    for (byte data[] : ep) {
                        double pos = Units.fixedPointToDouble(LittleEndian.getInt(data, 4));
                        frc[idx++] = (float)pos;
                    }
                }
                return frc;
            }
            public boolean isRotatedWithShape() {
                return false;
            }
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
            public InputStream getImageData() {
                return new ByteArrayInputStream(pd.getData());
            }

            public String getContentType() {
                return pd.getContentType();
            }

            public int getAlpha() {
                return (int)(shape.getAlpha(EscherProperties.FILL__FILLOPACITY)*100000.0);
            }
        };
    }

    /**
     * Returns fill type.
     * Must be one of the <code>FILL_*</code> constants defined in this class.
     *
     * @return type of fill
     */
    public int getFillType(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty prop = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__FILLTYPE);
        return prop == null ? FILL_SOLID : prop.getPropertyValue();
    }

    /**
     */
    protected void afterInsert(HSLFSheet sh){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__PATTERNTEXTURE);
        if(p != null) {
            int idx = p.getPropertyValue();
            EscherBSERecord bse = getEscherBSERecord(idx);
            bse.setRef(bse.getRef() + 1);
        }
    }

    @SuppressWarnings("resource")
    protected EscherBSERecord getEscherBSERecord(int idx){
        HSLFSheet sheet = shape.getSheet();
        if(sheet == null) {
            logger.log(POILogger.DEBUG, "Fill has not yet been assigned to a sheet");
            return null;
        }
        HSLFSlideShow ppt = sheet.getSlideShow();
        Document doc = ppt.getDocumentRecord();
        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if(bstore == null) {
            logger.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
            return null;
        }
        List<EscherRecord> lst = bstore.getChildRecords();
        return (EscherBSERecord)lst.get(idx-1);
    }

    /**
     * Sets fill type.
     * Must be one of the <code>FILL_*</code> constants defined in this class.
     *
     * @param type type of the fill
     */
    public void setFillType(int type){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        HSLFShape.setEscherProperty(opt, EscherProperties.FILL__FILLTYPE, type);
    }

    /**
     * Foreground color
     */
    public Color getForegroundColor(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST);

        if(p != null && (p.getPropertyValue() & 0x10) == 0) return null;

        return shape.getColor(EscherProperties.FILL__FILLCOLOR, EscherProperties.FILL__FILLOPACITY, -1);

    }

    /**
     * Foreground color
     */
    public void setForegroundColor(Color color){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        if (color == null) {
            HSLFShape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150000);
        }
        else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            HSLFShape.setEscherProperty(opt, EscherProperties.FILL__FILLCOLOR, rgb);
            HSLFShape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150011);
        }
    }

    /**
     * Background color
     */
    public Color getBackgroundColor(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST);

        if(p != null && (p.getPropertyValue() & 0x10) == 0) return null;

        return shape.getColor(EscherProperties.FILL__FILLBACKCOLOR, EscherProperties.FILL__FILLOPACITY, -1);
    }

    /**
     * Background color
     */
    public void setBackgroundColor(Color color){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        if (color == null) {
            HSLFShape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, -1);
        }
        else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            HSLFShape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, rgb);
        }
    }

    /**
     * <code>PictureData</code> object used in a texture, pattern of picture fill.
     */
    @SuppressWarnings("resource")
    public HSLFPictureData getPictureData(){
        AbstractEscherOptRecord opt = shape.getEscherOptRecord();
        EscherSimpleProperty p = HSLFShape.getEscherProperty(opt, EscherProperties.FILL__PATTERNTEXTURE);
        if (p == null) return null;

        HSLFSlideShow ppt = shape.getSheet().getSlideShow();
        List<HSLFPictureData> pict = ppt.getPictureData();
        Document doc = ppt.getDocumentRecord();

        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);

        java.util.List<EscherRecord> lst = bstore.getChildRecords();
        int idx = p.getPropertyValue();
        if (idx == 0){
            logger.log(POILogger.WARN, "no reference to picture data found ");
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
        HSLFShape.setEscherProperty(opt, (short)(EscherProperties.FILL__PATTERNTEXTURE + 0x4000), (data == null ? 0 : data.getIndex()));
        if(data != null && shape.getSheet() != null) {
            EscherBSERecord bse = getEscherBSERecord(data.getIndex());
            bse.setRef(bse.getRef() + 1);
        }
    }

}
