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

import org.apache.poi.hssf.record.common.FtrHeader;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: FeatHdr (Feature Header) Record
 * <P>
 * This record specifies common information for Shared Features, and
 *  specifies the beginning of a collection of records to define them.
 * The collection of data (Globals Substream ABNF, macro sheet substream
 *  ABNF or worksheet substream ABNF) specifies Shared Feature data.
 */
public final class FeatHdrRecord extends StandardRecord {
    /**
     * Specifies the enhanced protection type. Used to protect a
     * shared workbook by restricting access to some areas of it
     */
    public static final int SHAREDFEATURES_ISFPROTECTION = 0x02;
    /**
     * Specifies that formula errors should be ignored
     */
    public static final int SHAREDFEATURES_ISFFEC2       = 0x03;
    /**
     * Specifies the smart tag type. Recognises certain
     * types of entries (proper names, dates/times etc) and
     * flags them for action
     */
    public static final int SHAREDFEATURES_ISFFACTOID    = 0x04;
    /**
     * Specifies the shared list type. Used for a table
     * within a sheet
     */
    public static final int SHAREDFEATURES_ISFLIST       = 0x05;


    public static final short sid = 0x0867;

    private final FtrHeader futureHeader;
    // See SHAREDFEATURES
    private int isf_sharedFeatureType;
    // Should always be one
    private byte reserved;
    /**
     * 0x00000000 = rgbHdrData not present
     * 0xffffffff = rgbHdrData present
     */
    private long cbHdrData;
    /** We need a BOFRecord to make sense of this... */
    private byte[] rgbHdrData;

    public FeatHdrRecord() {
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);
    }

    public FeatHdrRecord(FeatHdrRecord other) {
        super(other);
        futureHeader = other.futureHeader.copy();
        isf_sharedFeatureType = other.isf_sharedFeatureType;
        reserved = other.reserved;
        cbHdrData = other.cbHdrData;
        rgbHdrData = (other.rgbHdrData == null) ? null : other.rgbHdrData.clone();
    }

    public FeatHdrRecord(RecordInputStream in) {
        futureHeader = new FtrHeader(in);

        isf_sharedFeatureType = in.readShort();
        reserved = in.readByte();
        cbHdrData = in.readInt();
        // Don't process this just yet, need the BOFRecord
        rgbHdrData = in.readRemainder();
    }

    public short getSid() {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        futureHeader.serialize(out);

        out.writeShort(isf_sharedFeatureType);
        out.writeByte(reserved);
        out.writeInt((int)cbHdrData);
        out.write(rgbHdrData);
    }

    protected int getDataSize() {
        return 12 + 2+1+4+rgbHdrData.length;
    }

    @Override
    public FeatHdrRecord copy() {
        //HACK: do a "cheat" clone, see Record.java for more information
        return new FeatHdrRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FEAT_HDR;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "futureHeader", () -> futureHeader,
            "isf_sharedFeatureType", () -> isf_sharedFeatureType,
            "reserved", () -> reserved,
            "cbHdrData", () -> cbHdrData,
            "rgbHdrData", () -> rgbHdrData
        );
    }
}
