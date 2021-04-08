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
import org.apache.poi.util.LittleEndianOutput;

/**
 * The begin record defines the start of a block of records for a (graphing
 * data object. This record is matched with a corresponding EndRecord.
 *
 * @see EndRecord
 */
public final class BeginRecord extends StandardRecord {
    public static final short sid = 0x1033;

    public BeginRecord() {}

    public BeginRecord(BeginRecord other) {
        super(other);
    }

    /**
     * @param in unused (since this record has no data)
     */
    public BeginRecord(RecordInputStream in) {}

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
    public BeginRecord copy() {
        return new BeginRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BEGIN;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return null;
    }
}
