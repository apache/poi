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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Flag whether to refresh all external data when loading a sheet.
 * (which hssf doesn't support anyhow so who really cares?)
 */
public final class RefreshAllRecord extends StandardRecord {
    public static final short sid = 0x01B7;

    private static final BitField refreshFlag = BitFieldFactory.getInstance(0x0001);

    private int _options;

    private RefreshAllRecord(int options) {
        _options = options;
    }

    private RefreshAllRecord(RefreshAllRecord other) {
        super(other);
        _options = other._options;
    }

    public RefreshAllRecord(RecordInputStream in) {
        this(in.readUShort());
    }

    public RefreshAllRecord(boolean refreshAll) {
        this(0);
        setRefreshAll(refreshAll);
    }

    /**
     * set whether to refresh all external data when loading a sheet
     * @param refreshAll or not
     */
    public void setRefreshAll(boolean refreshAll) {
        _options = refreshFlag.setBoolean(_options, refreshAll);
    }

    /**
     * get whether to refresh all external data when loading a sheet
     * @return refreshall or not
     */
    public boolean getRefreshAll() {
        return refreshFlag.isSet(_options);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_options);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public RefreshAllRecord copy() {
        return new RefreshAllRecord(this);
    }


    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.REFRESH_ALL;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "options", () -> _options,
            "refreshAll", this::getRefreshAll
        );
    }
}
