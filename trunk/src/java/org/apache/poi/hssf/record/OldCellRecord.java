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

/**
 * Base class for all old (Biff 2 - Biff 4) cell value records 
 *  (implementors of {@link CellValueRecordInterface}).
 * Subclasses are expected to manage the cell data values (of various types).
 */
public abstract class OldCellRecord {
    private final short    sid;
    private final boolean  isBiff2;   
    private final int      field_1_row;
    private final short    field_2_column;
    private int      field_3_cell_attrs; // Biff 2
    private short    field_3_xf_index;   // Biff 3+

    protected OldCellRecord(RecordInputStream in, boolean isBiff2) {
        this.sid = in.getSid();
        this.isBiff2 = isBiff2;
        field_1_row  = in.readUShort();
        field_2_column = in.readShort();
        
        if (isBiff2) {
            field_3_cell_attrs = in.readUShort() << 8;
            field_3_cell_attrs += in.readUByte();
        } else {
            field_3_xf_index     = in.readShort();
        }
    }

    public final int getRow() {
        return field_1_row;
    }

    public final short getColumn() {
        return field_2_column;
    }

    /**
     * get the index to the ExtendedFormat, for non-Biff2
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record
     */
    public final short getXFIndex() {
        return field_3_xf_index;
    }
    
    public int getCellAttrs()
    {
        return field_3_cell_attrs;
    }

    /**
     * Is this a Biff2 record, or newer?
     * 
     * @return true, if this is a Biff2 record or newer
     */
    public boolean isBiff2() {
        return isBiff2;
    }
    
    public short getSid() {
        return sid;
    }
    
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        String recordName = getRecordName();

        sb.append("[").append(recordName).append("]\n");
        sb.append("    .row    = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .col    = ").append(HexDump.shortToHex(getColumn())).append("\n");
        if (isBiff2()) {
            sb.append("    .cellattrs = ").append(HexDump.shortToHex(getCellAttrs())).append("\n");
        } else {
            sb.append("    .xfindex   = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
        }
        appendValueText(sb);
        sb.append("\n");
        sb.append("[/").append(recordName).append("]\n");
        return sb.toString();
    }
    
    /**
     * Append specific debug info (used by {@link #toString()} for the value
     * contained in this record. Trailing new-line should not be appended
     * (superclass does that).
     * 
     * @param sb the StringBuilder to append to
     */
    protected abstract void appendValueText(StringBuilder sb);

    /**
     * Gets the debug info BIFF record type name (used by {@link #toString()}.
     * 
     * @return the debug info BIFF record type name
     */
    protected abstract String getRecordName();
}
