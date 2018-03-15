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

import org.apache.poi.ddf.EscherBitmapBlip;
import org.apache.poi.ddf.EscherBlipRecord;
import org.apache.poi.ddf.EscherMetafileBlip;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.PngUtils;

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
    private EscherBlipRecord blip;

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
    public byte[] getData()
    {
        byte[] pictureData = blip.getPicturedata();

        //PNG created on MAC may have a 16-byte prefix which prevents successful reading.
        //Just cut it off!.
        if (PngUtils.matchesPngHeader(pictureData, 16))
        {
            byte[] png = new byte[pictureData.length-16];
            System.arraycopy(pictureData, 16, png, 0, png.length);
            pictureData = png;
        }

        return pictureData;
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
        return blip.getRecordId() - (short)0xF018;
    }

    /**
    * @see #getFormat
    * @return 'wmf', 'jpeg' etc depending on the format. never <code>null</code>
    */
    public String suggestFileExtension() {
        switch (blip.getRecordId()) {
            case EscherMetafileBlip.RECORD_ID_WMF:
                return "wmf";
            case EscherMetafileBlip.RECORD_ID_EMF:
                return "emf";
            case EscherMetafileBlip.RECORD_ID_PICT:
                return "pict";
            case EscherBitmapBlip.RECORD_ID_PNG:
                return "png";
            case EscherBitmapBlip.RECORD_ID_JPEG:
                return "jpeg";
            case EscherBitmapBlip.RECORD_ID_DIB:
                return "dib";
            default:
                return "";
        }
    }
    
    /**
     * Returns the mime type for the image
     */
    public String getMimeType() {
       switch (blip.getRecordId()) {
           case EscherMetafileBlip.RECORD_ID_WMF:
               return "image/x-wmf";
           case EscherMetafileBlip.RECORD_ID_EMF:
               return "image/x-emf";
           case EscherMetafileBlip.RECORD_ID_PICT:
               return "image/x-pict";
           case EscherBitmapBlip.RECORD_ID_PNG:
               return "image/png";
           case EscherBitmapBlip.RECORD_ID_JPEG:
               return "image/jpeg";
           case EscherBitmapBlip.RECORD_ID_DIB:
               return "image/bmp";
           default:
               return "image/unknown";
       }
    }
    
    /**
     * @return the POI internal image type, -1 if not unknown image type
     *
     * @see Workbook#PICTURE_TYPE_DIB
     * @see Workbook#PICTURE_TYPE_EMF
     * @see Workbook#PICTURE_TYPE_JPEG
     * @see Workbook#PICTURE_TYPE_PICT
     * @see Workbook#PICTURE_TYPE_PNG
     * @see Workbook#PICTURE_TYPE_WMF
     */
    public int getPictureType() {
        switch (blip.getRecordId()) {
            case EscherMetafileBlip.RECORD_ID_WMF:
                return Workbook.PICTURE_TYPE_WMF;
            case EscherMetafileBlip.RECORD_ID_EMF:
                return Workbook.PICTURE_TYPE_EMF;
            case EscherMetafileBlip.RECORD_ID_PICT:
                return Workbook.PICTURE_TYPE_PICT;
            case EscherBitmapBlip.RECORD_ID_PNG:
                return Workbook.PICTURE_TYPE_PNG;
            case EscherBitmapBlip.RECORD_ID_JPEG:
                return Workbook.PICTURE_TYPE_JPEG;
            case EscherBitmapBlip.RECORD_ID_DIB:
                return Workbook.PICTURE_TYPE_DIB;
            default:
                return -1;
        }        
    }
}
