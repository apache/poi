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
import org.apache.poi.hssf.record.Record;

/**
 * NUMBER (0x0203) Contains a numeric cell value. <P>
 * REFERENCE:  PG 334 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class NumberRecord extends StandardRecord implements CellValueRecordInterface {
    public static final short sid = 0x0203;
    private int field_1_row;
    private int field_2_col;
    private int field_3_xf;
    private double field_4_value;

    /** Creates new NumberRecord */
    public NumberRecord()
    {
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public NumberRecord(RecordInputStream in)
    {
        field_1_row   = in.readUShort();
        field_2_col   = in.readUShort();
        field_3_xf    = in.readUShort();
        field_4_value = in.readDouble();
    }

    public void setRow(int row)
    {
        field_1_row = row;
    }

    public void setColumn(short col)
    {
        field_2_col = col;
    }

    /**
     * set the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param xf  index to the XF record
     */
    public void setXFIndex(short xf)
    {
        field_3_xf = xf;
    }

    /**
     * set the value for the cell
     *
     * @param value  double representing the value
     */
    public void setValue(double value)
    {
        field_4_value = value;
    }

    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return (short)field_2_col;
    }

    /**
     * get the index to the ExtendedFormat
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record
     */
    public short getXFIndex()
    {
        return (short)field_3_xf;
    }

    /**
     * get the value for the cell
     *
     * @return double representing the value
     */
    public double getValue()
    {
        return field_4_value;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("[NUMBER]\n");
        sb.append("    .row    = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .col    = ").append(HexDump.shortToHex(getColumn())).append("\n");
        sb.append("    .xfindex= ").append(HexDump.shortToHex(getXFIndex())).append("\n");
        sb.append("    .value  = ").append(getValue()).append("\n");
        sb.append("[/NUMBER]\n");
        return sb.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getRow());
        out.writeShort(getColumn());
        out.writeShort(getXFIndex());
        out.writeDouble(getValue());
    }

    protected int getDataSize() {
        return 14;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      NumberRecord rec = new NumberRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_col = field_2_col;
      rec.field_3_xf = field_3_xf;
      rec.field_4_value = field_4_value;
      return rec;
    }
}
