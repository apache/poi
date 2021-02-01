
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
 * Contains a flag specifying whether the Gui should save externally linked values from other workbooks.
 *
 * @version 2.0-pre
 */

public final class BookBoolRecord extends StandardRecord {
    public static final short sid = 0xDA;

    private short field_1_save_link_values;

    public BookBoolRecord() {}

    public BookBoolRecord(BookBoolRecord other) {
        super(other);
        field_1_save_link_values = other.field_1_save_link_values;
    }

    public BookBoolRecord(RecordInputStream in) {
        field_1_save_link_values = in.readShort();
    }

    /**
     * set the save ext links flag
     *
     * @param flag flag (0/1 -off/on)
     */

    public void setSaveLinkValues(short flag)
    {
        field_1_save_link_values = flag;
    }

    /**
     * get the save ext links flag
     *
     * @return short 0/1 (off/on)
     */

    public short getSaveLinkValues()
    {
        return field_1_save_link_values;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_save_link_values);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public BookBoolRecord copy() {
        return new BookBoolRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BOOK_BOOL;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "saveLinkValues", this::getSaveLinkValues
        );
    }
}
