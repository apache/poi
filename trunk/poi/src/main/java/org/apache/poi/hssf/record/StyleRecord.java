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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.util.StringUtil;

/**
 * Describes a builtin to the gui or user defined style
 */
public final class StyleRecord extends StandardRecord {
    public static final short sid = 0x0293;

    private static final BitField styleIndexMask = BitFieldFactory.getInstance(0x0FFF);
    private static final BitField isBuiltinFlag  = BitFieldFactory.getInstance(0x8000);

    /** shared by both user defined and built-in styles */
    private int field_1_xf_index;

    // only for built in styles
    private int field_2_builtin_style;
    private int field_3_outline_style_level;

    // only for user defined styles
    private boolean field_3_stringHasMultibyte;
    private String field_4_name;

    /**
     * creates a new style record, initially set to 'built-in'
     */
    public StyleRecord() {
        field_1_xf_index = isBuiltinFlag.set(0);
    }

    public StyleRecord(StyleRecord other) {
        super(other);
        field_1_xf_index = other.field_1_xf_index;
        field_2_builtin_style = other.field_2_builtin_style;
        field_3_outline_style_level = other.field_3_outline_style_level;
        field_3_stringHasMultibyte = other.field_3_stringHasMultibyte;
        field_4_name = other.field_4_name;
    }

    public StyleRecord(RecordInputStream in) {
        field_1_xf_index = in.readShort();
        if (isBuiltin()) {
            field_2_builtin_style      = in.readByte();
            field_3_outline_style_level = in.readByte();
        } else {
            int field_2_name_length = in.readShort();

            if(in.remaining() < 1) {
                // Some files from Crystal Reports lack the is16BitUnicode byte
                //  the remaining fields, which is naughty
                if (field_2_name_length != 0) {
                    throw new RecordFormatException("Ran out of data reading style record");
                }
                // guess this is OK if the string length is zero
                field_4_name = "";
            } else {

                field_3_stringHasMultibyte = in.readByte() != 0x00;
                if (field_3_stringHasMultibyte) {
                    field_4_name = StringUtil.readUnicodeLE(in, field_2_name_length);
                } else {
                    field_4_name = StringUtil.readCompressedUnicode(in, field_2_name_length);
                }
            }
        }
    }

    /**
     * set the actual index of the style extended format record
     * @param xfIndex of the xf record
     */
    public void setXFIndex(int xfIndex) {
        field_1_xf_index = styleIndexMask.setValue(field_1_xf_index, xfIndex);
    }

    /**
     * get the actual index of the style extended format record
     * @see #getXFIndex()
     * @return index of the xf record
     */
    public int getXFIndex() {
        return styleIndexMask.getValue(field_1_xf_index);
    }

    /**
     * set the style's name
     * @param name of the style
     */
    public void setName(String name) {
        field_4_name = name;
        field_3_stringHasMultibyte = StringUtil.hasMultibyte(name);
        field_1_xf_index = isBuiltinFlag.clear(field_1_xf_index);
    }

    /**
     * if this is a builtin style set the number of the built in style
     * @param  builtinStyleId style number (0-7)
     *
     */
    public void setBuiltinStyle(int builtinStyleId) {
        field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
        field_2_builtin_style = builtinStyleId;
    }

    /**
     * set the row or column level of the style (if builtin 1||2)
     *
     * @param level The outline-level
     */
    public void setOutlineStyleLevel(int level) {
        field_3_outline_style_level = level & 0x00FF;
    }

    public boolean isBuiltin(){
        return isBuiltinFlag.isSet(field_1_xf_index);
    }

    /**
     * get the style's name
     * @return name of the style
     */
    public String getName() {
        return field_4_name;
    }

    @Override
    protected int getDataSize() {
        if (isBuiltin()) {
            return 4; // short, byte, byte
        }
        return 2 // short xf index
            + 3 // str len + flag
            + field_4_name.length() * (field_3_stringHasMultibyte ? 2 : 1);
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_xf_index);
        if (isBuiltin()) {
            out.writeByte(field_2_builtin_style);
            out.writeByte(field_3_outline_style_level);
        } else {
            out.writeShort(field_4_name.length());
            out.writeByte(field_3_stringHasMultibyte ? 0x01 : 0x00);
            if (field_3_stringHasMultibyte) {
                StringUtil.putUnicodeLE(getName(), out);
            } else {
                StringUtil.putCompressedUnicode(getName(), out);
            }
        }
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public StyleRecord copy() {
        return new StyleRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.STYLE;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "xfIndex", this::getXFIndex,
            "type", () -> isBuiltin() ? "built-in" : "user-defined",
            "builtin_style", () -> field_2_builtin_style,
            "outline_level", () -> field_3_outline_style_level,
            "name", this::getName
        );
    }
}
