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
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Label SST Record<P>
 * Description:  Refers to a string in the shared string table and is a column
 *               value.  <P>
 * REFERENCE:  PG 325 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class LabelSSTRecord extends Record implements CellValueRecordInterface {
    public final static short sid = 0xfd;
    private int field_1_row;
    private int field_2_column;
    private int field_3_xf_index;
    private int field_4_sst_index;

    public LabelSSTRecord()
    {
    }

    public LabelSSTRecord(RecordInputStream in)
    {
        field_1_row       = in.readUShort();
        field_2_column    = in.readUShort();
        field_3_xf_index  = in.readUShort();
        field_4_sst_index = in.readInt();
    }

    public void setRow(int row)
    {
        field_1_row = row;
    }

    public void setColumn(short col)
    {
        field_2_column = col;
    }

    /**
     * set the index to the extended format record
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param index - the index to the XF record
     */

    public void setXFIndex(short index)
    {
        field_3_xf_index = index;
    }

    /**
     * set the index to the string in the SSTRecord
     *
     * @param index - of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public void setSSTIndex(int index)
    {
        field_4_sst_index = index;
    }

    public int getRow()
    {
        return field_1_row;
    }

    public short getColumn()
    {
        return (short)field_2_column;
    }

    /**
     * get the index to the extended format record
     *
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return the index to the XF record
     */

    public short getXFIndex()
    {
        return (short)field_3_xf_index;
    }

    /**
     * get the index to the string in the SSTRecord
     *
     * @return index of string in the SST Table
     * @see org.apache.poi.hssf.record.SSTRecord
     */

    public int getSSTIndex()
    {
        return field_4_sst_index;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("[LABELSST]\n");
        sb.append("    .row     = ").append(HexDump.shortToHex(getRow())).append("\n");
        sb.append("    .column  = ").append(HexDump.shortToHex(getColumn())).append("\n");
        sb.append("    .xfindex = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
        sb.append("    .sstindex= ").append(HexDump.intToHex(getSSTIndex())).append("\n");
        sb.append("[/LABELSST]\n");
        return sb.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putUShort(data, 0 + offset, sid);
        LittleEndian.putUShort(data, 2 + offset, 10);
        LittleEndian.putUShort(data, 4 + offset, getRow());
        LittleEndian.putUShort(data, 6 + offset, getColumn());
        LittleEndian.putUShort(data, 8 + offset, getXFIndex());
        LittleEndian.putInt(data, 10 + offset, getSSTIndex());
        return getRecordSize();
    }

    protected int getDataSize() {
        return 10;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      LabelSSTRecord rec = new LabelSSTRecord();
      rec.field_1_row = field_1_row;
      rec.field_2_column = field_2_column;
      rec.field_3_xf_index = field_3_xf_index;
      rec.field_4_sst_index = field_4_sst_index;
      return rec;
    }
}
