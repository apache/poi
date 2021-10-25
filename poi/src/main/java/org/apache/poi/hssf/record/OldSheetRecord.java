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

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RecordFormatException;

/**
 * Title:        Bound Sheet Record (aka BundleSheet) (0x0085) for BIFF 5<P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheet name
 *               and tells where the Beginning of file record is within the HSSF
 *               file.
 */
public final class OldSheetRecord implements GenericRecord {

    public static final short sid = 0x0085;

    private final int field_1_position_of_BOF;
    private final int field_2_visibility;
    private final int field_3_type;
    private final byte[] field_5_sheetname;
    private CodepageRecord codepage;

    public OldSheetRecord(RecordInputStream in) {
        field_1_position_of_BOF = in.readInt();
        field_2_visibility = in.readUByte();
        field_3_type = in.readUByte();
        int field_4_sheetname_length = in.readUByte();
        if (field_4_sheetname_length > 0) {
            in.mark(1);
            byte b = in.readByte();
            // if the sheet name starts with a 0, we need to skip one byte, otherwise the following records will
            // fail with a LeftOverDataException
            if (b != 0) {
                try {
                    in.reset();
                } catch (IOException e) {
                    throw new RecordFormatException(e);
                }
            }
        }
        field_5_sheetname = IOUtils.safelyAllocate(field_4_sheetname_length, HSSFWorkbook.getMaxRecordLength());
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

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BOUND_SHEET;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "bof", this::getPositionOfBof,
            "visibility", () -> field_2_visibility,
            "type", () -> field_3_type,
            "sheetName", this::getSheetname
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }
}
