
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
 * defines how many add menu and del menu options are stored in the file.
 * Should always be set to 0 for HSSF workbooks.
 *
 * @version 2.0-pre
 */

public final class MMSRecord extends StandardRecord {
    public static final short sid = 0xC1;
    private byte field_1_addMenuCount;
    private byte field_2_delMenuCount;

    public MMSRecord() {}

    public MMSRecord(MMSRecord other) {
        field_1_addMenuCount = other.field_1_addMenuCount;
        field_2_delMenuCount = other.field_2_delMenuCount;
    }

    public MMSRecord(RecordInputStream in) {
        if (in.remaining()==0) {
            return;
        }

        field_1_addMenuCount = in.readByte();
        field_2_delMenuCount = in.readByte();
    }

    /**
     * set number of add menu options (set to 0)
     * @param am  number of add menu options
     */

    public void setAddMenuCount(byte am)
    {
        field_1_addMenuCount = am;
    }

    /**
     * set number of del menu options (set to 0)
     * @param dm  number of del menu options
     */

    public void setDelMenuCount(byte dm)
    {
        field_2_delMenuCount = dm;
    }

    /**
     * get number of add menu options (should be 0)
     * @return number of add menu options
     */

    public byte getAddMenuCount()
    {
        return field_1_addMenuCount;
    }

    /**
     * get number of add del options (should be 0)
     * @return number of add menu options
     */

    public byte getDelMenuCount()
    {
        return field_2_delMenuCount;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(getAddMenuCount());
        out.writeByte(getDelMenuCount());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public MMSRecord copy() {
        return new MMSRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.MMS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "addMenuCount", this::getAddMenuCount,
            "delMenuCount", this::getDelMenuCount
        );
    }
}
