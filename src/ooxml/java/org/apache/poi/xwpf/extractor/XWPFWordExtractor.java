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
package org.apache.poi.xwpf.extractor;

import java.io.IOException;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.xwpf.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageRelationship;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Helper class to extract text from an OOXML Word file
 */
public class XWPFWordExtractor extends POIXMLTextExtractor {
	private XWPFDocument document;
	private boolean fetchHyperlinks = false;
	
	public XWPFWordExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new XWPFDocument(container));
	}
	public XWPFWordExtractor(XWPFDocument document) {
		super(document);
		this.document = document;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFWordExtractor <filename.xlsx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new XWPFWordExtractor(POIXMLDocument.openPackage(
					args[0]
			));
		System.out.println(extractor.getText());
	}
	
	/**
	 * Should we also fetch the hyperlinks, when fetching 
	 *  the text content? Default is to only output the
	 *  hyperlink label, and not the contents
	 */
	public void setFetchHyperlinks(boolean fetch) {
		fetchHyperlinks = fetch;
	}

	public String getText() {
		CTBody body = document.getDocumentBody();
		StringBuffer text = new StringBuffer();
		
		// Loop over paragraphs
		CTP[] ps = body.getPArray();
		for (int i = 0; i < ps.length; i++) {
			// Loop over ranges and hyperlinks
			// TODO - properly intersperce ranges and hyperlinks
			CTR[] rs = ps[i].getRArray();
			for(int j = 0; j < rs.length; j++) {
				// Loop over text runs
				CTText[] texts = rs[j].getTArray();
				for (int k = 0; k < texts.length; k++) {
					text.append(
							texts[k].getStringValue()
					);
				}
			}
			
			CTHyperlink[] hls =  ps[i].getHyperlinkArray();
			for(CTHyperlink hl : hls) {
				for(CTR r : hl.getRArray()) {
					for(CTText txt : r.getTArray()) {
						text.append(txt.getStringValue());
					}
				}
				if(fetchHyperlinks) {
					String id = hl.getId();
					if(id != null) {
						PackageRelationship hlRel =
							document.getHyperlinks().getRelationshipByID(id);
						if(hlRel != null) {
							text.append(" <" + hlRel.getTargetURI().toString() + ">");
						}
					}
				}
			}
			
			// New line after each paragraph.
			text.append("\n");
		}
		
		return text.toString();
	}
}
