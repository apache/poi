
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
import org.apache.poi.hslf.record.StyleTextPropAtom.CharFlagsTextProp;
import org.apache.poi.hslf.record.StyleTextPropAtom.TextProp;
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
	/** The SlideShow we belong to */
	private SlideShow slideShow;
	
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
	 * Supply the SlideShow we belong to
	 */
	protected void supplySlideShow(SlideShow ss) {
		slideShow = ss;
	}
	
	/**
	 * Get the length of the text
	 */
	public int getLength() {
		return length;
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
	
	
	// --------------- Internal helpers on rich text properties -------
	
	/**
	 * Fetch the value of the given flag in the CharFlagsTextProp.
	 * Returns false if the CharFlagsTextProp isn't present, since the
	 *  text property won't be set if there's no CharFlagsTextProp.
	 */
	private boolean isCharFlagsTextPropVal(int index) {
		if(characterStyle == null) { return false; }
		
		CharFlagsTextProp cftp = (CharFlagsTextProp)
			characterStyle.findByName("char_flags");
		
		if(cftp == null) { return false; }
		return cftp.getSubValue(index);
	}
	/**
	 * Set the value of the given flag in the CharFlagsTextProp, adding
	 *  it if required. 
	 */
	private void setCharFlagsTextPropVal(int index, boolean value) {
		// Ensure we have the StyleTextProp atom we're going to need
		if(characterStyle == null) {
			parentRun.ensureStyleAtomPresent();
			// characterStyle will now be defined
		}
		
		CharFlagsTextProp cftp = (CharFlagsTextProp)
			fetchOrAddTextProp(characterStyle, "char_flags");
		cftp.setSubValue(value,index);
	}
	
	/**
	 * Returns the named TextProp, either by fetching it (if it exists) or adding it
	 *  (if it didn't)
	 * @param textPropCol The TextPropCollection to fetch from / add into
	 * @param textPropName The name of the TextProp to fetch/add
	 */
	private TextProp fetchOrAddTextProp(TextPropCollection textPropCol, String textPropName) {
		// Fetch / Add the TextProp
		TextProp tp = textPropCol.findByName(textPropName);
		if(tp == null) {
			tp = textPropCol.addWithName(textPropName);
		}
		return tp;
	}
	
	/**
	 * Fetch the value of the given Character related TextProp. 
	 * Returns -1 if that TextProp isn't present. 
	 * If the TextProp isn't present, the value from the appropriate 
	 *  Master Sheet will apply.
	 */
	private int getCharTextPropVal(String propName) {
		if(characterStyle == null) { return -1; }
		
		TextProp cTextProp = characterStyle.findByName(propName);
		if(cTextProp == null) { return -1; }
		return cTextProp.getValue();
	}
	/**
	 * Fetch the value of the given Paragraph related TextProp. 
	 * Returns -1 if that TextProp isn't present. 
	 * If the TextProp isn't present, the value from the appropriate 
	 *  Master Sheet will apply.
	 */
	private int getParaTextPropVal(String propName) {
		if(paragraphStyle == null) { return -1; }
		
		TextProp pTextProp = paragraphStyle.findByName(propName);
		if(pTextProp == null) { return -1; }
		return pTextProp.getValue();
	}
	
	/**
	 * Sets the value of the given Character TextProp, add if required
	 * @param propName The name of the Character TextProp
	 * @param val The value to set for the TextProp
	 */
	private void setParaTextPropVal(String propName, int val) {
		// Ensure we have the StyleTextProp atom we're going to need
		if(paragraphStyle == null) {
			parentRun.ensureStyleAtomPresent();
			// paragraphStyle will now be defined
		}
		
		TextProp tp = fetchOrAddTextProp(paragraphStyle, propName);
		tp.setValue(val);
	}
	/**
	 * Sets the value of the given Paragraph TextProp, add if required
	 * @param propName The name of the Paragraph TextProp
	 * @param val The value to set for the TextProp
	 */
	private void setCharTextPropVal(String propName, int val) {
		// Ensure we have the StyleTextProp atom we're going to need
		if(characterStyle == null) {
			parentRun.ensureStyleAtomPresent();
			// characterStyle will now be defined
		}
		
		TextProp tp = fetchOrAddTextProp(characterStyle, propName);
		tp.setValue(val);
	}
	
	
	// --------------- Friendly getters / setters on rich text properties -------
	
	public boolean isBold() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX);
	}
	public void setBold(boolean bold) {
		setCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX, bold);
	}
	
	public boolean isItalic() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX);
	}
	public void setItalic(boolean italic) {
		setCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX, italic);
	}
	
	public boolean isUnderlined() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX);
	}
	public void setUnderlined(boolean underlined) {
		setCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX, underlined);
	}
	
	public int getFontSize() {
		return getCharTextPropVal("font.size");
	}
	public void setFontSize(int fontSize) {
		setCharTextPropVal("font.size", fontSize);
	}
	
	public void setFontName(String fontName) {
		// Get the index for this font (adding if needed)
		int fontIdx = slideShow.getFontCollection().addFont(fontName);
		setCharTextPropVal("font.index", fontIdx);
	}
	public String getFontName() {
		int fontIdx = getCharTextPropVal("font.index");
		if(fontIdx == -1) { return null; }
		return slideShow.getFontCollection().getFontWithId(fontIdx);
	}
	
	
	// --------------- Internal HSLF methods, not intended for end-user use! -------
	
	/**
	 * Internal Use Only - get the underlying paragraph style collection.
	 * For normal use, use the friendly setters and getters 
	 */
	public TextPropCollection _getRawParagraphStyle() { return paragraphStyle; }
	/**
	 * Internal Use Only - get the underlying character style collection.
	 * For normal use, use the friendly setters and getters 
	 */
	public TextPropCollection _getRawCharacterStyle() { return characterStyle; }
}
