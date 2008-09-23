
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
    private byte              field_2_reserved;
    private byte              field_3_unicode_flag;
    private String            field_4_footer;

    public FooterRecord()
    {
    }

    /**
     * Constructs a FooterRecord record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public FooterRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A FooterRECORD");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        if (in.remaining() > 0)
        {
            field_1_footer_len = in.readByte();
            /** These two fields are a bit odd. They are not documented*/
            field_2_reserved = in.readByte();
            field_3_unicode_flag = in.readByte();						// unicode
    
                         if(isMultibyte())
                         {
                field_4_footer = in.readUnicodeLEString(LittleEndian.ubyteToInt( field_1_footer_len));
                         }
                         else
                         {
                field_4_footer = in.readCompressedUnicode(LittleEndian.ubyteToInt( field_1_footer_len));
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
        field_4_footer = footer;
        field_3_unicode_flag = 
            (byte) (StringUtil.hasMultibyte(field_4_footer) ? 1 : 0);
        // Check it'll fit into the space in the record
        
        if(field_4_footer == null) return;
        if(field_3_unicode_flag == 1) {
        	if(field_4_footer.length() > 127) {
        		throw new IllegalArgumentException("Footer string too long (limit is 127 for unicode strings)");
        	}
        } else {
        	if(field_4_footer.length() > 255) {
        		throw new IllegalArgumentException("Footer string too long (limit is 255 for non-unicode strings)");
        	}
        }
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
        return field_4_footer;
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
        short bytelen = (short)(isMultibyte() ?
            getFooterLength()*2 : getFooterLength() );
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,
                              ( short ) ((len - 4) + bytelen ));
        if (getFooterLength() > 0)
        {
            data[ 4 + offset ] = (byte)getFooterLength();
            data[ 6 + offset ] = field_3_unicode_flag;
            if(isMultibyte())
            {
                StringUtil.putUnicodeLE(getFooter(), data, 7 + offset);
            }
            else
            {
                StringUtil.putCompressedUnicode(getFooter(), data, 7 + offset); // [Shawn] Place the string in the correct offset
            }
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
        return (isMultibyte() ? 
            (retval + getFooterLength()*2) : (retval + getFooterLength()));
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      FooterRecord rec = new FooterRecord();
      rec.field_1_footer_len = field_1_footer_len;
      rec.field_2_reserved = field_2_reserved;
      rec.field_3_unicode_flag = field_3_unicode_flag;
      rec.field_4_footer = field_4_footer;
      return rec;
    }
}
