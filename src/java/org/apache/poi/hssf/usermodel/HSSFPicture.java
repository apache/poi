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

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherTextboxRecord;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.ObjRecord;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 */
public class HSSFPicture extends HSSFSimpleShape implements Picture {
	@SuppressWarnings("unused")
    private static POILogger logger = POILogFactory.getLogger(HSSFPicture.class);
	
    public static final int PICTURE_TYPE_EMF = HSSFWorkbook.PICTURE_TYPE_EMF;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = HSSFWorkbook.PICTURE_TYPE_WMF;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = HSSFWorkbook.PICTURE_TYPE_PICT;              // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = HSSFWorkbook.PICTURE_TYPE_JPEG;              // JFIF
    public static final int PICTURE_TYPE_PNG = HSSFWorkbook.PICTURE_TYPE_PNG;                // PNG
    public static final int PICTURE_TYPE_DIB = HSSFWorkbook.PICTURE_TYPE_DIB;                // Windows DIB

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
     * Reset the image to the dimension of the embedded image
     * 
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     */
    @Override
    public void resize(){
        resize(Double.MAX_VALUE);
    }

    /**
     * Resize the image proportionally.
     *
     * @see #resize(double, double)
     */
    @Override
    public void resize(double scale) {
        resize(scale,scale);
    }
    
    /**
     * Resize the image
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     * <p>
     * <code>resize(1.0,1.0)</code> keeps the original size,<br>
     * <code>resize(0.5,0.5)</code> resize to 50% of the original,<br>
     * <code>resize(2.0,2.0)</code> resizes to 200% of the original.<br>
     * <code>resize({@link Double#MAX_VALUE},{@link Double#MAX_VALUE})</code> resizes to the dimension of the embedded image. 
     * </p>
     *
     * @param scaleX the amount by which the image width is multiplied relative to the original width.
     * @param scaleY the amount by which the image height is multiplied relative to the original height.
     */
    @Override
    public void resize(double scaleX, double scaleY) {
        HSSFClientAnchor anchor = getClientAnchor();
        anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);

        HSSFClientAnchor pref = getPreferredSize(scaleX,scaleY);

        int row2 = anchor.getRow1() + (pref.getRow2() - pref.getRow1());
        int col2 = anchor.getCol1() + (pref.getCol2() - pref.getCol1());

        anchor.setCol2((short)col2);
        // anchor.setDx1(0);
        anchor.setDx2(pref.getDx2());

        anchor.setRow2(row2);
        // anchor.setDy1(0);
        anchor.setDy2(pref.getDy2());
    }

    /**
     * Calculate the preferred size for this picture.
     *
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.0.2
     */
    @Override
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
        return getPreferredSize(scale, scale);
    }
    
    /**
     * Calculate the preferred size for this picture.
     *
     * @param scaleX the amount by which image width is multiplied relative to the original width.
     * @param scaleY the amount by which image height is multiplied relative to the original height.
     * @return HSSFClientAnchor with the preferred size for this image
     * @since POI 3.11
     */
    @Override
    public HSSFClientAnchor getPreferredSize(double scaleX, double scaleY){
        ImageUtils.setPreferredSize(this, scaleX, scaleY);
        return getClientAnchor();
    }

    /**
     * Return the dimension of the embedded image in pixel
     *
     * @return image dimension in pixels
     */
    @Override
    public Dimension getImageDimension(){
        InternalWorkbook iwb = getPatriarch().getSheet().getWorkbook().getWorkbook();
        EscherBSERecord bse = iwb.getBSERecord(getPictureIndex());
        byte[] data = bse.getBlipRecord().getPicturedata();
        int type = bse.getBlipTypeWin32();
        return ImageUtils.getImageDimension(new ByteArrayInputStream(data), type);
    }
    
    /**
     * Return picture data for this shape
     *
     * @return picture data for this shape or {@code null} if picture wasn't embedded, i.e. external linked
     */
    @Override
    public HSSFPictureData getPictureData(){
        int picIdx = getPictureIndex();
        if (picIdx == -1) {
            return null;
        }
        
        HSSFPatriarch patriarch = getPatriarch();
        HSSFShape parent = getParent();
        while(patriarch == null && parent != null) {
            patriarch = parent.getPatriarch();
            parent = parent.getParent();
        }
        if(patriarch == null) {
            throw new IllegalStateException("Could not find a patriarch for a HSSPicture");
        }

        InternalWorkbook iwb = patriarch.getSheet().getWorkbook().getWorkbook();
        EscherBSERecord bse = iwb.getBSERecord(picIdx);
    	EscherBlipRecord blipRecord = bse.getBlipRecord();
    	return new HSSFPictureData(blipRecord);
    }

    @Override
    void afterInsert(HSSFPatriarch patriarch) {
        EscherAggregate agg = patriarch.getBoundAggregate();
        agg.associateShapeToObjRecord(getEscherContainer().getChildById(EscherClientDataRecord.RECORD_ID), getObjRecord());
        if(getPictureIndex() != -1) {
            EscherBSERecord bse =
                    patriarch.getSheet().getWorkbook().getWorkbook().getBSERecord(getPictureIndex());
            bse.setRef(bse.getRef() + 1);
        }
    }

    /**
     * The filename of the embedded image
     */
    public String getFileName() {
        EscherComplexProperty propFile = getOptRecord().lookup(
                      EscherProperties.BLIP__BLIPFILENAME);
        return (null == propFile)
            ? ""
            : StringUtil.getFromUnicodeLE(propFile.getComplexData()).trim();
    }
    
    public void setFileName(String data){
        // TODO: add trailing \u0000? 
        byte[] bytes = StringUtil.getToUnicodeLE(data);
        EscherComplexProperty prop = new EscherComplexProperty(EscherProperties.BLIP__BLIPFILENAME, true, bytes);
        setPropertyValue(prop);
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
    
    /**
     * @return the anchor that is used by this picture.
     */
    @Override
    public HSSFClientAnchor getClientAnchor() {
        HSSFAnchor a = getAnchor();
        return (a instanceof HSSFClientAnchor) ? (HSSFClientAnchor)a : null;
    }

    
    /**
     * @return the sheet which contains the picture shape
     */
    @Override
    public HSSFSheet getSheet() {
        return getPatriarch().getSheet();
    }
}
