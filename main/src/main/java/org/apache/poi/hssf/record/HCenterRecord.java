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
 * Whether to center between horizontal margins
 *
 * @version 2.0-pre
 */
public final class HCenterRecord extends StandardRecord {
    public static final short sid = 0x0083;
    private short field_1_hcenter;

    public HCenterRecord() {}

    public HCenterRecord(HCenterRecord other) {
        super(other);
        field_1_hcenter = other.field_1_hcenter;
    }

    public HCenterRecord(RecordInputStream in) {
        field_1_hcenter = in.readShort();
    }

    /**
     * set whether or not to horizonatally center this sheet.
     * @param hc  center - t/f
     */
    public void setHCenter(boolean hc) {
        field_1_hcenter = (short)(hc ? 1 : 0);
    }

    /**
     * get whether or not to horizonatally center this sheet.
     * @return center - t/f
     */
    public boolean getHCenter()
    {
        return (field_1_hcenter == 1);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_hcenter);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public HCenterRecord copy() {
        return new HCenterRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.H_CENTER;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("hcenter", this::getHCenter);
    }
}
