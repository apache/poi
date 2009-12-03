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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.util.Internal;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;

/**
 * Parent of XWPF headers and footers
 */
public abstract class XWPFHeaderFooter extends POIXMLDocumentPart{
	protected CTHdrFtr headerFooter;
	
	protected XWPFHeaderFooter(CTHdrFtr hdrFtr) {
		headerFooter = hdrFtr;
	}
	protected XWPFHeaderFooter() {
		headerFooter = CTHdrFtr.Factory.newInstance();
	}

	public XWPFHeaderFooter(PackagePart part, PackageRelationship rel) throws IOException {
		super(part, rel);
	}
	
    @Internal
	public CTHdrFtr _getHdrFtr() {
		return headerFooter;
	}

	/**
	 * Returns the paragraph(s) that holds
	 *  the text of the header or footer.
	 * Normally there is only the one paragraph, but
	 *  there could be more in certain cases, or 
	 *  a table.
	 */
	public XWPFParagraph[] getParagraphs() {
		XWPFParagraph[] paras = 
			new XWPFParagraph[headerFooter.getPArray().length];
		for(int i=0; i<paras.length; i++) {
			paras[i] = new XWPFParagraph(
					headerFooter.getPArray(i), null
			);
		}
		return paras;
	}
	/**
	 * Return the table(s) that holds the text
	 *  of the header or footer, for complex cases
	 *  where a paragraph isn't used.
	 * Normally there's just one paragraph, but some
	 *  complex headers/footers have a table or two
	 *  in addition. 
	 */
	public XWPFTable[] getTables() {
		XWPFTable[] tables = 
			new XWPFTable[headerFooter.getTblArray().length];
		for(int i=0; i<tables.length; i++) {
			tables[i] = new XWPFTable(
                    null,
                    headerFooter.getTblArray(i)
			);
		}
		return tables;
	}
	
	/**
	 * Returns the textual content of the header/footer,
	 *  by flattening out the text of its paragraph(s)
	 */
	public String getText() {
		StringBuffer t = new StringBuffer();
		
		XWPFParagraph[] paras = getParagraphs();
		for(int i=0; i<paras.length; i++) {
			if(! paras[i].isEmpty()) {
				String text = paras[i].getText();
				if(text != null && text.length() > 0) {
					t.append(text);
					t.append('\n');
				}
			}
		}
		
		XWPFTable[] tables = getTables();
		for(int i=0; i<tables.length; i++) {
			String text = tables[i].getText();
			if(text != null && text.length() > 0) {
				t.append(text);
				t.append('\n');
			}
		}
		
		return t.toString(); 
	}
}
