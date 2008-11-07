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

import org.apache.poi.xwpf.XWPFDocument;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;

/**
 * High level representation of a ooxml text document.
 */
public class XMLWordDocument {
	private XWPFDocument xwpfXML;
	
	public XMLWordDocument(XWPFDocument xml) {
		this.xwpfXML = xml;
	}
	
	public XWPFDocument _getXWPFXML() {
		return xwpfXML;
	}
	
	public XWPFHyperlink getHyperlinkByID(String id) {
		return xwpfXML.getHyperlinkByID(id);
	}
	public XWPFHyperlink[] getHyperlinks() {
		return xwpfXML.getHyperlinks();
	}
	
	public XWPFComment getCommentByID(String id) {
		return xwpfXML.getCommentByID(id);
	}
	public XWPFComment[] getComments() {
		return xwpfXML.getComments();
	}
	
	public XWPFHeaderFooterPolicy getHeaderFooterPolicy() {
		return xwpfXML.getHeaderFooterPolicy();
	}
}
