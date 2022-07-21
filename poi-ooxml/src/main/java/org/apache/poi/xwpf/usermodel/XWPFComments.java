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
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComments;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

/**
 * specifies all of the comments defined in the current document
 */
public class XWPFComments extends POIXMLDocumentPart {

    XWPFDocument document;
    private final List<XWPFComment> comments = new ArrayList<>();
    private final List<XWPFPictureData> pictures = new ArrayList<>();
    private CTComments ctComments;

    /**
     * Construct XWPFComments from a package part
     *
     * @param part the package part holding the data of the footnotes,
     */
    public XWPFComments(POIXMLDocumentPart parent, PackagePart part) {
        super(parent, part);
        this.document = (XWPFDocument) getParent();

        if (this.document == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Construct XWPFComments from scratch for a new document.
     */
    public XWPFComments() {
        ctComments = CTComments.Factory.newInstance();
    }

    /**
     * read comments form an existing package
     */
    @Override
    public void onDocumentRead() throws IOException {
        try (InputStream is = getPackagePart().getInputStream()) {
            CommentsDocument doc = CommentsDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctComments = doc.getComments();
            for (CTComment ctComment : ctComments.getCommentList()) {
                comments.add(new XWPFComment(ctComment, this));
            }
        } catch (XmlException e) {
            throw new POIXMLException("Unable to read comments", e);
        }

        for (POIXMLDocumentPart poixmlDocumentPart : getRelations()) {
            if (poixmlDocumentPart instanceof XWPFPictureData) {
                XWPFPictureData xwpfPicData = (XWPFPictureData) poixmlDocumentPart;
                pictures.add(xwpfPicData);
                document.registerPackagePictureData(xwpfPicData);
            }
        }
    }

    /**
     * Adds a picture to the comments.
     *
     * @param is     The stream to read image from
     * @param format The format of the picture, see {@link Document}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException If the format of the picture is not known.
     * @throws IOException            If reading the picture-data from the stream fails.
     * @see #addPictureData(InputStream, PictureType)
     */
    public String addPictureData(InputStream is, int format) throws InvalidFormatException, IOException {
        byte[] data = IOUtils.toByteArrayWithMaxLength(is, XWPFPictureData.getMaxImageSize());
        return addPictureData(data, format);
    }

    /**
     * Adds a picture to the comments.
     *
     * @param is     The stream to read image from
     * @param pictureType The {@link PictureType} of the picture
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException If the pictureType of the picture is not known.
     * @throws IOException            If reading the picture-data from the stream fails.
     * @since POI 5.2.3
     */
    public String addPictureData(InputStream is, PictureType pictureType) throws InvalidFormatException, IOException {
        byte[] data = IOUtils.toByteArrayWithMaxLength(is, XWPFPictureData.getMaxImageSize());
        return addPictureData(data, pictureType);
    }

    /**
     * Adds a picture to the comments.
     *
     * @param pictureData The picture data
     * @param format      The format of the picture, see {@link Document}
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException If the format of the picture is not known.
     */
    public String addPictureData(byte[] pictureData, int format) throws InvalidFormatException {
        return addPictureData(pictureData, PictureType.findByOoxmlId(format));
    }

    /**
     * Adds a picture to the comments.
     *
     * @param pictureData The picture data
     * @param pictureType The {@link PictureType} of the picture.
     * @return the index to this picture (0 based), the added picture can be
     * obtained from {@link #getAllPictures()} .
     * @throws InvalidFormatException If the pictureType of the picture is not known.
     * @since POI 5.2.3
     */
    public String addPictureData(byte[] pictureData, PictureType pictureType) throws InvalidFormatException {
        if (pictureType == null) {
            throw new InvalidFormatException("pictureType is not supported");
        }
        XWPFPictureData xwpfPicData = document.findPackagePictureData(pictureData);
        POIXMLRelation relDesc = XWPFPictureData.RELATIONS[pictureType.ooxmlId];

        if (xwpfPicData == null) {
            /* Part doesn't exist, create a new one */
            int idx = getXWPFDocument().getNextPicNameNumber(pictureType);
            xwpfPicData = (XWPFPictureData) createRelationship(relDesc, XWPFFactory.getInstance(), idx);
            /* write bytes to new part */
            PackagePart picDataPart = xwpfPicData.getPackagePart();
            try (OutputStream out = picDataPart.getOutputStream()) {
                out.write(pictureData);
            } catch (IOException e) {
                throw new POIXMLException(e);
            }

            document.registerPackagePictureData(xwpfPicData);
            pictures.add(xwpfPicData);
            return getRelationId(xwpfPicData);
        } else if (!getRelations().contains(xwpfPicData)) {
            /*
             * Part already existed, but was not related so far. Create
             * relationship to the already existing part and update
             * POIXMLDocumentPart data.
             */
            // TODO add support for TargetMode.EXTERNAL relations.
            RelationPart rp = addRelation(null, XWPFRelation.IMAGES, xwpfPicData);
            pictures.add(xwpfPicData);
            return rp.getRelationship().getId();
        } else {
            /* Part already existed, get relation id and return it */
            return getRelationId(xwpfPicData);
        }
    }

    /**
     * save and commit comments
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(
                CTComments.type.getName().getNamespaceURI(), "comments"));
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            ctComments.save(out, xmlOptions);
        }
    }

    public List<XWPFPictureData> getAllPictures() {
        return Collections.unmodifiableList(pictures);
    }

    /**
     * Gets the underlying CTComments object for the comments.
     *
     * @return CTComments object
     */
    public CTComments getCtComments() {
        return ctComments;
    }

    /**
     * set a new comments
     */
    @Internal
    public void setCtComments(CTComments ctComments) {
        this.ctComments = ctComments;
    }

    /**
     * Get the list of {@link XWPFComment} in the Comments part.
     */
    public List<XWPFComment> getComments() {
        return comments;
    }

    /**
     * Get the specified comment by position
     *
     * @param pos Array position of the comment
     */
    public XWPFComment getComment(int pos) {
        if (pos >= 0 && pos < ctComments.sizeOfCommentArray()) {
            return getComments().get(pos);
        }
        return null;
    }

    /**
     * Get the specified comment by comment id
     *
     * @param id comment id
     * @return the specified comment
     */
    public XWPFComment getCommentByID(String id) {
        for (XWPFComment comment : comments) {
            if (comment.getId().equals(id)) {
                return comment;
            }
        }
        return null;
    }

    /**
     * Get the specified comment by ctComment
     */
    public XWPFComment getComment(CTComment ctComment) {
        for (XWPFComment comment : comments) {
            if (comment.getCtComment() == ctComment) {
                return comment;
            }
        }
        return null;
    }

    /**
     * Create a new comment and add it to the document.
     *
     * @param cid comment Id
     */
    public XWPFComment createComment(BigInteger cid) {
        CTComment ctComment = ctComments.addNewComment();
        ctComment.setId(cid);
        XWPFComment comment = new XWPFComment(ctComment, this);
        comments.add(comment);
        return comment;
    }

    /**
     * Remove the specified comment if present.
     *
     * @param pos Array position of the comment to be removed
     * @return True if the comment was removed.
     */
    public boolean removeComment(int pos) {
        if (pos >= 0 && pos < ctComments.sizeOfCommentArray()) {
            comments.remove(pos);
            ctComments.removeComment(pos);
            return true;
        }
        return false;
    }

    public XWPFDocument getXWPFDocument() {
        if (null != document) {
            return document;
        }
        return (XWPFDocument) getParent();
    }

    public void setXWPFDocument(XWPFDocument document) {
        this.document = document;
    }

}
