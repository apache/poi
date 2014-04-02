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
package org.apache.poi.xwpf.model;

import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Decorator class for XWPFParagraph allowing to add hyperlinks 
 *  found in paragraph to its text.
 *  
 * Note - adds the hyperlink at the end, not in the right place...
 *  
 * @deprecated Use {@link XWPFHyperlinkRun} instead
 */
@Deprecated
public class XWPFHyperlinkDecorator extends XWPFParagraphDecorator {
	private StringBuffer hyperlinkText;
	
	/**
	 * @param nextDecorator The next decorator to use
	 * @param outputHyperlinkUrls Should we output the links too, or just the link text?
	 */
	public XWPFHyperlinkDecorator(XWPFParagraphDecorator nextDecorator, boolean outputHyperlinkUrls) {
		this(nextDecorator.paragraph, nextDecorator, outputHyperlinkUrls);
	}
	
	/**
	 * @param prgrph The paragraph of text to work on
	 * @param outputHyperlinkUrls Should we output the links too, or just the link text?
	 */
	public XWPFHyperlinkDecorator(XWPFParagraph prgrph, XWPFParagraphDecorator nextDecorator, boolean outputHyperlinkUrls) {
		super(prgrph, nextDecorator);
		
		hyperlinkText = new StringBuffer();
		
		// loop over hyperlink anchors
		for(CTHyperlink link : paragraph.getCTP().getHyperlinkList()){
			for (CTR r : link.getRList()) {
				// Loop over text runs
				for (CTText text : r.getTList()){
					hyperlinkText.append(text.getStringValue());
				}
			}
			if(outputHyperlinkUrls && paragraph.getDocument().getHyperlinkByID(link.getId()) != null) {
				hyperlinkText.append(" <"+paragraph.getDocument().getHyperlinkByID(link.getId()).getURL()+">");
			}
		}
	}
	
	public String getText()
	{
		return super.getText() + hyperlinkText;
	}
}
