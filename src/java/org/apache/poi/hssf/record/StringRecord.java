
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

import org.apache.poi.util.*;

import java.io.IOException;

/**
 * Supports the STRING record structure.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class StringRecord
        extends Record
{
    public final static short   sid = 0x207;
    private int                 field_1_string_length;
    private byte                field_2_unicode_flag;
    private String              field_3_string;


    public StringRecord()
    {
    }

    /**
     * Constructs a String record and sets its fields appropriately.
     *
     * @param id     id must be 0x204 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    public StringRecord( short id, short size, byte[] data )
    {
        super( id, size, data );
    }

    /**
     * Constructs an String record and sets its fields appropriately.
     *
     * @param id     id must be 0x204 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record
     */
    public StringRecord( short id, short size, byte[] data, int offset )
    {
        super( id, size, data, offset );
    }


    /**
     * Throw a runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */
    protected void validateSid( short id )
    {
        if (id != this.sid)
        {
            throw new RecordFormatException("Not a valid StringRecord");
        }
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */
    protected void fillFields( byte[] data, short size, int offset )
    {
        field_1_string_length           = LittleEndian.getUShort(data, 0 + offset);
        field_2_unicode_flag            = data[ 2 + offset ];
        if (isUnCompressedUnicode())
        {
            field_3_string = StringUtil.getFromUnicodeHigh(data, 3 + offset, field_1_string_length );
        }
        else
        {
            field_3_string = StringUtil.getFromCompressedUnicode(data, 3 + offset, field_1_string_length);
        }
    }

    public boolean isInValueSection()
    {
        return true;
    }

    private int getStringLength()
    {
        return field_1_string_length;
    }

    private int getStringByteLength()
    {
        return isUnCompressedUnicode() ? field_1_string_length * 2 : field_1_string_length;
    }

    /**
     * gives the current serialized size of the record. Should include the sid and reclength (4 bytes).
     */
    public int getRecordSize()
    {
        return 4 + 2 + 1 + getStringByteLength();
    }

    /**
     * is this uncompressed unicode (16bit)?  Or just 8-bit compressed?
     * @return isUnicode - True for 16bit- false for 8bit
     */
    public boolean isUnCompressedUnicode()
    {
        return (field_2_unicode_flag == 1);
    }

    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset to begin writing at
     * @param data byte array containing instance data
     * @return number of bytes written
     */
    public int serialize( int offset, byte[] data )
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (3 + getStringByteLength()));
        LittleEndian.putUShort(data, 4 + offset, field_1_string_length);
        data[6 + offset] = field_2_unicode_flag;
        if (isUnCompressedUnicode())
        {
            StringUtil.putUncompressedUnicode(field_3_string, data, 7 + offset);
        }
        else
        {
            StringUtil.putCompressedUnicode(field_3_string, data, 7 + offset);
        }
        return getRecordSize();
    }

    /**
     * return the non static version of the id for this record.
     */
    public short getSid()
    {
        return sid;
    }

    /**
     * @return The string represented by this record.
     */
    public String getString()
    {
        return field_3_string;
    }

    /**
     * Sets whether the string is compressed or not
     * @param unicode_flag   1 = uncompressed, 0 = compressed
     */
    public void setCompressedFlag( byte unicode_flag )
    {
        this.field_2_unicode_flag = unicode_flag;
    }

    /**
     * Sets the string represented by this record.
     */
    public void setString( String string )
    {
        this.field_1_string_length = string.length();
        this.field_3_string = string;
    }



    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[STRING]\n");
        buffer.append("    .string            = ")
            .append(field_3_string).append("\n");
        buffer.append("[/STRING]\n");
        return buffer.toString();
    }
    
    public Object clone() {
        StringRecord rec = new StringRecord();
        rec.field_1_string_length = this.field_1_string_length;
        rec.field_2_unicode_flag= this.field_2_unicode_flag;
        rec.field_3_string = this.field_3_string;
        return rec;

    }

}
