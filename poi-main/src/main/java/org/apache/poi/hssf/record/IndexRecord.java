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

import org.apache.poi.util.IntList;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Index Record (0x020B)<p/>
 * Description:  Occurs right after BOF, tells you where the DBCELL records are for a sheet
 *               Important for locating cells<p/>
 * NOT USED IN THIS RELEASE
 * REFERENCE:  PG 323 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<p/>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public class IndexRecord extends StandardRecord {
    public final static short sid = 0x020B;
    private int                field_2_first_row;       // first row on the sheet
    private int                field_3_last_row_add1;   // last row
    private int                field_4_zero;            // supposed to be zero
    private IntList            field_5_dbcells;         // array of offsets to DBCELL records

    public IndexRecord()
    {
    }

    public IndexRecord(RecordInputStream in)
    {
        int field_1_zero          = in.readInt();
        if (field_1_zero != 0) {
        	throw new RecordFormatException("Expected zero for field 1 but got " + field_1_zero);
        }
        field_2_first_row     = in.readInt();
        field_3_last_row_add1 = in.readInt();
        field_4_zero      = in.readInt();
        
        int nCells = in.remaining() / 4;
        field_5_dbcells = new IntList(nCells);
        for(int i=0; i<nCells; i++) {
            field_5_dbcells.add(in.readInt());
        }
    }

    public void setFirstRow(int row)
    {
        field_2_first_row = row;
    }

    public void setLastRowAdd1(int row)
    {
        field_3_last_row_add1 = row;
    }

    public void addDbcell(int cell)
    {
        if (field_5_dbcells == null)
        {
            field_5_dbcells = new IntList();
        }
        field_5_dbcells.add(cell);
    }

    public void setDbcell(int cell, int value)
    {
        field_5_dbcells.set(cell, value);
    }

    public int getFirstRow()
    {
        return field_2_first_row;
    }

    public int getLastRowAdd1()
    {
        return field_3_last_row_add1;
    }

    public int getNumDbcells()
    {
        if (field_5_dbcells == null)
        {
            return 0;
        }
        return field_5_dbcells.size();
    }

    public int getDbcellAt(int cellnum)
    {
        return field_5_dbcells.get(cellnum);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[INDEX]\n");
        buffer.append("    .firstrow       = ")
            .append(Integer.toHexString(getFirstRow())).append("\n");
        buffer.append("    .lastrowadd1    = ")
            .append(Integer.toHexString(getLastRowAdd1())).append("\n");
        for (int k = 0; k < getNumDbcells(); k++) {
            buffer.append("    .dbcell_").append(k).append(" = ")
                .append(Integer.toHexString(getDbcellAt(k))).append("\n");
        }
        buffer.append("[/INDEX]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {

        out.writeInt(0);
        out.writeInt(getFirstRow());
        out.writeInt(getLastRowAdd1());
        out.writeInt(field_4_zero);
        for (int k = 0; k < getNumDbcells(); k++) {
        	out.writeInt(getDbcellAt(k));
        }
    }

    protected int getDataSize() {
        return 16 // 4 ints
        	+ getNumDbcells() * 4;
    }
    
    /** 
     * @return the size of an INdexRecord when it needs to index the specified number of blocks
     */
    public static int getRecordSizeForBlockCount(int blockCount) {
        return 20 + 4 * blockCount;
    }  

    public short getSid() {
        return sid;
    }

    public Object clone() {
      IndexRecord rec = new IndexRecord();
      rec.field_2_first_row = field_2_first_row;
      rec.field_3_last_row_add1 = field_3_last_row_add1;
      rec.field_4_zero = field_4_zero;
      rec.field_5_dbcells = new IntList();
      rec.field_5_dbcells.addAll(field_5_dbcells);
      return rec;
    }
}
