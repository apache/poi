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
 * Base class for all cell value records (implementors of {@link CellValueRecordInterface}).
 * Subclasses are expected to manage the cell data values (of various types).
 */
public abstract class CellRecord extends StandardRecord implements CellValueRecordInterface {
    private int _rowIndex;
    private int _columnIndex;
    private int _formatIndex;

    protected CellRecord() {
        // fields uninitialised
    }

    protected CellRecord(RecordInputStream in) {
        _rowIndex = in.readUShort();
        _columnIndex = in.readUShort();
        _formatIndex = in.readUShort();
    }

    @Override
    public final void setRow(int row) {
        _rowIndex = row;
    }

    @Override
    public final void setColumn(short col) {
        _columnIndex = col;
    }

    /**
     * set the index to the ExtendedFormat
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param xf index to the XF record
     */
    @Override
    public final void setXFIndex(short xf) {
        _formatIndex = xf;
    }

    @Override
    public final int getRow() {
        return _rowIndex;
    }

    @Override
    public final short getColumn() {
        return (short) _columnIndex;
    }

    /**
     * get the index to the ExtendedFormat
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record
     */
    @Override
    public final short getXFIndex() {
        return (short) _formatIndex;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        String recordName = getRecordName();

        sb.append("[").append(recordName).append("]\n");
        sb.append("    .row    = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .col    = ").append(HexDump.shortToHex(getColumn())).append("\n");
        sb.append("    .xfindex= ").append(HexDump.shortToHex(getXFIndex())).append("\n");
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
     * @param sb the StringBuilder to write to
     */
    protected abstract void appendValueText(StringBuilder sb);

    /**
     * Gets the debug info BIFF record type name (used by {@link #toString()}.
     * 
     * @return the record type name
     */
    protected abstract String getRecordName();

    /**
     * writes out the value data for this cell record
     * 
     * @param out the output
     */
    protected abstract void serializeValue(LittleEndianOutput out);

    /**
     * @return the size (in bytes) of the value data for this cell record
     */
    protected abstract int getValueDataSize();

    @Override
    public final void serialize(LittleEndianOutput out) {
        out.writeShort(getRow());
        out.writeShort(getColumn());
        out.writeShort(getXFIndex());
        serializeValue(out);
    }

    @Override
    protected final int getDataSize() {
        return 6 + getValueDataSize();
    }

    protected final void copyBaseFields(CellRecord rec) {
        rec._rowIndex = _rowIndex;
        rec._columnIndex = _columnIndex;
        rec._formatIndex = _formatIndex;
    }
}
