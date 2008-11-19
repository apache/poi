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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Multiple Blank cell record(0x00BE) <P/>
 * Description:  Represents a  set of columns in a row with no value but with styling.
 *               In this release we have read-only support for this record type.
 *               The RecordFactory converts this to a set of BlankRecord objects.<P/>
 * REFERENCE:  PG 329 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P/>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Glen Stampoultzis (glens at apache.org)
 * @see BlankRecord
 */
public final class MulBlankRecord extends StandardRecord {
    public final static short sid = 0x00BE;
    
    private int               field_1_row;
    private short             field_2_first_col;
    private short[]           field_3_xfs;
    private short             field_4_last_col;


    /**
     * get the row number of the cells this represents
     *
     * @return row number
     */
    public int getRow()
    {
        return field_1_row;
    }

    /**
     * starting column (first cell this holds in the row)
     * @return first column number
     */
    public short getFirstColumn()
    {
        return field_2_first_col;
    }

    /**
     * ending column (last cell this holds in the row)
     * @return first column number
     */
    public short getLastColumn()
    {
        return field_4_last_col;
    }

    /**
     * get the number of columns this contains (last-first +1)
     * @return number of columns (last - first +1)
     */
    public int getNumColumns()
    {
        return field_4_last_col - field_2_first_col + 1;
    }

    /**
     * returns the xf index for column (coffset = column - field_2_first_col)
     * @param coffset  the column (coffset = column - field_2_first_col)
     * @return the XF index for the column
     */
    public short getXFAt(int coffset)
    {
        return field_3_xfs[ coffset ];
    }

    /**
     * @param in the RecordInputstream to read the record from
     */
    public MulBlankRecord(RecordInputStream in) {
        field_1_row       = in.readUShort();
        field_2_first_col = in.readShort();
        field_3_xfs       = parseXFs(in);
        field_4_last_col  = in.readShort();
    }

    private static short [] parseXFs(RecordInputStream in)
    {
        short[] retval = new short[ (in.remaining() - 2) / 2 ];

        for (int idx = 0; idx < retval.length;idx++)
        {
          retval[idx] = in.readShort();
        }
        return retval;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[MULBLANK]\n");
        buffer.append("row  = ").append(Integer.toHexString(getRow())).append("\n");
        buffer.append("firstcol  = ").append(Integer.toHexString(getFirstColumn())).append("\n");
        buffer.append(" lastcol  = ").append(Integer.toHexString(getLastColumn())).append("\n");
        for (int k = 0; k < getNumColumns(); k++) {
            buffer.append("xf").append(k).append("        = ").append(
                    Integer.toHexString(getXFAt(k))).append("\n");
        }
        buffer.append("[/MULBLANK]\n");
        return buffer.toString();
    }

    public short getSid()
    {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        throw new RecordFormatException( "Sorry, you can't serialize MulBlank in this release");
    }
    protected int getDataSize() {
        throw new RecordFormatException( "Sorry, you can't serialize MulBlank in this release");
    }
}
