
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
 * Title:        Style Record<P>
 * Description:  Describes a builtin to the gui or user defined style<P>
 * REFERENCE:  PG 390 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class StyleRecord
    extends Record
{
    public final static short sid                = 0x293;
    public final static short STYLE_USER_DEFINED = 0;
    public final static short STYLE_BUILT_IN     = 1;

    // shared by both user defined and builtin styles
    private short             field_1_xf_index;   // TODO: bitfield candidate

    // only for built in styles
    private byte              field_2_builtin_style;
    private byte              field_3_outline_style_level;

    // only for user defined styles
    private byte              field_2_name_length;
    private String            field_3_name;

    public StyleRecord()
    {
    }

    /**
     * Constructs a Style record and sets its fields appropriately.
     *
     * @param id     id must be 0x293 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public StyleRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Style record and sets its fields appropriately.
     *
     * @param id     id must be 0x293 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset
     */

    public StyleRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A STYLE RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_xf_index = LittleEndian.getShort(data, 0 + offset);
        if (getType() == 1)
        {
            field_2_builtin_style       = data[ 2 + offset ];
            field_3_outline_style_level = data[ 3 + offset ];
        }
        else if (getType() == 0)
        {
            field_2_name_length = data[ 2 + offset ];
            field_3_name        = new String(data, 3 + offset,
                                             LittleEndian.ubyteToInt(field_2_name_length));
        }

        // todo sanity check exception to make sure we're one or the other
    }

    /**
     * set the entire index field (including the type) (see bit setters that reference this method)
     *  @param index  bitmask
     */

    public void setIndex(short index)
    {
        field_1_xf_index = index;
    }

    // bitfields for field 1

    /**
     * set the type of the style (builtin or user-defined)
     * @see #STYLE_USER_DEFINED
     * @see #STYLE_BUILT_IN
     * @param type of style (userdefined/builtin)
     * @see #setIndex(short)
     */

    public void setType(short type)
    {
        field_1_xf_index = setField(field_1_xf_index, type, 0x8000, 15);
    }

    /**
     * set the actual index of the style extended format record
     * @see #setIndex(short)
     * @param index of the xf record
     */

    public void setXFIndex(short index)
    {
        field_1_xf_index = setField(field_1_xf_index, index, 0x1FFF, 0);
    }

    // end bitfields
    // only for user defined records

    /**
     * if this is a user defined record set the length of the style name
     * @param length of the style's name
     * @see #setName(String)
     */

    public void setNameLength(byte length)
    {
        field_2_name_length = length;
    }

    /**
     * set the style's name
     * @param name of the style
     * @see #setNameLength(byte)
     */

    public void setName(String name)
    {
        field_3_name = name;
    }

    // end user defined
    // only for buildin records

    /**
     * if this is a builtin style set teh number of the built in style
     * @param  builtin style number (0-7)
     *
     */

    public void setBuiltin(byte builtin)
    {
        field_2_builtin_style = builtin;
    }

    /**
     * set the row or column level of the style (if builtin 1||2)
     */

    public void setOutlineStyleLevel(byte level)
    {
        field_3_outline_style_level = level;
    }

    // end builtin records
    // field 1

    /**
     * get the entire index field (including the type) (see bit getters that reference this method)
     *  @return bitmask
     */

    public short getIndex()
    {
        return field_1_xf_index;
    }

    // bitfields for field 1

    /**
     * get the type of the style (builtin or user-defined)
     * @see #STYLE_USER_DEFINED
     * @see #STYLE_BUILT_IN
     * @return type of style (userdefined/builtin)
     * @see #getIndex()
     */

    public short getType()
    {
        return ( short ) ((field_1_xf_index & 0x8000) >> 15);
    }

    /**
     * get the actual index of the style extended format record
     * @see #getIndex()
     * @return index of the xf record
     */

    public short getXFIndex()
    {
        return ( short ) (field_1_xf_index & 0x1FFF);
    }

    // end bitfields
    // only for user defined records

    /**
     * if this is a user defined record get the length of the style name
     * @return length of the style's name
     * @see #getName()
     */

    public byte getNameLength()
    {
        return field_2_name_length;
    }

    /**
     * get the style's name
     * @return name of the style
     * @see #getNameLength()
     */

    public String getName()
    {
        return field_3_name;
    }

    // end user defined
    // only for buildin records

    /**
     * if this is a builtin style get the number of the built in style
     * @return  builtin style number (0-7)
     *
     */

    public byte getBuiltin()
    {
        return field_2_builtin_style;
    }

    /**
     * get the row or column level of the style (if builtin 1||2)
     */

    public byte getOutlineStyleLevel()
    {
        return field_3_outline_style_level;
    }

    // end builtin records
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[STYLE]\n");
        buffer.append("    .xf_index_raw    = ")
            .append(Integer.toHexString(getIndex())).append("\n");
        buffer.append("        .type        = ")
            .append(Integer.toHexString(getType())).append("\n");
        buffer.append("        .xf_index    = ")
            .append(Integer.toHexString(getXFIndex())).append("\n");
        if (getType() == STYLE_BUILT_IN)
        {
            buffer.append("    .builtin_style   = ")
                .append(Integer.toHexString(getBuiltin())).append("\n");
            buffer.append("    .outline_level   = ")
                .append(Integer.toHexString(getOutlineStyleLevel()))
                .append("\n");
        }
        else if (getType() == STYLE_USER_DEFINED)
        {
            buffer.append("    .name_length     = ")
                .append(Integer.toHexString(getNameLength())).append("\n");
            buffer.append("    .name            = ").append(getName())
                .append("\n");
        }
        buffer.append("[/STYLE]\n");
        return buffer.toString();
    }

    private short setField(int fieldValue, int new_value, int mask,
                           int shiftLeft)
    {
        return ( short ) ((fieldValue & ~mask)
                          | ((new_value << shiftLeft) & mask));
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        if (getType() == STYLE_BUILT_IN)
        {
            LittleEndian.putShort(data, 2 + offset,
                                  (( short ) 0x04));   // 4 bytes (8 total)
        }
        else
        {
            LittleEndian.putShort(data, 2 + offset,
                                  (( short ) (0x03 + getNameLength())));
        }
        LittleEndian.putShort(data, 4 + offset, getIndex());
        if (getType() == STYLE_BUILT_IN)
        {
            data[ 6 + offset ] = getBuiltin();
            data[ 7 + offset ] = getOutlineStyleLevel();
        }
        else
        {
            data[ 6 + offset ] = getNameLength();
            StringUtil.putCompressedUnicode(getName(), data, 7 + offset);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval;

        if (getType() == STYLE_BUILT_IN)
        {
            retval = 8;
        }
        else
        {
            retval = 7 + getNameLength();
        }
        return retval;
    }

    public short getSid()
    {
        return this.sid;
    }
}
