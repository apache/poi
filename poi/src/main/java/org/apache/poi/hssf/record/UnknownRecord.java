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

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Unknown record just tells you the sid so you can figure out what records you are missing.
 * Also helps us read/modify sheets we don't know all the records to.
 * (HSSF leaves these alone!)
 */
public final class UnknownRecord extends StandardRecord {

    /*
     * Some Record IDs used by POI as 'milestones' in the record stream
     */
    /**
     * seems to be part of the {@link PageSettingsBlock}. Not interpreted by POI.
     * The name 'PRINTSIZE' was taken from OOO source.<p>
     * The few POI test samples with this record have data { 0x03, 0x00 }.
     */
    public static final int PRINTSIZE_0033       = 0x0033;
    /**
     * Environment-Specific Print Record
     */
    public static final int PLS_004D             = 0x004D;
    public static final int SHEETPR_0081         = 0x0081;
    public static final int SORT_0090            = 0x0090;
    public static final int STANDARDWIDTH_0099   = 0x0099;
    public static final int SCL_00A0             = 0x00A0;
    public static final int BITMAP_00E9          = 0x00E9;
    public static final int PHONETICPR_00EF      = 0x00EF;
    public static final int LABELRANGES_015F     = 0x015F;
    public static final int QUICKTIP_0800        = 0x0800;
    public static final int SHEETEXT_0862        = 0x0862; // OOO calls this SHEETLAYOUT
    public static final int SHEETPROTECTION_0867 = 0x0867;
    public static final int HEADER_FOOTER_089C   = 0x089C;
    public static final int CODENAME_1BA         = 0x01BA;
    public static final int PLV_MAC              = 0x08C8;

    private int _sid;
    private byte[] _rawData;

    /**
     * @param id    id of the record -not validated, just stored for serialization
     * @param data  the data
     */
    public UnknownRecord(int id, byte[] data) {
        _sid = id & 0xFFFF;
        _rawData = data;
    }


    /**
     * construct an unknown record.  No fields are interpreted and the record will
     * be serialized in its original form more or less
     * @param in the RecordInputstream to read the record from
     */
    public UnknownRecord(RecordInputStream in) {
        _sid = in.getSid();
        _rawData = in.readRemainder();

        // TODO - put unknown OBJ sub-records in a different class
        // unknown sids in the range 0x0004-0x0013 are probably 'sub-records' of ObjectRecord
        // those sids are in a different number space.
    }

    /**
     * spit the record out AS IS. no interpretation or identification
     */
    @Override
    public void serialize(LittleEndianOutput out) {
        out.write(_rawData);
    }

    @Override
    protected int getDataSize() {
        return _rawData.length;
    }
    @Override
    public short getSid() {
        return (short) _sid;
    }

    /**
     * These BIFF record types are known but still uninterpreted by POI
     *
     * @param sid The identifier for an unknown record type
     *
     * @return the documented name of this BIFF record type, <code>null</code> if unknown to POI
     */
    public static String getBiffName(int sid) {
        // Note to POI developers:
        // Make sure you delete the corresponding entry from
        // this method any time a new Record subclass is created.
        return switch (sid) {
            case PRINTSIZE_0033 -> "PRINTSIZE";
            case PLS_004D -> "PLS";
            case 0x0050 -> "DCON"; // Data Consolidation Information
            case 0x007F -> "IMDATA";
            case SHEETPR_0081 -> "SHEETPR";
            case SORT_0090 -> "SORT"; // Sorting Options
            case 0x0094 -> "LHRECORD"; // .WK? File Conversion Information
            case STANDARDWIDTH_0099 -> "STANDARDWIDTH"; //Standard Column Width
            case SCL_00A0 -> "SCL"; // Window Zoom Magnification
            case 0x00AE -> "SCENMAN"; // Scenario Output Data

            case 0x00B2 -> "SXVI";        // (pivot table) View Item
            case 0x00B4 -> "SXIVD";       // (pivot table) Row/Column Field IDs
            case 0x00B5 -> "SXLI";        // (pivot table) Line Item Array

            case 0x00D3 -> "OBPROJ";
            case 0x00DC -> "PARAMQRY";
            case 0x00DE -> "OLESIZE";
            case BITMAP_00E9 -> "BITMAP";
            case PHONETICPR_00EF -> "PHONETICPR";
            case 0x00F1 -> "SXEX";        // PivotTable View Extended Information

            case LABELRANGES_015F -> "LABELRANGES";
            case 0x01BA -> "CODENAME";
            case 0x01A9 -> "USERBVIEW";
            case 0x01AD -> "QSI";

            case 0x01C0 -> "EXCEL9FILE";

            case 0x0802 -> "QSISXTAG";   // Pivot Table and Query Table Extensions
            case 0x0803 -> "DBQUERYEXT";
            case 0x0805 -> "TXTQUERY";
            case 0x0810 -> "SXVIEWEX9";  // Pivot Table Extensions

            case 0x0812 -> "CONTINUEFRT";
            case QUICKTIP_0800 -> "QUICKTIP";
            case SHEETEXT_0862 -> "SHEETEXT";
            case 0x0863 -> "BOOKEXT";
            case 0x0864 -> "SXADDL";    // Pivot Table Additional Info
            case SHEETPROTECTION_0867 -> "SHEETPROTECTION";
            case 0x086B -> "DATALABEXTCONTENTS";
            case 0x086C -> "CELLWATCH";
            case FeatRecord.v11_sid -> "SHARED FEATURE v11";
            case 0x0874 -> "DROPDOWNOBJIDS";
            case 0x0876 -> "DCONN";
            case FeatRecord.v12_sid -> "SHARED FEATURE v12";
            case 0x087B -> "CFEX";
            case 0x087C -> "XFCRC";
            case 0x087D -> "XFEXT";
            case 0x087F -> "CONTINUEFRT12";
            case 0x088B -> "PLV";
            case 0x088C -> "COMPAT12";
            case 0x088D -> "DXF";
            case 0x0892 -> "STYLEEXT";
            case 0x0896 -> "THEME";
            case 0x0897 -> "GUIDTYPELIB";
            case 0x089A -> "MTRSETTINGS";
            case 0x089B -> "COMPRESSPICTURES";
            case HEADER_FOOTER_089C -> "HEADERFOOTER";
            case 0x089D -> "CRTLAYOUT12";
            case 0x089E -> "CRTMLFRT";
            case 0x089F -> "CRTMLFRTCONTINUE";
            case 0x08A1 -> "SHAPEPROPSSTREAM";
            case 0x08A3 -> "FORCEFULLCALCULATION";
            case 0x08A4 -> "SHAPEPROPSSTREAM";
            case 0x08A5 -> "TEXTPROPSSTREAM";
            case 0x08A6 -> "RICHTEXTSTREAM";
            case 0x08A7 -> "CRTLAYOUT12A";

            case 0x08C8 -> "PLV{Mac Excel}";

            case 0x1001 -> "UNITS";
            case 0x1006 -> "CHARTDATAFORMAT";
            case 0x1007 -> "CHARTLINEFORMAT";

            default -> {
                if (isObservedButUnknown(sid)) {
                    yield "UNKNOWN-" + Integer.toHexString(sid).toUpperCase(Locale.ROOT);
                }
                yield null;
            }
        };
    }

    /**
     * @return <code>true</code> if the unknown record id has been observed in POI unit tests
     */
    private static boolean isObservedButUnknown(int sid) {
        // TODO Look up more of these in the latest [MS-XLS] doc and move to getBiffName
        return switch (sid) {
            // contains 2 bytes of data: 0x0001 or 0x0003
            // Seems to be written by MSAccess
            // contains text "[Microsoft JET Created Table]0021010"
            // appears after last cell value record and before WINDOW2
            // Written by Excel 2007
            // rawData is multiple of 12 bytes long
            // appears after last cell value record and before WINDOW2 or drawing records

            case 0x0033, 0x0034, 0x01BD, 0x01C2, 0x1009, 0x100A, 0x100B, 0x100C, 0x1014, 0x1017, 0x1018, 0x1019, 0x101A,
                 0x101B, 0x101D, 0x101E, 0x101F, 0x1020, 0x1021, 0x1022, 0x1024, 0x1025, 0x1026, 0x1027, 0x1032, 0x1033,
                 0x1034, 0x1035, 0x103A, 0x1041, 0x1043, 0x1044, 0x1045, 0x1046, 0x104A, 0x104B, 0x104E, 0x104F, 0x1051,
                 0x105C, 0x105D, 0x105F, 0x1060, 0x1062, 0x1063, 0x1064, 0x1065, 0x1066 -> true;
            default -> false;
        };
    }

    @Override
    public UnknownRecord copy() {
        // immutable - OK to return this
        return this;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.UNKNOWN;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        Supplier<String> biffName = () -> {
            String bn = getBiffName(_sid);
            return bn == null ? "UNKNOWNRECORD" : bn;
        };

        return GenericRecordUtil.getGenericProperties(
            "sid", this::getSid,
            "biffName", biffName,
            "rawData", () -> _rawData
        );
    }
}
