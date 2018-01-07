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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.RecordFormatException;

/**
 * Biff2 - Biff 4 Label Record (0x0004 / 0x0204) - read only support for 
 *  strings stored directly in the cell, from the older file formats that
 *  didn't use {@link LabelSSTRecord}
 */
public final class OldLabelRecord extends OldCellRecord {
    private final static POILogger logger = POILogFactory.getLogger(OldLabelRecord.class);
    //arbitrarily set, may need to increase
    private static final int MAX_RECORD_LENGTH = 100_000;

    public final static short biff2_sid = 0x0004;
    public final static short biff345_sid = 0x0204;

    private short          field_4_string_len;
    private final byte[]         field_5_bytes;
    private CodepageRecord codepage;

    /**
     * @param in the RecordInputstream to read the record from
     */
    public OldLabelRecord(RecordInputStream in)
    {
        super(in, in.getSid() == biff2_sid);

        if (isBiff2()) {
            field_4_string_len  = (short)in.readUByte();
        } else {
            field_4_string_len   = in.readShort();
        }

        // Can only decode properly later when you know the codepage
        field_5_bytes = IOUtils.safelyAllocate(field_4_string_len, MAX_RECORD_LENGTH);
        in.read(field_5_bytes, 0, field_4_string_len);

        if (in.remaining() > 0) {
            logger.log(POILogger.INFO,
                    "LabelRecord data remains: " + in.remaining() +
                    " : " + HexDump.toHex(in.readRemainder())
                    );
        }
    }

    public void setCodePage(CodepageRecord codepage) {
        this.codepage = codepage;
    }
    
    /**
     * get the number of characters this string contains
     * @return number of characters
     */
    public short getStringLength()
    {
        return field_4_string_len;
    }

    /**
     * Get the String of the cell
     * 
     * @return the String of the cell
     */
    public String getValue()
    {
        return OldStringRecord.getString(field_5_bytes, codepage);
    }

    /**
     * Not supported
     * 
     * @param offset not supported
     * @param data not supported
     * @return not supported
     */
    public int serialize(int offset, byte [] data) {
        throw new RecordFormatException("Old Label Records are supported READ ONLY");
    }
    
    public int getRecordSize() {
        throw new RecordFormatException("Old Label Records are supported READ ONLY");
    }

    @Override
    protected void appendValueText(StringBuilder sb) {
        sb.append("    .string_len= ").append(HexDump.shortToHex(field_4_string_len)).append("\n");
        sb.append("    .value       = ").append(getValue()).append("\n");
    }

    @Override
    protected String getRecordName() {
        return "OLD LABEL";
    }
}
