
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
     * @param id    id must be 0x100d or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public SeriesTextRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a SeriesText record and sets its fields appropriately.
     *
     * @param id    id must be 0x100d or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public SeriesTextRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
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

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_id                      = LittleEndian.getShort(data, 0x0 + offset);
        field_2_textLength              = data[ 0x2 + offset ];
        field_3_undocumented            = data[ 0x3 + offset ];
        field_4_text                    = StringUtil.getFromUnicodeHigh(data, 0x4 + offset, ((field_2_textLength *2)/2));

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SeriesText]\n");

        buffer.append("    .id                   = ")
            .append("0x")
            .append(HexDump.toHex((short)getId()))
            .append(" (").append(getId()).append(" )\n");

        buffer.append("    .textLength           = ")
            .append("0x")
            .append(HexDump.toHex((byte)getTextLength()))
            .append(" (").append(getTextLength()).append(" )\n");

        buffer.append("    .undocumented         = ")
            .append("0x")
            .append(HexDump.toHex((byte)getUndocumented()))
            .append(" (").append(getUndocumented()).append(" )\n");

        buffer.append("    .text                 = ")
            .append(" (").append(getText()).append(" )\n");

        buffer.append("[/SeriesText]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        LittleEndian.putShort(data, 4 + offset, field_1_id);
        data[ 6 + offset ] = field_2_textLength;
        data[ 7 + offset ] = field_3_undocumented;
        StringUtil.putUncompressedUnicodeHigh(field_4_text, data, 8 + offset);

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
        return this.sid;
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
    public byte getTextLength()
    {
        return field_2_textLength;
    }

    /**
     * Set the text length field for the SeriesText record.
     */
    public void setTextLength(byte field_2_textLength)
    {
        this.field_2_textLength = field_2_textLength;
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




