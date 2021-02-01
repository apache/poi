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
package org.apache.poi.hwpf.converter;

import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.util.Beta;

/**
 * User-implemented pictures manager to store images on-disk
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Beta
public interface PicturesManager
{
    /**
     * Stores image (probably on disk). Please, note that different output
     * format support different file types, so image conversion may be required.
     * For example, HTML browsers usually supports {@link PictureType#GIF},
     * {@link PictureType#JPEG}, {@link PictureType#PNG},
     * {@link PictureType#TIFF}, but rarely {@link PictureType#EMF} or
     * {@link PictureType#WMF}. FO (Apache FOP) supports at least PNG and SVG
     * types.
     * 
     * @param content
     *            picture content
     * @param pictureType
     *            detected picture type (may be {@link PictureType#UNKNOWN}
     * @param suggestedName
     *            suggested picture name (based on picture offset in file),
     *            supposed to be unique
     * @param widthInches
     *            display width in inches (scaled). May be useful for rendering
     *            vector images (such as EMF or WMF)
     * @param heightInches
     *            display height in inches (scaled). May be useful for rendering
     *            vector images (such as EMF or WMF)
     * @return path to file that can be used as reference in HTML (img's src) of
     *         XLS FO (fo:external-graphic's src) or <tt>null</tt> if image were
     *         not saved and should not be referenced from result HTML / FO.
     */
    String savePicture( byte[] content, PictureType pictureType,
            String suggestedName, float widthInches, float heightInches );
}
