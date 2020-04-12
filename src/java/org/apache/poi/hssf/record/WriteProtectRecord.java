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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Indicated that the sheet/workbook is write protected.
 *
 * @version 3.0-pre
 */
public final class WriteProtectRecord extends StandardRecord {
    public static final short sid = 0x86;

    public WriteProtectRecord() {}

    /**
     * @param in unused (since this record has no data)
     */
    public WriteProtectRecord(RecordInputStream in)
    {
        if (in.remaining() == 2) {
            in.readShort();
        }
    }

    public void serialize(LittleEndianOutput out) {
    }

    protected int getDataSize() {
        return 0;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public WriteProtectRecord copy() {
        return new WriteProtectRecord();
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.WRITE_PROTECT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
