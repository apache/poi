
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

/**
 * Title:        Footer Record <P>
 * Description:  Specifies the footer for a sheet<P>
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn Laubach (slaubach at apache dot org) Modified 3/14/02
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class FooterRecord
    extends Record
{
    public final static short sid = 0x15;
    private byte              field_1_footer_len;
    private String            field_2_footer;

    public FooterRecord()
    {
    }

    /**
     * Constructs a FooterRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x15 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public FooterRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a FooterRecord record and sets its fields appropriately.
     *
     * @param id     id must be 0x15 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public FooterRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A FooterRECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        if (size > 0)
        {
            field_1_footer_len = data[ 0 + offset ];
            field_2_footer     = StringUtil.getFromCompressedUnicode(data, 3 + offset, // [Shawn] Changed 1 to 3 for offset of string
                                            LittleEndian.ubyteToInt( field_1_footer_len) );
        }
    }

    /**
     * set the length of the footer string
     *
     * @param len  length of the footer string
     * @see #setFooter(String)
     */

    public void setFooterLength(byte len)
    {
        field_1_footer_len = len;
    }

    /**
     * set the footer string
     *
     * @param footer string to display
     * @see #setFooterLength(byte)
     */

    public void setFooter(String footer)
    {
        field_2_footer = footer;
    }

    /**
     * get the length of the footer string
     *
     * @return length of the footer string
     * @see #getFooter()
     */

    public short getFooterLength()
    {
        return (short)(0xFF & field_1_footer_len); // [Shawn] Fixed needing unsigned byte
    }

    /**
     * get the footer string
     *
     * @return footer string to display
     * @see #getFooterLength()
     */

    public String getFooter()
    {
        return field_2_footer;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FOOTER]\n");
        buffer.append("    .footerlen      = ")
            .append(Integer.toHexString(getFooterLength())).append("\n");
        buffer.append("    .footer         = ").append(getFooter())
            .append("\n");
        buffer.append("[/FOOTER]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        int len = 4;

        if (getFooterLength() > 0)
        {
            len+=3; // [Shawn] Fixed for two null bytes in the length
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) ((len - 4) + getFooterLength()));
        if (getFooterLength() > 0)
        {
            data[ 4 + offset ] = (byte)getFooterLength();
            StringUtil.putCompressedUnicode(getFooter(), data, 7 + offset); // [Shawn] Place the string in the correct offset
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval = 4;

        if (getFooterLength() > 0)
        {
            retval+=3; // [Shawn] Fixed for two null bytes in the length
        }
        return retval + getFooterLength();
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      FooterRecord rec = new FooterRecord();
      rec.field_1_footer_len = field_1_footer_len;
      rec.field_2_footer = field_2_footer;
      return rec;
    }
}
