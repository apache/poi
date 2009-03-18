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
package org.apache.poi.xslf;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
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
 * Experimental class to do low level processing
 *  of pptx files.
 *  
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 * 
 * WARNING - APIs expected to change rapidly
 */
public class XSLFSlideShow extends POIXMLDocument {
	public static final String MAIN_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml";
	public static final String NOTES_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml";
	public static final String SLIDE_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.slide+xml";
	public static final String SLIDE_LAYOUT_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout";
	public static final String NOTES_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide";
	public static final String COMMENT_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";

	private PresentationDocument presentationDoc;
    /**
     * The embedded OLE2 files in the OPC package
     */
    private List<PackagePart> embedds;

	public XSLFSlideShow(OPCPackage container) throws OpenXML4JException, IOException, XmlException {
		super(container);
		
		presentationDoc =
			PresentationDocument.Factory.parse(getCorePart().getInputStream());
		
        embedds = new LinkedList<PackagePart>();
		for (CTSlideIdListEntry ctSlide : getSlideReferences().getSldIdArray()) {
	          PackagePart slidePart =
	                getTargetPart(getCorePart().getRelationship(ctSlide.getId2()));
	          
	          for(PackageRelationship rel : slidePart.getRelationshipsByType(OLE_OBJECT_REL_TYPE))
	              embedds.add(getTargetPart(rel)); // TODO: Add this reference to each slide as well
	          
	          for(PackageRelationship rel : slidePart.getRelationshipsByType(PACK_OBJECT_REL_TYPE))
                  embedds.add(getTargetPart(rel));
		}
	}
	public XSLFSlideShow(String file) throws OpenXML4JException, IOException, XmlException {
		this(openPackage(file));
	}
	
	/**
	 * Returns the low level presentation base object
	 */
	public CTPresentation getPresentation() {
		return presentationDoc.getPresentation();
	}
	
	/**
	 * Returns the references from the presentation to its
	 *  slides.
	 * You'll need these to figure out the slide ordering,
	 *  and to get at the actual slides themselves
	 */
	public CTSlideIdList getSlideReferences() {
		return getPresentation().getSldIdLst();
	}
	/**
	 * Returns the references from the presentation to its
	 *  slide masters.
	 * You'll need these to get at the actual slide 
	 *  masters themselves
	 */
	public CTSlideMasterIdList getSlideMasterReferences() {
		return getPresentation().getSldMasterIdLst();
	}
	
	public PackagePart getSlideMasterPart(CTSlideMasterIdListEntry master) throws IOException, XmlException {
		try {
			return getTargetPart(
				getCorePart().getRelationship(master.getId2())
			);
		} catch(InvalidFormatException e) {
			throw new XmlException(e);
		}
	}
	/**
	 * Returns the low level slide master object from
	 *  the supplied slide master reference
	 */
	public CTSlideMaster getSlideMaster(CTSlideMasterIdListEntry master) throws IOException, XmlException {
		PackagePart masterPart = getSlideMasterPart(master);
		SldMasterDocument masterDoc =
			SldMasterDocument.Factory.parse(masterPart.getInputStream());
		return masterDoc.getSldMaster();
	}

	public PackagePart getSlidePart(CTSlideIdListEntry slide) throws IOException, XmlException {
		try {
			return getTargetPart(
					getCorePart().getRelationship(slide.getId2())
			);
		} catch(InvalidFormatException e) {
			throw new XmlException(e);
		}
	}
	/**
	 * Returns the low level slide object from
	 *  the supplied slide reference
	 */
	public CTSlide getSlide(CTSlideIdListEntry slide) throws IOException, XmlException {
		PackagePart slidePart = getSlidePart(slide);
		SldDocument slideDoc =
			SldDocument.Factory.parse(slidePart.getInputStream());
		return slideDoc.getSld();
	}

	/**
	 * Gets the PackagePart of the notes for the
	 *  given slide, or null if there isn't one.
	 */
	public PackagePart getNodesPart(CTSlideIdListEntry parentSlide) throws IOException, XmlException {
		PackageRelationshipCollection notes;
		PackagePart slidePart = getSlidePart(parentSlide);
		
		try {
			notes = slidePart.getRelationshipsByType(NOTES_RELATION_TYPE);
		} catch(InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
		
		if(notes.size() == 0) {
			// No notes for this slide
			return null;
		}
		if(notes.size() > 1) {
			throw new IllegalStateException("Expecting 0 or 1 notes for a slide, but found " + notes.size());
		}
		
		try {
			return getTargetPart(notes.getRelationship(0));
		} catch(InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * Returns the low level notes object for the given
	 *  slide, as found from the supplied slide reference
	 */
	public CTNotesSlide getNotes(CTSlideIdListEntry slide) throws IOException, XmlException {
		PackagePart notesPart = getNodesPart(slide);
		if(notesPart == null)
			return null;
		
		NotesDocument notesDoc =
			NotesDocument.Factory.parse(notesPart.getInputStream());
		
		return notesDoc.getNotes();
	}
	
	/**
	 * Returns all the comments for the given slide
	 */
	public CTCommentList getSlideComments(CTSlideIdListEntry slide) throws IOException, XmlException {
		PackageRelationshipCollection commentRels;
		PackagePart slidePart = getSlidePart(slide);
		
		try {
			commentRels = slidePart.getRelationshipsByType(COMMENT_RELATION_TYPE);
		} catch(InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
		
		if(commentRels.size() == 0) {
			// No comments for this slide
			return null;
		}
		if(commentRels.size() > 1) {
			throw new IllegalStateException("Expecting 0 or 1 comments for a slide, but found " + commentRels.size());
		}
		
		try {
			PackagePart cPart = getTargetPart(
					commentRels.getRelationship(0)
			);
			CmLstDocument commDoc = 
				CmLstDocument.Factory.parse(cPart.getInputStream());
			return commDoc.getCmLst();
		} catch(InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
	}

    /**
     * Get the document's embedded files.
     */
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException {
        return embedds;
    }

}
