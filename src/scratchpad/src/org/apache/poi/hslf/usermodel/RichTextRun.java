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

package org.apache.poi.hslf.usermodel;

import java.awt.Color;

import org.apache.poi.hslf.model.MasterSheet;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Sheet;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.textproperties.BitMaskTextProp;
import org.apache.poi.hslf.model.textproperties.CharFlagsTextProp;
import org.apache.poi.hslf.model.textproperties.ParagraphFlagsTextProp;
import org.apache.poi.hslf.model.textproperties.TextProp;
import org.apache.poi.hslf.model.textproperties.TextPropCollection;
import org.apache.poi.hslf.record.ColorSchemeAtom;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;


/**
 * Represents a run of text, all with the same style
 *
 */
public final class RichTextRun {
	protected POILogger logger = POILogFactory.getLogger(this.getClass());

	/** The TextRun we belong to */
	private TextRun parentRun;
	/** The SlideShow we belong to */
	private SlideShow slideShow;

	/** Where in the parent TextRun we start from */
	private int startPos;

	/** How long a string (in the parent TextRun) we represent */
	private int length;

	private String _fontname;
	/**
	 * Our paragraph and character style.
	 * Note - we may share these styles with other RichTextRuns
	 */
	private TextPropCollection paragraphStyle;
	private TextPropCollection characterStyle;
	private boolean sharingParagraphStyle;
	private boolean sharingCharacterStyle;

	/**
	 * Create a new wrapper around a (currently not)
	 *  rich text string
	 * @param parent
	 * @param startAt
	 * @param len
	 */
	public RichTextRun(TextRun parent, int startAt, int len) {
		this(parent, startAt, len, null, null, false, false);
	}
	/**
	 * Create a new wrapper around a rich text string
	 * @param parent The parent TextRun
	 * @param startAt The start position of this run
	 * @param len The length of this run
	 * @param pStyle The paragraph style property collection
	 * @param cStyle The character style property collection
	 * @param pShared The paragraph styles are shared with other runs
	 * @param cShared The character styles are shared with other runs
	 */
	public RichTextRun(TextRun parent, int startAt, int len,
	TextPropCollection pStyle,  TextPropCollection cStyle,
	boolean pShared, boolean cShared) {
		parentRun = parent;
		startPos = startAt;
		length = len;
		paragraphStyle = pStyle;
		characterStyle = cStyle;
		sharingParagraphStyle = pShared;
		sharingCharacterStyle = cShared;
	}

	/**
	 * Supply (normally default) textprops, and if they're shared,
	 *  when a run gets them
	 */
	public void supplyTextProps(TextPropCollection pStyle,  TextPropCollection cStyle, boolean pShared, boolean cShared) {
		if(paragraphStyle != null || characterStyle != null) {
			throw new IllegalStateException("Can't call supplyTextProps if run already has some");
		}
		paragraphStyle = pStyle;
		characterStyle = cStyle;
		sharingParagraphStyle = pShared;
		sharingCharacterStyle = cShared;
	}
	/**
	 * Supply the SlideShow we belong to
	 */
	public void supplySlideShow(SlideShow ss) {
		slideShow = ss;
		if (_fontname != null) {
			setFontName(_fontname);
			_fontname = null;
		}
	}

	/**
	 * Get the length of the text
	 */
	public int getLength() {
		return length;
	}

	/**
	 * The beginning index, inclusive.
	 *
	 * @return the beginning index, inclusive.
	 */
	public int getStartIndex(){
		return startPos;
	}

	/**
	 *  The ending index, exclusive.
	 *
	 * @return the ending index, exclusive.
	 */
	public int getEndIndex(){
		return startPos + length;
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
		String s = parentRun.normalize(text);
		setRawText(s);
	}

	/**
	 * Change the text
	 */
	public void setRawText(String text) {
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
		return getFlag(true, index);
	}

	private boolean getFlag(boolean isCharacter, int index) {
		TextPropCollection props;
		String propname;
		if (isCharacter){
			props = characterStyle;
			propname = CharFlagsTextProp.NAME;
		} else {
			props = paragraphStyle;
			propname = ParagraphFlagsTextProp.NAME;
		}

		BitMaskTextProp prop = null;
		if (props != null){
			prop = (BitMaskTextProp)props.findByName(propname);
		}
		if (prop == null){
			Sheet sheet = parentRun.getSheet();
			if(sheet != null){
				int txtype = parentRun.getRunType();
				MasterSheet master = sheet.getMasterSheet();
				if (master != null){
					prop = (BitMaskTextProp)master.getStyleAttribute(txtype, getIndentLevel(), propname, isCharacter);
				}
			} else {
				logger.log(POILogger.WARN, "MasterSheet is not available");
			}
		}

		return prop == null ? false : prop.getSubValue(index);
	}

	/**
	 * Set the value of the given flag in the CharFlagsTextProp, adding
	 *  it if required.
	 */
	private void setCharFlagsTextPropVal(int index, boolean value) {
		if(getFlag(true, index) != value) setFlag(true, index, value);
	}

	public void setFlag(boolean isCharacter, int index, boolean value) {
		TextPropCollection props;
		String propname;
		if (isCharacter){
			props = characterStyle;
			propname = CharFlagsTextProp.NAME;
		} else {
			props = paragraphStyle;
			propname = ParagraphFlagsTextProp.NAME;
		}

		// Ensure we have the StyleTextProp atom we're going to need
		if(props == null) {
			parentRun.ensureStyleAtomPresent();
			props = isCharacter ? characterStyle : paragraphStyle;
		}

		BitMaskTextProp prop = (BitMaskTextProp) fetchOrAddTextProp(props, propname);
		prop.setSubValue(value,index);
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
		TextProp prop = null;
		if (characterStyle != null){
			prop = characterStyle.findByName(propName);
		}

		if (prop == null){
			Sheet sheet = parentRun.getSheet();
			int txtype = parentRun.getRunType();
			MasterSheet master = sheet.getMasterSheet();
			if (master != null)
				prop = master.getStyleAttribute(txtype, getIndentLevel(), propName, true);
		}
		return prop == null ? -1 : prop.getValue();
	}
	/**
	 * Fetch the value of the given Paragraph related TextProp.
	 * Returns -1 if that TextProp isn't present.
	 * If the TextProp isn't present, the value from the appropriate
	 *  Master Sheet will apply.
	 */
	private int getParaTextPropVal(String propName) {
		TextProp prop = null;
		boolean hardAttribute = false;
		if (paragraphStyle != null){
			prop = paragraphStyle.findByName(propName);

			BitMaskTextProp maskProp = (BitMaskTextProp)paragraphStyle.findByName(ParagraphFlagsTextProp.NAME);
			hardAttribute = maskProp != null && maskProp.getValue() == 0;
		}
		if (prop == null && !hardAttribute){
			Sheet sheet = parentRun.getSheet();
			int txtype = parentRun.getRunType();
			MasterSheet master = sheet.getMasterSheet();
			if (master != null)
				prop = master.getStyleAttribute(txtype, getIndentLevel(), propName, false);
		}

		return prop == null ? -1 : prop.getValue();
	}

	/**
	 * Sets the value of the given Character TextProp, add if required
	 * @param propName The name of the Character TextProp
	 * @param val The value to set for the TextProp
	 */
	public void setParaTextPropVal(String propName, int val) {
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
	public void setCharTextPropVal(String propName, int val) {
		// Ensure we have the StyleTextProp atom we're going to need
		if(characterStyle == null) {
			parentRun.ensureStyleAtomPresent();
			// characterStyle will now be defined
		}

		TextProp tp = fetchOrAddTextProp(characterStyle, propName);
		tp.setValue(val);
	}


	// --------------- Friendly getters / setters on rich text properties -------

	/**
	 * Is the text bold?
	 */
	public boolean isBold() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX);
	}

	/**
	 * Is the text bold?
	 */
	public void setBold(boolean bold) {
		setCharFlagsTextPropVal(CharFlagsTextProp.BOLD_IDX, bold);
	}

	/**
	 * Is the text italic?
	 */
	public boolean isItalic() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX);
	}

	/**
	 * Is the text italic?
	 */
	public void setItalic(boolean italic) {
		setCharFlagsTextPropVal(CharFlagsTextProp.ITALIC_IDX, italic);
	}

	/**
	 * Is the text underlined?
	 */
	public boolean isUnderlined() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX);
	}

	/**
	 * Is the text underlined?
	 */
	public void setUnderlined(boolean underlined) {
		setCharFlagsTextPropVal(CharFlagsTextProp.UNDERLINE_IDX, underlined);
	}

	/**
	 * Does the text have a shadow?
	 */
	public boolean isShadowed() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.SHADOW_IDX);
	}

	/**
	 * Does the text have a shadow?
	 */
	public void setShadowed(boolean flag) {
		setCharFlagsTextPropVal(CharFlagsTextProp.SHADOW_IDX, flag);
	}

	/**
	 * Is this text embossed?
	 */
	 public boolean isEmbossed() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.RELIEF_IDX);
	}

	/**
	 * Is this text embossed?
	 */
	 public void setEmbossed(boolean flag) {
		setCharFlagsTextPropVal(CharFlagsTextProp.RELIEF_IDX, flag);
	}

	/**
	 * Gets the strikethrough flag
	 */
	public boolean isStrikethrough() {
		return isCharFlagsTextPropVal(CharFlagsTextProp.STRIKETHROUGH_IDX);
	}

	/**
	 * Sets the strikethrough flag
	 */
	public void setStrikethrough(boolean flag) {
		setCharFlagsTextPropVal(CharFlagsTextProp.STRIKETHROUGH_IDX, flag);
	}

	/**
	 * Gets the subscript/superscript option
	 *
	 * @return the percentage of the font size. If the value is positive, it is superscript, otherwise it is subscript
	 */
	public int getSuperscript() {
		int val = getCharTextPropVal("superscript");
		return val == -1 ? 0 : val;
	}

	/**
	 * Sets the subscript/superscript option
	 *
	 * @param val the percentage of the font size. If the value is positive, it is superscript, otherwise it is subscript
	 */
	public void setSuperscript(int val) {
		setCharTextPropVal("superscript", val);
	}

	/**
	 * Gets the font size
	 */
	public int getFontSize() {
		return getCharTextPropVal("font.size");
	}


	/**
	 * Sets the font size
	 */
	public void setFontSize(int fontSize) {
		setCharTextPropVal("font.size", fontSize);
	}

	/**
	 * Gets the font index
	 */
	public int getFontIndex() {
		return getCharTextPropVal("font.index");
	}

	/**
	 * Sets the font index
	 */
	public void setFontIndex(int idx) {
		setCharTextPropVal("font.index", idx);
	}


	/**
	 * Sets the font name to use
	 */
	public void setFontName(String fontName) {
		if (slideShow == null) {
			//we can't set font since slideshow is not assigned yet
			_fontname = fontName;
		} else {
			// Get the index for this font (adding if needed)
			int fontIdx = slideShow.getFontCollection().addFont(fontName);
			setCharTextPropVal("font.index", fontIdx);
		}
	}

	/**
	 * Gets the font name
	 */
	public String getFontName() {
		if (slideShow == null) {
			return _fontname;
		}
		int fontIdx = getCharTextPropVal("font.index");
		if(fontIdx == -1) { return null; }
		return slideShow.getFontCollection().getFontWithId(fontIdx);
	}

	/**
	 * @return font color as RGB value
	 * @see java.awt.Color
	 */
	public Color getFontColor() {
		int rgb = getCharTextPropVal("font.color");

		int cidx = rgb >> 24;
		if (rgb % 0x1000000 == 0){
			ColorSchemeAtom ca = parentRun.getSheet().getColorScheme();
			if(cidx >= 0 && cidx <= 7) rgb = ca.getColor(cidx);
		}
		Color tmp = new Color(rgb, true);
		return new Color(tmp.getBlue(), tmp.getGreen(), tmp.getRed());
	}

	/**
	 * Sets color of the text, as a int bgr.
	 * (PowerPoint stores as BlueGreenRed, not the more
	 *  usual RedGreenBlue)
	 * @see java.awt.Color
	 */
	public void setFontColor(int bgr) {
		setCharTextPropVal("font.color", bgr);
	}

	/**
	 * Sets color of the text, as a java.awt.Color
	 */
	public void setFontColor(Color color) {
		// In PowerPont RGB bytes are swapped, as BGR
		int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 254).getRGB();
		setFontColor(rgb);
	}

	/**
	 * Sets the type of horizontal alignment for the text.
	 * One of the <code>Align*</code> constants defined in the <code>TextBox</code> class.
	 *
	 * @param align - the type of alignment
	 */
	public void setAlignment(int align) {
		setParaTextPropVal("alignment", align);
	}
	/**
	 * Returns the type of horizontal alignment for the text.
	 * One of the <code>Align*</code> constants defined in the <code>TextBox</class> class.
	 *
	 * @return the type of alignment
	 */
	public int getAlignment() {
		return getParaTextPropVal("alignment");
	}

	/**
	 *
	 * @return indentation level
	 */
	public int getIndentLevel() {
		return paragraphStyle == null ? 0 : paragraphStyle.getReservedField();
	}

	/**
	 * Sets indentation level
	 *
	 * @param level indentation level. Must be in the range [0, 4]
	 */
	public void setIndentLevel(int level) {
		if(paragraphStyle != null ) paragraphStyle.setReservedField((short)level);
	}

	/**
	 * Sets whether this rich text run has bullets
	 */
	public void setBullet(boolean flag) {
		setFlag(false, ParagraphFlagsTextProp.BULLET_IDX, flag);
	}

	/**
	 * Returns whether this rich text run has bullets
	 */
	public boolean isBullet() {
		return getFlag(false, ParagraphFlagsTextProp.BULLET_IDX);
	}

	/**
	 * Returns whether this rich text run has bullets
	 */
	public boolean isBulletHard() {
		return getFlag(false, ParagraphFlagsTextProp.BULLET_IDX);
	}

	/**
	 * Sets the bullet character
	 */
	public void setBulletChar(char c) {
		setParaTextPropVal("bullet.char", c);
	}

	/**
	 * Returns the bullet character
	 */
	public char getBulletChar() {
		return (char)getParaTextPropVal("bullet.char");
	}

	/**
	 * Sets the bullet offset
	 */
	public void setBulletOffset(int offset) {
		setParaTextPropVal("bullet.offset", offset*Shape.MASTER_DPI/Shape.POINT_DPI);
	}

	/**
	 * Returns the bullet offset
	 */
	public int getBulletOffset() {
		return getParaTextPropVal("bullet.offset")*Shape.POINT_DPI/Shape.MASTER_DPI;
	}

	/**
	 * Sets the text offset
	 */
	public void setTextOffset(int offset) {
		setParaTextPropVal("text.offset", offset*Shape.MASTER_DPI/Shape.POINT_DPI);
	}

	/**
	 * Returns the text offset
	 */
	public int getTextOffset() {
		return getParaTextPropVal("text.offset")*Shape.POINT_DPI/Shape.MASTER_DPI;
	}

	/**
	 * Sets the bullet size
	 */
	public void setBulletSize(int size) {
		setParaTextPropVal("bullet.size", size);
	}

	/**
	 * Returns the bullet size
	 */
	public int getBulletSize() {
		return getParaTextPropVal("bullet.size");
	}

	/**
	 * Sets the bullet color
	 */
	public void setBulletColor(Color color) {
		int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 254).getRGB();
		setParaTextPropVal("bullet.color", rgb);
	}

	/**
	 * Returns the bullet color
	 */
	public Color getBulletColor() {
		int rgb = getParaTextPropVal("bullet.color");
		if(rgb == -1) return getFontColor();

		int cidx = rgb >> 24;
		if (rgb % 0x1000000 == 0){
			ColorSchemeAtom ca = parentRun.getSheet().getColorScheme();
			if(cidx >= 0 && cidx <= 7) rgb = ca.getColor(cidx);
		}
		Color tmp = new Color(rgb, true);
		return new Color(tmp.getBlue(), tmp.getGreen(), tmp.getRed());
	}

	/**
	 * Sets the bullet font
	 */
	public void setBulletFont(int idx) {
		setParaTextPropVal("bullet.font", idx);
		setFlag(false, ParagraphFlagsTextProp.BULLET_HARDFONT_IDX, true);
	}

	/**
	 * Returns the bullet font
	 */
	public int getBulletFont() {
		return getParaTextPropVal("bullet.font");
	}

	/**
	 * Sets the line spacing.
	 * <p>
	 * If linespacing >= 0, then linespacing is a percentage of normal line height.
	 * If linespacing < 0, the absolute value of linespacing is the spacing in master coordinates.
	 * </p>
	 */
	public void setLineSpacing(int val) {
		setParaTextPropVal("linespacing", val);
	}

	/**
	 * Returns the line spacing
	 * <p>
	 * If linespacing >= 0, then linespacing is a percentage of normal line height.
	 * If linespacing < 0, the absolute value of linespacing is the spacing in master coordinates.
	 * </p>
	 *
	 * @return the spacing between lines
	 */
	public int getLineSpacing() {
		int val = getParaTextPropVal("linespacing");
		return val == -1 ? 0 : val;
	}

	/**
	 * Sets spacing before a paragraph.
	 * <p>
	 * If spacebefore >= 0, then spacebefore is a percentage of normal line height.
	 * If spacebefore < 0, the absolute value of spacebefore is the spacing in master coordinates.
	 * </p>
	 */
	public void setSpaceBefore(int val) {
		setParaTextPropVal("spacebefore", val);
	}

	/**
	 * Returns spacing before a paragraph
	 * <p>
	 * If spacebefore >= 0, then spacebefore is a percentage of normal line height.
	 * If spacebefore < 0, the absolute value of spacebefore is the spacing in master coordinates.
	 * </p>
	 *
	 * @return the spacing before a paragraph
	 */
	public int getSpaceBefore() {
		int val = getParaTextPropVal("spacebefore");
		return val == -1 ? 0 : val;
	}

	/**
	 * Sets spacing after a paragraph.
	 * <p>
	 * If spaceafter >= 0, then spaceafter is a percentage of normal line height.
	 * If spaceafter < 0, the absolute value of spaceafter is the spacing in master coordinates.
	 * </p>
	 */
	public void setSpaceAfter(int val) {
		setParaTextPropVal("spaceafter", val);
	}

	/**
	 * Returns spacing after a paragraph
	 * <p>
	 * If spaceafter >= 0, then spaceafter is a percentage of normal line height.
	 * If spaceafter < 0, the absolute value of spaceafter is the spacing in master coordinates.
	 * </p>
	 *
	 * @return the spacing before a paragraph
	 */
	public int getSpaceAfter() {
		int val = getParaTextPropVal("spaceafter");
		return val == -1 ? 0 : val;
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
	/**
	 * Internal Use Only - are the Paragraph styles shared?
	 */
	public boolean _isParagraphStyleShared() { return sharingParagraphStyle; }
	/**
	 * Internal Use Only - are the Character styles shared?
	 */
	public boolean _isCharacterStyleShared() { return sharingCharacterStyle; }
}
