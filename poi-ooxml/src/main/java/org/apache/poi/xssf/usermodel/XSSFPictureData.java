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

package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Removal;

/**
 * Raw picture data, normally attached to a SpreadsheetML Drawing.
 * As a rule, pictures are stored in the /xl/media/ part of a SpreadsheetML package.
 */
public class XSSFPictureData extends POIXMLDocumentPart implements PictureData {

    private static final int DEFAULT_MAX_IMAGE_SIZE = 100_000_000;
    private static int MAX_IMAGE_SIZE = DEFAULT_MAX_IMAGE_SIZE;

    /**
     * @param length the max image size allowed for XSSF pictures
     */
    public static void setMaxImageSize(int length) {
        MAX_IMAGE_SIZE = length;
    }

    /**
     * @return the max image size allowed for XSSF pictures
     */
    public static int getMaxImageSize() {
        final int ioMaxSize = IOUtils.getByteArrayMaxOverride();
        return ioMaxSize < 0 ? MAX_IMAGE_SIZE : Math.min(MAX_IMAGE_SIZE, ioMaxSize);
    }

    /**
     * Relationships for each known picture type
     */
    private static final POIXMLRelation[] PICTURE_RELATIONS;
    static {
        PICTURE_RELATIONS = new POIXMLRelation[13];
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_EMF] = XSSFRelation.IMAGE_EMF;
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_WMF] = XSSFRelation.IMAGE_WMF;
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_PICT] = XSSFRelation.IMAGE_PICT;
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_JPEG] = XSSFRelation.IMAGE_JPEG;
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_PNG] = XSSFRelation.IMAGE_PNG;
        PICTURE_RELATIONS[Workbook.PICTURE_TYPE_DIB] = XSSFRelation.IMAGE_DIB;
        PICTURE_RELATIONS[XSSFWorkbook.PICTURE_TYPE_GIF] = XSSFRelation.IMAGE_GIF;
        PICTURE_RELATIONS[XSSFWorkbook.PICTURE_TYPE_TIFF] = XSSFRelation.IMAGE_TIFF;
        PICTURE_RELATIONS[XSSFWorkbook.PICTURE_TYPE_EPS] = XSSFRelation.IMAGE_EPS;
        PICTURE_RELATIONS[XSSFWorkbook.PICTURE_TYPE_BMP] = XSSFRelation.IMAGE_BMP;
        PICTURE_RELATIONS[XSSFWorkbook.PICTURE_TYPE_WPG] = XSSFRelation.IMAGE_WPG;
    }

    /**
     * Relationships for each known picture type
     * <p>
     * Changing this array has no effect, as POI uses its own copy.
     *
     * @deprecated the field will be removed
     */
    @Deprecated
    @Removal(version = "7.0.0")
    protected static final POIXMLRelation[] RELATIONS = PICTURE_RELATIONS.clone();

    /**
     * @param pictureType the picture type, used as the index into the relationships
     * @return the relationship for the picture type, or {@code null} if there is none
     */
    static POIXMLRelation getRelationForType(int pictureType) {
        return PICTURE_RELATIONS[pictureType];
    }

    /**
     * Create a new XSSFPictureData node
     *
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#addPicture(byte[], int)
     */
    protected XSSFPictureData() {
        super();
    }

    /**
     * Construct XSSFPictureData from a package part
     *
     * @param part the package part holding the drawing data,
     *
     * @since 3.14-Beta1
     */
    protected XSSFPictureData(PackagePart part) {
        super(part);
    }

    /**
     * Gets the picture data as a byte array.
     * <p>
     * Note, that this call might be expensive since all the picture data is copied into a temporary byte array.
     * You can grab the picture data directly from the underlying package part as follows:
     * <br>
     * <code>
     * InputStream is = getPackagePart().getInputStream();
     * </code>
     * </p>
     *
     * @return the picture data.
     */
    @Override
    public byte[] getData() {
        try (InputStream inputStream = getPackagePart().getInputStream()) {
            return IOUtils.toByteArrayWithMaxLength(inputStream, getMaxImageSize());
        } catch(IOException e) {
            throw new POIXMLException(e);
        }
    }

    @Override
    public String suggestFileExtension() {
        return getPackagePart().getPartName().getExtension();
    }

    @Override
    public int getPictureType() {
        String contentType = getPackagePart().getContentType();
        for (int i = 0; i < PICTURE_RELATIONS.length; i++) {
            if(PICTURE_RELATIONS[i] == null) continue;

            if(PICTURE_RELATIONS[i].getContentType().equals(contentType)){
                return i;
            }
        }
        return 0;
    }

    @Override
    public String getMimeType() {
        return getPackagePart().getContentType();
    }

    /**
     * *PictureData objects store the actual content in the part directly without keeping a
     * copy like all others therefore we need to handle them differently.
     */
    @Override
    protected void prepareForCommit() {
        // do not clear the part here
    }
}
