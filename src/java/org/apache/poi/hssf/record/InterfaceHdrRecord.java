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
 * Defines the beginning of Interface records (MMS)
 */
public final class InterfaceHdrRecord extends StandardRecord {
    public static final short sid = 0x00E1;

    /**
     * suggested (and probably correct) default
     */
    public static final int CODEPAGE = 0x04B0;

    private final int _codepage;

    public InterfaceHdrRecord(InterfaceHdrRecord other) {
        super(other);
        _codepage = other._codepage;
    }

    public InterfaceHdrRecord(int codePage) {
        _codepage = codePage;
    }

    public InterfaceHdrRecord(RecordInputStream in) {
        _codepage = in.readShort();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_codepage);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    public InterfaceHdrRecord copy() {
        return new InterfaceHdrRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.INTERFACE_HDR;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("codePage", () -> _codepage);
    }
}
