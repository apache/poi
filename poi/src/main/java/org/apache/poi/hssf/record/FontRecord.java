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

package org.apache.poi.hssf.record;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/** Describes a font in the workbook */
public final class FontRecord extends StandardRecord {
	// docs are wrong (0x231 Microsoft Support site article Q184647)
	public static final short sid                = 0x0031;
	public static final short SS_NONE            = 0;
	public static final short SS_SUPER           = 1;
	public static final short SS_SUB             = 2;
	public static final byte U_NONE              = 0;
	public static final byte U_SINGLE            = 1;
	public static final byte U_DOUBLE            = 2;
	public static final byte U_SINGLE_ACCOUNTING = 0x21;
	public static final byte U_DOUBLE_ACCOUNTING = 0x22;

	// 0 0x01 - Reserved bit must be 0
	// is this font in italics
	private static final BitField italic     = BitFieldFactory.getInstance(0x02);

	// 2 0x04 - reserved bit must be 0
	// is this font has a line through the center
	private static final BitField strikeout  = BitFieldFactory.getInstance(0x08);
	// some weird macintosh thing....but who understands those mac people anyhow
	private static final BitField macoutline = BitFieldFactory.getInstance(0x10);
	private static final BitField macshadow  = BitFieldFactory.getInstance(0x20);

	// in units of .05 of a point
	private short field_1_font_height;
	private short field_2_attributes;

	// 7-6 - reserved bits must be 0
	// the rest is unused
	private short field_3_color_palette_index;
	private short field_4_bold_weight;
	// 00none/01super/02sub
	private short field_5_super_sub_script;
	// 00none/01single/02double/21singleaccounting/22doubleaccounting
	private byte field_6_underline;
	// ?? defined by windows api logfont structure?
	private byte field_7_family;
	// ?? defined by windows api logfont structure?
	private byte field_8_charset;
	// must be 0
	private byte field_9_zero;
	/** possibly empty string never <code>null</code> */
	private String field_11_font_name;

	public FontRecord() {
	}

	public FontRecord(FontRecord other) {
		super(other);
		field_1_font_height = other.field_1_font_height;
		field_2_attributes = other.field_2_attributes;
		field_3_color_palette_index = other.field_3_color_palette_index;
		field_4_bold_weight = other.field_4_bold_weight;
		field_5_super_sub_script = other.field_5_super_sub_script;
		field_6_underline = other.field_6_underline;
		field_7_family = other.field_7_family;
		field_8_charset = other.field_8_charset;
		field_9_zero = other.field_9_zero;
		field_11_font_name = other.field_11_font_name;
	}

	public FontRecord(RecordInputStream in) {
		field_1_font_height         = in.readShort();
		field_2_attributes          = in.readShort();
		field_3_color_palette_index = in.readShort();
		field_4_bold_weight         = in.readShort();
		field_5_super_sub_script    = in.readShort();
		field_6_underline           = in.readByte();
		field_7_family              = in.readByte();
		field_8_charset             = in.readByte();
		field_9_zero                = in.readByte();
		int field_10_font_name_len  = in.readUByte();
		int unicodeFlags            = in.readUByte(); // options byte present always (even if no character data)

		if (field_10_font_name_len > 0) {
			if (unicodeFlags == 0) {   // is compressed unicode
				field_11_font_name = in.readCompressedUnicode(field_10_font_name_len);
			} else {   // is not compressed unicode
				field_11_font_name = in.readUnicodeLEString(field_10_font_name_len);
			}
		} else {
			field_11_font_name = "";
		}
	}

	/**
	 * sets the height of the font in 1/20th point units
	 *
	 * @param height  fontheight (in points/20)
	 */
	public void setFontHeight(short height) {
		field_1_font_height = height;
	}

	/**
	 * set the font attributes (see individual bit setters that reference this method)
	 *
	 * @param attributes    the bitmask to set
	 */
	public void setAttributes(short attributes) {
		field_2_attributes = attributes;
	}

	// attributes bitfields

	/**
	 * set the font to be italics or not
	 *
	 * @param italics - whether the font is italics or not
	 * @see #setAttributes(short)
	 */
	public void setItalic(boolean italics) {
		field_2_attributes = italic.setShortBoolean(field_2_attributes, italics);
	}

	/**
	 * set the font to be stricken out or not
	 *
	 * @param strike - whether the font is stricken out or not
	 * @see #setAttributes(short)
	 */
	public void setStrikeout(boolean strike) {
		field_2_attributes = strikeout.setShortBoolean(field_2_attributes, strike);
	}

	/**
	 * whether to use the mac outline font style thing (mac only) - Some mac person
	 * should comment this instead of me doing it (since I have no idea)
	 *
	 * @param mac - whether to do that mac font outline thing or not
	 * @see #setAttributes(short)
	 */
	public void setMacoutline(boolean mac) {
		field_2_attributes = macoutline.setShortBoolean(field_2_attributes, mac);
	}

	/**
	 * whether to use the mac shado font style thing (mac only) - Some mac person
	 * should comment this instead of me doing it (since I have no idea)
	 *
	 * @param mac - whether to do that mac font shadow thing or not
	 * @see #setAttributes(short)
	 */
	public void setMacshadow(boolean mac) {
		field_2_attributes = macshadow.setShortBoolean(field_2_attributes, mac);
	}

	/**
	 * set the font's color palette index
	 *
	 * @param cpi - font color index
	 */
	public void setColorPaletteIndex(short cpi) {
		field_3_color_palette_index = cpi;
	}

	/**
	 * set the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
	 * 0x190 for normal and 0x2bc for bold
	 *
	 * @param bw - a number between 100-1000 for the fonts "boldness"
	 */
	public void setBoldWeight(short bw) {
		field_4_bold_weight = bw;
	}

	/**
	 * set the type of super or subscript for the font
	 *
	 * @param sss  super or subscript option
	 * @see #SS_NONE
	 * @see #SS_SUPER
	 * @see #SS_SUB
	 */
	public void setSuperSubScript(short sss) {
		field_5_super_sub_script = sss;
	}

	/**
	 * set the type of underlining for the font
	 *
	 * @param u  super or subscript option
	 *
	 * @see #U_NONE
	 * @see #U_SINGLE
	 * @see #U_DOUBLE
	 * @see #U_SINGLE_ACCOUNTING
	 * @see #U_DOUBLE_ACCOUNTING
	 */
	public void setUnderline(byte u) {
		field_6_underline = u;
	}

	/**
	 * set the font family (TODO)
	 *
	 * @param f family
	 */
	public void setFamily(byte f) {
		field_7_family = f;
	}

	/**
	 * set the character set
	 *
	 * @param charset - character set
	 */
	public void setCharset(byte charset) {
		field_8_charset = charset;
	}


	/**
	 * set the name of the font
	 *
	 * @param fn - name of the font (i.e. "Arial")
	 */
	public void setFontName(String fn) {
		field_11_font_name = fn;
	}

	/**
	 * gets the height of the font in 1/20th point units
	 *
	 * @return fontheight (in points/20)
	 */
	public short getFontHeight() {
		return field_1_font_height;
	}

	/**
	 * get the font attributes (see individual bit getters that reference this method)
	 *
	 * @return attribute - the bitmask
	 */
	public short getAttributes() {
		return field_2_attributes;
	}

	/**
	 * get whether the font is to be italics or not
	 *
	 * @return italics - whether the font is italics or not
	 * @see #getAttributes()
	 */
	public boolean isItalic() {
		return italic.isSet(field_2_attributes);
	}

	/**
	 * get whether the font is to be stricken out or not
	 *
	 * @return strike - whether the font is stricken out or not
	 * @see #getAttributes()
	 */
	public boolean isStruckout(){
		return strikeout.isSet(field_2_attributes);
	}

	/**
	 * whether to use the mac outline font style thing (mac only) - Some mac person
	 * should comment this instead of me doing it (since I have no idea)
	 *
	 * @return mac - whether to do that mac font outline thing or not
	 * @see #getAttributes()
	 */
	public boolean isMacoutlined(){
		return macoutline.isSet(field_2_attributes);
	}

	/**
	 * whether to use the mac shado font style thing (mac only) - Some mac person
	 * should comment this instead of me doing it (since I have no idea)
	 *
	 * @return mac - whether to do that mac font shadow thing or not
	 * @see #getAttributes()
	 */
	public boolean isMacshadowed(){
		return macshadow.isSet(field_2_attributes);
	}

	/**
	 * get the font's color palette index
	 *
	 * @return cpi - font color index
	 */
	public short getColorPaletteIndex(){
		return field_3_color_palette_index;
	}

	/**
	 * get the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
	 * 0x190 for normal and 0x2bc for bold
	 *
	 * @return bw - a number between 100-1000 for the fonts "boldness"
	 */
	public short getBoldWeight(){
		return field_4_bold_weight;
	}

	/**
	 * get the type of super or subscript for the font
	 *
	 * @return super or subscript option
	 * @see #SS_NONE
	 * @see #SS_SUPER
	 * @see #SS_SUB
	 */
	public short getSuperSubScript(){
		return field_5_super_sub_script;
	}

	/**
	 * get the type of underlining for the font
	 *
	 * @return super or subscript option
	 *
	 * @see #U_NONE
	 * @see #U_SINGLE
	 * @see #U_DOUBLE
	 * @see #U_SINGLE_ACCOUNTING
	 * @see #U_DOUBLE_ACCOUNTING
	 */
	public byte getUnderline() {
		return field_6_underline;
	}

	/**
	 * get the font family (TODO)
	 *
	 * @return family
	 */
	public byte getFamily() {
		return field_7_family;
	}

	/**
	 * get the character set
	 *
	 * @return charset - character set
	 */
	public byte getCharset() {
		return field_8_charset;
	}

	/**
	 * get the name of the font
	 *
	 * @return fn - name of the font (i.e. "Arial")
	 */
	public String getFontName() {
		return field_11_font_name;
	}

	public void serialize(LittleEndianOutput out) {

		out.writeShort(getFontHeight());
		out.writeShort(getAttributes());
		out.writeShort(getColorPaletteIndex());
		out.writeShort(getBoldWeight());
		out.writeShort(getSuperSubScript());
		out.writeByte(getUnderline());
		out.writeByte(getFamily());
		out.writeByte(getCharset());
		out.writeByte(field_9_zero);
		int fontNameLen = field_11_font_name.length();
		out.writeByte(fontNameLen);
		boolean hasMultibyte = StringUtil.hasMultibyte(field_11_font_name);
		out.writeByte(hasMultibyte ? 0x01 : 0x00);
		if (fontNameLen > 0) {
			if (hasMultibyte) {
			   StringUtil.putUnicodeLE(field_11_font_name, out);
			} else {
				StringUtil.putCompressedUnicode(field_11_font_name, out);
			}
		}
	}
	protected int getDataSize() {
		int size = 16; // 5 shorts + 6 bytes
		int fontNameLen = field_11_font_name.length();
		if (fontNameLen < 1) {
			return size;
		}

		boolean hasMultibyte = StringUtil.hasMultibyte(field_11_font_name);
		return size + fontNameLen * (hasMultibyte ? 2 : 1);
	}

	public short getSid() {
		return sid;
	}

	/**
	 * Clones all the font style information from another
	 *  FontRecord, onto this one. This
	 *  will then hold all the same font style options.
	 *
	 * @param source the record to clone the properties from
	 */
	public void cloneStyleFrom(FontRecord source) {
		field_1_font_height         = source.field_1_font_height;
		field_2_attributes          = source.field_2_attributes;
		field_3_color_palette_index = source.field_3_color_palette_index;
		field_4_bold_weight         = source.field_4_bold_weight;
		field_5_super_sub_script    = source.field_5_super_sub_script;
		field_6_underline           = source.field_6_underline;
		field_7_family              = source.field_7_family;
		field_8_charset             = source.field_8_charset;
		field_9_zero                = source.field_9_zero;
		field_11_font_name          = source.field_11_font_name;
	}

	public int hashCode() {
		return Objects.hash(
			field_1_font_height
			, field_2_attributes
			, field_3_color_palette_index
			, field_4_bold_weight
			, field_5_super_sub_script
			, field_6_underline
			, field_7_family
			, field_8_charset
			, field_9_zero
			, field_11_font_name
		);
	}

	/**
	 * Does this FontRecord have all the same font
	 *  properties as the supplied FontRecord?
	 * Note that {@link #equals(Object)} will check
	 *  for exact objects, while this will check
	 *  for exact contents, because normally the
	 *  font record's position makes a big
	 *  difference too.
	 *
	 *  @param other the record to compare with
	 *
	 *  @return true, if the properties match
	 */
	public boolean sameProperties(FontRecord other) {

		return
		field_1_font_height         == other.field_1_font_height &&
		field_2_attributes          == other.field_2_attributes &&
		field_3_color_palette_index == other.field_3_color_palette_index &&
		field_4_bold_weight         == other.field_4_bold_weight &&
		field_5_super_sub_script    == other.field_5_super_sub_script &&
		field_6_underline           == other.field_6_underline &&
		field_7_family              == other.field_7_family &&
		field_8_charset             == other.field_8_charset &&
		field_9_zero                == other.field_9_zero &&
		Objects.equals(this.field_11_font_name, other.field_11_font_name)
		;
	}

    public boolean equals(Object o) {
        return (o instanceof FontRecord) && sameProperties((FontRecord) o);
    }

	@Override
	public FontRecord copy() {
		return new FontRecord(this);
	}

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.FONT;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"fontHeight", this::getFontHeight,
			"attributes", GenericRecordUtil.getBitsAsString(this::getAttributes,
					new BitField[]{italic,strikeout,macoutline,macshadow},
					new String[]{"ITALIC","STRIKEOUT","MACOUTLINE","MACSHADOW"}),
			"colorPalette", this::getColorPaletteIndex,
			"boldWeight", this::getBoldWeight,
			"superSubScript", this::getSuperSubScript,
			"underline", this::getUnderline,
			"family", this::getFamily,
			"charset", this::getCharset,
			"fontName", this::getFontName
		);
	}
}
