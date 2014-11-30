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
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Biff2 - Biff 4 Label Record (0x0004 / 0x0204) - read only support for 
 *  strings stored directly in the cell, from the older file formats that
 *  didn't use {@link LabelSSTRecord}
 */
public final class OldLabelRecord extends Record implements CellValueRecordInterface {
    private final static POILogger logger = POILogFactory.getLogger(OldLabelRecord.class);

    public final static short biff2_sid = 0x0004;
    public final static short biff345_sid = 0x0204;

    private short             sid;
    private int               field_1_row;
    private short             field_2_column;
    private int               field_3_cell_attrs; // Biff 2
    private short             field_3_xf_index;   // Biff 3+
    private short             field_4_string_len;
    private byte[]            field_5_bytes;
    //private XXXXX           codepage; // TODO Implement for this and OldStringRecord

    /**
     * @param in the RecordInputstream to read the record from
     */
    public OldLabelRecord(RecordInputStream in)
    {
        sid = in.getSid();

        field_1_row          = in.readUShort();
        field_2_column       = in.readShort();

        if (in.getSid() == biff2_sid) {
            field_3_cell_attrs = in.readUShort() << 8;
            field_3_cell_attrs += in.readUByte();
            field_4_string_len  = (short)in.readUByte();
        } else {
            field_3_xf_index     = in.readShort();
            field_4_string_len   = in.readShort();
        }

        // Can only decode properly later when you know the codepage
        field_5_bytes = new byte[field_4_string_len];
        in.read(field_5_bytes, 0, field_4_string_len);

        if (in.remaining() > 0) {
            logger.log(POILogger.INFO,
                    "LabelRecord data remains: " + in.remaining() +
                    " : " + HexDump.toHex(in.readRemainder())
                    );
        }
    }

    public boolean isBiff2() {
        return sid == biff2_sid;
    }

    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return field_2_column;
    }

    public short getXFIndex()
    {
        return field_3_xf_index;
    }
    public int getCellAttrs()
    {
        return field_3_cell_attrs;
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
     */
    public String getValue()
    {
        // We really need the codepage here to do this right...
        return new String(field_5_bytes);
    }

    /**
     * Not supported
     */
    public int serialize(int offset, byte [] data) {
        throw new RecordFormatException("Old Label Records are supported READ ONLY");
    }
    public int getRecordSize() {
        throw new RecordFormatException("Old Label Records are supported READ ONLY");
    }

    public short getSid()
    {
        return sid;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[OLD LABEL]\n");
        sb.append("    .row       = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .column    = ").append(HexDump.shortToHex(getColumn())).append("\n");
        if (isBiff2()) {
            sb.append("    .cellattrs = ").append(HexDump.shortToHex(getCellAttrs())).append("\n");
        } else {
            sb.append("    .xfindex   = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
        }
        sb.append("    .string_len= ").append(HexDump.shortToHex(field_4_string_len)).append("\n");
        sb.append("    .value       = ").append(getValue()).append("\n");
        sb.append("[/OLD LABEL]\n");
        return sb.toString();
    }

    /**
     * NO-OP!
     */
    public void setColumn(short col)
    {
    }

    /**
     * NO-OP!
     */
    public void setRow(int row)
    {
    }

    /**
     * no op!
     */
    public void setXFIndex(short xf)
    {
    }
}
