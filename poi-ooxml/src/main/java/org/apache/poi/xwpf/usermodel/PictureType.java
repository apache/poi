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
package org.apache.poi.xwpf.usermodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @since POI 5.2.3
 */
public enum PictureType {
    /**
     * Extended windows meta file
     */
    EMF(Document.PICTURE_TYPE_EMF),
    /**
     * Windows Meta File
     */
    WMF(Document.PICTURE_TYPE_WMF),
    /**
     * Mac PICT format
     */
    PICT(Document.PICTURE_TYPE_PICT),
    /**
     * JPEG format
     */
    JPEG(Document.PICTURE_TYPE_JPEG),
    /**
     * JPEG format
     */
    PNG(Document.PICTURE_TYPE_PNG),
    /**
     * Device independent bitmap
     */
    DIB(Document.PICTURE_TYPE_DIB),
    /**
     * GIF image format
     */
    GIF(Document.PICTURE_TYPE_GIF),
    /**
     * Tag Image File (.tiff)
     */
    TIFF(Document.PICTURE_TYPE_TIFF),
    /**
     * Encapsulated Postscript (.eps)
     */
    EPS(Document.PICTURE_TYPE_EPS),
    /**
     * Windows Bitmap (.bmp)
     */
    BMP(Document.PICTURE_TYPE_BMP),
    /**
     * WordPerfect graphics (.wpg)
     */
    WPG(Document.PICTURE_TYPE_WPG);

    /**
     * Map relating the old API constant values to their corresponding
     * enumeration value
     */
    private static final Map<Integer, PictureType> PICTURE_TYPE_BY_ID;

    static {
        PICTURE_TYPE_BY_ID = new HashMap<>();

        for (PictureType pictureType : values()) {
            PICTURE_TYPE_BY_ID.put(pictureType.id, pictureType);
        }
    }

    private int id;

    PictureType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * @param id for PictureType
     * @return PictureType, null if id does not match any PictureTypes
     */
    public static PictureType findById(int id) {
        return PICTURE_TYPE_BY_ID.get(id);
    }
}
