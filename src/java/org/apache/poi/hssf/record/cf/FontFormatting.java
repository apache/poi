
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
 * Created on January 22, 2008, 10:05 PM
 * 
 * @author Dmitriy Kumshayev
 */

public class FontFormatting 
{
    private byte [] record;
    
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
    private static final int OFFSET_NOT_USED = 104;
    private static final int OFFSET_FONT_FORMATING_END = 116;

    
    public final static int  FONT_CELL_HEIGHT_PRESERVED   = 0xFFFFFFFF;
    
    // FONT OPTIONS MASKS
    private static final BitField posture		= BitFieldFactory.getInstance(0x00000002);
    private static final BitField outline		= BitFieldFactory.getInstance(0x00000008);
    private static final BitField shadow		= BitFieldFactory.getInstance(0x00000010);
    private static final BitField cancellation	= BitFieldFactory.getInstance(0x00000080);
    
    // OPTION FLAGS MASKS
    
    private static final BitField styleModified			= BitFieldFactory.getInstance(0x00000002);
    private static final BitField outlineModified 		= BitFieldFactory.getInstance(0x00000008);
    private static final BitField shadowModified 		= BitFieldFactory.getInstance(0x00000010);
    private static final BitField cancellationModified	= BitFieldFactory.getInstance(0x00000080);

    /**
     * Escapement type - None
     */
    public final static short     SS_NONE             = 0;
    
    /**
     * Escapement type - Superscript
     */
    public final static short     SS_SUPER            = 1;
    
    /**
     * Escapement type - Subscript
     */
    public final static short     SS_SUB              = 2;
    
    /**
     *  Underline type - None
     */ 
    public final static byte      U_NONE              = 0;

    /**
     *  Underline type - Single
     */ 
    public final static byte      U_SINGLE            = 1;

    /**
     *  Underline type - Double
     */ 
    public final static byte      U_DOUBLE            = 2;

    /**
     *  Underline type - Single Accounting
     */ 
    public final static byte      U_SINGLE_ACCOUNTING = 0x21;
    
    /**
     *  Underline type - Double Accounting
     */ 
    public final static byte      U_DOUBLE_ACCOUNTING = 0x22;
    
    /**
     * Normal boldness (not bold)
     */

    protected final static short  FONT_WEIGHT_NORMAL   = 0x190;

    /**
     * Bold boldness (bold)
     */

    protected final static short  FONT_WEIGHT_BOLD     = 0x2bc;
    
    public FontFormatting()
    {
    	this(new byte[118]);
    	
    	this.setFontHeight((short)-1);
    	this.setItalic(false);
    	this.setFontWieghtModified(false);
    	this.setOutline(false);
    	this.setShadow(false);
    	this.setStrikeout(false);
    	this.setEscapementType((short)0);
    	this.setUnderlineType((byte)0);
    	this.setFontColorIndex((short)-1);

    	this.setFontStyleModified(false);
    	this.setFontOutlineModified(false);
    	this.setFontShadowModified(false);
    	this.setFontCancellationModified(false);
    	
    	this.setEscapementTypeModified(false);
    	this.setUnderlineTypeModified(false);
    	
    	LittleEndian.putShort(record, OFFSET_FONT_NAME, (short)0);
    	LittleEndian.putInt(record, OFFSET_NOT_USED, 0x00000001);
    	LittleEndian.putShort(record, OFFSET_FONT_FORMATING_END, (short)0x0001);
    }
    
    /** Creates new FontFormatting */
    private FontFormatting(byte[] record)
    {
    	this.record = record;
    }

    /** Creates new FontFormatting */
    public FontFormatting(RecordInputStream in)
	{
    	record = new byte[118];
		for (int i = 0; i != record.length; i++)
		{
			record[i] = in.readByte();
		}
	}
    
    public byte[] getRawRecord()
    {
    	return record;
    }
    
    /**
     * sets the height of the font in 1/20th point units
     *  
     *
     * @param height  fontheight (in points/20); or -1 to preserve the cell font height
     */
    
    public void setFontHeight(short height)
    {
    	LittleEndian.putInt(record, OFFSET_FONT_HEIGHT, height);
    }
    
    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20); or -1 if not modified
     */
    public short getFontHeight()
    {
    	return (short) LittleEndian.getInt(record, OFFSET_FONT_HEIGHT);
    }

	private void setFontOption(boolean option, BitField field)
	{
		int options = LittleEndian.getInt(record,OFFSET_FONT_OPTIONS);
    	options = field.setBoolean(options, option);
    	LittleEndian.putInt(record,OFFSET_FONT_OPTIONS, options);
	}
	
	private boolean getFontOption(BitField field)
	{
		int options = LittleEndian.getInt(record,OFFSET_FONT_OPTIONS);
       	return field.isSet(options);
	}
	
    /**
     * set the font to be italics or not
     *
     * @param italics - whether the font is italics or not
     * @see #setAttributes(short)
     */

    public void setItalic(boolean italic)
    {
    	setFontOption(italic, posture);
    }
    
    /**
     * get whether the font is to be italics or not
     *
     * @return italics - whether the font is italics or not
     * @see #getAttributes()
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
     * @see #getAttributes()
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

    private void setFontWeight(short bw)
    {
    	if( bw<100) { bw=100; }
    	if( bw>1000){ bw=1000; }
    	LittleEndian.putShort(record,OFFSET_FONT_WEIGHT, bw);
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
       	return LittleEndian.getShort(record,OFFSET_FONT_WEIGHT);
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
       	return LittleEndian.getShort(record,OFFSET_ESCAPEMENT_TYPE);
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
    	LittleEndian.putShort(record,OFFSET_ESCAPEMENT_TYPE, escapementType);
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
       	return LittleEndian.getShort(record,OFFSET_UNDERLINE_TYPE);
    }
    
    /**
     * set the type of underlining type for the font
     *
     * @param u  super or subscript option
     *
     * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_NONE
     * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE
     * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE
     * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_SINGLE_ACCOUNTING
     * @see org.apache.poi.hssf.usermodel.HSSFFontFormatting#U_DOUBLE_ACCOUNTING
     */
    public void setUnderlineType( short underlineType)
    {
    	LittleEndian.putShort(record,OFFSET_UNDERLINE_TYPE, underlineType);
    }
    
    
    public short getFontColorIndex()
    {
       	return (short)LittleEndian.getInt(record,OFFSET_FONT_COLOR_INDEX);
    }

    public void setFontColorIndex(short fci )
    {
       	LittleEndian.putInt(record,OFFSET_FONT_COLOR_INDEX,fci);
    }

    private boolean getOptionFlag(BitField field)
	{
		int optionFlags = LittleEndian.getInt(record,OFFSET_OPTION_FLAGS);
    	int value = field.getValue(optionFlags);
    	return value==0? true : false ;
	}

	private void setOptionFlag(boolean modified, BitField field)
	{
		int value = modified? 0 : 1;
    	int optionFlags = LittleEndian.getInt(record,OFFSET_OPTION_FLAGS);
    	optionFlags = field.setValue(optionFlags, value);
    	LittleEndian.putInt(record,OFFSET_OPTION_FLAGS, optionFlags);
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
    	LittleEndian.putInt(record,OFFSET_ESCAPEMENT_TYPE_MODIFIED, value);
    }
    public boolean isEscapementTypeModified()
    {
    	int escapementModified = LittleEndian.getInt(record,OFFSET_ESCAPEMENT_TYPE_MODIFIED);
    	return escapementModified == 0;
    }

    public void setUnderlineTypeModified(boolean modified)
    {
    	int value = modified? 0 : 1;
    	LittleEndian.putInt(record,OFFSET_UNDERLINE_TYPE_MODIFIED, value);
    }

    public boolean isUnderlineTypeModified()
    {
    	int underlineModified = LittleEndian.getInt(record,OFFSET_UNDERLINE_TYPE_MODIFIED);
    	return underlineModified == 0;
    }
    
    public void setFontWieghtModified(boolean modified)
    {
    	int value = modified? 0 : 1;
    	LittleEndian.putInt(record,OFFSET_FONT_WEIGHT_MODIFIED, value);
    }

    public boolean isFontWeightModified()
    {
    	int fontStyleModified = LittleEndian.getInt(record,OFFSET_FONT_WEIGHT_MODIFIED);
    	return fontStyleModified == 0;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Font Formatting]\n");
        
        buffer.append("    .font height = ").append(getFontHeight()).append(" twips\n");
        
        if( isFontStyleModified() )
        {
            buffer.append("    .font posture = ").append(isItalic()?"Italic":"Normal").append("\n");
        }
        else
        {
            buffer.append("    .font posture = ]not modified]").append("\n");
        }
        
        if( isFontOutlineModified() )
        {
            buffer.append("    .font outline = ").append(isOutlineOn()).append("\n");
        }
        else
        {
            buffer.append("    .font outline is not modified\n");
        }

        if( isFontShadowModified() )
        {
            buffer.append("    .font shadow = ").append(isShadowOn()).append("\n");
        }
        else
        {
            buffer.append("    .font shadow is not modified\n");
        }
        
        if( isFontCancellationModified() )
        {
            buffer.append("    .font strikeout = ").append(isStruckout()).append("\n");
        }
        else
        {
            buffer.append("    .font strikeout is not modified\n");
        }

        if( isFontStyleModified() )
        {
            buffer.append("    .font weight = ").
            	append(getFontWeight()).
            	append(
					getFontWeight() == FONT_WEIGHT_NORMAL ? "(Normal)"
							: getFontWeight() == FONT_WEIGHT_BOLD ? "(Bold)" : "0x"+Integer.toHexString(getFontWeight())).
            	append("\n");
        }
        else
        {
            buffer.append("    .font weight = ]not modified]").append("\n");
        }
        
        if( isEscapementTypeModified() )
        {
            buffer.append("    .escapement type = ").append(getEscapementType()).append("\n");
        }
        else
        {
            buffer.append("    .escapement type is not modified\n");
        }

        if( isUnderlineTypeModified() )
        {
            buffer.append("    .underline type = ").append(getUnderlineType()).append("\n");
        }
        else
        {
            buffer.append("    .underline type is not modified\n");
        }
        buffer.append("    .color index = ").append("0x"+Integer.toHexString(getFontColorIndex()).toUpperCase()).append("\n");
        

        buffer.append("    ====\n");
        buffer.append("    ["+OFFSET_FONT_HEIGHT+"] FONT HEIGHT: "+intToHex(OFFSET_FONT_HEIGHT)+"\n");
        buffer.append("    ["+OFFSET_FONT_OPTIONS+"] FONT OPTIONS: "+intToHex(OFFSET_FONT_OPTIONS)+"\n");
        buffer.append("    ["+OFFSET_FONT_WEIGHT+"] FONT WEIGHT: "+shortToHex(OFFSET_FONT_WEIGHT)+"\n");
        buffer.append("    ["+OFFSET_ESCAPEMENT_TYPE+"] FONT ESCAPEMENT: "+shortToHex(OFFSET_ESCAPEMENT_TYPE)+"\n");
        buffer.append("    ["+OFFSET_UNDERLINE_TYPE+"] FONT UNDERLINE: "+byteToHex(OFFSET_UNDERLINE_TYPE)+"\n");
        buffer.append("    ["+(OFFSET_UNDERLINE_TYPE+1)+"] FONT NOT USED: "+byteToHex(OFFSET_UNDERLINE_TYPE+1)+"\n");
        buffer.append("    ["+(OFFSET_UNDERLINE_TYPE+2)+"] FONT NOT USED: "+byteToHex(OFFSET_UNDERLINE_TYPE+2)+"\n");
        buffer.append("    ["+(OFFSET_UNDERLINE_TYPE+3)+"] FONT NOT USED: "+byteToHex(OFFSET_UNDERLINE_TYPE+3)+"\n");
        buffer.append("    ["+OFFSET_FONT_COLOR_INDEX+"] FONT COLIDX: "+intToHex(OFFSET_FONT_COLOR_INDEX)+"\n");
        buffer.append("    ["+(OFFSET_FONT_COLOR_INDEX+4)+"] FONT NOT USED: "+intToHex(OFFSET_FONT_COLOR_INDEX+4)+"\n");
        buffer.append("    ["+OFFSET_OPTION_FLAGS+"] FONT OPTIONS: "+intToHex(OFFSET_OPTION_FLAGS)+"\n");
        buffer.append("    ["+OFFSET_ESCAPEMENT_TYPE_MODIFIED+"] FONT ESC MOD: "+intToHex(OFFSET_ESCAPEMENT_TYPE_MODIFIED)+"\n");
        buffer.append("    ["+OFFSET_UNDERLINE_TYPE_MODIFIED+"] FONT UND MOD: "+intToHex(OFFSET_UNDERLINE_TYPE_MODIFIED)+"\n");
        buffer.append("    ["+OFFSET_FONT_WEIGHT+"] FONT WGH MOD: "+intToHex(OFFSET_FONT_WEIGHT)+"\n");
        buffer.append("    ["+OFFSET_NOT_USED+"] FONT NOT USED: "+intToHex(OFFSET_NOT_USED)+"\n");
        buffer.append("    ["+(OFFSET_NOT_USED+4)+"] FONT NOT USED: "+intToHex(OFFSET_NOT_USED+4)+"\n");
        buffer.append("    ["+(OFFSET_NOT_USED+8)+"] FONT NOT USED: "+intToHex(OFFSET_NOT_USED+8)+"\n");
        buffer.append("    ["+OFFSET_FONT_FORMATING_END+"] FONT FORMATTING END: "+shortToHex(OFFSET_FONT_FORMATING_END)+"\n");
        buffer.append("    ====\n");
        
        buffer.append("    [/Font Formatting]\n");
        return buffer.toString();
    }

    public Object clone() 
    {
      FontFormatting rec = new FontFormatting();
      if( record != null)
      {
    	  byte[] clone = new byte[record.length];
    	  System.arraycopy(record, 0, clone, 0, record.length);
    	  rec.record = clone;
      }
      return rec;
    }

	private String intToHex(int offset)
	{
		return Integer.toHexString(LittleEndian.getInt(record, offset));
	}
	private String shortToHex(int offset)
	{
		return Integer.toHexString(LittleEndian.getShort(record, offset)&0xFFFF);
	}
	private String byteToHex(int offset)
	{
		return Integer.toHexString(record[offset]&0xFF);
	}
	
}
