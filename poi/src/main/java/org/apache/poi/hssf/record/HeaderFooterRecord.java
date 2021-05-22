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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * The HEADERFOOTER record stores information added in Office Excel 2007 for headers/footers.
 */
public final class HeaderFooterRecord extends StandardRecord {
    public static final short sid = 0x089C;
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static final byte[] BLANK_GUID = new byte[16];

    private byte[] _rawData;

    public HeaderFooterRecord(byte[] data) {
        _rawData = data;
    }

    public HeaderFooterRecord(HeaderFooterRecord other) {
        super(other);
        _rawData = (other._rawData == null) ? null : other._rawData.clone();
    }

    /**
     * construct a HeaderFooterRecord record.  No fields are interpreted and the record will
     * be serialized in its original form more or less
     * @param in the RecordInputstream to read the record from
     */
    public HeaderFooterRecord(RecordInputStream in) {
        _rawData = in.readRemainder();
    }

    /**
     * spit the record out AS IS. no interpretation or identification
     */
    public void serialize(LittleEndianOutput out) {
        out.write(_rawData);
    }

    protected int getDataSize() {
        return _rawData.length;
    }

    public short getSid()
    {
        return sid;
    }

    /**
     * If this header belongs to a specific sheet view , the sheet view?s GUID will be saved here.
     * <p>
     * If it is zero, it means the current sheet. Otherwise, this field MUST match the guid field
     * of the preceding {@link UserSViewBegin} record.
     *
     * @return the sheet view?s GUID
     */
    public byte[] getGuid(){
        return Arrays.copyOfRange(_rawData, 12, 12+16);
    }

    /**
     * @return whether this record belongs to the current sheet
     */
    public boolean isCurrentSheet(){
        return Arrays.equals(getGuid(), BLANK_GUID);
    }

    @Override
    public HeaderFooterRecord copy() {
        return new HeaderFooterRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.HEADER_FOOTER;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties("rawData", () -> _rawData);
    }
}
