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

package org.apache.poi.hssf.record.chart;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * ENDOBJECT - Chart Future Record Type End Object (0x0855)
 */
public final class ChartEndObjectRecord extends StandardRecord {
    public static final short sid = 0x0855;

    private short rt;
    private short grbitFrt;
    private short iObjectKind;
    private byte[] reserved;

    public ChartEndObjectRecord(ChartEndObjectRecord other) {
        super(other);
        rt = other.rt;
        grbitFrt = other.grbitFrt;
        iObjectKind = other.iObjectKind;
        reserved = (other.reserved == null) ? null : other.reserved.clone();
    }

    public ChartEndObjectRecord(RecordInputStream in) {
        rt = in.readShort();
        grbitFrt = in.readShort();
        iObjectKind = in.readShort();

        // The spec says that there should be 6 bytes at the
        //  end, which must be there and must be zero
        // However, sometimes Excel forgets them...
        reserved = new byte[6];
        if(in.available() == 0) {
           // They've gone missing...
        } else {
           // Read the reserved bytes
           in.readFully(reserved);
        }
    }

    @Override
    protected int getDataSize() {
        return 2 + 2 + 2 + 6;
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(rt);
        out.writeShort(grbitFrt);
        out.writeShort(iObjectKind);
        // 6 bytes unused
        out.write(reserved);
    }

    @Override
    public ChartEndObjectRecord copy() {
        return new ChartEndObjectRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CHART_END_OBJECT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "rt", () -> rt,
            "grbitFrt", () -> grbitFrt,
            "iObjectKind", () -> iObjectKind,
            "reserved", () -> reserved
        );
    }
}
