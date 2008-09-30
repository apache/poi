
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



import org.apache.poi.util.*;

/**
 * The TXO record is used to define the properties of a text box.  It is followed
        by two continue records unless there is no actual text.  The first continue record contains
        the text data and the next continue record contains the formatting runs.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TextObjectBaseRecord extends Record {
    public final static short      sid                             = 0x1B6;
    
    private static final BitField reserved1               = BitFieldFactory.getInstance(0x0001);
    private static final BitField HorizontalTextAlignment = BitFieldFactory.getInstance(0x000E);
    private static final BitField VerticalTextAlignment   = BitFieldFactory.getInstance(0x0070);
    private static final BitField reserved2               = BitFieldFactory.getInstance(0x0180);
    private static final BitField textLocked              = BitFieldFactory.getInstance(0x0200);
    private static final BitField reserved3               = BitFieldFactory.getInstance(0xFC00);
    
    private  short      field_1_options;
    public final static short  HORIZONTAL_TEXT_ALIGNMENT_LEFT_ALIGNED = 1;
    public final static short  HORIZONTAL_TEXT_ALIGNMENT_CENTERED = 2;
    public final static short  HORIZONTAL_TEXT_ALIGNMENT_RIGHT_ALIGNED = 3;
    public final static short  HORIZONTAL_TEXT_ALIGNMENT_JUSTIFIED = 4;
    public final static short  VERTICAL_TEXT_ALIGNMENT_TOP    = 1;
    public final static short  VERTICAL_TEXT_ALIGNMENT_CENTER = 2;
    public final static short  VERTICAL_TEXT_ALIGNMENT_BOTTOM = 3;
    public final static short  VERTICAL_TEXT_ALIGNMENT_JUSTIFY = 4;
    private  short      field_2_textOrientation;
    public final static short       TEXT_ORIENTATION_NONE          = 0;
    public final static short       TEXT_ORIENTATION_TOP_TO_BOTTOM = 1;
    public final static short       TEXT_ORIENTATION_ROT_RIGHT     = 2;
    public final static short       TEXT_ORIENTATION_ROT_LEFT      = 3;
    private  short      field_3_reserved4;
    private  short      field_4_reserved5;
    private  short      field_5_reserved6;
    private  short      field_6_textLength;
    private  short      field_7_formattingRunLength;
    private  int        field_8_reserved7;


    public TextObjectBaseRecord()
    {

    }

    public TextObjectBaseRecord(RecordInputStream in)
    {
        field_1_options                = in.readShort();
        field_2_textOrientation        = in.readShort();
        field_3_reserved4              = in.readShort();
        field_4_reserved5              = in.readShort();
        field_5_reserved6              = in.readShort();
        field_6_textLength             = in.readShort();
        field_7_formattingRunLength    = in.readShort();
        field_8_reserved7              = in.readInt();

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[TXO]\n");
        buffer.append("    .options              = ")
            .append("0x").append(HexDump.toHex(  getOptions ()))
            .append(" (").append( getOptions() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .reserved1                = ").append(isReserved1()).append('\n'); 
            buffer.append("         .HorizontalTextAlignment     = ").append(getHorizontalTextAlignment()).append('\n'); 
            buffer.append("         .VerticalTextAlignment     = ").append(getVerticalTextAlignment()).append('\n'); 
            buffer.append("         .reserved2                = ").append(getReserved2()).append('\n'); 
        buffer.append("         .textLocked               = ").append(isTextLocked()).append('\n'); 
            buffer.append("         .reserved3                = ").append(getReserved3()).append('\n'); 
        buffer.append("    .textOrientation      = ")
            .append("0x").append(HexDump.toHex(  getTextOrientation ()))
            .append(" (").append( getTextOrientation() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved4            = ")
            .append("0x").append(HexDump.toHex(  getReserved4 ()))
            .append(" (").append( getReserved4() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved5            = ")
            .append("0x").append(HexDump.toHex(  getReserved5 ()))
            .append(" (").append( getReserved5() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved6            = ")
            .append("0x").append(HexDump.toHex(  getReserved6 ()))
            .append(" (").append( getReserved6() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .textLength           = ")
            .append("0x").append(HexDump.toHex(  getTextLength ()))
            .append(" (").append( getTextLength() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formattingRunLength  = ")
            .append("0x").append(HexDump.toHex(  getFormattingRunLength ()))
            .append(" (").append( getFormattingRunLength() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .reserved7            = ")
            .append("0x").append(HexDump.toHex(  getReserved7 ()))
            .append(" (").append( getReserved7() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/TXO]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_options);
        LittleEndian.putShort(data, 6 + offset + pos, field_2_textOrientation);
        LittleEndian.putShort(data, 8 + offset + pos, field_3_reserved4);
        LittleEndian.putShort(data, 10 + offset + pos, field_4_reserved5);
        LittleEndian.putShort(data, 12 + offset + pos, field_5_reserved6);
        LittleEndian.putShort(data, 14 + offset + pos, field_6_textLength);
        LittleEndian.putShort(data, 16 + offset + pos, field_7_formattingRunLength);
        LittleEndian.putInt(data, 18 + offset + pos, field_8_reserved7);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 4;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        TextObjectBaseRecord rec = new TextObjectBaseRecord();
    
        rec.field_1_options = field_1_options;
        rec.field_2_textOrientation = field_2_textOrientation;
        rec.field_3_reserved4 = field_3_reserved4;
        rec.field_4_reserved5 = field_4_reserved5;
        rec.field_5_reserved6 = field_5_reserved6;
        rec.field_6_textLength = field_6_textLength;
        rec.field_7_formattingRunLength = field_7_formattingRunLength;
        rec.field_8_reserved7 = field_8_reserved7;
        return rec;
    }




    /**
     * Get the options field for the TextObjectBase record.
     */
    public short getOptions()
    {
        return field_1_options;
    }

    /**
     * Set the options field for the TextObjectBase record.
     */
    public void setOptions(short field_1_options)
    {
        this.field_1_options = field_1_options;
    }

    /**
     * Get the text orientation field for the TextObjectBase record.
     *
     * @return  One of 
     *        TEXT_ORIENTATION_NONE
     *        TEXT_ORIENTATION_TOP_TO_BOTTOM
     *        TEXT_ORIENTATION_ROT_RIGHT
     *        TEXT_ORIENTATION_ROT_LEFT
     */
    public short getTextOrientation()
    {
        return field_2_textOrientation;
    }

    /**
     * Set the text orientation field for the TextObjectBase record.
     *
     * @param field_2_textOrientation
     *        One of 
     *        TEXT_ORIENTATION_NONE
     *        TEXT_ORIENTATION_TOP_TO_BOTTOM
     *        TEXT_ORIENTATION_ROT_RIGHT
     *        TEXT_ORIENTATION_ROT_LEFT
     */
    public void setTextOrientation(short field_2_textOrientation)
    {
        this.field_2_textOrientation = field_2_textOrientation;
    }

    /**
     * Get the reserved4 field for the TextObjectBase record.
     */
    public short getReserved4()
    {
        return field_3_reserved4;
    }

    /**
     * Set the reserved4 field for the TextObjectBase record.
     */
    public void setReserved4(short field_3_reserved4)
    {
        this.field_3_reserved4 = field_3_reserved4;
    }

    /**
     * Get the reserved5 field for the TextObjectBase record.
     */
    public short getReserved5()
    {
        return field_4_reserved5;
    }

    /**
     * Set the reserved5 field for the TextObjectBase record.
     */
    public void setReserved5(short field_4_reserved5)
    {
        this.field_4_reserved5 = field_4_reserved5;
    }

    /**
     * Get the reserved6 field for the TextObjectBase record.
     */
    public short getReserved6()
    {
        return field_5_reserved6;
    }

    /**
     * Set the reserved6 field for the TextObjectBase record.
     */
    public void setReserved6(short field_5_reserved6)
    {
        this.field_5_reserved6 = field_5_reserved6;
    }

    /**
     * Get the text length field for the TextObjectBase record.
     */
    public short getTextLength()
    {
        return field_6_textLength;
    }

    /**
     * Set the text length field for the TextObjectBase record.
     */
    public void setTextLength(short field_6_textLength)
    {
        this.field_6_textLength = field_6_textLength;
    }

    /**
     * Get the formatting run length field for the TextObjectBase record.
     */
    public short getFormattingRunLength()
    {
        return field_7_formattingRunLength;
    }

    /**
     * Set the formatting run length field for the TextObjectBase record.
     */
    public void setFormattingRunLength(short field_7_formattingRunLength)
    {
        this.field_7_formattingRunLength = field_7_formattingRunLength;
    }

    /**
     * Get the reserved7 field for the TextObjectBase record.
     */
    public int getReserved7()
    {
        return field_8_reserved7;
    }

    /**
     * Set the reserved7 field for the TextObjectBase record.
     */
    public void setReserved7(int field_8_reserved7)
    {
        this.field_8_reserved7 = field_8_reserved7;
    }

    /**
     * Sets the reserved1 field value.
     * reserved field
     */
    public void setReserved1(boolean value)
    {
        field_1_options = reserved1.setShortBoolean(field_1_options, value);
    }

    /**
     * reserved field
     * @return  the reserved1 field value.
     */
    public boolean isReserved1()
    {
        return reserved1.isSet(field_1_options);
    }

    /**
     * Sets the Horizontal text alignment field value.
     * 
     */
    public void setHorizontalTextAlignment(short value)
    {
        field_1_options = HorizontalTextAlignment.setShortValue(field_1_options, value);
    }

    /**
     * 
     * @return  the Horizontal text alignment field value.
     */
    public short getHorizontalTextAlignment()
    {
        return HorizontalTextAlignment.getShortValue(field_1_options);
    }

    /**
     * Sets the Vertical text alignment field value.
     * 
     */
    public void setVerticalTextAlignment(short value)
    {
        field_1_options = VerticalTextAlignment.setShortValue(field_1_options, value);
    }

    /**
     * 
     * @return  the Vertical text alignment field value.
     */
    public short getVerticalTextAlignment()
    {
        return VerticalTextAlignment.getShortValue(field_1_options);
    }

    /**
     * Sets the reserved2 field value.
     * 
     */
    public void setReserved2(short value)
    {
        field_1_options = reserved2.setShortValue(field_1_options, value);
    }

    /**
     * 
     * @return  the reserved2 field value.
     */
    public short getReserved2()
    {
        return reserved2.getShortValue(field_1_options);
    }

    /**
     * Sets the text locked field value.
     * Text has been locked
     */
    public void setTextLocked(boolean value)
    {
        field_1_options = textLocked.setShortBoolean(field_1_options, value);
    }

    /**
     * Text has been locked
     * @return  the text locked field value.
     */
    public boolean isTextLocked()
    {
        return textLocked.isSet(field_1_options);
    }

    /**
     * Sets the reserved3 field value.
     * 
     */
    public void setReserved3(short value)
    {
        field_1_options = reserved3.setShortValue(field_1_options, value);
    }

    /**
     * 
     * @return  the reserved3 field value.
     */
    public short getReserved3()
    {
        return reserved3.getShortValue(field_1_options);
    }
}
