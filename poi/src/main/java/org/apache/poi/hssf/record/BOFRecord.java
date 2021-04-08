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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.LittleEndianOutput;

/**
 * Somewhat of a misnomer, its used for the beginning of a set of records that
 * have a particular purpose or subject. Used in sheets and workbooks.
 */
public final class BOFRecord extends StandardRecord {
    /**
     * for BIFF8 files the BOF is 0x809. For earlier versions see
     *  {@link #biff2_sid} {@link #biff3_sid} {@link #biff4_sid}
     *  {@link #biff5_sid}
     */
    public static final short sid = 0x809;
    // SIDs from earlier BIFF versions
    public static final short biff2_sid = 0x009;
    public static final short biff3_sid = 0x209;
    public static final short biff4_sid = 0x409;
    public static final short biff5_sid = 0x809;

    /** suggested default (0x0600 - BIFF8) */
    public static final int VERSION             = 0x0600;
    /** suggested default 0x10d3 */
    public static final int BUILD               = 0x10d3;
    /** suggested default  0x07CC (1996) */
    public static final int BUILD_YEAR          = 0x07CC;   // 1996
    /** suggested default for a normal sheet (0x41) */
    public static final int HISTORY_MASK        = 0x41;

    public static final int TYPE_WORKBOOK       = 0x05;
    public static final int TYPE_VB_MODULE      = 0x06;
    public static final int TYPE_WORKSHEET      = 0x10;
    public static final int TYPE_CHART          = 0x20;
    public static final int TYPE_EXCEL_4_MACRO  = 0x40;
    public static final int TYPE_WORKSPACE_FILE = 0x100;

    private int field_1_version;
    private int field_2_type;
    private int field_3_build;
    private int field_4_year;
    private int field_5_history;
    private int field_6_rversion;

    /**
     * Constructs an empty BOFRecord with no fields set.
     */
    public BOFRecord() {}

    public BOFRecord(BOFRecord other) {
        super(other);
        field_1_version = other.field_1_version;
        field_2_type = other.field_2_type;
        field_3_build = other.field_3_build;
        field_4_year = other.field_4_year;
        field_5_history = other.field_5_history;
        field_6_rversion = other.field_6_rversion;
    }

    private BOFRecord(int type) {
        field_1_version = VERSION;
        field_2_type = type;
        field_3_build = BUILD;
        field_4_year = BUILD_YEAR;
        field_5_history = 0x01;
        field_6_rversion = VERSION;
    }

    public static BOFRecord createSheetBOF() {
        return new BOFRecord(TYPE_WORKSHEET);
    }

    public BOFRecord(RecordInputStream in) {
        field_1_version  = in.readShort();
        field_2_type     = in.readShort();

        // Some external tools don't generate all of
        //  the remaining fields
        if (in.remaining() >= 2) {
            field_3_build = in.readShort();
        }
        if (in.remaining() >= 2) {
            field_4_year = in.readShort();
        }
        if (in.remaining() >= 4) {
            field_5_history  = in.readInt();
        }
        if (in.remaining() >= 4) {
            field_6_rversion = in.readInt();
        }
    }

    /**
     * Version number - for BIFF8 should be 0x06
     * @see #VERSION
     * @param version version to be set
     */
    public void setVersion(int version) {
        field_1_version = version;
    }

    /**
     * type of object this marks
     * @see #TYPE_WORKBOOK
     * @see #TYPE_VB_MODULE
     * @see #TYPE_WORKSHEET
     * @see #TYPE_CHART
     * @see #TYPE_EXCEL_4_MACRO
     * @see #TYPE_WORKSPACE_FILE
     * @param type type to be set
     */
    public void setType(int type) {
        field_2_type = type;
    }

    /**
     * build that wrote this file
     * @see #BUILD
     * @param build build number to set
     */
    public void setBuild(int build) {
        field_3_build = build;
    }

    /**
     * Year of the build that wrote this file
     * @see #BUILD_YEAR
     * @param year build year to set
     */
    public void setBuildYear(int year) {
        field_4_year = year;
    }

    /**
     * set the history bit mask (not very useful)
     * @see #HISTORY_MASK
     * @param bitmask bitmask to set for the history
     */
    public void setHistoryBitMask(int bitmask) {
        field_5_history = bitmask;
    }

    /**
     * set the minimum version required to read this file
     *
     * @see #VERSION
     * @param version version to set
     */
    public void setRequiredVersion(int version) {
        field_6_rversion = version;
    }

    /**
     * Version number - for BIFF8 should be 0x06
     * @see #VERSION
     * @return version number of the generator of this file
     */
    public int getVersion() {
        return field_1_version;
    }

    /**
     * type of object this marks
     * @see #TYPE_WORKBOOK
     * @see #TYPE_VB_MODULE
     * @see #TYPE_WORKSHEET
     * @see #TYPE_CHART
     * @see #TYPE_EXCEL_4_MACRO
     * @see #TYPE_WORKSPACE_FILE
     * @return type of object
     */
    public int getType() {
        return field_2_type;
    }

    /**
     * get the build that wrote this file
     * @see #BUILD
     * @return short build number of the generator of this file
     */
    public int getBuild() {
        return field_3_build;
    }

    /**
     * Year of the build that wrote this file
     * @see #BUILD_YEAR
     * @return short build year of the generator of this file
     */
    public int getBuildYear() {
        return field_4_year;
    }

    /**
     * get the history bit mask (not very useful)
     * @see #HISTORY_MASK
     * @return int bitmask showing the history of the file (who cares!)
     */
    public int getHistoryBitMask() {
        return field_5_history;
    }

    /**
     * get the minimum version required to read this file
     *
     * @see #VERSION
     * @return int least version that can read the file
     */
    public int getRequiredVersion() {
        return field_6_rversion;
    }

    private String getTypeName() {
        switch(field_2_type) {
            case TYPE_CHART: return "chart";
            case TYPE_EXCEL_4_MACRO: return "excel 4 macro";
            case TYPE_VB_MODULE: return "vb module";
            case TYPE_WORKBOOK: return "workbook";
            case TYPE_WORKSHEET: return "worksheet";
            case TYPE_WORKSPACE_FILE: return "workspace file";
        }
        return "#error unknown type#";
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getVersion());
        out.writeShort(getType());
        out.writeShort(getBuild());
        out.writeShort(getBuildYear());
        out.writeInt(getHistoryBitMask());
        out.writeInt(getRequiredVersion());
    }

    protected int getDataSize() {
        return 16;
    }

    public short getSid(){
        return sid;
    }

    @Override
    public BOFRecord copy() {
        return new BOFRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BOF;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("version", this::getVersion);
        m.put("type", this::getType);
        m.put("typeName", this::getTypeName);
        m.put("build", this::getBuild);
        m.put("buildYear", this::getBuildYear);
        m.put("history", this::getHistoryBitMask);
        m.put("requiredVersion", this::getRequiredVersion);
        return Collections.unmodifiableMap(m);
    }
}
