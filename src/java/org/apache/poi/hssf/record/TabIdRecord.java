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
 * Contains an array of sheet id's.  Sheets always keep their ID regardless of what their name is.
 */
public final class TabIdRecord extends StandardRecord {
    public static final short sid = 0x013D;
    private static final short[] EMPTY_SHORT_ARRAY = { };

    private short[] _tabids;

    public TabIdRecord() {
        _tabids = EMPTY_SHORT_ARRAY;
    }

    public TabIdRecord(TabIdRecord other) {
        super(other);
        _tabids = (other._tabids == null) ? null : other._tabids.clone();
    }

    public TabIdRecord(RecordInputStream in) {
        int nTabs = in.remaining() / 2;
        _tabids = new short[nTabs];
        for (int i = 0; i < _tabids.length; i++) {
            _tabids[i] = in.readShort();
        }
    }

    /**
     * set the tab array.  (0,1,2).
     * @param array of tab id's {0,1,2}
     */
    public void setTabIdArray(short[] array) {
        _tabids = array.clone();
    }

    public void serialize(LittleEndianOutput out) {
        for (short tabid : _tabids) {
            out.writeShort(tabid);
        }
    }

    public int getTabIdSize() {
        return _tabids.length;
    }

    public short getTabIdAt(int index) {
        return _tabids[index];
    }

    protected int getDataSize() {
        return _tabids.length * 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public TabIdRecord copy() {
        return new TabIdRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.TAB_ID;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("elements", () -> _tabids);
    }
}
