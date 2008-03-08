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
package org.apache.poi.hwpf.extractor;

import java.io.File;
import java.io.IOException;

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.HWPFXML;
import org.apache.poi.hwpf.usermodel.HWPFXMLDocument;
import org.apache.poi.hxf.HXFDocument;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Helper class to extract text from an OOXML Word file
 */
public class HXFWordExtractor extends POIXMLTextExtractor {
	private HWPFXMLDocument document;
	
	public HXFWordExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new HWPFXMLDocument(
				new HWPFXML(container)
		));
	}
	public HXFWordExtractor(HWPFXMLDocument document) {
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
			new HXFWordExtractor(HXFDocument.openPackage(
					new File(args[0])
			));
		System.out.println(extractor.getText());
	}

	public String getText() {
		CTBody body = document._getHWPFXML().getDocumentBody();
		StringBuffer text = new StringBuffer();
		
		// Loop over paragraphs
		CTP[] ps = body.getPArray();
		for (int i = 0; i < ps.length; i++) {
			// Loop over ranges
			CTR[] rs = ps[i].getRArray();
			for (int j = 0; j < rs.length; j++) {
				// Loop over text runs
				CTText[] texts = rs[j].getTArray();
				for (int k = 0; k < texts.length; k++) {
					text.append(
							texts[k].getStringValue()
					);
				}
			}
			// New line after each paragraph.
			text.append("\n");
		}
		
		return text.toString();
	}
}
