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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.IOUtils;

/**
 * Raw picture data, normally attached to a WordprocessingML Drawing.
 * As a rule, pictures are stored in the /word/media/ part of a WordprocessingML package.
 *
 * @author Philipp Epp
 */
public class XWPFPictureData extends POIXMLDocumentPart {

    /**
     * Relationships for each known picture type
     */
    protected static final POIXMLRelation[] RELATIONS;

    static {
        RELATIONS = new POIXMLRelation[13];
        RELATIONS[Document.PICTURE_TYPE_EMF] = XWPFRelation.IMAGE_EMF;
        RELATIONS[Document.PICTURE_TYPE_WMF] = XWPFRelation.IMAGE_WMF;
        RELATIONS[Document.PICTURE_TYPE_PICT] = XWPFRelation.IMAGE_PICT;
        RELATIONS[Document.PICTURE_TYPE_JPEG] = XWPFRelation.IMAGE_JPEG;
        RELATIONS[Document.PICTURE_TYPE_PNG] = XWPFRelation.IMAGE_PNG;
        RELATIONS[Document.PICTURE_TYPE_DIB] = XWPFRelation.IMAGE_DIB;
        RELATIONS[Document.PICTURE_TYPE_GIF] = XWPFRelation.IMAGE_GIF;
        RELATIONS[Document.PICTURE_TYPE_TIFF] = XWPFRelation.IMAGE_TIFF;
        RELATIONS[Document.PICTURE_TYPE_EPS] = XWPFRelation.IMAGE_EPS;
        RELATIONS[Document.PICTURE_TYPE_BMP] = XWPFRelation.IMAGE_BMP;
        RELATIONS[Document.PICTURE_TYPE_WPG] = XWPFRelation.IMAGE_WPG;
    }

    private Long checksum;

    /**
     * Create a new XWPFGraphicData node
     */
    protected XWPFPictureData() {
        super();
    }

    /**
     * Construct XWPFPictureData from a package part
     *
     * @param part the package part holding the drawing data,
     * 
     * @since POI 3.14-Beta1
     */
    public XWPFPictureData(PackagePart part) {
        super(part);
    }
    
    @Override
    protected void onDocumentRead() throws IOException {
        super.onDocumentRead();
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
     * @return the Picture data.
     */
    public byte[] getData() {
        try {
            return IOUtils.toByteArray(getPackagePart().getInputStream());
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * Returns the file name of the image, eg image7.jpg . The original filename
     * isn't always available, but if it can be found it's likely to be in the
     * CTDrawing
     */
    public String getFileName() {
        String name = getPackagePart().getPartName().getName();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    /**
     * Suggests a file extension for this image.
     *
     * @return the file extension.
     */
    public String suggestFileExtension() {
        return getPackagePart().getPartName().getExtension();
    }

    /**
     * Return an integer constant that specifies type of this picture
     *
     * @return an integer constant that specifies type of this picture
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_EMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_WMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PICT
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_JPEG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PNG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_DIB
     */
    public int getPictureType() {
        String contentType = getPackagePart().getContentType();
        for (int i = 0; i < RELATIONS.length; i++) {
            if (RELATIONS[i] == null) {
                continue;
            }

            if (RELATIONS[i].getContentType().equals(contentType)) {
                return i;
            }
        }
        return 0;
    }

    public Long getChecksum() {
        if (this.checksum == null) {
            InputStream is = null;
            byte[] data;
            try {
                is = getPackagePart().getInputStream();
                data = IOUtils.toByteArray(is);
            } catch (IOException e) {
                throw new POIXMLException(e);
            } finally {
                IOUtils.closeQuietly(is);
            }
            this.checksum = IOUtils.calculateChecksum(data);
        }
        return this.checksum;
    }

    @Override
    public boolean equals(Object obj) {
        /*
         * In case two objects ARE equal, but its not the same instance, this
         * implementation will always run through the whole
         * byte-array-comparison before returning true. If this will turn into a
         * performance issue, two possible approaches are available:<br>
         * a) Use the checksum only and take the risk that two images might have
         * the same CRC32 sum, although they are not the same.<br>
         * b) Use a second (or third) checksum algorithm to minimise the chance
         * that two images have the same checksums but are not equal (e.g.
         * CRC32, MD5 and SHA-1 checksums, additionally compare the
         * data-byte-array lengths).
         */
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof XWPFPictureData)) {
            return false;
        }

        XWPFPictureData picData = (XWPFPictureData) obj;
        PackagePart foreignPackagePart = picData.getPackagePart();
        PackagePart ownPackagePart = this.getPackagePart();

        if ((foreignPackagePart != null && ownPackagePart == null)
                || (foreignPackagePart == null && ownPackagePart != null)) {
            return false;
        }

        if (ownPackagePart != null) {
            OPCPackage foreignPackage = foreignPackagePart.getPackage();
            OPCPackage ownPackage = ownPackagePart.getPackage();

            if ((foreignPackage != null && ownPackage == null)
                    || (foreignPackage == null && ownPackage != null)) {
                return false;
            }
            if (ownPackage != null) {

                if (!ownPackage.equals(foreignPackage)) {
                    return false;
                }
            }
        }

        Long foreignChecksum = picData.getChecksum();
        Long localChecksum = getChecksum();

        if (!(localChecksum.equals(foreignChecksum))) {
            return false;
        }
        return Arrays.equals(this.getData(), picData.getData());
    }

    @Override
    public int hashCode() {
        return getChecksum().hashCode();
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
