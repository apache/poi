
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
 * Flag specifying whether 1904 date windowing is used.
 *
 * @version 2.0-pre
 */

public final class DateWindow1904Record extends StandardRecord {
    public static final short sid = 0x22;

    private short field_1_window;

    public DateWindow1904Record() {}

    public DateWindow1904Record(DateWindow1904Record other) {
        super(other);
        field_1_window = other.field_1_window;
    }

    public DateWindow1904Record(RecordInputStream in) {
        field_1_window = in.readShort();
    }

    /**
     * sets whether or not to use 1904 date windowing (which means you'll be screwed in 2004)
     * @param window flag - 0/1 (false,true)
     */

    public void setWindowing(short window)
    {   // I hate using numbers in method names so I wont!
        field_1_window = window;
    }

    /**
     * gets whether or not to use 1904 date windowing (which means you'll be screwed in 2004)
     * @return window flag - 0/1 (false,true)
     */

    public short getWindowing()
    {
        return field_1_window;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getWindowing());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public DateWindow1904Record copy() {
        return new DateWindow1904Record(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DATE_WINDOW_1904;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("is1904", this::getWindowing);
    }
}
