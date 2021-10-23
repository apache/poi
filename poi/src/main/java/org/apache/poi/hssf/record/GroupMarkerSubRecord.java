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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * ftGmo (0x0006)<p>
 * The group marker record is used as a position holder for groups.
 */
public final class GroupMarkerSubRecord extends SubRecord {
    public static final short sid = 0x0006;

    private static final byte[] EMPTY_BYTE_ARRAY = { };

    // would really love to know what goes in here.
    private byte[] reserved;

    public GroupMarkerSubRecord() {
        reserved = EMPTY_BYTE_ARRAY;
    }

    public GroupMarkerSubRecord(GroupMarkerSubRecord other) {
        super(other);
        reserved = other.reserved.clone();
    }

    public GroupMarkerSubRecord(LittleEndianInput in, int size) {
        this(in,size,-1);
    }

    GroupMarkerSubRecord(LittleEndianInput in, int size, int cmoOt) {
        byte[] buf = IOUtils.safelyAllocate(size, HSSFWorkbook.getMaxRecordLength());
        in.readFully(buf);
        reserved = buf;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(sid);
        out.writeShort(reserved.length);
        out.write(reserved);
    }

    protected int getDataSize() {
        return reserved.length;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public GroupMarkerSubRecord copy() {
        return new GroupMarkerSubRecord(this);
    }


    @Override
    public SubRecordTypes getGenericRecordType() {
        return SubRecordTypes.GROUP_MARKER;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("reserved", () -> reserved);
    }
}
