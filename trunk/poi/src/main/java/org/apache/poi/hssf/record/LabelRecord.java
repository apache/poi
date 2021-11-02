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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.RecordFormatException;

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.util.HexDump.toHex;

/**
 * Label Record (0x0204) - read only support for strings stored directly in the cell...
 * Don't use this (except to read), use LabelSST instead
 *
 * @see LabelSSTRecord
 */
public final class LabelRecord extends Record implements CellValueRecordInterface {
    private static final Logger LOG = LogManager.getLogger(LabelRecord.class);

    public static final short sid = 0x0204;

    private int    field_1_row;
    private short  field_2_column;
    private short  field_3_xf_index;
    private short  field_4_string_len;
    private byte   field_5_unicode_flag;
    private String field_6_value;

    /** Creates new LabelRecord */
    public LabelRecord() {}

    public LabelRecord(LabelRecord other) {
        super(other);
        field_1_row = other.field_1_row;
        field_2_column = other.field_2_column;
        field_3_xf_index = other.field_3_xf_index;
        field_4_string_len = other.field_4_string_len;
        field_5_unicode_flag = other.field_5_unicode_flag;
        field_6_value = other.field_6_value;
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public LabelRecord(RecordInputStream in) {
        field_1_row          = in.readUShort();
        field_2_column       = in.readShort();
        field_3_xf_index     = in.readShort();
        field_4_string_len   = in.readShort();
        field_5_unicode_flag = in.readByte();
        if (field_4_string_len > 0) {
            if (isUnCompressedUnicode()) {
                field_6_value = in.readUnicodeLEString(field_4_string_len);
            } else {
                field_6_value = in.readCompressedUnicode(field_4_string_len);
            }
        } else {
            field_6_value = "";
        }

        if (in.remaining() > 0) {
            LOG.atInfo().log("LabelRecord data remains: {} : {}", box(in.remaining()), toHex(in.readRemainder()));
        }
    }

/*
 * READ ONLY ACCESS... THIS IS FOR COMPATIBILITY ONLY...USE LABELSST! public
 */
    @Override
    public int getRow()
    {
        return field_1_row;
    }

    @Override
    public short getColumn()
    {
        return field_2_column;
    }

    @Override
    public short getXFIndex()
    {
        return field_3_xf_index;
    }

    /**
     * get the number of characters this string contains
     * @return number of characters
     */
    public short getStringLength()
    {
        return field_4_string_len;
    }

    /**
     * is this uncompressed unicode (16bit)?  Or just 8-bit compressed?
     * @return isUnicode - True for 16bit- false for 8bit
     */
    public boolean isUnCompressedUnicode()
    {
        return (field_5_unicode_flag & 0x01) != 0;
    }

    /**
     * get the value
     *
     * @return the text string
     * @see #getStringLength()
     */
    public String getValue()
    {
        return field_6_value;
    }

    /**
     * THROWS A RUNTIME EXCEPTION..  USE LABELSSTRecords.  YOU HAVE NO REASON to use LABELRecord!!
     */
    @Override
    public int serialize(int offset, byte [] data) {
        throw new RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST");
    }
    @Override
    public int getRecordSize() {
        throw new RecordFormatException("Label Records are supported READ ONLY...convert to LabelSST");
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    /**
     * NO-OP!
     */
    @Override
    public void setColumn(short col)
    {
    }

    /**
     * NO-OP!
     */
    @Override
    public void setRow(int row)
    {
    }

    /**
     * no op!
     */
    @Override
    public void setXFIndex(short xf) {}

    @Override
    public LabelRecord copy() {
        return new LabelRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.LABEL;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "column", this::getColumn,
            "xfIndex", this::getXFIndex,
            "stringLen", this::getStringLength,
            "unCompressedUnicode", this::isUnCompressedUnicode,
            "value", this::getValue
        );
    }
}
