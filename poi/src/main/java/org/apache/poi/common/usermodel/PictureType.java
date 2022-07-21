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

package org.apache.poi.common.usermodel;

import org.apache.poi.poifs.filesystem.FileMagic;

import java.util.HashMap;

/**
 * General enum class to define a picture format/type
 *
 * @since POI 5.0
 */
public enum PictureType {

    /** Extended windows meta file */
    EMF("image/x-emf", ".emf", 2),
    /** Windows Meta File */
    WMF("image/x-wmf", ".wmf", 3),
    /** Mac PICT format */
    PICT("image/x-pict", ".pict", 4),
    /** JPEG format */
    JPEG("image/jpeg", ".jpg", 5),
    /** PNG format */
    PNG("image/png", ".png", 6),
    /** Device independent bitmap */
    DIB("image/dib", ".dib", 7),
    /** GIF image format */
    GIF("image/gif", ".gif", 8),
    /** Tag Image File (.tiff) */
    TIFF("image/tiff", ".tif", 9),
    /** Encapsulated Postscript (.eps) */
    EPS("image/x-eps", ".eps", 10),
    /** Windows Bitmap (.bmp) */
    BMP("image/x-ms-bmp", ".bmp", 11),
    /** WordPerfect graphics (.wpg) */
    WPG("image/x-wpg", ".wpg", 12),
    /** Microsoft Windows Media Photo image (.wdp) */
    WDP("image/vnd.ms-photo", ".wdp", 13),
    /** Scalable vector graphics (.svg) - supported by Office 2016 and higher */
    SVG("image/svg+xml", ".svg", -1),
    /** Unknown picture type - specific to escher bse record */
    UNKNOWN("", ".dat", -1),
    /** Picture type error - specific to escher bse record */
    ERROR("", ".dat", -1),
    /** JPEG in the YCCK or CMYK color space. */
    CMYKJPEG("image/jpeg", ".jpg", -1),
    /** client defined blip type - native-id 32 to 255 */
    CLIENT("", ".dat", -1)
    ;

    private static final HashMap<Integer, PictureType> PICTURE_TYPE_BY_OOXML_ID;

    static {
        PICTURE_TYPE_BY_OOXML_ID = new HashMap<>();

        for (PictureType pictureType : values()) {
            if (pictureType.ooxmlId >= -1) {
                PICTURE_TYPE_BY_OOXML_ID.put(pictureType.ooxmlId, pictureType);
            }
        }
    }

    public final String contentType, extension;
    public final int ooxmlId;

    PictureType(String contentType, String extension, int ooxmlId) {
        this.contentType = contentType;
        this.extension = extension;
        this.ooxmlId = ooxmlId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }

    public int getOoxmlId() {
        return ooxmlId;
    }

    public static PictureType valueOf(FileMagic fm) {
        switch (fm) {
            case BMP:
                return PictureType.BMP;
            case GIF:
                return PictureType.GIF;
            case JPEG:
                return PictureType.JPEG;
            case PNG:
                return PictureType.PNG;
            case XML:
                // this is quite fuzzy, to suppose all XMLs are SVGs when handling pictures ...
                return PictureType.SVG;
            case WMF:
                return PictureType.WMF;
            case EMF:
                return PictureType.EMF;
            case TIFF:
                return PictureType.TIFF;
            default:
            case UNKNOWN:
                return PictureType.UNKNOWN;
        }
    }

    /**
     * @param ooxmlId for PictureType
     * @return PictureType, null if ooxmlId does not match any PictureTypes
     */
    public static PictureType findByOoxmlId(int ooxmlId) {
        return PICTURE_TYPE_BY_OOXML_ID.get(ooxmlId);
    }
}
