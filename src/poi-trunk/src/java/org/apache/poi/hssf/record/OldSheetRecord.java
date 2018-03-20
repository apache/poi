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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;

/**
 * Title:        Bound Sheet Record (aka BundleSheet) (0x0085) for BIFF 5<P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheet name
 *               and tells where the Beginning of file record is within the HSSF
 *               file.
 */
public final class OldSheetRecord {

    //arbitrarily selected; may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    public final static short sid = 0x0085;

    private int field_1_position_of_BOF;
    private int field_2_visibility;
    private int field_3_type;
    private byte[] field_5_sheetname;
    private CodepageRecord codepage;

    public OldSheetRecord(RecordInputStream in) {
        field_1_position_of_BOF = in.readInt();
        field_2_visibility = in.readUByte();
        field_3_type = in.readUByte();
        int field_4_sheetname_length = in.readUByte();
        field_5_sheetname = IOUtils.safelyAllocate(field_4_sheetname_length, MAX_RECORD_LENGTH);
        in.read(field_5_sheetname, 0, field_4_sheetname_length);
    }

    public void setCodePage(CodepageRecord codepage) {
        this.codepage = codepage;
    }

    public short getSid() {
        return sid;
    }

    /**
     * get the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
     *
     * @return offset in bytes
     */
    public int getPositionOfBof() {
        return field_1_position_of_BOF;
    }

    /**
     * get the sheetname for this sheet.  (this appears in the tabs at the bottom)
     * @return sheetname the name of the sheet
     */
    public String getSheetname() {
        return OldStringRecord.getString(field_5_sheetname, codepage);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[BOUNDSHEET]\n");
        buffer.append("    .bof        = ").append(HexDump.intToHex(getPositionOfBof())).append("\n");
        buffer.append("    .visibility = ").append(HexDump.shortToHex(field_2_visibility)).append("\n");
        buffer.append("    .type       = ").append(HexDump.byteToHex(field_3_type)).append("\n");
        buffer.append("    .sheetname  = ").append(getSheetname()).append("\n");
        buffer.append("[/BOUNDSHEET]\n");
        return buffer.toString();
    }
}
