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
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;

/**
 * Title: COLINFO Record<p/>
 * Description:  Defines with width and formatting for a range of columns<p/>
 * REFERENCE:  PG 293 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<p/>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */
public final class ColumnInfoRecord extends StandardRecord {
    public static final short     sid = 0x7d;
    private int field_1_first_col;
    private int field_2_last_col;
    private int field_3_col_width;
    private int field_4_xf_index;
    private int field_5_options;
    private static final BitField hidden    = BitFieldFactory.getInstance(0x01);
    private static final BitField outlevel  = BitFieldFactory.getInstance(0x0700);
    private static final BitField collapsed = BitFieldFactory.getInstance(0x1000);
    // Excel seems write values 2, 10, and 260, even though spec says "must be zero"
    private int                 field_6_reserved;

    /**
     * Creates a column info record with default width and format
     */
    public ColumnInfoRecord() {
        setColumnWidth(2275);
        field_5_options = 2; 
        field_4_xf_index = 0x0f;
        field_6_reserved = 2; // seems to be the most common value
    }

    public ColumnInfoRecord(RecordInputStream in)
    {
        field_1_first_col = in.readUShort();
        field_2_last_col  = in.readUShort();
        field_3_col_width = in.readUShort();
        field_4_xf_index  = in.readUShort();
        field_5_options   = in.readUShort();
        switch(in.remaining()) {
            case 2: // usual case
                field_6_reserved  = in.readUShort();
                break;
            case 1:
                // often COLINFO gets encoded 1 byte short
                // shouldn't matter because this field is unused
                field_6_reserved  = in.readByte(); 
                break;
            default:
                throw new RuntimeException("Unusual record size remaining=(" + in.remaining() + ")");
        }
    }

    /**
     * set the first column this record defines formatting info for
     * @param fc - the first column index (0-based)
     */

    public void setFirstColumn(int fc)
    {
        field_1_first_col = fc;
    }

    /**
     * set the last column this record defines formatting info for
     * @param lc - the last column index (0-based)
     */

    public void setLastColumn(int lc)
    {
        field_2_last_col = lc;
    }

    /**
     * set the columns' width in 1/256 of a character width
     * @param cw - column width
     */

    public void setColumnWidth(int cw)
    {
        field_3_col_width = cw;
    }

    /**
     * set the columns' default format info
     * @param xfi - the extended format index
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     */

    public void setXFIndex(int xfi)
    {
        field_4_xf_index = xfi;
    }


    // start options bitfield

    /**
     * set whether or not these cells are hidden
     * @param ishidden - whether the cells are hidden.
     * @see #setOptions(int)
     */

    public void setHidden(boolean ishidden)
    {
        field_5_options = hidden.setBoolean(field_5_options, ishidden);
    }

    /**
     * set the outline level for the cells
     * @see #setOptions(int)
     * @param olevel -outline level for the cells
     */

    public void setOutlineLevel(int olevel)
    {
        field_5_options = outlevel.setValue(field_5_options, olevel);
    }

    /**
     * set whether the cells are collapsed
     * @param iscollapsed - wether the cells are collapsed
     * @see #setOptions(int)
     */

    public void setCollapsed(boolean iscollapsed)
    {
        field_5_options = collapsed.setBoolean(field_5_options,
                                                    iscollapsed);
    }

    // end options bitfield

    /**
     * get the first column this record defines formatting info for
     * @return the first column index (0-based)
     */

    public int getFirstColumn()
    {
        return field_1_first_col;
    }

    /**
     * get the last column this record defines formatting info for
     * @return the last column index (0-based)
     */

    public int getLastColumn()
    {
        return field_2_last_col;
    }

    /**
     * get the columns' width in 1/256 of a character width
     * @return column width
     */

    public int getColumnWidth()
    {
        return field_3_col_width;
    }

    /**
     * get the columns' default format info
     * @return the extended format index
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     */

    public int getXFIndex()
    {
        return field_4_xf_index;
    }

    public int getOptions() {
        return field_5_options;
    }
    public void setOptions(int field_5_options) {
        this.field_5_options = field_5_options;
    }
    
    // start options bitfield

    /**
     * get whether or not these cells are hidden
     * @return whether the cells are hidden.
     * @see #setOptions(int)
     */

    public boolean getHidden()
    {
        return hidden.isSet(field_5_options);
    }

    /**
     * get the outline level for the cells
     * @see #setOptions(int)
     * @return outline level for the cells
     */

    public int getOutlineLevel()
    {
        return outlevel.getValue(field_5_options);
    }

    /**
     * get whether the cells are collapsed
     * @return wether the cells are collapsed
     * @see #setOptions(int)
     */

    public boolean getCollapsed()
    {
        return collapsed.isSet(field_5_options);
    }

    // end options bitfield
    
    public boolean containsColumn(int columnIndex) {
        return field_1_first_col <= columnIndex && columnIndex <= field_2_last_col; 
    }
    public boolean isAdjacentBefore(ColumnInfoRecord other) {
        return field_2_last_col == other.field_1_first_col - 1;
    }
   
    /**
     * @return <code>true</code> if the format, options and column width match
     */
    public boolean formatMatches(ColumnInfoRecord other) {
        if (field_4_xf_index != other.field_4_xf_index) {
            return false;
        }
        if (field_5_options != other.field_5_options) {
            return false;
        }
        if (field_3_col_width != other.field_3_col_width) {
            return false;
        }
        return true;
    }
    
    
    public short getSid()
    {
        return sid;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getFirstColumn());
        out.writeShort(getLastColumn());
        out.writeShort(getColumnWidth());
        out.writeShort(getXFIndex());
        out.writeShort(field_5_options);
        out.writeShort(field_6_reserved);
    }

    protected int getDataSize() {
        return 12;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("[COLINFO]\n");
        sb.append("  colfirst = ").append(getFirstColumn()).append("\n");
        sb.append("  collast  = ").append(getLastColumn()).append("\n");
        sb.append("  colwidth = ").append(getColumnWidth()).append("\n");
        sb.append("  xfindex  = ").append(getXFIndex()).append("\n");
        sb.append("  options  = ").append(HexDump.shortToHex(field_5_options)).append("\n");
        sb.append("    hidden   = ").append(getHidden()).append("\n");
        sb.append("    olevel   = ").append(getOutlineLevel()).append("\n");
        sb.append("    collapsed= ").append(getCollapsed()).append("\n");
        sb.append("[/COLINFO]\n");
        return sb.toString();
    }

    public Object clone() {
        ColumnInfoRecord rec = new ColumnInfoRecord();
        rec.field_1_first_col = field_1_first_col;
        rec.field_2_last_col = field_2_last_col;
        rec.field_3_col_width = field_3_col_width;
        rec.field_4_xf_index = field_4_xf_index;
        rec.field_5_options = field_5_options;
        rec.field_6_reserved = field_6_reserved;
        return rec;
    }
}
