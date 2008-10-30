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

import org.apache.poi.hssf.util.CellRangeAddress8Bit;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Selection Record (0x001D)<P>
 * Description:  shows the user's selection on the sheet
 *               for write set num refs to 0<P>
 *
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class SelectionRecord extends Record {
    public final static short sid = 0x001D;
    private byte        field_1_pane;
    private int         field_2_row_active_cell;
    private int         field_3_col_active_cell;
    private int         field_4_active_cell_ref_index;
    private CellRangeAddress8Bit[] field_6_refs;

    /**
     * Creates a default selection record (cell A1, in pane ID 3)
     */
    public SelectionRecord(int activeCellRow, int activeCellCol) {
        field_1_pane = 3; // pane id 3 is always present.  see OOO sec 5.75 'PANE'
        field_2_row_active_cell = activeCellRow;
        field_3_col_active_cell = activeCellCol;
        field_4_active_cell_ref_index = 0;
        field_6_refs = new CellRangeAddress8Bit[] {
            new CellRangeAddress8Bit(activeCellRow, activeCellRow, activeCellCol, activeCellCol),
        };
    }

    public SelectionRecord(RecordInputStream in) {
        field_1_pane            = in.readByte();
        field_2_row_active_cell = in.readUShort();
        field_3_col_active_cell = in.readShort();
        field_4_active_cell_ref_index = in.readShort();
        int field_5_num_refs    = in.readUShort();
        
        field_6_refs = new CellRangeAddress8Bit[field_5_num_refs];
        for (int i = 0; i < field_6_refs.length; i++) {
            field_6_refs[i] = new CellRangeAddress8Bit(in);
        }
    }

    /**
     * set which window pane this is for
     */
    public void setPane(byte pane) {
        field_1_pane = pane;
    }

    /**
     * set the active cell's row
     * @param row number of active cell
     */
    public void setActiveCellRow(int row) {
        field_2_row_active_cell = row;
    }

    /**
     * set the active cell's col
     * @param col number of active cell
     */
    public void setActiveCellCol(short col) {
        field_3_col_active_cell = col;
    }

    /**
     * set the active cell's reference number
     * @param ref number of active cell
     */
    public void setActiveCellRef(short ref) {
        field_4_active_cell_ref_index = ref;
    }

    /**
     * @return the pane ID which window pane this is for
     */
    public byte getPane() {
        return field_1_pane;
    }

    /**
     * get the active cell's row
     * @return row number of active cell
     */
    public int getActiveCellRow() {
        return field_2_row_active_cell;
    }

    /**
     * get the active cell's col
     * @return col number of active cell
     */
    public int getActiveCellCol() {
        return field_3_col_active_cell;
    }

    /**
     * get the active cell's reference number
     * @return ref number of active cell
     */
    public int getActiveCellRef() {
        return (short)field_4_active_cell_ref_index;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SELECTION]\n");
        buffer.append("    .pane            = ")
            .append(Integer.toHexString(getPane())).append("\n");
        buffer.append("    .activecellrow   = ")
            .append(Integer.toHexString(getActiveCellRow())).append("\n");
        buffer.append("    .activecellcol   = ")
            .append(Integer.toHexString(getActiveCellCol())).append("\n");
        buffer.append("    .activecellref   = ")
            .append(Integer.toHexString(getActiveCellRef())).append("\n");
        buffer.append("    .numrefs         = ")
            .append(Integer.toHexString(field_6_refs.length)).append("\n");
        buffer.append("[/SELECTION]\n");
        return buffer.toString();
    }
    protected int getDataSize() {
        return 9 // 1 byte + 4 shorts 
            + CellRangeAddress8Bit.getEncodedSize(field_6_refs.length);
    }
    public int serialize(int offset, byte [] data) {
        int dataSize = getDataSize();
        LittleEndian.putUShort(data, 0 + offset, sid);
        LittleEndian.putUShort(data, 2 + offset, dataSize);
        LittleEndian.putByte(data, 4 + offset,  getPane());
        LittleEndian.putUShort(data, 5 + offset, getActiveCellRow());
        LittleEndian.putUShort(data, 7 + offset, getActiveCellCol());
        LittleEndian.putUShort(data, 9 + offset, getActiveCellRef());
        int nRefs = field_6_refs.length;
        LittleEndian.putUShort(data, 11 + offset, nRefs);
        for (int i = 0; i < field_6_refs.length; i++) {
            CellRangeAddress8Bit r = field_6_refs[i];
            r.serialize(offset + 13 + i * CellRangeAddress8Bit.ENCODED_SIZE, data);
        }
        return 4 + dataSize;
    }

    public int getRecordSize() {
        return 4 + getDataSize();
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
        SelectionRecord rec = new SelectionRecord(field_2_row_active_cell, field_3_col_active_cell);
        rec.field_1_pane = field_1_pane;
        rec.field_4_active_cell_ref_index = field_4_active_cell_ref_index;
        rec.field_6_refs = field_6_refs;
        return rec;
    }
}
