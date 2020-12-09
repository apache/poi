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
 * Refers to a string in the shared string table and is a column value.
 */
public final class LabelSSTRecord extends CellRecord {
    public static final short sid = 0xfd;
    private int field_4_sst_index;

    public LabelSSTRecord() {}

    public LabelSSTRecord(LabelSSTRecord other) {
        super(other);
        field_4_sst_index = other.field_4_sst_index;
    }

    public LabelSSTRecord(RecordInputStream in) {
        super(in);
        field_4_sst_index = in.readInt();
    }

    /**
     * set the index to the string in the SSTRecord
     *
     * @param index - of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */
    public void setSSTIndex(int index) {
        field_4_sst_index = index;
    }


    /**
     * get the index to the string in the SSTRecord
     *
     * @return index of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */
    public int getSSTIndex() {
        return field_4_sst_index;
    }

    @Override
    protected String getRecordName() {
    	return "LABELSST";
    }

    @Override
    protected void serializeValue(LittleEndianOutput out) {
        out.writeInt(getSSTIndex());
    }

    @Override
    protected int getValueDataSize() {
        return 4;
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public LabelSSTRecord copy() {
        return new LabelSSTRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.LABEL_SST;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "sstIndex", this::getSSTIndex
        );
    }
}
