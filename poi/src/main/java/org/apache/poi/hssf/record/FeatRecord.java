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
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.record.common.FeatFormulaErr2;
import org.apache.poi.hssf.record.common.FeatProtection;
import org.apache.poi.hssf.record.common.FeatSmartTag;
import org.apache.poi.hssf.record.common.FtrHeader;
import org.apache.poi.hssf.record.common.SharedFeature;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * Title: Feat (Feature) Record
 * <P>
 * This record specifies Shared Features data. It is normally paired
 *  up with a {@link FeatHdrRecord}.
 */
public final class FeatRecord extends StandardRecord {
    private static final Logger LOG = LogManager.getLogger(FeatRecord.class);
    public static final short sid = 0x0868;
    // SIDs from newer versions
    public static final short v11_sid = 0x0872;
    public static final short v12_sid = 0x0878;

    private final FtrHeader futureHeader;

    /** See SHAREDFEATURES_* on {@link FeatHdrRecord} */
    private int isf_sharedFeatureType;
    private byte reserved1; // Should always be zero
    private long reserved2; // Should always be zero
    /** Only matters if type is ISFFEC2 */
    private long cbFeatData;
    private int reserved3; // Should always be zero
    private CellRangeAddress[] cellRefs;

    /**
     * Contents depends on isf_sharedFeatureType :
     *  ISFPROTECTION -> FeatProtection
     *  ISFFEC2       -> FeatFormulaErr2
     *  ISFFACTOID    -> FeatSmartTag
     */
    private SharedFeature sharedFeature;

    public FeatRecord() {
        futureHeader = new FtrHeader();
        futureHeader.setRecordType(sid);
    }

    public FeatRecord(FeatRecord other) {
        super(other);
        futureHeader = other.futureHeader.copy();
        isf_sharedFeatureType = other.isf_sharedFeatureType;
        reserved1 = other.reserved1;
        reserved2 = other.reserved2;
        cbFeatData = other.cbFeatData;
        reserved3 = other.reserved3;
        cellRefs = (other.cellRefs == null) ? null :
            Stream.of(other.cellRefs).map(CellRangeAddress::copy).toArray(CellRangeAddress[]::new);
        sharedFeature = (other.sharedFeature == null) ? null : other.sharedFeature.copy();
    }

    public FeatRecord(RecordInputStream in) {
        futureHeader = new FtrHeader(in);

        isf_sharedFeatureType = in.readShort();
        reserved1 = in.readByte();
        reserved2 = in.readInt();
        int cref = in.readUShort();
        cbFeatData = in.readInt();
        reserved3 = in.readShort();

        cellRefs = new CellRangeAddress[cref];
        for(int i=0; i<cellRefs.length; i++) {
            cellRefs[i] = new CellRangeAddress(in);
        }

        switch(isf_sharedFeatureType) {
        case FeatHdrRecord.SHAREDFEATURES_ISFPROTECTION:
            sharedFeature = new FeatProtection(in);
            break;
        case FeatHdrRecord.SHAREDFEATURES_ISFFEC2:
            sharedFeature = new FeatFormulaErr2(in);
            break;
        case FeatHdrRecord.SHAREDFEATURES_ISFFACTOID:
            sharedFeature = new FeatSmartTag(in);
            break;
        default:
            LOG.atError().log("Unknown Shared Feature {} found!", box(isf_sharedFeatureType));
        }
    }

    public short getSid() {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        futureHeader.serialize(out);

        out.writeShort(isf_sharedFeatureType);
        out.writeByte(reserved1);
        out.writeInt((int)reserved2);
        out.writeShort(cellRefs.length);
        out.writeInt((int)cbFeatData);
        out.writeShort(reserved3);

        for (CellRangeAddress cellRef : cellRefs) {
            cellRef.serialize(out);
        }

        sharedFeature.serialize(out);
    }

    protected int getDataSize() {
        return 12 + 2+1+4+2+4+2+
            (cellRefs.length * CellRangeAddress.ENCODED_SIZE)
            +sharedFeature.getDataSize();
    }

    public int getIsf_sharedFeatureType() {
        return isf_sharedFeatureType;
    }

    public long getCbFeatData() {
        return cbFeatData;
    }
    public void setCbFeatData(long cbFeatData) {
        this.cbFeatData = cbFeatData;
    }

    public CellRangeAddress[] getCellRefs() {
        return cellRefs;
    }
    public void setCellRefs(CellRangeAddress[] cellRefs) {
        this.cellRefs = cellRefs;
    }

    public SharedFeature getSharedFeature() {
        return sharedFeature;
    }
    public void setSharedFeature(SharedFeature feature) {
        this.sharedFeature = feature;

        if(feature instanceof FeatProtection) {
            isf_sharedFeatureType = FeatHdrRecord.SHAREDFEATURES_ISFPROTECTION;
        }
        if(feature instanceof FeatFormulaErr2) {
            isf_sharedFeatureType = FeatHdrRecord.SHAREDFEATURES_ISFFEC2;
        }
        if(feature instanceof FeatSmartTag) {
            isf_sharedFeatureType = FeatHdrRecord.SHAREDFEATURES_ISFFACTOID;
        }

        if(isf_sharedFeatureType == FeatHdrRecord.SHAREDFEATURES_ISFFEC2) {
            cbFeatData = sharedFeature.getDataSize();
        } else {
            cbFeatData = 0;
        }
    }

    @Override
    public FeatRecord copy() {
        return new FeatRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.FEAT;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "futureHeader", () -> futureHeader,
            "isf_sharedFeatureType", this::getIsf_sharedFeatureType,
            "reserved1", () -> reserved1,
            "reserved2", () -> reserved2,
            "cbFeatData", this::getCbFeatData,
            "reserved3", () -> reserved3,
            "cellRefs", this::getCellRefs,
            "sharedFeature", this::getSharedFeature
        );
    }
}
