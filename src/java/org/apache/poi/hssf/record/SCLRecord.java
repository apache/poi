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
 * Specifies the window's zoom magnification.<p>
 * If this record isn't present then the windows zoom is 100%. see p384 Excel Dev Kit
 */
public final class SCLRecord extends StandardRecord {
    public static final short sid = 0x00A0;

    private short field_1_numerator;
    private short field_2_denominator;

    public SCLRecord() {}

    public SCLRecord(SCLRecord other) {
        super(other);
        field_1_numerator = other.field_1_numerator;
        field_2_denominator = other.field_2_denominator;
    }

    public SCLRecord(RecordInputStream in) {
        field_1_numerator              = in.readShort();
        field_2_denominator            = in.readShort();
    }

    @Override
    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_numerator);
        out.writeShort(field_2_denominator);
    }

    @Override
    protected int getDataSize() {
        return 2 + 2;
    }

    @Override
    public short getSid()
    {
        return sid;
    }

    @Override
    public SCLRecord copy() {
        return new SCLRecord(this);
    }

    /**
     * Get the numerator field for the SCL record.
     *
     * @return the numerator
     */
    public short getNumerator()
    {
        return field_1_numerator;
    }

    /**
     * Set the numerator field for the SCL record.
     *
     * @param field_1_numerator the numerator
     */
    public void setNumerator(short field_1_numerator)
    {
        this.field_1_numerator = field_1_numerator;
    }

    /**
     * Get the denominator field for the SCL record.
     *
     * @return the denominator
     */
    public short getDenominator()
    {
        return field_2_denominator;
    }

    /**
     * Set the denominator field for the SCL record.
     *
     * @param field_2_denominator the denominator
     */
    public void setDenominator(short field_2_denominator)
    {
        this.field_2_denominator = field_2_denominator;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.SCL;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "numerator", this::getNumerator,
            "denominator", this::getDenominator
        );
    }
}
