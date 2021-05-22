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

import org.apache.poi.ss.formula.constant.ConstantValueParser;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * This record stores the contents of an external cell or cell range
 */
public final class CRNRecord extends StandardRecord {
    public static final short sid = 0x005A;

    private int field_1_last_column_index;
    private int field_2_first_column_index;
    private int field_3_row_index;
    private Object[] field_4_constant_values;

    public CRNRecord(CRNRecord other) {
        super(other);
        field_1_last_column_index = other.field_1_last_column_index;
        field_2_first_column_index = other.field_2_first_column_index;
        field_3_row_index = other.field_3_row_index;
        // field_4_constant_values are instances of Double, Boolean, String, ErrorCode,
        // i.e. they are immutable and can their references can be simply cloned
        field_4_constant_values = (other.field_4_constant_values == null) ? null : other.field_4_constant_values.clone();
    }

    public CRNRecord(RecordInputStream in) {
        field_1_last_column_index = in.readUByte();
        field_2_first_column_index = in.readUByte();
        field_3_row_index = in.readShort();
        int nValues = field_1_last_column_index - field_2_first_column_index + 1;
        field_4_constant_values = ConstantValueParser.parse(in, nValues);
    }

    public int getNumberOfCRNs() {
        return field_1_last_column_index;
    }

    protected int getDataSize() {
        return 4 + ConstantValueParser.getEncodedSize(field_4_constant_values);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(field_1_last_column_index);
        out.writeByte(field_2_first_column_index);
        out.writeShort(field_3_row_index);
        ConstantValueParser.encode(out, field_4_constant_values);
    }

    /**
     * return the non static version of the id for this record.
     */
    public short getSid() {
        return sid;
    }

    @Override
    public CRNRecord copy() {
        return new CRNRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.CRN;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", () -> field_3_row_index,
            "firstColumn", () -> field_2_first_column_index,
            "lastColumn", () -> field_1_last_column_index,
            "constantValues", () -> field_4_constant_values
        );
    }
}
