
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

import java.io.UnsupportedEncodingException;

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
 * @author Glen Stampoultzis (glens at apache.org)
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
    private final int RICH_TEXT_BIT = 8;
    private final int EXT_BIT = 4;

    public UnicodeString()
    {
    }


    public int hashCode()
    {
        int stringHash = 0;
        if (field_3_string != null)
            stringHash = field_3_string.hashCode();
        return field_1_charCount + stringHash;
    }

    /**
     * Our handling of equals is inconsistent with compareTo.  The trouble is because we don't truely understand
     * rich text fields yet it's difficult to make a sound comparison.
     *
     * @param o     The object to compare.
     * @return      true if the object is actually equal.
     */
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
            try {
                field_3_string = new String(data, 3, getCharCount(), 
                                        StringUtil.getPreferredEncoding());
            } catch (UnsupportedEncodingException e) {
                // Extract the message out of our encoding
            	// error and then bubble a runtime exception.
            	String errorMessage = e.getMessage();
            	
            	// Make sure the message isn't null
            	if (errorMessage == null) {
            		errorMessage = e.toString();
           	}
                throw new RuntimeException(errorMessage);
            }
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

        // byte[] retval = new byte[ 3 + (getString().length() * charsize)];
        LittleEndian.putShort(data, 0 + offset, getCharCount());
        data[ 2 + offset ] = getOptionFlags();

//        System.out.println("Unicode: We've got "+retval[2]+" for our option flag");
        try {
            String unicodeString = new
String(getString().getBytes("Unicode"),"Unicode");
            if (getOptionFlags() == 0)
            {
                StringUtil.putCompressedUnicode(unicodeString, data, 0x3 +
offset);
            }
            else
            {
                StringUtil.putUncompressedUnicode(unicodeString, data,
                                                    0x3 + offset);
            }
        }
        catch (Exception e) {
            if (getOptionFlags() == 0)
            {
                StringUtil.putCompressedUnicode(getString(), data, 0x3 +
                                                offset);
            }
            else
            {
                StringUtil.putUncompressedUnicode(getString(), data,
                                                  0x3 + offset);
            }
        }
        return getRecordSize();
    }

    private boolean isUncompressedUnicode()
    {
        return (getOptionFlags() & 0x01) == 1;
    }

    public int getRecordSize()
    {
        int charsize = isUncompressedUnicode() ? 2 : 1;
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

    public boolean isRichText()
    {
        return (getOptionFlags() & RICH_TEXT_BIT) != 0;
    }

    int maxBrokenLength(final int proposedBrokenLength)
    {
        int rval = proposedBrokenLength;

        if (isUncompressedUnicode())
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

    public boolean isExtendedText()
    {
        return (getOptionFlags() & EXT_BIT) != 0;
    }

}
