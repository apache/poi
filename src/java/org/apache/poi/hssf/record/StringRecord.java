/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Supports the STRING record structure.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class StringRecord
        extends Record
{
    public final static short sid = 0x207;
    private int field_1_string_length;
    private byte field_2_unicode_flag;
    private String field_3_string;


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
            field_3_string = StringUtil.getFromUnicode(data, 3 + offset, field_1_string_length );
        }
        else
        {
            field_3_string = new String(data, 3 + offset, getStringLength());
        }
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
        LittleEndian.putShort(data, 2 + offset, ( short ) (2 + getStringByteLength()));
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

}
