
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        


package org.apache.poi.hslf.usermodel;

import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.record.StyleTextPropAtom.TextPropCollection;

/**
 * Represents a run of text, all with the same style
 * 
 * TODO: get access to the font/character properties
 *
 * @author Nick Burch
 */

public class RichTextRun
{
	/** The TextRun we belong to */
	private TextRun parentRun;
	
	/** Where in the parent TextRun we start from */
	private int startPos;
	
	/** How long a string (in the parent TextRun) we represent */
	private int length;
	
	/** 
	 * Our paragraph and character style.
	 * Note - we may share the Paragraph style with another RichTextRun
	 *  (the Character style should be ours alone) 
	 */
	private TextPropCollection paragraphStyle;
	private TextPropCollection characterStyle;
	
	/**
	 * Create a new wrapper around a (currently not)
	 *  rich text string
	 * @param parent
	 * @param startAt
	 * @param len
	 */
	public RichTextRun(TextRun parent, int startAt, int len) {
		this(parent, startAt, len, null, null);
	}
	/**
	 * Create a new wrapper around a rich text string
	 * @param parent The parent TextRun
	 * @param startAt The start position of this run
	 * @param len The length of this run
	 * @param pStyle The paragraph style property collection
	 * @param cStyle The character style property collection
	 */
	public RichTextRun(TextRun parent, int startAt, int len, 
	TextPropCollection pStyle,  TextPropCollection cStyle) {
		parentRun = parent;
		startPos = startAt;
		length = len;
		paragraphStyle = pStyle;
		characterStyle = cStyle;
	}

	/**
	 * Supply (normally default) textprops, when a run gets them 
	 */
	public void supplyTextProps(TextPropCollection pStyle,  TextPropCollection cStyle) {
		if(paragraphStyle != null || characterStyle != null) {
			throw new IllegalStateException("Can't call supplyTextProps if run already has some");
		}
		paragraphStyle = pStyle;
		characterStyle = cStyle;
	}
	
	/**
	 * Fetch the text, in output suitable form
	 */
	public String getText() {
		return parentRun.getText().substring(startPos, startPos+length);
	}
	/**
	 * Fetch the text, in raw storage form
	 */
	public String getRawText() {
		return parentRun.getRawText().substring(startPos, startPos+length);
	}
	
	/**
	 * Change the text
	 */
	public void setText(String text) {
		length = text.length();
		parentRun.changeTextInRichTextRun(this,text);
	}
	
	/**
	 * Tells the RichTextRun its new position in the parent TextRun
	 * @param startAt
	 */
	public void updateStartPosition(int startAt) {
		startPos = startAt;
	}
	
	
	
	/**
	 * Unit Testing Only - get the underlying paragraph style collection.
	 * For normal use, use the friendly setters and getters 
	 */
	public TextPropCollection _getRawParagraphStyle() { return paragraphStyle; }
	/**
	 * Unit Testing Only - get the underlying character style collection.
	 * For normal use, use the friendly setters and getters 
	 */
	public TextPropCollection _getRawCharacterStyle() { return characterStyle; }
}
