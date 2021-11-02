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
 * Represents a set of columns in a row with no value but with styling.
 *
 * @see BlankRecord
 */
public final class MulBlankRecord extends StandardRecord {
    public static final short sid = 0x00BE;

    private final int _row;
    private final int _firstCol;
    private final short[] _xfs;
    private final int _lastCol;

    public MulBlankRecord(int row, int firstCol, short[] xfs) {
        _row = row;
        _firstCol = firstCol;
        _xfs = xfs;
        _lastCol = firstCol + xfs.length - 1;
    }

    /**
     * @return the row number of the cells this represents
     */
    public int getRow() {
        return _row;
    }

    /**
     * @return starting column (first cell this holds in the row). Zero based
     */
    public int getFirstColumn() {
        return _firstCol;
    }

    /**
     * @return ending column (last cell this holds in the row). Zero based
     */
    public int getLastColumn() {
        return _lastCol;
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */
    public int getNumColumns() {
        return _lastCol - _firstCol + 1;
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @param coffset  the column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */
    public short getXFAt(int coffset) {
        return _xfs[coffset];
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public MulBlankRecord(RecordInputStream in) {
        _row       = in.readUShort();
        _firstCol = in.readShort();
        _xfs       = parseXFs(in);
        _lastCol  = in.readShort();
    }

    private static short [] parseXFs(RecordInputStream in) {
        short[] retval = new short[(in.remaining() - 2) / 2];

        for (int idx = 0; idx < retval.length;idx++) {
          retval[idx] = in.readShort();
        }
        return retval;
    }

    public short getSid() {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_row);
        out.writeShort(_firstCol);
        for (short xf : _xfs) {
            out.writeShort(xf);
        }
        out.writeShort(_lastCol);
    }

    protected int getDataSize() {
        // 3 short fields + array of shorts
        return 6 + _xfs.length * 2;
    }

    @Override
    public MulBlankRecord copy() {
        // immutable - so OK to return this
        return this;
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.MUL_BLANK;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "firstColumn", this::getFirstColumn,
            "lastColumn", this::getLastColumn,
            "xf", () -> _xfs
        );
    }
}
