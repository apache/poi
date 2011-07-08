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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFootnotes;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FootnotesDocument;

/**
 * Looks after the collection of Footnotes for a document
 *  
 * @author Mike McEuen (mceuen@hp.com)
 */
public class XWPFFootnotes extends POIXMLDocumentPart {
   private List<XWPFFootnote> listFootnote = new ArrayList<XWPFFootnote>();
   private CTFootnotes ctFootnotes;

	protected XWPFDocument document;

	/**
	 * Construct XWPFFootnotes from a package part
	 *
	 * @param part the package part holding the data of the footnotes,
	 * @param rel  the package relationship of type "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes"
	 */
	public XWPFFootnotes(PackagePart part, PackageRelationship rel) throws IOException, OpenXML4JException{
		super(part, rel);
	}

	/**
	 * Construct XWPFFootnotes from scratch for a new document.
	 */
	public XWPFFootnotes() {
	}

	/**
	 * Read document
	 */
	@Override
	protected void onDocumentRead () throws IOException {
	   FootnotesDocument notesDoc;
	   try {
	      InputStream is = getPackagePart().getInputStream();
	      notesDoc = FootnotesDocument.Factory.parse(is);
	      ctFootnotes = notesDoc.getFootnotes();
	   } catch (XmlException e) {
	      throw new POIXMLException();
	   }
	   
	   //get any Footnote
	   for(CTFtnEdn note : ctFootnotes.getFootnoteList()) {
	      listFootnote.add(new XWPFFootnote(note, this));
	   }
	}
	
	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTFootnotes.type.getName().getNamespaceURI(), "footnotes"));
		Map<String,String> map = new HashMap<String,String>();
		map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
		map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
		xmlOptions.setSaveSuggestedPrefixes(map);
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		ctFootnotes.save(out, xmlOptions);
		out.close();
	}

	public List<XWPFFootnote> getFootnotesList() {
		return listFootnote;
	}

	public XWPFFootnote getFootnoteById(int id) {
		for(XWPFFootnote note : listFootnote) {
			if(note.getCTFtnEdn().getId().intValue() == id)
				return note;
		}
		return null;
	}
	
	/**
	 * Sets the ctFootnotes
	 * @param footnotes
	 */
	public void setFootnotes(CTFootnotes footnotes) {
	   ctFootnotes = footnotes;
	}

	/**
	 * add an XWPFFootnote to the document
	 * @param footnote
	 * @throws IOException		 
	 */
	public void addFootnote(XWPFFootnote footnote){
	   listFootnote.add(footnote);
	   ctFootnotes.addNewFootnote().set(footnote.getCTFtnEdn());
	}

	/**
	 * add a footnote to the document
	 * @param footnote
	 * @throws IOException		 
	 */
	public XWPFFootnote addFootnote(CTFtnEdn note){
	   CTFtnEdn newNote = ctFootnotes.addNewFootnote();
	   newNote.set(note);
	   XWPFFootnote xNote = new XWPFFootnote(newNote, this);
	   listFootnote.add(xNote);
	   return xNote;
	}

	public void setXWPFDocument(XWPFDocument doc) {
	   document = doc;
	}

	/**
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
	 */
	public XWPFDocument getXWPFDocument() {
	   if ( document != null) {
	      return document;
	   } else {
	      return (XWPFDocument)getParent();
	   }
	}
}//end class

