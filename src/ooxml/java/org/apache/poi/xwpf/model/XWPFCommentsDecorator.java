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

import org.apache.poi.xwpf.usermodel.XWPFComment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;

/**
 * Decorator class for XWPFParagraph allowing to add comments 
 * found in paragraph to its text
 *
 * @author Yury Batrakov (batrakov at gmail.com)
 * 
 */
public class XWPFCommentsDecorator extends XWPFParagraphDecorator {
	private StringBuffer commentText;
	
	public XWPFCommentsDecorator(XWPFParagraphDecorator nextDecorator) {
		this(nextDecorator.paragraph, nextDecorator);
	}
	public XWPFCommentsDecorator(XWPFParagraph paragraph, XWPFParagraphDecorator nextDecorator) {
		super(paragraph, nextDecorator);

		XWPFComment comment;
		commentText = new StringBuffer();

		for(CTMarkupRange anchor : paragraph.getCTP().getCommentRangeStartArray())
		{
			if((comment = paragraph.getDocument().getCommentByID(anchor.getId().toString())) != null)
				commentText.append("\tComment by " + comment.getAuthor()+": "+comment.getText());
		}
	}

	public String getText() {
		return super.getText() + commentText;
	}
}
