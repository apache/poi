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
 * Defines whether a sheet or workbook is protected (HSSF DOES NOT SUPPORT ENCRYPTION)<p>
 * HSSF now supports the simple "protected" sheets (where they are not encrypted and open office et al
 * ignore the password record entirely).
 */
public final class ProtectRecord extends StandardRecord {
    public static final short sid = 0x0012;

    private static final BitField protectFlag = BitFieldFactory.getInstance(0x0001);

    private int _options;

    private ProtectRecord(int options) {
        _options = options;
    }

    private ProtectRecord(ProtectRecord other) {
        super(other);
        _options = other._options;
    }

    public ProtectRecord(boolean isProtected) {
        this(0);
        setProtect(isProtected);
    }

    public ProtectRecord(RecordInputStream in) {
        this(in.readShort());
    }

    /**
     * set whether the sheet is protected or not
     * @param protect whether to protect the sheet or not
     */
    public void setProtect(boolean protect) {
        _options = protectFlag.setBoolean(_options, protect);
    }

    /**
     * get whether the sheet is protected or not
     * @return whether to protect the sheet or not
     */
    public boolean getProtect() {
        return protectFlag.isSet(_options);
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
    public ProtectRecord copy() {
        return new ProtectRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.PROTECT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "options", () -> _options,
            "protect", this::getProtect
        );
    }
}
