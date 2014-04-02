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

/**
 * Title:        DBCell Record (0x00D7)<p/>
 * Description:  Used by Excel and other MS apps to quickly find rows in the sheets.<P>
 * REFERENCE:  PG 299/440 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height
 */
public final class DBCellRecord extends StandardRecord {
    public final static short sid = 0x00D7;
    public final static int BLOCK_SIZE = 32;
    
    public static final class Builder {
        private short[] _cellOffsets;
        private int _nCellOffsets;
        public Builder() {
        	_cellOffsets = new short[4];
		}

        public void addCellOffset(int cellRefOffset) {
            if (_cellOffsets.length <= _nCellOffsets) {
                short[] temp = new short[_nCellOffsets * 2];
                System.arraycopy(_cellOffsets, 0, temp, 0, _nCellOffsets);
                _cellOffsets = temp;
            }
            _cellOffsets[_nCellOffsets] = (short) cellRefOffset;
            _nCellOffsets++;
        }

        public DBCellRecord build(int rowOffset) {
            short[] cellOffsets = new short[_nCellOffsets];
            System.arraycopy(_cellOffsets, 0, cellOffsets, 0, _nCellOffsets);
            return new DBCellRecord(rowOffset, cellOffsets);
        }
    }
    /**
     * offset from the start of this DBCellRecord to the start of the first cell in
     * the next DBCell block.
     */
    private final int     field_1_row_offset;
    private final short[] field_2_cell_offsets;

    DBCellRecord(int rowOffset, short[]cellOffsets) {
        field_1_row_offset = rowOffset;
        field_2_cell_offsets = cellOffsets;
    }

    public DBCellRecord(RecordInputStream in) {
        field_1_row_offset   = in.readUShort();
        int size = in.remaining();        
        field_2_cell_offsets = new short[ size / 2 ];

        for (int i=0;i<field_2_cell_offsets.length;i++)
        {
            field_2_cell_offsets[ i ] = in.readShort();
        }
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();

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
        for (int k = 0; k < field_2_cell_offsets.length; k++) {
            out.writeShort(field_2_cell_offsets[ k ]);
        }
    }
    protected int getDataSize() {
        return 4 + field_2_cell_offsets.length * 2;
    }
    
    /**
     *  @return the size of the group of <tt>DBCellRecord</tt>s needed to encode
     *  the specified number of blocks and rows
     */
    public static int calculateSizeOfRecords(int nBlocks, int nRows) {
        // One DBCell per block.
        // 8 bytes per DBCell (non variable section)
        // 2 bytes per row reference
        return nBlocks * 8 + nRows * 2;
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
        // safe because immutable
        return this;
    }
}
