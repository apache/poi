
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
 * Title: Unicode String<P>
 * Description:  Unicode String record.  We implement these as a record, although
 *               they are really just standard fields that are in several records.
 *               It is considered more desirable then repeating it in all of them.<P>
 * REFERENCE:  PG 264 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author  Andrew C. Oliver
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version 2.0-pre
 */

public class UnicodeString
    extends Record
    implements Comparable
{
    public final static short sid = 0xFFF;
    private short             field_1_charCount;     // = 0;
    private byte              field_2_optionflags;   // = 0;
    private String            field_3_string;        // = null;

    public int hashCode()
    {
        return field_1_charCount;
    }

    public boolean equals(Object o)
    {
        if ((o == null) || (o.getClass() != this.getClass()))
        {
            return false;
        }
        UnicodeString other = ( UnicodeString ) o;

        return ((field_1_charCount == other.field_1_charCount)
                && (field_2_optionflags == other.field_2_optionflags)
                && field_3_string.equals(other.field_3_string));
    }

    public UnicodeString()
    {
    }

    /**
     * construct a unicode string record and fill its fields, ID is ignored
     * @param id - ignored
     * @param size - size of the data
     * @param data - the bytes of the string/fields
     */

    public UnicodeString(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * construct a unicode string from a string fragment + data
     */

    public UnicodeString(short id, short size, byte [] data, String prefix)
    {
        this(id, size, data);
        field_3_string = prefix + field_3_string;
        setCharCount();
    }

    /**
     * NO OP
     */

    protected void validateSid(short id)
    {

        // included only for interface compliance
    }

    protected void fillFields(byte [] data, short size)
    {
        field_1_charCount   = LittleEndian.getShort(data, 0);
        field_2_optionflags = data[ 2 ];
        if ((field_2_optionflags & 1) == 0)
        {
            field_3_string = new String(data, 3, getCharCount());
        }
        else
        {
            char[] array = new char[ getCharCount() ];

            for (int j = 0; j < array.length; j++)
            {
                array[ j ] = ( char ) LittleEndian.getShort(data,
                                                            3 + (j * 2));
            }
            field_3_string = new String(array);
        }
    }

    /**
     * get the number of characters in the string
     *
     *
     * @return number of characters
     *
     */

    public short getCharCount()
    {
        return field_1_charCount;
    }

    /**
     * set the number of characters in the string
     * @param cc - number of characters
     */

    public void setCharCount(short cc)
    {
        field_1_charCount = cc;
    }

    /**
     * sets the number of characters to whaterver number of characters is in the string
     * currently.  effectively setCharCount(getString.length()).
     * @see #setString(String)
     * @see #getString()
     */

    public void setCharCount()
    {
        field_1_charCount = ( short ) field_3_string.length();
    }

    /**
     * get the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     *
     * @return optionflags bitmask
     *
     */

    public byte getOptionFlags()
    {
        return field_2_optionflags;
    }

    /**
     * set the option flags which among other things return if this is a 16-bit or
     * 8 bit string
     *
     * @param of  optionflags bitmask
     *
     */

    public void setOptionFlags(byte of)
    {
        field_2_optionflags = of;
    }

    /**
     * get the actual string this contains as a java String object
     *
     *
     * @return String
     *
     */

    public String getString()
    {
        return field_3_string;
    }

    /**
     * set the actual string this contains
     * @param string  the text
     */

    public void setString(String string)
    {
        field_3_string = string;
        if (getCharCount() < field_3_string.length())
        {
            setCharCount();
        }
    }

    /**
     * unlike the real records we return the same as "getString()" rather than debug info
     * @see #getDebugInfo()
     * @return String value of the record
     */

    public String toString()
    {
        return getString();
    }

    /**
     * return a character representation of the fields of this record
     *
     *
     * @return String of output for biffviewer etc.
     *
     */

    public String getDebugInfo()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[UNICODESTRING]\n");
        buffer.append("    .charcount       = ")
            .append(Integer.toHexString(getCharCount())).append("\n");
        buffer.append("    .optionflags     = ")
            .append(Integer.toHexString(getOptionFlags())).append("\n");
        buffer.append("    .string          = ").append(getString())
            .append("\n");
        buffer.append("[/UNICODESTRING]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        int charsize = 1;

        if (getOptionFlags() == 1)
        {
            charsize = 2;
        }

        // byte[] retval = new byte[ 3 + (getString().length() * charsize) ];
        LittleEndian.putShort(data, 0 + offset, getCharCount());
        data[ 2 + offset ] = getOptionFlags();

//        System.out.println("Unicode: We've got "+retval[2]+" for our option flag");
        if (getOptionFlags() == 0)
        {
            StringUtil.putCompressedUnicode(getString(), data, 0x3 + offset);
        }
        else
        {
            StringUtil.putUncompressedUnicode(getString(), data,
                                              0x3 + offset);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int charsize = 1;

        if (getOptionFlags() == 1)
        {
            charsize = 2;
        }
        return 3 + (getString().length() * charsize);
    }

    public short getSid()
    {
        return this.sid;
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the records data (provided a big array of the file)
     */

    protected void fillFields(byte [] data, short size, int offset)
    {
    }

    public int compareTo(Object obj)
    {
        UnicodeString str = ( UnicodeString ) obj;

        return this.getString().compareTo(str.getString());
    }

    int maxBrokenLength(final int proposedBrokenLength)
    {
        int rval = proposedBrokenLength;

        if ((field_2_optionflags & 1) == 1)
        {
            int proposedStringLength = proposedBrokenLength - 3;

            if ((proposedStringLength % 2) == 1)
            {
                proposedStringLength--;
            }
            rval = proposedStringLength + 3;
        }
        return rval;
    }

//    public boolean equals(Object obj) {
//        if (!(obj instanceof UnicodeString)) return false;
//        
//        UnicodeString str = (UnicodeString)obj;
//        
//        
//       return this.getString().equals(str.getString());
//    }    
}
