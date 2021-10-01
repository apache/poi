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

import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherRecordTypes;
import org.apache.poi.sl.image.ImageHeaderPNG;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Represents binary data stored in the file.  Eg. A GIF, JPEG etc...
 */
public class HSSFPictureData implements PictureData
{
    // MSOBI constants for various formats.
    public static final short MSOBI_WMF   = 0x2160;
    public static final short MSOBI_EMF   = 0x3D40;
    public static final short MSOBI_PICT  = 0x5420;
    public static final short MSOBI_PNG   = 0x6E00;
    public static final short MSOBI_JPEG  = 0x46A0;
    public static final short MSOBI_DIB   = 0x7A80;
    // Mask of the bits in the options used to store the image format.
    public static final short FORMAT_MASK = (short) 0xFFF0;

    /**
     * Underlying escher blip record containing the bitmap data.
     */
    private final EscherBlipRecord blip;

    /**
     * Constructs a picture object.
     *
     * @param blip the underlying blip record containing the bitmap data.
     */
    public HSSFPictureData( EscherBlipRecord blip )
    {
        this.blip = blip;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.PictureData#getData()
     */
    @Override
    public byte[] getData() {
        return new ImageHeaderPNG(blip.getPicturedata()).extractPNG();
    }

    /**
     *
     * @return format of the picture.
     * @see HSSFWorkbook#PICTURE_TYPE_DIB
     * @see HSSFWorkbook#PICTURE_TYPE_WMF
     * @see HSSFWorkbook#PICTURE_TYPE_EMF
     * @see HSSFWorkbook#PICTURE_TYPE_PNG
     * @see HSSFWorkbook#PICTURE_TYPE_JPEG
     * @see HSSFWorkbook#PICTURE_TYPE_PICT
     */
    public int getFormat(){
        return blip.getRecordId() - EscherRecordTypes.BLIP_START.typeID;
    }

    /**
    * @see #getFormat
    * @return 'wmf', 'jpeg' etc depending on the format. never {@code null}
    */
    @Override
    public String suggestFileExtension() {
        switch (EscherRecordTypes.forTypeID(blip.getRecordId())) {
            case BLIP_WMF:
                return "wmf";
            case BLIP_EMF:
                return "emf";
            case BLIP_PICT:
                return "pict";
            case BLIP_PNG:
                return "png";
            case BLIP_JPEG:
                return "jpeg";
            case BLIP_DIB:
                return "dib";
            case BLIP_TIFF:
                return "tif";
            default:
                return "";
        }
    }

    @Override
    public String getMimeType() {
       switch (EscherRecordTypes.forTypeID(blip.getRecordId())) {
           case BLIP_WMF:
               return "image/x-wmf";
           case BLIP_EMF:
               return "image/x-emf";
           case BLIP_PICT:
               return "image/x-pict";
           case BLIP_PNG:
               return "image/png";
           case BLIP_JPEG:
               return "image/jpeg";
           case BLIP_DIB:
               return "image/bmp";
           case BLIP_TIFF:
               return "image/tiff";
           default:
               return "image/unknown";
       }
    }

    /**
     * @return the POI internal image type, 0 if unknown image type (was -1 prior to 5.0.0 but
     * that was inconsistent with other {@link PictureData} implementations)
     */
    @Override
    public int getPictureType() {
        switch (EscherRecordTypes.forTypeID(blip.getRecordId())) {
            case BLIP_WMF:
                return Workbook.PICTURE_TYPE_WMF;
            case BLIP_EMF:
                return Workbook.PICTURE_TYPE_EMF;
            case BLIP_PICT:
                return Workbook.PICTURE_TYPE_PICT;
            case BLIP_PNG:
                return Workbook.PICTURE_TYPE_PNG;
            case BLIP_JPEG:
                return Workbook.PICTURE_TYPE_JPEG;
            case BLIP_DIB:
                return Workbook.PICTURE_TYPE_DIB;
            case BLIP_TIFF:
                // not another int constant ...
            default:
                return 0;
        }
    }
}
