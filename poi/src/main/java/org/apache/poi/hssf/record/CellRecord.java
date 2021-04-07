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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Base class for all cell value records (implementors of {@link CellValueRecordInterface}).
 * Subclasses are expected to manage the cell data values (of various types).
 */
public abstract class CellRecord extends StandardRecord implements CellValueRecordInterface {
    private int _rowIndex;
    private int _columnIndex;
    private int _formatIndex;

    protected CellRecord() {}

    protected CellRecord(CellRecord other) {
        super(other);
        _rowIndex = other.getRow();
        _columnIndex = other.getColumn();
        _formatIndex = other.getXFIndex();
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

    @Override
    public abstract CellRecord copy();

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "col", this::getColumn,
            "xfIndex", this::getXFIndex
        );
    }
}
