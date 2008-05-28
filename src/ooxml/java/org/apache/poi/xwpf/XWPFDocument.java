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
package org.apache.poi.xwpf;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import org.apache.poi.POIXMLDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTComment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CommentsDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;

import org.apache.poi.xwpf.usermodel.XWPFHyperlink;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFComment;
import org.apache.poi.xwpf.usermodel.XWPFTable;

/**
 * Experimental class to do low level processing
 *  of docx files.
 * 
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 *  
 * WARNING - APIs expected to change rapidly
 */
public class XWPFDocument extends POIXMLDocument {
	public static final String MAIN_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	public static final String FOOTER_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml";
	public static final String HEADER_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml";
	public static final String STYLES_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml";
	public static final String STYLES_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
	public static final String HYPERLINK_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
	public static final String COMMENT_RELATION_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";
	
	private DocumentDocument wordDoc;
	protected List<XWPFComment> comments;
	protected List<XWPFHyperlink> hyperlinks;
	protected List<XWPFParagraph> paragraphs;
	protected List<XWPFTable> tables;
	
	public XWPFDocument(Package container) throws OpenXML4JException, IOException, XmlException {
		super(container);

		hyperlinks = new LinkedList<XWPFHyperlink>();
		comments = new LinkedList<XWPFComment>();
		paragraphs = new LinkedList<XWPFParagraph>();
		tables= new LinkedList<XWPFTable>();
		
		wordDoc =
			DocumentDocument.Factory.parse(getCorePart().getInputStream());
		
		// filling paragraph list
		for (CTP p : getDocumentBody().getPArray())	{
			paragraphs.add(new XWPFParagraph(p, this));
		}

		// Get the hyperlinks 
		// TODO: make me optional/separated in private function
		try	{
			Iterator <PackageRelationship> relIter = 
				getCorePart().getRelationshipsByType(HYPERLINK_RELATION_TYPE).iterator();
			while(relIter.hasNext()) {
				PackageRelationship rel = relIter.next();
				hyperlinks.add(new XWPFHyperlink(rel.getId(), rel.getTargetURI().toString()));
			}
		} catch(Exception e) {
			throw new OpenXML4JException(e.getLocalizedMessage());
		}

		// Get the comments, if there are any
		PackageRelationshipCollection commentsRel = getCmntRelations();
		if(commentsRel != null && commentsRel.size() > 0) {
			PackagePart commentsPart = getTargetPart(commentsRel.getRelationship(0));
			CommentsDocument cmntdoc = CommentsDocument.Factory.parse(commentsPart.getInputStream());
			for(CTComment ctcomment : cmntdoc.getComments().getCommentArray())
			{
				comments.add(new XWPFComment(ctcomment));
			}
			
			for(CTTbl table : getDocumentBody().getTblArray())
			{
				tables.add(new XWPFTable(table));
			}
		}
		
        this.embedds = new LinkedList<PackagePart>();
        for(PackageRelationship rel : getCorePart().getRelationshipsByType(OLE_OBJECT_REL_TYPE)) {
            embedds.add(getTargetPart(rel));
        }
        
        for(PackageRelationship rel : getCorePart().getRelationshipsByType(PACK_OBJECT_REL_TYPE)) {
            embedds.add(getTargetPart(rel));
        }
	}
	
	/**
	 * Returns the low level document base object
	 */
	public CTDocument1 getDocument() {
		return wordDoc.getDocument();
	}
	
	public Iterator<XWPFParagraph> getParagraphsIterator()
	{
		return paragraphs.iterator();
	}
	
	public Iterator<XWPFTable> getTablesIterator()
	{
		return tables.iterator();
	}
	
	public XWPFHyperlink getHyperlinkByID(String id)
	{
		Iterator<XWPFHyperlink> iter = hyperlinks.iterator();
		while(iter.hasNext())
		{
			XWPFHyperlink link = iter.next();
			if(link.getId().equals(id))
				return link; 
		}
		
		return null;
	}
	
	public XWPFComment getCommentByID(String id)
	{
		Iterator<XWPFComment> iter = comments.iterator();
		while(iter.hasNext())
		{
			XWPFComment comment = iter.next();
			if(comment.getId().equals(id))
				return comment; 
		}
		
		return null;
	}
	
	/**
	 * Returns the low level body of the document
	 */
	public CTBody getDocumentBody() {
		return getDocument().getBody();
	}
	
	/**
	 * Returns the styles object used
	 */
	public CTStyles getStyle() throws XmlException, IOException {
		PackagePart[] parts;
		try {
			parts = getRelatedByType(STYLES_RELATION_TYPE);
		} catch(InvalidFormatException e) {
			throw new IllegalStateException(e);
		}
		if(parts.length != 1) {
			throw new IllegalStateException("Expecting one Styles document part, but found " + parts.length);
		}
		
		StylesDocument sd =
			StylesDocument.Factory.parse(parts[0].getInputStream());
		return sd.getStyles();
	}

	protected PackageRelationshipCollection getCmntRelations() throws InvalidFormatException
	{
		return getCorePart().getRelationshipsByType(COMMENT_RELATION_TYPE);
	}
}

