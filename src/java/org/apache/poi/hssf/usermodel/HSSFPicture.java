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

package org.apache.poi.hssf.usermodel;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.hssf.model.InternalWorkbook;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 */
public class HSSFPicture extends HSSFSimpleShape implements Picture {
	private static POILogger logger = POILogFactory.getLogger(HSSFPicture.class);
	
    public static final int PICTURE_TYPE_EMF = HSSFWorkbook.PICTURE_TYPE_EMF;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = HSSFWorkbook.PICTURE_TYPE_WMF;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = HSSFWorkbook.PICTURE_TYPE_PICT;              // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = HSSFWorkbook.PICTURE_TYPE_JPEG;              // JFIF
    public static final int PICTURE_TYPE_PNG = HSSFWorkbook.PICTURE_TYPE_PNG;                // PNG
    public static final int PICTURE_TYPE_DIB = HSSFWorkbook.PICTURE_TYPE_DIB;                // Windows DIB

    /**
     * width of 1px in columns with default width in units of 1/256 of a character width
     */
    private static final float PX_DEFAULT = 32.00f;
    /**
     * width of 1px in columns with overridden width in units of 1/256 of a character width
     */
    private static final float PX_MODIFIED = 36.56f;

    /**
     * Height of 1px of a row
     */
    private static final int PX_ROW = 15;

    public HSSFPicture(EscherContainerRecord spContainer, ObjRecord objRecord) {
        super(spContainer, objRecord);
    }

    /**
     * Constructs a picture object.
     */
    public HSSFPicture( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        super.setShapeType(OBJECT_TYPE_PICTURE);
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) getObjRecord().getSubRecords().get(0);
        cod.setObjectType(CommonObjectDataSubRecord.OBJECT_TYPE_PICTURE);
    }

    public int getPictureIndex()
    {
        EscherSimpleProperty property = getOptRecord().lookup(EscherProperties.BLIP__BLIPTODISPLAY);
        if (null == property){
            return -1;
        }
        return property.getPropertyValue();
    }

    public void setPictureIndex( int pictureIndex )
    {
        setPropertyValue(new EscherSimpleProperty( EscherProperties.BLIP__BLIPTODISPLAY, false, true, pictureIndex));
    }

    @Override
    protected EscherContainerRecord createSpContainer() {
        EscherContainerRecord spContainer = super.createSpContainer();
        EscherOptRecord opt = spContainer.getChildById(EscherOptRecord.RECORD_ID);
        opt.removeEscherProperty(EscherProperties.LINESTYLE__LINEDASHING);
        opt.removeEscherProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH);
        spContainer.removeChildRecord(spContainer.getChildById(EscherTextboxRecord.RECORD_ID));
        return spContainer;
    }

    /**
     * Resize the image
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * <code>resize(1.0)</code> sets the original size, <code>resize(0.5)</code> resize to 50% of the original,
     * <code>resize(2.0)</code> resizes to 200% of the original.
     */
    public void resize(double scale){
        HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();
        anchor.setAnchorType(2);

        HSSFClientAnchor pref = getPreferredSize(scale);

        int row2 = anchor.getRow1() + (pref.getRow2() - pref.getRow1());
        int col2 = anchor.getCol1() + (pref.getCol2() - pref.getCol1());

        anchor.setCol2((short)col2);
        anchor.setDx1(0);
        anchor.setDx2(pref.getDx2());

        anchor.setRow2(row2);
        anchor.setDy1(0);
        anchor.setDy2(pref.getDy2());
    }

    /**
     * Reset the image to the original size.
     * 
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     */
    public void resize(){
        resize(1.0);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    public HSSFClientAnchor getPreferredSize(){
        return getPreferredSize(1.0);
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    public HSSFClientAnchor getPreferredSize(double scale){
        HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();

        Dimension size = getImageDimension();
        double scaledWidth = size.getWidth() * scale;
        double scaledHeight = size.getHeight() * scale;

        float w = 0;

        //space in the leftmost cell
        w += getColumnWidthInPixels(anchor.getCol1())*(1 - (float)anchor.getDx1()/1024);
        short col2 = (short)(anchor.getCol1() + 1);
        int dx2 = 0;

        while(w < scaledWidth){
            w += getColumnWidthInPixels(col2++);
        }

        if(w > scaledWidth) {
            //calculate dx2, offset in the rightmost cell
            col2--;
            double cw = getColumnWidthInPixels(col2);
            double delta = w - scaledWidth;
            dx2 = (int)((cw-delta)/cw*1024);
        }
        anchor.setCol2(col2);
        anchor.setDx2(dx2);

        float h = 0;
        h += (1 - (float)anchor.getDy1()/256)* getRowHeightInPixels(anchor.getRow1());
        int row2 = anchor.getRow1() + 1;
        int dy2 = 0;

        while(h < scaledHeight){
            h += getRowHeightInPixels(row2++);
        }
        if(h > scaledHeight) {
            row2--;
            double ch = getRowHeightInPixels(row2);
            double delta = h - scaledHeight;
            dy2 = (int)((ch-delta)/ch*256);
        }
        anchor.setRow2(row2);
        anchor.setDy2(dy2);

        return anchor;
    }

    private float getColumnWidthInPixels(int column){

        int cw = getPatriarch().getSheet().getColumnWidth(column);
        float px = getPixelWidth(column);

        return cw/px;
    }

    private float getRowHeightInPixels(int i){

        HSSFRow row = getPatriarch().getSheet().getRow(i);
        float height;
        if(row != null) height = row.getHeight();
        else height = getPatriarch().getSheet().getDefaultRowHeight();

        return height/PX_ROW;
    }

    private float getPixelWidth(int column){

        int def = getPatriarch().getSheet().getDefaultColumnWidth()*256;
        int cw = getPatriarch().getSheet().getColumnWidth(column);

        return cw == def ? PX_DEFAULT : PX_MODIFIED;
    }

    /**
     * Return the dimension of this image
     *
     * @return image dimension
     */
    public Dimension getImageDimension(){
        EscherBSERecord bse = getPatriarch().getSheet()._book.getBSERecord(getPictureIndex());
        byte[] data = bse.getBlipRecord().getPicturedata();
        int type = bse.getBlipTypeWin32();
        return ImageUtils.getImageDimension(new ByteArrayInputStream(data), type);
    }
    
    /**
     * Return picture data for this shape
     *
     * @return picture data for this shape
     */
    public HSSFPictureData getPictureData(){
        InternalWorkbook iwb = getPatriarch().getSheet().getWorkbook().getWorkbook();
    	EscherBlipRecord blipRecord = iwb.getBSERecord(getPictureIndex()).getBlipRecord();
    	return new HSSFPictureData(blipRecord);
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        EscherAggregate agg = patriarch._getBoundAggregate();
        agg.associateShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
        EscherBSERecord bse =
                patriarch.getSheet().getWorkbook().getWorkbook().getBSERecord(getPictureIndex());
        bse.setRef(bse.getRef() + 1);
    }

    /**
     * The color applied to the lines of this shape.
     */
    public String getFileName() {
        EscherComplexProperty propFile = (EscherComplexProperty) getOptRecord().lookup(
                      EscherProperties.BLIP__BLIPFILENAME);
        try {
            if (null == propFile){
                return "";
            }
            return new String(propFile.getComplexData(), "UTF-16LE").trim();
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
    
    public void setFileName(String data){
        try {
            EscherComplexProperty prop = new EscherComplexProperty(EscherProperties.BLIP__BLIPFILENAME, true, data.getBytes("UTF-16LE"));
            setPropertyValue(prop);
        } catch (UnsupportedEncodingException e) {
        	logger.log( POILogger.ERROR, "Unsupported encoding: UTF-16LE");
        }
    }

    @Override
    public void setShapeType(int shapeType) {
        throw new IllegalStateException("Shape type can not be changed in "+this.getClass().getSimpleName());
    }

    @Override
    protected HSSFShape cloneShape() {
        EscherContainerRecord spContainer = new EscherContainerRecord();
        byte [] inSp = getEscherContainer().serialize();
        spContainer.fillFields(inSp, 0, new DefaultEscherRecordFactory());
        ObjRecord obj = (ObjRecord) getObjRecord().cloneViaReserialise();
        return new HSSFPicture(spContainer, obj);
    }
}
