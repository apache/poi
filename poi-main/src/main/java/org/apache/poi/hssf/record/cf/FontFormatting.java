
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

package org.apache.poi.hssf.record.cf;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

/**
 * Font Formatting Block of the Conditional Formatting Rule Record.
 *
 * @author Dmitriy Kumshayev
 */
public final class FontFormatting
{
	private byte[] _rawData;

	private static final int OFFSET_FONT_NAME = 0;
	private static final int OFFSET_FONT_HEIGHT = 64;
	private static final int OFFSET_FONT_OPTIONS = 68;
	private static final int OFFSET_FONT_WEIGHT = 72;
	private static final int OFFSET_ESCAPEMENT_TYPE = 74;
	private static final int OFFSET_UNDERLINE_TYPE = 76;
	private static final int OFFSET_FONT_COLOR_INDEX = 80;
	private static final int OFFSET_OPTION_FLAGS = 88;
	private static final int OFFSET_ESCAPEMENT_TYPE_MODIFIED = 92;
	private static final int OFFSET_UNDERLINE_TYPE_MODIFIED = 96;
	private static final int OFFSET_FONT_WEIGHT_MODIFIED = 100;
	private static final int OFFSET_NOT_USED1 = 104;
	private static final int OFFSET_NOT_USED2 = 108;
	private static final int OFFSET_NOT_USED3 = 112; // for some reason Excel always writes  0x7FFFFFFF at this offset
	private static final int OFFSET_FONT_FORMATING_END = 116;
	private static final int RAW_DATA_SIZE = 118;


	public final static int  FONT_CELL_HEIGHT_PRESERVED   = 0xFFFFFFFF;

	// FONT OPTIONS MASKS
	private static final BitField posture       = BitFieldFactory.getInstance(0x00000002);
	private static final BitField outline       = BitFieldFactory.getInstance(0x00000008);
	private static final BitField shadow        = BitFieldFactory.getInstance(0x00000010);
	private static final BitField cancellation	= BitFieldFactory.getInstance(0x00000080);

	// OPTION FLAGS MASKS

	private static final BitField styleModified        = BitFieldFactory.getInstance(0x00000002);
	private static final BitField outlineModified      = BitFieldFactory.getInstance(0x00000008);
	private static final BitField shadowModified       = BitFieldFactory.getInstance(0x00000010);
	private static final BitField cancellationModified = BitFieldFactory.getInstance(0x00000080);

	/** Escapement type - None */
	public static final short SS_NONE  = 0;
	/** Escapement type - Superscript */
	public static final short SS_SUPER = 1;
	/** Escapement type - Subscript */
	public static final short SS_SUB   = 2;
	/** Underline type - None */
	public static final byte U_NONE               = 0;
	/** Underline type - Single */
	public static final byte U_SINGLE             = 1;
	/** Underline type - Double */
	public static final byte U_DOUBLE             = 2;
	/** Underline type - Single Accounting */
	public static final byte U_SINGLE_ACCOUNTING  = 0x21;
	/** Underline type - Double Accounting */
	public static final byte U_DOUBLE_ACCOUNTING  = 0x22;
	/** Normal boldness (not bold) */
	private static final short FONT_WEIGHT_NORMAL = 0x190;

	/**
	 * Bold boldness (bold)
	 */
	private static final short FONT_WEIGHT_BOLD	 = 0x2bc;

	private FontFormatting(byte[] rawData) {
		_rawData = rawData;
	}

	public FontFormatting()
	{
		this(new byte[RAW_DATA_SIZE]);

		setFontHeight(-1);
		setItalic(false);
		setFontWieghtModified(false);
		setOutline(false);
		setShadow(false);
		setStrikeout(false);
		setEscapementType((short)0);
		setUnderlineType((byte)0);
		setFontColorIndex((short)-1);

		setFontStyleModified(false);
		setFontOutlineModified(false);
		setFontShadowModified(false);
		setFontCancellationModified(false);

		setEscapementTypeModified(false);
		setUnderlineTypeModified(false);

		setShort(OFFSET_FONT_NAME, 0);
		setInt(OFFSET_NOT_USED1, 0x00000001);
		setInt(OFFSET_NOT_USED2, 0x00000000);
		setInt(OFFSET_NOT_USED3, 0x7FFFFFFF);// for some reason Excel always writes  0x7FFFFFFF at this offset
		setShort(OFFSET_FONT_FORMATING_END, 0x0001);
	}

	/** Creates new FontFormatting */
	public FontFormatting(RecordInputStream in)
	{
		this(new byte[RAW_DATA_SIZE]);
		for (int i = 0; i < _rawData.length; i++)
		{
			_rawData[i] = in.readByte();
		}
	}

	private short getShort(int offset) {
		return LittleEndian.getShort( _rawData, offset);
	}
	private void setShort(int offset, int value) {
		LittleEndian.putShort( _rawData, offset, (short)value);
	}
	private int getInt(int offset) {
		return LittleEndian.getInt( _rawData, offset);
	}
	private void setInt(int offset, int value) {
		LittleEndian.putInt( _rawData, offset, value);
	}

	public byte[] getRawRecord()
	{
		return _rawData;
	}

	/**
	 * sets the height of the font in 1/20th point units
	 *
	 *
	 * @param height  fontheight (in points/20); or -1 to preserve the cell font height
	 */

	public void setFontHeight(int height)
	{
		setInt(OFFSET_FONT_HEIGHT, height);
	}

	/**
	 * gets the height of the font in 1/20th point units
	 *
	 * @return fontheight (in points/20); or -1 if not modified
	 */
	public int getFontHeight()
	{
		return getInt(OFFSET_FONT_HEIGHT);
	}

	private void setFontOption(boolean option, BitField field)
	{
		int options = getInt(OFFSET_FONT_OPTIONS);
		options = field.setBoolean(options, option);
		setInt(OFFSET_FONT_OPTIONS, options);
	}

	private boolean getFontOption(BitField field)
	{
		int options = getInt(OFFSET_FONT_OPTIONS);
		return field.isSet(options);
	}

	/**
	 * set the font to be italics or not
	 *
	 * @param italic - whether the font is italics or not
	 * @see #setFontOption(boolean, org.apache.poi.util.BitField)
	 */

	public void setItalic(boolean italic)
	{
		setFontOption(italic, posture);
	}

	/**
	 * get whether the font is to be italics or not
	 *
	 * @return italics - whether the font is italics or not
	 * @see #getFontOption(org.apache.poi.util.BitField)
	 */

	public boolean isItalic()
	{
		return getFontOption(posture);
	}

	public void setOutline(boolean on)
	{
		setFontOption(on, outline);
	}

	public boolean isOutlineOn()
	{
		return getFontOption(outline);
	}

	public void setShadow(boolean on)
	{
		setFontOption(on, shadow);
	}

	public boolean isShadowOn()
	{
		return getFontOption(shadow);
	}

	/**
	 * set the font to be stricken out or not
	 *
	 * @param strike - whether the font is stricken out or not
	 */

	public void setStrikeout(boolean strike)
	{
		setFontOption(strike, cancellation);
	}

	/**
	 * get whether the font is to be stricken out or not
	 *
	 * @return strike - whether the font is stricken out or not
	 * @see #getFontOption(org.apache.poi.util.BitField)
	 */

	public boolean isStruckout()
	{
		return getFontOption(cancellation);
	}

	/**
	 * set the font weight (100-1000dec or 0x64-0x3e8).  Default is
	 * 0x190 for normal and 0x2bc for bold
	 *
	 * @param bw - a number between 100-1000 for the fonts "boldness"
	 */

	private void setFontWeight(short pbw)
	{
		short bw = pbw;
		if( bw<100) { bw=100; }
		if( bw>1000){ bw=1000; }
		setShort(OFFSET_FONT_WEIGHT, bw);
	}

	/**
	 * set the font weight to bold (weight=700) or to normal(weight=400) boldness.
	 *
	 * @param bold - set font weight to bold if true; to normal otherwise
	 */
	public void setBold(boolean bold)
	{
		setFontWeight(bold?FONT_WEIGHT_BOLD:FONT_WEIGHT_NORMAL);
	}

	/**
	 * get the font weight for this font (100-1000dec or 0x64-0x3e8).  Default is
	 * 0x190 for normal and 0x2bc for bold
	 *
	 * @return bw - a number between 100-1000 for the fonts "boldness"
	 */

	public short getFontWeight()
	{
		return getShort(OFFSET_FONT_WEIGHT);
	}

	/**
	 * get whether the font weight is set to bold or not
	 *
	 * @return bold - whether the font is bold or not
	 */

	public boolean isBold()
	{
		return getFontWeight()==FONT_WEIGHT_BOLD;
	}

	/**
	 * get the type of super or subscript for the font
	 *
	 * @return super or subscript option
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_NONE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_SUPER
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_SUB
	 */
	public short getEscapementType()
	{
		return getShort(OFFSET_ESCAPEMENT_TYPE);
	}

	/**
	 * set the escapement type for the font
	 *
	 * @param escapementType  super or subscript option
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_NONE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_SUPER
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#SS_SUB
	 */
	public void setEscapementType( short escapementType)
	{
		setShort(OFFSET_ESCAPEMENT_TYPE, escapementType);
	}

	/**
	 * get the type of underlining for the font
	 *
	 * @return font underlining type
	 *
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_NONE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE_ACCOUNTING
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE_ACCOUNTING
	 */

	public short getUnderlineType()
	{
		return getShort(OFFSET_UNDERLINE_TYPE);
	}

	/**
	 * set the type of underlining type for the font
	 *
	 * @param underlineType underline option
	 *
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_NONE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE_ACCOUNTING
	 * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE_ACCOUNTING
	 */
	public void setUnderlineType( short underlineType)
	{
		setShort(OFFSET_UNDERLINE_TYPE, underlineType);
	}


	public short getFontColorIndex()
	{
		return (short)getInt(OFFSET_FONT_COLOR_INDEX);
	}

	public void setFontColorIndex(short fci )
	{
		setInt(OFFSET_FONT_COLOR_INDEX,fci);
	}

	private boolean getOptionFlag(BitField field)
	{
		int optionFlags = getInt(OFFSET_OPTION_FLAGS);
		int value = field.getValue(optionFlags);
		return value==0? true : false ;
	}

	private void setOptionFlag(boolean modified, BitField field)
	{
		int value = modified? 0 : 1;
		int optionFlags = getInt(OFFSET_OPTION_FLAGS);
		optionFlags = field.setValue(optionFlags, value);
		setInt(OFFSET_OPTION_FLAGS, optionFlags);
	}


	public boolean isFontStyleModified()
	{
		return getOptionFlag(styleModified);
	}


	public void setFontStyleModified(boolean modified)
	{
		setOptionFlag(modified, styleModified);
	}

	public boolean isFontOutlineModified()
	{
		return getOptionFlag(outlineModified);
	}

	public void setFontOutlineModified(boolean modified)
	{
		setOptionFlag(modified, outlineModified);
	}

	public boolean isFontShadowModified()
	{
		return getOptionFlag(shadowModified);
	}

	public void setFontShadowModified(boolean modified)
	{
		setOptionFlag(modified, shadowModified);
	}
	public void setFontCancellationModified(boolean modified)
	{
		setOptionFlag(modified, cancellationModified);
	}

	public boolean isFontCancellationModified()
	{
		return getOptionFlag(cancellationModified);
	}

	public void setEscapementTypeModified(boolean modified)
	{
		int value = modified? 0 : 1;
		setInt(OFFSET_ESCAPEMENT_TYPE_MODIFIED, value);
	}
	public boolean isEscapementTypeModified()
	{
		int escapementModified = getInt(OFFSET_ESCAPEMENT_TYPE_MODIFIED);
		return escapementModified == 0;
	}

	public void setUnderlineTypeModified(boolean modified)
	{
		int value = modified? 0 : 1;
		setInt(OFFSET_UNDERLINE_TYPE_MODIFIED, value);
	}

	public boolean isUnderlineTypeModified()
	{
		int underlineModified = getInt(OFFSET_UNDERLINE_TYPE_MODIFIED);
		return underlineModified == 0;
	}

	public void setFontWieghtModified(boolean modified)
	{
		int value = modified? 0 : 1;
		setInt(OFFSET_FONT_WEIGHT_MODIFIED, value);
	}

	public boolean isFontWeightModified()
	{
		int fontStyleModified = getInt(OFFSET_FONT_WEIGHT_MODIFIED);
		return fontStyleModified == 0;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("	[Font Formatting]\n");

		buffer.append("	.font height = ").append(getFontHeight()).append(" twips\n");

		if( isFontStyleModified() )
		{
			buffer.append("	.font posture = ").append(isItalic()?"Italic":"Normal").append("\n");
		}
		else
		{
			buffer.append("	.font posture = ]not modified]").append("\n");
		}

		if( isFontOutlineModified() )
		{
			buffer.append("	.font outline = ").append(isOutlineOn()).append("\n");
		}
		else
		{
			buffer.append("	.font outline is not modified\n");
		}

		if( isFontShadowModified() )
		{
			buffer.append("	.font shadow = ").append(isShadowOn()).append("\n");
		}
		else
		{
			buffer.append("	.font shadow is not modified\n");
		}

		if( isFontCancellationModified() )
		{
			buffer.append("	.font strikeout = ").append(isStruckout()).append("\n");
		}
		else
		{
			buffer.append("	.font strikeout is not modified\n");
		}

		if( isFontStyleModified() )
		{
			buffer.append("	.font weight = ").
				append(getFontWeight()).
				append(
					getFontWeight() == FONT_WEIGHT_NORMAL ? "(Normal)"
							: getFontWeight() == FONT_WEIGHT_BOLD ? "(Bold)" : "0x"+Integer.toHexString(getFontWeight())).
				append("\n");
		}
		else
		{
			buffer.append("	.font weight = ]not modified]").append("\n");
		}

		if( isEscapementTypeModified() )
		{
			buffer.append("	.escapement type = ").append(getEscapementType()).append("\n");
		}
		else
		{
			buffer.append("	.escapement type is not modified\n");
		}

		if( isUnderlineTypeModified() )
		{
			buffer.append("	.underline type = ").append(getUnderlineType()).append("\n");
		}
		else
		{
			buffer.append("	.underline type is not modified\n");
		}
		buffer.append("	.color index = ").append("0x"+Integer.toHexString(getFontColorIndex()).toUpperCase()).append("\n");

		buffer.append("	[/Font Formatting]\n");
		return buffer.toString();
	}

	public Object clone()
	{
		byte[] rawData = _rawData.clone();
		return new FontFormatting(rawData);
	}
}
