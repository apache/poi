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

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * Defines a comment associated with a specified name.
 */
public final class NameCommentRecord extends StandardRecord {
    public static final short sid = 0x0894;

    private final short field_1_record_type;
    private final short field_2_frt_cell_ref_flag;
    private final long field_3_reserved;
    //private short field_4_name_length;
    //private short field_5_comment_length;
    private String field_6_name_text;
    private String field_7_comment_text;

    public NameCommentRecord(NameCommentRecord other) {
        field_1_record_type = other.field_1_record_type;
        field_2_frt_cell_ref_flag = other.field_2_frt_cell_ref_flag;
        field_3_reserved = other.field_3_reserved;
        field_6_name_text = other.field_6_name_text;
        field_7_comment_text = other.field_7_comment_text;
    }

    public NameCommentRecord(final String name, final String comment) {
        field_1_record_type = 0;
        field_2_frt_cell_ref_flag = 0;
        field_3_reserved = 0;
        field_6_name_text = name;
        field_7_comment_text = comment;
    }

    /**
     * @param ris the RecordInputstream to read the record from
     */
    public NameCommentRecord(final RecordInputStream ris) {
        field_1_record_type = ris.readShort();
        field_2_frt_cell_ref_flag = ris.readShort();
        field_3_reserved = ris.readLong();
        final int field_4_name_length = ris.readShort();
        final int field_5_comment_length = ris.readShort();

        if (ris.readByte() == 0) {
            field_6_name_text = StringUtil.readCompressedUnicode(ris, field_4_name_length);
        } else {
            field_6_name_text = StringUtil.readUnicodeLE(ris, field_4_name_length);
        }
        if (ris.readByte() == 0) {
            field_7_comment_text = StringUtil.readCompressedUnicode(ris, field_5_comment_length);
        } else {
            field_7_comment_text = StringUtil.readUnicodeLE(ris, field_5_comment_length);
        }
    }

    @Override
    public void serialize(final LittleEndianOutput out) {
        final int field_4_name_length = field_6_name_text.length();
        final int field_5_comment_length = field_7_comment_text.length();

        out.writeShort(field_1_record_type);
        out.writeShort(field_2_frt_cell_ref_flag);
        out.writeLong(field_3_reserved);
        out.writeShort(field_4_name_length);
        out.writeShort(field_5_comment_length);

        boolean isNameMultiByte = StringUtil.hasMultibyte(field_6_name_text);
        out.writeByte(isNameMultiByte ? 1 : 0);
        if (isNameMultiByte) {
            StringUtil.putUnicodeLE(field_6_name_text, out);
        } else {
            StringUtil.putCompressedUnicode(field_6_name_text, out);
        }
        boolean isCommentMultiByte = StringUtil.hasMultibyte(field_7_comment_text);
        out.writeByte(isCommentMultiByte ? 1 : 0);
        if (isCommentMultiByte) {
            StringUtil.putUnicodeLE(field_7_comment_text, out);
        } else {
            StringUtil.putCompressedUnicode(field_7_comment_text, out);
        }
    }

    @Override
    protected int getDataSize() {
        return 18 // 4 shorts + 1 long + 2 spurious 'nul's
                + (StringUtil.hasMultibyte(field_6_name_text) ? field_6_name_text.length() * 2 : field_6_name_text.length())
                + (StringUtil.hasMultibyte(field_7_comment_text) ? field_7_comment_text.length() * 2 : field_7_comment_text.length());
    }

    /**
     * return the non static version of the id for this record.
     */
    @Override
    public short getSid() {
        return sid;
    }

    /**
     * @return the name of the NameRecord to which this comment applies.
     */
    public String getNameText() {
        return field_6_name_text;
    }

    /**
     * Updates the name we're associated with, normally used
     * when renaming that Name
     *
     * @param newName the new name
     */
    public void setNameText(String newName) {
        field_6_name_text = newName;
    }

    /**
     * @return the text of the comment.
     */
    public String getCommentText() {
        return field_7_comment_text;
    }

    public void setCommentText(String comment) {
        field_7_comment_text = comment;
    }

    public short getRecordType() {
        return field_1_record_type;
    }

    @Override
    public NameCommentRecord copy() {
        return new NameCommentRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.NAME_COMMENT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "recordType", this::getRecordType,
            "frtCellRefFlag", () -> field_2_frt_cell_ref_flag,
            "reserved", () -> field_3_reserved,
            "name", this::getNameText,
            "comment", this::getCommentText
        );
    }
}
