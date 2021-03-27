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

/**
 * General enum class to define a picture format/type
 *
 * @since POI 5.0
 */
public enum PictureType {
    /** Extended windows meta file */
    EMF("image/x-emf",".emf"),
    /** Windows Meta File */
    WMF("image/x-wmf",".wmf"),
    /** Mac PICT format */
    PICT("image/pict",".pict"), // or image/x-pict (for HSLF) ???
    /** JPEG format */
    JPEG("image/jpeg",".jpg"),
    /** PNG format */
    PNG("image/png",".png"),
    /** Device independent bitmap */
    DIB("image/dib",".dib"),
    /** GIF image format */
    GIF("image/gif",".gif"),
    /** Tag Image File (.tiff) */
    TIFF("image/tiff",".tif"),
    /** Encapsulated Postscript (.eps) */
    EPS("image/x-eps",".eps"),
    /** Windows Bitmap (.bmp) */
    BMP("image/x-ms-bmp",".bmp"),
    /** WordPerfect graphics (.wpg) */
    WPG("image/x-wpg",".wpg"),
    /** Microsoft Windows Media Photo image (.wdp) */
    WDP("image/vnd.ms-photo",".wdp"),
    /** Scalable vector graphics (.svg) - supported by Office 2016 and higher */
    SVG("image/svg+xml", ".svg"),
    /** Unknown picture type - specific to escher bse record */
    UNKNOWN("", ".dat"),
    /** Picture type error - specific to escher bse record */
    ERROR("", ".dat"),
    /** JPEG in the YCCK or CMYK color space. */
    CMYKJPEG("image/jpeg", ".jpg"),
    /** client defined blip type - native-id 32 to 255 */
    CLIENT("", ".dat")
    ;

    public final String contentType,extension;

    PictureType(String contentType,String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
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
}
