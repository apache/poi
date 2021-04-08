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

/**
 * Row height for rows with undefined or not explicitly defined heights.
 *
 * @version 2.0-pre
 */

public final class DefaultRowHeightRecord extends StandardRecord {
    public static final short sid = 0x225;

    /** The default row height for empty rows is 255 twips (255 / 20 == 12.75 points) */
    public static final short DEFAULT_ROW_HEIGHT = 0xFF;

    private short field_1_option_flags;
    private short field_2_row_height;

    public DefaultRowHeightRecord() {
        field_1_option_flags = 0x0000;
        field_2_row_height = DEFAULT_ROW_HEIGHT;
    }

    public DefaultRowHeightRecord(DefaultRowHeightRecord other) {
        super(other);
        field_1_option_flags = other.field_1_option_flags;
        field_2_row_height = other.field_2_row_height;
    }

    public DefaultRowHeightRecord(RecordInputStream in) {
        field_1_option_flags = in.readShort();
        field_2_row_height   = in.readShort();
    }

    /**
     * set the (currently unimportant to HSSF) option flags
     * @param flags the bitmask to set
     */

    public void setOptionFlags(short flags)
    {
        field_1_option_flags = flags;
    }

    /**
     * set the default row height
     * @param height    for undefined rows/rows w/undefined height
     */

    public void setRowHeight(short height)
    {
        field_2_row_height = height;
    }

    /**
     * get the (currently unimportant to HSSF) option flags
     * @return flags - the current bitmask
     */

    public short getOptionFlags()
    {
        return field_1_option_flags;
    }

    /**
     * get the default row height
     * @return rowheight for undefined rows/rows w/undefined height
     */

    public short getRowHeight()
    {
        return field_2_row_height;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getOptionFlags());
        out.writeShort(getRowHeight());
    }

    protected int getDataSize() {
        return 4;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DefaultRowHeightRecord copy() {
        return new DefaultRowHeightRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DEFAULT_ROW_HEIGHT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "optionFlags", this::getOptionFlags,
            "rowHeight", this::getRowHeight
        );
    }
}
