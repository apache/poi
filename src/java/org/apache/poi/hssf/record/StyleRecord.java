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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Style Record<P>
 * Description:  Describes a builtin to the gui or user defined style<P>
 * REFERENCE:  PG 390 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author aviks : string fixes for UserDefined Style
 * @version 2.0-pre
 */
public final class StyleRecord extends Record {
    public final static short sid = 0x0293;

    private static final BitField fHighByte = BitFieldFactory.getInstance(0x01);

    public final static short STYLE_USER_DEFINED = 0;
    public final static short STYLE_BUILT_IN     = 1;

    // shared by both user defined and builtin styles
    private short             field_1_xf_index;   // TODO: bitfield candidate

    // only for built in styles
    private byte              field_2_builtin_style;
    private byte              field_3_outline_style_level;

    // only for user defined styles
    private short              field_2_name_length; //OO doc says 16 bit length, so we believe
    private byte               field_3_string_options;
    private String             field_4_name;

    public StyleRecord()
    {
    }

    /**
     * Constructs a Style record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */

    public StyleRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void fillFields(RecordInputStream in)
    {
        field_1_xf_index = in.readShort();
        if (getType() == STYLE_BUILT_IN)
        {
            field_2_builtin_style       = in.readByte();
            field_3_outline_style_level = in.readByte();
        }
        else if (getType() == STYLE_USER_DEFINED)
        {
            field_2_name_length = in.readShort();
            
            // Some files from Crystal Reports lack
            //  the remaining fields, which is naughty
            if(in.remaining() > 0) {
	            field_3_string_options = in.readByte();
	            
	            byte[] string = in.readRemainder();
	            if (fHighByte.isSet(field_3_string_options)) {
	                field_4_name= StringUtil.getFromUnicodeBE(string, 0, field_2_name_length);
	            } else {
	                field_4_name=StringUtil.getFromCompressedUnicode(string, 0, field_2_name_length);
	            }
            }
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
        field_4_name = name;
        
        // Fix up the length
        field_2_name_length = (short)name.length();
        //TODO set name string options
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

    public short getNameLength()
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
        return field_4_name;
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
                                  (( short ) (getRecordSize()-4)));
        }
        LittleEndian.putShort(data, 4 + offset, getIndex());
        if (getType() == STYLE_BUILT_IN)
        {
            data[ 6 + offset ] = getBuiltin();
            data[ 7 + offset ] = getOutlineStyleLevel();
        }
        else
        {
            LittleEndian.putShort(data, 6 + offset , getNameLength());
            data[8+offset]=this.field_3_string_options;
            StringUtil.putCompressedUnicode(getName(), data, 9 + offset);
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
             if (fHighByte.isSet(field_3_string_options))  {
                 retval= 9+2*getNameLength();
             }else {
                retval = 9 + getNameLength();
             }
        }
        return retval;
    }

    public short getSid()
    {
        return sid;
    }
}
