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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Header Record<P>
 * Description:  Specifies a header for a sheet<P>
 * REFERENCE:  PG 321 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn Laubach (slaubach at apache dot org) Modified 3/14/02
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class HeaderRecord extends Record {
    public final static short sid = 0x0014;
    private byte              field_1_header_len;
    private byte              field_2_reserved;
    private byte              field_3_unicode_flag;
    private String            field_4_header;

    public HeaderRecord()
    {
    }

    public HeaderRecord(RecordInputStream in)
    {
        if (in.remaining() > 0)
        {
            field_1_header_len = in.readByte();
            /** These two fields are a bit odd. They are not documented*/
            field_2_reserved = in.readByte();
            field_3_unicode_flag = in.readByte();						// unicode

                         if(isMultibyte())
                         {
                field_4_header = in.readUnicodeLEString(LittleEndian.ubyteToInt( field_1_header_len));
                         }
                         else
                         {
                field_4_header = in.readCompressedUnicode(LittleEndian.ubyteToInt( field_1_header_len));
                         }
        }
    }

    /**
     * see the unicode flag
     *
     * @return boolean flag
     *  true:footer string has at least one multibyte character
     */
     public boolean isMultibyte() {
         return ((field_3_unicode_flag & 0xFF) == 1);
    }

    /**
     * set the length of the header string
     *
     * @param len  length of the header string
     * @see #setHeader(String)
     */

    public void setHeaderLength(byte len)
    {
        field_1_header_len = len;
    }

    /**
     * set the header string
     *
     * @param header string to display
     * @see #setHeaderLength(byte)
     */

    public void setHeader(String header)
    {
        field_4_header = header;
        field_3_unicode_flag = 
            (byte) (StringUtil.hasMultibyte(field_4_header) ? 1 : 0);
        
        // Check it'll fit into the space in the record
        if(field_4_header == null) return;
        if(field_3_unicode_flag == 1) {
        	if(field_4_header.length() > 127) {
        		throw new IllegalArgumentException("Header string too long (limit is 127 for unicode strings)");
        	}
        } else {
        	if(field_4_header.length() > 255) {
        		throw new IllegalArgumentException("Header string too long (limit is 255 for non-unicode strings)");
        	}
        }
    }

    /**
     * get the length of the header string
     *
     * @return length of the header string
     * @see #getHeader()
     */

    public short getHeaderLength()
    {
        return (short)(0xFF & field_1_header_len); // [Shawn] Fixed needing unsigned byte
    }

    /**
     * get the header string
     *
     * @return header string to display
     * @see #getHeaderLength()
     */

    public String getHeader()
    {
        return field_4_header;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[HEADER]\n");
        buffer.append("    .length         = ").append(getHeaderLength())
            .append("\n");
        buffer.append("    .header         = ").append(getHeader())
            .append("\n");
        buffer.append("[/HEADER]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        int len = 4;

        if (getHeaderLength() != 0)
        {
            len+=3; // [Shawn] Fixed for two null bytes in the length
        }
        short bytelen = (short)(isMultibyte() ?
            getHeaderLength()*2 : getHeaderLength() );
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) ((len - 4) + bytelen));

        if (getHeaderLength() > 0)
        {
            data[ 4 + offset ] = (byte)getHeaderLength();
            data[ 6 + offset ] = field_3_unicode_flag;
            if(isMultibyte())
            {
                StringUtil.putUnicodeLE(getHeader(), data, 7 + offset);
            }
            else
            {
                StringUtil.putCompressedUnicode(getHeader(), data, 7 + offset); // [Shawn] Place the string in the correct offset
            }
        }
        return getRecordSize();
    }

    protected int getDataSize() {
        int retval = 0;

        if (getHeaderLength() != 0) {
            retval+=3; // [Shawn] Fixed for two null bytes in the length
        }
       return retval + getHeaderLength() * (isMultibyte() ? 2 : 1);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      HeaderRecord rec = new HeaderRecord();
      rec.field_1_header_len = field_1_header_len;
      rec.field_2_reserved = field_2_reserved;
      rec.field_3_unicode_flag = field_3_unicode_flag;
      rec.field_4_header = field_4_header;
      return rec;
    }
}
