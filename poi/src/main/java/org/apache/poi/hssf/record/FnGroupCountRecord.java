
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
 * umber of built in function groups in the current version of the Spreadsheet (probably only used on Windows)
 *
 * @version 2.0-pre
 */
public final class FnGroupCountRecord extends StandardRecord {
    public static final short sid   = 0x9c;

    /**
     * suggested default (14 dec)
     */

    public static final short COUNT = 14;
    private short             field_1_count;

    public FnGroupCountRecord() {}

    public FnGroupCountRecord(FnGroupCountRecord other) {
        super(other);
        field_1_count = other.field_1_count;
    }

    public FnGroupCountRecord(RecordInputStream in)
    {
        field_1_count = in.readShort();
    }

    /**
     * set the number of built-in functions
     *
     * @param count - number of functions
     */

    public void setCount(short count)
    {
        field_1_count = count;
    }

    /**
     * get the number of built-in functions
     *
     * @return number of built-in functions
     */

    public short getCount()
    {
        return field_1_count;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getCount());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public FnGroupCountRecord copy() {
        return new FnGroupCountRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FN_GROUP_COUNT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("count", this::getCount);
    }
}
