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

import org.apache.poi.common.usermodel.PictureType;

public interface Document {
    /**
     * Extended windows meta file
     */
    public static final int PICTURE_TYPE_EMF = PictureType.EMF.ooxmlId;

    /**
     * Windows Meta File
     */
    public static final int PICTURE_TYPE_WMF = PictureType.WMF.ooxmlId;

    /**
     * Mac PICT format
     */
    public static final int PICTURE_TYPE_PICT = PictureType.PICT.ooxmlId;

    /**
     * JPEG format
     */
    public static final int PICTURE_TYPE_JPEG = PictureType.JPEG.ooxmlId;

    /**
     * PNG format
     */
    public static final int PICTURE_TYPE_PNG = PictureType.PNG.ooxmlId;

    /**
     * Device independent bitmap
     */
    public static final int PICTURE_TYPE_DIB = PictureType.DIB.ooxmlId;

    /**
     * GIF image format
     */
    public static final int PICTURE_TYPE_GIF = PictureType.GIF.ooxmlId;

    /**
     * Tag Image File (.tiff)
     */
    public static final int PICTURE_TYPE_TIFF = PictureType.TIFF.ooxmlId;

    /**
     * Encapsulated Postscript (.eps)
     */
    public static final int PICTURE_TYPE_EPS = PictureType.EPS.ooxmlId;


    /**
     * Windows Bitmap (.bmp)
     */
    public static final int PICTURE_TYPE_BMP = PictureType.BMP.ooxmlId;

    /**
     * WordPerfect graphics (.wpg)
     */
    public static final int PICTURE_TYPE_WPG = PictureType.WPG.ooxmlId;
}
