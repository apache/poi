
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
 * Defines a series name
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author Andrew C. Oliver (acoliver at apache.org)
 */
public class SeriesTextRecord
    extends Record
{
    public final static short      sid                             = 0x100d;
    private  short      field_1_id;
    private  byte       field_2_textLength;
    private  byte       field_3_undocumented;
    private  String     field_4_text;


    public SeriesTextRecord()
    {

    }

    /**
     * Constructs a SeriesText record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public SeriesTextRecord(RecordInputStream in)
    {
        super(in);
    
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a SeriesText record");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_id                     = in.readShort();
        field_2_textLength             = in.readByte();
        field_3_undocumented           = in.readByte();
        field_4_text                   = in.readUnicodeLEString(
        		LittleEndian.ubyteToInt(field_2_textLength));
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SERIESTEXT]\n");
        buffer.append("    .id                   = ")
            .append("0x").append(HexDump.toHex(  getId ()))
            .append(" (").append( getId() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .textLength           = ")
            .append("0x").append(HexDump.toHex(  getTextLength ()))
            .append(" (").append( getTextLength() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .undocumented         = ")
            .append("0x").append(HexDump.toHex(  getUndocumented ()))
            .append(" (").append( getUndocumented() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .text                 = ")
            .append(" (").append( getText() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 

        buffer.append("[/SERIESTEXT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset + pos, field_1_id);
        data[ 6 + offset + pos ] = field_2_textLength;
        data[ 7 + offset + pos ] = field_3_undocumented;
        StringUtil.putUnicodeLE(field_4_text, data, 8 + offset + pos);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4  + 2 + 1 + 1 + (field_2_textLength *2);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        SeriesTextRecord rec = new SeriesTextRecord();
    
        rec.field_1_id = field_1_id;
        rec.field_2_textLength = field_2_textLength;
        rec.field_3_undocumented = field_3_undocumented;
        rec.field_4_text = field_4_text;
        return rec;
    }




    /**
     * Get the id field for the SeriesText record.
     */
    public short getId()
    {
        return field_1_id;
    }

    /**
     * Set the id field for the SeriesText record.
     */
    public void setId(short field_1_id)
    {
        this.field_1_id = field_1_id;
    }

    /**
     * Get the text length field for the SeriesText record.
     */
    public int getTextLength()
    {
        return LittleEndian.ubyteToInt(field_2_textLength);
    }

    /**
     * Set the text length field for the SeriesText record.
     * Needs to be wrapped.
     */
    public void setTextLength(byte field_2_textLength)
    {
        this.field_2_textLength = field_2_textLength;
    }
    /**
     * Set the text length field for the SeriesText record.
     */
    public void setTextLength(int field_2_textLength)
    {
    	if(field_2_textLength > 255) {
    		throw new IllegalArgumentException("Length must be 0-255");
    	}
    	if(field_2_textLength > 127) {
    		this.field_2_textLength = (byte)
    			(field_2_textLength-256);
    	} else {
    		this.field_2_textLength = (byte)field_2_textLength;
    	}
    }

    /**
     * Get the undocumented field for the SeriesText record.
     */
    public byte getUndocumented()
    {
        return field_3_undocumented;
    }

    /**
     * Set the undocumented field for the SeriesText record.
     */
    public void setUndocumented(byte field_3_undocumented)
    {
        this.field_3_undocumented = field_3_undocumented;
    }

    /**
     * Get the text field for the SeriesText record.
     */
    public String getText()
    {
        return field_4_text;
    }

    /**
     * Set the text field for the SeriesText record.
     */
    public void setText(String field_4_text)
    {
        this.field_4_text = field_4_text;
    }


}  // END OF CLASS




