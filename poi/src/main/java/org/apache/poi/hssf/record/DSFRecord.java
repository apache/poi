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
 * Tells if this is a double stream file. (never applies to HSSF generated files)<p>
 *
 * Double Stream files contain both BIFF8 and BIFF7 workbooks.
 */
public final class DSFRecord extends StandardRecord {
    public static final short sid = 0x0161;

    private static final BitField biff5BookStreamFlag = BitFieldFactory.getInstance(0x0001);

    private int _options;

    private DSFRecord(DSFRecord other) {
        super(other);
        _options = other._options;
    }

    private DSFRecord(int options) {
        _options = options;
    }

    public DSFRecord(boolean isBiff5BookStreamPresent) {
        this(0);
        _options = biff5BookStreamFlag.setBoolean(0, isBiff5BookStreamPresent);
    }

    public DSFRecord(RecordInputStream in) {
        this(in.readShort());
    }

    public boolean isBiff5BookStreamPresent() {
        return biff5BookStreamFlag.isSet(_options);
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
    public DSFRecord copy() {
        return new DSFRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.DSF;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "options", () -> _options,
            "biff5BookStreamPresent", this::isBiff5BookStreamPresent
        );
    }
}
