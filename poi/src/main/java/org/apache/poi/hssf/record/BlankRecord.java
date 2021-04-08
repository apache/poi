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
 * Represents a column in a row with no value but with styling.
 *
 * @version 2.0-pre
 */
public final class BlankRecord extends StandardRecord implements CellValueRecordInterface {
    public static final short sid = 0x0201;

    private int field_1_row;
    private short field_2_col;
    private short field_3_xf;

    /** Creates a new instance of BlankRecord */
    public BlankRecord() {}

    public BlankRecord(BlankRecord other) {
        super(other);
        field_1_row = other.field_1_row;
        field_2_col = other.field_2_col;
        field_3_xf  = other.field_3_xf;
    }


    public BlankRecord(RecordInputStream in) {
        field_1_row = in.readUShort();
        field_2_col = in.readShort();
        field_3_xf  = in.readShort();
    }

    /**
     * set the row this cell occurs on
     * @param row the row this cell occurs within
     */
    public void setRow(int row)
    {
        field_1_row = row;
    }

    /**
     * get the row this cell occurs on
     *
     * @return the row
     */
    public int getRow()
    {
        return field_1_row;
    }

    /**
     * get the column this cell defines within the row
     *
     * @return the column
     */
    public short getColumn()
    {
        return field_2_col;
    }

    /**
     * set the index of the extended format record to style this cell with
     *
     * @param xf - the 0-based index of the extended format
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     */
    public void setXFIndex(short xf)
    {
        field_3_xf = xf;
    }

    /**
     * get the index of the extended format record to style this cell with
     *
     * @return extended format index
     */
    public short getXFIndex()
    {
        return field_3_xf;
    }

    /**
     * set the column this cell defines within the row
     *
     * @param col the column this cell defines
     */

    public void setColumn(short col)
    {
        field_2_col = col;
    }

    /**
     * return the non static version of the id for this record.
     */
    public short getSid()
    {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getRow());
        out.writeShort(getColumn());
        out.writeShort(getXFIndex());
    }

    protected int getDataSize() {
        return 6;
    }

    @Override
    public BlankRecord copy() {
        return new BlankRecord(this);
    }

    @Override
    public HSSFRecordTypes getGenericRecordType() {
        return HSSFRecordTypes.BLANK;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "row", this::getRow,
            "col", this::getColumn,
            "xfIndex", this::getXFIndex
        );
    }
}
