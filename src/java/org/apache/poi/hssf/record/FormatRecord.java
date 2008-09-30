
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
 * Title:        Format Record<P>
 * Description:  describes a number format -- those goofy strings like $(#,###)<P>
 *
 * REFERENCE:  PG 317 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Shawn M. Laubach (slaubach at apache dot org)  
 * @version 2.0-pre
 */

public class FormatRecord
    extends Record
{
    public final static short sid = 0x41e;
    private short             field_1_index_code;

    private short             field_3_unicode_len;      // unicode string length
    private boolean          field_3_unicode_flag;     // it is not undocumented - it is unicode flag
    private String            field_4_formatstring;

    public FormatRecord()
    {
    }

    public FormatRecord(RecordInputStream in)
    {
        field_1_index_code       = in.readShort();
        field_3_unicode_len      = in.readShort();
        field_3_unicode_flag     = ( in.readByte() & (byte)0x01 ) != 0;
                                              
      if ( field_3_unicode_flag  ) {
          // unicode
          field_4_formatstring = in.readUnicodeLEString( field_3_unicode_len );
      }
      else {
          // not unicode
          field_4_formatstring = in.readCompressedUnicode( field_3_unicode_len );
      }
    }

    /**
     * set the format index code (for built in formats)
     *
     * @param index  the format index code
     * @see org.apache.poi.hssf.model.Workbook
     */

    public void setIndexCode(short index)
    {
        field_1_index_code = index;
    }

    /**
     * set the format string length
     *
     * @param len  the length of the format string
     * @see #setFormatString(String)
     */
    
    public void setFormatStringLength(byte len)
    {

	field_3_unicode_len = len;
    }

    /**
     * set whether the string is unicode
     *
     * @param unicode flag for whether string is unicode
     */

    public void setUnicodeFlag(boolean unicode) {
	field_3_unicode_flag = unicode;
    }

    /**
     * set the format string
     *
     * @param fs  the format string
     * @see #setFormatStringLength(byte)
     */

    public void setFormatString(String fs)
    {
        field_4_formatstring = fs;
        setUnicodeFlag(StringUtil.hasMultibyte(fs));
    }

    /**
     * get the format index code (for built in formats)
     *
     * @return the format index code
     * @see org.apache.poi.hssf.model.Workbook
     */

    public short getIndexCode()
    {
        return field_1_index_code;
    }

    /**
     * get the format string length
     *
     * @return the length of the format string
     * @see #getFormatString()
     */

   /* public short getFormatStringLength()
    {
        return field_3_unicode_flag ? field_3_unicode_len : field_2_formatstring_len;
    }*/

    /**
     * get whether the string is unicode
     *
     * @return flag for whether string is unicode
     */

    public boolean getUnicodeFlag() {
	return field_3_unicode_flag;
    }    

    /**
     * get the format string
     *
     * @return the format string
     */

    public String getFormatString()
    {
        return field_4_formatstring;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FORMAT]\n");
        buffer.append("    .indexcode       = ")
            .append(Integer.toHexString(getIndexCode())).append("\n");
        /*
        buffer.append("    .formatstringlen = ")
            .append(Integer.toHexString(getFormatStringLength()))
            .append("\n");
        */
        buffer.append("    .unicode length  = ")
            .append(Integer.toHexString(field_3_unicode_len)).append("\n");
        buffer.append("    .isUnicode       = ")
            .append( field_3_unicode_flag ).append("\n");
        buffer.append("    .formatstring    = ").append(getFormatString())
            .append("\n");
        buffer.append("[/FORMAT]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)( 2 + 2 + 1 + ( (field_3_unicode_flag) 
                                                                  ? 2 * field_3_unicode_len 
                                                                  : field_3_unicode_len ) ) );
                                                  // index + len + flag + format string length
        LittleEndian.putShort(data, 4 + offset, getIndexCode());
        LittleEndian.putShort(data, 6 + offset, field_3_unicode_len);
        data[ 8 + offset ] = (byte)( (field_3_unicode_flag) ? 0x01 : 0x00 );

      if ( field_3_unicode_flag ) {
          // unicode
          StringUtil.putUnicodeLE( getFormatString(), data, 9 + offset );
      }
      else {
          // not unicode
          StringUtil.putCompressedUnicode( getFormatString(), data, 9 + offset );
      }
      
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 9 + ( ( field_3_unicode_flag ) ? 2 * field_3_unicode_len : field_3_unicode_len );
    }

    public short getSid()
    {
        return sid;
    }
}
