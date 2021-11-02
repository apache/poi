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

package org.apache.poi.hssf.record.pivottable;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.HSSFRecordTypes;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.StringUtil;

/**
 * SXVD - View Fields (0x00B1)
 */
public final class ViewFieldsRecord extends StandardRecord {
    public static final short sid = 0x00B1;

    /** the value of the {@code cchName} field when the {@link #_name} is not present */
    private static final int STRING_NOT_PRESENT_LEN = 0xFFFF;
    /** 5 shorts */
    private static final int BASE_SIZE = 10;

    private final int _sxaxis;
    private final int _cSub;
    private final int _grbitSub;
    private final int _cItm;

    private String _name;

    /**
     * values for the {@link ViewFieldsRecord#_sxaxis} field
     */
    private enum Axis {
        NO_AXIS(0),
        ROW(1),
        COLUMN(2),
        PAGE(4),
        DATA(8);
        final int id;
        Axis(int id) {
            this.id = id;
        }
    }

    public ViewFieldsRecord(ViewFieldsRecord other) {
        super(other);
        _sxaxis = other._sxaxis;
        _cSub = other._cSub;
        _grbitSub = other._grbitSub;
        _cItm = other._cItm;
        _name = other._name;
    }

    public ViewFieldsRecord(RecordInputStream in) {
        _sxaxis = in.readShort();
        _cSub = in.readShort();
        _grbitSub = in.readShort();
        _cItm = in.readShort();

        int cchName = in.readUShort();
        if (cchName != STRING_NOT_PRESENT_LEN) {
            int flag = in.readByte();
            if ((flag & 0x01) != 0) {
                _name = in.readUnicodeLEString(cchName);
            } else {
                _name = in.readCompressedUnicode(cchName);
            }
        }
    }

    @Override
    protected void serialize(LittleEndianOutput out) {

        out.writeShort(_sxaxis);
        out.writeShort(_cSub);
        out.writeShort(_grbitSub);
        out.writeShort(_cItm);

        if (_name != null) {
            StringUtil.writeUnicodeString(out, _name);
        } else {
            out.writeShort(STRING_NOT_PRESENT_LEN);
        }
    }

    @Override
    protected int getDataSize() {
        if (_name == null) {
            return BASE_SIZE;
        }
        return BASE_SIZE
            + 1 // unicode flag
            + _name.length() * (StringUtil.hasMultibyte(_name) ? 2 : 1);
    }

    @Override
    public short getSid() {
        return sid;
    }

    @Override
    public ViewFieldsRecord copy() {
        return new ViewFieldsRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.VIEW_FIELDS;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "sxaxis", () -> _sxaxis,
            "cSub", () -> _cSub,
            "grbitSub", () -> _grbitSub,
            "cItm", () -> _cItm,
            "name", () -> _name
        );
    }
}
