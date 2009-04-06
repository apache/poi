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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Row Record (0x0208)<P/>
 * Description:  stores the row information for the sheet. <P/>
 * REFERENCE:  PG 379 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */
public final class RowRecord extends StandardRecord {
    public final static short sid = 0x0208;

    public static final int ENCODED_SIZE = 20;
    
    private static final int OPTION_BITS_ALWAYS_SET = 0x0100;
    private static final int DEFAULT_HEIGHT_BIT = 0x8000;

    private int field_1_row_number;
    private int field_2_first_col;
    private int field_3_last_col; // plus 1
    private short field_4_height;
    private short field_5_optimize; // hint field for gui, can/should be set to zero

    // for generated sheets.
    private short field_6_reserved;
    /** 16 bit options flags */
    private int field_7_option_flags;
    private static final BitField          outlineLevel  = BitFieldFactory.getInstance(0x07);

    // bit 3 reserved
    private static final BitField          colapsed      = BitFieldFactory.getInstance(0x10);
    private static final BitField          zeroHeight    = BitFieldFactory.getInstance(0x20);
    private static final BitField          badFontHeight = BitFieldFactory.getInstance(0x40);
    private static final BitField          formatted     = BitFieldFactory.getInstance(0x80);
    private short field_8_xf_index;   // only if isFormatted

    public RowRecord(int rowNumber) {
        field_1_row_number = rowNumber;
        field_4_height = (short)0xFF;
        field_5_optimize = ( short ) 0;
        field_6_reserved = ( short ) 0;
        field_7_option_flags = OPTION_BITS_ALWAYS_SET; // seems necessary for outlining

        field_8_xf_index = ( short ) 0xf;
        setEmpty();
    }

    public RowRecord(RecordInputStream in) {
        field_1_row_number   = in.readUShort();
        field_2_first_col    = in.readShort();
        field_3_last_col     = in.readShort();
        field_4_height       = in.readShort();
        field_5_optimize     = in.readShort();
        field_6_reserved     = in.readShort();
        field_7_option_flags = in.readShort();
        field_8_xf_index     = in.readShort();
    }

    /**
     * Updates the firstCol and lastCol fields to the reserved value (-1) 
     * to signify that this row is empty
     */
    public void setEmpty() {
        field_2_first_col = 0;
        field_3_last_col = 0;
    }
    public boolean isEmpty() {
        return (field_2_first_col | field_3_last_col) == 0;
    }
    
    /**
     * set the logical row number for this row (0 based index)
     * @param row - the row number
     */
    public void setRowNumber(int row) {
        field_1_row_number = row;
    }

    /**
     * set the logical col number for the first cell this row (0 based index)
     * @param col - the col number
     */
    public void setFirstCol(int col) {
        field_2_first_col = col;
    }

    /**
     * @param col - one past the zero-based index to the last cell in this row
     */
    public void setLastCol(int col) {
        field_3_last_col = col;
    }

    /**
     * set the height of the row
     * @param height of the row
     */
    public void setHeight(short height) {
        field_4_height = height;
    }

    /**
     * set whether to optimize or not (set to 0)
     * @param optimize (set to 0)
     */
    public void setOptimize(short optimize) {
        field_5_optimize = optimize;
    }

    // option bitfields

    /**
     * set the outline level of this row
     * @param ol - the outline level
     */
    public void setOutlineLevel(short ol) {
        field_7_option_flags = outlineLevel.setValue(field_7_option_flags, ol);
    }

    /**
     * set whether or not to collapse this row
     * @param c - collapse or not
     */
    public void setColapsed(boolean c) {
        field_7_option_flags = colapsed.setBoolean(field_7_option_flags, c);
    }

    /**
     * set whether or not to display this row with 0 height
     * @param z  height is zero or not.
     */
    public void setZeroHeight(boolean z) {
        field_7_option_flags = zeroHeight.setBoolean(field_7_option_flags, z);
    }

    /**
     * set whether the font and row height are not compatible
     * @param  f  true if they aren't compatible (damn not logic)
     */
    public void setBadFontHeight(boolean f) {
        field_7_option_flags = badFontHeight.setBoolean(field_7_option_flags, f);
    }

    /**
     * set whether the row has been formatted (even if its got all blank cells)
     * @param f  formatted or not
     */
    public void setFormatted(boolean f) {
        field_7_option_flags = formatted.setBoolean(field_7_option_flags, f);
    }

    // end bitfields

    /**
     * if the row is formatted then this is the index to the extended format record
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @param index to the XF record
     */
    public void setXFIndex(short index) {
        field_8_xf_index = index;
    }

    /**
     * get the logical row number for this row (0 based index)
     * @return row - the row number
     */
    public int getRowNumber() {
        return field_1_row_number;
    }

    /**
     * get the logical col number for the first cell this row (0 based index)
     * @return col - the col number
     */
    public int getFirstCol() {
        return field_2_first_col;
    }

    /**
     * get the logical col number for the last cell this row (0 based index), plus one 
     * @return col - the last col index + 1
     */
    public int getLastCol() {
        return field_3_last_col;
    }

    /**
     * get the height of the row
     * @return height of the row
     */
    public short getHeight() {
        return field_4_height;
    }

    /**
     * get whether to optimize or not (set to 0)
     * @return optimize (set to 0)
     */
    public short getOptimize() {
        return field_5_optimize;
    }

    /**
     * gets the option bitmask.  (use the individual bit setters that refer to this
     * method)
     * @return options - the bitmask
     */
    public short getOptionFlags() {
        return (short)field_7_option_flags;
    }

    // option bitfields

    /**
     * get the outline level of this row
     * @return ol - the outline level
     * @see #getOptionFlags()
     */
    public short getOutlineLevel() {
        return (short)outlineLevel.getValue(field_7_option_flags);
    }

    /**
     * get whether or not to colapse this row
     * @return c - colapse or not
     * @see #getOptionFlags()
     */
    public boolean getColapsed() {
        return (colapsed.isSet(field_7_option_flags));
    }

    /**
     * get whether or not to display this row with 0 height
     * @return - z height is zero or not.
     * @see #getOptionFlags()
     */
    public boolean getZeroHeight() {
        return zeroHeight.isSet(field_7_option_flags);
    }

    /**
     * get whether the font and row height are not compatible
     * @return - f -true if they aren't compatible (damn not logic)
     * @see #getOptionFlags()
     */
    public boolean getBadFontHeight() {
        return badFontHeight.isSet(field_7_option_flags);
    }

    /**
     * get whether the row has been formatted (even if its got all blank cells)
     * @return formatted or not
     * @see #getOptionFlags()
     */
    public boolean getFormatted() {
        return formatted.isSet(field_7_option_flags);
    }

    // end bitfields

    /**
     * if the row is formatted then this is the index to the extended format record
     * @see org.apache.poi.hssf.record.ExtendedFormatRecord
     * @return index to the XF record or bogus value (undefined) if isn't formatted
     */
    public short getXFIndex() {
        return field_8_xf_index;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[ROW]\n");
        sb.append("    .rownumber      = ").append(Integer.toHexString(getRowNumber()))
                .append("\n");
        sb.append("    .firstcol       = ").append(HexDump.shortToHex(getFirstCol())).append("\n");
        sb.append("    .lastcol        = ").append(HexDump.shortToHex(getLastCol())).append("\n");
        sb.append("    .height         = ").append(HexDump.shortToHex(getHeight())).append("\n");
        sb.append("    .optimize       = ").append(HexDump.shortToHex(getOptimize())).append("\n");
        sb.append("    .reserved       = ").append(HexDump.shortToHex(field_6_reserved)).append("\n");
        sb.append("    .optionflags    = ").append(HexDump.shortToHex(getOptionFlags())).append("\n");
        sb.append("        .outlinelvl = ").append(Integer.toHexString(getOutlineLevel())).append("\n");
        sb.append("        .colapsed   = ").append(getColapsed()).append("\n");
        sb.append("        .zeroheight = ").append(getZeroHeight()).append("\n");
        sb.append("        .badfontheig= ").append(getBadFontHeight()).append("\n");
        sb.append("        .formatted  = ").append(getFormatted()).append("\n");
        sb.append("    .xfindex        = ").append(Integer.toHexString(getXFIndex())).append("\n");
        sb.append("[/ROW]\n");
        return sb.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getRowNumber());
        out.writeShort(getFirstCol() == -1 ? (short)0 : getFirstCol());
        out.writeShort(getLastCol() == -1 ? (short)0 : getLastCol());
        out.writeShort(getHeight());
        out.writeShort(getOptimize());
        out.writeShort(field_6_reserved);
        out.writeShort(getOptionFlags());
        out.writeShort(getXFIndex());
    }

    protected int getDataSize() {
        return ENCODED_SIZE - 4;
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
      RowRecord rec = new RowRecord(field_1_row_number);
      rec.field_2_first_col = field_2_first_col;
      rec.field_3_last_col = field_3_last_col;
      rec.field_4_height = field_4_height;
      rec.field_5_optimize = field_5_optimize;
      rec.field_6_reserved = field_6_reserved;
      rec.field_7_option_flags = field_7_option_flags;
      rec.field_8_xf_index = field_8_xf_index;
      return rec;
    }
}
