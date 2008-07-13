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
        

/**
 * TableRecord - The record specifies a data table.
 * This record is preceded by a single Formula record that
 *  defines the first cell in the data table, which should
 *  only contain a single Ptg, {@link TblPtg}.
 * 
 * See p536 of the June 08 binary docs
 */
package org.apache.poi.hssf.record;

import org.apache.poi.hssf.record.formula.TblPtg;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;

public class TableRecord extends Record {
    public static final short sid = 566;
    private short field_1_ref_rowFirst;
    private short field_2_ref_rowLast;
    private short field_3_ref_colFirst;
    private short field_4_ref_colLast;
    
    private byte field_5_flags;
    private byte field_6_res;
    private short field_7_rowInputRow;
    private short field_8_colInputRow;
    private short field_9_rowInputCol;
    private short field_10_colInputCol;
    
    private BitField alwaysCalc      = BitFieldFactory.getInstance(0x0001);
    private BitField reserved1       = BitFieldFactory.getInstance(0x0002);
    private BitField rowOrColInpCell = BitFieldFactory.getInstance(0x0004);
    private BitField oneOrTwoVar     = BitFieldFactory.getInstance(0x0008);
    private BitField rowDeleted      = BitFieldFactory.getInstance(0x0010);
    private BitField colDeleted      = BitFieldFactory.getInstance(0x0020);
    private BitField reserved2       = BitFieldFactory.getInstance(0x0040);
    private BitField reserved3       = BitFieldFactory.getInstance(0x0080);

	protected void fillFields(RecordInputStream in) {
		field_1_ref_rowFirst = in.readShort();
		field_2_ref_rowLast  = in.readShort();
		field_3_ref_colFirst = in.readUByte();
		field_4_ref_colLast  = in.readUByte();
		field_5_flags        = in.readByte();
		field_6_res          = in.readByte();
		field_7_rowInputRow  = in.readShort();
		field_8_colInputRow  = in.readShort();
		field_9_rowInputCol  = in.readShort();
		field_10_colInputCol = in.readShort();
	}
	
    public TableRecord(RecordInputStream in) {
        super(in);
    }
    public TableRecord() {
    	super();
    }
    

	public short getRowFirst() {
		return field_1_ref_rowFirst;
	}
	public void setRowFirst(short field_1_ref_rowFirst) {
		this.field_1_ref_rowFirst = field_1_ref_rowFirst;
	}

	public short getRowLast() {
		return field_2_ref_rowLast;
	}
	public void setRowLast(short field_2_ref_rowLast) {
		this.field_2_ref_rowLast = field_2_ref_rowLast;
	}

	public short getColFirst() {
		return field_3_ref_colFirst;
	}
	public void setColFirst(short field_3_ref_colFirst) {
		this.field_3_ref_colFirst = field_3_ref_colFirst;
	}

	public short getColLast() {
		return field_4_ref_colLast;
	}
	public void setColLast(short field_4_ref_colLast) {
		this.field_4_ref_colLast = field_4_ref_colLast;
	}

	public byte getFlags() {
		return field_5_flags;
	}
	public void setFlags(byte field_5_flags) {
		this.field_5_flags = field_5_flags;
	}

	public byte getReserved() {
		return field_6_res;
	}
	public void setReserved(byte field_6_res) {
		this.field_6_res = field_6_res;
	}

	public short getRowInputRow() {
		return field_7_rowInputRow;
	}
	public void setRowInputRow(short field_7_rowInputRow) {
		this.field_7_rowInputRow = field_7_rowInputRow;
	}

	public short getColInputRow() {
		return field_8_colInputRow;
	}
	public void setColInputRow(short field_8_colInputRow) {
		this.field_8_colInputRow = field_8_colInputRow;
	}

	public short getRowInputCol() {
		return field_9_rowInputCol;
	}
	public void setRowInputCol(short field_9_rowInputCol) {
		this.field_9_rowInputCol = field_9_rowInputCol;
	}

	public short getColInputCol() {
		return field_10_colInputCol;
	}
	public void setColInputCol(short field_10_colInputCol) {
		this.field_10_colInputCol = field_10_colInputCol;
	}
	
	
	public boolean isAlwaysCalc() {
		return alwaysCalc.isSet(field_5_flags);
	}
	public void setAlwaysCalc(boolean flag) {
		field_5_flags = alwaysCalc.setByteBoolean(field_5_flags, flag);
	}
	
	public boolean isRowOrColInpCell() {
		return rowOrColInpCell.isSet(field_5_flags);
	}
	public void setRowOrColInpCell(boolean flag) {
		field_5_flags = rowOrColInpCell.setByteBoolean(field_5_flags, flag);
	}
	
	public boolean isOneNotTwoVar() {
		return oneOrTwoVar.isSet(field_5_flags);
	}
	public void setOneNotTwoVar(boolean flag) {
		field_5_flags = oneOrTwoVar.setByteBoolean(field_5_flags, flag);
	}
	
	public boolean isColDeleted() {
		return colDeleted.isSet(field_5_flags);
	}
	public void setColDeleted(boolean flag) {
		field_5_flags = colDeleted.setByteBoolean(field_5_flags, flag);
	}
	
	public boolean isRowDeleted() {
		return rowDeleted.isSet(field_5_flags);
	}
	public void setRowDeleted(boolean flag) {
		field_5_flags = rowDeleted.setByteBoolean(field_5_flags, flag);
	}

	
	public short getSid() {
		return sid;
	}

	public int serialize(int offset, byte[] data) {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (16));
        
        LittleEndian.putShort(data, 4 + offset, field_1_ref_rowFirst);
        LittleEndian.putShort(data, 6 + offset, field_2_ref_rowLast);
        LittleEndian.putByte(data, 8 + offset, field_3_ref_colFirst);
        LittleEndian.putByte(data, 9 + offset, field_4_ref_colLast);
        LittleEndian.putByte(data, 10 + offset, field_5_flags);
        LittleEndian.putByte(data, 11 + offset, field_6_res);
        LittleEndian.putShort(data, 12 + offset, field_7_rowInputRow);
        LittleEndian.putShort(data, 14 + offset, field_8_colInputRow);
        LittleEndian.putShort(data, 16 + offset, field_9_rowInputCol);
        LittleEndian.putShort(data, 18 + offset, field_10_colInputCol);
        
        return getRecordSize();
	}
	public int getRecordSize() {
		return 4+16;
	}
	
	protected void validateSid(short id) {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A TABLE RECORD");
        }
	}
	
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[TABLE]\n");
        buffer.append("    .row from      = ")
             .append(Integer.toHexString(field_1_ref_rowFirst)).append("\n");
        buffer.append("    .row to        = ")
            .append(Integer.toHexString(field_2_ref_rowLast)).append("\n");
        buffer.append("    .column from   = ")
            .append(Integer.toHexString(field_3_ref_colFirst)).append("\n");
        buffer.append("    .column to     = ")
            .append(Integer.toHexString(field_4_ref_colLast)).append("\n");
        
        buffer.append("    .flags         = ")
            .append(Integer.toHexString(field_5_flags)).append("\n");
        buffer.append("        .always calc     =")
            .append(isAlwaysCalc()).append("\n");
        
        buffer.append("    .reserved      = ")
            .append(Integer.toHexString(field_6_res)).append("\n");
        buffer.append("    .row input row = ")
            .append(Integer.toHexString(field_7_rowInputRow)).append("\n");
        buffer.append("    .col input row = ")
            .append(Integer.toHexString(field_8_colInputRow)).append("\n");
        buffer.append("    .row input col = ")
            .append(Integer.toHexString(field_9_rowInputCol)).append("\n");
        buffer.append("    .col input col = ")
            .append(Integer.toHexString(field_10_colInputCol)).append("\n");
        buffer.append("[/TABLE]\n");
        return buffer.toString();
    }
}
