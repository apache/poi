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

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Base class for all old (Biff 2 - Biff 4) cell value records
 *  (implementors of {@link CellValueRecordInterface}).
 * Subclasses are expected to manage the cell data values (of various types).
 */
public abstract class OldCellRecord implements GenericRecord {
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
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "column", this::getColumn,
            "biff2", this::isBiff2,
            "biff2CellAttrs", this::getCellAttrs,
            "xfIndex", this::getXFIndex
        );
    }

    @Override
    public final String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }
}
