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
package org.apache.poi.xslf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPresentation;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMaster;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideMasterIdListEntry;
import org.openxmlformats.schemas.presentationml.x2006.main.CmLstDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.NotesDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.PresentationDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.SldMasterDocument;

/**
 * Experimental class to do low level processing of pptx files.
 *
 * Most users should use the higher level {@link XMLSlideShow} instead.
 *
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 *
 * WARNING - APIs expected to change rapidly
 */
public class XSLFSlideShow extends POIXMLDocument {

    private final PresentationDocument presentationDoc;
    /**
     * The embedded OLE2 files in the OPC package
     */
    private final List<PackagePart> embeddedParts;

    public XSLFSlideShow(OPCPackage container) throws OpenXML4JException, IOException, XmlException {
        super(container);

        if(getCorePart().getContentType().equals(XSLFRelation.THEME_MANAGER.getContentType())) {
            rebase(getPackage());
        }

        try (InputStream stream = getCorePart().getInputStream()) {
            presentationDoc =
                    PresentationDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
        }

        embeddedParts = new LinkedList<>();
        for (CTSlideIdListEntry ctSlide : getSlideReferences().getSldIdArray()) {
            PackagePart corePart = getCorePart();
            PackagePart slidePart = corePart.getRelatedPart(corePart.getRelationship(ctSlide.getId2()));

            for(PackageRelationship rel : slidePart.getRelationshipsByType(OLE_OBJECT_REL_TYPE)) {
                if (TargetMode.EXTERNAL == rel.getTargetMode()) {
                    continue;
                }
                // TODO: Add this reference to each slide as well
                embeddedParts.add(slidePart.getRelatedPart(rel));
            }

            for (PackageRelationship rel : slidePart.getRelationshipsByType(PACK_OBJECT_REL_TYPE)) {
                embeddedParts.add(slidePart.getRelatedPart(rel));
            }
        }
    }

    public XSLFSlideShow(String file) throws OpenXML4JException, IOException, XmlException {
        this(openPackage(file));
    }

    /**
     * Returns the low level presentation base object
     */
    @Internal
    public CTPresentation getPresentation() {
        return presentationDoc.getPresentation();
    }

    /**
     * Returns the references from the presentation to its
     *  slides.
     * You'll need these to figure out the slide ordering,
     *  and to get at the actual slides themselves
     */
    @Internal
    public CTSlideIdList getSlideReferences() {
        if(! getPresentation().isSetSldIdLst()) {
            getPresentation().setSldIdLst(CTSlideIdList.Factory.newInstance());
        }
        return getPresentation().getSldIdLst();
    }

    /**
     * Returns the references from the presentation to its
     *  slide masters.
     * You'll need these to get at the actual slide
     *  masters themselves
     */
    @Internal
    public CTSlideMasterIdList getSlideMasterReferences() {
        return getPresentation().getSldMasterIdLst();
    }

    public PackagePart getSlideMasterPart(CTSlideMasterIdListEntry master) throws IOException, XmlException {
        try {
           PackagePart corePart = getCorePart();
            return corePart.getRelatedPart(
                corePart.getRelationship(master.getId2())
            );
        } catch(InvalidFormatException e) {
            throw new XmlException(e);
        }
    }
    /**
     * Returns the low level slide master object from
     *  the supplied slide master reference
     */
    @Internal
    public CTSlideMaster getSlideMaster(CTSlideMasterIdListEntry master) throws IOException, XmlException {
        PackagePart masterPart = getSlideMasterPart(master);
        try (InputStream stream = masterPart.getInputStream()) {
            SldMasterDocument masterDoc =
                    SldMasterDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            return masterDoc.getSldMaster();
        }
    }

    public PackagePart getSlidePart(CTSlideIdListEntry slide) throws IOException, XmlException {
        try {
            PackagePart corePart = getCorePart();
            return corePart.getRelatedPart(corePart.getRelationship(slide.getId2()));
        } catch(InvalidFormatException e) {
            throw new XmlException(e);
        }
    }
    /**
     * Returns the low level slide object from
     *  the supplied slide reference
     */
    @Internal
    public CTSlide getSlide(CTSlideIdListEntry slide) throws IOException, XmlException {
        PackagePart slidePart = getSlidePart(slide);
        try (InputStream stream = slidePart.getInputStream()) {
            SldDocument slideDoc = SldDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            return slideDoc.getSld();
        }
    }

    /**
     * Gets the PackagePart of the notes for the
     *  given slide, or null if there isn't one.
     */
    public PackagePart getNodesPart(CTSlideIdListEntry parentSlide) throws IOException, XmlException {
        PackageRelationshipCollection notes;
        PackagePart slidePart = getSlidePart(parentSlide);

        try {
            notes = slidePart.getRelationshipsByType(XSLFRelation.NOTES.getRelation());
        } catch(InvalidFormatException e) {
            throw new IOException(e);
        }

        if(notes.isEmpty()) {
            // No notes for this slide
            return null;
        }
        if(notes.size() > 1) {
            throw new IOException("Expecting 0 or 1 notes for a slide, but found " + notes.size());
        }

        try {
            return slidePart.getRelatedPart(notes.getRelationship(0));
        } catch(InvalidFormatException e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * Returns the low level notes object for the given
     *  slide, as found from the supplied slide reference
     */
    @Internal
    public CTNotesSlide getNotes(CTSlideIdListEntry slide) throws IOException, XmlException {
        PackagePart notesPart = getNodesPart(slide);
        if(notesPart == null)
            return null;

        try (InputStream stream = notesPart.getInputStream()) {
            NotesDocument notesDoc = NotesDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            return notesDoc.getNotes();
        }
    }

    /**
     * Returns all the comments for the given slide
     */
    @Internal
    public CTCommentList getSlideComments(CTSlideIdListEntry slide) throws IOException, XmlException {
        PackageRelationshipCollection commentRels;
        PackagePart slidePart = getSlidePart(slide);

        try {
            commentRels = slidePart.getRelationshipsByType(XSLFRelation.COMMENTS.getRelation());
        } catch(InvalidFormatException e) {
            throw new IOException(e);
        }

        if(commentRels.isEmpty()) {
            // No comments for this slide
            return null;
        }
        if(commentRels.size() > 1) {
            throw new IOException("Expecting 0 or 1 comments for a slide, but found " + commentRels.size());
        }

        try {
            PackagePart cPart = slidePart.getRelatedPart(
                    commentRels.getRelationship(0)
            );
            try (InputStream stream = cPart.getInputStream()) {
                CmLstDocument commDoc = CmLstDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
                return commDoc.getCmLst();
            }
        } catch(InvalidFormatException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get the document's embedded files.
     */
    @Override
    public List<PackagePart> getAllEmbeddedParts() throws OpenXML4JException {
        return embeddedParts;
    }

}
