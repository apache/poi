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

import org.apache.poi.POIXMLDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.DocumentDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;

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
	
	private DocumentDocument wordDoc;
	
	public XWPFDocument(Package container) throws OpenXML4JException, IOException, XmlException {
		super(container);
		
		wordDoc =
			DocumentDocument.Factory.parse(getCorePart().getInputStream());
	}
	
	/**
	 * Returns the low level document base object
	 */
	public CTDocument1 getDocument() {
		return wordDoc.getDocument();
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
	
	/**
	 * Returns all the hyperlink relations for the file.
	 * You'll generally want to get the target to get
	 *  the destination of the hyperlink
	 */
	public PackageRelationshipCollection getHyperlinks() {
		try {
			return getCorePart().getRelationshipsByType(HYPERLINK_RELATION_TYPE); 
		} catch(InvalidFormatException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
	}
}
