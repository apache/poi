/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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

/**
 * Represents binary data stored in the file.  Eg. A GIF, JPEG etc...
 *
 * @author Daniel Noll
 */
public class HSSFPictureData
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
    HSSFPictureData( EscherBlipRecord blip )
    {
        this.blip = blip;
    }

    /**
     * Gets the picture data.
     *
     * @return the picture data.
     */
    public byte[] getData()
    {
        return blip.getPicturedata();
    }

    /**
     * Suggests a file extension for this image.
     *
     * @return the file extension.
     */
    public String suggestFileExtension()
    {
        switch (blip.getRecordId())
        {
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
}

