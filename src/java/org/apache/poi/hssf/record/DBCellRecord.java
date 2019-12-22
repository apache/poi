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
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.Removal;

/**
 * Used by Excel and other MS apps to quickly find rows in the sheets.
 */
public final class DBCellRecord extends StandardRecord {
    public static final short sid = 0x00D7;
    public static final int BLOCK_SIZE = 32;

    /**
     * offset from the start of this DBCellRecord to the start of the first cell in
     * the next DBCell block.
     */
    private final int     field_1_row_offset;
    private final short[] field_2_cell_offsets;

    public DBCellRecord(int rowOffset, short[] cellOffsets) {
        field_1_row_offset = rowOffset;
        field_2_cell_offsets = cellOffsets;
    }

    public DBCellRecord(RecordInputStream in) {
        field_1_row_offset   = in.readUShort();
        int size = in.remaining();
        field_2_cell_offsets = new short[ size / 2 ];

        for (int i=0;i<field_2_cell_offsets.length;i++) {
            field_2_cell_offsets[ i ] = in.readShort();
        }
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("[DBCELL]\n");
        buffer.append("    .rowoffset = ").append(HexDump.intToHex(field_1_row_offset)).append("\n");
        for (int k = 0; k < field_2_cell_offsets.length; k++) {
            buffer.append("    .cell_").append(k).append(" = ")
                .append(HexDump.shortToHex(field_2_cell_offsets[ k ])).append("\n");
        }
        buffer.append("[/DBCELL]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_1_row_offset);
        for (short field_2_cell_offset : field_2_cell_offsets) {
            out.writeShort(field_2_cell_offset);
        }
    }
    protected int getDataSize() {
        return 4 + field_2_cell_offsets.length * 2;
    }

    public short getSid() {
        return sid;
    }

    @Override
    @SuppressWarnings("squid:S2975")
    @Deprecated
    @Removal(version = "5.0.0")
    public DBCellRecord clone() {
        return copy();
    }

    @Override
    public DBCellRecord copy() {
        // safe because immutable
        return this;
    }
}
