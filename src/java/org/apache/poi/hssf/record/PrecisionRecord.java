
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
 * Defines whether to store with full precision or what's displayed by the gui
 * (meaning have really screwed up and skewed figures or only think you do!)
 *
 * @version 2.0-pre
 */
public final class PrecisionRecord extends StandardRecord {
    public static final short sid = 0xE;

    private short field_1_precision;

    public PrecisionRecord() {}

    public PrecisionRecord(PrecisionRecord other) {
        super(other);
        field_1_precision = other.field_1_precision;
    }

    public PrecisionRecord(RecordInputStream in) {
        field_1_precision = in.readShort();
    }

    /**
     * set whether to use full precision or just skew all you figures all to hell.
     *
     * @param fullprecision - or not
     */
    public void setFullPrecision(boolean fullprecision) {
        field_1_precision = (short) (fullprecision ? 1 : 0);
    }

    /**
     * get whether to use full precision or just skew all you figures all to hell.
     *
     * @return fullprecision - or not
     */
    public boolean getFullPrecision()
    {
        return (field_1_precision == 1);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_precision);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public PrecisionRecord copy() {
        return new PrecisionRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PRECISION;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("precision", this::getFullPrecision);
    }
}
