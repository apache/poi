
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
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.BitField;

/**
 * Title:        Font Record - descrbes a font in the workbook (index = 0-3,5-infinity - skip 4)<P>
 * Description:  An element in the Font Table<P>
 * REFERENCE:  PG 315 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class FontRecord
    extends Record
{
    public final static short     sid                 =
        0x31;                                                 // docs are wrong (0x231 Microsoft Support site article Q184647)
    public final static short     SS_NONE             = 0;
    public final static short     SS_SUPER            = 1;
    public final static short     SS_SUB              = 2;
    public final static byte      U_NONE              = 0;
    public final static byte      U_SINGLE            = 1;
    public final static byte      U_DOUBLE            = 2;
    public final static byte      U_SINGLE_ACCOUNTING = 0x21;
    public final static byte      U_DOUBLE_ACCOUNTING = 0x22;
    private short                 field_1_font_height;        // in units of .05 of a point
    private short                 field_2_attributes;

    // 0 0x01 - Reserved bit must be 0
    static final private BitField italic     =
        new BitField(0x02);                                   // is this font in italics

    // 2 0x04 - reserved bit must be 0
    static final private BitField strikeout  =
        new BitField(0x08);                                   // is this font has a line through the center
    static final private BitField macoutline = new BitField(
        0x10);                                                // some weird macintosh thing....but who understands those mac people anyhow
    static final private BitField macshadow  = new BitField(
        0x20);                                                // some weird macintosh thing....but who understands those mac people anyhow

    // 7-6 - reserved bits must be 0
    // the rest is unused
    private short                 field_3_color_palette_index;
    private short                 field_4_bold_weight;
    private short                 field_5_super_sub_script;   // 00none/01super/02sub
    private byte                  field_6_underline;          // 00none/01single/02double/21singleaccounting/22doubleaccounting
    private byte                  field_7_family;             // ?? defined by windows api logfont structure?
    private byte                  field_8_charset;            // ?? defined by windows api logfont structure?
    private byte                  field_9_zero = 0;           // must be 0
    private byte                  field_10_font_name_len;     // length of the font name
    private String                field_11_font_name;         // whoa...the font name

    public FontRecord()
    {
    }

    /**
     * Constructs a Font record and sets its fields appropriately.
     *
     * @param id     id must be 0x31 (NOT 0x231 see MSKB #Q184647 for an "explanation of
     * this bug in the documentation) or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public FontRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Font record and sets its fields appropriately.
     *
     * @param id     id must be 0x31 (NOT 0x231 see MSKB #Q184647 for an "explanation of
     * this bug in the documentation) or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public FontRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A FONT RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_font_height         = LittleEndian.getShort(data, 0 + offset);
        field_2_attributes          = LittleEndian.getShort(data, 2 + offset);
        field_3_color_palette_index = LittleEndian.getShort(data, 4 + offset);
        field_4_bold_weight         = LittleEndian.getShort(data, 6 + offset);
        field_5_super_sub_script    = LittleEndian.getShort(data, 8 + offset);
        field_6_underline           = data[ 10 + offset ];
        field_7_family              = data[ 11 + offset ];
        field_8_charset             = data[ 12 + offset ];
        field_9_zero                = data[ 13 + offset ];
        field_10_font_name_len      = data[ 14 + offset ];
        if (field_10_font_name_len > 0)
        {
            if (data[ 15 ] == 0)
            {   // is compressed unicode
                field_11_font_name = StringUtil.getFromCompressedUnicode(data, 16,
                                                LittleEndian.ubyteToInt(field_10_font_name_len));
            }
            else
            {   // is not compressed unicode
                field_11_font_name = StringUtil.getFromUnicodeHigh(data, 16,
                        field_10_font_name_len);
            }
        }
    }

    /**
     * sets the height of the font in 1/20th point units
     *
     * @param height  fontheight (in points/20)
     */

    public void setFontHeight(short height)
    {
        field_1_font_height = height;
    }

    /**
     * set the font attributes (see individual bit setters that reference this method)
     *
     * @param attributes    the bitmask to set
     */

    public void setAttributes(short attributes)
    {
        field_2_attributes = attributes;
    }

    // attributes bitfields

    /**
     * set the font to be italics or not
     *
     * @param italics - whether the font is italics or not
     * @see #setAttributes(short)
     */

    public void setItalic(boolean italics)
    {
        field_2_attributes = italic.setShortBoolean(field_2_attributes, italics);
    }

    /**
     * set the font to be stricken out or not
     *
     * @param strike - whether the font is stricken out or not
     * @see #setAttributes(short)
     */

    public void setStrikeout(boolean strike)
    {
        field_2_attributes = strikeout.setShortBoolean(field_2_attributes, strike);
    }

    /**
     * whether to use the mac outline font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     *
     * @param mac - whether to do that mac font outline thing or not
     * @see #setAttributes(short)
     */

    public void setMacoutline(boolean mac)
    {
        field_2_attributes = macoutline.setShortBoolean(field_2_attributes, mac);
    }

    /**
     * whether to use the mac shado font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     *
     * @param mac - whether to do that mac font shadow thing or not
     * @see #setAttributes(short)
     */

    public void setMacshadow(boolean mac)
    {
        field_2_attributes = macshadow.setShortBoolean(field_2_attributes, mac);
    }

    /**
     * set the font's color palette index
     *
     * @param cpi - font color index
     */

    public void setColorPaletteIndex(short cpi)
    {
        field_3_color_palette_index = cpi;
    }

    /**
     * set the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     *
     * @param bw - a number between 100-1000 for the fonts "boldness"
     */

    public void setBoldWeight(short bw)
    {
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

    public void setSuperSubScript(short sss)
    {
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

    public void setUnderline(byte u)
    {
        field_6_underline = u;
    }

    /**
     * set the font family (TODO)
     *
     * @param f family
     */

    public void setFamily(byte f)
    {
        field_7_family = f;
    }

    /**
     * set the character set
     *
     * @param charset - characterset
     */

    public void setCharset(byte charset)
    {
        field_8_charset = charset;
    }

    /**
     * set the length of the fontname string
     *
     * @param len  length of the font name
     * @see #setFontName(String)
     */

    public void setFontNameLength(byte len)
    {
        field_10_font_name_len = len;
    }

    /**
     * set the name of the font
     *
     * @param fn - name of the font (i.e. "Arial")
     */

    public void setFontName(String fn)
    {
        field_11_font_name = fn;
    }

    /**
     * gets the height of the font in 1/20th point units
     *
     * @return fontheight (in points/20)
     */

    public short getFontHeight()
    {
        return field_1_font_height;
    }

    /**
     * get the font attributes (see individual bit getters that reference this method)
     *
     * @return attribute - the bitmask
     */

    public short getAttributes()
    {
        return field_2_attributes;
    }

    /**
     * get whether the font is to be italics or not
     *
     * @return italics - whether the font is italics or not
     * @see #getAttributes()
     */

    public boolean isItalic()
    {
        return italic.isSet(field_2_attributes);
    }

    /**
     * get whether the font is to be stricken out or not
     *
     * @return strike - whether the font is stricken out or not
     * @see #getAttributes()
     */

    public boolean isStruckout()
    {
        return strikeout.isSet(field_2_attributes);
    }

    /**
     * whether to use the mac outline font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     *
     * @return mac - whether to do that mac font outline thing or not
     * @see #getAttributes()
     */

    public boolean isMacoutlined()
    {
        return macoutline.isSet(field_2_attributes);
    }

    /**
     * whether to use the mac shado font style thing (mac only) - Some mac person
     * should comment this instead of me doing it (since I have no idea)
     *
     * @return mac - whether to do that mac font shadow thing or not
     * @see #getAttributes()
     */

    public boolean isMacshadowed()
    {
        return macshadow.isSet(field_2_attributes);
    }

    /**
     * get the font's color palette index
     *
     * @return cpi - font color index
     */

    public short getColorPaletteIndex()
    {
        return field_3_color_palette_index;
    }

    /**
     * get the bold weight for this font (100-1000dec or 0x64-0x3e8).  Default is
     * 0x190 for normal and 0x2bc for bold
     *
     * @return bw - a number between 100-1000 for the fonts "boldness"
     */

    public short getBoldWeight()
    {
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

    public short getSuperSubScript()
    {
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

    public byte getUnderline()
    {
        return field_6_underline;
    }

    /**
     * get the font family (TODO)
     *
     * @return family
     */

    public byte getFamily()
    {
        return field_7_family;
    }

    /**
     * get the character set
     *
     * @return charset - characterset
     */

    public byte getCharset()
    {
        return field_8_charset;
    }

    /**
     * get the length of the fontname string
     *
     * @return length of the font name
     * @see #getFontName()
     */

    public byte getFontNameLength()
    {
        return field_10_font_name_len;
    }

    /**
     * get the name of the font
     *
     * @return fn - name of the font (i.e. "Arial")
     */

    public String getFontName()
    {
        return field_11_font_name;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FONT]\n");
        buffer.append("    .fontheight      = ")
            .append(Integer.toHexString(getFontHeight())).append("\n");
        buffer.append("    .attributes      = ")
            .append(Integer.toHexString(getAttributes())).append("\n");
        buffer.append("         .italic     = ").append(isItalic())
            .append("\n");
        buffer.append("         .strikout   = ").append(isStruckout())
            .append("\n");
        buffer.append("         .macoutlined= ").append(isMacoutlined())
            .append("\n");
        buffer.append("         .macshadowed= ").append(isMacshadowed())
            .append("\n");
        buffer.append("    .colorpalette    = ")
            .append(Integer.toHexString(getColorPaletteIndex())).append("\n");
        buffer.append("    .boldweight      = ")
            .append(Integer.toHexString(getBoldWeight())).append("\n");
        buffer.append("    .supersubscript  = ")
            .append(Integer.toHexString(getSuperSubScript())).append("\n");
        buffer.append("    .underline       = ")
            .append(Integer.toHexString(getUnderline())).append("\n");
        buffer.append("    .family          = ")
            .append(Integer.toHexString(getFamily())).append("\n");
        buffer.append("    .charset         = ")
            .append(Integer.toHexString(getCharset())).append("\n");
        buffer.append("    .namelength      = ")
            .append(Integer.toHexString(getFontNameLength())).append("\n");
        buffer.append("    .fontname        = ").append(getFontName())
            .append("\n");
        buffer.append("[/FONT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        int realflen = getFontNameLength() * 2;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(
            data, 2 + offset,
            ( short ) (15 + realflen
                       + 1));   // 19 - 4 (sid/len) + font name length = datasize

        // undocumented single byte (1)
        LittleEndian.putShort(data, 4 + offset, getFontHeight());
        LittleEndian.putShort(data, 6 + offset, getAttributes());
        LittleEndian.putShort(data, 8 + offset, getColorPaletteIndex());
        LittleEndian.putShort(data, 10 + offset, getBoldWeight());
        LittleEndian.putShort(data, 12 + offset, getSuperSubScript());
        data[ 14 + offset ] = getUnderline();
        data[ 15 + offset ] = getFamily();
        data[ 16 + offset ] = getCharset();
        data[ 17 + offset ] = (( byte ) 0);
        data[ 18 + offset ] = getFontNameLength();
        data[ 19 + offset ] = ( byte ) 1;
        if (getFontName() != null) {
           StringUtil.putUncompressedUnicode(getFontName(), data, 20 + offset);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return (getFontNameLength() * 2) + 20;
    }

    public short getSid()
    {
        return this.sid;
    }
}
